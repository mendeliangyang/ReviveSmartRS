/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import common.model.SystemSetModel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Administrator
 */
public class DBOracleHelper {

    //todo oracleHelper
    private static final String jdbcOracleDriverName = "oracle.jdbc.driver.OracleDriver";

    private Connection ASAConnect(String UserID, String Password, String Machinename, String DBName, String dbPort) {
        StringBuffer temp = null;
        // Load the Sybase Driver
        try {
            //rl="jdbc:oracle:thin:@localhost:1521:orcl"; 
            Class.forName(jdbcOracleDriverName);
            temp = new StringBuffer();
            temp.append("jdbc:oracle:thin:@");
            temp.append(Machinename);
            temp.append(":");
            temp.append(dbPort);
            temp.append(":");
            temp.append(DBName);
            // and connect.
            return DriverManager.getConnection(temp.toString(), UserID, Password);
        } catch (ClassNotFoundException | SQLException e) {
            RSLogger.ErrorLogInfo("get db connection error." + e.getMessage());
            return null;
        } finally {
            temp = null;
        }
    }

    public Connection ConnectSybase(String pId) throws Exception {

        SystemSetModel setModel = DeployInfo.GetSystemSetsByID(pId);
        if (setModel == null) {
            RSLogger.ErrorLogInfo("Could not find the db connection.");
            return null;
        }
        return this.ASAConnect(setModel.dbUser, setModel.dbPwd, setModel.dbAddress, setModel.dbName, setModel.dbPort);
    }

}
