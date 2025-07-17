package com.cncmes.thread;

import com.cncmes.ctrl.RackClient;
import com.cncmes.data.RackMaterial;
import com.cncmes.data.RackProduct;
import com.cncmes.utils.DataUtils;

/**
 * @author LI ZI LONG
 * Provide static functions to control the most critical user threads
 *
 */
public class ThreadController {
	private static boolean stopSchedulerThread = false;
	
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
		
		String[] pRackIDs = RackProduct.getInstance().getRackIDsByLineName(lineName);
		if(!RackClient.getInstance().rackServerIsReady(lineName, pRackIDs[0])) return "Rack Manager is not ready";
		
		return readyChk;
	}
	
	public static void stopScheduler(){
		stopSchedulerThread = true;
	}
	
	public static boolean getSchedulerStopFlag(){
		return stopSchedulerThread;
	}
	
	public static void initStopFlag(){
		stopSchedulerThread = false;
	}
}
