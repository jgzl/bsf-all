package com.yh.csx.bsf.elasticsearch.mapper;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.elasticsearch.common.xcontent.XContentBuilder;

import com.yh.csx.bsf.elasticsearch.base.ElasticSearchException;
import com.yh.csx.bsf.elasticsearch.mapper.annotations.type.PercolatorField;
import com.yh.csx.bsf.elasticsearch.mapper.base.ElasticSearchMapper;

public class PercolatorFieldMapper implements ElasticSearchMapper {

	@Override
	public boolean isValidType(Field field) {
		Class<?> fieldClass = field.getType();
		return String.class.isAssignableFrom(fieldClass);
	}

	@Override
	public void mapDataType(XContentBuilder mappingBuilder, Field field) throws IOException {
		if (!isValidType(field)) {
			throw new ElasticSearchException(String.format("field type[%s] is invalid type of percolator.", field.getType()));
		}
		mappingBuilder.field("type", "percolator");
	}

	@Override
	public Class<? extends Annotation> getAnnotationType() {
		return PercolatorField.class;
	}
}
