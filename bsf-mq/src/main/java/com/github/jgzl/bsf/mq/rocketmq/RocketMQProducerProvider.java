package com.github.jgzl.bsf.mq.rocketmq;

import com.github.jgzl.bsf.core.serialize.JsonSerializer;

import java.util.Optional;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.jgzl.bsf.core.util.LogUtils;
import com.github.jgzl.bsf.mq.base.AbstractProducer;
import com.github.jgzl.bsf.mq.base.AbstractProducerProvider;
import com.github.jgzl.bsf.mq.base.MQException;

/**
 * @author: lihaifeng
 * @version: 2019-06-12 13:01
 * rocketmq生产者提供使用提供类
 **/
public class RocketMQProducerProvider extends AbstractProducerProvider {
    private static String DEFAULT_PRODUCER="csx-mq-producer";
    protected volatile DefaultMQProducer object;
    protected AbstractProducer producer;
    protected Object _lock = new Object();

    @Autowired
    RocketMQProperties rocketMQProperties;

    public DefaultMQProducer getProducer()
    {
        if(object == null) {
            synchronized(_lock) {
                if(object == null) {
                    try {
                        DefaultMQProducer mqProducer = new DefaultMQProducer(RocketMQProducerProvider.DEFAULT_PRODUCER);
                        mqProducer.setNamesrvAddr(rocketMQProperties.getNamesrvaddr());
                        mqProducer.setVipChannelEnabled(
                                   Optional.ofNullable(rocketMQProperties.getIsUseVIPChannel()).orElse(false));
                        mqProducer.start();
                        object=mqProducer;
                        producer = new AbstractProducer();
                        producer.setObject(object);
                        LogUtils.info(RocketMQProducerProvider.class,RocketMQProperties.Project,"生产者初始化成功");
                    }
                    catch (Exception exp)
                    {
                        LogUtils.error(RocketMQProducerProvider.class,RocketMQProperties.Project,"生产者初始化失败",exp);
                        this.close();
                        throw new MQException(exp);
                    }
                }
            }
        }
        return object;
    }

	@Override
	public <T> AbstractProducer sendMessage(String topic, String tag, T msg) {
        return RocketMQMonitor.hook().run("produce", ()->{
            try {
                String msgJson = null;
                if (msg instanceof String) {
                    msgJson = (String) msg;
                } else {
                    msgJson = new JsonSerializer().serialize(msg);
                }
                Message message = new Message(topic, Optional.ofNullable(tag).orElse(""), 
                		msgJson.getBytes(RemotingHelper.DEFAULT_CHARSET));
                getProducer().send(message);
                return producer;
            } catch (Exception exp) {
                LogUtils.error(RocketMQProducerProvider.class,RocketMQProperties.Project,"生产者消息发送失败",exp);
                throw new MQException(exp);
            }
        });
	}

	@Override
    public void close() {
        try {
            if (object != null) {
                object.shutdown();
                object = null;
                LogUtils.info(RocketMQProducerProvider.class,RocketMQProperties.Project,"生产者资源释放成功");
            }
            super.close();
        }
        catch (Exception exp)
        {
            LogUtils.error(RocketMQProducerProvider.class,RocketMQProperties.Project,"生产者资源释放失败",exp);
        }
    }
}
