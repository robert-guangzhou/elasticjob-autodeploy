package elasticjob.operation.simplejob;

import java.io.IOException;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkImpl;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryNTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dangdang.ddframe.job.lite.api.strategy.JobInstance;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodePath;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobBriefInfo;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobSettings;
import com.dangdang.ddframe.job.lite.lifecycle.internal.operate.JobOperateAPIImpl;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.exception.RegExceptionHandler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;

 
 
public class JobChangeListener implements TreeCacheListener {
	
	 
	
	private final static Logger logger = LoggerFactory.getLogger(JobChangeListener.class);
	private CoordinatorRegistryCenter regCenter;
 	private Random random;
	private ZookeeperRegistryCenter namespaceRegCenter;
	private long maxWaitTime=20;

 	private String supportGroups ;
	private SimpleCronJob simpleCronJob;
	
	public JobChangeListener(CoordinatorRegistryCenter regCenter, 
			ZookeeperRegistryCenter namespaceRegCenter,SimpleCronJob simpleCronJo,String supportGroups) {

		super();
		this.regCenter = regCenter;
		this.namespaceRegCenter=namespaceRegCenter;
		this.simpleCronJob=simpleCronJo;
		this.supportGroups=supportGroups;
		random=new Random();
	}

	public static   JobChangeListener addJobChangeListener(CoordinatorRegistryCenter regCenter,
			ZookeeperRegistryCenter namespaceRegCenter,SimpleCronJob simpleCronJob,String supportGroups) {

		ZookeeperRegistryCenter zkreg = (ZookeeperRegistryCenter) regCenter;

		CuratorFrameworkImpl frame = (CuratorFrameworkImpl) zkreg.getRawClient();
		TreeCache cache = new TreeCache(frame, "/");
		try {
			cache.start();
			// CHECKSTYLE:OFF
		} catch (final Exception ex) {
			// CHECKSTYLE:ON
			RegExceptionHandler.handleException(ex);
		}
		// zkreg.addCacheData(properties.getProperty("namespace"));
		// new TreeCache(frame.ins, );

		JobChangeListener listener = new JobChangeListener(regCenter,namespaceRegCenter,simpleCronJob,supportGroups);
		cache.getListenable().addListener(listener);
		return listener;
	}

	@Override
	public void childEvent(CuratorFramework client, TreeCacheEvent treeCacheEvent) throws Exception {
		try {
		ChildData data = treeCacheEvent.getData();
		if (data == null)
			return;
		String path = data.getPath();
		if (treeCacheEvent.getType() == Type.NODE_ADDED) {
			//System.out.println(path);
			if (path.endsWith("/config")  ) {
				
				//����ӳ٣����Ͷ�ڵ��ͻ
				Thread.sleep(random.nextInt(10000));
				 
				String jobName = path.substring(1, path.indexOf("/config"));
				//logger.debug("job add:  JOBNAME==" + jobName);

				startJob(  jobName);

			}
			return;

		}else if (treeCacheEvent.getType() == Type.NODE_UPDATED) {
			//System.out.println(path);
			if (path.endsWith("/status")  ) {
				
				//����ӳ٣����Ͷ�ڵ��ͻ
				Thread.sleep(random.nextInt(10000));
				 
				String jobName = path.substring(1, path.indexOf("/status"));
				//logger.debug("job add:  JOBNAME==" + jobName);

				startJob(  jobName);

			}
			return;

		}
		 
		else if (treeCacheEvent.getType() == Type.NODE_REMOVED) {
			//System.out.println(path);
			if (path.contains("/instances/")) {
				
				//��10��������ӳ٣����Ͷ�ڵ��ͻ��
				//TODO ʹ��zk transaction ��һ�����ͳ�ͻ��������instance ����
				
				Thread.sleep(random.nextInt(10000));
				logger.debug(" instances path=" + path);

 				String jobName = path.substring(1, path.indexOf("/instances"));

 				startJob(  jobName);

			}

		} 
		}catch(Exception e){
			logger.error("listener failuer", e);
		}

	}

	
	private void startJob(String jobName)   {
		
		
        try {
        	CuratorFramework client=(CuratorFramework) namespaceRegCenter.getRawClient();

            InterProcessMutex lock = new InterProcessMutex(client, "/start-job-lock/"+jobName);
            if (lock.acquire(maxWaitTime, TimeUnit.SECONDS)){
            	if (shouldStartJob(jobName)) {
        			simpleCronJob.startSimpleJob(jobName);
        			logger.info("jobName:" + jobName + " started");

        		}
                lock.release();
            }else
            	logger.error("get lock failure where try to start job:" + jobName);
        } catch (Exception e) {
        	logger.error("jobName:" + jobName + " start faile",e);
        }
	
	}
	private int getJobInstanceCount(final String jobName) {
		return regCenter.getChildrenKeys(new JobNodePath(jobName).getInstancesNodePath()).size();
	}

	public boolean getJobInstanceExist(final String jobName,String instanceId) {
		String path = new JobNodePath(jobName).getInstanceNodePath(instanceId);
		return regCenter.isExisted(path);
	}
	
	private boolean shouldStartJob(String jobName) {
		JobSettings setting = null;
		
		try{
			setting=simpleCronJob.getJobSetting(jobName);
		}catch(NullPointerException e) {
			//job is removed
			return false;
		}
		String status = simpleCronJob.getJobStatus(jobName);
		// if job not enabled ,return false
		if (status.equals(JobOperateAPIImpl.Enabled)==false) {
			return false;
		}
		
		String jobclass = setting.getJobClass();
		try {

			
			Class<?> c = Class.forName(jobclass);
			int runningCount = getJobInstanceCount(jobName);
			logger.debug(jobName+"'s"+
					"runningCount=" + runningCount + ",getShardingTotalCount=" + setting.getShardingTotalCount());
			//to enhance availability , instance number should great than sharding number;
 			if (runningCount > setting.getShardingTotalCount())
				return false;
 			
			
			JobInstance instance=new JobInstance();
			if (getJobInstanceExist( jobName,  instance.getJobInstanceId())) {
				logger.debug(jobName+"'s"+
						"instance=" + instance.getJobInstanceId() + "existed");
				return false;
			}
			
			String jobgroup = setting.getJobGroup()+",";
			logger.debug(jobName+"'s"+
					"groups=" + supportGroups + "-------jobgroup=" +jobgroup );
			return supportGroups.contains(jobgroup);
		} catch (ClassNotFoundException e) {
			logger.debug("class:" + jobclass + " not exist, no job need to start");
			return false;
		} 

	}
	
	 
  public   void startAllJob( ) {
	  Collection<JobBriefInfo> jobs = simpleCronJob.getAllJobsBriefInfo();
	  jobs.forEach( jobBrief->{
		  String jobName = jobBrief.getJobName();
		  
			startJob(  jobName) ;
		 
	  });
  }

}
