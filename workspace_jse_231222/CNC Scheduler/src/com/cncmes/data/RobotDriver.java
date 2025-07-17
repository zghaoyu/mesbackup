package com.cncmes.data;

import com.cncmes.base.DriverItems;
import com.cncmes.base.RunningData;

public class RobotDriver extends RunningData<DriverItems> {
	private static RobotDriver robotDriver = new RobotDriver();
	private RobotDriver(){}
	public static RobotDriver getInstance(){
		return robotDriver;
	}
}
