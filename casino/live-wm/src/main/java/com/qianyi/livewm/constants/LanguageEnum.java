package com.qianyi.livewm.constants;

import org.springframework.util.ObjectUtils;

public enum LanguageEnum {

    zh_CN(0, "简体中文", "zh_CN"),
    en_US(1, "英文", "en_US"),
    TH(2, "泰文","th_TH"),
    VI(3, "越文"),
    JA(4, "日文"),
    KO(5, "韩文"),
    IN(6, "印度文"),
    MY(7, "马来西亚文","as_MY"),
    in_ID(8, "印尼文"),
    zh_HK(9, "繁体中文"),
    ca_ES(10, "西文"),
    ;
    private Integer code;
    private String name;
    private String systemCode;

    LanguageEnum(Integer code, String name, String systemCode) {
        this.code = code;
        this.name = name;
        this.systemCode = systemCode;
    }

    LanguageEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getSystemCode() {
        return systemCode;
    }

    public static Integer getLanguageCode(String lanuage) {
        for (LanguageEnum languageEnum : LanguageEnum.values()) {
            if (!ObjectUtils.isEmpty(lanuage) && lanuage.equals(languageEnum.getSystemCode())) {
                return languageEnum.getCode();
            }
        }
        return en_US.code;
    }
}
