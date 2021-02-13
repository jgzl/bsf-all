# 努力送达事务

## 介绍
本系统集成了 REDIS & MQ，增加方法努力送达注解
1、注解提供了异常方法监控和自定义重试策略
2、方法需要满足幂等性



## 配置说明

```shell
##配置如下
#transaction.effort启用开关
bsf.mq.enabled=true
bsf.redis.enabled=true
bsf.transaction.effort.enabled=true
```


## 代码示例

```java
    @EffortTransaction({EtTime.S01, EtTime.S05})
    public void purchaseGroupAuths(Long userId) {
    }
    
    @EffortTransaction()
    public void groupAuths(Long userId) {
    
    }
 ```