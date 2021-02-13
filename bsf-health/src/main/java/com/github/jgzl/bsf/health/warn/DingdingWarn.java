package com.github.jgzl.bsf.health.warn;

import com.github.jgzl.bsf.core.util.*;
import com.github.jgzl.bsf.health.base.AbstractWarn;
import com.github.jgzl.bsf.health.base.Message;
import com.github.jgzl.bsf.health.config.WarnProperties;
import com.github.jgzl.bsf.message.dingding.DingdingProvider;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: lihaifeng
 * @version: 2019-07-23 20:13
 **/
public class DingdingWarn extends AbstractWarn {
    boolean driverExsit=false;
    public DingdingWarn(){
        driverExsit  = ReflectionUtils.tryClassForName("com.github.jgzl.bsf.message.dingding.DingdingProvider")!=null;
    }

    @Override
    public void notify(Message message) {
        if(!driverExsit){
            LogUtils.error(DingdingWarn.class,"health","未找到DingdingProvider",new Exception("不支持钉钉预警"));
            return;
        }
        DingdingProvider dingdingProvider = ContextUtils.getBean(DingdingProvider.class,false);
        if(dingdingProvider!=null) {
            val ip = NetworkUtils.getIpAddress();
            if(!StringUtils.isEmpty(ip)&&!WarnProperties.Default().getBsfHealthWarnDingdingFilterIP().contains(ip)) {
                List<String> tokens = new ArrayList<>();
                tokens.addAll(Arrays.asList(WarnProperties.Default().getBsfHealthWarnDingdingSystemAccessToken().split(",")));
                tokens.addAll(Arrays.asList(WarnProperties.Default().getBsfHealthWarnDingdingProjectAccessToken().split(",")));
                dingdingProvider.sendText(tokens.toArray(new String[tokens.size()]),
                        "【" + message.getWarnType().getDescription() + "】" + StringUtils.subString3(message.getTitle(), 100),
                        StringUtils.subString3(message.getTitle(), 100) + "\n" +
                                "详情:" + WebUtils.getBaseUrl()+"/bsf/health/\n"+
                                StringUtils.subString3(message.getContent(), 500));
            }
        }
    }
}
