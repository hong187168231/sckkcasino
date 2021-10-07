package com.qianyi.modulespringrabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DirectRabbitConfig {

    @Bean
    public Queue TestDirectQueue(){
        return new Queue("TestDirectQueue",true);
    }

    @Bean
    public Queue ChargeOrderQueue(){
        return new Queue("ChargeOrderQueue",true);
    }

    @Bean
    public DirectExchange TestDirectExchange(){
        return new DirectExchange("TestDirectExchange",true,false);
    }

    @Bean
    public DirectExchange ChargeOrderExchange(){
        return new DirectExchange("ChargeOrderExchange",true,false);
    }

    @Bean
    public Binding bindingDirect(){
        return BindingBuilder.bind(TestDirectQueue()).to(TestDirectExchange()).with("123");
    }

    @Bean
    public Binding bindingChargeOrder(){
        return BindingBuilder.bind(ChargeOrderQueue()).to(ChargeOrderExchange()).with("chargeOrder");
    }
}
