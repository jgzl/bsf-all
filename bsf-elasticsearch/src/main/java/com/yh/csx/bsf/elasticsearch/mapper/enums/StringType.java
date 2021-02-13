package com.yh.csx.bsf.elasticsearch.mapper.enums;

import java.lang.String;

public enum StringType {
    /**
     * A field to index structured content such as email addresses, hostnames, status codes, zip codes or tags.
     * <p/>
     * They are typically used for filtering (Find me all blog posts where status is published),
     * for sorting, and for aggregations. Keyword fields are only searchable by their exact value.
     */
    Keyword("keyword"),

    /**
     * A field to index full-text values, such as the body of an email or the description of a product.
     * These fields are analyzed, that is they are passed through an analyzer to convert the string into
     * a list of individual terms before being indexed.
     * The analysis process allows Elasticsearch to search for individual words within each full text field.
     * Text fields are not used for sorting and seldom used for aggregations
     */
    Text("text");

	private String code;

	private StringType(String code) {
		this.code = code;
	}

	public String code() {
		return code;
	}
}
