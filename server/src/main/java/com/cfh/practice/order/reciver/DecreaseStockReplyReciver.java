package com.cfh.practice.order.reciver;

import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

/**
 * @Author: cfh
 * @Date: 2018/9/22 21:45
 * @Description: 接受扣库存的消息处理结果的处理类
 */
public class DecreaseStockReplyReciver {
    @RabbitListener(queues = "decreaseStockReplyQueue", queuesToDeclare = @Queue("decreaseStockReplyQueue)"))
    public void receive() {

    }

}
