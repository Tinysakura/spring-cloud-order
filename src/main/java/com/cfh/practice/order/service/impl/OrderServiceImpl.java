package com.cfh.practice.order.service.impl;

import com.cfh.practice.client.ProductClient;
import com.cfh.practice.order.dataobject.OrderDetail;
import com.cfh.practice.order.dataobject.OrderMaster;
import com.cfh.practice.order.dataobject.ProductInfo;
import com.cfh.practice.order.dto.CartDTO;
import com.cfh.practice.order.dto.OrderDTO;
import com.cfh.practice.order.enums.OrderStatusEnum;
import com.cfh.practice.order.enums.PayStatusEnum;
import com.cfh.practice.order.repository.OrderDetailRepository;
import com.cfh.practice.order.repository.OrderMasterRepository;
import com.cfh.practice.order.service.OrderService;
import com.cfh.practice.order.util.KeyUtil;
import common.DecreaseStockInput;
import common.ProductInfoOutput;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 廖师兄
 * 2017-12-10 16:44
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderMasterRepository orderMasterRepository;

    @Autowired
    private ProductClient productClient;

    @Transactional
    @Override
    public OrderDTO create(OrderDTO orderDTO) {
        String orderId = KeyUtil.genUniqueKey();

        //查询商品信息(调用商品服务)
        List<String> productIdList = new ArrayList<>();
        for (OrderDetail orderDetail : orderDTO.getOrderDetailList()) {
            productIdList.add(orderDetail.getProductId());
        }

        List<ProductInfoOutput> productInfos = productClient.listForOrder(productIdList);

        //计算总价
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderDetail orderDetail : orderDTO.getOrderDetailList()) {
            for (ProductInfoOutput productInfo : productInfos) {
                if(productInfo.getProductId().equals(orderDetail.getProductId())) {
                    BigDecimal quantity = new BigDecimal(orderDetail.getProductQuantity());
                    BigDecimal priece = productInfo.getProductPrice();
                    BigDecimal amount = quantity.multiply(priece);

                    totalAmount = totalAmount.add(amount);
                    //做一个商品的快照
                    BeanUtils.copyProperties(productInfo, orderDetail);
                    orderDetail.setOrderId(orderId);
                    orderDetail.setDetailId(KeyUtil.genUniqueKey());

                    orderDetailRepository.save(orderDetail);
                }
            }
        }

        //扣库存(调用商品服务)
        List<DecreaseStockInput> decreaseStockInputs = new ArrayList<>();

        for (OrderDetail orderDetail : orderDTO.getOrderDetailList()) {
            DecreaseStockInput decreaseStockInput = new DecreaseStockInput();
            decreaseStockInput.setProductId(orderDetail.getProductId());
            decreaseStockInput.setProductQuantity(orderDetail.getProductQuantity());

            decreaseStockInputs.add(decreaseStockInput);
        }
        productClient.decreaseStock(decreaseStockInputs);

        //订单入库
        OrderMaster orderMaster = new OrderMaster();
        orderDTO.setOrderId(orderId);
        BeanUtils.copyProperties(orderDTO, orderMaster);
        orderMaster.setOrderAmount(totalAmount);
        orderMaster.setOrderStatus(OrderStatusEnum.NEW.getCode());
        orderMaster.setPayStatus(PayStatusEnum.WAIT.getCode());

        orderMasterRepository.save(orderMaster);
        return orderDTO;
    }
}
