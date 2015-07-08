/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.io.File;

/**
 *
 * @author Administrator
 */
public class FileHelper {

    /**
     * 获取文件扩展名
     *
     * @param filename
     * @return
     */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    /**
     * 判断文件或目录是否存在
     *
     * @param filePath 文件路径
     * @param isCreate 不存在创建新文件
     * @return
     * @throws java.lang.Exception
     */
    public static boolean CheckFileExist(String filePath, boolean isCreate) throws Exception {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                if (isCreate) {
                    file.mkdir();
                }
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            throw new Exception(e.getLocalizedMessage());
        }
    }

    /**
     * 判断文件或目录是否存在,如果不存在创建
     * @param filePath 文件路径
     * @return
     * @throws Exception 
     */
    public static boolean CheckFileExist(String filePath) throws Exception {
        return CheckFileExist(filePath, true);
    }

}
