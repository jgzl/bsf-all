package com.yh.csx.bsf.elasticsearch.mapper.enums;

import java.lang.String;

public enum SimilarityAlgorithm {
    Default("BM25"),
    
    BM25("BM25");

	private String code;

	private SimilarityAlgorithm(String code) {
		this.code = code;
	}

	public String code() {
		return code;
	}
}
