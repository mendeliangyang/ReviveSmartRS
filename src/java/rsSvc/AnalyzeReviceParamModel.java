/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsSvc;

import common.DeployInfo;
import common.UtileSmart;
import common.model.OperateTypeEnum;
import common.model.ReviveRSParamModel;
import common.model.SignModel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Administrator
 */
public class AnalyzeReviceParamModel implements IAnalyzeReviceParamModel {
    
    @Override
    public ReviveRSParamModel transferReviveRSParamModel(String param, OperateTypeEnum operateType) throws Exception {
        ReviveRSParamModel paramModel = null;
        JSONObject jsonObj = null, jsonBody = null, jsonNote = null, jsonHead = null, jsonValues = null, jsonPkvalue = null;
        JSONArray url_columns = null, db_columns = null;
        String strTemp = null;
        Object objPkvalue = null;
        try {
            jsonObj = JSONObject.fromObject(param);
            jsonHead = jsonObj.getJSONObject("head");
            paramModel = new ReviveRSParamModel();
            paramModel.rsid = UtileSmart.GetJsonString(jsonHead, "RSID", true);
            paramModel.token = UtileSmart.GetJsonString(jsonHead, "token");
            //file operate
            if (OperateTypeEnum.fileOperate == operateType) {
                paramModel.handle = UtileSmart.GetJsonString(jsonHead, "handle");
// todo    public String fileColumn; //base64，或者上传文件指定保存文件的列名称
            }
            jsonBody = jsonObj.getJSONObject("body");
            jsonNote = jsonBody.getJSONObject("note");
            jsonValues = jsonBody.getJSONObject("values");
            
            paramModel.db_tableName = UtileSmart.GetJsonString(jsonNote, DeployInfo.paramTableName, true);
            //get primary value.if string single clolumn ,if object multi column
            if (jsonNote.containsKey("pkValue")) {
                objPkvalue = jsonNote.get("pkValue");
                if (objPkvalue instanceof String) {
                    //String
                    paramModel.pkValue = UtileSmart.GetJsonString(jsonNote, "pkValue");
                } else if (objPkvalue instanceof JSONObject) {
                    //jsonObject
                    paramModel.pkValues = new HashMap<>();
                    jsonPkvalue = (JSONObject) objPkvalue;
                    Set pkValueKeys = jsonPkvalue.keySet();
                    for (Object pkValueKey : pkValueKeys) {
                        paramModel.pkValues.put(pkValueKey.toString(), jsonPkvalue.getString(pkValueKey.toString()));
                    }
                }
            }
            // select
            if (OperateTypeEnum.select == operateType) {
                paramModel.sql = UtileSmart.GetJsonString(jsonValues, "sql");
                paramModel.db_orderBy = UtileSmart.GetJsonString(jsonValues, "db_orderBy");
                paramModel.db_pageSize = UtileSmart.overrideParseShort(UtileSmart.GetJsonString(jsonNote, "db_pageSize"));
                paramModel.db_pageNum = UtileSmart.overrideParseInt(UtileSmart.GetJsonString(jsonNote, "db_pageNum"));
                paramModel.db_skipNum = UtileSmart.overrideParseInt(UtileSmart.GetJsonString(jsonNote, "db_skipNum"));
                paramModel.db_topNum = UtileSmart.overrideParseShort(UtileSmart.GetJsonString(jsonNote, "db_topNum"));
                if (jsonNote.containsKey("url_columns")) {
                    paramModel.db_RULcolumns = new HashSet<>();
                    url_columns = jsonNote.getJSONArray("url_columns");
                    for (int i = 0; i < url_columns.size(); i++) {
                        paramModel.db_RULcolumns.add(url_columns.getString(i));
                    }
                }
                if (jsonNote.containsKey("db_columns")) {
                    paramModel.db_columns = new HashSet<>();
                    db_columns = jsonNote.getJSONArray("db_columns");
                    for (int i = 0; i < db_columns.size(); i++) {
                        paramModel.db_columns.add(db_columns.getString(i));
                    }
                }
            }
            // insert or update
            if (OperateTypeEnum.insert == operateType || OperateTypeEnum.update == operateType) {
                paramModel.db_valueColumns = new HashMap<>();
                Iterator keys = jsonValues.keys();
                while (keys.hasNext()) {
                    strTemp = (String) keys.next();
                    paramModel.db_valueColumns.put(strTemp, jsonValues.getString(strTemp));
                }
                keys = null;
            }
            //pkvalue 为空，安装列值 update delete
            if (paramModel.pkValue == null && (OperateTypeEnum.delete == operateType || OperateTypeEnum.update == operateType)) {
                paramModel.db_valueFilter = new HashMap<>();
                Iterator noteIterator = jsonNote.keys();
                while (noteIterator.hasNext()) {
                    strTemp = (String) noteIterator.next();
                    paramModel.db_valueFilter.put(strTemp, jsonNote.getString(strTemp));
                }
                noteIterator = null;
            }
            return paramModel;
        } catch (Exception e) {
            common.RSLogger.ErrorLogInfo("transferReviveRSParamModel IAnalyzeReviceParamModel error.strParam:" + param + e.getLocalizedMessage());
            throw new Exception("transferReviveRSParamModel error." + e.getLocalizedMessage());
        } finally {
            common.UtileSmart.FreeObjects(jsonObj, jsonBody, jsonNote, jsonHead, jsonValues, url_columns, db_columns, strTemp, operateType);
        }
    }
    
    @Override
    public SignModel transferReviveRSSignModel(String param, OperateTypeEnum operateType) throws Exception {
        SignModel signModel = null;
        JSONObject jsonObj = null, jsonBody = null, jsonNote = null, jsonHead = null, jsonValues = null;
        JSONArray rsids = null;
        try {
            jsonObj = JSONObject.fromObject(param);
            signModel = new SignModel();
            if (operateType == OperateTypeEnum.signOff) {
                signModel.token = jsonObj.getJSONObject("head").getString("token");
                return signModel;
            }
            jsonHead = jsonObj.getJSONObject("head");
            jsonBody = jsonObj.getJSONObject("body");
            jsonNote = jsonBody.getJSONObject("note");
            jsonValues = jsonBody.getJSONObject("values");
            signModel.name = jsonNote.getString("rs_Name");
            signModel.pwd = jsonNote.getString("rs_Pwd");
            signModel.signOnRSID = new HashSet<>();
            rsids = jsonValues.getJSONArray("RSIDs");
            for (int i = 0; i < rsids.size(); i++) {
                signModel.signOnRSID.add(rsids.getString(i));
            }
            return signModel;
        } catch (Exception e) {
            common.RSLogger.ErrorLogInfo("transferReviveRSSignModel IAnalyzeReviceParamModel error.strParam:" + param + e.getLocalizedMessage());
            throw new Exception("transferReviveRSSignModel error." + e.getLocalizedMessage());
        } finally {
            common.UtileSmart.FreeObjects(jsonObj, jsonBody, jsonNote, jsonHead, jsonValues, operateType);
        }
    }

    /**
     * 验证json格式，和是否登录
     *
     * @param param
     * @param isMustSign
     * @return
     * @throws Exception
     */
    public JSONObject verificationParam(String param, boolean isMustSign) throws Exception {
        //todo  后续可以直接检查是否登录
        JSONObject jsonObj = null, jsonBody = null, jsonNote = null, jsonHead = null, jsonValues = null;
        JSONArray rsids = null;
        try {
            jsonObj = JSONObject.fromObject(param);
        } finally {
            common.UtileSmart.FreeObjects(jsonObj, jsonBody, jsonNote, jsonHead, jsonValues);
        }
        if (isMustSign) {
            
        }
        return jsonObj;
    }
    
}
