/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsSocketTranscation;

import java.io.UnsupportedEncodingException;

/**
 *
 * @author Administrator
 */
public class SocketMsgSet {
    
    /**
     * 报文头（0x02） +操作类型（0x81）+ 交易码（6个字节）+ 数据长度（4个字节）+ 报文内容（ buf ）+ 报文尾（0x03）
     */
    public static final int  iMHeadLen = 1,iMOperonLen=1,iMDealCodeLen = 6,iMDataLen = 4,iMEnd=1;
    
    /**
     * 除了 buf体的标准长度
     */
    public static final int iMNormLen = iMHeadLen+iMOperonLen+iMDealCodeLen+iMDataLen+iMEnd;
    
    /**
     *             数据包格式：040402
        报文头（0x02） +操作类型（0x25）+ 交易码（6个字节）+ 数据长度（4个字节）+ 报文内容（ buf ）+ 报文尾（0x03）
     */
    public static byte[] TakeMsgByte(byte bHead,byte bOperon,String strDealCode,byte[] bBuf,byte bEnd) throws UnsupportedEncodingException{
        
        byte[] bMsg = new byte[iMNormLen+bBuf.length];
        bMsg[0] = bHead;bMsg[1] = bOperon;
        System.arraycopy(common.DatagramCoder.takeStringToByte(strDealCode), 0, bMsg, SocketMsgSet.iMHeadLen+SocketMsgSet.iMOperonLen, SocketMsgSet.iMDealCodeLen);
        
        System.arraycopy(common.DatagramCoder.intToByteArray(bBuf.length), 0, bMsg, SocketMsgSet.iMHeadLen+SocketMsgSet.iMOperonLen+SocketMsgSet.iMDealCodeLen, SocketMsgSet.iMDataLen);
        System.arraycopy(bBuf, 0, bMsg, SocketMsgSet.iMHeadLen+SocketMsgSet.iMOperonLen+SocketMsgSet.iMDealCodeLen+ SocketMsgSet.iMDataLen, bBuf.length);
        
        bMsg[bMsg.length] = bEnd;
        return bMsg;
        
    }
    
    /**
     *  消息结束头，与消息未默认 0x02，0x03
     * @param bOperon 
     * @param strDealCode 
     * @param bBuf 
     * @return
     * @throws UnsupportedEncodingException 
     */
    public static byte[] TakeMsgByte(byte bOperon,String strDealCode,byte[] bBuf) throws UnsupportedEncodingException{
       return TakeMsgByte((byte)0x02,bOperon,strDealCode,bBuf,(byte)0x03);
    }
    
}
