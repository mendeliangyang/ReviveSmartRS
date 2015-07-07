/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsSvc.Electornic;

import net.sf.json.JSONObject;

/**
 *
 * @author Administrator
 */
public class ElectornicBase {
    
    public final String  resultJsonKey ="result";
    public final String resultJsonInfoKey="infolist";

    /**
     * 组织返回结果
     * @param retCode
     * @param retMsg
     * @param token
     * @param retObject
     * @return
     */
    public String ResponseResult(EnumElectornicRetCode retCode, String retMsg,String token, Object retObject) {
        /*"retCode": "0000",
         "retMsg": "",
         "logNo": "2015070609227748502",
         "sessionId": "5te0q4004p4v03ao3kfbanry",
         */
        JSONObject resultJson = new JSONObject();
        resultJson.accumulate("retCode", retCode.getRetCode());
        if (retMsg.isEmpty()) {
            resultJson.accumulate("retMsg", retCode.getDescribe());
        } else {
            resultJson.accumulate("retMsg", retMsg);
        }
        if (!token.isEmpty()) {
            resultJson.accumulate("sessionId", token);
        }
        if (retObject != null) {
            resultJson.accumulate(resultJsonKey, retObject);
        }
        return resultJson.toString();
    }
}
