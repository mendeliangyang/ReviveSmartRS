/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jms;

import java.util.Date;
import java.util.HashSet;

/**
 *
 * @author Administrator
 */
public class JMSQueueMessage {

    private final static HashSet<String> msgQueue = new HashSet<>();

//    public static HashSet<String> GetMsgQueueTranscript() {
//        return (HashSet<String>) msgQueue.clone();
//    }
    public static HashSet<String> HandelMsgQueue() {
        if (msgQueue.isEmpty()) {
            return null;
        }
        HashSet<String> newMsg = null;
        synchronized (msgQueue) {
            System.out.println("handel msgCount: "+msgQueue.size()+"    time: "+new Date());
            newMsg = (HashSet<String>) msgQueue.clone();
            msgQueue.clear();
        }
        return newMsg;
    }

    public static void AsyncWriteMessage(String msg) {
        try {
            Thread t = new Thread(new AsyncThreadWriteMsg(msg));
            t.start();
        } catch (Exception e) {
            common.RSLogger.ErrorLogInfo("AsyncWriteMessage error." + msg + e.getLocalizedMessage());
        }
    }

    public static class AsyncThreadWriteMsg implements Runnable {

        private final IJMSQueueAsyncWrite asyncWrite = new JMSQueueAsyncWrite();
        private String message;

        AsyncThreadWriteMsg(String pMsg) {
            this.message = pMsg;
        }

        @Override
        public void run() {
            asyncWrite.AsyncWriteMessage(msgQueue, message);
        }
    }
}
