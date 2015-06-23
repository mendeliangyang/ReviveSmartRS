/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webSocket;

import common.UtileSmart;
import common.model.MsgClientPush;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Administrator
 */
public class AnalyzeMsg implements IAnalyzeMessage {

    @Override
    public MsgClientPush transferMsg(String param) {
        MsgClientPush msgclientPush = null;
        JSONObject jsonObj = null,jsonBody=null;
        JSONArray jsonArrayPushIds=null;
        try {
            //localhost:8007/ReviveSmartRS/InquireTrial
            // {"head": { "RSID":"AustraliaBank" },"body": {  "pushId":["DebitCardNum1"] }  }
            //{"head": { "RSID":"AustraliaBank" },"body": { "rs_Name":"demo","rs_Pwd":"demo", "pushId":["DebitCardNum1","DebitCardNum2"] }  }
            jsonObj = JSONObject.fromObject(param);
            msgclientPush = new MsgClientPush();
            msgclientPush.rsid = jsonObj.getJSONObject("head").getString("RSID");
            jsonBody = jsonObj.getJSONObject("body");
            msgclientPush.userName = common.UtileSmart.GetJsonString(jsonBody, "rs_Name");
            msgclientPush.userPwd = common.UtileSmart.GetJsonString(jsonBody, "rs_Pwd");
            jsonArrayPushIds= jsonBody.getJSONArray("pushId");
            for (int i = 0; i < jsonArrayPushIds.size(); i++) {
                msgclientPush.pushIds.add(jsonArrayPushIds.getString(i))  ;
            }
        } catch (Exception e) {
            common.RSLogger.ErrorLogInfo("transferMsg error. /n"+"param : "+param+" ."+e.getLocalizedMessage());
        }finally{
            UtileSmart.FreeObjects(jsonObj,jsonArrayPushIds);
        }
        return msgclientPush;
    }

}
