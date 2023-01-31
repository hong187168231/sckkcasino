package com.qianyi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
@MapperScan({"com.qianyi.casinoadmin.repository"})
public class CasinoAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(CasinoAdminApplication.class, args);
    }

}
