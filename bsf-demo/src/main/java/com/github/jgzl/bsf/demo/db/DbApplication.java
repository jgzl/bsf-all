package com.github.jgzl.bsf.demo.db;

import com.github.jgzl.bsf.core.db.DbHelper;
import com.github.jgzl.bsf.core.util.ContextUtils;
import lombok.var;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sql.DataSource;

@SpringBootApplication
public class DbApplication {
    public static void main(String[] args) {
        SpringApplication.run(DbApplication.class, args);
        var data1 = ContextUtils.getBean(DataSource.class,"data1",true);
        var r = DbHelper.get(data1,(c)->{return c.executeList("select * from t1",null);});
        var data2 = ContextUtils.getBean(DataSource.class,"data2",true);
        var r2= DbHelper.get(data2,(c)->{ return c.executeList("select * from admin",null);});
        DbHelper.transaction(data2,1,()->{
            var r3= DbHelper.transactionGet(data2,(c)->{ return c.executeList("select * from admin",null);});
            var r4= DbHelper.transactionGet(data2,(c)->{ return c.executeSql("update admin set username='bbb'",null);});
            var r5= DbHelper.transactionGet(data1,(c)->{ return c.executeSql("update t1 set name=1 where id=1",null);});
        });
    }
}
