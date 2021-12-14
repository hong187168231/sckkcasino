package com.qianyi.casinoreport.consumer.levelSharePro.report;

import com.qianyi.casinocore.vo.ShareProfitBO;
import com.qianyi.casinoreport.business.LevelProxyReportBusiness;
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
@RabbitListener(queues = RabbitMqConstants.TWO_REPORT_PROFIT_QUEUE)
@Component
public class TwoProxyReportConsumer {

    @Autowired
    private LevelProxyReportBusiness levelproxyReportBusiness;
    /**
     *  代理线总报表分润队列
     * @param shareProfitBO
     * @param channel
     * @param message
     * @throws IOException
     */
    @RabbitHandler

    public void process(ShareProfitBO shareProfitBO, Channel channel, Message message) throws IOException {
        log.info("TWO-start 总报表-游戏id:{},代理线报表分润队列：{}",shareProfitBO.getRecordId(),shareProfitBO);
        try {
            //进行总报表处理
            levelproxyReportBusiness.processReport(shareProfitBO);
        } catch (Exception e) {
            log.error("TWO 总报表-分润总报表处理执行异常:"+ e);
        }finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }
        log.info("TWO-end 总报表-游戏id:{},代理线报表分润队列：{}",shareProfitBO.getRecordId(),shareProfitBO);
    }
}
