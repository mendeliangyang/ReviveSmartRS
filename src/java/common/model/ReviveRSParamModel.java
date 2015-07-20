/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.model;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author Administrator
 */
public class ReviveRSParamModel {

    public String token;//登录凭证

    public String rsid; //rsid
    public String handle; //base64 或者上传，指定 数据 insert，或者update
    public String db_tableName; //需要操作的表名称

    public String pkValue; //查询，删除，修改，删除 如果使用pkValue
    public Map<String, String> pkValues;// 如果是多列，使用 pkValues替代

    public String fileColumn; //base64，或者上传文件指定保存文件的列名称

    public String sql; //查询，sql条件

    public short db_pageSize = -1; //查询，传入分页大小 与db_pageNum一起使用

    public int db_pageNum = -1;//查询，分页页码 与db_pageSize一起使用

    public int db_skipNum = -1; //查询，跳过多少条数据查询 与db_topNum一起使用

    public short db_topNum = -1; //查询，查询多少条数据 与db_skipNum一起使用

    public String db_orderBy; //查询，需要指定的排序

    public Set<String> db_columns;//查询是需要用指定查询的columns

    public Set<String> db_RULcolumns;//查询是需要用指定查询的RULcolumns

    public Map<String, String> db_valueColumns;//添加，指定列名称和值  ，修改，指定修改的列和值

    public Map<String, String> db_valueFilter;//修改，安装列值来修改数据，删除，安装列值来删除数据

    public void destroySelf() {
        this.db_RULcolumns = null;
        this.db_columns = null;
        this.db_orderBy = null;
        this.db_tableName = null;
        this.db_valueColumns = null;
        this.db_valueFilter = null;
        this.fileColumn = null;
        this.handle = null;
        this.pkValue = null;
        this.rsid = null;
        this.sql = null;
        this.pkValues = null;
    }

}
