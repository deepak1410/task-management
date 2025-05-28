## Folder Structure for Log Generation using Correlation ID

```
common-lib/
    src/main/java/com/deeptech/common/logging/
        CommonConstants.java
        CorrelationIdFilter.java

identity-service/
    src/main/resources/
        logback-spring.xml
    src/main/java/com/deeptech/identityservice/config/
        LoggingConfig.java

task-service/
    src/main/java/com/deeptech/taskservice/config/
        FeignConfig.java
    src/main/resources/
        logback-spring.xml
    src/main/java/com/deeptech/identityservice/config/
        LoggingConfig.java
```

## Steps
* Add slf4j-api dependency in common-lib and slf4j-api and logstash-logback-encoder dependencies in each service.
* Add correlationId related constants in CommonApplicationConstants.
* Add a filter CorrelationIdFilter to filter the HTTP requests and put correlationId to the MDC.
* Create a LoggingConfig configuration class in each service to create a FilterRegistrationBean bean and set URL patterns.
* Create a FeignConfig configuration class in the service that uses feign and set correlationId in the template header.
* Create logback-spring.xml in each service to configure logging. This xml file also sets the correlationId. 