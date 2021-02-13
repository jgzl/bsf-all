package com.github.jgzl.bsf.elk;

import org.aspectj.lang.ProceedingJoinPoint;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import com.github.jgzl.bsf.elk.requestid.RequestUtil;
/**
 * xxljob 定时任务切面追加RequestUtil.REQUEST_TRACEID 设置
 * @author Robin.Wang
 * @date	2020-04-20
 * 
 * */
@Aspect
public class ElkXxlJobTaskAspect {
    @Pointcut("@within(com.xxl.job.core.handler.annotation.JobHandler) && execution(* execute(..))")
    public void pointcut() {
    }
    
    @Around("pointcut()")
    public Object handle(ProceedingJoinPoint joinPoint) throws Throwable { 
    	MDC.put(RequestUtil.REQUEST_TRACEID, RequestUtil.getRequestId());
    	Object result= joinPoint.proceed();
    	MDC.clear();
    	return result;
    }

}
