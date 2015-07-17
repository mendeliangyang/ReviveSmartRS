/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Time;

/**
 *
 * @author Administrator
 */
public class VerificationSign {
    

    // private static final short signOutTime = 30; //minute
    /**
     * //common.VerificationSign.verificationSignOn(signModel.token, "rsid");
     *
     * @param token
     * @param rsid
     * @return
     */
    public static boolean verificationSignOn(String token, String rsid) {
        //todo 验证是否登录
        if (token == null || rsid == null || token.isEmpty() || rsid.isEmpty()) {
            return true;
        }
        //根据 token rsid 查询相关记录
        String strTempSql = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet result = null;
        Date rs_SignDate = null, nowDate = null;
        Time rs_SignTime = null, nowTime = null;
        int iFlag = -1;
        try {
            conn = DBHelper.ConnectSybase(DeployInfo.MasterRSID);
            stmt = conn.createStatement();
            strTempSql = "select getdate() as nowDate ";
            result = stmt.executeQuery(strTempSql);
            if (result.next()) {
                nowDate = result.getDate("nowDate");
                nowTime = result.getTime("nowDate");
            }
            result.close();
            result = null;
            strTempSql = null;
            strTempSql = String.format("SELECT rs_Name,rs_SignDate,rs_DeviceType FROM rsSignRecord where rs_signToken='%s' and  rs_SystemName='%s'", token, rsid);
            result = stmt.executeQuery(strTempSql);
            // 循环取出数据，如果有多条数据表示数据库数据错误，单条数据，判断 rs_SignDate 的时间是否超时，如果没有超时返回true（并更新token rs_SignDate重新获取数据库时间）否则返回false
            iFlag = 0;
            while (result.next()) {
                iFlag++;
                if (iFlag == 1) {
                    rs_SignDate = result.getDate("rs_SignDate");
                    rs_SignTime = result.getTime("rs_SignDate");
                } else {
                    //多条数据数据异常 删除数据，要求用户重新登录
                    VerificationSign.cancelSignOn(stmt, token);
                    return false;
                }
            }
            result.close();
            result = null;
            if (nowDate.equals(rs_SignDate)) {
                long slipTime = nowTime.getTime() - rs_SignTime.getTime();
                if (slipTime < (DeployInfo.GetHttpTimeOut() * 60 * 1000)) {
                    //会话在有效期内
                    //更新当前的操作时间
                    iFlag = stmt.executeUpdate(String.format("update rsSignRecord set rs_SignDate =getdate() where rs_signToken='%s'", token));
                    if (iFlag > 0) {
                        return true;
                    } else {
                        //更新最新操作时间失败，要求用户重新登录
                        VerificationSign.cancelSignOn(stmt, token);
                        return false;
                    }

                } else {
                    //会话超时
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            common.RSLogger.ErrorLogInfo("verificationSignOn error." + e.getLocalizedMessage());
            return false;
        } finally {
            DBHelper.CloseConnection(result, stmt, conn);
            common.UtileSmart.FreeObjects(strTempSql);
        }
    }

    public static int cancelSignOn(Statement stmt, String token) throws Exception {
        if (stmt == null) {
            throw new Exception("cancelSignOn error stmt is null");
        }
        if (stmt.isClosed()) {
            throw new Exception("cancelSignOn error stmt is closed");
        }
        return stmt.executeUpdate(String.format("delete rsSignRecord where rs_signToken='%s'", token));
    }

    public static boolean verificationSignUser(Statement stmt, String rs_user, String rs_pwd) throws Exception {
        ResultSet result = null;
        try {
            result = stmt.executeQuery(String.format("SELECT count(*) as isExist FROM dbo.rsUser where rs_Name = '%s' and rs_Pwd= '%s'", rs_user, rs_pwd));
            result.next();
            int iFlag = result.getInt("isExist");
            result.close();
            result = null;
            return iFlag == 1;
        } catch (Exception e) {
            common.RSLogger.ErrorLogInfo("verificationSignUser error." + e.getLocalizedMessage());
            throw new Exception("verificationSignUser error." + e.getLocalizedMessage());
        }
    }

    public static boolean verificationSignUser(String rs_user, String rs_pwd) throws Exception {
        //todo 验证用户名
        if (rs_user==null||rs_pwd==null||rs_user.isEmpty()||rs_pwd.isEmpty()) {
            return true;
        }
        ResultSet result = null;
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DBHelper.ConnectSybase(DeployInfo.MasterRSID);
            stmt = conn.createStatement();
            result = stmt.executeQuery(String.format("SELECT count(*) as isExist FROM dbo.rsUser where rs_Name = '%s' and rs_Pwd= '%s'", rs_user, rs_pwd));
            result.next();
            int iFlag = result.getInt("isExist");
            result.close();
            result = null;
            return iFlag == 1;
        } catch (Exception e) {
            common.RSLogger.ErrorLogInfo("verificationSignUser error." + e.getLocalizedMessage());
            throw new Exception("verificationSignUser error." + e.getLocalizedMessage());
        }finally{
            common.DBHelper.CloseConnection(result, stmt, conn);
        }
    }
}
