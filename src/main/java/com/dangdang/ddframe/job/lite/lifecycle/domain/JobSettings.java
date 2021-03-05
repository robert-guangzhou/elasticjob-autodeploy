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

package com.dangdang.ddframe.job.lite.lifecycle.domain;
 
import com.dangdang.ddframe.job.executor.handler.JobProperties.JobPropertiesEnum;
import com.dangdang.ddframe.job.lite.lifecycle.internal.operate.JobOperateAPIImpl;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * 作业设置对象.
 * 
 * @author zhangliang
 */
@Getter
@Setter
@JsonIgnoreProperties
public final class JobSettings implements Serializable {
	
	
	

     private static final long serialVersionUID = -6532210090618686688L;
    
	
 	private String jobName;
    
    private String jobType;
    
    private String jobClass;
    
    private String cron;
    
    private int shardingTotalCount;
    
    private String shardingItemParameters;
    
    private String jobParameter;
    
    private boolean monitorExecution;
    
    private boolean streamingProcess;
    
    private int maxTimeDiffSeconds;
    
    private int monitorPort = -1;
    
    private boolean failover;
    
    private boolean misfire;
    
    private String jobShardingStrategyClass;
    
    private String description;
    
 	private Map<String, String> jobProperties = new LinkedHashMap<>(JobPropertiesEnum.values().length, 1);
    
    private String scriptCommandLine;
    
    private int reconcileIntervalMinutes;
    
    private String jobGroup="default";
    
    private String status=JobOperateAPIImpl.Enabled;


    
    public JobSettings(String job_name, String job_type, String job_class, String cron, int sharding_total_count, String sharding_item_parameters,
			String job_parameter, boolean monitor_execution, boolean streaming_process, int max_time_diff_seconds, int monitor_port, boolean failover, boolean misfire,
			String job_sharding_strategy_class, String description, String script_command_line, int reconcile_interval_minutes, String job_group, String status) {
		// TODO Auto-generated constructor stub
    	
    	super();
    	this.jobName=job_name;
    	this.description=description;
    	this.jobClass=job_class;
    	this.cron=cron;
    	this.shardingTotalCount=sharding_total_count;
    	this.shardingItemParameters=sharding_item_parameters;
    	this.jobParameter=job_parameter;
    	this.monitorExecution=monitor_execution;
    	this.streamingProcess=streaming_process;
    	this.maxTimeDiffSeconds=max_time_diff_seconds;
    	this.monitorPort=monitor_port;
    	this.failover=failover;
    	this.misfire=misfire;
    	this.jobShardingStrategyClass=job_sharding_strategy_class;
    	this.scriptCommandLine=script_command_line;
    	this.reconcileIntervalMinutes=reconcile_interval_minutes;
    	this.jobGroup=job_group;
    	this.status=status;
    	this.jobType=job_type;
    	
    	
	}



	public JobSettings() {
		// TODO Auto-generated constructor stub
	}
    
}
