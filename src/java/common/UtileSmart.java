/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
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

    public static Object getObjectFromMap(Map<String, Object> map, String key, boolean throwError) throws Exception {
        if (map == null || key == null) {
            throw new Exception("error on getObjectFromMap , paramateter is null.");
        }
        if (!map.containsKey(key)) {
            if (throwError) {
                throw new Exception("error on getObjectFromMap , map no containsKey :" + key);
            }
            return null;
        }
        Object value = map.get(key);
        if (value == null && throwError) {
            throw new Exception(String.format("error on getObjectFromMap, The inside of the map %s value is empty ", key));
        }
        return value;
    }

    public static String getStringFromMap(Map<String, Object> map, String key) throws Exception {
        return getObjectFromMap(map, key, true).toString();
    }

    public static Object[] getListFromMap(Map<String, Object> map, String key) throws Exception {
        return (Object[]) getObjectFromMap(map, key, true);
    }

    public static String tryGetStringFromMap(Map<String, Object> map, String key) throws Exception {
        return getObjectFromMap(map, key, false).toString();
    }

    public static Object[] tryGetListFromMap(Map<String, Object> map, String key) throws Exception {
        return (Object[]) getObjectFromMap(map, key, false);
    }

    public static void cleanMapTDString(Map<String, Map<String, String>> mapOrigin) {
        if (mapOrigin == null) {
            return;
        }
        Iterator iterator = mapOrigin.values().iterator();
        while (iterator.hasNext()) {
            Map<String, String> next = (Map<String, String>) iterator.next();
            mapOrigin.values().remove(next);
            next.clear();
        }
        mapOrigin.clear();
    }

    /**
     * *
     * 获取当前系统时间字符串 yyyyMMddHH
     *
     * @return
     */
    public static String getCurrentDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHH");//设置日期格式
        return df.format(new Date());
    }

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * *
     * 获取当前系统时间字符串 yyyyMMddHH
     *
     * @return
     */
    public static String getCurrentDateShortTime() {
        SimpleDateFormat df = new SimpleDateFormat("HHmmss");//设置日期格式
        return df.format(new Date());
    }

    /**
     * *
     * 获取当前系统 秒的时间字符串 ss
     *
     * @return
     */
    public static String getCurrentDateSecond() {
        SimpleDateFormat df = new SimpleDateFormat("ss");//设置日期格式
        return df.format(new Date());
    }

    static Random random = new Random();

    public static String getRandomStrbySeed(int pSeed) {
        return String.format("%d", random.nextInt(pSeed));
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
            common.RSLogger.ErrorLogInfo(String.format("UtileSmart: GetJsonString error:%s", ex.getLocalizedMessage()), ex);
        }
        return null;
    }
}
