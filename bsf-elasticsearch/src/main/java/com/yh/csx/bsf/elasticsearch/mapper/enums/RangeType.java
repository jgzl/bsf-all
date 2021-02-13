package com.yh.csx.bsf.elasticsearch.mapper.enums;

public enum RangeType {
    /**
     * A range of signed 32-bit integers with a minimum value of -2^31 and maximum of 2^31-1.
     */
    IntegerRange("integer_range"),

    /**
     * A range of single-precision 32-bit IEEE 754 floating point values.
     */
    FloatRange("float_range"),

    /**
     * A range of signed 64-bit integers with a minimum value of -263 and maximum of 263-1.
     */
    LongRange("long_range"),

    /**
     * A range of double-precision 64-bit IEEE 754 floating point values.
     */
    DoubleRange("double_range"),

    /**
     * A range of date values represented as unsigned 64-bit integer milliseconds elapsed since system epoch.
     */
    DateRange("date_range");

	private String code;

	private RangeType(String code) {
		this.code = code;
	}

	public String code() {
		return code;
	}
}
