package com.github.jgzl.bsf.core.util;

import com.github.jgzl.bsf.core.base.Callable;
import com.github.jgzl.bsf.core.config.CoreProperties;
import lombok.var;

public class TimeWatchUtils {
    public static void print(boolean isPrint,String msg, Callable.Action0 action0){
        print(isPrint,msg, () -> {
            action0.invoke();
            return 1;
        });
    }

    public static <T> T print(boolean isPrint,String msg,Callable.Func0<T> action0){
        if(isPrint) {
            var b = System.currentTimeMillis();
            T t = action0.invoke();
            var e = System.currentTimeMillis();
            LogUtils.info(TimeWatchUtils.class, CoreProperties.Project, msg + " 耗时:" + (e - b) + "毫秒");
            return t;
        }else {
            return action0.invoke();
        }
    }
}
