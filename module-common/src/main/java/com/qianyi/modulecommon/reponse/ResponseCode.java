package com.qianyi.modulecommon.reponse;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResponseCode {

    //系统级代码，上限100
    FAIL(-2,"fail"),
    ERROR(-1, "error"),
    SUCCESS(0, "success"),
    AUTHENTICATION_NOPASS(1, "认证失败"),
    AUTHORIZATION_NOPASS(2, "授权失败"),
    GOOGLEAUTH_NOPASS(3, "谷歌身份验证码错误"),
    RISK(6, "风险操作"),

    //业务级代码（101-999）
    PARAMETER_NOTNULLL(101, "参数必填"),
    CUSTOM(999, "自定义"),
    FINDUSER_PAGE(100, "用户信息错误");

    private int code;
    private String msg;
}
