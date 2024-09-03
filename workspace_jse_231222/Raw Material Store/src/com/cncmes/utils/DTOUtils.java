package com.cncmes.utils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.cncmes.dao.DAO;
import com.cncmes.dao.impl.DAOImpl;

public class DTOUtils {
	/**
	 * @param dtoPackageName
	 * @return Map<Key,Value> Key is the class name and Value is the data table
	 *         name
	 * @throws ClassNotFoundException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static Map<String, String> getDTODataTables(String dtoPackageName)
			throws ClassNotFoundException, NoSuchFieldException, SecurityException, InstantiationException,
			IllegalAccessException, URISyntaxException, IOException {
		Map<String, String> tableNames = new LinkedHashMap<String, String>();
		String[] classNames = ClassUtils.getClassNames(dtoPackageName);
		if (null != classNames) {
			for (int i = 0; i < classNames.length; i++) {
				tableNames.put(classNames[i], getDTORealTableName(classNames[i]));
			}
		} else {
			tableNames = null;
		}
		return tableNames;
	}

	/**
	 * @param className
	 * @return Map<Key,Value> Key is the field name and Value is the field type
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 */
	public static Map<String, Object> getDTOFields(String className) throws ClassNotFoundException, SecurityException {
		Map<String, Object> fieldsMap = new LinkedHashMap<String, Object>();
		@SuppressWarnings({ "rawtypes" })
		Class clazz = Class.forName(className);

		Field[] fs = clazz.getDeclaredFields();
		for (Field f : fs) {
			if (!"realTableName".equals(f.getName())) {
				fieldsMap.put(f.getName(), f.getType());
			}
		}

		return fieldsMap;
	}

	public static String getDTORealTableName(String className) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, NoSuchFieldException, SecurityException {
		String tableName = "";

		@SuppressWarnings("rawtypes")
		Class clazz = Class.forName(className);
		Object dto = clazz.newInstance();
		Field f = clazz.getDeclaredField("realTableName");
		f.setAccessible(true);
		tableName = (String) f.get(dto);

		return tableName;
	}

	public static boolean saveDataIntoDB(String dtoClassName, String[] title, Object[][] data) throws SQLException {
		boolean success = true;
		boolean changeFlag = false;
		String newVal;
		@SuppressWarnings("rawtypes")
		Class clazz;
		Object dto;
		Field f;
		Map<String, Object> fieldsMap;
		Object type;
		if (null == title || null == data)
			return false;

		int rowCount = data.length;
		int colCount = title.length;

		try {
			clazz = Class.forName(dtoClassName);
			dto = clazz.newInstance();
		} catch (Exception e) {
			success = false;
			throw new SQLException("Reflect DTO fail");
		}

		try {
			fieldsMap = DTOUtils.getDTOFields(dtoClassName);
		} catch (Exception e1) {
			success = false;
			throw new SQLException("Reflect fields map fail");
		}

		DAO dao = new DAOImpl(dtoClassName);
		for (int j = 0; j < rowCount; j++) {
			// dataID = data[j][0];
			for (int i = 0; i < colCount; i++) {
				newVal = "" + data[j][i];
				if (null != data[j][i] && !"".equals(newVal)) {
					try {
						f = clazz.getDeclaredField(title[i]);
						f.setAccessible(true);
						type = fieldsMap.get(title[i]);
						if (type.equals(boolean.class))
							f.set(dto, Boolean.parseBoolean(newVal.trim()));
						if (type.equals(byte.class))
							f.set(dto, Byte.parseByte(newVal.trim()));
						if (type.equals(short.class))
							f.set(dto, Short.parseShort(newVal.trim()));
						if (type.equals(int.class))
							f.set(dto, Integer.parseInt(newVal.trim()));
						if (type.equals(long.class))
							f.set(dto, Long.parseLong(newVal.trim()));
						if (type.equals(float.class))
							f.set(dto, Float.parseFloat(newVal.trim()));
						if (type.equals(double.class))
							f.set(dto, Double.parseDouble(newVal.trim()));
						if (type.equals(String.class))
							f.set(dto, newVal.trim());
					} catch (Exception e) {
						success = false;
						throw new SQLException("Reflect DTO field fail:" + e.getMessage());
					}
					changeFlag = true;
				}
			}
			if (changeFlag) {
				// if (null != dataID && Integer.valueOf("" + dataID) > 0) {
				// try {
				// dao.update(dto);
				// } catch (Exception e) {
				// success = false;
				// throw new SQLException("Update data failed:" +
				// e.getMessage());
				// }
				// } else {
				try {
					dao.add(dto);
				} catch (Exception e) {
					success = false;
					throw new SQLException("Add data failed:" + e.getMessage());
				}
				// }
			}
		}

		return success;
	}

	public static ArrayList<Object> getDataFromDB(String dtoClassName, String[] cndParasName, String[] cndParasVal) {
		String[] title = null;
		Object[][] data = null;
		DAO dao = new DAOImpl(dtoClassName);
		title = dao.getDataFields();// id is the first element

		try {
			ArrayList<Object> vos = null;
			if (null != cndParasName && null != cndParasVal) {
				vos = dao.findByCnd(cndParasName, cndParasVal);
			} else {
				vos = dao.findAll();
			}
			if (null != vos) {
				data = new Object[vos.size()][title.length];
				@SuppressWarnings("rawtypes")
				Class clazz = Class.forName(dtoClassName);
				Field field = null;
				for (int i = 0; i < vos.size(); i++) {
					for (int j = 0; j < title.length; j++) {
						field = clazz.getDeclaredField(title[j]);
						field.setAccessible(true);
						data[i][j] = field.get(vos.get(i));
					}
				}
			}
		} catch (Exception e1) {
		}

		ArrayList<Object> rtnData = new ArrayList<Object>();
		rtnData.add(0, title);
		rtnData.add(1, data);

		return rtnData;
	}
}
