package com.avitosl.purchaseservice.feign;

import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                try {
                    RequestAttributes requestAttributes = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
                    if (requestAttributes instanceof ServletRequestAttributes) {
                        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                        String token = (String) request.getAttribute("jwtToken");
                        if (token != null) {
                            template.header("Authorization", "Bearer " + token);
                        }
                        // Mark internal service calls for wallet operations
                        template.header("X-Internal-Service", "purchase-service");
                    }
                } catch (Exception e) {
                    // No request bound, ignore
                }
            }
        };
    }
}