package it.polito.wa2.server.keycloak

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.admin.client.resource.RealmResource
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated

@Validated
@Configuration
class KeycloakConfig(
    @Value("\${keycloak.auth-server-url}") private val serverUrl: String,
    @Value("\${keycloak.realm}") private val realm: String,
    @Value("\${keycloak.resource}") private val clientId: String,
    @Value("\${keycloak.credentials.username}") private val userName: String,
    @Value("\${keycloak.credentials.password}") private val password: String
) {

    val keycloak: Keycloak = KeycloakBuilder.builder()
        .serverUrl(serverUrl)
        .realm(realm)
        .grantType(OAuth2Constants.PASSWORD)
        .username(userName)
        .password(password)
        .clientId(clientId)
        .resteasyClient(ResteasyClientBuilder().connectionPoolSize(10).build())
        .build()

    fun getRealm(): RealmResource{
        return keycloak.realm(realm)
    }

}