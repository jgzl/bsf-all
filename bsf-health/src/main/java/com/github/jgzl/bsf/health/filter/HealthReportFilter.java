package com.github.jgzl.bsf.health.filter;

import com.github.jgzl.bsf.core.util.ContextUtils;
import com.github.jgzl.bsf.core.util.LogUtils;
import com.github.jgzl.bsf.core.util.PropertyUtils;
import com.github.jgzl.bsf.health.base.Report;
import com.github.jgzl.bsf.health.collect.HealthCheckProvider;
import com.github.jgzl.bsf.health.config.HealthProperties;
import lombok.val;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: lihaifeng
 * @version: 2019-07-25 21:51
 **/
public class HealthReportFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        val request = (HttpServletRequest) servletRequest;
        val response = (HttpServletResponse) servletResponse;
    	if(request.getRequestURI().equalsIgnoreCase(org.springframework.util.StringUtils.trimTrailingCharacter( request.getContextPath(),'/')+"/bsf/health/")){
        	try
            {
                val healthProvider = ContextUtils.getBean(HealthCheckProvider.class,false);
                String html;
                if(healthProvider!=null)
                {
                    Report report =null;
                    boolean isAnalyse = true;
                    if("false".equalsIgnoreCase(request.getParameter("isAnalyse"))) {
                       isAnalyse = false;
                    }
                    report = healthProvider.getReport(isAnalyse);
                    if(request.getContentType()!=null&& request.getContentType().contains("json")) {
                        response.setHeader("Content-type", "application/json;charset=UTF-8");
                       html = report.toJson();
                    }else
                    {
                        response.setHeader("Content-type", "text/html;charset=UTF-8");
                        html = report.toHtml().replace("\r\n","<br/>").replace("\n","<br/>").replace("/n","\n").replace("/r","\r");
                        if(PropertyUtils.getPropertyCache("bsf.health.dump.enabled",false)) {
                            html = "dump信息:<a href='dump/'>查看</a><br/>" + html;
                        }
                    }
                }
                else
                {
                    response.setHeader("Content-type", "text/html;charset=UTF-8");
                    html = "请配置bsf.health.enabled=true,bsf.health.check.enabled=true";
                }

                response.setCharacterEncoding("UTF-8");
                response.getWriter().append(html);
                response.getWriter().flush();
                response.getWriter().close();
            }
            catch (Exception e){
                LogUtils.error(HealthReportFilter.class, HealthProperties.Project,"/bsf/health/打开出错",e);
                response.getWriter().close();
            }
        }
    }

    @Override
    public void destroy() {

    }
}
