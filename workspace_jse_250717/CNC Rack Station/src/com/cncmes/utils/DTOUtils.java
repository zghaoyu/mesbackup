package com.cncmes.utils;

//import java.awt.Dimension;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

//import com.cncmes.base.MyTable;
//import com.cncmes.base.MyTableModel;
//import com.cncmes.base.RowHeaderTableModel;
import com.cncmes.dao.DAO;
import com.cncmes.dao.impl.DAOImpl;
import com.cncmes.data.SystemConfig;

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
	
	public static ArrayList<Object> getDataFromDB(String dtoClassName,String[] cndParasName,String[] cndParasVal){
		String[] title = null;
		Object[][] data = null;
		DAO dao = new DAOImpl(dtoClassName);
		title = dao.getDataFields();//id is the first element
		
		try {
			ArrayList<Object> vos = null;
			if(null!=cndParasName && null!=cndParasVal){
				vos = dao.findByCnd(cndParasName, cndParasVal);
			}else{
				vos = dao.findAll();
			}
			if(null != vos){
				data = new Object[vos.size()][title.length];
				@SuppressWarnings("rawtypes")
				Class clazz = Class.forName(dtoClassName);
				Field field = null;
				for(int i=0; i<vos.size(); i++){
					for(int j=0; j<title.length; j++){
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
	
	public static boolean saveDataIntoDB(String dtoClassName, String[] title, Object[][] data) throws SQLException{
		boolean success = true;
		boolean changeFlag = false;
		String newVal;
		@SuppressWarnings("rawtypes")
		Class clazz;
		Object dto;
		Field f;
		Map<String,Object> fieldsMap;
		Object type,dataID;
		if(null==title || null==data) return false;
		
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
		for(int j=0; j<rowCount; j++){
			dataID = data[j][0];
			for(int i=0; i<colCount; i++){
				newVal = ""+data[j][i];
				if(null!=data[j][i] && !"".equals(newVal)){
					try {
						f = clazz.getDeclaredField(title[i]);
						f.setAccessible(true);
						type = fieldsMap.get(title[i]);
						if(type.equals(boolean.class)) f.set(dto, Boolean.parseBoolean(newVal.trim()));
						if(type.equals(byte.class)) f.set(dto, Byte.parseByte(newVal.trim()));
						if(type.equals(short.class)) f.set(dto, Short.parseShort(newVal.trim()));
						if(type.equals(int.class)) f.set(dto, Integer.parseInt(newVal.trim()));
						if(type.equals(long.class)) f.set(dto, Long.parseLong(newVal.trim()));
						if(type.equals(float.class)) f.set(dto, Float.parseFloat(newVal.trim()));
						if(type.equals(double.class)) f.set(dto, Double.parseDouble(newVal.trim()));
						if(type.equals(String.class)) f.set(dto, newVal.trim());
					} catch (Exception e) {
						success = false;
						throw new SQLException("Reflect DTO field fail:"+e.getMessage());
					}
					changeFlag = true;
				}
			}
			if(changeFlag){
				if(null!=dataID && Integer.valueOf(""+dataID)>0){
					try {
						dao.update(dto);
					} catch (Exception e) {
						success = false;
						throw new SQLException("Update data failed:"+e.getMessage());
					}
				}else{
					try {
						dao.add(dto);
					} catch (Exception e) {
						success = false;
						throw new SQLException("Add data failed:"+e.getMessage());
					}
				}
			}
		}
		
		return success;
	}
	
	/**
	 * show the data table with column header mode
	 * @param myTable reference of the data table
	 * @param dtoClassName the dto of the data
	 * @param addNewRow whether it is a new row
	 * @param cndParasName null means showing all data; not null means by searching conditions on parameters name
	 * @param cndParasVal null means showing all data; not null means by searching conditions on parameters value
	 */
//	public static void setDataTable(MyTable myTable,String dtoClassName,int addNewRow,String[] cndParasName,String[] cndParasVal){
//		if(null==myTable) return;
//		myTable.setModel(new MyTableModel(dtoClassName,addNewRow,cndParasName,cndParasVal));
//		myTable.setRowHeight(28);
//		myTable.setRenderFields(cndParasName);
//		fitTableColumns(myTable,1.2f);
//	}
	
	/**
	 * show all data stored in newTitle and newData
	 * @param myTable reference of the data table
	 * @param dtoClassName dto of the data
	 * @param addNewRow whether there is new data row
	 * @param newTitle the title of the data
	 * @param newData the real data
	 * @param tableRowHeader reference of the row header, null means using column header
	 */
//	public static void setDataTableEx(MyTable myTable,String dtoClassName,int addNewRow,String[] newTitle,Object[][] newData,JTable tableRowHeader){
//		if(null==myTable) return;
//		myTable.setModel(new MyTableModel(dtoClassName,addNewRow,newTitle,newData,(null!=tableRowHeader?true:false)));
//		myTable.setRowHeight(28);
//		myTable.setRenderFields(newTitle);
//		fitTableColumns(myTable,1.2f);
//		if(null!=tableRowHeader){
//			tableRowHeader.setModel(new RowHeaderTableModel(newTitle));
//			tableRowHeader.setRowHeight(28);
//			tableRowHeader.setPreferredScrollableViewportSize(new Dimension(180,0));
//		}
//	}
	
	public static void fitTableColumns(JTable myTable,float factor){
		if(null==myTable) return;
		myTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTableHeader header = myTable.getTableHeader();
        int rowCount = myTable.getRowCount();
        Enumeration<TableColumn> columns = myTable.getColumnModel().getColumns();
        while(columns.hasMoreElements())
        {
            TableColumn column = (TableColumn)columns.nextElement();
            int col = header.getColumnModel().getColumnIndex(column.getIdentifier());
            int width = (int)header.getDefaultRenderer().getTableCellRendererComponent
            (myTable, column.getIdentifier(), false, false, -1, col).getPreferredSize().getWidth();
            for(int row = 0; row < rowCount; row++)
            {
                int preferedWidth = (int)myTable.getCellRenderer(row, col).getTableCellRendererComponent
                (myTable, myTable.getValueAt(row, col), false, false, row, col).getPreferredSize().getWidth();
                width = Math.max(width, preferedWidth);
            }
            header.setResizingColumn(column);
            column.setWidth((int)(width*factor+myTable.getIntercellSpacing().width));
        }
   }
	
	@SuppressWarnings("unchecked")
	public static String[][] getRenderConfig(String dtoClassName){
		boolean renderIsSet = false;
		String[][] config = null;
		String[] renderNames = null;
		String[] renderFields = null;
		
		SystemConfig sysCfg = SystemConfig.getInstance();
		LinkedHashMap<String, Object> rootMap = sysCfg.getData("MenuCfg");
		if(null!=rootMap && rootMap.size()>0){
			for(String key:rootMap.keySet()){
				LinkedHashMap<String, Object> menuMap = (LinkedHashMap<String, Object>) rootMap.get(key);
				if(null!=menuMap && menuMap.size()>0){
					for(String subName:menuMap.keySet()){
						LinkedHashMap<String, Object> subMap = (LinkedHashMap<String, Object>) menuMap.get(subName);
						if(null!=subMap && dtoClassName.endsWith(""+subMap.get("dtoClass"))){
							String rfs = ""+subMap.get("renderFields");
							String rns = ""+subMap.get("renderers");
							if(!"".equals(rfs) && !"".equals(rns)){
								renderFields = rfs.split(",");
								renderNames = rns.split(",");
								renderIsSet = true;
								break;
							}
						}
					}
					if(renderIsSet) break;
				}
			}
		}
		
		if(renderIsSet){
			config = new String[2][renderFields.length];
			config[0] = renderFields;
			config[1] = renderNames;
		}
		
		return config;
	}
	
	@SuppressWarnings("unchecked")
	public static String[] getFieldRendererInitVals(String[][] renderCfg, String fieldName){
		String[] vals = null;
		
		if(null!=renderCfg){
			SystemConfig sysCfg = SystemConfig.getInstance();
			LinkedHashMap<String, Object> rootMap = sysCfg.getData("RenderCfg");
			for(int j=0; j<renderCfg[0].length; j++){
				if(fieldName.equals(renderCfg[0][j])){
					LinkedHashMap<String, Object> renderMap = (LinkedHashMap<String, Object>) rootMap.get(renderCfg[1][j]);
					if(null!=renderMap) vals = (String[]) renderMap.get("listVals");
					break;
				}
			}
		}
		
		return vals;
	}
	
	public static Object[][] initDataDefaultByRender(String[][] renderCfg, String[] fields, Object[][] data){
		if(null!=renderCfg && null!=fields && null!=data && fields.length==data[0].length){
			String[] vals = null;
			for(int i=0; i<fields.length; i++){
				vals = getFieldRendererInitVals(renderCfg,fields[i]);
				if(null!=vals && vals.length>0){
					for(int j=0; j<data.length; j++){
						if(null==data[j][i]) data[j][i] = vals[0];
					}
				}
			}
		}
		return data;
	}
}
