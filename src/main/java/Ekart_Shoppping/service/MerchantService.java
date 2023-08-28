package Ekart_Shoppping.service;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import Ekart_Shoppping.dto.Merchant;
import Ekart_Shoppping.dto.Product;
import Ekart_Shoppping.helper.ResponseStructure;
import jakarta.servlet.http.HttpSession;


public interface MerchantService {

	
	public ResponseEntity<ResponseStructure<Merchant>> signup(Merchant merchant, String date, MultipartFile picture)
			throws IOException;


	public ResponseEntity<ResponseStructure<Merchant>> verifyOtp(String email, int otp);
	
	public ResponseEntity<ResponseStructure<Merchant>> resendotp(String email);
	
	public ResponseEntity<ResponseStructure<Merchant>> login(String email,String password,HttpSession session);
	
	public ResponseEntity<ResponseStructure<Merchant>> addProduct(HttpSession session, Product product,
			MultipartFile pic)  throws IOException;
	
	public ResponseEntity<ResponseStructure<List<Product>>> featchAllProducts(HttpSession session);
}
