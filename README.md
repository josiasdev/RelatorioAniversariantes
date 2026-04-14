# 🤖 Relatório de Aniversariantes Automatizado

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen.svg)
![Selenium](https://img.shields.io/badge/Selenium-WebDriver-blue.svg)
![Status](https://img.shields.io/badge/Status-Concluído-success.svg)

Oi! Este projeto foi criado para centralizar e automatizar a emissão de relatórios semanais da igreja. O objetivo é evitar o trabalho manual repetitivo, tendo um "robô" que acessa o sistema, extrai os dados e monta um documento pronto para impressão e compartilhamento.

Ele faz a distinção automática entre membros, congregados e aniversariantes de casamento, agrupando as informações por congregação e pela Sede.

> ⚠️ **Aviso Legal:** Este projeto utiliza automação web (Web Scraping). Ele foi desenvolvido para uso pessoal/institucional visando otimização de tempo. O código pode precisar de ajustes futuros caso a plataforma alvo sofra atualizações na sua interface (HTML/CSS).

---

### 🎯 Sobre o sistema alvo (Church)

Toda a automação atua de forma transparente na plataforma [Church](https://church15.churchsoftware.com.br/frmlogin/).

O Church é um sistema de gestão online para igrejas considerado um dos mais completos do Brasil, desenvolvido em diversos módulos para integração de vários setores (ideal para pequenas, médias e grandes igrejas).

Alguns dos recursos que a plataforma oferece incluem:
* **Secretaria completa:** Cadastro de membros, congregados, controle de aniversariantes, aplicativo Church Digital.
* **Tesouraria e Financeiro:** Entradas, saídas, conciliação bancária, contribuições.
* **Outros recursos:** Departamento Infantil, contagem de culto, backup de dados, 100% online.

---

### ⚙️ O que o robô faz exatamente?

Quando acionado via API, o sistema realiza os seguintes passos em segundo plano:

1. Abre o navegador Google Chrome de forma invisível (`headless`).
2. Faz o login no sistema usando as credenciais configuradas.
3. Navega de forma direta para as telas de relatórios de Membros, Congregados e Casamentos.
4. Preenche as datas da semana atual e aplica os filtros necessários via formulário.
5. Extrai os dados em tempo real das tabelas de resultado.
6. Processa e limpa as informações (ex: remoção de duplicidade de casais e ordenação alfabética).
7. Gera um **arquivo PDF final consolidado**, com cabeçalho personalizado e todas as listas formatadas.

---

### 💻 Tecnologias utilizadas

A base do projeto é construída com ferramentas robustas do ecossistema Java:

* **Java 17** e **Spring Boot 3.5** (Framework base)
* **Maven** (Gerenciador de dependências)
* **Selenium WebDriver + WebDriverManager** (Automação do navegador e extração de dados)
* **OpenPDF** (Criação e formatação do arquivo PDF)
* **SpringDoc OpenAPI / Swagger UI** (Documentação interativa da API)
* **Lombok** (Redução de boilerplate de código)

---

### 📂 Estrutura do Projeto

A arquitetura foi dividida focando em responsabilidade única (SOLID):
* `config/` ➔ Configurações do Swagger e documentação OpenAPI.
* `dto/` ➔ Classes de transporte de dados (`AniversarianteDTO`, `CasamentoDTO`).
* `service/` ➔ Regras de negócio e orquestração:
    * `WebScraperService`: Focado apenas em navegar e extrair os dados.
    * `PdfService`: Focado apenas em "desenhar" o PDF.
    * `RelatorioAniversariantesService`: Orquestra o web scraper e a geração de PDF, além de limpar os dados.

---

### 🚀 Como colocar para rodar na sua máquina

**1. Pré-requisitos:**
* Java 17 (ou superior) instalado.
* Maven instalado.
* Navegador Google Chrome instalado no sistema operacional.

**2. Configurando as senhas:**
As senhas não ficam expostas no código. Vá até a pasta `src/main/resources` e abra o arquivo `application.properties`. Insira suas credenciais:

```properties
spring.application.name=RelatorioAniversariantes

# Credenciais para o login no sistema Church
app.credentials.client-code=SEU_CODIGO_DE_CLIENTE
app.credentials.username=SEU_USUARIO
app.credentials.password=SUA_SENHA
```

**3. Compilando e executando:**

Abra um terminal na pasta principal do projeto e baixe as dependências:

```bash
mvn clean install
```

**Execute a aplicação:**
```bash
java -jar target/RelatorioAniversariantes-0.0.1-SNAPSHOT.jar
```

### 🕹️ Como usar a API
A forma mais fácil e visual de testar a automação é usando o Swagger.

Com o projeto rodando, abra o seu navegador e acesse:

👉 [LocalHost](http://localhost:8080/swagger-ui.html)

Abra o endpoint de geração de relatórios.

Clique no botão "Try it out" e depois em "Execute".

O servidor vai responder imediatamente. 

Acompanhe o console da sua IDE/Terminal para ver o robô trabalhando.

Quando finalizado, o arquivo relatorio_aniversariantes_ANO-MES-DIA.pdf aparecerá na pasta raiz do projeto!

### 🔮 Próximos Passos (Roadmap)
[ ] Implementar rotina de agendamento automático (@Scheduled) para rodar toda segunda-feira de manhã.

[ ] Integrar envio automático do PDF gerado via WhatsApp API.

[ ] Adicionar testes unitários para a lógica de filtro e deduplicação de dados.