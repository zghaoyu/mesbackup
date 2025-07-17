package com.cncmes.utils;

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
		commonCfg.put("NCProgramsRootDir", "\\\\10.10.81.38\\NC_Programs");
		
		LinkedHashMap<String,Object> databaseCfg = new LinkedHashMap<String,Object>();
		databaseCfg.put("url", "jdbc:mysql://127.0.0.1:3306/cnc_client");
		databaseCfg.put("username", "root");
		databaseCfg.put("userpwd", "111111");
		databaseCfg.put("driver", "com.mysql.jdbc.Driver");
		
		LinkedHashMap<String,Object> menuCfg = new LinkedHashMap<String,Object>();
		LinkedHashMap<String,Object> renderCfg = new LinkedHashMap<String,Object>();
		
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
				
				if("MenuCfg".equals(nodeName)){
					String menuName = "",subMenuName = "";
					String dtoHome = node.attributeValue("dtoHome");
					Iterator<Element> iterMenu = node.elementIterator();
					while(iterMenu.hasNext()){
						Element menuNode = iterMenu.next();
						menuName = menuNode.attributeValue("name");
						
						Iterator<Element> iterCfg = menuNode.elementIterator();
						LinkedHashMap<String,Object> subMenu = new LinkedHashMap<String,Object>();
						while(iterCfg.hasNext()){
							Element cfg = iterCfg.next();
							LinkedHashMap<String,Object> subCfg = new LinkedHashMap<String,Object>();
							subMenuName = cfg.attributeValue("name");
							subCfg.put("name", subMenuName);
							subCfg.put("dtoHome", dtoHome);
							subCfg.put("dtoClass", cfg.attributeValue("dtoClass"));
							subCfg.put("renderFields", cfg.attributeValue("renderFields"));
							subCfg.put("renderers", cfg.attributeValue("renderers"));
							subCfg.put("rights", cfg.attributeValue("rights"));
							if(null!=cfg.attributeValue("editor")){
								subCfg.put("editor", cfg.attributeValue("editor"));
							}
							subMenu.put(subMenuName, subCfg);
						}
						menuCfg.put(menuName, subMenu);
					}
				}
				
				if("RenderCfg".equals(nodeName)){
					String renderName = "",dtoClass="",fieldName="",fixedVals="";
					String dtoHome = node.attributeValue("dtoHome");
					Iterator<Element> iterRender = node.elementIterator();
					while(iterRender.hasNext()){
						Element renderNode = iterRender.next();
						renderName = renderNode.attributeValue("name");
						dtoClass = renderNode.attributeValue("dtoClass");
						fieldName = renderNode.attributeValue("fieldName");
						fixedVals = renderNode.attributeValue("fixedVals");
						
						String[] vals = null;
						if(!"".equals(fixedVals)){
							vals = fixedVals.split(",");
						}else if(!"".equals(dtoHome) && !"".equals(dtoClass) && !"".equals(fieldName)){
							ArrayList<Object> dt = DTOUtils.getDataFromDB(dtoHome+"."+dtoClass,null,null);
							if(null!=dt){
								String[] fields = (String[]) dt.get(0);
								Object[][] data = (Object[][]) dt.get(1);
								int col = -1;
								for(int i=0; i<fields.length; i++){
									if(fieldName.equals(fields[i])){
										col = i;
										break;
									}
								}
								if(col > 0 && null != data){
									String temp = "";
									for(int i=0; i<data.length; i++){
										if("".equals(temp)){
											temp = ""+data[i][col];
										}else{
											temp += ","+data[i][col];
										}
									}
									vals = temp.split(",");
								}
							}
						}
						
						LinkedHashMap<String,Object> render = new LinkedHashMap<String,Object>();
						render.put("name", renderName);
						render.put("dtoHome", dtoHome);
						render.put("dtoClass", dtoClass);
						render.put("fieldName", fieldName);
						render.put("fixedVals", fixedVals);
						render.put("listVals", vals);
						renderCfg.put(renderName, render);
					}
				}
			}
			is.close();
		} catch (Exception e) {
			LogUtils.errorLog("XmlUtils-parseSystemConfig() ERR:"+e.getMessage());
		}
		
		sysConfig.getDataMap().put("CommonCfg", commonCfg);
		sysConfig.getDataMap().put("DatabaseCfg", databaseCfg);
		sysConfig.getDataMap().put("MenuCfg", menuCfg);
		sysConfig.getDataMap().put("RenderCfg", renderCfg);
		
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
					if("MenuCfg".equals(key)){
						String dtoHome = "";
						for(String menuName:items.keySet()){
							Element menu = cfg.addElement("Menu");
							menu.addAttribute("name", menuName);
							
							@SuppressWarnings("unchecked")
							LinkedHashMap<String,Object> subMenuMap = (LinkedHashMap<String, Object>) items.get(menuName);
							if(null!=subMenuMap && subMenuMap.size()>0){
								for(String subMenuName:subMenuMap.keySet()){
									Element subMenu = menu.addElement("SubMenu");
									@SuppressWarnings("unchecked")
									LinkedHashMap<String,Object> subMap = (LinkedHashMap<String, Object>) subMenuMap.get(subMenuName);
									if(null==subMap) continue;
									subMenu.addAttribute("name", subMenuName);
									subMenu.addAttribute("dtoClass", ""+subMap.get("dtoClass"));
									subMenu.addAttribute("renderFields", ""+subMap.get("renderFields"));
									subMenu.addAttribute("renderers", ""+subMap.get("renderers"));
									subMenu.addAttribute("rights", ""+subMap.get("rights"));
									if(null!=subMap.get("editor")){
										subMenu.addAttribute("editor", ""+subMap.get("editor"));
									}
									dtoHome = (String)subMap.get("dtoHome");
								}
							}
						}
						cfg.addAttribute("dtoHome", dtoHome);
					}else if("RenderCfg".equals(key)){
						String dtoHome = "";
						for(String renderName:items.keySet()){
							Element render = cfg.addElement("Renderer");
							render.addAttribute("name", renderName);
							@SuppressWarnings("unchecked")
							LinkedHashMap<String,Object> subMap = (LinkedHashMap<String, Object>) items.get(renderName);
							if(null!=subMap && subMap.size()>0){
								for(String name:subMap.keySet()){
									if("dtoHome".equals(name)){
										dtoHome = (String) subMap.get(name);
									}else if("listVals".equals(name)){
									}else{
										render.addAttribute(name, (String) subMap.get(name));
									}
								}
							}
						}
						cfg.addAttribute("dtoHome", dtoHome);
					}else{
						for(String item:items.keySet()){
							Element cfgItem = cfg.addElement(item);
							cfgItem.setText((String)items.get(item));
							if("socketResponseTimeOut".equals(item)) cfgItem.addAttribute("unit", "s");
							if("socketResponseInterval".equals(item)) cfgItem.addAttribute("unit", "ms");
							if("DeviceMonitorInterval".equals(item)) cfgItem.addAttribute("unit", "s");
						}
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
