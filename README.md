# bsf-all
## 介绍
BSF 为 base service framework 的简写，定义为技术团队的基础框架,用于基础服务的集成和跟业务无关的基础技术集成。

BSF集成了自研的监控报警，用来监控各个服务系统的性能及异常告警。集成并封装Apollo,Rocket MQ,Redis, Elastic Search,ELK,XXLJOB, Sharding JDBC,Cat,Eureka,七牛云等第三方中间件，提供简易使用的底层框架。

## 愿景
为了更好地支持业务开发，让开发人员从中间件中解放出来，专注业务以提高开发效率。同时基础框架集中统一优化中间件相关服务及使用，为开发人员提供高性能,更方便的基础服务接口及工具。

## 项目结构规范说明
```
bsf-all 
    -- bsf-core (项目核心类库)
    -- bsf-demo (项目集成使用demo)
    -- bsf-dependencies (项目依赖pom定义)
        -- README.md (说明文档，必须有)
    -- bsf-starter （项目full-start包）
    -- bsf-elk (ELK集成)
    -- bsf-job (XXL-JOB集成)
    -- bsf-cat (CAT监控集成)
    -- bsf-apollo (Apollo配置中心集成)
    -- bsf-message (消息-短信-钉钉消息集成)
    -- bsf-shardingjdbc (分库分表ShardingJDBC 集成) 
    -- bsf-mq (消息队列Rocket MQ集成) 
    -- bsf-redis(缓存Redis集成)
    -- bsf-eureka(服务注册与发现集成)
    -- bsf-file（文件服务集成）
    -- bsf-elasticsearch(ES集成) 
    -- bsf-health（自研健康检查） 
    -- bsf-transaction（努力送达事务） 
    -- 框架名 (例如:bsf-elk,cat,apollo等)
```

## 相关文档
本系统个子模块分别集成分装了对应中间件服务，文档如下：
1. [bsf-core](bsf-core/README.md)
2. [bsf-demo](bsf-demo/README.md)
3. [bsf-dependencies](bsf-dependencies/README.md)
4. [bsf-starter](bsf-starter/README.md)
5. [bsf-elk](bsf-elk/README.md) 
6. [bsf-job](bsf-job/README.md) 
7. [bsf-cat](bsf-cat/README.md) 
8. [bsf-apollo](bsf-apollo/README.md) 
9. [bsf-message](bsf-message/README.md) 
10. [bsf-shardingjdbc](bsf-shardingjdbc/README.md) 
11. [bsf-mq](bsf-mq/README.md) 
12. [bsf-redis](bsf-redis/README.md) 
13. [bsf-eureka](bsf-eureka/README.md) 
14. [bsf-file](bsf-file/README.md) 
15. [bsf-elasticsearch](bsf-elasticsearch/README.md) 
16. [bsf-health](bsf-health/README.md) 
17. [bsf-transaction](bsf-transaction/README.md) 

## 编译说明

注意：
    首次下载代码,首先构建bsf-dependencies模块，然后在构建整个工程。

```shell 
	mvn install bsf-dependencies
	mvn install bsf-all
```
如果缺少JAR包，请下载resources下的[elasticsearch-sql-6.7.1.0.zip](resources/elasticsearch-sql-6.7.1.0.zip)


## 版本升级/切换
```
备注: 格式:1.0.0-RELEASE (版本号+-+RELEASE/SNAPSHOT) 
cd bsf-dependencies
mvn versions:set -DgenerateBackupPoms=false
或
mvn versions:set -DgenerateBackupPoms=false -DnewVersion={version}
```

## 使用说明

1. 依赖引用

    继承bsf-dependencies
``` 
    <parent>
        <groupId>com.yh.csx.bsf</groupId>
        <artifactId>bsf-dependencies</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
```
    或者引入依赖 bsf-starter
```
    <dependency>
        <groupId>com.yh.csx.bsf</groupId>
        <artifactId>bsf-dependencies</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
    <dependency>
       <artifactId>bsf-starter</artifactId>
       <groupId>com.yh.csx.bsf</groupId>
       <version>1.0.0-SNAPSHOT</version>
    </dependency>
```

2. Demo程序  
    框架的使用demo，请参考[bsf-demo](bsf-demo)  
    各个组件的使用，请参考相关模块文档。

3. 参考properties配置文件  
    [resources/application.properties](resources/application.properties)

## 升级说明

    1.1  bsf.file.retryUpload 增加七牛云努力上传重试机制，默认3次

## 参与贡献
架构师: [李海峰](https://github.com/jgzl)  
开发: 李海峰

维护: 李海峰  

##### by lihaifeng