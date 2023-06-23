package it.polito.wa2.server.keycloak

import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.representations.idm.RoleRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.validation.annotation.Validated
import java.util.*
import java.util.stream.Collectors

@Validated
@Configuration
class KeycloakConfig(
    @Value("\${keycloak.auth-server-url}") private val serverUrl: String,
    @Value("\${keycloak.realm}") private val realm: String,
    @Value("\${keycloak.admin.realm}") private val adminRealm: String,
    @Value("\${keycloak.admin.client}") private val clientId: String,
    @Value("\${keycloak.credentials.username}") private val adminUsername: String,
    @Value("\${keycloak.credentials.password}") private val adminPassword: String,
    @Value("\${mail.sender.host}") private val host: String,
    @Value("\${mail.sender.port}") private val port: Int,
    @Value("\${mail.sender.username}") private val username: String,
    @Value("\${mail.sender.password}") private val password: String,
    @Value("\${mail.sender.protocol}") private val protocol: String,
    @Value("\${mail.sender.debug}") private val debug: String,
    @Value("\${mail.sender.auth}") private val auth: String,
    @Value("\${mail.sender.starttls.enable}") private val starttls: String,
    @Value("\${server.url}") private val url: String,


    ) {
    private val keycloak: Keycloak = KeycloakBuilder.builder()
        .serverUrl(serverUrl)
        .realm(adminRealm)
        .grantType(OAuth2Constants.PASSWORD)
        .clientId(clientId)
        .username(adminUsername)
        .password(adminPassword)
        .build()


    private fun configureJavaMailProperties(properties: Properties) {
        properties["mail.transport.protocol"] = protocol
        properties["mail.smtp.auth"] = auth
        properties["mail.smtp.starttls.enable"] = starttls
        properties["mail.debug"] = debug
    }

    @Bean
    fun javaMailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = host
        mailSender.port = port
        mailSender.username = username
        mailSender.password = password
        configureJavaMailProperties(mailSender.javaMailProperties)
        return mailSender
    }


    fun getRealm(): RealmResource {
        return keycloak.realm(realm)
    }

    fun assignRoles(username: String, roles: List<String>) {
        val roleList: List<RoleRepresentation> = rolesToRealmRoleRepresentation(roles)
        val id = getRealm().users().search(username).first().id
        getRealm().users().get(id).roles().realmLevel().add(roleList)
    }

    fun resetPassword(email: String, uuid: UUID) {
        val id = getRealm().users().search(email).first().id
        getRealm().users().get(id)


        val newUrl = "$url/resetpasswordapply/$uuid"


        val message = SimpleMailMessage()
        message.subject = "[Ticketing service] - Password forgot"
        message.text =
            "We recently received a request to reset the password for your account associated with Ticketing Service.\n\nTo initiate the password reset process click here: $newUrl"
        message.setTo(email)

        javaMailSender().send(message)
    }

    fun applyResetPassword(email: String, password: String) {
        val id = getRealm().users().search(email).first().id
        val newCredentials = Credentials.createPasswordCredentials(password)
        newCredentials.isTemporary = false

        getRealm().users().get(id).resetPassword(newCredentials)
    }

    private fun rolesToRealmRoleRepresentation(roles: List<String>): List<RoleRepresentation> {
        val existingRoles: List<RoleRepresentation> = getRealm().roles().list()
        val serverRoles: List<String> = existingRoles.stream()
            .map(RoleRepresentation::getName)
            .collect(Collectors.toList())
        val resultRoles: MutableList<RoleRepresentation> = mutableListOf()
        for (role in roles) {
            val index = serverRoles.indexOf(role)
            if (index != -1)
                resultRoles.add(existingRoles[index])
        }
        return resultRoles
    }

}