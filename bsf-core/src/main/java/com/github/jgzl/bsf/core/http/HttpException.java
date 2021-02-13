package com.github.jgzl.bsf.core.http;

import com.github.jgzl.bsf.core.base.BsfException;

/**
 * @author: lihaifeng
 * @version: 2019-07-22 20:34
 **/
public class HttpException extends BsfException {
    public HttpException(Throwable exp)
    {
        super(exp);
    }

    public HttpException(String message)
    {
        super(message);
    }

    public HttpException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
