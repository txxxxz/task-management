package com.taskManagement.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 定时任务类，定时处理订单状态
 */
@Component
@Slf4j
public class ScheduledTasks {

    /**
     * 定时任务，每分钟执行一次
     * 定时处理派送中订单
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrders(){
        log.info("定时处理派送中订单：{}", LocalDateTime.now());

        // 计算超时时间，15分钟之前
        LocalDateTime time = LocalDateTime.now().plusHours(-1);

        // 查询超时订单
        // List<Orders> timeOutOrders= orderAdminMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);

        // if (timeOutOrders != null && !timeOutOrders.isEmpty())
        // {
        //     for(Orders orders:timeOutOrders)
        //     {
        //         // 设置订单状态为已完成
        //         orders.setStatus(Orders.COMPLETED);
        //
        //         // 设置派送完成时间
        //         orders.setDeliveryTime(LocalDateTime.now());
        //
        //     }
        // }
    }

}

