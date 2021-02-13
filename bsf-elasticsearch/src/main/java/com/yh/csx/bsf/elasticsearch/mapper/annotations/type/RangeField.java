package com.yh.csx.bsf.elasticsearch.mapper.annotations.type;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.yh.csx.bsf.elasticsearch.mapper.enums.RangeType;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface RangeField {
    /**
     * type of range
     * <p>
     * {@link RangeType}
     */
    RangeType type();

    /**
     * The date format(s) that can be parsed. Defaults to strict_date_optional_time||epoch_millis.
     * <p>
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-date-format.html
     */
    String format() default "strict_date_optional_time||epoch_millis";

    /**
     * Try to convert strings to numbers and truncate fractions for integers.
     * Accepts true (default) and false.
     */
    boolean coerce() default true;

    /**
     * Mapping field-level query time boosting. Accepts a floating point number, defaults to 1.0.
     */
    float boost() default 1.0f;

    /**
     * Should the field be searchable? Accepts true (default) or false.
     */
    boolean index() default true;


    /**
     * Whether the field value should be stored and retrievable separately from the _source field.
     * Accepts true or false (default).
     */
    boolean store() default false;
}