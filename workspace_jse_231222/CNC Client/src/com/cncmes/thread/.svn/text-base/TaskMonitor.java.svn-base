package com.cncmes.thread;

import com.cncmes.ctrl.ConnectDevices;
import com.cncmes.ctrl.SchedulerClient;
import com.cncmes.data.CncData;
import com.cncmes.data.TaskData;
import com.cncmes.gui.CNCClient;
import com.cncmes.utils.RunningMsg;
import com.cncmes.utils.ThreadUtils;

/**
 * @author LI ZI LONG
 * Reconnect all "SHUTDOWN" devices;
 *  Refresh GUI if data of device is changed; 
 *  Task monitoring
 */
public class TaskMonitor {
	private static String lineName;
	private static int monitorInterval = 5000; //unit is ms
	private static boolean bTaskMonitorEnabled = true;
	private static TaskMonitor taskMonitor = new TaskMonitor();
	private TaskMonitor(){}
	
	public static TaskMonitor getInstance(String ln){
		lineName = ln;
		return taskMonitor;
	}
	
	public static void setMonitorInterval(int interval){
		monitorInterval = interval;
	}
	
	public static void disableTaskMonitor(){
		bTaskMonitorEnabled = false;
	}
	
	public static void enableTaskMonitor(){
		bTaskMonitorEnabled = true;
	}
	
	public static boolean taskMonitorIsEnabled(){
		return bTaskMonitorEnabled;
	}
	
	/**
	 * start the Task Monitoring thread
	 */
	public void run(){
		ThreadUtils.Run(new MyThread());
		RunningMsg.set("TaskMonitor Started");
	}
	
	class MyThread implements Runnable{
		@Override
		public void run() {
			SchedulerClient scClient = SchedulerClient.getInstance();
			CNCClient cncClient = CNCClient.getInstance();
			TaskData taskData = TaskData.getInstance();
			CncData cncData = CncData.getInstance();
			
			while(!ThreadController.getStopFlag()){
				try {
					ConnectDevices.ConnectAllDevices();
					if(bTaskMonitorEnabled) scClient.getTask(lineName);
					if(taskData.getDataChangeFlag()){
						cncClient.setTaskQueueContent(taskData.taskInfo());
						taskData.resetDataChangeFlag();
						Thread.sleep(1000);
					}else{
						if(!cncData.getDataChangeFlag()){
							Thread.sleep(monitorInterval);
						}else{
							Thread.sleep(1000);
						}
					}
				} catch (Exception e) {
				}
			}
			RunningMsg.set("TaskMonitor Stop - "+ThreadController.getStopFlag());
		}
	}
}
