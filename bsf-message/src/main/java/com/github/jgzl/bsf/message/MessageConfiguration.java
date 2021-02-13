package com.github.jgzl.bsf.message;

import com.github.jgzl.bsf.core.util.LogUtils;
import com.github.jgzl.bsf.message.dingding.DingdingConfiguration;
import com.github.jgzl.bsf.message.flybook.FlyBookConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author: lihaifeng
 * @version: 2019-05-31 13:18
 **/
@Configuration
@ConditionalOnProperty(name = "bsf.message.enabled", havingValue = "true")
@EnableConfigurationProperties({MessageProperties.class})
@Import({DingdingConfiguration.class, FlyBookConfiguration.class})
public class MessageConfiguration implements InitializingBean {

    @Override
    public void afterPropertiesSet(){
        LogUtils.info(MessageConfiguration.class,MessageProperties.Project,"已启动!!!");
    }
}
