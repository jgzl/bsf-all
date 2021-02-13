package com.yh.csx.bsf.elasticsearch.mapper.annotations.type;

import java.lang.annotation.*;

/**
 * https://www.elastic.co/guide/en/elasticsearch/reference/current/geo-point.html
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface GeoPointField {
    /**
     * If true, malformed geo-points are ignored.
     * If false (default), malformed geo-points throw an exception and reject the whole document.
     */
    boolean ignore_malformed() default false;
}