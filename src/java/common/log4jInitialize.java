/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import org.apache.log4j.Logger;

/**
 *
 * @author Administrator
 */
public class log4jInitialize {

    private static Logger log4jErrLog = null;
    private static Logger log4jNormalLog = null;
    private static Logger log4jDeployLog = null;
    private static Logger log4jWSErrLog = null;

    public static void initializeLog4j() throws Exception {
        //读取配置文件
        System.setProperty("log4jdir", String.format("%s", DeployInfo.GetDeployLogPath()));
        org.apache.log4j.PropertyConfigurator.configure(String.format("%s%s", DeployInfo.GetDelplyRootPath(), "log4j.properties"));

        log4jErrLog = Logger.getLogger("rsErrorLog");
        log4jNormalLog = Logger.getLogger("rsNormalLog");
        log4jDeployLog = Logger.getLogger("rsDeployLog");
        log4jWSErrLog = Logger.getLogger("wsErrorLog");

    }

    public static void WSErrorLog(String logMsg) {
        log4jWSErrLog.error(logMsg);
    }

    public static void ErrorLog(String logMsg) {
        log4jErrLog.error(logMsg);
    }

    public static void NormalLog(String logMsg) {
        log4jNormalLog.info(logMsg);
    }

    public static void DeployLog(String logMsg) {
        log4jDeployLog.info(logMsg);
    }
}
