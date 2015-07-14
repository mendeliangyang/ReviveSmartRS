/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket;

import common.FormationResult;
import common.VerificationSign;
import common.model.ExecuteResultParam;
import common.model.MsgClientPush;
import common.model.MsgFilterModel;
import common.model.ResponseResultCode;
import common.model.SystemSetModel;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
@ServerEndpoint("/InquireTrial")
public class AssignTrial {

    private final static Set<Session> peers = Collections.synchronizedSet(new HashSet<Session>());

    public final static Map<MsgFilterModel, Set<Session>> pushMap = Collections.synchronizedMap(new HashMap<MsgFilterModel, Set<Session>>());

    final static IAnalyzeMessage analyzeMsg = new AnalyzeMsg();

    static {
        //根据配置的 pushid 分配需要推送的消息队列
        //初始化map
        try {
            Set<SystemSetModel> systemSet = common.DeployInfo.GetSystemSets();
            for (Iterator<SystemSetModel> iterator = systemSet.iterator(); iterator.hasNext();) {
                SystemSetModel next = iterator.next();
                for (Iterator<MsgFilterModel> iterator1 = next.msgFilters.iterator(); iterator1.hasNext();) {
                    MsgFilterModel next1 = iterator1.next();
                    pushMap.put(next1, new HashSet<>());
                }
            }
            //启动线程
            Thread decoyThread = new Thread(new Decoy(), "decoyThread");
            decoyThread.start();
            //启动守护线程
            Thread reapDataGuardThread = new Thread(new ReapDataGuard(decoyThread), "reapDataGuardThread");
            reapDataGuardThread.start();
        } catch (Exception e) {
        }

    }

    AssignTrial() throws Exception {

    }

    @OnMessage
    public void onMessage(Session session, String msgParam) {
        System.out.println("AssignTrial onMessage " + session.getId() + " msg: " + msgParam);
        try {
            MsgClientPush msgClient = analyzeMsg.transferMsg(msgParam);
            FormationResult formationResult = new FormationResult();
            if (msgClient == null) {
               WebSocketHelper.sendTextToClient(session, formationResult.formationResult(ResponseResultCode.Error,new ExecuteResultParam("解析参数失败", msgParam)));
                return;
            }
            //todo 验证用户名密码
            if(!VerificationSign.verificationSignUser(msgClient.userName,msgClient.userPwd)){
                WebSocketHelper.sendTextToClient(session, formationResult.formationResult(ResponseResultCode.Error,new ExecuteResultParam("用户名或密码错误", msgParam)));
                return;
            }
            
            for (MsgFilterModel msgFilterModel : pushMap.keySet()) {
                for (String clientPushMsgId : msgClient.pushIds) {
                    if (msgFilterModel.pushMsgId.equals(clientPushMsgId)) {
                        synchronized (pushMap) {
                            if (!pushMap.get(msgFilterModel).contains(session)) {
                                pushMap.get(msgFilterModel).add(session);
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            common.RSLogger.ErrorLogInfo("webSocket onMessage error." + e.getLocalizedMessage());
            System.out.printf(e.getLocalizedMessage());
        }
    }

    @OnError
    public void onError(Session session, Throwable t) {
        common.RSLogger.ErrorLogInfo("AssignTrial onError" + t.getLocalizedMessage());
        System.out.println("AssignTrial onError" + session.getId() + "error");
    }

    @OnOpen
    public void onOpen(Session session) {
        //peers.add(session);
        System.out.println("AssignTrial onOpen" + session.getId() + "open");
    }

    @OnClose
    public void onClose(Session session) {
        //peers.remove(session);
        System.out.println("AssignTrial onClose" + session.getId() + "close");
    }

}
