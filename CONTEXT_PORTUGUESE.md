# Contexto do Projeto

## Visão Geral

RelatorioAniversariantes é uma aplicação Java 17 com Spring Boot que automatiza o fluxo semanal de relatórios de aniversariantes e casamentos de uma igreja. Ela acessa a plataforma de gestão Church (especificamente `https://church15.churchsoftware.com.br/frmlogin/`) por automação de navegador, coleta dados de membros, congregados e aniversariantes de casamento, processa o resultado, gera um PDF consolidado e envia o documento pelo WhatsApp usando uma instância local da Evolution API.

O objetivo do projeto é reduzir trabalho manual repetitivo e gerar um relatório semanal pronto para compartilhamento, fazendo a distinção automática entre membros, congregados e aniversariantes de casamento, agrupando as informações por congregação e pela Sede.

## Responsabilidades Principais

- Disparar a geração do relatório por meio de um endpoint HTTP (`GET /relatorios/gerarAniversariantes`).
- Executar a mesma geração automaticamente toda segunda-feira às 08:00 no fuso America/Fortaleza (`@Scheduled`).
- Usar Selenium WebDriver com Chrome em modo headless para acessar o sistema externo da igreja.
- Tratar automaticamente semanas que cruzam a virada de mês (ex: 29/06 a 05/07), fragmentando as consultas dia a dia para garantir a extração completa dos dados.
- Extrair aniversariantes membros, aniversariantes congregados e aniversariantes de casamento (incluindo dados da congregação) da semana atual.
- Ordenar os registros cronologicamente em relação ao início da semana (segunda-feira).
- Remover casais duplicados nos aniversários de casamento normalizando a ordem dos cônjuges.
- Tratar paginação em múltiplos resultados de busca e seleção atômica de seletores via execução de JavaScript.
- Gerar um PDF formatado com tabelas de 4 colunas (com repetição de cabeçalho e divisão dinâmica de páginas).
- Enviar o PDF gerado para o WhatsApp por meio da Evolution API.

## Tecnologias

- **Java 17** e **Spring Boot 3.5.x** (Framework base)
- **Maven** (Gerenciador de dependências)
- **Spring Web**, **Spring Scheduling**, **Spring Async**
- **Selenium WebDriver** e **WebDriverManager** (Automação do navegador e extração de dados)
- **OpenPDF** (Criação e formatação do arquivo PDF)
- **Apache POI**
- **SpringDoc OpenAPI / Swagger UI** (Documentação interativa da API)
- **Lombok** (Redução de boilerplate de código)
- **Docker Compose** para o serviço da Evolution API

## Arquitetura

A arquitetura foi desenhada seguindo o padrão MVC (Model-View-Controller) adaptado para uma API, priorizando o Princípio da Responsabilidade Única (SOLID).

```text
src/main/java/com/github/josiasdev/RelatorioAniversariantes/
├── config/           # Configurações globais (CORS, Documentação, Segurança)
├── controller/       # Exposição dos Endpoints e documentação da API
├── dto/              # Objetos de Transferência de Dados (Estrutura das entidades)
└── service/          # Lógica de Negócio, Orquestração, Raspagem, PDF e Integração WhatsApp
```

### Componentes Principais

- `RelatorioAniversariantesApplication`: Ponto de entrada Spring Boot. Habilita execução assíncrona e tarefas agendadas.
- `RelatorioAniversariantesController`: Atua como o "recepcionista" da API. Valida a entrada e delega o trabalho pesado para os serviços de forma assíncrona.
- `RelatorioAniversariantesService`: Orquestra o fluxo de alto nível, disparando o agendamento, desduplicando casais de casamento, invocando a geração do PDF e solicitando o envio via WhatsApp.
- `RelatorioSemanalService`: Responsável pelo cálculo do período semanal. Caso a semana perpasse dois meses diferentes, realiza a extração dia a dia para contornar limitações do formulário do sistema Church, consolida as listas e ordena os dados cronologicamente a partir de segunda-feira.
- `WebScraperService`: Especialista em navegação Selenium, seleção atômica de opções de formulários via JS, tratamento de paginação de resultados e extração bruta de dados (incluindo congregação).
- `PdfService`: Especialista em design e formatação de documentos. Monta tabelas de 4 colunas para membros, congregados e casamentos, configurando repetição de cabeçalhos e divisão de linhas entre páginas.
- `WhatsAppService`: Lê o PDF gerado, converte para Base64 e envia como documento via Evolution API com tratamento resiliente de exceções.
- DTOs (`AniversarianteDTO`, `CasamentoDTO`, `DadosRelatorioDTO`): Representam os dados extraídos, garantindo padronização e incluindo o campo congregação nos casamentos.

## API

O endpoint principal é:

```text
GET /relatorios/gerarAniversariantes
```

Comportamento esperado:
- Retorna HTTP `202 Accepted` quando o processo é iniciado.
- Executa a geração do relatório de forma assíncrona.
- Grava o PDF gerado na raiz do projeto usando este padrão de nome:
  `relatorio_aniversariantes_YYYY-MM-DD.pdf`

Com a aplicação em execução, o Swagger UI fica disponível em:
`http://localhost:8080/swagger-ui.html`

## Fluxo de Execução

1. Calcula o período da semana atual (de segunda-feira a domingo).
2. Inicia o Chrome headless pelo Selenium.
3. Autentica no sistema externo de gestão da igreja usando as credenciais de `application.properties`.
4. Coleta os dados através do `RelatorioSemanalService`: se for no mesmo mês realiza busca direta; se cruzar a virada do mês, realiza consultas dia a dia.
5. O `WebScraperService` executa seleções de formulários via JS, lida com a paginação da tabela de resultados e extrai os campos (incluindo a congregação).
6. Ordena todas as listas por dia relativo à segunda-feira de início da semana.
7. Normaliza a ordem dos nomes dos casais e remove duplicidades em casamentos.
8. Gera o PDF final consolidado via `PdfService` com tabelas de 4 colunas.
9. Envia o PDF pelo WhatsApp para o número configurado.
10. Encerra a sessão do navegador.

## Docker / Integração WhatsApp

O sistema usa a [Evolution API](https://doc.evolution-api.com/v2/pt/get-started/introduction) rodando localmente via Docker para enviar as mensagens.

O arquivo `docker/docker-compose.yml` inicia um container da API na porta `8081`. A chave de segurança (API Key) deve ser configurada em um arquivo `.env` dentro da pasta `docker/` como `EVOLUTION_API_KEY=...` e não deve ser versionada.

Passos para conectar o número:
1. Suba a API (`docker compose up -d`).
2. Acesse o Evolution Manager no navegador: `http://localhost:8081/manager`.
3. Crie uma instância com nome `igreja`, integração `Baileys` e informe sua chave.
4. Conecte a instância escaneando o QR Code pelo WhatsApp do seu celular.

## Segurança E Configuração

- Não documentar, commitar ou expor credenciais, chaves de API, números de telefone, valores de autenticação ou segredos locais.
- Manter as senhas e configurações de disparo seguras nos arquivos locais de configuração Spring (`application.properties`) e no arquivo `.env` do Docker.
- Tratar PDFs gerados como dados sensíveis (contêm informações pessoais).

## Requisitos Operacionais

- Java 17 ou superior
- Maven
- Google Chrome instalado no sistema operacional
- Docker, quando o envio por WhatsApp for necessário
- Credenciais privadas válidas para o sistema Church
- Instância da Evolution API configurada e conectada ao WhatsApp

## Notas De Manutenção

- **Vulnerabilidade de Automação Web**: O scraping depende da estrutura HTML da plataforma externa. Se a plataforma alterar IDs de elementos, classes de tabela ou fluxo de navegação, o `WebScraperService` precisará de ajustes.
- **Lógica de Transição de Mês**: O `RelatorioSemanalService` resolve a fragmentação da busca semanal automaticamente quando há troca de mês.
- A tarefa agendada roda toda segunda-feira às 08:00 no fuso America/Fortaleza.
