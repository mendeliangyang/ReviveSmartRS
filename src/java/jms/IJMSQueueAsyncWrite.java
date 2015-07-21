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
public interface IJMSQueueAsyncWrite {
    public boolean AsyncWriteMessage(HashSet<DataVaryModel> msgs,DataVaryModel msg);
}
