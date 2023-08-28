package Ekart_Shoppping.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import Ekart_Shoppping.dto.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
 
	Product findByName(String name);

	List<Product> findByStatus(boolean flag);
}
