package Ekart_Shoppping.service.implimentation;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import Ekart_Shoppping.dto.Customer;
import Ekart_Shoppping.dto.Item;
import Ekart_Shoppping.dto.Payment;
import Ekart_Shoppping.dto.Product;
import Ekart_Shoppping.dto.ShoppingOrder;
import Ekart_Shoppping.dto.Shoppingcart;
import Ekart_Shoppping.dto.Wishlist;
import Ekart_Shoppping.helper.JwtUtil;
import Ekart_Shoppping.helper.ResponseStructure;
import Ekart_Shoppping.helper.SendMail;
import Ekart_Shoppping.repository.CustomerRepository;
import Ekart_Shoppping.repository.PaymentRepository;
import Ekart_Shoppping.repository.ProductRepository;
import Ekart_Shoppping.repository.ShopingRespository;
import Ekart_Shoppping.repository.ShoppingOrerRespository;
import Ekart_Shoppping.repository.WishListReposirory;
import Ekart_Shoppping.service.CustomerServicce;
import jakarta.servlet.http.HttpSession;

@Service
public class Customer_implimenation implements CustomerServicce {

	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	SendMail mail;

	@Autowired
	ProductRepository productRepository;

	@Autowired
	Item item;

	@Autowired
	Shoppingcart shoppingCart;

	@Autowired
	WishListReposirory wishlistRepository;

	@Autowired
	PaymentRepository paymentRepository;

	@Autowired
	ShopingRespository cartRespository;

	@Autowired
	BCryptPasswordEncoder encoder;

	@Autowired
	JwtUtil jwtUtil;

	@Autowired
	ShoppingOrerRespository shoppingOrderRepository;

	@Override
	public ResponseEntity<ResponseStructure<Customer>> signup(Customer customer, String date) {

		ResponseStructure<Customer> structure = new ResponseStructure<>();
		customer.setDob(LocalDate.parse(date));
		customer.setPassword(encoder.encode(customer.getPassword()));
		if (customerRepository.findByEmail(customer.getEmail()) != null
				|| customerRepository.findByMobile(customer.getMobile()) != null) {

			structure.setData(null);
			structure.setMessage("email and mobile shpuld be not to repeated");
			structure.setStatus_code(HttpStatus.ALREADY_REPORTED.value());
			return new ResponseEntity<>(structure, HttpStatus.ALREADY_REPORTED);
		}

		String token = jwtUtil.generateJwtTokenForCustomer(customer, Duration.ofMinutes(5));

		customer.setToken(token);

		// logic for sending the mail
		if (mail.sendLink(customer)) {
			customerRepository.save(customer);

			structure.setData(customer);
			structure.setMessage("Verification Link send to Email Succesfull");
			structure.setStatus_code(HttpStatus.CREATED.value());
			return new ResponseEntity<>(structure, HttpStatus.CREATED);
		} else {
			structure.setData(null);
			structure.setMessage("Something Went Worng");
			structure.setStatus_code(HttpStatus.BAD_GATEWAY.value());
			return new ResponseEntity<>(structure, HttpStatus.BAD_GATEWAY);
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<Customer>> verify_link(String email, String token) {
		ResponseStructure<Customer> structure = new ResponseStructure<>();
		Customer customer = customerRepository.findByEmail(email);

		if (customer.getToken().equals(token)) {
			customer.setStatus(true);
			customer.setToken(null);
			customerRepository.save(customer);
			structure.setData(customer);
			structure.setMessage("Account Created Succesfuully");
			structure.setStatus_code(HttpStatus.CREATED.value());
			return new ResponseEntity<>(structure, HttpStatus.CREATED);
		} else {
			structure.setData(null);
			structure.setMessage("Incorrect link");
			structure.setStatus_code(HttpStatus.NOT_ACCEPTABLE.value());
			return new ResponseEntity<>(structure, HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<Customer>> login(String email, String password, HttpSession session) {
		ResponseStructure<Customer> structure = new ResponseStructure<>();
		Customer customer = customerRepository.findByEmail(email);
		if (customer == null) {
			structure.setData(null);
			structure.setMessage("Incorrect Email");
			structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
			return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
		} else {
			if (encoder.matches(password, customer.getPassword())) {
				if (customer.isStatus()) {
					session.setAttribute("customer", customer);

					structure.setData(customer);
					structure.setMessage("Login Success");
					structure.setStatus_code(HttpStatus.CREATED.value());
					return new ResponseEntity<>(structure, HttpStatus.CREATED);
				} else {
					structure.setData(null);
					structure.setMessage("Mail verification Pending, Click on Forgot password and verify otp");
					structure.setStatus_code(HttpStatus.BAD_REQUEST.value());
					return new ResponseEntity<>(structure, HttpStatus.BAD_REQUEST);
				}
			} else {
				structure.setData(null);
				structure.setMessage("Incorrect Password");
				structure.setStatus_code(HttpStatus.BAD_REQUEST.value());
				return new ResponseEntity<>(structure, HttpStatus.BAD_REQUEST);
			}
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<Customer>> forgotLink(String email) {
		ResponseStructure<Customer> structure = new ResponseStructure<>();
		Customer customer = customerRepository.findByEmail(email);
		if (customer == null) {
			structure.setData(null);
			structure.setMessage("Incorrect Email");
			structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
			return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
		} else {
			// String token = "EKART" + new Random().nextInt(10000, 999999);
			String token = jwtUtil.generateJwtTokenForCustomer(customer, Duration.ofMinutes(5));
			customer.setToken(token);
			customerRepository.save(customer);
			// logic for sending the mail
			if (mail.sendResetLink(customer)) {
				Customer customer2 = customerRepository.save(customer);
				structure.setData(customer);
				structure.setMessage("Link send succesfull cheack once");
				structure.setStatus_code(HttpStatus.CREATED.value());
				return new ResponseEntity<>(structure, HttpStatus.CREATED);
			} else {
				structure.setData(null);
				structure.setMessage("check ur network once ,network error");
				structure.setStatus_code(HttpStatus.NOT_FOUND.value());
				return new ResponseEntity<>(structure, HttpStatus.NOT_FOUND);
			}
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<Customer>> resetPassword(String email, String token) {
		Customer customer = customerRepository.findByEmail(email);
		ResponseStructure<Customer> structure = new ResponseStructure<>();

		if (jwtUtil.isTokenExpired(token)) {
			structure.setData(null);
			structure.setMessage("Token has expired");
			structure.setStatus_code(HttpStatus.BAD_REQUEST.value());
			return new ResponseEntity<>(structure, HttpStatus.BAD_REQUEST);
		}

		if (customer.getToken().equals(token)) {
			customer.setStatus(true);
			customer.setToken(null);
			Customer updatedCustomer = customerRepository.save(customer);

			structure.setData(updatedCustomer);
			structure.setMessage("Verified Successfully");
			structure.setStatus_code(HttpStatus.OK.value());
			return new ResponseEntity<>(structure, HttpStatus.OK);
		} else {
			structure.setData(null);
			structure.setMessage("Not Verified");
			structure.setStatus_code(HttpStatus.BAD_REQUEST.value());
			return new ResponseEntity<>(structure, HttpStatus.BAD_REQUEST);
		}

	}

	@Override
	public ResponseEntity<ResponseStructure<Customer>> setpassword(String email, String password) {
		Customer customer = customerRepository.findByEmail(email);
		ResponseStructure<Customer> structure = new ResponseStructure<>();
		customer.setPassword(encoder.encode(password));
		customerRepository.save(customer);
		structure.setData(customer);
		structure.setMessage("Password Set Success");
		structure.setStatus_code(HttpStatus.OK.value());
		return new ResponseEntity<>(structure, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<ResponseStructure<List<Product>>> view_products(HttpSession session) {
		ResponseStructure<List<Product>> structure = new ResponseStructure<>();
		List<Product> products = productRepository.findByStatus(true);
		if (session.getAttribute("customer") == null) {
			structure.setData(null);
			structure.setMessage("Logain Again");
			structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
			return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
		} else {
			if (products.isEmpty()) {
				structure.setData(null);
				structure.setMessage("No Products Present");
				structure.setStatus_code(HttpStatus.NOT_FOUND.value());
				return new ResponseEntity<>(structure, HttpStatus.NOT_FOUND);
			} else {
				structure.setData(products);
				structure.setMessage("Products");
				structure.setStatus_code(HttpStatus.FOUND.value());
				return new ResponseEntity<>(structure, HttpStatus.FOUND);
			}
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<Product>> addCart(HttpSession session, int id) {
		Customer customer = (Customer) session.getAttribute("customer");
		ResponseStructure<Product> structure = new ResponseStructure<>();
		if (session.getAttribute("customer") == null) {
			structure.setData(null);
			structure.setMessage("Logain Again");
			structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
			return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
		} else {
			Product product = productRepository.findById(id).orElse(null);
			if (product.getStock() >= 1) {

				Shoppingcart cart = customer.getShoppingCart();
				if (cart == null) {
					cart = this.shoppingCart;
				}
				List<Item> items = cart.getItems();
				if (items == null) {
					items = new ArrayList<>();
				}

				if (items.isEmpty()) {
					item.setDescription(product.getDescription());
					item.setImage(product.getImage());
					item.setName(product.getName());
					item.setPrice(product.getPrice());
					item.setQuantity(1);
					items.add(item);
				} else {
					boolean flag = false;
					for (Item item : items) {
						if (item.getName().equals(product.getName())) {
							item.setQuantity(item.getQuantity() + 1);
							item.setPrice(item.getPrice() + product.getPrice());
							item.setDescription(product.getDescription());
							item.setImage(product.getImage());
							flag = false;
							break;
						} else {
							flag = true;
						}
					}
					if (flag) {
						item.setDescription(product.getDescription());
						item.setImage(product.getImage());
						item.setName(product.getName());
						item.setPrice(product.getPrice());
						item.setQuantity(1);
						items.add(item);
					}
				}
				cart.setItems(items);
				customer.setShoppingCart(cart);

				product.setStock(product.getStock() - 1);
				productRepository.save(product);

				session.removeAttribute("customer");
				session.setAttribute("customer", customerRepository.save(customer));
				structure.setData(product);
				structure.setMessage("Product Added Successful");
				structure.setStatus_code(HttpStatus.ACCEPTED.value());
				return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
			} else {
				structure.setData(null);
				structure.setMessage("Out of Stock");
				structure.setStatus_code(HttpStatus.NOT_ACCEPTABLE.value());
				return new ResponseEntity<>(structure, HttpStatus.NOT_ACCEPTABLE);
			}
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<List<Item>>> viewCart(HttpSession session) {
		Customer customer = (Customer) session.getAttribute("customer");
		ResponseStructure<List<Item>> structure = new ResponseStructure<>();
		if (customer == null) {
			structure.setData(null);
			structure.setMessage("Logain Again");
			structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
			return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
		} else {

			if (customer.getShoppingCart() == null || customer.getShoppingCart().getItems() == null
					|| customer.getShoppingCart().getItems().isEmpty()) {
				structure.setData(null);
				structure.setMessage("No Items in cart");
				structure.setStatus_code(HttpStatus.NOT_ACCEPTABLE.value());
				return new ResponseEntity<>(structure, HttpStatus.NOT_FOUND);
			} else {
				List<Item> items = customer.getShoppingCart().getItems();
				structure.setData(items);
				structure.setMessage("Items");
				structure.setStatus_code(HttpStatus.FOUND.value());
				return new ResponseEntity<>(structure, HttpStatus.FOUND);
			}
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<List<Item>>> removeFromCart(HttpSession session, int id) {
		Customer customer = (Customer) session.getAttribute("customer");
		ResponseStructure<List<Item>> structure = new ResponseStructure<>();
		if (customer == null) {
			structure.setData(null);
			structure.setMessage("Logain Again");
			structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
			return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
		} else {
			List<Item> items = customer.getShoppingCart().getItems();
			Item item = null;
			boolean flag = false;
			for (Item item1 : items) {
				if (item1.getId() == id) {
					item = item1;
					if (item1.getQuantity() > 1) {
						item1.setPrice(item1.getPrice() - (item1.getPrice() / item1.getQuantity()));
						item1.setQuantity(item1.getQuantity() - 1);
						break;
					} else {
						flag = true;
						break;
					}
				}

			}
			if (flag) {
				items.remove(item);
			}

			Product product = productRepository.findByName(item.getName());
			product.setStock(product.getStock() + 1);
			productRepository.save(product);

			session.removeAttribute("customer");
			session.setAttribute("customer", customerRepository.save(customer));

			structure.setData(items);
			structure.setMessage("Product Removed from Cart Success");
			structure.setStatus_code(HttpStatus.OK.value());
			return new ResponseEntity<>(structure, HttpStatus.FOUND);
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<List<Wishlist>>> create_wishlist(HttpSession session, int id, String name) {
		Customer customer = (Customer) session.getAttribute("customer");
		ResponseStructure<List<Wishlist>> structure = new ResponseStructure<>();
		if (customer == null) {
			structure.setData(null);
			structure.setMessage("Logain Again");
			structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
			return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
		} else {
			if (wishlistRepository.findByName(name) == null) {
				Wishlist wishlist = new Wishlist();
				wishlist.setName(name);

				Product product = productRepository.findById(id).orElse(null);
				List<Wishlist> list = customer.getWishlists();
				if (list == null) {
					list = new ArrayList<>();
				}

				if (product != null) {
					List<Product> products = new ArrayList<>();
					products.add(product);
					wishlist.setProducts(products);

					list.add(wishlist);

					customer.setWishlists(list);

					session.removeAttribute("customer");
					session.setAttribute("customer", customerRepository.save(customer));
					structure.setData(list);
					structure.setMessage("WishList Creation Success and Product added to Wishlist");
					structure.setStatus_code(HttpStatus.CREATED.value());
					return new ResponseEntity<>(structure, HttpStatus.CREATED);

				} else {

					list.add(wishlist);

					customer.setWishlists(list);
					session.removeAttribute("customer");
					session.setAttribute("customer", customerRepository.save(customer));
					structure.setData(list);
					structure.setMessage("WishList Creation Success");
					structure.setStatus_code(HttpStatus.CREATED.value());
					return new ResponseEntity<>(structure, HttpStatus.CREATED);
				}
			} else {
				structure.setData(null);
				structure.setMessage("WishList Already Exists");
				structure.setStatus_code(HttpStatus.ALREADY_REPORTED.value());
				return new ResponseEntity<>(structure, HttpStatus.ALREADY_REPORTED);
			}
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<List<Wishlist>>> view_wishlist(HttpSession session) {
		Customer customer = (Customer) session.getAttribute("customer");
		ResponseStructure<List<Wishlist>> structure = new ResponseStructure<>();
		if (customer == null) {
			structure.setData(null);
			structure.setMessage("Logain Again");
			structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
			return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
		} else {
			List<Wishlist> list = customer.getWishlists();
			if (list == null || list.isEmpty()) {
				structure.setData(null);
				structure.setMessage("No Wishlist Found");
				structure.setStatus_code(HttpStatus.NOT_FOUND.value());
				return new ResponseEntity<>(structure, HttpStatus.NOT_FOUND);
			} else {
				structure.setData(list);
				structure.setMessage("Wishlist");
				structure.setStatus_code(HttpStatus.FOUND.value());
				return new ResponseEntity<>(structure, HttpStatus.FOUND);
			}
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<Wishlist>> viewWishlistProducts(HttpSession session, int id) {
		Customer customer = (Customer) session.getAttribute("customer");
		ResponseStructure<Wishlist> structure = new ResponseStructure<>();
		if (customer == null) {
			structure.setData(null);
			structure.setMessage("Logain Again");
			structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
			return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
		} else {
			Wishlist wishlist = wishlistRepository.findById(id).orElse(null);
			if (wishlist.getProducts() == null || wishlist.getProducts().isEmpty()) {
				structure.setData(null);
				structure.setMessage("No items present");
				structure.setStatus_code(HttpStatus.NOT_FOUND.value());
				return new ResponseEntity<>(structure, HttpStatus.NOT_FOUND);
			} else {
				structure.setData(wishlist);
				structure.setMessage("Product in Wishlist");
				structure.setStatus_code(HttpStatus.FOUND.value());
				return new ResponseEntity<>(structure, HttpStatus.NOT_FOUND);
			}
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<Wishlist>> addToWishList(int wid, int pid, HttpSession session) {
		Customer customer = (Customer) session.getAttribute("customer");
		ResponseStructure<Wishlist> structure = new ResponseStructure<>();
		if (customer == null) {
			structure.setData(null);
			structure.setMessage("Logain Again");
			structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
			return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
		} else {
			Wishlist wishlist = wishlistRepository.findById(wid).orElse(null);
			Product product = productRepository.findById(pid).orElse(null);

			List<Product> list = wishlist.getProducts();
			if (list == null) {
				list = new ArrayList<>();
			}
			boolean flag = true;
			for (Product product2 : list) {
				if (product2 == product) {
					flag = false;
					break;
				}
			}
			if (flag) {
				list.add(product);

				wishlist.setProducts(list);
				wishlistRepository.save(wishlist);
				structure.setData(wishlist);
				structure.setMessage("Item Added to Wish list");
				structure.setStatus_code(HttpStatus.CREATED.value());
				return new ResponseEntity<>(structure, HttpStatus.CREATED);
			} else {
				structure.setData(null);
				structure.setMessage("Item Already Exists in Wishlist");
				structure.setStatus_code(HttpStatus.ALREADY_REPORTED.value());
				return new ResponseEntity<>(structure, HttpStatus.ALREADY_REPORTED);
			}
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<Wishlist>> removeFromWishList(int wid, int pid, HttpSession session) {
		Customer customer = (Customer) session.getAttribute("customer");
		ResponseStructure<Wishlist> structure = new ResponseStructure<>();
		if (customer == null) {
			structure.setData(null);
			structure.setMessage("Logain Again");
			structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
			return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
		} else {
			Wishlist wishlist = wishlistRepository.findById(wid).orElse(null);
			Product product = productRepository.findById(pid).orElse(null);
			wishlist.getProducts().remove(product);
			wishlistRepository.save(wishlist);
			structure.setData(wishlist);
			structure.setMessage("Item Removed from Wish list");
			structure.setStatus_code(HttpStatus.ACCEPTED.value());
			return new ResponseEntity<>(structure, HttpStatus.ACCEPTED);
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<Wishlist>> deleteWishlist(int wid, HttpSession session) {
		Customer customer = (Customer) session.getAttribute("customer");
		ResponseStructure<Wishlist> structure = new ResponseStructure<>();
		if (customer == null) {
			structure.setData(null);
			structure.setMessage("Logain Again");
			structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
			return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
		} else {
			Wishlist wishlist = wishlistRepository.findById(wid).orElse(null);
			Wishlist wishlist2 = null;
			for (Wishlist wishlist3 : customer.getWishlists()) {
				if (wishlist3.getName().equals(wishlist.getName())) {
					wishlist2 = wishlist3;
				}
			}

			customer.getWishlists().remove(wishlist2);
			session.setAttribute("customer", customerRepository.save(customer));
			wishlistRepository.delete(wishlist);
			structure.setData(wishlist);
			structure.setMessage("Wishlist deleted Success");
			structure.setStatus_code(HttpStatus.OK.value());
			return new ResponseEntity<>(structure, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<List<Payment>>> checkPayment(HttpSession session) {
		Customer customer = (Customer) session.getAttribute("customer");
		ResponseStructure<List<Payment>> structure = new ResponseStructure<>();
		if (customer == null) {
			structure.setData(null);
			structure.setMessage("Logain Again");
			structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
			return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
		} else {
			List<Payment> payments = paymentRepository.findAll();
			if (payments.isEmpty()) {
				structure.setData(null);
				structure.setMessage("Sorry you can not place order, There is an internal error try after some time");
				structure.setStatus_code(HttpStatus.NOT_FOUND.value());
				return new ResponseEntity<>(structure, HttpStatus.NOT_FOUND);
			} else {
				structure.setData(payments);
				structure.setMessage("List of Payments");
				structure.setStatus_code(HttpStatus.ALREADY_REPORTED.value());
				return new ResponseEntity<>(structure, HttpStatus.ALREADY_REPORTED);
			}
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<Customer>> checkAddress(HttpSession session, int pid) {

		Customer customer = (Customer) session.getAttribute("customer");
		ResponseStructure<Customer> structure = new ResponseStructure<>();
		if (customer == null) {
			structure.setData(null);
			structure.setMessage("Logain Again");
			structure.setStatus_code(HttpStatus.NOT_FOUND.value());
			return new ResponseEntity<>(structure, HttpStatus.NOT_FOUND);
		} else {
			structure.setData(customer);
			structure.setMessage("procced for payment");
			structure.setStatus_code(HttpStatus.OK.value());
			return new ResponseEntity<>(structure, HttpStatus.OK);
		}

	}

	@Override
	public ResponseEntity<ResponseStructure<List<ShoppingOrder>>> viewOrders(HttpSession session) {

		Customer customer = (Customer) session.getAttribute("customer");
		ResponseStructure<List<ShoppingOrder>> structure = new ResponseStructure<>();
		if (customer == null) {
			structure.setData(null);
			structure.setMessage("Logain Again");
			structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
			return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
		} else {
			List<ShoppingOrder> list = customer.getOrders();
			if (list == null || list.isEmpty()) {
				structure.setData(null);
				structure.setMessage("No Orders Yet");
				structure.setStatus_code(HttpStatus.OK.value());
				return new ResponseEntity<>(structure, HttpStatus.OK);
			} else {
				structure.setData(list);
				structure.setMessage("Your Order");
				structure.setStatus_code(HttpStatus.CREATED.value());
				return new ResponseEntity<>(structure, HttpStatus.CREATED);
			}
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<ShoppingOrder>> submitOrder(HttpSession session, @RequestParam int pid,
			@RequestParam String address) throws RazorpayException {

		Customer customer = (Customer) session.getAttribute("customer");
		ResponseStructure<ShoppingOrder> structure = new ResponseStructure<>();
		if (customer == null) {
			structure.setData(null);
			structure.setMessage("Login Again");
			structure.setStatus_code(HttpStatus.NOT_FOUND.value());
			return new ResponseEntity<>(structure, HttpStatus.NOT_FOUND);
		} else {
			Payment payment = paymentRepository.findById(pid).orElse(null);
			ShoppingOrder order = new ShoppingOrder();
			order.setAddress(address);
			order.setPaymentMode(payment.getName());
			order.setDeliveryDate(LocalDateTime.now().plusDays(3));
			Shoppingcart cart = customer.getShoppingCart();
			if (cart == null) {
				structure.setData(null);
				structure.setMessage("Please add items to your cart");
				structure.setStatus_code(HttpStatus.NOT_FOUND.value());
				return new ResponseEntity<>(structure, HttpStatus.NOT_FOUND);
			}
			if (cart.getItems() == null || cart.getItems().isEmpty()) {
				structure.setData(null);
				structure.setMessage("Please add items to your cart");
				structure.setStatus_code(HttpStatus.BAD_REQUEST.value());
				return new ResponseEntity<>(structure, HttpStatus.BAD_REQUEST);
			}
			double total = 0;
			for (Item item : cart.getItems()) {
				total = total + item.getPrice();
			}
			order.setTotalPrice(total);
			order.setItems(cart.getItems());

			if (payment.getName().equalsIgnoreCase("RazorPay")) {
				JSONObject object = new JSONObject();
				object.put("currency", "INR");
				object.put("amount", total * 100);

				RazorpayClient client = new RazorpayClient("rzp_test_WOkek02qzU8zkJ", "wHpnWVu3jNVB59aehItjTchA");
				Order order1 = client.orders.create(object);
				order.setStatus(order1.get("status"));
				order.setCurrency("INR");
				order.setOrderId(order1.get("id"));
				order.setPayment_key("rzp_test_WOkek02qzU8zkJ");
				order.setCompany_name("E-Kart");
				structure.setMessage("Order created successfully payment");
				structure.setData(order);
				structure.setStatus_code(HttpStatus.CREATED.value());
				return new ResponseEntity<>(structure, HttpStatus.CREATED);
			} else {
				List<ShoppingOrder> list = customer.getOrders();
				if (list == null) {
					list = new ArrayList<>();
				}
				list.add(order);
				customer.setOrders(list);
				customer.setAddress(address);
				cart.setItems(null);
				customer.setShoppingCart(null);
				Customer customer1 = customerRepository.save(customer);
				cartRespository.delete(cart);
				session.removeAttribute("customer");
				session.setAttribute("customer", customer1);
				structure.setMessage("Order created successfully null");
				structure.setData(order);
				structure.setStatus_code(HttpStatus.CREATED.value());
				return new ResponseEntity<>(structure, HttpStatus.CREATED);
			}
		}
	}

}
