package Ekart_Shoppping.service;

import java.util.List;

import org.springframework.http.ResponseEntity;


import Ekart_Shoppping.dto.Admin;
import Ekart_Shoppping.dto.Customer;
import Ekart_Shoppping.dto.Merchant;
import Ekart_Shoppping.dto.Payment;
import Ekart_Shoppping.dto.Product;
import Ekart_Shoppping.helper.ResponseStructure;
import jakarta.servlet.http.HttpSession;

public interface AdminService {

	public ResponseEntity<ResponseStructure<Admin>> createAdmin(Admin admin);

	public ResponseEntity<ResponseStructure<Admin>> adminlogin(String email, String password, HttpSession httpSession);

	public ResponseEntity<ResponseStructure<List<Product>>> viewAllProduct(HttpSession session);

	public ResponseEntity<ResponseStructure<List<Merchant>>> viewAllMerchant(HttpSession session);

	public ResponseEntity<ResponseStructure<Payment>> addpayment(HttpSession session, Payment payment);

	public ResponseEntity<ResponseStructure<List<Payment>>> viewallPayment(HttpSession session);

	public ResponseEntity<ResponseStructure<List<Customer>>> viewAllCustomer(HttpSession session);

	public ResponseEntity<ResponseStructure<List<Product>>> changeStatus(int id, HttpSession session);

	
	
	
}
