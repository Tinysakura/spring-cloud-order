package com.cfh.practice.order.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: cfh
 * @Date: 2018/9/22 19:20
 * @Description: 配置redission
 */
@Configuration
@RefreshScope
public class RedissonConfig {

    @Value("${redisson.host}")
    String host;

    @Value("${redisson,port}")
    String port;

    @Bean
    public RedissonClient redissonClient(){
        Config redissonConfig = new Config();
        //使用单机模式的redis
        redissonConfig.useSingleServer().setAddress("redis://" + host + ":" + port);

        return Redisson.create(redissonConfig);
    }

}
