package com.yh.csx.bsf.elasticsearch.mapper.base;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;

import com.google.common.collect.Maps;
import com.github.jgzl.bsf.core.util.ReflectionUtils;
import com.yh.csx.bsf.elasticsearch.base.ElasticSearchException;
import com.yh.csx.bsf.elasticsearch.mapper.BinaryFieldMapper;
import com.yh.csx.bsf.elasticsearch.mapper.BooleanFieldMapper;
import com.yh.csx.bsf.elasticsearch.mapper.CompletionFieldMapper;
import com.yh.csx.bsf.elasticsearch.mapper.DateFieldMapper;
import com.yh.csx.bsf.elasticsearch.mapper.GeoPointFieldMapper;
import com.yh.csx.bsf.elasticsearch.mapper.IPFieldMapper;
import com.yh.csx.bsf.elasticsearch.mapper.MultiFieldMapper;
import com.yh.csx.bsf.elasticsearch.mapper.NumericFieldMapper;
import com.yh.csx.bsf.elasticsearch.mapper.PercolatorFieldMapper;
import com.yh.csx.bsf.elasticsearch.mapper.RangeFieldMapper;
import com.yh.csx.bsf.elasticsearch.mapper.StringFieldMapper;

public class ElasticSearchMapperRegistry {

	private static final Map<Class<? extends Annotation>, ElasticSearchMapper> fieldAnnotation2MapperMap = Maps.newHashMap();
	private static final Map<Class<?>, ElasticSearchMapper> defaultField2MapperMap = Maps.newHashMap();

	static {
		BooleanFieldMapper booleanMapper = new BooleanFieldMapper();
		DateFieldMapper dateMapper = new DateFieldMapper();
		NumericFieldMapper numericMapper = new NumericFieldMapper();
		StringFieldMapper stringMapper = new StringFieldMapper();
		register(new BinaryFieldMapper());
		register(booleanMapper);
		register(new CompletionFieldMapper());
		register(dateMapper);
		register(new GeoPointFieldMapper());
		register(new IPFieldMapper());
		register(new MultiFieldMapper());
		register(numericMapper);
		register(new PercolatorFieldMapper());
		register(new RangeFieldMapper());
		register(stringMapper);

		defaultMapper(Boolean.class, booleanMapper);
		defaultMapper(boolean.class, booleanMapper);
		defaultMapper(Date.class, booleanMapper);
		NumericFieldMapper.getValiadFieldTypes().forEach(f -> defaultMapper(f, numericMapper));
		defaultMapper(String.class, stringMapper);
	}

	public static ElasticSearchMapper getMapperByAnnotationType(Class<? extends Annotation> clazz) {
		return fieldAnnotation2MapperMap.get(clazz);
	}

	// 字段没有注解的时候默认mapper
	public static ElasticSearchMapper getDefaultMapperByField(Field field) {
		Class<?> type = field.getType();
		ElasticSearchMapper mapper = defaultField2MapperMap.get(type);
		if (mapper == null) {
			Class<?> genericTypeClass = type;
			if (ReflectionUtils.isCollectionType(field)) {
				if (!ReflectionUtils.isValidCollectionType(field)) {
					throw new ElasticSearchException(String.format("Unsupported list class type, name[%s].", field.getName()));
				}
				genericTypeClass = (Class<?>) ReflectionUtils.getCollectionGenericType(field);
			}
			for (Class<?> key : defaultField2MapperMap.keySet()) {
				if (genericTypeClass.isAssignableFrom(key)) {
					mapper = defaultField2MapperMap.get(key);
					break;
				}
			}
		}
		return mapper;
	}
	
	private static void register(ElasticSearchMapper mapper) {
		fieldAnnotation2MapperMap.put(mapper.getAnnotationType(), mapper);
	}

	private static void defaultMapper(Class<?> fieldType, ElasticSearchMapper mapper) {
		defaultField2MapperMap.put(fieldType, mapper);
	}
}
