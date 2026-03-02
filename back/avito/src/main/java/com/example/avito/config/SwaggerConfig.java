package com.example.avito.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public OpenAPI customOpenAPI() {
        String openIdConnectUrl = issuerUri + "/.well-known/openid-configuration";

        return new OpenAPI()
            .info(new Info()
                .title("Avito-like API")
                .version("0.0.1")
                .description("API с защитой через Keycloak"))
            .components(new Components()
                .addSecuritySchemes("keycloak-oauth2", new SecurityScheme()
                    .type(SecurityScheme.Type.OAUTH2)
                    .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow() 
                            .authorizationUrl(issuerUri + "/protocol/openid-connect/auth")
                            .tokenUrl(issuerUri + "/protocol/openid-connect/token")
                            .scopes(new Scopes()
                                .addString("openid", "openid")
                                .addString("profile", "profile")
                            )
                        )
                    )
                )
            )
            .addSecurityItem(new SecurityRequirement().addList("keycloak-oauth2"));
    }
}