package com.qianyi.casinoadmin.install.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "rebate")
@PropertySource("classpath:install/proxyRebateConfig.properties")
@Data
public class ProxyRebateConfigFile {
    private Integer firstMoney;
    private BigDecimal firstProfit;
    private Integer secondMoney;
    private BigDecimal secondProfit;
    private Integer thirdMoney;
    private BigDecimal thirdProfit;
    private Integer fourMoney;
    private BigDecimal fourProfit;
    private Integer fiveMoney;
    private BigDecimal fiveProfit;
}
