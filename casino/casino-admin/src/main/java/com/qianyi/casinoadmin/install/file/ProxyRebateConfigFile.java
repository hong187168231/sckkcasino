package com.qianyi.casinoadmin.install.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "rebate")
@PropertySource("classpath:install/proxyRebateConfig.properties")
@Data
public class ProxyRebateConfigFile {
    private Integer firstMoney;
    private Double firstProfit;
    private Integer secondMoney;
    private Double secondProfit;
    private Integer thirdMoney;
    private Double thirdProfit;
    private Integer fourMoney;
    private Double fourProfit;
    private Integer fiveMoney;
    private Double fiveProfit;
}
