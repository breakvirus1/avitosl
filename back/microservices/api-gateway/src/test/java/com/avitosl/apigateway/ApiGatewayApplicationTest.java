package com.avitosl.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ApiGatewayApplication.class)
class ApiGatewayApplicationTest {

    @Test
    void contextLoads() {
        // Verify that the Spring application context loads successfully for API Gateway
        assertThat(true).isTrue();
    }
}
