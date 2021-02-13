package com.yh.csx.bsf.elasticsearch.mapper;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.elasticsearch.common.xcontent.XContentBuilder;

import com.yh.csx.bsf.elasticsearch.mapper.annotations.type.RangeField;
import com.yh.csx.bsf.elasticsearch.mapper.base.ElasticSearchMapper;
import com.yh.csx.bsf.elasticsearch.mapper.enums.RangeType;

public class RangeFieldMapper implements ElasticSearchMapper {

	@Override
	public boolean isValidType(Field field) {
		return true;
	}

	@Override
	public void mapDataType(XContentBuilder mappingBuilder, Field field) throws IOException {
		RangeField rangeField = field.getDeclaredAnnotation(RangeField.class);
		mappingBuilder.field("type", rangeField.type().code());

        if (rangeField.type() == RangeType.DateRange) {
            mappingBuilder.field("format", rangeField.format());
        }

        if (!rangeField.coerce()) {
            mappingBuilder.field("coerce", rangeField.coerce());
        }

        if (rangeField.boost() != 1.0f) {
            mappingBuilder.field("boost", rangeField.boost());
        }

        if (!rangeField.index()) {
            mappingBuilder.field("index", rangeField.index());
        }

        if (rangeField.store()) {
            mappingBuilder.field("store", rangeField.store());
        }
	}

	@Override
	public Class<? extends Annotation> getAnnotationType() {
		return RangeField.class;
	}
}
