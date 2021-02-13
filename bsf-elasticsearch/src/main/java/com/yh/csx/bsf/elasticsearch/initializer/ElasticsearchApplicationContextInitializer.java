package com.yh.csx.bsf.elasticsearch.initializer;

import com.github.jgzl.bsf.core.config.CoreProperties;
import com.github.jgzl.bsf.core.util.PropertyUtils;
import com.yh.csx.bsf.elasticsearch.ElasticSearchProperties;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public class ElasticsearchApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        if("false".equalsIgnoreCase(environment.getProperty(CoreProperties.BsfEnabled))){
            return;
        }
        String propertyValue = environment.getProperty(ElasticSearchProperties.SpringApplicationName);
        if (!Strings.isEmpty(propertyValue)) {
            PropertyUtils.setDefaultInitProperty(ElasticsearchApplicationContextInitializer.class,ElasticSearchProperties.Project,ElasticSearchProperties.ManagementHealthElasticSearchEnabled,"false");
        }
    }
}
