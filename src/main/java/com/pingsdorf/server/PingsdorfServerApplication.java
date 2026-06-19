package com.pingsdorf.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PingsdorfServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PingsdorfServerApplication.class, args);
    }
}
