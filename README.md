# 🤖 Automated Church Birthday & Anniversary Report

Read this in other languages: [Português](README_pt_BR.md)

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen.svg)
![Selenium](https://img.shields.io/badge/Selenium-WebDriver-blue.svg)
![Status](https://img.shields.io/badge/Status-Completed-success.svg)

This application automates the generation and delivery of weekly church reports for member birthdays, congregant birthdays, and wedding anniversaries. By leveraging headless web automation, intelligent date range handling, PDF rendering, and WhatsApp integration, it removes manual administrative tasks.

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

1. Launches a headless Google Chrome browser instance via Selenium WebDriver.
2. Authenticates into the church management system using configured credentials.
3. Evaluates the week date range (Monday through Sunday). If a week crosses a month boundary (e.g. June 29 to July 5), it performs day-by-day queries to ensure complete data extraction.
4. Uses atomic JavaScript execution (`selecionarOpcoesDeFormaAtomica`) to reliably select all form dropdown values across complex reactive UI components.
5. Navigates multi-page table result pagination (`clicarProximaPaginaSeExistir`) to extract all member, congregant, and wedding anniversary entries (including congregation info).
6. Sorts all entries chronologically starting from Monday.
7. Normalizes spouse name ordering to remove duplicate wedding anniversary couples.
8. Generates a formatted **PDF document** (`PdfService`) with multi-page table splitting, repeated table headers, and custom design layouts.
9. Automatically sends the PDF file to a designated WhatsApp recipient using **Evolution API** (`WhatsAppService`).

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
    │       ├── controller/     # REST API endpoints & Swagger documentation
    │       ├── dto/            # Data Transfer Objects (AniversarianteDTO, CasamentoDTO, DadosRelatorioDTO)
    │       ├── service/        # Core business logic & orchestration
    │       │   ├── RelatorioAniversariantesService.java # Primary workflow & scheduled tasks
    │       │   ├── RelatorioSemanalService.java         # Week calculation & cross-month data collection
    │       │   ├── WebScraperService.java               # Selenium scraping, atomic form select & pagination
    │       │   ├── PdfService.java                      # PDF design, 4-column tables & page split logic
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

Once connected, your Spring Boot service can autonomously dispatch report PDFs to WhatsApp!

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
```

#### 3. Build & Run

From the project root:

```bash
# Compile and run tests
mvn clean package

# Run application
java -jar target/RelatorioAniversariantes-0.0.1-SNAPSHOT.jar
```

---

### 🕹️ Manual API Triggering & Swagger UI

The report generation task runs automatically every Monday at **08:00 AM** (`America/Fortaleza` timezone). You can also trigger it manually:

1. Open Swagger UI in your browser: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
2. Expand `GET /relatorios/gerarAniversariantes`.
3. Click **Try it out** and then **Execute**.
4. The API returns `202 Accepted` and processes the report asynchronously.

Upon completion, the PDF file `relatorio_aniversariantes_YYYY-MM-DD.pdf` is saved in the root folder and sent to your configured WhatsApp number.

---

### 🧪 Running Automated Tests

Run unit tests via Maven:

```bash
mvn test
```

Unit tests verify multi-month week date logic, chronological day sorting, and couple deduplication algorithms in `RelatorioSemanalServiceTest`.

---

### 👨‍💻 Author

Developed with dedication by **Josias Batista**.

[![Portfolio](https://img.shields.io/badge/Portfolio-000000?style=for-the-badge&logo=vercel&logoColor=white)](https://josias-batista-portfolio.vercel.app)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/josias-batista/)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/josiasdev)

> If this repository helps your workflow, consider giving it a **⭐ Star**!