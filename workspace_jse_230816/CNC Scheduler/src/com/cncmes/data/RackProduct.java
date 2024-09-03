package com.cncmes.data;

import java.util.LinkedHashMap;

import com.cncmes.base.SpecItems;

public class RackProduct extends RackData {
	private static RackProduct rackProduct = new RackProduct();
	private RackProduct(){}
	public static RackProduct getInstance(){
		return rackProduct;
	}
	
	/**
	 * @param lineName
	 * @return "OK" once the product rack validation is passed
	 */
	public String rackValidate(String lineName){
		String ret = "OK";
		
		String[] rackIDs = getRackIDsByLineName(lineName);
		if(null == rackIDs) ret = "Please configure at least one product rack for "+lineName+" line";
		
		return ret;
	}
	
	public String[] getTableTitle(){
		String[] tableTitle = new String[]{"ID","LineName","RackID","WorkpieceID",
				"State","Process","Surface","SimTime","Program","NCModel"};
		
		return tableTitle;
	}
	
	public Object[][] getTableData(String lineName){
		int emptySlotsCount = 0;
		int notEmptySlotsCount = 0;
		int rowCount = 0;
		int colCount = getTableTitle().length;
		
		emptySlotsCount = getEmptySlotsCount(lineName,"All");
		String[] wpIDs = getNotEmptySlotsVal(lineName,"All");
		if(null != wpIDs) notEmptySlotsCount = wpIDs.length;
		rowCount = notEmptySlotsCount + emptySlotsCount;
		Object[][] tableData = new Object[rowCount][colCount];
		
		if(null != wpIDs){
			String[] mainKeys = getNotEmptySlotsMainKey(lineName,"All");
			for(int row=0; row<notEmptySlotsCount; row++){
				String[] keys = mainKeys[row].split("_");
				tableData[row][0] = row+1;
				tableData[row][1] = keys[0];
				tableData[row][2] = keys[1];
				tableData[row][3] = wpIDs[row];
				
				LinkedHashMap<SpecItems,String> spec = WorkpieceData.getInstance().getAllProcInfo(wpIDs[row]);
				if(null != spec){
					tableData[row][4] = "Finished";
					tableData[row][5] = spec.get(SpecItems.PROCESSNAME);
					tableData[row][6] = spec.get(SpecItems.SURFACE);
					tableData[row][7] = spec.get(SpecItems.SIMTIME);
					tableData[row][8] = spec.get(SpecItems.PROGRAM);
					tableData[row][9] = spec.get(SpecItems.NCMODEL);
				}else{
					tableData[row][4] = "Alarm";
					tableData[row][5] = "";
					tableData[row][6] = "";
					tableData[row][7] = "";
					tableData[row][8] = "";
					tableData[row][9] = "";
				}
			}
		}
		
		if(emptySlotsCount > 0){
			String[] mainKeys = getEmptySlotsMainKey(lineName,"All");
			for(int row=notEmptySlotsCount; row<rowCount; row++){
				String[] keys = mainKeys[row-notEmptySlotsCount].split("_");
				tableData[row][0] = row+1;
				tableData[row][1] = keys[0];
				tableData[row][2] = keys[1];
				tableData[row][3] = "";
				tableData[row][4] = "";
				tableData[row][5] = "";
				tableData[row][6] = "";
				tableData[row][7] = "";
				tableData[row][8] = "";
				tableData[row][9] = "";
			}
		}
		
		return tableData;
	}
}
