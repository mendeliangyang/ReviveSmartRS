/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket;

import common.DBHelper;
import common.DeployInfo;
import common.FormationResult;
import common.model.ExecuteResultParam;
import common.model.MsgFilterModel;
import common.model.ResponseResultCode;
import common.model.ReviveRSParamModel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.websocket.Session;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Administrator
 */
public class ReapData implements Runnable {

    MsgFilterModel msgFilterModel = null;
    Set<Session> sessions = null;

    public ReapData(MsgFilterModel pMsgFilterModel, Set<Session> pSessions) {
        msgFilterModel = pMsgFilterModel;
        sessions = pSessions;
    }

    @Override
    public void run() {
        FormationResult formationResult = null;
        ReviveRSParamModel paramModel = null;
        String strResult = null;
        ExecuteResultParam result = null, result1 = null;
        Set<Session> closeSessions = null;
        try {
            if (msgFilterModel == null || sessions == null || sessions.isEmpty()) {
                return;
            }
            formationResult = new FormationResult();
            //构造reviveRSParam
            paramModel = new ReviveRSParamModel();

            paramModel.rsid = msgFilterModel.rsid;
            paramModel.sql = msgFilterModel.sqlFilter;
            paramModel.db_tableName = msgFilterModel.dbTable;

            if (msgFilterModel.pageSize > 0) {
                paramModel.db_RULcolumns = msgFilterModel.dbURLColumns;
                paramModel.db_columns = msgFilterModel.dbColumns;
                paramModel.db_pageSize = msgFilterModel.pageSize;
                paramModel.db_pageNum = 1;

                result = DBHelper.ExecuteSqlSelect(paramModel.rsid, DBHelper.SqlSelectPageFactory(paramModel));
                result1 = DBHelper.ExecuteSqlSelect(paramModel.rsid, DBHelper.SqlSelectCountFactory(paramModel));
                //todo  websocket 添加推送验证（登录） token  替换登录 token
                if (result.ResultCode >= 0) {
                    if (result1 != null && result1.ResultCode >= 0) {
                        JSONArray rowsCountJson = result1.ResultJsonObject.getJSONArray(DeployInfo.ResultDataTag);
                        Iterator iteratorRows = rowsCountJson.iterator();
                        JSONObject rowsCount = (JSONObject) iteratorRows.next();
                        result.ResultJsonObject.accumulate("rowsCount", rowsCount.getString("rowsCount"));
                    }
                    strResult = formationResult.formationResult(ResponseResultCode.Success,"token",msgFilterModel.pushMsgId, new ExecuteResultParam( result.ResultJsonObject));
                } else {
                    strResult = formationResult.formationResult(ResponseResultCode.Error, "token",msgFilterModel.pushMsgId, new ExecuteResultParam(result.errMsg,paramModel.sql));
                }
            } else {
                result = DBHelper.ExecuteSqlSelect(paramModel.rsid, DBHelper.SqlSelectCountFactory(paramModel));
                if (result.ResultCode >= 0) {
                    strResult = formationResult.formationResult(ResponseResultCode.Success,"token",msgFilterModel.pushMsgId, new ExecuteResultParam( result.ResultJsonObject));
                } else {
                    strResult = formationResult.formationResult(ResponseResultCode.Error, "token",msgFilterModel.pushMsgId, new ExecuteResultParam(result.errMsg,paramModel.sql));
                }
            }
            closeSessions = new HashSet<>();
            for (Session session : sessions) {
                boolean isOpen = WebSocketHelper.asyncSendTextToClient(session, strResult);
                if (!isOpen) {
                    closeSessions.add(session);
                }
            }
            for (Session closeSession : closeSessions) {
                sessions.remove(closeSession);
            }
            System.out.println("push msg sessionCount: " + sessions.size());
            paramModel = null;

        } catch (Exception ex) {
            common.RSLogger.ErrorLogInfo("ReapData error." + ex.getLocalizedMessage());
        } finally {
            common.UtileSmart.FreeObjects(formationResult, paramModel, strResult, result, result1, closeSessions);
        }
    }

}
