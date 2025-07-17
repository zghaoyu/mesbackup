package com.cncmes.thread;

import java.util.LinkedHashMap;

import com.cncmes.ctrl.SocketClient;
import com.cncmes.data.SystemConfig;
import com.cncmes.utils.LogUtils;

/**
 * @author LI ZI LONG
 * Provide static functions to control the most critical user threads
 *
 */
public class ThreadController {
	private static boolean stopAllThread;
	
	/**
	 * start the most critical user threads
	 */
	public static String Run(String lineName){
		String readyToStart = "OK";
		
		LinkedHashMap<String,Object> sysConfig = SystemConfig.getInstance().getCommonCfg();
		boolean runningLog = Integer.valueOf((String)sysConfig.get("RunningLog"))>0?true:false;
		boolean debugLog = Integer.valueOf((String)sysConfig.get("DebugLog"))>0?true:false;
		int monitorInterval = Integer.valueOf((String)sysConfig.get("DeviceMonitorInterval"))>0?(Integer.valueOf((String)sysConfig.get("DeviceMonitorInterval"))*1000):1000;
		
		LogUtils.clearLog(false);
		LogUtils.setEnabledFlag(runningLog);
		LogUtils.setDebugLogFlag(debugLog);
		initStopFlag();
		
		//Start system working threads
		DeviceMonitor.setMonitorInterval(monitorInterval);
		TaskMonitor.enableTaskMonitor();
		TaskMonitor.getInstance(lineName).run();
		TaskProcessor.getInstance(lineName).run();
		DeviceMonitor.getInstance().run();
		
		return readyToStart;
	}
	
	/**
	 * stop the most critical user threads
	 */
	public static void Stop(){
		stopAllThread = true;
		SocketClient.getInstance().disconnectAllSockets();
		System.out.println("ThreadController Stop() is called");
	}
	
	/**
	 * @return state of the switch which control the most critical user threads
	 */
	public static boolean getStopFlag(){
		return stopAllThread;
	}
	
	/**
	 * initialize the state of the switch which control the most critical user threads
	 */
	public static void initStopFlag(){
		stopAllThread = false;
	}
}
