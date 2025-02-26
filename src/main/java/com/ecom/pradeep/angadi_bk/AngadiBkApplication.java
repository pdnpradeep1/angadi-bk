package com.ecom.pradeep.angadi_bk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AngadiBkApplication {

	public static void main(String[] args) {
		SpringApplication.run(AngadiBkApplication.class, args);
	}

}
