package com.cfh.practice.order.controller;

import com.cfh.practice.client.ProductClient;
import com.cfh.practice.common.ProductInfoOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: cfh
 * @Date: 2018/9/25 17:16
 * @Description:
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private ProductClient productClient;

    @PostMapping("/fallback")
    public List<ProductInfoOutput> testFallback(@RequestParam("productId") String productId) {
        List<String> list = new ArrayList<>();
        list.add(productId);

        return productClient.listForOrder(list);
    }
}
