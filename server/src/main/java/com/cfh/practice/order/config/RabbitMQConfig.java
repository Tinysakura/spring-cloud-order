package com.cfh.practice.order.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: cfh
 * @Date: 2018/9/23 14:37
 * @Description: rabbitmq的相关配置
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 修改rabbimq的简单转换器为json转换器以发送对象
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}
