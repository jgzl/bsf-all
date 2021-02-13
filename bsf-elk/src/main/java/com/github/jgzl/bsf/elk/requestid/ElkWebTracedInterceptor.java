package com.github.jgzl.bsf.elk.requestid;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import com.github.jgzl.bsf.core.util.WebUtils;


/**
 *	通过设置MDC，记录traceid来追踪请求处理
 * @author Robin.Wang
 * @date	2020-01-15
 *
 */
public class ElkWebTracedInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		//设置请求traceid
		WebUtils.bindContext(request, response);
		MDC.put(RequestUtil.REQUEST_TRACEID, RequestUtil.getRequestId());
		return true;
	}
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable Exception ex) throws Exception {
		MDC.clear();
	}
}

