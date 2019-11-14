### 发布方确认

> 发布方确认：消息到达exchange， exchange到达queue

- 如果消息没有到exchange, 则confirm回调, ack=false
- 如果消息到达exchange, 则confirm回调, ack=true
- exchange到queue成功,则不回调returnedMessage
- exchange到queue失败,则回调returnedMessage(需设置mandatory=true,否则不会回调,消息就丢了)

|                                                |                          |                |
| ---------------------------------------------- | ------------------------ | -------------- |
| 消息推送到server，没找到exchange               | ConfirmCallback（false） |                |
| 消息推送到server，找到exchange，但是没找到队列 | ConfirmCallback（true）  | RetrunCallback |
| 消息推送到server，exchange和队列都没找到       | ConfirmCallback（false） |                |
| 消息推送成功                                   | ConfirmCallback （true） |                |



```java
public class HelloSender implements RabbitTemplate.ReturnCallback {

    private RabbitTemplate rabbitTemplate;

    public HelloSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send() {
        String context = "你好现在是 " + new Date() + "";
        System.out.println("HelloSender发送内容 : " + context);

        this.rabbitTemplate.setReturnCallback(this);
        this.rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            // 如果发送到交换器都没有成功（比如说删除了交换器），ack 返回值为 false
            // 如果发送到交换器成功，但是没有匹配的队列（比如说取消了绑定），ack 返回值为还是 true （这是一个坑，需要注意）
            if (!ack) {
                System.out.println("HelloSender消息发送失败");
                System.err.println(cause);
                System.err.println(correlationData);
            } else {
                System.out.println("HelloSender消息发送成功 ");
                System.out.println(cause);
                System.out.println(correlationData);
            }
        });
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        this.rabbitTemplate.convertAndSend(RabbitConfig.DIRECT_EXCHANGE, RabbitConfig.ROUTING_KEY_A, context, correlationData);
    }

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.out.println("sender return " + message.toString() + "===" + replyCode + "===" + exchange + "===" + routingKey);
    }
}
```

### rabbitTemplate 多例
> 因为要设置回调类，所以应是prototype类型，如果是singleton类型，多次设置回调类会报错 

```java
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

@Bean
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    return template;
}
```

### basicReject与basicNack区别
```
basicReject：是接收端告诉服务器这个消息我拒绝接收,不处理,可以设置是否放回到队列中还是丢掉，而且只能一次拒绝一个消息,官网中有明确说明不能批量拒绝消息，为解决批量拒绝消息才有了basicNack。

basicNack：可以一次拒绝N条消息，客户端可以设置basicNack方法的multiple参数为true，服务器会拒绝指定了delivery_tag的所有未确认的消息(tag是一个64位的long值，最大值是9223372036854775807)。
```

### 消息100%可靠性投递解决方案

[慕课网消息100%投递](https://www.imooc.com/video/17854)

