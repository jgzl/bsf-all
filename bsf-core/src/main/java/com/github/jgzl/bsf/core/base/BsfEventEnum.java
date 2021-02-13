package com.github.jgzl.bsf.core.base;

import java.util.HashMap;

/**
 * @author: chejiangyi
 * @version: 2019-08-10 16:00
 **/
public enum BsfEventEnum {
	/**
	 * 属性缓存更新事件
	 * */
    PropertyCacheUpdateEvent(new HashMap<String,Object>().getClass(),"属性缓存更新事件");
     /**
      * 描述
      * */
    String desc;
    /**
     * 类
     * */
    Class dataClass;
    public Class getDataClass(){
        return dataClass;
    }
    BsfEventEnum(Class dataClass,String desc){
        this.desc = desc;
        this.dataClass = dataClass;
    }
}
