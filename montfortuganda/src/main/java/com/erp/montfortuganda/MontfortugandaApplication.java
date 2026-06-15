package com.erp.montfortuganda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // Added for secure record lifecycle tracking
public class MontfortugandaApplication {
	public static void main(String[] args) {
		SpringApplication.run(MontfortugandaApplication.class, args);
	}
}