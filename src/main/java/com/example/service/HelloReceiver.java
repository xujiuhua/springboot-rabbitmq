package com.example.service;

import com.example.config.RabbitConfig;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

/**
 * <p></p>
 *
 * @author jiuhua.xu
 * @version 1.0
 * @since JDK 1.8
 */
@Component
@RabbitListener(queues = RabbitConfig.QUEUE_A)
public class HelloReceiver {

    @RabbitHandler
    public void process(String hello, Channel channel, Message message) throws IOException {
        System.out.println("HelloReceiver收到  : " + hello + ", 收到时间" + new Date());
        try {
//            int a = 1/0;
            //告诉服务器收到这条消息 已经被我消费了 可以在队列删掉 这样以后就不会再发了 否则消息服务器以为这条消息没处理掉 后续还会在发
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            System.out.println("receiver success");
        } catch (Exception e) {
            e.printStackTrace();
            // @param requeue true if the rejected message(s) should be requeued rather than discarded/dead-lettered
            // 第三个参数如果是true，则消息重回队列重发
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false,false);

            // 第二个参数如果是true，则消息重回队列重发
//            channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            System.out.println("receiver fail");
        }

    }
}
