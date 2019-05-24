package com.chen.miaosha.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 *  四种交换机模式：
 *     DirectExchenge: 按照 routingkey 分发到指定的队列中
 *     TopicExchenge:  多关键子匹配
 *     FanoutExchenge: 将消息分发到所有的绑定队列中，无 routingkey 的概念，相当于广播
 *     HeadersExchenge: 通过添加属性 key-value 进行匹配
 *
 */
@Configuration
public class RabbitMQConfig {

    public static final String MIAOSHA_QUEUE = "miaosha.queue";

    @Bean
    public Queue queue(){
        return new Queue(MIAOSHA_QUEUE,true);
    }


    /*

    // 指定的 routingkey
    public static final String DIRECT_QUEUE = "direct.queue";

    public static final String TOPIC_QUEUE1 = "topic.queue1";
    public static final String TOPIC_QUEUE2 = "topic.queue2";
    public static final String TOPIC_EXCHANGE = "topicExchage";

    public static final String FANOUT_QUEUE1 = "fanout.queue1";
    public static final String FANOUT_QUEUE2 = "fanout.queue2";
    public static final String FANOUT_EXCHANGE = "fanoutExchage";

    public static final String HEADER_QUEUE = "header.queue";
    public static final String HEADERS_EXCHANGE = "headersExchage";

    *//**
     *  DirectExchenge: 按照 routingkey 分发到指定的队列中
     *//*
    @Bean
    public Queue directQueue(){

        return new Queue(DIRECT_QUEUE,true);
    }

    *//**
     * TopicExchange 模式 ： 由 TopicExchange 来决定该绑定哪个（ routingkey ）来进行发送消息
     *//*
    @Bean
    public Queue topicQueue1(){
        return new Queue(TOPIC_QUEUE1,true);
    }
    @Bean
    public Queue topicQueue2(){
        return new Queue(TOPIC_QUEUE2,true);
    }
    @Bean
    public TopicExchange topicExchenge(){
        return new TopicExchange(TOPIC_EXCHANGE);
    }
    // 只绑定到指定 "topic.key1" 的队列中
    @Bean
    public Binding topicBinding1(){
       return BindingBuilder.bind(topicQueue1()).to(topicExchenge()).with("topic.key1");
    }
    // 绑定到以 "topic." 开头的队列中
    @Bean
    public Binding topicBinding2(){
        return BindingBuilder.bind(topicQueue1()).to(topicExchenge()).with("topic.#");
    }

    *//**
     *  FanoutExchange 模式：将消息分发到所有的绑定队列中，无 routingkey 的概念，相当于广播
     *//*
    @Bean
    public Queue fanoutQueue1(){
        return new Queue(FANOUT_QUEUE1,true);
    }
    @Bean
    public Queue fanoutQueue2(){
        return new Queue(FANOUT_QUEUE2,true);
    }
    @Bean
    public FanoutExchange fanoutExchange(){
        return new FanoutExchange(RabbitMQConfig.FANOUT_EXCHANGE);
    }
    @Bean
    public Binding fanoutBinging1(){
        return BindingBuilder.bind(fanoutQueue1()).to(fanoutExchange());
    }
    @Bean
    public Binding fanoutBinging2(){
        return BindingBuilder.bind(fanoutQueue2()).to(fanoutExchange());
    }

    *//**
     *  HeadersExchange 模式 :  通过添加属性 key-value 进行匹配
     *//*
    @Bean
    public Queue headersQueue(){
        return new Queue(HEADER_QUEUE,true);
    }
    @Bean
    public HeadersExchange headersExchange(){
        return new HeadersExchange(HEADERS_EXCHANGE);
    }
    @Bean
    public Binding headerBing(){
        Map<String,Object> map = new HashMap<>();
        map.put("header1","key1");
        map.put("header2","key2");
        // 只绑定指定的头部信息 key-value，进行匹配
        return BindingBuilder.bind(headersQueue()).to(headersExchange()).whereAll(map).match();
    }
*/


}
