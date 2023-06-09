package com.qianyi.casinoreport.consumer.levelGroup;

import com.qianyi.casinocore.vo.ProxyUserBO;
import com.qianyi.casinoreport.business.usergroup.LevelUserGroupTransactionService;
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
@RabbitListener(queues = RabbitMqConstants.THREE_ADDUSERTOTEAM_DIRECTQUEUE)
@Component
public class ThreeGroupConsumer {

    @Autowired
    private LevelUserGroupTransactionService levelUserGroupTransactionService;

    /**
     *  代理线新增人数消息队列
     * @param proxyUserBO
     * @param channel
     * @param message
     * @throws IOException
     */
    @RabbitHandler

    public void process(ProxyUserBO proxyUserBO, Channel channel, Message message) throws IOException {
        log.info("THREE-start 代理线新增成员消息队列：{}",proxyUserBO);
        try {
            levelUserGroupTransactionService.processItem(proxyUserBO);
        }finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }
        log.info("THREE-end 代理线新增成员消息队列：{}",proxyUserBO);
    }
}
