/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.text.SimpleDateFormat;
import java.util.Date;
import net.sf.json.JSONObject;

/**
 *
 * @author Administrator
 */
public class UtileSmart {

    public static void FreeObjects(Object... objects) {
        if (objects != null) {
            for (Object obj : objects) {
                obj = null;
            }
        }
    }

    /***
     * 获取当前系统时间字符串  yyyyMMddHH
     * @return 
     */
    public static String getCurrentDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHH");//设置日期格式
        return df.format(new Date());
    }

    public static short overrideParseShort(String strShort) {
        if (strShort != null && !strShort.isEmpty()) {
            return Short.parseShort(strShort);
        }
        return -1;
    }

    public static int overrideParseInt(String strInt) {
        if (strInt != null && !strInt.isEmpty()) {
            return Integer.parseInt(strInt);
        }
        return -1;
    }

    /**
     *
     * @param jsonObj
     * @param strParam
     * @param isException 没有 strParam 属性，true引发异常，false返回null
     * @return
     * @throws Exception
     */
    public static String GetJsonString(JSONObject jsonObj, String strParam, boolean isException) throws Exception {
        if (jsonObj.containsKey(strParam)) {
            return jsonObj.getString(strParam);
        }
        if (isException) {
            throw new Exception("jsonObject There is no " + strParam);
        }
        return null;
    }

    /**
     *
     * @param jsonObj
     * @param strParam
     * @return 没有 strParam 属性返回null
     */
    public static String GetJsonString(JSONObject jsonObj, String strParam) {
        try {
            return GetJsonString(jsonObj, strParam, false);
        } catch (Exception ex) {
            common.RSLogger.ErrorLogInfo(ex.getLocalizedMessage());
        }
        return null;
    }
}
