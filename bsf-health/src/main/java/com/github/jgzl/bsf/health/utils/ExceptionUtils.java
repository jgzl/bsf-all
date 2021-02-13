package com.github.jgzl.bsf.health.utils;

import com.github.jgzl.bsf.core.http.DefaultHttpClient;
import com.github.jgzl.bsf.core.http.HttpClient;
import com.github.jgzl.bsf.core.thread.ThreadPool;
import com.github.jgzl.bsf.core.util.NetworkUtils;
import com.github.jgzl.bsf.core.util.PropertyUtils;
import com.github.jgzl.bsf.health.base.EnumLevelType;
import com.github.jgzl.bsf.health.base.EnumWarnType;
import com.github.jgzl.bsf.health.base.Message;
import com.github.jgzl.bsf.health.config.HealthProperties;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import org.apache.http.entity.ContentType;

import java.util.HashMap;
import java.util.Map;

public class ExceptionUtils {
    private final static int exceptionType=1;
    private final static String exceptionUrl="bsf.report.exception.url";
    public static void reportException(Message message){
        if(message.getWarnType()==EnumWarnType.ERROR){
            ThreadPool.System.submit("bsf系统任务:异常上报",()->{
                Map<String,Object> param=new HashMap();
                param.put("exceptionType",exceptionType);
                param.put("exceptionLevel",message.getLevelType().getLevel());
                param.put("exceptionContent",String.format("[%s][%s][%s]%s",
                        NetworkUtils.getIpAddress(),
                        PropertyUtils.getPropertyCache(HealthProperties.BsfEnv,""),
                        com.github.jgzl.bsf.core.util.StringUtils.nullToEmpty(PropertyUtils.getPropertyCache(HealthProperties.SpringApplictionName,"")),
                        message.getContent()));
                param.put("exceptionTitle",message.getTitle());
                param.put("projectBeName", PropertyUtils.getPropertyCache(HealthProperties.SpringApplictionName,StringUtils.EMPTY));
                HttpClient.Params params = HttpClient.Params.custom().setContentType(ContentType.APPLICATION_JSON).add(param).build();
                DefaultHttpClient.Default.post(PropertyUtils.getPropertyCache(exceptionUrl,StringUtils.EMPTY), params);
            });
        }
    }
    public static void reportException(EnumLevelType levelType,String title,String content){
        reportException(new Message(EnumWarnType.ERROR,title,content,levelType));
    }
}

