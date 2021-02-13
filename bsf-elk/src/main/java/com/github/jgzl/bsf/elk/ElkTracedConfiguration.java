package com.github.jgzl.bsf.elk;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.github.jgzl.bsf.elk.requestid.ElkWebTracedInterceptor;
import com.github.jgzl.bsf.elk.requestid.FeginRequestInterceptor;

/**
 * ELK 调用链日志配置类
 * @author Robin.Wang
 * @date	2020-04-30
 * */
@Configuration
@ConditionalOnProperty(name = "bsf.elk.request.traced.enabled", havingValue = "true")
public class ElkTracedConfiguration {

	@Bean	
	@ConditionalOnClass(name="feign.RequestInterceptor")
	public FeginRequestInterceptor feginRequestInterceptor(){
		return new FeginRequestInterceptor();
	}
	
	@Bean
	@ConditionalOnClass(name = { "org.aspectj.lang.annotation.Aspect", "com.xxl.job.core.executor.XxlJobExecutor" })
	public ElkXxlJobTaskAspect elkXxlJobTaskAspect() {
		return new ElkXxlJobTaskAspect();
	}
	
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	@ConditionalOnWebApplication
	public ElkWebTracedInterceptor elkWebTracedInterceptor(){
		return new ElkWebTracedInterceptor();
	}
}
