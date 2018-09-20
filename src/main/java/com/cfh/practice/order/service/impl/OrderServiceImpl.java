package com.cfh.practice.order.service.impl;

import com.cfh.practice.order.dataobject.OrderMaster;
import com.cfh.practice.order.dto.OrderDTO;
import com.cfh.practice.order.enums.OrderStatusEnum;
import com.cfh.practice.order.enums.PayStatusEnum;
import com.cfh.practice.order.repository.OrderMasterRepository;
import com.cfh.practice.order.service.OrderService;
import com.cfh.practice.order.util.KeyUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Created by 廖师兄
 * 2017-12-10 16:44
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMasterRepository orderDetailRepository;

    @Autowired
    private OrderMasterRepository orderMasterRepository;

    @Override
    public OrderDTO create(OrderDTO orderDTO) {

        //TODO 查询商品信息(调用商品服务)
        //TODO 计算总价
        //TODO 扣库存(调用商品服务)

        //订单入库
        OrderMaster orderMaster = new OrderMaster();
        orderDTO.setOrderId(KeyUtil.genUniqueKey());
        BeanUtils.copyProperties(orderDTO, orderMaster);
        orderMaster.setOrderAmount(new BigDecimal(5));
        orderMaster.setOrderStatus(OrderStatusEnum.NEW.getCode());
        orderMaster.setPayStatus(PayStatusEnum.WAIT.getCode());

        orderMasterRepository.save(orderMaster);
        return orderDTO;
    }
}
