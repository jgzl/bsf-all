package com.github.jgzl.bsf.mq;

import com.github.jgzl.bsf.core.util.LogUtils;
import com.github.jgzl.bsf.mq.rocketmq.RocketMQConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author: lihaifeng
 * @version: 2019-06-12 12:02
 **/
@Configuration
@ConditionalOnProperty(name = "bsf.mq.enabled", havingValue = "true")
@EnableConfigurationProperties(MQProperties.class)
@Import(RocketMQConfiguration.class)
public class MQConfiguration implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        LogUtils.info(MQConfiguration.class,MQProperties.Project,"已启动!!!");
    }

}
