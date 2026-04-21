package com.avitosl.eureka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = EurekaServerApplication.class)
class EurekaServerApplicationTest {

    @Test
    void contextLoads() {
        // Verify that the Eureka Server Spring application context loads successfully
        assertTrue(true);
    }
}
