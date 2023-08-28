package Ekart_Shoppping.dto;



import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Data
@Entity
@Component
public class ShoppingOrder {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int id;
	private String paymentMode;
	private String address;
	private double totalPrice;
	private LocalDateTime deliveryDate;
	private String orderId;
	private String transactionId;
	private String currency;
	private String payment_key;
	private String company_name;
	private String status;

	@OneToMany
	private List<Item> items;

	
		
	
}
