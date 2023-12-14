package com.cncmes.utils;

import com.cncmes.data.SystemConfig;
import com.cncmes.handler.ResultSetHandler;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class MonitorDBUtils {
	private static String URL = null;
	private static String USERNAME = null;
	private static String USERPWD = null;
	private static String DRIVER = null;

	private MonitorDBUtils(){}
	static{
		try {
			XmlUtils.parseSystemConfig();
			getCommonSettings();
			
			Class.forName(DRIVER);
		} catch (Exception e) {
			LogUtils.errorLog("MonitorDBUtils fails to load system config:"+e.getMessage());
		}
	}

	private static void getCommonSettings() {
		try {
			SystemConfig sysCfg = SystemConfig.getInstance();
			LinkedHashMap<String,Object> config = sysCfg.getMonitorDatabaseCfg();
			URL = (String) config.get("url");
			USERNAME = (String) config.get("username");
			USERPWD = (String) config.get("userpwd");
			DRIVER = (String) config.get("driver");
//			USERPWD = DesUtils.decrypt(USERPWD).replace(",", "");
//			USERNAME = DesUtils.decrypt(USERNAME).replace(",", "");
		} catch (Exception e) {
		}
	}
	
	public static Connection getConnection() throws SQLException{
		Connection conn = null;
		try {
			getCommonSettings();
			conn = DriverManager.getConnection(URL, USERNAME, USERPWD);
		} catch (SQLException e) {
			throw e;
		}
		return conn;
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
	
	public static int update(String sql, Object...args) throws SQLException{
		Connection conn = null;
		PreparedStatement ps = null;
		int rows = -1;
		
		try{
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			if(null != args){
				for(int i=0; i<args.length; i++){
					ps.setObject(i+1, args[i]);
				}
			}
			rows = ps.executeUpdate();
		}catch(SQLException e){
			throw e;
		}finally{
			close(null, ps, conn);
		}
		
		return rows;
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
			throw e;
		}finally{
			close(rs, ps, conn);
		}
		
		return list;
	}
	
	public static int count(String sql) throws SQLException{
		int cnt = -1;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try{
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs.next()) cnt = rs.getInt(1);
		}catch(SQLException e){
			throw e;
		}finally{
			close(rs, ps, conn);
		}
		
		return cnt;
	}
}
