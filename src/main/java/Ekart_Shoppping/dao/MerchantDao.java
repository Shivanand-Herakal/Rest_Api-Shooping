package Ekart_Shoppping.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import Ekart_Shoppping.dto.Merchant;
import Ekart_Shoppping.dto.Product;
import Ekart_Shoppping.repository.MerchantRepository;
import Ekart_Shoppping.repository.ProductRepository;



@Repository
public class MerchantDao {

	
	@Autowired
	MerchantRepository merchantRepository;
	
	@Autowired
	ProductRepository productRepository;

	public Merchant findByEmail(String email) {
		return merchantRepository.findByEmail(email);
	}

	public Merchant findByMobile(long mobile) {
		return merchantRepository.findByMobile(mobile);
	}

	public Merchant save(Merchant merchant) {
		return merchantRepository.save(merchant);
	}

	public Product findProductByName(String name) {
		return productRepository.findByName(name);
		}

		public Product findProductById(int id) {
			return productRepository.findById(id).orElse(null);
		}

		public void removeProduct(Product product) {
			productRepository.delete(product);
		}

	}

