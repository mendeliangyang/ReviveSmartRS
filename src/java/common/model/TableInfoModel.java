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
public class TableInfoModel {

    public TableInfoModel() {
    }

    public TableInfoModel(String ptbName, String ptbId, Set<TableDetailModel> ptableDetails) {
        this.tbName = ptbName;
        this.tbId = ptbId;
        this.tableDetails = ptableDetails;
    }

    public String tbName;
    public String tbId;
    public Set<TableDetailModel> tableDetails = new HashSet<>();
    //public TableDetailModel tbPrimaryKey;
    //主键集合方便操作 ，如果集合count=1 表示单列主键，count<1表示多列主键
    public Set<TableDetailModel> tbPrimaryKeys = new HashSet<>();
    //identitykeys
    public TableDetailModel identityKey = null;

    /**
     * get column data Type by column name
     *
     * @param pColumn column name
     * @return
     */
    public DataBaseTypeEnum getColumnDataType(String pColumn) {
        if (tableDetails != null) {
            for (TableDetailModel tableDetail : tableDetails) {
                if (tableDetail.name.equals(pColumn)) {
                    return tableDetail.dataType;
                }
            }
        }
        return null;
    }

    /**
     * if single primary column return primary column detail 
     * else not single column reutn null
     *
     * @return primary column detail
     */
    public TableDetailModel getPrimariyColumn() {
        if (tbPrimaryKeys == null) {
            return null;
        }
        if (tbPrimaryKeys.size() != 1) {
            return null;
        }
        for (TableDetailModel tbPrimaryKey : tbPrimaryKeys) {
            if (tbPrimaryKey.isPrimaryKey) {
                return tbPrimaryKey;
            }
        }
        return null;
    }

    /**
     * whether column is primarykey
     *
     * @param pColumn column name
     * @return
     */
    public boolean CheckColumnIsPrimary(String pColumn) {
        if (tbPrimaryKeys == null) {
            return false;
        }
        for (TableDetailModel tableDetail : tbPrimaryKeys) {
            if (tableDetail.name.equals(pColumn) && tableDetail.isPrimaryKey) {
                return true;
            }
        }
        return false;
    }

    /**
     * get column detail by column name
     *
     * @param pColumn column name
     * @return TableDetailModel
     */
    public TableDetailModel getColumnDetail(String pColumn) {
        if (tableDetails == null) {
            return null;
        }
        for (TableDetailModel tableDetail : tableDetails) {
            if (tableDetail.name.equals(pColumn)) {
                return tableDetail;
            }
        }
        return null;
    }

    public void clear() {
        tbName = null;
        tbId = null;
        if (tableDetails != null) {
            for (TableDetailModel tableDetail : tableDetails) {
                tableDetail.clear();
                tableDetail = null;
            }
            tableDetails.clear();
        }
        if (tbPrimaryKeys != null) {
            for (TableDetailModel tempPrimary : tbPrimaryKeys) {
                tempPrimary.clear();
                tempPrimary = null;
            }
            tbPrimaryKeys.clear();
        }

    }
}
