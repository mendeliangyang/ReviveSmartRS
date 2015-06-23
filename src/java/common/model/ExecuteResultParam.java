/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.model;

import net.sf.json.JSONObject;

/**
 *
 * @author Administrator
 */
public class ExecuteResultParam {
    public ExecuteResultParam(){}
    public ExecuteResultParam(int pCode){
        this.ResultCode = pCode;
    }
    public ExecuteResultParam(int pCode,String pErrMsg){
        this.ResultCode = pCode;
        this.errMsg = pErrMsg;
    } 
    public ExecuteResultParam(int pCode,String pErrMsg,JSONObject jsonObject){
        this.ResultCode = pCode;
        this.errMsg = pErrMsg;
        this.ResultJsonObject = jsonObject;
    }
    public JSONObject ResultJsonObject;
    public int ResultCode;
    public String errMsg;
    
}
