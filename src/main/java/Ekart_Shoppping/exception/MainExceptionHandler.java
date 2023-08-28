package Ekart_Shoppping.exception;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import Ekart_Shoppping.helper.ResponseStructure;

@ControllerAdvice
public class MainExceptionHandler {

	@ExceptionHandler(ArithmeticException.class)
	public ResponseEntity<ResponseStructure<String>> handle(ArithmeticException exception) {
		ResponseStructure<String> structure = new ResponseStructure<>();
		structure.setStatus_code(HttpStatus.BAD_REQUEST.value());
		structure.setMessage("There is an Arithmetic exception");
		structure.setData(exception.getMessage());

		return new ResponseEntity<ResponseStructure<String>>(structure, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(NoZeroException.class)
	public ResponseEntity<ResponseStructure<String>> handle(NoZeroException exception) {
		ResponseStructure<String> structure = new ResponseStructure<>();
		structure.setStatus_code(HttpStatus.BAD_REQUEST.value());
		structure.setMessage("There is an NoZero exception");
		structure.setData(exception.getMessage());

		return new ResponseEntity<ResponseStructure<String>>(structure, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(IOException.class)
	public ResponseEntity<ResponseStructure<String>> handle(IOException exception) {
		ResponseStructure<String> structure = new ResponseStructure<>();
		structure.setStatus_code(HttpStatus.BAD_REQUEST.value());
		structure.setMessage("File Not Supported");
		structure.setData(exception.getMessage());

		return new ResponseEntity<ResponseStructure<String>>(structure, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<ResponseStructure<String>> handle(NullPointerException exception) {
		ResponseStructure<String> structure = new ResponseStructure<>();
		structure.setStatus_code(HttpStatus.BAD_REQUEST.value());
		structure.setMessage("Null");
		structure.setData(exception.getMessage());

		return new ResponseEntity<ResponseStructure<String>>(structure, HttpStatus.BAD_REQUEST);
	}
}
