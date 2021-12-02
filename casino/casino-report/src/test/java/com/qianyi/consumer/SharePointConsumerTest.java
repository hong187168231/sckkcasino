package com.qianyi.consumer;

import com.qianyi.casinocore.vo.ShareProfitMqVo;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class SharePointConsumerTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void should_send_message_to_mq(){
        ShareProfitMqVo shareProfitMqVo = new ShareProfitMqVo();
        shareProfitMqVo.setUserId(10240l);
        shareProfitMqVo.setGameRecordId(88390l);
        shareProfitMqVo.setValidbet(BigDecimal.valueOf(10));
        shareProfitMqVo.setBetTime("2021-12-02 13:24:59");
        rabbitTemplate.convertAndSend(RabbitMqConstants.SHAREPROFIT_DIRECTQUEUE_DIRECTEXCHANGE,RabbitMqConstants.SHAREPROFIT_DIRECT,shareProfitMqVo,new CorrelationData(UUID.randomUUID().toString()));
        log.info("success");
    }
}
