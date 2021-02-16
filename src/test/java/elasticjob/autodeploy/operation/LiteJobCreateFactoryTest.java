package elasticjob.autodeploy.operation;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobSettings;

import elasticjob.autodeploy.operation.testjob.JavaDataflowJob;
import elasticjob.autodeploy.operation.testjob.JavaSimpleJob;
import elasticjob.autodeploy.operation.testjob.SpringDataflowJob;

@SpringBootTest(classes = JobChangeListenerMain.class, properties = { "command.line.runner.enabled=false",
		"application.runner.enabled=false" })
@ActiveProfiles("dev")
@ExtendWith(SpringExtension.class)
class LiteJobCreateFactoryTest {

	@Autowired
	private LiteJobOperation liteJobOperation;

	@Autowired
	private LiteJobCreateFactory liteJobCreateFactory;

 
//	@Test
//	void testStartJob() {
//		fail("Not yet implemented");
//	}

	@Test
	void testCreateSimpleJobStringStringStringIntStringString() {
		String jobName = "test-job1";
		String cronString = "0/5 * * * * ?";
		String jobGroup = "default";
		int shardingNum = 1;
		String shardingItemParameters = "";
		String description = "test-job";
		try {

			liteJobCreateFactory.createSimpleJob(jobName, cronString, JavaSimpleJob.class.getCanonicalName(),
					shardingNum, shardingItemParameters, description);
			liteJobCreateFactory.startJob(jobName);
			Thread.sleep(1000 * 20);
		} catch (IOException | InterruptedException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	void testCreateSimpleJobStringStringStringStringIntStringStringString() {
		String jobName = "test-job2";
		String cronString = "0/5 * * * * ?";
		String jobGroup = "default";
		int shardingNum = 1;
		String shardingItemParameters = "";
		String description = "test-job";
		try {

			liteJobCreateFactory.createSimpleJob(jobGroup, jobName, cronString, JavaSimpleJob.class.getCanonicalName(),
					shardingNum, shardingItemParameters, description, "jobParameter");
			liteJobCreateFactory.startJob(jobName);
			Thread.sleep(1000 * 20);
		} catch (IOException | InterruptedException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	void testCreateSimpleJobStringStringStringStringIntStringStringStringBooleanBoolean() {
		String jobName = "test-job3";
		String cronString = "0/5 * * * * ?";
		String jobGroup = "default";
		int shardingNum = 1;
		String shardingItemParameters = "";
		String description = "test-job";
		try {

			liteJobCreateFactory.createSimpleJob(jobGroup, jobName, cronString, JavaSimpleJob.class.getCanonicalName(),
					shardingNum, shardingItemParameters, description, "jobParameter", false, false);
			liteJobCreateFactory.startJob(jobName);
			Thread.sleep(1000 * 20);
		} catch (IOException | InterruptedException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	void testCreateScriptJobStringStringStringString() {

		String jobName = "test-ScriptJob";
		String cronString = "0/5 * * * * ?";
		String jobGroup = "default";
		int shardingNum = 1;
		String shardingItemParameters = "";
		String description = "test-ScriptJob";
		try {

			liteJobCreateFactory.createScriptJob(jobName, cronString, buildScriptCommandLine(), description);
			liteJobCreateFactory.startJob(jobName);

			Thread.sleep(1000 * 20);
		} catch (IOException | InterruptedException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}

	}

	@Test
	void testCreateScriptJobStringStringStringStringString() {
		String jobName = "test-ScriptJob2";
		String cronString = "0/5 * * * * ?";
		String jobGroup = "non-default";
		int shardingNum = 1;
		String shardingItemParameters = "";
		String description = "test-ScriptJob";
		try {

			liteJobCreateFactory.createScriptJob(jobGroup, jobName, cronString, buildScriptCommandLine(), description);
			liteJobCreateFactory.startJob(jobName);

			Thread.sleep(1000 * 20);
		} catch (IOException | InterruptedException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	void testCreateDataFlowJobStringStringStringIntStringString() {

		String jobName = "test-DataFlowJob1";
		String cronString = "0/5 * * * * ?";
		String jobGroup = "default";
		int shardingNum = 3;
		String shardingItemParameters = "0=Beijing,1=Shanghai,2=Guangzhou";
		String description = "test-ScriptJob";
		try {

			liteJobCreateFactory.createDataFlowJob(jobName, cronString, JavaDataflowJob.class.getCanonicalName(),
					shardingNum, shardingItemParameters, description);
			liteJobCreateFactory.startJob(jobName);

			Thread.sleep(1000 * 60);
		} catch (IOException | InterruptedException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	void testCreateDataFlowJobStringStringStringStringIntStringStringString() {
		String jobName = "test-Spring-DataFlowJob1";
		String cronString = "0/5 * * * * ?";
		String jobGroup = "default";
		int shardingNum = 3;
		String shardingItemParameters = "0=Beijing,1=Shanghai,2=Guangzhou";
		String description = "test-ScriptJob";
		try {

			liteJobCreateFactory.createDataFlowJob(jobGroup,jobName, cronString, SpringDataflowJob.class.getCanonicalName(),
					shardingNum, shardingItemParameters, description, "jobpara");
			liteJobCreateFactory.startJob(jobName);

			Thread.sleep(1000 * 30);
		} catch (IOException | InterruptedException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	void testCreateJobStringStringStringStringStringIntStringStringStringBooleanBooleanIntIntBooleanStringBoolean() {

		String jobName = "test-Spring-DataFlowJob2";
		String cronString = "0/10 * * * * ?";
		String jobGroup = "default";
		int shardingNum = 1;
		String shardingItemParameters = "";
		String description = "test-ScriptJob";
		try {

			liteJobCreateFactory.createJob("DATAFLOW", jobGroup, jobName, cronString,
					SpringDataflowJob.class.getCanonicalName(), shardingNum, shardingItemParameters, description,
					"jobParameter", false, false, 10, 10, true, "", true);
			liteJobCreateFactory.startJob(jobName);

			Thread.sleep(1000 * 20);
		} catch (IOException | InterruptedException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}

	}

	@Test
	void testCreateJobJobSettings() {

		String jobName = "test-testCreateJobJobSettings";
		String cronString = "0/10 * * * * ?";
		String jobGroup = "default";
		int shardingNum = 1;
		String shardingItemParameters = "";
		String description = "test-ScriptJob";
		try {
			JobSettings settings = new JobSettings();
			settings.setJobType(LiteJobCreateFactory.SIMPLE);
			settings.setJobClass( JavaSimpleJob.class.getCanonicalName());
			settings.setJobName(jobName);
			settings.setShardingTotalCount(shardingNum);
			settings.setShardingItemParameters(shardingItemParameters);
			settings.setDescription(description);
			settings.setJobParameter("jobParameter");
			settings.setMisfire(true);
			settings.setFailover(true);
			settings.setReconcileIntervalMinutes(30);
			settings.setMaxTimeDiffSeconds(10);
			settings.setScriptCommandLine("");
			settings.setStreamingProcess(false);
			settings.setJobGroup(jobGroup);
			settings.setCron(cronString);
			liteJobCreateFactory.createJob(settings);
			Thread.sleep(1000 * 20);
		} catch (IOException | InterruptedException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	void testCreateJobJobSettingsBoolean() {
		String jobName = "test-testCreateJobJobSettings-notoverride";
		String cronString = "0/10 * * * * ?";
		String jobGroup = "default";
		int shardingNum = 1;
		String shardingItemParameters = "";
		String description = "test-ScriptJob";
		try {
			JobSettings settings = new JobSettings();
			settings.setJobType(LiteJobCreateFactory.SIMPLE);
			settings.setJobClass( JavaSimpleJob.class.getCanonicalName());
			settings.setJobName(jobName);
			settings.setShardingTotalCount(shardingNum);
			settings.setShardingItemParameters(shardingItemParameters);
			settings.setDescription(description);
			settings.setJobParameter("jobParameter");
			settings.setMisfire(true);
			settings.setFailover(true);
			settings.setReconcileIntervalMinutes(30);
			settings.setMaxTimeDiffSeconds(10);
			settings.setScriptCommandLine("");
			settings.setStreamingProcess(false);
			settings.setJobGroup(jobGroup);
			settings.setCron(cronString);
			liteJobCreateFactory.createJob(settings,false);
			Thread.sleep(1000 * 20);
		} catch(DuplicateKeyException e2) {
			
		}
		catch (IOException | InterruptedException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	void testChangeGroup() {
		String jobName = "test-testchangedgroup";
		String cronString = "0/10 * * * * ?";
		String jobGroup = "default";
		int shardingNum = 1;
		String shardingItemParameters = "";
		String description = "test-ScriptJob";
		try {
			JobSettings settings = new JobSettings();
			settings.setJobType(LiteJobCreateFactory.SIMPLE);
			settings.setJobClass( JavaSimpleJob.class.getCanonicalName());
			settings.setJobName(jobName);
			settings.setShardingTotalCount(shardingNum);
			settings.setShardingItemParameters(shardingItemParameters);
			settings.setDescription(description);
			settings.setJobParameter("jobParameter");
			settings.setMisfire(true);
			settings.setFailover(true);
			settings.setReconcileIntervalMinutes(30);
			settings.setMaxTimeDiffSeconds(10);
			settings.setScriptCommandLine("");
			settings.setStreamingProcess(false);
			settings.setJobGroup(jobGroup);
			settings.setCron(cronString);
			
			liteJobCreateFactory.createJob(settings,true);
			liteJobCreateFactory.startJob(jobName);
			Thread.sleep(1000 * 60);
			liteJobCreateFactory.changeGroup(jobName, "groug-changed");
		} catch (IOException | InterruptedException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

	private static String buildScriptCommandLine() throws IOException {
		if (System.getProperties().getProperty("os.name").contains("Windows")) {
			return Paths.get(LiteJobCreateFactoryTest.class.getResource("/script/demo.bat").getPath().substring(1))
					.toString();
		}
		Path result = Paths.get(LiteJobCreateFactoryTest.class.getResource("/script/demo.sh").getPath());
		Files.setPosixFilePermissions(result, PosixFilePermissions.fromString("rwxr-xr-x"));
		return result.toString();
	}

}
