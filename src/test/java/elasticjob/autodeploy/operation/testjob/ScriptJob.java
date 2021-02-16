package elasticjob.autodeploy.operation.testjob;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
 import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;

public class ScriptJob {
	private static void setUpScriptJob(final CoordinatorRegistryCenter regCenter,
			final JobEventConfiguration jobEventConfig) throws IOException {
		JobCoreConfiguration coreConfig = JobCoreConfiguration.newBuilder("scriptElasticJob", "0/5 * * * * ?", 3)
				.build();
		ScriptJobConfiguration scriptJobConfig = new ScriptJobConfiguration(coreConfig, buildScriptCommandLine());
		new JobScheduler(regCenter, LiteJobConfiguration.newBuilder(scriptJobConfig).build(), jobEventConfig).init();
	}

	private static String buildScriptCommandLine() throws IOException {
		if (System.getProperties().getProperty("os.name").contains("Windows")) {
			return Paths.get(ScriptJob.class.getResource("/script/demo.bat").getPath().substring(1)).toString();
		}
		Path result = Paths.get(ScriptJob.class.getResource("/script/demo.sh").getPath());
		Files.setPosixFilePermissions(result, PosixFilePermissions.fromString("rwxr-xr-x"));
		return result.toString();
	}
}
