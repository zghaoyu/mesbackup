package com.cncmes.utils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DTOUtils {
	/**
	 * @param dtoPackageName
	 * @return Map<Key,Value> Key is the class name and Value is the data table name
	 * @throws ClassNotFoundException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	public static Map<String,String> getDTODataTables(String dtoPackageName) throws ClassNotFoundException, NoSuchFieldException, SecurityException, InstantiationException, IllegalAccessException, URISyntaxException, IOException{
		Map<String,String> tableNames = new LinkedHashMap<String,String>();
		String[] classNames = ClassUtils.getClassNames(dtoPackageName);
		if(null != classNames){
			for(int i=0; i<classNames.length; i++){
				tableNames.put(classNames[i], getDTORealTableName(classNames[i]));
			}
		}else{
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
	public static Map<String,Object> getDTOFields(String className) throws ClassNotFoundException, SecurityException{
		Map<String,Object> fieldsMap = new LinkedHashMap<String,Object>();
		@SuppressWarnings({ "rawtypes" })
		Class clazz = Class.forName(className);
		
		Field[] fs = clazz.getDeclaredFields();
		for(Field f:fs){
			if(!"realTableName".equals(f.getName())){
				fieldsMap.put(f.getName(), f.getType());
			}
		}
		
		return fieldsMap;
	}
	
	public static String getDTORealTableName(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException{
		String tableName = "";
		
		@SuppressWarnings("rawtypes")
		Class clazz = Class.forName(className);
		Object dto = clazz.newInstance();
		Field f = clazz.getDeclaredField("realTableName");
		f.setAccessible(true);
		tableName = (String) f.get(dto);
		
		return tableName;
	}
}
