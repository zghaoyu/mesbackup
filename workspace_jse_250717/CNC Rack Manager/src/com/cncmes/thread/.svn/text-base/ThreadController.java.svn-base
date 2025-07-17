package com.cncmes.thread;

import com.cncmes.base.SchedulerItems;
import com.cncmes.ctrl.SchedulerClient;
import com.cncmes.data.RackMaterial;
import com.cncmes.data.RackProduct;
import com.cncmes.utils.DataUtils;

/**
 * @author LI ZI LONG
 * Provide static functions to control the most critical user threads
 *
 */
public class ThreadController {
	private static boolean stopRackManagerThread = false;
	
	/**
	 * use it to start the most critical user threads
	 */
	public static String Run(String lineName){
		String readyChk = "OK";
		initStopFlag();
		
		DataUtils.getMachiningSpec();
		DataUtils.getMescode();
		DataUtils.getDeviceInfo();
		
		readyChk = RackMaterial.getInstance().rackValidate(lineName);
		if("OK".equals(readyChk)){
			readyChk = RackProduct.getInstance().rackValidate(lineName);
			if(!"OK".equals(readyChk)) return readyChk;
		}else{
			return readyChk;
		}
		
		if(!SchedulerClient.getInstance().schedulerServerIsReady(SchedulerItems.PORTTASK)) return "Scheduler is not ready";
		
		return readyChk;
	}
	
	public static void stopRackManager(){
		stopRackManagerThread = true;
		System.out.println("ThreadController stopRackManager() is called");
	}
	
	public static boolean getRackManagerStopFlag(){
		return stopRackManagerThread;
	}
	
	/**
	 * initialize the state of the switch which control the most critical user threads
	 */
	public static void initStopFlag(){
		stopRackManagerThread = false;
	}
}
