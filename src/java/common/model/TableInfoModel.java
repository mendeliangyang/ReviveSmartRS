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
    public TableDetailModel tbPrimaryKey;
    public String tbName;
    public String tbId;
    public Set<TableDetailModel> tableDetails = new HashSet<>();

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

    }
}
