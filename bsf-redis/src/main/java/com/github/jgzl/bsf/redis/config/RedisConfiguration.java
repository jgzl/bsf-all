package com.github.jgzl.bsf.redis.config;

import com.github.jgzl.bsf.core.config.BsfConfiguration;
import com.github.jgzl.bsf.core.base.BsfEnvironmentEnum;
import com.github.jgzl.bsf.core.util.LogUtils;
import com.github.jgzl.bsf.core.util.PropertyUtils;
import com.github.jgzl.bsf.core.util.StringUtils;
import com.github.jgzl.bsf.redis.RedisException;
import com.github.jgzl.bsf.redis.RedisProvider;
import com.github.jgzl.bsf.redis.annotation.RedisCacheAspect;
import com.github.jgzl.bsf.redis.impl.ClusterRedisProvider;
import com.github.jgzl.bsf.redis.impl.RedisClusterMonitor;
import com.github.jgzl.bsf.redis.impl.RedisLockMonitor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


/**
 * @author Huang Zhaoping
 **/
@Configuration
@Import(BsfConfiguration.class)
@EnableConfigurationProperties(RedisProperties.class)
@ConditionalOnProperty(name = "bsf.redis.enabled", havingValue = "true")
public class RedisConfiguration implements InitializingBean, DisposableBean {

    @Override
    public void afterPropertiesSet() {
        LogUtils.info(RedisConfiguration.class,RedisProperties.Project,"已启动,addressList:"+ StringUtils.nullToEmpty(PropertyUtils.getPropertyCache(RedisProperties.BsfRedisNodes,"")));
    }

    @Override
    public void destroy() throws Exception {
        RedisLockMonitor.destroy();
    }

    @Bean(destroyMethod = "close")
    @Lazy
    public JedisCluster jedisCluster(RedisProperties properties) {
        List<String> nodes = properties.getNodes();
        if (nodes == null || nodes.isEmpty()) {
            String defaultNodes = PropertyUtils.getPropertyCache(BsfEnvironmentEnum.REDIS_DEV.getServerkey(),"");
            if (defaultNodes == null || (defaultNodes = defaultNodes.trim()).length() == 0) {
                throw new RedisException("缺少bsf.redis.nodes配置");
            }
            nodes = Arrays.asList(defaultNodes.split("\\s*,\\s*"));
        }
        Set<HostAndPort> addressList = new LinkedHashSet<>();
        for (String node : nodes) {
            addressList.add(HostAndPort.parseString(node));
        }
        return new JedisCluster(addressList, properties.getConnectTimeout(), properties.getSoTimeout(), properties.getMaxAttempts(), properties.getPassword(), properties.getClientName(), properties.getPool());
    }

    @Lazy
    @Bean
    public RedisProvider redisProvider(JedisCluster jedisCluster, RedisProperties properties) {
        String keyPrefix = properties.getKeyPrefix();
        String springAppName = PropertyUtils.getPropertyCache(RedisProperties.SpringApplicationName,"");
        if (keyPrefix == null || keyPrefix.length() == 0) {
            // 根据spring.application.name设置默认值
            if (springAppName != null && springAppName.length() > 0) {
                keyPrefix = springAppName.replace("csx-", "").replace("-provider", "");
            }
        }
        ClusterRedisProvider service = new ClusterRedisProvider(jedisCluster);
        service.setKeyPrefix(keyPrefix);
        return service;
    }

    @Bean
    public RedisCacheAspect redisCacheAspect(RedisProvider redisProvider) {
        return new RedisCacheAspect(redisProvider);
    }

    @Lazy
    @ConditionalOnBean(RedisProvider.class)
    @Bean
    public RedisClusterMonitor redisClusterMonitor(RedisProvider redisProvider){
        if(redisProvider instanceof ClusterRedisProvider)
        { return ((ClusterRedisProvider)redisProvider).getRedisMonitor();}
        else
        {  return null;}
    }
}
