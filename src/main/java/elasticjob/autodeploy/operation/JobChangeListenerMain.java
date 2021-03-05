package elasticjob.autodeploy.operation;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.AnnotatedElementUtils;

import com.dangdang.ddframe.job.lite.api.strategy.JobInstance;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;

//@Configuration
 
//@EnableAutoConfiguration  
@SpringBootApplication
//@ImportResource("classpath:applicationContext.xml")
@ComponentScan(basePackages="${ComponentScan.basePackages}" )
public class JobChangeListenerMain {
	
	
	public static void main(String[] args) {
        

		ConfigurableApplicationContext applicationContext = SpringApplication.run(JobChangeListenerMain.class, args);

//		applicationContext.getBeansWithAnnotation(ComponentScan.class).forEach((name, instance) -> {
//		    Set<ComponentScan> scans = AnnotatedElementUtils.getMergedRepeatableAnnotations(instance.getClass(), ComponentScan.class);
//		    for (ComponentScan scan : scans) {
//		        System.err.println(Arrays.toString(scan.basePackageClasses()));
//		        System.err.println(Arrays.toString(scan.basePackages()));
//		    }
//		});
	}
	
	
	@Bean
	  public static PropertySourcesPlaceholderConfigurer createPropertyConfigurer()
	  {
	    PropertySourcesPlaceholderConfigurer propertyConfigurer = new PropertySourcesPlaceholderConfigurer();
	    propertyConfigurer.setTrimValues(true);
	    return propertyConfigurer;
	  }
	 
}
