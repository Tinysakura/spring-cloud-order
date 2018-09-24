package com.cfh.practice.order.service;

import com.cfh.practice.order.dto.OrderDTO;

/**
 * Created by 廖师兄
 * 2017-12-10 16:39
 */
public interface OrderService {

    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    OrderDTO create(OrderDTO orderDTO);

    /**
     * 修改订单状态为已完成，只有卖家有权限调用该接口
     * @param orderID
     * @return
     */
    OrderDTO finish(String orderID);
}
