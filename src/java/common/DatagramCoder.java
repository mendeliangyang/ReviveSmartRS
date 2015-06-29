/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 *
 * @author Administrator
 */
public class DatagramCoder {

    static String DefaultCharset = "ASCII";
    
    
    public static byte[] strHex16ToByte(String strHex16){
        if (strHex16.isEmpty()||strHex16.length()%2!=0) {
            return null;
        }
        byte[] bRet = new byte[strHex16.length()/2];
        for (int i = 0,j=0; i < bRet.length; i++,j+=2) {
            bRet[i] = (byte) Integer.parseInt(strHex16.substring(j,2),16);
        }
        return bRet;
    }

    public static byte[] checkMsgForm(byte[] bytes, byte byteHead, byte byteOperator, byte[] byteDealCode) {
        byte[] byteTemp = null, byteData = null;
        int iEffect = 0;
        if (bytes == null || bytes.length < 2) {
            return null;
        }
        if (bytes[iEffect] != byteHead) {
            return null;
        }
        iEffect++;
        if (bytes[iEffect] != byteOperator) {
            return null;
        }
        iEffect++;
        if (byteDealCode != null) {
            int iDealCodeLen = byteDealCode.length;
            byteTemp = new byte[iDealCodeLen];
            System.arraycopy(bytes, iEffect, byteTemp, 0, iDealCodeLen);
            for (int i = 0; i < iDealCodeLen; i++) {
                if (byteTemp[i] != byteDealCode[i]) {
                    return null;
                }
            }
            byteTemp = null;
            iEffect += iDealCodeLen;
        }

        byteTemp = new byte[4];

        byteTemp[0] = bytes[iEffect + 3];
        byteTemp[1] = bytes[iEffect + 2];
        byteTemp[2] = bytes[iEffect + 1];
        byteTemp[3] = bytes[iEffect + 0];
        iEffect += 4;
        int iDataLen = unsigned4BytesToInt(byteTemp, 0);
        byteTemp = null;
        byteData = new byte[iDataLen];
        System.arraycopy(bytes, iEffect, byteData, 0, iDataLen);
        iEffect += iDataLen;
        if (bytes[iEffect] == 0x03) {
            return byteData;
        }
        return null;
    }

    public static char[] padRight(char[] byteSource, int size) {
        if (byteSource.length == size) {
            return byteSource;
        }
        int iLack = size - byteSource.length;
        if (iLack < 0) {
            return byteSource;
        }
        char[] byteResult = new char[size];
        System.arraycopy(byteSource, 0, byteResult, 0, byteSource.length);
        for (int i = iLack; i > size; i--) {
            byteResult[i] = 0x00;
        }
        return byteResult;
    }
    
    public static String padRight(String strSource,int iSize){
        return padRight(strSource,iSize,'0');
    }
    
    public static String padRight(String strSource,int size,char cFill){
         if (strSource.length() == size) {
            return strSource;
        }
        int iLack = size - strSource.length();
        if (iLack < 0) {
            return strSource;
        }
        StringBuffer sbStr = new StringBuffer();
        sbStr.append(strSource);
        for (int i = iLack; i > size; i--) {
            sbStr.append(cFill);
        }
        return sbStr.toString();
    }

    public static byte[] padRight(byte[] byteSource, int iSize) {
        return DatagramCoder.padRight(byteSource, iSize, (byte)0x00);
    }

    public static byte[] padRight(byte[] byteSource, int size, byte byteFill) {
        if (byteSource.length == size) {
            return byteSource;
        }
        int iLack = size - byteSource.length;
        if (iLack < 0) {
            return byteSource;
        }
        byte[] byteResult = new byte[size];
        System.arraycopy(byteSource, 0, byteResult, 0, byteSource.length);
        for (int i = iLack; i > size; i--) {
            byteResult[i] = byteFill;
        }
        return byteResult;
    }

    public static String printHexString(byte[] b) {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            result = result + hex.toUpperCase();
        }
        return result;

    }

    public static byte[] getBytes(char[] chars) {
        Charset cs = Charset.forName(DefaultCharset);
        CharBuffer cb = CharBuffer.allocate(chars.length);
        cb.put(chars);
        cb.flip();
        ByteBuffer bb = cs.encode(cb);
        return bb.array();

    }

// byte转char
    public static char[] getChars(byte[] bytes) {
        Charset cs = Charset.forName(DefaultCharset);
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        CharBuffer cb = cs.decode(bb);
        return cb.array();
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * convert string to byte[]
     *
     * @param str
     * @return
     * @throws UnsupportedEncodingException
     */
    public static byte[] takeStringToByte(String str) throws UnsupportedEncodingException {
        return str.getBytes(DefaultCharset);
    }

    /**
     * 将一个单字节的byte转换成32位的int
     *
     * @param b byte
     * @return convert result
     */
    public static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }

    /**
     * 将一个单字节的Byte转换成十六进制的数
     *
     * @param b . * byte
     * @return convert result
     */
    public static String byteToHex(byte b) {
        int i = b & 0xFF;
        return Integer.toHexString(i);
    }

    /**
     * 将一个4byte的数组转换成32位的int
     *
     * @param buf bytes buffer
     * @param byte[]中开始转换的位置
     * @return convert result
     */
    public static int unsigned4BytesToInt(byte[] buf, int pos) {
        int firstByte = 0;
        int secondByte = 0;
        int thirdByte = 0;
        int fourthByte = 0;
        int index = pos;
//        firstByte = (0x000000FF & ((int) buf[index]));
//        secondByte = (0x000000FF & ((int) buf[index + 1]));
//        thirdByte = (0x000000FF & ((int) buf[index + 2]));
//        fourthByte = (0x000000FF & ((int) buf[index + 3]));
        firstByte = (0x000000FF & ((int) buf[index + 3]));
        secondByte = (0x000000FF & ((int) buf[index + 2]));
        thirdByte = (0x000000FF & ((int) buf[index + 1]));
        fourthByte = (0x000000FF & ((int) buf[index + 0]));
        index = index + 4;
        return ((int) (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) & 0xFFFFFFFF;
    }

    /**
     * 将16位的short转换成byte数组
     *
     * @param s short
     * @return byte[] 长度为2
     *
     */
    public static byte[] shortToByteArray(short s) {
        byte[] targets = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = (targets.length - 1 - i) * 8;
            targets[i] = (byte) ((s >>> offset) & 0xff);
        }
        return targets;
    }

    /**
     * 将32位整数转换成长度为4的byte数组
     *
     * @param s int
     * @return byte[]
     *
     */
    public static byte[] intToByteArray(int s) {
        byte[] targets = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = (targets.length - 1 - i) * 8;
            targets[i] = (byte) ((s >>> offset) & 0xff);
        }
        return targets;
    }

    /**
     * long to byte[]
     *
     * @param s long
     * @return byte[]
     *
     */
    public static byte[] longToByteArray(long s) {
        byte[] targets = new byte[2];
        for (int i = 0; i < 8; i++) {
            int offset = (targets.length - 1 - i) * 8;
            targets[i] = (byte) ((s >>> offset) & 0xff);
        }
        return targets;
    }

    /**
     * 32位int转byte[]
     */
    public static byte[] int2byte(int res) {
        byte[] targets = new byte[4];
        targets[0] = (byte) (res & 0xff);// 最低位  
        targets[1] = (byte) ((res >> 8) & 0xff);// 次低位  
        targets[2] = (byte) ((res >> 16) & 0xff);// 次高位  
        targets[3] = (byte) (res >>> 24);// 最高位,无符号右移。  
        return targets;
    }

    /**
     * 将长度为2的byte数组转换为16位int
     *
     * @param res byte[]
     * @return int
     *
     */
    public static int byte2int(byte[] res) {
        // res = InversionByte(res);  
        // 一个byte数据左移24位变成0x??000000，再右移8位变成0x00??0000  
        int targets = (res[0] & 0xff) | ((res[1] << 8) & 0xff00); // | 表示安位或  
        return targets;
    }

}
