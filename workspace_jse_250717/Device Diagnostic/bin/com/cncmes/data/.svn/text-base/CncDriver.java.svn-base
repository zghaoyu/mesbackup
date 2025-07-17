package com.cncmes.data;

import java.util.LinkedHashMap;

import com.cncmes.base.DriverItems;
import com.cncmes.base.DriverType;
import com.cncmes.base.RunningData;

public class CncDriver extends RunningData<DriverItems> {
	private static CncDriver cncDriver = new CncDriver();
	private CncDriver(){}
	public static CncDriver getInstance(){
		return cncDriver;
	}
	
	public String getCNCSupportProcess(String cncModel){
		String process = "";
		
		LinkedHashMap<DriverItems,Object> drv = getData(cncModel);
		if(null != drv) process = (String) drv.get(DriverItems.PROCESS);
		
		return process;
	}
	
	public String getAnyCNCModel(){
		String model = "";
		
		if(dataMap.size() > 0){
			for(String key:dataMap.keySet()){
				model = key;
				break;
			}
		}
		
		return model;
	}
	
	public DriverType getDriverType(String cncModel){
		DriverType drvType = DriverType.UNKOWN;
		
		if(null != getData(cncModel)){
			if(null != getData(cncModel).get(DriverItems.DRIVER)){
				String driver = (String) getData(cncModel).get(DriverItems.DRIVER);
				if(driver.indexOf("com.cncmes.drv") < 0){
					drvType = DriverType.DLL;
				}else if(driver.indexOf("com.cncmes.drv.CncDrvWeb") >= 0){
					drvType = DriverType.WEBAPI;
				}else{
					drvType = DriverType.NETWORKCMD;
				}
			}
		}
		
		return drvType;
	}
	
	public String[] getCncModelByDriver(String driverName){
		String[] cncModel = null;
		String models = "";
		
		for(String model:dataMap.keySet()){
			String driver = (String) dataMap.get(model).get(DriverItems.DRIVER);
			if(driverName.equals(driver)){
				if("".equals(models)){
					models = model;
				}else{
					models += "," + model;
				}
			}
		}
		if(!"".equals(models)) cncModel = models.split(",");
		
		return cncModel;
	}
	
	public String getCmdTerminator(String model){
		String terminator = "CR";
		
		if(null != getData(model)){
			terminator = (String) getData(model).get(DriverItems.CMDENDCHR);
		}
		
		return terminator;
	}
	
	public String[] getCncModels(){
		String[] models = null;
		
		if(dataMap.size() > 0){
			models = new String[dataMap.size()];
			int idx = -1;
			for(String model:dataMap.keySet()){
				idx++;
				models[idx] = model;
			}
		}
		
		return models;
	}
}
