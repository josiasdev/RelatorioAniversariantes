package com.github.josiasdev.RelatorioAniversariantes.service;

import com.github.josiasdev.RelatorioAniversariantes.dto.AniversarianteDTO;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class RelatorioAniversariantesService {

    @Value("${app.credentials.client-code}")
    private String clientCode;

    @Value("${app.credentials.username}")
    private String username;

    @Value("${app.credentials.password}")
    private String password;

    public String gerarRelatorioAniversariantes() throws IOException, InterruptedException {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1280,720");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));

        try {
            LocalDate today = LocalDate.now();
            LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            int startDay = startOfWeek.getDayOfMonth();
            int endDay = endOfWeek.getDayOfMonth();
            String monthNamePt = startOfWeek.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));

            System.out.println("Buscando aniversariantes para o mês de: " + monthNamePt);
            System.out.println("Período da semana: de " + startDay + " a " + endDay);


            // Login
            System.out.println("Acessando a página de login...");
            driver.get("https://church15.churchsoftware.com.br/frmlogin/");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("codcliente"))).sendKeys(clientCode);
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Próximo')]"))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys(username);
            driver.findElement(By.id("password")).sendKeys(password);
            driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]")).click();

            // Navegação
            System.out.println("Navegando para relatórios...");
            wait.until(ExpectedConditions.elementToBeClickable(By.id("modulo_nome"))).click();
            TimeUnit.SECONDS.sleep(2);
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='../blkSiteMap/blkSiteMap.php?glo_modulo=Secretaria']"))).click();

            // Interagindo dentro do iframe
            System.out.println("Mudando para o iframe...");
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("mnuMainXI_13_iframe")));
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[normalize-space(text())='Relatórios']"))).click();
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='../ctrtipoaniversario/ctrtipoaniversario.php']"))).click();

        }
    }
}
