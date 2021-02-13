package com.yh.csx.bsf.elasticsearch.mapper;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;

import com.github.jgzl.bsf.core.util.ReflectionUtils;
import com.yh.csx.bsf.elasticsearch.base.ElasticSearchException;
import com.yh.csx.bsf.elasticsearch.mapper.annotations.type.CompletionContext;
import com.yh.csx.bsf.elasticsearch.mapper.annotations.type.CompletionField;
import com.yh.csx.bsf.elasticsearch.mapper.base.ElasticSearchMapper;

public class CompletionFieldMapper implements ElasticSearchMapper {

	@Override
	public boolean isValidType(Field field) {
		if (ReflectionUtils.isCollectionType(field)) {
			if (!ReflectionUtils.isValidCollectionType(field)) {
				throw new ElasticSearchException(String.format("Unsupported list class type, name[%s].", field.getName()));
			}
			Class<?> genericTypeClass = (Class<?>) ReflectionUtils.getCollectionGenericType(field);
			return String.class.isAssignableFrom(genericTypeClass);
		} else {
			Class<?> fieldClass = field.getType();
			return String.class.isAssignableFrom(fieldClass);
		}
	}

	@Override
	public void mapDataType(XContentBuilder mappingBuilder, Field field) throws IOException {
		if (!isValidType(field)) {
			throw new ElasticSearchException(String.format("field type[%s] is invalid type of string.", field.getType()));
		}

		CompletionField completionField = field.getDeclaredAnnotation(CompletionField.class);
		mappingBuilder.field("type", "completion");

        if (!"simple".equalsIgnoreCase(completionField.analyzer())) {
            mappingBuilder.field("analyzer", completionField.analyzer());
        }

        if (!"simple".equalsIgnoreCase(completionField.search_analyzer())) {
            mappingBuilder.field("search_analyzer", completionField.search_analyzer());
        }

        if (!completionField.preserve_separators()) {
            mappingBuilder.field("preserve_separators", completionField.preserve_separators());
        }

        if (!completionField.preserve_position_increments()) {
            mappingBuilder.field("preserve_position_increments", completionField.preserve_position_increments());
        }

        if (completionField.max_input_length() != 50) {
            mappingBuilder.field("max_input_length", completionField.max_input_length());
        }

		if (completionField.contexts().length > 0) {
			mappingBuilder.startArray("contexts");
			for (CompletionContext completionContext : completionField.contexts()) {
				mappingBuilder.field("name", completionContext.name());
				mappingBuilder.field("type", completionContext.type());

				if (StringUtils.isNotBlank(completionContext.path())) {
					mappingBuilder.field("path", completionContext.path());
				}
			}
			mappingBuilder.endArray();
		}
	}

	@Override
	public Class<? extends Annotation> getAnnotationType() {
		return CompletionField.class;
	}
}
