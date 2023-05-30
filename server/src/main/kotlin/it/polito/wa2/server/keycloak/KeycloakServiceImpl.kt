package it.polito.wa2.server.keycloak

import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.stereotype.Service
import java.util.*
import javax.ws.rs.core.Response

@Service
class KeycloakServiceImpl(private val keycloakConfig: KeycloakConfig): KeycloakService {
    companion object {
        const val CLIENT = "app_client"
        const val EXPERT = "app_expert"
        const val MANAGER = "app_manager"
    }
    override fun addClient(userDTO: UserDTO) {
        val user = createUser(userDTO)
        user.realmRoles = Collections.singletonList(CLIENT)
        addUser(user)
    }

    override fun addExpert(userDTO: UserDTO) {
        val user = createUser(userDTO)
        user.realmRoles = Collections.singletonList(EXPERT)
        addUser(user)
    }

    private fun createUser(userDTO: UserDTO): UserRepresentation{
        val credentials: CredentialRepresentation =
            Credentials.createPasswordCredentials(userDTO.password)
        val user = UserRepresentation()
        user.username = userDTO.userName
        user.firstName = userDTO.firstName
        user.lastName = userDTO.lastName
        user.email = userDTO.email
        user.credentials = Collections.singletonList(credentials)
        user.isEnabled = true
        return user
    }

    private fun addUser(user: UserRepresentation){
        keycloakConfig.getRealm().users().create(user)
    }

}