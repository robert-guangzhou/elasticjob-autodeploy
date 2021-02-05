package elasticjob.operation;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobSettings;
import com.dangdang.ddframe.job.lite.lifecycle.domain.ServerBriefInfo;

import elasticjob.operation.simplejob.JobChangeListenerMain;
import elasticjob.operation.simplejob.SimpleCronJob;
 import elasticjob.operation.testjob.JavaSimpleJob;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = JobChangeListenerMain.class,properties = { 
		  "command.line.runner.enabled=false", 
		  "application.runner.enabled=false" })
@ActiveProfiles("dev")  
public class SimpleCronJobTest {
	@Autowired
	SimpleCronJob simpleCronJob;
	@Test
	public void testCreateJob() {
		
		try {
			simpleCronJob.createJob( "testjobName",
					"0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName() , 1, "","default");
			
			simpleCronJob.createJob( "noParameterTestjobName",
					"0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName() , 1, "","description",null,10,true,"noParameter");
			
		 
			
			Thread.sleep(1000*1);
		} catch (IOException e) {
			fail("ioexcption");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//throws IOException {

			
		
	}
	@Test
	public void testGetServer() {
		try {
			String jobName="test-jobName";
			simpleCronJob.createJob( jobName,
					"0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName() , 1, "","testjobParameter");
			
		 
			Thread.sleep(1000*1);
			simpleCronJob.startSimpleJob(jobName);
			Thread.sleep(1000*60);
			System.out.println(simpleCronJob.getServersTotalCount());
			Collection<ServerBriefInfo> server = simpleCronJob.getAllServersBriefInfo();
			server.forEach( s->{
				s.getInstances().forEach(instance->{
					System.out.println("instance:"+instance);
				});
				
				s.getJobNames().forEach(instance->{
					System.out.println("job:"+instance);
				});
			});
			Thread.sleep(1000*60);
		} catch (IOException e) {
			fail("ioexcption");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testStartJob() {
		
		try {
			String jobName="test-jobName2";
			simpleCronJob.createJob( jobName,
					"0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName() , 2, "","testjobParameter");
			
		 
			Thread.sleep(1000*1);
			simpleCronJob.startSimpleJob(jobName);
			Thread.sleep(1000*60);
			
			simpleCronJob.removeJob(jobName);
			
			Thread.sleep(1000*60);
		} catch (IOException e) {
			fail("ioexcption");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//throws IOException {

			
		
	}
	
	@Test
	public void testRemoveServer() {
		
		try {
			String jobName="test-jobName";
			simpleCronJob.createJob( jobName,
					"0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName() , 1, "","testjobParameter");
			
		 
			Thread.sleep(1000*1);
			simpleCronJob.startSimpleJob(jobName);
			Thread.sleep(1000*60);
			
			simpleCronJob.removeServer("192.168.157.1");
			
			Thread.sleep(1000*60);
		} catch (IOException e) {
			fail("ioexcption");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//throws IOException {

			
		
	}
	
	
	@Test
	public void testDisbaleJob() {
		
		try {
			String jobName="test-disablejobName";
			simpleCronJob.createJob( jobName,
					"0/20 * * * * ?", JavaSimpleJob.class.getCanonicalName() , 1, "","testjobParameter");
			
		 
			Thread.sleep(1000*1);
			simpleCronJob.startSimpleJob(jobName);
			Thread.sleep(1000*30);
			
			simpleCronJob.disableJob(jobName);
			System.out.println("disabled");
			Thread.sleep(1000*60);

			System.out.println("startd to enable");
			simpleCronJob.enableJob(jobName);

			System.out.println("enabled");
			Thread.sleep(1000*60);
			simpleCronJob.shutdownJob(jobName);
			Thread.sleep(1000*60);
			simpleCronJob.removeJob(jobName);

		} catch (IOException e) {
			fail("ioexcption");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	 

			
		
	}
	
	@Test
	public void testRemoveJob() {
		try {
			JobScheduler sc = simpleCronJob.createJob( "RemovedName",
					"0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName() , 1,null, "");
			Thread.sleep(1000*1);
			System.out.println("begin to remove job");
			simpleCronJob.removeJob("RemovedName");
			//sc.getSchedulerFacade().shutdownInstance();
			System.out.println("removed job");
			Thread.sleep(1000*1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUpdateCronString() {
		try {
			JobScheduler sc = simpleCronJob.createJob( "UpdateName",
					"0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName() , 1,"", "");
			Thread.sleep(1000*1);
			System.out.println("begin to update job");
			simpleCronJob.updateJobCron("UpdateName","0/10 * * * * ?");
			//sc.getSchedulerFacade().shutdownInstance();
			 
			Thread.sleep(1000*1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUpdateNotExistJob() {
		try {

			simpleCronJob.updateJobCron("UpdateName2","0/10 * * * * ?");
			 
		} catch (IOException e) {
			return;
		}
		fail("not catached");
	}

	@Test
	public void testUpdateJob() {
		try {
			String jobName="UpdateName";
			JobScheduler sc = simpleCronJob.createJob( jobName,
					"0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName() , 1,"UpdateName", "");
			Thread.sleep(1000*1);
			System.out.println("begin to update job");
			simpleCronJob.updateJob("UpdateName","0/10 * * * * ?",2,"0=Beijing,1=Shanghai,2=Guangzhou");
			//sc.getSchedulerFacade().shutdownInstance();
			 
			Thread.sleep(1000*1);
			
			simpleCronJob.updateJobCron("UpdateName","0/20 * * * * ?");
			Thread.sleep(1000*1);
			simpleCronJob.updateJob(  jobName,   "0/10 * * * * ?", 1,
					"0=Beijing,1=Shanghai,2=Guangzhou",    "jobParameter");
			Thread.sleep(1000*1);
			simpleCronJob.updateJob(  jobName,   "0/10 * * * * ?", 1,
					"0=Beijing,1=Shanghai,2=Guangzhou",  " description", "jobParameter");
			
			//simpleCronJob.updateJob(jobName, CanonicalName, cronString, shardingNum, shardingItemParameters, jobParameter);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testChangeGroup() {
		try {
			String jobName="changeGroupJob";
			JobScheduler sc = simpleCronJob.createJob( jobName,
					"0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName() , 1,"changeGroupJob", "");
			Thread.sleep(1000*1);
			System.out.println("begin to change group job");
			String newGroup="changedgroup";
			
			simpleCronJob.changeGroup(jobName, newGroup);
			//sc.getSchedulerFacade().shutdownInstance();
			 
			Thread.sleep(1000*1);
			JobSettings setting = simpleCronJob.getJobSetting(jobName);
			
			System.out.println(setting.getJobGroup());
 			assertEquals(setting.getJobGroup(),newGroup);
 			Thread.sleep(1000*120);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetPropoerties() {
		try {
			String jobName="test-UpdateName";
			 
			simpleCronJob.createJob( jobName,
					"0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName() , 1, "","description",null,10,true,"test-group");
			
			//simpleCronJob.startSimpleJob(jobName);
			JobSettings setting = simpleCronJob.getJobSetting(jobName);
			
			setting.getJobProperties().forEach((key,values)->{
				System.out.println(key+"->"+values);
				
			});
			System.out.println(setting.getJobGroup());
			Thread.sleep(1000*1);
			 
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
