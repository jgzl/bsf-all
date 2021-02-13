package com.yh.csx.bsf.elasticsearch.base;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface ElasticSearchAware {

	@JsonIgnore
	String get_id();
}
