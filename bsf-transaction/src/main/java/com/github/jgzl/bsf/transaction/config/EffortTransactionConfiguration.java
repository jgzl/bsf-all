package com.github.jgzl.bsf.transaction.config;

import com.github.jgzl.bsf.core.config.BsfConfiguration;
import com.github.jgzl.bsf.core.util.ContextUtils;
import com.github.jgzl.bsf.core.util.JsonUtils;
import com.github.jgzl.bsf.core.util.LogUtils;
import com.github.jgzl.bsf.core.util.PropertyUtils;
import com.github.jgzl.bsf.mq.rocketmq.RocketMQConsumerProvider;
import com.github.jgzl.bsf.mq.rocketmq.RocketMQProducerProvider;
import com.github.jgzl.bsf.redis.RedisProvider;
import com.github.jgzl.bsf.transaction.annotation.EffortTransactionAspect;
import com.github.jgzl.bsf.transaction.base.EffortThreadLocal;
import com.github.jgzl.bsf.transaction.base.EffortTransactionObject;
import io.netty.util.internal.StringUtil;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author huojuncheng
 */
@Configuration
@Import(BsfConfiguration.class)
@ConditionalOnProperty(name = "bsf.transaction.effort.enabled", havingValue = "true")
public class EffortTransactionConfiguration  implements InitializingBean {

    private RocketMQConsumerProvider rocketMQConsumerProvider;

    private RedisProvider redisProvider;


    @Override
    public void afterPropertiesSet(){
        if(startEffortTransaction()){
            LogUtils.info(EffortTransactionConfiguration.class, EffortTransactionProperties.project, "已启动");
        }else{
            LogUtils.error(EffortTransactionConfiguration.class, EffortTransactionProperties.project, "启动异常",null);
        }
    }

    @Bean
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    @ConditionalOnExpression("${bsf.mq.enabled:true}&&${bsf.redis.enabled:true}")
    public EffortTransactionAspect effortTransactionAspect() {
        return new EffortTransactionAspect();
    }

    private boolean startEffortTransaction() {
        rocketMQConsumerProvider = ContextUtils.getBean(RocketMQConsumerProvider.class, false);
        if (rocketMQConsumerProvider == null) {
            LogUtils.error(EffortTransactionConfiguration.class, EffortTransactionProperties.project, "设置bsf.mq.enabled = true", null);
            return false;
        }
        redisProvider = ContextUtils.getBean(RedisProvider.class, false);
        if (redisProvider == null) {
            LogUtils.error(EffortTransactionConfiguration.class, EffortTransactionProperties.project, "设置bsf.redis.enabled = true", null);
            return false;
        }
        rocketMQConsumerProvider.subscribe(group(), EffortTransactionProperties.topic,
                new String[]{appName().concat(EffortTransactionProperties.tagBsf)}, msg -> {
                    EffortTransactionObject eObject = msg.getData();
                    redisProvider.tryLock(eObject.getUuid(),
                            EffortTransactionProperties.locktime, () -> {
                                int currentTimes=eObject.getCurrentTimes();
                                try {
                                    EffortThreadLocal.getInstance().set(eObject);
                                    invoke(eObject.getClassName(), eObject.getMethod(), eObject.getParam());
                                    LogUtils.info(EffortTransactionConfiguration.class, EffortTransactionProperties.project,
                                            "["+eObject.getClassName()+"."+eObject.getMethod()+"]重试["+currentTimes+"]成功");
                                    eObject.setStatus(EffortTransactionProperties.executeSucess);
                                    eObject.setTag(EffortTransactionProperties.tagElse);
                                    producerMsg(eObject);
                                } catch (Exception e) {
                                    if(currentTimes==eObject.getReconsumeTimes()){
                                        eObject.setStatus(EffortTransactionProperties.executeFail);
                                        eObject.setTag(EffortTransactionProperties.tagElse);
                                        producerMsg(eObject);
                                    }
                                    LogUtils.error(EffortTransactionConfiguration.class, EffortTransactionProperties.project,
                                            "["+eObject.getClassName()+"."+eObject.getMethod()+"]重试["+currentTimes+"]异常", e);
                                } finally {
                                    EffortThreadLocal.getInstance().remove();
                                }
                            });
                }, EffortTransactionObject.class);
        return true;
    }

    private void producerMsg(EffortTransactionObject eObject){
        try {
            RocketMQProducerProvider provider = ContextUtils.getBean(RocketMQProducerProvider.class, true);
            String appName = PropertyUtils.getPropertyCache(EffortTransactionProperties.springApplicationName, StringUtil.EMPTY_STRING);
            Message message = new Message(EffortTransactionProperties.topic, appName.concat(eObject.getTag()),
                    JsonUtils.serialize(eObject).getBytes("UTF-8"));
            message.setDelayTimeLevel(1);
            provider.getProducer().setRetryTimesWhenSendFailed(3);
            provider.getProducer().send(message);
        } catch (Exception e) {}
    }
    private Object invoke(String className, String methodName, String param[]) throws Exception {
        Class cls = Class.forName(className);
        Object bean = ContextUtils.getBean(cls, true);
        for (Method method : cls.getDeclaredMethods()) {
            if (methodName.equals(method.getName())) {
                Object[] paramObject = getParameters(param, method.getParameterTypes());
                return paramObject == null ? method.invoke(bean) : method.invoke(bean, paramObject);
            }
        }
        return null;
    }

    private Object[] getParameters(String[] param, Type[] types) {
        if (param == null || param.length == 0) {
            return null;
        }
        Object[] paramObject = new Object[param.length];
        for (int i = 0; i < param.length; i++) {
            paramObject[i] = JsonUtils.deserialize(param[i], types[i]);
        }
        return paramObject;
    }

    private String group() {
        String profiles = PropertyUtils.getPropertyCache(EffortTransactionProperties.springProActive, StringUtil.EMPTY_STRING);
        return appName().concat("-").concat(profiles).concat("-GROUP").toUpperCase();
    }

    private String appName() {
        return PropertyUtils.getPropertyCache(EffortTransactionProperties.springApplicationName, StringUtil.EMPTY_STRING);
    }
}