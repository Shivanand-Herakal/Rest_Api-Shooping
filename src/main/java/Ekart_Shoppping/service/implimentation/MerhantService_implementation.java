package Ekart_Shoppping.service.implimentation;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;
import org.springframework.web.multipart.MultipartFile;

import Ekart_Shoppping.dao.MerchantDao;
import Ekart_Shoppping.dto.Merchant;
import Ekart_Shoppping.dto.Product;
import Ekart_Shoppping.helper.ResponseStructure;
import Ekart_Shoppping.helper.SendMail;
import Ekart_Shoppping.repository.ProductRepository;
import Ekart_Shoppping.service.MerchantService;
import jakarta.servlet.http.HttpSession;

@Service
public class MerhantService_implementation implements MerchantService {
	@Autowired
	MerchantDao merchantDao;

	@Autowired
	SendMail mail;

	@Autowired
	ProductRepository productRepository;
	
	@Autowired
	BCryptPasswordEncoder encoder;

	@Override
	public ResponseEntity<ResponseStructure<Merchant>> signup(Merchant merchant, String date, MultipartFile pic)
			throws IOException {
		ResponseStructure<Merchant> structure = new ResponseStructure<>();

		merchant.setDob(LocalDate.parse(date));

		byte[] picture = new byte[pic.getInputStream().available()];
		pic.getInputStream().read(picture);
		merchant.setPicture(picture);

		if (merchantDao.findByEmail(merchant.getEmail()) != null
				|| merchantDao.findByMobile(merchant.getMobile()) != null) {
			structure.setStatus_code(HttpStatus.BAD_REQUEST.value());
			structure.setMessage("Email or Mobile Should not be repeated");
			return new ResponseEntity<>(structure, HttpStatus.BAD_REQUEST);
		}

		int otp = new Random().nextInt(100000, 999999);
		merchant.setOtp(otp);
		merchant.setOtpgeneratedtime(LocalDateTime.now());;
		
		if (mail.sendOtp(merchant)) {
			Merchant merchant2 = merchantDao.save(merchant);
			structure.setData(merchant2);
			structure.setStatus_code(HttpStatus.CREATED.value());
			structure.setMessage("Account created successfully");
			return new ResponseEntity<>(structure, HttpStatus.CREATED);
		} else {
			structure.setStatus_code(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage("Something went wrong, Check email and try again");
			return new ResponseEntity<>(structure, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResponseEntity<ResponseStructure<Merchant>> verifyOtp(String email, int otp) {
		ResponseStructure<Merchant> structure = new ResponseStructure<>();
		Merchant merchant = merchantDao.findByEmail(email);
		System.out.println(merchant + "------------------------------------------------");
		if (merchant.getOtp() == otp) {
			merchant.setStatus(true);
			merchant.setOtp(0);
			merchantDao.save(merchant);
			structure.setData(merchant);
			structure.setStatus_code(HttpStatus.CREATED.value());
			structure.setMessage("Otp Verified Successfully");
			return new ResponseEntity<>(structure, HttpStatus.CREATED);
		} else {

			structure.setData(null);
			structure.setMessage("Otp Not Verified Sucessfully");
			structure.setStatus_code(HttpStatus.BAD_REQUEST.value());
			return new ResponseEntity<>(structure, HttpStatus.BAD_REQUEST);
		}

	}

	public ResponseEntity<ResponseStructure<Merchant>> resendotp(String email) {
		ResponseStructure<Merchant> structure = new ResponseStructure<>();
		Merchant merchant = merchantDao.findByEmail(email);
		if(merchant!=null) {
		int otp = new Random().nextInt(100000, 999999);
		merchant.setOtp(otp);
		merchant.setOtpgeneratedtime(LocalDateTime.now());

		// logic for sending the mail
		if (mail.sendOtp(merchant)) {
			Merchant merchant2 = merchantDao.save(merchant);
			structure.setData(merchant2);
			structure.setMessage("resend OTP sent successfull");
			structure.setStatus_code(HttpStatus.CREATED.value());
			return new ResponseEntity<>(structure, HttpStatus.CREATED);
		} else {
			structure.setData(null);
			structure.setMessage("Check mail once ,Something went wrong");
			structure.setStatus_code(HttpStatus.BAD_REQUEST.value());
			return new ResponseEntity<>(structure, HttpStatus.BAD_REQUEST);
		}
		}else {
			structure.setData(null);
			structure.setMessage("Merchant not founfd,pls signup again");
			structure.setStatus_code(HttpStatus.BAD_REQUEST.value());
			return new ResponseEntity<>(structure, HttpStatus.BAD_REQUEST);
		}
	
		}

	public ResponseEntity<ResponseStructure<Merchant>> login(String email, String password, HttpSession session) {
		ResponseStructure<Merchant> structure = new ResponseStructure<>();
		Merchant merchant = merchantDao.findByEmail(email);
		if (merchant == null) {
			structure.setData(null);
			structure.setMessage("please check once email and password and enter currectly");
			structure.setStatus_code(HttpStatus.BAD_REQUEST.value());
			return new ResponseEntity<>(structure, HttpStatus.BAD_REQUEST);
		} else {
			if (merchant.getPassword().equals(password)) {
				if (merchant.isStatus()) {
					session.setAttribute("merchant", merchant);
					structure.setData(merchant);
					structure.setMessage("Login succesfull");
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

	public ResponseEntity<ResponseStructure<Merchant>> addProduct(HttpSession session, Product product,
			MultipartFile pic) throws IOException {
		ResponseStructure<Merchant> structure = new ResponseStructure<>();
		if (session.getAttribute("merchant") == null) {
			structure.setData(null);
			structure.setMessage("Login Again to add the product");
			structure.setStatus_code(HttpStatus.BAD_REQUEST.value());
			return new ResponseEntity<>(structure, HttpStatus.BAD_REQUEST);
		} else {

			Merchant merchant = (Merchant) session.getAttribute("merchant");
			byte[] picture = new byte[pic.getInputStream().available()];
			pic.getInputStream().read(picture);

			product.setImage(picture);
			product.setName(merchant.getName() + "-" + product.getName());

			Product product2 = merchantDao.findProductByName(product.getName());
			if (product2 != null) {
				product.setId(product2.getId());
				product.setStock(product.getStock() + product2.getStock());
			}
			List<Product> products = merchant.getProducts();
			if (products == null) {
				products = new ArrayList<>();
			}
			products.add(product);
			merchant.setProducts(products);

			session.setAttribute("merchant", merchantDao.save(merchant));
			structure.setData(merchant);
			structure.setMessage("Product Added Successfull");
			structure.setStatus_code(HttpStatus.CREATED.value());
			return new ResponseEntity<>(structure, HttpStatus.CREATED);
		}
	}
   @Override
	public ResponseEntity<ResponseStructure<List<Product>>> featchAllProducts(HttpSession session) {
		ResponseStructure<List<Product>> structure = new ResponseStructure<>();
		Merchant merchant = (Merchant) session.getAttribute("merchant");
		if (session.getAttribute("merchant") == null) {
			structure.setData(null);
			structure.setMessage("Login Again to View All Products");
			structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
			
			return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
		} else {
			List<Product> products = merchant.getProducts();
			if (products == null || products.isEmpty()) {
				structure.setData(null);
				structure.setMessage("Products NOt Found");
				structure.setStatus_code(HttpStatus.NOT_FOUND.value());
				return new ResponseEntity<>(structure, HttpStatus.NOT_FOUND);
			} 
				structure.setData(products);
				structure.setMessage("All Products Found At One Stage");
				structure.setStatus_code(HttpStatus.FOUND.value());
				return new ResponseEntity<>(structure, HttpStatus.FOUND);
			}
		}
	

	public ResponseEntity<ResponseStructure<Product>> deleteProduct(int id, HttpSession session) {
		ResponseStructure<Product> structure = new ResponseStructure<>();
		if (session.getAttribute("merchant") == null) {
			structure.setData(null);
			structure.setMessage("Login Again");
			structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
			return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
		} else {
			Product product = merchantDao.findProductById(id);
			Merchant merchant = (Merchant) session.getAttribute("merchant");
			structure.setStatus_code(HttpStatus.ACCEPTED.value());
			if (merchant.getProducts() == null || merchant.getProducts().isEmpty()) {
				structure.setStatus_code(HttpStatus.NOT_FOUND.value());
				structure.setMessage("Products Not Found");
				return new ResponseEntity<>(structure, HttpStatus.NOT_FOUND);
			} else {

				merchant.getProducts().remove(product);
				session.removeAttribute("merchant");
				session.setAttribute("merchant", merchantDao.save(merchant));
				merchantDao.removeProduct(product);
				structure.setStatus_code(HttpStatus.ACCEPTED.value());
				structure.setMessage("Deleted Successfully");
				return new ResponseEntity<>(structure, HttpStatus.ACCEPTED);
			}
		}
	}

	public ResponseEntity<ResponseStructure<Product>> updateProduct(HttpSession session, int id, Product product) {
		ResponseStructure<Product> structure = new ResponseStructure<>();
		if (session.getAttribute("merchant") == null) {
			structure.setData(null);
			structure.setMessage("Login Again");
			structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
			return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
		} else {
			Merchant merchant1 = (Merchant) session.getAttribute("merchant");
			Merchant merchant = merchantDao.findByEmail(merchant1.getEmail());
			session.setAttribute("merchant", merchant);
			if (merchant.getProducts() == null || merchant.getProducts().isEmpty()) {
				structure.setMessage("Unable to Update The Product");
				structure.setData(product);
				structure.setStatus_code(HttpStatus.UNAUTHORIZED.value());
				return new ResponseEntity<>(structure, HttpStatus.UNAUTHORIZED);
			} else {
				product.setImage(merchantDao.findProductById(product.getId()).getImage());
				product.setStatus(merchantDao.findProductById(product.getId()).isStatus());
				productRepository.save(product);
				structure.setData(product);
				structure.setStatus_code(HttpStatus.ACCEPTED.value());
				structure.setMessage("Product updated succesfull");
				return new ResponseEntity<>(structure, HttpStatus.ACCEPTED);
			}

		}

	}
	
	public ResponseEntity<ResponseStructure<Merchant>>sendForgotOtp(String email){
		ResponseStructure<Merchant> structure=new ResponseStructure<>();
		Merchant merchant=merchantDao.findByEmail(email);
		if(merchant==null) {
			structure.setData(merchant);
			structure.setMessage(merchant.getEmail()+"Email doesn't exits,create account first");
			structure.setStatus_code(HttpStatus.BAD_REQUEST.value());
			return new ResponseEntity<>(structure,HttpStatus.BAD_REQUEST);
		} else {
			int otp=new Random().nextInt(100000,999999);
			merchant.setOtp(otp);
			merchant.setOtpgeneratedtime(LocalDateTime.now());
			
			if(mail.sendOtp(merchant)) {
				Merchant merchant2=merchantDao.save(merchant);
				structure.setData(merchant2);
				structure.setStatus_code(HttpStatus.OK.value());
				structure.setMessage(merchant2.getEmail()+"OTP send succesfull,check once");
				return new ResponseEntity<>(structure,HttpStatus.OK);
			}else {
				structure.setData(null);
				structure.setStatus_code(HttpStatus.NOT_ACCEPTABLE.value());
				structure.setMessage("Something went Wrong, Check email and try again");
				return new ResponseEntity<>(structure, HttpStatus.NOT_ACCEPTABLE);
			}
		}
	}
	
	
	public ResponseEntity<ResponseStructure<Merchant>> submitForgotOtp(String email, int otp) {
		ResponseStructure<Merchant> structure = new ResponseStructure<>();
		Merchant merchant = merchantDao.findByEmail(email);

		if (merchant != null && merchant.getOtp() == otp) {
			LocalDateTime otpGeneratedTime = merchant.getOtpgeneratedtime();
			LocalDateTime currentTime = LocalDateTime.now();
			Duration duration = Duration.between(otpGeneratedTime, currentTime);

			if (duration.toMinutes() <= 5) {
				merchant.setStatus(true);
				merchant.setOtp(0);
				merchantDao.save(merchant);
				structure.setData(merchant);
				structure.setMessage("Account Verified Successfully");
				structure.setStatus_code(HttpStatus.ACCEPTED.value());
			} else {
				structure.setData(null);
				structure.setMessage("OTP has expired.");
				structure.setStatus_code(HttpStatus.NOT_ACCEPTABLE.value());
			}
		} else {
			structure.setData(null);
			structure.setMessage("Incorrect OTP");
			structure.setStatus_code(HttpStatus.NOT_ACCEPTABLE.value());
		}

		return new ResponseEntity<>(structure, HttpStatus.OK);
	}


	
	public ResponseEntity<ResponseStructure<Merchant>> resendForgotOtp(String email) {
		ResponseStructure<Merchant> structure = new ResponseStructure<>();
		Merchant merchant = merchantDao.findByEmail(email);
		if(merchant!=null) {
		int otp = new Random().nextInt(100000, 999999);
		merchant.setOtp(otp);

		if (mail.sendOtp(merchant)) {
			Merchant merchant2 = merchantDao.save(merchant);
			structure.setData(merchant);
			structure.setMessage("Otp resend Success");
			structure.setStatus_code(HttpStatus.ACCEPTED.value());
			return new ResponseEntity<>(structure, HttpStatus.ACCEPTED);
		} else {
			structure.setData(null);
			structure.setMessage("Something went Wrong, Check email and try again");
			structure.setStatus_code(HttpStatus.NOT_ACCEPTABLE.value());
			return new ResponseEntity<>(structure, HttpStatus.NOT_ACCEPTABLE);
		}
		}else {
			structure.setData(null);
			structure.setMessage("Email not found,check once ");
			structure.setStatus_code(HttpStatus.NOT_ACCEPTABLE.value());
			return new ResponseEntity<>(structure, HttpStatus.NOT_ACCEPTABLE);
		}
	}
	public ResponseEntity<ResponseStructure<Merchant>> setPassword(String email, String password) {
		ResponseStructure<Merchant> structure = new ResponseStructure<>();
		Merchant merchant = merchantDao.findByEmail(email);
		merchant.setPassword(password);
		merchantDao.save(merchant);
		structure.setData(merchant);
		structure.setMessage("Password Reset Success");
		structure.setStatus_code(HttpStatus.CREATED.value());
		return new ResponseEntity<>(structure, HttpStatus.CREATED);
	}
}
