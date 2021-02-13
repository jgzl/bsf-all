package com.github.jgzl.bsf.health.warn;

import com.github.jgzl.bsf.core.util.*;
import com.github.jgzl.bsf.health.base.AbstractWarn;
import com.github.jgzl.bsf.health.base.Message;
import com.github.jgzl.bsf.health.config.WarnProperties;
import com.github.jgzl.bsf.message.flybook.FlyBookProvider;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: huojuncheng
 * @version: 2020-08-25 20:13
 **/
public class FlyBookWarn extends AbstractWarn {
    boolean driverExsit=false;
    public FlyBookWarn(){
        driverExsit  = ReflectionUtils.tryClassForName("com.github.jgzl.bsf.message.flybook.FlyBookProvider")!=null;
    }

    @Override
    public void notify(Message message) {
        if(!driverExsit){
            LogUtils.error(FlyBookWarn.class,"health","未找到FlyBookProvider",new Exception("不支持飞书预警"));
            return;
        }
        FlyBookProvider flyBookProvider = ContextUtils.getBean(FlyBookProvider.class,false);
        if(flyBookProvider!=null) {
            val ip = NetworkUtils.getIpAddress();
            if(!StringUtils.isEmpty(ip)&&!WarnProperties.Default().getBsfHealthWarnFlybookFilterIP().contains(ip)) {
                List<String> tokens = new ArrayList<>();
                tokens.addAll(Arrays.asList(WarnProperties.Default().getBsfHealthWarnFlybookSystemAccessToken().split(",")));
                tokens.addAll(Arrays.asList(WarnProperties.Default().getBsfHealthWarnFlybookProjectAccessToken().split(",")));
                flyBookProvider.sendText(tokens.toArray(new String[tokens.size()]),
                         StringUtils.subString3(message.getTitle(), 100),
                        StringUtils.subString3(message.getTitle(), 100) + "\n" +
                                "详情:" + WebUtils.getBaseUrl()+"/bsf/health/\n"+
                                "【" + message.getWarnType().getDescription() + "】"+StringUtils.subString3(message.getContent(), 500));
            }
        }
    }
}
