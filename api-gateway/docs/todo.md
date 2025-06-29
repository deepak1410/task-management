## Items for enhancements
### **1. Rate Limiting & Throttling**
- **Why?** Prevent abuse and ensure fair usage
- **Implementation**:
  ```yaml
  spring:
    cloud:
      gateway:
        routes:
          - id: task-service
            filters:
              - name: RequestRateLimiter
                args:
                  redis-rate-limiter:
                    replenishRate: 10  # requests per second
                    burstCapacity: 20  # max burst
                    requestedTokens: 1
  ```
  Requires Redis for distributed rate limiting.

### **2. Circuit Breaker (Resilience4J)**
- **Why?** Handle downstream service failures gracefully
- **Implementation**:
  ```yaml
  filters:
    - name: CircuitBreaker
      args:
        name: taskService
        fallbackUri: forward:/fallback/task
        statusCodes: 500,502,503,504
  ```
  Add a fallback controller to return cached/default responses.

### **3. Distributed Tracing**
- **Why?** Debug microservice interactions
- **Implementation**:
  ```xml
  <!-- Add to pom.xml -->
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-sleuth</artifactId>
  </dependency>
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-sleuth-zipkin</artifactId>
  </dependency>
  ```

### **4. Metrics & Monitoring**
- **Why?** Track performance and health
- **Implementation**:
  ```yaml
  management:
    endpoints:
      web:
        exposure:
          include: health,metrics,gateway
  ```
  Integrate with Prometheus/Grafana.

### **5. Request/Response Transformation**
- **Why?** Modify payloads between services
- **Implementation**:
  ```yaml
  filters:
    - name: ModifyRequestBody
      args:
        inClass: String
        outClass: String
        rewriteFunction: "requestBody -> return modifiedBody"
  ```

### **6. Service Discovery Integration**
- **Why?** Dynamic routing if you scale services
- **Implementation**:
  ```yaml
  spring:
    cloud:
      discovery:
        client:
          simple:
            instances:
              task-service:
                - uri: http://task-service-1
                - uri: http://task-service-2
  ```


