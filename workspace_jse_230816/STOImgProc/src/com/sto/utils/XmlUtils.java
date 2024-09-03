package com.sto.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.sto.data.ProductSpec;
import com.sto.data.ProfileString;
import com.sto.data.SystemConfig;

public class XmlUtils {
	public static String getXmlFolder(){
		String folderPath = System.getProperty("user.dir") + File.separator + "XmlFiles";
		File file = new File(folderPath);
		if(!file.exists()) file.mkdirs();
		
		return folderPath;
	}
	
	public static String getXmlFilePath(boolean forWrite, String fileName){
		String path = getXmlFolder() + File.separator + fileName;
		
		if(!forWrite){
			if(!new File(path).exists()) path = "com/sto/xml/" + fileName;
		}
		
		return path;
	}
	
	public static void parseSystemConfig(){
		SystemConfig sysConfig = SystemConfig.getInstance();
		
		LinkedHashMap<String,Object> commonCfg = new LinkedHashMap<String,Object>();
		commonCfg.put("imgProcessingInterval", "5");
		commonCfg.put("imgProcQtyPerCycle", "600");
		commonCfg.put("runningLog", "0");
		
		LinkedHashMap<String,Object> imgPathCfg = new LinkedHashMap<String,Object>();
		imgPathCfg.put("okImgsFolder", "E:\\xRayImgs\\OK_IMG");
		imgPathCfg.put("ngImgsFolder", "E:\\xRayImgs\\NG_IMG");
		
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
				
				if("ImgPathCfg".equals(nodeName)){
					Iterator<Element> iterCfg = node.elementIterator();
					while(iterCfg.hasNext()){
						Element cfg = iterCfg.next();
						imgPathCfg.put(cfg.getName(), cfg.getTextTrim());
					}
				}
			}
			is.close();
		} catch (Exception e) {
			LogUtils.errorLog("XmlUtils-parseSystemConfig() ERR:"+e.getMessage());
		}
		
		sysConfig.getDataMap().put("CommonCfg", commonCfg);
		sysConfig.getDataMap().put("ImgPathCfg", imgPathCfg);
		
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
						if("imgProcessingInterval".equals(item)) cfgItem.addAttribute("unit", "s");
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
	
	public static String readProfileString(String key){
		String val = "";
		
		ProfileString profileString = ProfileString.getInstance();
		LinkedHashMap<String,Object> values = new LinkedHashMap<String,Object>();
		
		key = "K"+MathUtils.MD5Encode(key);
		String xmlFilePath = getXmlFilePath(false, "ProfileString.xml");
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
				if("Values".equals(nodeName)){
					Iterator<Element> iterCfg = node.elementIterator();
					while(iterCfg.hasNext()){
						Element cfg = iterCfg.next();
						values.put(cfg.getName(), cfg.getTextTrim());
					}
				}
			}
			is.close();
		} catch (Exception e) {
			LogUtils.errorLog("XmlUtils-readProfileString() ERR:"+e.getMessage());
		}
		
		profileString.getDataMap().put("Values", values);
		val = (null!=values.get(key)?(String)values.get(key):"");
		return val;
	}
	
	public static boolean writeProfileString(String key, String val){
		boolean success = true;
		
		if(null!=key && val!=null 
			&& !"".equals(key.trim()) 
			&& !"".equals(val.trim())){
			
			key = "K"+MathUtils.MD5Encode(key);
			readProfileString("");
			ProfileString profileString = ProfileString.getInstance();
			LinkedHashMap<String,Object> values = profileString.getData("Values");
			values.put(key, val);
			
			Document doc = DocumentHelper.createDocument();
			Element cfg = doc.addElement("ProfileString").addElement("Values");
			
			for(String k:values.keySet()){
				Element cfgItem = cfg.addElement(k);
				cfgItem.setText((String)values.get(k));
			}
			
			String xmlFilePath = getXmlFilePath(true, "ProfileString.xml");
			try {
				FileWriter fileWriter = new FileWriter(xmlFilePath);
				XMLWriter writer = new XMLWriter(fileWriter, OutputFormat.createPrettyPrint());
				writer.write(doc);
				writer.close();
			} catch (Exception e) {
				LogUtils.errorLog("XmlUtils-writeProfileString() ERR:"+e.getMessage());
				success = false;
			}
		}
		
		return success;
	}
	
	public static LinkedHashMap<String,String> parseLicFile(){
		LinkedHashMap<String,String> config = new LinkedHashMap<String,String>();
		String xmlFilePath = getXmlFilePath(false, "License.xml");
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
				config.put(node.getName(), node.getTextTrim());
			}
			is.close();
		} catch (Exception e) {
			LogUtils.errorLog("XmlUtils-parseLicFile() ERR:"+e.getMessage());
		}
		return config;
	}
	
	public static boolean saveLicFile(LinkedHashMap<String,String> config){
		boolean success = true;
		
		if(null != config && config.size() > 0){
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("License");
			for(String key:config.keySet()){
				Element cfgItem = root.addElement(key);
				cfgItem.setText((String)config.get(key));
			}
			
			String xmlFilePath = getXmlFilePath(true, "License.xml");
			try {
				FileWriter fileWriter = new FileWriter(xmlFilePath);
				XMLWriter writer = new XMLWriter(fileWriter, OutputFormat.createPrettyPrint());
				writer.write(doc);
				writer.close();
			} catch (Exception e) {
				LogUtils.errorLog("XmlUtils-saveLicFile() ERR:"+e.getMessage());
				success = false;
			}
		}
		
		return success;
	}
	
	public static void parseProductSpec(){
		ProductSpec productSpec = ProductSpec.getInstance();
		Map<String, LinkedHashMap<String, Object>> prodSpec = productSpec.getDataMap();
		prodSpec.clear();
		
		String specName = "", imgProcRootDir = "", nodeName = "";
		String xmlFilePath = getXmlFilePath(false, "ProductSpec.xml");
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
				nodeName = node.getName();
				if("Criteria".equals(nodeName)){
					LinkedHashMap<String,Object> criteriaSetting = new LinkedHashMap<String,Object>();
					ArrayList<String> rawImgFolders = new ArrayList<String>();
					specName = node.attributeValue("specName");
					imgProcRootDir = node.attributeValue("imgProcRootDir");
					if(null==specName || null==imgProcRootDir) continue;
					if("".equals(specName.trim()) || "".equals(imgProcRootDir.trim())) continue;
					
					criteriaSetting.put("specName", specName);
					criteriaSetting.put("imgProcRootDir", imgProcRootDir);
					
					Iterator<Element> iterCfg = node.elementIterator();
					while(iterCfg.hasNext()){
						Element cfg = iterCfg.next();
						nodeName = cfg.getName();
						if("rawImgFolders".equals(nodeName)){
							Iterator<Element> iterFolders = cfg.elementIterator();
							while(iterFolders.hasNext()){
								Element folder = iterFolders.next();
								if(!"".equals(folder.getTextTrim())){
									rawImgFolders.add(folder.getTextTrim());
								}
							}
						}else{
							criteriaSetting.put(cfg.getName(), cfg.getTextTrim());
						}
					}
					
					criteriaSetting.put("rawImgFolders", rawImgFolders);
					prodSpec.put(imgProcRootDir, criteriaSetting);
				}
			}
			is.close();
		} catch (Exception e) {
			LogUtils.errorLog("XmlUtils-parseProductSpec() ERR:"+e.getMessage());
		}
		
		if(xmlFilePath.startsWith("com/")) saveProductSpec(prodSpec);
	}
	
	@SuppressWarnings("unchecked")
	public static boolean saveProductSpec(Map<String, LinkedHashMap<String,Object>> config){
		boolean success = true;
		
		if(null != config && config.size() > 0){
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("ProductSpec");
			String specName = "", imgProcRootDir = "";
			ArrayList<String> rawImgFolders = new ArrayList<String>();
			
			for(String key:config.keySet()){
				LinkedHashMap<String,Object> items = config.get(key);
				if(null != items && items.size() > 0){
					specName = (String)items.get("specName");
					imgProcRootDir = (String)items.get("imgProcRootDir");
					rawImgFolders = (ArrayList<String>) items.get("rawImgFolders");
					if(null==specName || null==imgProcRootDir) continue;
					
					Element cfg = root.addElement("Criteria");
					cfg.addAttribute("specName", specName);
					cfg.addAttribute("imgProcRootDir", imgProcRootDir);
					
					for(String item:items.keySet()){
						if("specName".equals(item)
							|| "imgProcRootDir".equals(item)
							|| "rawImgFolders".equals(item)) continue;
						
						Element cfgItem = cfg.addElement(item);
						cfgItem.setText((String)items.get(item));
						if("onePixel".equals(item)) cfgItem.addAttribute("unit", "mm");
						if("minPoleDistance".equals(item)) cfgItem.addAttribute("unit", "mm");
						if("maxPoleDistance".equals(item)) cfgItem.addAttribute("unit", "mm");
						if("maxPoleAngle".equals(item)) cfgItem.addAttribute("unit", "degree");
					}
					
					if(null!=rawImgFolders && rawImgFolders.size()>0){
						Element cfgItem = cfg.addElement("rawImgFolders");
						for(int i=0; i<rawImgFolders.size(); i++){
							Element folder = cfgItem.addElement("imgFolder");
							folder.setText(rawImgFolders.get(i));
						}
					}
				}
			}
			
			String xmlFilePath = getXmlFilePath(true, "ProductSpec.xml");
			try {
				FileWriter fileWriter = new FileWriter(xmlFilePath);
				XMLWriter writer = new XMLWriter(fileWriter, OutputFormat.createPrettyPrint());
				writer.write(doc);
				writer.close();
			} catch (Exception e) {
				LogUtils.errorLog("XmlUtils-saveProductSpec() ERR:"+e.getMessage());
				success = false;
			}
		}
		
		return success;
	}
}
