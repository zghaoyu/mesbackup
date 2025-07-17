package com.cncmes.ctrl;

import java.lang.reflect.Method;

import com.cncmes.base.CNC;
import com.cncmes.handler.CncDataHandler;
import com.cncmes.utils.LogUtils;

public class CncFactory {
	/**
	 * @param	
	 *       driver - the driver class path or dll name to be loaded
	 *       dataHandler - the result data processor,must specify one
	 *       cncModel - the CNC model name,must specify one
	 * @return	null when fail to load the driver
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static CNC getInstance(String driver, String dataHandler, String cncModel){
		CNC cncCtrl = null;
		CncDataHandler cncDataHandler = null;
		driver = driver.trim();
		dataHandler = dataHandler.trim();
		cncModel = cncModel.trim();
		
		if(!"".equals(driver)){
			Class clazz = null;
			Method method = null;
			try {
				if(!"".equals(dataHandler)){
					dataHandler = "com.cncmes.handler.impl."+dataHandler;
					clazz = Class.forName(dataHandler);
					method = clazz.getMethod("getInstance");
					cncDataHandler = (CncDataHandler) method.invoke(null);
				}
				
				if(driver.indexOf("com.cncmes.drv") < 0){
					String libName = driver;
					if(driver.lastIndexOf(".dll") >= 0) libName = driver.replace(".dll", "");
					driver = "com.cncmes.drv.CncDrvDll";
					clazz = Class.forName(driver);
					method = clazz.getMethod("getInstance",String.class);
					cncCtrl = (CNC) method.invoke(null,libName);
				}else{
					clazz = Class.forName(driver);
					method = clazz.getMethod("getInstance");
					cncCtrl = (CNC) method.invoke(null);
				}
			} catch (Exception e) {
				LogUtils.errorLog("CncFactory getInstance("+driver+","+dataHandler+","+cncModel+") ERR:"+e.getMessage());
			}
		}
		if(null != cncCtrl && null != cncDataHandler) cncCtrl.setDataHandler(cncModel, cncDataHandler);
		
		return cncCtrl;
	}
}
