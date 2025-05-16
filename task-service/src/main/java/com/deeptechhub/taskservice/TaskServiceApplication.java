package com.deeptechhub.taskservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableFeignClients
@EnableMethodSecurity
public class TaskServiceApplication {

	public static void main(String[] args) {

		SpringApplication.run(TaskServiceApplication.class, args);
	}

}
