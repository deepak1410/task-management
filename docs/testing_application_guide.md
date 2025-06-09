## Implementing Unit/Integration tests
* The project covers following types of tests:
    - Unit tests: Focus on testing logic in services, utils, mappers
    - Integration tests: Focus on testing endpoints

### Add test dependencies:
* Add spring-boot-dependencies and testcontainers BOM in the pom.xml of parent project.
  ```xml
      <dependencyManagement>
          <dependencies>
              <!-- Spring Boot BOM (manages JUnit/Mockito transitively) -->
              <dependency>
                  <groupId>org.springframework.boot</groupId>
                  <artifactId>spring-boot-dependencies</artifactId>
                  <version>${spring-boot.version}</version>
                  <type>pom</type>
                  <scope>import</scope>
              </dependency>
 
              <!-- Testcontainers BOM -->
              <dependency>
                  <groupId>org.testcontainers</groupId>
                  <artifactId>testcontainers-bom</artifactId>
                  <version>${testcontainers.version}</version>
                  <type>pom</type>
                  <scope>import</scope>
              </dependency>
          </dependencies>
      </dependencyManagement>
  ```
* Add JaCoCo plugin as dependency-management in the parent project. 
  ```xml
   <!-- JaCoCo maven plugin -->
    <plugin>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
        <version>0.8.12</version>
        <executions>
            <execution>
                <goals>
                    <goal>prepare-agent</goal>
                </goals>
            </execution>
            <execution>
                <id>report</id>
                <phase>prepare-package</phase>
                <goals>
                    <goal>report</goal>
                </goals>
                <configuration>
                    <excludes>
                        <exclude>**/dto/**</exclude>
                        <exclude>**/config/**</exclude>
                        <exclude>**/exception/**</exclude>
                        <exclude>**/domain/**</exclude>
                        <exclude>**/*Application*</exclude>
                    </excludes>
                </configuration>
            </execution>
        </executions>
    </plugin>
  ```
* Child modules contain following dependencies:
```xml
<!-- Test -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.security</groupId>
			<artifactId>spring-security-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.mockito</groupId>
			<artifactId>mockito-junit-jupiter</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.testcontainers</groupId>
			<artifactId>junit-jupiter</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.testcontainers</groupId>
			<artifactId>postgresql</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.testcontainers</groupId>
			<artifactId>testcontainers</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>test</scope>
		</dependency>
```
* Add jacoco maven plugin in children pom.xml for generating test coverage reports.

  ```xml
  <plugin>
      <groupId>org.jacoco</groupId>
      <artifactId>jacoco-maven-plugin</artifactId>
  </plugin>
  ```

### Setting Test configuration
* Add application.yml for test in src/test/resources with following content.
* Add dummy values of secrets for test.
```
spring:
  profiles:
    active: test
  main:
    allow-bean-definition-overriding: true
    lazy-initialization: true  # Helps with test startup performance
  jpa:
    hibernate:
      ddl-auto: create-drop
```
* Create a configuration class TestConfig for setting test secrets.
* Create a TestContainersConfig class with @Testcontainers annotation to set datasource, redis, and mail configs.
* Create a BaseIntegrationTest with following annotations
  ```java
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    @Import(TestConfig.class)
    @Transactional
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
    public abstract class BaseIntegrationTest extends TestContainersConfig {
    
    }
  ```
* All the Unit test classes have @ExtendWith(MockitoExtension.class) on them to use Mockito for mocking.
* The integration test classes extend the BaseIntegrationTest so the settings defined there are used.
  ```java
    class UserControllerIntegrationTest extends BaseIntegrationTest
  ```