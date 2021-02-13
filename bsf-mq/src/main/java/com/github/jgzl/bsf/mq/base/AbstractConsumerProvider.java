package com.github.jgzl.bsf.mq.base;

import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

/**
 * @author: chejiangyi
 * @version: 2019-06-12 14:49
 * 消费者提供者: 提供不同类型的消息队列的消费者
 **/
public class AbstractConsumerProvider extends AbstractConsumer {
	/**
	 * 	MQ消费订阅接口
	 * @param consumergroup 消费组
	 * @param topic 订阅主题
	 * @param filterTags 标签过滤器
	 * @param runnable 处理方法
	 * @param type 消息体类型
	 * @return AbstractConsumer 
	 * */
    public  <T> AbstractConsumer subscribe(String consumergroup, String topic, String[] filterTags,
    		SubscribeRunable<T> runnable, Class<T> type) {return null;}
	/**
	 * 	MQ消费订阅接口
	 * @param consumergroup 消费组
	 * @param mode 消费模式：集群模式，广播消息
	 * @param topic 订阅主题
	 * @param filterTags 标签过滤器
	 * @param runnable 处理方法
	 * @param type 消息体类型
	 * @return AbstractConsumer 
	 * */
    public  <T> AbstractConsumer subscribe(String consumergroup,MessageModel mode,  String topic, String[] filterTags,
    		SubscribeRunable<T> runnable, Class<T> type) {return null;}
	/**
	 * 	MQ消费订阅接口
	 * @param consumergroup 消费组
	 * @param topic 订阅主题
	 * @param filterTags 标签过滤器
	 * @param runnable 处理方法
	 * @param type 消息体类型
	 * @param isOrderly 是否顺序消费
	 * @return AbstractConsumer 
	 * */
    public <T> AbstractConsumer subscribe(String consumergroup,String topic,String[] filterTags,
    		SubscribeRunable<T> runnable,Class<T> type,boolean isOrderly) {return null;}
	/**
	 * 	MQ消费订阅接口
	 * @param consumergroup 消费组
	 * @param mode 消费模式：集群模式，广播消息
	 * @param topic 订阅主题
	 * @param filterTags 标签过滤器
	 * @param runnable 处理方法
	 * @param type 消息体类型
	 * @param isOrderly 是否顺序消费
	 * @return AbstractConsumer 
	 * */
    public  <T> AbstractConsumer subscribe(String consumergroup,MessageModel mode,  String topic, String[] filterTags, 
    		SubscribeRunable<T> runnable, Class<T> type,boolean isOrderly) {return null; }
    /**
	 * 	MQ消费订阅接口
	 *  	增加线程数限制扩展，适应同业务不同消费组并发特性的场景
	 * @param minThread
	 * @param maxThread
	 * @param consumergroup 消费组
	 * @param mode 消费模式：集群模式，广播消息
	 * @param topic 订阅主题
	 * @param filterTags 标签过滤器
	 * @param runnable 处理方法
	 * @param type 消息体类型
	 * @param isOrderly 是否顺序消费
	 * @return AbstractConsumer 
	 * */
    public  <T> AbstractConsumer subscribe(Integer consumeThreadMin,Integer consumeThreadMax,String consumergroup,
    		MessageModel mode,  String topic, String[] filterTags, SubscribeRunable<T> runnable,
    		Class<T> type,boolean isOrderly) {return null; }

}

