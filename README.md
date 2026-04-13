# Relatório de Aniversariantes Automatizado

Este projeto foi criado para centralizar e automatizar a emissão de relatórios semanais da igreja. O objetivo é evitar o trabalho manual repetitivo, tendo um "robô" que acessa o sistema, extrai os dados e monta um documento pronto para uso.

Ele faz a distinção automática entre membros, congregados e aniversariantes de casamento, agrupando as informações por congregação e pela Sede.

### O que o robô faz exatamente?

Quando acionado, o sistema realiza os seguintes passos em segundo plano:

1. Abre o navegador de forma invisível.
2. Faz o login no sistema de gestão da igreja usando as credenciais configuradas.
3. Navega pelas telas de relatórios de Membros, Congregados e Casamentos.
4. Preenche as datas da semana atual e aplica os filtros necessários.
5. Extrai os dados das tabelas de resultado.
6. Processa as informações (como remover casais que foram cadastrados em duplicidade, organizando por ordem alfabética).
7. Gera um arquivo final consolidado (utilizando PDF e estruturação em Excel) com todas as listas formatadas.

### Tecnologias que usamos aqui

A base do projeto é construída com ferramentas robustas do ecossistema Java:

* Java 17 e Spring Boot 3.5
* Maven (gerenciador de dependências)
* Selenium WebDriver e WebDriverManager (para a automação do navegador)
* OpenPDF (para a criação e formatação do arquivo PDF final)
* Apache POI (para manipulação e geração de planilhas Excel)
* SpringDoc OpenAPI / Swagger UI (para documentar e facilitar o uso da API)
* Lombok (para deixar o código mais limpo)

### Como colocar para rodar na sua máquina

Se você precisar rodar, testar ou continuar o desenvolvimento, siga os passos abaixo.

**1. O que você vai precisar:**

* Ter o Java 17 (ou superior) instalado.
* Ter o Maven instalado no seu computador.
* Ter o navegador Google Chrome instalado (o robô precisa dele para navegar).

**2. Configurando as senhas:**

O sistema precisa acessar a plataforma da igreja, mas não deixamos as senhas expostas no código. Você precisa configurar o seu arquivo de propriedades.

Vá até a pasta `src/main/resources` e abra o arquivo `application.properties`. Preencha com os seus dados reais:

```properties
spring.application.name=RelatorioAniversariantes

# Credenciais para o login no sistema
app.credentials.client-code=SEU_CODIGO_DE_CLIENTE
app.credentials.username=SEU_USUARIO
app.credentials.password=SUA_SENHA
```

**3. Compilando e executando:**

Abra um terminal na pasta principal do projeto e peça ao Maven para baixar as dependências e construir a aplicação:
```bash
mvn clean install
```

Depois que compilar com sucesso, você pode rodar o projeto pela sua IDE de preferência ou executando o arquivo gerado:
```bash
java -jar target/RelatorioAniversariantes-0.0.1-SNAPSHOT.jar
```

### Como usar a API

A forma mais fácil e visual de testar a automação é usando a tela do Swagger que configuramos.

Com o projeto rodando, abra o seu navegador e acesse:
```bash
http://localhost:8080/swagger-ui.html
```

Nessa tela, você verá o endpoint de geração de relatórios. Basta abrir a opção, clicar no botão "Try it out" e depois em "Execute".

O servidor vai responder na hora que recebeu o seu pedido. Em seguida, basta acompanhar o console (terminal) para ver o robô trabalhando. Quando ele terminar, o arquivo PDF aparecerá na mesma pasta onde o projeto está rodando.

Fique à vontade para explorar, modificar e melhorar o código!