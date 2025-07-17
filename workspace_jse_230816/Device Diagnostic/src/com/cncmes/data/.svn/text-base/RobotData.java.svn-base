package com.cncmes.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.cncmes.base.DeviceObserver;
import com.cncmes.base.DeviceState;
import com.cncmes.base.DeviceSubject;
import com.cncmes.base.RobotItems;
import com.cncmes.base.RunningData;
import com.cncmes.handler.impl.CncClientDataHandler;
import com.cncmes.utils.DataUtils;
import com.cncmes.utils.MathUtils;
import com.cncmes.utils.MyFileUtils;
import com.cncmes.utils.MySystemUtils;

import net.sf.json.JSONObject;

public class RobotData extends RunningData<RobotItems> implements DeviceSubject {
	private static RobotData robotData = new RobotData();
	private static ArrayList<DeviceObserver> observers = new ArrayList<DeviceObserver>();
	private RobotData(){}
	
	public static RobotData getInstance(){
		return robotData;
	}
	
	@Override
	public void registerObserver(DeviceObserver observer) {
		observers.add(observer);
	}

	@Override
	public void deleteObserver(DeviceObserver observer) {
		int i = observers.indexOf(observer);
		if(i >= 0) observers.remove(i);
	}
	
	@Override
	public void notifyObservers(ArrayList<DeviceObserver> observers, String data, boolean threadMode, boolean threadSequential, boolean theLastThread) {
		if(observers.size() > 0){
			for(DeviceObserver observer:observers){
				observer.update(robotData, data, threadMode, threadSequential, theLastThread);
			}
		}
	}
	
	public String getDataForObservers(String robotIP){
		String info = "", jsonStr = "";
		
		jsonStr = getRobotInfoJsonStr(robotIP);
		if(!"".equals(jsonStr)){
			JSONObject jsonObj = JSONObject.fromObject(jsonStr);
			info = robotIP + ":" + jsonObj.getString("port");
			info += "," + robotIP;
			info += "," + jsonObj.getString("port");
			info += "," + jsonObj.getString("state");
			info += "," + jsonObj.getString("model");
			info += "," + jsonObj.getString("lineName");
			info += "," + jsonObj.getString("position");
			info += "," + jsonObj.getString("capacity");
			info += "," + jsonObj.getString("slot1");
			info += "," + jsonObj.getString("slot2");
			info += "," + jsonObj.getString("slot3");
			info += "," + jsonObj.getString("slot4");
			info += "," + jsonObj.getString("slot5");
			info += "," + jsonObj.getString("slot6");
			info += "," + jsonObj.getString("slot7");
			info += "," + jsonObj.getString("slot8");
			info += "," + jsonObj.getString("slot9");
			info += "," + jsonObj.getString("gripMaterial");
			info += "," + jsonObj.getString("wpIDs");
			info += "," + jsonObj.getString("wpSlotIDs");
			info += "," + jsonObj.getString("wpStates");
			info += "," + jsonObj.getString("dataHandlerIP");
			info += "," + jsonObj.getString("dataHandlerPort");
			info += "," + jsonObj.getString("battery");
			info += "," + MathUtils.MD5Encode(info);
		}
		
		return info;
	}
	
	private String getRobotInfoJsonStr(String robotIP){
		String info = "";
		
		LinkedHashMap<RobotItems,Object> dt = getData(robotIP);
		if(null != dt){
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("port", "" + dt.get(RobotItems.PORT));
			jsonObj.put("state", "" + dt.get(RobotItems.STATE));
			jsonObj.put("model", "" + dt.get(RobotItems.MODEL));
			jsonObj.put("lineName", "" + dt.get(RobotItems.LINENAME));
			jsonObj.put("position", "" + dt.get(RobotItems.POSITION));
			jsonObj.put("capacity", "" + dt.get(RobotItems.CAPACITY));
			jsonObj.put("battery", "" + dt.get(RobotItems.BATTERY));
			jsonObj.put("slot1", "" + (null!=dt.get(RobotItems.SLOT1)?dt.get(RobotItems.SLOT1):""));
			jsonObj.put("slot2", "" + (null!=dt.get(RobotItems.SLOT2)?dt.get(RobotItems.SLOT2):""));
			jsonObj.put("slot3", "" + (null!=dt.get(RobotItems.SLOT3)?dt.get(RobotItems.SLOT3):""));
			jsonObj.put("slot4", "" + (null!=dt.get(RobotItems.SLOT4)?dt.get(RobotItems.SLOT4):""));
			jsonObj.put("slot5", "" + (null!=dt.get(RobotItems.SLOT5)?dt.get(RobotItems.SLOT5):""));
			jsonObj.put("slot6", "" + (null!=dt.get(RobotItems.SLOT6)?dt.get(RobotItems.SLOT6):""));
			jsonObj.put("slot7", "" + (null!=dt.get(RobotItems.SLOT7)?dt.get(RobotItems.SLOT7):""));
			jsonObj.put("slot8", "" + (null!=dt.get(RobotItems.SLOT8)?dt.get(RobotItems.SLOT8):""));
			jsonObj.put("slot9", "" + (null!=dt.get(RobotItems.SLOT9)?dt.get(RobotItems.SLOT9):""));
			jsonObj.put("gripMaterial", "" + (null!=dt.get(RobotItems.GRIPMATERIAL)?dt.get(RobotItems.GRIPMATERIAL):""));
			jsonObj.put("wpIDs", "" + (null!=dt.get(RobotItems.WPIDS)?dt.get(RobotItems.WPIDS):""));
			jsonObj.put("wpSlotIDs", "" + (null!=dt.get(RobotItems.WPSLOTIDS)?dt.get(RobotItems.WPSLOTIDS):""));
			jsonObj.put("wpStates", "" + (null!=dt.get(RobotItems.WPSTATES)?dt.get(RobotItems.WPSTATES):""));
			jsonObj.put("dataHandlerIP", CncClientDataHandler.getIP());
			jsonObj.put("dataHandlerPort", ""+CncClientDataHandler.getPort());
			info = jsonObj.toString();
		}
		
		return info;
	}
	
	public void dumpRobotInfo(String robotIP){
		String path = MySystemUtils.getMemoryDumpPath(robotIP);
		if(!"".equals(path)){
			String content = getRobotInfoJsonStr(robotIP);
			if(!"".equals(content)) MyFileUtils.saveToFile(content, path);
		}
	}
	
	public void restoreRobotInfo(String robotIP, DeviceState restoreState, boolean informObserver, boolean threadMode, boolean threadSequential, boolean theLastThread){
		String path = MySystemUtils.getMemoryDumpPath(robotIP);
		if(!"".equals(path)){
			try {
				String jsonStr = FileUtils.readFileToString(new File(path), "UTF-8");
				JSONObject jsonObj = JSONObject.fromObject(jsonStr);
				DeviceState devState = DataUtils.getDevStateByString(jsonObj.getString("state"));
				if(null == restoreState || devState == restoreState){
					setData(robotIP, RobotItems.PORT, Integer.valueOf(jsonObj.getString("port")));
					setData(robotIP, RobotItems.STATE, devState);
					setData(robotIP, RobotItems.MODEL, jsonObj.getString("model"));
					setData(robotIP, RobotItems.LINENAME, jsonObj.getString("lineName"));
					setData(robotIP, RobotItems.POSITION, jsonObj.getString("position"));
					setData(robotIP, RobotItems.CAPACITY, jsonObj.getString("capacity"));
					setData(robotIP, RobotItems.SLOT1, jsonObj.getString("slot1"));
					setData(robotIP, RobotItems.SLOT2, jsonObj.getString("slot2"));
					setData(robotIP, RobotItems.SLOT3, jsonObj.getString("slot3"));
					setData(robotIP, RobotItems.SLOT4, jsonObj.getString("slot4"));
					setData(robotIP, RobotItems.SLOT5, jsonObj.getString("slot5"));
					setData(robotIP, RobotItems.SLOT6, jsonObj.getString("slot6"));
					setData(robotIP, RobotItems.SLOT7, jsonObj.getString("slot7"));
					setData(robotIP, RobotItems.SLOT8, jsonObj.getString("slot8"));
					setData(robotIP, RobotItems.SLOT9, jsonObj.getString("slot9"));
					setData(robotIP, RobotItems.WPIDS, jsonObj.getString("wpIDs"));
					setData(robotIP, RobotItems.WPSLOTIDS, jsonObj.getString("wpSlotIDs"));
					setData(robotIP, RobotItems.WPSTATES, jsonObj.getString("wpStates"));
					if(informObserver) notifyObservers(observers,getDataForObservers(robotIP),threadMode,threadSequential,theLastThread);
				}
			} catch (IOException e) {
			}
		}
	}
	
	public String[] getAlarmingRobots(String lineName){
		String[] robots = null;
		String temp = "";
		int index = 0;
		
		if(null != lineName){
			LinkedHashMap<RobotItems, Object> dt = null;
			for(String robotIP:dataMap.keySet()){
				dt = dataMap.get(robotIP);
				if(lineName.equals(dt.get(RobotItems.LINENAME)+"")){
					if(DeviceState.ALARMING == (DeviceState)dt.get(RobotItems.STATE)){
						index++;
						if("".equals(temp)){
							temp = index + ":" + robotIP;
						}else{
							temp += "," + index + ":" + robotIP;
						}
					}
				}
			}
		}
		if(!"".equals(temp)) robots = temp.split(",");
		
		return robots;
	}
	
	/**
	 * 
	 * @param ip
	 * @param devState
	 * @param threadMode whether use thread mode
	 * @param threadSequential whether the thread is executed sequentially
	 * @param theLastThread whether it's the last thread,only set to true to execute all threads
	 * @param forceUpdate whether to force update
	 */
	public synchronized void setRobotState(String ip, DeviceState devState, boolean threadMode,boolean threadSequential,boolean theLastThread,boolean forceUpdate){
		DeviceState oriState = getRobotState(ip);
		setData(ip, RobotItems.STATE, devState);
		if(oriState != devState || forceUpdate) notifyObservers(observers,getDataForObservers(ip),threadMode,threadSequential,theLastThread);
	}
	
	/**
	 * @param ip The IP address of the robot
	 * @return The robot state of specified IP
	 */
	public synchronized DeviceState getRobotState(String ip){
		return ((DeviceState)getData(ip).get(RobotItems.STATE));
	}
	
	/**
	 * @return The Standby robot IP, return null when there is no Standby robot
	 */
	public synchronized String getStandbyRobotIP(){
		String robotIP = null;
		if(!dataMap.isEmpty()){
			Set<String> set = dataMap.keySet();
			Iterator<String> it = set.iterator();
			String ip = null;
			DeviceState devState;
			while(it.hasNext()){
				ip = it.next();
				devState = (DeviceState) dataMap.get(ip).get(RobotItems.STATE);
				if(DeviceState.STANDBY == devState){
					robotIP = ip;
					break;
				}
			}
		}
		return robotIP;
	}
	
	public int getWorkingRobotQty(String lineName){
		int qty = 0;
		
		if(null != lineName){
			LinkedHashMap<RobotItems, Object> dt = null;
			for(String ip:dataMap.keySet()){
				dt = dataMap.get(ip);
				if(lineName.equals(dt.get(RobotItems.LINENAME)+"")){
					if(DeviceState.WORKING == (DeviceState)dt.get(RobotItems.STATE)){
						qty++;
					}
				}
			}
		}
		
		return qty;
	}
	
	/**
	 * @param ip The IP address of the robot
	 * @return The robot info,includes ip,model and state
	 */
	public String getRobotDataString(String ip){
		String data = "";
		LinkedHashMap<RobotItems,Object> dtMap = dataMap.get(ip);
		if(null != dtMap){
			data = data + "ip: " + dtMap.get(RobotItems.IP) + "\r\n";
			data = data + "model: " + dtMap.get(RobotItems.MODEL) + "\r\n";
			data = data + "state: " + dtMap.get(RobotItems.STATE);
		}
		return data;
	}
	
	/**
	 * 
	 * @param robotIP the IP address of the Robot
	 * @return the workpiece gripped in Robot's hands
	 */
	public String getGripMaterial(String robotIP){
		String gripMaterial = "";
		
		LinkedHashMap<RobotItems, Object> data = getData(robotIP);
		if(null != data && null != data.get(RobotItems.GRIPMATERIAL)){
			gripMaterial = (String) data.get(RobotItems.GRIPMATERIAL);
		}
		
		return gripMaterial;
	}
	
	/**
	 * 
	 * @param robotIP the IP address of the Robot
	 * @param workpieceID the workpiece which has been unloaded successfully from the Robot's tray
	 */
	public void deleteMaterialInfo(String robotIP, String workpieceID){
		String wpIDs="",wpStates="",slotIDs="";
		String wpIDsNew="",wpStatesNew="",slotIDsNew="";
		
		LinkedHashMap<RobotItems,String> info = getMaterialInfo(robotIP);
		wpIDs = info.get(RobotItems.WPIDS);
		wpStates = info.get(RobotItems.WPSTATES);
		slotIDs = info.get(RobotItems.WPSLOTIDS);
		
		if(!"".equals(wpIDs)){
			String[] arrIDs = wpIDs.split(";");
			String[] arrStates = wpStates.split(";");
			String[] arrSlotIDs = slotIDs.split(";");
			for(int i=0; i<arrIDs.length; i++){
				if(!arrIDs[i].equals(workpieceID)){
					if("".equals(wpIDsNew)){
						wpIDsNew = arrIDs[i];
						wpStatesNew = arrStates[i];
						slotIDsNew = arrSlotIDs[i];
					}else{
						wpIDsNew += ";" + arrIDs[i];
						wpStatesNew += ";" + arrStates[i];
						slotIDsNew += ";" + arrSlotIDs[i];
					}
				}
			}
		}
		
		setData(robotIP, RobotItems.WPIDS, wpIDsNew);
		setData(robotIP, RobotItems.WPSLOTIDS, slotIDsNew);
		setData(robotIP, RobotItems.WPSTATES, wpStatesNew);
	}
	
	/**
	 * 
	 * @param robotIP the IP address of the Robot
	 * @param workpieceID the workpiece which has been loaded successfully onto the Robot's tray
	 * @param slotID the tray number of the Robot
	 * @param workpieceState the state of the loaded workpiece
	 */
	public void addMaterialInfo(String robotIP, String workpieceID, String slotID, DeviceState workpieceState){
		String wpIDs="",wpStates="",slotIDs="";
		LinkedHashMap<RobotItems,String> info = getMaterialInfo(robotIP);
		wpIDs = info.get(RobotItems.WPIDS);
		wpStates = info.get(RobotItems.WPSTATES);
		slotIDs = info.get(RobotItems.WPSLOTIDS);
		
		wpIDs = ("".equals(wpIDs)?workpieceID:(wpIDs+";"+workpieceID));
		wpStates = ("".equals(wpStates)?(""+workpieceState):(wpStates+";"+workpieceState));
		slotIDs = ("".equals(slotIDs)?slotID:(slotIDs+";"+slotID));
		
		setData(robotIP, RobotItems.WPIDS, wpIDs);
		setData(robotIP, RobotItems.WPSLOTIDS, slotIDs);
		setData(robotIP, RobotItems.WPSTATES, wpStates);
	}
	
	/**
	 * 
	 * @param robotIP the IP address of the Robot
	 * @return all workpieces in the Robot's tray
	 */
	public LinkedHashMap<RobotItems,String> getMaterialInfo(String robotIP){
		String wpIDs="",wpStates="",slotIDs="";
		LinkedHashMap<RobotItems,String> info = new LinkedHashMap<RobotItems,String>();
		
		LinkedHashMap<RobotItems, Object> data = getData(robotIP);
		if(null != data){
			wpIDs = (null!=data.get(RobotItems.WPIDS)?(String)data.get(RobotItems.WPIDS):"");
			wpStates = (null!=data.get(RobotItems.WPSTATES)?(String)data.get(RobotItems.WPSTATES):"");
			slotIDs = (null!=data.get(RobotItems.WPSLOTIDS)?(String)data.get(RobotItems.WPSLOTIDS):"");
		}
		
		info.put(RobotItems.WPIDS, wpIDs);
		info.put(RobotItems.WPSTATES, wpStates);
		info.put(RobotItems.WPSLOTIDS, slotIDs);
		
		return info;
	}
	
	/**
	 * 
	 * @param robotIP the IP address of the Robot
	 */
	public void clearMaterialInfo(String robotIP){
		setData(robotIP, RobotItems.WPIDS, "");
		setData(robotIP, RobotItems.WPSLOTIDS, "");
		setData(robotIP, RobotItems.WPSTATES, "");
	}
	
	public int getTrayCapacity(String robotIP){
		int capacity = 0;
		
		LinkedHashMap<RobotItems, Object> data = getData(robotIP);
		if(null != data){
			capacity = (int) data.get(RobotItems.CAPACITY);
			if(capacity > 9) capacity = 9;
		}
		
		return capacity;
	}
	
	public synchronized void updateSlot(String robotIP,int slotNo,Object val){
		LinkedHashMap<RobotItems, Object> rack = getData(robotIP);
		if(null != rack){
			RobotItems key = getRobotItems(slotNo);
			if(null != key) rack.put(key, val);
		}
	}
	
	public String getSlotVal(String robotIP,int slotNo){
		String val = "";
		LinkedHashMap<RobotItems, Object> data = getData(robotIP);
		if(null != data){
			RobotItems key = getRobotItems(slotNo);
			if(null != key) val = (null!=data.get(key)?(String)data.get(key):"");
		}
		return val;
	}
	
	public String[] getMaterialIDsFromTray(String robotIP){
		String[] ids = null;
		String temp = "", id = "";
		int trayCapacity = getTrayCapacity(robotIP);
		
		if(trayCapacity > 0){
			for(int i=1; i<=trayCapacity; i++){
				id = getSlotVal(robotIP, i);
				if(!"".equals(id)){
					if("".equals(temp)){
						temp = id;
					}else{
						temp += "," + id;
					}
				}
			}
		}
		if(!"".equals(temp)) ids = temp.split(",");
		
		return ids;
	}
	
	/**
	 * @param robotIP
	 * @return The not-empty slot number in array, return null while the robotIP is not existing or all slot is empty
	 */
	public synchronized String[] getNotEmptySlots(String robotIP){
		String[] notEmptySlots = null;
		
		LinkedHashMap<RobotItems, Object> rack = getData(robotIP);
		if(null != rack){
			String slots = "";
			int capacity = (int) rack.get(RobotItems.CAPACITY);
			if(capacity > 9) capacity = 9;
			
			for(int i=1; i<=capacity; i++){
				RobotItems key = getRobotItems(i);
				Object slot = rack.get(key);
				if(null != slot && !"".equals(slot)){
					if("".equals(slots)){
						slots = ""+i;
					}else{
						slots += ","+i;
					}
				}
			}
			
			if(!"".equals(slots)) notEmptySlots = slots.split(",");
		}
		
		return notEmptySlots;
	}
	
	/**
	 * @param robotIP
	 * @return The empty slot number in array, return null while the robotIP is not existing or there is no empty slot
	 */
	public synchronized String[] getEmptySlots(String robotIP){
		String[] emptySlots = null;
		
		LinkedHashMap<RobotItems, Object> dt = getData(robotIP);
		if(null != dt){
			String slots = "";
			int capacity = (int) dt.get(RobotItems.CAPACITY);
			if(capacity > 9) capacity = 9;
			
			for(int i=1; i<=capacity; i++){
				RobotItems key = getRobotItems(i);
				Object slot = dt.get(key);
				if(null == slot || (null != slot && "".equals(slot))){
					if("".equals(slots)){
						slots = ""+i;
					}else{
						slots += ","+i;
					}
				}
			}
			
			if(!"".equals(slots)) emptySlots = slots.split(",");
		}
		
		return emptySlots;
	}
	
	public String[] getRobotsByLineName(String lineName){
		String[] robots = null;
		String line = "", temp = "";
		
		if(dataMap.size() > 0){
			for(String ip:dataMap.keySet()){
				line = (String) dataMap.get(ip).get(RobotItems.LINENAME);
				if(lineName.equals(line)){
					if("".equals(temp)){
						temp = ip;
					}else{
						temp += "," + ip;
					}
				}
			}
			if(!"".equals(temp)) robots = temp.split(",");
		}
		
		return robots;
	}
	
	public int getEmptySlotsCount(String robotIP){
		String[] emptySlots = getEmptySlots(robotIP);
		if(null == emptySlots){
			return 0;
		}else{
			return emptySlots.length;
		}
	}
	
	public int getNotEmptySlotsCount(String robotIP){
		return (getTrayCapacity(robotIP) - getEmptySlotsCount(robotIP));
	}
	
	private RobotItems getRobotItems(int slotNo){
		RobotItems robotItem = null;
		
		if(1 == slotNo) robotItem = RobotItems.SLOT1;
		if(2 == slotNo) robotItem = RobotItems.SLOT2;
		if(3 == slotNo) robotItem = RobotItems.SLOT3;
		if(4 == slotNo) robotItem = RobotItems.SLOT4;
		if(5 == slotNo) robotItem = RobotItems.SLOT5;
		
		if(6 == slotNo) robotItem = RobotItems.SLOT6;
		if(7 == slotNo) robotItem = RobotItems.SLOT7;
		if(8 == slotNo) robotItem = RobotItems.SLOT8;
		if(9 == slotNo) robotItem = RobotItems.SLOT9;
		
		return robotItem;
	}
}
