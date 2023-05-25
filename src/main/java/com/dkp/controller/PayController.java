package com.dkp.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dkp.pay.BootPayModule;

@RestController
public class PayController {
	
	BootPayModule bootpay;
	
	@PostMapping("/pay")
	public String doPay() { 
		//TODO 결제로직 작성 중
		bootpay.payMain();
		return "";
	}

}
