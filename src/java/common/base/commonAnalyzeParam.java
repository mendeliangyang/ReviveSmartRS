/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.base;

import common.model.ParamBaseModel;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Administrator
 */
public class commonAnalyzeParam {

    private String RSID = null;
    private String token = null;

    public String getRSID() {
        return RSID;
    }

    protected void setRSID(String RSID) {
        this.RSID = RSID;
    }

    public String getToken() {
        return token;
    }

    private void setToken(String token) {
        this.token = token;
    }

    public ParamBaseModel AnalyzeParamBase(String param) throws Exception {
        ParamBaseModel baseModel = null;
        JSONObject jsonObj = null;
        try {
            jsonObj = JSONObject.fromObject(param);
            baseModel = new ParamBaseModel();
            baseModel.jsonHead = jsonObj.getJSONObject("head");
            if (baseModel.jsonHead.containsKey(common.DeployInfo.paramRSIDKey)) {
                this.setRSID(baseModel.jsonHead.getString(common.DeployInfo.paramRSIDKey));
            }
            if (baseModel.jsonHead.containsKey(common.DeployInfo.paramtokenKey)) {
                this.setToken(baseModel.jsonHead.getString(common.DeployInfo.paramtokenKey));
            }
            baseModel.jsonBody = jsonObj.getJSONObject("body");
            if (baseModel.jsonBody.containsKey("note")) {
                baseModel.jsonNote = baseModel.jsonBody.getJSONObject("note");
            }
            if (baseModel.jsonBody.containsKey("values")) {
                baseModel.jsonValues = baseModel.jsonBody.getJSONObject("values");
            }
            return baseModel;
        } catch (Exception e) {
            common.RSLogger.ErrorLogInfo("AnalyzeParamBase IAnalyzeReviceParamModel error.strParam:" + param + e.getLocalizedMessage());
            throw new Exception("transferReviveRSSignModel error." + e.getLocalizedMessage());
        } finally {
            common.UtileSmart.FreeObjects(jsonObj);
        }
    }

    /**
     *
     * @param param
     * @param content head 头，body 消息内容， 消息内容中的note， values 消息内容中的values
     * @param paramModel
     * @throws Exception
     */
    public void AnalyzeParamToMap(String param, String content, Map<String, Object> paramModel) throws Exception {
        ParamBaseModel baseModel = null;
        JSONObject objSourceTemp = null, objValueTemp = null;
        try {
            baseModel = AnalyzeParamBase(param);
            switch (content) {
                case "head":
                    objSourceTemp = baseModel.jsonHead;
                    break;
                case "body":
                    objSourceTemp = baseModel.jsonBody;
                    break;
                case "note":
                    objSourceTemp = baseModel.jsonNote;
                    break;
                case "values":
                    objSourceTemp = baseModel.jsonValues;
                    break;
                default:
                    common.RSLogger.ErrorLogInfo("AnalyzeParamToMap error ,invalid content.:" + content + param);
                    throw new Exception("AnalyzeParamToMap error ,invalid content." + content);
            }
            if (objSourceTemp == null) {
                common.RSLogger.ErrorLogInfo("AnalyzeParamToMap error , content is null.:" + content + param);
                throw new Exception("AnalyzeParamToMap error , content is null." + content);
            }
            if (paramModel == null) {
                return;
            }
            for (String key : paramModel.keySet()) {
                if (objSourceTemp.containsKey(key)) {
                    Object objTemp = objSourceTemp.get(key);
                    if (objTemp instanceof String) {
                        paramModel.replace(key, objTemp);
                    } else if (objTemp instanceof JSONArray) {
                        paramModel.replace(key, JSONArray.fromObject(objTemp).toArray());
                    } else if (objTemp instanceof JSONObject) {
                        paramModel.replace(key, objTemp.toString());
                        //throw new Exception("not supper jsonobject.");
                    } else {
                        throw new Exception("param unknow type.");
                    }
                }
            }
        } catch (Exception e) {
            common.RSLogger.ErrorLogInfo("AnalyzeParamBase IAnalyzeReviceParamModel error.strParam:" + param + e.getLocalizedMessage());
            throw new Exception("AnalyzeParamBase error." + e.getLocalizedMessage());
        } finally {
            common.UtileSmart.FreeObjects(baseModel);
        }

    }

    public void AnalyzeParamHeadToMap(String param, Map<String, Object> paramModel) throws Exception {
        AnalyzeParamToMap(param, "head", paramModel);
    }

    public void AnalyzeParamBodyToMap(String param, Map<String, Object> paramModel) throws Exception {
        AnalyzeParamToMap(param, "body", paramModel);
    }

    public void AnalyzeParamNoteToMap(String param, Map<String, Object> paramModel) throws Exception {
        AnalyzeParamToMap(param, "note", paramModel);
    }

    public void AnalyzeParamValuesToMap(String param, Map<String, Object> paramModel) throws Exception {
        AnalyzeParamToMap(param, "values", paramModel);
    }

}
