# elasticjob-autodeploy��������
automatic  deploy elasticjob's job from definition in database , include  deploy job implemented by spring bean, current only support simpleJob


�������ݿ�Ķ��壬�Զ�����elasticjob����ҵ��֧���Զ�����spring beanģʽ����ҵ����ǰֻ֧��simple job����֧��dataflow ��shell job��

��Ҫ�������³���

 - ����jobParameter ʵ�ֲ�ͬ�����ִ�У�������Ҫ�������б�����籨����job
 - ��Ҫ����ܿ�job���л������簴Ӧ�����Ͳ�ͬ��job���𵽲�ͬ���������Ӳ�ͬ����Դ��job�ֿ������
 - ��������job����
 

Ŀǰ�汾����elasticjob-lite 2.1.4 �����޸ĺ���չ��

Ϊ֧���Զ�����Ϊsimple job��չ��һ�����ԣ�job group��job groupֵĬ��Ϊdefault�����ڲ�ʹ�ñ���������ԭʼ��elastic job���Զ�������default�顣

ͬʱΪ���ø���job״̬����job root path��������һ��status node��¼״̬��״̬�����elasticjobԭ����һ���ģ��������£�

```java
public final static String Shutdown="SHUTDOWN";
public final static String Disabled="DISABLED";
public final static String Removed="REMOVED";
public final static String Enabled="ENABLED";
```

 
 ��ͬ����ԭ��elasticjob״̬������server/worker����instances�ϡ�

ÿһ������job��ʵ�����̳�Ϊworker��worker��ֻ����

 1. job����worker֧��group��һ��worker����ͬʱ֧�ֶ��group����������Ϊautodeploy.elasticjob.supportGroups��Ĭ��ֻΪdefault   
2��autodeploy.elasticjob.syncFromDB=trueʱ�����д����ݿⶨʱͬ��job��zookeeper����ͬһ��Ӧ��/namespace�½���autodeploy.elasticjob.syncFromDB=true�Ľ��̲�����3����


## �Զ������߼�

 1. ����elastic job ��zk�ϵ�root path����elasticjob��namespace �ڵ�
 2. �жϼ����¼��Ƿ�Ϊ����job��job״̬�仯��job instance ��ʧ
 3.  �����������������жϹ������Ƿ����ڱ�worker֧�ֵ���
 
 ����������������ͨ�������߼������Ƿ���в���
 
 4. �Ȳ���jobִ���࣬�����ڲ�����
 5.  job instance �����Ƿ񲻶����Ƭ������1
 
 ��ʵ�ʲ���ʱ��
 
 6. ͨ����ȡzk latch �����worker�ظ�ִ�е���instance ��������
 7. �ȳ���spring beanģʽ����job��������ʹ��SpringJobScheduler
 8. spring beanʧ���ٳ�����ͨ��ģʽ�ڴ�job������JobScheduler����

## ���ݿ�ͬ���߼�
��autodeploy.elasticjob.syncFromDB=true���Զ�����һ��elasticjob����ͬ�����ݣ�����������£�

```bash
autodeploy.elasticjob.syncFromDB=true
autodeploy.elasticjob.syncFromDB.jobName=syncElasticJobFromDB
autodeploy.elasticjob.syncFromDB.jobGroup=syncElasticJobGroup
autodeploy.elasticjob.syncFromDB.cron=0 0/5 * * * ?
```
ͬ��ֻ������zk����job��������ʵ������scheduler��scheduler��worker����ͨ��zk����������

## ��������

 - RemoveServer ���Ƴ�ĳ��ip������job instance�������������Źر�ĳ��ip�Ľ��̣��۲����������job instance���˳�������ͨ���鿴log�ж�ʱ�����������־��
update zookeeper heartbeat at 01:40:48,2 scheduler is running
������̵�autodeploy.elasticjob.syncFromDB=true��ʣ��scheduler ����Ϊ1�����=false��scheduler ����Ӧ��Ϊ0��ͬʱ������zk��listenerNamespace node�£�ȱʡΪelasticJobListener���鿴��������Ϣ

```shell
get /elasticJobListener/workers/192.168.157.1@-@17512
00:11:42-testgroup,default,-4
```
����ʾ�����е�4����������scheduler����


 - SyncFromZk����zookeeper��ͬ��job��Ϣ�����ݿ⣬��Ҫ���ڴ�ԭ��elasticjob����Ǩ�����ݣ�ע�����job name�����ݿ��Ѿ����ڵ����ݲ������
 - CleanRemovedJob ��Ĭ��remove jobֻ����zookeeperɾ��server��leader��instance�����ݣ�config��Ϣ�ᱣ����status����Ϊremoved��ʹ��CleanRemovedJob ��ʵ��ɾ��zk��status=removed����config ��Ϣ��ʧ��job
 
 ����ʹ��ʾ��
 

```bash
java -jar  --RemoveServer=192.168.157.3 --RemoveServer=192.168.157.2 --RemoveServer=192.168.157.3 --CleanRemovedJob --SyncFromZk
```
�������������һ��ִ�ж����
 
 ������������Ժ�����������ݿ�ͬ�����Զ�����worker��ͬʱִ�С�
 
## Spring boot/Spring bean �ر�ע��
����beanģʽ��job��package��Ҫ���õ�ComponentScan.basePackages �����У���: 

```bash
ComponentScan.basePackages=com.radishgz.elasticjobautodeploy.example,elasticjob.operation.simplejob
```
elasticjob.operation.simplejob ΪĬ�ϵ�package�����뱣����

##License
���ִ������elasticjob 2.1.5�޸ġ�
����Ŀʹ��APACHE LICENSE, VERSION 2.0��


 