package Ekart_Shoppping.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Ekart_Shoppping.dto.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

	Customer findByEmail(String email);

	Object findByMobile(long mobile);

}
