package com.qianyi.lottery.constants;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("project.lotto")
@Component
@Data
public class LottoConfig {

    private String enterpriseId;

    private String apiUrl;

    private String currency;
}
