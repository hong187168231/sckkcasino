package com.qianyi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CasinoWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(CasinoWebApplication.class, args);
    }

}
