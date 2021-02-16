package elasticjob.autodeploy.operation;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import elasticjob.autodeploy.operation.JobChangeListenerMain;
import elasticjob.autodeploy.operation.LiteJobCreateFactory;
import elasticjob.autodeploy.operation.LiteJobOperation;
import elasticjob.autodeploy.operation.testjob.JavaSimpleJob;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = JobChangeListenerMain.class)
@ActiveProfiles("dev")  
public class JobChangeListenerTest {
	
	@Autowired
	private LiteJobOperation liteJobOperation;

	@Autowired
	private LiteJobCreateFactory liteJobCreateFactory;
	 

//	@Test
//	public void testCreateJob() {
//		
//		try {
//			String jobName="noParameterTestjobName";
//			ZookeeperRegistryCenter nameReg = JobChangeListenerMain.initNameSpaceRegCenter();
//			JobChangeListener listener = JobChangeListener.addJobChangeListener(SimpleCronJob.getRegCenter(),nameReg);
//		 	liteJobCreateFactory.createSimpleJob( jobName,
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
 			liteJobCreateFactory.createSimpleJob(jobName,
					"0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName() , 1, "",
					"description");
 			liteJobCreateFactory.startJob(jobName);
 			liteJobCreateFactory.createSimpleJob(jobName+"testgroup",
					"0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName() , 1, "",
					"description");
		 	
			Thread.sleep(1000*60);
			liteJobOperation.removeJob(jobName);
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
