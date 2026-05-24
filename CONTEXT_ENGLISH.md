# Project Context

## Overview

RelatorioAniversariantes is a Java 17 and Spring Boot application that automates the weekly birthday report workflow for a church. It accesses the Church management platform through browser automation, collects data for members, congregants, and wedding anniversaries, processes the result, generates a consolidated PDF, and sends the document through WhatsApp using a local Evolution API instance.

The project is intended to reduce repetitive manual work and produce a ready-to-share weekly report.

## Main Responsibilities

- Trigger report generation through an HTTP API endpoint.
- Run the same report generation automatically every Monday at 08:00 in the America/Fortaleza timezone.
- Use Selenium WebDriver with headless Chrome to access the external church management system.
- Extract member birthdays, congregant birthdays, and wedding anniversaries for the current week.
- Sort report entries by day.
- Remove duplicated wedding anniversary couples by normalizing spouse order.
- Generate a formatted PDF with a header image when available.
- Send the generated PDF to WhatsApp through Evolution API.

## Technology Stack

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

## Architecture

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

## Main Components

- `RelatorioAniversariantesApplication`: Spring Boot entry point. Enables async execution and scheduled tasks.
- `RelatorioAniversariantesController`: exposes the report generation endpoint under `/relatorios`.
- `RelatorioAniversariantesService`: orchestrates the full workflow: date range calculation, scraping, sorting, de-duplication, PDF generation, and WhatsApp delivery.
- `WebScraperService`: handles Selenium setup, login, navigation, form filling, and table extraction.
- `PdfService`: creates the final PDF with sections for members, congregants, and wedding anniversaries.
- `WhatsAppService`: reads the generated PDF, converts it to Base64, and sends it as a document through Evolution API.
- DTOs: represent extracted birthday, wedding anniversary, and consolidated report data.

## API

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

## Runtime Flow

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

## Docker / WhatsApp Integration

The `docker/docker-compose.yml` file starts an Evolution API container on local port `8081`. The API key must be provided through an environment variable or local `.env` file and must not be committed.

## Security And Configuration Notes

- Do not document, commit, or expose credentials, API keys, phone numbers, authentication values, or local secret values.
- Do not copy values from local Spring configuration files into documentation, examples, logs, issues, commits, or pull requests.
- Keep runtime configuration private and environment-specific.
- Treat generated PDFs as potentially sensitive because they contain personal data.

## Development Commands

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

## Operational Requirements

- Java 17 or newer
- Maven
- Google Chrome installed
- Docker, when WhatsApp sending is needed
- Valid private credentials for the external church management system
- A configured Evolution API instance connected to WhatsApp

## Maintenance Notes

- The scraping code depends on the external platform HTML structure. If that platform changes element IDs, table classes, or navigation flow, `WebScraperService` may need updates.
- The weekly report currently uses the current date to determine the Monday-Sunday range.
- The scheduled task runs every Monday at 08:00 in the America/Fortaleza timezone.
- PDF formatting is centralized in `PdfService`.
- Report delivery is centralized in `WhatsAppService`.
