/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jms;

import common.model.DataVaryModel;
import java.util.HashSet;

/**
 *
 * @author Administrator
 */
public class JMSQueueAsyncWrite implements IJMSQueueAsyncWrite {

    @Override
    public boolean AsyncWriteMessage(HashSet<DataVaryModel> msgs, DataVaryModel msg) {
        synchronized (msgs) {
            for (DataVaryModel msg1 : msgs) {
                if (msg1.tbName.equals(msg.tbName)) {
                    if (msg1.varyType == 1) {
                        msg1.pkValues_insert.putAll(msg.pkValues_insert);
                    } else if (msg1.varyType == 2) {
                        msg1.pkValues_update.putAll(msg.pkValues_update);
                    } else if (msg1.varyType == 4) {
                        msg1.pkValues_delete.putAll(msg.pkValues_delete);
                    }
                    msg1.varyType = msg1.varyType | msg.varyType;

                }
            }
//            for (String msgTemp : msgs) {
//                if (msg.equals(msgTemp)) {
//                    return false;
//                }
//            }
            msgs.add(msg);
            return true;
        }
    }

}
