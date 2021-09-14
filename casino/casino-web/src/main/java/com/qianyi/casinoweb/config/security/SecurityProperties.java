package com.qianyi.casinoweb.config.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "security", ignoreInvalidFields = false)
public class SecurityProperties {

    private final Auth auth = new Auth();

    @Data
    public static class Auth{
        private List<String> ignoreUrls;
    }
}
