package com.github.jgzl.bsf.client;

import com.github.jgzl.bsf.core.base.BsfEnvironmentEnum;
import com.github.jgzl.bsf.core.config.CoreProperties;
import com.github.jgzl.bsf.core.util.PropertyUtils;
import com.github.jgzl.bsf.client.timeout.FeginRibbonTimeoutExtend;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

/**
 * Eureka 注册
 * @author: lihaifeng
 * @version: 2019-05-28 12:08
 **/
public class EurekaApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        if("false".equalsIgnoreCase(environment.getProperty(CoreProperties.BsfEnabled))){
            return;
        }
        this.initializeSystemProperty(environment);
    }

    void initializeSystemProperty(ConfigurableEnvironment environment) {
        String propertyValue = environment.getProperty(EurekaProperties.StringApplicationName);
        if(propertyValue==null)
        {   return; }

        propertyValue = environment.getProperty(EurekaProperties.BsfEurekaClientEnabled);
        if (StringUtils.isEmpty(propertyValue)|| "false".equalsIgnoreCase(propertyValue)) {
            {
                setDefaultProperty(EurekaProperties.EurekaClientEnabled,"false","关闭eureka client启动");
                return;
            }
        }
        setDefaultProperty(EurekaProperties.EurekaClientServiceUrlDefaultZone, environment.getProperty(BsfEnvironmentEnum.EUREKA_DEV.getServerkey(),""),"");
        setDefaultProperty(EurekaProperties.EurekaInstanceLeaseExpirationDurationInSencodes,"5","[eureka server调优]");
        setDefaultProperty(EurekaProperties.EurekaInstanceLeaseRenwalIntervalInSeconds,"2","[eureka server调优]");
        setDefaultProperty(EurekaProperties.EurekaInstancePerferIpAddress,"true","[eureka server调优]");
        setDefaultProperty(EurekaProperties.SpringMainAllowBeanDefinitionOverriding,"true","");
        setDefaultProperty(EurekaProperties.RibbonReadTimeOut,"30000","[eureka client调优]");
        setDefaultProperty(EurekaProperties.RibbonConnectTimeOut,"30000","[eureka client调优]");
//        propertyValue = environment.getProperty("hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds");
//        if(StringUtils.isEmpty(propertyValue)) {
//            setProperty("hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds","30000");
//        }
//        propertyValue = environment.getProperty("eureka.client.healthcheck.enabled");
//        if(StringUtils.isEmpty(propertyValue)) {
//            setProperty("eureka.client.healthcheck.enabled","true");
//        }
        EurekaMonitor.init();
        FeginRibbonTimeoutExtend.setTimeoutHook();
    }

    void setDefaultProperty(String key,String defaultPropertyValue,String message)
    {
        PropertyUtils.setDefaultInitProperty(EurekaApplicationContextInitializer.class,EurekaProperties.Project,key,defaultPropertyValue,message);
    }
}
