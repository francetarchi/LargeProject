package com.wineadvisor.wineadvisor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
public class WineAdvisorApplication {

	public static void main(String[] args) {
		SpringApplication.run(WineAdvisorApplication.class, args);

		System.out.println("\n\n------------- WineAdvisor Application Started -------------");
		System.out.println("\n\n");
	}

}
