package com.qianyi.casinoreport.consumer.levelSharePro;

import com.qianyi.casinocore.vo.ShareProfitBO;
import com.qianyi.casinocore.vo.ShareProfitMqVo;
import com.qianyi.casinoreport.business.shareprofit.LevelShareProfitBusiness;
import com.qianyi.casinoreport.business.shareprofit.LevelShareprofitItemService;
import com.qianyi.casinoreport.business.shareprofit.ShareProfitBusiness;
import com.qianyi.casinoreport.business.shareprofit.ShareprofitItemService;
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
@RabbitListener(queues = RabbitMqConstants.ONE_SHAREPROFIT_DIRECTQUEUE)
@Component
public class FirstSharePointConsumer {

    @Autowired
    private LevelShareprofitItemService sharepointItemService;

    /**
     *  代理线分润消息队列
     * @param shareProfitBO
     * @param channel
     * @param message
     * @throws IOException
     */
    @RabbitHandler

    public void process(ShareProfitBO shareProfitBO, Channel channel, Message message) throws IOException {
        log.info("ONE-start 游戏id:{},代理线分润消息队列：{}",shareProfitBO.getRecordId(),shareProfitBO);
        sharepointItemService.levelProcessItem(shareProfitBO);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        log.info("ONE-end 游戏id:{},代理线分润消息队列：{}",shareProfitBO.getRecordId(),shareProfitBO);
    }
}
