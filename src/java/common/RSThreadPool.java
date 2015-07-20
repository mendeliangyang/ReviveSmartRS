/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Administrator
 */
public class RSThreadPool {

    static ExecutorService rsCachedThreadPool = null;

    public static boolean initialTheadPool() {
        rsCachedThreadPool = Executors.newCachedThreadPool();
        return true;
    }

    public static void ThreadPoolExecute(Runnable run) {
        rsCachedThreadPool.execute(run);
    }

    public static void singleThreadExecuter(Runnable run) {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.execute(run);
    }
    
    /**
     * definite time take
     * @param run Runnable commoand
     * @param corePoolSize 
     * @param initialDelay 
     * @param period 
     * @param unit 
     */
    public static void scheduledThreadPoolExecutor(Runnable run,int corePoolSize, long initialDelay, long period,TimeUnit unit){
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(corePoolSize);
        exec.scheduleAtFixedRate(run, initialDelay, period, unit);
    }
    

}
