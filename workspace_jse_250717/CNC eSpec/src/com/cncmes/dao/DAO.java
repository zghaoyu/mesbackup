package com.cncmes.dao;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * 
 * @author W000055
 * Interfaces of Data Access Object
 *
 */
public interface DAO {
	/**
	 * 
	 * @return the record count of current database query
	 * @throws SQLException
	 */
	public int count() throws SQLException;
	
	/**
	 * 
	 * @param id the record to be deleted
	 * @return the affected record count after operation
	 * @throws SQLException
	 */
	public int delete(int id) throws SQLException;
	
	/**
	 * 
	 * @param dto the Data Transfer Object to be saved
	 * @return the affected record count after operation
	 * @throws SQLException
	 */
	public int add(Object dto) throws SQLException;
	
	/**
	 * 
	 * @param dto the Data Transfer Object to be updated
	 * @return the affected record count after operation
	 * @throws SQLException
	 */
	public int update(Object dto) throws SQLException;
	
	/**
	 * 
	 * @param id the record associated with this id to be queried
	 * @return the Data Transfer Object after operation
	 * @throws SQLException
	 */
	public Object findById(int id) throws SQLException;
	
	/**
	 * 
	 * @param Page the data page to be queried
	 * @param Limit the record count to be queried
	 * @return Data Transfer Objects in ArrayList
	 * @throws SQLException
	 */
	public ArrayList<Object> findByPage(int Page,int Limit) throws SQLException;
	
	/**
	 * 
	 * @return Data Transfer Objects in ArrayList
	 * @throws SQLException
	 */
	public ArrayList<Object> findAll() throws SQLException;
	
	/**
	 * 
	 * @param paras the parameters for conditional query
	 * @param vals the values for conditional query
	 * @return Data Transfer Objects in ArrayList
	 * @throws SQLException
	 */
	public ArrayList<Object> findByCnd(String[] paras,String[] vals) throws SQLException;
	
	/**
	 * 
	 * @return data fields of the Data Transfer Object in String Array
	 */
	public String[] getDataFields();
}
