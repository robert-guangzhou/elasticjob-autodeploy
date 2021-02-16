package elasticjob.autodeploy.operation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkImpl;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.script.ScriptJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobCoreConfiguration.Builder;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventBus;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.exception.JobConfigurationException;
import com.dangdang.ddframe.job.exception.JobSystemException;
import com.dangdang.ddframe.job.executor.AbstractElasticJobExecutor;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.schedule.JobScheduleController;
import com.dangdang.ddframe.job.lite.internal.schedule.JobShutdownHookPlugin;
import com.dangdang.ddframe.job.lite.internal.schedule.LiteJob;
import com.dangdang.ddframe.job.lite.internal.schedule.LiteJobFacade;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodePath;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.exception.RegExceptionHandler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.google.common.base.Optional;

import elasticjob.autodeploy.operation.mapper.JobSettingsMapper;

import com.dangdang.ddframe.job.lite.lifecycle.api.JobSettingsAPI;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobBriefInfo;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobSettings;
import com.dangdang.ddframe.job.lite.lifecycle.domain.ServerBriefInfo;
import com.dangdang.ddframe.job.lite.lifecycle.internal.operate.JobOperateAPIImpl;
import com.dangdang.ddframe.job.lite.lifecycle.internal.settings.*;
import com.dangdang.ddframe.job.lite.lifecycle.internal.statistics.JobStatisticsAPIImpl;
import com.dangdang.ddframe.job.lite.lifecycle.internal.statistics.ServerStatisticsAPIImpl;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;

@Component
public class LiteJobOperation {
	private final static Logger logger = LoggerFactory.getLogger(LiteJobOperation.class);

	

	
	public LiteJobOperation() {
		super();
		// initOnce();

	}


	
	@Autowired
	private  CoordinatorRegistryCenter regCenter;

	 
	
	@Autowired
	private   JobSettingsAPI jobSettingsAPI;

	@Autowired
	private   JobOperateAPIImpl jobOperationAPI;
	
	@Autowired
	private   JobStatisticsAPIImpl jobStatisticsAPI;
	
	@Autowired
	private   ServerStatisticsAPIImpl serverStatisticsAPI;
 

	public int getServersTotalCount() {

		return serverStatisticsAPI.getServersTotalCount();
	}

	public Collection<ServerBriefInfo> getAllServersBriefInfo() {

		return serverStatisticsAPI.getAllServersBriefInfo();
	}

	public Collection<JobBriefInfo> getAllJobsBriefInfo() {

		return jobStatisticsAPI.getAllJobsBriefInfo();
	}

	


	

	public void removeJob(String jobName) {

		Optional<String> job = Optional.of(jobName);

		jobOperationAPI.remove(job, Optional.<String>absent());
		// JobNodePath jobNodePath = new JobNodePath(jobName);
		// String jobPath = jobNodePath.toString();
		// remove all node of job
//		CuratorFrameworkImpl frame  = (CuratorFrameworkImpl) getRegCenter().getRawClient();
//
//		String path="/"+jobName;
//		try {
//			frame.delete().deletingChildrenIfNeeded().forPath(path);
//		} catch (Exception e) {
//			logger.error(e.getLocalizedMessage(),e);
//			//e.printStackTrace();
//		}
	}

	public void removeServer(String ip) {
		Optional<String> serverIp = Optional.of(ip);

		jobOperationAPI.remove(Optional.<String>absent(), serverIp);
	}

	public void disableJob(String jobName) {

		// JobSettings jobSettings = jobSettingsAPI.getJobSettings(jobName);
		// JobBriefInfo brief = jobStatisticsAPI.getJobBriefInfo(jobName);
		Optional<String> job = Optional.of(jobName);
		jobOperationAPI.disable(job, Optional.<String>absent());
//		JobNodePath jobNodePath = new JobNodePath(jobName);
//		for (String each : regCenter.getChildrenKeys(jobNodePath.getServerNodePath())) {
//
//			Optional<String> ip = Optional.of(each);
//			
//			
//		}

	}

	public String getJobStatus(String jobName) {
		return jobOperationAPI.getJobStatus(jobName);
	}

	public void enableJob(String jobName) {
		Optional<String> job = Optional.of(jobName);
		jobOperationAPI.enable(job, Optional.<String>absent());
	}

	public void shutdownJob(String jobName) {
		Optional<String> job = Optional.of(jobName);
		jobOperationAPI.shutdown(job, Optional.<String>absent());
		// JobSettings jobSettings = jobSettingsAPI.getJobSettings(jobName);
		// JobBriefInfo brief = jobStatisticsAPI.getJobBriefInfo(jobName);

//		JobNodePath jobNodePath = new JobNodePath(jobName);
//		for (String each : regCenter.getChildrenKeys(jobNodePath.getServerNodePath())) {
//
//			Optional<String> ip = Optional.of(each);
//			Optional<String> job = Optional.of(jobName);
//			jobOperationAPI.enable(job, ip);
//		}

	}

	public JobSettings getJobSetting(String jobName) {
		return jobSettingsAPI.getJobSettings(jobName);

	}

	public void setJobSetting(JobSettings jobSettings) {
		jobSettingsAPI.updateJobSettings(jobSettings);

	}

	public void updateJobCron(String jobName, String cronString) throws IOException {

		try {
			JobSettings setting = getJobSetting(jobName);
			setting.setCron(cronString);
			setJobSetting(setting);
		} catch (NullPointerException e) {
			throw new IOException("Job:  " + jobName + " not exist");
		}

	}

	public void updateJob(String jobName, String cronString, int shardingNum, String shardingItemParameters)
			throws IOException {

		try {
			JobSettings setting = getJobSetting(jobName);
			setting.setCron(cronString);
			setting.setShardingItemParameters(shardingItemParameters);
			setting.setShardingTotalCount(shardingNum);
			setJobSetting(setting);
		} catch (NullPointerException e) {
			throw new IOException("Job:  " + jobName + " not exist");
		}

	}

	public void updateJob(String jobName, String cronString, int shardingNum, String shardingItemParameters,
			String jobParameter) throws IOException {

		try {
			JobSettings setting = getJobSetting(jobName);
			setting.setCron(cronString);
			setting.setShardingItemParameters(shardingItemParameters);
			setting.setShardingTotalCount(shardingNum);
			setting.setJobParameter(jobParameter);
			setJobSetting(setting);
		} catch (NullPointerException e) {
			throw new IOException("Job:  " + jobName + " not exist");

		}

	}

	public void updateJob(String jobName, String cronString, int shardingNum, String shardingItemParameters,
			String description, String jobParameter) throws IOException {

		try {
			JobSettings setting = getJobSetting(jobName);
			setting.setCron(cronString);
			setting.setShardingItemParameters(shardingItemParameters);

			setting.setShardingTotalCount(shardingNum);
			setting.setJobParameter(jobParameter);
			setting.setDescription(description);
			setJobSetting(setting);
		} catch (NullPointerException e) {
			throw new IOException("Job:  " + jobName + " not exist");

		}

	}
//	private static JobScheduler setUpSimpleJob(CoordinatorRegistryCenter regCenter,
//			JobEventConfiguration jobEventConfig, String jobName, String CanonicalName, String cronString,
//			int shardingNum, String shardingItemParameters, String jobParameter) {
//		return setUpSimpleJob(  regCenter,
//				  jobEventConfig,   jobName,   CanonicalName,   cronString,
//				  shardingNum,   shardingItemParameters,   jobParameter,maxTimeDiffSeconds,defultOverwrite);
//		
//	}


	

	

	public CoordinatorRegistryCenter getRegCenter() {
		return regCenter;
	}

	

	

	


	@Resource
	JobSettingsMapper jobSettingsMapper;

	public void syncJobsFromZK() {

		Collection<JobBriefInfo> jobs = getAllJobsBriefInfo();
		for (JobBriefInfo job : jobs) {
			String jobName = job.getJobName();
			JobSettings entity = getJobSetting(jobName);
			try {

				String status = getJobStatus(jobName);
				entity.setStatus(status);
				jobSettingsMapper.insert(entity);
			} catch (Exception e) {
				logger.info(entity.getJobName() + " insert failure,it maybe exist in db");
			}
		}
	}

	
	/**
	 * remove those jobs that lose configuration or status is removed
	 */
	public void cleanRemovedJob() {

		List<String> jobNames = regCenter.getChildrenKeys("/");
		CuratorFrameworkImpl frame = (CuratorFrameworkImpl) getRegCenter().getRawClient();

		for (String jobName : jobNames) {

			try {
		        JobNodePath jobNodePath = new JobNodePath(jobName);
		        //Config not exist also be delete
				 String liteJobConfigJson = regCenter.get(jobNodePath.getConfigNodePath()); 
				if (liteJobConfigJson == null || getJobStatus(jobName).equals(JobOperateAPIImpl.Removed)) {

					String path = "/" + jobName;
					try {
						frame.delete().deletingChildrenIfNeeded().forPath(path);
					} catch (Exception e) {
						logger.error(e.getLocalizedMessage(), e);
						// e.printStackTrace();
					}
				}
			} catch (Exception e) {
				logger.error(jobName + " clean fail", e);
			}
		}

	}
}
