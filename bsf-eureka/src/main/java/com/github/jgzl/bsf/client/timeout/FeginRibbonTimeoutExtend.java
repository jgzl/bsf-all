package com.github.jgzl.bsf.client.timeout;

import java.lang.reflect.Method;

import com.github.jgzl.bsf.core.util.ClassPoolUtils;
import com.github.jgzl.bsf.core.util.LogUtils;
import com.github.jgzl.bsf.core.util.PropertyUtils;
import com.github.jgzl.bsf.core.util.ReflectionUtils;
import com.github.jgzl.bsf.client.EurekaMonitor;
import com.github.jgzl.bsf.client.EurekaProperties;

import feign.Feign;
import feign.Request;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import lombok.val;

/**
 * @author: chejiangyi
 * @version: 2020-01-14 15:09
 **/
public class FeginRibbonTimeoutExtend {
    private static String newMethodCode() {
        String code = "{" +
                "       String params=com.github.jgzl.bsf.client.timeout.FeginRibbonTimeoutExtend.getTimeOutInfo(this.target,this.metadata.configKey());\n " +
                "		if(null!=params&&!\"\".equals(params)){\n"+
                "       	com.github.jgzl.bsf.client.timeout.FeginRibbonTimeoutExtend.setOption(this.options,\"connectTimeoutMillis\",Integer.valueOf(params.split(\",\")[0]));\n" +
                "       	com.github.jgzl.bsf.client.timeout.FeginRibbonTimeoutExtend.setOption(this.options,\"readTimeoutMillis\",Integer.valueOf(params.split(\",\")[1]));\n" +
                "		}"+		
                "       return $0.invokeOld($$);\n" +
                "}";
        return code;
    }
    private volatile static boolean isload = false;

    public static void setTimeoutHook() {
        if(!PropertyUtils.getPropertyCache("ribbon.timeout.extend.enabled",false)){
            return;
        }
        try {
            ClassPool classPool = ClassPoolUtils.getInstance();
            CtClass ctClass = classPool.get("feign.SynchronousMethodHandler");
            if (!isload) {
                isload = true;
                CtMethod ctMethod = ctClass.getDeclaredMethod("invoke");
                CtMethod mold = CtNewMethod.copy(ctMethod, "invokeOld", ctClass, null);
                ctClass.addMethod(mold);
                ctMethod.setBody(newMethodCode());

                if (ctClass.isFrozen()) {
                    ctClass.defrost();
                }
                ctClass.toClass();
                LogUtils.info(EurekaMonitor.class, EurekaProperties.Project, "注入feign.SynchronousMethodHandler ok");
            }
        } catch (Exception exp) {
            LogUtils.error(EurekaMonitor.class,  EurekaProperties.Project, "注入feign.SynchronousMethodHandler 异常", exp);
        }
    }

    public static String getTimeOutInfo(Object type,String configKey){
        Method[] methods= type.getClass().getDeclaredMethods();        
        for(int i=0;i<=methods.length;i++)
        {
        	if(configKey.equals(Feign.configKey(type.getClass(), methods[i])))
        	{
        		 val an = methods[i].getAnnotation(FeginRibbonTimeout.class);
        		 if(an!=null)
        		 {
        			 return an.connectTimeout()+","+an.readTimeout();
        		 }
        	}
        }
        return "";
    }

    public static void setOption(Request.Options options,String filedName,Object value){
        val filed = ReflectionUtils.findField(options.getClass(),filedName);
        ReflectionUtils.setFieldValue(filed,options,value);
    }
}
