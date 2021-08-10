package com.qianyi.modulecommon.reponse;

public enum ResponseCode {

    //系统级代码，上限100
    ERROR(-1, "error"),
    SUCCESS(0, "success"),
    AUTHENTICATION_NOPASS(1, "认证失败"),
    AUTHORIZATION_NOPASS(2, "授权失败"),
    RISK(6, "风险操作"),

    //业务级代码（101-999）
    PARAMETER_NOTNULLL(101, "参数必填"),
    CUSTOM(999, "自定义");

    private int code;
    private String msg;

    private ResponseCode(String msg) {
        this.code = 999;
        this.msg = msg;
    }

    private ResponseCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
