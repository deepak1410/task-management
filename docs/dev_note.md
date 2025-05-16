## Implementing Role Based Access Control (RBAC)
* Created an enum Role that contains different roles. Spring expects roles to be prefixed with ROLE_. This method does this via getAuthority().
* In JwtAuthenticationFilter set authority via UserDetailsMapper.
* In JwtAuthenticationFilter create UsernamePasswordAuthenticationToken with this authority and set that in the SecurityContext.
* Add @EnableMethodSecurity in the main class TaskServiceApplication to enable role-based access.
* In UserDetailsServiceImpl set authority.
* Add an Exception handler for AccessDenied in GlobalExceptionHandler.
* In the RestController class add @PreAuthorize with hasAnyRole on the endpoints

## Implementing Unit/Integration tests
* The project covers following types of tests:
  - Unit tests: Focus on testing logic in services, utils, mappers
  - Integration tests: Focus on testing endpoints
* Add test dependencies:
  - spring-boot-starter-test, mockito-core, junit-jupiter, testcontainer in the respective pom.xml files
  - In child modules such as identity-service and task-service only add following dependency as JUnit, Mockito, and more — it's a comprehensive test starter covers common testing needs in Spring Boot applications.
  ```<dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-test</artifactId>
     <scope>test</scope>
  </dependency>```