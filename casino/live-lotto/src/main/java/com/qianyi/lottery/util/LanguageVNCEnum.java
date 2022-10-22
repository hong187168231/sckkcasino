package com.qianyi.lottery.util;

import com.qianyi.modulecommon.config.LocaleConfig;
import org.springframework.util.ObjectUtils;

public enum LanguageVNCEnum {

    en_US("en", "英文", LocaleConfig.en_US.toString()),
    zh_CN("zh", "中文", LocaleConfig.zh_CN.toString()),
    JP("jp", "日本语"),
    TH("th", "泰语",LocaleConfig.th_TH.toString()),
    VN("vn", "越南文"),
    KH("km", "高棉语", LocaleConfig.km_KH.toString()),
    ;
    private String code;
    private String name;
    private String systemCode;

    LanguageVNCEnum(String code, String name, String systemCode) {
        this.code = code;
        this.name = name;
        this.systemCode = systemCode;
    }

    LanguageVNCEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getSystemCode() {
        return systemCode;
    }

    public static String getLanguageCode(String language) {
        for (LanguageVNCEnum languageEnum : values()) {
            if (!ObjectUtils.isEmpty(language) && language.equals(languageEnum.getSystemCode())) {
                return languageEnum.getCode();
            }
        }
        return en_US.code;
    }

    public static void main(String[] args) {
        String km_kh = getLanguageCode("km_KH");
        System.out.println(km_kh);
    }
}
