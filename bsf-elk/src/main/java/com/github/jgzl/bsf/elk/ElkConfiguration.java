package com.github.jgzl.bsf.elk;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.Encoder;

import com.github.jgzl.bsf.core.base.BsfEnvironmentEnum;
import com.github.jgzl.bsf.core.config.BsfConfiguration;
import com.github.jgzl.bsf.core.util.LogUtils;
import com.github.jgzl.bsf.core.util.PropertyUtils;
import com.github.jgzl.bsf.elk.requestid.RequestUtil;

import lombok.var;
import net.logstash.logback.appender.LogstashTcpSocketAppender;
import net.logstash.logback.encoder.LogstashEncoder;
import java.util.Arrays;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @version : 2019-05-27 14:30
 * @author: lihaifeng * 
 * @date 2020-01-15
 **/
@Configuration
@EnableConfigurationProperties(ElkProperties.class)
@ConditionalOnProperty(name = "bsf.elk.enabled", havingValue = "true")
@Import(value = { BsfConfiguration.class })
public class ElkConfiguration {

	@Autowired
	ElkProperties elkProperties;
	@Autowired(required = false)
	LogStatisticsFilter logStatisticsFilter;

	private Encoder<ILoggingEvent> createEncoder() {
		LogstashEncoder encoder = new LogstashEncoder();
		var appName = elkProperties.getAppName();
		if (elkProperties.getAppName().length() == 0) {
			appName = elkProperties.getSpringAppName();
		}
		if (appName.length() == 0) {
			throw new IllegalArgumentException("缺少appName配置");
		}
		encoder.setIncludeMdc(true);
		encoder.setIncludeMdcKeyNames(Arrays.asList(new String[] { RequestUtil.REQUEST_TRACEID }));
		encoder.setCustomFields("{\"appname\":\"" + appName + "\",\"appindex\":\"applog\"}");
		encoder.setEncoding("UTF-8");
		return encoder;
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	public LogstashTcpSocketAppender logstashTcpSocketAppender() {
		LogstashTcpSocketAppender appender = new LogstashTcpSocketAppender();
		var destinations = elkProperties.getDestinations();
		if (elkProperties.getDestinations() == null || elkProperties.getDestinations().length == 0) {
			destinations = new String[] {
					PropertyUtils.getPropertyCache(BsfEnvironmentEnum.ELK_DEV.getServerkey(), "") };
		}
		for (String destination : destinations) {
			appender.addDestination(destination);
		}

		appender.setEncoder(createEncoder());

		ILoggerFactory factory = LoggerFactory.getILoggerFactory();
		if (factory instanceof LoggerContext) {
			LoggerContext context = ((LoggerContext) factory);
			appender.setContext(context);

			context.getLogger("ROOT").addAppender(appender);
			LogUtils.info(ElkConfiguration.class, ElkProperties.Project,
					"已启动!!!" + " " + ElkProperties.Destinations + "=" + String.join(",", destinations));
		}
		if (logStatisticsFilter != null) {
			// 增加错误日志统计拦截
			appender.addFilter(logStatisticsFilter);
		}
		return appender;
	}

	@Bean
	@ConditionalOnProperty(name = "bsf.health.log.statistic.enabled", havingValue = "true")
	LogStatisticsFilter getLogStatisticsFilter() {
		return new LogStatisticsFilter();
	}



}
