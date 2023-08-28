package Ekart_Shoppping.service.implimentation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import Ekart_Shoppping.dto.Admin;
import Ekart_Shoppping.dto.Customer;
import Ekart_Shoppping.dto.Merchant;
import Ekart_Shoppping.dto.Payment;
import Ekart_Shoppping.dto.Product;
import Ekart_Shoppping.helper.ResponseStructure;
import Ekart_Shoppping.repository.AdminRepository;
import Ekart_Shoppping.repository.CustomerRepository;
import Ekart_Shoppping.repository.MerchantRepository;
import Ekart_Shoppping.repository.PaymentRepository;
import Ekart_Shoppping.repository.ProductRepository;
import Ekart_Shoppping.service.AdminService;
import jakarta.mail.Session;
import jakarta.servlet.http.HttpSession;

@Service
public  class Admin_implimentation implements AdminService {

	@Autowired
	AdminRepository adminRepository;
	
	@Autowired
	ProductRepository productRepository;
	
	@Autowired
	MerchantRepository merchantRepository;
	
	@Autowired
	CustomerRepository customerRepository;
	
	@Autowired
	PaymentRepository paymentRepository;

	@Override
	public ResponseEntity<ResponseStructure<Admin>> createAdmin(Admin admin) {
		ResponseStructure<Admin> structure = new ResponseStructure<>();
		int existingEntries = adminRepository.countuser(admin.getUsername(), admin.getPassword());
		if (existingEntries == 0) {
			adminRepository.save(admin);
			structure.setData(admin);
			structure.setMessage("Account Create for Admin");
			structure.setStatus_code(HttpStatus.CREATED.value());
			return new ResponseEntity<>(structure, HttpStatus.CREATED);
		} else {
			structure.setData(null);
			structure.setMessage("Admin Cannot More than one");
			structure.setStatus_code(HttpStatus.ALREADY_REPORTED.value());
			return new ResponseEntity<>(structure, HttpStatus.ALREADY_REPORTED);
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<Admin>> adminlogin(String username, String password, HttpSession session) {
		ResponseStructure<Admin> structure = new ResponseStructure<>();
		Admin admin = adminRepository.findByUsername(username);
		if (admin == null) {
			structure.setData(null);
			structure.setMessage("Incorrect username");
			structure.setStatus_code(HttpStatus.NOT_FOUND.value());
			return new ResponseEntity<>(structure, HttpStatus.NOT_FOUND);
		} else {
			if (admin.getPassword().equals(password)) {
				session.setAttribute("admin", admin);

				structure.setData(admin);
				structure.setMessage("Login Success");
				structure.setStatus_code(HttpStatus.CREATED.value());
				return new ResponseEntity<>(structure, HttpStatus.CREATED);
			} else {
				structure.setData(null);
				structure.setMessage("Incorrect Password");
				structure.setStatus_code(HttpStatus.BAD_REQUEST.value());
				return new ResponseEntity<>(structure, HttpStatus.BAD_REQUEST);
			}
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<List<Product>>> viewAllProduct(HttpSession session) {
		ResponseStructure<List<Product>> structure=new ResponseStructure<>();
		if(session.getAttribute("admin")==null) {
	       structure.setData(null);
	       structure.setMessage("please lodgin agin");
	       structure.setStatus_code(HttpStatus.BAD_REQUEST.value());
	       return new ResponseEntity<>(structure,HttpStatus.BAD_REQUEST);
	}else {
		List<Product>products=productRepository.findAll();
		if(products.isEmpty()) {
			structure.setData(null);
			structure.setMessage("No Products Found");
			structure.setStatus_code(HttpStatus.NOT_FOUND.value());
			return new ResponseEntity<>(structure,HttpStatus.NOT_FOUND);
		}
		 else {
				structure.setMessage("Product Found");
				structure.setData(products);
				structure.setStatus_code(HttpStatus.FOUND.value());
				return new ResponseEntity<>(structure, HttpStatus.FOUND);
			}
	}
	}

	@Override
	public ResponseEntity<ResponseStructure<List<Merchant>>> viewAllMerchant(HttpSession session) {
		ResponseStructure<List<Merchant>> structure=new ResponseStructure<>();
		if(session.getAttribute("admin")==null) {
			structure.setData(null);
			structure.setMessage("Please Login Again");
			structure.setStatus_code(HttpStatus.BAD_REQUEST.value());
			return new ResponseEntity<>(structure,HttpStatus.BAD_REQUEST);
		}else {
			List<Merchant>merchants=merchantRepository.findAll();
			if(merchants.isEmpty()) {
				structure.setData(null);
				structure.setMessage("No Merchants Found");
				structure.setStatus_code(HttpStatus.NOT_FOUND.value());
				return new ResponseEntity<>(structure,HttpStatus.NOT_FOUND);
			}else {
				structure.setData(merchants);
				structure.setMessage("All The Merchants Found here");
				structure.setStatus_code(HttpStatus.ACCEPTED.value());
				return new ResponseEntity<>(structure,HttpStatus.ACCEPTED);
			}
			
		}
	}
	
//	@Override
//	public ResponseEntity<ResponseStructure<List<Customer>>> viewallcustomer(HttpSession session) {
//		ResponseStructure<List<Customer>> structure = new ResponseStructure<>();
//		if (session.getAttribute("admin") == null) {
//			structure.setData(null);
//			structure.setMessage("Login Again");
//			structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
//			return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
//		} else {
//			List<Customer> customers = customerRepository.findAll();
//			if (customers.isEmpty()) {
//				structure.setData(null);
//				structure.setMessage("No Customers Data");
//				structure.setStatus_code(HttpStatus.NOT_FOUND.value());
//				return new ResponseEntity<>(structure, HttpStatus.NOT_FOUND);
//			} else {
//				structure.setData(customers);
//				structure.setMessage("Customers Data");
//				structure.setStatus_code(HttpStatus.FOUND.value());
//				return new ResponseEntity<>(structure, HttpStatus.FOUND);
//			}
//		}
//	}

	@Override
	public ResponseEntity<ResponseStructure<Payment>> addpayment(HttpSession session, Payment payment) {
		Payment payment2 = paymentRepository.findByName(payment.getName());
		ResponseStructure<Payment> structure = new ResponseStructure<>();
		if (session.getAttribute("admin") == null) {
			structure.setData(null);
			structure.setMessage("Login Again");
			structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
			return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
		} else {
			if (payment2 == null) {
				paymentRepository.save(payment);
				structure.setData(payment);
				structure.setMessage("Payment method added succesfully");
				structure.setStatus_code(HttpStatus.CREATED.value());
				return new ResponseEntity<>(structure, HttpStatus.CREATED);
			} else {
				structure.setData(null);
				structure.setMessage("payment Method Already Exits");
				structure.setStatus_code(HttpStatus.NOT_ACCEPTABLE.value());
				return new ResponseEntity<>(structure, HttpStatus.NOT_ACCEPTABLE);
			}
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<List<Payment>>> viewallPayment(HttpSession session) {
		ResponseStructure<List<Payment>> structure = new ResponseStructure<>();
		List<Payment> payment = paymentRepository.findAll();
		if (session.getAttribute("admin") == null) {
			structure.setData(null);
			structure.setMessage("Login Again");
			structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
			return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
		} else {
			structure.setData(payment);
			structure.setMessage("All Payment Method");
			structure.setStatus_code(HttpStatus.FOUND.value());
			return new ResponseEntity<>(structure, HttpStatus.FOUND);
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<List<Customer>>> viewAllCustomer(HttpSession session) {
		ResponseStructure<List<Customer>> structure = new ResponseStructure<>();
		if (session.getAttribute("admin") == null) {
			structure.setData(null);
			structure.setMessage("Login Again");
			structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
			return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
		} else {
			List<Customer> customers = customerRepository.findAll();
			if (customers.isEmpty()) {
				structure.setData(null);
				structure.setMessage("No Customers Data");
				structure.setStatus_code(HttpStatus.NOT_FOUND.value());
				return new ResponseEntity<>(structure, HttpStatus.NOT_FOUND);
			} else {
				structure.setData(customers);
				structure.setMessage("Customers Data");
				structure.setStatus_code(HttpStatus.FOUND.value());
				return new ResponseEntity<>(structure, HttpStatus.FOUND);
			}
		}
	}

	@Override
	public ResponseEntity<ResponseStructure<List<Product>>> changeStatus(int id, HttpSession session) {
		ResponseStructure<List<Product>> structure = new ResponseStructure<>();
		if (session.getAttribute("admin") == null) {
			structure.setData(null);
			structure.setMessage("Login Again");
			structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
			return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
		} else {
			Product product = productRepository.findById(id).orElse(null);
			if (product.isStatus()) {
				product.setStatus(false);
			} else {
				product.setStatus(true);
			}
			productRepository.save(product);
			List<Product> products = productRepository.findAll();
			if (products.isEmpty()) {
				structure.setData(null);
				structure.setMessage("No Products Data");
				structure.setStatus_code(HttpStatus.NOT_FOUND.value());
				return new ResponseEntity<>(structure, HttpStatus.NOT_FOUND);
			} else {
				structure.setData(products);
				structure.setMessage("Status Changed Success");
				structure.setStatus_code(HttpStatus.ACCEPTED.value());
				return new ResponseEntity<>(structure, HttpStatus.ACCEPTED);
			}
		}
	}


	}




		
	


	
