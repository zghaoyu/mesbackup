package com.cncmes.utils;

import java.sql.*;
import java.util.*;

import com.cncmes.data.SystemConfig;
import com.cncmes.handler.ResultSetHandler;

public class DBUtils {
	private static String URL = null;
	private static String USERNAME = null;
	private static String USERPWD = null;
	private static String DRIVER = null;
	
	private DBUtils(){}
	static{
		try {
			XmlUtils.parseSystemConfig();
			getCommonSettings();
			
			Class.forName(DRIVER);
		} catch (Exception e) {
			LogUtils.errorLog("DBUtils fails to load system config:"+e.getMessage());
		}
	}

	private static void getCommonSettings() {
		try {
			SystemConfig sysCfg = SystemConfig.getInstance();
			LinkedHashMap<String,Object> config = sysCfg.getDatabaseCfg();
			URL = (String) config.get("url");
			USERNAME = (String) config.get("username");
			USERPWD = (String) config.get("userpwd");
			DRIVER = (String) config.get("driver");
			USERPWD = DesUtils.decrypt(USERPWD).replace(",", "");
			USERNAME = DesUtils.decrypt(USERNAME).replace(",", "");
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
	private static List convertList(ResultSet rs) throws SQLException {
		List list = new ArrayList();
		ResultSetMetaData md = rs.getMetaData();//获取键名
		int columnCount = md.getColumnCount();//获取行的数量
		while (rs.next()) {
			Map rowData = new HashMap();//声明Map
			for (int i = 1; i <= columnCount; i++) {
				rowData.put(md.getColumnName(i), rs.getObject(i));//获取键名及值
			}
			list.add(rowData);
		}
		return list;
	}
	public static List execute(String sql)
	{
		Connection conn = null;
		Statement  statement = null;
		try {
			conn = getConnection();
			statement = conn.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			List list = convertList(resultSet);
			conn.close();
			statement.close();
			return list;
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}

		return null;
	}
}
