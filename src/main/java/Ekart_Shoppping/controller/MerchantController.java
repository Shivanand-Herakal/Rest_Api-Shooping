package Ekart_Shoppping.controller;


import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import Ekart_Shoppping.dto.Merchant;
import Ekart_Shoppping.dto.Product;
import Ekart_Shoppping.exception.NoZeroException;
import Ekart_Shoppping.helper.ResponseStructure;
import Ekart_Shoppping.service.implimentation.MerhantService_implementation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/merchants")
public class MerchantController {
   
	
	@Autowired
	MerhantService_implementation merchantService_implementation;

	@PostMapping("/hello")
	public ResponseEntity<ResponseStructure<Merchant>> saveStudent(@RequestBody Merchant merchant)
			throws NoZeroException {
		ResponseStructure<Merchant> structure = new ResponseStructure<>();
		if (merchant.getMobile() == 0000000000) {
			throw new NoZeroException("Mobile Number Not repeated");
		} else {
			structure.setData(merchant);
			structure.setStatus_code(HttpStatus.CREATED.value());
			structure.setMessage("Account created SUccess");
			return new ResponseEntity<ResponseStructure<Merchant>>(structure, HttpStatus.CREATED);
		}
	}

	@PostMapping("/signup")
	public ResponseEntity<ResponseStructure<Merchant>> signup(@ModelAttribute Merchant merchant,
			@RequestParam String date, @RequestPart MultipartFile pic) throws IOException {
		return merchantService_implementation.signup(merchant, date, pic);
	}

	@PostMapping("/verify-otp/{email}")
	public ResponseEntity<ResponseStructure<Merchant>> verifyOtp(@PathVariable String email, @RequestParam int otp) {
		return merchantService_implementation.verifyOtp(email, otp);
	}
	
	@GetMapping("/resend-opt/{email}")
	public ResponseEntity<ResponseStructure<Merchant>> resendotp(@PathVariable String email ) {
		return merchantService_implementation.resendotp(email);
	}
	
	@PostMapping("/forgotpassword")
	public ResponseEntity<ResponseStructure<Merchant>> sendForgotOtp(@RequestParam String email) {
		return merchantService_implementation.sendForgotOtp(email);
	}

	@PostMapping("/forgot-otp/{email}")
	public ResponseEntity<ResponseStructure<Merchant>> submitForgotOtp(@PathVariable String email,@RequestParam int otp) {
		return merchantService_implementation.submitForgotOtp(email, otp);
	}
	
	@PostMapping("/login")
	public ResponseEntity<ResponseStructure<Merchant>> login(@RequestParam String email, @RequestParam String password,HttpServletResponse response, HttpSession session) {
		System.out.printf(email,password);
		return merchantService_implementation.login(email, password,  session);
	}
	
	@PostMapping("/product-add")
	public ResponseEntity<ResponseStructure<Merchant>> addProduct(HttpSession session, @ModelAttribute Product product,
			@RequestParam MultipartFile pic) throws IOException {
		return merchantService_implementation.addProduct(session, product, pic);
	}
	
	@GetMapping("/product-view")
	public ResponseEntity<ResponseStructure<List<Product>>> featchAllProducts(HttpSession session){
		return merchantService_implementation.featchAllProducts(session);
	}
	
	@PutMapping("/product-delete/{id}")
	public ResponseEntity<ResponseStructure<Product>> deleteProduct(@PathVariable int id, HttpSession session)
	{
		return merchantService_implementation.deleteProduct(id,session);
	}
	
	@GetMapping("/product-update/{id}")
	public ResponseEntity<ResponseStructure<Product>> updateProduct( @RequestBody Product product,HttpSession session,@PathVariable int id)
	{
	     return merchantService_implementation.updateProduct(session,id,product);
	}
	
	@GetMapping("/resend-forgot-otp/{email}")
	public ResponseEntity<ResponseStructure<Merchant>> resendForgotOtp(@PathVariable String email){
		return merchantService_implementation.resendForgotOtp(email);
	}
	@PostMapping("/reset-password")
	public ResponseEntity<ResponseStructure<Merchant>> setPassword(@RequestParam String email,
			@RequestParam String password) {
		return merchantService_implementation.setPassword(email, password);
	}
}
