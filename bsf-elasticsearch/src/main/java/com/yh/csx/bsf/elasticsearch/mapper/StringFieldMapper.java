package com.yh.csx.bsf.elasticsearch.mapper;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;

import com.github.jgzl.bsf.core.util.ReflectionUtils;
import com.yh.csx.bsf.elasticsearch.base.ElasticSearchException;
import com.yh.csx.bsf.elasticsearch.mapper.annotations.type.StringField;
import com.yh.csx.bsf.elasticsearch.mapper.base.ElasticSearchMapper;
import com.yh.csx.bsf.elasticsearch.mapper.enums.IndexOptions;
import com.yh.csx.bsf.elasticsearch.mapper.enums.SimilarityAlgorithm;
import com.yh.csx.bsf.elasticsearch.mapper.enums.StringType;
import com.yh.csx.bsf.elasticsearch.mapper.enums.TermVector;

public class StringFieldMapper implements ElasticSearchMapper {
	
	@Override
	public boolean isValidType(Field field) {
		if (ReflectionUtils.isCollectionType(field)) {
			if (!ReflectionUtils.isValidCollectionType(field)) {
				throw new IllegalArgumentException(String.format("Unsupported list class type, name[%s].", field.getName()));
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

		if (field.isAnnotationPresent(getAnnotationType())) {
			StringField stringField = field.getDeclaredAnnotation(StringField.class);
			mapDataType(mappingBuilder, stringField);
		} else {
			mappingBuilder.field("type", "keyword");
		}
	}

	@Override
	public Class<? extends Annotation> getAnnotationType() {
		return StringField.class;
	}

    public static void mapDataType(XContentBuilder mappingBuilder, StringField stringField) throws IOException {
        mappingBuilder.field("type", stringField.type().code());

        if (stringField.boost() != 1.0f) {
            mappingBuilder.field("boost", stringField.boost());
        }

        if (!stringField.doc_values()) {
            mappingBuilder.field("doc_values", stringField.doc_values());
        }

        if (stringField.copy_to().length > 0) {
            if (stringField.copy_to().length == 1) {
                mappingBuilder.field("copy_to", stringField.copy_to()[0]);
            }
            else {
                mappingBuilder.array("copy_to", stringField.copy_to());
            }
        }

        if (stringField.eager_global_ordinals()) {
            mappingBuilder.field("eager_global_ordinals", stringField.eager_global_ordinals());
        }

        if (stringField.ignore_above() != 2147483647) {
            mappingBuilder.field("ignore_above", stringField.ignore_above());
        }

        if (!stringField.index()) {
            mappingBuilder.field("index", stringField.index());
        }

        if (stringField.index_options() != IndexOptions.Default) {
            mappingBuilder.field("index_options", stringField.index_options().code());
        }

        if (StringUtils.isNotBlank(stringField.null_value())) {
            mappingBuilder.field("null_value", stringField.null_value());
        }

        if (stringField.store()) {
            mappingBuilder.field("store", stringField.store());
        }

        if (stringField.similarity() != SimilarityAlgorithm.Default) {
            mappingBuilder.field("similarity", stringField.similarity().code());
        }

        // full text setting
        if (stringField.type() == StringType.Text) {

            if (StringUtils.isNotBlank(stringField.analyzer())) {
                mappingBuilder.field("analyzer", stringField.analyzer());
            }

            if (StringUtils.isNotBlank(stringField.search_analyzer())) {
                mappingBuilder.field("analyzer", stringField.search_analyzer());
            }

            if (StringUtils.isNotBlank(stringField.search_quote_analyzer())) {
                mappingBuilder.field("analyzer", stringField.search_quote_analyzer());
            }

            if (stringField.position_increment_gap() != 100) {
                mappingBuilder.field("position_increment_gap", stringField.position_increment_gap());
            }

            if (!stringField.norms()) {
                mappingBuilder.field("norms", stringField.norms());
            }

            if (stringField.term_vector() != TermVector.No) {
                mappingBuilder.field("term_vector", stringField.term_vector().code());
            }

            if (stringField.fielddata().enable() && stringField.type() == StringType.Text) {
                mappingBuilder.field("fielddata", stringField.fielddata().enable());

                if (stringField.fielddata().frequency().enable()) {

                    mappingBuilder.startObject("fielddata_frequency_filter");

                    if (stringField.fielddata().frequency().min() > 0) {
                        mappingBuilder.field("min", stringField.fielddata().frequency().min());
                    }

                    if (stringField.fielddata().frequency().max() > 0) {
                        mappingBuilder.field("max", stringField.fielddata().frequency().max());
                    }

                    if (stringField.fielddata().frequency().min_segment_size() > 0) {
                        mappingBuilder.field("min_segment_size", stringField.fielddata().frequency().min_segment_size());
                    }

                    mappingBuilder.endObject();
                }
            }
        }
    }

}
