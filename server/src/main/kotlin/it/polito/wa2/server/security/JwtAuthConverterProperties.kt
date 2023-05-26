package it.polito.wa2.server.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated

@Validated
@Configuration
class JwtAuthConverterProperties(
    @Value("\${jwt.auth.converter.resource-id}") val resourceId: String,
    @Value("\${jwt.auth.converter.principal-attribute}") val principalAttribute: String
)