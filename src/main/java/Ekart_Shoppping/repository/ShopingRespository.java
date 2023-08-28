package Ekart_Shoppping.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Ekart_Shoppping.dto.Shoppingcart;

public interface ShopingRespository extends JpaRepository<Shoppingcart, Integer> {

}
