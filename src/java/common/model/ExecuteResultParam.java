/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.model;

import common.DeployInfo;
import java.util.Iterator;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Administrator
 */
public class ExecuteResultParam {

    public ExecuteResultParam() {
    }

    public ExecuteResultParam(int pCode) {
        this.ResultCode = pCode;
    }

    public ExecuteResultParam(int pCode, String pErrMsg) {
        this.ResultCode = pCode;
        this.errMsg = pErrMsg;
    }

    public ExecuteResultParam(int pCode, String pErrMsg, JSONObject jsonObject) {
        this.ResultCode = pCode;
        this.errMsg = pErrMsg;
        this.ResultJsonObject = jsonObject;
    }

    public ExecuteResultParam(JSONObject jsonObject) {
        this.ResultJsonObject = jsonObject;
    }

    public ExecuteResultParam(Map<String, Map<String, String>> resultMap, boolean isArray) {
        JSONArray array = new JSONArray();
        for (Iterator<String> iterator = resultMap.keySet().iterator(); iterator.hasNext();) {
            String next = iterator.next();
            array.add(JSONObject.fromObject(resultMap.get(next)));
        }
        JSONObject obj = new JSONObject();
        obj.accumulate(DeployInfo.ResultDataTag, array);
        this.ResultJsonObject = obj;
    }

    public ExecuteResultParam(String pErrMsg, String pExecuteStr) {
        this.errMsg = pErrMsg;
        this.executeStr = pExecuteStr;
    }

    public ExecuteResultParam(String pErrMsg, String pExecuteStr, Exception pException) {
        this.errMsg = pErrMsg;
        this.executeStr = pExecuteStr;
        this.exception = pException;
    }

    public JSONObject ResultJsonObject;
    public int ResultCode;
    public String errMsg;
    public Exception exception;
    public String executeStr;

}
