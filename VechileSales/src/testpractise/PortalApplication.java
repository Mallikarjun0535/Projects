package com.dizzion.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class PortalApplication {

    public static void main(String[] args) {
        setupJvmProperties();
        SpringApplication.run(PortalApplication.class, args);
    }

    private static void setupJvmProperties() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        System.setProperty("jsse.enableSNIExtension", "false");
    }
}
