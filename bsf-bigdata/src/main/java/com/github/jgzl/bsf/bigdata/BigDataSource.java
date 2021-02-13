package com.github.jgzl.bsf.bigdata;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.jgzl.bsf.core.util.ContextUtils;
import lombok.var;

public class BigDataSource {

    public static DruidDataSource getDefaultDataSource(){
        var find = ContextUtils.getBean(DruidDataSource.class,"tidbDataSource",false);
        if(find==null)
        {
            find = ContextUtils.getBean(DruidDataSource.class,"clickHouseDataSource",false);
        }
        return find;
    }
}
