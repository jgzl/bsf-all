package com.yh.csx.bsf.elasticsearch.mapper;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.elasticsearch.common.xcontent.XContentBuilder;

import com.yh.csx.bsf.elasticsearch.base.ElasticSearchException;
import com.yh.csx.bsf.elasticsearch.mapper.annotations.type.BinaryField;
import com.yh.csx.bsf.elasticsearch.mapper.base.ElasticSearchMapper;

public class BinaryFieldMapper implements ElasticSearchMapper {

	@Override
	public boolean isValidType(Field field) {
		Class<?> fieldClass = field.getType();
		return String.class.isAssignableFrom(fieldClass);
	}

	@Override
	public void mapDataType(XContentBuilder mappingBuilder, Field field) throws IOException {
		if (!isValidType(field)) {
			throw new ElasticSearchException(String.format("field type[%s] is invalid type of binary.", field.getType()));
		}

		mappingBuilder.field("type", "binary");
		if (field.isAnnotationPresent(getAnnotationType())) {
			BinaryField binaryField = field.getDeclaredAnnotation(BinaryField.class);
			if (!binaryField.doc_values()) {
				mappingBuilder.field("doc_values", binaryField.doc_values());
			}

			if (binaryField.store()) {
				mappingBuilder.field("store", binaryField.store());
			}
		}
	}

	@Override
	public Class<? extends Annotation> getAnnotationType() {
		return BinaryField.class;
	}
}
