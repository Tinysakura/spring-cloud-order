package com.cfh.practice.order.reciver;

import com.cfh.practice.common.DecreaseStockReply;
import com.cfh.practice.order.dataobject.OrderMaster;
import com.cfh.practice.order.enums.OrderStatusEnum;
import com.cfh.practice.order.repository.OrderMasterRepository;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: cfh
 * @Date: 2018/9/22 21:45
 * @Description: 接受扣库存的消息处理结果的处理类
 */
@Component
public class DecreaseStockReplyReciver {
    @Autowired
    private OrderMasterRepository orderMasterRepository;

    @RabbitListener(queuesToDeclare = @Queue("decreaseStockReplyQueue"))
    public void receive(DecreaseStockReply decreaseStockReply) {
        OrderMaster orderMaster = orderMasterRepository.findById(decreaseStockReply.getOrderId()).get();

        //如果扣库存成功则将订单状态改为完成，否则改为已取消，并进行一系诶后续程式(通知用户，退款等)
        if (decreaseStockReply.getStatus() == 0){
            orderMaster.setOrderStatus(OrderStatusEnum.FINISHED.getCode());

            orderMasterRepository.save(orderMaster);

            //todo :通知用户支付等后续流程
            //...
            return;
        }

        orderMaster.setOrderStatus(OrderStatusEnum.CANCEL.getCode());
        orderMasterRepository.save(orderMaster);

        //todo :通知用户下单失败等后续流程
        //...
        return;
    }

}
