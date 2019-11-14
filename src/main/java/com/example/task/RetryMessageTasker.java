package com.example.task;

import com.alibaba.fastjson.JSON;
import com.example.entity.BrokerMessageLog;
import com.example.entity.Order;
import com.example.order.OrderSender;
import com.example.service.IBrokerMessageLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *
 *
 * </p>
 *
 * @author jiuhua.xu
 * @version 1.0
 * @since JDK 1.8
 */
@Slf4j
@Component
public class RetryMessageTasker {

    private final IBrokerMessageLogService brokerMessageLogService;
    private final OrderSender orderSender;

    public RetryMessageTasker(IBrokerMessageLogService brokerMessageLogService, final OrderSender orderSender) {
        this.brokerMessageLogService = brokerMessageLogService;
        this.orderSender = orderSender;
    }

    @Scheduled(initialDelay = 3000, fixedDelay = 10000)
    public void reSend() {
        System.out.println("定时任务开始，" + LocalDateTime.now());
        // pull status = 0 and timeout message
        List<BrokerMessageLog> list =  brokerMessageLogService.findTimeout();
        list.forEach(a -> {
            LocalDateTime now = LocalDateTime.now();
            if (a.getTryCount() >= 3) {
                // update fail message
                a.setStatus(2);
                a.setUpdateTime(now);
                brokerMessageLogService.updateById(a);
            } else {
                // resend
                a.setTryCount(a.getTryCount() + 1);
                a.setNextRetry(now.plusSeconds(30));
                brokerMessageLogService.updateById(a);

                Order order = JSON.parseObject(a.getMessage(), Order.class);
                try {
                    orderSender.send(order);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("TODO 处理异常...");
                }
            }
        });
    }

}
