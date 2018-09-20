package com.cfh.practice.order.config;

import com.cfh.practice.order.dataobject.ProductInfo;
import com.cfh.practice.order.dto.CartDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author: cfh
 * @Date: 2018/9/20 23:37
 * @Description: 使用feign调用注册在eureka上的服务
 */
@FeignClient(name = "product")//服务名
public interface ProductClient {

    @GetMapping("/msg")
    String productMsg();

    @PostMapping("/product/listForOrder")
    List<ProductInfo> listForOrder(@RequestBody List<String> productIdList);

    @PostMapping("/product/decreaseStock")
    void decreaseStock(@RequestBody List<CartDTO> cartDTOList);
}
