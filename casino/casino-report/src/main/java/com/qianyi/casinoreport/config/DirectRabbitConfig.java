package com.qianyi.casinoreport.config;

import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DirectRabbitConfig {

//    @Bean
//    public Queue TestDirectQueue(){
//        return new Queue("TestDirectQueueDev",true);
//    }
//    @Bean
//    public DirectExchange TestDirectExchange(){
//        return new DirectExchange("TestDirectExchangeDev",true,false);
//    }
//    @Bean
//    public Binding bindingDirect(){
//        return BindingBuilder.bind(TestDirectQueue()).to(TestDirectExchange()).with("123");
//    }

    /**
     * 充值消息MQ
     * @return
     */
    @Bean
    public Queue ChargeOrderQueue(){
        return new Queue(RabbitMqConstants.CHARGEORDER_QUEUE,true);
    }
    @Bean
    public DirectExchange ChargeOrderExchange(){
        return new DirectExchange(RabbitMqConstants.CHARGEORDER_DIRECTQUEUE_DIRECTEXCHANGE,true,false);
    }
    @Bean
    public Binding bindingChargeOrder(){
        return BindingBuilder.bind(ChargeOrderQueue()).to(ChargeOrderExchange()).with(RabbitMqConstants.INGCHARGEORDER_DIRECT);
    }


    /**
     * 分润MQ
     * @return
     */
    @Bean
    public Queue shareProfitDirectQueue(){
        return new Queue(RabbitMqConstants.SHAREPROFIT_DIRECTQUEUE,true);
    }

    @Bean
    public DirectExchange shareProfitDirectQueueDirectExchange(){
        return new DirectExchange(RabbitMqConstants.SHAREPROFIT_DIRECTQUEUE_DIRECTEXCHANGE,true,false);
    }

    @Bean
    public Binding ShareProfitDirect(){
        return BindingBuilder.bind(shareProfitDirectQueue()).to(shareProfitDirectQueueDirectExchange()).with(RabbitMqConstants.SHAREPROFIT_DIRECT);
    }


    /**
     * 团队新增成员
     * @return
     */
    @Bean
    public Queue addUserToTeamQueue(){
        return new Queue(RabbitMqConstants.ADDUSERTOTEAM_DIRECTQUEUE,true);
    }

    @Bean
    public DirectExchange addUserToTeamDirectQueueDirectExchange(){
        return new DirectExchange(RabbitMqConstants.ADDUSERTOTEAM_DIRECTQUEUE_DIRECTEXCHANGE,true,false);
    }

    @Bean
    public Binding addUserToTeam(){
        return BindingBuilder.bind(addUserToTeamQueue()).to(addUserToTeamDirectQueueDirectExchange()).with(RabbitMqConstants.ADDUSERTOTEAM_DIRECT);
    }
}
