package com.example.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * <p></p>
 *
 * @author jiuhua.xu
 * @version 1.0
 * @since JDK 1.8
 */
@Configuration
public class RabbitConfig {

    public static final String DIRECT_EXCHANGE = "directExchange";

    public static final String QUEUE_A = "queue_A";
    public static final String QUEUE_B = "queue_B";

    public static final String ROUTING_KEY_A = "routingKey_A";
    public static final String ROUTING_KEY_B = "routingKey_B";

    @Bean
    public Queue QueueA() {
        return new Queue(QUEUE_A);
    }

    @Bean
    public Queue QueueB() {
        return new Queue(QUEUE_B);
    }

    @Bean
    DirectExchange directExchange() {
        return new DirectExchange(DIRECT_EXCHANGE);
    }

    @Bean
    Binding bindingExchangeRAA() {
        return BindingBuilder.bind(QueueA()).to(directExchange()).with(ROUTING_KEY_A);
    }

    @Bean
    Binding bindingExchangeRAB() {
        return BindingBuilder.bind(QueueA()).to(directExchange()).with(ROUTING_KEY_B);
    }

    @Bean
    Binding bindingExchangeRBA() {
        return BindingBuilder.bind(QueueB()).to(directExchange()).with(ROUTING_KEY_B);
    }


    public static final String EXCHANGE_ORDER = "order-exchange";
    public static final String QUEUE_ORDER = "order-queue";
    public static final String ROUTING_KEY_ORDER_ADD = "order.add";

    @Bean
    public Queue queueOrder() {
        return new Queue(QUEUE_ORDER, true);
    }

    @Bean
    TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_ORDER);
    }

    @Bean
    Binding bindingExchangeOrder() {
        return BindingBuilder.bind(queueOrder()).to(topicExchange()).with("order.#");
    }

    /**
     *  因为要设置回调类，所以应是prototype类型，如果是singleton类型，多次设置回调类会报错
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // 必须设置为 true，不然当 发送到交换器成功，但是没有匹配的队列，不会触发 ReturnCallback 回调
        // 而且 ReturnCallback 比 ConfirmCallback 先回调，意思就是 ReturnCallback 执行完了才会执行 ConfirmCallback
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

}
