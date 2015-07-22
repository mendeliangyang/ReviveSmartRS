/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Administrator
 */
public class FileHelper {

    public static boolean ConvertBase64ToImage(String Base64param, String filePath) {
        if (Base64param == null) // 图像数据为空
        {
            return false;
        }
        //BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] data = Base64.decodeBase64(Base64param);
            try (OutputStream stream = new FileOutputStream(filePath)) {
                stream.write(data);
                stream.close();
            }
            return true;
        } catch (Exception e) {
            RSLogger.ErrorLogInfo(String.format("ConvertBase64ToImage error: %s", e.getLocalizedMessage()), e);
            return false;
        }

    }

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
     * 根据路径删除指定的目录或文件，无论存在与否
     *
     * @param sPath 要删除的目录或文件
     * @return 删除成功返回 true，否则返回 false。
     */
    public void DeleteFolder(String sPath) {
        File file = new File(sPath);
        // 判断目录或文件是否存在
        if (!file.exists()) {  // 不存在返回 false
            return;
        } else {
            // 判断是否为文件
            if (file.isFile()) {  // 为文件时调用删除文件方法
                deleteFile(sPath);
            } else {  // 为目录时调用删除目录方法
                deleteDirectory(sPath);
            }
        }
    }

    /**
     * 删除目录（文件夹）以及目录下的文件
     *
     * @param sPath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public boolean deleteDirectory(String sPath) {
        //如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        //如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        //删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            //删除子文件
            if (files[i].isFile()) {
                deleteFile(files[i].getAbsolutePath());
            } //删除子目录
            else {
                deleteDirectory(files[i].getAbsolutePath());
            }
        }
        //删除当前目录
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 删除单个文件
     *
     * @param sPath 被删除文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static void deleteFile(String sPath) {
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
        }
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
     *
     * @param filePath 文件路径
     * @return
     * @throws Exception
     */
    public static boolean CheckFileExist(String filePath) throws Exception {
        return CheckFileExist(filePath, true);
    }

}
