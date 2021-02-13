package com.github.jgzl.bsf.mq.base;

import com.github.jgzl.bsf.core.base.BsfException;

/**
 * @author: lihaifeng
 * @version: 2019-06-12 14:35
 * 消息队列的异常
 **/
public class MQException extends BsfException {
    public MQException(Exception exp)
    {
        super(exp);
    }
}
