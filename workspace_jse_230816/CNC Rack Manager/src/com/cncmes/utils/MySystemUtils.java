package com.cncmes.utils;

import java.io.File;
import java.sql.SQLException;
import java.util.LinkedHashMap;

import com.cncmes.base.DeviceState;
import com.cncmes.base.MemoryItems;
import com.cncmes.base.SchedulerItems;
import com.cncmes.ctrl.RackServer;
import com.cncmes.ctrl.SchedulerClient;
import com.cncmes.data.RackMaterial;
import com.cncmes.data.RackProduct;
import com.cncmes.data.RackTemp;
import com.cncmes.data.SystemConfig;

public class MySystemUtils {
	private static LinkedHashMap<String,Object> sysConfig = null;
	private MySystemUtils(){}
	static{
		try {
			XmlUtils.parseSystemConfig();
			getCommonSettings();
		} catch (Exception e) {
			LogUtils.errorLog("SystemUtils startup error:"+e.getMessage());
		}
	}

	private static void getCommonSettings() {
		SystemConfig sysCfg = SystemConfig.getInstance();
		sysConfig = sysCfg.getCommonCfg();
	}
	
	public static String getNCProgramRootDir() {
		String ncProgramsRootDir = "";
		try {
			SystemConfig sysCfg = SystemConfig.getInstance();
			LinkedHashMap<String,Object> config = sysCfg.getCommonCfg();
			ncProgramsRootDir = (String)config.get("NCProgramsRootDir");
		} catch (Exception e) {
		}
		
		return ncProgramsRootDir;
	}
	
	/**
	 * settings include Machining Spec,MES code list,CNC/Robot/Scanner driver,CNC/Robot/Scanner/Rack/Scheduler info
	 * @param lineName the registered line name in database
	 * @return "OK" if load all settings successfully
	 */
	public static String sysLoadSettingsFromDB(){
		String msg = sysDatabaseOK();
		if(!"OK".equals(msg)) return msg;
		
		msg = DataUtils.getMachiningSpec();
		if(!"OK".equals(msg)) return msg;
		
		msg = DataUtils.getMescode();
		if(!"OK".equals(msg)) return msg;
		
		msg = DataUtils.getDeviceDriver();
		if(!"OK".equals(msg)) return msg;
		
		msg = DataUtils.getDeviceInfo();
		if(!"OK".equals(msg)) return msg;
		
		return "OK";
	}
	
	/**
	 * @return "OK" once system database is working, else return failed reason
	 */
	public static String sysDatabaseOK(){
		getCommonSettings();
		if(null == sysConfig) return "Load system configure failed";
		try {
			DBUtils.getConnection();
		} catch (SQLException e) {
			return "Connect system database failed:"+e.getMessage();
		}
		return "OK";
	}
	
	/**
	 * @param portType PORTMACHINE|PORTROBOT|PORTMATERIAL|PORTTASK|PORTTASKUPDATE
	 * @return "OK" once system Scheduler is working, else return failed reason
	 */
	public static String sysSchedulerOK(SchedulerItems portType){
		getCommonSettings();
		if(null == sysConfig) return "Load system configure failed";
		
		if(Integer.valueOf((String)sysConfig.get("CheckScheduler"))>0){
			if(!SchedulerClient.getInstance().schedulerServerIsReady(portType)){
				return "Connect Scheduler failed";
			}
		}
		
		return "OK";
	}
	
	/**
	 * 
	 * @return "OK" if system is ready to start, else return the failed reason
	 */
	public static String sysReadyToStart(){
		//Scheduler checking
		String readyToStart = MySystemUtils.sysSchedulerOK(SchedulerItems.PORTMATERIAL);
		if(!"OK".equals(readyToStart)) return readyToStart;
		
		return "OK";
	}
	
	/**
	 * @return "OK" once system is ready to stop, else return failed reason
	 */
	public static String sysReayToStop(){
		String msg = "OK";
		
		//1. Standby Material count checking
		RackMaterial mRack = RackMaterial.getInstance();
		if(mRack.getNotEmptySlotsCount("All", "All")>0){
			msg = "There are standby materials";
			return msg;
		}
		
		//2. Working Material count checking
		RackTemp tmpRack = RackTemp.getInstance();
		if(tmpRack.getNotEmptySlotsCount("All", "All")>0){
			msg = "There are materials under machining";
			return msg;
		}
		
		return "OK";
	}
	
	/**
	 * 
	 * @param ip the device IP or object ID
	 * @return the local path to store the memory dump
	 */
	public static String getMemoryDumpPath(String ip){
		String path = System.getProperty("user.dir") + File.separator + "memDump";
		
		if(MyFileUtils.makeFolder(path)){
			path = path + File.separator + ip + ".txt";
		}else{
			path = "";
		}
		
		return path;
	}
	
	/**
	 * 
	 * @param lineName the registered line name in database
	 * @param memItem specify to dump what info(CNC|ROBOT|TASK|ALL)
	 */
	public static void sysMemoryDump(String lineName, String rackID, MemoryItems memItem){
		boolean bEnabled = false; //TODO To enable it after MP started
		if(bEnabled){
			//1. Dump Rack Material
			if(MemoryItems.RACKMATERIAL == memItem || MemoryItems.ALL == memItem){
				RackMaterial rackM = RackMaterial.getInstance();
				rackM.dumpRackInfo(lineName, rackID, "RackMaterial");
			}
			
			//2. Dump Rack Product
			if(MemoryItems.RACKPRODUCT == memItem || MemoryItems.ALL == memItem){
				RackProduct rackP = RackProduct.getInstance();
				rackP.dumpRackInfo(lineName, rackID, "RackProduct");
			}
			
			//3. Dump Control Center
			if(MemoryItems.CONTROLCENTER == memItem || MemoryItems.ALL == memItem){
				RackServer rackSvr = RackServer.getInstance();
				rackSvr.dumpControlCenterInfo();
			}
		}
	}
	
	/**
	 * 
	 * @param lineName the registered line name in database
	 * @param memItem specify to restore what info(CNC|ROBOT|TASK|ALL) from hard disk
	 * @param restoreState specify what state need to restore(null to restore all states)
	 * @param informObserver whether to inform the observers or not(true to inform)
	 */
	public static void sysMemoryRestore(String lineName, String rackID, MemoryItems memItem, DeviceState restoreState, boolean informObserver){
		//1. Restore Rack Material
		if(MemoryItems.RACKMATERIAL == memItem || MemoryItems.ALL == memItem){
			RackMaterial rackM = RackMaterial.getInstance();
			rackM.restoreRackInfo(lineName, rackID, restoreState, informObserver, "RackMaterial");
		}
		
		//2. Restore Rack Product
		if(MemoryItems.RACKPRODUCT == memItem || MemoryItems.ALL == memItem){
			RackProduct rackP = RackProduct.getInstance();
			rackP.restoreRackInfo(lineName, rackID, restoreState, informObserver, "RackProduct");
		}
		
		//3. Restore Control Center
		if(MemoryItems.CONTROLCENTER == memItem || MemoryItems.ALL == memItem){
			RackServer rackSvr = RackServer.getInstance();
			rackSvr.restoreControlCenterInfo(true, true);
		}
	}
}
