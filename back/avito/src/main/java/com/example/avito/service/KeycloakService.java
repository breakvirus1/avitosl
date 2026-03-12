package com.example.avito.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;

@Service
public class KeycloakService {

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.bearer-only}")
    private boolean bearerOnly;

    public Keycloak getKeycloakInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(authServerUrl)
                .realm("master")
                .username("keycloak")
                .password("keycloak")
                .clientId("admin-cli")
                .grantType("password")
                .build();
    }

    public String createUser(String email, String firstName, String password) {
        Keycloak keycloak = getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        List<UserRepresentation> existingUsers = usersResource.search(email);
        if (existingUsers != null && !existingUsers.isEmpty()) {
            throw new RuntimeException("Пользователь с email " + email + " уже существует в Keycloak");
        }

        UserRepresentation user = new UserRepresentation();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setEnabled(true);
        user.setEmailVerified(false);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        user.setCredentials(Collections.singletonList(credential));

Response response = usersResource.create(user);
        try {
            String location = response.getLocation().getPath();
            if (location != null && location.contains("/users/")) {
                return location.substring(location.lastIndexOf("/") + 1);
            }
            throw new RuntimeException("Не удалось получить ID пользователя из Keycloak");
        } finally {
            response.close();
        }
    }

    public void assignRoleToUser(String userId, String roleName) {
        Keycloak keycloak = getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        List<RoleRepresentation> realmRoles = realmResource.roles().list();
        RoleRepresentation role = realmRoles.stream()
                .filter(r -> r.getName().equals(roleName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Роль " + roleName + " не найдена"));

        usersResource.get(userId).roles().realmLevel()
                .add(Collections.singletonList(role));
    }

    public void deleteUser(String keycloakId) {
        try {
            Keycloak keycloak = getKeycloakInstance();
            RealmResource realmResource = keycloak.realm(realm);
            realmResource.users().delete(keycloakId);
        } catch (Exception e) {

            System.err.println("Ошибка удаления пользователя из Keycloak: " + e.getMessage());
        }
    }

    public UserRepresentation getUserByEmail(String email) {
        Keycloak keycloak = getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        List<UserRepresentation> users = usersResource.search(email);
        if (users == null || users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }

    public List<UserRepresentation> getAllUsers() {
        Keycloak keycloak = getKeycloakInstance();
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();
        
        
        return usersResource.list();
    }

    public boolean validateToken(String token) {
        try {
            Keycloak keycloak = getKeycloakInstance();
            RealmResource realmResource = keycloak.realm(realm);
            return token != null && !token.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}