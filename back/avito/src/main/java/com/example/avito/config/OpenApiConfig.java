package com.example.avito.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${spring.application.name:Avito Backend API}")
    private String appName;

    private static final String OAUTH_SCHEME_NAME = "keycloak";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(appName)
                        .description("REST API для электронной доски объявлений. Авторизация через Keycloak. Доступ к эндпоинтам контролируется ролями из JWT токена.")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(OAUTH_SCHEME_NAME))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(OAUTH_SCHEME_NAME, createOAuthScheme()));
    }

    private SecurityScheme createOAuthScheme() {
        String authorizationUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/auth";
        String tokenUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("Keycloak OAuth2. Роли извлекаются из JWT токена (resource_access.avito-backend.roles)")
                .flows(new io.swagger.v3.oas.models.security.OAuthFlows()
                        .authorizationCode(new io.swagger.v3.oas.models.security.OAuthFlow()
                                .authorizationUrl(authorizationUrl)
                                .tokenUrl(tokenUrl)));
    }
}
