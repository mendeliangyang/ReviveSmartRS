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
public class JMSQueueAsyncWrite implements IJMSQueueAsyncWrite {

    
    @Override
    public boolean AsyncWriteMessage(HashSet<String> msgs, String msg) {
        synchronized (msgs) {
            for (String msgTemp : msgs) {
                if (msg.equals(msgTemp)) {
                    return false;
                }
            }
            msgs.add(msg);
            System.out.println("write msgCount: "+msgs.size()+"    time: "+new Date());
            return true;
        }
    }

}
