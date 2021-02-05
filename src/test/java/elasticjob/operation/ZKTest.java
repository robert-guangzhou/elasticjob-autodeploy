package elasticjob.operation;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
import org.apache.curator.framework.api.transaction.TransactionCreateBuilder;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryNTimes;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import elasticjob.operation.simplejob.SimpleCronJob;

public class ZKTest {

	@Autowired
	SimpleCronJob simpleCronJob;

	
	@Test 
	public void testTransaction() {
		CuratorFramework client=(CuratorFramework) simpleCronJob.getRegCenter().getRawClient();
		 CuratorTransaction transaction = client.inTransaction();  
		  
         Collection<CuratorTransactionResult> results;
		try {
			TransactionCreateBuilder builder = transaction.create();
			
			//transaction.commit();
			CuratorTransactionFinal actions = builder
	         .forPath("/a/path", "some data".getBytes()).and().setData()  
	         .forPath("/another/path", "other data".getBytes()).and().delete().forPath("/yet/another/path")  
	         .and();
			results = actions.commit();
			 for (CuratorTransactionResult result : results) {  
	             System.out.println(result.getForPath() + " - " + result.getType());  
	         }  
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  

        
 	}
	
	
	
	@Test 
	public void  TestLock() {
		CuratorFramework client = CuratorFrameworkFactory.newClient("192.168.157.140:2181", new RetryNTimes(
                10, 5000));
                
        client.start();//启动客户端别忘记了
        
        InterProcessMutex lock = new InterProcessMutex(client, "/mylock");
        CountDownLatch countDownLatch = new CountDownLatch(2);
        new Thread(()->{
            try {
                if (lock.acquire(5, TimeUnit.SECONDS)){
                    System.out.println(Thread.currentThread().getName()+" 获得了锁");
                    lock.release();
                }else
                    System.out.println(Thread.currentThread().getName()+" 没拿到锁");
            } catch (Exception e) {
                e.printStackTrace();
            }
            countDownLatch.countDown();
        },"一号线程").start();

        new Thread(()->{
            try {
                if (lock.acquire(5, TimeUnit.SECONDS)){
                    System.out.println(Thread.currentThread().getName()+" 获得了锁");
                    lock.release();
                }else
                    System.out.println(Thread.currentThread().getName()+" 没拿到锁");
            } catch (Exception e) {
                e.printStackTrace();
            }
            countDownLatch.countDown();
        },"二号线程").start();

        try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
