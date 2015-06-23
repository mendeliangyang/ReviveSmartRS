/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.model;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Administrator
 */
public class SystemSetModel {
    public SystemSetModel(){}
    
    public SystemSetModel(String pdbAddress,String pdbName,String phttpPath,String pid,String pdbUser,String pdbPwd,String pdbPort){
        this.dbAddress = pdbAddress;
        this.dbName = pdbName;
        this.httpPath = phttpPath;
        this.id = pid;
        this.dbUser = pdbUser;
        this.dbPwd = pdbPwd;
        this.dbPort = pdbPort;
    }
    public String dbAddress;
    public String dbName;
    public String httpPath;
    public String id;
    public String dbUser;
    public String dbPwd;
    public String dbPort;
    
    public Set<MsgFilterModel> msgFilters=new HashSet<>();
}
