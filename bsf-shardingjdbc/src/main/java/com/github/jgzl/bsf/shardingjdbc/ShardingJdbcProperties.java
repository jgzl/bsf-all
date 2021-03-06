package com.github.jgzl.bsf.shardingjdbc;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author: lihaifeng
 * @version: 2019-08-12 15:35
 **/
@ConfigurationProperties
public class ShardingJdbcProperties {
    public static String Project="Sharding-jdbc";
    public static String SpringShardingSphereEnabled="spring.shardingsphere.enabled";
    public static String SpringApplicationName="spring.application.name";
}
