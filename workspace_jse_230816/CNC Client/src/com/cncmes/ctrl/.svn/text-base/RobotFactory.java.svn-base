package com.cncmes.ctrl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.cncmes.base.Robot;
import com.cncmes.utils.LogUtils;

/**
 * Factory to Create Robot Controller
 * @author LI ZI LONG
 *
 */
public class RobotFactory {
	/**
	 * @param	driver - specify the driver class path or dll name to be loaded
	 * @return	null when fail to load the driver
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Robot getInstance(String driver){
		Robot ctrl = null;
		Class clazz = null;
		Method method = null;
		try{
			if(driver.indexOf("com.cncmes.drv") < 0){
				String libName = driver;
				if(driver.lastIndexOf(".dll") >= 0) libName = driver.replace(".dll", "");
				driver = "com.cncmes.drv.RobotDrvDll";
				clazz = Class.forName(driver);
				method = clazz.getMethod("getInstance",String.class);
				ctrl = (Robot) method.invoke(null,libName);
			}else{
				clazz = Class.forName(driver);
				method = clazz.getMethod("getInstance");
				ctrl = (Robot) method.invoke(null);
			}
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			LogUtils.errorLog("RobotFactory getInstance("+driver+")"+e.getMessage());
		}
		return ctrl;
	}
}
