package com.github.jgzl.bsf.demo.message;


import com.github.jgzl.bsf.core.util.WarnUtils;
import com.github.jgzl.bsf.health.base.EnumLevelType;
import com.github.jgzl.bsf.health.utils.ExceptionUtils;
import com.github.jgzl.bsf.message.dingding.DingdingProvider;
import com.github.jgzl.bsf.message.flybook.FlyBookProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
@RestController
public class MessageApplication {
    @Autowired(required =false)
    private DingdingProvider dingdingProvider;

    @Autowired(required =false)
    private FlyBookProvider flyBookProvider;

    public static void main(String[] args){
        SpringApplication.run(MessageApplication.class, args);
    }
    @GetMapping("/dingding/{content}")
    public void sendDingding(@PathVariable  String content) throws Exception {
        dingdingProvider.sendText(new String[]{},"测试",content);

    }
    @GetMapping("/flybook/{content}")
    public void sendFlybook(@PathVariable String content) throws Exception {
        ExceptionUtils.reportException(EnumLevelType.HIGN,"title","content");
        WarnUtils.notifynow(WarnUtils.ALARM_ERROR, "TITLE", "CONTENT");
        flyBookProvider.sendText(new String[]{"7d81fe4d-da9d-4145-b289-6bd954edf117"},"测试",content);

    }
    @GetMapping("/notifynow/{content}")
    public void notifynow(@PathVariable String content) throws Exception {
        WarnUtils.notifynow("ERROR", "测试", content);
    }


}