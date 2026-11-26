# Restaurant Management API

API REST de gestão de usuários para um sistema compartilhado de restaurantes, desenvolvida para o **Tech Challenge – Fase 1** da Pós-Tech FIAP.

Clientes e donos de restaurante podem se cadastrar, buscar usuários, atualizar o perfil, trocar a senha, excluir a conta e fazer login com autenticação JWT.

## Tecnologias

| Categoria | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Validation, Security) |
| Autenticação | JWT com Spring Security OAuth2 Resource Server (HS256) |
| Banco de dados | PostgreSQL 17 |
| Migrations | Flyway |
| Senhas | BCrypt |
| Documentação | springdoc-openapi (Swagger UI) |
| Testes | JUnit 5, Mockito, AssertJ, Spring MockMvc |
| Infraestrutura | Docker (build multi-stage) e Docker Compose |

## Funcionalidades

- Cadastro de usuários dos tipos `CUSTOMER` e `RESTAURANT_OWNER`
- E-mail e login únicos entre usuários ativos
- Busca por id e busca paginada por nome
- Atualização de perfil, separada da troca de senha
- Troca de senha com confirmação da senha atual
- Exclusão lógica: o usuário é marcado com `deleted_at`, e não removido do banco
- Login com geração de token JWT
- Registro da data da última alteração (`lastModifiedAt`)
- Erros padronizados no formato **ProblemDetail (RFC 7807)**
- API versionada em `/api/v1`

## Pré-requisitos

- [Docker](https://www.docker.com/) com Docker Compose
- Opcional, apenas para rodar os testes fora do Docker: JDK 21

## Como executar

### 1. Clone o repositório

```bash
git clone https://github.com/buffo2fs/fiap-tech-challenge-fase1.git
cd fiap-tech-challenge-fase1
```

### 2. Crie o arquivo `.env`

Copie o modelo:

```bash
# Linux / macOS / Git Bash
cp .env.example .env
```

```powershell
# Windows PowerShell
Copy-Item .env.example .env
```

### 3. Gere a chave do JWT

A chave assina os tokens e deve ter pelo menos 256 bits, em Base64. Gere uma chave aleatória:

```bash
# Linux / macOS / Git Bash
openssl rand -base64 32
```

```powershell
# Windows PowerShell
$b = New-Object byte[] 32; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b)
```

Abra o `.env` e substitua o valor de `JWT_SECRET` pela chave gerada.

### 4. Suba a aplicação

```bash
docker compose up --build -d
```

O Compose sobe o PostgreSQL e a API. As tabelas são criadas automaticamente pelo Flyway. Aguarde cerca de 20 segundos e confira:

```bash
docker compose ps
```

A API fica disponível em `http://localhost:8080`.

### 5. Para encerrar

```bash
docker compose down -v
```

O `-v` também apaga o volume do banco. Sem ele, os dados são mantidos para a próxima execução.

## Variáveis de ambiente

| Variável | Descrição | Padrão |
|---|---|---|
| `DB_NAME` | Nome do banco | `restaurant_db` |
| `DB_USER` | Usuário do banco | `restaurant_user` |
| `DB_PASSWORD` | Senha do banco | `change_me` |
| `DB_PORT` | Porta do PostgreSQL exposta no host | `5432` |
| `APP_PORT` | Porta da API exposta no host | `8080` |
| `JWT_SECRET` | Chave de assinatura do JWT, em Base64, com 32 bytes ou mais | **obrigatória** |
| `JWT_EXPIRATION_MINUTES` | Validade do token, em minutos | `60` |

Se a porta 5432 já estiver em uso, altere `DB_PORT` no `.env`, por exemplo para `5433`.

## Autenticação

1. Cadastre um usuário em `POST /api/v1/users`.
2. Faça login em `POST /api/v1/auth/login`. A resposta contém o `accessToken`.
3. Envie o token nas demais requisições:

```http
Authorization: Bearer <accessToken>
```

Regras de acesso:

- **Públicos:** cadastro, login e Swagger.
- **Exigem token:** busca por id e busca por nome.
- **Exigem token e só podem ser feitos pelo próprio usuário:** atualização, troca de senha e exclusão. Outro usuário recebe `403`.

## Endpoints

| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| `POST` | `/api/v1/users` | Cadastrar usuário | Público |
| `POST` | `/api/v1/auth/login` | Login e emissão do token | Público |
| `GET` | `/api/v1/users/{id}` | Buscar por id | Token |
| `GET` | `/api/v1/users?name=` | Buscar por nome, com paginação | Token |
| `PUT` | `/api/v1/users/{id}` | Atualizar perfil | Próprio usuário |
| `PATCH` | `/api/v1/users/{id}/password` | Trocar senha | Próprio usuário |
| `DELETE` | `/api/v1/users/{id}` | Excluir conta (exclusão lógica) | Próprio usuário |

A busca por nome aceita os parâmetros `page`, `size` e `sort`, por exemplo `?name=john&page=0&size=10&sort=name`.

### Formato dos erros

Todos os erros seguem o padrão ProblemDetail (RFC 7807):

```json
{
  "type": "about:blank",
  "title": "Resource not found",
  "status": 404,
  "detail": "User with id 999 not found",
  "instance": "/api/v1/users/999"
}
```

| Status | Quando ocorre |
|---|---|
| `400` | Campos inválidos ou parâmetros incorretos |
| `401` | Token ausente, inválido ou expirado, ou credenciais incorretas |
| `403` | Tentativa de alterar a conta de outro usuário |
| `404` | Usuário não encontrado |
| `409` | E-mail ou login já em uso |

## Documentação

### Swagger

Com a aplicação rodando, acesse `http://localhost:8080/swagger-ui.html`.

Para testar as rotas protegidas: execute o login, copie o `accessToken` da resposta, clique em **Authorize** e cole o token.

A especificação OpenAPI também está disponível em [`docs/openapi.yaml`](docs/openapi.yaml).

### Postman

A pasta [`postman`](postman) contém:

- `restaurant-management-api.postman_collection.json`: 20 requisições, com cenários de sucesso e erro e testes automatizados
- `restaurant-management-local.postman_environment.json`: environment local

Como usar:

1. No Postman, clique em **Import** e selecione os dois arquivos.
2. Selecione o environment **Restaurant Management - Local**.
3. Clique com o botão direito na coleção → **Run collection** → **Run**.

A coleção cadastra um usuário, faz login, salva o token automaticamente, testa todos os endpoints e, ao final, exclui o usuário. Por isso, ela pode ser executada várias vezes seguidas.

## Testes automatizados

Os testes unitários e de controller não dependem do banco nem do Docker:

```bash
# Linux / macOS / Git Bash
./mvnw test
```

```powershell
# Windows PowerShell
.\mvnw test
```

## Estrutura do projeto

```text
src/main/java/br/com/fiap/restaurant_management
├── config        # Segurança, JWT, OpenAPI e BCrypt
├── controller    # Endpoints REST
├── domain        # Entidades JPA (User, Address, UserType)
├── dto           # Objetos de entrada e saída da API
├── exception     # Exceções e tratamento global de erros
├── mapper        # Conversão entre entidades e DTOs
├── repository    # Acesso a dados (Spring Data JPA)
└── service       # Regras de negócio

src/main/resources
├── application.yml
└── db/migration  # Scripts do Flyway
```

## Decisões técnicas

- **Exclusão lógica:** preserva o histórico e os vínculos futuros, como pedidos e avaliações. Índices únicos parciais (`WHERE deleted_at IS NULL`) permitem reutilizar o e-mail e o login de usuários excluídos.
- **Troca de senha em endpoint separado:** a atualização de perfil nunca altera a senha. A troca exige a senha atual.
- **Mesma resposta para login inexistente e senha incorreta:** evita revelar quais logins existem no sistema.
- **Senhas com BCrypt:** a senha nunca é armazenada nem devolvida em texto puro.
- **Validação do schema:** o Hibernate só valida (`ddl-auto: validate`). As alterações do banco são feitas exclusivamente por migrations do Flyway.

## Autor

Lucas Buffo – Pós-Tech FIAP