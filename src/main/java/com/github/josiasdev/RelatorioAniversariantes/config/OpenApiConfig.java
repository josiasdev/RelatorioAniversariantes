package com.github.josiasdev.RelatorioAniversariantes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Geração de Relatórios")
                        .description("API responsável pela automação da geração de relatórios de aniversariantes da igreja.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Josias Batista")
                                .email("josiasmartins098@gmail.com")
                                .url("https://www.linkedin.com/in/josias-batista/"))
                );
    }

}
