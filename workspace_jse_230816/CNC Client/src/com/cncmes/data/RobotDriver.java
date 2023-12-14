package com.cncmes.data;

import com.cncmes.base.DriverItems;
import com.cncmes.base.DriverType;
import com.cncmes.base.RunningData;

/**
 * Robot Driver Configuration
 * @author LI ZI LONG
 *
 */
public class RobotDriver extends RunningData<DriverItems> {
	private static RobotDriver robotDriver = new RobotDriver();
	private RobotDriver(){}
	public static RobotDriver getInstance(){
		return robotDriver;
	}
	
	public DriverType getDriverType(String model){
		DriverType drvType = DriverType.UNKOWN;
		
		if(null != getData(model)){
			if(null != getData(model).get(DriverItems.DRIVER)){
				String driver = (String) getData(model).get(DriverItems.DRIVER);
				if(driver.indexOf("com.cncmes.drv") < 0){
					drvType = DriverType.DLL;
				}else{
					drvType = DriverType.NETWORKCMD;
				}
			}
		}
		
		return drvType;
	}
	
	public String[] getRobots(){
		String[] robots = null;
		
		if(dataMap.size()>0){
			robots = new String[dataMap.size()];
			int idx = -1;
			for(String robot:dataMap.keySet()){
				idx++;
				robots[idx] = robot;
			}
		}
		
		return robots;
	}
	
	public String getCmdTerminator(String model){
		String terminator = "CR";
		
		if(null != getData(model)){
			terminator = (String) getData(model).get(DriverItems.CMDENDCHR);
		}
		
		return terminator;
	}
}
