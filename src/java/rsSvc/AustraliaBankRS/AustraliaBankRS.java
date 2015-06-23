/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsSvc.AustraliaBankRS;

import common.DBHelper;
import common.DatagramCoder;
import common.FormationResult;
import common.comInterface.IFormationResult;
import common.model.ResponseResultCode;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import net.sf.json.JSONObject;
import rsSocketTranscation.SocketClientHelper;

/**
 * REST Web Service
 *
 * @author Administrator
 */
@Path("AustraliaBankRS")
public class AustraliaBankRS {

    @Context
    private UriInfo context;

    private IFormationResult formationResult = new FormationResult();

    /**
     * Creates a new instance of AustraliaBankRS
     */
    public AustraliaBankRS() {
    }

    @POST
    @Path("ChangePassword")
    public String ChangePassword(String param) {

        txn_040402Entity txn = null;
        JSONObject jObj = null, jBody = null;
        int iFlag = -1;
        byte[] byteMsg = null, byteRets = null;
        try {
            jObj = JSONObject.fromObject(param);
            jBody = jObj.getJSONObject("body");
            txn = new txn_040402Entity();
            txn.CardNo = common.DatagramCoder.PadRight(jBody.getString("CardNo").toCharArray(), 150);
            txn.OldPasswd = common.DatagramCoder.PadRight(jBody.getString("OldPasswd").toCharArray(), 30);
            txn.NewPasswd = common.DatagramCoder.PadRight(jBody.getString("NewPasswd").toCharArray(), 30);
            txn.IdCardType = common.DatagramCoder.PadRight(jBody.getString("IdCardType").toCharArray(), 3);
            txn.IdCardNo = common.DatagramCoder.PadRight(jBody.getString("IdCardNo").toCharArray(), 20);

            //            数据包格式：040402
            //报文头（0x02） +操作类型（0x81）+ 交易码（6个字节）+ 数据长度（4个字节）+ 报文内容（ buf ）+ 报文尾（0x03）
            int iMsgSize = 1 + 1 + 6 + 4 + 150 + 30 + 30 + 3 + 20 + 1;
            byteMsg = new byte[iMsgSize];
            byteMsg[0] = (byte) 0x02;
            byteMsg[1] = (byte) 0x81;
            System.arraycopy(common.DatagramCoder.takeStringToByte("040402"), 0, byteMsg, 2, 6);
            System.arraycopy(common.DatagramCoder.intToByteArray(150 + 30 + 30 + 3 + 20), 0, byteMsg, 8, 4);
            System.arraycopy(DatagramCoder.getBytes(txn.CardNo), 0, byteMsg, 8 + 4, 150);
            System.arraycopy(DatagramCoder.getBytes(txn.OldPasswd), 0, byteMsg, 8 + 4 + 150, 30);
            System.arraycopy(DatagramCoder.getBytes(txn.NewPasswd), 0, byteMsg, 8 + 4 + 150 + 30, 30);
            System.arraycopy(DatagramCoder.getBytes(txn.IdCardType), 0, byteMsg, 8 + 4 + 150 + 30 + 30, 3);
            System.arraycopy(DatagramCoder.getBytes(txn.IdCardNo), 0, byteMsg, 8 + 4 + 150 + 30 + 30 + 3, 20);
            byteMsg[245] = (byte) (int) 0x03;

            byteRets = SocketClientHelper.DealOnce(byteMsg);

            //checkMsgForm
            byte[] byteData = null, byteOddNum = null, byteRet = null;
            //判断报文格式是否正确
            byteData = DatagramCoder.checkMsgForm(byteRets, (byte) 0x02, (byte) 0x81, null);
            if (byteData == null) {
                return formationResult.formationResult(ResponseResultCode.Error, "获取申请数据失败", "", (Object) null);
            }
//            byteOddNum = new byte[4];
//            byteOddNum[0] = byteData[5];
//            byteOddNum[1] = byteData[4];
//            byteOddNum[2] = byteData[3];
//            byteOddNum[3] = byteData[2];
//            int iOddNum = DatagramCoder.unsigned4BytesToInt(byteOddNum, 0);
            if (byteData == null||byteData.length==0) {
                return formationResult.formationResult(ResponseResultCode.Error, "申请改密失败", "", (Object) null);
            }
            //定时发送    报文头（0x02） +操作类型（0x84）+单号长度（4个字节）+单号+ 报文尾（0x03）
            //把第一次返回的信息直接替换操作类型在返回给服务端
            byteMsg = byteRets;
            byteMsg[1] = (byte) 0x84;
            int iRequestCount = 0;
            while (true) {
                //请求 24次，2分钟，如果没有返回表示处理失败
                if (iRequestCount > 24) {
                    return formationResult.formationResult(ResponseResultCode.Error, "改密处理失败。", "", (Object) null);
                }
                //接收：报文头（0x02） +操作类型（0x84）+交易状态长度（4个字节）+交易状态+ 报文尾（0x03）
                byteData = SocketClientHelper.DealOnce(byteMsg);
                //判断报文格式是否正确
                byte[] byteNum = DatagramCoder.checkMsgForm(byteData, (byte) 0x02, (byte) 0x84, null);
                if (null ==byteNum) {
                    return formationResult.formationResult(ResponseResultCode.Error, "获取处理结果失败。", "", (Object) null);
                }
                //有0和1  不考虑处理失败
//                byteNum= new byte[4];
//                byteNum[0] = byteData[5];
//                byteNum[1] = byteData[4];
//                byteNum[2] = byteData[3];
//                byteNum[3] = byteData[2];
//                int iMsgLen = DatagramCoder.unsigned4BytesToInt(byteNum, 0);
//                byteRet = new byte[iMsgLen];
//                System.arraycopy(byteData, 6, byteRet, 0, iMsgLen);
                    //todo 不清楚具体的数据
                //System.Text.Encoding.ASCII.GetString(byteRet);
                //int iRetCode= BitConverter.ToInt32(byteRet,0);
                if (byteNum[0] == 0x01) {
                    return formationResult.formationResult(ResponseResultCode.Success, "修改密码成功。", "",(Object) null);
                }
                    //5s 询问一次  一定时间内都是0就更新为失败
                //System.Threading.Thread.Sleep(5000);
                Thread.sleep(5000);
                iRequestCount++;
            }
        } catch (Exception ex) {
            common.RSLogger.ErrorLogInfo("ReviveSignOff error." + ex.getLocalizedMessage());
            return formationResult.formationResult(ResponseResultCode.Error, ex.getLocalizedMessage(), "", (Object) null);
        } finally {
            common.UtileSmart.FreeObjects(jObj, txn, jBody, byteMsg, byteRets);
        }
    }

    public class txn_040402Entity {

        public char[] CardNo;
        public char[] OldPasswd;
        public char[] NewPasswd;
        public char[] IdCardType;
        public char[] IdCardNo;
    }
}
