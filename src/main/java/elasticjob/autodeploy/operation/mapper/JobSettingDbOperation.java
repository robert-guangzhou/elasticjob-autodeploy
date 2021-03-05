package elasticjob.autodeploy.operation.mapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobSettings;

@Component
public class JobSettingDbOperation {

	private BasicDataSource dataSource;
	private final static Logger logger = LoggerFactory.getLogger(JobSettingDbOperation.class);

	@Value("${elasticjob.datasource.driver-class-name}")
	private String rdbDriver;

	@Value("${elasticjob.datasource.url}")
	private String rdbUrl;

	@Value("${elasticjob.datasource.username}")
	private String rdbUserName;

	@Value("${elasticjob.datasource.password}")
	private String rdbPassword;

	@Value("${autodeploy.elasticjob.event.rdb:false}")
	private boolean isRdbEvent = false;

	@PostConstruct
	public void setUpEventTraceDataSource() {

		dataSource = new BasicDataSource();
		dataSource.setDriverClassName(rdbDriver);
		dataSource.setUrl(rdbUrl);
		dataSource.setUsername(rdbUserName);
		dataSource.setPassword(rdbPassword);
		dataSource.setDefaultAutoCommit(true);

	}

	public JobSettingDbOperation() {
		super();

	}

	private String SettingTable = "job_settings";

	public List<JobSettings> getAllJobSetting() {
		String sql = String.format("SELECT job_name, " + "job_type , " + "job_class , " + "cron , "
				+ "sharding_total_count , " + "sharding_item_parameters , " + "job_parameter , "
				+ "monitor_execution , " + "streaming_process , " + "max_time_diff_seconds , " + "monitor_port , "
				+ "failover , " + "misfire , " + "job_sharding_strategy_class , " + "description , "
				+ "script_command_line , " + "reconcile_interval_minutes , " + "job_group , " + "status  FROM %s ",
				SettingTable);
		List<JobSettings> result = new ArrayList<>();

		try (Connection conn = dataSource.getConnection();

				PreparedStatement preparedStatement = conn.prepareStatement(sql);
				ResultSet resultSet = preparedStatement.executeQuery();) {

			while (resultSet.next()) {
				JobSettings jobStatusTraceEvent = new JobSettings(resultSet.getString(1), resultSet.getString(2),
						resultSet.getString(3), resultSet.getString(4), resultSet.getInt(5), resultSet.getString(6),
						resultSet.getString(7), resultSet.getBoolean(8), resultSet.getBoolean(9), resultSet.getInt(10),
						resultSet.getInt(11), resultSet.getBoolean(12), resultSet.getBoolean(13),
						resultSet.getString(14), resultSet.getString(15), resultSet.getString(16), resultSet.getInt(17),
						resultSet.getString(18), resultSet.getString(19));
				result.add(jobStatusTraceEvent);
			}
		} catch (final SQLException ex) {
			// TODO 记录失败直接输出日志,未来可考虑配置化
			logger.error(ex.getMessage());
		}
		return result;
	}

	public boolean insert(final JobSettings jobSettings) {

		String sql = "INSERT INTO  " + SettingTable + " (job_name,  " + "job_type ,  " + "job_class ,  " + "cron ,  "
				+ "sharding_total_count ,  " + "sharding_item_parameters ,  " + "job_parameter ,  "
				+ "monitor_execution ,  " + "streaming_process ,  " + "max_time_diff_seconds ,  " + "monitor_port ,  "
				+ "failover ,  " + "misfire ,  " + "job_sharding_strategy_class ,  " + "description ,  "
				+ "script_command_line ,  " + "reconcile_interval_minutes ,  " + "job_group ,  "
				+ "status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?)";
		boolean result = false;
		try (Connection conn = dataSource.getConnection();
				PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
			preparedStatement.setString(1, jobSettings.getJobName());
			preparedStatement.setString(2, jobSettings.getJobType());
			preparedStatement.setString(3, jobSettings.getJobClass());
			preparedStatement.setString(4, jobSettings.getCron());
			preparedStatement.setInt(5, jobSettings.getShardingTotalCount());
			preparedStatement.setString(6, jobSettings.getShardingItemParameters());
			preparedStatement.setString(7, jobSettings.getJobParameter());
			preparedStatement.setBoolean(8, jobSettings.isMonitorExecution());
			preparedStatement.setBoolean(9, jobSettings.isStreamingProcess());
			preparedStatement.setInt(10, jobSettings.getMaxTimeDiffSeconds());
			preparedStatement.setInt(11, jobSettings.getMonitorPort());
			preparedStatement.setBoolean(12, jobSettings.isFailover());
			preparedStatement.setBoolean(13, jobSettings.isMisfire());
			preparedStatement.setString(14, jobSettings.getJobShardingStrategyClass());
			preparedStatement.setString(15, jobSettings.getDescription());
			preparedStatement.setString(16, jobSettings.getScriptCommandLine());
			preparedStatement.setInt(17, jobSettings.getReconcileIntervalMinutes());
			preparedStatement.setString(18, jobSettings.getJobGroup());
			preparedStatement.setString(19, jobSettings.getStatus());
			preparedStatement.execute();
			result = true;
		} catch (final SQLException ex) {
			// TODO 记录失败直接输出日志,未来可考虑配置化
			logger.error(ex.getMessage());
		}

		return result;
	}

	public boolean insertBatch(List<JobSettings> lists) {

		String sql = "INSERT INTO  " + SettingTable + " (job_name,  " + "job_type ,  " + "job_class ,  " + "cron ,  "
				+ "sharding_total_count ,  " + "sharding_item_parameters ,  " + "job_parameter ,  "
				+ "monitor_execution ,  " + "streaming_process ,  " + "max_time_diff_seconds ,  " + "monitor_port ,  "
				+ "failover ,  " + "misfire ,  " + "job_sharding_strategy_class ,  " + "description ,  "
				+ "script_command_line ,  " + "reconcile_interval_minutes ,  " + "job_group ,  "
				+ "status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?)";
		boolean result = false;
		try (Connection conn = dataSource.getConnection();) {
			for (int i = 0; i < lists.size(); i++) {
				JobSettings jobSettings = lists.get(i);
				try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

					preparedStatement.setString(1, jobSettings.getJobName());
					preparedStatement.setString(2, jobSettings.getJobType());
					preparedStatement.setString(3, jobSettings.getJobClass());
					preparedStatement.setString(4, jobSettings.getCron());
					preparedStatement.setInt(5, jobSettings.getShardingTotalCount());
					preparedStatement.setString(6, jobSettings.getShardingItemParameters());
					preparedStatement.setString(7, jobSettings.getJobParameter());
					preparedStatement.setBoolean(8, jobSettings.isMonitorExecution());
					preparedStatement.setBoolean(9, jobSettings.isStreamingProcess());
					preparedStatement.setInt(10, jobSettings.getMaxTimeDiffSeconds());
					preparedStatement.setInt(11, jobSettings.getMonitorPort());
					preparedStatement.setBoolean(12, jobSettings.isFailover());
					preparedStatement.setBoolean(13, jobSettings.isMisfire());
					preparedStatement.setString(14, jobSettings.getJobShardingStrategyClass());
					preparedStatement.setString(15, jobSettings.getDescription());
					preparedStatement.setString(16, jobSettings.getScriptCommandLine());
					preparedStatement.setInt(17, jobSettings.getReconcileIntervalMinutes());
					preparedStatement.setString(18, jobSettings.getJobGroup());
					preparedStatement.setString(19, jobSettings.getStatus());
					preparedStatement.execute();
				}
			}
			result = true;
		} catch (final SQLException ex) {
			// TODO 记录失败直接输出日志,未来可考虑配置化
			logger.error(ex.getMessage());
		}

		return result;
	}

	public boolean deleteById(String jobName) {
		String sql = "delete from   " + SettingTable + " where job_name=?";
		boolean result = false;
		try (Connection conn = dataSource.getConnection();
				PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
			preparedStatement.setString(1, jobName);
			preparedStatement.execute();
			result = true;
		} catch (final SQLException ex) {
			// TODO 记录失败直接输出日志,未来可考虑配置化
			logger.error(ex.getMessage());
		}

		return result;
	}
}
