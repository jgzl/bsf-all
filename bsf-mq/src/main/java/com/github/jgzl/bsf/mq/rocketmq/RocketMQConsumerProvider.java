package com.github.jgzl.bsf.mq.rocketmq;
import com.github.jgzl.bsf.core.serialize.JsonSerializer;
import com.github.jgzl.bsf.core.util.LogUtils;
import com.github.jgzl.bsf.core.util.WarnUtils;
import com.github.jgzl.bsf.mq.base.AbstractConsumer;
import com.github.jgzl.bsf.mq.base.AbstractConsumerProvider;
import com.github.jgzl.bsf.mq.base.MQException;
import com.github.jgzl.bsf.mq.base.Message;
import com.github.jgzl.bsf.mq.base.SubscribeRunable;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author: chejiangyi
 * @version: 2019-06-12 13:01
 * rocketmq的消费者封装使用提供类
 **/
public class RocketMQConsumerProvider extends AbstractConsumerProvider{

    @Autowired
    RocketMQProperties rocketMQProperties;

    @Override
    public <T> AbstractConsumer subscribe(String consumergroup,String topic,String[] filterTags,
            SubscribeRunable<T> runnable,Class<T> type) {       
    	return subscribe( consumergroup,MessageModel.CLUSTERING,topic,  filterTags, runnable, type,false);
    }
    @Override
    public <T> AbstractConsumer subscribe(String consumergroup,String topic,String[] filterTags,
            SubscribeRunable<T> runnable,Class<T> type,boolean isOrderly) {
       
        return subscribe( consumergroup,MessageModel.CLUSTERING,topic,  filterTags,runnable,type,isOrderly);
    }
    @Override
    public  <T> AbstractConsumer subscribe(String consumergroup,MessageModel mode,String topic, String[] filterTags,
            SubscribeRunable<T> runnable, Class<T> type) {
        return subscribe( consumergroup,mode,topic,  filterTags, runnable, type,false);
    }
    
    @Override
    public  <T> AbstractConsumer subscribe(String consumergroup,MessageModel mode,String topic, String[] filterTags,
            SubscribeRunable<T> runnable, Class<T> type,boolean isOrderly) {
        return subscribe(rocketMQProperties.getConsumeThreadMin(),rocketMQProperties.getConsumeThreadMax(), 
                consumergroup,mode,topic,  filterTags, runnable, type,false);
    }
    
    @Override
    public  <T> AbstractConsumer subscribe(Integer consumeThreadMin,Integer consumeThreadMax,String consumergroup,
            MessageModel mode,  String topic, String[] filterTags, SubscribeRunable<T> runnable,
            Class<T> type,boolean isOrderly) {
        DefaultMQPushConsumer consumer =null;
         try {
             consumer = new DefaultMQPushConsumer(consumergroup);
             consumer.setNamesrvAddr(rocketMQProperties.getNamesrvaddr());
             consumer.setInstanceName(UUID.randomUUID().toString());
             consumer.setVipChannelEnabled(Optional.ofNullable(rocketMQProperties.getIsUseVIPChannel())
            		 .orElse(false));
             consumer.setConsumeThreadMin(consumeThreadMin);
             consumer.setConsumeThreadMax(consumeThreadMax);
             consumer.setMessageModel(Optional.ofNullable(mode).orElse(MessageModel.CLUSTERING));
             consumer.subscribe(topic,volidateFilters(filterTags));
             if(isOrderly) {
                 consumerOrderly(consumer, runnable,  type);
             } else {
                 consumeConcurrently( consumer, runnable, type);
             }
             consumer.start();
             AbstractConsumer abstractConsumer = new AbstractConsumer();
             abstractConsumer.setObject(consumer);
             LogUtils.info(RocketMQConsumerProvider.class,RocketMQProperties.Project,
                     String.format("rocketmq 消费者%s,队列%s 启动成功",consumergroup,topic));
             return abstractConsumer;
         }
         catch (Exception exp)
         {
             LogUtils.error(RocketMQConsumerProvider.class,RocketMQProperties.Project,
                     String.format("rocketmq 消费者%s,队列%s 启动失败",consumergroup,topic),exp);
             if(consumer!=null)
             {
                 consumer.shutdown();  
             }
             throw new MQException(exp);
         }
    }
    /**
     * 	并发消费
     *	@author Robin.Wang
     *	@date	2020-12-28
     * */
    private <T> void consumeConcurrently(DefaultMQPushConsumer consumer,SubscribeRunable<T> runnable, Class<T> type){
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                MessageExt messageExt = msgs.get(0);
                try {
                     processMessage(messageExt, runnable, type);
                } catch (Exception e) {
                     String warnMsg=String.format("MQ 消费者%s,msgId:[%s],toppic:[%s] tag:[%s]消费异常,尝试消费%s次",
                                          consumer.getConsumerGroup(),messageExt.getMsgId(),messageExt.getTopic(),
                                          messageExt.getTags(),messageExt.getReconsumeTimes());
                    int reconsumeTimes = messageExt.getReconsumeTimes();
                    if (reconsumeTimes < rocketMQProperties.getReconsumeTimes()) {
                        LogUtils.warn(RocketMQConsumerProvider.class,RocketMQProperties.Project,warnMsg,e);
                        context.setDelayLevelWhenNextConsume(rocketMQProperties.getReconsumeTimes());
                        try {
                            Thread.sleep(rocketMQProperties.getReconsumeInterval());
                        }catch(Exception ex) {
                            LogUtils.warn(RocketMQConsumerProvider.class,RocketMQProperties.Project,"sleep exception",ex);
                        }
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }else {                   	
                   	 LogUtils.error(RocketMQConsumerProvider.class,RocketMQProperties.Project,warnMsg,e);
                   	 WarnUtils.notifynow(WarnUtils.ALARM_ERROR, "MQ 消费异常", warnMsg);
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
    }
    /**
     *	顺序消费
     *	@author Robin.Wang
     *	@date	2020-12-28
     * */
    private<T> void consumerOrderly(DefaultMQPushConsumer consumer,SubscribeRunable<T> runnable, Class<T> type) {
         consumer.registerMessageListener(new MessageListenerOrderly() {
             @Override
             public ConsumeOrderlyStatus consumeMessage(List<MessageExt> messageExts, ConsumeOrderlyContext context) {
                 context.setAutoCommit(true);
                 MessageExt messageExt = messageExts.get(0);
                 try {
                     processMessage(messageExt, runnable, type); 
                 } catch (Exception e) {
                     String warnMsg=String.format("MQ消费者%s,msgId:[%s],toppic:[%s] tag:[%s]顺序消费异常,尝试消费%s次",
                             consumer.getConsumerGroup(),messageExt.getMsgId(),messageExt.getTopic(),
                             messageExt.getTags(),messageExt.getReconsumeTimes());
                     if (messageExt.getReconsumeTimes() < rocketMQProperties.getReconsumeTimes()) {
                         LogUtils.warn(RocketMQConsumerProvider.class,RocketMQProperties.Project,warnMsg,e);
                         /**
                                               *  消费失败暂停（默认50毫秒）后重试，用于并发控制
                          * */
                          try {
                              Thread.sleep(rocketMQProperties.getReconsumeInterval());
                         }catch(Exception ex) {
                              LogUtils.warn(RocketMQConsumerProvider.class,RocketMQProperties.Project,"sleep exception",ex);
                         }
                         return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                     }else {
                         LogUtils.error(RocketMQConsumerProvider.class,RocketMQProperties.Project,warnMsg,e);
                          WarnUtils.notifynow(WarnUtils.ALARM_ERROR, "MQ 消费异常", warnMsg);
                     }
                 }
                 
                 return ConsumeOrderlyStatus.SUCCESS;
             }
         });
    }
    
    /**
     * 	消息处理
     * */
    private <T> void processMessage(MessageExt messageExt,
            SubscribeRunable<T> runnable,Class<T> type) throws UnsupportedEncodingException {
         byte[] body = messageExt.getBody();	    
         String msg = new String(body, RemotingHelper.DEFAULT_CHARSET);
         RocketMQMonitor.hook().run("consume", () -> {
             if(type == String.class)
             {
                 runnable.run(new Message<T>(messageExt.getMsgId(),
                          messageExt.getTopic(),messageExt.getTags(), (T)msg));
             }
             else
             {
                 runnable.run(new Message<T>(messageExt.getMsgId(),messageExt.getTopic(),messageExt.getTags(),
                         new JsonSerializer().deserialize(msg,type)));
             }
             return null;
         });
    }
    /**
     * 	校验过滤器
     * */
    private String volidateFilters(String[] filters) {
        String[] filterTags=Optional.ofNullable(filters).orElse(new String[] {});
        String filter=StringUtils.join(filterTags);
        if(StringUtils.isNotBlank(filter)) {
            return filter;
        }
        return "*";
    }

}
