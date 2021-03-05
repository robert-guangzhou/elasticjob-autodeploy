package elasticjob.operation.simplejob;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import elasticjob.autodeploy.operation.JobChangeListenerMain;
import elasticjob.autodeploy.operation.SyncJobFromDatabaseJob;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = JobChangeListenerMain.class,properties = { 
		  "command.line.runner.enabled=false", 
		  "application.runner.enabled=false" })
@ActiveProfiles("dev")  
class SyncJobFromDatabaseTest {

	@Autowired
	SyncJobFromDatabaseJob syncJobFromDatabaseJob;
//	@Test
//	void testGetJobsFromZK() {
//		syncJobFromDatabase.getJobsFromZK();
//	}

	
	@Test
	void testCompareJobsFromZK(){
		
		String jobName="testJobName";
		syncJobFromDatabaseJob.compareJobsFromZK(jobName);
	}
}
