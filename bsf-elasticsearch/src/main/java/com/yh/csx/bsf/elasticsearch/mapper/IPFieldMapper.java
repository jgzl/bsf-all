package com.yh.csx.bsf.elasticsearch.mapper;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;

import com.yh.csx.bsf.elasticsearch.base.ElasticSearchException;
import com.yh.csx.bsf.elasticsearch.mapper.annotations.type.IPField;
import com.yh.csx.bsf.elasticsearch.mapper.base.ElasticSearchMapper;

public class IPFieldMapper implements ElasticSearchMapper {

	@Override
	public boolean isValidType(Field field) {
		Class<?> fieldClass = field.getType();
		return String.class.isAssignableFrom(fieldClass);
	}

	@Override
	public void mapDataType(XContentBuilder mappingBuilder, Field field) throws IOException {
		if (!isValidType(field)) {
			throw new ElasticSearchException(String.format("field type[%s] is invalid type of ip.", field.getType()));
		}

		IPField ipField = field.getDeclaredAnnotation(IPField.class);
		mappingBuilder.field("type", "ip");

        if (ipField.boost() != 1.0f) {
            mappingBuilder.field("boost", ipField.boost());
        }

        if (!ipField.doc_values()) {
            mappingBuilder.field("doc_values", ipField.doc_values());
        }

        if (!ipField.index()) {
            mappingBuilder.field("index", ipField.index());
        }

        if (StringUtils.isNotBlank(ipField.null_value())) {
            mappingBuilder.field("null_value", ipField.null_value());
        }

        if (ipField.store()) {
            mappingBuilder.field("store", ipField.store());
        }
	}

	@Override
	public Class<? extends Annotation> getAnnotationType() {
		return IPField.class;
	}
}
