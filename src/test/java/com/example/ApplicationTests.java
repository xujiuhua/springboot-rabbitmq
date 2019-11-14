package com.example;


import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.entity.BrokerMessageLog;
import com.example.entity.Order;
import com.example.order.OrderSender;
import com.example.service.IBrokerMessageLogService;
import com.example.service.IOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    private OrderSender orderSender;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IBrokerMessageLogService brokerMessageLogService;

    @Test
    public void send() throws Exception {
        Order order = new Order();
        long id = IdWorker.getId();
        order.setId(id);
        order.setName("测试订单1");
        order.setMessageId(id + "$" + UUID.randomUUID().toString());
//        orderSender.send(order);
    }

    @Test
    public void testTb() {
        List<Order> list = orderService.list();
        assertEquals(1, list.size());
    }

    @Test
    public void testInsertLog() {
        BrokerMessageLog log = new BrokerMessageLog();
        log.setCreateTime(LocalDateTime.now());
        boolean save = brokerMessageLogService.save(log);
        assertTrue(save);
    }

    @Test
    public void testCreateOrder() {
        Order order = new Order();
        long id = IdWorker.getId();
        order.setId(id);
        order.setName("测试订单" + System.currentTimeMillis());
        order.setMessageId(id + "$" + UUID.randomUUID().toString());
        orderService.createOrder(order);
    }

    @Test
    public void test2() {
        String a = "1194788014136516610$b71d1b94-b460-490b-9efc-0fd11d5923a6";
        BrokerMessageLog one = brokerMessageLogService.getOne(Wrappers.<BrokerMessageLog>lambdaQuery().eq(BrokerMessageLog::getMessageId, a));
        assertEquals(a, one.getMessageId());
    }

}
