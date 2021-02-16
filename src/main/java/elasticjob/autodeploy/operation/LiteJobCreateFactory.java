package elasticjob.autodeploy.operation;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.JobType;
import com.dangdang.ddframe.job.api.script.ScriptJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobCoreConfiguration.Builder;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.lifecycle.api.JobSettingsAPI;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobSettings;
import com.dangdang.ddframe.job.lite.lifecycle.internal.operate.JobOperateAPIImpl;
import com.dangdang.ddframe.job.lite.lifecycle.internal.settings.JobSettingsAPIImpl;
import com.dangdang.ddframe.job.lite.lifecycle.internal.statistics.JobStatisticsAPIImpl;
import com.dangdang.ddframe.job.lite.lifecycle.internal.statistics.ServerStatisticsAPIImpl;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;

import elasticjob.autodeploy.operation.mapper.JobSettingsMapper;


@Component
public class LiteJobCreateFactory {

	
	private final static Logger logger = LoggerFactory.getLogger(LiteJobOperation.class);
 	@Value("${autodeploy.elasticjob.defaultReconcileIntervalMinutes:10}")
	private int defaultReconcileIntervalMinutes = 10;

	@Value("${autodeploy.elasticjob.defaultMisfire:true}")
	private boolean defaultMisfire = true;

	@Value("${autodeploy.elasticjob.defaultFailover:true}")
	private boolean defaultFailover = true;

	@Value("${autodeploy.elasticjob.maxTimeDiffSeconds: 1} ")
	private int defaultMaxTimeDiffSeconds;

	@Value("${autodeploy.elasticjob.defultOverwrite: true} ")
	private boolean defultOverwrite;


	
	@Autowired
	LiteJobApiFactory apiFactory;
	
	@Autowired
	public ApplicationContext applicationContext;

	
	@Autowired
	LiteJobOperation liteJobOperation;
 
	
	@Autowired
	private  CoordinatorRegistryCenter regCenter;

	 
	
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
	
	private JobEventRdbConfiguration jobEventConfig;

	@PostConstruct 
	public void setUpEventTraceDataSource() {
		if (!isRdbEvent) {
			jobEventConfig=null;
			return;
		}
		BasicDataSource result = new BasicDataSource();
		result.setDriverClassName(rdbDriver);
		result.setUrl(rdbUrl);
		result.setUsername(rdbUserName);
		result.setPassword(rdbPassword);
		result.setDefaultAutoCommit(true);
		jobEventConfig=new JobEventRdbConfiguration(result);
	}
	
	JobScheduler createJobScheduler(JobSettings settings, boolean overwrite) {
		LiteJobConfiguration liteJobConfig = createLiteJobConfig(  settings,overwrite);

		JobScheduler scheduler = null;
		Class<?> c = null;
		try {
			c = Class.forName(settings.getJobClass());
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
			logger.info("create SpringJobScheduler for class:" + settings.getJobClass());

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
	
	
	final static String JOB_GROUP_KEY = "job_group";

	
	public static final String SIMPLE="SIMPLE";
	public static final String DATAFLOW="DATAFLOW";
	public static final String SCRIPT="SCRIPT";
	
	private LiteJobConfiguration createLiteJobConfig(JobSettings settings,boolean overwrite) {
		String jobName=settings.getJobName();
		String CanonicalName = settings.getJobClass();
		String cronString = settings.getCron();
		int shardingNum = settings.getShardingTotalCount();
		String  shardingItemParameters= settings.getShardingItemParameters();
		String description = settings.getDescription();
		String jobParameter = settings.getJobParameter();
		
		int timeDiffSeconds = settings.getMaxTimeDiffSeconds();
		String job_group_name = settings.getJobGroup();
		int reconcileIntervalMinutes = settings.getReconcileIntervalMinutes();
		boolean failover = settings.isMisfire();
		boolean misfire = settings.isFailover();
		
		
		JobCoreConfiguration coreConfig = null;
		Builder builder = JobCoreConfiguration.newBuilder(jobName, cronString, shardingNum).misfire(misfire)
				.failover(failover).jobParameter(jobParameter).jobProperties(JOB_GROUP_KEY, job_group_name);
		// builder.jobGroup(job_group_name);
		if (description != null && description.isEmpty() == false)
			builder.description(description);
		if (shardingItemParameters != null && shardingItemParameters.isEmpty() == false)
			builder.shardingItemParameters(shardingItemParameters);

		coreConfig = builder.build();
		LiteJobConfiguration liteJobConfig =null;
		JobTypeConfiguration jobTypeConfiguration=null;
		String jobType = settings.getJobType();
		if (jobType.equals(SIMPLE))
		  jobTypeConfiguration = new SimpleJobConfiguration(coreConfig, CanonicalName);
		if (jobType.equals(SCRIPT))
			jobTypeConfiguration=new ScriptJobConfiguration(coreConfig, settings.getScriptCommandLine());
		
		if (jobType.equals(DATAFLOW))
			jobTypeConfiguration=new DataflowJobConfiguration(coreConfig,
					CanonicalName, settings.isStreamingProcess());
			
		
		// LiteJobConfiguration simpleJobRootConfig =
		// LiteJobConfiguration.newBuilder(simpleJobConfig).build();
		// JobScheduler scheduler;

		liteJobConfig= LiteJobConfiguration.newBuilder(jobTypeConfiguration)
				.maxTimeDiffSeconds(timeDiffSeconds).jobGroup(job_group_name)
				.reconcileIntervalMinutes(reconcileIntervalMinutes).overwrite(overwrite).build();
		return liteJobConfig;
	}
	
	
	private JobSettings createDefaultSettings(String type) {
		JobSettings settings=new JobSettings();
		settings.setFailover(defaultFailover);
		settings.setMaxTimeDiffSeconds(defaultMaxTimeDiffSeconds);
		settings.setMisfire(defaultMisfire);
		settings.setReconcileIntervalMinutes(defaultReconcileIntervalMinutes);
		if (type.equals(DATAFLOW))
				settings.setStreamingProcess(true);
		settings.setJobType(type);
 		return settings;
		
	}
 
	public void startJob(String jobName) throws IOException {

		try {
			JobSettings setting = liteJobOperation.getJobSetting(jobName);
			String jobclass = setting.getJobClass();

			try {
				Class<?> c = Class.forName(jobclass);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				logger.error("job:" + jobName + "'s class��" + jobclass + " not exist");
				throw new IOException("job:" + jobName + "'s class��" + jobclass + " not exist");
			}

//			String group_name = setting.getJobProperties().getOrDefault(JOB_GROUP_KEY, "default");

			JobScheduler scheduler = createJobScheduler(setting,false);
 
			scheduler.start();

		} catch (NullPointerException e) {
			throw new IOException("job:" + jobName + " start failure");

		}

//		LiteJobConfiguration liteJobConfigFromRegCenter = scheduler.getSchedulerFacade()
//				.updateJobConfiguration(liteJobConfig);

	}
	
	public JobScheduler createSimpleJob(String jobName, String cronString, String canonicalName, int shardingNum,
			String shardingItemParameters, String description) throws IOException {
 
		
		return createJob(SIMPLE,"default",jobName, cronString, canonicalName, shardingNum, shardingItemParameters,
				description, "", 
				defaultMisfire, defaultFailover,defaultReconcileIntervalMinutes,defaultMaxTimeDiffSeconds, defultOverwrite,"",false);


	}
	
	public JobScheduler createSimpleJob(String jobGroup,String jobName, String cronString, String canonicalName, int shardingNum,
			String shardingItemParameters, String description,String jobParameter) throws IOException {
 
		
		return createJob(SIMPLE,jobGroup,jobName, cronString, canonicalName, shardingNum, shardingItemParameters,
				description, jobParameter, 
				defaultMisfire, defaultFailover,defaultReconcileIntervalMinutes,defaultMaxTimeDiffSeconds, defultOverwrite,"",false);


	}
	
	public JobScheduler createSimpleJob(String jobGroup,String jobName, String cronString, String canonicalName, int shardingNum,
			String shardingItemParameters, String description,String jobParameter,boolean misfire, boolean failover) throws IOException {
 
		
		return createJob(SIMPLE,jobGroup,jobName, cronString, canonicalName, shardingNum, shardingItemParameters,
				description, jobParameter, 
				misfire, failover,defaultReconcileIntervalMinutes,defaultMaxTimeDiffSeconds, defultOverwrite,"",false);


	}
	
	
	public JobScheduler createScriptJob(String jobName, String cronString,
			String ScriptCommandLine, String description) throws IOException {
 
		
		return createJob(SCRIPT,"default",jobName, cronString, ScriptJob.class.getCanonicalName(), 1, "",
				description, "", 
				defaultMisfire, defaultFailover,defaultReconcileIntervalMinutes,defaultMaxTimeDiffSeconds, defultOverwrite,ScriptCommandLine,false);
		 

	}
	
	public JobScheduler createScriptJob(String jobGroup,String jobName, String cronString,
			String ScriptCommandLine, String description) throws IOException {
 
		
		return createJob(SCRIPT,jobGroup,jobName, cronString, ScriptJob.class.getCanonicalName(), 1, "",
				description, "", 
				defaultMisfire, defaultFailover,defaultReconcileIntervalMinutes,defaultMaxTimeDiffSeconds, defultOverwrite,ScriptCommandLine,false);
		 

	}

	
	public JobScheduler createDataFlowJob(String jobName, String cronString, String canonicalName, int shardingNum,
			String shardingItemParameters, String description) throws IOException {
 
		
		return createJob(DATAFLOW,"default",jobName, cronString, canonicalName, shardingNum, shardingItemParameters,
				description, "", 
				defaultMisfire, defaultFailover,defaultReconcileIntervalMinutes,defaultMaxTimeDiffSeconds, defultOverwrite,"",true);


	}
	
	public JobScheduler createDataFlowJob(String jobGroup,String jobName, String cronString, String canonicalName, int shardingNum,
			String shardingItemParameters, String description,String jobParameter) throws IOException {
 
		
		return createJob(DATAFLOW,jobGroup,jobName, cronString, canonicalName, shardingNum, shardingItemParameters,
				description, jobParameter, 
				defaultMisfire, defaultFailover,defaultReconcileIntervalMinutes,defaultMaxTimeDiffSeconds, defultOverwrite,"",true);


	}
	

	@Resource
	JobSettingsMapper jobSettingsMapper;
	public JobScheduler createJob(String jobType,String jobGroup,String jobName, String cronString, String canonicalName,
			int shardingNum, String shardingItemParameters, String description, String jobParameter, boolean misfire,
			boolean failover, int reconcileIntervalMinutes, int maxTimeDiffSeconds,
			boolean overwrite, String scriptCommandLine, boolean streamingProcess) throws IOException {
		  JobSettings settings = createDefaultSettings(jobType);
			settings.setJobClass(canonicalName);
			settings.setJobName(jobName);
			settings.setShardingTotalCount(shardingNum);
			settings.setShardingItemParameters(shardingItemParameters);
			settings.setDescription(description);
			settings.setJobParameter(jobParameter);
			settings.setMisfire(misfire);
			settings.setFailover(failover);
			settings.setReconcileIntervalMinutes(reconcileIntervalMinutes);
			settings.setMaxTimeDiffSeconds(maxTimeDiffSeconds);
			settings.setScriptCommandLine(scriptCommandLine);
			settings.setStreamingProcess(streamingProcess);
			settings.setJobGroup(jobGroup);
			settings.setCron(cronString);
			return createJob(settings,overwrite);
	}
	
	
	

	public JobScheduler createJob(JobSettings settings ) throws IOException {

		return createJob(settings,true); 
		
		
	}
	/**
	 * @param settings LitJob Setting
	 * @param overwrite if overwrite=false and job is exist ,no setting be changed and will fail if job exist in database
	 * @return
	 * @throws IOException
	 */
	public JobScheduler createJob(JobSettings settings,boolean overwrite) throws IOException {
		
		if (overwrite)
			jobSettingsMapper.deleteById(settings.getJobName());
		jobSettingsMapper.insert(settings);
		
		return createJobScheduler(settings,overwrite); 
		
		
	}
	
	@Value("${autodeploy.elasticjob.changeGroupWaitTime: 10} ")
	private Integer changeGroupWaitTime;

	
	public void changeGroup(String jobName, String newGroup) throws IOException {

		try {
			JobSettings setting = liteJobOperation.getJobSetting(jobName);
			if (setting.getJobGroup().equals(newGroup)) {
				logger.info("change jobgroup: group name not changed,jobname=" + jobName);
				return;
			}

			logger.info("change jobgroup: remove exist" + jobName);
			liteJobOperation.removeJob(jobName);
			Thread.sleep(1000 * changeGroupWaitTime);

			String cronString = setting.getCron();
			String CanonicalName = setting.getJobClass();
			int shardingNum = setting.getShardingTotalCount();
			String shardingItemParameters = setting.getShardingItemParameters();
			String description = setting.getDescription();
			String jobParameter = setting.getJobParameter();
			int timeDiffSeconds = setting.getMaxTimeDiffSeconds();
			setting.setJobGroup(newGroup);
			boolean overwrite = true;
			String job_group_name = newGroup;
			createJob(setting);

		} catch (NullPointerException | InterruptedException e) {
			String msg = "change jobgroup :  job " + jobName + " change fail";
			logger.error(msg, e);
			throw new IOException(msg);
		}

	}

}
