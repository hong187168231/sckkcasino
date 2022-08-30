package com.qianyi.liveae.constants;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("project.ae")
@Component
@Data
public class AeConfig {

    private String cert;

    private String agentId;

    private String currency;

    private String apiUrl;

    private HORSEBOOK HORSEBOOK;

    private SV388 SV388;

    @Data
    public static class HORSEBOOK {

        private String minbet;

        private String maxbet;

        private String maxBetSumPerHorse;

        private String minorMinbet;

        private String minorMaxbet;

        private String minorMaxBetSumPerHorse;

    }

    @Data
    public static class SV388 {

        private String minbet;

        private String maxbet;

        private String mindraw;

        private String maxdraw;

        private String matchlimit;

    }
}
