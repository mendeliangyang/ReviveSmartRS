/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.sql.Connection;
import java.sql.Statement;

/**
 *
 * @author Administrator
 */
public interface IDBHelper {
    
    /**
     *
     * @param pId
     * @return
     * @throws Exception
     */
    public Connection ConnectSybase(String pId) throws Exception;
    
    /**
     *
     * @param stmt
     * @param connection
     */
    public void CloseConnection(Statement stmt, Connection connection);
    
    
    
}
