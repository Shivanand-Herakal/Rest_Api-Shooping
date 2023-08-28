package Ekart_Shoppping.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Ekart_Shoppping.dto.Wishlist;

public interface WishListReposirory  extends JpaRepository<Wishlist, Integer>{

	Wishlist findByName(String name);
}
