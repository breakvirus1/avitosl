package com.example.avito.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${server.port:8081}")
    private String serverPort;

    @Value("${spring.application.name:Avito Backend API}")
    private String appName;

    private static final String OAUTH_SCHEME_NAME = "keycloak_oauth";

    @Bean
    public OpenAPI api() {
        
        return new OpenAPI()
                .info(new Info()
                        .title(appName)
                        .description("REST API для электронной доски объявлений. Авторизация через Keycloak.")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(OAUTH_SCHEME_NAME))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(OAUTH_SCHEME_NAME, createOAuthScheme()));
    }

    private SecurityScheme createOAuthScheme() {
        String authUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/auth";
        String tokenUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        
        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("OAuth 2.0 authentication using Keycloak with Authorization Code flow")
                .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                                .authorizationUrl(authUrl)
                                .tokenUrl(tokenUrl)
                                .scopes(new Scopes()
                                        .addString("openid", "OpenID Connect scope")
                                        .addString("profile", "Access user profile")
                                        .addString("email", "Access user email")
                                        .addString("roles", "Access user roles"))));
    }
}
