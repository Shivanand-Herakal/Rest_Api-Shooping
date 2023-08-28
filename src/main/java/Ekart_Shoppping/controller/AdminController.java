package Ekart_Shoppping.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Ekart_Shoppping.dto.Admin;
import Ekart_Shoppping.dto.Customer;
import Ekart_Shoppping.dto.Customer;
import Ekart_Shoppping.dto.Merchant;
import Ekart_Shoppping.dto.Payment;
import Ekart_Shoppping.dto.Product;
import Ekart_Shoppping.helper.ResponseStructure;
import Ekart_Shoppping.repository.ProductRepository;
import Ekart_Shoppping.service.AdminService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/admin")
public class AdminController {

	
	@Autowired
	AdminService adminService;

	@PostMapping("/create")
	public ResponseEntity<ResponseStructure<Admin>>createAdmin(@ModelAttribute Admin admin){
		return adminService.createAdmin(admin);
	}
	
	@PostMapping("/login")
	public ResponseEntity<ResponseStructure<Admin>>adminlogin(String username,String password,HttpSession httpSession){
		return adminService.adminlogin(username,password,httpSession);
	}
	@GetMapping("/view-all_products")
	public ResponseEntity<ResponseStructure<List<Product>>> viwAllProducts(HttpSession session){
		return adminService.viewAllProduct(session);
	}
	@GetMapping("/view-all_merchants")
	public ResponseEntity<ResponseStructure<List<Merchant>>>viewAllMerchant(HttpSession session){
		return adminService.viewAllMerchant(session);
	}
	@GetMapping("/view-all_customer")
	public ResponseEntity<ResponseStructure<List<Customer>>>viewAllCustomer(HttpSession session){
		return adminService.viewAllCustomer(session);
	}

	@PostMapping("/payment-add")
	public ResponseEntity<ResponseStructure<Payment>> addpayment(HttpSession session, Payment payment) {
		return adminService.addpayment(session, payment);
	}
	
	@GetMapping("/view-all-payment")
	public ResponseEntity<ResponseStructure<List<Payment>>> viewallPayment(HttpSession session){
		return adminService.viewallPayment(session);
	}
	@GetMapping("/product-changestatus/{id}")
	public ResponseEntity<ResponseStructure<List<Product>>> changeStatus(@PathVariable int id, HttpSession session) {
		return adminService.changeStatus(id, session);
	}
}
