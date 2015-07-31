/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket.transfer;

import java.io.IOException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author Administrator
 */
@ServerEndpoint("/advisory")
public class advisoryTransfer {

    @OnMessage
    public String onMessage(Session session,String message) throws IOException {
       System.out.println(message);
       session.getBasicRemote().sendText("over");
        return null;
    }
    
     @OnError
    public void onError(Session session, Throwable t) {
        common.RSLogger.wsErrorLogInfo("AssignTrial onError" + t.getLocalizedMessage(), new Exception(t));
    }

    @OnOpen
    public void onOpen(Session session) {
        //peers.add(session);
        //common.RSLogger.LogInfo(String.format("AssignTrial onOpen '%s' open", session.getId()));
        System.out.println(String.format("AssignTrial onOpen '%s' open", session.getId()));
    }

    @OnClose
    public void onClose(Session session) {
        //peers.remove(session);
       // common.RSLogger.wsErrorLogInfo(String.format("AssignTrial onClose '%s' close", session.getId()));
         System.out.println(String.format("AssignTrial onClose '%s' open", session.getId()));
    }

    
}
