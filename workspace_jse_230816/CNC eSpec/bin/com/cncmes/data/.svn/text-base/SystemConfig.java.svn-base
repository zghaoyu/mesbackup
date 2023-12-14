package com.cncmes.data;

import java.util.LinkedHashMap;

import com.cncmes.base.RunningData;
import com.cncmes.utils.XmlUtils;

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
	
	@SuppressWarnings("unchecked")
	public LinkedHashMap<String, Object> getMenuCfgByDtoClass(String dtoClass){
		LinkedHashMap<String, Object> rtnData = null;
		
		LinkedHashMap<String, Object> menuMap = getData("MenuCfg");
		if(null!=menuMap && menuMap.size()>0){
			for(String menuName:menuMap.keySet()){
				LinkedHashMap<String, Object> dt = (LinkedHashMap<String, Object>) menuMap.get(menuName);
				if(null!=dt && dt.size()>0){
					for(String subMenuName:dt.keySet()){
						LinkedHashMap<String, Object> subMenuMap = (LinkedHashMap<String, Object>) dt.get(subMenuName);
						if(null==subMenuMap) continue;
						if(null!=subMenuMap.get("dtoClass") && 
								dtoClass.endsWith(""+subMenuMap.get("dtoClass"))){
							rtnData = subMenuMap;
							break;
						}
					}
					if(null!=rtnData) break;
				}
			}
		}
		return rtnData;
	}
}
