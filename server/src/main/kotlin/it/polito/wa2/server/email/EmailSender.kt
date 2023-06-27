package it.polito.wa2.server.email

import it.polito.wa2.server.UnprocessableMailException
import it.polito.wa2.server.UnprocessableProfileException
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.MailMessage
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.validation.annotation.Validated
import java.util.*

@Validated
@Configuration
class EmailSender (
    @Value("\${mail.sender.host}") private val host: String,
    @Value("\${mail.sender.port}") private val port: Int,
    @Value("\${mail.sender.username}") private val username: String,
    @Value("\${mail.sender.password}") private val password: String,
    @Value("\${mail.sender.protocol}") private val protocol: String,
    @Value("\${mail.sender.debug}") private val debug: String,
    @Value("\${mail.sender.auth}") private val auth: String,
    @Value("\${mail.sender.starttls.enable}") private val starttls: String,
) {
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

    fun send(message: SimpleMailMessage){
        try{
            javaMailSender().send(message)
        }
        catch (e: Exception){
            throw UnprocessableMailException("Error sending email, address could be wrong")
        }
    }
}