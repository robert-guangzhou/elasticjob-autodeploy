# elasticjob-autodeploy基本介绍
automatic  deploy elasticjob's job from definition in database , include  deploy job implemented by spring bean, current only support simpleJob


根据数据库的定义，自动部署elasticjob的作业，支持自动部署spring bean模式的作业。当前只支持simple job，不支持dataflow 及shell job。

主要适用以下场景

 - 依托jobParameter 实现不同任务的执行，并且需要经常进行变更，如报表类job
 - 需要分类管控job运行环境，如按应用类型不同的job部署到不同机器、连接不同数据源的job分开部署等
 - 优雅下线job进程
 

目前版本居于elasticjob-lite 2.1.4 进行修改和扩展。

为支持自动部署，为simple job扩展了一个属性：job group，job group值默认为default，对于不使用本软件部署的原始的elastic job会自动归属到default组。

同时为更好跟踪job状态，在job root path下增加了一个status node记录状态，状态定义和elasticjob原生是一样的，具体如下：

```java
public final static String Shutdown="SHUTDOWN";
public final static String Disabled="DISABLED";
public final static String Removed="REMOVED";
public final static String Enabled="ENABLED";
```

 
 不同的是原生elasticjob状态定义在server/worker或者instances上。

每一个运行job的实例进程称为worker，worker上只运行

 1. job属于worker支持group，一个worker可以同时支持多个group，配置属性为autodeploy.elasticjob.supportGroups，默认只为default   
2、autodeploy.elasticjob.syncFromDB=true时候，运行从数据库定时同步job到zookeeper，在同一组应用/namespace下建议autodeploy.elasticjob.syncFromDB=true的进程不超过3个。


## 自动部署逻辑

 1. 监听elastic job 在zk上的root path，即elasticjob的namespace 节点
 2. 判断监听事件是否为新增job、job状态变化、job instance 丢失
 3.  如果属于上述情况再判断归属组是否属于本worker支持的组
 
 满足以上条件后，再通过以下逻辑进行是否进行部署
 
 4. 先查找job执行类，不存在不部署
 5.  job instance 数量是否不多余分片数量加1
 
 在实际部署时候
 
 6. 通过获取zk latch 避免多worker重复执行导致instance 数量过多
 7. 先尝试spring bean模式启动job，即优先使用SpringJobScheduler
 8. spring bean失败再尝试普通类模式期待job，即用JobScheduler启动

## 数据库同步逻辑
当autodeploy.elasticjob.syncFromDB=true会自动启动一个elasticjob用于同步数据，相关配置如下：

```bash
autodeploy.elasticjob.syncFromDB=true
autodeploy.elasticjob.syncFromDB.jobName=syncElasticJobFromDB
autodeploy.elasticjob.syncFromDB.jobGroup=syncElasticJobGroup
autodeploy.elasticjob.syncFromDB.cron=0 0/5 * * * ?
```
同步只负责在zk生成job，并不会实际启动scheduler，scheduler由worker进程通过zk监听启动。

## 杂项命令

 - RemoveServer ，移除某个ip上所有job instance，可以用于优雅关闭某个ip的进程，观察进程上所有job instance都退出，可以通过查看log中定时输出的如下日志：
update zookeeper heartbeat at 01:40:48,2 scheduler is running
如果进程的autodeploy.elasticjob.syncFromDB=true，剩余scheduler 数量为1，如果=false，scheduler 数量应该为0，同时可以在zk的listenerNamespace node下（缺省为elasticJobListener）查看到类似信息

```shell
get /elasticJobListener/workers/192.168.157.1@-@17512
00:11:42-testgroup,default,-4
```
上述示意结果中的4就是运行中scheduler数量


 - SyncFromZk，从zookeeper总同步job信息到数据库，主要用于从原生elasticjob环境迁移数据，注意如果job name在数据库已经存在的数据不会更新
 - CleanRemovedJob ，默认remove job只是在zookeeper删除server、leader、instance等数据，config信息会保留及status设置为removed，使用CleanRemovedJob 会实际删除zk上status=removed或者config 信息丢失的job
 
 命令使用示意
 

```bash
java -jar  --RemoveServer=192.168.157.3 --RemoveServer=192.168.157.2 --RemoveServer=192.168.157.3 --CleanRemovedJob --SyncFromZk
```
即杂项命令可以一次执行多个。
 
 但杂项命令不可以和主命令，及数据库同步及自动部署（worker）同时执行。
 
## Spring boot/Spring bean 特别注意
所有bean模式的job的package需要配置到ComponentScan.basePackages 属性中，如: 

```bash
ComponentScan.basePackages=com.radishgz.elasticjobautodeploy.example,elasticjob.operation.simplejob
```
elasticjob.operation.simplejob 为默认的package，必须保留。

##License
部分代码基于elasticjob 2.1.5修改。
本项目使用APACHE LICENSE, VERSION 2.0。


 