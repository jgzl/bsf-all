package com.github.jgzl.bsf.cat.remote;

import com.dianping.cat.Cat;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: lihaifeng
 * @version: 2019-08-30 11:31
 **/
public class CatRemoteContext implements Cat.Context {

    private Map<String, String> properties = new HashMap<>();

    @Override
    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    @Override
    public String getProperty(String key) {
        return properties.get(key);
    }
}