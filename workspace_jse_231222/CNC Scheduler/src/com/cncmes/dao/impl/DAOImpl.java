package com.cncmes.dao.impl;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.cncmes.dao.DAO;
import com.cncmes.handler.ResultSetHandler;
import com.cncmes.utils.DBUtils;
import com.cncmes.utils.DTOUtils;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.SqlServerUtils;

public class DAOImpl implements DAO {
	private String tableName;
	private String dtoClassName;
	private Map<String,Object> fieldsMap;
	
	public DAOImpl(String dtoClassName){
		try {
			this.dtoClassName = dtoClassName;
			this.tableName = DTOUtils.getDTORealTableName(dtoClassName);
			this.fieldsMap = DTOUtils.getDTOFields(dtoClassName);
		} catch (Exception e) {
			LogUtils.errorLog("DAOImpl(String:"+dtoClassName+") fail");
		}
	}
	public DAOImpl(String dtoClassName,Boolean nothaveRealTable){
		try {
			this.dtoClassName = dtoClassName;
			this.fieldsMap = DTOUtils.getDTOFields(dtoClassName);
		} catch (Exception e) {
			LogUtils.errorLog("DAOImpl(String:"+dtoClassName+") fail");
		}
	}
	
	@Override
	public int count() throws SQLException {
		String sql = "select count(id) from " + tableName;
		return DBUtils.count(sql);
	}

	@Override
	public int delete(int id) throws SQLException {
		String sql = "delete from " + tableName + " where id=?";
		return DBUtils.update(sql, id);
	}

	@Override
	public int add(Object dto) throws SQLException {
		@SuppressWarnings("rawtypes")
		Class clazz;
		try {
			clazz = Class.forName(dtoClassName);
		} catch (ClassNotFoundException e) {
			throw new SQLException("Reflect DTO fail");
		}
		Set<String> set = fieldsMap.keySet();
		Iterator<String> it = set.iterator();
		String fields = "";
		String vals = "";
		String fname = "";
		Object[] setVal = new Object[set.size() - 1];
		int i = -1;
		while(it.hasNext()){
			fname = it.next();
			if(!"id".equals(fname)){
				i++;
				Field f;
				try {
					f = clazz.getDeclaredField(fname);
					f.setAccessible(true);
					setVal[i] = f.get(dto);
				} catch (Exception e) {
					throw new SQLException("Reflect DTO Declared Field fail");
				}
				
				if("".equals(fields)){
					fields = fname;
					vals = "?";
				}else{
					fields = fields + "," + fname;
					vals = vals + "," + "?";
				}
			}
		}
		
		String sql = "insert into " + tableName + "(" + fields + ") values(" + vals + ")";
		return DBUtils.update(sql, setVal);
	}

	@Override
	public int update(Object dto) throws SQLException {
		@SuppressWarnings("rawtypes")
		Class clazz;
		try {
			clazz = Class.forName(dtoClassName);
		} catch (ClassNotFoundException e) {
			throw new SQLException("Reflect DTO fail");
		}
		Set<String> set = fieldsMap.keySet();
		Iterator<String> it = set.iterator();
		String fields = "";
		String fname = "";
		Object[] setVal = new Object[set.size()];
		int i = -1;
		int id = 0;
		while(it.hasNext()){
			fname = it.next();
			if(!"id".equals(fname)){
				i++;
				Field f;
				try {
					f = clazz.getDeclaredField(fname);
					f.setAccessible(true);
					setVal[i] = f.get(dto);
				} catch (Exception e) {
					throw new SQLException("Reflect DTO Declared Field fail");
				}
				
				if("".equals(fields)){
					fields = fname + "=?";
				}else{
					fields = fields + "," + fname + "=?";
				}
			}else{
				try {
					Field f = clazz.getDeclaredField(fname);
					f.setAccessible(true);
					id = (int) f.get(dto);
				} catch (Exception e) {
					throw new SQLException("Reflect DTO Declared Field fail");
				}
			}
		}
		setVal[i + 1] = id;
		
		String sql = "update " + tableName + " set " + fields + " where id=?";
		return DBUtils.update(sql, setVal);
	}
	
	@Override
	public String[] getDataFields(){
		Set<String> set = fieldsMap.keySet();
		Iterator<String> it = set.iterator();
		String fields = "id";
		String fname = "";
		while(it.hasNext()){
			fname = it.next();
			if(!"id".equals(fname)){
				fields = fields + "," + fname;
			}
		}
		String[] fs = fields.split(",");
		return fs;
	}
	
	@Override
	public Object findById(int id) throws SQLException {
		Set<String> set = fieldsMap.keySet();
		Iterator<String> it = set.iterator();
		String fields = "id";
		String fname = "";
		while(it.hasNext()){
			fname = it.next();
			if(!"id".equals(fname)){
				fields = fields + "," + fname;
			}
		}
		
		String sql = "select " + fields + " from " + tableName + " where id=?";
		ArrayList<Object> vos = DBUtils.query(sql, new ResultSetHandle(), id);
		
		Object vo = null;
		if(null != vos){
			vo = vos.get(0);
		}
		return vo;
	}

	@Override
	public ArrayList<Object> findByPage(int Page, int Limit) throws SQLException {
		Set<String> set = fieldsMap.keySet();
		Iterator<String> it = set.iterator();
		String fields = "id";
		String fname = "";
		while(it.hasNext()){
			fname = it.next();
			if(!"id".equals(fname)){
				fields = fields + "," + fname;
			}
		}
		
		String sql = "select " + fields + " from " + tableName + " limit " + ((Page-1)*Limit) + "," + Limit;
		return DBUtils.query(sql, new ResultSetHandle());
	}

	@Override
	public ArrayList<Object> findAll() throws SQLException {
		Set<String> set = fieldsMap.keySet();
		Iterator<String> it = set.iterator();
		String fields = "id";
		String fname = "";
		while(it.hasNext()){
			fname = it.next();
			if(!"id".equals(fname)){
				fields = fields + "," + fname;
			}
		}
		
		String sql = "select " + fields + " from " + tableName;
		return DBUtils.query(sql, new ResultSetHandle());
	}

	public List<Object> findOrderSchedulerByMoCode(String moCode) throws SQLException {
		String sql = "SELECT mom_order.MoId ,mom_order.MoCode,sd.dPreDate as PERequestDate,mom_orderdetail.Qty from mom_order \n" +
				"left join mom_orderdetail on mom_order.MoId = mom_orderdetail.MoId\n" +
				"left join SO_SOMain on SO_SOMain.cSOCode = mom_orderdetail.OrderCode\n" +
				"LEFT join SO_SODetails sd on SO_SOMain.ID = sd.ID and mom_orderdetail.InvCode=sd.cInvCode\n" +
				"WHERE mom_order.MoCode = '"+moCode+"'";
		return SqlServerUtils.query(sql,new ResultSetHandle());
	}
	
	@Override
	public ArrayList<Object> findByCnd(String[] paras, String[] vals) throws SQLException {
		int page = 0;
		int limit = 0;
		
		Set<String> set = fieldsMap.keySet();
		Iterator<String> it = set.iterator();
		String fields = "id";
		String fname = "";
		while(it.hasNext()){
			fname = it.next();
			if(!"id".equals(fname)){
				fields = fields + "," + fname;
			}
		}
		
		String cnd = "";
		Object type = null;
		if(null != paras && null != vals){
			if(paras.length == vals.length){
				for(int i=0; i<paras.length; i++){
					if("page".equals(paras[i])){
						page = Integer.parseInt(vals[i]);
					}else if("limit".equals(paras[i])){
						limit = Integer.parseInt(vals[i]);
					}else{
						type = fieldsMap.get(paras[i]);
						if(null == type){
							throw new SQLException("Para " + paras[i] + " is unrecognized");
						}else{
							if(type.equals(String.class)){
								if("".equals(cnd)){
									cnd = paras[i] + "='" + vals[i] + "'";
								}else{
									cnd += " AND " + paras[i] + "='" + vals[i] + "'";
								}
							}else{
								if("".equals(cnd)){
									cnd = paras[i] + "=" + vals[i];
								}else{
									cnd += " AND " + paras[i] + "=" + vals[i];
								}
							}
						}
					}
				}
			}else{
				throw new SQLException("Condition array's size is not equal");
			}
		}
		
		if(!"".equals(cnd)) cnd = " WHERE " + cnd;
		if(page > 0 && limit > 0) cnd += " limit " + ((page-1)*limit) + "," + limit;
		
		String sql = "select " + fields + " from " + tableName + cnd;
		return DBUtils.query(sql, new ResultSetHandle());
	}
	
	class ResultSetHandle implements ResultSetHandler{
		@Override
		public ArrayList<Object> doHandle(ResultSet rs) throws SQLException {
			ArrayList<Object> list = null;
			if(null != rs){
				@SuppressWarnings("rawtypes")
				Class clazz;
				try {
					clazz = Class.forName(dtoClassName);
				} catch (ClassNotFoundException e) {
					throw new SQLException("Reflect DTO fail");
				}
				
				while(rs.next()){
					if(null == list) list = new ArrayList<Object>();
					
					Object vo;
					try {
						vo = clazz.newInstance();
						Field f = clazz.getDeclaredField("id");
						f.setAccessible(true);
						f.set(vo, rs.getInt(1));
						
						Set<String> set = fieldsMap.keySet();
						Iterator<String> it = set.iterator();
						String fname = "";
						Object type;
						int idx = 1;
						while(it.hasNext()){
							fname = it.next();
							if(!"id".equals(fname)){
								idx++;
								if(idx>rs.getMetaData().getColumnCount())
								{
									continue;
								}
								f = clazz.getDeclaredField(fname);
								f.setAccessible(true);
								type = fieldsMap.get(fname);
								if(type.equals(boolean.class)) f.set(vo, rs.getBoolean(idx));
								if(type.equals(byte.class)) f.set(vo, rs.getByte(idx));
								if(type.equals(short.class)) f.set(vo, rs.getShort(idx));
								if(type.equals(int.class)) f.set(vo, rs.getInt(idx));
								if(type.equals(long.class)) f.set(vo, rs.getLong(idx));
								if(type.equals(float.class)) f.set(vo, rs.getFloat(idx));
								if(type.equals(double.class)) f.set(vo, rs.getDouble(idx));
								if(type.equals(String.class)) f.set(vo, rs.getString(idx));
								if(type.equals(Date.class)) f.set(vo, rs.getDate(idx));

							}
						}
					} catch (Exception e) {
						throw new SQLException("Create DTO fail");
					}
					
					list.add(vo);
				}
			}
			return list;
		}
	}

}
