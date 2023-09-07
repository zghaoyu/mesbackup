package com.cncmes.data;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.apache.commons.io.FileUtils;

import com.cncmes.base.DeviceState;
import com.cncmes.base.RunningData;
import com.cncmes.base.SchedulerDataItems;
import com.cncmes.ctrl.CtrlCenterClient;
import com.cncmes.utils.DataUtils;
import com.cncmes.utils.MyFileUtils;

import net.sf.json.JSONObject;

public class SchedulerRobot extends RunningData<SchedulerDataItems> {
	private static SchedulerRobot sRobot = new SchedulerRobot();
	private SchedulerRobot(){}
	public static SchedulerRobot getInstance(){
		return sRobot;
	}
	
	public String[] getTableTitle(){
		String[] tableTitle = new String[]{"ID","LineName","Model","IP","Port","State","Position","Capacity","Battery"};
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
					tableData[rowIndex][1] = data.get(SchedulerDataItems.LINENAME);
					tableData[rowIndex][2] = data.get(SchedulerDataItems.MODEL);
					tableData[rowIndex][3] = data.get(SchedulerDataItems.IP);
					tableData[rowIndex][4] = data.get(SchedulerDataItems.PORT);
					tableData[rowIndex][5] = data.get(SchedulerDataItems.STATE);
					tableData[rowIndex][6] = data.get(SchedulerDataItems.POSITION);
					tableData[rowIndex][7] = data.get(SchedulerDataItems.CAPACITY);
					tableData[rowIndex][8] = data.get(SchedulerDataItems.BATTERY);
				}
			}
		}
		
		return tableData;
	}
	
	private String getRobotInfoJsonStr(String key){
		String jsonStr = "", lineName = "";
		
		LinkedHashMap<SchedulerDataItems,Object> data = dataMap.get(key);
		if(null != data){
			JSONObject jsonObj = new JSONObject();
			lineName = ""+data.get(SchedulerDataItems.LINENAME);
			jsonObj.put("lineName", lineName);
			jsonObj.put("ip", ""+data.get(SchedulerDataItems.IP));
			jsonObj.put("port", ""+data.get(SchedulerDataItems.PORT));
			jsonObj.put("model", ""+data.get(SchedulerDataItems.MODEL));
			jsonObj.put("state", ""+data.get(SchedulerDataItems.STATE));
			jsonObj.put("position", ""+data.get(SchedulerDataItems.POSITION));
			jsonObj.put("capacity", ""+data.get(SchedulerDataItems.CAPACITY));
			jsonObj.put("battery", ""+data.get(SchedulerDataItems.BATTERY));
			jsonObj.put("slot1", ""+data.get(SchedulerDataItems.SLOT1));
			jsonObj.put("slot2", ""+data.get(SchedulerDataItems.SLOT2));
			jsonObj.put("slot3", ""+data.get(SchedulerDataItems.SLOT3));
			jsonObj.put("slot4", ""+data.get(SchedulerDataItems.SLOT4));
			jsonObj.put("slot5", ""+data.get(SchedulerDataItems.SLOT5));
			jsonObj.put("slot6", ""+data.get(SchedulerDataItems.SLOT6));
			jsonObj.put("slot7", ""+data.get(SchedulerDataItems.SLOT7));
			jsonObj.put("slot8", ""+data.get(SchedulerDataItems.SLOT8));
			jsonObj.put("slot9", ""+data.get(SchedulerDataItems.SLOT9));
			jsonObj.put("gripMaterial", ""+data.get(SchedulerDataItems.GRIPMATERIAL));
			jsonObj.put("wpIDs", ""+data.get(SchedulerDataItems.WPIDS));
			jsonObj.put("wpSlotIDs", ""+data.get(SchedulerDataItems.WPSLOTIDS));
			jsonObj.put("wpStates", ""+data.get(SchedulerDataItems.WPSTATES));
			jsonObj.put("controlCenter", ""+data.get(SchedulerDataItems.CONTROLCENTER));
			jsonStr = jsonObj.toString();
		}
		
		return jsonStr;
	}
	
	public void dumpRobotInfo(String key){
		String path = DataUtils.getMemoryDumpPath(key);
		if(!"".equals(path)){
			String content = getRobotInfoJsonStr(key);
			if(!"".equals(content)) MyFileUtils.saveToFile(content, path);
		}
	}
	
	public void restoreRobotInfo(String key, DeviceState restoreState, boolean informObserver){
		String path = DataUtils.getMemoryDumpPath(key);
		if(!"".equals(path)){
			if(MyFileUtils.getFilePassedDays(path)<=0.5){
				try {
					String jsonStr = FileUtils.readFileToString(new File(path), "UTF-8");
					JSONObject jsonObj = JSONObject.fromObject(jsonStr);
					DeviceState devState = DataUtils.getDevStateByString(jsonObj.getString("state"));
					if(null == restoreState || devState == restoreState){
						setData(key, SchedulerDataItems.LINENAME, ""+jsonObj.getString("lineName"));
						setData(key, SchedulerDataItems.IP, ""+jsonObj.getString("ip"));
						setData(key, SchedulerDataItems.PORT, ""+jsonObj.getString("port"));
						setData(key, SchedulerDataItems.MODEL, ""+jsonObj.getString("model"));
						setData(key, SchedulerDataItems.STATE, DataUtils.getDevStateByString(""+jsonObj.getString("state")));
						setData(key, SchedulerDataItems.POSITION, ""+jsonObj.getString("position"));
						setData(key, SchedulerDataItems.CAPACITY, ""+jsonObj.getString("capacity"));
						setData(key, SchedulerDataItems.BATTERY, ""+jsonObj.getString("battery"));
						setData(key, SchedulerDataItems.SLOT1, ""+jsonObj.getString("slot1"));
						setData(key, SchedulerDataItems.SLOT2, ""+jsonObj.getString("slot2"));
						setData(key, SchedulerDataItems.SLOT3, ""+jsonObj.getString("slot3"));
						setData(key, SchedulerDataItems.SLOT4, ""+jsonObj.getString("slot4"));
						setData(key, SchedulerDataItems.SLOT5, ""+jsonObj.getString("slot5"));
						setData(key, SchedulerDataItems.SLOT6, ""+jsonObj.getString("slot6"));
						setData(key, SchedulerDataItems.SLOT7, ""+jsonObj.getString("slot7"));
						setData(key, SchedulerDataItems.SLOT8, ""+jsonObj.getString("slot8"));
						setData(key, SchedulerDataItems.SLOT9, ""+jsonObj.getString("slot9"));
						setData(key, SchedulerDataItems.GRIPMATERIAL, ""+jsonObj.getString("gripMaterial"));
						setData(key, SchedulerDataItems.WPIDS, ""+jsonObj.getString("wpIDs"));
						setData(key, SchedulerDataItems.WPSLOTIDS, ""+jsonObj.getString("wpSlotIDs"));
						setData(key, SchedulerDataItems.WPSTATES, ""+jsonObj.getString("wpStates"));
						if(informObserver){
							CtrlCenterClient ctrlCC = CtrlCenterClient.getInstance();
							String[] ccComCfg = (""+jsonObj.getString("controlCenter")).split(":");
							ctrlCC.informControlCenter("updateRobotInfo", ccComCfg[0], ccComCfg[1], ""+jsonObj.getString("ip"), jsonStr, false);
						}
					}
				} catch (IOException e) {
				}
			}
		}
	}
	
	public void getRobotList(String lineName,LinkedHashMap<String,LinkedHashMap<SchedulerDataItems,Object>> dt){
		dt.clear();
		if(dataMap.size() > 0){
			for(String key:dataMap.keySet()){
				LinkedHashMap<SchedulerDataItems,Object> data = dataMap.get(key);
				String ln = "All";
				if(!"All".equals(lineName)) ln = (String) data.get(SchedulerDataItems.LINENAME);
				if(lineName.equals(ln)){
					DeviceState devState = (DeviceState) data.get(SchedulerDataItems.STATE);
					if(DeviceState.STANDBY == devState) dt.put(key, data);
				}
			}
		}
	}
	
	public synchronized DeviceState getRobotState(String key){
		DeviceState state = DeviceState.SHUTDOWN;
		LinkedHashMap<SchedulerDataItems,Object> dt = dataMap.get(key);
		if(null != dt) state = (DeviceState) dt.get(SchedulerDataItems.STATE);
		return state;
	}
	
	public synchronized void setRobotState(String key, DeviceState val){
		LinkedHashMap<SchedulerDataItems,Object> dt = dataMap.get(key);
		if(null != dt) dt.put(SchedulerDataItems.STATE, val);
	}
	
	public String[] getEmptySlots(String key){
		String[] emptySlots = null;
		String temp = "", itemVal = null;
		int capacity = getTrayCapacity(key);
		
		if(capacity > 0){
			for(int i=1; i<=capacity; i++){
				itemVal = getSlotVal(key, i);
				if(null != itemVal && "".equals(itemVal)){
					if("".equals(temp)){
						temp = "" + i;
					}else{
						temp += "," + i;
					}
				}
			}
		}
		if(!"".equals(temp)) emptySlots = temp.split(",");
		
		return emptySlots;
	}
	
	public String getSlotVal(String key, int slotNo){
		Object itemVal = null;
		if(1 == slotNo) itemVal = getItemVal(key, SchedulerDataItems.SLOT1);
		if(2 == slotNo) itemVal = getItemVal(key, SchedulerDataItems.SLOT2);
		if(3 == slotNo) itemVal = getItemVal(key, SchedulerDataItems.SLOT3);
		if(4 == slotNo) itemVal = getItemVal(key, SchedulerDataItems.SLOT4);
		if(5 == slotNo) itemVal = getItemVal(key, SchedulerDataItems.SLOT5);
		if(6 == slotNo) itemVal = getItemVal(key, SchedulerDataItems.SLOT6);
		if(7 == slotNo) itemVal = getItemVal(key, SchedulerDataItems.SLOT7);
		if(8 == slotNo) itemVal = getItemVal(key, SchedulerDataItems.SLOT8);
		if(9 == slotNo) itemVal = getItemVal(key, SchedulerDataItems.SLOT9);
		
		return (null==itemVal?null:(String)itemVal);
	}
	
	public int getTrayCapacity(String key){
		int capacity = 0;
		
		LinkedHashMap<SchedulerDataItems,Object> dt = dataMap.get(key);
		if(null != dt && null != dt.get(SchedulerDataItems.CAPACITY)){
			capacity = Integer.valueOf(dt.get(SchedulerDataItems.CAPACITY)+"");
		}
		
		return capacity;
	}
	
	public LinkedHashMap<SchedulerDataItems,Object> getStatisticData(String lineName){
		LinkedHashMap<SchedulerDataItems,Object> sd = new LinkedHashMap<SchedulerDataItems,Object>();
		
		if(dataMap.size() > 0){
			int workingQty = 0;
			int standbyQty = 0;
			int shutdownQty = 0;
			int alarmQty = 0;
			int planQty = 0;
			
			for(String key:dataMap.keySet()){
				LinkedHashMap<SchedulerDataItems,Object> data = dataMap.get(key);
				String ln = "All";
				if(!"All".equals(lineName)) ln = (String) data.get(SchedulerDataItems.LINENAME);
				
				if(lineName.equals(ln)){
					DeviceState devState = (DeviceState) data.get(SchedulerDataItems.STATE);
					if(devState == DeviceState.WORKING) workingQty++;
					if(devState == DeviceState.STANDBY) standbyQty++;
					if(devState == DeviceState.SHUTDOWN) shutdownQty++;
					if(devState == DeviceState.ALARMING) alarmQty++;
					if(devState == DeviceState.PLAN) planQty++;
				}
			}
			
			sd.put(SchedulerDataItems.QTYWORKING, workingQty);
			sd.put(SchedulerDataItems.QTYSTANDBY, standbyQty);
			sd.put(SchedulerDataItems.QTYSHUTDOWN, shutdownQty);
			sd.put(SchedulerDataItems.QTYALARM, alarmQty);
			sd.put(SchedulerDataItems.QTYPLAN, planQty);
		}
		
		return sd;
	}
}
