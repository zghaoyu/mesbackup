package com.cncmes.dao;

import java.sql.SQLException;
import java.util.ArrayList;

public interface DAO {
	public int count() throws SQLException;
	public int delete(int id) throws SQLException;
	public int add(Object dto) throws SQLException;
	public int update(Object dto) throws SQLException;
	public Object findById(int id) throws SQLException;
	public ArrayList<Object> findByPage(int Page,int Limit) throws SQLException;
	public ArrayList<Object> findAll() throws SQLException;
	public ArrayList<Object> findByCnd(String[] paras,String[] vals) throws SQLException;
	public String[] getDataFields();
}
