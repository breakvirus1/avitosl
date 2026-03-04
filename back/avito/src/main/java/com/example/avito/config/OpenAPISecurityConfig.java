package com.example.avito.config;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.Scopes;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPISecurityConfig {

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    private static final String OAUTH_SCHEME_NAME = "keycloak_oauth";  

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(OAUTH_SCHEME_NAME, createOAuthScheme()))
                .addSecurityItem(new SecurityRequirement().addList(OAUTH_SCHEME_NAME))
                .info(new Info()
                        .title("avito API with Keycloak")
                        .description("REST API protected by Keycloak")
                        .version("1.0.0"));
    }

    private SecurityScheme createOAuthScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new io.swagger.v3.oas.models.security.OAuthFlows()
                        .implicit(createImplicitFlow()));
    }

    private io.swagger.v3.oas.models.security.OAuthFlow createImplicitFlow() {
        String authUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/auth";

        return new io.swagger.v3.oas.models.security.OAuthFlow()
                .authorizationUrl(authUrl)
                .scopes(new Scopes()
                        .addString("read_access", "Read data")
                        .addString("write_access", "Modify data")
                );
    }
}