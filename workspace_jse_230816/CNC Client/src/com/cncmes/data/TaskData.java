package com.cncmes.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.commons.io.FileUtils;

import com.cncmes.base.CncItems;
import com.cncmes.base.DeviceObserver;
import com.cncmes.base.DeviceState;
import com.cncmes.base.DeviceSubject;
import com.cncmes.base.RobotItems;
import com.cncmes.base.RunningData;
import com.cncmes.base.TaskItems;
import com.cncmes.ctrl.SchedulerClient;
import com.cncmes.utils.DataUtils;
import com.cncmes.utils.MathUtils;
import com.cncmes.utils.MyFileUtils;
import com.cncmes.utils.MySystemUtils;
import com.cncmes.utils.TimeUtils;

import net.sf.json.JSONObject;

/**
 * Task Information
 * @author LI ZI LONG
 */
public class TaskData extends RunningData<TaskItems> implements DeviceSubject {
	private static ArrayList<DeviceObserver> observers = new ArrayList<DeviceObserver>();
	private static TaskData tqHandle = new TaskData();
	
	private TaskData(){}
	
	public static TaskData getInstance(){
		return tqHandle;
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
		if(observers.size() > 0 && !"".equals(data)){
			for(DeviceObserver observer:observers){
				observer.update(tqHandle, data, threadMode, threadSequential, theLastThread);
			}
		}
	}
	
	public String getDataForObservers(String taskID){
		String dataStr = "", jsonStr = "";
		
		jsonStr = getTaskInfoJsonStr(taskID);
		if(!"".equals(jsonStr)){
			dataStr = "updateTask";
			dataStr += "," + jsonStr;
			dataStr += "," + MathUtils.MD5Encode(dataStr);
		}
		
		return dataStr;
	}
	
	private String getJsonVal(JSONObject jsonObj, String key){
		String val = "";
		try {
			val = jsonObj.getString(key);
		} catch (Exception e) {
		}
		return val;
	}
	
	private String getTaskInfoJsonStr(String taskID){
		String dataStr = "",machineIP="",robotIP="";
		
		LinkedHashMap<TaskItems, Object> dt = getData(taskID);
		if(null != dt){
			CncData cncData = CncData.getInstance();
			RobotData robotData = RobotData.getInstance();
			machineIP = dt.get(TaskItems.MACHINEIP)+"";
			robotIP = dt.get(TaskItems.ROBOTIP)+"";
			
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("machineState", cncData.getCncState(machineIP)+"");
			jsonObj.put("robotState", robotData.getRobotState(robotIP)+"");
			jsonObj.put("taskState", dt.get(TaskItems.STATE)+"");
			jsonObj.put("machineIP", machineIP);
			jsonObj.put("robotIP", robotIP);
			jsonObj.put("taskID", taskID);
			jsonObj.put("machineKey", machineIP+":"+cncData.getData(machineIP).get(CncItems.PORT));
			jsonObj.put("robotKey", robotIP+":"+robotData.getData(robotIP).get(RobotItems.PORT));
			jsonObj.put("workpieceIDs", dt.get(TaskItems.MATERIALIDS));
			jsonObj.put("rackIDs", ""+dt.get(TaskItems.RACKIDS));
			jsonObj.put("rackSlotIDs", dt.get(TaskItems.SLOTIDS));
			jsonObj.put("lineName", dt.get(TaskItems.LINENAME));
			jsonObj.put("engineer", dt.get(TaskItems.ENGINEER));
			jsonObj.put("materialStates", dt.get(TaskItems.MATERIALSTATES));
			jsonObj.put("errorCode", ""+(null!=dt.get(TaskItems.ERRORCODE)?dt.get(TaskItems.ERRORCODE):""));
			if("ALARMING".equals(""+dt.get(TaskItems.STATE))) jsonObj.put("alarmTime", TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
			
			jsonObj.put("robotWPIDs", robotData.getItemVal(robotIP, RobotItems.WPIDS));
			jsonObj.put("robotWPSlotIDs", robotData.getItemVal(robotIP, RobotItems.WPSLOTIDS));
			jsonObj.put("robotWPStates", robotData.getItemVal(robotIP, RobotItems.WPSTATES));
			
			String wpIDs="",wpZones="",wpStates="";
			String wpInfo = cncData.getWorkpiecesInMachine(machineIP);
			if(!"".equals(wpInfo)){
				String[] info = wpInfo.split("/");
				wpIDs = info[0];wpZones = info[1];wpStates = info[2];
			}
			jsonObj.put("machineWPIDs", wpIDs);
			jsonObj.put("machineWPSlotIDs", wpZones);
			jsonObj.put("machineWPStates", wpStates);
			
			dataStr = jsonObj.toString();
		}
		
		return dataStr;
	}
	
	public void dumpTaskInfo(String taskID){
		String path = MySystemUtils.getMemoryDumpPath(taskID);
		if(!"".equals(path)){
			String content = getTaskInfoJsonStr(taskID);
			if(!"".equals(content)) MyFileUtils.saveToFile(content, path);
		}
	}
	
	public void restoreTaskInfo(String taskID, DeviceState restoreState, boolean informObserver, boolean threadMode, boolean threadSequential, boolean theLastThread){
		String path = MySystemUtils.getMemoryDumpPath(taskID);
		if(!"".equals(path)){
			if(MyFileUtils.getFilePassedDays(path)<=0.5){
				try {
					String jsonStr = FileUtils.readFileToString(new File(path), "UTF-8");
					JSONObject jsonObj = JSONObject.fromObject(jsonStr);
					DeviceState devState = DataUtils.getDevStateByString(jsonObj.getString("taskState"));
					if(null == restoreState || devState == restoreState){
						setData(taskID, TaskItems.STATE, devState);
						setData(taskID, TaskItems.MACHINEIP, jsonObj.getString("machineIP"));
						setData(taskID, TaskItems.ROBOTIP, jsonObj.getString("robotIP"));
						setData(taskID, TaskItems.MATERIALIDS, jsonObj.getString("workpieceIDs"));
						setData(taskID, TaskItems.MATERIALSTATES, jsonObj.getString("materialStates"));
						setData(taskID, TaskItems.RACKIDS, jsonObj.getString("rackIDs"));
						setData(taskID, TaskItems.SLOTIDS, jsonObj.getString("rackSlotIDs"));
						setData(taskID, TaskItems.LINENAME, jsonObj.getString("lineName"));
						setData(taskID, TaskItems.ENGINEER, jsonObj.getString("engineer"));
						setData(taskID, TaskItems.ERRORCODE, jsonObj.getString("errorCode"));
						setData(taskID, TaskItems.SCHEDULERTASKID, taskID);
						if(informObserver) notifyObservers(observers,getDataForObservers(taskID),threadMode,threadSequential,theLastThread);
					}
				} catch (IOException e) {
				}
			}
		}
	}
	
	/**
	 * @param taskInfo The task information in json string
	 * @return true when the task is pushed successfully, the task is only pushed when the machine state is STANDBY or FINISH
	 */
	public synchronized boolean taskPush(String taskInfo){
		DeviceState cncState, robotState, taskState;
		String cncIP, robotIP, taskID, alarmTime, materialStates;
		String workpieceIDs, rackIDs, slotIDs, lineName, engineer;
		
		JSONObject jsonTaskInfo = JSONObject.fromObject(taskInfo);
		taskID = getJsonVal(jsonTaskInfo, "taskID");
		LinkedHashMap<TaskItems, Object> dt = getData(taskID);
		if(null != dt){
			Object rawState = dt.get(TaskItems.STATE);
			if(null != rawState){
				DeviceState curState = (DeviceState)rawState;
				if(DeviceState.WAITUL == curState || DeviceState.PLAN == curState || DeviceState.LOCK == curState) return true;
			}
		}
		
		cncState = DataUtils.getDevStateByString(getJsonVal(jsonTaskInfo, "machineState"));
		if(cncState == DeviceState.FINISH || cncState == DeviceState.STANDBY){
			robotState = DataUtils.getDevStateByString(getJsonVal(jsonTaskInfo, "robotState"));
			taskState = DataUtils.getDevStateByString(getJsonVal(jsonTaskInfo, "taskState"));
			cncIP = getJsonVal(jsonTaskInfo, "machineIP");
			robotIP = getJsonVal(jsonTaskInfo, "robotIP");
			workpieceIDs = getJsonVal(jsonTaskInfo, "workpieceIDs");
			rackIDs = getJsonVal(jsonTaskInfo, "rackIDs");
			slotIDs = getJsonVal(jsonTaskInfo, "rackSlotIDs");
			lineName = getJsonVal(jsonTaskInfo, "lineName");
			engineer = getJsonVal(jsonTaskInfo, "engineer");
			alarmTime = getJsonVal(jsonTaskInfo, "alarmTime");
			materialStates = getJsonVal(jsonTaskInfo, "materialStates");
			
			setData(taskID,TaskItems.SCHEDULERTASKID,taskID);
			setData(taskID,TaskItems.MACHINESTATE,cncState);
			setData(taskID,TaskItems.ROBOTSTATE,robotState);
			setData(taskID,TaskItems.STATE,taskState);
			if(cncState == DeviceState.FINISH){
				setData(taskID,TaskItems.STATE,DeviceState.WAITUL);
			}else{
				setData(taskID,TaskItems.STATE,DeviceState.PLAN);
			}
			setData(taskID,TaskItems.MACHINEIP,cncIP);
			setData(taskID,TaskItems.ROBOTIP,robotIP);
			setData(taskID,TaskItems.MATERIALIDS,workpieceIDs);
			setData(taskID,TaskItems.RACKIDS,rackIDs);
			setData(taskID,TaskItems.SLOTIDS,slotIDs);
			setData(taskID,TaskItems.LINENAME,lineName);
			setData(taskID,TaskItems.ENGINEER,engineer);
			setData(taskID,TaskItems.ALARMTIME,alarmTime);
			setData(taskID,TaskItems.MATERIALSTATES,materialStates);
			setData(taskID,TaskItems.MODEL,CncData.getInstance().getData(cncIP).get(CncItems.MODEL));
			return true;
		}
		
		return false;
	}
	
	/**
	 * @return The task will be deleted from both CNC Client & Scheduler
	 */
	public synchronized void taskPopByID(String taskID,boolean threadMode){
		String lineName = (String) getData(taskID).get(TaskItems.LINENAME);
		
		//In thread mode, the task will be removed after getting feedback from Scheduler
		if(threadMode) setData(taskID, TaskItems.STATE, DeviceState.FINISH);
		
		//The removeTask(lineName, taskID) operation must be ahead of removeData(taskID)
		boolean ok = SchedulerClient.getInstance().removeTask(lineName, taskID, threadMode);
		if(!threadMode && ok) removeData(taskID);
	}
	
	/**
	 * @return Only the WAITUL and PLAN state task will be processed
	 */
	public synchronized LinkedHashMap<TaskItems,Object> getTask(){
		LinkedHashMap<TaskItems,Object> data = null;
		for(String key:dataMap.keySet()){
			Object state = dataMap.get(key).get(TaskItems.STATE);
			if(null != state){
				DeviceState devState = (DeviceState)state;
				if(devState == DeviceState.PLAN || devState == DeviceState.WAITUL){
					data = dataMap.get(key);
					break;
				}else if(devState == DeviceState.FINISH){
					taskPopByID(key, false);
				}
			}
		}
		
		return data;
	}
	
	public String[] getTasksByLineName(String lineName){
		String[] tasks = null;
		String line = "", temp = "";
		
		if(dataMap.size() > 0){
			for(String key:dataMap.keySet()){
				line = (String) dataMap.get(key).get(TaskItems.LINENAME);
				if(lineName.equals(line)){
					if("".equals(temp)){
						temp = key;
					}else{
						temp += "," + key;
					}
				}
			}
			if(!"".equals(temp)) tasks = temp.split(",");
		}
		
		return tasks;
	}
	
	public LinkedHashMap<TaskItems,Object> getTaskByID(String taskID){
		return dataMap.get(taskID);
	}
	
	public DeviceState getTaskState(String taskID){
		return (DeviceState)getData(taskID).get(TaskItems.STATE);
	}
	
	/**
	 * 
	 * @param taskID
	 * @param state
	 * @param threadMode whether use thread mode
	 * @param threadSequential whether the thread is executed sequentially
	 * @param theLastThread whether it's the last thread,only set to true to execute all threads
	 * @param errCode the error description, pass null if there is no error happens
	 * @param forceUpdate whether to force update
	 */
	public void setTaskState(String taskID, DeviceState state, boolean threadMode,boolean threadSequential,boolean theLastThread, String errCode, boolean forceUpdate){
		DeviceState oriState = getTaskState(taskID);
		setData(taskID, TaskItems.STATE, state);
		if(DeviceState.ALARMING == state){
			CncData cncData = CncData.getInstance();
			String machineIP = (String) getData(taskID).get(TaskItems.MACHINEIP);
			if(null == errCode || "".equals(errCode)) errCode = "Undefined";
			setData(taskID, TaskItems.ERRORCODE, errCode);
			cncData.setData(machineIP, CncItems.D_ERR, errCode);
			cncData.saveMachiningData(machineIP);
		}
		if(oriState != state || forceUpdate) notifyObservers(observers, getDataForObservers(taskID), threadMode, threadSequential, theLastThread);
	}
	
	public String getErrorDesc(String taskID){
		String err = "";
		LinkedHashMap<TaskItems, Object> dt = getData(taskID);
		if(null!=dt) err = (String) dt.get(TaskItems.ERRORCODE);
		return err;
	}
	
	/**
	 * @return A string describes the status of the task
	 */
	public String[] taskInfo(){
		String[] tasks = null;
		ArrayList<String> ts = new ArrayList<String>();
		
		if(taskCount() > 0){
			try {
				int i = -1;
				for(String key:dataMap.keySet()){
					i++;
					ts.add((i+1) + ":" + dataMap.get(key).get(TaskItems.MACHINEIP) + " - " + dataMap.get(key).get(TaskItems.STATE));
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
}
