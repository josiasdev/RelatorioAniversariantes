# Project Context

## Overview

RelatorioAniversariantes is a Java 17 and Spring Boot application that automates the weekly birthday and wedding anniversary report workflow for a church. It accesses the Church management platform (specifically at `https://church15.churchsoftware.com.br/frmlogin/`) through browser automation, collects data for members, congregants, and wedding anniversaries, processes the result, generates a consolidated PDF, and sends the document through WhatsApp using a local Evolution API instance.

The project is intended to reduce repetitive manual work and produce a ready-to-share weekly report, distinguishing automatically between members, congregants, and wedding anniversaries, grouped by congregation and headquarters.

## Main Responsibilities

- Trigger report generation through an HTTP API endpoint (`GET /relatorios/gerarAniversariantes`).
- Expose an execution monitoring endpoint (`GET /relatorios/status`) to check real-time status (`IDLE`, `EM_PROGRESSO`, `SUCESSO`, `ERRO`), duration, record totals, and error details.
- Run the report generation automatically every Monday at 08:00 in the America/Fortaleza timezone (`@Scheduled`).
- Use Selenium WebDriver with headless Chrome to access the external church management system, with retry logic for login stability.
- Handle cross-month week ranges seamlessly by dividing queries day-by-day when a week spans across two consecutive months.
- Extract member birthdays, congregant birthdays, and wedding anniversaries for the current week (including congregation details).
- Sort report entries chronologically relative to the start of the week.
- Remove duplicated wedding anniversary couples by normalizing spouse order.
- Support multi-page results scraping and dynamic form interactions via JavaScript DOM execution.
- Generate a formatted PDF in a dedicated output directory (`relatorios/`) with multi-page table splitting and repeated table headers.
- Perform automated cleanup (`RelatorioLimpezaService`) for PDF reports older than configured retention days (default: 30 days).
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
└── service/          # Business logic, orchestration, scraping, tracking, retention & WhatsApp
```

### Main Components

- `RelatorioAniversariantesApplication`: Spring Boot entry point. Enables async execution and scheduled tasks.
- `RelatorioAniversariantesController`: exposes report generation (`GET /relatorios/gerarAniversariantes`) and execution monitoring (`GET /relatorios/status`). Validates input and delegates work asynchronously.
- `RelatorioAniversariantesService`: orchestrates the top-level report workflow, triggering scheduled jobs, deduplicating wedding couples, generating the PDF file, updating tracker status, and sending via WhatsApp.
- `RelatorioExecucaoTrackerService`: tracks in-memory execution state (`IDLE`, `EM_PROGRESSO`, `SUCESSO`, `ERRO`), timestamps, duration, output path, and record totals.
- `RelatorioLimpezaService`: automatically purges generated PDF reports older than configured retention days from the `relatorios/` directory.
- `RelatorioSemanalService`: handles date range calculations for weekly reports. If a week crosses a month boundary (e.g., June 29 to July 5), it performs day-by-day queries to avoid missing data, consolidates data sets, and sorts all entries chronologically relative to the week's Monday.
- `WebScraperService`: handles Selenium setup, login retries, navigation, atomic form select execution via JavaScript, multi-page pagination navigation, and raw data extraction with clean driver shutdown.
- `PdfService`: handles document design and formatting. Creates output folder (`relatorios/`), generates 4-column tables for members, congregants, and marriage anniversaries (including congregation info), configuring page split behavior and repeated table headers.
- `WhatsAppService`: reads the generated PDF from the output directory, converts it to Base64, and sends it as a document through Evolution API with robust error handling.
- DTOs (`AniversarianteDTO`, `CasamentoDTO`, `DadosRelatorioDTO`): represent extracted birthday, wedding anniversary (including congregation), and consolidated report data.

## API

Main endpoints under `/relatorios`:

```text
GET /relatorios/gerarAniversariantes
GET /relatorios/status
```

Expected behavior:
- `GET /relatorios/gerarAniversariantes`: Returns HTTP `202 Accepted` when the process starts and runs report generation asynchronously.
- `GET /relatorios/status`: Returns HTTP `200 OK` with JSON containing current execution status, start/end timestamps, duration, generated file path, and record metrics.
- PDF output path pattern: `relatorios/relatorio_aniversariantes_YYYY-MM-DD.pdf`

Swagger UI is available when the application is running:
`http://localhost:8080/swagger-ui.html`

## Runtime Flow

1. Calculate current week range (Monday to Sunday).
2. Update tracker state to `EM_PROGRESSO`.
3. Start headless Chrome through Selenium with login retry logic.
4. Authenticate into the external church management system using credentials from `application.properties`.
5. Fetch data via `RelatorioSemanalService`: single search if within the same month, or day-by-day queries if crossing a month boundary.
6. `WebScraperService` selects search options via atomic JavaScript execution, handles pagination across multi-page table results, and extracts data including congregation info.
7. Sort all extracted lists by day relative to the week's Monday.
8. Normalize spouse name order to remove duplicate wedding anniversary couples.
9. Generate the final PDF with `PdfService` saved in the `relatorios/` folder.
10. Send the PDF through WhatsApp to the configured number.
11. Update tracker state to `SUCESSO` (or `ERRO` on failure).
12. Close browser session cleanly in a `finally` block.

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
- **Report Retention**: `RelatorioLimpezaService` runs nightly at 03:00 to purge PDFs older than retention threshold.
- **Scheduled Task**: The scheduled task runs every Monday at 08:00 in the `America/Fortaleza` timezone.
