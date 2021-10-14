package com.qianyi.casinoadmin.install;

import com.qianyi.casinocore.model.PlatformConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "platform")
@PropertySource("classpath:install/platformConfig.properties")
@Data
public class PlatformConfigBean {
    private BigDecimal clearCodeNum;
    private BigDecimal betRate;
    private BigDecimal chargeMinMoney;
    private BigDecimal chargeMaxMoney;
    private BigDecimal chargeServiceMoney;
    private BigDecimal chargeRate;
    private BigDecimal withdrawMinMoney;
    private BigDecimal withdrawMaxMoney;
    private BigDecimal withdrawServiceMoney;
    private BigDecimal withdrawRate;
    private Integer ipMaxNum;
    private BigDecimal wmMoney;
    private BigDecimal wmMoneyWarning;
    private BigDecimal firstCommission;
    private BigDecimal secondCommission;
    private BigDecimal thirdCommission;
    private String domainNameConfiguration;
    private Integer registerSwitch;
}