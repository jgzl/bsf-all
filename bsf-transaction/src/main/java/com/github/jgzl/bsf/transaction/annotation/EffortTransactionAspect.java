package com.github.jgzl.bsf.transaction.annotation;

import com.github.jgzl.bsf.core.base.EtTime;
import com.github.jgzl.bsf.core.util.ContextUtils;
import com.github.jgzl.bsf.core.util.JsonUtils;
import com.github.jgzl.bsf.core.util.PropertyUtils;
import com.github.jgzl.bsf.mq.rocketmq.RocketMQProducerProvider;
import com.github.jgzl.bsf.transaction.base.EffortThreadLocal;
import com.github.jgzl.bsf.transaction.base.EffortTransactionObject;
import com.github.jgzl.bsf.transaction.config.EffortTransactionProperties;
import io.netty.util.internal.StringUtil;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * @author huojuncheng
 */
@Aspect
public class EffortTransactionAspect {

    @Pointcut("@annotation(com.github.jgzl.bsf.transaction.annotation.EffortTransaction)")
    public void effortTransaction() {
    }

    @Around("effortTransaction()")
    public void doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            joinPoint.proceed();
        } catch (Exception e) {
            try {
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                Method method = signature.getMethod();
                EffortTransaction effortTransaction = method.getAnnotation(EffortTransaction.class);
                EffortTransactionObject obj = EffortThreadLocal.getInstance().get();
                EtTime[] etTimes=effortTransaction.value();
                if(etTimes.length>0){
                    if (obj == null) {
                        obj=setProperty(joinPoint.getArgs(),method,etTimes);
                        EffortThreadLocal.getInstance().set(obj);
                    } else {
                        obj.setDelayTimeLevel(getDelayTimeLevel(effortTransaction.value(), obj.getCurrentTimes()));
                    }
                    if (obj.getCurrentTimes() < obj.getReconsumeTimes()) {
                        obj.setCurrentTimes(obj.getCurrentTimes() + 1);
                        obj.setTag(EffortTransactionProperties.tagBsf);
                        obj.setStatus(EffortTransactionProperties.executeFail);
                        obj.setException(getExceptionInformation(e));
                        producerMsg(obj);
                    }
                }else{
                    if (obj == null) {
                        obj=setProperty(joinPoint.getArgs(),method,etTimes);
                        obj.setTag(EffortTransactionProperties.tagElse);
                        obj.setStatus(EffortTransactionProperties.executeFail);
                        obj.setException(getExceptionInformation(e));
                        producerMsg(obj);
                    }
                }

            } finally {
                EffortThreadLocal.getInstance().remove();
                throw e;
            }
        }
    }
    private EffortTransactionObject setProperty(Object[] params,Method method ,EtTime[] etTimes){
        EffortTransactionObject obj=new EffortTransactionObject();
        obj.setClassName(method.getDeclaringClass().getName());
        obj.setMethod(method.getName());
        obj.setParam(getParameters(params));
        obj.setReconsumeTimes(etTimes.length);
        obj.setUuid(UUID.randomUUID().toString());
        obj.setCurrentTimes(0);
        obj.setStartTime(new Date());
        obj.setEndTime(getEndTime(etTimes));
        if(etTimes.length>0){
            obj.setDelayTimeLevel(getDelayTimeLevel(etTimes, 0));
        }else{
            obj.setDelayTimeLevel(EtTime.S01.getCode());
        }
        return obj;
    }
    private  String getExceptionInformation(Exception ex){
        StringBuffer sOut = new StringBuffer();
        sOut.append(ex.getMessage() + "\r\n");
        StackTraceElement[] trace = ex.getStackTrace();
        for (StackTraceElement s : trace) {
            sOut.append("\tat " + s + "\r\n");
        }
        return sOut.toString();
    }
    private Date getEndTime(EtTime[] etTimes){
        int second = 0;
        for (EtTime etTime : etTimes) {
            second=second+etTime.getSecond()+EffortTransactionProperties.methodSecond;
        }
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.SECOND,second);
        return calendar.getTime();
    }
    private int getDelayTimeLevel(EtTime[] delayTimeLevel, int currentTimes) {
        return delayTimeLevel.length > currentTimes ? delayTimeLevel[currentTimes].getCode() :
                delayTimeLevel[delayTimeLevel.length - 1].getCode();
    }

    private String[] getParameters(Object[] paramValues) {
        if (paramValues == null || paramValues.length == 0) {
            return null;
        }
        String[] param = new String[paramValues.length];
        for (int i = 0; i < paramValues.length; i++) {
            param[i] = JsonUtils.serialize(paramValues[i]);
        }
        return param;
    }
    private void producerMsg(EffortTransactionObject eObject) throws InterruptedException, RemotingException, MQClientException,
            MQBrokerException, UnsupportedEncodingException {
        try {
            RocketMQProducerProvider provider = ContextUtils.getBean(RocketMQProducerProvider.class, true);
            String appName = PropertyUtils.getPropertyCache(EffortTransactionProperties.springApplicationName, StringUtil.EMPTY_STRING);
            Message message = new Message(EffortTransactionProperties.topic, appName.concat(eObject.getTag()),
                    JsonUtils.serialize(eObject).getBytes("UTF-8"));
            message.setDelayTimeLevel(eObject.getDelayTimeLevel());
            provider.getProducer().setRetryTimesWhenSendFailed(3);
            provider.getProducer().send(message);
        } catch (Exception e) {
            throw e;
        }
    }
}