package com.github.jgzl.bsf.health.initializer;

import com.github.jgzl.bsf.health.config.HealthProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

/**
 * @author: lihaifeng
 * @version: 2019-05-28 12:08
 **/
public class HealthApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        this.initializeSystemProperty(environment);
    }

    void initializeSystemProperty(ConfigurableEnvironment environment) {
        String propertyValue = environment.getProperty(HealthProperties.SpringApplictionName);
        if(propertyValue==null)
        {   return; }

        propertyValue = environment.getProperty(HealthProperties.BsfHealthEnabled);
        if (StringUtils.isEmpty(propertyValue)|| "false".equalsIgnoreCase(propertyValue)) {
            {
                return;
            }
        }
        //注入eureka
    }
}
