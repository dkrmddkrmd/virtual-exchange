package com.example.virtual_exchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VirtualExchangeApplication {

	public static void main(String[] args) {
		SpringApplication.run(VirtualExchangeApplication.class, args);
	}

}
