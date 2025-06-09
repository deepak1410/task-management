## Api-Gateway

### Purpose
Implementing api-gateway in microservice architecture is important for the following purposes:
* Routing, Security, Observability, Single-entry-point

### Technology choice
* Spring Cloud Gateway
* Netty Server

### Project structure for api-gateway

```
task-management/
├── .gitignore
├── docker-compose.yml
├── .env
├── secrets/    # Secrets used for local and production (gitignored)
│    ├── db_password.txt
│    ├── jwt_secret.txt
│    └── gmail_pass.txt
├── api-gateway/
│   ├── src/
│   │   ├── main/
│   │      ├── java/com/taskmanagement/apigateway/
│   │      │   ├── config/
│   │      │   ├── filters/
│   │      │   ├── security/
│   │      │   └── ApiGatewayApplication.java
│   │      └── resources/
│   │          ├── application.yml
│   │          └── bootstrap.yml
│   ├── Dockerfile
│   └── pom.xml
│
├───identity-service/
└── task-service/
```
## Features
* Centralized entry to the task-management app
* Request routing to the downstream microservices
* Swagger Aggregation

## Implementation steps
### 1. Create a new module api-gateway
* Add a new module api-gateway to the project as mentioned in the project structure.

### 2. Add dependencies
* Add spring-cloud and spring-boot BOMs to the parent of api-gateway POM.
* Add the required dependencies to the api-gateway POM.

### 3. Configure routing (application.yml)

### 4. Implement JWT Authentication Filter

### 5. Add Circuit Breaker Fallback

### 6. Add Rate-limiting

### 7. Update docker-compose.yml to include api-gateway
