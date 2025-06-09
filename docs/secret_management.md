### Project Structure to manage secrets

```
task-management/
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
* Environment variables and secrets have been segregated to ensure that configurations and secrets are managed separately.
* docker-compose contains a section `secrets` which contains the mappings of secrets with their relative paths.
```yaml
secrets:
  db_password:
    file: ./secrets/db_password.txt
  jwt_secret:
    file: ./secrets/jwt_secret.txt
  gmail_pass:
    file: ./secrets/gmail_pass.txt
```
* Services which are using these secrets also contain these secret variables.
* application.yml of the different services which are using these secrets have relative path mapping of the secret files.

```yaml
spring:
  profiles:
    active: local
  datasource:
    url: jdbc:postgresql://localhost:5432/authdb
    username: ${POSTGRES_USER:pguser}
    password-file: ../secrets/db_password.txt
    driver-class-name: org.postgresql.Driver

jwt:
  secret-file: ../secrets/jwt_secret.txt

mail:
  password-file: ../secrets/gmail_pass.txt
```
### Initializing secrets in project
* A SecretConfig class is added to read the secrets when the service is run using main application from IDE or command line.
* A DataBaseConfig class is also added to read and set the secrets for Database when the service is started. 

```java
@Configuration
public class SecretConfig {

    @Bean
    @Profile("!test") // Only activate this for non-test profiles
    public String jwtSecret(@Value("${jwt.secret-file}") String secretFile) throws IOException {
        return readSecretFile(secretFile, "JWT secret");
    }

    @Bean
    @Profile("!test")
    public String gmailPassword(@Value("${mail.password-file}") String passwordFile) throws IOException {
        return readSecretFile(passwordFile, "Gmail password");
    }

    private String readSecretFile(String filePath, String secretName) throws IOException {
        Resource resource = new FileSystemResource(filePath);
        if (!resource.exists()) {
            throw new RuntimeException(secretName + " file not found at: " + filePath);
        }
        return Files.readString(Paths.get(resource.getURI())).trim();
    }
}
```
