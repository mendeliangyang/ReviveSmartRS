/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket;

import common.model.MsgClientPushParam;

/**
 *
 * @author Administrator
 */
public interface IAnalyzeMessage {
    public MsgClientPushParam transferMsg(String param);  
}
