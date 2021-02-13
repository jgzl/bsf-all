package com.github.jgzl.bsf.client.timeout;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FeginRibbonTimeout {
    /**
     * 连接超时时间
     * @return
     */
    int connectTimeout() default 60000;

    /**
     * 传输超时时间
     * @return
     */
    int readTimeout() default 90000;
}
