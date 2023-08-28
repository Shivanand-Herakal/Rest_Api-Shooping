package Ekart_Shoppping.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import Ekart_Shoppping.dto.Merchant;


public interface MerchantRepository extends JpaRepository<Merchant, String> {

	Merchant findByEmail(String name);

	Merchant findByMobile(long mobile);

}
