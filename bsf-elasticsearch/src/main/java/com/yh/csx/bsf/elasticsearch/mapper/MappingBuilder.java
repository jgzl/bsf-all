package com.yh.csx.bsf.elasticsearch.mapper;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import com.github.jgzl.bsf.core.util.ReflectionUtils;
import com.yh.csx.bsf.elasticsearch.base.ElasticSearchException;
import com.yh.csx.bsf.elasticsearch.mapper.annotations.Document;
import com.yh.csx.bsf.elasticsearch.mapper.annotations.IgnoreField;
import com.yh.csx.bsf.elasticsearch.mapper.base.ElasticSearchMapper;
import com.yh.csx.bsf.elasticsearch.mapper.base.ElasticSearchMapperRegistry;

public class MappingBuilder {

	public XContentBuilder buildMapping(Class<?> documentClazz) {
		if (documentClazz == null) {
			throw new ElasticSearchException("param[documentClazz] can not be null!");
		}

		if (!documentClazz.isAnnotationPresent(Document.class)) {
			throw new ElasticSearchException(String.format("Can't find annotation[@Document] at class[%s]", documentClazz.getName()));
		}

		try {
			return buildMappingInner(documentClazz);
		} catch (IOException e) {
			throw new ElasticSearchException("io exception:"+e.getMessage());
		}
	}

	private XContentBuilder buildMappingInner(Class<?> documentClazz) throws IOException {
		XContentBuilder mappingBuilder = XContentFactory.jsonBuilder().prettyPrint().startObject();
		Document document = documentClazz.getAnnotation(Document.class);
		if (document == null) {
			throw new IllegalStateException(String.format("Can't find annotation[@Document] at class[%s]", documentClazz.getName()));
		}
		buildTypeProperty(mappingBuilder, documentClazz);
		mappingBuilder.endObject();
		return mappingBuilder;
	}

	private void buildTypeProperty(XContentBuilder mappingBuilder, Class<?> clazz) throws IOException {
		mappingBuilder.startObject("properties");

		Field[] classFields = ReflectionUtils.retrieveFields(clazz);
		for (Field classField : classFields) {
			String fieldName = classField.getName();

			if (Modifier.isTransient(classField.getModifiers()) || Modifier.isStatic(classField.getModifiers()) || fieldName.equals("$VRc") || fieldName.equals("serialVersionUID")) {
				continue;
			}

			if (classField.getAnnotation(IgnoreField.class) != null) {
				continue;
			}
			buildFieldProperty(mappingBuilder, classField);
		}
		mappingBuilder.endObject();
	}

	private void buildFieldProperty(XContentBuilder mappingBuilder, Field field) throws IOException {
		List<ElasticSearchMapper> mapperList = new ArrayList<ElasticSearchMapper>();
		Annotation[] annotations = field.getAnnotations();
		// 优先根据注解匹配
		for (Annotation annotation : annotations) {
			ElasticSearchMapper mapper = ElasticSearchMapperRegistry.getMapperByAnnotationType(annotation.annotationType());
			if (mapper != null) {
				mapperList.add(mapper);
			}
		}
		if(mapperList.size() > 1) {
			throw new ElasticSearchException(String.format("field [%s] has more than 1 field type annotations.", field.getName()));
		}
		
		if (mapperList.isEmpty()) {
			ElasticSearchMapper mapper = ElasticSearchMapperRegistry.getDefaultMapperByField(field);
			mappingBuilder.startObject(field.getName());
			if (mapper != null) {
				mapper.mapDataType(mappingBuilder, field);
			} else if (ReflectionUtils.isCollectionType(field)) {
				// Collection type field
				if (!ReflectionUtils.isValidCollectionType(field)) {
					throw new IllegalArgumentException(String.format("Unsupported list class type, name[%s].", field.getName()));
				}
				Type genericType = ReflectionUtils.getCollectionGenericType(field);
				// Nested Doc Type
				mappingBuilder.field("type", "nested");
				buildTypeProperty(mappingBuilder, (Class<?>) genericType);
			} else {
				// Inner Doc Type
				mappingBuilder.field("type", "object");
				buildTypeProperty(mappingBuilder, field.getType());
			}
			mappingBuilder.endObject();
		} else {
			ElasticSearchMapper mapper = mapperList.get(0);
			mappingBuilder.startObject(field.getName());
			mapper.mapDataType(mappingBuilder, field);
			mappingBuilder.endObject();
		}
	}

}
