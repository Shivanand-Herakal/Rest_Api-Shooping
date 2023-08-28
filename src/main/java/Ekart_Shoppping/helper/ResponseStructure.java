package Ekart_Shoppping.helper;

import java.util.List;

import Ekart_Shoppping.dto.Product;
import lombok.Data;

@Data
public class ResponseStructure <T>{

 String Message;
 int Status_code;
 T Data;
}
