# ImobSystem API

API REST de um sistema de gestão imobiliária (CRM). Cada imobiliária cadastrada tem seus próprios corretores, imóveis, clientes e negociações, e os dados são isolados por imobiliária a cada requisição. O projeto também expõe um feed XML de imóveis para portais e integra assinatura de planos via Asaas.

## Tecnologias

| Camada | Tecnologia | Versão |
|---|---|---|
| Linguagem | Java | 21 |
| Framework | Spring Boot | 4.0.6 |
| Web | Spring Boot Starter WebMVC | — |
| Persistência | Spring Data JPA / Hibernate | — |
| Banco de dados | PostgreSQL | — |
| Segurança | Spring Security + JWT (jjwt) | 0.12.6 |
| Documentação | springdoc-openapi (Swagger UI) | 2.5.0 |
| Boilerplate | Lombok | — |
| Build | Maven (wrapper 3.3.4) | — |
| Container | Docker (multi-stage, Temurin 21) | — |

## Funcionalidades

- Cadastro de imobiliária com usuário administrador e login com JWT.
- Gestão de corretores com dois perfis (`ADMIN` e `CORRETOR`), incluindo métricas por corretor e ranking de captações.
- Cadastro de imóveis com filtros opcionais por endereço, status, finalidade e tipo.
- Fotos de imóveis (lista de URLs vinculadas ao imóvel).
- Cadastro de clientes (comprador, locatário ou proprietário).
- Negociações ligando imóvel, cliente e corretor, com funil de status.
- Feed XML no formato ListingDataFeed, em rota pública, para consumo por portais.
- Assinatura de planos (`BASICO`, `PROFISSIONAL`, `PREMIUM`) via Asaas, com webhook de pagamento e bloqueio das rotas quando o plano vence.
- Assistente de chat que consulta a API da Anthropic usando os dados visíveis ao corretor logado e pode cadastrar imóvel ou cliente a partir da conversa.

## Arquitetura e estrutura de pastas

Organização em camadas: os `controllers` só recebem a requisição e delegam; a regra de negócio e as verificações de acesso ficam nos `services`; o acesso ao banco fica nos `repositories` (interfaces do Spring Data JPA); as entidades JPA ficam em `models`; e a entrada e saída da API trafega em `records` dentro de `dtos`. O pacote `config` reúne segurança (filtro JWT, configuração do Spring Security), CORS, o interceptor que checa o plano e o handler global de exceções.

```
src/main/java/com/system/imob
├── ImobApplication.java
├── config/          # SecurityConfig, JwtUtil, JwtAuthFilter, AuthUtil,
│                    # CorsConfig, WebConfig, PlanoInterceptor, GlobalExceptionHandler
├── controllers/     # Auth, Imobiliaria, Plano, Corretor, Imovel, FotoImovel,
│                    # Cliente, Negociacao, Chat, Feed, Webhook
├── services/        # regras de negócio e controle de acesso
├── repositories/    # interfaces JpaRepository
├── models/          # Imobiliaria, Corretor, Imovel, FotoImovel, Cliente, Negociacao
├── dtos/
│   ├── requests/
│   └── responses/
└── enums/           # PerfilUsuario, StatusImovel, TipoImovel, Finalidade,
                     # TipoCliente, StatusNegocio, TipoPlano, StatusPlano
```

O modelo de dados e o diagrama ER estão em [docs/modelagem.md](docs/modelagem.md).

## Endpoints

A coluna "Autenticação" indica se a rota exige o header `Authorization: Bearer <token>`.

### Autenticação

| Método | Rota | Descrição | Autenticação |
|---|---|---|---|
| POST | `/auth/registro` | Cria a imobiliária e o corretor administrador, e devolve o token | Não |
| POST | `/auth/login` | Autentica por e-mail e senha e devolve o token | Não |

### Imobiliárias e planos

| Método | Rota | Descrição | Autenticação |
|---|---|---|---|
| POST | `/imobiliarias/cadastrar` | Cadastra uma imobiliária | Não |
| GET | `/imobiliarias` | Lista as imobiliárias | Sim |
| GET | `/imobiliarias/{id}` | Busca uma imobiliária por id | Sim |
| GET | `/imobiliarias/minha` | Retorna a imobiliária do corretor logado | Sim |
| PUT | `/imobiliarias/logo` | Atualiza a logo (Base64) da própria imobiliária; só `ADMIN` | Sim |
| POST | `/imobiliarias/plano` | Assina ou troca o plano e devolve o link de pagamento; só `ADMIN` | Sim |
| GET | `/imobiliarias/plano` | Status do plano, vencimento e dias restantes | Sim |

### Corretores

| Método | Rota | Descrição | Autenticação |
|---|---|---|---|
| POST | `/corretores/cadastrar` | Cadastra um corretor em uma imobiliária | Não |
| GET | `/corretores` | Lista os corretores da imobiliária; filtros opcionais `nome`, `email` e `perfil`; só `ADMIN` | Sim |
| GET | `/corretores/{id}` | Busca um corretor por id; só `ADMIN` | Sim |
| GET | `/corretores/{id}/imoveis` | Imóveis captados pelo corretor; só `ADMIN` | Sim |
| GET | `/corretores/{id}/clientes` | Clientes do corretor; só `ADMIN` | Sim |
| GET | `/corretores/{id}/metricas` | Totais de imóveis, clientes, negociações e negócios ganhos; só `ADMIN` | Sim |
| GET | `/corretores/captacoes` | Quantidade de imóveis captados por corretor; só `ADMIN` | Sim |
| DELETE | `/corretores/{id}` | Remove um corretor da própria imobiliária; só `ADMIN` | Sim |

### Imóveis e fotos

| Método | Rota | Descrição | Autenticação |
|---|---|---|---|
| POST | `/imoveis` | Cadastra um imóvel vinculado ao corretor logado | Sim |
| GET | `/imoveis` | Lista imóveis; filtros opcionais `endereco`, `status`, `finalidade` e `tipo` | Sim |
| GET | `/imoveis/{id}` | Busca um imóvel por id | Sim |
| PUT | `/imoveis/{id}` | Atualiza um imóvel | Sim |
| DELETE | `/imoveis/{id}` | Remove um imóvel | Sim |
| POST | `/imoveis/{imovelId}/fotos` | Adiciona a URL de uma foto ao imóvel | Sim |
| GET | `/imoveis/{imovelId}/fotos` | Lista as fotos do imóvel | Sim |
| DELETE | `/imoveis/{imovelId}/fotos/{fotoId}` | Remove uma foto | Sim |

### Clientes

| Método | Rota | Descrição | Autenticação |
|---|---|---|---|
| POST | `/clientes` | Cadastra um cliente | Sim |
| GET | `/clientes` | Lista os clientes visíveis ao corretor logado | Sim |
| GET | `/clientes/{id}` | Busca um cliente por id | Sim |
| PUT | `/clientes/{id}` | Atualiza um cliente | Sim |
| DELETE | `/clientes/{id}` | Remove um cliente | Sim |

### Negociações

| Método | Rota | Descrição | Autenticação |
|---|---|---|---|
| POST | `/negociacoes` | Cria uma negociação entre imóvel, cliente e corretor | Sim |
| GET | `/negociacoes` | Lista as negociações visíveis ao corretor logado | Sim |
| GET | `/negociacoes/{id}` | Busca uma negociação por id | Sim |
| PUT | `/negociacoes/{id}/status` | Atualiza o status da negociação no funil | Sim |

### Chat, feed e webhook

| Método | Rota | Descrição | Autenticação |
|---|---|---|---|
| POST | `/chat` | Envia uma mensagem ao assistente e recebe a resposta ou o registro criado | Sim |
| GET | `/feed/xml/{imobiliariaId}` | Feed XML dos imóveis marcados para publicação em portais | Não |
| POST | `/webhooks/asaas` | Recebe eventos de pagamento do Asaas | Não (valida o header `asaas-access-token`) |

## Autenticação e segurança

O login e o registro devolvem um JWT assinado em HMAC, com o e-mail no `subject` e o perfil e o id da imobiliária como claims. A cada requisição, o `JwtAuthFilter` lê o header `Authorization: Bearer <token>`, valida a assinatura e a expiração, carrega o corretor pelo e-mail e o coloca no contexto do Spring Security. A API é stateless (sem sessão) e as senhas são gravadas com BCrypt.

São dois perfis. O `ADMIN` enxerga todos os registros da própria imobiliária e é o único que gerencia corretores, plano e logo. O `CORRETOR` só acessa os imóveis, clientes e negociações que ele mesmo criou. Em nenhum caso um corretor alcança dados de outra imobiliária.

Além disso, o `PlanoInterceptor` responde `402 Payment Required` quando o plano da imobiliária está vencido ou inativo, liberando apenas as rotas de login, de plano e do webhook, para que a assinatura possa ser renovada.

## Como executar

### Pré-requisitos

- JDK 21
- PostgreSQL com um banco criado (por padrão `imobsystem`)
- Docker, se preferir rodar em container
- Maven não precisa estar instalado: o projeto usa o Maven Wrapper

### Variáveis de ambiente

Todas têm um valor padrão no `application.properties`, voltado ao ambiente local. Em qualquer ambiente exposto, defina pelo menos `DATABASE_PASSWORD`, `JWT_SECRET` e as chaves das integrações.

| Variável | Descrição | Exemplo |
|---|---|---|
| `DATABASE_URL` | URL JDBC do PostgreSQL | `jdbc:postgresql://localhost:5432/imobsystem` |
| `DATABASE_USERNAME` | Usuário do banco | `postgres` |
| `DATABASE_PASSWORD` | Senha do banco | `sua-senha` |
| `JWT_SECRET` | Chave de assinatura do JWT, com no mínimo 32 caracteres | `troque-esta-chave-por-uma-com-32-ou-mais` |
| `JWT_EXPIRATION` | Validade do token em milissegundos | `86400000` |
| `ANTHROPIC_API_KEY` | Chave da API usada pelo `/chat` | `sua-chave` |
| `ANTHROPIC_MODEL` | Modelo usado no `/chat` | `claude-haiku-4-5` |
| `ASAAS_API_KEY` | Chave da API do Asaas | `sua-chave` |
| `ASAAS_API_URL` | URL base do Asaas | `https://sandbox.asaas.com/api/v3` |
| `ASAAS_WEBHOOK_TOKEN` | Token esperado no header do webhook | `seu-token` |

O schema do banco é criado e atualizado pelo Hibernate (`spring.jpa.hibernate.ddl-auto=update`), então não há migrations para rodar.

### Rodando com o Maven Wrapper

Linux e macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Para gerar o `.jar` e executá-lo:

```bash
./mvnw clean package
java -jar target/imob-0.0.1-SNAPSHOT.jar
```

### Rodando com Docker

O `Dockerfile` compila o projeto e roda apenas o `.jar` no estágio final.

```bash
docker build -t imobsystem-api .
docker run -p 8081:8081 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/imobsystem \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=sua-senha \
  -e JWT_SECRET=troque-esta-chave-por-uma-com-32-ou-mais \
  imobsystem-api
```

### Acessos

A aplicação sobe na porta `8081`.

- API: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI (JSON): `http://localhost:8081/api-docs`

## Testes

Há um teste de carga do contexto do Spring (`ImobApplicationTests`), que sobe a aplicação e falha se alguma configuração ou bean estiver quebrado. Como o contexto inclui o JPA, o PostgreSQL precisa estar acessível para o teste rodar.

```bash
./mvnw test
```

## Autor

Victor Hugo Gomes

- LinkedIn: https://linkedin.com/in/victorgomesdev
- GitHub: https://github.com/VictorHGomes
