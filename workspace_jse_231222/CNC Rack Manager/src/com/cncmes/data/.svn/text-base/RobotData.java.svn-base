package com.cncmes.data;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import com.cncmes.base.DeviceState;
import com.cncmes.base.RobotItems;
import com.cncmes.base.RunningData;

public class RobotData extends RunningData<RobotItems> {
	private static RobotData robotData = new RobotData();
	private RobotData(){}
	
	public static RobotData getInstance(){
		return robotData;
	}
	
	/**
	 * @param ip The IP address of the robot
	 * @param devState The state of the robot to be set
	 */
	public synchronized void setRobotState(String ip, DeviceState devState){
		setData(ip, RobotItems.STATE, devState);
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
	
	public synchronized int getRackCapacity(String robotIP){
		int capacity = 0;
		
		LinkedHashMap<RobotItems, Object> rack = getData(robotIP);
		if(null != rack){
			capacity = (int) rack.get(RobotItems.CAPACITY);
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
		
		LinkedHashMap<RobotItems, Object> rack = getData(robotIP);
		if(null != rack){
			String slots = "";
			int capacity = (int) rack.get(RobotItems.CAPACITY);
			if(capacity > 9) capacity = 9;
			
			for(int i=1; i<=capacity; i++){
				RobotItems key = getRobotItems(i);
				Object slot = rack.get(key);
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
	
	public int getEmptySlotsCount(String robotIP){
		String[] emptySlots = getEmptySlots(robotIP);
		if(null == emptySlots){
			return 0;
		}else{
			return emptySlots.length;
		}
	}
	
	public int getNotEmptySlotsCount(String robotIP){
		return (getRackCapacity(robotIP) - getEmptySlotsCount(robotIP));
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
