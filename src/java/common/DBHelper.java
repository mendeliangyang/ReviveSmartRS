/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

//Import the necessary classes
import common.model.ExecuteResultParam;
import common.model.DataBaseTypeEnum;
import common.model.DBDetailModel;
import common.model.ReviveRSParamModel;
import common.model.TableDetailModel;
import common.model.SystemSetModel;
import common.model.TableInfoModel;
import java.sql.*; // JDBC

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties; // Properties
import java.util.Set;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class DBHelper {

    private static Connection ConnectSybase() {
        return DBHelper.ASAConnect("sa", "123456", "192.168.169.217", "AustraliaBank", "5000");

        //return DBHelper.ASAConnect("sa", "123456",DeployInfo.GetDBAddress(),DeployInfo.GetDBname());
    }

    public static Connection ConnectSybase(String pId) throws Exception {
//        return DBHelper.ASAConnect("sa", "123456", "192.168.169.217", "AustraliaBank");
        SystemSetModel setModel = DeployInfo.GetSystemSetsByID(pId);
        if (setModel == null) {
            RSLogger.ErrorLogInfo("Could not find the db connection.");
            return null;
        }
        return DBHelper.ASAConnect(setModel.dbUser, setModel.dbPwd, setModel.dbAddress, setModel.dbName, setModel.dbPort);
    }

    public static void CloseConnection(Statement stmt, Connection connection) {
        try {
            if (stmt != null) {
                stmt.close();
                stmt = null;
            }
        } catch (SQLException e) {
            RSLogger.ErrorLogInfo("CloseConnection stmt" + e.getLocalizedMessage());
        }
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            RSLogger.ErrorLogInfo("CloseConnection connection" + e.getLocalizedMessage());
        } finally {
            stmt = null;
            connection = null;
        }
    }

    public static void CloseConnection(ResultSet resultSet, Statement stmt, Connection connection) {
        try {
            if (resultSet != null) {
                resultSet.close();
                resultSet = null;
            }
        } catch (SQLException e) {
            RSLogger.ErrorLogInfo("CloseConnection resultSet" + e.getLocalizedMessage());
        } finally {
            CloseConnection(stmt, connection);
        }
    }

    private static void CloseConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            RSLogger.ErrorLogInfo("CloseConnection connection" + e.getLocalizedMessage());
        }
    }

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

    private static Set<TableDetailModel> SearchTableDetail(String tableName, String RSID) throws Exception {

//        StringBuffer sqlsb = new StringBuffer("SELECT sc.name,sc.type,sc.status,sc.usertype FROM syscolumns sc INNER JOIN sysobjects so ON sc.id = so.id WHERE so.name = '");
        StringBuffer sqlsb = new StringBuffer("select  o.name as tbName , c.name,c.type, c.status,c.usertype  ,t.name as dataTypeName from sysobjects o inner join syscolumns c on c.id = o.id inner join systypes t on t.usertype = c.usertype where o.type = 'U' and o.name ='");
        sqlsb.append(tableName);
        sqlsb.append("'");

        Set<TableDetailModel> tableDetail = new java.util.HashSet<>();
        TableDetailModel tempColumn = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet result = null;
        try {
            conn = DBHelper.ConnectSybase(RSID);
            stmt = conn.createStatement();
            result = stmt.executeQuery(sqlsb.toString());
            while (result.next()) {
                tempColumn = new TableDetailModel(result.getString("name"), result.getString("type"), result.getString("status"), result.getString("usertype"), "");
                tableDetail.add(tempColumn);
                tempColumn = null;
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            RSLogger.LogInfo("SearchTableDetail error" + e.getLocalizedMessage());
        } finally {
            sqlsb = null;
            DBHelper.CloseConnection(result, stmt, conn);
        }
        return tableDetail;
    }

    private static String SearchTablePrimaryKey(String tableName, String RSID) throws Exception {
//        String sqlStr = "select 'tableName' = object_name(sc.id),\n"
//                + "'columnName' = index_col(object_name(sc.id),sc.indid,1),\n"
//                + "'indexDescription' = convert(varchar(210), case when (sc.status & 16)<>0 then 'clustered' else 'nonclustered' end\n"
//                + "+ case when (sc.status & 1)<>0 then ', '+'ignore duplicate keys' else '' end\n"
//                + "+ case when (sc.status & 2)<>0 then ', '+'unique' else '' end\n"
//                + "+ case when (sc.status & 4)<>0 then ', '+'ignore duplicate rows' else '' end\n"
//                + "+ case when (sc.status & 64)<>0 then ', '+'statistics' else case when (status & 32)<>0 then ', '+'hypothetical' else '' end end\n"
//                + "+ case when (sc.status & 2048)<>0 then ', '+'primary key' else '' end\n"
//                + "+ case when (sc.status & 4096)<>0 then ', '+'unique key' else '' end\n"
//                + "+ case when (sc.status & 8388608)<>0 then ', '+'auto create' else '' end\n"
//                + "+ case when (sc.status & 16777216)<>0 then ', '+'stats no recompute' else '' end),\n"
//                + "'indexName' = name\n"
//                + "from sysindexes sc where  (sc.status & 64) = 0 \n"
//                + "order by sc.id";

        Connection conn = null;
        Statement stmt = null;
        ResultSet result = null;
        try {
            conn = DBHelper.ConnectSybase(RSID);
            stmt = conn.createStatement();
            result = stmt.executeQuery(SearchTablePrimaryKeyStr);
            while (result.next()) {
                if (tableName.equals(result.getString("tableName")) && result.getString("indexDescription").indexOf("primary key") > 0) {
                    return result.getString("columnName");
                }
            }
        } catch (SQLException e) {
            // e.printStackTrace();
            RSLogger.ErrorLogInfo("get table primary key error:" + e.getMessage());
        } finally {
            DBHelper.CloseConnection(result, stmt, conn);
        }
        return null;

    }

    private static String SearchTableIdentityKey(String tableName, String RSID) throws Exception {
//        String sqlStr = "select object_name(sc.id) as 'tn' , sc.name as 'cn' from syscolumns sc inner join sysobjects so on sc.id = so.id where  so.name='" + tableName + "' and  sc.status =128 ";
        Connection conn = null;
        Statement stmt = null;
        ResultSet result = null;
        try {
            conn = DBHelper.ConnectSybase(RSID);
            stmt = conn.createStatement();
            result = stmt.executeQuery(String.format(SearchTableIdentityKeyStr, tableName));
            if (result.next()) {
                return result.getString("cn");
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            RSLogger.ErrorLogInfo("SearchTableIdentityKey error" + e.getMessage());
        } finally {
            DBHelper.CloseConnection(result, stmt, conn);
        }
        return null;
    }

    private static String FindTableIdentityKey(Set<TableDetailModel> tableDetails) {
        if (tableDetails == null || tableDetails.isEmpty()) {
            return null;
        }
        for (TableDetailModel tableDetail : tableDetails) {
            if (tableDetail.status.equals("128")) {
                return tableDetail.name;
            }
        }
        return null;
    }

    /**
     * 根据传入数据生成插入语句
     *
     *
     * @param paramModel
     * @return
     */
    public static String SqlInsertFactory(ReviveRSParamModel paramModel) throws Exception {
        StringBuffer tempSql = new StringBuffer();
        StringBuffer tempColumn = new StringBuffer();
        StringBuffer tempValue = new StringBuffer();
        String identityKeyName = null;
        try {
            Set tableIterator = FindTableDetail(paramModel.db_tableName, paramModel.rsid);

            identityKeyName = FindTableIdentityKey(tableIterator);
            if (identityKeyName != null) {
                tempSql.append("SET NOCOUNT ON ");
            }

            tempSql.append(" INSERT INTO ").append(paramModel.db_tableName).append("( ");

            Iterator keys = paramModel.db_valueColumns.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                tempColumn.append(key).append(" ,");
                DataBaseTypeEnum colunmType = GetColumnType(tableIterator, key);
                if (colunmType == DataBaseTypeEnum.number || colunmType == DataBaseTypeEnum.decimal) {
                    tempValue.append(" ").append(paramModel.db_valueColumns.get(key)).append(" ,");
                } else if (colunmType == DataBaseTypeEnum.charset || colunmType == DataBaseTypeEnum.date || colunmType == DataBaseTypeEnum.time || colunmType == DataBaseTypeEnum.datetime) {
                    tempValue.append("'").append(paramModel.db_valueColumns.get(key)).append("' ,");
                } else {
                    throw new Exception("error: columnTypeUnknown  key." + key);
                }
                key = null;
            }
            tempSql.append(tempColumn.substring(0, tempColumn.length() - 1)).append(" ) VALUES (");
            tempSql.append(tempValue.substring(0, tempValue.length() - 1)).append(")");

            if (identityKeyName != null) {
                tempSql.append(" SELECT @@IDENTITY AS ").append(identityKeyName);
            }

            RSLogger.LogInfo(tempSql.toString());
            return tempSql.toString();
        } catch (Exception e) {
            RSLogger.ErrorLogInfo("SqlInsertFactory error." + e.getMessage());
            throw new Exception("SqlInsertFactory error." + e.getMessage());
        } finally {
            tempSql = null;
            tempColumn = null;
            tempValue = null;
            identityKeyName = null;
        }
    }

    /**
     * 如果 param 包含pkvalue 表示按照主键修改数据， 如果 是其他列对应数据，表示按条件修改
     *
     *
     * @param paramModel
     * @return 修改数据行数
     */
    public static String SqlUpdateFactory(ReviveRSParamModel paramModel) throws Exception {
        StringBuffer sqlsb = new StringBuffer();
        try {
            sqlsb.append("UPDATE ").append(paramModel.db_tableName).append(" SET ");
            Set tableIterator = FindTableDetail(paramModel.db_tableName, paramModel.rsid);

            Iterator keys = paramModel.db_valueColumns.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                DataBaseTypeEnum colunmType = GetColumnType(tableIterator, key);
                sqlsb.append(key).append("= ");
                if (colunmType == DataBaseTypeEnum.number || colunmType == DataBaseTypeEnum.decimal) {
                    sqlsb.append(" ").append(paramModel.db_valueColumns.get(key)).append(" ,");
                } else if (colunmType == DataBaseTypeEnum.charset || colunmType == DataBaseTypeEnum.date || colunmType == DataBaseTypeEnum.time || colunmType == DataBaseTypeEnum.datetime) {
                    sqlsb.append("'").append(paramModel.db_valueColumns.get(key)).append("'  ,");
                } else {
                    throw new Exception("error: columnTypeUnknown  key." + key);
                }
            }
            String tempSql = sqlsb.substring(0, sqlsb.length() - 1);
            sqlsb.delete(0, sqlsb.length());
            sqlsb.append(tempSql);
            sqlsb.append(" WHERE ");

            if (paramModel.pkValue != null && !paramModel.pkValue.isEmpty()) {
                //获取主键
                String tablePrimary = SearchTablePrimaryKey(paramModel.db_tableName, paramModel.rsid);
                if (tablePrimary == null || tablePrimary.equals("")) {
                    throw new Exception("error: primaryKey not find." + tablePrimary);
                }
                sqlsb.append(tablePrimary).append("=");
                DataBaseTypeEnum colunmType = GetColumnType(tableIterator, tablePrimary);
                if (colunmType == DataBaseTypeEnum.number || colunmType == DataBaseTypeEnum.decimal) {
                    sqlsb.append(" ").append(paramModel.pkValue).append(" ");
                } else if (colunmType == DataBaseTypeEnum.charset || colunmType == DataBaseTypeEnum.date || colunmType == DataBaseTypeEnum.time || colunmType == DataBaseTypeEnum.datetime) {
                    sqlsb.append("'").append(paramModel.pkValue).append("' ");
                } else {
                    throw new Exception("error: primaryKey unknow type." + tablePrimary);
                }
                RSLogger.LogInfo(sqlsb.toString());
                return sqlsb.toString();
            } else {
                //param不包含主键，按照传入条件进行删除
                Iterator noteIterator = paramModel.db_valueFilter.keySet().iterator();//jsonNote.keys();
                while (noteIterator.hasNext()) {
                    String key = (String) noteIterator.next();
                    //检查 key 是否有效 ，
                    if (!IsColumnExist(tableIterator, key)) {
                        continue;
                    }
                    sqlsb.append("  ").append(key).append(" = ");
                    DataBaseTypeEnum colunmType = GetColumnType(tableIterator, key);
                    if (colunmType == DataBaseTypeEnum.number || colunmType == DataBaseTypeEnum.decimal) {
                        sqlsb.append(" ").append(paramModel.db_valueFilter.get(key)).append(" ");
                    } else if (colunmType == DataBaseTypeEnum.charset || colunmType == DataBaseTypeEnum.date || colunmType == DataBaseTypeEnum.time || colunmType == DataBaseTypeEnum.datetime) {
                        sqlsb.append("'").append(paramModel.db_valueFilter.get(key)).append("' ");
                    } else {
                        throw new Exception("error: columnTypeUnknown  key." + key);
                    }
                    sqlsb.append(" and");
                }
                RSLogger.LogInfo(sqlsb.toString());
                return sqlsb.substring(0, sqlsb.lastIndexOf("and"));
            }
        } catch (Exception e) {
            RSLogger.ErrorLogInfo("SqlUpdateFactory error." + e.getLocalizedMessage());
            throw new Exception("SqlUpdateFactory error." + e.getLocalizedMessage());
        }
    }

    /**
     * 如果 param 包含pkvalue 表示按照主键删除数据， 如果 是其他列对应数据，表示按条件删除数据
     *
     *
     * @param paramModel
     * @return 修改数据行数
     * @throws java.lang.Exception
     */
    public static String SqlDeleteFactory(ReviveRSParamModel paramModel) throws Exception {
        StringBuffer sqlSb = new StringBuffer();
        try {
            sqlSb.append("DELETE ").append(paramModel.db_tableName).append(" WHERE ");
            Set tableIterator = FindTableDetail(paramModel.db_tableName, paramModel.rsid);

            //判断是否存在主键,存在主键安装主键删除数据
            if (paramModel.pkValue != null && !paramModel.pkValue.isEmpty()) {
                //获取主键
                String tablePrimary = SearchTablePrimaryKey(paramModel.db_tableName, paramModel.rsid);
                if (tablePrimary == null || tablePrimary.equals("")) {
                    throw new Exception("error: primaryKey not find." + tablePrimary);
                }
                sqlSb.append(tablePrimary).append("=");
                DataBaseTypeEnum colunmType = GetColumnType(tableIterator, tablePrimary);
                if (colunmType == DataBaseTypeEnum.number || colunmType == DataBaseTypeEnum.decimal) {
                    sqlSb.append(" ").append(paramModel.pkValue).append(" ");
                } else if (colunmType == DataBaseTypeEnum.charset || colunmType == DataBaseTypeEnum.date || colunmType == DataBaseTypeEnum.time || colunmType == DataBaseTypeEnum.datetime) {
                    sqlSb.append("'").append(paramModel.pkValue).append("' ");
                } else {
                    throw new Exception("error: primaryKey unknow type." + tablePrimary);
                }
                RSLogger.LogInfo(sqlSb.toString());
                return sqlSb.toString();
            } else {
                //param不包含主键，按照传入条件进行删除
                Iterator keys = paramModel.db_valueFilter.keySet().iterator(); //jsonNote.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    //检查 key 是否有效 ，
                    if (!IsColumnExist(tableIterator, key)) {
                        continue;
                    }
                    sqlSb.append("  ").append(key).append(" = ");
                    DataBaseTypeEnum colunmType = GetColumnType(tableIterator, key);
                    if (colunmType == DataBaseTypeEnum.number || colunmType == DataBaseTypeEnum.decimal) {
                        sqlSb.append(" ").append(paramModel.db_valueFilter.get(key)).append(" ");
                    } else if (colunmType == DataBaseTypeEnum.charset || colunmType == DataBaseTypeEnum.date || colunmType == DataBaseTypeEnum.time || colunmType == DataBaseTypeEnum.datetime) {
                        sqlSb.append("'").append(paramModel.db_valueFilter.get(key)).append("' ");
                    } else {
                        throw new Exception("error: columnTypeUnknown  key." + key);
                    }
                    sqlSb.append(" and");
                }
                RSLogger.LogInfo(sqlSb.toString());
                return sqlSb.substring(0, sqlSb.lastIndexOf("and"));
            }
        } catch (Exception e) {
            RSLogger.ErrorLogInfo("SqlDeleteFactory error." + e.getLocalizedMessage());
            throw new Exception("SqlDeleteFactory error." + e.getLocalizedMessage());
        }
    }

    /**
     * 如果 param 包含pkvalue 表示按照主键删除数据， 其次根据 sql中的sql语句查询 是其他列对应数据，表示按条件删除数据 根据
     * note 中其列的值自动生成查询条件
     *
     *
     * @param paramModel
     * @return
     * @throws java.lang.Exception
     */
    public static String SqlSelectFactory(ReviveRSParamModel paramModel) throws Exception {
        Set tableIterator = null;
        Set<String> columnsName = null;
        Iterator tableDetailIterator = null;
        String tempSql = null, linkTerm = null, tablePrimary = null;
        DataBaseTypeEnum colunmType = null;
        StringBuilder sqlsb = null;
        try {
            sqlsb = new StringBuilder();
            sqlsb.append("SELECT ");
            tableIterator = FindTableDetail(paramModel.db_tableName, paramModel.rsid);

            if (paramModel.db_columns != null && !paramModel.db_columns.isEmpty()) {
                columnsName = paramModel.db_columns;
            } else {
                tableDetailIterator = tableIterator.iterator();
                columnsName = getColumnsName(tableDetailIterator);
            }
            for (String nextColumnName : columnsName) {
                if (isExistMember(paramModel.db_RULcolumns, nextColumnName)) {
                    sqlsb.append("STR_REPLACE(");
                    sqlsb.append(nextColumnName);
                    sqlsb.append(",'|','|");
                    sqlsb.append(DeployInfo.GetDeployHttpFilePath());
                    sqlsb.append("')");
                    sqlsb.append(" as ");
                    sqlsb.append(nextColumnName);
                } else {
                    sqlsb.append(nextColumnName);
                }
                sqlsb.append(" ,");
            }
            columnsName = null;
            tempSql = sqlsb.substring(0, sqlsb.length() - 1);
            sqlsb.delete(0, sqlsb.length());
            sqlsb.append(tempSql);
            tempSql = null;
            sqlsb.append(" FROM ");
            sqlsb.append(paramModel.db_tableName);

            linkTerm = " WHERE ";

            if (paramModel.pkValue != null && !paramModel.pkValue.isEmpty()) {

                sqlsb.append(linkTerm);
                linkTerm = " AND ";
                //获取主键
                tablePrimary = SearchTablePrimaryKey(paramModel.db_tableName, paramModel.rsid);
                if (tablePrimary == null || tablePrimary.equals("")) {
                    //return "primaryKey not find";
                    throw new Exception("error: primaryKey not find." + tablePrimary);
                }
                sqlsb.append(tablePrimary).append("=");
                colunmType = GetColumnType(tableIterator, tablePrimary);
                if (colunmType == DataBaseTypeEnum.number || colunmType == DataBaseTypeEnum.decimal) {
                    sqlsb.append(" ").append(paramModel.pkValue).append(" ");
                } else if (colunmType == DataBaseTypeEnum.charset || colunmType == DataBaseTypeEnum.date || colunmType == DataBaseTypeEnum.time || colunmType == DataBaseTypeEnum.datetime) {
                    sqlsb.append("'").append(paramModel.pkValue).append("' ");
                } else {
                    //sqlsb.append("columnTypeUnknown");
                    throw new Exception("error: columnTypeUnknown ." + tablePrimary);
                }
                RSLogger.LogInfo(sqlsb.toString());
                return sqlsb.toString();
            }

            if (paramModel.sql != null && !paramModel.sql.isEmpty()) {
                sqlsb.append(linkTerm).append(paramModel.sql);
            }
            if (paramModel.db_orderBy != null && !paramModel.db_orderBy.isEmpty()) {
                sqlsb.append(" order by ");
                sqlsb.append(paramModel.db_orderBy);
            }

            RSLogger.LogInfo("sql: " + sqlsb.toString());
            return sqlsb.toString();
        } catch (Exception e) {
            throw new Exception("SqlSelectFactory error:" + e.getLocalizedMessage());
        } finally {
            UtileSmart.FreeObjects(tableIterator, columnsName, tableDetailIterator, tableDetailIterator, tempSql, linkTerm, tablePrimary, colunmType, sqlsb);
        }

    }

    public static String SqlSelectPageFactory(ReviveRSParamModel paramModel) throws Exception {
        Set tableIterator = null;
        String identityKeyName = null, linkTerm = null, tablePrimary = null;
        StringBuffer sqlsb = null;
        Set<String> columnsName = null;
        Iterator tableDetailIterator = null;
        DataBaseTypeEnum colunmType = null;
        try {
            tableIterator = FindTableDetail(paramModel.db_tableName, paramModel.rsid);
            identityKeyName = FindTableIdentityKey(tableIterator);
            sqlsb = new StringBuffer();
            sqlsb.append("SELECT ");
            if (paramModel.db_columns != null && !paramModel.db_columns.isEmpty()) {
                columnsName = paramModel.db_columns;
            } else {
                tableDetailIterator = tableIterator.iterator();
                columnsName = getColumnsName(tableDetailIterator);
            }
            for (String nextColumnName : columnsName) {
                if (identityKeyName != null && identityKeyName.equals(nextColumnName)) {
                    //convert(varchar,id) id
                    sqlsb.append(" convert (varchar,");
                    sqlsb.append(nextColumnName);
                    sqlsb.append(")");
                    sqlsb.append(" as ");
                    sqlsb.append(nextColumnName);
                } else if (isExistMember(paramModel.db_RULcolumns, nextColumnName)) {
                    sqlsb.append("STR_REPLACE(");
                    sqlsb.append(nextColumnName);
                    sqlsb.append(",'|','|");
                    sqlsb.append(DeployInfo.GetDeployHttpFilePath());
                    sqlsb.append("')");
                    sqlsb.append(" as ");
                    sqlsb.append(nextColumnName);
                } else {
                    sqlsb.append(nextColumnName);
                }
                sqlsb.append(" ,");
            }

            sqlsb.append(" sybid=identity(12) into #temp FROM ");
            sqlsb.append(paramModel.db_tableName);

            linkTerm = " WHERE ";

            if (paramModel.pkValue != null && !paramModel.pkValue.isEmpty()) {

                sqlsb.append(linkTerm);
                linkTerm = " AND ";
                //获取主键
                tablePrimary = SearchTablePrimaryKey(paramModel.db_tableName, paramModel.rsid);
                if (tablePrimary == null || tablePrimary.equals("")) {
                    //return "primaryKey not find";
                    throw new Exception("error: primaryKey not find." + tablePrimary);
                }
                sqlsb.append(tablePrimary).append("=");
                colunmType = GetColumnType(tableIterator, tablePrimary);
                if (colunmType == DataBaseTypeEnum.number || colunmType == DataBaseTypeEnum.decimal) {
                    sqlsb.append(" ").append(paramModel.pkValue).append(" ");
                } else if (colunmType == DataBaseTypeEnum.charset || colunmType == DataBaseTypeEnum.date || colunmType == DataBaseTypeEnum.time || colunmType == DataBaseTypeEnum.datetime) {
                    sqlsb.append("'").append(paramModel.pkValue).append("' ");
                } else {
                    throw new Exception("error: columnTypeUnknown  key." + tablePrimary);
                }
                RSLogger.LogInfo(sqlsb.toString());
                return sqlsb.toString();
            }

            if (paramModel.sql != null && !paramModel.sql.isEmpty()) {
                sqlsb.append(linkTerm).append(paramModel.sql);
            }
            if (paramModel.db_orderBy != null && !paramModel.db_orderBy.isEmpty()) {
                sqlsb.append(" order by ");
                sqlsb.append(paramModel.db_orderBy);
            }
            int pageOffset = 0, topNum = 0;
            if (paramModel.db_pageSize != -1 && paramModel.db_pageNum != -1) {
                pageOffset = (paramModel.db_pageNum - 1) * paramModel.db_pageSize;
                topNum = pageOffset + paramModel.db_pageSize;
            } else if (paramModel.db_skipNum != -1 && paramModel.db_topNum != -1) {
                pageOffset = paramModel.db_skipNum;
                topNum = paramModel.db_topNum + paramModel.db_skipNum;
            }
            sqlsb.append(" select * from #temp where sybid> ");
            sqlsb.append(pageOffset);
            sqlsb.append(" and sybid <= ");
            sqlsb.append(topNum);
            RSLogger.LogInfo("sql: " + sqlsb.toString());
            return sqlsb.toString();
        } catch (Exception e) {
            throw new Exception("SqlSelectPageFactory error:" + e.getLocalizedMessage());
        } finally {
            UtileSmart.FreeObjects(tableIterator, identityKeyName, linkTerm, tablePrimary, sqlsb, columnsName, tableDetailIterator, colunmType);
        }

    }

    private static Set<String> getColumnsName(JSONArray JsonColunms) {
        if (JsonColunms == null || JsonColunms.isEmpty()) {
            return null;
        }
        Set<String> columnsName = new HashSet<>();
        for (int i = 0; i < JsonColunms.size(); i++) {
            columnsName.add(JsonColunms.getString(i));
        }
        return columnsName;
    }

    private static Set<String> getColumnsName(Iterator tableDetailIterator) {
        if (tableDetailIterator == null) {
            return null;
        }
        Set<String> columnsName = new HashSet<>();
        while (tableDetailIterator.hasNext()) {
            columnsName.add(((TableDetailModel) tableDetailIterator.next()).name);
        }
        return columnsName;
    }

    public static String SqlSelectCountFactory(ReviveRSParamModel paramModel) throws Exception {
        StringBuffer sqlsb = null;
        String linkTerm = null;
        try {
            sqlsb = new StringBuffer();
            sqlsb.append("SELECT COUNT(*) as rowsCount FROM ");
            sqlsb.append(paramModel.db_tableName);
            linkTerm = " WHERE ";
            if (paramModel.sql != null && !paramModel.sql.isEmpty()) {
                sqlsb.append(linkTerm).append(paramModel.sql);
            }
            return sqlsb.toString();
        } catch (Exception e) {
            RSLogger.ErrorLogInfo("SqlSelectCountFactory error." + e.getLocalizedMessage());
            throw new Exception("SqlSelectCountFactory error." + e.getLocalizedMessage());
        } finally {
            sqlsb = null;
            linkTerm = null;
        }
    }

    private static DataBaseTypeEnum GetColumnType(Set<TableDetailModel> tableDetails, String columnName) {
        if (tableDetails == null || tableDetails.isEmpty()) {
            return null;
        }
        for (TableDetailModel tableDetail : tableDetails) {
            if (columnName.equals(tableDetail.name)) {
                return GetColumnType(tableDetail.strDataType);
            }
        }

        return null;

        //select * from systypes
    }

    private static DataBaseTypeEnum GetColumnType(String pStrDataType) {
        //TODO 支持小数，和日期时间类型
        switch (pStrDataType) {
            case "int":
                return DataBaseTypeEnum.number;
            case "varchar":
                return DataBaseTypeEnum.charset;
            case "datetime":
                return DataBaseTypeEnum.datetime;
            case "date":
                return DataBaseTypeEnum.date;
            case "time":
                return DataBaseTypeEnum.time;
            case "decimal":
                return DataBaseTypeEnum.decimal;
            default:
                return null;
        }
    }

    private static boolean IsColumnExist(Set<TableDetailModel> tableDetails, String columnName) {
        if (tableDetails == null || tableDetails.isEmpty()) {
            return false;
        }
        for (TableDetailModel tableDetail : tableDetails) {
            if (columnName.equals(tableDetail.name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 执行sql语句
     *
     * @param rsid
     * @param sqlStr
     * @return
     */
    public static ExecuteResultParam ExecuteSql(String rsid, Set<String> sqlStr) throws SQLException {
        if (sqlStr==null) {
            return new ExecuteResultParam(-1,"execute sqls is null");
        }
        Connection conn = null;
        Statement stmt = null;
        Integer iRows=0;
        try {
            conn = DBHelper.ConnectSybase(rsid);
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            for (String sqlStrItem : sqlStr) {
                iRows += stmt.executeUpdate(sqlStrItem);
            }
            conn.commit();
            return new ExecuteResultParam(iRows);
        } catch (Exception e) {
            RSLogger.ErrorLogInfo("ExecuteSql error sql:" + sqlStr + "exception.msg" + e.getLocalizedMessage());
            if (conn != null) {
                conn.rollback();
            }
            return new ExecuteResultParam(-1, e.getLocalizedMessage());
        } finally {
            DBHelper.CloseConnection(stmt, conn);
        }
    }

    /**
     * 执行sql语句
     *
     * @param rsid
     * @param sqlStr
     * @return
     */
    public static ExecuteResultParam ExecuteSql(String rsid, String sqlStr) {
        RSLogger.LogInfo("executeSql : " + rsid + " sql: " + sqlStr);
        Connection conn = null;
        Statement stmt = null;
        // conn.setAutoCommit( false );
        // conn.rollback();
        // conn.commit();
        try {
            conn = DBHelper.ConnectSybase(rsid);
            stmt = conn.createStatement();
            Integer IRows = stmt.executeUpdate(sqlStr);
            RSLogger.LogInfo(IRows.toString() + " row changed");
            return new ExecuteResultParam(IRows);
        } catch (Exception e) {
            RSLogger.ErrorLogInfo("ExecuteSql error sql:" + sqlStr + "exception.msg" + e.getLocalizedMessage());
            return new ExecuteResultParam(-1, e.getLocalizedMessage());
        } finally {
            DBHelper.CloseConnection(stmt, conn);
        }
    }

    /**
     * 执行sql语句查询
     *
     * @param rsid
     * @param sqlStr
     * @return
     */
    public static ExecuteResultParam ExecuteSqlSelect(String rsid, String sqlStr) throws Exception {
        RSLogger.LogInfo("executeSql : " + rsid + " sql: " + sqlStr);
        Connection conn = null;
        Statement stmt = null;
        JSONObject table = null;
        ResultSet result = null;
        try {
            conn = DBHelper.ConnectSybase(rsid);
            stmt = conn.createStatement();
            result = stmt.executeQuery(sqlStr);
            ResultSetMetaData rsmd = result.getMetaData();
            int columnCount = rsmd.getColumnCount();
            table = new JSONObject();
            JSONArray rows = new JSONArray();
            JSONObject row = null;
            while (result.next()) {
                row = new JSONObject();
                for (int j = 1; j <= columnCount; j++) {
                    //todo 根据列的类型转换数据类型
                    //1获取操作的表
                    //2，根据字段名称转换
                    row.accumulate(rsmd.getColumnName(j), result.getString(j));
                }
                rows.add(row);
                row = null;
            }
            rsmd = null;
            result.close();
            table.accumulate(DeployInfo.ResultDataTag, rows);
            rows = null;
            //table.accumulate("rowCount", dataIndex);
        } catch (SQLException e) {
            //e.printStackTrace();
            RSLogger.ErrorLogInfo("ExecuteSqlSelect err sql:" + sqlStr + "exception.msg" + e.getLocalizedMessage());
            return new ExecuteResultParam(-1, e.getLocalizedMessage());
        } finally {
            DBHelper.CloseConnection(result, stmt, conn);
        }
        return new ExecuteResultParam(0, "", table);
    }

    /**
     * 插入数据，并查询当前数据的identity
     *
     * @param rsid
     * @param sqlStr
     * @return
     * @throws Exception
     */
    public static ExecuteResultParam ExecuteSqlOnceSelect(String rsid, String sqlStr) throws Exception {
        RSLogger.LogInfo("executeSql : " + rsid + " sql: " + sqlStr);
        Connection conn = null;
        Statement stmt = null;
        JSONObject table = null;
        ResultSet result = null;
        try {
            conn = DBHelper.ConnectSybase(rsid);
            stmt = conn.createStatement();
            result = stmt.executeQuery(sqlStr);
            ResultSetMetaData rsmd = result.getMetaData();
            int columnCount = rsmd.getColumnCount();
            table = new JSONObject();
            JSONObject row = null;
            while (result.next()) {
                row = new JSONObject();
                for (int j = 1; j <= columnCount; j++) {
                    //todo 根据列的类型转换数据类型
                    //1获取操作的表
                    //2，根据字段名称转换
                    row.accumulate(rsmd.getColumnName(j), result.getString(j));
                }
            }
            rsmd = null;
            result.close();
            table.accumulate(DeployInfo.ResultDataTag, row);
            //table.accumulate("rowCount", dataIndex);
        } catch (SQLException e) {
            //e.printStackTrace();
            RSLogger.ErrorLogInfo("ExecuteSqlSelect err sql:" + sqlStr + "exception.msg" + e.getLocalizedMessage());
            return new ExecuteResultParam(-1, e.getLocalizedMessage());
        } finally {
            DBHelper.CloseConnection(result, stmt, conn);
        }
        return new ExecuteResultParam(0, "", table);
    }

    public static ExecuteResultParam ExecuteSqlSelect(String rsid, String sqlStr, List<String> url_columns) throws Exception {
        RSLogger.LogInfo("executeSql : " + rsid + " sql: " + sqlStr);
        Connection conn = null;
        Statement stmt = null;
        JSONObject table = null;
        ResultSet result = null;
        try {
            conn = DBHelper.ConnectSybase(rsid);
            stmt = conn.createStatement();
            result = stmt.executeQuery(sqlStr);
            ResultSetMetaData rsmd = result.getMetaData();
            int columnCount = rsmd.getColumnCount();
            table = new JSONObject();
            JSONArray rows = new JSONArray();
            JSONObject row = null;
            String tempColumnName = "";
            StringBuffer tempColumnValue = new StringBuffer();
            while (result.next()) {
                row = new JSONObject();
                tempColumnValue.delete(0, tempColumnValue.length());
                for (int j = 1; j <= columnCount; j++) {
                    tempColumnValue.delete(0, tempColumnValue.length());
                    tempColumnName = rsmd.getColumnName(j);
                    tempColumnValue.append(result.getString(j));
                    if (!isExistMember(url_columns, tempColumnName)) {
                        row.accumulate(tempColumnName, tempColumnValue.toString());
                    } else {
                        if (tempColumnValue != null || !"".equals(tempColumnValue) || !"null".equals(tempColumnValue.toString().toLowerCase())) {
                            String[] values = tempColumnValue.toString().split("\\" + DeployInfo.StringLinkMark);
                            tempColumnValue.delete(0, tempColumnValue.length());
                            if (values != null || 0 != values.length) {
                                for (int i = 0; i < values.length; i++) {
                                    if (i != 0 && i != values.length) {
                                        tempColumnValue.append(DeployInfo.StringLinkMark);
                                        //tempColumnValue = tempColumnValue + DeployInfo.StringLinkMark;
                                    }
                                    String value = values[i];
                                    if (value.equals("") || value.equals(" ") || value.toLowerCase().equals("null")) {
                                        continue;
                                    }
                                    value = DeployInfo.GetDeployHttpFilePath() + value;
                                    tempColumnValue.append(value);
                                    //tempColumnValue = tempColumnValue + value;
                                }
                            }
                        }
                        row.accumulate(tempColumnName, tempColumnValue.toString());
                    }
                }
                rows.add(row);
                row = null;
            }
            rsmd = null;
            result.close();
            table.accumulate(DeployInfo.ResultDataTag, rows);
            //table.accumulate("rowCount", dataIndex);
        } catch (SQLException e) {
            RSLogger.ErrorLogInfo("ExecuteSqlSelect err sql:" + sqlStr + "exception.msg" + e.getLocalizedMessage());
            return new ExecuteResultParam(-1, e.getLocalizedMessage());
        } finally {
            DBHelper.CloseConnection(result, stmt, conn);
        }
        return new ExecuteResultParam(0, "", table);
    }

    private static boolean isExistMember(List<String> strs, String member) {
        if (strs == null || strs.isEmpty()) {
            return false;
        }
        for (String str : strs) {
            if (str.equals(member)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isExistMember(Set<String> strs, String member) {
        if (strs == null || strs.isEmpty()) {
            return false;
        }
        for (String str : strs) {
            if (str.equals(member)) {
                return true;
            }
        }
        return false;
    }

    private static TableInfoModel GetTabelInfoByLocal(Set<TableInfoModel> tableInfos, String tableId) {
        if (tableInfos == null || tableInfos.isEmpty()) {
            return null;
        }
        for (TableInfoModel tempTableinfo : tableInfos) {
            if (tempTableinfo.tbId.equals(tableId)) {
                return tempTableinfo;
            }
        }
        return null;
    }

    /**
     * 更新表结构到数据库指定表中。
     *
     * @param rsid
     * @return
     */
    @Deprecated
    public static ExecuteResultParam GetTableInfosByDataBase(String rsid) {
        //TODO 如果需要使用该方法，请重新测试，因为获取表结构的方式有所改动
        // RSLogger.LogInfo("GetTableInfos : " + rsid);
        Connection conn = null;
        Statement stmt = null;
        ResultSet result = null;
        Set<TableInfoModel> tableInfos = new HashSet<>();
        //Set<TableDetailModel> tableDetails = new HashSet<>();
        Set<String> insertSqls = new HashSet<>();
        //获取数据库连接
        try {
            String sqlForTableName = "select ob.* from sysobjects ob where ob.type='U' ";
            String sqlInsertTableInfo = null;
            StringBuffer sqlTableDetailWhere = new StringBuffer();
            conn = DBHelper.ConnectSybase(rsid);
            stmt = conn.createStatement();
            result = stmt.executeQuery(sqlForTableName);
            //ResultSetMetaData rsmd = result.getMetaData();
            //int columnCount = rsmd.getColumnCount();
            TableInfoModel tempTableInfo = null;
            while (result.next()) {
                tempTableInfo = new TableInfoModel();
                tempTableInfo.tbName = result.getString("name");
                sqlTableDetailWhere.append("'");
                sqlTableDetailWhere.append(tempTableInfo.tbName);
                sqlTableDetailWhere.append("',");
                tempTableInfo.tbId = result.getString("id");
                // row.accumulate(rsmd.getColumnName(j), result.getString(j));
                tableInfos.add(tempTableInfo);
                //添加到需要执行的sql语句中
                insertSqls.add("insert into alltable1 (id,name) values(" + tempTableInfo.tbId + ",'" + tempTableInfo.tbName + "')");
            }
            result.close();
            sqlForTableName = String.format("SELECT sc.* FROM syscolumns sc INNER JOIN sysobjects so ON sc.id = so.id WHERE so.name in (%s) order by sc.id ", sqlTableDetailWhere.subSequence(0, sqlTableDetailWhere.lastIndexOf(",")));

            ResultSet result1 = stmt.executeQuery(sqlForTableName);
            TableDetailModel tempTabelDetail = null;
            while (result1.next()) {
                tempTabelDetail = new TableDetailModel();
                tempTabelDetail.name = result1.getString("name");
                tempTabelDetail.tbId = result1.getString("id");
                tempTabelDetail.dataLength = result1.getString("length");
                tempTabelDetail.type = result1.getString("type");
                tempTabelDetail.strDataType = result1.getString("dataTypeName");
                tempTabelDetail.dataType = GetColumnType(tempTabelDetail.strDataType);
                tempTabelDetail.status = result1.getString("status");
                insertSqls.add(String.format("insert into tableinfo1(tableid,name,type,length)values(%s,'%s','%s',%s)", tempTabelDetail.tbId, tempTabelDetail.name, tempTabelDetail.dataType.toString(), tempTabelDetail.dataLength));
                TableInfoModel temptableInfo = GetTabelInfoByLocal(tableInfos, tempTabelDetail.tbId);
                temptableInfo.tableDetails.add(tempTabelDetail);
            }
            result1.close();
            int iRet;
            for (String insertSql : insertSqls) {
                iRet = stmt.executeUpdate(insertSql);
                if (iRet != 1) {
                    return new ExecuteResultParam(-2, "insert data error.sql: " + insertSql);
                }
            }
            return new ExecuteResultParam(0, "success");
            //查询用户表中的column信息
        } catch (Exception e) {
            RSLogger.ErrorLogInfo(e.getLocalizedMessage());
            return new ExecuteResultParam(-1, e.getLocalizedMessage());
        } finally {
            DBHelper.CloseConnection(result, stmt, conn);
        }

    }

    private static Set<DBDetailModel> dbModel = new HashSet<>();

    private static ExecuteResultParam loadDBInfo(String rsid) {
        RSLogger.LogInfo("GetTableInfos : " + rsid);
        Connection conn = null;
        Statement stmt = null;
        ResultSet result = null;
        Set<TableInfoModel> tableInfos = new HashSet<>();

        //Set<TableDetailModel> tableDetails = new HashSet<>();
        //Set<String> insertSqls = new HashSet<>();
        //获取数据库连接
        try {
            String sqlForTableName = "select ob.* from sysobjects ob where ob.type='U' ";
            String sqlInsertTableInfo = null;
            StringBuffer sqlTableDetailWhere = new StringBuffer();
            conn = DBHelper.ConnectSybase(rsid);
            stmt = conn.createStatement();
            result = stmt.executeQuery(sqlForTableName);
            //ResultSetMetaData rsmd = result.getMetaData();
            //int columnCount = rsmd.getColumnCount();
            TableInfoModel tempTableInfo = null;
            while (result.next()) {
                tempTableInfo = new TableInfoModel();
                tempTableInfo.tbName = result.getString("name");
                sqlTableDetailWhere.append("'");
                sqlTableDetailWhere.append(tempTableInfo.tbName);
                sqlTableDetailWhere.append("',");
                tempTableInfo.tbId = result.getString("id");
                // row.accumulate(rsmd.getColumnName(j), result.getString(j));
                tableInfos.add(tempTableInfo);
            }
            result.close();
            sqlForTableName = String.format("select c.id as id, c.length as length , o.name as tbName , c.name,c.type, c.status,c.usertype  ,t.name as dataTypeName from sysobjects o inner join syscolumns c on c.id = o.id inner join systypes t on t.usertype = c.usertype where o.type = 'U' and o.name in (%s) order by c.id", sqlTableDetailWhere.subSequence(0, sqlTableDetailWhere.lastIndexOf(",")));

            ResultSet result1 = stmt.executeQuery(sqlForTableName);
            TableDetailModel tempTabelDetail = null;
            while (result1.next()) {
                tempTabelDetail = new TableDetailModel();
                tempTabelDetail.name = result1.getString("name");
                tempTabelDetail.tbId = result1.getString("id");
                tempTabelDetail.dataLength = result1.getString("length");
                tempTabelDetail.type = result1.getString("type");
                tempTabelDetail.strDataType = result1.getString("dataTypeName");
                tempTabelDetail.dataType = GetColumnType(tempTabelDetail.strDataType);
                tempTabelDetail.status = result1.getString("status");
                TableInfoModel temptableInfo = GetTabelInfoByLocal(tableInfos, tempTabelDetail.tbId);
                temptableInfo.tableDetails.add(tempTabelDetail);
            }
            result1.close();
            String tempPrimaryName = null;
            ResultSet result2 = stmt.executeQuery(SearchTablePrimaryKeyStr);
            for (TableInfoModel tableInfo : tableInfos) {
                while (result2.next()) {
                    if (tableInfo.tbName.equals(result2.getString("tableName")) && result2.getString("indexDescription").indexOf("primary key") > 0) {
                        tempPrimaryName = result2.getString("columnName");
                        break;
                    }
                }
                if (tempPrimaryName == null || tempPrimaryName.isEmpty()) {
                    continue;
                }
                for (TableDetailModel tableDetail : tableInfo.tableDetails) {
                    if (tableDetail.name.equals(tempPrimaryName)) {
                        tableDetail.isPrimaryKey = true;
                        tableInfo.tbPrimaryKey = tableDetail;
                        break;
                    }
                }
            }
            result2.close();
            DBDetailModel dbDetailModel = new DBDetailModel();
            dbDetailModel.rsId = rsid;
            dbDetailModel.dbName = rsid;
            dbDetailModel.dbTableInfos = tableInfos;
            dbModel.add(dbDetailModel);
            dbDetailModel = null;
            tableInfos = null;
            return new ExecuteResultParam(0, "success");
            //查询用户表中的column信息
        } catch (Exception e) {
            RSLogger.ErrorLogInfo(e.getLocalizedMessage());
            return new ExecuteResultParam(-1, e.getLocalizedMessage());
        } finally {
            DBHelper.CloseConnection(result, stmt, conn);
        }
    }

    private static DBDetailModel GetRSIDModel(String rsid) throws Exception {
        LoadDBInfo();
        for (DBDetailModel next : dbModel) {
            if (next.rsId.equals(rsid)) {
                return next;
            }
        }
        return null;
    }

    private static Set<DBDetailModel> LoadDBInfo() throws Exception {
        if (dbModel.isEmpty()) {
            Set<SystemSetModel> modelSet = DeployInfo.GetSystemSets();
            for (SystemSetModel next : modelSet) {
                loadDBInfo(next.id);
            }
        }
        return dbModel;
    }

    private static Set<TableDetailModel> FindTableDetail(String tableName, String RSID) throws Exception {
        DBDetailModel dbDetailModel = GetRSIDModel(RSID);
        for (TableInfoModel tableInfo : dbDetailModel.dbTableInfos) {
            if (tableInfo.tbName.equals(tableName)) {
                return tableInfo.tableDetails;
            }
        }
        return null;
    }

    /*
     查询表主键sql语句
     */
    private static final String SearchTablePrimaryKeyStr = "select 'tableName' = object_name(sc.id),\n"
            + "'columnName' = index_col(object_name(sc.id),sc.indid,1),\n"
            + "'indexDescription' = convert(varchar(210), case when (sc.status & 16)<>0 then 'clustered' else 'nonclustered' end\n"
            + "+ case when (sc.status & 1)<>0 then ', '+'ignore duplicate keys' else '' end\n"
            + "+ case when (sc.status & 2)<>0 then ', '+'unique' else '' end\n"
            + "+ case when (sc.status & 4)<>0 then ', '+'ignore duplicate rows' else '' end\n"
            + "+ case when (sc.status & 64)<>0 then ', '+'statistics' else case when (status & 32)<>0 then ', '+'hypothetical' else '' end end\n"
            + "+ case when (sc.status & 2048)<>0 then ', '+'primary key' else '' end\n"
            + "+ case when (sc.status & 4096)<>0 then ', '+'unique key' else '' end\n"
            + "+ case when (sc.status & 8388608)<>0 then ', '+'auto create' else '' end\n"
            + "+ case when (sc.status & 16777216)<>0 then ', '+'stats no recompute' else '' end),\n"
            + "'indexName' = name\n"
            + "from sysindexes sc where  (sc.status & 64) = 0 \n"
            + "order by sc.id";
    /*
     查询表identity列sql语句
     */
    private static final String SearchTableIdentityKeyStr = "select object_name(sc.id) as 'tn' , sc.name as 'cn' from syscolumns sc inner join sysobjects so on sc.id = so.id where  so.name='%s' and  sc.status =128 ";
}
