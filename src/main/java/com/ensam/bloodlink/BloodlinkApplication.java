package com.ensam.bloodlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // necessaire pour la tache quotidienne d'expiration des poches (BF24)
public class BloodlinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(BloodlinkApplication.class, args);
	}
}