package com.vodafoneziggo.assignment.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 * Bootstraps Spring Boot, scans components, and starts the embedded server.
 */
@SpringBootApplication
public class OrderApiApplication {

    // Launches the Spring application context
	public static void main(String[] args) {
		SpringApplication.run(OrderApiApplication.class, args);
	}

}
