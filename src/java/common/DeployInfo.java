/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import common.model.MsgFilterModel;
import common.model.SystemSetModel;
import java.io.File;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

public class DeployInfo {

    public static String paramTableName = "db_tableName";
    public static String ResultDataTag = "resultDatas";
    public static String StringLinkMark = "|";
    public static String paramtokenKey = "token";
    public static String paramRSIDKey ="RSID";
    public static String httpPathLinkMark ="/";
    
    public static final String MasterRSID ="ReviveSmartDB";

    public static boolean readSetUp() throws Exception {
        File fXmlFile = null;
        DocumentBuilderFactory dbFactory = null;
        DocumentBuilder dBuilder = null;
        Document doc = null;
        NodeList systemNodelist = null, msgFilterList = null, dbColumns = null, dbURLColumns = null;
        SystemSetModel setModel = null;
        MsgFilterModel msgFilter = null;
        Element tempSet = null, tempMsgEle = null, dbColumn = null, dbURLColumn = null;
        try {
            
            fXmlFile = new File(DoGetDelplyRootPath() + File.separator
                    + "setupDeploy.xml");

            dbFactory = DocumentBuilderFactory
                    .newInstance();

            dBuilder = dbFactory.newDocumentBuilder();

            doc = dBuilder.parse(fXmlFile);

            // optional, but recommended
            // read this -
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
//            doc.getDocumentElement().normalize();
//            System.out.println("Root element :"
//                    + doc.getDocumentElement().getNodeName());
            systemNodelist = doc.getElementsByTagName("systemSet");

            for (int i = 0; i < systemNodelist.getLength(); i++) {
                setModel = new SystemSetModel();
                tempSet = (Element) systemNodelist.item(i);
                //todo 检查如果有重复ID 应该启动服务失败配置文件有误，不能出现重复rsid
                setModel.id = tempSet.getAttribute("id");
                setModel.dbAddress = tempSet.getElementsByTagName("dbAddress").item(0)
                        .getTextContent();
                setModel.dbName = tempSet.getElementsByTagName("dbName").item(0)
                        .getTextContent();
                setModel.dbUser = tempSet.getElementsByTagName("dbUser").item(0)
                        .getTextContent();
                setModel.dbPwd = tempSet.getElementsByTagName("dbpwd").item(0)
                        .getTextContent();
                setModel.dbPort = tempSet.getElementsByTagName("dbPort").item(0)
                        .getTextContent();
//                setModel.httpPath = tempSet.getElementsByTagName("httpPath").item(0)
//                        .getTextContent();
                msgFilterList = tempSet.getElementsByTagName("pushMsg");
                tempSet = null;
                for (int j = 0; j < msgFilterList.getLength(); j++) {
                    msgFilter = new MsgFilterModel();
                    msgFilter.rsid = setModel.id;
                    tempMsgEle = (Element) msgFilterList.item(j);
                    //todo 检查如果有重复pushId 应该启动服务失败配置文件有误，不能出现重复pushId ，
                    msgFilter.pushMsgId = tempMsgEle.getAttribute("pushId");
                    msgFilter.dbTable = tempMsgEle.getElementsByTagName("dbTable").item(0).getTextContent();
                    msgFilter.sqlFilter = tempMsgEle.getElementsByTagName("sqlFilter").item(0).getTextContent();
                    msgFilter.pageSize = UtileSmart.overrideParseShort(tempMsgEle.getElementsByTagName("pageSize").item(0).getTextContent());
                    dbColumns = tempMsgEle.getElementsByTagName("dbColumn");
                    for (int k = 0; k < dbColumns.getLength(); k++) {
                        dbColumn = (Element) dbColumns.item(k);
                        if (dbColumn == null) {
                            break;
                        }
                        msgFilter.dbColumns.add(dbColumn.getTextContent());
                        dbColumn = null;
                    }
                    dbURLColumns = tempMsgEle.getElementsByTagName("dbURLColumn");
                    for (int k = 0; k < dbURLColumns.getLength(); k++) {
                        dbURLColumn = (Element) dbURLColumns.item(k);
                        if (dbURLColumn == null) {
                            break;
                        }
                        msgFilter.dbURLColumns.add(dbURLColumn.getTextContent());
                        dbURLColumn = null;
                    }
                    setModel.msgFilters.add(msgFilter);
                    msgFilter = null;
                    tempMsgEle = null;
                    dbColumns = null;
                    dbURLColumns = null;
                }
                msgFilterList = null;

                SystemSets.add(setModel);
                setModel = null;
            }
            HttpPath_Str = doc.getElementsByTagName("httpPath").item(0)
                    .getTextContent();
            httpAddress = doc.getElementsByTagName("httpAddress").item(0)
                    .getTextContent();
            httpPort = doc.getElementsByTagName("httpPort").item(0)
                    .getTextContent();
            httpTimeOut = UtileSmart.overrideParseShort(doc.getElementsByTagName("httpTimeOut").item(0)
                    .getTextContent());

            //配置http文件目录
            StringBuffer deployHttpFilePath_sb = new StringBuffer();
            deployHttpFilePath_sb.append("http://").append(GethttpAddress()).append(":").append(GethttpPort()).append("/ReviveSmartRS/FileDepot/");
            DeployHttpFilePath = deployHttpFilePath_sb.toString();
            deployHttpFilePath_sb = null;
            return true;

        } catch (Exception ex) {
            RSLogger.ErrorLogInfo(ex.getLocalizedMessage(), ex);
            RSLogger.SetUpLogInfo(String.format("readSetUp error. setUpFilePath : %s", fXmlFile) );
            throw ex;// new Exception("readSetUp error. setUpFilePath :"+fXmlFile);
           // return false;
        } finally {
            fXmlFile = null;
            dbFactory = null;
            dBuilder = null;
            doc = null;
            systemNodelist = null;
            msgFilterList = null;
            dbColumns = null;
            dbURLColumns = null;
            setModel = null;
            msgFilter = null;
            tempSet = null;
            tempMsgEle = null;
            dbColumn = null;
            dbURLColumn = null;
        }
    }

    private static void GetComputerInfo() {
        ComputerIp = NetHelper.getHostIp(NetHelper.getInetAddress());
    }

    private static final Set<SystemSetModel> SystemSets = new java.util.HashSet<>();

    private static String HttpPath_Str = null;
    private static String httpAddress = null;
    private static String httpPort = null;
    private static String ComputerIp = null;

    private static short httpTimeOut = 5;

    public static short GetHttpTimeOut() throws Exception {
        if (httpTimeOut<=0) {
            throw new Exception("have't load deployInfo.");
        }
        return httpTimeOut;
    }

    public static String GethttpAddress() throws Exception {
        if (httpAddress == null || httpAddress.isEmpty()) {
            //readSetUp();
            throw new Exception("have't load deployInfo.");
        }
        return httpAddress;
    }

    public static String GethttpPort() throws Exception {
        if (httpPort == null || httpPort.isEmpty()) {
            //readSetUp();
            throw new Exception("have't load deployInfo.");
        }
        return httpPort;
    }

    public static Set<SystemSetModel> GetSystemSets() throws Exception {
        if (SystemSets == null || SystemSets.isEmpty()) {
            //readSetUp();
            throw new Exception("have't load deployInfo.");
        }
        return SystemSets;
    }

    public static SystemSetModel GetSystemSetsByID(String id) throws Exception {
        if (SystemSets == null || SystemSets.isEmpty()) {
            //readSetUp();
            throw new Exception("have't load deployInfo.");
        }
        for (SystemSetModel SystemSet : SystemSets) {
            if (SystemSet.id.equals(id)) {
                return SystemSet;
            }
        }
        return null;
    }

    private static String GetHttpPath() throws Exception {
        if (HttpPath_Str == null || HttpPath_Str.isEmpty()) {
            //readSetUp();
            throw new Exception("have't load deployInfo.");
        }
        return HttpPath_Str;
    }

    public static String GetComputerIp() throws Exception {
        if (ComputerIp == null || ComputerIp.isEmpty()) {
            GetComputerInfo();
            throw new Exception("have't load ComputerInfo.");
        }
        return ComputerIp;
    }

//	public static String DeployRootPath = "c:/temp";new File("").getAbsolutePath()
    public static String DeployRootPath = null;

    //加载本地目录，并返回
    private static String DoGetDelplyRootPath() {
        StringBuffer sb = new StringBuffer();
        DeployRootPath = DeployInfo.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        DeployRootPath = DeployRootPath.substring(1, DeployRootPath.indexOf("WEB-INF"));
        DeployRootPath = sb.append(File.separator).append(DeployRootPath).toString();
        sb.delete(0, sb.length());
        DeployFilePath = sb.append(DeployRootPath).append("FileDepot").toString();// DeployRootPath + "FileDepot";
        sb.delete(0, sb.length());
        DeployLogPath = sb.append(DeployRootPath).append("Log").toString();//DeployRootPath + "Log";
        sb.delete(0, sb.length());
        sb = null;
        return DeployRootPath;
    }

    public static String GetDelplyRootPath() throws Exception {
        if (DeployRootPath == null || DeployRootPath.isEmpty()) {
            throw new Exception("have't load DelplyRootPath.");
        }
        return DeployRootPath;
    }

    private static String DeployFilePath = null;

    public static String GetDeployFilePath() throws Exception {
        if (DeployFilePath == null || DeployFilePath.isEmpty()) {
            throw new Exception("have't load DeployFilePath.");
        }
        return DeployFilePath;
    }

    private static String DeployLogPath = null;

    public static String GetDeployLogPath() throws Exception {
        if (DeployLogPath == null || DeployLogPath.isEmpty()) {
            throw new Exception("have't load DeployLogPath.");
        }
        return DeployLogPath;
    }

    // public static String DeployHttpFilePath = GetHttpPath() + "FileDepot/";
    public static String DeployHttpFilePath = null;

    public static String GetDeployHttpFilePath() throws Exception {
        if (DeployHttpFilePath == null || DeployHttpFilePath.isEmpty()) {
            throw new Exception("have't load DeployHttpFilePath.");
        }
        return DeployHttpFilePath;
    }

	// rootPath+= File.separator+"webapps";
    // System.out.println("webappsPath :"+rootPath);
}
