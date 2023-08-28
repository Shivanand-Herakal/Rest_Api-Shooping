package Ekart_Shoppping.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import Ekart_Shoppping.dto.Admin;

public interface AdminRepository extends JpaRepository<Admin, Integer> {

	@Query("Select count(a) from Admin a")
	int countuser(String username,String password);

	
	Admin findByUsername (String username);


	


}
