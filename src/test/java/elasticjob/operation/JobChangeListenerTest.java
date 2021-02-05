package elasticjob.operation;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import elasticjob.operation.simplejob.JobChangeListenerMain;
import elasticjob.operation.simplejob.SimpleCronJob;
import elasticjob.operation.testjob.JavaSimpleJob;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = JobChangeListenerMain.class)
@ActiveProfiles("dev")  
public class JobChangeListenerTest {
	
	@Autowired
	SimpleCronJob simpleCronJob;

//	@Test
//	public void testCreateJob() {
//		
//		try {
//			String jobName="noParameterTestjobName";
//			ZookeeperRegistryCenter nameReg = JobChangeListenerMain.initNameSpaceRegCenter();
//			JobChangeListener listener = JobChangeListener.addJobChangeListener(SimpleCronJob.getRegCenter(),nameReg);
//		 	SimpleCronJob.createJob( jobName,
//					"0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName() , 1, "","description",null,10,true,"default");
//		 	
//		 	Thread.sleep(1000*20);
//		 	 
//		 	 
//		 	JobInstance instance=new JobInstance();
//		 	String id=instance.getJobInstanceId();
//			  boolean i = listener.getJobInstanceExist( jobName,  instance.getJobInstanceId());
//			
//			//assertEquals(i,true);
//			
// 				 Thread.sleep(1000*60);
//			
//		} catch (IOException e) {
//			fail("ioexcption");
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		//throws IOException {
//
//			
//		
//	}

	
 
	
	@Test
	public void testRemoteJob() {
		
		try {
 			String jobName="remoteJob";
 			simpleCronJob.createJob(jobName,
					"0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName() , 1, "",
					"description",null,10,true,"default");
			
 			simpleCronJob.createJob(jobName+"testgroup",
					"0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName() , 1, "",
					"description",null,10,true,"testgroup");
		 	
			Thread.sleep(1000*120);
			simpleCronJob.removeJob(jobName);
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
	public void testGetJobName() {
	String path="/noParameterTestjobName/instances/192.168.157.1@-@17540";
	
	String jobName = path.substring(1, path.indexOf("/instances"));
	System.out.println("instances JOBNAME==" + jobName);
	}
}
