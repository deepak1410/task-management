
### The Logout flow

```mermaid
sequenceDiagram
    participant Client
    participant IdentityService
    participant Redis
    
    Client->>IdentityService: POST /logout (with JWT)
    IdentityService->>Redis: SET token "blacklisted" EX 15m
    IdentityService->>Client: 204 No Content
    Client->>IdentityService: Subsequent request with blacklisted token
    IdentityService->>Redis: CHECK token
    Redis-->>IdentityService: "blacklisted"
    IdentityService->>Client: 401 Unauthorized
```