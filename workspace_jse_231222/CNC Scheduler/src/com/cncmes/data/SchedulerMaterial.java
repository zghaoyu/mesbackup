package com.cncmes.data;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.io.FileUtils;

import com.cncmes.base.DeviceState;
import com.cncmes.base.RunningData;
import com.cncmes.base.SchedulerDataItems;
import com.cncmes.utils.DataUtils;
import com.cncmes.utils.MyFileUtils;

import net.sf.json.JSONObject;

public class SchedulerMaterial extends RunningData<SchedulerDataItems> {
	private static SchedulerMaterial sMaterial = new SchedulerMaterial();
	private SchedulerMaterial(){}
	public static SchedulerMaterial getInstance(){
		return sMaterial;
	}
	
	public String[] getTableTitle(){
		String[] tableTitle = new String[]{"ID","WorkpieceID","State","ProcQTY","Surface","SimTime","ProcName","NCModel","Program"};
		return tableTitle;
	}
	
	public Object[][] getTableData(String lineName, String state){
		int rowCount = 1;
		int colCount = getTableTitle().length;
		if(dataMap.size() > 0) rowCount = dataMap.size();
		Object[][] tableData = new Object[rowCount][colCount];
		
		DeviceState devState = null;
		if("Working".equals(state)) devState = DeviceState.WORKING;
		if("Standby".equals(state)) devState = DeviceState.STANDBY;
		if("Shutdown".equals(state)) devState = DeviceState.SHUTDOWN;
		if("Alarm".equals(state)) devState = DeviceState.ALARMING;
		
		if(dataMap.size() > 0){
			int rowIndex = -1;
			for(String key:dataMap.keySet()){
				LinkedHashMap<SchedulerDataItems,Object> data = dataMap.get(key);
				String ln = "All";
				if(!"All".equals(lineName)) ln = (String) data.get(SchedulerDataItems.LINENAME);
				DeviceState ds = null;
				if(null != devState) ds = (DeviceState) data.get(SchedulerDataItems.STATE);
				
				if(lineName.equals(ln) && devState == ds){
					rowIndex++;
					tableData[rowIndex][0] = rowIndex + 1;
					tableData[rowIndex][1] = key;
					tableData[rowIndex][2] = data.get(SchedulerDataItems.STATE);
					tableData[rowIndex][3] = data.get(SchedulerDataItems.PROCESSQTY);
					tableData[rowIndex][4] = data.get(SchedulerDataItems.SURFACE);
					tableData[rowIndex][5] = data.get(SchedulerDataItems.SIMTIME);
					tableData[rowIndex][6] = data.get(SchedulerDataItems.PROCESSNAME);
					tableData[rowIndex][7] = data.get(SchedulerDataItems.NCMODEL);
					tableData[rowIndex][8] = data.get(SchedulerDataItems.PROGRAM);
				}
			}
		}
		
		return tableData;
	}
	
	private String getMaterialInfoJsonStr(String lineName){
		String jsonStr = "";
		
		if(dataMap.size() > 0){
			String ln = "";
			JSONObject jsonObj = new JSONObject();
			for(String materialID:dataMap.keySet()){
				LinkedHashMap<SchedulerDataItems,Object> data = dataMap.get(materialID);
				ln = "All";
				if(!"All".equals(lineName)) ln = (String) data.get(SchedulerDataItems.LINENAME);
				
				if(lineName.equals(ln)){
					jsonObj.put("materialID", materialID);
					jsonObj.put("lineName", ""+data.get(SchedulerDataItems.LINENAME));
					jsonObj.put("state", ""+data.get(SchedulerDataItems.STATE));
					jsonObj.put("processQty", ""+data.get(SchedulerDataItems.PROCESSQTY));
					jsonObj.put("surface", ""+data.get(SchedulerDataItems.SURFACE));
					jsonObj.put("simTime", ""+data.get(SchedulerDataItems.SIMTIME));
					jsonObj.put("processName", ""+data.get(SchedulerDataItems.PROCESSNAME));
					jsonObj.put("ncModel", ""+data.get(SchedulerDataItems.NCMODEL));
					jsonObj.put("program", ""+data.get(SchedulerDataItems.PROGRAM));
					if("".equals(jsonStr)){
						jsonStr = jsonObj.toString();
					}else{
						jsonStr += "\r\n" + jsonObj.toString();
					}
				}
			}
		}
		
		return jsonStr;
	}
	
	public void dumpMaterialInfo(String lineName){
		String key = "Material_" + lineName;
		String path = DataUtils.getMemoryDumpPath(key);
		if(!"".equals(path)){
			String content = getMaterialInfoJsonStr(lineName);
			if(!"".equals(content)) MyFileUtils.saveToFile(content, path);
		}
	}
	
	public void restoreMaterialInfo(String lineName, DeviceState restoreState){
		String key = "Material_" + lineName;
		String path = DataUtils.getMemoryDumpPath(key);
		if(!"".equals(path)){
			if(MyFileUtils.getFilePassedDays(path)<=0.5){
				try {
					String[] jsonStrs = FileUtils.readFileToString(new File(path), "UTF-8").split("\r\n");
					JSONObject jsonObj = null;
					DeviceState devState = null;
					String materialID = "";
					for(String jsonStr:jsonStrs){
						jsonObj = JSONObject.fromObject(jsonStr);
						devState = DataUtils.getDevStateByString(jsonObj.getString("state"));
						if(null == restoreState || devState == restoreState){
							materialID = jsonObj.getString("materialID");
							setData(materialID, SchedulerDataItems.LINENAME, jsonObj.getString("lineName"));
							setData(materialID, SchedulerDataItems.STATE, devState);
							setData(materialID, SchedulerDataItems.PROCESSQTY, jsonObj.getString("processQty"));
							setData(materialID, SchedulerDataItems.SURFACE, jsonObj.getString("surface"));
							setData(materialID, SchedulerDataItems.SIMTIME, jsonObj.getString("simTime"));
							setData(materialID, SchedulerDataItems.PROCESSNAME, jsonObj.getString("processName"));
							setData(materialID, SchedulerDataItems.NCMODEL, jsonObj.getString("ncModel"));
							setData(materialID, SchedulerDataItems.PROGRAM, jsonObj.getString("program"));
						}
					}
				} catch (IOException e) {
				}
			}
		}
	}
	public void	setSpecifiedMaterialStatus(String[] materialsID,DeviceState deviceState)      //make the material resume work
	{
		LinkedHashMap<String,LinkedHashMap<SchedulerDataItems,Object>> map = new LinkedHashMap<>();
//		System.out.println(Arrays.deepToString(materialsID));
//		System.out.println("-------------------------------");
		if(dataMap.size() > 0){
			for(String key:dataMap.keySet()){
				for(String materialID:materialsID)
				{
					if(key.equals(materialID))
					{
						LinkedHashMap<SchedulerDataItems,Object> data = dataMap.get(key);
						           // change fixture status and put fixture on the last position of procession
							data.put(SchedulerDataItems.STATE,deviceState);
							map.put(materialID,data);


					}
				}
			}

			for(String key : map.keySet())
			{
				dataMap.remove(key);
				dataMap.put(key,map.get(key));
			}
			System.out.println("dataMap-----------------------------------------------------------");
			for(Map.Entry<String,LinkedHashMap<SchedulerDataItems,Object>> entry:dataMap.entrySet()){
				System.out.println(entry.getKey() + ", Value = " + entry.getValue());
			}
			System.out.println("dataMap-----------------------------------------------------------");
		}
	}

	public void  getMaterialList(String lineName,LinkedHashMap<String,LinkedHashMap<SchedulerDataItems,Object>> dt){
		dt.clear();
		if(dataMap.size() > 0){
			for(String key:dataMap.keySet()){
				LinkedHashMap<SchedulerDataItems,Object> data = dataMap.get(key);
				String ln = "All";
				if(!"All".equals(lineName)) ln = (String) data.get(SchedulerDataItems.LINENAME);
				if(lineName.equals(ln)){
					DeviceState devState = (DeviceState) data.get(SchedulerDataItems.STATE);
					if(DeviceState.STANDBY == devState || DeviceState.UNSCHEDULE == devState) dt.put(key, data);
				}
			}
		}
	}
	
	public void setMaterialState(String key, DeviceState val){
		LinkedHashMap<SchedulerDataItems,Object> dt = dataMap.get(key);
		if(null != dt) dt.put(SchedulerDataItems.STATE, val);
	}
	
	public DeviceState getMaterialState(String key){
		return (DeviceState)dataMap.get(key).get(SchedulerDataItems.STATE);
	}
	
	public LinkedHashMap<SchedulerDataItems,Object> getStatisticData(String lineName){
		LinkedHashMap<SchedulerDataItems,Object> sd = new LinkedHashMap<SchedulerDataItems,Object>();
		
		if(dataMap.size() > 0){
			int totalQty = 0;
			int workingQty = 0;
			int standbyQty = 0;
			int planQty = 0;
			int finishQty = 0;
			
			for(String key:dataMap.keySet()){
				LinkedHashMap<SchedulerDataItems,Object> data = dataMap.get(key);
				String ln = "All";
				if(!"All".equals(lineName)) ln = (String) data.get(SchedulerDataItems.LINENAME);
				
				if(lineName.equals(ln)){
					totalQty++;
					DeviceState devState = (DeviceState) data.get(SchedulerDataItems.STATE);
					if(devState == DeviceState.WORKING) workingQty++;
					if(devState == DeviceState.STANDBY) standbyQty++;
					if(devState == DeviceState.PLAN) planQty++;
					if(devState == DeviceState.FINISH) finishQty++;
				}
			}
			
			sd.put(SchedulerDataItems.QTYTOTAL, totalQty);
			sd.put(SchedulerDataItems.QTYWORKING, workingQty);
			sd.put(SchedulerDataItems.QTYSTANDBY, standbyQty);
			sd.put(SchedulerDataItems.QTYPLAN, planQty);
			sd.put(SchedulerDataItems.QTYFINISH, finishQty);
		}
		
		return sd;
	}
}
