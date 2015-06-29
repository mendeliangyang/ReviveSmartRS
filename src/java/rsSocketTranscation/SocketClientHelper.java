/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsSocketTranscation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public class SocketClientHelper {

    
    
    /**
     *
     * @param byteMsg
     * @return
     */
    public static byte[] DealOnce(byte[] byteMsg) throws IOException {
        Socket socketClient = null;
        byte[] bytePartResult = null,byteRet=null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        int iReceive = 0;
        Map<String, byte[]> byteMapRets=null;
        
            int iPartRet=512;
        try {

            socketClient = new Socket();
            socketClient.setSoTimeout(5000);
            socketClient.connect(new InetSocketAddress("192.168.169.188", 7777), 5000);
            outputStream = socketClient.getOutputStream();
            outputStream.write(byteMsg);
            outputStream.flush();

            inputStream = socketClient.getInputStream();
            //iReceive = socketClient.getReceiveBufferSize();
            bytePartResult = new byte[iPartRet];
            int iPart = 0, iPartSize = 0, iTotal = 0;
            //iPart=inputStream.read(byteResult);
//            if (iPart!=iReceive) { 
//                throw new IOException("readReceive error.read Count no equal receiveBufferSize.");
//            }
            byteMapRets = new HashMap<>();
            while ((iPartSize = inputStream.read(bytePartResult)) != -1) {
                byteMapRets.put(iPart+"$"+iPartSize, bytePartResult);
                iPart++;
                iTotal += iPartSize;
                //读取数据超过在数组范围内，表示读取数据完成
                if (iPartRet>=iPartSize) {
                    break;
                }
            }
            byteRet = new byte[iTotal];
            iPart= 0;
            int iCutIndex=0;
            for (Map.Entry<String, byte[]> entrySet : byteMapRets.entrySet()) {
                String key = entrySet.getKey();
                iCutIndex=key.indexOf("$");
                iCutIndex=Integer.parseInt( key.substring(iCutIndex+1, key.length()));
                byte[] value = entrySet.getValue();
                System.arraycopy(value, 0, byteRet, iPart, iCutIndex);
                iPart += iCutIndex;
            }

        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (socketClient != null) {
                socketClient.close();
            }
            common.UtileSmart.FreeObjects(socketClient, bytePartResult, outputStream, inputStream);
        }

        return byteRet;
    }

}
