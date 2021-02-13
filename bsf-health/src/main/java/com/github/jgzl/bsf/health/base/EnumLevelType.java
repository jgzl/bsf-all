package com.github.jgzl.bsf.health.base;

/**
 * @author: huojuncheng
 * @version: 2020-10-10
 **/
public enum EnumLevelType {
	//报警级别
    HIGN(3,"极其严重"),
    MIDDLE(2,"严重"),
    LOW(1,"一般"),;

    private int level =1;
    private String description;
    public String getDescription(){return description;}
    public int getLevel(){return level;}
    EnumLevelType(int level, String description){
        this.description = description;
        this.level = level;
    }
}
