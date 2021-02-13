package com.yh.csx.bsf.elasticsearch.mapper.base;

public class GeoPoint {
    /**
     * latitude
     */
    private float lat;
    /**
     * longitude
     */
    private float lon;
    
    public GeoPoint(float lat, float lon) {
		super();
		this.lat = lat;
		this.lon = lon;
	}

	public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }
}
