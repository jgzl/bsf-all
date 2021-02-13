package com.github.jgzl.bsf.transaction.base;

/**
 * @author huojuncheng
 */
public class EffortThreadLocal {
    private static ThreadLocal<EffortTransactionObject> instance;

    public static ThreadLocal<EffortTransactionObject> getInstance(){
        if(instance==null){
            synchronized (EffortThreadLocal.class){
                if(instance==null){
                    instance=new ThreadLocal();
                }
            }
        }
        return instance;
    }
}
