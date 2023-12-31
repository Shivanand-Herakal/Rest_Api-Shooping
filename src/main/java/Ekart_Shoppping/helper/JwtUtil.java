package Ekart_Shoppping.helper;


import java.time.Duration;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import Ekart_Shoppping.dto.Customer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
@Component
public class JwtUtil {

	@Autowired
	SecretKeyGeneratory keyGenerator;

	private static final long EXPIRATION_TIME_MS = 3600000;

	private static final String SECRET_KEY = new SecretKeyGeneratory().key();

	public String generateJwtTokenForCustomer(Customer customer, Duration duration) {
		Date date=new Date();
		Date expiration = new Date(date.getTime() + duration.toMillis());

		String alphanumericCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		StringBuilder randomString = new StringBuilder();
		Random random = new Random();
		int length = 10; // You can adjust the length of the random string

		for (int i = 0; i < length; i++) {
			int index = random.nextInt(alphanumericCharacters.length());
			char randomChar = alphanumericCharacters.charAt(index);
			randomString.append(randomChar);
		}

		String token = Jwts.builder().setId(UUID.randomUUID().toString()).setSubject(randomString.toString())
				.setIssuedAt(date).setExpiration(expiration).signWith(SignatureAlgorithm.HS256, SECRET_KEY).compact();

		return token;
	}

	public boolean isTokenExpired(String token) {
		Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
		Date expirationDate = claims.getExpiration();
		Date currentDate = new Date();
		return expirationDate.before(currentDate);
	}
}
