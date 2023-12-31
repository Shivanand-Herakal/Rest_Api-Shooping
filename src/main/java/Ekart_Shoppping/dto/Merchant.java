package Ekart_Shoppping.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Entity
@Data
@Validated
@Component
public class Merchant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@NotBlank(message = "Name must not be blank.")
	private String name;
	
	@Email(message = "Please provide a valid email address.")
	private String email;

	@Digits(integer = 10, fraction = 0, message = "Mobile number should have exactly 10 digits.")
	private long mobile;

	@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$", message = "Password must be at least 8 characters long and contain at least one lowercase letter, one uppercase letter, one digit, and one special character.")
	private String password;

	@NotNull(message = "Date of birth must not be null.")
	private LocalDate dob;

	@NotBlank(message = "Gender must not be blank.")
	private String gender;

	@NotBlank(message = "Address must not be blank.")
	private String address;

//	@Digits(integer = 6, fraction = 0, message = "OTP should have exactly 6 digits.")
	private int otp;

//	@AssertTrue(message = "Status must be true.")
	private boolean status;
	
	private LocalDateTime otpgeneratedtime;

	@Lob
	@Column(columnDefinition = "MEDIUMBLOB")
	private byte[] picture;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	List<Product> products;

}
