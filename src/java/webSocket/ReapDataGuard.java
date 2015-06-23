/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket;


/**
 *
 * @author Administrator
 */
public class ReapDataGuard implements Runnable {

    public ReapDataGuard(Thread thread) {
        targetThread = thread;
    }

    private final Thread targetThread;

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(7000);
                if (!targetThread.isAlive()) {
                    //todo restart failed
                    targetThread.start();
                }
            }
        } catch (InterruptedException ex) {
            common.RSLogger.ErrorLogInfo("reapDataGuardThread runError."+ex.getLocalizedMessage());
        }
    }

}
