package com.github.josiasdev.RelatorioAniversariantes.service;

import com.github.josiasdev.RelatorioAniversariantes.dto.AniversarianteDTO;

import io.github.bonigarcia.wdm.WebDriverManager;
import net.bytebuddy.asm.Advice;
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
            realizarLogin(driver, wait);
            navegarParaFormularioDeAniversariantes(driver, wait);
            preencherEBuscar(driver, wait, startOfWeek, endOfWeek);

            return extrairDadosDaTabela(driver);
        } finally {
            System.out.println("Fechando o navegador...");
            driver.quit();
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

    private void navegarParaFormularioDeAniversariantes(WebDriver driver, WebDriverWait wait) throws InterruptedException{
        System.out.println("Navegando para o formulário de relatórios...");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("modulo_nome"))).click();
        TimeUnit.SECONDS.sleep(2);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='../blkSiteMap/blkSiteMap.php?glo_modulo=Secretaria']"))).click();

        System.out.println("Entrando no iframe e selecionando o relatório...");
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("mnuMainXI_13_iframe")));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[normalize-space(text())='Relatórios']"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='../ctrtipoaniversario/ctrtipoaniversario.php']"))).click();
    }

    private void preencherEBuscar(WebDriver driver, WebDriverWait wait, LocalDate startOfWeek, LocalDate endOfWeek)  throws InterruptedException {
        System.out.println("Preenchendo o formulário de busca...");
        new Select(wait.until(ExpectedConditions.presenceOfElementLocated(By.id("id_sc_field_tipo")))).selectByValue("1");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("sc_OK_bot"))).click();

        Select selectRegional = new Select(driver.findElement(By.id("id_sc_field_setor")));
        for (WebElement option : selectRegional.getOptions()) {
            selectRegional.selectByValue(option.getAttribute("value"));
        }
        TimeUnit.SECONDS.sleep(10);
        Select selectCongregacao = new Select(driver.findElement(By.id("id_sc_field_congregacao")));
        for (WebElement option : selectCongregacao.getOptions()) {
            selectCongregacao.selectByValue(option.getAttribute("value"));
        }

        String monthNamePt = startOfWeek.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        driver.findElement(By.id("id_sc_field_dia1")).sendKeys(String.valueOf(startOfWeek.getDayOfMonth()));
        driver.findElement(By.id("id_sc_field_dia2")).sendKeys(String.valueOf(endOfWeek.getDayOfMonth()));
        new Select(driver.findElement(By.id("id_sc_field_mes"))).selectByVisibleText(monthNamePt.substring(0, 1).toUpperCase() + monthNamePt.substring(1));
        new Select(driver.findElement(By.id("id_sc_field_situacao"))).selectByVisibleText("Ativo");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("sub_form_b"))).click();
    }
    private List<AniversarianteDTO> extrairDadosDaTabela(WebDriver driver) throws InterruptedException {
        System.out.println("Extraindo dados da tabela...");
        TimeUnit.SECONDS.sleep(3);
        List<WebElement> linhas = driver.findElements(By.xpath("//tr[@class='scGridFieldOdd' or @class='scGridFieldEven']"));
        return linhas.stream()
                .map(linha -> {
                    List<WebElement> celulas = linha.findElements(By.tagName("td"));
                    if (celulas.size() > 5) {
                        return new AniversarianteDTO(
                                celulas.get(1).getText(),
                                celulas.get(2).getText(),
                                celulas.get(3).getText(),
                                celulas.get(5).getText()
                        );
                    }
                    return null;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }
}