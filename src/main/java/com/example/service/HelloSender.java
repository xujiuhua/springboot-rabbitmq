package com.example.service;

import com.example.config.RabbitConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * <p></p>
 *
 * @author jiuhua.xu
 * @version 1.0
 * @since JDK 1.8
 */
@Service
public class HelloSender {

    private RabbitTemplate rabbitTemplate;

    public HelloSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;

//        this.rabbitTemplate.setReturnCallback(this);

        this.rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            // 如果发送到交换器都没有成功（比如说删除了交换器），ack 返回值为 false
            // 如果发送到交换器成功，但是没有匹配的队列（比如说取消了绑定），ack 返回值为还是 true （这是一个坑，需要注意）
            if (!ack) {
                System.out.println("HelloSender 消息发送失败");
                System.err.println(cause);
                System.err.println(correlationData);
            } else {
                System.out.println("HelloSender 消息发送成功 ");
                System.out.println(cause);
                System.out.println(correlationData);
            }
        });
    }

    public void send() {
        String context = "你好现在是 " + new Date() + "";
        System.out.println("HelloSender发送内容 : " + context);
        // 防止发送消息失败，先将消息存入本地
        // DB 或者 缓存

//        this.rabbitTemplate.setReturnCallback(this);
//        this.rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
//            // 如果发送到交换器都没有成功（比如说删除了交换器），ack 返回值为 false
//            // 如果发送到交换器成功，但是没有匹配的队列（比如说取消了绑定），ack 返回值为还是 true （这是一个坑，需要注意）
//            if (!ack) {
//                System.out.println("HelloSender 消息发送失败");
//                System.err.println(cause);
//                System.err.println(correlationData);
//            } else {
//                System.out.println("HelloSender 消息发送成功 ");
//                System.out.println(cause);
//                System.out.println(correlationData);
//            }
//        });

        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        this.rabbitTemplate.convertAndSend(RabbitConfig.DIRECT_EXCHANGE, RabbitConfig.ROUTING_KEY_A, context, correlationData);

        // error: NO_ROUTE
//        this.rabbitTemplate.convertAndSend(RabbitConfig.DIRECT_EXCHANGE, "RabbitConfig.ROUTING_KEY_A", context, correlationData);

        // error: no exchange 'RabbitConfig.DIRECT_EXCHANGE'
//        this.rabbitTemplate.convertAndSend("RabbitConfig.DIRECT_EXCHANGE", RabbitConfig.ROUTING_KEY_A, context, correlationData);
    }

//    @Override
//    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
//        // 发送到交换器成功，但是没有匹配的队列
//        System.out.println("Sender Return " + message.toString());
//        System.out.println("replyCode: " + replyCode);
//        System.out.println("replyText: " + replyText);
//        System.out.println("exchange: " + exchange);
//        System.out.println("routingKey: " + routingKey);
//    }

}

