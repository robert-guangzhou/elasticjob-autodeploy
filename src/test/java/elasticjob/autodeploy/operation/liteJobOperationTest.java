package elasticjob.autodeploy.operation;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobSettings;
import com.dangdang.ddframe.job.lite.lifecycle.domain.ServerBriefInfo;

import elasticjob.autodeploy.operation.JobChangeListenerMain;
import elasticjob.autodeploy.operation.LiteJobCreateFactory;
import elasticjob.autodeploy.operation.LiteJobOperation;
import elasticjob.autodeploy.operation.testjob.JavaSimpleJob;

@SpringBootTest(classes = JobChangeListenerMain.class, properties = { "command.line.runner.enabled=false",
		"application.runner.enabled=false" })
@ActiveProfiles("dev")
@ExtendWith(SpringExtension.class)
//@SpringBootTest(classes = {LiteJobApiFactory.class,JobChangeListenerMain.class,LiteJobCreateFactory.class,LiteJobOperation.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class liteJobOperationTest {
	 
	@Autowired
	private LiteJobOperation liteJobOperation;

	@Autowired
	private LiteJobCreateFactory liteJobCreateFactory;
	
	@Test
	public void testCreateJob() {

		try {
			liteJobCreateFactory.createSimpleJob("testjobName", "0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName(), 1, "",
					"default");

			liteJobCreateFactory.createSimpleJob("noParameterTestjobName", "0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName(),
					1, "", "description");

			Thread.sleep(1000 * 1);
		} catch (IOException e) {
			fail("ioexcption");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// throws IOException {

	}

	@Test
	public void testGetServer() {
		try {
			String jobName = "test-jobName";
			liteJobCreateFactory.createSimpleJob(jobName, "0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName(), 1, "",
					"testjobParameter");

			Thread.sleep(1000 * 1);
			liteJobCreateFactory.startJob(jobName);
			Thread.sleep(1000 * 60);
			System.out.println(liteJobOperation.getServersTotalCount());
			Collection<ServerBriefInfo> server = liteJobOperation.getAllServersBriefInfo();
			server.forEach(s -> {
				s.getInstances().forEach(instance -> {
					System.out.println("instance:" + instance);
				});

				s.getJobNames().forEach(instance -> {
					System.out.println("job:" + instance);
				});
			});
			Thread.sleep(1000 * 60);
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
			String jobName = "test-jobName2";
			liteJobCreateFactory.createSimpleJob(jobName, "0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName(), 2, "",
					"testjobParameter");

			Thread.sleep(1000 * 1);
			liteJobCreateFactory.startJob(jobName);
			Thread.sleep(1000 * 60);

			liteJobOperation.removeJob(jobName);

			Thread.sleep(1000 * 60);
		} catch (IOException e) {
			fail("ioexcption");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// throws IOException {

	}

	@Test
	public void testRemoveServer() {

		try {
			String jobName = "test-jobName";
			liteJobCreateFactory.createSimpleJob(jobName, "0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName(), 1, "",
					"testjobParameter");

			Thread.sleep(1000 * 1);
			liteJobCreateFactory.startJob(jobName);
			Thread.sleep(1000 * 60);

			liteJobOperation.removeServer("192.168.157.1");

			Thread.sleep(1000 * 60);
		} catch (IOException e) {
			fail("ioexcption");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// throws IOException {

	}

	@Test
	public void testDisbaleJob() {

		try {
			String jobName = "test-disablejobName";
			liteJobCreateFactory.createSimpleJob(jobName, "0/20 * * * * ?", JavaSimpleJob.class.getCanonicalName(), 1, "",
					"testjobParameter");

			Thread.sleep(1000 * 1);
			liteJobCreateFactory.startJob(jobName);
			Thread.sleep(1000 * 30);

			liteJobOperation.disableJob(jobName);
			System.out.println("disabled");
			Thread.sleep(1000 * 60);

			System.out.println("startd to enable");
			liteJobOperation.enableJob(jobName);

			System.out.println("enabled");
			Thread.sleep(1000 * 60);
			liteJobOperation.shutdownJob(jobName);
			Thread.sleep(1000 * 60);
			liteJobOperation.removeJob(jobName);

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
			JobScheduler sc = liteJobCreateFactory.createSimpleJob("RemovedName", "0/5 * * * * ?",
					JavaSimpleJob.class.getCanonicalName(), 1, null, "");
			Thread.sleep(1000 * 1);
			System.out.println("begin to remove job");
			liteJobOperation.removeJob("RemovedName");
			// sc.getSchedulerFacade().shutdownInstance();
			System.out.println("removed job");
			Thread.sleep(1000 * 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testUpdateCronString() {
		try {
			String jobName="UpdateName";
			JobScheduler sc = liteJobCreateFactory.createSimpleJob("UpdateName", "0/5 * * * * ?",
					JavaSimpleJob.class.getCanonicalName(), 1, "", "");
			Thread.sleep(1000 * 1);
			System.out.println("begin to update job");
			liteJobOperation.updateJobCron("UpdateName", "0/10 * * * * ?");
			// sc.getSchedulerFacade().shutdownInstance();

			Thread.sleep(1000 * 1);
			
			liteJobCreateFactory.startJob(jobName);
			Thread.sleep(1000 * 20);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testUpdateNotExistJob() {
		try {

			liteJobOperation.updateJobCron("UpdateName2", "0/10 * * * * ?");

		} catch (IOException e) {
			return;
		}
		fail("not catached");
	}

	@Test
	public void testUpdateJob() {
		try {
			String jobName = "UpdateName";
			JobScheduler sc = liteJobCreateFactory.createSimpleJob(jobName, "0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName(),
					1, "UpdateName", "");
			Thread.sleep(1000 * 1);
			System.out.println("begin to update job");
			liteJobOperation.updateJob("UpdateName", "0/10 * * * * ?", 2, "0=Beijing,1=Shanghai,2=Guangzhou");
			// sc.getSchedulerFacade().shutdownInstance();

			Thread.sleep(1000 * 1);

			liteJobOperation.updateJobCron("UpdateName", "0/20 * * * * ?");
			Thread.sleep(1000 * 1);
			liteJobOperation.updateJob(jobName, "0/10 * * * * ?", 1, "0=Beijing,1=Shanghai,2=Guangzhou", "jobParameter");
			Thread.sleep(1000 * 1);
			liteJobOperation.updateJob(jobName, "0/10 * * * * ?", 1, "0=Beijing,1=Shanghai,2=Guangzhou", " description",
					"jobParameter");

			// liteJobOperation.updateJob(jobName, CanonicalName, cronString, shardingNum,
			// shardingItemParameters, jobParameter);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testChangeGroup() {
		try {
			String jobName = "changeGroupJob";
			JobScheduler sc = liteJobCreateFactory.createSimpleJob(jobName, "0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName(),
					1, "changeGroupJob", "");
			Thread.sleep(1000 * 1);
			System.out.println("begin to change group job");
			String newGroup = "changedgroup";

			liteJobCreateFactory.changeGroup(jobName, newGroup);
			// sc.getSchedulerFacade().shutdownInstance();

			Thread.sleep(1000 * 1);
			JobSettings setting = liteJobOperation.getJobSetting(jobName);

			System.out.println(setting.getJobGroup());
			assertEquals(setting.getJobGroup(), newGroup);
			Thread.sleep(1000 * 120);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testGetPropoerties() {
		try {
			String jobName = "test-UpdateName";

			liteJobCreateFactory.createSimpleJob("test-group",jobName, "0/5 * * * * ?", JavaSimpleJob.class.getCanonicalName(), 1, "",
					"description", null );

			// liteJobOperation.startSimpleJob(jobName);
			JobSettings setting = liteJobOperation.getJobSetting(jobName);

			setting.getJobProperties().forEach((key, values) -> {
				System.out.println(key + "->" + values);

			});
			System.out.println(setting.getJobGroup());
			Thread.sleep(1000 * 1);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testcleanRemovedJob() {
		try {
			liteJobOperation.cleanRemovedJob();
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	
	@Test
	public void testsyncJobsFromZK() {
		try {
			liteJobOperation.syncJobsFromZK();
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	 
}
