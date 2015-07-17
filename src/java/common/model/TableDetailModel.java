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

    public TableDetailModel() {
    }

    public TableDetailModel(String pName, String pType, String pStatus, String pUsertype, String ptbId) {
        this.name = pName;
        this.type = pType;
        this.status = pStatus;
        this.usertype = pUsertype;
        this.tbId = ptbId;
        this.isPrimaryKey = false;
    }

    public TableDetailModel(String pName, String pType, String pStatus, String pUsertype, String ptbId, boolean pIsPrimary) {
        this.name = pName;
        this.type = pType;
        this.status = pStatus;
        this.usertype = pUsertype;
        this.tbId = ptbId;
        this.isPrimaryKey = pIsPrimary;
    }
    public String name; //名字
    public String type;//类型
    public String status;//状态
    public String usertype;//类型
    public String tbId;//表id，按照数据库的表id
    public String dataLength;//数据长度
    public DataBaseTypeEnum dataType;//数据类型对应java
    public String strDataType;//
    public boolean isPrimaryKey;//标识该列是不是主键列

    public void clear() {
        name = null;
        type = null;
        status = null;
        usertype = null;
        tbId = null;
        dataLength = null;
        dataType = null;
        strDataType = null;
    }
}
