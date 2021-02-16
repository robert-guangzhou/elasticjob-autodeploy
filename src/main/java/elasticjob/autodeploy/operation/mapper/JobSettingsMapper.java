package elasticjob.autodeploy.operation.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobSettings;

@Mapper
public interface JobSettingsMapper extends BaseMapper<JobSettings> {

}
