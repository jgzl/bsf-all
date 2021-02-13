package com.github.jgzl.bsf.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.github.jgzl.bsf.core.thread.ThreadPool;
import com.github.jgzl.bsf.core.util.LogUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 代码来自网络，二次修改
 * 参考: https://www.iteye.com/blog/shift-alt-ctrl-2399603
 * <p>
 * </p>
 * DATE 17/10/19.
 *
 * @author liuguanqing.
 */
public abstract class AbstractCanalConsumer {
    protected static final String SEP = SystemUtils.LINE_SEPARATOR;
    protected static String contextFormat = null;
    protected static String rowFormat = null;
    protected static String transactionFormat = null;
    protected static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    static {
        StringBuilder sb = new StringBuilder();
        sb.append(SEP)
                .append("-------------Batch-------------")
                .append(SEP)
                .append("* Batch Id: [%s] ,count : [%s] , Mem size : [%s] , Time : %s")
                .append(SEP)
                .append("* Start : [%s] ")
                .append(SEP)
                .append("* End : [%s] ")
                .append(SEP)
                .append("-------------------------------")
                .append(SEP);
        contextFormat = sb.toString();

        sb = new StringBuilder();
        sb.append(SEP)
                .append("+++++++++++++Row+++++++++++++>>>")
                .append("binlog[%s:%s] , name[%s,%s] , eventType : %s , executeTime : %s , delay : %sms")
                .append(SEP);
        rowFormat = sb.toString();

        sb = new StringBuilder();
        sb.append(SEP)
                .append("===========Transaction %s : %s=======>>>")
                .append("binlog[%s:%s] , executeTime : %s , delay : %sms")
                .append(SEP);
        transactionFormat = sb.toString();
    }

    protected volatile boolean running = false;
    //protected Thread   thread;

    /**
     * 日志使用标识
     */
    protected String logTag="canal consumer";
    /**
     * 使用zk进行CanalConnector配置管理
     */
    @Getter @Setter
    protected String zkServers;//cluster
    /**
     * 使用local文件进行CanalConnector配置管理
     * 格式: ip:端口
     */
    @Getter @Setter
    protected String address;//single，ip:port
    /**
     * 本次消费者canal的唯一标识(类似消息队列的唯一标识)
     */
    @Getter @Setter
    protected String destination;
    /**
     * 订阅库的用户名
     */
    @Getter @Setter
    protected String username;
    /**
     * 订阅库的密码
     */
    @Getter @Setter
    protected String password;
    /**
     * 消费者批次消费的批量数量
     */
    @Getter @Setter
    protected int batchSize = 1024;//
    /**
     *canal过滤相关表,同canal filter，用于过滤database或者table的相关数据。
     *1）所有表：.*   or  .*\\..*
     *2）canal schema下所有表： canal\\..*
     *3）canal下的以canal打头的表：canal\\.canal.*
     *4）canal schema下的一张表：canal.test1
     *5）多个规则组合使用：canal\\..*,mysql.test1,mysql.test2 (逗号分隔)
     */
    @Getter @Setter
    protected String filter = ".*";//同canal filter，用于过滤database或者table的相关数据。
    /**
     * 开启debug，会把每条消息的详情打印 测试环境建议开启，线上环境建议关闭
     */
    @Getter @Setter
    protected boolean debug = true;//开启debug，会把每条消息的详情打印

    /**
     * 消费异常处理策略
     * 1:retry，重试，重试默认为3次，由retryTimes参数决定，如果重试次数达到阈值，则跳过，并且记录日志。
     * 2:ignore,直接忽略，不重试，记录日志。
     */
    @Getter @Setter
    protected int exceptionStrategy = 1;
    /**
     * 消费异常情况下 retry模式下的重试次数设置
     */
    @Getter @Setter
    protected int retryTimes = 3;
    /**
     * 当binlog没有数据时，主线程等待的时间，单位ms,大于0
     */
    @Getter @Setter
    protected int waitingTime = 1000;//

    protected CanalConnector connector;

    /**
     * 强烈建议捕获异常
     * @param header
     * @param afterColumns
     */
    public abstract void insert(CanalEntry.Header header,List<CanalEntry.Column> afterColumns);

    /**
     * 强烈建议捕获异常
     * @param header
     * @param beforeColumns 变化之前的列数据
     * @param afterColumns 变化之后的列数据
     */
    public abstract void update(CanalEntry.Header header,List<CanalEntry.Column> beforeColumns,List<CanalEntry.Column> afterColumns);

    /**
     * 强烈建议捕获异常
     * @param header
     * @param beforeColumns 删除之前的列数据
     */
    public abstract void delete(CanalEntry.Header header,List<CanalEntry.Column> beforeColumns);

    /**
     * 创建表
     * @param header 可以从header中获得schema、table的名称
     * @param sql
     */
    public void createTable(CanalEntry.Header header,String sql) {
        String schema = header.getSchemaName();
        String table = header.getTableName();
        LogUtils.info(this.getClass(),logTag,String.format("parse event,create table,schema: %s,table: %s,SQL: %s",new String[] {schema,table,sql}));
    }

    /**
     * 修改表结构,即alter指令，需要声明：通过alter增加索引、删除索引，也是此操作。
     * @param header 可以从header中获得schema、table的名称
     * @param sql
     */
    public void alterTable(CanalEntry.Header header,String sql) {
        String schema = header.getSchemaName();
        String table = header.getTableName();
        LogUtils.info(this.getClass(),logTag,String.format("parse event,alter table,schema: %s,table: %s,SQL: %s",new String[] {schema,table,sql}));
    }

    /**
     * 清空、重建表
     * @param header 可以从header中获得schema、table的名称
     * @param sql
     */
    public void truncateTable(CanalEntry.Header header,String sql) {
        String schema = header.getSchemaName();
        String table = header.getTableName();
        LogUtils.info(this.getClass(),logTag,String.format("parse event,truncate table,schema: %s,table: %s,SQL: %s",new String[] {schema,table,sql}));
    }

    /**
     * 重命名schema或者table，注意
     * @param header 可以从header中获得schema、table的名称
     * @param sql
     */
    public void rename(CanalEntry.Header header,String sql) {
        String schema = header.getSchemaName();
        String table = header.getTableName();
        LogUtils.info(this.getClass(),logTag,String.format("parse event,rename table,schema: %s,table: %s,SQL: %s",new String[] {schema,table,sql}));
    }

    /**
     * 创建索引,通过“create index on table”指令
     * @param header 可以从header中获得schema、table的名称
     * @param sql
     */
    public void createIndex(CanalEntry.Header header,String sql) {
        String schema = header.getSchemaName();
        String table = header.getTableName();
        LogUtils.info(this.getClass(),logTag,String.format("parse event,create index,schema: %s,table: %s,SQL: %s",new String[] {schema,table,sql}));
    }

    /**
     * 删除索引，通过“delete index on table”指令
     * @param header      * 可以从header中获得schema、table的名称
     * @param sql
     */
    public void deleteIndex(CanalEntry.Header header,String sql) {
        String schema = header.getSchemaName();
        String table = header.getTableName();
        LogUtils.info(this.getClass(),logTag,String.format("parse event,delete table,schema: %s,table: %s,SQL: %s",new String[] {schema,table,sql}));
    }

    /**
     * 强烈建议捕获异常，非上述已列出的其他操作，非核心
     * 除了“insert”、“update”、“delete”操作之外的，其他类型的操作.
     * 默认实现为“无操作”
     * @param entry
     */
    public void whenOthers(CanalEntry.Entry entry) {
    }

    public void connectorBatchArk(long batchId){

    }

    public synchronized void start() {
        if(waitingTime <= 0 ) {
            throw new IllegalArgumentException("waitingTime must be greater than 0");
        }
        if(ExceptionStrategy.codeOf(exceptionStrategy) == null) {
            throw new IllegalArgumentException("exceptionStrategy is not valid,1 or 2");
        }

        if(running) {
            return;
        }
        running = true;
        if(zkServers != null && zkServers.length() > 0) {
            connector = CanalConnectors.newClusterConnector(zkServers,destination,username,password);
            
        } else if (address != null){
            String[] segments = address.split(":");
            SocketAddress socketAddress = new InetSocketAddress(segments[0],Integer.valueOf(segments[1]));
            connector = CanalConnectors.newSingleConnector(socketAddress,destination,username,password);
        } else {
            throw new IllegalArgumentException("zkServers or address cant be null at same time,you should specify one of them!");
        }         
        ThreadPool.System.submit("canal同步任务",()->{process();});

    }


    protected synchronized void stop() {
        if (!running) {
            return;
        }
        //MDC.remove("destination");
        running = false;//process()将会在下一次loop时退出
    }
    /**
     *
     * 用于控制当连接异常时，重试的策略，我们不应该每次都是立即重试，否则将可能导致大量的错误，在空转时导致CPU过高的问题
     * sleep策略基于简单的累加，最长不超过3S
     */
    private void sleepWhenFailed(int times) {
        if(times <= 0) {
            return;
        }
        try {
            if(times > 20) {
                times = 0;
            }
            int sleepTime = 1000 + times * 100;//最大sleep 3s。
            Thread.sleep(sleepTime);
        } catch (Exception ex) {
            //
        }
    }

    protected void process() {
        int times = 0;
        while (running) {
            try {
                sleepWhenFailed(times);
                //after block,should check the status of thread.
                if(!running) {
                    break;
                }
                MDC.put("destination", destination);
                connector.connect();
                connector.subscribe(filter);               
                times = 0;//reset;
                
                while (running) {
                    Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据，不确认
                    long batchId = message.getId();
                    int size = message.getEntries().size();
                    if (batchId == -1 || size == 0) {
                        try {
                            Thread.sleep(waitingTime);
                        } catch (InterruptedException e) {
                            //
                        }
                        continue;
                    }
                    //logger
                    printBatch(message, batchId);

                    //遍历每条消息
                    for(CanalEntry.Entry entry : message.getEntries()) {
                        session(entry);//no exception
                    }
                    //ack all the time。
                    connectorBatchArk(batchId);
                    connector.ack(batchId);
                }
            } catch (Exception e) {
                LogUtils.error(this.getClass(),logTag,"process error!", e);
                times++;
            } finally {
                connector.disconnect();
                MDC.remove("destination");
            }
        }
    }

    protected void session(CanalEntry.Entry entry) {
        CanalEntry.EntryType entryType = entry.getEntryType();
        int times = 0;
        boolean success = false;
        while (!success) {
            if(times > 0) {
                /**
                 * 1:retry，重试，重试默认为3次，由retryTimes参数决定，如果重试次数达到阈值，则跳过，并且记录日志。
                 * 2:ignore,直接忽略，不重试，记录日志。
                 */
                if (exceptionStrategy == ExceptionStrategy.RETRY.code) {
                    if(times >= retryTimes) {
                        break;
                    }
                } else {
                    break;
                }
            }
            try {
                switch (entryType) {
                    case TRANSACTIONBEGIN:
                        transactionBegin(entry);
                        break;
                    case TRANSACTIONEND:
                        transactionEnd(entry);
                        break;
                    case ROWDATA:
                        rowData(entry);
                        break;
                    default:
                        break;
                }
                success = true;
            } catch (Exception e) {
                times++;
                LogUtils.error(this.getClass(),logTag,("parse event has an error ,times: + " + times + ", data:" + entry.toString()), e);
            }

        }

        if(debug && success) {
            LogUtils.info(this.getClass(),logTag,("parse event success,position:" + entry.getHeader().getLogfileOffset()));
        }
    }

    private void rowData(CanalEntry.Entry entry) throws Exception {
        CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
        CanalEntry.EventType eventType = rowChange.getEventType();
        CanalEntry.Header header = entry.getHeader();
        long executeTime = header.getExecuteTime();
        long delayTime = new Date().getTime() - executeTime;
        String sql = rowChange.getSql();
        if(debug) {
            if (eventType == CanalEntry.EventType.QUERY || rowChange.getIsDdl()) {
                LogUtils.info(this.getClass(),logTag,String.format("------SQL----->>> type : %s , sql : %s ", new Object[]{eventType.getNumber(), sql}));
            }
            LogUtils.info(this.getClass(),logTag,String.format(rowFormat,
                    new Object[]{
                            header.getLogfileName(),
                            String.valueOf(header.getLogfileOffset()),
                            header.getSchemaName(),
                            header.getTableName(),
                            eventType,
                            String.valueOf(executeTime),
                            String.valueOf(delayTime)
                    }));
        }

        try {
            //对于DDL，直接执行，因为没有行变更数据
            switch (eventType) {
                case CREATE:
                    createTable(header,sql);
                    return;
                case ALTER:
                    alterTable(header,sql);
                    return;
                case TRUNCATE:
                    truncateTable(header,sql);
                    return;
                case ERASE:
                    LogUtils.info(this.getClass(),logTag,"parse event : erase,ignored!");
                    return;
                case QUERY:
                    LogUtils.info(this.getClass(),logTag,"parse event : query,ignored!");
                    return;
                case RENAME:
                    rename(header,sql);
                    return;
                case CINDEX:
                    createIndex(header,sql);
                    return;
                case DINDEX:
                    deleteIndex(header,sql);
                    return;
                default:
                    break;
            }
            //对于有行变更操作的
            for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                switch (eventType) {
                    case DELETE:
                        delete(header, rowData.getBeforeColumnsList());
                        break;
                    case INSERT:
                        insert(header, rowData.getAfterColumnsList());
                        break;
                    case UPDATE:
                        update(header, rowData.getBeforeColumnsList(), rowData.getAfterColumnsList());
                        break;
                    default:
                        whenOthers(entry);
                }
            }
        } catch (Exception e) {
            LogUtils.error(this.getClass(),logTag,"process event error ,",e);
            LogUtils.error(this.getClass(),logTag,String.format(rowFormat,
                    new Object[]{
                            header.getLogfileName(),
                            String.valueOf(header.getLogfileOffset()),
                            header.getSchemaName(),
                            header.getTableName(),
                            eventType,
                            String.valueOf(executeTime),
                            String.valueOf(delayTime)
                    }),e);
            throw e;//重新抛出
        }
    }

    /**
     * default，only logging information
     * @param entry
     */
    public void transactionBegin(CanalEntry.Entry entry) {
        if(!debug) {
            return;
        }
        try {
            CanalEntry.TransactionBegin begin = CanalEntry.TransactionBegin.parseFrom(entry.getStoreValue());
            // 打印事务头信息，执行的线程id，事务耗时
            CanalEntry.Header header = entry.getHeader();
            long executeTime = header.getExecuteTime();
            long delayTime = new Date().getTime() - executeTime;
            LogUtils.info(this.getClass(),logTag,String.format(transactionFormat,
                    new Object[] {
                            "begin",
                            begin.getTransactionId(),
                            header.getLogfileName(),
                            String.valueOf(header.getLogfileOffset()),
                            String.valueOf(header.getExecuteTime()),
                            String.valueOf(delayTime)
                    }));
        } catch (Exception e) {
            LogUtils.error(this.getClass(),logTag,"parse event has an error , data:" + entry.toString(), e);
        }
    }

    public void transactionEnd(CanalEntry.Entry entry) {
        if(!debug) {
            return;
        }
        try {
            CanalEntry.TransactionEnd end = CanalEntry.TransactionEnd.parseFrom(entry.getStoreValue());
            // 打印事务提交信息，事务id
            CanalEntry.Header header = entry.getHeader();
            long executeTime = header.getExecuteTime();
            long delayTime = new Date().getTime() - executeTime;
            LogUtils.info(this.getClass(),logTag,String.format(transactionFormat,
                    new Object[]{
                            "end",
                            end.getTransactionId(),
                            header.getLogfileName(),
                            String.valueOf(header.getLogfileOffset()),
                            String.valueOf(header.getExecuteTime()),
                            String.valueOf(delayTime)
                    }));
        } catch (Exception e) {
            LogUtils.error(this.getClass(),logTag,"parse event has an error , data:" + entry.toString(), e);
        }
    }


    /**
     * 打印当前batch的摘要信息
     * @param message
     * @param batchId
     */
    protected void printBatch(Message message, long batchId) {
        List<CanalEntry.Entry> entries = message.getEntries();
        if(CollectionUtils.isEmpty(entries)) {
            return;
        }

        long memSize = 0;
        for (CanalEntry.Entry entry : entries) {
            memSize += entry.getHeader().getEventLength();
        }
        int size = entries.size();
        String startPosition = buildPosition(entries.get(0));
        String endPosition = buildPosition(message.getEntries().get(size - 1));

        LogUtils.info(this.getClass(),logTag,String.format(contextFormat, new Object[] {
                batchId,
                size,
                memSize,
                new SimpleDateFormat(DATE_FORMAT).format(new Date()),
                startPosition,
                endPosition }
        ));
    }

    protected String buildPosition(CanalEntry.Entry entry) {
        CanalEntry.Header header = entry.getHeader();
        long time = header.getExecuteTime();
        Date date = new Date(time);
        StringBuilder sb = new StringBuilder();
        sb.append(header.getLogfileName())
                .append(":")
                .append(header.getLogfileOffset())
                .append(":")
                .append(header.getExecuteTime())
                .append("(")
                .append(new SimpleDateFormat(DATE_FORMAT).format(date))
                .append(")");
        return sb.toString();
    }

    enum ExceptionStrategy {
        RETRY(1),
        IGNORE(2);
        int code;
        ExceptionStrategy(int code) {
            this.code = code;
        }
        public static ExceptionStrategy codeOf(Integer code) {
            if(code == null) {
                return null;
            }
            for(ExceptionStrategy e : ExceptionStrategy.values()) {
                if(e.code == code) {
                    return e;
                }
            }
            return null;
        }
    }
}