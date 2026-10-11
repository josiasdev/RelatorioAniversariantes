package com.github.josiasdev.RelatorioAniversariantes.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.scraper")
public class ScraperProperties {

    private Urls urls = new Urls();
    private long waitSeconds = 100;
    private long sleepMillisNavigation = 2000;
    private long sleepMillisPagination = 3000;

    @Getter
    @Setter
    public static class Urls {
        private String login;
        private String aniversarios;
        private String casamentos;
    }
}
