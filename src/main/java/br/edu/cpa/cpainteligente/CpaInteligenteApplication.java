package br.edu.cpa.cpainteligente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CpaInteligenteApplication {

	public static void main(String[] args) {
		SpringApplication.run(CpaInteligenteApplication.class, args);
	}

}
