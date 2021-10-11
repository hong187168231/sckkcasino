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


}
