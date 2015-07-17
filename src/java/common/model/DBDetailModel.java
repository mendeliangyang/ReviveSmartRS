/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.model;

import java.util.Set;

/**
 *
 * @author Administrator
 */
public class DBDetailModel {

    public DBDetailModel() {
    }

    public DBDetailModel(String pdbName, String prsId, Set<TableInfoModel> pTableInfos) {
        this.dbName = pdbName;
        this.rsId = prsId;
        this.dbTableInfos = pTableInfos;
    }
    public String dbName;
    public String rsId;
    public Set<TableInfoModel> dbTableInfos;

    public void clear() {
        dbName = null;
        rsId = null;
        if (dbTableInfos != null) {
            for (TableInfoModel dbTableInfo : dbTableInfos) {
                dbTableInfo.clear();
                dbTableInfo = null;
            }
            dbTableInfos.clear();
        }
    }

}
