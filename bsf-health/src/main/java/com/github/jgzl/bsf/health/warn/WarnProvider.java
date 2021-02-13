package com.github.jgzl.bsf.health.warn;

import com.github.jgzl.bsf.core.thread.ThreadPool;
import com.github.jgzl.bsf.core.util.LogUtils;
import com.github.jgzl.bsf.core.util.NetworkUtils;
import com.github.jgzl.bsf.core.util.PropertyUtils;
import com.github.jgzl.bsf.core.util.StringUtils;
import com.github.jgzl.bsf.health.base.AbstractWarn;
import com.github.jgzl.bsf.health.base.EnumWarnType;
import com.github.jgzl.bsf.health.base.Message;
import com.github.jgzl.bsf.health.config.HealthProperties;
import com.github.jgzl.bsf.health.config.WarnProperties;
import com.github.jgzl.bsf.health.utils.ExceptionUtils;
import lombok.val;
import org.springframework.boot.ApplicationArguments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: chejiangyi
 * @version: 2019-07-23 20:20
 **/
public class WarnProvider extends AbstractWarn implements AutoCloseable,org.springframework.boot.ApplicationRunner {
    private ConcurrentLinkedDeque<Message> messages = new ConcurrentLinkedDeque<>();
    private AtomicInteger atomicInteger = new AtomicInteger(0);
    private Object lock = new Object();
    private List<AbstractWarn> warns = new ArrayList<>();
    private boolean isClose=false;
    private DuplicateFilter duplicateFilter = new DuplicateFilter();
    private AtomicBoolean atomicChannel = new AtomicBoolean(false);
    public WarnProvider()
    {
        isClose=false;
        RegisterWarn();
        ThreadPool.System.submit("bsf系统任务:WarnProvider 实时报警任务",()->{
            while (!ThreadPool.System.isShutdown()&&!isClose) {
                try {
                    notifyRunning();
                } catch (Exception exp) {
                    LogUtils.warn(WarnProvider.class, HealthProperties.Project, "WarnProvider 消息循环异常");
                }
                try{Thread.sleep(WarnProperties.Default().getBsfHealthWarnTimeSpan()*1000);}catch (Exception e){}
            }
        });
    }
    public void RegisterWarn(){
        if("true".equals(PropertyUtils.getPropertyCache("bsf.message.dingding.enabled","false"))){
            warns.add(new DingdingWarn());
        }
        if("true".equals(PropertyUtils.getPropertyCache("bsf.message.flybook.enabled","false"))){
            warns.add(new FlyBookWarn());
        }
    }

    public void ClearWarn(){
        warns.clear();
    }

    private void notifyRunning(){
        Message temp = new Message();
       val allmsgs = getAllmessage();int msgscount = atomicInteger.getAndSet(0);temp.setWarnType(EnumWarnType.WARN);
       if(msgscount>0) {
           StringBuilder content = new StringBuilder();
           content.append(String.format("最新报警累计:%s条,详情请查看日志系统,最后%s条报警内容如下:\n", msgscount, WarnProperties.Default().getBsfHealthWarnCacheCount()));
           allmsgs.forEach(c -> {
               if (c.getWarnType().getLevel() > (temp.getWarnType()).getLevel()) {
                   temp.setWarnType(c.getWarnType());
               }
               content.append(String.format("[%s][%s]内容%s\n", c.getWarnType().getDescription(), c.getTitle(), c.getContent()));
           });
           temp.setTitle(String.format("收到%s条报警", msgscount));
           temp.setContent(content.toString());
           notifynow(temp);
       }
    }

    @Override
    public void notify(Message message) {
        addMessage(message);
    }
    /**
     * 	方法重载
     * */
    public void notify(String warnType, String title,String content)
    {
    	Message message=new Message(EnumWarnType.valueOf(warnType),title,content);
    	addMessage(message);
    }
    private void addMessage(Message msg){
        atomicInteger.getAndIncrement();
        synchronized(lock) {
            //加锁
            messages.add(msg);
            //清理多余
            if (messages.size() > WarnProperties.Default().getBsfHealthWarnCacheCount()) {
                for (int i = 0; i < messages.size() - WarnProperties.Default().getBsfHealthWarnCacheCount(); i++) {
                    if (!messages.isEmpty()) {
                        messages.removeFirst();
                    }
                }
            }
        }
    }

    private List<Message> getAllmessage()
    {
        List<Message> msgs = new ArrayList<>();
        synchronized(lock) {
            messages.forEach(c->{msgs.add(c);});
            messages.clear();
        }
        return msgs;
    }

    public void notifynow(Message message)
    {
    	notifyMessage0(message);
    }
    /**
     * 	方法重载
     * */
    public void notifynow(String warnType, String title,String content)
    {
    	Message message=new Message(EnumWarnType.valueOf(warnType),title,content);
    	notifyMessage0(message);
    }
    
    /**
     * 	方法私有化，避免重载方法循环调用
     * */
    private void notifyMessage0(Message message)
    {
          String title=message.getTitle();
    	  message.setTitle(String.format("[%s][%s][%s][%s]%s",
                  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                  NetworkUtils.getIpAddress(),
                  PropertyUtils.getPropertyCache(HealthProperties.BsfEnv,""),
                  StringUtils.nullToEmpty(PropertyUtils.getPropertyCache(HealthProperties.SpringApplictionName,"")),
                  StringUtils.nullToEmpty(message.getTitle())));
          if(!duplicateFilter.ifDuplicat(message.getContent()) && atomicChannel.get()){
              if(message!=null && EnumWarnType.ERROR==message.getWarnType()) ExceptionUtils.reportException(message.getLevelType(),title,message.getContent());
              for(AbstractWarn warn:warns){
                  warn.notify(message);
              }
          }
    }
    
    @Override
    public void close(){
        isClose=true;
    }

    @Override
    public void run(ApplicationArguments args) {
        atomicChannel.getAndSet(true);
        LogUtils.info(WarnProvider.class, HealthProperties.Project, "开启消息通道");
    }

    /**
     * 简单重复过滤算法
     * 去除数值并hash
     */
    private class DuplicateFilter{
        private int cacheMax = 100;
        private volatile List<Integer> cacheTag = new ArrayList(cacheMax+5);
        private long lastClearTime = System.currentTimeMillis();
        public boolean ifDuplicat(String message){
            int hash = StringUtils.nullToEmpty(message).replaceAll("\\d+","").hashCode();
            /*超过1分钟清理*/
            if(System.currentTimeMillis()-lastClearTime>TimeUnit.MINUTES.toMillis(WarnProperties.Default().getBsfHealthWarnDuplicateTimeSpan())) {
                cacheTag.clear();
                lastClearTime=System.currentTimeMillis();
            }
            /*过长清理*/
            if(cacheTag.size()>=cacheMax) {
                cacheTag.clear();
            }
            if (!cacheTag.contains(hash)) {
                cacheTag.add(hash);
                return false;
            }
            return true;
        }
    }

}
