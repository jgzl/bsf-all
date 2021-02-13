package com.github.jgzl.bsf.message.flybook;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: huojuncheng
 * @version: 2020-08-25 15:10
 **/
@Configuration
@EnableConfigurationProperties(FlyBookProperties.class)
@ConditionalOnProperty(name = "bsf.message.flybook.enabled", havingValue = "true")
public class FlyBookConfiguration implements InitializingBean {

    @Override
    public void afterPropertiesSet(){

    }

    @Bean
    public FlyBookProvider getFlyBook()
    {
        return new FlyBookProvider();
    }
}
