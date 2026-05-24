# Contexto do Projeto

## Visão Geral

RelatorioAniversariantes é uma aplicação Java 17 com Spring Boot que automatiza o fluxo semanal de relatórios de aniversariantes de uma igreja. Ela acessa a plataforma de gestão Church por automação de navegador, coleta dados de membros, congregados e aniversariantes de casamento, processa o resultado, gera um PDF consolidado e envia o documento pelo WhatsApp usando uma instância local da Evolution API.

O objetivo do projeto é reduzir trabalho manual repetitivo e gerar um relatório semanal pronto para compartilhamento.

## Responsabilidades Principais

- Disparar a geração do relatório por meio de um endpoint HTTP.
- Executar a mesma geração automaticamente toda segunda-feira às 08:00 no fuso America/Fortaleza.
- Usar Selenium WebDriver com Chrome em modo headless para acessar o sistema externo da igreja.
- Extrair aniversariantes membros, aniversariantes congregados e aniversariantes de casamento da semana atual.
- Ordenar os registros por dia.
- Remover casais duplicados nos aniversários de casamento normalizando a ordem dos cônjuges.
- Gerar um PDF formatado com imagem de cabeçalho quando disponível.
- Enviar o PDF gerado para o WhatsApp por meio da Evolution API.

## Tecnologias

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

## Arquitetura

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

## Componentes Principais

- `RelatorioAniversariantesApplication`: ponto de entrada Spring Boot. Habilita execução assíncrona e tarefas agendadas.
- `RelatorioAniversariantesController`: expõe o endpoint de geração de relatório em `/relatorios`.
- `RelatorioAniversariantesService`: orquestra o fluxo completo: cálculo do período, scraping, ordenação, remoção de duplicidade, geração do PDF e envio pelo WhatsApp.
- `WebScraperService`: cuida da configuração do Selenium, login, navegação, preenchimento de formulários e extração das tabelas.
- `PdfService`: cria o PDF final com seções para membros, congregados e aniversariantes de casamento.
- `WhatsAppService`: lê o PDF gerado, converte o arquivo para Base64 e envia como documento pela Evolution API.
- DTOs: representam os dados extraídos de aniversariantes, casamentos e relatório consolidado.

## API

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

## Fluxo de Execução

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

## Docker / Integração WhatsApp

O arquivo `docker/docker-compose.yml` inicia um container da Evolution API na porta local `8081`. A chave da API deve ser fornecida por variável de ambiente ou arquivo `.env` local e não deve ser versionada.

## Segurança E Configuração

- Não documentar, commitar ou expor credenciais, chaves de API, números de telefone, valores de autenticação ou segredos locais.
- Não copiar valores de arquivos locais de configuração Spring para documentação, exemplos, logs, issues, commits ou pull requests.
- Manter a configuração de execução privada e específica por ambiente.
- Tratar PDFs gerados como dados potencialmente sensíveis, pois eles contêm informações pessoais.

## Comandos De Desenvolvimento

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

## Requisitos Operacionais

- Java 17 ou superior
- Maven
- Google Chrome instalado
- Docker, quando o envio por WhatsApp for necessário
- Credenciais privadas válidas para o sistema externo de gestão da igreja
- Instância da Evolution API configurada e conectada ao WhatsApp

## Notas De Manutenção

- O scraping depende da estrutura HTML da plataforma externa. Se a plataforma alterar IDs de elementos, classes de tabela ou fluxo de navegação, o `WebScraperService` pode precisar de ajustes.
- O relatório semanal usa a data atual para calcular o período de segunda a domingo.
- A tarefa agendada roda toda segunda-feira às 08:00 no fuso America/Fortaleza.
- A formatação do PDF fica centralizada no `PdfService`.
- O envio do relatório fica centralizado no `WhatsAppService`.
