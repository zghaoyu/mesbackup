package com.cncmes.utils;

import java.util.ArrayList;
import java.util.ResourceBundle;

public class PropertiesUtils {
	private static ArrayList<ResourceBundle> resb = new ArrayList<ResourceBundle>();
	
	private static ResourceBundle getResourceBundle(String baseName){
		ResourceBundle rb = null;
		
		if(resb.size() > 0){
			for(int i=0; i<resb.size(); i++){
				if(resb.get(i).getBaseBundleName().indexOf(baseName) >= 0){
					rb = resb.get(i);
					break;
				}
			}
		}
		
		return rb;
	}
	
	public static String getProperty(String baseName, String keyName) throws Exception{
		ResourceBundle rb = getResourceBundle(baseName);
		String prop = null;
		if(null == rb){
			try{
				rb = ResourceBundle.getBundle(baseName);
				prop = rb.getString(keyName);
				resb.add(rb);
			}catch(Exception e){
				throw new Exception(e.getMessage());
			}
		}else{
			prop = rb.getString(keyName);
		}
		return prop;
	}
	
	public static String getSysProperty(String keyName){
		String s = System.getProperty(keyName); //like "os.name","java.io.tmpdir"
		return s;
	}
}
