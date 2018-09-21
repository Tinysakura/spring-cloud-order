package com.cfh.practice.order.controller;

import com.cfh.practice.order.config.GirlConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: cfh
 * @Date: 2018/9/21 20:30
 * @Description: 测试spring cloud config的自动刷新功能
 */
@RestController
public class ConfigController {

    @Autowired
    GirlConfig girlConfig;

    @GetMapping("/girl")
    public String testAutoRefreshConfig(){
        return "girl"+" "+"name:"+girlConfig.getName()+" "+"age:"+girlConfig.getAge();
    }

}
