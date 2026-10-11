# 🤖 Relatório de Aniversariantes Automatizado

Leia em outros idiomas: [English](README.md)

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen.svg)
![Selenium](https://img.shields.io/badge/Selenium-WebDriver-blue.svg)
![Status](https://img.shields.io/badge/Status-Conclu%C3%ADdo-success.svg)

Esta aplicação foi criada para automatizar a emissão e entrega de relatórios semanais da igreja, abrangendo aniversariantes membros, congregados e aniversariantes de casamento. Utilizando automação web invisível, cálculo inteligente de períodos semanais, geração de PDF formatado, monitoramento de status e envio via WhatsApp, o sistema elimina tarefas administrativas manuais repetitivas.

O sistema faz a distinção automática entre membros, congregados e casamentos, agrupando os dados por congregação e pela Sede.

> ⚠️ **Aviso Legal:** Este projeto utiliza automação web (Web Scraping). Foi desenvolvido para uso pessoal e institucional visando otimização de fluxo de trabalho. O código pode requerer ajustes caso a plataforma alvo altere a estrutura do seu HTML ou navegação.

---

### 🎯 Sobre o sistema alvo (Church)

A automação atua de forma transparente na plataforma de gestão online [Church](https://church15.churchsoftware.com.br/frmlogin/).

O Church é um sistema de gestão em nuvem utilizado por diversas igrejas no Brasil para integrar os setores administrativo e pastoral:
* **Secretaria e Membros:** Cadastro de membros, congregados, controle de aniversariantes e aplicativo Church Digital.
* **Tesouraria e Financeiro:** Entradas, saídas, conciliação bancária e contribuições.
* **Outros Recursos:** Departamento infantil, contagem de público de cultos e backups online.

---

### ⚙️ Como a Automação Funciona

Quando acionado manualmente via API ou automaticamente pelo agendador cron:

1. Atualiza o estado da execução no rastreador (`RelatorioExecucaoTrackerService`) para `EM_PROGRESSO`.
2. Inicia uma instância do navegador Google Chrome em modo invisível (`headless`) via Selenium WebDriver, contando com algoritmo de tentativas automáticas no login.
3. Faz login no sistema Church utilizando as credenciais configuradas.
4. Avalia o período da semana (de segunda-feira a domingo). Caso a semana cruze a virada do mês (ex: 29 de junho a 05 de julho), o `RelatorioSemanalService` executa consultas dia a dia para contornar limitações do formulário externo e garantir a extração completa dos dados.
5. Executa seleções atômicas de formulários via JavaScript (`selecionarOpcoesDeFormaAtomica`) para garantir que os seletores reativos do sistema sejam preenchidos sem perda de contexto ou erros de elemento desatualizado.
6. Navega pela paginação de resultados (`clicarProximaPaginaSeExistir`) para extrair todos os registros de membros, congregados e casamentos (incluindo a congregação).
7. Ordena todas as listas cronologicamente a partir de segunda-feira.
8. Normaliza a ordem dos nomes dos cônjuges para eliminar casais duplicados nos aniversários de casamento.
9. Gera um **arquivo PDF consolidado** (`PdfService`) na pasta dedicada `relatorios/` com tabelas de 4 colunas, repetição de cabeçalho entre páginas e formatação limpa.
10. Envia o relatório automaticamente para o WhatsApp configurado utilizando a **Evolution API** (`WhatsAppService`).
11. Atualiza o status no rastreador para `SUCESSO` (ou `ERRO` com detalhes) e encerra com segurança o Chrome driver no bloco `finally`.
12. Executa a limpeza automática (`RelatorioLimpezaService`) de relatórios antigos na pasta `relatorios/` além do prazo de retenção (padrão: 30 dias).

---

### 💻 Tecnologias Utilizadas

* **Java 17** e **Spring Boot 3.5**
* **Maven** (Gerenciamento de dependências)
* **Selenium WebDriver + WebDriverManager** (Automação de navegador headless)
* **OpenPDF** (Geração e formatação de relatórios em PDF)
* **SpringDoc OpenAPI / Swagger UI** (Documentação interativa da API)
* **Lombok** (Redução de código boilerplate)
* **Docker Compose** (Orquestração do container da Evolution API para WhatsApp)

---

### 📂 Arquitetura do Projeto

A arquitetura segue o padrão **MVC (Model-View-Controller)** adaptado para uma API REST, respeitando rigorosamente o **Princípio da Responsabilidade Única (SOLID)**.

```text
src/
└── main/
    ├── java/
    │   └── com/github/josiasdev/RelatorioAniversariantes/
    │       ├── config/         # Configurações globais (CORS, Swagger UI, Async, propriedades do scraper)
    │       ├── controller/     # Endpoints da API REST (/relatorios/gerarAniversariantes & /relatorios/status)
    │       ├── dto/            # Objetos de Transferência de Dados (AniversarianteDTO, CasamentoDTO, DadosRelatorioDTO)
    │       ├── exception/      # Exceções de negócio e GlobalExceptionHandler (409/400)
    │       ├── service/        # Lógica de negócio e orquestração
    │       │   ├── RelatorioAniversariantesService.java # Fluxo principal e tarefas agendadas (@Scheduled)
    │       │   ├── RelatorioExecucaoTrackerService.java # Monitoramento de status em tempo real
    │       │   ├── RelatorioLimpezaService.java         # Limpeza agendada de relatórios antigos
    │       │   ├── RelatorioSemanalService.java         # Cálculo de datas e busca por transição de mês
    │       │   ├── WebScraperService.java               # Scraping Selenium, seleção via JS e paginação
    │       │   ├── PdfService.java                      # Design do PDF, tabelas de 4 colunas e saída em relatorios/
    │       │   └── WhatsAppService.java                 # Envio do arquivo PDF via Evolution API
    │       └── RelatorioAniversariantesApplication.java  # Classe principal da aplicação Spring Boot
    └── resources/
        └── application.properties # Parâmetros de ambiente e credenciais
docker/
└── docker-compose.yml          # Container da Evolution API do WhatsApp
```

---

### 📱 Integração com WhatsApp (Evolution API)

O sistema possui integração automática para enviar o PDF gerado diretamente para o WhatsApp. Para garantir que essa funcionalidade seja gratuita e segura, utilizamos a [Evolution API](https://doc.evolution-api.com/v2/pt/get-started/introduction) rodando localmente via Docker.

#### 1. Configurar as variáveis de ambiente (.env)

O repositório inclui um arquivo `docker-compose.yml` na pasta `docker/`. Crie um arquivo `.env` dentro da pasta `docker/`:

```bash
EVOLUTION_API_KEY=SUA_CHAVE_SUPER_SECRETA_AQUI
```

#### 2. Subir o container da API

Navegue até a pasta `docker/` e execute:

```bash
docker compose up -d
```

A API do WhatsApp estará pronta na porta `8081`.

#### 3. Conectar a Conta do WhatsApp (Evolution Manager)

1. Acesse no navegador: `http://localhost:8081/manager`
2. Clique no botão azul **+ INSTÂNCIA** e preencha:
   * **Nome da instância:** `igreja`
   * **Integração:** `Baileys`
   * **API Key:** Digite a sua chave secreta (definida no `.env`)
3. Abra a instância criada e clique em **CONECTAR**.
4. Escaneie o **QR Code** pelo WhatsApp do celular (**Configurações > Dispositivos Conectados > Conectar dispositivo**).

---

### 🚀 Como Executar o Projeto

#### 1. Pré-requisitos

* **Java 17** ou superior instalado
* **Maven** instalado
* Navegador **Google Chrome** instalado no sistema operacional
* **Docker** (para envio via Evolution API)

#### 2. Configurar Credenciais (Variáveis de Ambiente)

As credenciais **não ficam mais no `application.properties`**. Configure-as via variáveis de ambiente — copie o template e preencha:

```bash
cp .env.example .env
# edite .env com suas credenciais reais e exporte no shell ou no ambiente do processo Java
```

| Variável | Descrição | Exemplo |
|---|---|---|
| `CHURCH_CLIENT_CODE` | Código do cliente no sistema Church | `19700` |
| `CHURCH_USERNAME` | Usuário do Church | `seu_usuario` |
| `CHURCH_PASSWORD` | Senha do Church | `sua_senha` |
| `EVOLUTION_API_KEY` | Chave da API (header `apikey`) | `chave_secreta` |
| `WHATSAPP_BASE_URL` | URL base da Evolution API (opcional) | `http://localhost:8081` |
| `WHATSAPP_INSTANCE` | Nome da instância (opcional) | `igreja` |
| `WHATSAPP_DESTINATARIO` | Destinatário (país+DDD+número) | `5585999999999` |

> 🔐 **Importante:** se você usava uma versão anterior com senhas no `application.properties`, **rotacione a senha do Church e a API key da Evolution** — elas podem estar no histórico do Git. O arquivo `.env` já está no `.gitignore` e nunca deve ser commitado.

Outras configurações opcionais (com defaults em `application.properties`): pastas/retenção de relatórios, URLs do scraper, timings e parâmetros do Resilience4j (tentativas e backoff do login).

#### 3. Compilar e Executar

Na raiz do projeto:

```bash
# Compilar o projeto e executar testes unitários e de integração
mvn clean package

# Executar a aplicação
java -jar target/RelatorioAniversariantes-0.0.1-SNAPSHOT.jar
```

---

### 🕹️ Disparo Manual e Monitoramento em Tempo Real via Swagger UI

O sistema roda automaticamente toda segunda-feira às **08:00** (fuso `America/Fortaleza`). Você também pode interagir via Swagger UI:

👉 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

1. **Disparar Relatório**: Execute o endpoint `GET /relatorios/gerarAniversariantes`. O servidor responde imediatamente com `202 Accepted`.
2. **Consultar Status**: Execute o endpoint `GET /relatorios/status`. O servidor retorna o estado detalhado em formato JSON:
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

Ao finalizar, o arquivo `relatorios/relatorio_aniversariantes_ANO-MES-DIA.pdf` é gravado na pasta `relatorios/` e enviado pelo WhatsApp!

---

### 🧪 Testes Automatizados

Para rodar a suíte de testes automatizados via Maven (requer Docker para os testes de integração):

```bash
mvn test
```

Os testes cobrem:
* Período semanal em viradas de mês e ordenação cronológica (`RelatorioSemanalServiceTest`)
* Transições de estado do rastreador de execução (`RelatorioExecucaoTrackerServiceTest`)
* Endpoints REST da API via MockMvc, incluindo o bloqueio 409 de execução concorrente (`RelatorioAniversariantesControllerTest`)
* Orquestração: alerta de falha via WhatsApp, métricas e guarda de concorrência (`RelatorioAniversariantesServiceTest`)
* **Integração real (Testcontainers + WireMock + Chrome headless):** extração completa contra fixtures HTML da plataforma Church, com paginação e agrupamento por congregação (`WebScraperServiceIntegrationTest`)
* **Resiliência do login:** primeira tentativa responde 500 e o retry do Resilience4j recupera o fluxo (`WebScraperLoginRetryIntegrationTest`)
* **Envio WhatsApp:** payload, header `apikey` e tratamento de erro 5xx (`WhatsAppServiceIntegrationTest`)

---

### 🛡️ Resiliência e Alertas

* **Login com retry (Resilience4j):** até 3 tentativas com backoff exponencial (2s → 4s → 8s), configurável em `resilience4j.retry.instances.login.*`.
* **Envio WhatsApp com retry:** 2 tentativas antes de propagar `WhatsAppException`, configurável em `resilience4j.retry.instances.whatsapp.*`.
* **Alerta automático de falha:** se a geração do relatório falhar, o admin recebe uma mensagem de texto no WhatsApp com horário, erro e orientação — independentemente do PDF não ter sido gerado.
* **Guarda de concorrência:** um segundo disparo durante uma execução em curso retorna HTTP `409 Conflict`; o agendamento semanal que colide com uma execução manual é ignorado com aviso no log.

---

### 📈 Observabilidade

* **Actuator:** `GET /actuator/health`, `/actuator/info`, `/actuator/metrics` e `/actuator/prometheus` (exposição padrão em `application.properties`).
* **Métricas customizadas (Micrometer):**
  * `relatorio.geracao.execucoes` (counter, tag `outcome=sucesso|erro`)
  * `relatorio.geracao.tempo` (timer com a duração de cada execução)
  * `relatorio.whatsapp.envio` (counter, tag `outcome=sucesso|erro`)
* **Logs estruturados:** SLF4J com pattern rico de data/hora/nível/thread (`logback-spring.xml`); profile `dev` eleva para DEBUG.

---

### 👨‍💻 Autor

Desenvolvido com dedicação por **Josias Batista**.

[![Portfólio](https://img.shields.io/badge/Portfólio-000000?style=for-the-badge&logo=vercel&logoColor=white)](https://josias-batista-portfolio.vercel.app)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/josias-batista/)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/josiasdev)

> Se este projeto te ajudou ou você achou a arquitetura interessante, deixe uma **⭐ Star** no repositório!
