/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class RSLogger {

    private static Logger dealLog = null;
    private static Logger errLog = null;
    private static Logger setLog = null;

    public static void LogInfo(String logMsg) {
        if (dealLog == null) {
            new RSLogger().RSLoggerInitial();
        }
        if (dealLog != null) {
            //System.out.println(logMsg);
            //dealLog.log(Level.INFO, logMsg);
        }
    }
    
    public static void ErrorLogInfo(String logMsg){
        if (errLog == null) {
            new RSLogger().RSLoggerInitial();
        }
        if (errLog != null) {
            System.err.println(logMsg);
            errLog.log(Level.INFO, logMsg);
        }
    }
    
    public static void SetUpLogInfo(String logMsg){
          if (setLog == null) {
            new RSLogger().RSLoggerInitial();
        }
        if (setLog != null) {
            System.out.println(logMsg);
            setLog.log(Level.INFO, logMsg);
        }
    }

    public void RSLoggerInitial() {

        dealLog = Logger.getLogger("dealLog");
        errLog = Logger.getLogger("errorLog");
        setLog =Logger.getLogger("setupLog");
        setLog.setLevel(Level.ALL);
        errLog .setLevel(Level.ALL);
        dealLog.setLevel(Level.INFO);

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        dealLog.addHandler(consoleHandler);
        ConsoleHandler errorHandler = new ConsoleHandler();
        errorHandler.setLevel(Level.ALL);
        errLog.addHandler(errorHandler);
        ConsoleHandler setHandler = new ConsoleHandler();
        setHandler.setLevel(Level.ALL);
        setLog.addHandler(setHandler);
                
        FileHandler fileHandler = null;
        FileHandler errFileHandler = null;
        FileHandler setFileHandler = null;
        try {

//			Date nowDate = new Date(); 
//			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH");
//			fileHandler = new FileHandler(DeployInfo.DeployLogPath+"/RSlog"+dateFormat.format( nowDate )+".log");
            fileHandler = new FileHandler(DeployInfo.GetDeployLogPath() + File.separator+"RSlog.log");
            errFileHandler = new FileHandler(DeployInfo.GetDeployLogPath() + File.separator+"ErrRSlog.log");
            setFileHandler = new FileHandler(DeployInfo.GetDeployLogPath() + File.separator+"SetupLog.log");
            
        } catch (Exception e) {
            Logger.getLogger(RSLogger.class.getName()).log(Level.SEVERE, null, e);
        } 
        errFileHandler.setLevel(Level.ALL);
        errFileHandler.setFormatter(new MyLogHander());
        errLog.addHandler(errFileHandler);
        fileHandler.setLevel(Level.INFO);
        fileHandler.setFormatter(new MyLogHander());
        dealLog.addHandler(fileHandler);
        setFileHandler.setLevel(Level.ALL);
        setFileHandler.setFormatter(new MyLogHander());
        setLog.addHandler(setFileHandler);

    }

    class MyLogHander extends Formatter {

        @Override
        public String format(LogRecord record) {
            Date now = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String nowTime = dateFormat.format(now);

            return "\r\n\r\n\r\n\r\n" + record.getLevel() + nowTime + "\r\n" + record.getMessage() + "\r\n";

        }
    }
}
