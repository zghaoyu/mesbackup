package com.cncmes.utils;

import java.util.LinkedHashMap;

import com.cncmes.base.DummyItems;
import com.cncmes.data.SystemConfig;

public class DebugUtils {
	private static String dummyCNC = "";
	private static String dummyRobot = "";
	private static SystemConfig sysCfg = SystemConfig.getInstance();
	private static boolean autoInputMaterial;
	public static boolean cycleWorking;
	public static boolean FiveAspectProcess = false;
	public static boolean MassProduction = false;
	private DebugUtils(){}
	
	private static void getCommonSettings(){
		try {
			LinkedHashMap<String,Object> config = sysCfg.getCommonCfg();
			dummyCNC = (String)config.get("DummyCNC");
			dummyRobot = (String)config.get("DummyRobot");
		} catch (Exception e) {
			LogUtils.errorLog("DebugUtils fails to load system config:"+e.getMessage());
		}
	}
	
	public static String getDummyDeviceIP(DummyItems item){
		String dummyDeviceIP = "";
		getCommonSettings();
		
		if(null != item){
			switch(item){
			case IP_CNC:
				dummyDeviceIP = dummyCNC;
				break;
			case IP_ROBOT:
				dummyDeviceIP = dummyRobot;
				break;
			}
		}
		
		return dummyDeviceIP;
	}
	
	public static boolean setAutoInputMaterial(boolean autoMode){
		autoInputMaterial = autoMode;
		return autoInputMaterial;
	}
	
	public static boolean autoInputMaterialEnabled(){
		return autoInputMaterial;
	}

	public static void setCycleWorking(boolean selected) {
		cycleWorking = selected;
	}

	public static boolean isFiveAspectProcess() {
		return FiveAspectProcess;
	}

	public static void setFiveAspectProcess(boolean fiveAspectProcess) {
		DebugUtils.FiveAspectProcess = fiveAspectProcess;
	}

	public static boolean isCycleWorking() {
		return cycleWorking;
	}

	public static boolean isMassProduction() {
		return MassProduction;
	}

	public static void setMassProduction(boolean massProduction) {
		MassProduction = massProduction;
	}
}
