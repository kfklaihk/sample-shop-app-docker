package com.docker.atsea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages={"com.docker.atsea"})
@EntityScan("com.docker.atsea.model")
public class AtSeaApp {
	
	public static void main(String[] args) {
		SpringApplication.run(AtSeaApp.class, args);
	}
}
