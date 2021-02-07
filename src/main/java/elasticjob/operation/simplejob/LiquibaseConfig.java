package elasticjob.operation.simplejob;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import liquibase.integration.spring.SpringLiquibase;

@Configuration
public class LiquibaseConfig {

	@Value("${autodeploy.elasticjob.liquibase.enabled:ture}")
	private boolean enabled;

	@Bean
	public SpringLiquibase liquibase(DataSource dataSource) {
		SpringLiquibase liquibase = new SpringLiquibase();
		 
			liquibase.setDataSource(dataSource);
			liquibase.setChangeLog("classpath:liquibase/db.changelog-master.yaml");
			liquibase.setContexts("development,test,production");
			liquibase.setShouldRun(enabled);
		 
		return liquibase;
	}

}