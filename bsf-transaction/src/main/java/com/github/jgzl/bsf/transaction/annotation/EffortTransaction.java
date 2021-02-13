package com.github.jgzl.bsf.transaction.annotation;

import com.github.jgzl.bsf.core.base.EtTime;

import java.lang.annotation.*;
/**
 * @author huojuncheng
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EffortTransaction {
    EtTime[] value() default {};
}
