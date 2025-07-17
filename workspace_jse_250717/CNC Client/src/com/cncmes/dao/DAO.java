package com.cncmes.dao;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Interfaces of Database Access Object
 * @author LI ZI LONG
 *
 */
public interface DAO {
	/**
	 * @return the total record count
	 * @throws SQLException
	 */
	public int count() throws SQLException;
	
	/**
	 * @param id the record ID to delete
	 * @return true if operation is successful
	 * @throws SQLException
	 */
	public int delete(int id) throws SQLException;
	
	/**
	 * @param dto the Data Transferring Object
	 * @return the affected record count
	 * @throws SQLException
	 */
	public int add(Object dto) throws SQLException;
	public int monitorDBadd(Object dto) throws SQLException;

	/**
	 * @param dto the Data Transferring Object
	 * @return the affected record count
	 * @throws SQLException
	 */
	public int update(Object dto) throws SQLException;
	public int monitorDBupdate(Object dto) throws SQLException;

	/**
	 * @param id the record ID to be queried
	 * @return the data object from the query
	 * @throws SQLException
	 */
	public Object findById(int id) throws SQLException;
	
	/**
	 * @param Page the data page number to be queried
	 * @param Limit the record count in a page
	 * @return data objects from the query
	 * @throws SQLException
	 */
	public ArrayList<Object> findByPage(int Page,int Limit) throws SQLException;
	
	/**
	 * @return all data objects from the query
	 * @throws SQLException
	 */
	public ArrayList<Object> findAll() throws SQLException;
	
	/**
	 * @param paras conditional parameters name for the query
	 * @param vals conditional parameters value for the query
	 * @return data objects from the query
	 * @throws SQLException
	 */
	public ArrayList<Object> findByCnd(String[] paras,String[] vals) throws SQLException;
	
	/**
	 * @return all data fields
	 */
	public String[] getDataFields();

	public ArrayList<Object> monitorDataBaseFindByCnd(String[] paras,String[] vals) throws SQLException;
}
