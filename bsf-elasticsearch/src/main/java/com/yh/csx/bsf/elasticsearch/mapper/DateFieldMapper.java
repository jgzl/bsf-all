package com.yh.csx.bsf.elasticsearch.mapper;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.xcontent.XContentBuilder;

import com.github.jgzl.bsf.core.util.ReflectionUtils;
import com.yh.csx.bsf.elasticsearch.mapper.annotations.type.DateField;
import com.yh.csx.bsf.elasticsearch.mapper.base.ElasticSearchMapper;

public class DateFieldMapper implements ElasticSearchMapper {

	@Override
	public boolean isValidType(Field field) {
		if (ReflectionUtils.isCollectionType(field)) {
			if (!ReflectionUtils.isValidCollectionType(field)) {
				throw new ElasticsearchException(String.format("Unsupported list class type, name[%s].", field.getName()));
			}
			Class<?> genericTypeClass = (Class<?>) ReflectionUtils.getCollectionGenericType(field);
			return Date.class.isAssignableFrom(genericTypeClass);
		} else {
			Class<?> fieldClass = field.getType();
			return Date.class.isAssignableFrom(fieldClass);
		}
	}

	@Override
	public void mapDataType(XContentBuilder mappingBuilder, Field field) throws IOException {
		if (!isValidType(field)) {
			throw new ElasticsearchException(String.format("field type[%s] is invalid type of date.", field.getType()));
		}

		mappingBuilder.field("type", "date");
		if (field.isAnnotationPresent(getAnnotationType())) {
			DateField dateField = field.getDeclaredAnnotation(DateField.class);
			mappingBuilder.field("type", "date");
	        mappingBuilder.field("format", dateField.format());

	        if (dateField.boost() != 1.0f) {
	            mappingBuilder.field("boost", dateField.boost());
	        }

	        if (!dateField.doc_values()) {
	            mappingBuilder.field("doc_values", dateField.doc_values());
	        }

	        if (dateField.ignore_malformed()) {
	            mappingBuilder.field("ignore_malformed", dateField.ignore_malformed());
	        }

	        if (!dateField.index()) {
	            mappingBuilder.field("index", dateField.index());
	        }

	        if (StringUtils.isNotBlank(dateField.null_value())) {
	            mappingBuilder.field("null_value", dateField.null_value());
	        }

	        if (dateField.store()) {
	            mappingBuilder.field("store", dateField.store());
	        }

	        if (!"ROOT".equalsIgnoreCase(dateField.locale())) {
	            mappingBuilder.field("locale", dateField.locale());
	        }
		}
	}

	@Override
	public Class<? extends Annotation> getAnnotationType() {
		return DateField.class;
	}
}
