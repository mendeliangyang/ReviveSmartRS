/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.websocket.Session;

/**
 *
 * @author Administrator
 */
public class WebSocketHelper {

    public static boolean sendTextToClient(Session session, String strMsg) throws IOException {
        if (session.isOpen()) {
            session.getBasicRemote().sendText(strMsg);
            return true;
        }
        return false;
    }

    public static boolean asyncSendTextToClient(Session session, String strMsg) throws IOException {
        if (session.isOpen()) {
            session.getAsyncRemote().sendText(strMsg);
            common.RSLogger.wsErrorLogInfo(String.format("%s:%s", session.getId(), strMsg));
            return true;
        }
        return false;
    }

    /**
     * 批量发送消息，如果该session已经关闭。在session集合中 synchronized 移出该 session
     *
     * @param sessions
     * @param strMsg
     * @throws IOException
     */
    public static void asyncSendTextToClient(Set<Session> sessions, String strMsg) throws IOException {
        Set<Session> closeSessions = new HashSet<>();

        for (Session session : sessions) {
            if (session.isOpen()) {
                session.getAsyncRemote().sendText(strMsg);
            } else {
                closeSessions.add(session);
            }
        }

        for (Session closeSession : closeSessions) {
            synchronized (sessions) {
                sessions.remove(closeSession);
            }
        }

    }
}
