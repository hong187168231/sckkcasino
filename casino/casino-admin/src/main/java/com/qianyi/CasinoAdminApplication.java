package com.qianyi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CasinoAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(CasinoAdminApplication.class, args);
    }

}
