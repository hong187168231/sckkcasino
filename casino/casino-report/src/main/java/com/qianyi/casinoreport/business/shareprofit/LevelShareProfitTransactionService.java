package com.qianyi.casinoreport.business.shareprofit;

import com.qianyi.casinocore.vo.ShareProfitBO;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class LevelShareProfitTransactionService {
    @Autowired
    private RabbitTemplate rabbitTemplate;


    /**
     * 分发各级代理分润处理mq
     * @param shareProfitBOList
     */
    public void processShareProfitMq(List<ShareProfitBO> shareProfitBOList) {
        if (shareProfitBOList!=null && shareProfitBOList.size()>0){
            String routingKey=null;
            for (int i = 0; i < shareProfitBOList.size(); i++) {
                if (i==0){
                    routingKey= RabbitMqConstants.ONE_SHAREPROFIT_DIRECT;
                }
                if (i==1){
                    routingKey= RabbitMqConstants.TWO_SHAREPROFIT_DIRECT;
                }
                if (i==2){
                    routingKey= RabbitMqConstants.THREE_SHAREPROFIT_DIRECT;
                }
                rabbitTemplate.convertAndSend(RabbitMqConstants.LEVEL_SHAREPROFIT_DIRECTQUEUE_DIRECTEXCHANGE,
                        routingKey,shareProfitBOList.get(i), new CorrelationData(UUID.randomUUID().toString()));
            }
        }
    }

}
