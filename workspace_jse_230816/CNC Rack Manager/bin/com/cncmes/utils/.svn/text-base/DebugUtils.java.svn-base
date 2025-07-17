package com.cncmes.utils;

import java.util.LinkedHashMap;

import com.cncmes.base.DummyItems;
import com.cncmes.data.SystemConfig;

public class DebugUtils {
	private static String dummyCNC = "";
	private static String dummyRobot = "";
	
	private DebugUtils(){}
	static{
		try {
			SystemConfig sysCfg = SystemConfig.getInstance();
			LinkedHashMap<String,Object> config = sysCfg.getCommonCfg();
			dummyCNC = (String)config.get("DummyCNC");
			dummyRobot = (String)config.get("DummyRobot");
		} catch (Exception e) {
			LogUtils.errorLog("DebugUtils fails to load system config:"+e.getMessage());
		}
	}
	
	public static String getDummyDeviceIP(DummyItems item){
		String dummyDeviceIP = "";
		
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
}
