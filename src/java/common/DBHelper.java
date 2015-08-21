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
import common.model.SqlFactoryResultModel;
import common.model.TableDetailModel;
import common.model.SystemSetModel;
import common.model.TableInfoModel;
import java.sql.*; // JDBC
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties; // Properties
import java.util.Set;
import java.util.UUID;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import snaq.db.ConnectionPool;

public class DBHelper {

    private static final int ConnectionPoolTimeout = 4000;//4s，超时时间
    private static final int GetConnectionFromPoolTimeout = 10000;//从连接池中获取链接超时时间10s，

    private static Map<String, ConnectionPool> mapConnectionPool = new HashMap<>();

    public static boolean initializePool() throws Exception {
        //get systemSet    
        ConnectionPool tempCp = null;
        StringBuffer temp = new StringBuffer();
        Set<SystemSetModel> systemSet = DeployInfo.GetSystemSets();

        Class c = Class.forName("com.sybase.jdbc3.jdbc.SybDriver");  // Fill JDBC driver class name here.
        Driver driver = (Driver) c.newInstance();
        DriverManager.registerDriver(driver);
        for (SystemSetModel tempSystemSet : systemSet) {
//            if (tempSystemSet.id.equals("ElectornicBank") || tempSystemSet.id.equals("microCredit")) {
            temp.delete(0, temp.length());
            // Use the Sybase jConnect driver...
            temp.append("jdbc:sybase:Tds:");
            // to connect to the supplied machine name...
            temp.append(tempSystemSet.dbAddress);
            // on the default port number for ASA...
            temp.append(":");
            temp.append(tempSystemSet.dbPort);
            temp.append("/");
            //temp.append(":5000/");
            temp.append(tempSystemSet.dbName);
            temp.append("?ServiceName=");
            temp.append(tempSystemSet.dbName);
            //temp.append("?language=us_english&charset=cp936");
            // 1:pool-name,2:min,3:max,4:size,5:timeout,6:url,7:name,8:passwd
            tempCp = new ConnectionPool(tempSystemSet.id, 5, 5, 6, ConnectionPoolTimeout, temp.toString(), tempSystemSet.dbUser, tempSystemSet.dbPwd);
            tempCp.setAsyncDestroy(true);
            tempCp.setCaching(false);
            mapConnectionPool.put(tempSystemSet.id, tempCp);
//            }
        }
        return true;
    }

    public static Connection GetConnectionFromPool(String rsid) throws Exception {
        if (mapConnectionPool == null || mapConnectionPool.isEmpty()) {
            initializePool();
        }
        ConnectionPool cp = mapConnectionPool.get(rsid);
        if (cp == null) {
            throw new Exception(String.format("GetConnectionFromPool error. can't fund connectionPool by name :'%s'", rsid));
        }
        Connection tempCon = cp.getConnection(GetConnectionFromPoolTimeout);
        if (tempCon == null) {
            throw new Exception(String.format("Get Connection null,rsid :'%s'", rsid));
        }

        return tempCon;
    }

    private static Connection ConnectSybase() {
        return DBHelper.ASAConnect("sa", "123456", "192.168.169.217", "AustraliaBank", "5000");
        //return DBHelper.ASAConnect("sa", "123456",DeployInfo.GetDBAddress(),DeployInfo.GetDBname());
    }

    public static Connection ConnectSybase(String pId) throws Exception {
        //使用 connectionPool
//        if (pId.equals("ElectornicBank") || pId.equals("microCredit")) {
        return GetConnectionFromPool(pId);
//        }

        //直接jdbc
//        SystemSetModel setModel = DeployInfo.GetSystemSetsByID(pId);
//        if (setModel == null) {
//            RSLogger.ErrorLogInfo("Could not find the db connection.");
//            return null;
//        }
//        return DBHelper.ASAConnect(setModel.dbUser, setModel.dbPwd, setModel.dbAddress, setModel.dbName, setModel.dbPort);
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
            return DriverManager.getConnection(temp.toString(), _props);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e) {
            //e.printStackTrace();
            RSLogger.ErrorLogInfo("get db connection error." + e.getMessage());
            return null;
        } finally {
            temp = null;
        }
    }

    /**
     * 根据传入数据生成插入语句
     *
     *
     * @param paramModel
     * @return
     */
    public static SqlFactoryResultModel SqlInsertFactory(ReviveRSParamModel paramModel) throws Exception {
        StringBuffer tempSql = new StringBuffer();
        StringBuffer tempColumn = new StringBuffer();
        StringBuffer tempValue = new StringBuffer();
        String strUUIDTemp = null;
        SqlFactoryResultModel sqlResultModel = new SqlFactoryResultModel();
        TableInfoModel pTableInfo = null;
        TableDetailModel tempTableColumnDetail = null;
        TableDetailModel singlePrimary = null;
        boolean primaryColumnNoValue = true;
        try {

            pTableInfo = FindTableInformation(paramModel.db_tableName, paramModel.rsid);

            if (pTableInfo == null) {
                throw new Exception(String.format("could not find table's %s information.rsid's %s", paramModel.db_tableName, paramModel.rsid));
            }

            //Set tableIterator = FindTableDetail(paramModel.db_tableName, paramModel.rsid);
            //get identityKey , inserted with get identity value.
            if (pTableInfo.identityKey != null) {
                tempSql.append("SET NOCOUNT ON ");
            }

            tempSql.append(" INSERT INTO ").append(paramModel.db_tableName).append("( ");

            Iterator keys = paramModel.db_valueColumns.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                tempColumn.append(key).append(" ,");
                tempTableColumnDetail = pTableInfo.getColumnDetail(key);
                if (paramModel.db_valueColumns.get(key).equals("db_defaultV")) {
                    tempValue.append(" ").append("default").append(" ,");
                } else if (tempTableColumnDetail.dataType == DataBaseTypeEnum.number || tempTableColumnDetail.dataType == DataBaseTypeEnum.decimal) {
                    tempValue.append(" ").append(paramModel.db_valueColumns.get(key)).append(" ,");
                } else if (tempTableColumnDetail.dataType == DataBaseTypeEnum.charset || tempTableColumnDetail.dataType == DataBaseTypeEnum.date || tempTableColumnDetail.dataType == DataBaseTypeEnum.time || tempTableColumnDetail.dataType == DataBaseTypeEnum.datetime) {
                    tempValue.append("'").append(paramModel.db_valueColumns.get(key)).append("' ,");
                } else {
                    throw new Exception("error: columnTypeUnknown  key." + key);
                }
                //primaryColumnHasValue  params has primary value，don't need uuid
                if (pTableInfo.CheckColumnIsPrimary(key)) {
                    primaryColumnNoValue = false;
                }
                key = null;
            }
            // qualification 1 primariy column no value , 2 primary column data type is charset, 3 primary column is  single column
            if (primaryColumnNoValue) {
                singlePrimary = pTableInfo.getPrimariyColumn();
                if (singlePrimary != null && singlePrimary.dataType == DataBaseTypeEnum.charset) {
                    sqlResultModel.columnValue = new HashMap<>(); //new CollectionsUtils.ConstMap<>();
                    strUUIDTemp = UUID.randomUUID().toString();
                    sqlResultModel.columnValue.put(singlePrimary.name, strUUIDTemp);
                    tempSql.append(tempColumn).append(singlePrimary.name).append(" ) VALUES (");
                    tempSql.append(tempValue).append("'").append(strUUIDTemp).append("'").append(")");
                }

            } else {
                tempSql.append(tempColumn.substring(0, tempColumn.length() - 1)).append(" ) VALUES (");
                tempSql.append(tempValue.substring(0, tempValue.length() - 1)).append(")");
            }

            if (pTableInfo.identityKey != null) {
                tempSql.append(" SELECT @@IDENTITY AS ").append(pTableInfo.identityKey.name);
            }
            sqlResultModel.strSql = tempSql.toString();
            return sqlResultModel;
        } catch (Exception e) {
            RSLogger.ErrorLogInfo("SqlInsertFactory error." + e.getMessage(), e);
            throw new Exception("SqlInsertFactory error." + e.getMessage());
        } finally {
            tempSql = null;
            tempColumn = null;
            tempValue = null;
            tempTableColumnDetail = null;
            singlePrimary = null;
            pTableInfo = null;
            strUUIDTemp = null;
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
        TableInfoModel pTableInfo = null;
        TableDetailModel tempTableColumnDetail = null;
        TableDetailModel singlePrimary = null;
        try {

            pTableInfo = FindTableInformation(paramModel.db_tableName, paramModel.rsid);

            if (pTableInfo == null) {
                throw new Exception(String.format("could not find table's %s information.rsid's %s", paramModel.db_tableName, paramModel.rsid));
            }
            sqlsb.append("UPDATE ").append(paramModel.db_tableName).append(" SET ");

            //Set tableIterator = FindTableDetail(paramModel.db_tableName, paramModel.rsid);
            Iterator keys = paramModel.db_valueColumns.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                tempTableColumnDetail = pTableInfo.getColumnDetail(key);
                sqlsb.append(key).append("= ");
                if (paramModel.db_valueColumns.get(key).equals("db_defaultV")) {
                    sqlsb.append(" ").append("default").append(" ,");
                } else if (tempTableColumnDetail.dataType == DataBaseTypeEnum.number || tempTableColumnDetail.dataType == DataBaseTypeEnum.decimal) {
                    sqlsb.append(" ").append(paramModel.db_valueColumns.get(key)).append(" ,");
                } else if (tempTableColumnDetail.dataType == DataBaseTypeEnum.charset || tempTableColumnDetail.dataType == DataBaseTypeEnum.date || tempTableColumnDetail.dataType == DataBaseTypeEnum.time || tempTableColumnDetail.dataType == DataBaseTypeEnum.datetime) {
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
                //String tablePrimary = FindTablePrimaryKey(tableIterator); //SearchTablePrimaryKey(paramModel.db_tableName, paramModel.rsid);
                singlePrimary = pTableInfo.getPrimariyColumn();
                if (singlePrimary == null) {
                    throw new Exception(String.format("error: primaryKey: %s not find. tableName:%s, rsid:%s", singlePrimary.name, paramModel.db_tableName, paramModel.rsid));
                }
                sqlsb.append(singlePrimary.name).append("=");
                // fill up pkValues for ws-ms.
                if (paramModel.pkValues == null) {
                    paramModel.pkValues = new HashMap<>();
                }
                paramModel.pkValues.put(singlePrimary.name, paramModel.pkValue);

                if (singlePrimary.dataType == DataBaseTypeEnum.number || singlePrimary.dataType == DataBaseTypeEnum.decimal) {
                    sqlsb.append(" ").append(paramModel.pkValue).append(" ");
                } else if (singlePrimary.dataType == DataBaseTypeEnum.charset || singlePrimary.dataType == DataBaseTypeEnum.date || singlePrimary.dataType == DataBaseTypeEnum.time || singlePrimary.dataType == DataBaseTypeEnum.datetime) {
                    sqlsb.append("'").append(paramModel.pkValue).append("' ");
                } else {
                    throw new Exception(String.format("error: primaryKey:%s unknow type. tableName:%s, rsid:%s", singlePrimary.name, paramModel.db_tableName, paramModel.rsid));
                }
                return sqlsb.toString();
            } else if (paramModel.pkValues != null && !paramModel.pkValues.isEmpty()) {
                if (pTableInfo.tbPrimaryKeys == null || pTableInfo.tbPrimaryKeys.isEmpty()) {
                    throw new Exception(String.format("error: primaryKey: %s not find. tableName:%s, rsid:%s", singlePrimary.name, paramModel.db_tableName, paramModel.rsid));
                }
                Set<String> pkValueKeys = paramModel.pkValues.keySet();
                for (String pkValueKey : pkValueKeys) {
                    singlePrimary = pTableInfo.getPrimariyColumnByName(pkValueKey);
                    if (singlePrimary == null) {
                        throw new Exception(String.format("error: primaryKey: %s not find. tableName:%s, rsid:%s", singlePrimary.name, paramModel.db_tableName, paramModel.rsid));
                    }
                    sqlsb.append(singlePrimary.name).append("=");
                    if (singlePrimary.dataType == DataBaseTypeEnum.number || singlePrimary.dataType == DataBaseTypeEnum.decimal) {
                        sqlsb.append(" ").append(paramModel.pkValues.get(pkValueKey)).append(" ");
                    } else if (singlePrimary.dataType == DataBaseTypeEnum.charset || singlePrimary.dataType == DataBaseTypeEnum.date || singlePrimary.dataType == DataBaseTypeEnum.time || singlePrimary.dataType == DataBaseTypeEnum.datetime) {
                        sqlsb.append("'").append(paramModel.pkValues.get(pkValueKey)).append("' ");
                    } else {
                        throw new Exception(String.format("error: primaryKey:%s unknow type. tableName:%s, rsid:%s", singlePrimary.name, paramModel.db_tableName, paramModel.rsid));
                    }
                    sqlsb.append(" and ");
                }
                return sqlsb.substring(0, sqlsb.lastIndexOf("and"));
            } else {
                //param不包含主键，按照传入条件进行删除
                Iterator noteIterator = paramModel.db_valueFilter.keySet().iterator();//jsonNote.keys();
                while (noteIterator.hasNext()) {
                    String key = (String) noteIterator.next();
                    tempTableColumnDetail = pTableInfo.getColumnDetail(key);
                    if (null == tempTableColumnDetail) {
                        continue;
                    }
                    sqlsb.append("  ").append(key).append(" = ");
                    if (tempTableColumnDetail.dataType == DataBaseTypeEnum.number || tempTableColumnDetail.dataType == DataBaseTypeEnum.decimal) {
                        sqlsb.append(" ").append(paramModel.db_valueFilter.get(key)).append(" ");
                    } else if (tempTableColumnDetail.dataType == DataBaseTypeEnum.charset || tempTableColumnDetail.dataType == DataBaseTypeEnum.date || tempTableColumnDetail.dataType == DataBaseTypeEnum.time || tempTableColumnDetail.dataType == DataBaseTypeEnum.datetime) {
                        sqlsb.append("'").append(paramModel.db_valueFilter.get(key)).append("' ");
                    } else {
                        throw new Exception("error: columnTypeUnknown  key." + key);
                    }
                    sqlsb.append(" and");
                }
                return sqlsb.substring(0, sqlsb.lastIndexOf("and"));
            }
        } catch (Exception e) {
            RSLogger.ErrorLogInfo("SqlUpdateFactory error." + e.getLocalizedMessage());
            throw new Exception("SqlUpdateFactory error." + e.getLocalizedMessage());
        } finally {
            pTableInfo = null;
            tempTableColumnDetail = null;
            singlePrimary = null;
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

        TableInfoModel pTableInfo = null;
        TableDetailModel tempTableColumnDetail = null;
        TableDetailModel singlePrimary = null;
        try {

            pTableInfo = FindTableInformation(paramModel.db_tableName, paramModel.rsid);

            if (pTableInfo == null) {
                throw new Exception(String.format("could not find table's %s information.rsid's %s", paramModel.db_tableName, paramModel.rsid));
            }
            //Set tableIterator = FindTableDetail(paramModel.db_tableName, paramModel.rsid);
            sqlSb.append("DELETE ").append(paramModel.db_tableName).append(" WHERE ");

            //判断是否存在主键,存在主键安装主键删除数据
            if (paramModel.pkValue != null && !paramModel.pkValue.isEmpty()) {
                //获取主键
                singlePrimary = pTableInfo.getPrimariyColumn();
                if (singlePrimary == null) {
                    throw new Exception(String.format("error: primaryKey: %s not find. tableName:%s, rsid:%s", singlePrimary.name, paramModel.db_tableName, paramModel.rsid));
                }
                sqlSb.append(singlePrimary.name).append("=");
                if (singlePrimary.dataType == DataBaseTypeEnum.number || singlePrimary.dataType == DataBaseTypeEnum.decimal) {
                    sqlSb.append(" ").append(paramModel.pkValue).append(" ");
                } else if (singlePrimary.dataType == DataBaseTypeEnum.charset || singlePrimary.dataType == DataBaseTypeEnum.date || singlePrimary.dataType == DataBaseTypeEnum.time || singlePrimary.dataType == DataBaseTypeEnum.datetime) {
                    sqlSb.append("'").append(paramModel.pkValue).append("' ");
                } else {
                    throw new Exception(String.format("error: primaryKey:%s unknow type. tableName:%s, rsid:%s", singlePrimary.name, paramModel.db_tableName, paramModel.rsid));
                }
                return sqlSb.toString();
            } else if (paramModel.pkValues != null && !paramModel.pkValues.isEmpty()) {
                if (pTableInfo.tbPrimaryKeys == null || pTableInfo.tbPrimaryKeys.isEmpty()) {
                    throw new Exception(String.format("error: primaryKey: %s not find. tableName:%s, rsid:%s", singlePrimary.name, paramModel.db_tableName, paramModel.rsid));
                }
                Set<String> pkValueKeys = paramModel.pkValues.keySet();
                for (String pkValueKey : pkValueKeys) {
                    singlePrimary = pTableInfo.getPrimariyColumnByName(pkValueKey);
                    if (singlePrimary == null) {
                        throw new Exception(String.format("error: primaryKey: %s not find. tableName:%s, rsid:%s", singlePrimary.name, paramModel.db_tableName, paramModel.rsid));
                    }
                    sqlSb.append(singlePrimary.name).append("=");
                    if (singlePrimary.dataType == DataBaseTypeEnum.number || singlePrimary.dataType == DataBaseTypeEnum.decimal) {
                        sqlSb.append(" ").append(paramModel.pkValues.get(pkValueKey)).append(" ");
                    } else if (singlePrimary.dataType == DataBaseTypeEnum.charset || singlePrimary.dataType == DataBaseTypeEnum.date || singlePrimary.dataType == DataBaseTypeEnum.time || singlePrimary.dataType == DataBaseTypeEnum.datetime) {
                        sqlSb.append("'").append(paramModel.pkValues.get(pkValueKey)).append("' ");
                    } else {
                        throw new Exception(String.format("error: primaryKey:%s unknow type. tableName:%s, rsid:%s", singlePrimary.name, paramModel.db_tableName, paramModel.rsid));
                    }
                    sqlSb.append(" and ");
                }
                return sqlSb.substring(0, sqlSb.lastIndexOf("and"));
            } else {
                //param不包含主键，按照传入条件进行删除
                Iterator keys = paramModel.db_valueFilter.keySet().iterator(); //jsonNote.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    tempTableColumnDetail = pTableInfo.getColumnDetail(key);
                    if (null == tempTableColumnDetail) {
                        continue;
                    }
                    sqlSb.append("  ").append(key).append(" = ");
                    if (tempTableColumnDetail.dataType == DataBaseTypeEnum.number || tempTableColumnDetail.dataType == DataBaseTypeEnum.decimal) {
                        sqlSb.append(" ").append(paramModel.db_valueFilter.get(key)).append(" ");
                    } else if (tempTableColumnDetail.dataType == DataBaseTypeEnum.charset || tempTableColumnDetail.dataType == DataBaseTypeEnum.date || tempTableColumnDetail.dataType == DataBaseTypeEnum.time || tempTableColumnDetail.dataType == DataBaseTypeEnum.datetime) {
                        sqlSb.append("'").append(paramModel.db_valueFilter.get(key)).append("' ");
                    } else {
                        throw new Exception("error: columnTypeUnknown  key." + key);
                    }
                    sqlSb.append(" and");
                }
                return sqlSb.substring(0, sqlSb.lastIndexOf("and"));
            }
        } catch (Exception e) {
            RSLogger.ErrorLogInfo("SqlDeleteFactory error." + e.getLocalizedMessage());
            throw new Exception("SqlDeleteFactory error." + e.getLocalizedMessage());
        } finally {
            pTableInfo = null;
            tempTableColumnDetail = null;
            singlePrimary = null;
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
        // Set tableIterator = null;
        Set<String> columnsName = null;
        String tempSql = null, linkTerm = null;
        StringBuilder sqlsb = null;

        TableInfoModel pTableInfo = null;
        TableDetailModel singlePrimary = null;
        try {
            pTableInfo = FindTableInformation(paramModel.db_tableName, paramModel.rsid);
            if (pTableInfo == null) {
                throw new Exception(String.format("could not find table's %s information.rsid's %s", paramModel.db_tableName, paramModel.rsid));
            }
            sqlsb = new StringBuilder();
            sqlsb.append("SELECT ");
            if (paramModel.db_columns != null && !paramModel.db_columns.isEmpty()) {
                columnsName = paramModel.db_columns;
            } else {
                columnsName = pTableInfo.getColumnsName();
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
            columnsName.clear();
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
                singlePrimary = pTableInfo.getPrimariyColumn();
                if (singlePrimary == null) {
                    throw new Exception(String.format("error: primaryKey: %s not find. tableName:%s, rsid:%s", singlePrimary.name, paramModel.db_tableName, paramModel.rsid));
                }

                sqlsb.append(singlePrimary.name).append("=");
                //colunmType = GetColumnType(tableIterator, tablePrimary);
                if (singlePrimary.dataType == DataBaseTypeEnum.number || singlePrimary.dataType == DataBaseTypeEnum.decimal) {
                    sqlsb.append(" ").append(paramModel.pkValue).append(" ");
                } else if (singlePrimary.dataType == DataBaseTypeEnum.charset || singlePrimary.dataType == DataBaseTypeEnum.date || singlePrimary.dataType == DataBaseTypeEnum.time || singlePrimary.dataType == DataBaseTypeEnum.datetime) {
                    sqlsb.append("'").append(paramModel.pkValue).append("' ");
                } else {
                    //sqlsb.append("columnTypeUnknown");
                    throw new Exception(String.format("error: primaryKey:%s unknow type. tableName:%s, rsid:%s", singlePrimary.name, paramModel.db_tableName, paramModel.rsid));
                }
                return sqlsb.toString();
            } else if (paramModel.pkValues != null && !paramModel.pkValues.isEmpty()) {
                if (pTableInfo.tbPrimaryKeys == null || pTableInfo.tbPrimaryKeys.isEmpty()) {
                    throw new Exception(String.format("error: primaryKey: %s not find. tableName:%s, rsid:%s", singlePrimary.name, paramModel.db_tableName, paramModel.rsid));
                }
                sqlsb.append(linkTerm);
                linkTerm = " AND ";
                Set<String> pkValueKeys = paramModel.pkValues.keySet();
                for (String pkValueKey : pkValueKeys) {
                    singlePrimary = pTableInfo.getPrimariyColumnByName(pkValueKey);
                    if (singlePrimary == null) {
                        throw new Exception(String.format("error: primaryKey: %s not find. tableName:%s, rsid:%s", singlePrimary.name, paramModel.db_tableName, paramModel.rsid));
                    }
                    sqlsb.append(singlePrimary.name).append("=");
                    if (singlePrimary.dataType == DataBaseTypeEnum.number || singlePrimary.dataType == DataBaseTypeEnum.decimal) {
                        sqlsb.append(" ").append(paramModel.pkValues.get(pkValueKey)).append(" ");
                    } else if (singlePrimary.dataType == DataBaseTypeEnum.charset || singlePrimary.dataType == DataBaseTypeEnum.date || singlePrimary.dataType == DataBaseTypeEnum.time || singlePrimary.dataType == DataBaseTypeEnum.datetime) {
                        sqlsb.append("'").append(paramModel.pkValues.get(pkValueKey)).append("' ");
                    } else {
                        throw new Exception(String.format("error: primaryKey:%s unknow type. tableName:%s, rsid:%s", singlePrimary.name, paramModel.db_tableName, paramModel.rsid));
                    }
                    sqlsb.append(" and ");
                }
                return sqlsb.substring(0, sqlsb.lastIndexOf("and"));
            }

            if (paramModel.sql != null && !paramModel.sql.isEmpty()) {
                sqlsb.append(linkTerm).append(paramModel.sql);
            }
            if (paramModel.db_orderBy != null && !paramModel.db_orderBy.isEmpty()) {
                sqlsb.append(" order by ");
                sqlsb.append(paramModel.db_orderBy);
            }
            return sqlsb.toString();
        } catch (Exception e) {
            throw new Exception("SqlSelectFactory error:" + e.getLocalizedMessage());
        } finally {
            UtileSmart.FreeObjects(tempSql, linkTerm, sqlsb, pTableInfo, singlePrimary);
        }

    }

    public static String SqlSelectPageFactory(ReviveRSParamModel paramModel) throws Exception {
        String linkTerm = null, tablePrimary = null;
        StringBuffer sqlsb = null;
        Set<String> columnsName = null;
        //tempTableName 在使用连接池是发现 #temp会出现重复
        String tempTableName = null;

        TableInfoModel pTableInfo = null;
        TableDetailModel singlePrimary = null;
        try {
            pTableInfo = FindTableInformation(paramModel.db_tableName, paramModel.rsid);
            if (pTableInfo == null) {
                throw new Exception(String.format("could not find table's %s information.rsid's %s", paramModel.db_tableName, paramModel.rsid));
            }

            // temp TableName 生成： 表名秒时间随机数（99）
            tempTableName = String.format("#t_%s%s%s", paramModel.db_tableName, common.UtileSmart.getCurrentDateSecond(), UtileSmart.getRandomStrbySeed(99));

            sqlsb = new StringBuffer();
            sqlsb.append("SELECT ");
            if (paramModel.db_columns != null && !paramModel.db_columns.isEmpty()) {
                columnsName = paramModel.db_columns;
            } else {
                columnsName = pTableInfo.getColumnsName();
            }
            for (String nextColumnName : columnsName) {
                if (pTableInfo.identityKey != null && pTableInfo.identityKey.name.equals(nextColumnName)) {
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

            //sqlsb.append(" sybid=identity(12) into #temp FROM ");
            sqlsb.append(" sybid=identity(12) into ").append(tempTableName).append(" FROM ");
            sqlsb.append(paramModel.db_tableName);
            linkTerm = " WHERE ";

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
            //sqlsb.append(" select * from #temp where sybid> ");
            sqlsb.append(" select * from ").append(tempTableName).append(" where sybid> ");
            sqlsb.append(pageOffset);
            sqlsb.append(" and sybid <= ");
            sqlsb.append(topNum);
            return sqlsb.toString();
        } catch (Exception e) {
            throw new Exception("SqlSelectPageFactory error:" + e.getLocalizedMessage());
        } finally {
            UtileSmart.FreeObjects(linkTerm, tablePrimary, sqlsb, columnsName, tempTableName, pTableInfo, singlePrimary);
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
     *
     * @param rsid
     * @param sqlStr List<String> 按照集合顺序执行sql ，如果无须按照集合顺序来执行sql选择 Set<String>
     * 效率较高
     * @return
     * @throws SQLException
     */
    public static ExecuteResultParam ExecuteSql(String rsid, List<String> sqlStr) throws SQLException {
        if (sqlStr == null) {
            return new ExecuteResultParam(-1, "execute sqls is null");
        }
        Connection conn = null;
        Statement stmt = null;
        Integer iRows = 0;
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
     * @param sqlStr Set<String> set 集合效率较高，但是存储数据无须，如果需要按照集合顺序来执行sql选择
     * List<String>
     * @return
     */
    public static ExecuteResultParam ExecuteSql(String rsid, Set<String> sqlStr) throws SQLException {
        if (sqlStr == null) {
            return new ExecuteResultParam(-1, "execute sqls is null");
        }
        Connection conn = null;
        Statement stmt = null;
        Integer iRows = 0;
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
     * 执行sql语句 增删改
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
                    row.accumulate(rsmd.getColumnLabel(j), result.getString(j));
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
     * 使用完成后，注意清理 map，避免内存泄漏 cleanMapTDString
     *
     * @param rsid
     * @param sqlStr
     * @param TableName
     * @return
     * @throws Exception
     */
    public static Map<String, Map<String, String>> ExecuteSqlSelectReturnMap(String rsid, String sqlStr, String TableName) throws Exception {
        Connection conn = null;
        Statement stmt = null;
        ResultSet result = null;
        TableInfoModel pTableInfo = null;
        TableDetailModel singlePrimary = null;
        try {

            pTableInfo = FindTableInformation(TableName, rsid);

            if (pTableInfo == null) {
                throw new Exception(String.format("ExecuteSqlSelectReturnMap could not find table's %s information.rsid's %s", TableName, rsid));
            }

            singlePrimary = pTableInfo.getPrimariyColumn();

            conn = DBHelper.ConnectSybase(rsid);
            stmt = conn.createStatement();
            result = stmt.executeQuery(sqlStr);
            ResultSetMetaData rsmd = result.getMetaData();
            int columnCount = rsmd.getColumnCount();

            Map<String, String> rowMap = null;
            Map<String, Map<String, String>> map = new HashMap<>();

            while (result.next()) {
                rowMap = new HashMap<>();
                for (int j = 1; j <= columnCount; j++) {
//                    rowMap.put(rsmd.getColumnName(j), result.getString(rsmd.getColumnName(j)));
                    rowMap.put(rsmd.getColumnLabel(j), result.getString(j));

                }
                map.put(result.getString(singlePrimary.name), rowMap);
            }
            rsmd = null;
            result.close();
            return map;
        } catch (SQLException e) {
            //e.printStackTrace();
            RSLogger.ErrorLogInfo("ExecuteSqlSelectReturnMap ExecuteSqlSelect err sql:" + sqlStr + "exception.msg" + e.getLocalizedMessage(), e);
            throw new Exception("ExecuteSqlSelectReturnMap ExecuteSqlSelect err sql:" + sqlStr + "exception.msg" + e.getLocalizedMessage());

        } finally {
            DBHelper.CloseConnection(result, stmt, conn);
        }
    }

    /**
     * 执行sql语句查询
     *
     * @param rsid
     * @param sqlStr
     * @return
     */
    public static String ExecuteSqlSelectOne(String rsid, String sqlStr) throws Exception {
        Connection conn = null;
        Statement stmt = null;
        ResultSet result = null;
        String resultStr = null;
        try {
            conn = DBHelper.ConnectSybase(rsid);
            stmt = conn.createStatement();
            result = stmt.executeQuery(sqlStr);
            ResultSetMetaData rsmd = result.getMetaData();
            int columnCount = rsmd.getColumnCount();
            if (result.getRow() > 1 || result.getRow() < 0) {
                return resultStr;
            }
            result.next();
            resultStr = result.getString(1);
            rsmd = null;
            result.close();

        } catch (SQLException e) {
            RSLogger.ErrorLogInfo("ExecuteSqlSelect err sql:" + sqlStr + "exception.msg" + e.getLocalizedMessage());
        } finally {
            DBHelper.CloseConnection(result, stmt, conn);
        }
        return resultStr;
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
            RSLogger.ErrorLogInfo("ExecuteSqlSelect err sql:" + sqlStr + "exception.msg" + e.getLocalizedMessage(), e);
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

        Connection conn = null;
        Statement stmt = null;
        ResultSet result = null;
        Set<String> insertSqls = new HashSet<>();
        //获取数据库连接
        try {
            DBDetailModel dbDetailModel = GetRSIDModel(rsid);
            for (TableInfoModel dbTableInfo : dbDetailModel.dbTableInfos) {
                for (TableDetailModel tableDetail : dbTableInfo.tableDetails) {
                    insertSqls.add(String.format("insert into tableinfo1(tableid,name,type,length)values(%s,'%s','%s',%s)",
                            tableDetail.tbId, tableDetail.name, tableDetail.dataType.toString(), tableDetail.dataLength));
                }
            }

            conn = DBHelper.ConnectSybase(rsid);
            stmt = conn.createStatement();
            int iRet;
            for (String insertSql : insertSqls) {
                iRet = stmt.executeUpdate(insertSql);
                if (iRet != 1) {
                    return new ExecuteResultParam(-2, "insert data error.sql: " + insertSql);
                }
            }
            return new ExecuteResultParam(0, "success");

        } catch (Exception e) {
            RSLogger.ErrorLogInfo(e.getLocalizedMessage());
            return new ExecuteResultParam(-1, e.getLocalizedMessage());
        } finally {
            DBHelper.CloseConnection(result, stmt, conn);
        }

    }

    private static Set<DBDetailModel> dbModel = new HashSet<>();

    private static ExecuteResultParam loadDBInfo(String rsid) throws Exception {
        Connection conn = null;
        Statement stmt = null;
        ResultSet result = null;
        Set<TableInfoModel> tableInfos = new HashSet<>();
        //获取数据库连接
        try {
            String sqlForTableName = "select ob.* from sysobjects ob where ob.type in ('U','V') ";
            String sqlInsertTableInfo = null;
            StringBuffer sqlTableDetailWhere = new StringBuffer();
            conn = DBHelper.ConnectSybase(rsid);
            stmt = conn.createStatement();
            result = stmt.executeQuery(sqlForTableName);
            //ResultSetMetaData rsmd = result.getMetaData();
            //int columnCount = rsmd.getColumnCount();
            //添加用户表
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
            //添加用户表的数据类型
            sqlForTableName = String.format("select c.id as id, c.length as length , o.name as tbName , c.name,c.type, c.status,c.usertype  ,t.name as dataTypeName from sysobjects o inner join syscolumns c on c.id = o.id inner join systypes t on t.usertype = c.usertype where o.type in ('U','V') and o.name in (%s) order by c.id", sqlTableDetailWhere.subSequence(0, sqlTableDetailWhere.lastIndexOf(",")));
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
                if (tempTabelDetail.status.equals("128")) {
                    temptableInfo.identityKey = tempTabelDetail;
                }
                temptableInfo.tableDetails.add(tempTabelDetail);
            }
            result1.close();
            String tempPrimaryName = null;
            ResultSet result2 = stmt.executeQuery(SearchTabelPrimaryKeyStrByUTable);
//            while (result.next()) {
//                if (tableName.equals(result.getString("tableName")) && result.getString("indexDescription").indexOf("primary key") > 0) {
//                    return result.getString("columnName");
//                }
//            }
            while (result2.next()) {
                for (TableInfoModel tableInfo : tableInfos) {
                    if (tableInfo.tbName.equals(result2.getString("tabname"))) {
                        tempPrimaryName = result2.getString("columnname");
                        for (TableDetailModel tableDetail : tableInfo.tableDetails) {
                            if (tableDetail.name.equals(tempPrimaryName)) {
                                tableDetail.isPrimaryKey = true;
                                tableInfo.tbPrimaryKeys.add(tableDetail);
                                break;
                            }
                        }
                    }
                }
//                if (tempPrimaryName == null || tempPrimaryName.isEmpty()) {
//                    //log no primary table .
//                    common.RSLogger.SetUpLogInfo(String.format("special loadDBInformation  tableName is %s , rsid is %s  no primary.", tableInfo.tbName, rsid));
//                    //throw new Exception(String.format("loadDBInformation error tableName is %s , rsid is %s", tableInfo.tbName, rsid));
//                    //continue;
//                }

            }

            result2.close();
            DBDetailModel dbDetailModel = new DBDetailModel();
            dbDetailModel.rsId = rsid;
            dbDetailModel.dbName = rsid;
            dbDetailModel.dbTableInfos = tableInfos;
            dbModel.add(dbDetailModel);
            dbDetailModel = null;
            tableInfos = null;
            RSLogger.SetUpLogInfo(String.format("loadDBInfo success rsid :%s,", rsid));
            return new ExecuteResultParam(0, "success");
            //查询用户表中的column信息
        } catch (Exception e) {
            RSLogger.ErrorLogInfo(String.format("loadDBInfo error %s, rsid :%s,", e.getLocalizedMessage(), rsid), e);
            RSLogger.SetUpLogInfo(String.format("loadDBInfo error %s, rsid :%s,", e.getLocalizedMessage(), rsid));
            //return new ExecuteResultParam(-1, e.getLocalizedMessage());
            throw new Exception(String.format("loadDBInfo error %s, rsid :%s,", e.getLocalizedMessage(), rsid));
        } finally {
            DBHelper.CloseConnection(result, stmt, conn);
        }
    }

    private static DBDetailModel GetRSIDModel(String rsid) throws Exception {
        // LoadDBInfo();
        for (DBDetailModel next : dbModel) {
            if (next.rsId.equals(rsid)) {
                return next;
            }
        }
        return null;
    }

    public static boolean LoadDBInfo() throws Exception {
        if (dbModel != null) {
            for (DBDetailModel dbModel1 : dbModel) {
                dbModel1.clear();
            }
            dbModel.clear();
        }
        Set<SystemSetModel> modelSet = DeployInfo.GetSystemSets();
        for (SystemSetModel next : modelSet) {
            loadDBInfo(next.id);
        }
        return true;
    }

    /**
     * 在本地数据库信息中读取 表信息，
     *
     * @param tableName
     * @param RSID
     * @return
     * @throws Exception
     */
    private static TableInfoModel FindTableInformation(String tableName, String RSID) throws Exception {
        DBDetailModel dbDetailModel = GetRSIDModel(RSID);
        for (TableInfoModel tableInfo : dbDetailModel.dbTableInfos) {
            if (tableInfo.tbName.equals(tableName)) {
                return tableInfo;
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

    private static final String SearchTablePrimaryKeyStrByUserTable = "select 'tableName' = object_name(sc.id),\n"
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
            + "from sysindexes sc where  (sc.status & 64) = 0  and object_name(sc.id) in (select ob.name from sysobjects ob where ob.type='U' )\n"
            + "order by sc.id ";

    /**
     * 查询用户表，和主键列
     */
    private static final String SearchTabelPrimaryKeyStrByUTable = "select  tabname ,columnname from \n"
            + "(  SELECT  object_name(id) tabname,  index_col( object_name(id) ,indid,1) columnname  FROM sysindexes  WHERE status & 2048=2048 \n"
            + "    union \n"
            + "    SELECT  object_name(id),  index_col( object_name(id) ,indid,2)  FROM sysindexes    WHERE status & 2048=2048 \n"
            + "    union\n"
            + "    SELECT  object_name(id),  index_col( object_name(id) ,indid,3)  FROM sysindexes  WHERE status & 2048=2048 \n"
            + "    union \n"
            + "    SELECT  object_name(id),    index_col( object_name(id) ,indid,4)  FROM sysindexes  WHERE status & 2048=2048 \n"
            + "    union  \n"
            + "    select  object_name(id),  index_col( object_name(id) ,indid,5)    FROM sysindexes  WHERE status & 2048=2048 \n"
            + "    union \n"
            + "    SELECT  object_name(id),  index_col( object_name(id) ,indid,6)  FROM sysindexes    WHERE status & 2048=2048 ) pk\n"
            + "where  columnname is not null";

    /*
     查询表identity列sql语句
     */
    private static final String SearchTableIdentityKeyStr = "select object_name(sc.id) as 'tn' , sc.name as 'cn' from syscolumns sc inner join sysobjects so on sc.id = so.id where  so.name='%s' and  sc.status =128 ";
}
