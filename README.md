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
    │       ├── config/         # Global configuration (CORS, OpenAPI/Swagger, Security)
    │       ├── controller/     # REST API endpoints (/relatorios/gerarAniversariantes & /relatorios/status)
    │       ├── dto/            # Data Transfer Objects (AniversarianteDTO, CasamentoDTO, DadosRelatorioDTO)
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

#### 2. Configure Credentials (`application.properties`)

Update `src/main/resources/application.properties` with your church credentials and WhatsApp recipient details:

```properties
spring.application.name=RelatorioAniversariantes

# Church Management Platform Credentials
app.credentials.client-code=YOUR_CLIENT_CODE
app.credentials.username=YOUR_USERNAME
app.credentials.password=YOUR_PASSWORD

# --- WHATSAPP CONFIGURATION (EVOLUTION API) ---
whatsapp.api.url=http://localhost:8081/message/sendMedia/igreja
whatsapp.api.key=YOUR_SUPER_SECRET_KEY_HERE
# Recipient phone number (Country code + Area code + Number)
whatsapp.destinatario=5585999999999

# --- REPORT STORAGE & RETENTION ---
app.reports.output-directory=relatorios
app.reports.retention-days=30
```

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

Run unit and integration test suite via Maven:

```bash
mvn test
```

Unit tests cover:
* Multi-month week boundary logic & chronological sorting (`RelatorioSemanalServiceTest`)
* Execution tracker status transitions and JSON payload generation (`RelatorioExecucaoTrackerServiceTest`)
* REST API endpoints with MockMvc (`RelatorioAniversariantesControllerTest`)

---

### 👨‍💻 Author

Developed with dedication by **Josias Batista**.

[![Portfolio](https://img.shields.io/badge/Portfolio-000000?style=for-the-badge&logo=vercel&logoColor=white)](https://josias-batista-portfolio.vercel.app)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/josias-batista/)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/josiasdev)

> If this repository helps your workflow, consider giving it a **⭐ Star**!