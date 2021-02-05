package elasticjob.operation.simplejob;

import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;

public class EmptyElasticJobListener implements ElasticJobListener {

	@Override
	public void beforeJobExecuted(ShardingContexts shardingContexts) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterJobExecuted(ShardingContexts shardingContexts) {
		// TODO Auto-generated method stub

	}

}
