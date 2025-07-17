package com.cncmes.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.cncmes.data.SystemConfig;

public class XmlUtils {
	private static String getXmlFolder(){
		String folderPath = System.getProperty("user.dir") + File.separator + "XmlFiles";
		File file = new File(folderPath);
		if(!file.exists()) file.mkdirs();
		
		return folderPath;
	}
	
	public static String getXmlFilePath(boolean forWrite, String fileName){
		String path = getXmlFolder() + File.separator + fileName;
		
		if(!forWrite){
			if(!new File(path).exists()) path = "com/cncmes/xml/" + fileName;
		}
		
		return path;
	}
	
	public static void parseSystemConfig(){
		SystemConfig sysConfig = SystemConfig.getInstance();
		
		LinkedHashMap<String,Object> commonCfg = new LinkedHashMap<String,Object>();
		commonCfg.put("socketResponseTimeOut", "2");
		commonCfg.put("socketResponseInterval", "10");
		commonCfg.put("cmdRetryCount", "5");
		commonCfg.put("DeviceMonitorInterval", "5");
		commonCfg.put("NCProgramsRootDir", "\\\\10.10.81.38\\NC_Programs");
		commonCfg.put("CheckScheduler", "1");
		commonCfg.put("CheckRackManager", "1");
		commonCfg.put("RunningLog", "1");
		commonCfg.put("DebugLog", "0");
		commonCfg.put("DummyCNC", "");
		commonCfg.put("DummyRobot", "");
		
		LinkedHashMap<String,Object> databaseCfg = new LinkedHashMap<String,Object>();
		LinkedHashMap<String, Object> sqlServerCfg = new LinkedHashMap<String, Object>();
		databaseCfg.put("url", "jdbc:mysql://127.0.0.1:3306/cnc_client");
		databaseCfg.put("username", "root");
		databaseCfg.put("userpwd", "111111");
		databaseCfg.put("driver", "com.mysql.jdbc.Driver");
		
		LinkedHashMap<String,Object> ftpCfg = new LinkedHashMap<String,Object>();
		ftpCfg.put("ip", "127.0.0.1");
		ftpCfg.put("port", "21");
		ftpCfg.put("username", "wwwlionking");
		ftpCfg.put("userpwd", "www111111");

		LinkedHashMap<String, Object> monitorDatabaseCfg = new LinkedHashMap<String, Object>();
		monitorDatabaseCfg.put("url", "jdbc:mysql://10.10.204.97:3306/cnc_mes?useSSL=false&serverTimezone=GMT%2B8");
		monitorDatabaseCfg.put("username", "root");
		monitorDatabaseCfg.put("userpwd", "111111");
		monitorDatabaseCfg.put("driver", "com.mysql.jdbc.Driver");
		
		String xmlFilePath = getXmlFilePath(false, "SysConfig.xml");
		SAXReader reader = new SAXReader();
		InputStream is = null;
		try {
			if(xmlFilePath.startsWith("com/")){
				is = Thread.currentThread().getContextClassLoader().getResourceAsStream(xmlFilePath);
			}else{
				is = new FileInputStream(new File(xmlFilePath));
			}
			Document doc = reader.read(is);
			Element root = doc.getRootElement();
			Iterator<Element> iterRoot = root.elementIterator();
			while(iterRoot.hasNext()){
				Element node = iterRoot.next();
				String nodeName = node.getName();
				if("CommonCfg".equals(nodeName)){
					Iterator<Element> iterCfg = node.elementIterator();
					while(iterCfg.hasNext()){
						Element cfg = iterCfg.next();
						commonCfg.put(cfg.getName(), cfg.getTextTrim());
					}
				}
				
				if("DatabaseCfg".equals(nodeName)){
					Iterator<Element> iterCfg = node.elementIterator();
					while(iterCfg.hasNext()){
						Element cfg = iterCfg.next();
						databaseCfg.put(cfg.getName(), cfg.getTextTrim());
					}
				}
				
				if("FtpCfg".equals(nodeName)){
					Iterator<Element> iterCfg = node.elementIterator();
					while(iterCfg.hasNext()){
						Element cfg = iterCfg.next();
						ftpCfg.put(cfg.getName(), cfg.getTextTrim());
					}
				}

				if ("SqlServerCfg".equals(nodeName)) {
					Iterator<Element> iterCfg = node.elementIterator();
					while (iterCfg.hasNext()) {
						Element cfg = iterCfg.next();
						sqlServerCfg.put(cfg.getName(), cfg.getTextTrim());
					}
				}
				if ("MonitorDatabaseCfg".equals(nodeName)) {
					Iterator<Element> iterCfg = node.elementIterator();
					while (iterCfg.hasNext()) {
						Element cfg = iterCfg.next();
						monitorDatabaseCfg.put(cfg.getName(), cfg.getTextTrim());
					}
				}
			}
			is.close();
		} catch (Exception e) {
			LogUtils.errorLog("XmlUtils-parseSystemConfig() ERR:"+e.getMessage());
		}
		
		sysConfig.getDataMap().put("CommonCfg", commonCfg);
		sysConfig.getDataMap().put("DatabaseCfg", databaseCfg);
		sysConfig.getDataMap().put("SqlServerCfg", sqlServerCfg);
		sysConfig.getDataMap().put("FtpCfg", ftpCfg);
		sysConfig.getDataMap().put("MonitorDatabaseCfg", monitorDatabaseCfg);

		if(xmlFilePath.startsWith("com/")) saveSystemConfig(sysConfig.getDataMap());
	}
	
	public static boolean saveSystemConfig(Map<String, LinkedHashMap<String,Object>> config){
		boolean success = true;
		
		if(null != config && config.size() > 0){
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("SysConfig");
			for(String key:config.keySet()){
				LinkedHashMap<String,Object> items = config.get(key);
				if(null != items && items.size() > 0){
					Element cfg = root.addElement(key);
					for(String item:items.keySet()){
						Element cfgItem = cfg.addElement(item);
						cfgItem.setText((String)items.get(item));
						if("socketResponseTimeOut".equals(item)) cfgItem.addAttribute("unit", "s");
						if("socketResponseInterval".equals(item)) cfgItem.addAttribute("unit", "ms");
						if("DeviceMonitorInterval".equals(item)) cfgItem.addAttribute("unit", "s");
					}
				}
			}
			
			String xmlFilePath = getXmlFilePath(true, "SysConfig.xml");
			try {
				FileWriter fileWriter = new FileWriter(xmlFilePath);
				XMLWriter writer = new XMLWriter(fileWriter, OutputFormat.createPrettyPrint());
				writer.write(doc);
				writer.close();
			} catch (Exception e) {
				LogUtils.errorLog("XmlUtils-saveSystemConfig() ERR:"+e.getMessage());
				success = false;
			}
		}
		
		return success;
	}
}
