package com.github.jgzl.bsf.core.config;

import com.github.jgzl.bsf.core.common.PropertyCache;
import com.github.jgzl.bsf.core.http.DefaultHttpClient;
import com.github.jgzl.bsf.core.http.HttpClientProperties;
import com.github.jgzl.bsf.core.initializer.CoreApplicationRunner;
import com.github.jgzl.bsf.core.thread.ThreadMonitor;
import com.github.jgzl.bsf.core.thread.ThreadPool;
import com.github.jgzl.bsf.core.thread.ThreadPoolProperties;
import lombok.Getter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

/**
 * @author: chejiangyi
 * @version: 2019-05-27 11:19
 **/
@Configuration
@Import(WebConfiguration.class)
@EnableConfigurationProperties({CoreProperties.class,HttpClientProperties.class, ThreadPoolProperties.class})
@Getter
public class BsfConfiguration {

    @Bean(initMethod = "close")
    @Lazy
    public DefaultHttpClient getDefaultHttpClient()
    {
        if(DefaultHttpClient.Default==null||DefaultHttpClient.Default.isClose()){
             DefaultHttpClient.initDefault();
        }
        return DefaultHttpClient.Default;
    }
    @Bean(initMethod = "shutdown")
    @Lazy
    public ThreadPool getSystemThreadPool()
    {
        if(ThreadPool.System==null||ThreadPool.System.isShutdown()){
            ThreadPool.initSystem();
        }
        return ThreadPool.System;
    }
    @Bean
    @ConditionalOnBean(ThreadPool.class)
    @Lazy
    public ThreadMonitor getSystemThreadPoolMonitor()
    {
        return ThreadPool.System.getThreadMonitor();
    }
    @Bean
    @Lazy
    public PropertyCache getPropertyCache(){
        PropertyCache.Default.clear();
        return PropertyCache.Default;
    }

    @Bean
    public CoreApplicationRunner coreApplicationRunner(){
        return new CoreApplicationRunner();
    }
}
