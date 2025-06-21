package com.chat_room_app.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * contains configuration of JavaMailSender Bean
 */
@Lazy
@Configuration
public class EmailConfiguration {

    @Value("${support.email}")
    private String emailUsername;

    @Value("${email.secret.key}")
    private String emailSecretKey;

    /*
     * makes a JavaMailSenderImpl object with the desired attributes
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername(emailUsername);
        mailSender.setPassword(emailSecretKey);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
}
