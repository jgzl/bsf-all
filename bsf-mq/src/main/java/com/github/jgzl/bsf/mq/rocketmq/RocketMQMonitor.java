package com.github.jgzl.bsf.mq.rocketmq;

import com.github.jgzl.bsf.core.common.Collector;

/**
 * @author Huang Zhaoping
 */
public class RocketMQMonitor {
    private static String name="rocketmq.info";

    public static Collector.Hook hook(){
        return Collector.Default.hook(name+".hook");
    }
}
