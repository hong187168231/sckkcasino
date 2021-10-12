package com.qianyi.casinoreport.consumer;

import com.qianyi.casinocore.business.ShareProfitBusiness;
import com.qianyi.casinocore.vo.ShareProfitMqVo;
import com.qianyi.casinocore.vo.ShareProfitVo;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RabbitListener(queues = RabbitMqConstants.SHAREPROFIT_DIRECTQUEUE)
@Component
public class SharePointConsumer {

    @Autowired
    private ShareProfitBusiness shareProfitBusiness;

    @RabbitHandler
    public void process(ShareProfitMqVo shareProfitMqVo, Channel channel, Message message) throws IOException {
        log.info("消费者接受到的消息是：{}",shareProfitMqVo);
        shareProfitBusiness.procerssShareProfit(shareProfitMqVo);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
