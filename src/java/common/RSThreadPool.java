/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
}
