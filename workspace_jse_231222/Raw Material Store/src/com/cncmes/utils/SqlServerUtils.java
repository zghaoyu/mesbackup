package com.cncmes.utils;

import com.cncmes.data.SystemConfig;
import com.cncmes.handler.ResultSetHandler;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * *Eddie
 * *
 */
public class SqlServerUtils {
    private static String URL = null;
    private static String USERNAME = null;
    private static String USERPWD = null;
    private static String DRIVER = null;
    private SqlServerUtils(){}
    static{
        try {
            XmlUtils.parseSystemConfig();
            getCommonSettings();

            Class.forName(DRIVER);
        } catch (Exception e) {
            LogUtils.errorLog("SqlServerUtils fails to load system config:"+e.getMessage());
        }
    }
    public static void getCommonSettings() {
        try {
            SystemConfig sysCfg = SystemConfig.getInstance();
            LinkedHashMap<String,Object> config = sysCfg.getSqlServerCfg();
            URL = (String) config.get("url");
            USERNAME = (String) config.get("username");
            USERPWD = (String) config.get("userpwd");
            DRIVER = (String) config.get("driver");
            USERPWD = DesUtils.decrypt(USERPWD).replace(",", "");
            USERNAME = DesUtils.decrypt(USERNAME).replace(",", "");
        } catch (Exception e) {
        }
    }
    public static Connection getConnection() throws SQLException {
        Connection conn = null;
        try {
            getCommonSettings();
            conn = DriverManager.getConnection(URL, USERNAME, USERPWD);
        } catch (SQLException e) {
            throw e;
        }
        return conn;
    }

    public static ArrayList<Object> query(String sql, ResultSetHandler rsHdl, Object...args) throws SQLException{
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<Object> list = null;

        try{
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            if(null != args){
                for(int i=0; i<args.length; i++){
                    ps.setObject(i+1, args[i]);
                }
            }
            rs = ps.executeQuery();
            list = rsHdl.doHandle(rs);

        }catch(SQLException e){
            System.out.println("query error:please input valid value");
            throw e;

        }finally{
            close(rs, ps, conn);
        }

        return list;
    }
    public static void close(ResultSet rs, Statement stmt, Connection conn){
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
