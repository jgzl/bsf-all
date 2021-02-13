package com.yh.csx.bsf.elasticsearch.mapper;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;

import com.yh.csx.bsf.elasticsearch.base.ElasticSearchException;
import com.yh.csx.bsf.elasticsearch.mapper.annotations.type.MultiField;
import com.yh.csx.bsf.elasticsearch.mapper.annotations.type.MultiNestedField;
import com.yh.csx.bsf.elasticsearch.mapper.annotations.type.TokenCountField;
import com.yh.csx.bsf.elasticsearch.mapper.base.ElasticSearchMapper;

public class MultiFieldMapper implements ElasticSearchMapper {

	@Override
	public boolean isValidType(Field field) {
		Class<?> fieldClass = field.getType();
		return String.class.isAssignableFrom(fieldClass);
	}

	@Override
	public void mapDataType(XContentBuilder mappingBuilder, Field field) throws IOException {
		if (!isValidType(field)) {
			throw new ElasticSearchException(String.format("field type[%s] is invalid type of string.", field.getType()));
		}
		MultiField multiField = field.getDeclaredAnnotation(MultiField.class);
		StringFieldMapper.mapDataType(mappingBuilder, multiField.mainField());

		mappingBuilder.startObject("fields");
		for (MultiNestedField otherField : multiField.fields()) {
			mappingBuilder.startObject(otherField.name());
			StringFieldMapper.mapDataType(mappingBuilder, otherField.field());
			mappingBuilder.endObject();
		}

		for (TokenCountField tokenCountField : multiField.tokenFields()) {
			mappingBuilder.startObject(tokenCountField.name());
			mapTokenCountFieldType(mappingBuilder, tokenCountField);
			mappingBuilder.endObject();
		}
		mappingBuilder.endObject();
	}

	@Override
	public Class<? extends Annotation> getAnnotationType() {
		return MultiField.class;
	}

	private void mapTokenCountFieldType(XContentBuilder mappingBuilder, TokenCountField tokenCountField) throws IOException {
		mappingBuilder.field("type", "token_count");
		mappingBuilder.field("analyzer", tokenCountField.analyzer());

		if (!tokenCountField.enable_position_increments()) {
			mappingBuilder.field("enable_position_increments", tokenCountField.enable_position_increments());
		}

		if (tokenCountField.boost() != 1.0f) {
			mappingBuilder.field("boost", tokenCountField.boost());
		}

		if (!tokenCountField.doc_values()) {
			mappingBuilder.field("doc_values", tokenCountField.doc_values());
		}

		if (!tokenCountField.index()) {
			mappingBuilder.field("index", tokenCountField.index());
		}

		if (StringUtils.isNotBlank(tokenCountField.null_value())) {
			mappingBuilder.field("null_value", tokenCountField.null_value());
		}

		if (tokenCountField.store()) {
			mappingBuilder.field("store", tokenCountField.store());
		}
	}
}
