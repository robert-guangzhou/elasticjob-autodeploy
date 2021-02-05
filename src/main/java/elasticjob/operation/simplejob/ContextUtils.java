package elasticjob.operation.simplejob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ContextUtils {
	
	   @Autowired
	   public   ApplicationContext context;

	    private ContextUtils() {
	    }

	    public   void setApplicationContext(ApplicationContext applicationContext) {
	        context = applicationContext;
	    }

	    public   Object getBean(String beanName) {
	        return context.getBean(beanName);
	    }

	    public   <T> T getBean(Class<T> t) {
	        return context.getBean(t);
	    }
}
