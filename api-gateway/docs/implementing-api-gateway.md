## Features included
* Request routing to the downstream microservices such as identity-service and task-service.
* Centralized logging using CorrelationId.
* Centralized API documentation. URL: http://localhost:8070/swagger-ui/index.html
  - Integration with API documentation of downstream microservices
* Rate Limiting and Throttling
* Request/Response Logging

## Adding Rate Limiting in Api-Gateway
Rate limiting helps in achieving:
- Fair usage across clients.
- Protects Backend services from DDoS attacks or accidental flood.
- Improve system reliability under load.

### Preferred option
- RateLimiter using Redis and Spring Cloud Gateway
- Algorithm: Token bucket

### How Redis RateLimiter works
Each request is evaluated against a configured policy:
- replenishRate: tokens added per seconds.
- burstCapacity: max tokens stored.
- requestedToken: tokens required per requests.

### Implementation steps
* Add following dependencies
  - spring-cloud-starter-gateway
  - spring-boot-starter-data-redis-reactive
* Add Redis as a service in docker-compose and run the redis service.
* Configure rate-limiting policies in application.yml
* Add a Rate-limiting config with IPKeyResolver bean.
* Add a RateLimitHeaderFilter for setting up headers

## Future Changes
* Circuit Breaker using Resilience4J
* Distributed Tracing using Sleuth + Zipkin
* Metrics & Monitoring (Prometheus + Grafana)
* Service Discovery Integration