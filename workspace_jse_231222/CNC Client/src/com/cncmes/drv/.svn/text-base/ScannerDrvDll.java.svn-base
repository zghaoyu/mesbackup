package com.cncmes.drv;

import com.cncmes.base.Scanner;
import com.cncmes.base.ScannerItems;
import com.cncmes.data.ScannerData;
import com.sun.jna.Native;

/**
 * Scanner External Driver
 * @author LI ZI LONG
 *
 */
public class ScannerDrvDll implements Scanner {
	private static ScannerDrvDll scannerDrvDll = new ScannerDrvDll();
	private ScannerDrvDll(){}
	public static ScannerDrvDll getInstance(String libName){
		Native.register(libName);
		return scannerDrvDll;
	}
	
	private native String doScanning(String ip, int port);
	
	private int getPort(String ip){
		int port = 0;
		try {
			port = (int) ScannerData.getInstance().getData(ip).get(ScannerItems.PORT);
		} catch (Exception e) {
		}
		return port;
	}
	
	@Override
	public String doScanning(String ip) {
		return doScanning(ip, getPort(ip));
	}

}
