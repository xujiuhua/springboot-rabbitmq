package com.example.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dao.OrderDao;
import com.example.entity.BrokerMessageLog;
import com.example.entity.Order;
import com.example.order.OrderSender;
import com.example.service.IBrokerMessageLogService;
import com.example.service.IOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p></p>
 *
 * @author jiuhua.xu
 * @version 1.0
 * @since JDK 1.8
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderDao, Order> implements IOrderService {

    private final IBrokerMessageLogService brokerMessageLogService;
    private final OrderSender orderSender;

    public OrderServiceImpl(IBrokerMessageLogService brokerMessageLogService, final OrderSender orderSender) {
        this.brokerMessageLogService = brokerMessageLogService;
        this.orderSender = orderSender;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(Order order) {

        LocalDateTime now = LocalDateTime.now();

        // step1: 插入订单表
        baseMapper.insert(order);

        // step2: 插入日志表
        BrokerMessageLog log = new BrokerMessageLog();
        log.setCreateTime(now);
        log.setNextRetry(now.plusSeconds(30));
        log.setUpdateTime(now);
        log.setStatus(0);
        log.setTryCount(0);
        log.setMessageId(order.getMessageId());
        log.setMessage(JSON.toJSONString(order));
        boolean save = brokerMessageLogService.save(log);
        if (save) {
            // step3: 消息队列
            this.sent(order);
        }
    }

    private void sent(Order order) {
        try {
            orderSender.send(order);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
