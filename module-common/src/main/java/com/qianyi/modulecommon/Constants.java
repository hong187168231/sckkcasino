package com.qianyi.modulecommon;

/**
 * 常量
 */
public class Constants {

    //是/否，开/关, 真/假
    public final static Integer open = 1;
    public final static Integer close = 0;
    public final static Integer yes = 0;
    public final static Integer no = 1;

    //订单状态
    public final static Integer order_wait = 1;//等待确认
    public final static Integer order_success = 2;//确认成功
    public final static Integer order_fail = 3;//确认失败

    //充值订单状态
    public final static Integer chargeOrder_wait = 0;//原始状态，未接单
    public final static Integer chargeOrder_success = 1;//确认成功
    public final static Integer chargeOrder_fail = 2;//确认失败
    public final static Integer chargeOrder_disabled = 3;//失效
    public final static Integer chargeOrder_connected = 4;//已接单

    //提现订单状态 1：通过，2：拒绝，3：冻结
    public final static Integer withdrawOrder_wait = 0;//原始状态，未接单
    public final static Integer withdrawOrder_success = 1;//确认成功
    public final static Integer withdrawOrder_fail = 2;//拒绝
    public final static Integer withdrawOrder_freeze = 3;//冻结
    public final static Integer withdrawOrder_connected = 4;//已接单

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

    public final static Integer WITHDRAW_PASS = 1;
    public final static Integer WITHDRAW_REFUSE = 2;
    public final static Integer WITHDRAW_ORDER = 3;
}
