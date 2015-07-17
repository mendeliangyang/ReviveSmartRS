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
import common.model.DepotFileDetailModel;
import common.model.ExecuteResultParam;
import common.model.FileDepotParamModel;
import common.model.ResponseResultCode;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

/**
 * REST Web Service
 *
 * @author Administrator
 */
@Path("FileDepot")
public class FileDepotRS {

    private FormationResult formationResult = new FormationResult();
    @Context
    private UriInfo context;

    /**
     * Creates a new instance of FileDepotRS
     */
    public FileDepotRS() {
    }

    @POST
    @Path("UpBase64File")
    public String UpBase64File(String param) {
        String strUpFileName = null, strSvcFileLocalName = null;
        StringBuffer sbFilePathTemp = new StringBuffer(), sbTemp = new StringBuffer();
        boolean bSvcFileExist;
        ExecuteResultParam resultParam = null;
        Set<String> setStrSqls = new HashSet<>();
        int saveFlag = 1;
         FileDepotParamModel paramModel=null;
        try {
            //解析参数
            paramModel = analyzeBase64Param(param);

            boolean isSignOn = common.VerificationSign.verificationSignOn(paramModel.token, paramModel.rsid);
            if (!isSignOn) {
                return formationResult.formationResult(ResponseResultCode.Error,new ExecuteResultParam("请您先登录系统。", param));
            }
            if (paramModel.fileDetaile == null || paramModel.fileDetaile.isEmpty()) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("base64串为空。", param));
            }

            for (DepotFileDetailModel fileDetaile : paramModel.fileDetaile) {
                //检查文件路径，和文件是否存在
                strUpFileName = fileDetaile.fileName;
                //组织路径  root/rsid/date(yymmddhh)/Type
                //第一级目录
                sbFilePathTemp.append(paramModel.rsid);
                sbTemp.append(DeployInfo.GetDeployFilePath()).append(File.separator).append(paramModel.rsid);
                FileHelper.CheckFileExist(sbTemp.toString());
                //二级目录
                sbFilePathTemp.append(File.separator).append(common.UtileSmart.getCurrentDate());
                sbTemp.append(File.separator).append(common.UtileSmart.getCurrentDate());
                FileHelper.CheckFileExist(sbTemp.toString());

                sbFilePathTemp.append(File.separator).append(fileDetaile.fileOwnType);
                sbTemp.append(File.separator).append(fileDetaile.fileOwnType);
                FileHelper.CheckFileExist(sbTemp.toString());
                //检查上次文件是否存在
                sbFilePathTemp.append(File.separator).append(strUpFileName).toString();
                strSvcFileLocalName = sbTemp.append(File.separator).append(strUpFileName).toString();
                bSvcFileExist = FileHelper.CheckFileExist(strSvcFileLocalName, false);
                if (bSvcFileExist) {
                    return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam( "文件已经存在，不能修改。请联系管理员维护附件系统。", param));
                }
                
                //判断数据库是否存在 ownid 和 fpath重复的数据，如果有数据重复不能上传文件
                resultParam = DBHelper.ExecuteSqlOnceSelect(DeployInfo.MasterRSID, String.format("SELECT COUNT(*) AS ROWSCOUNT FROM FILEDEPOT WHERE OWNID<>'%s' AND FPATH='%s'", paramModel.ownid, sbFilePathTemp.toString()));
                if (resultParam.ResultCode != 0) {
                    return formationResult.formationResult(ResponseResultCode.Error,new ExecuteResultParam( String.format("检查数据库文件信息发送错误。%s", resultParam.errMsg), param));
                }
                //检查ROWSCOUNT 不为0可以继续操作 ROWSCOUNT 不等于0表示有其他文件关联该文件，要求客户修改文件名称，或者联系管理员维护服务器文件
                if (resultParam.ResultJsonObject != null) {
                    if (Integer.parseInt(resultParam.ResultJsonObject.getJSONObject(DeployInfo.ResultDataTag).getString("ROWSCOUNT")) > 0) {
                        return formationResult.formationResult(ResponseResultCode.Error,new ExecuteResultParam( String.format("‘%s’,该文件名已经存在并于与其他业务数据关联，请修改文件名称重新提交，或者联系管理员维护附件服务器。", strUpFileName), param));
                    }
                }
                //判断如果类型应该是纯字符串，如果包含 文件路径分隔符(File.separator) 错误路径
                if (fileDetaile.fileOwnType.indexOf(File.separator)>0) {
                    return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(String.format("文件类型错误，类型中不应该包含文件分隔符", strUpFileName), paramModel.toStringInformation()) ); 
                }
                //调用解析图片方法，返回路径
                int baseIndex = fileDetaile.fileBase64Value.indexOf(";base64,");
                if (!FileHelper.ConvertBase64ToImage(fileDetaile.fileBase64Value.substring(baseIndex + 8, fileDetaile.fileBase64Value.length()), strSvcFileLocalName)) {
                    return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(String.format("%s: convert image failed", fileDetaile.fileName), param));
                }
                //本次文件保存成功，设置本地路径值，后续操作失败可以返回删除保存的文件
                fileDetaile.fileLocalPath = strSvcFileLocalName;

                //生成sql语句，待文件全部上传成功，保存到数据库
                setStrSqls.add(String.format(
                        "INSERT INTO FILEDEPOT (FID,FNAME,FPATH,FSUMMARY,OWNID,OWNFILETYPE) VALUES ('%s','%s','%s','%s','%s','%s')",
                        UUID.randomUUID().toString(), strUpFileName, sbFilePathTemp.toString(), "md5", paramModel.ownid, fileDetaile.fileOwnType));

                sbTemp.delete(0, sbTemp.length());
                sbFilePathTemp.delete(0, sbFilePathTemp.length());

            }
            //保存数据到数据库
            resultParam = DBHelper.ExecuteSql(DeployInfo.MasterRSID, setStrSqls);
            if (resultParam.ResultCode >= 0) {
                saveFlag = 0;
                //保存成功，将数据库信息返回
                resultParam = SelectDepotFileByOwn(new FileDepotParamModel(paramModel.ownid));
                return formationResult.formationResult(ResponseResultCode.Success, new  ExecuteResultParam(resultParam.ResultJsonObject));
            } else {
                //TODO 如果操作数据库失败，需要把之前上传的文件全部在服务器上删除，负责会影响下次上传
                return formationResult.formationResult(ResponseResultCode.Error,new ExecuteResultParam(resultParam.errMsg, param));
            }
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error,new ExecuteResultParam(e.getLocalizedMessage(), param,e));
        } finally {
            if (saveFlag == 1) {
                DeleteFile(paramModel.fileDetaile);
            }
            common.UtileSmart.FreeObjects(strUpFileName, strSvcFileLocalName, sbFilePathTemp, sbTemp, resultParam, setStrSqls,paramModel);
        }

    }

    public FileDepotParamModel analyzeBase64Param(String strJson) throws Exception {
        JSONObject jsonObj = null;
        JSONObject jsonHead = null;
        JSONObject jsonBody = null;
        JSONArray arrayBase64 = null;
        JSONObject jsonTempBase64 = null;
        FileDepotParamModel paramModel = null;
        DepotFileDetailModel tempDetailModel = null;
        try {
            jsonObj = JSONObject.fromObject(strJson);
            paramModel = new FileDepotParamModel();
            jsonHead = jsonObj.getJSONObject("head");
            jsonBody = jsonObj.getJSONObject("body");

            paramModel.rsid = jsonHead.getString(DeployInfo.paramRSIDKey);
            paramModel.token = jsonHead.getString(DeployInfo.paramtokenKey);
            paramModel.ownid = jsonBody.getString("ownid");
            arrayBase64 = jsonBody.getJSONArray("base64");
            paramModel.fileDetaile = new HashSet<>();
            for (Object arrayBase641 : arrayBase64) {
                jsonTempBase64 = ((JSONObject) arrayBase641);
                tempDetailModel = new DepotFileDetailModel();
                tempDetailModel.fileBase64Value = jsonTempBase64.getString("base64value");
                tempDetailModel.fileName = jsonTempBase64.getString("filename");
                tempDetailModel.fileOwnType = jsonTempBase64.getString("fileType");
                paramModel.fileDetaile .add(tempDetailModel);
            }
            return paramModel;
        } catch (Exception e) {
            throw new Exception(String.format("analyzeBase64Param error :%s", e.getLocalizedMessage()));
        } finally {
            common.UtileSmart.FreeObjects(jsonBody, jsonHead, jsonObj, jsonTempBase64, arrayBase64, tempDetailModel);
        }

    }

    /**
     * 上传文件到服务器
     *
     * @param pRSID
     * @param ptoken
     * @param pownid
     * @param formFileData
     * @param pfileType
     * @param pfilename
     * @return
     */
    @POST
    @Path("UpLoadFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String UpLoadFile(@FormDataParam("RSID") String pRSID, @FormDataParam("token") String ptoken, @FormDataParam("ownid") String pownid, @FormDataParam("filename") String pfilename, @FormDataParam("fileType") String pfileType, FormDataMultiPart formFileData) {

        FileDepotParamModel paramModel = new FileDepotParamModel();
        paramModel.ownid = pownid;
        paramModel.rsid = pRSID;
        paramModel.token = ptoken;
        paramModel.fileDetaile = new HashSet<>();
        DepotFileDetailModel detailModel = new DepotFileDetailModel();
        detailModel.fileName = pfilename;
        detailModel.fileOwnType = pfileType;
        paramModel.fileDetaile.add(detailModel);

        return SaveUpLoadFile(formFileData, paramModel, false);
    }

    /**
     * 上传文件到服务器
     *
     * @param strParam json字符串，文本参数
     * @param formFileData file 对象
     * @return
     */
    @POST
    @Path("ModifyDepotFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String ModifyDepotFile(@FormDataParam("param") String strParam, FormDataMultiPart formFileData) {
        return "developing";// SaveUpLoadFile(formFileData, strParam, true);
    }

    /**
     * 解析json
     *
     * @param strJson
     * @return
     * @throws Exception
     */
    public FileDepotParamModel analyzeUpLoadFileJsonStr(String strJson, boolean isModify) throws Exception {

        FileDepotParamModel paramModel = null;
        JSONObject jsonObj = null, jsonBody = null, jsonHead = null, jsonTempFile = null;
        JSONArray jsonFileDes = null;
        try {
            jsonObj = JSONObject.fromObject(strJson);
            paramModel = new FileDepotParamModel();

            jsonHead = jsonObj.getJSONObject("head");
            jsonBody = jsonObj.getJSONObject("body");
            paramModel.rsid = jsonHead.getString(DeployInfo.paramRSIDKey);
            paramModel.token = jsonHead.getString(DeployInfo.paramtokenKey);

            paramModel.ownid = jsonBody.getString("ownid");

            jsonFileDes = jsonBody.getJSONArray("fileDes");
            for (Object jsonFileDe : jsonFileDes) {
                jsonTempFile = (JSONObject) jsonFileDe;
                if (isModify) {
                    paramModel.addFileDetail(jsonTempFile.getString("filename"), jsonTempFile.getString("fileType"), jsonTempFile.getString("fileOverlay"), jsonTempFile.getString("fileId"));
                } else {
                    paramModel.addFileDetail(jsonTempFile.getString("filename"), jsonTempFile.getString("fileType"), jsonTempFile.getString("fileOverlay"));
                }
            }
            return paramModel;
        } catch (Exception e) {
            throw new Exception("analyze FileParamModel error.:" + e.getLocalizedMessage());
        } finally {
            common.UtileSmart.FreeObjects(paramModel, jsonObj, jsonBody, jsonHead, jsonTempFile, jsonFileDes);
        }
    }

    /**
     * 解析json
     *
     * @param strJson
     * @return
     * @throws Exception
     */
    public FileDepotParamModel analyzeNormalJsonStr(String strJson) throws Exception {

        FileDepotParamModel paramModel = null;
        JSONObject jsonObj = null, jsonBody = null, jsonHead = null, jsonTempFile = null;
        JSONArray jsonFileDes = null;
        DepotFileDetailModel fileDetailModel = null;
        try {
            jsonObj = JSONObject.fromObject(strJson);
            paramModel = new FileDepotParamModel();

            jsonHead = jsonObj.getJSONObject("head");
            jsonBody = jsonObj.getJSONObject("body");
            paramModel.rsid = jsonHead.getString(DeployInfo.paramRSIDKey);
            paramModel.token = jsonHead.getString(DeployInfo.paramtokenKey);

            paramModel.ownid = jsonBody.getString("ownid");
            if (jsonBody.containsKey("fileIds")) {
                jsonFileDes = jsonBody.getJSONArray("fileIds");
                for (int i = 0; i < jsonFileDes.size(); i++) {
                    paramModel.addFileDetail(jsonFileDes.getString(i));
                }
                paramModel.selectFlag = 1;
            } else if (jsonBody.containsKey("fileTypes")) {
                jsonFileDes = jsonBody.getJSONArray("fileTypes");
                for (int i = 0; i < jsonFileDes.size(); i++) {
                    fileDetailModel = new DepotFileDetailModel();
                    fileDetailModel.fileOwnType = jsonFileDes.getString(i);
                    paramModel.fileDetaile.add(fileDetailModel);
                }
                paramModel.selectFlag = 2;
            }

            return paramModel;
        } catch (Exception e) {
            throw new Exception("analyze FileParamModel error.:" + e.getLocalizedMessage());
        } finally {
            common.UtileSmart.FreeObjects(paramModel, jsonObj, jsonBody, jsonHead, jsonTempFile, jsonFileDes, fileDetailModel);
        }
    }

    /**
     * 保存上传文件到服务器
     *
     * @param formFileData
     * @param strJson
     * @param isModify 是否提交
     * @return
     */
    private String SaveUpLoadFile(FormDataMultiPart formFileData, FileDepotParamModel paramModel, boolean isModify) {

        InputStream isTempFile = null;
        FormDataContentDisposition detailTempFile = null;
        String strSvcFileLocalName = null, strUpFileName = null, strTempFilePath = null;
        MediaType mtTempFile = null;
        StringBuffer sbTemp = new StringBuffer();
        StringBuffer sbFilePathTemp = new StringBuffer();
        boolean bSvcFileExist = false;
        Set<String> setStrSqls = new HashSet<>();
        ExecuteResultParam resultParam = null;
        DepotFileDetailModel tempFileDetailModel = null;
        int saveFlag = 1;
        if (paramModel == null) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("解析参数发生错误", "check : form-data"));
        }

        boolean isSignOn = common.VerificationSign.verificationSignOn(paramModel.token, paramModel.rsid);
        if (!isSignOn) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("请您先登录系统", paramModel.toStringInformation()));
        }
        try {
            List<FormDataBodyPart> listFile = formFileData.getFields("file");
            for (FormDataBodyPart tempFile : listFile) {
                isTempFile = tempFile.getValueAs(InputStream.class);
                detailTempFile = tempFile.getFormDataContentDisposition();
                mtTempFile = tempFile.getMediaType();
                strUpFileName = detailTempFile.getFileName();
                //组织路径  root/rsid/date(yymmddhh)/Type
                //第一级目录
                sbFilePathTemp.append(paramModel.rsid);
                sbTemp.append(DeployInfo.GetDeployFilePath()).append(File.separator).append(paramModel.rsid);
                FileHelper.CheckFileExist(sbTemp.toString());
                //二级目录
                sbFilePathTemp.append(File.separator).append(common.UtileSmart.getCurrentDate());
                sbTemp.append(File.separator).append(common.UtileSmart.getCurrentDate());
                FileHelper.CheckFileExist(sbTemp.toString());
                tempFileDetailModel = paramModel.getFileDetailModel(strUpFileName);
                if (tempFileDetailModel == null) {
                    return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(String.format("获取文件‘ %s’的详细参数失败。", strUpFileName), paramModel.toStringInformation()) );
                }
                //判断如果类型应该是纯字符串，如果包含 文件路径分隔符(File.separator) 错误路径
                if (tempFileDetailModel.fileOwnType.indexOf(File.separator)>0) {
                    return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(String.format("文件类型错误，类型中不应该包含文件分隔符", strUpFileName), paramModel.toStringInformation()) ); 
                }
                sbFilePathTemp.append(File.separator).append(tempFileDetailModel.fileOwnType);
                sbTemp.append(File.separator).append(tempFileDetailModel.fileOwnType);
                FileHelper.CheckFileExist(sbTemp.toString());
                //检查上次文件是否存在
                sbFilePathTemp.append(File.separator).append(strUpFileName).toString();
                strSvcFileLocalName = sbTemp.append(File.separator).append(strUpFileName).toString();
                bSvcFileExist = FileHelper.CheckFileExist(strSvcFileLocalName, false);
                if (bSvcFileExist && isModify == false) {
                    return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(String.format("文件已经存在，不能修改。请联系管理员维护附件系统。%s", strUpFileName), paramModel.toStringInformation()));
                }
                //判断数据库是否存在 ownid 和 fpath重复的数据，如果有数据重复不能上传文件
                resultParam = DBHelper.ExecuteSqlOnceSelect(DeployInfo.MasterRSID, String.format("SELECT COUNT(*) AS ROWSCOUNT FROM FILEDEPOT WHERE OWNID<>'%s' AND FPATH='%s'", paramModel.ownid, sbFilePathTemp.toString()));
                if (resultParam.ResultCode != 0) {
                    return formationResult.formationResult(ResponseResultCode.Error,new ExecuteResultParam(String.format("检查数据库文件信息发送错误。%s : Msg : %s",strUpFileName ,resultParam.errMsg), paramModel.toStringInformation()) );
                }
                //检查ROWSCOUNT 不为0可以继续操作 ROWSCOUNT 不等于0表示有其他文件关联该文件，要求客户修改文件名称，或者联系管理员维护服务器文件
                if (resultParam.ResultJsonObject != null) {
                    if (Integer.parseInt(resultParam.ResultJsonObject.getJSONObject(DeployInfo.ResultDataTag).getString("ROWSCOUNT")) > 0) {
                        return formationResult.formationResult(ResponseResultCode.Error,new ExecuteResultParam(String.format("‘%s’,该文件名已经存在并于与其他业务数据关联，请修改文件名称重新提交，或者联系管理员维护附件服务器。", strUpFileName), paramModel.toStringInformation()) );
                    }
                }

                try (OutputStream fileOutputStream = new FileOutputStream(
                        strSvcFileLocalName)) {
                    int read = 0;
                    final byte[] bytes = new byte[1024];
                    while ((read = isTempFile.read(bytes)) != -1) {
                        fileOutputStream.write(bytes, 0, read);
                    }
                    fileOutputStream.close();
                    //本次文件保存成功，设置本地路径值，后续操作失败可以返回删除保存的文件
                    tempFileDetailModel.fileLocalPath = strSvcFileLocalName;
                    //生成sql语句，待文件全部上传成功，保存到数据库
                    if (isModify) {
                        //todo  修改文件 下面sql不可用
                        // 1,保存新文件到服务器，
                        // 2，根据 uuid 查询到以前的数据，删除服务器以前的文件
                        //3，根据 uuid 更新最新的数据
                        setStrSqls.add(String.format(
                                "INSERT INTO FILEDEPOT (FID,FNAME,FPATH,FSUMMARY,OWNID,OWNFILETYPE) VALUES ('%s','%s','%s','%s','%s','%s')",
                                UUID.randomUUID().toString(), strUpFileName, sbFilePathTemp.toString(), "md5", paramModel.ownid, tempFileDetailModel.fileOwnType));
                    } else {
                        setStrSqls.add(String.format(
                                "INSERT INTO FILEDEPOT (FID,FNAME,FPATH,FSUMMARY,OWNID,OWNFILETYPE) VALUES ('%s','%s','%s','%s','%s','%s')",
                                UUID.randomUUID().toString(), strUpFileName, sbFilePathTemp.toString(), "md5", paramModel.ownid, tempFileDetailModel.fileOwnType));
                    }
                } catch (IOException e) {
                    return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(), paramModel.toStringInformation(),e) );
                }
                sbTemp.delete(0, sbTemp.length());
                sbFilePathTemp.delete(0, sbFilePathTemp.length());
            }
            //保存数据到数据库
            resultParam = DBHelper.ExecuteSql(DeployInfo.MasterRSID, setStrSqls);
            if (resultParam.ResultCode >= 0) {
                saveFlag = 0;
                //保存成功，将数据库信息返回
                resultParam = SelectDepotFileByOwn(new FileDepotParamModel(paramModel.ownid));
                return formationResult.formationResult(ResponseResultCode.Success,new ExecuteResultParam( resultParam.ResultJsonObject));
            } else {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam( String.format("保存数据失败：%s", resultParam.errMsg), paramModel.toStringInformation()));
            }
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error,new ExecuteResultParam( e.getLocalizedMessage(), paramModel.toStringInformation(),e));
        } finally {
            if (saveFlag == 1) {
                DeleteFile(paramModel.fileDetaile);
            }
            common.UtileSmart.FreeObjects(isTempFile, detailTempFile,
                    strSvcFileLocalName, strUpFileName, mtTempFile, strTempFilePath,
                    sbTemp, sbFilePathTemp, setStrSqls, resultParam, paramModel, tempFileDetailModel);
        }

    }

    /**
     * 删除指定文件
     *
     * @param fileDetailModels
     */
    private void DeleteFile(Set<DepotFileDetailModel> fileDetailModels) {
        if (fileDetailModels != null && !fileDetailModels.isEmpty()) {
            for (DepotFileDetailModel fileDetailModel : fileDetailModels) {
                if (fileDetailModel.fileLocalPath != null && !fileDetailModel.fileLocalPath.isEmpty()) {
                    FileHelper.deleteFile(fileDetailModel.fileLocalPath);
                }
            }
        }

    }

    /**
     * 根据own删除own的所有文件
     *
     * @param strParam
     * @return
     */
    @POST
    @Path("DeleteDepotFileByOwn")
    public String DeleteDepotFileByOwn(String strParam) {
        //sbTemp.append(DeployInfo.GetDeployFilePath()).append(sbFilePathTemp);
        return null;
    }

    /**
     * 根据own作废 own下面的文件修改数据库状态
     *
     * @param strParam
     * @return
     */
    @POST
    @Path("InvalidDepotFileByOwn")
    public String InvalidDepotFileByOwn(String strParam) {
        //sbTemp.append(DeployInfo.GetDeployFilePath()).append(sbFilePathTemp);
        FileDepotParamModel paramModel = null;
        ExecuteResultParam resultModel = null;
        try {
            paramModel = analyzeNormalJsonStr(strParam);

            boolean isSignOn = common.VerificationSign.verificationSignOn(paramModel.token, paramModel.rsid);
            if (!isSignOn) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("请您先登录系统。", strParam));
            }

            resultModel = InvalidDepotFile(paramModel);
            if (resultModel.ResultCode >= 0) {
                return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam( resultModel.ResultJsonObject) );
            } else {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam( resultModel.errMsg,strParam));
            }
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam( e.getLocalizedMessage(),strParam,e));
        }
    }

    private ExecuteResultParam InvalidDepotFile(FileDepotParamModel paramModel) throws Exception {
        StringBuffer sbSql = new StringBuffer();
        StringBuffer sbSqlDelete = new StringBuffer();
        List<String> lsSql = null;
        try {
            sbSql.append(String.format("insert into FILEDEPOT_LS (FID,FNAME,FPATH,FSUMMARY,OWNID,OWNFILETYPE,UPLOADDATE) select  FID,FNAME,FPATH,FSUMMARY,OWNID,OWNFILETYPE,UPLOADDATE from FILEDEPOT  as t_f where t_f.OWNID='%s' ", paramModel.ownid));
            sbSqlDelete.append(String.format("delete FILEDEPOT  where OWNID='%s' ", paramModel.ownid));
            if (paramModel.fileDetaile != null && paramModel.fileDetaile.size() > 0) {
                if (paramModel.selectFlag == 1) {
                    sbSql.append(" and t_f.FID in (");
                    sbSqlDelete.append(" and FID in (");
                } else if (paramModel.selectFlag == 2) {
                    sbSql.append(" and t_f.OWNFILETYPE in ( ");
                    sbSqlDelete.append(" and OWNFILETYPE in ( ");
                } else {
                    return new ExecuteResultParam(-1, "解析json参数失败，文件描述不为空，但是标记值未修改。");
                }
                for (DepotFileDetailModel paramModelTemp : paramModel.fileDetaile) {
                    if (paramModel.selectFlag == 1) {
                        sbSql.append("'").append(paramModelTemp.fileId).append("'");
                        sbSqlDelete.append("'").append(paramModelTemp.fileId).append("'");
                    } else if (paramModel.selectFlag == 2) {
                        sbSql.append("'").append(paramModelTemp.fileOwnType).append("'");
                        sbSqlDelete.append("'").append(paramModelTemp.fileOwnType).append("'");
                    } else {
                        return new ExecuteResultParam(-1, "解析json参数失败，文件描述不为空，但是标记值未修改。");
                    }
                    sbSql.append(',');
                    sbSqlDelete.append(",");
                }
                sbSql.deleteCharAt(sbSql.length() - 1);
                sbSql.append(")");

                sbSqlDelete.deleteCharAt(sbSqlDelete.length() - 1);
                sbSqlDelete.append(")");
            }
            lsSql = new ArrayList<>();

            lsSql.add(sbSql.toString());
            lsSql.add(sbSqlDelete.toString());
            return DBHelper.ExecuteSql(DeployInfo.MasterRSID, lsSql);
        } catch (Exception e) {
            throw new Exception(e.getLocalizedMessage());
        } finally {
            common.UtileSmart.FreeObjects(paramModel, sbSql, sbSqlDelete);
        }
    }

    /**
     * 根据own查询own对应的所有
     *
     * @param strParam
     * @return
     */
    @POST
    @Path("SelectDepotFileByOwn")
    public String SelectDepotFileByOwn(String strParam) {
        FileDepotParamModel paramModel = null;
        ExecuteResultParam resultModel = null;
        try {
            paramModel = analyzeNormalJsonStr(strParam);

            boolean isSignOn = common.VerificationSign.verificationSignOn(paramModel.token, paramModel.rsid);
            if (!isSignOn) {
                return formationResult.formationResult(ResponseResultCode.Error,  new ExecuteResultParam("请您先登录系统。", strParam));
            }

            resultModel = SelectDepotFileByOwn(paramModel);
            if (resultModel.ResultCode >= 0) {
                return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam(resultModel.ResultJsonObject));
            } else {
                return formationResult.formationResult(ResponseResultCode.Error,new ExecuteResultParam( resultModel.errMsg,strParam));
            }
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(e.getLocalizedMessage(),strParam,e));
        }
    }

    private ExecuteResultParam SelectDepotFileByOwn(FileDepotParamModel paramModel) throws Exception {
        List<String> urlColumns = null;
        StringBuffer sbSql = new StringBuffer();
        try {
            sbSql.append(String.format("select * from FILEDEPOT where OWNID='%s' ", paramModel.ownid));

            if (paramModel.fileDetaile != null && paramModel.fileDetaile.size() > 0) {
                if (paramModel.selectFlag == 1) {
                    sbSql.append(" and FID in (");
                } else if (paramModel.selectFlag == 2) {
                    sbSql.append(" and OWNFILETYPE in (");
                } else {
                    return new ExecuteResultParam(-1, "解析json参数失败，文件描述不为空，但是标记值未修改。");
                }
                for (DepotFileDetailModel paramModelTemp : paramModel.fileDetaile) {
                    if (paramModel.selectFlag == 1) {
                        sbSql.append("'").append(paramModelTemp.fileId).append("'");
                    } else if (paramModel.selectFlag == 2) {
                        sbSql.append("'").append(paramModelTemp.fileOwnType).append("'");
                    } else {
                        return new ExecuteResultParam(-1, "解析json参数失败，文件描述不为空，但是标记值未修改。");
                    }
                    sbSql.append(',');
                }
                sbSql.deleteCharAt(sbSql.length() - 1);
                sbSql.append(")");
            }
            urlColumns = new ArrayList<>();
            urlColumns.add("FPATH");
            return DBHelper.ExecuteSqlSelect(DeployInfo.MasterRSID, sbSql.toString(), urlColumns);
        } catch (Exception e) {
            throw new Exception(e.getLocalizedMessage());
        } finally {
            common.UtileSmart.FreeObjects(paramModel, sbSql, urlColumns);
        }
    }
}
