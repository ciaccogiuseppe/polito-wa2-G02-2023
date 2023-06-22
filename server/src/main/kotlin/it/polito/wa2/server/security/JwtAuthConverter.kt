package it.polito.wa2.server.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.stereotype.Component
import java.util.stream.Collectors
import java.util.stream.Stream

@Component
class JwtAuthConverter(private val properties: JwtAuthConverterProperties) : Converter<Jwt, AbstractAuthenticationToken> {

    private final val jwtGrantedAuthoritiesConverter: JwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()

    override fun convert(source: Jwt): AbstractAuthenticationToken? {
        val authorities: Collection<GrantedAuthority> = Stream.concat(
            jwtGrantedAuthoritiesConverter.convert(source)?.stream()?:Stream.empty(),
            extractResourceRoles(source).stream()).collect(Collectors.toSet())
        return JwtAuthenticationToken(source, authorities, getPrincipalClaimName(source))
    }

    private fun getPrincipalClaimName(source: Jwt): String{
        val claimName: String = properties.principalAttribute
        return source.getClaim(claimName)
    }

    private fun extractResourceRoles(source: Jwt): Collection<GrantedAuthority> {
        val resourceAccess: Map<String, Any>? = source.getClaim("resource_access")
        val resource = resourceAccess?.get(properties.resourceId)
        if(resource !is Map<*,*>?) return setOf()
        val resourceRoles = resource?.get("roles")
        if(resourceRoles !is Collection<*>?) return setOf()
        if(resourceAccess == null || resource == null || resourceRoles == null)
            return setOf()
        return resourceRoles.stream().map{SimpleGrantedAuthority("ROLE_$it")}
            .collect(Collectors.toSet())
    }
}