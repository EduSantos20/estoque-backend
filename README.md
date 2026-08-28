# Estoque de Chuteiras (Backend API)

Esta é a API de backend para o sistema de controle de estoque de chuteiras. O sistema foi desenvolvido com Java e Spring Boot para gerenciar o estoque, vendas, usuários e relatórios de vendas semanais.

## Tecnologias Utilizadas

*   **Linguagem:** Java 25
*   **Framework:** Spring Boot 4.1.0
*   **Segurança:** Spring Security com autenticação JWT
*   **Banco de Dados:** PostgreSQL
*   **Persistência:** Spring Data JPA / Hibernate
*   **Utilitários:** Lombok, Spring Boot Starter Validation
*   **Gerenciador de Dependências:** Maven

## Requisitos

*   Java Development Kit (JDK) 25 ou superior
*   Maven
*   PostgreSQL

## Configuração

1.  **Banco de Dados:**
    Certifique-se de ter um banco de dados PostgreSQL criado com o nome `estoque_chuteiras`.

2. **Variáveis de Configuração:**
    Edite o arquivo `src/main/resources/application.yml` para configurar a conexão com o banco de dados e outras propriedades:

    ```yaml
    spring:
      datasource:
        url: jdbc:postgresql://localhost:5432/estoque_chuteiras
        username: seu_usuario
        password: sua_senha

    app:
      jwt:
        secret: coloque_aqui_uma_chave_secreta_com_pelo_menos_32_caracteres
    ```

## Como Executar

Para rodar o projeto localmente, utilize o seguinte comando na raiz do projeto:

```bash
mvn spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

## Estrutura do Projeto

*   `src/main/java/com/loja/estoque/controller`: Endpoints da API.
*   `src/main/java/com/loja/estoque/dto`: Objetos de transferência de dados (requisições e respostas).
*   `src/main/java/com/loja/estoque/model`: Entidades JPA.
*   `src/main/java/com/loja/estoque/repository`: Interfaces de acesso a dados.
*   `src/main/java/com/loja/estoque/service`: Lógica de negócio.
*   `src/main/java/com/loja/estoque/security`: Configuração de segurança e autenticação JWT.
