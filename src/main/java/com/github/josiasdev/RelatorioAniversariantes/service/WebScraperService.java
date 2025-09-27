package com.github.josiasdev.RelatorioAniversariantes.service;

import com.github.josiasdev.RelatorioAniversariantes.dto.AniversarianteDTO;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class WebScraperService {
    @Value("${app.credentials.client-code}")
    private String clientCode;

    @Value("${app.credentials.username}")
    private String username;

    @Value("${app.credentials.password}")
    private String password;

    public List<AniversarianteDTO> extrairAniversariantesDaSemana(LocalDate startOfWeek, LocalDate endOfWeek) throws InterruptedException {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=800,600");

        WebDriver driver = new ChromeDriver(options);
        try{
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));


        }
    }

    private void realizarLogin(WebDriver driver, WebDriverWait wait){
        System.out.println("Acessando a página de login...");
        driver.get("https://church15.churchsoftware.com.br/frmlogin/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("codcliente"))).sendKeys(clientCode);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Próximo')]"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]")).click();
    }

}
