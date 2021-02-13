package com.github.jgzl.bsf.core.base;

/**
 * @author: lihaifeng
 * @version: 2019-05-27 13:44
 **/
public enum Environment {
	//开发环境
    dev("开发"), 
    //生产环境
    prd("生产");

    private String name;
    Environment(String name)
    {
        this.name = name;
    }
}
