/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jms;

import common.model.DataVaryModel;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public class JMSQueueMessage {

    private final static HashSet<DataVaryModel> msgQueue = new HashSet<>();

//    public static HashSet<String> GetMsgQueueTranscript() {
//        return (HashSet<String>) msgQueue.clone();
//    }
    public static HashSet<DataVaryModel> HandelMsgQueue() {
        if (msgQueue.isEmpty()) {
            return null;
        }
        HashSet<DataVaryModel> newMsg = null;
        synchronized (msgQueue) {
            newMsg = (HashSet<DataVaryModel>) msgQueue.clone();
            msgQueue.clear();
        }
        return newMsg;
    }

    public static void AsyncWriteMessage(String tbName, int varyType, Map<String, String> pkValues) {
        try {
            DataVaryModel msgModel = new DataVaryModel();
            msgModel.tbName = tbName;

            msgModel.varyType = varyType;
            if (msgModel.varyType == 1) {
                msgModel.pkValues_insert = pkValues;
            } else if (msgModel.varyType == 2) {
                msgModel.pkValues_update = pkValues;
            } else if (msgModel.varyType == 4) {
                msgModel.pkValues_delete = pkValues;
            }
//            Thread t = new Thread(new AsyncThreadWriteMsg(msg));
//            t.start();
            common.RSThreadPool.wsWriteMsgSingleThreadPool(new AsyncThreadWriteMsg(msgModel));
        } catch (Exception e) {
            common.RSLogger.ErrorLogInfo(String.format("AsyncWriteMessage error. %s,", e.getLocalizedMessage()));
        }
    }

    public static class AsyncThreadWriteMsg implements Runnable {

        private final IJMSQueueAsyncWrite asyncWrite = new JMSQueueAsyncWrite();
        private DataVaryModel messageModel;

        AsyncThreadWriteMsg(DataVaryModel pMsg) {
            this.messageModel = pMsg;
        }

        @Override
        public void run() {
            asyncWrite.AsyncWriteMessage(msgQueue, messageModel);
        }
    }
}
