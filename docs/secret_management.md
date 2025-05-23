### Project Structure to manage secrets

```
deeptechhub/
├── .gitignore
├── docker-compose.yml
├── .env
├── secrets/                # Secrets used for local and production (gitignored)
│    ├── db_password.txt
│    ├── jwt_secret.txt
│    └── gmail_pass.txt
├── identity-service/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/
│   │       │       └── deeptechhub/
│   │       │           └── identityservice/
│   │       │               ├── config/
│   │       │               │   ├── SecretConfig.java
│   │       │               │
│   │       │               └── IdentityServiceApplication.java
│   │       └── resources/
│   │           ├── application.yml
│   │           ├── application-docker.yml
│   │           └── application-local.yml
│   └── Dockerfile
└── task-service/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── deeptechhub/
│       │           └── taskservice/
│       │               ├── config/
│       │               │   └── SecretConfig.java
│       │               └── TaskServiceApplication.java
│       └── resources/
│           ├── application.yml
│           ├── application-docker.yml
│           └── application-local.yml
└── Dockerfile
```