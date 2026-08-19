package com.ncv.razorpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
public class RazorpayApplication {

	public static void main(String[] args) {
		SpringApplication.run(RazorpayApplication.class, args);
	}

}
