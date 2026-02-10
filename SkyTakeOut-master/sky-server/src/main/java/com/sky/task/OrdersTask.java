package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类，定时处理订单状态
 */
@Component
@Slf4j
public class OrdersTask{

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理支付超时订单
     */
    @Scheduled(cron = "0 * * * * ? ") // 每分钟触发一次
//    @Scheduled(cron ="1/5 * * * * ?")
    public void processTimeoutOrder(){
        log.info("定时处理超时订单：{}", LocalDateTime.now());

        // select * from orders where status = ? and order_time <(当前时间 - 15 分钟)
        List<Orders> byStatusAndOrdertimeLT = orderMapper.getByStatusAndOrdertimeLT(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));

        if(byStatusAndOrdertimeLT != null && byStatusAndOrdertimeLT.size() > 0){
            for (Orders orders : byStatusAndOrdertimeLT) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("支付超时，取消订单");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 处理一直处于待派送状态的订单
     */
    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨一点触发一次
//    @Scheduled(cron ="0/5 * * * * ?")
    public void processDeliveryOrder(){
        log.info("处理处于待派送状态的订单：{}", LocalDateTime.now());
        List<Orders> byStatus = orderMapper.getByStatusAndOrdertimeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusMinutes(-60));

        if(byStatus != null && byStatus.size() > 0){
            for (Orders orders : byStatus) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }

}
