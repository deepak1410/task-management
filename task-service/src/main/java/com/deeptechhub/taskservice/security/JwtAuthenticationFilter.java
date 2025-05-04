package com.deeptechhub.taskservice.security;

import com.deeptechhub.common.dto.UserDto;
import com.deeptechhub.taskservice.client.IdentityServiceClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * A filter to authenticate all incoming requests by validating JWT tokens and setting up the security context.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsMapper userDetailsMapper;
    private final IdentityServiceClient identityServiceClient;
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        log.debug("authHeader has been retrieved as {}", authHeader);

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwtToken = authHeader.substring(7);
        final String username = jwtService.extractUsername(jwtToken);
        log.debug("Username has been extracted as {}", username);

        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            //Get the user details from identity-service
            UserDto userDto = identityServiceClient.getUserByUsername(username);
            UserDetails userDetails = userDetailsMapper.toUserDetails(userDto);

            boolean isTokenValid = jwtService.isTokenValid(jwtToken, userDto);
            log.debug("The token is valid");

            if(isTokenValid) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, jwtToken, userDetails.getAuthorities());

                authToken.setDetails(userDto); //Set userDto in the securityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
