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
import common.RSLogger;
import common.comInterface.IFormationResult;
import common.model.DepotFileDetailModel;
import common.model.ExecuteResultParam;
import common.model.FileDepotParamModel;
import common.model.ResponseResultCode;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    private IFormationResult formationResult = new FormationResult();
    @Context
    private UriInfo context;

    /**
     * Creates a new instance of FileDepotRS
     */
    public FileDepotRS() {
    }

    /**
     * 上传文件到服务器
     *
     * @param strParam json字符串，文本参数
     * @param formFileData file 对象
     * @return
     */
    @POST
    @Path("UpLoadFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String UpLoadFile(@FormDataParam("param") String strParam, FormDataMultiPart formFileData) {
        return SaveUpLoadFile(formFileData, strParam, false);
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
        return SaveUpLoadFile(formFileData, strParam, true);
    }

    /**
     * 解析json
     *
     * @param strJson
     * @return
     * @throws Exception
     */
    public FileDepotParamModel analyzeJsonStr(String strJson) throws Exception {

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
                paramModel.addFileDetail(jsonTempFile.getString(""), jsonTempFile.getString(""), jsonTempFile.getString(""));
            }
            return paramModel;
        } catch (Exception e) {
            throw new Exception("analyze FileParamModel error.:" + e.getLocalizedMessage());
        } finally {
            common.UtileSmart.FreeObjects(paramModel, jsonObj, jsonBody, jsonHead, jsonTempFile, jsonFileDes);
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
    public String SaveUpLoadFile(FormDataMultiPart formFileData, String strJson, boolean isModify) {
        InputStream isTempFile = null;
        FormDataContentDisposition detailTempFile = null;
        String strSvcFileName = null, strUpFileName = null, strTempFilePath = null;
        MediaType mtTempFile = null;
        StringBuffer sbTemp = new StringBuffer();
        boolean bSvcFileExist = false;
        Set<String> setStrSqls = new HashSet<>();
        ExecuteResultParam resultParam = null;
        FileDepotParamModel paramModel = null;
        DepotFileDetailModel tempFileDetailModel = null;
        try {
            paramModel = analyzeJsonStr(strJson);
        } catch (Exception e) {
            return formationResult.formationResult(ResponseResultCode.Error, "解析参数发生错误。", (Object) null);
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
                sbTemp.append(DeployInfo.GetDeployFilePath()).append(File.separator).append(paramModel.rsid);
                FileHelper.CheckFileExist(sbTemp.toString());
                //二级目录
                sbTemp.append(File.separator).append(common.UtileSmart.getCurrentDate());
                FileHelper.CheckFileExist(sbTemp.toString());
                //TODO 如何传入参数 type 路径  并且判断 typePath中是否包含 (File.separator)  如果包含需要判断该文件夹是否存在
                tempFileDetailModel = paramModel.getFileDetailModel(strUpFileName);
                if (tempFileDetailModel == null) {
                    return formationResult.formationResult(ResponseResultCode.Error, String.format("获取文件‘ %s’的详细参数失败。", strUpFileName), (Object) null);
                }
                sbTemp.append(File.separator).append(tempFileDetailModel.fileOwnType);
                FileHelper.CheckFileExist(sbTemp.toString());
                //检查上次文件是否存在
                strSvcFileName = sbTemp.append(File.separator).append(strUpFileName).toString();
                bSvcFileExist = FileHelper.CheckFileExist(strSvcFileName, false);
                if (bSvcFileExist && isModify == false) {
                    return formationResult.formationResult(ResponseResultCode.Error, "文件已经存在，不能修改。请联系管理员维护附件系统。", (Object) null);
                }
                //判断数据库是否存在 ownid 和 fpath重复的数据，如果有数据重复不能上传文件
                resultParam = DBHelper.ExecuteSqlOnceSelect(DeployInfo.MasterRSID, String.format("SELECT COUNT(*) AS ROWSCOUNT FROM FILEDEPOT WHERE OWNID<>'%s' AND FPATH='%s'", paramModel.ownid, strSvcFileName));
                if (resultParam.ResultCode >= 0) {
                    return formationResult.formationResult(ResponseResultCode.Error, String.format("检查数据库文件信息发送错误。%s", resultParam.errMsg), (Object) null);
                }
                //检查ROWSCOUNT 不为0可以继续操作 ROWSCOUNT 不等于0表示有其他文件关联该文件，要求客户修改文件名称，或者联系管理员维护服务器文件
                if (resultParam.ResultJsonObject != null && resultParam.ResultJsonObject.isArray()) {
                    if (Integer.parseInt(resultParam.ResultJsonObject.getString("ROWSCOUNT")) > 0) {
                        return formationResult.formationResult(ResponseResultCode.Error, String.format("‘%s’,该文件名已经存在并于与其他业务数据关联，请修改文件名称重新提交，或者联系管理员维护附件服务器。", strUpFileName), (Object) null);
                    }
                }

                try (OutputStream fileOutputStream = new FileOutputStream(
                        strSvcFileName)) {
                    int read = 0;
                    final byte[] bytes = new byte[1024];
                    while ((read = isTempFile.read(bytes)) != -1) {
                        fileOutputStream.write(bytes, 0, read);
                    }
                    fileOutputStream.close();
                    //生成sql语句，待文件全部上传成功，保存到数据库
                    setStrSqls.add(String.format(
                            "INSERT INTO FILEDEPOT (FID,FNAME,FPATH,FSUMMARY,OWNID,OWNFILETYPE) VALUES ('%s','%s','%s','%s','%s','%s')",
                            UUID.randomUUID().toString(), strUpFileName, strSvcFileName, "md5", paramModel.ownid, tempFileDetailModel.fileOwnType));
                } catch (IOException e) {
                    RSLogger.ErrorLogInfo(e.getMessage());
                    return formationResult.formationResult(
                            ResponseResultCode.Error, "", (Object) null);
                }
                sbTemp.delete(0, sbTemp.length());
            }
            //保存数据到数据库
            resultParam = DBHelper.ExecuteSql(DeployInfo.MasterRSID, setStrSqls);
            if (resultParam.ResultCode >= 0) {
                return formationResult.formationResult(ResponseResultCode.Success, resultParam.ResultCode + "", resultParam.ResultJsonObject);
            } else {
                return formationResult.formationResult(ResponseResultCode.Error, resultParam.errMsg, (Object) null);
            }
        } catch (Exception e) {
            RSLogger.ErrorLogInfo("FileDepot UploadFile error:" + e.
                    getLocalizedMessage());
            return formationResult.formationResult(ResponseResultCode.Error, e.
                    getLocalizedMessage(), (Object) null);
        } finally {
            common.UtileSmart.FreeObjects(isTempFile, detailTempFile,
                    strSvcFileName, strUpFileName, mtTempFile, strTempFilePath,
                    sbTemp, setStrSqls, resultParam, paramModel, tempFileDetailModel);
        }

    }

}
