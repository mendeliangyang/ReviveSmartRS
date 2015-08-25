/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsSvc.ManageSystem;

import common.DBHelper;
import common.DeployInfo;
import common.FormationResult;
import common.UtileSmart;
import common.model.ExecuteResultParam;
import common.model.ResponseResultCode;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import rsSvc.SignVerify.SignInformationModel;

/**
 * REST Web Service
 *
 * @author Administrator
 */
@Path("mamageSystem")
public class MamageSystemResource {

    MamageSystemAnalyzeParam mamageSysAnalyze = new MamageSystemAnalyzeParam();
    private final FormationResult formationResult = new FormationResult();
    @Context
    private UriInfo context;

    /**
     * Creates a new instance of MamageSystemResource
     */
    public MamageSystemResource() {
    }

    @POST
    @Path("DeleteOrg")
    public String DeleteOrg(String param) {
        String paramKey_orgNum = "orgNum";
        ExecuteResultParam resultParam = null;
        String sqlStr = null, selectResultStr = null;
        Map<String, Object> paramMap = null;
        try {
            paramMap = new HashMap<String, Object>();

            paramMap.put(paramKey_orgNum, null);

            mamageSysAnalyze.AnalyzeParamBodyToMap(param, paramMap);
            // verify token
            rsSvc.SignVerify.SignCommon.verifySign(mamageSysAnalyze.getToken(), true);

            sqlStr = String.format("SELECT count(*) as orgUpNumCount FROM organization where orgUpNum='%s'",
                    UtileSmart.getStringFromMap(paramMap, paramKey_orgNum));

            selectResultStr = DBHelper.ExecuteSqlSelectOne(mamageSysAnalyze.getRSID(), sqlStr);
            if (selectResultStr == null) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("该机构不存在", param));
                //new ExecuteResultParam("该问题已经处理，不能再进行操作", param)
            } else if (Integer.parseInt(selectResultStr) != 0) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("该机构下存在子机构不能删除", param));
            }
            sqlStr = String.format("SELECT count(*) as deviceCount FROM device where deviceOrgNo='%s'",
                    UtileSmart.getStringFromMap(paramMap, paramKey_orgNum));

            selectResultStr = DBHelper.ExecuteSqlSelectOne(mamageSysAnalyze.getRSID(), sqlStr);
            if (selectResultStr != null && Integer.parseInt(selectResultStr) != 0) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("该机构下存设备不能删除", param));
            }
            sqlStr = String.format("SELECT count(*) userCount FROM userInfo where userOrgNum ='%s'",
                    UtileSmart.getStringFromMap(paramMap, paramKey_orgNum));

            selectResultStr = DBHelper.ExecuteSqlSelectOne(mamageSysAnalyze.getRSID(), sqlStr);
            if (selectResultStr != null && Integer.parseInt(selectResultStr) != 0) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("该机构下存用户不能删除", param));
            }

            sqlStr = String.format("delete organization where orgNum ='%s'",
                    UtileSmart.getStringFromMap(paramMap, paramKey_orgNum));
            resultParam = DBHelper.ExecuteSql(mamageSysAnalyze.getRSID(), sqlStr);
            if (resultParam.ResultCode >= 0) {
                return formationResult.formationResult(ResponseResultCode.Success, resultParam);
            } else {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(resultParam.errMsg, param));
            }
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), param, e));
        } finally {
            paramMap.clear();
            UtileSmart.FreeObjects(resultParam, param, sqlStr, paramMap);
        }
    }

    @POST
    @Path("InvalidDevice")
    public String InvalidDevice(String param) {
        String paramKey_id = "id";
        ExecuteResultParam resultParam = null;
        String sqlStr = null, selectResultStr = null;
        Map<String, Object> paramMap = null;
        try {
            paramMap = new HashMap<String, Object>();

            paramMap.put(paramKey_id, null);

            mamageSysAnalyze.AnalyzeParamBodyToMap(param, paramMap);
            // verify token
            rsSvc.SignVerify.SignCommon.verifySign(mamageSysAnalyze.getToken(), true);

            //SELECT count(*) as orgUpNumCount FROM organization where orgUpNum='%s'
            sqlStr = String.format("select deviceUser from device where id='%s'",
                    UtileSmart.getStringFromMap(paramMap, paramKey_id));

            selectResultStr = DBHelper.ExecuteSqlSelectOne(mamageSysAnalyze.getRSID(), sqlStr);
            if (selectResultStr != null) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("该设备存在使用者请，不能删除。", param));
                //new ExecuteResultParam("该问题已经处理，不能再进行操作", param)
            }

            sqlStr = String.format("update device set deviceValid='0' where id='%s'",
                    UtileSmart.getStringFromMap(paramMap, paramKey_id));
            resultParam = DBHelper.ExecuteSql(mamageSysAnalyze.getRSID(), sqlStr);
            if (resultParam.ResultCode >= 0) {
                return formationResult.formationResult(ResponseResultCode.Success, resultParam);
            } else {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(resultParam.errMsg, param));
            }
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), param, e));
        } finally {
            paramMap.clear();
            UtileSmart.FreeObjects(resultParam, param, sqlStr, paramMap);
        }
    }

    @POST
    @Path("SignInMM")
    public String SignInMM(String param) {
        String paramKey_userIdcd = "userIdcd";
        String paramKey_userPwd = "userPwd";
        String paramKey_deviceMac = "deviceMac";

        ExecuteResultParam resultParam = null;
        String sqlStr = null;
        Map<String, Object> paramMap = null;
        Map<String, Map<String, String>> resultMap = null;
        try {
            paramMap = new HashMap<String, Object>();

            paramMap.put(paramKey_userIdcd, null);
            paramMap.put(paramKey_userPwd, null);
            paramMap.put(paramKey_deviceMac, null);

            mamageSysAnalyze.AnalyzeParamBodyToMap(param, paramMap);

            //SELECT count(*) as orgUpNumCount FROM organization where orgUpNum='%s'
            sqlStr = String.format("SELECT u.id,u.userAddress,u.userEmail,u.userIdcd,u.userMobilePhone,u.userName,u.userNum,u.userOrgNum,o.orgName as orgName,"
                    + "o.orgLevel,u.userRole,u.userSex,u.userTelephone ,r.pow_id,r.name as rouleName ,p.name powerName,p.id as powerId FROM userInfo u "
                    + "left join organization o on u.userOrgNum=o.orgNum  left join roleInfo r on u.userRole=r.id left join power p on r.pow_id=p.id "
                    + "where u.userIdcd='%s' and u.userPwd='%s' and u.id=(select d.deviceUser from device d where d.deviceMac='%s')",
                    UtileSmart.getStringFromMap(paramMap, paramKey_userIdcd), UtileSmart.getStringFromMap(paramMap, paramKey_userPwd), UtileSmart.getStringFromMap(paramMap, paramKey_deviceMac));

            resultMap = DBHelper.ExecuteSqlSelectReturnMap(mamageSysAnalyze.getRSID(), sqlStr, "userInfo");
            if (resultMap != null && resultMap.size() == 1) {
                SignInformationModel signModel = rsSvc.SignVerify.SignCommon.SignIn(resultMap.keySet().iterator().next(), null, null);
                return formationResult.formationResult(ResponseResultCode.Success, signModel.token, new ExecuteResultParam(resultMap, false));
            } else {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("输入用户名或密码错误或设备mac值不匹配.", param));
            }
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), param, e));
        } finally {
            paramMap.clear();
            UtileSmart.cleanMapTDString(resultMap);
            UtileSmart.FreeObjects(resultParam, param, sqlStr, paramMap);
        }
    }

    @POST
    @Path("SignInM")
    public String SignInM(String param) {
        String paramKey_userIdcd = "userIdcd";
        String paramKey_userPwd = "userPwd";

        ExecuteResultParam resultParam = null;
        String sqlStr = null;
        Map<String, Object> paramMap = null;
        Map<String, Map<String, String>> resultMap = null;
        try {
            paramMap = new HashMap<String, Object>();

            paramMap.put(paramKey_userIdcd, null);
            paramMap.put(paramKey_userPwd, null);

            mamageSysAnalyze.AnalyzeParamBodyToMap(param, paramMap);

            //SELECT count(*) as orgUpNumCount FROM organization where orgUpNum='%s'
            sqlStr = String.format("SELECT u.id,u.userAddress,u.userEmail,u.userIdcd,u.userMobilePhone,u.userName,u.userNum,u.userOrgNum,o.orgName as orgName,o.orgLevel,u.userRole,u.userSex,u.userTelephone ,r.pow_id,r.name as rouleName ,p.name powerName,p.id as powerId FROM userInfo u left join organization o on u.userOrgNum=o.orgNum  left join roleInfo r on u.userRole=r.id left join power p on r.pow_id=p.id where u.userIdcd='%s' and u.userPwd='%s'",
                    UtileSmart.getStringFromMap(paramMap, paramKey_userIdcd), UtileSmart.getStringFromMap(paramMap, paramKey_userPwd));

            resultMap = DBHelper.ExecuteSqlSelectReturnMap(mamageSysAnalyze.getRSID(), sqlStr, "userInfo");
            if (resultMap != null && resultMap.size() == 1) {
                SignInformationModel signModel = rsSvc.SignVerify.SignCommon.SignIn(resultMap.keySet().iterator().next(), null, null);
                return formationResult.formationResult(ResponseResultCode.Success, signModel.token, new ExecuteResultParam(resultMap, false));
            } else {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("输入用户名或密码错误.", param));
            }
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), param, e));
        } finally {
            paramMap.clear();
            UtileSmart.cleanMapTDString(resultMap);
            UtileSmart.FreeObjects(resultParam, param, sqlStr, paramMap);
        }
    }

    @POST
    @Path("SignOutM")
    public String SignOutM(String param) {
        try {
            rsSvc.SignVerify.SignCommon.SignOut("");
            return formationResult.formationResult(ResponseResultCode.Success);
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), param, e));
        } finally {
        }
    }

    @POST
    @Path("SelectAllOrg")
    public String SelectAllOrg(String param) {
        try {
            JSONArray jsonArray = null;
            //SELECT * FROM organization where orgLevel=1
            mamageSysAnalyze.AnalyzeParamBodyToMap(param, null);
            // verify token
            rsSvc.SignVerify.SignCommon.verifySign(mamageSysAnalyze.getToken(), true);
            //分级递归查询
            //jsonArray = SearchOrgRoot(mamageSysAnalyze.getRSID());
            //一次性查询，本地递归组装数据
            jsonArray = SelectRootOrg(mamageSysAnalyze.getRSID());
            JSONObject jsonObj = new JSONObject();
            jsonObj.accumulate(DeployInfo.ResultDataTag, jsonArray);
            return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam(jsonObj));
        } catch (Exception ex) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(ex.getLocalizedMessage(), param, ex));
        }
    }

    private JSONArray SearchOrgRoot(String rsid) throws Exception {
        Map<String, Map<String, String>> map = null;
        try {
            map = DBHelper
                    .ExecuteSqlSelectReturnMap(rsid, "SELECT o.orgName [text],o.* FROM organization o  where orgLevel='1'", "organization");
            JSONArray jsonArray = new JSONArray();
            for (Map<String, String> value : map.values()) {
                JSONObject rootObj = JSONObject.fromObject(value);
                SearchOrgDown(value.get("orgNum"), rsid, rootObj);
                jsonArray.add(rootObj);
            }
            return jsonArray;
        } catch (Exception ex) {
            throw new Exception("SearchOrgRoot error" + ex.getLocalizedMessage());
        } finally {
            UtileSmart.cleanMapTDString(map);
        }

    }

    private void SearchOrgDown(String upOrgNum, String rsid, JSONObject obj) throws Exception {
        Map<String, Map<String, String>> downMap = null;
        try {
            downMap = DBHelper
                    .ExecuteSqlSelectReturnMap(rsid, String.format("SELECT o.orgName [text],o.* FROM organization o  where orgUpNum='%s'", upOrgNum), "organization");

            if (downMap != null && downMap.size() > 0) {
                for (Map<String, String> value : downMap.values()) {
                    JSONArray downArray = new JSONArray();
                    JSONObject downObj = JSONObject.fromObject(value);
                    SearchOrgDown(value.get("orgNum"), rsid, downObj);
                    downArray.add(downObj);
                    obj.accumulate("children", downArray);
                }
            }

        } catch (Exception ex) {
            common.RSLogger.ErrorLogInfo("SearchOrgDown error" + ex.getLocalizedMessage() + "upOrgNum:" + upOrgNum, ex);
            throw new Exception("SearchOrgDown error" + ex.getLocalizedMessage() + "upOrgNum:" + upOrgNum);
        } finally {

            UtileSmart.cleanMapTDString(downMap);
        }
    }
//

    public JSONArray SelectRootOrg(String rsid) throws Exception {
        Map<String, Map<String, String>> map = null;
        try {
            map = DBHelper
                    .ExecuteSqlSelectReturnMap(rsid, String.format("SELECT o.orgName [text], o.* FROM organization o "), "organization");
            JSONArray jsonArray = new JSONArray();
            for (Map<String, String> value : map.values()) {
                if (value.get("orgNum") == null || value.get("orgUpNum") == null) {
                    continue;
                }
                if (value.get("orgNum").equals(value.get("orgUpNum"))) {
                    JSONObject obj = JSONObject.fromObject(value);
                    findDownOrg(map, obj, value.get("orgNum"));
                    jsonArray.add(obj);
                }
            }
            return jsonArray;
        } finally {
            UtileSmart.cleanMapTDString(map);
        }

    }

    void findDownOrg(Map<String, Map<String, String>> map, JSONObject obj, String upOrgNum) {
        for (Map<String, String> value : map.values()) {
            if (value.get("orgNum") == null || value.get("orgUpNum") == null) {
                continue;
            }
            if (value.get("orgUpNum").equals(upOrgNum) && !value.get("orgNum").equals(value.get("orgUpNum"))) {
                JSONArray downArray = null;
                JSONObject objDown = JSONObject.fromObject(value);
                findDownOrg(map, objDown, value.get("orgNum"));
                if (obj.containsKey("children")) {
                    downArray = obj.getJSONArray("children");
                }
                if (downArray == null) {
                    downArray = new JSONArray();
                }
                downArray.add(objDown);
                obj.remove("children");
                obj.accumulate("children", downArray);
            }
        }
    }

}
