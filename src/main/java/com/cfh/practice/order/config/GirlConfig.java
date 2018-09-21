package com.cfh.practice.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @Author: cfh
 * @Date: 2018/9/21 20:29
 * @Description: 测试spring cloud config的自动刷新
 */
//指定使用的配置的前缀
@ConfigurationProperties("girl")
@Component
@Data
@RefreshScope
public class GirlConfig {
    private String name;
    private int age;
}
