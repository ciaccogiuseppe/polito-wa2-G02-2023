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
    @Value("\${keycloak.admin.realm}") private val admin_realm: String,
    @Value("\${keycloak.admin.client}") private val clientId: String,
    @Value("\${keycloak.credentials.username}") private val adminUsername:String,
    @Value("\${keycloak.credentials.password}") private val adminPassword:String,
    @Value("\${keycloak.credentials.secret}") private val clientSecret: String
) {
    val keycloak: Keycloak = KeycloakBuilder.builder()
        .serverUrl(serverUrl)
        .realm(admin_realm)
        .grantType(OAuth2Constants.PASSWORD)
        .clientId(clientId)
        .username(adminUsername)
        .password(adminPassword)
        .build()

    fun getRealm(): RealmResource{
        return keycloak.realm(realm)
    }

}