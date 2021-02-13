package com.github.jgzl.bsf.elk.requestid;

import java.util.UUID;

import com.github.jgzl.bsf.core.util.WebUtils;

/**
 * @author: chejiangyi
 * @version: 2020-01-14 09:53
 **/
public class RequestUtil {
	/**
	 * HTTP请求trace ID,用于调用链日志追踪
	 * */
    public static final String REQUEST_TRACEID = "traceid";
    /**
     * 	获取请求header中的的traceid，如果没有则生成一个UUID
     * */
    public static String getRequestId(){
        String requestid = null;        
        if(WebUtils.getRequest()!=null) {
            requestid = WebUtils.getRequest().getHeader(REQUEST_TRACEID);   
        }      
        if (requestid == null) {
        	requestid=UUID.randomUUID().toString();
        }
        return requestid;
    }
}
