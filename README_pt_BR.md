# 🤖 Relatório de Aniversariantes Automatizado

Leia em outros idiomas: [English](README.md)

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen.svg)
![Selenium](https://img.shields.io/badge/Selenium-WebDriver-blue.svg)
![Status](https://img.shields.io/badge/Status-Conclu%C3%ADdo-success.svg)

Esta aplicação foi criada para automatizar a emissão e entrega de relatórios semanais da igreja, abrangendo aniversariantes membros, congregados e aniversariantes de casamento. Utilizando automação web invisível, cálculo inteligente de períodos semanais, geração de PDF formatado e envio via WhatsApp, o sistema elimina tarefas administrativas manuais repetitivas.

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

1. Inicia uma instância do navegador Google Chrome em modo invisível (`headless`) via Selenium WebDriver.
2. Faz login no sistema Church utilizando as credenciais configuradas.
3. Avalia o período da semana (de segunda-feira a domingo). Caso a semana cruze a virada do mês (ex: 29 de junho a 05 de julho), o `RelatorioSemanalService` executa consultas dia a dia para contornar limitações do formulário externo e garantir a extração completa dos dados.
4. Executa seleções atômicas de formulários via JavaScript (`selecionarOpcoesDeFormaAtomica`) para garantir que os seletores reativos do sistema sejam preenchidos sem perda de contexto ou erros de elemento desatualizado.
5. Navega pela paginação de resultados (`clicarProximaPaginaSeExistir`) para extrair todos os registros de membros, congregados e casamentos (incluindo a congregação).
6. Ordena todas as listas cronologicamente a partir de segunda-feira.
7. Normaliza a ordem dos nomes dos cônjuges para eliminar casais duplicados nos aniversários de casamento.
8. Gera um **arquivo PDF consolidado** (`PdfService`) com tabelas de 4 colunas, repetição de cabeçalho entre páginas e formatação limpa.
9. Envia o relatório automaticamente para o WhatsApp configurado utilizando a **Evolution API** (`WhatsAppService`).

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
    │       ├── config/         # Configurações globais (CORS, Swagger UI, Segurança)
    │       ├── controller/     # Endpoints da API REST e documentação Swagger
    │       ├── dto/            # Objetos de Transferência de Dados (AniversarianteDTO, CasamentoDTO, DadosRelatorioDTO)
    │       ├── service/        # Lógica de negócio e orquestração
    │       │   ├── RelatorioAniversariantesService.java # Fluxo principal e tarefas agendadas (@Scheduled)
    │       │   ├── RelatorioSemanalService.java         # Cálculo de datas e busca por transição de mês
    │       │   ├── WebScraperService.java               # Scraping Selenium, seleção via JS e paginação
    │       │   ├── PdfService.java                      # Design do PDF, tabelas de 4 colunas e quebra de páginas
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

Pronto! A sua aplicação Spring Boot já tem permissão para enviar os PDFs de forma autônoma.

---

### 🚀 Como Executar o Projeto

#### 1. Pré-requisitos

* **Java 17** ou superior instalado
* **Maven** instalado
* Navegador **Google Chrome** instalado no sistema operacional
* **Docker** (para envio via Evolution API)

#### 2. Configurar Credenciais (`application.properties`)

Atualize o arquivo `src/main/resources/application.properties` com suas credenciais do sistema Church e os dados do destinatário do WhatsApp:

```properties
spring.application.name=RelatorioAniversariantes

# Credenciais para login no sistema Church
app.credentials.client-code=SEU_CODIGO_DE_CLIENTE
app.credentials.username=SEU_USUARIO
app.credentials.password=SUA_SENHA

# --- CONFIGURAÇÕES DO WHATSAPP (EVOLUTION API) ---
whatsapp.api.url=http://localhost:8081/message/sendMedia/igreja
whatsapp.api.key=SUA_CHAVE_SUPER_SECRETA_AQUI
# Número do destinatário (Código do país + DDD + Número)
whatsapp.destinatario=5585999999999
```

#### 3. Compilar e Executar

Na raiz do projeto:

```bash
# Compilar o projeto e executar testes
mvn clean package

# Executar a aplicação
java -jar target/RelatorioAniversariantes-0.0.1-SNAPSHOT.jar
```

---

### 🕹️ Disparo Manual via Swagger UI

O sistema roda automaticamente toda segunda-feira às **08:00** (fuso `America/Fortaleza`). Você também pode dispará-lo manualmente:

1. Acesse o Swagger UI no navegador: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
2. Expanda a rota `GET /relatorios/gerarAniversariantes`.
3. Clique em **Try it out** e depois em **Execute**.
4. O servidor retornará status `202 Accepted` e executará o relatório em segundo plano.

Ao finalizar, o arquivo `relatorio_aniversariantes_ANO-MES-DIA.pdf` será salvo na raiz do projeto e enviado para o WhatsApp!

---

### 🧪 Testes Automatizados

Para rodar os testes unitários via Maven:

```bash
mvn test
```

Os testes unitários validam a lógica de período semanal em viradas de mês, a ordenação cronológica e a desduplicação de casais no `RelatorioSemanalServiceTest`.

---

### 👨‍💻 Autor

Desenvolvido com dedicação por **Josias Batista**.

[![Portfólio](https://img.shields.io/badge/Portfólio-000000?style=for-the-badge&logo=vercel&logoColor=white)](https://josias-batista-portfolio.vercel.app)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/josias-batista/)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/josiasdev)

> Se este projeto te ajudou ou você achou a arquitetura interessante, deixe uma **⭐ Star** no repositório!
