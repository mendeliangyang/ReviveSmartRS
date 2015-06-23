/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import common.comInterface.IFormationResult;
import common.model.ResponseResultCode;
import net.sf.json.JSONObject;

/**
 *
 * @author Administrator
 */
public class FormationResult implements IFormationResult {

    @Override
    public String formationResult(ResponseResultCode resultCode, String errMsg, Object... result) {
        JSONObject resultJson = new JSONObject();
        JSONObject resultHeadContext = new JSONObject();
        resultHeadContext.accumulate("resultCode", resultCode.toString());
        resultHeadContext.accumulate("errMsg", errMsg);
        resultJson.accumulate("head", resultHeadContext);
        if (result != null) {
            for (Object result1 : result) {
                //Object result1 = result[i];
                resultJson.accumulate("body", result1);
            }
        }
        //resultJson.accumulate("body", result);
        return resultJson.toString();
    }

    @Override
    public String formationResult(ResponseResultCode resultCode, String errMsg, String token, String pushId, Object... result) {
        JSONObject resultJson = new JSONObject();
        JSONObject resultHeadContext = new JSONObject();
        resultHeadContext.accumulate("resultCode", resultCode.toString());
        resultHeadContext.accumulate("errMsg", errMsg);
        resultHeadContext.accumulate("token", token);
        resultHeadContext.accumulate("pushId", pushId);
        resultJson.accumulate("head", resultHeadContext);
        if (result != null) {
            for (Object result1 : result) {
                //Object result1 = result[i];
                resultJson.accumulate("body", result1);
            }
        }
        //resultJson.accumulate("body", result);
        return resultJson.toString();
    }

    @Override
    public String formationResult(ResponseResultCode resultCode, String errMsg, String token, Object... result) {
        JSONObject resultJson = new JSONObject();
        JSONObject resultHeadContext = new JSONObject();
        resultHeadContext.accumulate("resultCode", resultCode.toString());
        resultHeadContext.accumulate("errMsg", errMsg);
        resultHeadContext.accumulate("token", token);
        resultJson.accumulate("head", resultHeadContext);
        if (result != null) {
            for (Object result1 : result) {
                //Object result1 = result[i];
                resultJson.accumulate("body", result1);
            }
        }
        //resultJson.accumulate("body", result);
        return resultJson.toString();
    }
}
