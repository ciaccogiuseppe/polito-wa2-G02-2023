package it.polito.wa2.server.security

// import lombok.RequiredArgsConstructor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

// @RequiredArgsConstructor
@Configuration
@EnableWebSecurity
class WebSecurityConfig(val jwtAuthConverter: JwtAuthConverter ) {
    companion object {
        const val MANAGER = "manager"
        const val CLIENT = "client"
        const val EXPERT = "expert"
    }

    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain{
        httpSecurity.authorizeHttpRequests()
            .requestMatchers(HttpMethod.GET, "/test/anonymous", "/test/anonymous/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/test/manager", "/test/manager/**").hasRole(MANAGER)
            .requestMatchers(HttpMethod.GET, "/test/expert", "/test/expert/**").hasAnyRole(MANAGER, EXPERT)
            .requestMatchers(HttpMethod.GET, "/test/client", "/test/client/**").hasAnyRole(MANAGER, CLIENT)
            .requestMatchers(HttpMethod.GET, "/API/ticketing/history/**").hasRole(MANAGER)
            .requestMatchers(HttpMethod.GET, "/API/products/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/login").permitAll()
            .anyRequest().authenticated()
        httpSecurity.oauth2ResourceServer()
            .jwt()
            .jwtAuthenticationConverter(jwtAuthConverter)
        httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        httpSecurity.csrf().disable()
        return httpSecurity.build()
    }
}