package elasticjob.operation.simplejob;

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

import elasticjob.operation.simplejob.mapper.JobSettingsMapper;

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
public class SimpleCronJob {
	private final static Logger logger = LoggerFactory.getLogger(SimpleCronJob.class);

	// private static Properties properties;
	private static CoordinatorRegistryCenter regCenter;

	private static JobEventRdbConfiguration jobEventConfig;
	private static JobSettingsAPI jobSettingsAPI;

	private static JobOperateAPIImpl jobOperationAPI;
	private static JobStatisticsAPIImpl jobStatisticsAPI;
	private static ServerStatisticsAPIImpl serverStatisticsAPI;

	@Value("${autodeploy.elasticjob.maxTimeDiffSeconds: 1} ")
	private int maxTimeDiffSeconds;

	@Value("${autodeploy.elasticjob.defultOverwrite: true} ")
	private boolean defultOverwrite;

	@Value("${autodeploy.elasticjob.changeGroupWaitTime: 10} ")
	private Integer changeGroupWaitTime;

	public SimpleCronJob() {
		super();
		// initOnce();

	}

	// https://www.jianshu.com/p/7f3c5a06b77f
	@PostConstruct
	public void initOnce() {
//		Configuration config = null;
		try {

			if (regCenter == null) {

				regCenter = setUpRegistryCenter();
				jobEventConfig = setUpEventTraceDataSource();

				jobSettingsAPI = new JobSettingsAPIImpl(regCenter);

				jobOperationAPI = new JobOperateAPIImpl(regCenter);

				jobStatisticsAPI = new JobStatisticsAPIImpl(regCenter);

				serverStatisticsAPI = new ServerStatisticsAPIImpl(regCenter);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

	}

	@Value("${autodeploy.elasticjob.serverLists}")
	private String serverLists;

	@Value("${autodeploy.elasticjob.namespace}")
	private String nameSpaces;

	private CoordinatorRegistryCenter setUpRegistryCenter() {
		ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(serverLists, nameSpaces);
		CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(zkConfig);
		result.init();

		return result;
	}

	public int getServersTotalCount() {

		return serverStatisticsAPI.getServersTotalCount();
	}

	public Collection<ServerBriefInfo> getAllServersBriefInfo() {

		return serverStatisticsAPI.getAllServersBriefInfo();
	}

	public Collection<JobBriefInfo> getAllJobsBriefInfo() {

		return jobStatisticsAPI.getAllJobsBriefInfo();
	}

	@Value("${spring.datasource.driver-class-name}")
	private String rdbDriver;

	@Value("${spring.datasource.url}")
	private String rdbUrl;

	@Value("${spring.datasource.username}")
	private String rdbUserName;

	@Value("${spring.datasource.password}")
	private String rdbPassword;

	@Value("${autodeploy.elasticjob.event.rdb:false}")
	private boolean isRdbEvent = false;

	@Value("${autodeploy.elasticjob.defaultReconcileIntervalMinutes:10}")
	private int defaultReconcileIntervalMinutes = 10;

	@Value("${autodeploy.elasticjob.defaultMisfire:true}")
	private boolean defaultMisfire = true;

	@Value("${autodeploy.elasticjob.defaultFailover:true}")
	private boolean defaultFailover = true;

	private JobEventRdbConfiguration setUpEventTraceDataSource() {
		if (!isRdbEvent)
			return null;
		BasicDataSource result = new BasicDataSource();
		result.setDriverClassName(rdbDriver);
		result.setUrl(rdbUrl);
		result.setUsername(rdbUserName);
		result.setPassword(rdbPassword);
		result.setDefaultAutoCommit(true);
		return new JobEventRdbConfiguration(result);
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

	public JobScheduler createSimpleJobScheduler(String jobName, String jobClass, String cronString,
			int shardingTotalCount, String shardingItemParameters, String description, String jobParameter,
			int maxTimeDiffSeconds, boolean overwrite, String job_group_name) {

		return createSimpleJobScheduler(jobName, jobClass, cronString, shardingTotalCount, shardingItemParameters,
				description, jobParameter, maxTimeDiffSeconds, overwrite, job_group_name,
				defaultReconcileIntervalMinutes, defaultMisfire, defaultFailover);
	}

	final static String JOB_GROUP_KEY = "job_group";

	private LiteJobConfiguration createLiteJobConfig(String jobName, String CanonicalName, String cronString,
			int shardingNum, String shardingItemParameters, String description, String jobParameter,
			int timeDiffSeconds, boolean overwrite, String job_group_name, int reconcileIntervalMinutes,
			boolean misfire, boolean failover) {
		JobCoreConfiguration coreConfig = null;
		Builder builder = JobCoreConfiguration.newBuilder(jobName, cronString, shardingNum).misfire(misfire)
				.failover(failover).jobParameter(jobParameter).jobProperties(JOB_GROUP_KEY, job_group_name);
		// builder.jobGroup(job_group_name);
		if (description != null && description.isEmpty() == false)
			builder.description(description);
		if (shardingItemParameters != null && shardingItemParameters.isEmpty() == false)
			builder.shardingItemParameters(shardingItemParameters);

		coreConfig = builder.build();

		SimpleJobConfiguration simpleJobConfig = new SimpleJobConfiguration(coreConfig, CanonicalName);
		// LiteJobConfiguration simpleJobRootConfig =
		// LiteJobConfiguration.newBuilder(simpleJobConfig).build();
		// JobScheduler scheduler;

		LiteJobConfiguration liteJobConfig = LiteJobConfiguration.newBuilder(simpleJobConfig)
				.maxTimeDiffSeconds(timeDiffSeconds).jobGroup(job_group_name)
				.reconcileIntervalMinutes(reconcileIntervalMinutes).overwrite(overwrite).build();
		return liteJobConfig;
	}

	public void startSimpleJob(String jobName) throws IOException {

		try {
			JobSettings setting = getJobSetting(jobName);
			String jobclass = setting.getJobClass();

			try {
				Class<?> c = Class.forName(jobclass);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				logger.error("job:" + jobName + "'s class��" + jobclass + " not exist");
				throw new IOException("job:" + jobName + "'s class��" + jobclass + " not exist");
			}

			String group_name = setting.getJobProperties().getOrDefault(JOB_GROUP_KEY, "default");

			JobScheduler scheduler = createSimpleJobScheduler(jobName, setting.getJobClass(), setting.getCron(),
					setting.getShardingTotalCount(), setting.getShardingItemParameters(), setting.getDescription(),
					setting.getJobParameter(), setting.getMaxTimeDiffSeconds(), false, group_name,
					setting.getReconcileIntervalMinutes(), setting.isMisfire(), setting.isFailover());
			// (String jobName, String jobClass, String cron, int shardingTotalCount,
			// String shardingItemParameters, String description, String jobParameter, int
			// maxTimeDiffSeconds, boolean overwrite,
			// String jobGroup, int reconcileIntervalMinutes, boolean misfire, boolean
			// failover)

//			LiteJobConfiguration liteJobConfig = createLiteJobConfig(jobName, setting.getJobClass(), setting.getCron(),
//					setting.getShardingTotalCount(), setting.getShardingItemParameters(), setting.getDescription(),
//					setting.getJobParameter(), setting.getMaxTimeDiffSeconds(), false, group_name,
//					setting.getReconcileIntervalMinutes(),setting.isMisfire(),setting.isFailover());
//			JobScheduler scheduler;
//			if (jobEventConfig == null) {
//				scheduler = new JobScheduler(regCenter, liteJobConfig);
//			} else {
//				scheduler = new JobScheduler(regCenter, liteJobConfig, jobEventConfig);
//			}
			// scheduler.getSchedulerFacade()
			// .updateJobConfiguration(liteJobConfig);

			scheduler.start();

		} catch (NullPointerException e) {
			throw new IOException("job:" + jobName + " start failure");

		}

//		LiteJobConfiguration liteJobConfigFromRegCenter = scheduler.getSchedulerFacade()
//				.updateJobConfiguration(liteJobConfig);

	}

	public CoordinatorRegistryCenter getRegCenter() {
		return regCenter;
	}

	public JobScheduler createJob(String jobName, String cronString, String CanonicalName, int shardingNum,
			String shardingItemParameters, String description) throws IOException {

		return createJob(jobName, cronString, CanonicalName, shardingNum, shardingItemParameters, description, "",
				maxTimeDiffSeconds, defultOverwrite);

	}

	public JobScheduler createJob(String jobName, String cronString, String CanonicalName, int shardingNum,
			String shardingItemParameters, String description, String jobParameter, String job_group_name)
			throws IOException {

		return createJob(jobName, cronString, CanonicalName, shardingNum, shardingItemParameters, description,
				jobParameter, maxTimeDiffSeconds, defultOverwrite, "default");

	}

	public JobScheduler createJob(String jobName, String cronString, String CanonicalName, int shardingNum,
			String shardingItemParameters, String description, String jobParameter) throws IOException {

		return createJob(jobName, cronString, CanonicalName, shardingNum, shardingItemParameters, description,
				jobParameter, maxTimeDiffSeconds, defultOverwrite);

	}

	public JobScheduler createJob(String jobName, String cronString, String CanonicalName, int shardingNum,
			String shardingItemParameters, String description, String jobParameter, int timeDiffSeconds,
			boolean overwrite) throws IOException {

		return createJob(jobName, cronString, CanonicalName, shardingNum, shardingItemParameters, description,
				jobParameter, timeDiffSeconds, overwrite, "default");

	}

	public JobScheduler createJob(String jobName, String cronString, String CanonicalName, int shardingNum,
			String shardingItemParameters, String description, String jobParameter, int timeDiffSeconds,
			boolean overwrite, String job_group_name) throws IOException {

		return createSimpleJobScheduler(jobName, CanonicalName, cronString, shardingNum, shardingItemParameters,
				description, jobParameter, timeDiffSeconds, overwrite, job_group_name);
	}

	public JobScheduler createJob(JobSettings settings) throws IOException {

		return createSimpleJobScheduler(settings.getJobName(), settings.getJobClass(), settings.getCron(),
				settings.getShardingTotalCount(), settings.getShardingItemParameters(), settings.getDescription(),
				settings.getJobParameter(), settings.getMaxTimeDiffSeconds(), true, settings.getJobGroup(),
				settings.getReconcileIntervalMinutes(), settings.isMisfire(), settings.isFailover());
	}

	@Autowired
	public ApplicationContext applicationContext;

	private JobScheduler createSimpleJobScheduler(String jobName, String jobClass, String cron, int shardingTotalCount,
			String shardingItemParameters, String description, String jobParameter, int maxTimeDiffSeconds,
			boolean overwrite, String jobGroup, int reconcileIntervalMinutes, boolean misfire, boolean failover) {
		LiteJobConfiguration liteJobConfig = createLiteJobConfig(jobName, jobClass, cron, shardingTotalCount,
				shardingItemParameters, description, jobParameter, maxTimeDiffSeconds, overwrite, jobGroup,
				reconcileIntervalMinutes, misfire, failover);

		JobScheduler scheduler = null;
		Class<?> c = null;
		try {
			c = Class.forName(jobClass);
			Object b = applicationContext.getBean(c);
			// Object b = ContextUtils.getBean(jobClass);
			if (jobEventConfig == null) {

				scheduler = new SpringJobScheduler((ElasticJob) b, regCenter, liteJobConfig,
						new EmptyElasticJobListener());
				// scheduler =new JobScheduler(regCenter, liteJobConfig);
			} else {
				scheduler = new SpringJobScheduler((ElasticJob) b, regCenter, liteJobConfig, jobEventConfig);

				// scheduler = new JobScheduler(regCenter, liteJobConfig, jobEventConfig);
			}
			scheduler.getSchedulerFacade().updateJobConfiguration(liteJobConfig);
			logger.info("create SpringJobScheduler for class:" + jobClass);

		} catch (ClassNotFoundException | BeansException e) {
			logger.info("can't found class or create bean, use JobScheduler to replace SpringJobScheduler");
			if (jobEventConfig == null) {

				scheduler = new JobScheduler(regCenter, liteJobConfig);
			} else {
				scheduler = new JobScheduler(regCenter, liteJobConfig, jobEventConfig);
			}
			scheduler.getSchedulerFacade().updateJobConfiguration(liteJobConfig);

		}

		// scheduler.init();
		return scheduler;
	}

	public void changeGroup(String jobName, String newGroup) throws IOException {

		try {
			JobSettings setting = getJobSetting(jobName);
			if (setting.getJobGroup().equals(newGroup)) {
				logger.info("change jobgroup: group name not changed,jobname=" + jobName);
				return;
			}

			logger.info("change jobgroup: remove exist" + jobName);
			removeJob(jobName);
			Thread.sleep(1000 * changeGroupWaitTime);

			String cronString = setting.getCron();
			String CanonicalName = setting.getJobClass();
			int shardingNum = setting.getShardingTotalCount();
			String shardingItemParameters = setting.getShardingItemParameters();
			String description = setting.getDescription();
			String jobParameter = setting.getJobParameter();
			int timeDiffSeconds = setting.getMaxTimeDiffSeconds();
			boolean overwrite = true;
			String job_group_name = newGroup;
			createJob(jobName, cronString, CanonicalName, shardingNum, shardingItemParameters, description,
					jobParameter, timeDiffSeconds, overwrite, job_group_name);

		} catch (NullPointerException | InterruptedException e) {
			String msg = "change jobgroup :  job " + jobName + " change fail";
			logger.error(msg, e);
			throw new IOException(msg);
		}

	}

	public JobScheduler createJob(String jobName, String cron, String canonicalName, int shardingTotalCount,
			String shardingItemParameters, String description, String jobParameter, String jobGroup, boolean misfire,
			boolean failover) {

		return createSimpleJobScheduler(jobName, canonicalName, cron, shardingTotalCount, shardingItemParameters,
				description, jobParameter, maxTimeDiffSeconds, defultOverwrite, jobGroup,
				defaultReconcileIntervalMinutes, misfire, failover);

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
