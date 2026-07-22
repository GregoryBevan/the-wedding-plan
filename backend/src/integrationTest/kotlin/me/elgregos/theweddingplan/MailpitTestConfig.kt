package me.elgregos.theweddingplan

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

@TestConfiguration
class MailpitTestConfig {

    @Bean
    fun javaMailSender(
        @Value("\${spring.mail.host}") host: String,
        @Value("\${spring.mail.port}") port: Int,
    ): JavaMailSender = JavaMailSenderImpl().apply {
        this.host = host
        this.port = port
    }
}

