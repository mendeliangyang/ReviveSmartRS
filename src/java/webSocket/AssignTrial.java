/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket;

import common.FormationResult;
import common.VerificationSign;
import common.model.ExecuteResultParam;
import common.model.MsgClientPushParam;
import common.model.MsgFilterModel;
import common.model.ResponseResultCode;
import common.model.SystemSetModel;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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

    public static void initialWebSocketService() throws Exception {
        //根据配置的 pushid 分配需要推送的消息队列
        //初始化map
        Set<SystemSetModel> systemSet = common.DeployInfo.GetSystemSets();
        for (Iterator<SystemSetModel> iterator = systemSet.iterator(); iterator.hasNext();) {
            SystemSetModel next = iterator.next();
            for (Iterator<MsgFilterModel> iterator1 = next.msgFilters.iterator(); iterator1.hasNext();) {
                MsgFilterModel next1 = iterator1.next();
                pushMap.put(next1, new HashSet<>());
            }
        }
            //启动线程
//            Thread decoyThread = new Thread(new Decoy(), "decoyThread");
//            decoyThread.start();
        //启动守护线程
//            Thread reapDataGuardThread = new Thread(new ReapDataGuard(decoyThread), "reapDataGuardThread");
//            reapDataGuardThread.start();
        //scheduledThreadPoolExecutor replace thread , guardThread
        common.RSThreadPool.scheduledThreadPoolExecutor(new Decoy(), 1, 4000, 10000, TimeUnit.MILLISECONDS);
        common.RSLogger.SetUpLogInfo("websocket service initial success.");

    }

    AssignTrial() throws Exception {

    }

    @OnMessage
    public void onMessage(Session session, String msgParam) {
        common.RSLogger.LogInfo(String.format("onMessage： receive the message form %s :%s", session.getId(), msgParam));
        try {
            MsgClientPushParam msgClient = analyzeMsg.transferMsg(msgParam);
            FormationResult formationResult = new FormationResult();
            if (msgClient == null) {
                WebSocketHelper.sendTextToClient(session, formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("解析参数失败", msgParam)));
                return;
            }
            //todo 验证用户名密码
            if (!VerificationSign.verificationSignUser(msgClient.userName, msgClient.userPwd)) {
                WebSocketHelper.sendTextToClient(session, formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("用户名或密码错误", msgParam)));
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
            //register success, send pushid to client.
            //just send success to client.
            WebSocketHelper.sendTextToClient(session, formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam()));
            //search data from db by pushid, and send to client.
            //RSThreadPool.ThreadPoolExecute(new ManualPushMsg(msgClient));
        } catch (Exception e) {
            common.RSLogger.ErrorLogInfo("webSocket onMessage error." + e.getLocalizedMessage(), e);
            System.out.printf(e.getLocalizedMessage());
        }
    }

    @OnError
    public void onError(Session session, Throwable t) {
        common.RSLogger.ErrorLogInfo("AssignTrial onError" + t.getLocalizedMessage(), new Exception(t));
    }

    @OnOpen
    public void onOpen(Session session) {
        //peers.add(session);
        common.RSLogger.LogInfo(String.format("AssignTrial onOpen '%s' open", session.getId()));
    }

    @OnClose
    public void onClose(Session session) {
        //peers.remove(session);
        common.RSLogger.LogInfo(String.format("AssignTrial onClose '%s' close", session.getId()));
    }

}
