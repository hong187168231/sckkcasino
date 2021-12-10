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


    public final static String FOUR_SHAREPROFIT_DIRECTQUEUE = "four.profit.queue";
    public final static String FOUR_SHAREPROFIT_DIRECT = "fourShareProfit";

    public final static String FIVE_SHAREPROFIT_DIRECTQUEUE = "five.profit.queue";
    public final static String FIVE_SHAREPROFIT_DIRECT = "fiveShareProfit";


    public final static String SIX_SHAREPROFIT_DIRECTQUEUE = "six.profit.queue";
    public final static String SIX_SHAREPROFIT_DIRECT = "sixShareProfit";


    /**
     * 各级新增人数mq
     */
    public final static String LEVEL_ADDUSERTOTEAM_DIRECTQUEUE_DIRECTEXCHANGE = "level.add.user.to.team.exchange";

    public final static String ONE_ADDUSERTOTEAM_DIRECTQUEUE = "one.add.user.to.team.queue";
    public final static String ONE_ADDUSERTOTEAM_DIRECT = "oneAddUserDev";

    public final static String TWO_ADDUSERTOTEAM_DIRECTQUEUE = "two.add.user.to.team.queue";
    public final static String TWO_ADDUSERTOTEAM_DIRECT = "twoAddUserDev";

    public final static String THREE_ADDUSERTOTEAM_DIRECTQUEUE = "three.add.user.to.team.queue";
    public final static String THREE_ADDUSERTOTEAM_DIRECT = "threeAddUserDev";



    /**
     * 各级充值mq
     */
    public final static String LEVEL_CHARGEORDER_DIRECTQUEUE_DIRECTEXCHANGE = "level.charge.order.exchange";

    public final static String ONE_CHARGEORDER_QUEUE = "one.charge.order.queue";
    public final static String ONE_INGCHARGEORDER_DIRECT = "one.chargeOrderDev";

    public final static String TWO_CHARGEORDER_QUEUE = "two.charge.order.queue";
    public final static String TWO_INGCHARGEORDER_DIRECT = "two.chargeOrderDev";


    public final static String THREE_CHARGEORDER_QUEUE = "three.charge.order.queue";
    public final static String THREE_INGCHARGEORDER_DIRECT = "three.chargeOrderDev";


}



