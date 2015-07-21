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
    static ExecutorService wsSingleThreadWrite = null;

    public static boolean initialTheadPool() {
        rsCachedThreadPool = Executors.newCachedThreadPool();
        wsSingleThreadWrite = Executors.newSingleThreadExecutor();
        return true;
    }

    public static void ThreadPoolExecute(Runnable run) {
        rsCachedThreadPool.execute(run);
    }

    /**
     * write message to ws-ms queue.
     * @param run 
     */
    public static void wsWriteMsgSingleThreadPool(Runnable run) {
        wsSingleThreadWrite.execute(run);
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
