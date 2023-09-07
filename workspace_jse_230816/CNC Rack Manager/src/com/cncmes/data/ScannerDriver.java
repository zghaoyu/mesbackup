package com.cncmes.data;

import com.cncmes.base.DriverItems;
import com.cncmes.base.RunningData;

public class ScannerDriver extends RunningData<DriverItems> {
	private static ScannerDriver scannerDriver = new ScannerDriver();
	private ScannerDriver(){}
	public static ScannerDriver getInstance(){
		return scannerDriver;
	}
}
