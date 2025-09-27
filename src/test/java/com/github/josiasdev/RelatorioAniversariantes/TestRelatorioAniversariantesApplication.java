package com.github.josiasdev.RelatorioAniversariantes;

import org.springframework.boot.SpringApplication;

public class TestRelatorioAniversariantesApplication {

	public static void main(String[] args) {
		SpringApplication.from(RelatorioAniversariantesApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
