package com.github.jgzl.bsf.elk.requestid;

import org.slf4j.MDC;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * Feign HTTP调用请求头增加RequestUtil.REQUEST_TRACEID参数
 * @author: lihaifeng
 * @version: 2019-08-30 13:21
 **/
public class FeginRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
    	if(MDC.get(RequestUtil.REQUEST_TRACEID)==null) {
    		MDC.put(RequestUtil.REQUEST_TRACEID, RequestUtil.getRequestId());
    	}
        requestTemplate.header(RequestUtil.REQUEST_TRACEID, MDC.get(RequestUtil.REQUEST_TRACEID)); 
    }
}
