package com.yh.csx.bsf.elasticsearch.mapper;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;

import com.github.jgzl.bsf.core.util.ReflectionUtils;
import com.yh.csx.bsf.elasticsearch.base.ElasticSearchException;
import com.yh.csx.bsf.elasticsearch.mapper.annotations.type.BooleanField;
import com.yh.csx.bsf.elasticsearch.mapper.base.ElasticSearchMapper;

public class BooleanFieldMapper implements ElasticSearchMapper {

	@Override
	public boolean isValidType(Field field) {
		if (ReflectionUtils.isCollectionType(field)) {
			if (!ReflectionUtils.isValidCollectionType(field)) {
				throw new ElasticSearchException(String.format("Unsupported list class type, name[%s].", field.getName()));
			}

			Class<?> genericTypeClass = (Class<?>) ReflectionUtils.getCollectionGenericType(field);
			return Boolean.class == genericTypeClass || boolean.class == genericTypeClass;
		} else {
			Class<?> fieldClass = field.getType();
			return Boolean.class == fieldClass || boolean.class == fieldClass;
		}
	}


	@Override
	public void mapDataType(XContentBuilder mappingBuilder, Field field) throws IOException {
		if (!isValidType(field)) {
			throw new ElasticSearchException(String.format("field type[%s] is invalid type of boolean.", field.getType()));
		}

		mappingBuilder.field("type", "boolean");
		if (field.isAnnotationPresent(getAnnotationType())) {
			BooleanField booleanField = field.getDeclaredAnnotation(BooleanField.class);
			if (booleanField.boost() != 1.0f) {
				mappingBuilder.field("boost", booleanField.boost());
			}

			if (!booleanField.doc_values()) {
				mappingBuilder.field("doc_values", booleanField.doc_values());
			}

			if (!booleanField.index()) {
				mappingBuilder.field("index", booleanField.index());
			}

			if (StringUtils.isNotBlank(booleanField.null_value())) {
				mappingBuilder.field("null_value", booleanField.null_value());
			}

			if (booleanField.store()) {
				mappingBuilder.field("store", booleanField.store());
			}
		}
	}


	@Override
	public Class<? extends Annotation> getAnnotationType() {
		return BooleanField.class;
	}
}
