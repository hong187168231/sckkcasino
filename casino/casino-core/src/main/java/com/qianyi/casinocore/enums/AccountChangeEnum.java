package com.qianyi.casinocore.enums;

/**
 * 账变类型配置
 */
public enum AccountChangeEnum {

    WASH_CODE(0, "XM","洗码领取");

    private Integer type;

    private String code;

    private String name;

    AccountChangeEnum(Integer type, String code, String name) {
        this.type = type;
        this.code = code;
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}
