package com.qianyi.modulecommon;

/**
 * 常量
 */
public class Constants {

    //是/否，开/关, 真/假
    public final static Integer open = 1;
    public final static Integer close = 0;
    public final static Integer yes = 1;
    public final static Integer no = 1;

    //订单状态
    public final static Integer order_wait = 1;//等待确认
    public final static Integer order_success = 2;//确认成功
    public final static Integer order_fail = 3;//确认失败

    /** 单个用户银行卡最大绑定数量 */
    public final static Integer BANK_USER_BOUND_MAX = 6;

    
    //会员状态
    public final static Integer USER_NORMAL = 1; //正常
    public final static Integer USER_LOCK_ACCOUNT = 2; //冻结账户
    public final static Integer USER_LOCK_BALANCE = 3; //冻结资金

    public final static Integer USER_LANGUAGE_CH = 1; //默认中文


    public final static Integer MIN_PASSWORD_NUM = 6; //密码最小位数
    public final static Integer MAX_PASSWORD_NUM = 12; //密码最大位数

    public final static Integer BANK_CLOSE = 1; //银行卡禁用
    public final static Integer BANK_OPEN = 0; //银行卡启用

    public final static Integer MAX_BANK_NUM = 6; //绑定银行卡最多数


}
