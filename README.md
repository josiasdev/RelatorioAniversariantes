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

A arquitetura foi desenhada seguindo o padrão **MVC (Model-View-Controller)** adaptado para uma API, priorizando o **Princípio da Responsabilidade Única (SOLID)** para facilitar a manutenção e escalabilidade.

```text
src/
└── main/
    ├── java/
    │   └── com/github/josiasdev/RelatorioAniversariantes/
    │       ├── config/         # Configurações globais (CORS, Documentação, Segurança)
    │       ├── controller/     # Exposição dos Endpoints e documentação da API
    │       ├── dto/            # Objetos de Transferência de Dados (Estrutura das entidades)
    │       ├── service/        # Lógica de Negócio, Orquestração e Integração (WhatsApp)
    │       └── ...             # Classe principal da aplicação
    └── resources/
        └── application.properties # Parâmetros de ambiente e credenciais
docker/
└── docker-compose.yml          # Orquestração do container da Evolution API
```

Detalhamento das Camadas:

- config/: Centraliza as definições do sistema. O arquivo OpenApiConfig.java configura a interface visual do Swagger, permitindo testar o robô sem necessidade de ferramentas externas.
- controller/: Atua como o "recepcionista" da API. O RelatorioAniversariantesController.java valida a entrada e delega o trabalho pesado para os serviços, respondendo de forma assíncrona para não travar a conexão do usuário.
- dto/: Define o contrato dos dados. Garante que as informações extraídas pelo robô (como nomes e datas) cheguem ao gerador de PDF em um formato padronizado e limpo. 
- service/: Onde a "mágica" acontece.
  - WebScraperService: Especialista em navegação e extração bruta de dados via Selenium.
  - PdfService: Especialista em design e formatação de documentos.
  - RelatorioAniversariantesService: O cérebro do sistema. Ele decide a ordem das ações, limpa dados duplicados e une as capacidades de raspagem e geração de documentos.

---

###  📱 Integração com WhatsApp (Evolution API)

O sistema possui integração automática para enviar o PDF gerado diretamente para o WhatsApp. Para garantir que essa funcionalidade seja gratuita e segura, utilizamos a [Evolution API ](https://doc.evolution-api.com/v2/pt/get-started/introduction) rodando localmente via Docker.

**1. Configurando a segurança do Docker (.env):**

O repositório inclui um arquivo docker-compose.yml pré-configurado na pasta docker/. Para não expor credenciais, a API Key é lida de variáveis de ambiente.
Crie um arquivo chamado .env na mesma pasta do docker-compose.yml e defina uma senha segura:
```bash
EVOLUTION_API_KEY=SUA_CHAVE_SUPER_SECRETA_AQUI
```


**2. Subindo a API:**

Navegue até a pasta docker/ e execute:
```bash
docker compose up -d
```
A API do WhatsApp estará rodando na porta 8081.

**3. Conectando o seu número (Via Painel Visual):**
A API já vem com o Evolution Manager embutido para facilitar a conexão:
1. Abra o seu navegador e acesse: http://localhost:8081/manager
2. Clique no botão azul **+ INSTÂNCIA** e preencha os dados:
  * **Nome da instância:** `igreja`
  * **Integração:** Selecione `Baileys`
  * **API Key:** Cole a sua chave (a mesma do arquivo `.env`)
  * Clique em **SALVAR**.
3. A instância aparecerá na lista. Clique nela e, na tela de configurações, clique no botão vermelho **CONECTAR**.
4. Um **QR Code** aparecerá na tela do seu computador
5. Pegue o seu celular, abra o WhatsApp e vá em: **Configurações > Dispositivos conectados > Conectar dispositivo**.
6. Escaneie o QR Code na tela.


Pronto! A interface mostrará o status "Conectado" e o seu robô Java já tem permissão para enviar os PDFs de forma autônoma.


### 🚀 Como colocar a API Java para rodar

**1. Pré-requisitos:**
* Java 17 (ou superior) instalado.
* Maven instalado.
* Navegador Google Chrome instalado no sistema operacional.
* Ambiente Docker rodando a Evolution API (conforme passos acima).

**2. Configurando as senhas:**
As senhas e configurações de disparo não ficam expostas no código. Vá até a pasta `src/main/resources` e abra o arquivo `application.properties`. Insira suas credenciais e os dados da sua Evolution API:

```properties
spring.application.name=RelatorioAniversariantes

# Credenciais para o login no sistema Church
app.credentials.client-code=SEU_CODIGO_DE_CLIENTE
app.credentials.username=SEU_USUARIO
app.credentials.password=SUA_SENHA

# --- CONFIGURAÇÕES DO WHATSAPP (EVOLUTION API) ---
whatsapp.api.url=http://localhost:8081/message/sendMedia/igreja
whatsapp.api.key=SUA_GLOBAL_API_KEY_AQUI
# Número do destinatário (Código do país + DDD + Número)
whatsapp.destinatario=5511999999999
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

### 🕹️ Como testar a Automação
O sistema roda automaticamente toda segunda-feira às 8h da manhã. Porém, você pode dispará-lo manualmente usando o Swagger.

Com o projeto rodando, abra o seu navegador e acesse:

👉 [LocalHost - Swagger UI](http://localhost:8080/swagger-ui.html)

1. Abra o endpoint de geração de relatórios.
2. Clique no botão "Try it out" e depois em "Execute".
3. O servidor vai responder com status 202 (Accepted).
4. Acompanhe o console da sua IDE/Terminal para ver o robô trabalhando.

Quando finalizado, o arquivo relatorio_aniversariantes_ANO-MES-DIA.pdf aparecerá na pasta raiz do projeto e será disparado via WhatsApp!


### 🔮 Roadmap (Concluído)
[X] Implementar rotina de agendamento automático (@Scheduled) para rodar toda segunda-feira de manhã.

[X] Adicionar testes unitários para a lógica de filtro e duplicação de dados.

[X] Integrar envio automático do PDF gerado via WhatsApp API.


---
**Desenvolvido por [Josias Batista](https://josias-batista-portfolio.vercel.app)** 🚀 [GitHub](https://github.com/josiasdev) | 💼 [LinkedIn](https://www.linkedin.com/in/josias-batista/)  
*Fique à vontade para explorar, modificar e contribuir com o código!*