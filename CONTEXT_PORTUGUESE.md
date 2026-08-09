# Contexto do Projeto

## Visão Geral

RelatorioAniversariantes é uma aplicação Java 17 com Spring Boot que automatiza o fluxo semanal de relatórios de aniversariantes e casamentos de uma igreja. Ela acessa a plataforma de gestão Church (especificamente `https://church15.churchsoftware.com.br/frmlogin/`) por automação de navegador, coleta dados de membros, congregados e aniversariantes de casamento, processa o resultado, gera um PDF consolidado e envia o documento pelo WhatsApp usando uma instância local da Evolution API.

O objetivo do projeto é reduzir trabalho manual repetitivo e gerar um relatório semanal pronto para compartilhamento, fazendo a distinção automática entre membros, congregados e aniversariantes de casamento, agrupando as informações por congregação e pela Sede.

## Responsabilidades Principais

- Disparar a geração do relatório por meio de um endpoint HTTP (`GET /relatorios/gerarAniversariantes`).
- Expor um endpoint de monitoramento (`GET /relatorios/status`) para consultar o estado em tempo real (`IDLE`, `EM_PROGRESSO`, `SUCESSO`, `ERRO`), duração, totais extraídos e mensagens de erro.
- Executar a mesma geração automaticamente toda segunda-feira às 08:00 no fuso America/Fortaleza (`@Scheduled`).
- Usar Selenium WebDriver com Chrome em modo headless para acessar o sistema externo da igreja, contando com tentativas automáticas em caso de falha de login.
- Tratar automaticamente semanas que cruzam a virada de mês (ex: 29/06 a 05/07), fragmentando as consultas dia a dia para garantir a extração completa dos dados.
- Extrair aniversariantes membros, aniversariantes congregados e aniversariantes de casamento (incluindo dados da congregação) da semana atual.
- Ordenar os registros cronologicamente em relação ao início da semana (segunda-feira).
- Remover casais duplicados nos aniversários de casamento normalizando a ordem dos cônjuges.
- Tratar paginação em múltiplos resultados de busca e seleção atômica de seletores via execução de JavaScript.
- Gerar um PDF formatado em pasta dedicada (`relatorios/`) com tabelas de 4 colunas (com repetição de cabeçalho e divisão dinâmica de páginas).
- Realizar limpeza automática (`RelatorioLimpezaService`) de relatórios PDF mais antigos que a retenção configurada (padrão: 30 dias).
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
└── service/          # Lógica de Negócio, Orquestração, Raspagem, Rastreamento, Retenção, PDF e WhatsApp
```

### Componentes Principais

- `RelatorioAniversariantesApplication`: Ponto de entrada Spring Boot. Habilita execução assíncrona e tarefas agendadas.
- `RelatorioAniversariantesController`: Atua como o "recepcionista" da API. Expõe a geração de relatórios (`GET /relatorios/gerarAniversariantes`) e consulta de status (`GET /relatorios/status`).
- `RelatorioAniversariantesService`: Orquestra o fluxo de alto nível, disparando o agendamento, desduplicando casais, atualizando o rastreador de status, gerando o PDF e solicitando o envio via WhatsApp.
- `RelatorioExecucaoTrackerService`: Mantém em memória o estado da execução (`IDLE`, `EM_PROGRESSO`, `SUCESSO`, `ERRO`), horários, duração, caminho do arquivo e totais de registros.
- `RelatorioLimpezaService`: Apaga automaticamente arquivos PDF antigos da pasta `relatorios/` de acordo com a retenção configurada (30 dias).
- `RelatorioSemanalService`: Responsável pelo cálculo do período semanal. Caso a semana perpasse dois meses diferentes, realiza a extração dia a dia para contornar limitações do formulário do sistema Church, consolida as listas e ordena os dados cronologicamente a partir de segunda-feira.
- `WebScraperService`: Especialista em navegação Selenium com tentativas de login, seleção atômica via JS, tratamento de paginação e encerramento seguro do driver em bloco `finally`.
- `PdfService`: Especialista em design e formatação de documentos. Garante a criação da pasta `relatorios/` e gera tabelas de 4 colunas com repetição de cabeçalho.
- `WhatsAppService`: Lê o PDF gerado a partir do diretório de saída, converte para Base64 e envia como documento via Evolution API.
- DTOs (`AniversarianteDTO`, `CasamentoDTO`, `DadosRelatorioDTO`): Representam os dados extraídos.

## API

Endpoints sob `/relatorios`:

```text
GET /relatorios/gerarAniversariantes
GET /relatorios/status
```

Comportamento esperado:
- `GET /relatorios/gerarAniversariantes`: Retorna HTTP `202 Accepted` quando o processo é iniciado assincronamente.
- `GET /relatorios/status`: Retorna HTTP `200 OK` com o estado atual, horários de execução, duração, totais de membros/congregados/casamentos e caminho do arquivo.
- Padrão do PDF gerado: `relatorios/relatorio_aniversariantes_YYYY-MM-DD.pdf`

Com a aplicação em execução, o Swagger UI fica disponível em:
`http://localhost:8080/swagger-ui.html`

## Fluxo de Execução

1. Calcula o período da semana atual (de segunda-feira a domingo).
2. Atualiza o status no rastreador para `EM_PROGRESSO`.
3. Inicia o Chrome headless com retry no login.
4. Autentica no sistema externo Church.
5. Coleta os dados através do `RelatorioSemanalService` (busca direta ou fragmentada por dia na troca de mês).
6. O `WebScraperService` executa seleções via JS, navega pelas páginas de resultados e extrai os campos.
7. Ordena todas as listas por dia relativo à segunda-feira.
8. Normaliza a ordem dos nomes dos casais e remove duplicidades.
9. Gera o PDF final consolidado via `PdfService` na pasta `relatorios/`.
10. Envia o PDF pelo WhatsApp para o número configurado.
11. Atualiza o rastreador com o status `SUCESSO` (ou `ERRO` em caso de falha).
12. Encerra a sessão do navegador no bloco `finally`.

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
- **Retenção de Arquivos**: O `RelatorioLimpezaService` executa diariamente às 03:00 para apagar PDFs antigos.
- A tarefa agendada roda toda segunda-feira às 08:00 no fuso America/Fortaleza.
