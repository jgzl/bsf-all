package com.github.jgzl.bsf.message.base;

import com.github.jgzl.bsf.core.base.BsfException;

/**
 * @author: lihaifeng
 * @version: 2019-06-13 17:45
 **/
public class MessageException extends BsfException {
    public MessageException(Exception exp)
    {
        super(exp);
    }
    public MessageException(String message)
    {
        super(message);
    }
}
