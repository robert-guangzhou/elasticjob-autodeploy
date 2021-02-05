package elasticjob.operation.testjob;
/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */



import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;

import elasticjob.operation.simplejob.SimpleCronJob;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaSimpleJob implements SimpleJob {
    
	private final static Logger logger = LoggerFactory.getLogger(JavaSimpleJob.class);

    @Override
    public void execute(final ShardingContext shardingContext) {
    	
    	
        logger.info(String.format("Pid:%d,jobName:%s, Item: %s | Time: %s | Thread: %s | %s",
        		getPid(),shardingContext.getJobName(),
                shardingContext.getShardingItem(), new SimpleDateFormat("HH:mm:ss").format(new Date()), Thread.currentThread().getId(), "SIMPLE"));
       
    }
    
    public static int getPid() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();        
        String name = runtime.getName(); // format: "pid@hostname"
        try {
            return Integer.parseInt(name.substring(0, name.indexOf('@')));
        } catch (Exception e) {
            return -1;
        }
    }  
}
