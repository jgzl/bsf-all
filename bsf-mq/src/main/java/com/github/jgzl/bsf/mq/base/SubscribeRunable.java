package com.github.jgzl.bsf.mq.base;

/**
 * @author: lihaifeng
 * @version: 2019-06-12 15:34
 * 订阅回调接口
 **/
public interface SubscribeRunable<T> {
     void run(Message<T> msg);
}
