/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsSvc.advisory;

import common.DBHelper;
import common.DeployInfo;
import common.FormationResult;
import common.UtileSmart;
import common.model.ExecuteResultParam;
import common.model.ResponseResultCode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * REST Web Service
 *
 * @author Administrator
 */
@Path("advisoryBus")
public class AdvisoryBusResource {

    @Context
    private UriInfo context;

    AdvisoryAnalyzeParam advisoryAnalyzeParam = new AdvisoryAnalyzeParam();
    private FormationResult formationResult = new FormationResult();

    /**
     * Creates a new instance of AdvisoryBusResource
     */
    public AdvisoryBusResource() {
    }

    @POST
    @Path("NormalSignUp")
    public String NormalSignUp(String param) {
        String paramKey_uNickName = "uNickName",
                paramKey_uCompany = "uCompany",
                paramKey_uCompanyNO = "uCompanyNO",
                paramKey_uPhone = "uPhone",
                paramKey_uPwd = "uPwd",
                paramKey_vcode = "vcode";
        ExecuteResultParam resultParam = null;
        String sqlStr = null, uId = null;
        Map<String, Object> paramMap = null;
        try {
            //get uName 姓名  uCompany 公司名称  uCompanyNO 公司编号 uPhone 手机号码  vcode 验证码  uPwd 登录密码
            paramMap = new HashMap<String, Object>();

            paramMap.put(paramKey_uNickName, null);
            paramMap.put(paramKey_uCompany, null);
            paramMap.put(paramKey_uCompanyNO, null);
            paramMap.put(paramKey_uPhone, null);
            paramMap.put(paramKey_uPwd, null);
            paramMap.put(paramKey_vcode, null);

            advisoryAnalyzeParam.AnalyzeParamBodyToMap(param, paramMap);

            uId = common.UtileSmart.getUUID();
            sqlStr = String.format("insert into AdUser (uId,uNickName,uPwd,uType,uPhone,uCompany,uCompanyNO) values('%s','%s','%s',%d,'%s','%s','%s')",
                    uId,
                    UtileSmart.getStringFromMap(paramMap, paramKey_uNickName),
                    UtileSmart.getStringFromMap(paramMap, paramKey_uPwd),
                    1,
                    UtileSmart.getStringFromMap(paramMap, paramKey_uPhone),
                    UtileSmart.getStringFromMap(paramMap, paramKey_uCompany),
                    UtileSmart.getStringFromMap(paramMap, paramKey_uCompanyNO));

            resultParam = DBHelper.ExecuteSql(advisoryAnalyzeParam.getRSID(), sqlStr);

            if (resultParam.ResultCode >= 0) {
                //JMSQueueMessage.AsyncWriteMessage(paramModel.db_tableName, 2, paramModel.pkValues);

                JSONObject resultJson = new JSONObject();
                resultJson.accumulate("uId", uId);
                if (resultParam.ResultJsonObject == null) {
                    resultParam.ResultJsonObject = new JSONObject();
                }
                resultParam.ResultJsonObject.accumulate(DeployInfo.ResultDataTag, resultJson);

                return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam(resultParam.ResultJsonObject));
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
    @Path("SignIn")
    public String SignIn(String param) {

        String paramKey_uName = "uName",
                paramKey_uPwd = "uPwd";

        ExecuteResultParam resultParam = null;
        String sqlStr = null, selectReultStr = null;
        Map<String, Object> paramMap = null;

        List<String> sqlList = null;

        try {
            //get uName 姓名  uPwd 登录密码
            paramMap = new HashMap<String, Object>();

            paramMap.put(paramKey_uName, null);
            paramMap.put(paramKey_uPwd, null);

            advisoryAnalyzeParam.AnalyzeParamBodyToMap(param, paramMap);

            sqlStr = String.format("select uId from AdUser where uPhone='%s' and uPwd='%s'",
                    UtileSmart.getStringFromMap(paramMap, paramKey_uName),
                    UtileSmart.getStringFromMap(paramMap, paramKey_uPwd));

            selectReultStr = DBHelper.ExecuteSqlSelectOne(advisoryAnalyzeParam.getRSID(), sqlStr);
            //获取查询到数据
            if (selectReultStr == null) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("您输入的用户名或密码错误。", param));
            }

            //删除登录信息，插入新的登录记录
            sqlList = new ArrayList<String>();
            sqlList.add(String.format("delete UserSignRecord  where uId='%s'", selectReultStr));
            sqlList.add(String.format("insert into UserSignRecord (uId,signTime) values ('%s',getdate())", selectReultStr));
            resultParam = DBHelper.ExecuteSql(advisoryAnalyzeParam.getRSID(), sqlList);

            if (resultParam.ResultCode >= 0) {
                //return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam());
                JSONObject resultJson = new JSONObject();
                resultJson.accumulate("uId", selectReultStr);
                if (resultParam.ResultJsonObject == null) {
                    resultParam.ResultJsonObject = new JSONObject();
                }
                if (resultParam.ResultJsonObject == null) {
                    resultParam.ResultJsonObject = new JSONObject();
                }
                resultParam.ResultJsonObject.accumulate(DeployInfo.ResultDataTag, resultJson);
                return formationResult.formationResult(ResponseResultCode.Success, selectReultStr, new ExecuteResultParam(resultParam.ResultJsonObject));
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
    @Path("SignOut")
    public String SignOut(String param) {

        String paramKey_uId = "uId";

        ExecuteResultParam resultParam = null;
        Map<String, Object> paramMap = null;

        try {
            //get uName 姓名  uPwd 登录密码
            paramMap = new HashMap<String, Object>();

            paramMap.put(paramKey_uId, null);

            advisoryAnalyzeParam.AnalyzeParamBodyToMap(param, paramMap);

            resultParam = DBHelper.ExecuteSql(advisoryAnalyzeParam.getRSID(), String.format("delete UserSignRecord  where uId='%s'", UtileSmart.getStringFromMap(paramMap, paramKey_uId)));

            if (resultParam.ResultCode >= 0) {
                return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam());
            } else {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(resultParam.errMsg, param));
            }
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), param, e));
        } finally {
            paramMap.clear();
            UtileSmart.FreeObjects(resultParam, param, paramMap);
        }
    }

    @POST
    @Path("subMitQ")
    public String subMitQ(String param) {
        String paramKey_uId = "uId", paramKey_qTitle = "qTitle", paramKey_specialSolveUId = "specialSolveUId";
        ExecuteResultParam resultParam = null;
        String sqlStr = null, qId = null;
        Map<String, Object> paramMap = null;
        try {
            //get uName 姓名  qTitle 问题，即问题标题
            paramMap = new HashMap<String, Object>();

            paramMap.put(paramKey_uId, null);
            paramMap.put(paramKey_qTitle, null);
            paramMap.put(paramKey_specialSolveUId, null);

            advisoryAnalyzeParam.AnalyzeParamBodyToMap(param, paramMap);
            qId = common.UtileSmart.getUUID();
            sqlStr = String.format("insert into question (qId,qTitle,qPutDate,uId,specialSolveUId) values ('%s','%s',getdate(),'%s','%s')",
                    qId,
                    UtileSmart.getStringFromMap(paramMap, paramKey_qTitle),
                    UtileSmart.getStringFromMap(paramMap, paramKey_uId), UtileSmart.tryGetStringFromMap(paramMap, paramKey_specialSolveUId));
            resultParam = DBHelper.ExecuteSql(advisoryAnalyzeParam.getRSID(), sqlStr);

            if (resultParam.ResultCode >= 0) {
                JSONObject resultJson = new JSONObject();
                resultJson.accumulate("qId", qId);
                if (resultParam.ResultJsonObject == null) {
                    resultParam.ResultJsonObject = new JSONObject();
                }
                resultParam.ResultJsonObject.accumulate(DeployInfo.ResultDataTag, resultJson);

                return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam(resultParam.ResultJsonObject));
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
    @Path("processQ")
    public String processQ(String param) {

        String paramKey_qId = "qId", paramKey_hUId = "hUId";
        ExecuteResultParam resultParam = null;
        String sqlStr = null, selectResultStr = null;
        Map<String, Object> paramMap = null;
        List<String> sqlList = null;
        try {
            //get uName 姓名  qTitle 问题，即问题标题
            paramMap = new HashMap<String, Object>();

            paramMap.put(paramKey_qId, null);
            paramMap.put(paramKey_hUId, null);

            advisoryAnalyzeParam.AnalyzeParamBodyToMap(param, paramMap);

            //1，判断问题是否已经处理，如果处理不能再进行操作
            sqlStr = String.format("select qSolveFlag  from question  where qId='%s'",
                    UtileSmart.getStringFromMap(paramMap, paramKey_qId));

            selectResultStr = DBHelper.ExecuteSqlSelectOne(advisoryAnalyzeParam.getRSID(), sqlStr);
            sqlList = new ArrayList<String>();
            //2，判断响应是否是否为空如果不为空，   获取当前时间，并更新
            if (selectResultStr == null) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("该问题不存在", param));
                //new ExecuteResultParam("该问题已经处理，不能再进行操作", param)
            } else if (selectResultStr.equals("3")) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("该问题已处理", param));
            } else if (selectResultStr.equals("1")) {
                sqlList.add(String.format("update question set qAnswerDate=getdate() ,qSolveFlag=3 where  qId='%s'", UtileSmart.getStringFromMap(paramMap, paramKey_qId)));
            }
            //3，插入handler 表，当前处理人
            sqlList.add(String.format("insert into Handler (qId,hUId,hPutDate,hSolve) values('%s','%s',getdate(),1)",
                    UtileSmart.getStringFromMap(paramMap, paramKey_qId),
                    UtileSmart.getStringFromMap(paramMap, paramKey_hUId)));

            resultParam = DBHelper.ExecuteSql(advisoryAnalyzeParam.getRSID(), sqlList);

            if (resultParam.ResultCode >= 0) {
                return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam());
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
    @Path("solveQ")
    public String solveQ(String param) {

        String paramKey_qId = "qId", paramKey_hUId = "hUId";
        ExecuteResultParam resultParam = null;
        String sqlStr = null, selectResultStr = null;
        Map<String, Object> paramMap = null;
        List<String> sqlList = null;
        try {
            //get uName 姓名  qTitle 问题，即问题标题
            paramMap = new HashMap<String, Object>();

            paramMap.put(paramKey_qId, null);
            paramMap.put(paramKey_hUId, null);

            advisoryAnalyzeParam.AnalyzeParamBodyToMap(param, paramMap);

            //1，判断问题是否已经处理，如果处理不能再进行操作
            sqlStr = String.format("select qSolveFlag as unsovle from question  where qId='%s'",
                    UtileSmart.getStringFromMap(paramMap, paramKey_qId));

            selectResultStr = DBHelper.ExecuteSqlSelectOne(advisoryAnalyzeParam.getRSID(), sqlStr);
            if (selectResultStr == null || selectResultStr.equals("2")) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("该问题已经处理，不能再进行操作", param));
            }
            sqlList = new ArrayList<String>();
            //更新question标记处理完成
            sqlList.add(String.format("update question set qSolveFlag=2,qSolveDate=getdate() where  qId='%s'", UtileSmart.getStringFromMap(paramMap, paramKey_qId)));
            //更新 handler 标记solve 处理人
            sqlList.add(String.format("update Handler set hSolve=2 , hQuitDate=getdate() where qId='%s' and hUId='%s'", UtileSmart.getStringFromMap(paramMap, paramKey_qId), UtileSmart.getStringFromMap(paramMap, paramKey_hUId)));

            resultParam = DBHelper.ExecuteSql(advisoryAnalyzeParam.getRSID(), sqlList);

            if (resultParam.ResultCode >= 0) {
                return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam());
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
    @Path("getVerifyCode")
    public String getVerifyCode(String param) {

        String paramKey_uPhone = "uPhone";
        ExecuteResultParam resultParam = null;
        String sqlStr = null, selectResultStr = null;
        Map<String, Object> paramMap = null;
        try {
            //get uName 姓名  qTitle 问题，即问题标题
            paramMap = new HashMap<String, Object>();
            paramMap.put(paramKey_uPhone, null);
            advisoryAnalyzeParam.AnalyzeParamBodyToMap(param, paramMap);

            UtileSmart.getStringFromMap(paramMap, paramKey_uPhone);

            //根据uName 获取用户手机号码，发送验证短信到手机上，
//            sqlStr = String.format("select uName from AdUser where uPhone='%s'",
//                    UtileSmart.getStringFromMap(paramMap, "uPhone"));
//
//            selectResultStr = DBHelper.ExecuteSqlSelectOne(advisoryAnalyzeParam.getRSID(), sqlStr);
//            if (selectResultStr == null) {
//                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("您输入的用户账号有误", param));
//            } else {
//
//                //发送短信到该手机
//            }
            resultParam = new ExecuteResultParam();
            JSONObject resultJson = new JSONObject();
            resultJson.accumulate("vcode", "0000");
            if (resultParam.ResultJsonObject == null) {
                resultParam.ResultJsonObject = new JSONObject();
            }
            resultParam.ResultJsonObject.accumulate(DeployInfo.ResultDataTag, resultJson);

            return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam(resultParam.ResultJsonObject));
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), param, e));
        } finally {
            paramMap.clear();
            UtileSmart.FreeObjects(resultParam, param, sqlStr, paramMap);
        }
    }

    @POST
    @Path("modifyPwd")
    public String modifyPwd(String param) {

        String paramKey_uName = "uName", paramKey_uPwd = "uPwd", paramKey_vcode = "vcode";

        ExecuteResultParam resultParam = null;
        String sqlStr = null, selectResultStr = null;
        Map<String, Object> paramMap = null;
        try {
            //get uName 姓名  qTitle 问题，即问题标题
            paramMap = new HashMap<String, Object>();
            paramMap.put(paramKey_uName, null);
            paramMap.put(paramKey_uPwd, null);
            paramMap.put(paramKey_vcode, null);

            advisoryAnalyzeParam.AnalyzeParamBodyToMap(param, paramMap);

            //根据uName 获取用户手机号码，发送验证短信到手机上，
            sqlStr = String.format("select uId from AdUser where uPhone='%s'",
                    UtileSmart.getStringFromMap(paramMap, paramKey_uName));

            selectResultStr = DBHelper.ExecuteSqlSelectOne(advisoryAnalyzeParam.getRSID(), sqlStr);
            if (selectResultStr == null) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("您输入的用户账号有误", param));
            } else {
                //调用  验证接口
                sqlStr = String.format("update AdUser set uPwd = '%s' where uId='%s'", UtileSmart.getStringFromMap(paramMap, paramKey_uPwd), selectResultStr);
                resultParam = DBHelper.ExecuteSql(advisoryAnalyzeParam.getRSID(), sqlStr);
                if (resultParam.ResultCode >= 0) {
                    return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam());
                } else {
                    return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(resultParam.errMsg, param));
                }
            }
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), param, e));
        } finally {
            paramMap.clear();
            UtileSmart.FreeObjects(resultParam, param, sqlStr, paramMap);
        }
    }

    @POST
    @Path("solveQList")
    public String solveQList(String param) {

        String paramKey_hUId = "hUId", paramKey_hSolve = "hSolve", paramKey_skipNum = "skipNum", paramKey_topNum = "topNum";

        ExecuteResultParam resultParam = null, resultParam1 = null;
        String sqlStr = null, tempWhere = null;
        int skipNum = 0, topNum = 0;
        Map<String, Object> paramMap = null;
        try {
            //get uName 姓名  qTitle 问题，即问题标题
            paramMap = new HashMap<String, Object>();

            paramMap.put(paramKey_hUId, null);
            //hSolve	1未解决，2解决。0查询全部
            paramMap.put(paramKey_hSolve, null);
            paramMap.put(paramKey_skipNum, null);
            paramMap.put(paramKey_topNum, null);

            advisoryAnalyzeParam.AnalyzeParamBodyToMap(param, paramMap);
            switch (UtileSmart.getStringFromMap(paramMap, paramKey_hSolve)) {
                case "1":
                    tempWhere = "and hSolve=1";
                    break;
                case "2":
                    tempWhere = "and hSolve=2";
                    break;
                default:
                    tempWhere = "";
                    break;
            }
            skipNum = Integer.parseInt(UtileSmart.getStringFromMap(paramMap, paramKey_skipNum));
            topNum = Integer.parseInt(UtileSmart.getStringFromMap(paramMap, paramKey_topNum)) + skipNum;
            //SELECT qTitle ,qPutDate ,hSolve,qId sybid=identity(12) into #temp_question FROM question select * from #temp_question where sybid> %d and sybid <= %d
            //+ "select qTitle,qPutDate  from question where qId in (select qId from Handler where hUname='%s' %s )"
            sqlStr = String.format("SELECT qTitle ,qPutDate ,qSolveFlag,qId, sybid=identity(12) into #temp_question FROM question where qId in (select qId from Handler where hUId='%s' %s ) select * from #temp_question where sybid> %d and sybid <= %d",
                    UtileSmart.getStringFromMap(paramMap, paramKey_hUId), tempWhere, skipNum, topNum);

            resultParam = DBHelper.ExecuteSqlSelect(advisoryAnalyzeParam.getRSID(), sqlStr);

            resultParam1 = DBHelper.ExecuteSqlSelect(advisoryAnalyzeParam.getRSID(), String.format("select count(*) as rowsCount from question where  qId in (select qId from Handler where hUId='%s' %s )",
                    UtileSmart.getStringFromMap(paramMap, paramKey_hUId), tempWhere));

            if (resultParam.ResultCode >= 0) {
                if (resultParam1 != null && resultParam1.ResultCode >= 0) {
                    JSONArray rowsCountJson = resultParam1.ResultJsonObject.getJSONArray(DeployInfo.ResultDataTag);
                    Iterator iterator = rowsCountJson.iterator();
                    JSONObject rowsCount = (JSONObject) iterator.next();
                    resultParam.ResultJsonObject.accumulate("rowsCount", rowsCount.getString("rowsCount"));
                }
                return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam(resultParam.ResultJsonObject));
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
    @Path("solveCount")
    public String solveCount(String param) {
        String paramKey_hUId = "hUId", paramKey_hSolve = "hSolve";
        ExecuteResultParam resultParam = null;
        String sqlStr = null, tempWhere = null;
        Map<String, Object> paramMap = null;
        try {
            //get uName 姓名  qTitle 问题，即问题标题
            paramMap = new HashMap<String, Object>();

            paramMap.put("hUId", null);
            //hSolve	1未解决，2解决。0查询全部
            paramMap.put("hSolve", null);

            advisoryAnalyzeParam.AnalyzeParamBodyToMap(param, paramMap);
            switch (UtileSmart.getStringFromMap(paramMap, paramKey_hSolve)) {
                case "1":
                    tempWhere = "and hSolve=1";
                    break;
                case "2":
                    tempWhere = "and hSolve=2";
                    break;
                default:
                    tempWhere = "";
                    break;
            }
            sqlStr = String.format("select count(*) as sovleCount from Handler where hUId = '%s' %s ",
                    UtileSmart.getStringFromMap(paramMap, paramKey_hUId), tempWhere);
            resultParam = DBHelper.ExecuteSqlSelect(advisoryAnalyzeParam.getRSID(), sqlStr);
            if (resultParam.ResultCode >= 0) {
                return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam(resultParam.ResultJsonObject));
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

}
