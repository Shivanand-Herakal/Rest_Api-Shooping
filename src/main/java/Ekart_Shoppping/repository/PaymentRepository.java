package Ekart_Shoppping.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Ekart_Shoppping.dto.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

	Payment findByName(String name);

}
