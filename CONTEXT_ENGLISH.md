# Project Context

## Overview

RelatorioAniversariantes is a Java 17 and Spring Boot application that automates the weekly birthday and wedding anniversary report workflow for a church. It accesses the Church management platform (specifically at `https://church15.churchsoftware.com.br/frmlogin/`) through browser automation, collects data for members, congregants, and wedding anniversaries, processes the result, generates a consolidated PDF, and sends the document through WhatsApp using a local Evolution API instance.

The project is intended to reduce repetitive manual work and produce a ready-to-share weekly report, distinguishing automatically between members, congregants, and wedding anniversaries, grouped by congregation and headquarters.

## Main Responsibilities

- Trigger report generation through an HTTP API endpoint (`GET /relatorios/gerarAniversariantes`).
- Run the same report generation automatically every Monday at 08:00 in the America/Fortaleza timezone (`@Scheduled`).
- Use Selenium WebDriver with headless Chrome to access the external church management system.
- Handle cross-month week ranges seamlessly by dividing queries day-by-day when a week spans across two consecutive months.
- Extract member birthdays, congregant birthdays, and wedding anniversaries for the current week (including congregation details).
- Sort report entries chronologically relative to the start of the week.
- Remove duplicated wedding anniversary couples by normalizing spouse order.
- Support multi-page results scraping and dynamic form interactions via JavaScript DOM execution.
- Generate a formatted PDF with multi-page table splitting and repeated table headers.
- Send the generated PDF to WhatsApp through Evolution API.

## Technology Stack

- **Java 17** and **Spring Boot 3.5.x**
- **Maven** (Dependency management)
- **Spring Web**, **Spring Scheduling**, **Spring Async**
- **Selenium WebDriver** and **WebDriverManager** (Browser automation)
- **OpenPDF** (PDF creation and formatting)
- **Apache POI**
- **SpringDoc OpenAPI / Swagger UI** (Interactive API documentation)
- **Lombok** (Boilerplate reduction)
- **Docker Compose** for the Evolution API service

## Architecture

The architecture follows an MVC (Model-View-Controller) pattern adapted for an API, prioritizing the Single Responsibility Principle (SOLID).

```text
src/main/java/com/github/josiasdev/RelatorioAniversariantes/
├── config/           # Global configurations (CORS, Documentation, Security)
├── controller/       # API endpoints and documentation
├── dto/              # Data Transfer Objects (Entity structures)
└── service/          # Business logic, orchestration, scraping, PDF generation & WhatsApp
```

### Main Components

- `RelatorioAniversariantesApplication`: Spring Boot entry point. Enables async execution and scheduled tasks.
- `RelatorioAniversariantesController`: exposes the report generation endpoint under `/relatorios`. Validates input and delegates work to services asynchronously.
- `RelatorioAniversariantesService`: orchestrates the top-level report workflow, triggering scheduled jobs, deduplicating wedding couples, generating the PDF file, and sending it via WhatsApp.
- `RelatorioSemanalService`: handles date range calculations for weekly reports. If a week crosses a month boundary (e.g., June 29 to July 5), it performs day-by-day queries to avoid missing data, consolidates data sets, and sorts all entries chronologically relative to the week's Monday.
- `WebScraperService`: handles Selenium setup, login, navigation, atomic form select execution via JavaScript, multi-page pagination navigation, and raw data extraction.
- `PdfService`: handles document design and formatting. Generates 4-column tables for members, congregants, and marriage anniversaries (including congregation info), configuring page split behavior and repeated table headers.
- `WhatsAppService`: reads the generated PDF, converts it to Base64, and sends it as a document through Evolution API with robust error handling.
- DTOs (`AniversarianteDTO`, `CasamentoDTO`, `DadosRelatorioDTO`): represent extracted birthday, wedding anniversary (including congregation), and consolidated report data.

## API

The main endpoint is:

```text
GET /relatorios/gerarAniversariantes
```

Expected behavior:
- Returns HTTP `202 Accepted` when the process starts.
- Runs the report generation asynchronously.
- Writes the generated PDF to the project root using this naming pattern:
  `relatorio_aniversariantes_YYYY-MM-DD.pdf`

Swagger UI is available when the application is running:
`http://localhost:8080/swagger-ui.html`

## Runtime Flow

1. Calculate current week range (Monday to Sunday).
2. Start headless Chrome through Selenium.
3. Authenticate into the external church management system using credentials from `application.properties`.
4. Fetch data via `RelatorioSemanalService`: single search if within the same month, or day-by-day queries if crossing a month boundary.
5. `WebScraperService` selects search options via atomic JavaScript execution, handles pagination across multi-page table results, and extracts data including congregation info.
6. Sort all extracted lists by day relative to the week's Monday.
7. Normalize spouse name order to remove duplicate wedding anniversary couples.
8. Generate the final PDF with `PdfService` featuring formatted 4-column tables.
9. Send the PDF through WhatsApp to the configured number.
10. Close the browser session.

## Docker / WhatsApp Integration

The application integrates with WhatsApp via [Evolution API](https://doc.evolution-api.com/v2/pt/get-started/introduction). 

The `docker/docker-compose.yml` file starts an Evolution API container on local port `8081`. 
The API key must be provided through a local `.env` file containing `EVOLUTION_API_KEY=...` in the `docker/` directory, which must not be committed.

To connect your number:
1. Start the container (`docker compose up -d`).
2. Access the Evolution Manager at `http://localhost:8081/manager`.
3. Create an instance named `igreja` using the `Baileys` integration and your API key.
4. Connect the instance by scanning the QR code with your WhatsApp app.

## Security And Configuration Notes

- Do not document, commit, or expose credentials, API keys, phone numbers, authentication values, or local secret values.
- Keep runtime configuration private and environment-specific within `src/main/resources/application.properties` and the `docker/.env` file.
- Treat generated PDFs as potentially sensitive because they contain personal data.

## Operational Requirements

- Java 17 or newer
- Maven
- Google Chrome installed on the host operating system
- Docker, when WhatsApp sending is needed
- Valid private credentials for the external church management system
- A configured Evolution API instance connected to WhatsApp

## Maintenance Notes

- **Web Scraping Vulnerability**: The scraping code depends on the external platform HTML structure. If that platform changes element IDs, table classes, or navigation flow, `WebScraperService` will need updates.
- **Cross-Month Logic**: The `RelatorioSemanalService` handles multi-month week transitions automatically.
- **Scheduled Task**: The scheduled task runs every Monday at 08:00 in the `America/Fortaleza` timezone.
