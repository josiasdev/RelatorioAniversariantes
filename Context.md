# Project Context / Contexto do Projeto

## English

### Overview

RelatorioAniversariantes is a Java 17 and Spring Boot application that automates the weekly birthday report workflow for a church. It accesses the Church management platform through browser automation, collects data for members, congregants, and wedding anniversaries, processes the result, generates a consolidated PDF, and sends the document through WhatsApp using a local Evolution API instance.

The project is intended to reduce repetitive manual work and produce a ready-to-share weekly report.

### Main Responsibilities

- Trigger report generation through an HTTP API endpoint.
- Run the same report generation automatically every Monday at 08:00 in the America/Fortaleza timezone.
- Use Selenium WebDriver with headless Chrome to access the external church management system.
- Extract member birthdays, congregant birthdays, and wedding anniversaries for the current week.
- Sort report entries by day.
- Remove duplicated wedding anniversary couples by normalizing spouse order.
- Generate a formatted PDF with a header image when available.
- Send the generated PDF to WhatsApp through Evolution API.

### Technology Stack

- Java 17
- Spring Boot 3.5.x
- Maven
- Spring Web
- Spring Scheduling
- Spring Async
- Selenium WebDriver
- WebDriverManager
- OpenPDF
- Apache POI
- SpringDoc OpenAPI / Swagger UI
- Lombok
- Docker Compose for the Evolution API service

### Architecture

The application follows a simple API-oriented MVC/service structure.

```text
src/main/java/com/github/josiasdev/RelatorioAniversariantes/
├── RelatorioAniversariantesApplication.java
├── config/
│   └── OpenApiConfig.java
├── controller/
│   └── RelatorioAniversariantesController.java
├── dto/
│   ├── AniversarianteDTO.java
│   ├── CasamentoDTO.java
│   └── DadosRelatorioDTO.java
└── service/
    ├── PdfService.java
    ├── RelatorioAniversariantesService.java
    ├── WebScraperService.java
    └── WhatsAppService.java
```

### Main Components

- `RelatorioAniversariantesApplication`: Spring Boot entry point. Enables async execution and scheduled tasks.
- `RelatorioAniversariantesController`: exposes the report generation endpoint under `/relatorios`.
- `RelatorioAniversariantesService`: orchestrates the full workflow: date range calculation, scraping, sorting, de-duplication, PDF generation, and WhatsApp delivery.
- `WebScraperService`: handles Selenium setup, login, navigation, form filling, and table extraction.
- `PdfService`: creates the final PDF with sections for members, congregants, and wedding anniversaries.
- `WhatsAppService`: reads the generated PDF, converts it to Base64, and sends it as a document through Evolution API.
- DTOs: represent extracted birthday, wedding anniversary, and consolidated report data.

### API

The main endpoint is:

```text
GET /relatorios/gerarAniversariantes
```

Expected behavior:

- Returns HTTP `202 Accepted` when the process starts.
- Runs the report generation asynchronously.
- Writes the generated PDF to the project root using this naming pattern:

```text
relatorio_aniversariantes_YYYY-MM-DD.pdf
```

Swagger UI is available when the application is running:

```text
http://localhost:8080/swagger-ui.html
```

### Runtime Flow

1. Calculate the current week from Monday to Sunday.
2. Start headless Chrome through Selenium.
3. Authenticate into the external church management system.
4. Collect member birthdays for the week.
5. Collect congregant birthdays for the week.
6. Collect wedding anniversaries for the week.
7. Sort all lists by day.
8. Normalize and remove duplicated wedding anniversary couples.
9. Generate the final PDF.
10. Send the PDF through WhatsApp.
11. Close the browser session.

### Docker / WhatsApp Integration

The `docker/docker-compose.yml` file starts an Evolution API container on local port `8081`. The API key must be provided through an environment variable or local `.env` file and must not be committed.

### Security And Configuration Notes

- Do not document, commit, or expose credentials, API keys, phone numbers, authentication values, or local secret values.
- Do not copy values from local Spring configuration files into documentation, examples, logs, issues, commits, or pull requests.
- Keep runtime configuration private and environment-specific.
- Treat generated PDFs as potentially sensitive because they contain personal data.

### Development Commands

Build the project:

```bash
mvn clean install
```

Run the packaged application:

```bash
java -jar target/RelatorioAniversariantes-0.0.1-SNAPSHOT.jar
```

Run tests:

```bash
mvn test
```

Start Evolution API:

```bash
cd docker
docker compose up -d
```

### Operational Requirements

- Java 17 or newer
- Maven
- Google Chrome installed
- Docker, when WhatsApp sending is needed
- Valid private credentials for the external church management system
- A configured Evolution API instance connected to WhatsApp

### Maintenance Notes

- The scraping code depends on the external platform HTML structure. If that platform changes element IDs, table classes, or navigation flow, `WebScraperService` may need updates.
- The weekly report currently uses the current date to determine the Monday-Sunday range.
- The scheduled task runs every Monday at 08:00 in the America/Fortaleza timezone.
- PDF formatting is centralized in `PdfService`.
- Report delivery is centralized in `WhatsAppService`.

---

## Português

### Visão Geral

RelatorioAniversariantes é uma aplicação Java 17 com Spring Boot que automatiza o fluxo semanal de relatórios de aniversariantes de uma igreja. Ela acessa a plataforma de gestão Church por automação de navegador, coleta dados de membros, congregados e aniversariantes de casamento, processa o resultado, gera um PDF consolidado e envia o documento pelo WhatsApp usando uma instância local da Evolution API.

O objetivo do projeto é reduzir trabalho manual repetitivo e gerar um relatório semanal pronto para compartilhamento.

### Responsabilidades Principais

- Disparar a geração do relatório por meio de um endpoint HTTP.
- Executar a mesma geração automaticamente toda segunda-feira às 08:00 no fuso America/Fortaleza.
- Usar Selenium WebDriver com Chrome em modo headless para acessar o sistema externo da igreja.
- Extrair aniversariantes membros, aniversariantes congregados e aniversariantes de casamento da semana atual.
- Ordenar os registros por dia.
- Remover casais duplicados nos aniversários de casamento normalizando a ordem dos cônjuges.
- Gerar um PDF formatado com imagem de cabeçalho quando disponível.
- Enviar o PDF gerado para o WhatsApp por meio da Evolution API.

### Tecnologias

- Java 17
- Spring Boot 3.5.x
- Maven
- Spring Web
- Spring Scheduling
- Spring Async
- Selenium WebDriver
- WebDriverManager
- OpenPDF
- Apache POI
- SpringDoc OpenAPI / Swagger UI
- Lombok
- Docker Compose para o serviço da Evolution API

### Arquitetura

A aplicação segue uma estrutura simples orientada a API, com controller, serviços e DTOs.

```text
src/main/java/com/github/josiasdev/RelatorioAniversariantes/
├── RelatorioAniversariantesApplication.java
├── config/
│   └── OpenApiConfig.java
├── controller/
│   └── RelatorioAniversariantesController.java
├── dto/
│   ├── AniversarianteDTO.java
│   ├── CasamentoDTO.java
│   └── DadosRelatorioDTO.java
└── service/
    ├── PdfService.java
    ├── RelatorioAniversariantesService.java
    ├── WebScraperService.java
    └── WhatsAppService.java
```

### Componentes Principais

- `RelatorioAniversariantesApplication`: ponto de entrada Spring Boot. Habilita execução assíncrona e tarefas agendadas.
- `RelatorioAniversariantesController`: expõe o endpoint de geração de relatório em `/relatorios`.
- `RelatorioAniversariantesService`: orquestra o fluxo completo: cálculo do período, scraping, ordenação, remoção de duplicidade, geração do PDF e envio pelo WhatsApp.
- `WebScraperService`: cuida da configuração do Selenium, login, navegação, preenchimento de formulários e extração das tabelas.
- `PdfService`: cria o PDF final com seções para membros, congregados e aniversariantes de casamento.
- `WhatsAppService`: lê o PDF gerado, converte o arquivo para Base64 e envia como documento pela Evolution API.
- DTOs: representam os dados extraídos de aniversariantes, casamentos e relatório consolidado.

### API

O endpoint principal é:

```text
GET /relatorios/gerarAniversariantes
```

Comportamento esperado:

- Retorna HTTP `202 Accepted` quando o processo é iniciado.
- Executa a geração do relatório de forma assíncrona.
- Grava o PDF gerado na raiz do projeto usando este padrão de nome:

```text
relatorio_aniversariantes_YYYY-MM-DD.pdf
```

Com a aplicação em execução, o Swagger UI fica disponível em:

```text
http://localhost:8080/swagger-ui.html
```

### Fluxo de Execução

1. Calcula a semana atual de segunda a domingo.
2. Inicia o Chrome headless pelo Selenium.
3. Autentica no sistema externo de gestão da igreja.
4. Coleta os aniversariantes membros da semana.
5. Coleta os aniversariantes congregados da semana.
6. Coleta os aniversariantes de casamento da semana.
7. Ordena todas as listas por dia.
8. Normaliza e remove casais duplicados.
9. Gera o PDF final.
10. Envia o PDF pelo WhatsApp.
11. Fecha a sessão do navegador.

### Docker / Integração WhatsApp

O arquivo `docker/docker-compose.yml` inicia um container da Evolution API na porta local `8081`. A chave da API deve ser fornecida por variável de ambiente ou arquivo `.env` local e não deve ser versionada.

### Segurança E Configuração

- Não documentar, commitar ou expor credenciais, chaves de API, números de telefone, valores de autenticação ou segredos locais.
- Não copiar valores de arquivos locais de configuração Spring para documentação, exemplos, logs, issues, commits ou pull requests.
- Manter a configuração de execução privada e específica por ambiente.
- Tratar PDFs gerados como dados potencialmente sensíveis, pois eles contêm informações pessoais.

### Comandos De Desenvolvimento

Compilar o projeto:

```bash
mvn clean install
```

Executar a aplicação empacotada:

```bash
java -jar target/RelatorioAniversariantes-0.0.1-SNAPSHOT.jar
```

Executar testes:

```bash
mvn test
```

Subir a Evolution API:

```bash
cd docker
docker compose up -d
```

### Requisitos Operacionais

- Java 17 ou superior
- Maven
- Google Chrome instalado
- Docker, quando o envio por WhatsApp for necessário
- Credenciais privadas válidas para o sistema externo de gestão da igreja
- Instância da Evolution API configurada e conectada ao WhatsApp

### Notas De Manutenção

- O scraping depende da estrutura HTML da plataforma externa. Se a plataforma alterar IDs de elementos, classes de tabela ou fluxo de navegação, o `WebScraperService` pode precisar de ajustes.
- O relatório semanal usa a data atual para calcular o período de segunda a domingo.
- A tarefa agendada roda toda segunda-feira às 08:00 no fuso America/Fortaleza.
- A formatação do PDF fica centralizada no `PdfService`.
- O envio do relatório fica centralizado no `WhatsAppService`.
