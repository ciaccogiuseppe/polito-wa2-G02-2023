package it.polito.wa2.server.keycloak

import org.keycloak.representations.idm.CredentialRepresentation

class Credentials {
    companion object{
        fun createPasswordCredentials(password: String): CredentialRepresentation{
            val passwordCredentials = CredentialRepresentation()
            passwordCredentials.isTemporary = false
            passwordCredentials.type = CredentialRepresentation.PASSWORD
            passwordCredentials.value = password
            return passwordCredentials
        }
    }
}