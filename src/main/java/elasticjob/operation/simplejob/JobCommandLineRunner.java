package elasticjob.operation.simplejob;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.quartz.impl.SchedulerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.api.strategy.JobInstance;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;

@ConditionalOnProperty(
		  prefix = "application.runner", 
		  value = "enabled", 
		  havingValue = "true", 
		  matchIfMissing = true)
@Component 
public class JobCommandLineRunner implements ApplicationRunner {
     public final static String workerRegisterPath="/workers";

	private static ZookeeperRegistryCenter namespaceRegCenter;
	private static String instanceId;
	
	private final static Logger logger = LoggerFactory.getLogger(JobChangeListenerMain.class);

	public static int getPid() {
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		String name = runtime.getName(); // format: "pid@hostname"
		try {
			return Integer.parseInt(name.substring(0, name.indexOf('@')));
		} catch (Exception e) {
			return -1;
		}
	}
	

//  @Value("${mail.smtp.auth}")

	@Value("${autodeploy.elasticjob.heartbeat}")
	private   int heartbeat; 
	//default value can't have blank and 
	@Value("${autodeploy.elasticjob.supportGroups:default,} ")
	private String supportGroups ;
	
	
	@Value("${autodeploy.elasticjob.serverLists}")
	private String serverLists ;
	
	@Value("${autodeploy.elasticjob.listenerNamespace}")
	private String listenerNamespac ;

	@Autowired
	private SimpleCronJob simpleCronJob;
	 
	
	
	@Value("${autodeploy.elasticjob.syncFromDB:false}")
	private boolean syncFromDB ;
	
	@Value("${autodeploy.elasticjob.syncFromDB.jobName:syncElasticJobFromDB}")
	private String syncJobName;
	 
	@Value("${autodeploy.elasticjob.syncFromDB.jobGroup:syncElasticJobGroup}")
	private String syncJobGroup;

	@Value("${autodeploy.elasticjob.syncFromDB.cron:0 0/5 * * * ?}")
	private String syncCron;
	
	@Value("${autodeploy.elasticjob.isWorker:ture}")
	private boolean isWorker ;
	
//	@Autowired
//	private ApplicationContext applicationContext; 

	
	@Autowired
	private SyncJobFromDatabaseJob syncJobFromDatabaseJob;
	
	public   void mainCmd(ApplicationArguments args) throws Exception {
		List<String> ServerIps = args.getOptionValues("RemoveServer");
		
		if (ServerIps!=null) {
			for (String ip:ServerIps)
				simpleCronJob.removeServer(ip);
			return;
		}
		
		ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
		//simpleCronJob.initOnce(); 

		//String t = PropertiesTool.getConfiguration().getString("autodeploy.elasticjob.heartbeat", "300");
		//int heartbeat = Integer.valueOf(t);
		logger.info("JobCommandLineRunner begin to running");
		initNameSpaceRegCenter();
		JobInstance instance = new JobInstance();
		instanceId = instance.getJobInstanceId();
		
		if (syncFromDB) {
//			simpleCronJob.createJob(syncJobName, "0 0/10 * * * ?", SyncJobFromDatabaseJob.class.getCanonicalName(),
//					1, "", "sync ElasticJob From Database","",syncJobGroup);
			syncJobFromDatabaseJob.compareJobsFromZK(syncJobName);
			//don't need misfail and failover
			JobScheduler schuduler = simpleCronJob.createJob(syncJobName, syncCron, SyncJobFromDatabaseJob.class.getCanonicalName(),
					1, "", "sync ElasticJob From Database","",syncJobGroup,false,false);
			schuduler.start();
			//simpleCronJob.startSimpleJob(syncJobName);
		}
		
		if (!supportGroups.endsWith(","))
			supportGroups=supportGroups.trim()+",";
		
		
		if (isWorker) {
			JobChangeListener jobLister = JobChangeListener.addJobChangeListener
				(simpleCronJob.getRegCenter(),namespaceRegCenter,simpleCronJob,supportGroups);
			jobLister.startAllJob( );
		// System.out.printf("锟斤拷始时锟戒：%s\n\n", new SimpleDateFormat("HH:mm:ss").format(new
		// Date()));
		
		}else {
			logger.info("isWorker=false,JobChangeListener not started. ");

			supportGroups="";
		}
		TimerTask timerTask = new TimerTask(supportGroups.trim());
		
		timer.scheduleAtFixedRate(timerTask, 1, heartbeat, TimeUnit.SECONDS);
		// timer.scheduleWithFixedDelay(timerTask, 1000, 2000, TimeUnit.MILLISECONDS);

		// Thread.sleep(1000*300);
	}

	public   ZookeeperRegistryCenter initNameSpaceRegCenter()  {
		
		ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(serverLists, listenerNamespac);
		namespaceRegCenter = new ZookeeperRegistryCenter(zkConfig);
		namespaceRegCenter.init();
		return namespaceRegCenter;
	}
	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	private static String getNowAsString() {
		return dateFormat.format(new Date());

	}

	private   class TimerTask implements Runnable {

		private String supportGroups;

//      private final int sleepTime;
//      private final SimpleDateFormat dateFormat;
//      private final int pid;
//      
		public TimerTask(String supportGroups) {
			this.supportGroups = supportGroups;

		}

		@Override
		public void run() {
			//namespaceRegCenter.persistEphemeral("/" + instanceId, now + "-" + supportGroups);
			workerPersistent();

		}

	}

	
	private   void workerPersistent() {
		SchedulerRepository rep = SchedulerRepository.getInstance();
 		int size = rep.lookupAll().size();
		
		namespaceRegCenter.persistEphemeral( workerRegisterPath+"/"+ instanceId, getNowAsString() + "-" + supportGroups+"-"+size);
		logger.info("update zookeeper heartbeat at " + getNowAsString()+","+size+" scheduler is running");

	}
	@Override
	public void run(ApplicationArguments args) throws Exception {
		try {
			mainCmd(args);}
		catch(Exception e) {
			e.printStackTrace();
		}		
	}

}
