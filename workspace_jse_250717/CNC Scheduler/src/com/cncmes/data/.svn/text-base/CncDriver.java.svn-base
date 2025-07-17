package com.cncmes.data;

import java.util.LinkedHashMap;

import com.cncmes.base.DriverItems;
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
}
