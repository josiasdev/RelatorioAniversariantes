# 🤖 Automated Church Birthday & Anniversary Report

Read this in other languages: [Português](README_pt_BR.md)

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen.svg)
![Selenium](https://img.shields.io/badge/Selenium-WebDriver-blue.svg)
![Status](https://img.shields.io/badge/Status-Completed-success.svg)

This application automates the generation and delivery of weekly church reports for member birthdays, congregant birthdays, and wedding anniversaries. By leveraging headless web automation, intelligent date range handling, PDF rendering, execution tracking, and WhatsApp integration, it removes manual administrative tasks.

The application automatically categorizes members, congregants, and wedding anniversaries, grouping entries by congregation and headquarters.

> ⚠️ **Disclaimer:** This project uses browser automation (Web Scraping). It was built for personal and institutional workflow optimization. Code adjustments may be required if the target platform updates its HTML structure or navigation layout.

---

### 🎯 About the Target Platform (Church)

The automation interacts directly with the online church management platform [Church](https://church15.churchsoftware.com.br/frmlogin/).

Church is a comprehensive cloud-based church management system used across Brazil to integrate administrative and pastoral operations:
* **Secretariat & Membership:** Member records, congregants, birthday tracking, and digital tools.
* **Financial Management:** Income, expenses, bank reconciliation, and tithes/offerings.
* **Additional Features:** Children's department, attendance counters, online backups.

---

### ⚙️ How the Automation Works

When triggered manually via API or automatically by the cron scheduler:

1. Updates the execution tracker status (`RelatorioExecucaoTrackerService`) to `EM_PROGRESSO`.
2. Launches a headless Google Chrome browser instance via Selenium WebDriver with automated login retry logic.
3. Authenticates into the church management system using configured credentials.
4. Evaluates the current week date range (Monday through Sunday). If a week crosses a month boundary (e.g. June 29 to July 5), it performs day-by-day queries to ensure complete data extraction.
5. Uses atomic JavaScript execution (`selecionarOpcoesDeFormaAtomica`) to reliably select all form dropdown values across complex reactive UI components.
6. Navigates multi-page table result pagination (`clicarProximaPaginaSeExistir`) to extract all member, congregant, and wedding anniversary entries (including congregation info).
7. Sorts all entries chronologically starting from Monday.
8. Normalizes spouse name ordering to remove duplicate wedding anniversary couples.
9. Generates a formatted **PDF document** (`PdfService`) in a dedicated folder (`relatorios/`) with multi-page table splitting, repeated table headers, and custom design layouts.
10. Automatically sends the PDF file to a designated WhatsApp recipient using **Evolution API** (`WhatsAppService`).
11. Updates the execution status to `SUCESSO` (or `ERRO` with details) and safely closes the Chrome driver in a `finally` block.
12. Automatically purges old reports (`RelatorioLimpezaService`) beyond configured retention threshold (default: 30 days).

---

### 💻 Tech Stack

* **Java 17** & **Spring Boot 3.5**
* **Maven** (Dependency management)
* **Selenium WebDriver + WebDriverManager** (Headless browser automation)
* **OpenPDF** (Formatted PDF report generation)
* **SpringDoc OpenAPI / Swagger UI** (Interactive API documentation)
* **Lombok** (Code boilerplate reduction)
* **Docker Compose** (Evolution API orchestration for WhatsApp delivery)

---

### 📂 Project Architecture

The application follows an **MVC (Model-View-Controller)** pattern adapted for API workflows, strictly observing the **Single Responsibility Principle (SOLID)**.

```text
src/
└── main/
    ├── java/
    │   └── com/github/josiasdev/RelatorioAniversariantes/
    │       ├── config/         # Global configuration (CORS, OpenAPI/Swagger, Async, scraper properties)
    │       ├── controller/     # REST API endpoints (/relatorios/gerarAniversariantes & /relatorios/status)
    │       ├── dto/            # Data Transfer Objects (AniversarianteDTO, CasamentoDTO, DadosRelatorioDTO)
    │       ├── exception/      # Business exceptions & GlobalExceptionHandler (409/400)
    │       ├── service/        # Core business logic & orchestration
    │       │   ├── RelatorioAniversariantesService.java # Primary workflow & scheduled tasks
    │       │   ├── RelatorioExecucaoTrackerService.java # Real-time execution status tracking
    │       │   ├── RelatorioLimpezaService.java         # PDF retention cleanup scheduled job
    │       │   ├── RelatorioSemanalService.java         # Week calculation & cross-month data collection
    │       │   ├── WebScraperService.java               # Selenium scraping, atomic JS select & pagination
    │       │   ├── PdfService.java                      # PDF design, 4-column tables & relatorios/ output
    │       │   └── WhatsAppService.java                 # Evolution API WhatsApp file dispatcher
    │       └── RelatorioAniversariantesApplication.java  # Main application entry point
    └── resources/
        └── application.properties # Application properties & credentials placeholder
docker/
└── docker-compose.yml          # Container orchestration for WhatsApp Evolution API
```

---

### 📱 WhatsApp Integration (Evolution API)

The system automatically sends generated PDF reports to WhatsApp using [Evolution API](https://doc.evolution-api.com/v2/pt/get-started/introduction) running locally via Docker.

#### 1. Configure Docker Environment Variables (`.env`)

A preconfigured `docker-compose.yml` is provided inside the `docker/` folder. Create a `.env` file in `docker/`:

```bash
EVOLUTION_API_KEY=YOUR_SUPER_SECRET_KEY_HERE
```

#### 2. Launch Evolution API Container

Navigate to `docker/` and run:

```bash
docker compose up -d
```

The WhatsApp API service will start on port `8081`.

#### 3. Connect WhatsApp Account (Evolution Manager)

1. Open your browser and navigate to: `http://localhost:8081/manager`
2. Click **+ INSTANCE** and fill in the fields:
   * **Instance Name:** `igreja`
   * **Integration:** `Baileys`
   * **API Key:** Enter your secret key (from `.env`)
3. Open the created instance and click **CONNECT**.
4. Scan the generated QR code using WhatsApp on your phone (**Settings > Linked Devices > Link a Device**).

---

### 🚀 Getting Started

#### 1. Prerequisites

* **Java 17** or higher installed
* **Maven** installed
* **Google Chrome** installed on the host system
* **Docker** (for WhatsApp delivery via Evolution API)

#### 2. Configure Credentials (Environment Variables)

Credentials **are no longer stored in `application.properties`**. Configure them via environment variables — copy the template and fill it in:

```bash
cp .env.example .env
# edit .env with your real credentials and export them in the shell or process environment
```

| Variable | Description | Example |
|---|---|---|
| `CHURCH_CLIENT_CODE` | Church platform client code | `19700` |
| `CHURCH_USERNAME` | Church username | `your_username` |
| `CHURCH_PASSWORD` | Church password | `your_password` |
| `EVOLUTION_API_KEY` | Evolution API key (`apikey` header) | `secret_key` |
| `WHATSAPP_BASE_URL` | Evolution API base URL (optional) | `http://localhost:8081` |
| `WHATSAPP_INSTANCE` | Instance name (optional) | `igreja` |
| `WHATSAPP_DESTINATARIO` | Recipient (country+area+number) | `5585999999999` |

> 🔐 **Important:** if you previously had credentials in `application.properties`, **rotate the Church password and the Evolution API key** — they may exist in the Git history. The `.env` file is already in `.gitignore` and must never be committed.

Other optional settings (with defaults in `application.properties`): report folder/retention, scraper URLs, timings and Resilience4j parameters (login attempts and backoff).

#### 3. Build & Run

From the project root:

```bash
# Compile and run unit & integration tests
mvn clean package

# Run application
java -jar target/RelatorioAniversariantes-0.0.1-SNAPSHOT.jar
```

---

### 🕹️ Manual API Triggering & Real-Time Status Monitoring

The report generation task runs automatically every Monday at **08:00 AM** (`America/Fortaleza` timezone). You can also interact via Swagger UI:

👉 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

1. **Trigger Report**: Execute `GET /relatorios/gerarAniversariantes`. Returns HTTP `202 Accepted` immediately.
2. **Monitor Status**: Execute `GET /relatorios/status`. Returns real-time JSON metrics:
   ```json
   {
     "status": "SUCESSO",
     "mensagem": "Relatório gerado com sucesso (relatorios/relatorio_aniversariantes_2026-08-09.pdf)...",
     "inicioExecucao": "2026-08-09T16:00:00",
     "fimExecucao": "2026-08-09T16:00:15",
     "duracaoSegundos": 15,
     "arquivoGerado": "relatorios/relatorio_aniversariantes_2026-08-09.pdf",
     "totais": {
       "membros": 12,
       "congregados": 5,
       "casamentos": 3
     }
   }
   ```

Upon completion, the PDF file `relatorios/relatorio_aniversariantes_YYYY-MM-DD.pdf` is generated and sent to your configured WhatsApp number.

---

### 🧪 Running Automated Tests

Run the unit and integration test suite via Maven (Docker is required for integration tests):

```bash
mvn test
```

Tests cover:
* Multi-month week boundary logic & chronological sorting (`RelatorioSemanalServiceTest`)
* Execution tracker status transitions and JSON payload generation (`RelatorioExecucaoTrackerServiceTest`)
* REST API endpoints with MockMvc, including the 409 concurrent-run guard (`RelatorioAniversariantesControllerTest`)
* Orchestration: WhatsApp failure alerts, metrics and concurrency guard (`RelatorioAniversariantesServiceTest`)
* **Real integration (Testcontainers + WireMock + headless Chrome):** full extraction against Church platform HTML fixtures, including pagination and congregation grouping (`WebScraperServiceIntegrationTest`)
* **Login resilience:** first attempt returns 500 and the Resilience4j retry recovers the flow (`WebScraperLoginRetryIntegrationTest`)
* **WhatsApp delivery:** payload, `apikey` header and 5xx error handling (`WhatsAppServiceIntegrationTest`)

---

### 🛡️ Resilience & Alerts

* **Login retry (Resilience4j):** up to 3 attempts with exponential backoff (2s → 4s → 8s), configurable via `resilience4j.retry.instances.login.*`.
* **WhatsApp delivery retry:** 2 attempts before propagating `WhatsAppException`, configurable via `resilience4j.retry.instances.whatsapp.*`.
* **Automatic failure alerts:** if report generation fails, the admin receives a WhatsApp text message with the time, error and guidance — independent of the missing PDF.
* **Concurrency guard:** a second trigger during a running execution returns HTTP `409 Conflict`; a scheduled run colliding with a manual one is skipped with a warning log.

---

### 📈 Observability

* **Actuator:** `GET /actuator/health`, `/actuator/info`, `/actuator/metrics` and `/actuator/prometheus` (default exposure in `application.properties`).
* **Custom metrics (Micrometer):**
  * `relatorio.geracao.execucoes` (counter, tag `outcome=sucesso|erro`)
  * `relatorio.geracao.tempo` (timer for each run duration)
  * `relatorio.whatsapp.envio` (counter, tag `outcome=sucesso|erro`)
* **Structured logging:** SLF4J with a rich date/level/thread pattern (`logback-spring.xml`); the `dev` profile raises the level to DEBUG.

---

### 👨‍💻 Author

Developed with dedication by **Josias Batista**.

[![Portfolio](https://img.shields.io/badge/Portfolio-000000?style=for-the-badge&logo=vercel&logoColor=white)](https://josias-batista-portfolio.vercel.app)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/josias-batista/)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/josiasdev)

> If this repository helps your workflow, consider giving it a **⭐ Star**!