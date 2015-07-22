/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

public class RSLogger {

    public static void Initial() throws Exception {
        //new RSLogger().RSLoggerInitial();

        log4jInitialize.initializeLog4j();

    }

    public static void LogInfo(String logMsg) {
        log4jInitialize.NormalLog(logMsg);
    }

    /**
     * 错误日志记录
     *
     * @param logMsg 错误信息
     */
    public static void ErrorLogInfo(String logMsg) {
        log4jInitialize.ErrorLog(logMsg);
    }

    /**
     * 错误日志记录
     *
     * @param strExecute string参数
     * @param exception 异常信息
     */
    public static void ErrorLogInfo(String strExecute, Exception exception) {
        StringBuffer sbLog = new StringBuffer();
        sbLog.append("strParam:").append(strExecute);
        if (exception != null) {
            StackTraceElement[] trace = exception.getStackTrace();
            for (StackTraceElement tempTrace : trace) {
                sbLog.append("\r\n").append(tempTrace);
            }
        }
        log4jInitialize.ErrorLog(sbLog.toString());
        sbLog.delete(0, sbLog.length());
        sbLog = null;
    }

    /**
     * websocket 错误日志
     *
     * @param logMsg
     */
    public static void wsErrorLogInfo(String logMsg) {
        log4jInitialize.WSErrorLog(logMsg);
    }

    /**
     * websocket 错误日志
     *
     * @param logMsg
     * @param exception
     */
    public static void wsErrorLogInfo(String logMsg, Exception exception) {
        StringBuffer sbLog = new StringBuffer();
        sbLog.append("errorMessage:").append(logMsg);
        if (exception != null) {
            StackTraceElement[] trace = exception.getStackTrace();
            for (StackTraceElement tempTrace : trace) {
                sbLog.append("\r\n").append(tempTrace);
            }
        }
        wsErrorLogInfo(sbLog.toString());
        sbLog.delete(0, sbLog.length());
        sbLog = null;
    }

    public static void SetUpLogInfo(String logMsg) {
        log4jInitialize.DeployLog(logMsg);
    }

}
