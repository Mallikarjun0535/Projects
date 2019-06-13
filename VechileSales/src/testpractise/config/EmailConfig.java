package com.dizzion.portal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.mail.general")
    public JavaMailSender generalMailSender() {
        return javaMailSender();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.mail.emergency")
    public JavaMailSender emergencyMailSender() {
        return javaMailSender();
    }

    private JavaMailSender javaMailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        javaMailSender.setJavaMailProperties(properties);
        return javaMailSender;
    }
}
