package com.qianyi.casinoreport.consumer.levelChargeOrder;

import com.qianyi.casinocore.vo.RechargeProxyBO;
import com.qianyi.casinoreport.business.recharge.LevelRechargeRecordBussiness;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RabbitListener(queues = RabbitMqConstants.TWO_CHARGEORDER_QUEUE)
@Component
public class ThreeChargeOrderConsumer {

    @Autowired
    private LevelRechargeRecordBussiness rechargeRecordBussiness;

    /**
     *  代理线分润消息队列
     * @param rechargeProxyBO
     * @param channel
     * @param message
     * @throws IOException
     */
    @RabbitHandler

    public void process(RechargeProxyBO rechargeProxyBO, Channel channel, Message message) throws IOException {
        log.info("THREE-start 充值订单ID:{},代理线充值消息队列：{}",rechargeProxyBO.getChargeOrderId(),rechargeProxyBO);
        try {
            rechargeRecordBussiness.processItem(rechargeProxyBO);
        }finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }
        log.info("THREE-end 充值订单ID:{},代理线充值消息队列：{}",rechargeProxyBO.getChargeOrderId(),rechargeProxyBO);
    }
}
