package com.github.jgzl.bsf.demo.bigdata;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.github.jgzl.bsf.bigdata.BigDataSource;
import com.github.jgzl.bsf.core.db.DbHelper;
import lombok.var;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;


@SpringBootApplication(exclude = { DruidDataSourceAutoConfigure.class,DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class })
public class ClickHouseApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClickHouseApplication.class, args);
        var r = DbHelper.get(BigDataSource.getDefaultDataSource(),(c)->{
            return c.executeList("select * from apply_order limit 0,10",null);
        });
        var a=1;
    }
}
