package com.qianyi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class CasinoJobApplication {

    public static void main(String[] args) {
        SpringApplication.run(CasinoJobApplication.class, args);
    }

}
