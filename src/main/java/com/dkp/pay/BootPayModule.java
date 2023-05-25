package com.dkp.pay;

import java.util.HashMap;
import kr.co.bootpay.Bootpay;

public class BootPayModule {
	
	final Bootpay bootpay = new Bootpay("646f378a3049c8001df8befc", "hjIFSp0BFQNhOljWUDKYa8tefBwOXgew2tU8Ke1fi/4=");

	public void payMain() {
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
}


