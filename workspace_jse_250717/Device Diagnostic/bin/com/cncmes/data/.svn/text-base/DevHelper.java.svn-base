package com.cncmes.data;

import java.util.LinkedHashMap;

import com.cncmes.base.DriverItems;
import com.cncmes.base.RunningData;

public class DevHelper extends RunningData<DriverItems> {
	private static DevHelper devHelper = new DevHelper();
	private DevHelper(){}
	public static DevHelper getInstance(){
		return devHelper;
	}
	
	public String[] getAllHelpers(){
		String[] helpers = null;
		if(dataMap.size() > 0){
			helpers = new String[dataMap.size()];
			int idx = -1;
			for(String model:dataMap.keySet()){
				idx++;
				helpers[idx] = model;
			}
		}
		return helpers;
	}
	
	public Object getCtrlByModel(String model){
		Object ctrl = null;
		
		if(null != getData(model)){
			ctrl = getData(model).get(DriverItems.CTRL);
		}
		
		return ctrl;
	}
	
	public String getCmdTerminator(String model){
		String terminator = "CR";
		
		if(null != getData(model)){
			terminator = (String) getData(model).get(DriverItems.CMDENDCHR);
		}
		
		return terminator;
	}
	
	public String getModelByDriver(String driver){
		String model = "";
		
		if(dataMap.size() > 0){
			LinkedHashMap<DriverItems, Object> dt = null;
			for(String md:dataMap.keySet()){
				dt = dataMap.get(md);
				if(null != dt){
					if(driver.equals(""+dt.get(DriverItems.DRIVER))){
						model = md;
						break;
					}
				}
			}
		}
		
		return model;
	}
}
