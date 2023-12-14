package com.cncmes.data;

import java.util.LinkedHashMap;

import com.cncmes.base.RunningData;
import com.cncmes.utils.XmlUtils;

/**
 * System Common Configuration
 * @author LI ZI LONG
 *
 */
public class SystemConfig extends RunningData<String> {
	private static SystemConfig sysCfg = new SystemConfig();
	private SystemConfig(){}
	static {
		XmlUtils.parseSystemConfig();
	}
	
	public static SystemConfig getInstance(){
		return sysCfg;
	}
	
	public LinkedHashMap<String, Object> getCommonCfg(){
		return dataMap.get("CommonCfg");
	}
	
	public LinkedHashMap<String, Object> getDatabaseCfg(){
		return dataMap.get("DatabaseCfg");
	}
	public LinkedHashMap<String, Object> getMonitorDatabaseCfg(){
		return dataMap.get("MonitorDatabaseCfg");
	}
	
	public LinkedHashMap<String, Object> getFtpCfg(){
		return dataMap.get("FtpCfg");
	}
	
	private Object[][] getCfgData(String configID){
		Object[][] data = new Object[][]{};
		
		LinkedHashMap<String, Object> cfg = dataMap.get(configID);
		int i = -1;
		if(null != cfg && cfg.size() > 0){
			data = new Object[cfg.size()][2];
			for(String key:cfg.keySet()){
				i++;
				data[i][1] = cfg.get(key);
				if("".equals(""+data[i][1])) data[i][1] = "null";
				if("socketResponseTimeOut".equals(key)) key += "(s)";
				if("socketResponseInterval".equals(key)) key += "(ms)";
				if("DeviceMonitorInterval".equals(key)) key += "(s)";
				data[i][0] = key;
			}
		}
		
		return data;
	}
	
	public String[] getCommonCfgTitle(){
		return new String[]{"ParaName","ParaVal"};
	}
	
	public Object[][] getCommonCfgData(){
		return getCfgData("CommonCfg");
	}
	
	public String[] getDatabaseCfgTitle(){
		return new String[]{"ParaName","ParaVal"};
	}
	
	public Object[][] getDatabaseCfgData(){
		return getCfgData("DatabaseCfg");
	}
	
	public String[] getFtpCfgTitle(){
		return new String[]{"ParaName","ParaVal"};
	}
	
	public Object[][] getFtpCfgData(){
		return getCfgData("FtpCfg");
	}
}
