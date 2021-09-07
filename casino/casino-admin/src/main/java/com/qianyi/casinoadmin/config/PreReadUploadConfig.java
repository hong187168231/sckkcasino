package com.qianyi.casinoadmin.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix="upload",ignoreInvalidFields = true)
@Configuration
@Setter
@Getter
public class PreReadUploadConfig {
    private Map<UriEnum, String> uriMap;

    public enum UriEnum {
        localtion, windows, linux,
    }
    public String getBasePath() {
        String location ;
        String os = System.getProperty("os.name");
        if(os.toLowerCase().startsWith("win")) {
            location = this.uriMap.get(UriEnum.windows);
        } else if (os.toLowerCase().startsWith("lin")){
            location = this.uriMap.get(UriEnum.linux);
        } else {
            location = this.uriMap.get(UriEnum.localtion);
        }
        return location;
    }
}