/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket;

import common.model.DataVaryModel;
import common.model.MsgFilterModel;
import java.util.HashSet;
import java.util.Set;
import javax.websocket.Session;
import jms.JMSQueueMessage;

/**
 *
 * @author Administrator
 */
public class Decoy implements Runnable {

    @Override
    public void run() {
        HashSet<DataVaryModel> msgs = null;

        try {
            //检查jmsqueue是否有新的消息，并且分发处理
            //将jmsqueue clone一份
            msgs = JMSQueueMessage.HandelMsgQueue();
            if (msgs == null) {
                return;
            }
            for (DataVaryModel msg : msgs) {
                //找到 tableName 和msg 一致的 集合进行查询数据和分发
                for (MsgFilterModel msgFilterModel : AssignTrial.pushMap.keySet()) {
                    Set<Session> sessions = AssignTrial.pushMap.get(msgFilterModel);
                    if (sessions == null || sessions.isEmpty()) {
                        //System.out.println(msgFilterModel.pushMsgId + "no session,so no reapdata");
                        //这个key下面没有session 不需要查询数据
                        continue;
                    }
                    if (msgFilterModel.dbTable.equals(msg)) {
                        //启动新的线程处理该数据变动
//                            Thread reapDataThread = new Thread(new ReapData(msgFilterModel, sessions));
//                            reapDataThread.start();
                        // add pkvalues to filterModel
                        msgFilterModel.varyData = msg;
                        common.RSThreadPool.ThreadPoolExecute(new ReapData(msgFilterModel, sessions));
                    }
                }
            }
        } catch (Exception ex) {
            common.RSLogger.ErrorLogInfo("Decoy error." + ex.getLocalizedMessage());
        }

    }
}
