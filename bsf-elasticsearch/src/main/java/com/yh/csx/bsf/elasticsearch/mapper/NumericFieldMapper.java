package com.yh.csx.bsf.elasticsearch.mapper;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;

import com.google.common.collect.ImmutableMap;
import com.github.jgzl.bsf.core.util.ReflectionUtils;
import com.yh.csx.bsf.elasticsearch.base.ElasticSearchException;
import com.yh.csx.bsf.elasticsearch.mapper.annotations.type.NumberField;
import com.yh.csx.bsf.elasticsearch.mapper.base.ElasticSearchMapper;
import com.yh.csx.bsf.elasticsearch.mapper.enums.NumberType;

public class NumericFieldMapper implements ElasticSearchMapper {

    private static final Map<Class<?>, NumberType> validNumericFieldClassMap = ImmutableMap.<Class<?>, NumberType>builder()
            .put(Double.class, NumberType.Double)
            .put(BigDecimal.class, NumberType.Double)
            .put(Float.class, NumberType.Float)
            .put(Long.class, NumberType.Long)
            .put(BigInteger.class, NumberType.Long)
            .put(Integer.class, NumberType.Integer)
            .put(Short.class, NumberType.Short)
            .put(Byte.class, NumberType.Byte)

            .put(double.class, NumberType.Double)
            .put(float.class, NumberType.Float)
            .put(long.class, NumberType.Long)
            .put(int.class, NumberType.Integer)
            .put(short.class, NumberType.Short)
            .put(byte.class, NumberType.Byte)
            .build();

	public static Collection<Class<?>> getValiadFieldTypes() {
		return validNumericFieldClassMap.keySet();
	}
    
	@Override
	public boolean isValidType(Field field) {
		if (ReflectionUtils.isCollectionType(field)) {
			if (!ReflectionUtils.isValidCollectionType(field)) {
				throw new ElasticSearchException(String.format("Unsupported more than one collection generic type, name[%s].", field.getName()));
			}

			Class<?> genericTypeClass = (Class<?>) ReflectionUtils.getCollectionGenericType(field);
			return validNumericFieldClassMap.keySet().contains(genericTypeClass);
		} else {
			Class<?> fieldClass = field.getType();
			return validNumericFieldClassMap.keySet().contains(fieldClass);
		}
	}

	@Override
	public void mapDataType(XContentBuilder mappingBuilder, Field field) throws IOException {
		if (!isValidType(field)) {
			throw new ElasticSearchException(String.format("field type[%s] is invalid type of number.", field.getType()));
		}

		if (field.isAnnotationPresent(getAnnotationType())) {
			NumberField numberField = field.getDeclaredAnnotation(NumberField.class);
			mappingBuilder.field("type", numberField.type().code());
	        if (!numberField.coerce()) {
	            mappingBuilder.field("coerce", numberField.coerce());
	        }

	        if (numberField.boost() != 1.0f) {
	            mappingBuilder.field("boost", numberField.boost());
	        }

	        if (!numberField.doc_values()) {
	            mappingBuilder.field("doc_values", numberField.doc_values());
	        }

	        if (numberField.ignore_malformed()) {
	            mappingBuilder.field("ignore_malformed", numberField.ignore_malformed());
	        }

	        if (!numberField.index()) {
	            mappingBuilder.field("index", numberField.index());
	        }

	        if (StringUtils.isNotBlank(numberField.null_value())) {
	            mappingBuilder.field("null_value", numberField.null_value());
	        }

	        if (numberField.store()) {
	            mappingBuilder.field("store", numberField.store());
	        }

	        if (numberField.type() == NumberType.ScaledFloat && numberField.scaling_factor() != 1) {
	            mappingBuilder.field("scaling_factor", numberField.scaling_factor());
	        }
		} else {
			if (ReflectionUtils.isCollectionType(field)) {
				Class<?> genericTypeClass = (Class<?>) ReflectionUtils.getCollectionGenericType(field);
				NumberType numberType = validNumericFieldClassMap.get(genericTypeClass);
				mappingBuilder.field("type", numberType.code());
			} else {
				NumberType numberType = validNumericFieldClassMap.get(field.getType());
				mappingBuilder.field("type", numberType.code());
			}
		}

	}

	@Override
	public Class<? extends Annotation> getAnnotationType() {
		return NumberField.class;
	}
}
