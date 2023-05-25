package com.dkp.dk;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import kr.co.bootpay.Bootpay;

@SpringBootTest
class DkProjectApplicationTests {

	final Bootpay bootpay = new Bootpay("646f378a3049c8001df8befc", "hjIFSp0BFQNhOljWUDKYa8tefBwOXgew2tU8Ke1fi/4=");
	
	@Test
	void 토큰_발급() {
		
		try {
		    HashMap<String, Object> res = bootpay.getAccessToken();
		    if(res.get("error_code") == null) { //success
		        System.out.println("goGetToken success: " + res);
		    } else {
		        System.out.println("goGetToken false: " + res);
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}
	
	@Test
	void 결제_단건_조회() {
		
		try {
		    HashMap<String, Object> token = bootpay.getAccessToken();
		    if(token.get("error_code") != null) { //failed
		        return;
		    }
		    String receiptId = "646f4d5a9f326b002058baef"; 
		    HashMap<String, Object> res = bootpay.getReceipt(receiptId);
		    if(res.get("error_code") == null) { //success
		        System.out.println("confirm success: " + res);
		    } else {
		        System.out.println("confirm false: " + res);
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}

}
