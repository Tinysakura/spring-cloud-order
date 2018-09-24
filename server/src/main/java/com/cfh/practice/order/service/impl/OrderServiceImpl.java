package com.cfh.practice.order.service.impl;

import com.cfh.practice.client.ProductClient;
import com.cfh.practice.common.ProductInfoOutput;
import com.cfh.practice.order.dataobject.OrderDetail;
import com.cfh.practice.order.dataobject.OrderMaster;
import com.cfh.practice.order.dto.OrderDTO;
import com.cfh.practice.order.enums.OrderErrorsEnum;
import com.cfh.practice.order.enums.OrderStatusEnum;
import com.cfh.practice.order.enums.PayStatusEnum;
import com.cfh.practice.order.exception.OrderException;
import com.cfh.practice.order.repository.OrderDetailRepository;
import com.cfh.practice.order.repository.OrderMasterRepository;
import com.cfh.practice.order.service.OrderService;
import com.cfh.practice.order.util.KeyUtil;
import com.sun.javafx.binding.StringFormatter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

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
//        List<DecreaseStockInput> decreaseStockInputs = new ArrayList<>();
//
//        for (OrderDetail orderDetail : orderDTO.getOrderDetailList()) {
//            DecreaseStockInput decreaseStockInput = new DecreaseStockInput();
//            decreaseStockInput.setProductId(orderDetail.getProductId());
//            decreaseStockInput.setProductQuantity(orderDetail.getProductQuantity());
//
//            decreaseStockInputs.add(decreaseStockInput);
//        }
//        productClient.decreaseStock(decreaseStockInputs);

        //Todo 实现异步扣库存
        //使用分布式锁保证正确性
        RLock distributedLock = redissonClient.getLock("decrease_stock");
        //这里的锁时间写死了，应该放在配置在文件中根据实际场景进行调整
        distributedLock.lock(1000, TimeUnit.MICROSECONDS);
        try {
            //获取redis中相应商品的数量减去相应的库存
            String keyFormat = "product_stock_%s";
            for (OrderDetail detail : orderDTO.getOrderDetailList()) {
                String key = StringFormatter.format(keyFormat, detail.getProductId()).getValue();
                String stockString = redisTemplate.opsForValue().get(key);
                Long stockLong = new Long(stockString);
                stockLong -= detail.getProductQuantity();
                redisTemplate.opsForValue().set(key, String.valueOf(stockLong));
            }

            //发送一个减库存的消息
            orderDTO.setOrderId(orderId);
            sendDecreaseStockMessage(orderDTO);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            distributedLock.unlock();
        }

        //订单入库
        OrderMaster orderMaster = new OrderMaster();
        orderDTO.setOrderId(orderId);
        BeanUtils.copyProperties(orderDTO, orderMaster);
        orderMaster.setOrderAmount(totalAmount);
        orderMaster.setOrderStatus(OrderStatusEnum.NEW.getCode());
        orderMaster.setPayStatus(PayStatusEnum.WAIT.getCode());

        try {
            orderMasterRepository.save(orderMaster);
        } catch (Exception e) {
            e.printStackTrace();
            //订单入库若失败需要手动回滚redis中的数据
            rollBackRedis(orderDTO.getOrderDetailList());
        }
        return orderDTO;
    }

    @Override
    @Transactional
    public OrderDTO finish(String orderID) {
        Optional<OrderMaster> optionalOrderMaster = orderMasterRepository.findById(orderID);

        if (!optionalOrderMaster.isPresent()) {
            throw new OrderException(OrderErrorsEnum.UNPRESENT.getCode(), OrderErrorsEnum.UNPRESENT.getMessage());
        }

        OrderMaster orderMaster = optionalOrderMaster.get();
        //检查订单状态，只有状态为新订单的订单才能被确认
        if (orderMaster.getOrderStatus() != OrderStatusEnum.NEW.getCode()) {
            throw  new OrderException(OrderErrorsEnum.ERRORSTATU.getCode(), OrderErrorsEnum.ERRORSTATU.getMessage());
        }

        List<OrderDetail> orderDetails = orderDetailRepository.findAllByOrderId(orderID);
        OrderDTO orderDTO = new OrderDTO();
        BeanUtils.copyProperties(orderMaster, orderDTO);

        //这里最好再对orderDetailList的状态做一次检查
        orderDTO.setOrderDetailList(orderDetails);

        orderMaster.setOrderStatus(OrderStatusEnum.FINISHED.getCode());
        orderMasterRepository.save(orderMaster);

        return orderDTO;
    }

    private void rollBackRedis(List<OrderDetail> orderDetails){
        String keyFormat = "product_stock_%s";
        for (OrderDetail detail : orderDetails) {
            String key = StringFormatter.format(keyFormat, detail.getProductId()).getValue();
            redisTemplate.opsForValue().increment(key, detail.getProductQuantity());
        }
    }

    private void sendDecreaseStockMessage(OrderDTO orderDTO){
        rabbitTemplate.convertAndSend("decreaseStockQueue", orderDTO);
    }

}
