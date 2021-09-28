package com.qianyi.casinocore;

public class CoreConstants {

    //系统配置组别表,分组
    public interface SysConfigGroup {
        int GROUP_FINANCE = 1; //财务相关
        int GROUP_IP = 2; //IP配置相关
        int GROUP_BET = 3; //打码配置相关
    }

    public interface SysConfigName {
        String CAPTCHA_RATE = "captchaRate"; //打码倍率名称
        String CAPTCHA_MIN = "captchaMin";   //最低金额重置打码量
    }
}
