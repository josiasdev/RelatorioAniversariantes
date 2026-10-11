package com.github.josiasdev.RelatorioAniversariantes;

import com.github.josiasdev.RelatorioAniversariantes.config.ScraperProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(ScraperProperties.class)
@SpringBootApplication
public class RelatorioAniversariantesApplication {

	public static void main(String[] args) {
		SpringApplication.run(RelatorioAniversariantesApplication.class, args);
	}

}
