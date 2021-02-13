package com.yh.csx.bsf.elasticsearch.mapper.base;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.elasticsearch.common.xcontent.XContentBuilder;

public interface ElasticSearchMapper {

	boolean isValidType(Field field);
	
	void mapDataType(XContentBuilder mappingBuilder, Field field) throws IOException;
	
	Class<? extends Annotation> getAnnotationType();
}
