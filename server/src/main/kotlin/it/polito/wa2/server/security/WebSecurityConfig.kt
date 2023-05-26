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
            .requestMatchers(HttpMethod.GET, "/API/client/ticketing/**").hasRole(CLIENT)
            .requestMatchers(HttpMethod.GET, "/API/expert/ticketing/**").hasRole(EXPERT)
            .requestMatchers(HttpMethod.GET, "/API/manager/ticketing/**").hasRole(MANAGER)
            .requestMatchers(HttpMethod.GET, "/API/client/ticketing/filter**").hasRole(CLIENT)
            .requestMatchers(HttpMethod.GET, "/API/expert/ticketing/filter**").hasRole(EXPERT)
            .requestMatchers(HttpMethod.GET, "/API/manager/ticketing/filter**").hasRole(MANAGER)
            .requestMatchers(HttpMethod.POST, "/API/client/ticketing/").hasRole(CLIENT)
            .requestMatchers(HttpMethod.PUT, "/API/manager/ticketing/assign").hasRole(MANAGER)
            .requestMatchers(HttpMethod.PUT, "/API/manager/ticketing/update").hasRole(MANAGER)
            .requestMatchers(HttpMethod.PUT, "/API/client/ticketing/update").hasRole(CLIENT)
            .requestMatchers(HttpMethod.PUT, "/API/expert/ticketing/update").hasRole(EXPERT)
            .requestMatchers(HttpMethod.GET, "/API/profiles/**").hasRole(MANAGER)
            .requestMatchers(HttpMethod.POST, "/API/profiles").permitAll()
            .requestMatchers(HttpMethod.PUT, "/API/manager/profiles/**").hasRole(MANAGER)
            .requestMatchers(HttpMethod.PUT, "/API/client/profiles/**").hasRole(CLIENT)
            .requestMatchers(HttpMethod.PUT, "/API/expert/profiles/**").hasRole(EXPERT)
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