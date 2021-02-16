package elasticjob.autodeploy.operation;

import javax.annotation.PostConstruct;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.lite.lifecycle.api.JobSettingsAPI;
import com.dangdang.ddframe.job.lite.lifecycle.internal.operate.JobOperateAPIImpl;
import com.dangdang.ddframe.job.lite.lifecycle.internal.settings.JobSettingsAPIImpl;
import com.dangdang.ddframe.job.lite.lifecycle.internal.statistics.JobStatisticsAPIImpl;
import com.dangdang.ddframe.job.lite.lifecycle.internal.statistics.ServerStatisticsAPIImpl;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;

import lombok.Getter;

@Component
public class LiteJobApiFactory {

	@Value("${autodeploy.elasticjob.serverLists}")
	private String serverLists;

	@Value("${autodeploy.elasticjob.namespace}")
	private String nameSpaces;

	// private static Properties properties;
	
	private static CoordinatorRegistryCenter regCenter;

	private static JobEventRdbConfiguration jobEventConfig;
	private static JobSettingsAPI jobSettingsAPI;

	private static JobOperateAPIImpl jobOperationAPI;
	private static JobStatisticsAPIImpl jobStatisticsAPI;
	private static ServerStatisticsAPIImpl serverStatisticsAPI;

	@PostConstruct
	public void initOnce() {
//		Configuration config = null;
		try {

			if (regCenter == null) {

				regCenter = setUpRegistryCenter();
				//jobEventConfig = setUpEventTraceDataSource();

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

	private CoordinatorRegistryCenter setUpRegistryCenter() {
		ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(serverLists, nameSpaces);
		CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(zkConfig);
		result.init();

		return result;
	}

	
	
	@Bean
	public CoordinatorRegistryCenter getregCenter() {
		return this.regCenter;
	}
	
	
//	@Bean
//	public JobEventRdbConfiguration getJobEventConfig() {
//		return jobEventConfig;
//	}
 	@Bean
	public JobSettingsAPI getJobSettingsAPI() {
		return jobSettingsAPI;
	}

	@Bean
	public JobOperateAPIImpl getJobOperateAPIImpl() {
		return jobOperationAPI;
	}
	
	@Bean
	public JobStatisticsAPIImpl getJobStatisticsAPIImpl() {
		return jobStatisticsAPI;
	}
	@Bean
	public ServerStatisticsAPIImpl getServerStatisticsAPIImpl() {
		return serverStatisticsAPI;
	}

	
}
