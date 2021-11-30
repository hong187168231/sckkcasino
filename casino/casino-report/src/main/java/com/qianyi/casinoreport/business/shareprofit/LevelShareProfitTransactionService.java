package com.qianyi.casinoreport.business.shareprofit;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.ShareProfitBO;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
            ShareProfitBO firstShareProfitBO=shareProfitBOList.get(0);
            ShareProfitBO secondShareProfitBO=shareProfitBOList.get(1);
            ShareProfitBO thirdShareProfitBO=shareProfitBOList.get(2);
            rabbitTemplate.convertAndSend(RabbitMqConstants.LEVEL_SHAREPROFIT_DIRECTQUEUE_DIRECTEXCHANGE,
                    RabbitMqConstants.ONE_SHAREPROFIT_DIRECT, firstShareProfitBO, new CorrelationData(UUID.randomUUID().toString()));
            rabbitTemplate.convertAndSend(RabbitMqConstants.LEVEL_SHAREPROFIT_DIRECTQUEUE_DIRECTEXCHANGE,
                    RabbitMqConstants.TWO_SHAREPROFIT_DIRECT, secondShareProfitBO, new CorrelationData(UUID.randomUUID().toString()));
            rabbitTemplate.convertAndSend(RabbitMqConstants.LEVEL_SHAREPROFIT_DIRECTQUEUE_DIRECTEXCHANGE,
                    RabbitMqConstants.THREE_SHAREPROFIT_DIRECT, thirdShareProfitBO, new CorrelationData(UUID.randomUUID().toString()));
        }
    }

}
