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
import common.model.MsgClientPushParam;
import common.model.MsgFilterModel;
import common.model.ResponseResultCode;
import common.model.ReviveRSParamModel;
import java.util.Iterator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import static webSocket.AssignTrial.pushMap;

/**
 *
 * @author Administrator
 */
public class ManualPushMsg implements Runnable {

    public MsgClientPushParam msgParam = null;

    public ManualPushMsg(MsgClientPushParam p) {
        msgParam = p;
    }

    @Override
    public void run() {

        ReviveRSParamModel paramModel = null;
        ExecuteResultParam result = null;
        ExecuteResultParam result1 = null;
        String strResult = null;
        FormationResult formationResult = new FormationResult();
        try {
            if (msgParam == null || msgParam.sessionClient == null || msgParam.pushIds == null || msgParam.pushIds.isEmpty()) {
                return;
            }
            //构造reviveRSParam
            for (MsgFilterModel msgFilterModel : pushMap.keySet()) {
                for (String clientPushMsgId : msgParam.pushIds) {
                    if (msgFilterModel.pushMsgId.equals(clientPushMsgId)) {
                        paramModel = new ReviveRSParamModel();
                        paramModel.rsid = msgFilterModel.rsid;
                        paramModel.sql = msgFilterModel.sqlFilter;
                        paramModel.db_tableName = msgFilterModel.dbTable;

                        if (msgFilterModel.pageSize > 0) {
                            paramModel.db_RULcolumns = msgFilterModel.dbURLColumns;
                            paramModel.db_columns = msgFilterModel.dbColumns;
                            paramModel.db_pageSize = msgFilterModel.pageSize;
                            paramModel.db_pageNum = 1;

                            try {

                                result = DBHelper.ExecuteSqlSelect(paramModel.rsid, DBHelper.SqlSelectPageFactory(paramModel));
                                result1 = DBHelper.ExecuteSqlSelect(paramModel.rsid, DBHelper.SqlSelectCountFactory(paramModel));

                            } catch (Exception ex) {
                                common.RSLogger.wsErrorLogInfo(paramModel.getInformation(), ex);
                            }

                            //todo  websocket 添加推送验证（登录） token  替换登录 token
                            if (result.ResultCode >= 0) {
                                if (result1 != null && result1.ResultCode >= 0) {
                                    JSONArray rowsCountJson = result1.ResultJsonObject.getJSONArray(DeployInfo.ResultDataTag);
                                    Iterator iteratorRows = rowsCountJson.iterator();
                                    JSONObject rowsCount = (JSONObject) iteratorRows.next();
                                    result.ResultJsonObject.accumulate("rowsCount", rowsCount.getString("rowsCount"));
                                }
                                strResult = formationResult.formationResult(ResponseResultCode.Success, "token", msgFilterModel.pushMsgId, new ExecuteResultParam(result.ResultJsonObject));
                            } else {
                                strResult = formationResult.formationResult(ResponseResultCode.Error, "token", msgFilterModel.pushMsgId, new ExecuteResultParam(result.errMsg, paramModel.sql));
                            }
                        } else {
                            try {
                                result = DBHelper.ExecuteSqlSelect(paramModel.rsid, DBHelper.SqlSelectCountFactory(paramModel));
                            } catch (Exception ex) {
                                common.RSLogger.wsErrorLogInfo(paramModel.getInformation(), ex);
                            }
                            if (result.ResultCode >= 0) {
                                strResult = formationResult.formationResult(ResponseResultCode.Success, "token", msgFilterModel.pushMsgId, new ExecuteResultParam(result.ResultJsonObject));
                            } else {
                                strResult = formationResult.formationResult(ResponseResultCode.Error, "token", msgFilterModel.pushMsgId, new ExecuteResultParam(result.errMsg, paramModel.sql));
                            }
                        }
                        WebSocketHelper.asyncSendTextToClient(msgParam.sessionClient, strResult);
                    }
                }
            }
        } catch (Exception e) {
            common.RSLogger.wsErrorLogInfo(String.format("ManualPushMsg param : %s", paramModel.getInformation()), e);
        }

    }

}
