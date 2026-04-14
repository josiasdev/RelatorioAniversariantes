package com.github.josiasdev.RelatorioAniversariantes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(new Server().url("http://localhost:8080").description("Servidor Local")))
                .info(new Info()
                        .title("Church Report Automation API")
                        .description("Sistema inteligente para extração, processamento e geração de relatórios " +
                                "automatizados a partir da plataforma Church Software.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Josias Batista")
                                .email("josiasmartins098@gmail.com")
                                .url("https://www.linkedin.com/in/josias-batista/"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}