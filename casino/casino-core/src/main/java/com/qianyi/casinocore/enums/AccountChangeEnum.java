package com.qianyi.casinocore.enums;

/**
 * 账变类型配置
 */
public enum AccountChangeEnum {

    WASH_CODE(0, "XM","洗码领取"),
    TOPUP_CODE(1, "CZ","充值"),
    ADD_CODE(2, "RGZJ","人工增加"),
    WITHDRAW_CODE(3, "TX","提现"),
    WITHDRAWDEFEATED_CODE(4, "TXSB","提现失败"),
    SUB_CODE(5, "RGKJ","人工扣减"),
    WITHDRAW_APPLY(6, "QY","提现申请"),
    WM_IN(7, "WMIN","转入WM"),
    WM_OUT(8, "WMOUT","WM转出");

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