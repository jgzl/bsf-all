package com.yh.csx.bsf.elasticsearch.mapper.enums;

public enum IndexOptions {
    Default("none"),

    /**
     * Only the doc number is indexed. Can answer the question Does this term exist in this field?
     */
    Docs("docs"),

    /**
     * Doc number and term frequencies are indexed.
     * Term frequencies are used to score repeated terms higher than single terms.
     */
    Freqs("freqs"),

    /**
     * Doc number, term frequencies, and term positions (or order) are indexed.
     * Positions can be used for proximity or phrase queries.
     */
	Positions("positions"),

    /**
     * Doc number, term frequencies, positions,
     * and start and end character offsets (which map the term back to the original string) are indexed.
     * Offsets are used by the postings highlighter.
     */
	Offsets("offsets");
	
	private String code;
	
    private IndexOptions(String code) {
		this.code = code;
	}

	public  String code() {
		return code;
	}
}
