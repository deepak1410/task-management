package com.deeptechhub.taskservice.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class TestContainersConfig {

    @Container
    static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Container
    static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:alpine")
            .withExposedPorts(6379);

    @Container
    static final GenericContainer<?> mailhogContainer = new GenericContainer<>("mailhog/mailhog")
            .withExposedPorts(1025, 8025);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // Database
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // Redis
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", redisContainer::getFirstMappedPort);

        // Mail (MailHog)
        registry.add("spring.mail.host", mailhogContainer::getHost);
        registry.add("spring.mail.port", mailhogContainer::getFirstMappedPort);
    }
}