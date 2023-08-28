package Ekart_Shoppping.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.razorpay.RazorpayException;

import Ekart_Shoppping.dto.Payment;
import Ekart_Shoppping.dto.Customer;
import Ekart_Shoppping.dto.Item;
import Ekart_Shoppping.dto.Product;
import Ekart_Shoppping.dto.ShoppingOrder;
import Ekart_Shoppping.dto.Wishlist;
import Ekart_Shoppping.helper.ResponseStructure;
import Ekart_Shoppping.service.CustomerServicce;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("customer")
public class CustomerController {

	@Autowired
	CustomerServicce customerServicce;
	
	@PostMapping("/signup")
	public ResponseEntity<ResponseStructure<Customer>>signup(@ModelAttribute Customer customer,String date){
		return customerServicce.signup(customer,date);
	}
	@GetMapping("/verify-otp/{email}/{token}")
	public ResponseEntity<ResponseStructure<Customer>> verify_link(@PathVariable String email,
			@PathVariable String token) {
		return customerServicce.verify_link(email, token);
	}

	@PostMapping("/login")
	public ResponseEntity<ResponseStructure<Customer>> login(@RequestParam String email, @RequestParam String password,
			HttpSession session) {
		return customerServicce.login(email, password, session);
	}

	@GetMapping("/products-view")
	public ResponseEntity<ResponseStructure<List<Product>>> view_products(HttpSession session) {
		return customerServicce.view_products(session);
	}

	@GetMapping("/cart-add/{id}")
	public ResponseEntity<ResponseStructure<Product>> addCart(HttpSession session, @PathVariable int id) {
		return customerServicce.addCart(session, id);
	}

	@GetMapping("/cart-view")
	public ResponseEntity<ResponseStructure<List<Item>>> viewCart(HttpSession session) {
		return customerServicce.viewCart(session);
	}

	@GetMapping("/cart-remove/{id}")
	public ResponseEntity<ResponseStructure<List<Item>>> removeFromCart(HttpSession session, @PathVariable int id) {
		return customerServicce.removeFromCart(session, id);
	}
	@PostMapping("/wishlist-create/{id}")
	public ResponseEntity<ResponseStructure<List<Wishlist>>> create_wishlist(HttpSession session, @PathVariable int id,
			@RequestParam String name) {
		return customerServicce.create_wishlist(session, id, name);
	}

	@GetMapping("/wishlist-view")
	public ResponseEntity<ResponseStructure<List<Wishlist>>> view_wishlist(HttpSession session) {
		return customerServicce.view_wishlist(session);
	}

	@GetMapping("/wishlist/product-view/{id}")
	public ResponseEntity<ResponseStructure<Wishlist>> viewWishlistProducts(@PathVariable int id, HttpSession session) {
		return customerServicce.viewWishlistProducts(session, id);
	}

	@GetMapping("/wishlist-add/{wid}/{pid}")
	public ResponseEntity<ResponseStructure<Wishlist>> addToWishList(@PathVariable int wid, @PathVariable int pid,
			HttpSession session) {
		return customerServicce.addToWishList(wid, pid, session);
	}

	@GetMapping("/wishlist-remove/{wid}/{pid}")
	public ResponseEntity<ResponseStructure<Wishlist>> removeFromWishList(@PathVariable int wid, @PathVariable int pid,
			HttpSession session) {
		return customerServicce.removeFromWishList(wid, pid, session);
	}

	@GetMapping("/wishlist-delete/{wid}")
	public ResponseEntity<ResponseStructure<Wishlist>> deleteWishlist(@PathVariable int wid, HttpSession session) {
		return customerServicce.deleteWishlist(wid, session);
	}

	@GetMapping("/placeorder")
	public ResponseEntity<ResponseStructure<List<Payment>>> checkPayment(HttpSession session) {
		return customerServicce.checkPayment(session);
	}

	@PostMapping("/placeorder")
	public ResponseEntity<ResponseStructure<Customer>> checkAddress(HttpSession session, @RequestParam int pid) {
		return customerServicce.checkAddress(session, pid);
	}

	@PostMapping("/submitorder")
	public ResponseEntity<ResponseStructure<ShoppingOrder>> submitOrder(HttpSession session, @RequestParam int pid,
			@RequestParam String address) throws RazorpayException {
		return customerServicce.submitOrder(session, pid, address);
	}

	@GetMapping("/orders-view")
	public ResponseEntity<ResponseStructure<List<ShoppingOrder>>> viewOrder(HttpSession session) {
		return customerServicce.viewOrders(session);
	}

	@PostMapping("/forgotpassword")
	public ResponseEntity<ResponseStructure<Customer>> forgotLink(@RequestParam String email) throws Exception {
		return customerServicce.forgotLink(email);
	}

	@GetMapping("/reset-password/{email}/{token}")
	public ResponseEntity<ResponseStructure<Customer>> resetPassword(@PathVariable String email,
			@PathVariable String token) {
		return customerServicce.resetPassword(email, token);
	}

	@PostMapping("/reset-password")
	public ResponseEntity<ResponseStructure<Customer>> setpassword(@RequestParam String email,
			@RequestParam String password) {
		return customerServicce.setpassword(email, password);
	}
}


