package com.github.jgzl.bsf.client;

import com.github.jgzl.bsf.core.config.BsfConfiguration;
import com.github.jgzl.bsf.core.util.LogUtils;
import com.github.jgzl.bsf.client.format.DateFormatRegister;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

/**
 * @author: lihaifeng
 * @version: 2019-06-15 14:50
 **/
@Configuration
@ConditionalOnProperty(name = "bsf.eureka.client.enabled", havingValue = "true")
@Import(BsfConfiguration.class)
@EnableConfigurationProperties({EurekaProperties.class,KongProperties.class})
@EnableEurekaClient
public class EurekaClientConfiguration implements InitializingBean {
    @Autowired
    EurekaProperties eurekaProperties;

    @Override
    public void afterPropertiesSet() {
        LogUtils.info(EurekaClientConfiguration.class,EurekaProperties.ClientProject,"已启动!!!"+" "+EurekaProperties.EurekaClientServiceUrlDefaultZone+"="+ eurekaProperties.getDefaultZone());
    }
    @Bean
    @ConditionalOnClass(name = "com.yh.csx.bsf.health.collect.HealthCheckProvider")
    @Lazy
    public EurekaMonitor eurekaClientMonitor(){
        return new EurekaMonitor();

    }
    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnProperty(name = "bsf.eureka.kong.enabled", havingValue = "true")
    public KongCheckHealthProvider kongCheckHealthProvider(KongProperties kongProperties){
        return new KongCheckHealthProvider(kongProperties);
    }
    @Bean
    @ConditionalOnProperty(name = "bsf.fegin.dateformat.enabled", havingValue = "true",matchIfMissing=true)
    public DateFormatRegister dateFormatRegister(){
        return new DateFormatRegister();
    }
}
