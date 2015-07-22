/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsSvc;

import common.DBHelper;
import common.DeployInfo;
import common.FileHelper;
import common.FormationResult;
import common.model.ExecuteResultParam;
import common.RSLogger;
import common.model.ResponseResultCode;
import common.UtileSmart;
import common.model.OperateTypeEnum;
import common.model.ReviveRSParamModel;
import common.model.SqlFactoryResultModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.util.UUID;
import jms.JMSQueueMessage;
import org.apache.commons.codec.binary.Base64;

/**
 * REST Web Service
 *
 * @author Administrator
 */
@Path("RS")
public class ReviveRS {

//    @Context
//    private UriInfo context;
    private IAnalyzeReviceParamModel analyzeParamModel = new AnalyzeReviceParamModel();

    private FormationResult formationResult = new FormationResult();

    /**
     * Creates a new instance of ReviveRS
     */
    public ReviveRS() {
    }

    @POST
    @Path("SayHello")
    public String SayHello() throws Exception {
        //DBHelper.ExecuteSql("microCredit", "update abc set a=1");
        // DBHelper.GetTableInfosByDataBase("E_Bank");
//        String sqlStr = DBHelper.SqlSelectPageFactory("{\"head\": {\"RSID\":\"AustraliaBank\"},\n"
//                + "                \"body\":\n"
//                + "                {\n"
//                + "                    \"note\":{\"db_tableName\":\"BankCard\",\"db_skipNum\":\"2\",\"db_topNum\":\"4\",\"db_columns\":[\"Uidcardpics\",\"UidCard\",\"CcardStatus\",\"UhomeAdd\"]},\n"
//                + "                    \"values\":{\"sql\":\"1=1\"}\n"
//                + "                }}");
//        return DeployInfo.DoGetDelplyRootPath() + "   log :" + DeployInfo.DeployLogPath + File.separator + "RSlog.log" + "     FileDepot: " + DeployInfo.DeployFilePath + File.separator;
//DBHelper.SearchTableIdentityKey("BankCard", "AustraliaBank");
        RSLogger.ErrorLogInfo("测试写错误日志");
        RSLogger.LogInfo("测试normal日志写入");
        RSLogger.SetUpLogInfo("测试setUp日志写入");
        RSLogger.wsErrorLogInfo("websocketerror write.");
        try {
            // DBHelper.initializePool();
        } catch (Exception e) {
        }
        return "only test "; //NetHelper.test();
    }

    @POST
    @Path("UpLoadFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String UpLoadFile(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDisposition, @FormDataParam("param") String paramStr) {
        try {
            String newFileName = UUID.randomUUID().toString();
            String FileExtension = "";
            String fileName = fileDisposition.getFileName();
            if (fileName.indexOf(".") > 1) {
                FileExtension = FileHelper.getExtensionName(fileName);
                newFileName = newFileName + "." + FileExtension;
            }
            RSLogger.LogInfo("***** fileName " + fileDisposition.getFileName());
            RSLogger.LogInfo("***** NewName " + newFileName);

            String filePath = DeployInfo.GetDeployFilePath() + File.separator + newFileName;

            RSLogger.LogInfo("***** filePath " + filePath);

            try (OutputStream fileOutputStream = new FileOutputStream(filePath)) {
                int read = 0;
                final byte[] bytes = new byte[1024];
                while ((read = fileInputStream.read(bytes)) != -1) {
                    fileOutputStream.write(bytes, 0, read);
                }
                fileOutputStream.close();
                RSLogger.LogInfo("File Upload Successfully");
//        String HttpPath = "http://192.168.169.217:6060/ReviveSmartRS/FileDepot/" + newFileName;
                return HandlerFileToDatabase(paramStr, newFileName, filePath);
            } catch (IOException e) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), paramStr, e));
            }
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), paramStr, e));
        }

    }

    private String HandlerFileToDatabase(String paramStr, String newFileName, String filePath) {
        //String HttpPath = DeployInfo.DeployHttpFilePath + newFileName;
        String HttpPath = newFileName;
        if (paramStr == null || paramStr.isEmpty()) {
            JSONObject body = new JSONObject();
            body.accumulate("HttpPath", HttpPath);
            body.accumulate("FilePath", filePath);
            return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam(body));
        } else {
            //根据传入参数进行数据更改
            JSONObject jsonObj = JSONObject.fromObject(paramStr);
            JSONObject jsonHead = jsonObj.getJSONObject("head");
            String handleStr = jsonHead.getString("handle");

            switch (handleStr.toUpperCase()) {
                case "INSERT":
                case "INSERTMODEL":
                    return InsertModel(ConstructFileColumnparam(paramStr, HttpPath));
                case "UPDATE":
                case "UPDATEMODEL":
                    return UpdateModel(ConstructFileColumnparam(paramStr, HttpPath));
                default:
                    return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("paramError", paramStr));
            }
        }
    }

    private String HandlerBase64FileToDatabase(String paramStr) {

        //根据传入参数进行数据更改
        JSONObject jsonObj = JSONObject.fromObject(paramStr);
        JSONObject jsonHead = jsonObj.getJSONObject("head");
        String handleStr = jsonHead.getString("handle");

        switch (handleStr.toUpperCase()) {
            case "INSERT":
            case "INSERTMODEL":
                return InsertModel(paramStr);
            case "UPDATE":
            case "UPDATEMODEL":
                return UpdateModel(paramStr);
            default:
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("paramError", paramStr));
        }

    }

    @POST
    @Path("UpBase64File")
    public String UpBase64File(String param) {
        try {
            //加载json
            JSONObject jsonObj = JSONObject.fromObject(param);
            JSONObject jsonBase64s = jsonObj.getJSONObject("body").getJSONObject("base64");
            JSONObject jsonValues = jsonObj.getJSONObject("body").getJSONObject("values");
            String tempFilePath = null, newFileName = null, filePath = null;
            Iterator base64Iterator = jsonBase64s.keys();
            while (base64Iterator.hasNext()) {
                String key = (String) base64Iterator.next();
                String tempBase64 = jsonBase64s.getString(key);
                //构造文件名称和全路径
                String suffix = ".jpg";//data:image/jpeg;base64,
                if (tempBase64.indexOf("data:image/bmp;base64,") >= 0) {
                    suffix = ".png";
                }
                newFileName = UUID.randomUUID().toString() + suffix;
                filePath = DeployInfo.GetDeployFilePath() + File.separator + newFileName;
                tempFilePath = newFileName;
                //tempFilePath = DeployInfo.DeployHttpFilePath + newFileName;
                //调用解析图片方法，返回路径
                //tempBase64
                int baseIndex = tempBase64.indexOf(";base64,");
                if (!FileHelper.ConvertBase64ToImage(tempBase64.substring(baseIndex + 8, tempBase64.length()), filePath)) {
                    return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(key + ": convert image failed", param));
                }
                //更改values 中的列值
                if (jsonValues != null && jsonValues.containsKey(key)) {
                    jsonValues.remove(key);
                }
                jsonValues.accumulate(key, DeployInfo.StringLinkMark + tempFilePath);
            }

            return HandlerBase64FileToDatabase(jsonObj.toString());
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), param, e));
        }

    }


    /**
     *
     * @return
     */
    private String ConstructFileColumnparam(String sourceParam, String newColumnValue) {
        JSONObject jsonObj = JSONObject.fromObject(sourceParam);
        JSONObject jsonBody = jsonObj.getJSONObject("body");
        String columnName = jsonBody.getJSONObject("note").getString("fileColumn");
        JSONObject jsonValues = jsonBody.getJSONObject("values");
        if (columnName == null || columnName.isEmpty()) {
            return sourceParam;
        }
        if (jsonValues != null && jsonValues.containsKey(columnName)) {
            jsonValues.remove(columnName);
        }
        jsonValues.accumulate(columnName, DeployInfo.StringLinkMark + newColumnValue);
        return jsonObj.toString();
    }

    /**
     * 获取不带扩展名的文件名
     *
     * @param filename
     * @return
     */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    /**
     * @param param
     * @return
     * @POST @Path("SelectModel") public String SelectModel(String param) {
     * Connection conn = null; Statement stmt = null; JSONObject table = null;
     * try { JSONObject jsonObj = JSONObject.fromObject(param); String RSID =
     * JSONObject.fromObject(param).getJSONObject("head").getString("RSID");
     * JSONObject jsonBody = jsonObj.getJSONObject("body"); JSONObject jsonNote
     * = jsonBody.getJSONObject("note"); String tableName =
     * jsonNote.getString(DeployInfo.paramTableName); String sqlStr =
     * common.DBHelper.SqlSelectFactory(param); RSLogger.LogInfo("sql: " +
     * sqlStr);
     *
     * conn = DBHelper.ConnectSybase(RSID);
     *
     * stmt = conn.createStatement(); ResultSet result =
     * stmt.executeQuery(sqlStr.toString()); ResultSetMetaData rsmd =
     * result.getMetaData(); int columnCount = rsmd.getColumnCount(); table =
     * new JSONObject(); JSONArray rows = new JSONArray(); JSONObject row =
     * null; //分页控制器 int dataIndex = 0, pageSize = 0, pageNum = 0, pageOffset =
     * 0, topNum = 0; if (jsonNote.containsKey("db_pageSize") &&
     * jsonNote.containsKey("db_pageNum")) { pageSize =
     * jsonNote.getInt("db_pageSize"); pageNum = jsonNote.getInt("db_pageNum");
     * pageOffset = (pageNum - 1) * pageSize; topNum = pageOffset + pageSize; }
     * else if (jsonNote.containsKey("db_skipNum") &&
     * jsonNote.containsKey("db_topNum")) { pageOffset =
     * jsonNote.getInt("db_skipNum"); topNum = jsonNote.getInt("db_topNum"); }
     * while (result.next()) { if ((pageNum <= 0 && topNum == 0) || (dataIndex
     * >= pageOffset && dataIndex < topNum)) { row = new JSONObject(); for (int
     * j = 1; j <= columnCount; j++) { row.accumulate(rsmd.getColumnName(j),
     * result.getString(j)); } rows.add(row); } dataIndex++; }
     * table.accumulate(DeployInfo.ResultDataTag, rows);
     * table.accumulate("rowCount", dataIndex); } catch (SQLException e) { //
     * Auto-generated catch block e.printStackTrace();
     * RSLogger.ErrorLogInfo(e.getMessage()); return
     * formationResult(ResponseResultCode.Error, e.getMessage(), null); }
     * finally { DBHelper.CloseConnection(stmt, conn); }
     *
     * return formationResult(ResponseResultCode.Success, "", table); }
     */
    @POST
    @Path("SelectModel")
    public String SelectModel(String param) {
        ExecuteResultParam resultParam = null, resultParam1 = null;
        ReviveRSParamModel paramModel = null;
        String sqlStr = null, sqlStr1 = null;

        try {
            paramModel = analyzeParamModel.transferReviveRSParamModel(param, OperateTypeEnum.select);

            boolean isSignOn = common.VerificationSign.verificationSignOn(paramModel.token, paramModel.rsid);
            if (!isSignOn) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("请您先登录系统。", param));
            }

            //判断是否有分页
            if ((paramModel.db_pageNum != -1 && paramModel.db_pageSize != -1) || (paramModel.db_skipNum != -1 && paramModel.db_topNum != -1)) {
                //调用分页的sql语句构造
                sqlStr = DBHelper.SqlSelectPageFactory(paramModel);
                sqlStr1 = DBHelper.SqlSelectCountFactory(paramModel);
            } else {
                sqlStr = DBHelper.SqlSelectFactory(paramModel);
            }
            //执行sql查询
            resultParam = DBHelper.ExecuteSqlSelect(paramModel.rsid, sqlStr);
            if (sqlStr1 != null) {
                resultParam1 = DBHelper.ExecuteSqlSelect(paramModel.rsid, sqlStr1);
            }
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
            if (paramModel != null) {
                paramModel.destroySelf();
            }
            UtileSmart.FreeObjects(resultParam, resultParam1, paramModel, param, sqlStr, sqlStr1);
        }
    }

    @POST
    @Path("SelectModelCount")
    public String SelectModelCount(String param) {
        ExecuteResultParam resultParam = null;
        String sqlStr = null;
        ReviveRSParamModel paramModel = null;
        try {
            paramModel = analyzeParamModel.transferReviveRSParamModel(param, OperateTypeEnum.select);

            boolean isSignOn = common.VerificationSign.verificationSignOn(paramModel.token, paramModel.rsid);
            if (!isSignOn) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("请您先登录系统。", param));
            }

            sqlStr = DBHelper.SqlSelectCountFactory(paramModel);
            //执行sql查询
            resultParam = DBHelper.ExecuteSqlSelect(paramModel.rsid, sqlStr);
            if (resultParam.ResultCode >= 0) {
                return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam(resultParam.ResultJsonObject));
            } else {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(resultParam.errMsg, param));
            }
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), param, e));
        } finally {
            if (paramModel != null) {
                paramModel.destroySelf();
            }
            UtileSmart.FreeObjects(resultParam, sqlStr, param, paramModel);
        }
    }

    @POST
    @Path("UpdateModel")
    public String UpdateModel(String param) {
        ExecuteResultParam resultParam = null;
        ReviveRSParamModel paramModel = null;
        try {
            paramModel = analyzeParamModel.transferReviveRSParamModel(param, OperateTypeEnum.update);

            boolean isSignOn = common.VerificationSign.verificationSignOn(paramModel.token, paramModel.rsid);
            if (!isSignOn) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("请您先登录系统。", param));
            }

            resultParam = DBHelper.ExecuteSql(paramModel.rsid, DBHelper.SqlUpdateFactory(paramModel));

            if (resultParam.ResultCode >= 0) {
                JMSQueueMessage.AsyncWriteMessage(paramModel.db_tableName, 2, paramModel.pkValues);
                return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam());
            } else {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(resultParam.errMsg, param));
            }
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), param, e));
        } finally {
            if (paramModel != null) {
                paramModel.destroySelf();
            }
            UtileSmart.FreeObjects(resultParam, paramModel, param);
        }

    }

    @POST
    @Path("DeleteModel")
    public String DeleteModel(String param) {
        ExecuteResultParam resultParam = null;
        ReviveRSParamModel paramModel = null;
        try {
            paramModel = analyzeParamModel.transferReviveRSParamModel(param, OperateTypeEnum.delete);

            boolean isSignOn = common.VerificationSign.verificationSignOn(paramModel.token, paramModel.rsid);
            if (!isSignOn) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("请您先登录系统。", param));
            }
            resultParam = DBHelper.ExecuteSql(paramModel.rsid, DBHelper.SqlDeleteFactory(paramModel));
            if (resultParam.ResultCode >= 0) {
                return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam());
            } else {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(resultParam.errMsg, param));
            }
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), param, e));
        } finally {
            if (paramModel != null) {
                paramModel.destroySelf();
            }
            UtileSmart.FreeObjects(resultParam, param, paramModel);
        }
    }

    @POST
    @Path("InsertModel")
    public String InsertModel(String param) {
        ExecuteResultParam resultParam = null;
        ReviveRSParamModel paramModel = null;
        SqlFactoryResultModel sqlResultModel = null;
        try {
            paramModel = analyzeParamModel.transferReviveRSParamModel(param, OperateTypeEnum.insert);

            boolean isSignOn = common.VerificationSign.verificationSignOn(paramModel.token, paramModel.rsid);
            if (!isSignOn) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("请您先登录系统。", param));
            }
            sqlResultModel = DBHelper.SqlInsertFactory(paramModel);
            //如果有identity 开始的sql语句以 SET NOCOUNT  ON 开始 执行查询方法
            if (sqlResultModel.strSql.startsWith("SET NOCOUNT ON")) {
                resultParam = DBHelper.ExecuteSqlOnceSelect(paramModel.rsid, sqlResultModel.strSql);
            } else {
                resultParam = DBHelper.ExecuteSql(paramModel.rsid, sqlResultModel.strSql);
            }

            if (resultParam.ResultCode >= 0) {
                //notify data changed
                JMSQueueMessage.AsyncWriteMessage(paramModel.db_tableName, 1, paramModel.pkValues);
                if (sqlResultModel.columnValue != null && !sqlResultModel.columnValue.isEmpty()) {
                    JSONObject resultJson = new JSONObject();
                    for (String keySet : sqlResultModel.columnValue.keySet()) {
                        resultJson.accumulate(keySet, sqlResultModel.columnValue.get(keySet));
                    }
                    if (resultParam.ResultJsonObject == null) {
                        resultParam.ResultJsonObject = new JSONObject();
                    }
                    resultParam.ResultJsonObject.accumulate(DeployInfo.ResultDataTag, resultJson);
                }
                return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam(resultParam.ResultJsonObject));
            } else {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(resultParam.errMsg, param));
            }
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), param, e));
        } finally {
            if (paramModel != null) {
                paramModel.destroySelf();
            }
            UtileSmart.FreeObjects(resultParam, param, paramModel, sqlResultModel);
        }
    }

    @POST
    @Path("SetUpTableInfo")
    public String SetUpTableInfo(String param) {
        ExecuteResultParam resultParam = null;
        try {
            resultParam = DBHelper.GetTableInfosByDataBase(JSONObject.fromObject(param).getJSONObject("head").getString("RSID"));
            if (resultParam.ResultCode >= 0) {
                return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam());
            } else {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(resultParam.errMsg, param));
            }
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), param, e));
        } finally {
            UtileSmart.FreeObjects(resultParam, param);
        }

    }

//    public String formationResult(ResponseResultCode resultCode, String errMsg, Object... result) {
//
//        JSONObject resultJson = new JSONObject();
//        JSONObject resultHeadContext = new JSONObject();
//        resultHeadContext.accumulate("resultCode", resultCode.toString());
//        resultHeadContext.accumulate("errMsg", errMsg);
//        resultJson.accumulate("head", resultHeadContext);
//        if (result != null) {
//            for (Object result1 : result) {
//                //Object result1 = result[i];
//                resultJson.accumulate("body", result1);
//            }
//        }
//        //resultJson.accumulate("body", result);
//        return resultJson.toString();
//    }
}
