package com.cncmes.data;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.apache.commons.io.FileUtils;

import com.cncmes.base.DeviceState;
import com.cncmes.base.RunningData;
import com.cncmes.ctrl.CtrlCenterClient;
import com.cncmes.gui.Scheduler;
import com.cncmes.utils.DataUtils;
import com.cncmes.utils.MyFileUtils;

import net.sf.json.JSONObject;

public class SchedulerTask extends RunningData<String> {
	private static SchedulerTask sTask = new SchedulerTask();
	private static Scheduler schedulerGUI = Scheduler.getInstance();
	
	private SchedulerTask(){}
	public static SchedulerTask getInstance(){
		return sTask;
	}
	
	public String[] getTaskShowList(){
		String task = "",temp = "";
		String[] tasks = null;
		
		if(dataMap.size() > 0){
			JSONObject jsonObj = new JSONObject();
			try {
				for(String lineName:dataMap.keySet()){
					LinkedHashMap<String, Object> dt = getData(lineName);
					if(null != dt){
						for(String taskID:dt.keySet()){
							temp = (String) dt.get(taskID);
							jsonObj = JSONObject.fromObject(temp);
							if(null != jsonObj){
								if("".equals(task)){
									task = jsonObj.getString("robotIP") + "->" + jsonObj.getString("machineIP") + "(" + jsonObj.getString("taskState") + ")";
								}else{
									task += "," + jsonObj.getString("robotIP") + "->" + jsonObj.getString("machineIP") + "(" + jsonObj.getString("taskState") + ")";
								}
							}
						}
					}
				}
			} catch (Exception e) {
			}
		}
		
		if(!"".equals(task)) tasks = task.split(",");
		return tasks;
	}
	
	public String getTaskInfoByLineName(String lineName){
		String info = "",temp = "",taskState = "";
		
		LinkedHashMap<String, Object> dt = getData(lineName);
		if(null != dt){
			try {
				JSONObject jsonObj = new JSONObject();
				for(String taskID:dt.keySet()){
					temp = (String) dt.get(taskID);
					jsonObj = JSONObject.fromObject(temp);
					taskState = "";
					if(null != jsonObj) taskState = jsonObj.getString("taskState");
					
					if("STANDBY".equals(taskState)){
						if("".equals(info)){
							info = (String)dt.get(taskID);
						}else{
							info += "/" + dt.get(taskID);
						}
					}
				}
			} catch (Exception e) {
			}
		}
		
		return info;
	}
	
	public String getTaskInfo(String lineName, String taskID){
		String taskInfo = "";
		
		LinkedHashMap<String, Object> dt = getData(lineName);
		if(null != dt){
			try {
				taskInfo = (String) dt.get(taskID);
			} catch (Exception e) {
			}
		}
		
		return taskInfo;
	}
	
	public void dumpTaskInfo(String lineName, String taskID){
		String key = "Task_" + lineName + "_" + taskID;
		String path = DataUtils.getMemoryDumpPath(key);
		if(!"".equals(path)){
			String content = getTaskInfo(lineName, taskID);
			if(!"".equals(content)) MyFileUtils.saveToFile(content, path);
		}
	}
	
	public void restoreTaskInfo(String lineName, DeviceState restoreState, boolean informObserver){
		String[] keys = MyFileUtils.getFileList(DataUtils.getMemoryDumpRootDir(), "Task_" + lineName + "_");
		if(null == keys) return;
		
		String taskID = "";
		String[] temp = null;
		for(String path:keys){
			if(!"".equals(path)){
				if(MyFileUtils.getFilePassedDays(path)<=0.5){
					temp = path.split("_");
					try {
						taskID = temp[temp.length - 1];
						String jsonStr = FileUtils.readFileToString(new File(path), "UTF-8");
						JSONObject jsonObj = JSONObject.fromObject(jsonStr);
						DeviceState devState = DataUtils.getDevStateByString(jsonObj.getString("taskState"));
						if(null == restoreState || devState == restoreState){
							addTaskInfo(lineName, taskID, jsonStr);
							if(informObserver){
								CtrlCenterClient ctrlCC = CtrlCenterClient.getInstance();
								String[] ccComCfg = (""+jsonObj.getString("controlCenter")).split(":");
								ctrlCC.informControlCenter("updateTaskInfo", ccComCfg[0], ccComCfg[1], ""+jsonObj.getString("ip"), jsonStr, false);
							}
						}
					} catch (IOException e) {
					}
				}
			}
		}
	}
	
	public int getTaskCount(){
		int taskCount = 0;
		LinkedHashMap<String, Object> dt = null;
		
		if(dataMap.size() > 0){
			try {
				for(String lineName:dataMap.keySet()){
					dt = dataMap.get(lineName);
					if(null != dt) taskCount += dt.size();
				}
			} catch (Exception e) {
			}
		}
		
		return taskCount;
	}
	
	public void addTaskInfo(String lineName, String taskID, String taskInfo){
		setData(lineName, taskID, taskInfo);
		schedulerGUI.refreshGUI(4);
	}
	
	public boolean removeTaskInfo(String lineName, String taskID){
		boolean success = true;
		
		LinkedHashMap<String, Object> dt = getData(lineName);
		if(null != dt){
			try {
				dt.remove(taskID);
				schedulerGUI.refreshGUI(4);
			} catch (Exception e) {
				success = false;
			}
		}
		
		return success;
	}
	
	public boolean updateTaskInfo(String lineName, String taskID, String taskInfo){
		boolean success = true;
		
		LinkedHashMap<String, Object> dt = getData(lineName);
		if(null != dt && null != dt.get(taskID)){
			try {
				dt.put(taskID, taskInfo);
				schedulerGUI.refreshGUI(4);
			} catch (Exception e) {
				success = false;
			}
		}
		
		return success;
	}
	
	public String getAlarmTask(){
		String task = "",temp = "",taskState = "";
		
		if(dataMap.size() > 0){
			JSONObject jsonObj = new JSONObject();
			try {
				for(String lineName:dataMap.keySet()){
					LinkedHashMap<String, Object> dt = getData(lineName);
					if(null != dt){
						for(String taskID:dt.keySet()){
							temp = (String) dt.get(taskID);
							jsonObj = JSONObject.fromObject(temp);
							taskState = "";
							if(null != jsonObj) taskState = jsonObj.getString("taskState");
							
							if("ALARMING".equals(taskState) || "HANDLING".equals(taskState)){
								if("".equals(task)){
									task = temp;
								}else{
									task += "/" + temp;
								}
							}
						}
					}
				}
			} catch (Exception e) {
			}
		}
		
		return task;
	}
}
