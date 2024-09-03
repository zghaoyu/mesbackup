package com.sto.data;

import java.util.LinkedHashMap;

import com.sto.base.RunningData;
import com.sto.utils.XmlUtils;

/**
 * Criteria Setting of OK Product
 * @author LI ZI LONG
 *
 */
public class ProfileString extends RunningData<String> {
	private static ProfileString profileString = new ProfileString();
	private ProfileString(){}
	static {
		XmlUtils.readProfileString("");
	}
	
	public static ProfileString getInstance(){
		return profileString;
	}
	
	public LinkedHashMap<String, Object> getProfileString(){
		return dataMap.get("Values");
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
				data[i][0] = key;
			}
		}
		
		return data;
	}
	
	public String[] getProfileStringTitle(){
		return new String[]{"ParaName","ParaVal"};
	}
	
	public Object[][] getProfileStringData(){
		return getCfgData("Values");
	}
}
