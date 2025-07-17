package com.cncmes.ctrl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.cncmes.base.Scanner;

public class ScannerFactory {
	/**
	 * @param	driver - specify the driver class path or dll name to be loaded
	 * @return	null when fail to load the driver
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Scanner getInstance(String driver){
		if(driver.indexOf("com.cncmes.drv") < 0){
			String libName = driver;
			if(driver.lastIndexOf(".dll") >= 0) libName = driver.replace(".dll", "");
			try {
				driver = "com.cncmes.drv.ScannerDrvDll";
				Class clazz = Class.forName(driver);
				Method method = clazz.getMethod("getInstance",String.class);
				return (Scanner) method.invoke(null,libName);
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				return null;
			}
		}else{
			try {
				Class clazz = Class.forName(driver);
				Method method = clazz.getMethod("getInstance");
				return (Scanner) method.invoke(null);
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				return null;
			}
		}
	}
}
