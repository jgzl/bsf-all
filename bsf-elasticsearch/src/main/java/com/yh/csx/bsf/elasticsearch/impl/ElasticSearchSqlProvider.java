package com.yh.csx.bsf.elasticsearch.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import com.alibaba.druid.pool.DruidDataSource;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.jgzl.bsf.core.serialize.JsonSerializer;
import com.yh.csx.bsf.elasticsearch.ElasticSearchProperties;
import com.yh.csx.bsf.elasticsearch.base.ElasticSearchAware;
import com.yh.csx.bsf.elasticsearch.mapper.MappingBuilder;

public class ElasticSearchSqlProvider implements AutoCloseable {

	private TransportClient transportClient;
	private DruidDataSource druidDataSource;
	private JdbcTemplate jdbcTemplate;
	private ElasticSearchProperties elasticSearchProperties;
	private MappingBuilder mappingBuilder = new MappingBuilder();

	public ElasticSearchSqlProvider(ElasticSearchProperties elasticSearchProperties, TransportClient transportClient, DruidDataSource druidDataSource){
		this.elasticSearchProperties = elasticSearchProperties;
		this.transportClient = transportClient;
		this.druidDataSource = druidDataSource;
		jdbcTemplate = new JdbcTemplate(this.druidDataSource);
	}
	
    public void createIndex(String index, String type) {
        ElasticSearchMonitor.hook().run("createIndex", () -> {
            getClient().prepareIndex(index, type).setSource().get();
        });
    }
    
	/**
	 * 功能描述：新建索引
	 *
	 * @param index 索引名
     * @param type  类型
     * @param documentClazz mapping规则
	 */
	public void createIndex(String indexName, String type, Class<?> documentClazz) {
		if (documentClazz != null) {
			ElasticSearchMonitor.hook().run("createIndex", () -> {
				getClient().admin().indices().prepareCreate(indexName).addMapping(type, mappingBuilder.buildMapping(documentClazz)).get();
			});
		} else {
			createIndex(indexName, type);
		}
	}
	
	/**
	 * 功能描述：设置mapping
	 *
	 * @param index 索引名
     * @param type  类型
     * @param documentClazz mapping规则
	 */
	public void putMapping(String indexName, String type, Class<?> documentClazz) {
		if (documentClazz != null) {
			ElasticSearchMonitor.hook().run("putMapping", () -> {
				PutMappingRequest mappingRequest = Requests.putMappingRequest(indexName).type(type).source(mappingBuilder.buildMapping(documentClazz));
				getClient().admin().indices().putMapping(mappingRequest).actionGet();
			});
		}
	}
	
	public <T> List<T> searchBySql(String sql, Class<T> clazz) {
		return ElasticSearchMonitor.hook().run("searchBySql", () -> {
			return jdbcTemplate.query(sql, new BeanPropertyRowMapper<T>(clazz), new Object[]{});
		});
	}

	public void deleteBySql(String sql) {
		ElasticSearchMonitor.hook().run("deleteBySql", () -> {
			DruidDataSource dataSource = this.druidDataSource;
			Connection connection = null;
			PreparedStatement ps = null;
			try {
				connection = dataSource.getConnection();
				ps = connection.prepareStatement(sql);
				ps.execute();
			} catch (Exception ex) {

			} finally {
				JdbcUtils.closeStatement(ps);
				DataSourceUtils.releaseConnection(connection, dataSource);
			}
		});
	}
	
	public <T extends ElasticSearchAware> void insertData(String index, String type, Collection<T> coll) {
		ElasticSearchMonitor.hook().run("insertData", () -> {
			int size = coll.size();
			if (size < this.elasticSearchProperties.getBulkSize()) {
				coll.forEach(item -> buildRequest(index, type, item).get());
			} else {
				bulkInsertData(index, type, coll);
			}
		});
	}

	private <T extends ElasticSearchAware> void bulkInsertData(String index, String type, Collection<T> coll) {
		BulkRequestBuilder bulkRequest = getClient().prepareBulk();
		coll.forEach((item) -> {
			bulkRequest.add(buildRequest(index, type, item));
		});
		bulkRequest.get();
	}
	
	private <T extends ElasticSearchAware> IndexRequestBuilder buildRequest(String index, String type, T obj) {
		String id = obj.get_id();
		JsonSerializer jsonSerializer = new JsonSerializer();
		String json = jsonSerializer.serialize(obj);
		HashMap<String,Object> jsonObject = jsonSerializer.deserialize(json, new TypeReference<HashMap<String,Object>>(){}.getType());
		//JSONObject.parseObject(json);
		if (StringUtils.isEmpty(id)) {
			return getClient().prepareIndex(index, type).setSource(jsonObject);
		} else {
			return getClient().prepareIndex(index, type).setId(id).setSource(jsonObject);
		}
	}

	@Override
	public void close() {
		if(transportClient!=null)
		{try{transportClient.close();}catch (Exception e){}}
		if(druidDataSource!=null)
		{try{druidDataSource.close();}catch (Exception e){}}
	}
	
	private TransportClient getClient() {
		return this.transportClient;
	}


}
