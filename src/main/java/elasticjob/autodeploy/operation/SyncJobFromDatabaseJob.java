package elasticjob.autodeploy.operation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.apache.curator.framework.imps.CuratorFrameworkImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobBriefInfo;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobSettings;
import com.dangdang.ddframe.job.lite.lifecycle.internal.operate.JobOperateAPIImpl;

import elasticjob.autodeploy.operation.mapper.JobSettingsMapper;

@Component
public class SyncJobFromDatabaseJob implements SimpleJob {

	public String getName() {
		return this.getClass().getCanonicalName();
	}

	private final static Logger logger = LoggerFactory.getLogger(SyncJobFromDatabaseJob.class);

	@Resource
	JobSettingsMapper jobSettingsMapper;

	@Autowired
	private LiteJobOperation liteJobOperation;

	@Autowired
	private LiteJobCreateFactory liteJobCreateFactory;
	
	@Override
	public void execute(ShardingContext shardingContext) {

		String selfName = shardingContext.getJobName();
		compareJobsFromZK(selfName);
	}

	private boolean shouldRemoveAndRestart(JobSettings dbSetting, JobSettings zkEntity) {
		if (dbSetting.getJobClass().equals(zkEntity.getJobClass())
				&& dbSetting.getJobGroup().equals(zkEntity.getJobGroup())
				&& dbSetting.getJobParameter().equals(zkEntity.getJobParameter()))
			return false;
		return true;

	}

	private boolean shouldUpdate(JobSettings dbSetting, JobSettings zkEntity) {
		if (dbSetting.getCron().equals(zkEntity.getCron()) && dbSetting.isFailover() == zkEntity.isFailover()
				&& dbSetting.isMisfire() == zkEntity.isMisfire() &&

				dbSetting.getDescription().equals(zkEntity.getDescription()) &&
				// dbSetting.getJobShardingStrategyClass().equals(zkEntity.getJobShardingStrategyClass())
				// &&
				dbSetting.getReconcileIntervalMinutes() == zkEntity.getReconcileIntervalMinutes()
				&& dbSetting.getMaxTimeDiffSeconds() == zkEntity.getMaxTimeDiffSeconds()
				&& dbSetting.getShardingItemParameters().equals(zkEntity.getShardingItemParameters())
				&& dbSetting.getShardingTotalCount() == zkEntity.getShardingTotalCount()

		)
			return false;
		return true;

	}

	private JobSettings updateSetting(JobSettings dbSetting, JobSettings zkEntity) {

		zkEntity.setCron(dbSetting.getCron());
		zkEntity.setFailover(dbSetting.isFailover());
		zkEntity.setMisfire(dbSetting.isMisfire());
		zkEntity.setDescription(dbSetting.getDescription());
		zkEntity.setReconcileIntervalMinutes(dbSetting.getReconcileIntervalMinutes());
		zkEntity.setMaxTimeDiffSeconds(dbSetting.getMaxTimeDiffSeconds());
		zkEntity.setShardingItemParameters(dbSetting.getShardingItemParameters());
		zkEntity.setShardingTotalCount(dbSetting.getShardingTotalCount());
		return zkEntity;

	}

	public void compareJobsFromZK(String selfName) {

		logger.info("synchronizing jobs from database by job:" + selfName);
		QueryWrapper queryWrapper = new QueryWrapper<>();

		List<JobSettings> list = jobSettingsMapper.selectList(queryWrapper);

		List<String> dbJobNameList = new ArrayList<String>();

		for (JobSettings dbSetting : list) {
			String jobName = dbSetting.getJobName();
			if (dbSetting.getStatus() == null || dbSetting.getStatus().isEmpty())
				dbSetting.setStatus(JobOperateAPIImpl.Enabled);
			dbJobNameList.add(jobName);
			JobSettings zkEntity = null;
			try {
				zkEntity = liteJobOperation.getJobSetting(jobName);
			} catch (NullPointerException e) {
				zkEntity = null;
				// nothing need to do
			}

			if (zkEntity == null && dbSetting.getStatus().equals(JobOperateAPIImpl.Enabled)) {
				logger.info("find new job:" + jobName);
				try {
					//don't need 
					liteJobCreateFactory.createJobScheduler(dbSetting,true);
				} catch (Exception e) {
					logger.info("create new job fail,jobname is" + jobName, e);
				}

			} else if (dbSetting.getStatus().equals(JobOperateAPIImpl.Removed)) {
				removeJob(jobName);
				continue;

			} else if (dbSetting.getStatus().equals(JobOperateAPIImpl.Shutdown)) {
				shutdownJob(jobName);
				continue;

			} else if (dbSetting.getStatus().equals(JobOperateAPIImpl.Disabled)) {
				if (!liteJobOperation.getJobStatus(jobName).equals(JobOperateAPIImpl.Disabled)
						&& !liteJobOperation.getJobStatus(jobName).equals(JobOperateAPIImpl.Shutdown)
						&& !liteJobOperation.getJobStatus(jobName).equals(JobOperateAPIImpl.Removed)) {

					liteJobOperation.disableJob(jobName);
				}
				continue;
			} else if (shouldRemoveAndRestart(dbSetting, zkEntity)) {
				logger.info("remove and reset job:" + jobName);

				try {
					liteJobOperation.removeJob(jobName);
					Thread.sleep(1000 * 10);
					//don't update data in database
					liteJobCreateFactory.createJobScheduler(dbSetting,true);

				} catch (Exception e) {
					logger.info("remove and reset job fail,jobname is" + jobName, e);
				}

			} else if (shouldUpdate(dbSetting, zkEntity)) {
				logger.info("update job:" + jobName);

				try {
					liteJobOperation.setJobSetting(updateSetting(dbSetting, zkEntity));

				} catch (Exception e) {
					logger.info("update job fail,jobname is" + jobName, e);
				}

			}

			if (!liteJobOperation.getJobStatus(jobName).equals(JobOperateAPIImpl.Enabled)) {
				liteJobOperation.enableJob(jobName);
			}
		}

		Collection<JobBriefInfo> jobs = liteJobOperation.getAllJobsBriefInfo();
		for (JobBriefInfo job : jobs) {
			String jobName = job.getJobName();
			if (selfName.equals(jobName)) {// don't remove self
				continue;
			}

			if (!dbJobNameList.contains(job.getJobName())) {
				removeJob(jobName);

			}
			// JobSettings entity = simpleCronJob.getJobSetting(job.getJobName());
			// jobSettingsMapper.insert(entity);
		}
	}

	private void shutdownJob(String jobName) {
		try {
			String status = liteJobOperation.getJobStatus(jobName);
			if (!status.equals(JobOperateAPIImpl.Shutdown)) {
				logger.info("shutdown  job:" + jobName);
				liteJobOperation.shutdownJob(jobName);
			}
		} catch (Exception e) {
			logger.info("shutdown   job fail,jobname is" + jobName, e);
		}

	}

	private void removeJob(String jobName) {
		try {
			String status = liteJobOperation.getJobStatus(jobName);
			if (!status.equals(JobOperateAPIImpl.Removed)) {
				logger.info("remove job:" + jobName);
				liteJobOperation.removeJob(jobName);
			}
		} catch (Exception e) {
			logger.info("remove   job fail,jobname is" + jobName, e);
		}
	}
}
