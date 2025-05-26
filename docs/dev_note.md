## Implementing Role Based Access Control (RBAC)
* Created an enum Role that contains different roles. Spring expects roles to be prefixed with ROLE_. This method does this via getAuthority().
* In JwtAuthenticationFilter set authority via UserDetailsMapper.
* In JwtAuthenticationFilter create UsernamePasswordAuthenticationToken with this authority and set that in the SecurityContext.
* Add @EnableMethodSecurity in the main class TaskServiceApplication to enable role-based access.
* In UserDetailsServiceImpl set authority.
* Add an Exception handler for AccessDenied in GlobalExceptionHandler.
* In the RestController class add @PreAuthorize with hasAnyRole on the endpoints
