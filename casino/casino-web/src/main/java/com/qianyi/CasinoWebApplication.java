package com.qianyi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@Slf4j
public class CasinoWebApplication {

    public static void main(String[] args) {
        log.info("dev环境部署成功");
        SpringApplication.run(CasinoWebApplication.class, args);
    }

}
