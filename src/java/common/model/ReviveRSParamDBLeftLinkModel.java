/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.model;

import java.util.List;

/**
 *
 * @author Administrator
 */
public class ReviveRSParamDBLeftLinkModel {

    public String dbll_tableName; //关联的表名
    public String dbll_sourceCol; //关联主表的列明
    public String dbll_linkCol;//关联从表的列明
    public List<String> dbll_linkSeekCol;//查询从表的列明

}
