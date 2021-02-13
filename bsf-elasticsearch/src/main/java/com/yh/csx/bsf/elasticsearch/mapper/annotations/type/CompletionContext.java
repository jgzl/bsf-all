package com.yh.csx.bsf.elasticsearch.mapper.annotations.type;

public @interface CompletionContext {

    String name();

    String type() default "category";

    String path() default "";
}
