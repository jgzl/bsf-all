package com.github.jgzl.bsf.elk;

import com.github.jgzl.bsf.core.util.LogUtils;
import com.github.jgzl.bsf.elk.requestid.ElkWebTracedInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author: lihaifeng
 * @version : 2019-05-27 14:30
 * 
 **/
@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "bsf.elk.web.enabled", havingValue = "true")
public class ElkWebConfiguration implements WebMvcConfigurer {

	/**
	 * 	日志追踪拦截器
	 * */
	@Autowired(required=false)
	ElkWebTracedInterceptor elkWebTracedInterceptor;
	
    @Bean
    public ElkWebInterceptor elkWebInterceptor() {
        return new ElkWebInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        
        if(elkWebTracedInterceptor!=null) {
        	registry.addInterceptor(elkWebTracedInterceptor);
        }
        registry.addInterceptor(elkWebInterceptor());
        LogUtils.info(ElkWebConfiguration.class,ElkProperties.Project,"elk请求耗时拦截器注册成功");
    }

    @Bean
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    public WebControllerAspect webControllerAspect(){
        return new WebControllerAspect();
    }
    

	


}
