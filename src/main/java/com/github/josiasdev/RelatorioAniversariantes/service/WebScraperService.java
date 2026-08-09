package com.github.josiasdev.RelatorioAniversariantes.service;

import com.github.josiasdev.RelatorioAniversariantes.dto.AniversarianteDTO;
import com.github.josiasdev.RelatorioAniversariantes.dto.CasamentoDTO;
import com.github.josiasdev.RelatorioAniversariantes.dto.DadosRelatorioDTO;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class WebScraperService {
    private static final int MAX_TENTATIVAS_SELECAO = 3;
    private static final By CAMPO_REGIONAIS = By.id("id_sc_field_setor");
    private static final By CAMPO_CONGREGACOES_ANIVERSARIANTES = By.id("id_sc_field_congregacao");
    private static final By CAMPO_CONGREGACOES_CASAMENTOS = By.id("SC_tblcongregacoes_nome");

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

        selecionarTodasOpcoesValidas(driver, wait, CAMPO_REGIONAIS, "regionais");
        aguardarCampoPronto(wait, CAMPO_CONGREGACOES_ANIVERSARIANTES, "congregacoes");
        selecionarTodasOpcoesValidas(driver, wait, CAMPO_CONGREGACOES_ANIVERSARIANTES, "congregacoes");

        String monthNamePt = startOfWeek.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        driver.findElement(By.id("id_sc_field_dia1")).sendKeys(String.valueOf(startOfWeek.getDayOfMonth()));
        driver.findElement(By.id("id_sc_field_dia2")).sendKeys(String.valueOf(endOfWeek.getDayOfMonth()));
        new Select(driver.findElement(By.id("id_sc_field_mes"))).selectByVisibleText(monthNamePt.substring(0, 1).toUpperCase() + monthNamePt.substring(1));
        new Select(driver.findElement(By.id("id_sc_field_situacao"))).selectByVisibleText("Ativo");

        wait.until(ExpectedConditions.elementToBeClickable(By.id("sub_form_b"))).click();
    }

    private void preencherEBuscarCasamentos(WebDriver driver, WebDriverWait wait, LocalDate startOfWeek, LocalDate endOfWeek) throws InterruptedException {
        System.out.println("Buscando Aniversariantes de Casamento...");

        aguardarCampoPronto(wait, CAMPO_CONGREGACOES_CASAMENTOS, "congregacoes de casamento");
        selecionarTodasOpcoesValidas(driver, wait, CAMPO_CONGREGACOES_CASAMENTOS, "congregacoes de casamento");

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
        List<AniversarianteDTO> resultados = new ArrayList<>();
        Set<String> registrosUnicos = new HashSet<>();
        Set<String> paginasVisitadas = new HashSet<>();

        int pagina = 1;
        while (pagina <= 100) {
            TimeUnit.SECONDS.sleep(3);
            String assinaturaPagina = obterAssinaturaPagina(driver);
            if (!paginasVisitadas.add(assinaturaPagina)) {
                System.out.println("Pagina repetida detectada em aniversariantes. Encerrando paginacao.");
                break;
            }

            List<WebElement> linhas = driver.findElements(By.xpath("//tr[@class='scGridFieldOdd' or @class='scGridFieldEven']"));
            int totalAntes = resultados.size();

            for (WebElement linha : linhas) {
                AniversarianteDTO aniversariante = extrairAniversarianteDaLinha(linha);
                if (aniversariante != null && registrosUnicos.add(criarChaveAniversariante(aniversariante))) {
                    resultados.add(aniversariante);
                }
            }

            System.out.printf("Pagina %d de aniversariantes: %d novos registros.%n", pagina, resultados.size() - totalAntes);

            if (!clicarProximaPaginaSeExistir(driver)) {
                break;
            }

            pagina++;
        }

        System.out.println("Total de aniversariantes extraidos: " + resultados.size());
        return resultados;
    }

    private List<CasamentoDTO> extrairTabelaCasamentos(WebDriver driver) throws InterruptedException {
        System.out.println("Extraindo dados da tabela de Casamentos...");
        List<CasamentoDTO> resultados = new java.util.ArrayList<>();
        Set<String> registrosUnicos = new HashSet<>();
        Set<String> paginasVisitadas = new HashSet<>();

        int pagina = 1;
        while (pagina <= 100) {
            TimeUnit.SECONDS.sleep(3);
            String assinaturaPagina = obterAssinaturaPagina(driver);
            if (!paginasVisitadas.add(assinaturaPagina)) {
                System.out.println("Pagina repetida detectada em casamentos. Encerrando paginacao.");
                break;
            }

            List<WebElement> tbodies = driver.findElements(By.xpath("//tbody[starts-with(@id, 'tbody_rptCasaAniv_')]"));

            String congregacaoAtual = "Sede"; // Valor padrão inicial
            int totalAntes = resultados.size();

            for (WebElement tbody : tbodies) {
                String id = tbody.getAttribute("id");

                if (id != null && id.endsWith("_top")) {
                    try {
                        // Scriptcase gera a congregação no _top dentro da classe scGridBlockFont
                        WebElement tdCongregacao = tbody.findElement(By.xpath(".//td[contains(@class, 'scGridBlockFont')]//tr[1]/td[3]"));
                        congregacaoAtual = tdCongregacao.getText().replace("&nbsp;", "").trim();
                    } catch (Exception e) {
                        System.out.println("Aviso: Não encontrou a congregação no bloco _top. Mantendo anterior: " + congregacaoAtual);
                    }
                } else if (id != null && id.endsWith("_bot")) {
                    // O _bot contém os casais
                    List<WebElement> linhas = tbody.findElements(By.xpath(".//tr[contains(@class, 'scGridFieldOdd') or contains(@class, 'scGridFieldEven')]"));

                    for (WebElement linha : linhas) {
                        CasamentoDTO casamento = extrairCasamentoDaLinha(linha, congregacaoAtual);
                        if (casamento != null && registrosUnicos.add(criarChaveCasamento(casamento))) {
                            resultados.add(casamento);
                        }
                    }
                }
            }

            System.out.printf("Pagina %d de casamentos: %d novos registros.%n", pagina, resultados.size() - totalAntes);

            if (!clicarProximaPaginaSeExistir(driver)) {
                break;
            }

            pagina++;
        }

        System.out.println("Total de casamentos extraidos: " + resultados.size());
        return resultados;
    }

    private AniversarianteDTO extrairAniversarianteDaLinha(WebElement linha) {
        List<WebElement> celulas = linha.findElements(By.tagName("td"));
        if (celulas.size() > 5) {
            String diaBruto = celulas.get(1).getText().trim();
            String diaLimpo = diaBruto.replaceAll("^(\\d{1,2}).*", "$1");

            return new AniversarianteDTO(
                    diaLimpo,
                    celulas.get(2).getText(),
                    celulas.get(3).getText(),
                    celulas.get(5).getText()
            );
        }

        return null;
    }

    private CasamentoDTO extrairCasamentoDaLinha(WebElement linha, String congregacaoAtual) {
        List<WebElement> celulas = linha.findElements(By.tagName("td"));
        if (celulas.size() >= 4) {
            return new CasamentoDTO(
                    celulas.get(0).getText(),
                    celulas.get(2).getText(),
                    celulas.get(3).getText(),
                    congregacaoAtual
            );
        }

        return null;
    }

    private boolean clicarProximaPaginaSeExistir(WebDriver driver) throws InterruptedException {
        List<By> seletoresProximaPagina = List.of(
                By.xpath("//*[contains(@onclick, \"nm_gp_move('avanca'\") or contains(@onclick, 'nm_gp_move(\"avanca\"') or contains(@onclick, 'avanca')]"),
                By.xpath("//*[starts-with(@id, 'forward_') or starts-with(@id, 'sc_next_') or contains(@id, '_next_') or contains(@id, 'avanca') or contains(@id, 'forward')]"),
                By.xpath("//*[contains(translate(@title, 'ÁÀÂÃÉÊÍÓÔÕÚÇáàâãéêíóôõúç', 'AAAAEEIOOOUCaaaaeeiooouc'), 'Proxima') or contains(translate(@title, 'ÁÀÂÃÉÊÍÓÔÕÚÇáàâãéêíóôõúç', 'AAAAEEIOOOUCaaaaeeiooouc'), 'Avancar')]"),
                By.xpath("//*[.//img[contains(translate(@title, 'ÁÀÂÃÉÊÍÓÔÕÚÇáàâãéêíóôõúç', 'AAAAEEIOOOUCaaaaeeiooouc'), 'Proxima') or contains(translate(@alt, 'ÁÀÂÃÉÊÍÓÔÕÚÇáàâãéêíóôõúç', 'AAAAEEIOOOUCaaaaeeiooouc'), 'Proxima') or contains(translate(@title, 'ÁÀÂÃÉÊÍÓÔÕÚÇáàâãéêíóôõúç', 'AAAAEEIOOOUCaaaaeeiooouc'), 'Avancar') or contains(translate(@alt, 'ÁÀÂÃÉÊÍÓÔÕÚÇáàâãéêíóôõúç', 'AAAAEEIOOOUCaaaaeeiooouc'), 'Avancar')]]")
        );

        for (By seletor : seletoresProximaPagina) {
            for (WebElement elemento : driver.findElements(seletor)) {
                if (podeClicarNaProximaPagina(elemento)) {
                    System.out.println("Avancando para a proxima pagina do resultado...");
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", elemento);
                    TimeUnit.SECONDS.sleep(3);
                    return true;
                }
            }
        }

        return false;
    }

    private void aguardarCampoPronto(WebDriverWait wait, By seletor, String descricao) {
        wait.until(driver -> {
            try {
                WebElement campo = driver.findElement(seletor);
                return campo.isDisplayed()
                        && campo.isEnabled()
                        && !campo.findElements(By.tagName("option")).isEmpty();
            } catch (StaleElementReferenceException e) {
                return false;
            }
        });
        System.out.printf("Campo de %s carregado.%n", descricao);
    }

    private void selecionarTodasOpcoesValidas(WebDriver driver, WebDriverWait wait, By seletor, String descricao) {
        StaleElementReferenceException ultimoErro = null;

        for (int tentativa = 1; tentativa <= MAX_TENTATIVAS_SELECAO; tentativa++) {
            try {
                WebElement selectElement = wait.until(ExpectedConditions.elementToBeClickable(seletor));
                long selecionadas = selecionarOpcoesDeFormaAtomica(driver, selectElement);
                System.out.printf("Selecionadas %d opcoes em %s.%n", selecionadas, descricao);
                return;
            } catch (StaleElementReferenceException e) {
                ultimoErro = e;
                System.out.printf("Campo %s foi atualizado durante a selecao (tentativa %d de %d). Tentando novamente.%n",
                        descricao, tentativa, MAX_TENTATIVAS_SELECAO);
                aguardarCampoPronto(wait, seletor, descricao);
            }
        }

        throw ultimoErro;
    }

    private long selecionarOpcoesDeFormaAtomica(WebDriver driver, WebElement selectElement) {
        Object resultado = ((JavascriptExecutor) driver).executeScript(
                "const select = arguments[0];" +
                        "const opcoes = Array.from(select.options);" +
                        "if (select.multiple) {" +
                        "  const validas = opcoes.filter(option => option.value.trim() !== '' && option.text.trim().toLocaleLowerCase('pt-BR') !== 'selecione');" +
                        "  opcoes.forEach(option => option.selected = false);" +
                        "  validas.forEach(option => option.selected = true);" +
                        "  select.dispatchEvent(new Event('input', { bubbles: true }));" +
                        "  select.dispatchEvent(new Event('change', { bubbles: true }));" +
                        "  select.dispatchEvent(new Event('blur', { bubbles: true }));" +
                        "  return validas.length;" +
                        "}" +
                        "const opcaoGeral = opcoes.find(option => {" +
                        "  const texto = option.text.trim().toLocaleLowerCase('pt-BR');" +
                        "  return texto.includes('todos') || texto.includes('todas') || option.value.trim() === '0';" +
                        "});" +
                        "if (!opcaoGeral) return 0;" +
                        "select.value = opcaoGeral.value;" +
                        "select.dispatchEvent(new Event('input', { bubbles: true }));" +
                        "select.dispatchEvent(new Event('change', { bubbles: true }));" +
                        "select.dispatchEvent(new Event('blur', { bubbles: true }));" +
                        "return 1;",
                selectElement
        );

        return ((Number) resultado).longValue();
    }

    private boolean podeClicarNaProximaPagina(WebElement elemento) {
        try {
            String textoCompleto = (
                    obterAtributo(elemento, "id") + " " +
                            obterAtributo(elemento, "class") + " " +
                            obterAtributo(elemento, "title") + " " +
                            obterAtributo(elemento, "aria-disabled") + " " +
                            obterAtributo(elemento, "disabled")
            ).toLowerCase(Locale.ROOT);

            return elemento.isDisplayed()
                    && elemento.isEnabled()
                    && !textoCompleto.contains("disabled")
                    && !textoCompleto.contains("inativo");
        } catch (Exception e) {
            return false;
        }
    }

    private String obterAssinaturaPagina(WebDriver driver) {
        List<WebElement> linhas = driver.findElements(By.xpath("//tr[contains(@class, 'scGridFieldOdd') or contains(@class, 'scGridFieldEven')]"));
        return linhas.stream()
                .map(WebElement::getText)
                .collect(Collectors.joining("|"));
    }

    private String criarChaveAniversariante(AniversarianteDTO aniversariante) {
        return String.join("|",
                limparTexto(aniversariante.getDia()),
                limparTexto(aniversariante.getNome()),
                limparTexto(aniversariante.getIdade()),
                limparTexto(aniversariante.getCongregacao())
        );
    }

    private String criarChaveCasamento(CasamentoDTO casamento) {
        return String.join("|",
                limparTexto(casamento.getDia()),
                limparTexto(casamento.getCasal()),
                limparTexto(casamento.getDataCasamento()),
                limparTexto(casamento.getCongregacao())
        );
    }

    private String limparTexto(String texto) {
        return texto == null ? "" : texto.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    private String obterAtributo(WebElement elemento, String atributo) {
        String valor = elemento.getAttribute(atributo);
        return valor == null ? "" : valor;
    }
}
