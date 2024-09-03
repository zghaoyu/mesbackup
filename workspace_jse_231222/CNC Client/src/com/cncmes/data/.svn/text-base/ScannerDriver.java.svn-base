package com.cncmes.data;

import com.cncmes.base.DriverItems;
import com.cncmes.base.RunningData;

/**
 * Scanner Driver Configuration
 * @author LI ZI LONG
 *
 */
public class ScannerDriver extends RunningData<DriverItems> {
	private static ScannerDriver scannerDriver = new ScannerDriver();
	private ScannerDriver(){}
	public static ScannerDriver getInstance(){
		return scannerDriver;
	}
	
	public String getCmdTerminator(String model){
		String terminator = "CR";
		
		if(null != getData(model)){
			terminator = (String) getData(model).get(DriverItems.CMDENDCHR);
		}
		
		return terminator;
	}
}
