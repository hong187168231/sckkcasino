package com.qianyi.modulespringrabbitmq.config;

/**
 * MQ常量
 */
public class RabbitMqConstants {

    public final static String SHAREPROFIT_DIRECTQUEUE = "profit.queue";
    public final static String SHAREPROFIT_DIRECTQUEUE_DIRECTEXCHANGE = "profit.exchange";
    public final static String SHAREPROFIT_DIRECT = "shareProfit";

    public final static String ADDUSERTOTEAM_DIRECTQUEUE = "add.user.to.team.queue";
    public final static String ADDUSERTOTEAM_DIRECTQUEUE_DIRECTEXCHANGE = "add.user.to.team.exchange";
    public final static String ADDUSERTOTEAM_DIRECT = "addUserDev";

    public final static String CHARGEORDER_QUEUE = "charge.order.queue";
    public final static String CHARGEORDER_DIRECTQUEUE_DIRECTEXCHANGE = "charge.order.exchange";
    public final static String INGCHARGEORDER_DIRECT = "chargeOrderDev";




    public final static String LEVEL_SHAREPROFIT_DIRECTQUEUE_DIRECTEXCHANGE = "level.profit.exchange";

    public final static String ONE_SHAREPROFIT_DIRECTQUEUE = "one.profit.queue";
    public final static String ONE_SHAREPROFIT_DIRECT = "oneShareProfit";


    public final static String TWO_SHAREPROFIT_DIRECTQUEUE = "two.profit.queue";
    public final static String TWO_SHAREPROFIT_DIRECT = "twoShareProfit";

    public final static String THREE_SHAREPROFIT_DIRECTQUEUE = "three.profit.queue";
    public final static String THREE_SHAREPROFIT_DIRECT = "threeShareProfit";
}
