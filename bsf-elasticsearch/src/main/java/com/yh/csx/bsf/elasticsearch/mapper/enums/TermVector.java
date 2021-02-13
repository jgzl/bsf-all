package com.yh.csx.bsf.elasticsearch.mapper.enums;


public enum TermVector {
    /**
     * No term vectors are stored. (default)
     */
    No ("no"),

    /**
     * Just the terms in the field are stored.
     */
    Yes("yes"),

    /**
     * Terms and positions are stored.
     */
    WithPositions("with_positions"),
    /**
     * Terms and character offsets are stored.
     */
    WithOffsets("with_offsets"),

    /**
     * Terms, positions, and character offsets are stored.
     */
    WithPositionsOffsets("with_positions_offsets");

	private String code;

	private TermVector(String code) {
		this.code = code;
	}

	public String code() {
		return code;
	}
}
