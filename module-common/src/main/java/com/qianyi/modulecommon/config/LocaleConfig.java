package com.qianyi.modulecommon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * 配置国际化语言
 */
@Configuration
public class LocaleConfig {

    @Bean(name = "messageSource")
    public ResourceBundleMessageSource getMessageSource() throws Exception {
        ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
        resourceBundleMessageSource.setDefaultEncoding("GBK");
        resourceBundleMessageSource.setBasenames("static/i18n/messages");
        return resourceBundleMessageSource;
    }
}
