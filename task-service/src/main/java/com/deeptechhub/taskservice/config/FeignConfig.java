package com.deeptechhub.taskservice.config;

import com.deeptechhub.common.CommonApplicationConstants;
import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    public RequestInterceptor correlationInterceptor() {
        return template -> {
            String correlationId = MDC.get(CommonApplicationConstants.CORRELATION_ID_MDC_KEY);
            if(correlationId != null) {
                template.header(CommonApplicationConstants.CORRELATION_ID_HEADER, correlationId);
            }
        };
    }

}
