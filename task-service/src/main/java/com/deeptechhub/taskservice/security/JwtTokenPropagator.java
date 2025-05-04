package com.deeptechhub.taskservice.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
public class JwtTokenPropagator implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        var requestAttributes = RequestContextHolder.getRequestAttributes();

        if(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            String authHeader = request.getHeader(AUTHORIZATION);

            if(authHeader != null && authHeader.startsWith("Bearer ")) {
                template.header(AUTHORIZATION, authHeader);
            }
        }
    }
}
