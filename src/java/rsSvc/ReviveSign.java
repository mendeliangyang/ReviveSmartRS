/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsSvc;

import common.DBHelper;
import common.DeployInfo;
import common.FormationResult;
import common.VerificationSign;
import common.model.ExecuteResultParam;
import common.model.OperateTypeEnum;
import common.model.ResponseResultCode;
import common.model.SignModel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * REST Web Service
 *
 * @author Administrator
 */
@Path("Revive")
public class ReviveSign {

//    @Context
//    private UriInfo context;
    /**
     * Creates a new instance of ReviveSign
     */
    private IAnalyzeReviceParamModel analyzeParamModel = new AnalyzeReviceParamModel();

    private FormationResult formationResult = new FormationResult();

    public ReviveSign() {
    }

    @POST
    @Path("SignOn")
    public String ReviveSignOn(String param) {
        SignModel signModel = null;
        String strTempSql = null, strTokenTemp = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet result = null;
        int iFlag = -1;
        StringBuffer sbTemp = null;
        try {
            signModel = analyzeParamModel.transferReviveRSSignModel(param, OperateTypeEnum.signOn);
            conn = DBHelper.ConnectSybase(DeployInfo.MasterRSID);
            stmt = conn.createStatement();
            if (!common.VerificationSign.verificationSignUser(stmt, signModel.name, signModel.pwd)) {
                //登录用户名或密码有误
                return formationResult.formationResult(ResponseResultCode.Error,new ExecuteResultParam("登录用户名或密码有误", param));
            }
            sbTemp = new StringBuffer();
            sbTemp.append("select count(*) as systemCount from rsSystemUser where rs_SystemName in(");
            for (String signOnRSID : signModel.signOnRSID) {
                sbTemp.append("'").append(signOnRSID).append("',");
            }
            sbTemp.delete(sbTemp.length() - 1, sbTemp.length());
            sbTemp.append(")");
            result = stmt.executeQuery(sbTemp.toString());
            result.next();
            iFlag = result.getInt("systemCount");
            result.close();
            result = null;
            sbTemp.delete(0, sbTemp.length());
            sbTemp = null;
            if (iFlag != signModel.signOnRSID.size()) {
                //输入的rsid不正确
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("RSID 未授权。",param));
            }

            //生成signid
            strTokenTemp = UUID.randomUUID().toString();
            for (String signOnRSID : signModel.signOnRSID) {
//                sbTemp.delete(0, sbTemp.length());
//                sbTemp.append(signModel.name).append("$").append(signOnRSID);
//                strTokenTemp = EncryptToken.EncryptLoginToken(sbTemp.toString());
                strTempSql = String.format("delete rsSignRecord where rs_Name='%s'and rs_SystemName ='%s'", signModel.name, signOnRSID);
                iFlag = stmt.executeUpdate(strTempSql);
                if (iFlag < 0) {
                    //delete error
                    return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("登录失败，数据异常，请稍后重试(D)。",param));
                }
                strTempSql = String.format("insert into rsSignRecord (rs_Name ,rs_SystemName,rs_signToken,rs_DeviceType) values('%s','%s','%s',1)", signModel.name, signOnRSID, strTokenTemp);
                iFlag = stmt.executeUpdate(strTempSql);
                if (iFlag != 1) {
                    //insert error
                    return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("登录失败，数据异常，请稍后重试(I)。",param));
                }
                strTempSql = null;
            }
            return formationResult.formationResult(ResponseResultCode.Success, strTokenTemp,  new ExecuteResultParam());
        } catch (Exception ex) {
            common.RSLogger.ErrorLogInfo("ReviveSignOn error." + ex.getLocalizedMessage());
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(ex.getLocalizedMessage(), param,ex));
        } finally {
            DBHelper.CloseConnection(result, stmt, conn);
            if (signModel != null) {
                signModel.signOnRSID = null;
            }
            common.UtileSmart.FreeObjects(signModel, strTempSql);
        }
        //根据name pwd 查询用户，判断是否存在
        //SELECT count(*) as isExist FROM dbo.rsUser where rs_Name = '' and rs_Pwd= ''
        //isExist !=1 表示用户有误，登录失败
        //根据rsid查询判断是否存在rsid
        // select count(*) as systemCount from rsSystemUser where rs_SystemName in('AustraliaBank','E_Bank')
        //systemCount 和in 里面的数组size不匹配，表示登录的rsids输入有误--登录失败
        //登录成功生产sign
        /*生成sign token  token生成规则 根据用户名 + rsid …… （加密）(guid--去数据库查询数据)
         数据操作的时候传入token根据这些条件+rsid在数据库中查询，如果不存在表示没有登录，存在判断date是否已经超时，如果超时提醒用户超时，*/
        //根据判断是否存在登录记录，如果存在改写登录时间，如果不存在插入登录记录 每次操作一个rsid，循环操作全部
        // 无论有没有，直接delete 然后插入新的数据
        //select count(*) isSign from rsSignRecord where rs_Name=''and rs_SystemName =''
    }

    @POST
    @Path("SignOff")
    public String ReviveSignOff(String param) {
        /*
         根据 SignToken 清除所有rsSignRecord
         */
        SignModel signModel = null;
        Connection conn = null;
        Statement stmt = null;
        int iFlag = -1;
        try {
            signModel = analyzeParamModel.transferReviveRSSignModel(param, OperateTypeEnum.signOff);
            conn = DBHelper.ConnectSybase(DeployInfo.MasterRSID);
            stmt = conn.createStatement();
            iFlag = VerificationSign.cancelSignOn(stmt, signModel.token);
            if (iFlag < 0) {
                //delete error
                return formationResult.formationResult(ResponseResultCode.Error,new ExecuteResultParam( "数据异常，请稍后重试(D)。", param));
            }
            return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam());
        } catch (Exception ex) {
            common.RSLogger.ErrorLogInfo("ReviveSignOff error." + ex.getLocalizedMessage());
            return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam(ex.getLocalizedMessage(), param,ex));
        } finally {
            DBHelper.CloseConnection(stmt, conn);
            common.UtileSmart.FreeObjects(signModel);
        }
    }

}
