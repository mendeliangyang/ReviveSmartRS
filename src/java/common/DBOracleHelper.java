/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author Administrator
 */
public class DBOracleHelper {
    
    private static Connection ASAConnect(String UserID, String Password, String Machinename, String DBName, String dbPort) {
        // uses global Connection variable

        //String _coninfo = Machinename;
        StringBuffer temp = null;
        // Load the Sybase Driver
        try {
            Properties _props = new Properties();
            _props.put("user", UserID);
            _props.put("password", Password);
            Class.forName("com.sybase.jdbc3.jdbc.SybDriver").newInstance();
            temp = new StringBuffer();
            // Use the Sybase jConnect driver...
            temp.append("jdbc:sybase:Tds:");
            // to connect to the supplied machine name...
            temp.append(Machinename);
            // on the default port number for ASA...
            temp.append(":");
            temp.append(dbPort);
            temp.append("/");
            //temp.append(":5000/");
            temp.append(DBName);
            temp.append("?ServiceName=");
            temp.append(DBName);
            // and connect.
            RSLogger.LogInfo(temp.toString());
            return DriverManager.getConnection(temp.toString(), _props);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e) {
            //e.printStackTrace();
            RSLogger.ErrorLogInfo("get db connection error." + e.getMessage());
            return null;
        } finally {
            temp = null;
        }
    }
    
}
