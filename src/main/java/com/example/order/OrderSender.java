package com.example.order;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.api.R;
import com.example.config.RabbitConfig;
import com.example.entity.BrokerMessageLog;
import com.example.entity.Order;
import com.example.service.IBrokerMessageLogService;
import com.example.service.IOrderService;
import com.rabbitmq.client.Return;
import com.rabbitmq.client.ReturnCallback;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Wrapper;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p></p>
 *
 * @author jiuhua.xu
 * @version 1.0
 * @since JDK 1.8
 */
@Slf4j
@Component
public class OrderSender {

    private final RabbitTemplate rabbitTemplate;
    private final IBrokerMessageLogService brokerMessageLogService;
    private Map<String, String> returnCallBackMap = new HashMap<>();

    /**
     * 发送到交换器成功，但是没有匹配的队列
     */
    private final RabbitTemplate.ReturnCallback returnedMessage = (message, replyCode, replyText, exchange, routingKey) -> {
        // 发送到交换器成功，但是没有匹配的队列
        System.out.println("Sender Return " + message.toString());
        System.out.println("replyCode: " + replyCode);
        System.out.println("replyText: " + replyText);
        System.out.println("exchange: " + exchange);
        System.out.println("routingKey: " + routingKey);
        RCB rcb = RCB.builder()
                .replyCode(replyCode)
                .replyText(replyText)
                .exchange(exchange)
                .routingKey(routingKey)
                .message(message)
                .build();

        String messageId = (String) message.getMessageProperties().getHeaders().get("spring_returned_message_correlation");
        returnCallBackMap.put(messageId, JSON.toJSONString(rcb));

    };

    public OrderSender(
            final RabbitTemplate rabbitTemplate,
            final IBrokerMessageLogService brokerMessageLogService
    ) {

        this.rabbitTemplate = rabbitTemplate;
        this.brokerMessageLogService = brokerMessageLogService;

        this.rabbitTemplate.setReturnCallback(returnedMessage);

        this.rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            // 如果发送到交换器都没有成功（比如说删除了交换器），ack 返回值为 false
            if (!ack) {
                System.out.format("%s 消息发送失败, correlationData: %s", this.getClass().getName(), correlationData);
                System.out.println();
                System.err.println(cause);
                System.err.println(correlationData);
                final String id = correlationData.getId();

                BrokerMessageLog brokerMessageLog = this.brokerMessageLogService.getOne(Wrappers.<BrokerMessageLog>lambdaQuery().eq(BrokerMessageLog::getMessageId, id));
                int retryMaxCnt = 5;
                while (Objects.isNull(brokerMessageLog)) {
                    if (retryMaxCnt-- <= 0) {
                        break;
                    }

                    try {
                        Thread.sleep(200);
                        brokerMessageLog = this.brokerMessageLogService.getOne(Wrappers.<BrokerMessageLog>lambdaQuery().eq(BrokerMessageLog::getMessageId, id));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                brokerMessageLog.setUpdateTime(LocalDateTime.now());
                brokerMessageLog.setCause(cause);
                this.brokerMessageLogService.updateById(brokerMessageLog);

            } else {

                System.out.format("%s 消息发送成功, correlationData: %s", this.getClass().getName(), correlationData);
                System.out.println();
                // 更新表broker_message_log
                final String id = correlationData.getId();
                log.info("id: {}", id);

                // 轮询查询数据， 这里可能会查询不到数据，因为数据还没有持久到数据库，方法就执行到此处。
                BrokerMessageLog brokerMessageLog = this.brokerMessageLogService.getOne(Wrappers.<BrokerMessageLog>lambdaQuery().eq(BrokerMessageLog::getMessageId, id));
                int retryMaxCnt = 10;
                while (Objects.isNull(brokerMessageLog)) {
                    if (retryMaxCnt-- <= 0) {
                        break;
                    }

                    try {
                        Thread.sleep(200);
                        brokerMessageLog = this.brokerMessageLogService.getOne(Wrappers.<BrokerMessageLog>lambdaQuery().eq(BrokerMessageLog::getMessageId, id));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (Objects.nonNull(brokerMessageLog)) {

                    // 如果发送到交换器成功，但是没有匹配的队列（比如说取消了绑定），ack 返回值为还是 true （这是一个坑，需要注意）
                    if (returnCallBackMap.containsKey(correlationData.getId())) {
                        log.warn("未匹配到队列，发送失败");
                        brokerMessageLog.setUpdateTime(LocalDateTime.now());
                        brokerMessageLog.setStatus(4);
                        brokerMessageLog.setReturnBack(returnCallBackMap.get(correlationData.getId()));
                        this.brokerMessageLogService.updateById(brokerMessageLog);
                        returnCallBackMap.remove(correlationData.getId());
                        return;
                    }

                    brokerMessageLog.setUpdateTime(LocalDateTime.now());
                    brokerMessageLog.setStatus(1);
                    this.brokerMessageLogService.updateById(brokerMessageLog);
                }

            }
        });
    }

    /**
     * 核心业务代码
     *
     * @param order
     * @throws Exception
     */
    public void send(Order order) throws Exception {

        // step3: 发送到消息队列
        CorrelationData correlationData = new CorrelationData(order.getMessageId());
        this.rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_ORDER, RabbitConfig.ROUTING_KEY_ORDER_ADD, order, correlationData);
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


    public static void main(String[] args) {
        RCB rcb = RCB.builder()
                .replyCode(1)
                .replyText("2")
                .exchange("2")
                .routingKey("2")
                .build();

        System.out.println(rcb);

        System.out.println(JSON.toJSONString(rcb));
    }
}


@Builder
@Data
class RCB {
    int replyCode;
    String replyText;
    String exchange;
    String routingKey;
    Message message;
}