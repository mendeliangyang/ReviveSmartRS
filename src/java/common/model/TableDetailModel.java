/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.model;

/**
 *
 * @author Administrator
 */
public class TableDetailModel {
    public TableDetailModel(){}
    public TableDetailModel(String pName,String pType,String pStatus,String pUsertype,String ptbId){
        this.name = pName;
        this.type = pType;
        this.status = pStatus;
        this.usertype = pUsertype;
        this.tbId = ptbId;
        this.isPrimaryKey=false;
    }
    public TableDetailModel(String pName,String pType,String pStatus,String pUsertype,String ptbId,boolean pIsPrimary){
        this.name = pName;
        this.type = pType;
        this.status = pStatus;
        this.usertype = pUsertype;
        this.tbId = ptbId;
        this.isPrimaryKey=pIsPrimary;
    }
    public String name;
    public String type;
    public String status;
    public String usertype;
    public String tbId;
    public String dataLength;
    public DataBaseTypeEnum dataType;
    public String strDataType;
    public boolean isPrimaryKey;
}

