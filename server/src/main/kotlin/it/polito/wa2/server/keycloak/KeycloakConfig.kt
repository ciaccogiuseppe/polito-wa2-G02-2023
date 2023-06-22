package it.polito.wa2.server.keycloak

import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.representations.idm.RoleRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated
import java.util.stream.Collectors

@Validated
@Configuration
class KeycloakConfig(
    @Value("\${keycloak.auth-server-url}") private val serverUrl: String,
    @Value("\${keycloak.realm}") private val realm: String,
    @Value("\${keycloak.admin.realm}") private val adminRealm: String,
    @Value("\${keycloak.admin.client}") private val clientId: String,
    @Value("\${keycloak.credentials.username}") private val adminUsername:String,
    @Value("\${keycloak.credentials.password}") private val adminPassword:String
) {
    private val keycloak: Keycloak = KeycloakBuilder.builder()
        .serverUrl(serverUrl)
        .realm(adminRealm)
        .grantType(OAuth2Constants.PASSWORD)
        .clientId(clientId)
        .username(adminUsername)
        .password(adminPassword)
        .build()

    fun getRealm(): RealmResource{
        return keycloak.realm(realm)
    }

    fun assignRoles(username: String, roles: List<String>){
        val roleList: List<RoleRepresentation> = rolesToRealmRoleRepresentation(roles)
        val id = getRealm().users().search(username).first().id
        getRealm().users().get(id).roles().realmLevel().add(roleList)
    }

    private fun rolesToRealmRoleRepresentation(roles: List<String>): List<RoleRepresentation>{
        val existingRoles: List<RoleRepresentation> = getRealm().roles().list()
        val serverRoles: List<String> = existingRoles.stream()
            .map(RoleRepresentation::getName)
            .collect(Collectors.toList())
        val resultRoles: MutableList<RoleRepresentation> = mutableListOf()
        for(role in roles){
            val index = serverRoles.indexOf(role)
            if(index != -1)
                resultRoles.add(existingRoles[index])
        }
        return resultRoles
    }

}