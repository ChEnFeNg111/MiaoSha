package com.chen.miaosha.rabbitmq;

import com.chen.miaosha.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQSender {

    private static Logger logger = LoggerFactory.getLogger(RabbitMQSender.class);

    @Autowired
    AmqpTemplate amqpTemplate;

    /**
     *  将 MiaoShaMessage 秒杀信息先发送到指定的队列中
     * @param miaoshaMessage
     */
    public void sender(Object miaoshaMessage){
        String msg = RedisService.beanToString(miaoshaMessage);
        logger.info(msg);
        amqpTemplate.convertAndSend(RabbitMQConfig.MIAOSHA_QUEUE,msg);
    }

    /**
     *  发送到指定 routingkey 的队列中
     * @param message
     *//*
    public void directSender(Object message){
        String msg = RedisService.beanToString(message);
        logger.info("DirectSenderMassage:"+msg);

        // 发送到指定的 routingkey 的队列中（RabbitMQConfig.DIRECT_QUEUE）
        amqpTemplate.convertAndSend(RabbitMQConfig.DIRECT_QUEUE,msg);

    }

    *//**
     *  通过 TopicExchange 来发送到能够匹配的 routingkey 队列中
     * @param message
     *//*
    public void topicSendet(Object message){
        String msg = RedisService.beanToString(message);
        logger.info("TopicSenderMassage:"+msg);
        // 发送到指定的 routingkey 队列中
        amqpTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE,"topic.key1",msg+"1");
        amqpTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE,"topic.key2",msg+"2");
    }

    *//**
     * FanoutExchange : 将消息分发到所有的绑定队列中，无 routingkey 的概念，相当于广播
     *//*
    public void fanoutSender(Object message){
        String msg = RedisService.beanToString(message);
        logger.info("FanoutSenderMessage:"+msg);
        amqpTemplate.convertAndSend(RabbitMQConfig.FANOUT_EXCHANGE,msg);
    }

    *//**
     *  HeadersExchange : 发送添加了属性 key-value 消息
     *//*
    public void headersExchange(Object message){
        String msg = RedisService.beanToString(message);
        logger.info("HeadersSenderMessage: "+msg);

        // 添加头部的 key-value 属性
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("header1","value1");
        messageProperties.setHeader("header2","value2");

        Message obj = new Message(msg.getBytes(),messageProperties);

        amqpTemplate.convertAndSend(RabbitMQConfig.HEADERS_EXCHANGE,"",obj);
    }
*/

}
