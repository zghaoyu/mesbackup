package com.cncmes.data;

import java.util.LinkedHashMap;

import com.cncmes.base.RackItems;
import com.cncmes.base.SpecItems;
import com.cncmes.base.WorkpieceItems;

public class RackProduct extends RackData {
	private static RackProduct rackProduct = new RackProduct();
	private RackProduct(){}
	public static RackProduct getInstance(){
		return rackProduct;
	}
	
	public int getPort(String lineName){
		int port = 0;
		
		if(dataMap.size() > 0){
			for(String id:dataMap.keySet()){
				String ln = (String) dataMap.get(id).get(RackItems.LINENAME);
				if("All".equals(lineName) || (!"All".equals(lineName) && lineName.equals(ln))){
					port = (int)(dataMap.get(id).get(RackItems.PORT));
					break;
				}
			}
		}
		
		return port;
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
		String[] tableTitle = new String[]{"ID","LineName","RackID","SlotID","WorkpieceID",
				"State","Process","Surface","SimTime","Program","NCModel"};
		
		return tableTitle;
	}
	
	public Object[][] getTableData(String lineName){
		int emptySlotsCount = 0;
		int notEmptySlotsCount = 0;
		int rowCount = 0;
		int colCount = getTableTitle().length;
		
		String[] emptySlots = getEmptySlots(lineName,"All");
		emptySlotsCount = (null==emptySlots)?0:emptySlots.length;
		String[] notEmptySlots = getNotEmptySlots(lineName,"All");
		String[] wpIDs = getNotEmptySlotsVal(lineName,"All");
		if(null != wpIDs) notEmptySlotsCount = wpIDs.length;
		rowCount = notEmptySlotsCount + emptySlotsCount;
		Object[][] tableData = new Object[rowCount][colCount];
		
		if(null != wpIDs){
			String[] mainKeys = getNotEmptySlotsMainKey(lineName,"All");
			WorkpieceData wpData = WorkpieceData.getInstance();
			for(int row=0; row<notEmptySlotsCount; row++){
				String[] keys = mainKeys[row].split("_");
				tableData[row][0] = row+1;
				tableData[row][1] = keys[0];
				tableData[row][2] = keys[1];
				tableData[row][3] = notEmptySlots[row];
				tableData[row][4] = wpIDs[row];
				
				LinkedHashMap<SpecItems,String> spec = WorkpieceData.getInstance().getAllProcInfo(wpIDs[row]);
				if(null != spec){
					tableData[row][5] = ""+wpData.getWorkpieceState(wpIDs[row]);
					tableData[row][6] = (null!=wpData.getItemVal(wpIDs[row], WorkpieceItems.PROCESS))?wpData.getItemVal(wpIDs[row], WorkpieceItems.PROCESS):spec.get(SpecItems.PROCESSNAME);
					tableData[row][7] = (null!=wpData.getItemVal(wpIDs[row], WorkpieceItems.SURFACE))?wpData.getItemVal(wpIDs[row], WorkpieceItems.SURFACE):spec.get(SpecItems.SURFACE);
					tableData[row][8] = (null!=wpData.getItemVal(wpIDs[row], WorkpieceItems.MACHINETIME))?wpData.getItemVal(wpIDs[row], WorkpieceItems.MACHINETIME):spec.get(SpecItems.SIMTIME);
					tableData[row][9] = (null!=wpData.getItemVal(wpIDs[row], WorkpieceItems.PROGRAM))?wpData.getItemVal(wpIDs[row], WorkpieceItems.PROGRAM):spec.get(SpecItems.PROGRAM);
					tableData[row][10] = (null!=wpData.getItemVal(wpIDs[row], WorkpieceItems.NCMODEL))?wpData.getItemVal(wpIDs[row], WorkpieceItems.NCMODEL):spec.get(SpecItems.NCMODEL);
				}else{
					tableData[row][5] = "ALARM";
					tableData[row][6] = "";
					tableData[row][7] = "";
					tableData[row][8] = "";
					tableData[row][9] = "";
					tableData[row][10] = "";
				}
			}
		}
		
		if(emptySlotsCount > 0){
			String[] mainKeys = getEmptySlotsMainKey(lineName,"All");
			for(int row=0; row<emptySlotsCount; row++){
				String[] keys = mainKeys[row].split("_");
				tableData[row+notEmptySlotsCount][0] = row+notEmptySlotsCount+1;
				tableData[row+notEmptySlotsCount][1] = keys[0];
				tableData[row+notEmptySlotsCount][2] = keys[1];
				tableData[row+notEmptySlotsCount][3] = emptySlots[row];
				tableData[row+notEmptySlotsCount][4] = "";
				tableData[row+notEmptySlotsCount][5] = "";
				tableData[row+notEmptySlotsCount][6] = "";
				tableData[row+notEmptySlotsCount][7] = "";
				tableData[row+notEmptySlotsCount][8] = "";
				tableData[row+notEmptySlotsCount][9] = "";
				tableData[row+notEmptySlotsCount][10] = "";
			}
		}
		
		return tableData;
	}
}
