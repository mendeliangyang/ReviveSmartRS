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
                        if (msg1.pkValues_inserts == null) {
                            msg1.pkValues_inserts = new HashSet<>();
                        }
                        msg1.pkValues_inserts.add(msg.pkValues_insert);
                    } else if (msg1.varyType == 2) {
                        if (msg1.pkValues_updates == null) {
                            msg1.pkValues_updates = new HashSet<>();
                        }
                        msg1.pkValues_updates.add(msg.pkValues_update);
                    } else if (msg1.varyType == 4) {
                        if (msg1.pkValues_deletes == null) {
                            msg1.pkValues_deletes = new HashSet<>();
                        }
                        msg1.pkValues_deletes.add(msg.pkValues_delete);
                    }
                    msg1.varyType = msg1.varyType | msg.varyType;
                    return false;
                }
            }

            if (msg.varyType == 1) {
                msg.pkValues_inserts = new HashSet<>();
                msg.pkValues_inserts.add(msg.pkValues_insert);
            } else if (msg.varyType == 2) {
                msg.pkValues_updates = new HashSet<>();
                msg.pkValues_updates.add(msg.pkValues_update);
            } else if (msg.varyType == 4) {
                msg.pkValues_deletes = new HashSet<>();
                msg.pkValues_deletes.add(msg.pkValues_delete);
            }
            msgs.add(msg);
            return true;
        }
    }

}
