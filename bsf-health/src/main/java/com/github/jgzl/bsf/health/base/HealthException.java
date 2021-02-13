package com.github.jgzl.bsf.health.base;

import com.github.jgzl.bsf.core.base.BsfException;

/**
 * @author: lihaifeng
 * @version: 2019-07-24 15:33
 **/
public class HealthException extends BsfException {
    public HealthException(Throwable exp)
    {
        super(exp);
    }

    public HealthException(String message)
    {
        super(message);
    }

    public HealthException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
