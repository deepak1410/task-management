package com.deeptechhub.apigateway.config;

import com.deeptechhub.common.logging.CorrelationIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures request correlation ID for logging.
 * <p>
 * Registers {@link CorrelationIdFilter} for all URLs to add a unique correlation ID to each request.
 */
@Configuration
public class LoggingConfig {

    /**
     * Registers the correlation ID filter.
     * @return the filter registration bean
     */
    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilter() {
        FilterRegistrationBean<CorrelationIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CorrelationIdFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1); // High priority
        return registration;
    }
}
