/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsSvc.AustraliaBankRS;

import common.DatagramCoder;
import common.FormationResult;
import common.model.ExecuteResultParam;
import common.model.ResponseResultCode;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import net.sf.json.JSONObject;
import rsSocketTranscation.SocketClientHelper;
import rsSocketTranscation.SocketMsgSet;
import rsSvc.AustraliaBankRS.Model.txn_0402Entity;

/**
 * REST Web Service
 *
 * @author Administrator
 */
@Path("AustraliaBankRS")
public class AustraliaBankRS {

    @Context
    private UriInfo context;

    private FormationResult formationResult = new FormationResult();

    /**
     * Creates a new instance of AustraliaBankRS
     */
    public AustraliaBankRS() {
    }

    @POST
    @Path("ChangePassword")
    public String ChangePassword(String param) {

        txn_0402Entity txn = null;
        JSONObject jObj = null, jBody = null;
        int iFlag = -1;
        byte[] byteMsg = null, byteRets = null;
        String strTemp = null;
        try {
            jObj = JSONObject.fromObject(param);
            jBody = jObj.getJSONObject("body");
            txn = new txn_0402Entity();
            txn.CardNo = common.DatagramCoder.padRight(jBody.getString("CardNo").toCharArray(), 150);

            //去掉 首尾  02  03
            strTemp= jBody.getString("OldPasswd");
            txn.OldPasswd=DatagramCoder.hexStringToBytes(DatagramCoder.padRight(strTemp.substring(2, strTemp.length() - 4), 60));
           
            strTemp= jBody.getString("NewPasswd");
            txn.NewPasswd = DatagramCoder.hexStringToBytes( DatagramCoder.padRight( strTemp.substring(2,strTemp.length()-4),60));

            txn.IdCardType = common.DatagramCoder.padRight(jBody.getString("IdCardType").toCharArray(), 3);
            txn.IdCardNo = common.DatagramCoder.padRight(jBody.getString("IdCardNo").toCharArray(), 20);

            //            数据包格式：040402
            //报文头（0x02） +操作类型（0x81）+ 交易码（6个字节）+ 数据长度（4个字节）+ 报文内容（ buf ）+ 报文尾（0x03）
            byteMsg = SocketMsgSet.TakeMsgByte((byte) 0x81, "040402", txn.toBytesFromSelf());

            byteRets = SocketClientHelper.DealOnce(byteMsg);

            //checkMsgForm
            byte[] byteData = null, byteOddNum = null, byteRet = null;
            //判断报文格式是否正确
            byteData = DatagramCoder.checkMsgForm(byteRets, (byte) 0x02, (byte) 0x81, null);
            if (byteData == null) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("获取申请数据失败",param ));
            }
//            byteOddNum = new byte[4];1
//            byteOddNum[0] = byteData[5];
//            byteOddNum[1] = byteData[4];
//            byteOddNum[2] = byteData[3];
//            byteOddNum[3] = byteData[2];
//            int iOddNum = DatagramCoder.unsigned4BytesToInt(byteOddNum, 0);
            if (byteData == null || byteData.length == 0) {
                return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("申请改密失败",param ));
            }
            //定时发送    报文头（0x02） +操作类型（0x84）+单号长度（4个字节）+单号+ 报文尾（0x03）
            //把第一次返回的信息直接替换操作类型在返回给服务端
            byteMsg = byteRets;
            byteMsg[1] = (byte) 0x84;
            int iRequestCount = 0;
            while (true) {
                //请求 24次，2分钟，如果没有返回表示处理失败
                if (iRequestCount > 24) {
                    return formationResult.formationResult(ResponseResultCode.Error,new ExecuteResultParam("改密处理失败",param ));
                }
                //接收：报文头（0x02） +操作类型（0x84）+交易状态长度（4个字节）+交易状态+ 报文尾（0x03）
                byteData = SocketClientHelper.DealOnce(byteMsg);
                //判断报文格式是否正确
                byte[] byteNum = DatagramCoder.checkMsgForm(byteData, (byte) 0x02, (byte) 0x84, null);
                if (null == byteNum) {
                    return formationResult.formationResult(ResponseResultCode.Error, new ExecuteResultParam("获取处理结果失败",param ));
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
                if (byteNum[0] == 0x31) {
                    return formationResult.formationResult(ResponseResultCode.Success, new ExecuteResultParam());
                }
                //5s 询问一次  一定时间内都是0就更新为失败
                //System.Threading.Thread.Sleep(5000);
                Thread.sleep(5000);
                iRequestCount++;
            }
        } catch (Exception ex) {
            common.RSLogger.ErrorLogInfo("ReviveSignOff error." + ex.getLocalizedMessage());
            return formationResult.formationResult(ResponseResultCode.Error,new ExecuteResultParam(ex.getLocalizedMessage(),param ,ex));
        } finally {
            common.UtileSmart.FreeObjects(jObj, txn, jBody, byteMsg, byteRets);
        }
    }

}
