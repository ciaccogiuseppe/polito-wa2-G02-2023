package it.polito.wa2.server.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/test")
class TestController {
    @GetMapping("/anonymous")
    fun getAnonymous(): ResponseEntity<String>{
        return ResponseEntity.ok("Hello Anonymous")
    }

    @GetMapping("/manager")
    fun getManager(principal: Principal): ResponseEntity<String>{
        val token: JwtAuthenticationToken = principal as JwtAuthenticationToken
        val userName = token.tokenAttributes["name"]
        val userEmail = token.tokenAttributes["email"]
        return ResponseEntity.ok("Hello Manager\nuser name: "+ userName + "user email: "+ userEmail)
    }

    @GetMapping("/client")
    fun getClient(principal: Principal): ResponseEntity<String>{
        val token: JwtAuthenticationToken = principal as JwtAuthenticationToken
        val userName = token.tokenAttributes["name"]
        val userEmail = token.tokenAttributes["email"]
        return ResponseEntity.ok("Hello Client\nuser name: "+ userName + "user email: "+ userEmail)
    }

    @GetMapping("/expert")
    fun getExpert(principal: Principal): ResponseEntity<String>{
        val token: JwtAuthenticationToken = principal as JwtAuthenticationToken
        val userName = token.tokenAttributes["name"]
        val userEmail = token.tokenAttributes["email"]
        return ResponseEntity.ok("Hello Expert\nuser name: "+ userName + "user email: "+ userEmail)
    }
}