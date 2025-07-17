package com.sto.data;

import java.util.LinkedHashMap;

import com.sto.base.RunningData;
import com.sto.utils.XmlUtils;

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
	
	public LinkedHashMap<String, Object> getImgPathCfg(){
		return dataMap.get("ImgPathCfg");
	}
	
	public int getImgProcInterval(){
		int procInterval = 1;
		LinkedHashMap<String, Object> cfg = getCommonCfg();
		if(null!=cfg) procInterval = Integer.parseInt(""+cfg.get("imgProcessingInterval"));
		return procInterval;
	}
	
	public int getImgProcQtyPerCycle(int defaultVal){
		int procQty = (defaultVal<=0)?4:defaultVal;
		LinkedHashMap<String, Object> cfg = getCommonCfg();
		if(null!=cfg) procQty = Integer.parseInt(""+cfg.get("imgProcQtyPerCycle"));
		return procQty;
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
				if("imgProcessingInterval".equals(key)) key += "(s)";
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
	
	public String[] getImgPathCfgTitle(){
		return new String[]{"ParaName","ParaVal"};
	}
	
	public Object[][] getImgPathCfgData(){
		return getCfgData("ImgPathCfg");
	}
}
