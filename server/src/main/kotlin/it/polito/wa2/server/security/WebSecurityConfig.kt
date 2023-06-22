package it.polito.wa2.server.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class WebSecurityConfig(val jwtAuthConverter: JwtAuthConverter ) {
    companion object {
        const val MANAGER = "manager"
        const val CLIENT = "client"
        const val EXPERT = "expert"
        const val VENDOR = "vendor"
    }

    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain{
        httpSecurity.authorizeHttpRequests()
            .requestMatchers(HttpMethod.GET, "").permitAll()
            .requestMatchers(HttpMethod.GET, "/*").permitAll()
            .requestMatchers(HttpMethod.GET, "/static/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/API/manager/**").hasRole(MANAGER)
            .requestMatchers(HttpMethod.POST, "/API/manager/**").hasRole(MANAGER)
            .requestMatchers(HttpMethod.PUT, "/API/manager/**").hasRole(MANAGER)
            .requestMatchers(HttpMethod.GET, "/API/client/**").hasRole(CLIENT)
            .requestMatchers(HttpMethod.POST, "/API/client/**").hasRole(CLIENT)
            .requestMatchers(HttpMethod.PUT, "/API/client/**").hasRole(CLIENT)
            .requestMatchers(HttpMethod.GET, "/API/expert/**").hasRole(EXPERT)
            .requestMatchers(HttpMethod.PUT, "/API/expert/**").hasRole(EXPERT)
            .requestMatchers(HttpMethod.POST, "/API/vendor/**").hasRole(VENDOR)
            .requestMatchers(HttpMethod.PUT, "/API/vendor/**").hasRole(VENDOR)
            .requestMatchers(HttpMethod.GET, "/API/authenticated/**").hasAnyRole(CLIENT, EXPERT, MANAGER, VENDOR)
            .requestMatchers(HttpMethod.GET, "/API/public/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/API/public/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/API/login/**").permitAll()
            .requestMatchers(HttpMethod.POST,"/API/refreshtoken/**").permitAll()
            .requestMatchers(HttpMethod.POST,"/API/signup/**").permitAll()
            .requestMatchers(HttpMethod.GET,"/API/resetPassword/**").permitAll()
            .requestMatchers(HttpMethod.PUT,"/API/resetPassword/**").permitAll()
            .requestMatchers(HttpMethod.POST,"/API/createExpert").hasRole(MANAGER)
            .requestMatchers(HttpMethod.POST,"/API/createVendor").hasRole(MANAGER)
            .requestMatchers(HttpMethod.GET, "/API/attachment/**").hasAnyRole(CLIENT, EXPERT)
            .requestMatchers(HttpMethod.GET, "/API/chat/**").hasAnyRole(CLIENT, EXPERT)
            .requestMatchers(HttpMethod.POST, "/API/chat/**").hasAnyRole(CLIENT, EXPERT)
            .anyRequest().authenticated()
        httpSecurity.oauth2ResourceServer()
            .jwt()
            .jwtAuthenticationConverter(jwtAuthConverter)
        httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        httpSecurity.csrf().disable()
        httpSecurity.cors()
        return httpSecurity.build()
    }
}