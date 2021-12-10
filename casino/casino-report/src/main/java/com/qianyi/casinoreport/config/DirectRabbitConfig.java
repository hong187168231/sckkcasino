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



    /**
     * 代理线分润MQ
     * @return
     */
    @Bean
    public Queue oneShareProfitDirectQueue(){
        return new Queue(RabbitMqConstants.ONE_SHAREPROFIT_DIRECTQUEUE,true);
    }

    @Bean
    public DirectExchange oneShareProfitDirectQueueDirectExchange(){
        return new DirectExchange(RabbitMqConstants.LEVEL_SHAREPROFIT_DIRECTQUEUE_DIRECTEXCHANGE,true,false);
    }
    @Bean
    public Binding oneShareProfitDirect(){
        return BindingBuilder.bind(oneShareProfitDirectQueue()).to(oneShareProfitDirectQueueDirectExchange()).with(RabbitMqConstants.ONE_SHAREPROFIT_DIRECT);
    }



    @Bean
    public Queue twoShareProfitDirectQueue(){
        return new Queue(RabbitMqConstants.TWO_SHAREPROFIT_DIRECTQUEUE,true);
    }
    @Bean
    public DirectExchange twoShareProfitDirectQueueDirectExchange(){
        return new DirectExchange(RabbitMqConstants.LEVEL_SHAREPROFIT_DIRECTQUEUE_DIRECTEXCHANGE,true,false);
    }
    @Bean
    public Binding twoShareProfitDirect(){
        return BindingBuilder.bind(twoShareProfitDirectQueue()).to(twoShareProfitDirectQueueDirectExchange()).with(RabbitMqConstants.TWO_SHAREPROFIT_DIRECT);
    }



    @Bean
    public Queue threeShareProfitDirectQueue(){
        return new Queue(RabbitMqConstants.THREE_SHAREPROFIT_DIRECTQUEUE,true);
    }
    @Bean
    public DirectExchange threeShareProfitDirectQueueDirectExchange(){
        return new DirectExchange(RabbitMqConstants.LEVEL_SHAREPROFIT_DIRECTQUEUE_DIRECTEXCHANGE,true,false);
    }
    @Bean
    public Binding threeShareProfitDirect(){
        return BindingBuilder.bind(threeShareProfitDirectQueue()).to(threeShareProfitDirectQueueDirectExchange()).with(RabbitMqConstants.THREE_SHAREPROFIT_DIRECT);
    }
    /**
     * 四
     */
    @Bean
    public Queue fourShareProfitDirectQueue(){
        return new Queue(RabbitMqConstants.FOUR_SHAREPROFIT_DIRECTQUEUE,true);
    }
    @Bean
    public DirectExchange fourShareProfitDirectQueueDirectExchange(){
        return new DirectExchange(RabbitMqConstants.LEVEL_SHAREPROFIT_DIRECTQUEUE_DIRECTEXCHANGE,true,false);
    }
    @Bean
    public Binding fourShareProfitDirect(){
        return BindingBuilder.bind(fourShareProfitDirectQueue()).to(fourShareProfitDirectQueueDirectExchange()).with(RabbitMqConstants.FOUR_SHAREPROFIT_DIRECT);
    }

    /**
     * 五
     */
    @Bean
    public Queue fiveShareProfitDirectQueue(){
        return new Queue(RabbitMqConstants.FIVE_SHAREPROFIT_DIRECTQUEUE,true);
    }
    @Bean
    public DirectExchange fiveShareProfitDirectQueueDirectExchange(){
        return new DirectExchange(RabbitMqConstants.LEVEL_SHAREPROFIT_DIRECTQUEUE_DIRECTEXCHANGE,true,false);
    }
    @Bean
    public Binding fiveShareProfitDirect(){
        return BindingBuilder.bind(fiveShareProfitDirectQueue()).to(fiveShareProfitDirectQueueDirectExchange()).with(RabbitMqConstants.FIVE_SHAREPROFIT_DIRECT);
    }

    /**
     * 六
     */
    @Bean
    public Queue sixShareProfitDirectQueue(){
        return new Queue(RabbitMqConstants.SIX_SHAREPROFIT_DIRECTQUEUE,true);
    }
    @Bean
    public DirectExchange sixShareProfitDirectQueueDirectExchange(){
        return new DirectExchange(RabbitMqConstants.LEVEL_SHAREPROFIT_DIRECTQUEUE_DIRECTEXCHANGE,true,false);
    }
    @Bean
    public Binding sixShareProfitDirect(){
        return BindingBuilder.bind(sixShareProfitDirectQueue()).to(sixShareProfitDirectQueueDirectExchange()).with(RabbitMqConstants.SIX_SHAREPROFIT_DIRECT);
    }


    /**
     * 代理线充值消息MQ
     * @return
     */
    @Bean
    public Queue oneChargeOrderQueue(){
        return new Queue(RabbitMqConstants.ONE_CHARGEORDER_QUEUE,true);
    }
    @Bean
    public DirectExchange oneChargeOrderExchange(){
        return new DirectExchange(RabbitMqConstants.LEVEL_CHARGEORDER_DIRECTQUEUE_DIRECTEXCHANGE,true,false);
    }
    @Bean
    public Binding oneBindingChargeOrder(){
        return BindingBuilder.bind(oneChargeOrderQueue()).to(oneChargeOrderExchange()).with(RabbitMqConstants.ONE_INGCHARGEORDER_DIRECT);
    }




    @Bean
    public Queue twoChargeOrderQueue(){
        return new Queue(RabbitMqConstants.TWO_CHARGEORDER_QUEUE,true);
    }
    @Bean
    public DirectExchange twoChargeOrderExchange(){
        return new DirectExchange(RabbitMqConstants.LEVEL_CHARGEORDER_DIRECTQUEUE_DIRECTEXCHANGE,true,false);
    }
    @Bean
    public Binding twoBindingChargeOrder(){
        return BindingBuilder.bind(twoChargeOrderQueue()).to(twoChargeOrderExchange()).with(RabbitMqConstants.TWO_INGCHARGEORDER_DIRECT);
    }





    @Bean
    public Queue threeChargeOrderQueue(){
        return new Queue(RabbitMqConstants.THREE_CHARGEORDER_QUEUE,true);
    }
    @Bean
    public DirectExchange threeChargeOrderExchange(){
        return new DirectExchange(RabbitMqConstants.LEVEL_CHARGEORDER_DIRECTQUEUE_DIRECTEXCHANGE,true,false);
    }
    @Bean
    public Binding threeBindingChargeOrder(){
        return BindingBuilder.bind(threeChargeOrderQueue()).to(threeChargeOrderExchange()).with(RabbitMqConstants.THREE_INGCHARGEORDER_DIRECT);
    }


    /**
     * 各级代理新增成员
     * @return
     */
    @Bean
    public Queue oneAddUserToTeamQueue(){
        return new Queue(RabbitMqConstants.ONE_ADDUSERTOTEAM_DIRECTQUEUE,true);
    }
    @Bean
    public DirectExchange oneAddUserToTeamDirectQueueDirectExchange(){
        return new DirectExchange(RabbitMqConstants.LEVEL_ADDUSERTOTEAM_DIRECTQUEUE_DIRECTEXCHANGE,true,false);
    }
    @Bean
    public Binding oneAddUserToTeam(){
        return BindingBuilder.bind(oneAddUserToTeamQueue()).to(oneAddUserToTeamDirectQueueDirectExchange()).with(RabbitMqConstants.ONE_ADDUSERTOTEAM_DIRECT);
    }



    @Bean
    public Queue twoAddUserToTeamQueue(){
        return new Queue(RabbitMqConstants.TWO_ADDUSERTOTEAM_DIRECTQUEUE,true);
    }
    @Bean
    public DirectExchange twoAddUserToTeamDirectQueueDirectExchange(){
        return new DirectExchange(RabbitMqConstants.LEVEL_ADDUSERTOTEAM_DIRECTQUEUE_DIRECTEXCHANGE,true,false);
    }
    @Bean
    public Binding twoAddUserToTeam(){
        return BindingBuilder.bind(twoAddUserToTeamQueue()).to(twoAddUserToTeamDirectQueueDirectExchange()).with(RabbitMqConstants.TWO_ADDUSERTOTEAM_DIRECT);
    }




    @Bean
    public Queue threeAddUserToTeamQueue(){
        return new Queue(RabbitMqConstants.THREE_ADDUSERTOTEAM_DIRECTQUEUE,true);
    }
    @Bean
    public DirectExchange threeAddUserToTeamDirectQueueDirectExchange(){
        return new DirectExchange(RabbitMqConstants.LEVEL_ADDUSERTOTEAM_DIRECTQUEUE_DIRECTEXCHANGE,true,false);
    }
    @Bean
    public Binding threeAddUserToTeam(){
        return BindingBuilder.bind(threeAddUserToTeamQueue()).to(threeAddUserToTeamDirectQueueDirectExchange()).with(RabbitMqConstants.THREE_ADDUSERTOTEAM_DIRECT);
    }


}
