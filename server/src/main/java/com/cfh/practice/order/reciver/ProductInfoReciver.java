package com.cfh.practice.order.reciver;

import com.cfh.practice.order.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.javafx.binding.StringFormatter;
import common.ProductInfoOutput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * @Author: cfh
 * @Date: 2018/9/22 19:53
 * @Description: ProductInfo的消息监听者
 */
@Slf4j
public class ProductInfoReciver {
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 监听消息队列，将productInfo的值序列化之后存储到Redis中供订单创建的业务使用
     */
    //@RabbitListener(queues = "productInfoOutput", queuesToDeclare = @Queue("productInfoOutput"))
    public void reciveProductInfos(String message){
        String key = "product_stock_%s";

        List<ProductInfoOutput> productInfoOutputs = JsonUtil.string2Obj(message,
                new TypeReference<List<ProductInfoOutput>>() {});

        log.info("反序列化成功{}", productInfoOutputs);

        for (ProductInfoOutput productInfoOutput : productInfoOutputs){
            redisTemplate.opsForValue().set(StringFormatter.format(key, productInfoOutput.getProductId()).getValue(),
                    String.valueOf(productInfoOutput.getProductStock()));
        }
    }

}
