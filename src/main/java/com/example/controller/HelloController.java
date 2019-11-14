package com.example.controller;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.entity.Order;
import com.example.order.OrderSender;
import com.example.service.HelloSender;
import com.example.service.IOrderService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * <p></p>
 *
 * @author jiuhua.xu
 * @version 1.0
 * @since JDK 1.8
 */
@RestController
public class HelloController {

    private final IOrderService orderService;
    private final HelloSender helloSender;


    public HelloController(
            final IOrderService orderService,
            final HelloSender helloSender
    ) {
        this.orderService = orderService;
        this.helloSender = helloSender;
    }


    @PostMapping("hello")
    public void hello() {
        helloSender.send();
    }

    @PostMapping("/create/order")
    public void create() throws Exception {
        Order order = new Order();
        long id = IdWorker.getId();
        order.setId(id);
        order.setName("测试订单" + System.currentTimeMillis());
        order.setMessageId(id + "$" + UUID.randomUUID().toString());
        orderService.createOrder(order);
    }
}
