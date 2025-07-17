package com.cncmes.base;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.table.AbstractTableModel;

import com.cncmes.data.SystemConfig;
import com.cncmes.utils.DTOUtils;
import com.cncmes.utils.LoginSystem;

public class MyTableModel extends AbstractTableModel{
	private static final long serialVersionUID = 6L;
	private static String[] title = null;
	private static boolean[] dataChgFlags = null, columnReadOnly = null;
	private static boolean rowHeaderMode = false;
	private static Object[][] myData = null, oriData;
	private static int rowCount = 0, colCount = 0, oriRowCount = 0;
	private static String lastDtoClassName = "", currDtoClassName = "";
	private static SystemConfig sysConfig = SystemConfig.getInstance();
	private static LinkedHashMap<String, Object> menuConfig = null;
	private static PermissionItems permission = PermissionItems.DEVMONITORING;
	
	public MyTableModel(String dtoClassName,int addNewRow,String[] cndParasName,String[] cndParasVal){
		super();
		permission = PermissionItems.DEVMONITORING;
		menuConfig = sysConfig.getMenuCfgByDtoClass(dtoClassName);
		rowHeaderMode = false;
		currDtoClassName = dtoClassName;
		if(null!=menuConfig){
			permission = LoginSystem.getPermission(Integer.parseInt(""+menuConfig.get("rights")));
		}
		
		if(!lastDtoClassName.equals(dtoClassName) || null==oriData || 0==addNewRow 
				|| (null!=cndParasName && null!=cndParasVal 
				&& cndParasName.length==cndParasVal.length)){
			ArrayList<Object> dtList = DTOUtils.getDataFromDB(dtoClassName, cndParasName, cndParasVal);
			title = (String[]) dtList.get(0);
			oriData = (Object[][]) dtList.get(1);
			
			if(null!=oriData){
				rowCount = oriData.length;
				colCount = title.length;
				dataChgFlags = new boolean[rowCount];
				lastDtoClassName = dtoClassName;
				oriRowCount = rowCount;
				
				//myData is a different copy of oriData in the memory
				myData = new Object[rowCount][colCount];
				for(int i=0; i<rowCount; i++){
					for(int j=0; j<colCount; j++){
						myData[i][j] = oriData[i][j];
					}
				}
			}else{
				rowCount = 1;//To avoid error
				oriRowCount = 0;
				myData = new Object[rowCount][colCount];
			}
			
			columnReadOnly = new boolean[colCount];
			columnReadOnly[0] = true; //First column is ID
			SystemConfig sysCfg = SystemConfig.getInstance();
			LinkedHashMap<String, Object> menu = sysCfg.getMenuCfgByDtoClass(dtoClassName);
			if(null!=menu){
				if(null!=menu.get("editor") && (""+menu.get("editor")).startsWith("com.")){
					for(int i=0; i<columnReadOnly.length; i++){
						columnReadOnly[i] = true;
					}
				}
			}
		}
		
		if(addNewRow > 0){
			rowCount++;
			Object[][] newData = new Object[rowCount][colCount];
			boolean[] newFlags = new boolean[rowCount];
			for(int i=0; i<(rowCount-1); i++){
				newFlags[i] = dataChgFlags[i];
				for(int j=0; j<colCount; j++){
					newData[i][j] = myData[i][j];
				}
			}
			myData = new Object[rowCount][colCount];
			myData = newData;
			dataChgFlags = new boolean[rowCount];
			dataChgFlags = newFlags;
		}
		myData = DTOUtils.initDataDefaultByRender(DTOUtils.getRenderConfig(dtoClassName),title,myData);
	}
	
	public MyTableModel(String dtoClassName,int addNewRow,String[] newTitle,Object[][] newData,boolean bVerticalHeader){
		super();
		permission = PermissionItems.DEVMONITORING;
		menuConfig = sysConfig.getMenuCfgByDtoClass(dtoClassName);
		rowHeaderMode = bVerticalHeader;
		currDtoClassName = dtoClassName;
		if(null!=menuConfig){
			permission = LoginSystem.getPermission(Integer.parseInt(""+menuConfig.get("rights")));
		}
		
		if(null!=newTitle && null!=newData && newData.length>0 
				&& newTitle.length==newData[0].length){
			title = newTitle;
			oriData = newData;
			
			rowCount = oriData.length;
			colCount = title.length;
			dataChgFlags = new boolean[rowCount];
			lastDtoClassName = dtoClassName;
			oriRowCount = rowCount;
			
			//myData is a different copy of oriData in the memory
			myData = new Object[rowCount][colCount];
			for(int i=0; i<rowCount; i++){
				for(int j=0; j<colCount; j++){
					myData[i][j] = oriData[i][j];
				}
			}
			
			columnReadOnly = new boolean[colCount];
			columnReadOnly[0] = true; //First column is ID
		}
		
		if(null==oriData){
			rowCount = 1;//To avoid error
			oriRowCount = 0;
			myData = new Object[rowCount][colCount];
			columnReadOnly = new boolean[colCount];
			columnReadOnly[0] = true; //First column is ID
		}else if(addNewRow > 0){
			rowCount++;
			Object[][] newDt = new Object[rowCount][colCount];
			boolean[] newFlags = new boolean[rowCount];
			for(int i=0; i<(rowCount-1); i++){
				newFlags[i] = dataChgFlags[i];
				for(int j=0; j<colCount; j++){
					newDt[i][j] = myData[i][j];
				}
			}
			myData = new Object[rowCount][colCount];
			myData = newDt;
			dataChgFlags = new boolean[rowCount];
			dataChgFlags = newFlags;
		}
		myData = DTOUtils.initDataDefaultByRender(DTOUtils.getRenderConfig(dtoClassName),title,myData);
		
		for(int i=0; i<title.length; i++){
			if(title[i].startsWith("ncProgram_")) columnReadOnly[i] = true;
		}
	}
	
	@Override
	public int getRowCount() {
		if(rowHeaderMode){
			return colCount;
		}else{
			return rowCount;
		}
	}

	@Override
	public int getColumnCount() {
		if(rowHeaderMode){
			return rowCount;
		}else{
			return colCount;
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object val = null;
		if(null!=myData){
			try {
				if(rowHeaderMode){
					val = myData[columnIndex][rowIndex];
				}else{
					val = myData[rowIndex][columnIndex];
				}
			} catch (Exception e) {
				val = null;
			}
		}
		
		return val;
	}
	
	@Override
	public void setValueAt(Object value, int row, int column){
		int dataRow, dataCol;
		if(rowHeaderMode){
			dataRow = column;
			dataCol = row;
		}else{
			dataRow = row;
			dataCol = column;
		}
		myData[dataRow][dataCol] = value;
		
		if(dataRow>=oriRowCount){
			if(null==value || "".equals(""+value)){
				dataChgFlags[dataRow] = false;
				for(int i=0; i<myData[dataRow].length; i++){
					if(null!=myData[dataRow][i] && !"".equals(""+myData[dataRow][i])){
						dataChgFlags[dataRow] = true;
						break;
					}
				}
			}else{
				dataChgFlags[dataRow] = true;
			}
		}else{
			if(!(""+oriData[dataRow][dataCol]).equals(""+value)){
				dataChgFlags[dataRow] = true;
			}else{
				dataChgFlags[dataRow] = false;
				for(int i=0; i<myData[dataRow].length; i++){
					if(!(""+myData[dataRow][i]).equals(""+oriData[dataRow][i])){
						dataChgFlags[dataRow] = true;
						break;
					}
				}
			}
		}
	}
	
	@Override
	public String getColumnName(int column){
		if(rowHeaderMode){
			return title[0];
		}else{
			return title[column];
		}
	}
	
	@Override
	public boolean isCellEditable(int row, int column){
		if(LoginSystem.accessDenied(permission)){
			return false;
		}else{
			if(rowHeaderMode){
				return (!columnReadOnly[row]);
			}else{
				return (!columnReadOnly[column]);
			}
		}
	}
	
	public String getDtoClassName(){
		return currDtoClassName;
	}
	
	public String[] getDataTitle(){
		return title;
	}
	
	public Object[][] getCurrentData(){
		return myData;
	}
	
	public boolean isRowHeaderMode(){
		return rowHeaderMode;
	}
	
	public boolean dataIsChanged(){
		boolean changed = false;
		
		if(null!=dataChgFlags && dataChgFlags.length>0){
			for(int i=0; i<dataChgFlags.length; i++){
				if(dataChgFlags[i]){
					changed = true;
					break;
				}
			}
		}
		
		return changed;
	}
	
	public Object[][] getChangedData(){
		Object[][] data = null;
		
		//1.Get changed row count
		int changedRows = 0;
		if(null!=dataChgFlags && dataChgFlags.length>0){
			for(int i=0; i<dataChgFlags.length; i++){
				if(dataChgFlags[i]) changedRows++;
			}
		}
		
		//2.Get changed data
		int index = -1;
		if(changedRows > 0){
			data = new Object[changedRows][colCount];
			for(int i=0; i<dataChgFlags.length; i++){
				if(dataChgFlags[i]){
					index++;
					for(int j=0; j<colCount; j++){
						data[index][j] = myData[i][j];
					}
				}
			}
		}
		
		return data;
	}
}