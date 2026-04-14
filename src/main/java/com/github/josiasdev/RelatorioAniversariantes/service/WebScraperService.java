package com.github.josiasdev.RelatorioAniversariantes.service;

import com.github.josiasdev.RelatorioAniversariantes.dto.AniversarianteDTO;
import com.github.josiasdev.RelatorioAniversariantes.dto.CasamentoDTO;
import com.github.josiasdev.RelatorioAniversariantes.dto.DadosRelatorioDTO;
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

    public DadosRelatorioDTO extrairTodosOsDados(LocalDate startOfWeek, LocalDate endOfWeek) throws InterruptedException {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");

        WebDriver driver = new ChromeDriver(options);
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));
            realizarLogin(driver, wait);

            navegarPara(driver, "https://church15.churchsoftware.com.br/ctrtipoaniversario/ctrtipoaniversario.php");
            preencherEBuscarAniversariantes(driver, wait, startOfWeek, endOfWeek, "Membros");
            List<AniversarianteDTO> membros = extrairTabelaAniversariantes(driver);

            navegarPara(driver, "https://church15.churchsoftware.com.br/ctrtipoaniversario/ctrtipoaniversario.php");
            preencherEBuscarAniversariantes(driver, wait, startOfWeek, endOfWeek, "Congregados");
            List<AniversarianteDTO> congregados = extrairTabelaAniversariantes(driver);

            navegarPara(driver, "https://church15.churchsoftware.com.br/rptCasaAniv/rptCasaAniv.php");
            preencherEBuscarCasamentos(driver, wait, startOfWeek, endOfWeek);
            List<CasamentoDTO> casamentos = extrairTabelaCasamentos(driver);

            return new DadosRelatorioDTO(membros, congregados, casamentos);

        } finally {
            System.out.println("Processo finalizado. Fechando o navegador...");
            driver.quit();
        }
    }

    private void realizarLogin(WebDriver driver, WebDriverWait wait) {
        System.out.println("Acessando a página de login...");
        driver.get("https://church15.churchsoftware.com.br/frmlogin/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("codcliente"))).sendKeys(clientCode);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Próximo')]"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]")).click();
    }

    private void navegarPara(WebDriver driver, String url) throws InterruptedException {
        System.out.println("Navegando para: " + url);
        TimeUnit.SECONDS.sleep(2);
        driver.get(url);
        TimeUnit.SECONDS.sleep(2);
    }

    private void preencherEBuscarAniversariantes(WebDriver driver, WebDriverWait wait, LocalDate startOfWeek, LocalDate endOfWeek, String tipoRelatorio) throws InterruptedException {
        System.out.println("Buscando Aniversariantes de: " + tipoRelatorio);
        WebElement selectTipo = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("id_sc_field_tipo")));
        new Select(selectTipo).selectByVisibleText(tipoRelatorio);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("sc_OK_bot"))).click();

        WebElement regionalElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("id_sc_field_setor")));
        Select selectRegional = new Select(regionalElement);
        for (WebElement option : selectRegional.getOptions()) { selectRegional.selectByValue(option.getAttribute("value")); }

        TimeUnit.SECONDS.sleep(5);

        Select selectCongregacao = new Select(driver.findElement(By.id("id_sc_field_congregacao")));
        for (WebElement option : selectCongregacao.getOptions()) { selectCongregacao.selectByValue(option.getAttribute("value")); }

        String monthNamePt = startOfWeek.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        driver.findElement(By.id("id_sc_field_dia1")).sendKeys(String.valueOf(startOfWeek.getDayOfMonth()));
        driver.findElement(By.id("id_sc_field_dia2")).sendKeys(String.valueOf(endOfWeek.getDayOfMonth()));
        new Select(driver.findElement(By.id("id_sc_field_mes"))).selectByVisibleText(monthNamePt.substring(0, 1).toUpperCase() + monthNamePt.substring(1));
        new Select(driver.findElement(By.id("id_sc_field_situacao"))).selectByVisibleText("Ativo");

        wait.until(ExpectedConditions.elementToBeClickable(By.id("sub_form_b"))).click();
    }

    private void preencherEBuscarCasamentos(WebDriver driver, WebDriverWait wait, LocalDate startOfWeek, LocalDate endOfWeek) throws InterruptedException {
        System.out.println("Buscando Aniversariantes de Casamento...");

        WebElement congregacaoElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("SC_tblcongregacoes_nome")));
        Select selectCongregacao = new Select(congregacaoElement);
        for (WebElement option : selectCongregacao.getOptions()) {
            selectCongregacao.selectByValue(option.getAttribute("value"));
        }

        driver.findElement(By.id("SC_dia")).sendKeys(String.valueOf(startOfWeek.getDayOfMonth()));
        driver.findElement(By.id("SC_dia_input_2")).sendKeys(String.valueOf(endOfWeek.getDayOfMonth()));

        WebElement mesElement = driver.findElement(By.id("SC_mes"));
        String monthNamePt = startOfWeek.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        new Select(mesElement).selectByVisibleText(monthNamePt.substring(0, 1).toUpperCase() + monthNamePt.substring(1));

        WebElement situacaoElement = driver.findElement(By.id("SC_tblmembros_situacaomembro"));
        new Select(situacaoElement).selectByVisibleText("Ativo");

        System.out.println("Clicando no botão de Pesquisa do Casamento...");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("sc_b_pesq_bot"))).click();
    }

    private List<AniversarianteDTO> extrairTabelaAniversariantes(WebDriver driver) throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
        List<WebElement> linhas = driver.findElements(By.xpath("//tr[@class='scGridFieldOdd' or @class='scGridFieldEven']"));
        return linhas.stream().map(linha -> {
            List<WebElement> celulas = linha.findElements(By.tagName("td"));
            if (celulas.size() > 5) {
                return new AniversarianteDTO(celulas.get(1).getText(), celulas.get(2).getText(), celulas.get(3).getText(), celulas.get(5).getText());
            }
            return null;
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());
    }

    private List<CasamentoDTO> extrairTabelaCasamentos(WebDriver driver) throws InterruptedException {
        System.out.println("Extraindo dados da tabela de Casamentos...");
        TimeUnit.SECONDS.sleep(3);

        List<WebElement> linhas = driver.findElements(By.xpath("//tr[contains(@class, 'scGridFieldOdd') or contains(@class, 'scGridFieldEven')]"));

        return linhas.stream().map(linha -> {
            List<WebElement> celulas = linha.findElements(By.tagName("td"));

            if (celulas.size() >= 4) {
                return new CasamentoDTO(
                        celulas.get(0).getText(),
                        celulas.get(2).getText(),
                        celulas.get(3).getText()
                );
            }
            return null;
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());
    }
}