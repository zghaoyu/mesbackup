package com.cncmes.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.cncmes.base.CncItems;
import com.cncmes.base.DeviceState;
import com.cncmes.base.RunningData;
import com.cncmes.base.TaskItems;

/**
 * @author LI ZI LONG
 * Singleton class,it provides functions to access the task queue
 *
 */
public class TaskData extends RunningData<TaskItems> {
	private static TaskData tqHandle = new TaskData();
	private TaskData(){}
	
	public static TaskData getInstance(){
		return tqHandle;
	}
	
	/**
	 * Use it to clear the task queue
	 */
	public synchronized void taskClear(){
		clearData();
	}
	
	/**
	 * @param cncIP The CNC IP address associated with the task to be pushed into the task queue
	 * @return true when the task is pushed into the task queue correctly
	 */
	public synchronized boolean taskPush(String cncIP, DeviceState state, String robotIP, String workpieceIDs, String rackIDs, String slotIDs){
		LinkedHashMap<TaskItems, Object> dt = getData(cncIP);
		if(null != dt){
			Object rawState = dt.get(TaskItems.STATE);
			if(null != rawState){
				DeviceState curState = (DeviceState)rawState;
				if(DeviceState.WAITUL == curState || DeviceState.PLAN == curState || DeviceState.LOCK == curState) return true;
			}
		}
		
		if(state == DeviceState.FINISH || state == DeviceState.STANDBY){
			setData(cncIP,TaskItems.MACHINEIP,cncIP);
			if(state == DeviceState.FINISH){
				setData(cncIP,TaskItems.STATE,DeviceState.WAITUL);
				setData(cncIP,TaskItems.MATERIALIDS,"");
				setData(cncIP,TaskItems.RACKIDS,"");
				setData(cncIP,TaskItems.SLOTIDS,"");
			}else{
				setData(cncIP,TaskItems.STATE,DeviceState.PLAN);
				setData(cncIP,TaskItems.MATERIALIDS,workpieceIDs);
				setData(cncIP,TaskItems.RACKIDS,rackIDs);
				setData(cncIP,TaskItems.SLOTIDS,slotIDs);
			}
			setData(cncIP,TaskItems.MODEL,CncData.getInstance().getData(cncIP).get(CncItems.MODEL));
			setData(cncIP,TaskItems.ROBOTIP,robotIP);
			setData(cncIP,TaskItems.MACHINESTATE,state);
		}
		
		return true;
	}
	
	/**
	 * @return The task was popped out from the task queue
	 */
	public synchronized String taskPop(){
		String ip = getFirstKey();
		if(!"".equals(ip)) removeData(ip);
		return ip;
	}
	
	/**
	 * @return The task was popped out from the task queue
	 */
	public synchronized void taskPopByCncIP(String cncIP){
		removeData(cncIP);
	}
	
	/**
	 * @return The First In task
	 */
	public synchronized LinkedHashMap<TaskItems,Object> taskHandle(){
		LinkedHashMap<TaskItems,Object> data = null;
		for(String key:dataMap.keySet()){
			Object state = dataMap.get(key).get(TaskItems.STATE);
			if(null != state){
				DeviceState devState = (DeviceState)state;
				if(devState == DeviceState.PLAN || devState == DeviceState.WAITUL){
					data = dataMap.get(key);
					break;
				}
			}
		}
		
		return data;
	}
	
	public synchronized LinkedHashMap<TaskItems,Object> getLockTask(String cncIP){
		LinkedHashMap<TaskItems,Object> data = null;
		String ip;
		for(String key:dataMap.keySet()){
			ip = (String) dataMap.get(key).get(TaskItems.MACHINEIP);
			if(cncIP.equals(ip)){
				data = dataMap.get(key);
				break;
			}
		}
		
		return data;
	}
	
	/**
	 * @param ip The CNC IP address associated with the task
	 * @param item The specific item associated with the task
	 * @param val The value of the specific item
	 */
	public synchronized void taskUpdate(String ip, TaskItems item, Object val){
		setData(ip,item,val);
	}
	
	/**
	 * @return A string describes the status of the task
	 */
	public synchronized String[] taskInfo(){
		String[] tasks = null;
		ArrayList<String> ts = new ArrayList<String>();
		
		if(taskCount() > 0){
			try {
				int i = -1;
				for(String key:dataMap.keySet()){
					i++;
					ts.add((i+1) + ":" + key + " - " + dataMap.get(key).get(TaskItems.STATE));
				}
			} catch (Exception e) {
			}
		}
		
		if(!ts.isEmpty()){
			tasks = new String[ts.size()];
			for(int i=0; i<ts.size(); i++){
				tasks[i] = ts.get(i);
			}
		}
		
		return tasks;
	}
	
	/**
	 * @return The remain task count in the task queue
	 */
	public synchronized int taskCount(){
		return size();
	}
	
	public boolean deviceIsPlannedForOtherTask(String curTaskID,String deviceIP,TaskItems item){
		boolean planned = false;
		
		if(taskCount() > 0){
			try {
				for(String key:dataMap.keySet()){
					if(!key.equals(curTaskID)){
						if(deviceIP.equals(""+dataMap.get(key).get(item))){
							planned = true;
							break;
						}
					}
				}
			} catch (Exception e) {
			}
		}
		
		return planned;
	}
}
