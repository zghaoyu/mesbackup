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

import com.cncmes.base.CncWebAPIItems;
import com.cncmes.data.CncEthernetCmd;
import com.cncmes.data.CncWebAPI;
import com.cncmes.data.RobotEthernetCmd;
import com.cncmes.data.SystemConfig;

public class XmlUtils {
	private static final String WEBAPICMDPARASEPERATOR = ";";
	
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
	
	/**
	 * 
	 * @throws Exception DOM4J parsing exception
	 */
	public static void parseCncWebAPIXml() throws Exception{
		String xmlFilePath = getXmlFilePath(false, "CncWebAPI.xml");
		
		SAXReader reader = new SAXReader();
		InputStream is = null;
		if(xmlFilePath.startsWith("com/")){
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(xmlFilePath);
		}else{
			is = new FileInputStream(new File(xmlFilePath));
		}
		Document doc = reader.read(is);
		Element root = doc.getRootElement();
		Iterator<Element> iterCnc = root.elementIterator();
		
		CncWebAPI cncWebAPI = CncWebAPI.getInstance();
		cncWebAPI.clearData();
		while(iterCnc.hasNext()){
			Element cncNode = iterCnc.next();
			String brand = cncNode.attributeValue("brand");
			String model = cncNode.attributeValue("model");
			String mainKey = cncNode.attributeValue("key");
			String cmdUrl = cncNode.attributeValue("commandUrl");
			String ncProgramName_sub1 = cncNode.attributeValue("ncProgramName_sub1");
			String ncProgramName_sub2 = cncNode.attributeValue("ncProgramName_sub2");
			String ncProgramName_sub3 = cncNode.attributeValue("ncProgramName_sub3");
			String ncProgramName_sub4 = cncNode.attributeValue("ncProgramName_sub4");
			String ncProgramName_sub5 = cncNode.attributeValue("ncProgramName_sub5");
			String ncProgramName_sub6 = cncNode.attributeValue("ncProgramName_sub6");
			String ncProgramName_main = cncNode.attributeValue("ncProgramName_main");
			String cmdCoordinate1 = cncNode.attributeValue("cmdCoordinate1");
			String cmdCoordinate2 = cncNode.attributeValue("cmdCoordinate2");
			String cmdCoordinate3 = cncNode.attributeValue("cmdCoordinate3");
			String cmdCoordinate4 = cncNode.attributeValue("cmdCoordinate4");
			String cmdCoordinate5 = cncNode.attributeValue("cmdCoordinate5");
			String cmdCoordinate6 = cncNode.attributeValue("cmdCoordinate6");
			String debugIP = cncNode.attributeValue("debugIP");
			String debugPort = cncNode.attributeValue("debugPort");
			
			String ftpPort = "", ftpUser = "", ftpPwd = "";
			if(null != cncNode.attributeValue("ftpPort") && null != cncNode.attributeValue("ftpUser") && null != cncNode.attributeValue("ftpPwd")){
				ftpPort = cncNode.attributeValue("ftpPort");
				ftpUser = cncNode.attributeValue("ftpUser");
				ftpPwd = cncNode.attributeValue("ftpPwd");
			}
			if(!cmdUrl.endsWith("/")) cmdUrl += "/";
			
			LinkedHashMap<CncWebAPIItems,String> commonCfg = new LinkedHashMap<CncWebAPIItems,String>();
			commonCfg.put(CncWebAPIItems.BRAND, brand);
			commonCfg.put(CncWebAPIItems.MODEL, model);
			commonCfg.put(CncWebAPIItems.URL, cmdUrl);
			
			commonCfg.put(CncWebAPIItems.NCPROGSUB1, ncProgramName_sub1);
			commonCfg.put(CncWebAPIItems.NCPROGSUB2, ncProgramName_sub2);
			commonCfg.put(CncWebAPIItems.NCPROGSUB3, ncProgramName_sub3);
			commonCfg.put(CncWebAPIItems.NCPROGSUB4, ncProgramName_sub4);
			commonCfg.put(CncWebAPIItems.NCPROGSUB5, ncProgramName_sub5);
			commonCfg.put(CncWebAPIItems.NCPROGSUB6, ncProgramName_sub6);
			commonCfg.put(CncWebAPIItems.NCPROGMAIN, ncProgramName_main);
			commonCfg.put(CncWebAPIItems.COORDINATE1, cmdCoordinate1);
			commonCfg.put(CncWebAPIItems.COORDINATE2, cmdCoordinate2);
			commonCfg.put(CncWebAPIItems.COORDINATE3, cmdCoordinate3);
			commonCfg.put(CncWebAPIItems.COORDINATE4, cmdCoordinate4);
			commonCfg.put(CncWebAPIItems.COORDINATE5, cmdCoordinate5);
			commonCfg.put(CncWebAPIItems.COORDINATE6, cmdCoordinate6);
			if(!"".equals(ftpPort) && !"".equals(ftpUser) && !"".equals(ftpPwd)){
				commonCfg.put(CncWebAPIItems.FTPPORT, ftpPort);
				commonCfg.put(CncWebAPIItems.FTPUSER, ftpUser);
				commonCfg.put(CncWebAPIItems.FTPPWD, ftpPwd);
			}
			commonCfg.put(CncWebAPIItems.DEBUGIP, debugIP);
			commonCfg.put(CncWebAPIItems.DEBUGPORT, debugPort);
			cncWebAPI.setData(mainKey, "Common", commonCfg);
			
			Iterator<Element> iterOpr = cncNode.elementIterator();
			while(iterOpr.hasNext()){
				Element oprNode = iterOpr.next();
				String operationName = oprNode.attributeValue("name");
				String operator = "Myself";
				if(null!=oprNode.attributeValue("operator")) operator = oprNode.attributeValue("operator");
				
				LinkedHashMap<String,Object> commands = new LinkedHashMap<String,Object>();
				LinkedHashMap<CncWebAPIItems,String> oprInfo = new LinkedHashMap<CncWebAPIItems,String>();
				oprInfo.put(CncWebAPIItems.OPERATOR, operator);
				commands.put(operationName, oprInfo);
				
				Iterator<Element> iterCmd = oprNode.elementIterator();
				while(iterCmd.hasNext()){
					Element cmdNode = iterCmd.next();
					String cmdID = cmdNode.attributeValue("id");
					String cmdName = cmdNode.attributeValue("name");
					String cmdSensorCheck = "";
					if ( null != cmdNode.attributeValue("sensorcheck")){
						cmdSensorCheck = cmdNode.attributeValue("sensorcheck");
					}else{
						cmdSensorCheck = "";
					}
					
					
					LinkedHashMap<CncWebAPIItems,String> cmdInfo = new LinkedHashMap<CncWebAPIItems,String>();
					cmdInfo.put(CncWebAPIItems.ID, cmdID);
					cmdInfo.put(CncWebAPIItems.NAME, cmdName);
					cmdInfo.put(CncWebAPIItems.SENSORCHECK, cmdSensorCheck);
					
					Iterator<Element> iterPara = cmdNode.elementIterator();
					while(iterPara.hasNext()){
						Element paraNode = iterPara.next();
						String inParas = "", inParasType = "", inParasVal = "";
						String outParas = "", outParasType = "", outParasVal = "";
						
						Iterator<Element> iterParas = paraNode.elementIterator();
						while(iterParas.hasNext()){
							Element parasNode = iterParas.next();
							String paraName = parasNode.getName();
							String paraType = parasNode.attributeValue("type");
							String paraVal = parasNode.getTextTrim();
							
							if("InputParas".equals(paraNode.getName())){
								if("".equals(inParas)){
									inParas = paraName;
									inParasType = paraType;
									inParasVal = paraVal;
								}else{
									inParas += WEBAPICMDPARASEPERATOR + paraName;
									inParasType += WEBAPICMDPARASEPERATOR + paraType;
									inParasVal += WEBAPICMDPARASEPERATOR + paraVal;
								}
							}else if("OutputParas".equals(paraNode.getName())){
								if("".equals(outParas)){
									outParas = paraName;
									outParasType = paraType;
									outParasVal = paraVal;
								}else{
									outParas += WEBAPICMDPARASEPERATOR + paraName;
									outParasType += WEBAPICMDPARASEPERATOR + paraType;
									outParasVal += WEBAPICMDPARASEPERATOR + paraVal;
								}
							}
						}
						
						if(!"".equals(inParas)){
							cmdInfo.put(CncWebAPIItems.INPARAS, inParas);
							cmdInfo.put(CncWebAPIItems.INPARASTYPE, inParasType);
							cmdInfo.put(CncWebAPIItems.INPARASVAL, inParasVal);
						}
						if(!"".equals(outParas)){
							cmdInfo.put(CncWebAPIItems.OUTPARAS, outParas);
							cmdInfo.put(CncWebAPIItems.OUTPARASTYPE, outParasType);
							cmdInfo.put(CncWebAPIItems.OUTPARASVAL, outParasVal);
						}
					}
					
					commands.put(cmdName+"#"+cmdID, cmdInfo);
				}
				
				cncWebAPI.setData(mainKey, operationName, commands);
			}
		}
	}
	
	public static boolean saveCncWebAPIXml(){
		boolean success = true;
		
		String xmlFilePath = getXmlFilePath(true, "CncWebAPI.xml");
		Map<String,LinkedHashMap<String,Object>> dataMap = CncWebAPI.getInstance().getDataMap();
		if(null != dataMap && dataMap.size() > 0){
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("CncWebAPI");
			
			for(String mainKey:dataMap.keySet()){
				@SuppressWarnings("unchecked")
				LinkedHashMap<CncWebAPIItems,String> commonCfg = (LinkedHashMap<CncWebAPIItems, String>) dataMap.get(mainKey).get("Common");
				if(null == commonCfg) continue;
				
				String brand = commonCfg.get(CncWebAPIItems.BRAND);
				String model = commonCfg.get(CncWebAPIItems.MODEL);
				String commandUrl = commonCfg.get(CncWebAPIItems.URL);
				String ncProgramName_sub1 = commonCfg.get(CncWebAPIItems.NCPROGSUB1);
				String ncProgramName_sub2 = commonCfg.get(CncWebAPIItems.NCPROGSUB2);
				String ncProgramName_sub3 = commonCfg.get(CncWebAPIItems.NCPROGSUB3);
				String ncProgramName_sub4 = commonCfg.get(CncWebAPIItems.NCPROGSUB4);
				String ncProgramName_sub5 = commonCfg.get(CncWebAPIItems.NCPROGSUB5);
				String ncProgramName_sub6 = commonCfg.get(CncWebAPIItems.NCPROGSUB6);
				String ncProgramName_main = commonCfg.get(CncWebAPIItems.NCPROGMAIN);
				String cmdCoordinate1 = commonCfg.get(CncWebAPIItems.COORDINATE1);
				String cmdCoordinate2 = commonCfg.get(CncWebAPIItems.COORDINATE2);
				String cmdCoordinate3 = commonCfg.get(CncWebAPIItems.COORDINATE3);
				String cmdCoordinate4 = commonCfg.get(CncWebAPIItems.COORDINATE4);
				String cmdCoordinate5 = commonCfg.get(CncWebAPIItems.COORDINATE5);
				String cmdCoordinate6 = commonCfg.get(CncWebAPIItems.COORDINATE6);
				String debugIP = commonCfg.get(CncWebAPIItems.DEBUGIP);
				String debugPort = commonCfg.get(CncWebAPIItems.DEBUGPORT);
				
				String ftpPort = "", ftpUser = "", ftpPwd = "";
				if(null != commonCfg.get(CncWebAPIItems.FTPPORT) && 
						null != commonCfg.get(CncWebAPIItems.FTPUSER) &&
						null != commonCfg.get(CncWebAPIItems.FTPPWD)){
					ftpPort = commonCfg.get(CncWebAPIItems.FTPPORT);
					ftpUser = commonCfg.get(CncWebAPIItems.FTPUSER);
					ftpPwd = commonCfg.get(CncWebAPIItems.FTPPWD);
				}
				
				Element cncNode = root.addElement("CNC")
						.addAttribute("key", ""+mainKey)
						.addAttribute("brand", brand)
						.addAttribute("model", model)
						.addAttribute("commandUrl", commandUrl)
						.addAttribute("debugPort", debugPort)
						.addAttribute("ncProgramName_sub1", ncProgramName_sub1)
						.addAttribute("ncProgramName_sub2", ncProgramName_sub2)
						.addAttribute("ncProgramName_sub3", ncProgramName_sub3)
						.addAttribute("ncProgramName_sub4", ncProgramName_sub4)
						.addAttribute("ncProgramName_sub5", ncProgramName_sub5)
						.addAttribute("ncProgramName_sub6", ncProgramName_sub6)
						.addAttribute("ncProgramName_main", ncProgramName_main)
						.addAttribute("cmdCoordinate1", cmdCoordinate1)
						.addAttribute("cmdCoordinate2", cmdCoordinate2)
						.addAttribute("cmdCoordinate3", cmdCoordinate3)
						.addAttribute("cmdCoordinate4", cmdCoordinate4)
						.addAttribute("cmdCoordinate5", cmdCoordinate5)
						.addAttribute("cmdCoordinate6", cmdCoordinate6)
						.addAttribute("debugIP", debugIP);
				
				if(!"".equals(ftpPort) && !"".equals(ftpUser) && !"".equals(ftpPwd)){
					cncNode.addAttribute("ftpPort", ftpPort)
					.addAttribute("ftpUser", ftpUser)
					.addAttribute("ftpPwd", ftpPwd);
				}
				
				LinkedHashMap<String,Object> operationMap = dataMap.get(mainKey);
				if(null != operationMap && operationMap.size() > 0){
					for(String oprName:operationMap.keySet()){
						if("Common".equals(oprName)) continue;
						@SuppressWarnings("unchecked")
						LinkedHashMap<String,Object> cmdMap = (LinkedHashMap<String, Object>) operationMap.get(oprName);
						if(null == cmdMap || cmdMap.size() <= 0) continue;
						Element oprNode = cncNode.addElement("Operation")
								.addAttribute("name", oprName);
						
						for(String cmdName:cmdMap.keySet()){
							@SuppressWarnings("unchecked")
							LinkedHashMap<CncWebAPIItems,String> cmdInfo = (LinkedHashMap<CncWebAPIItems, String>) cmdMap.get(cmdName);
							if(oprName.equals(cmdName)){
								String operator = (null!=cmdInfo.get(CncWebAPIItems.OPERATOR))?cmdInfo.get(CncWebAPIItems.OPERATOR):"Myself";
								oprNode.addAttribute("operator", operator);
								continue;
							}
							String id = (null!=cmdInfo.get(CncWebAPIItems.ID))?cmdInfo.get(CncWebAPIItems.ID):"";
							String name = (null!=cmdInfo.get(CncWebAPIItems.NAME))?cmdInfo.get(CncWebAPIItems.NAME):"";
							String sensorcheck = (null!=cmdInfo.get(CncWebAPIItems.SENSORCHECK))?cmdInfo.get(CncWebAPIItems.SENSORCHECK):"";
							String inParas = (null!=cmdInfo.get(CncWebAPIItems.INPARAS))?cmdInfo.get(CncWebAPIItems.INPARAS):"";
							String inParasType = (null!=cmdInfo.get(CncWebAPIItems.INPARASTYPE))?cmdInfo.get(CncWebAPIItems.INPARASTYPE):"";
							String inParasVal = (null!=cmdInfo.get(CncWebAPIItems.INPARASVAL))?cmdInfo.get(CncWebAPIItems.INPARASVAL):"";
							String outParas = (null!=cmdInfo.get(CncWebAPIItems.OUTPARAS))?cmdInfo.get(CncWebAPIItems.OUTPARAS):"";
							String outParasType = (null!=cmdInfo.get(CncWebAPIItems.OUTPARASTYPE))?cmdInfo.get(CncWebAPIItems.OUTPARASTYPE):"";
							String outParasVal = (null!=cmdInfo.get(CncWebAPIItems.OUTPARASVAL))?cmdInfo.get(CncWebAPIItems.OUTPARASVAL):"";
							
							if("".equals(id) || "".equals(name)) continue;
							Element cmdNode = oprNode.addElement("Command")
									.addAttribute("id", id)
									.addAttribute("name", name)
									.addAttribute("sensorcheck", sensorcheck);
							
							if(!"".equals(inParas) && !"".equals(inParasType) && !"".equals(inParasVal)){
								String[] paras = inParas.split(WEBAPICMDPARASEPERATOR);
								String[] type = inParasType.split(WEBAPICMDPARASEPERATOR);
								String[] vals = inParasVal.split(WEBAPICMDPARASEPERATOR);
								
								Element paraNode = cmdNode.addElement("InputParas");
								for(int i=0; i<paras.length; i++){
									paraNode.addElement(paras[i]).addAttribute("type", type[i]).setText(vals[i]);
								}
							}
							
							if(!"".equals(outParas) && !"".equals(outParasType) && !"".equals(outParasVal)){
								String[] paras = outParas.split(WEBAPICMDPARASEPERATOR);
								String[] type = outParasType.split(WEBAPICMDPARASEPERATOR);
								String[] vals = outParasVal.split(WEBAPICMDPARASEPERATOR);
								
								Element paraNode = cmdNode.addElement("OutputParas");
								for(int i=0; i<paras.length; i++){
									paraNode.addElement(paras[i]).addAttribute("type", type[i]).setText(vals[i]);
								}
							}
						}
					}
				}
			}
			
			try {
				FileWriter fileWriter = new FileWriter(xmlFilePath);
				XMLWriter writer = new XMLWriter(fileWriter, OutputFormat.createPrettyPrint());
				writer.write(doc);
				writer.close();
			} catch (Exception e) {
				success = false;
				LogUtils.errorLog("XmlUtils-saveCncWebAPIXml ERR:"+e.getMessage());
			}
		}
		
		return success;
	}
	
	public static boolean parseRobotEthernetCmdXml(){
		boolean success = true;
		String xmlFilePath = getXmlFilePath(false, "RobotEthernetCmd.xml");
		SAXReader reader = new SAXReader();
		InputStream is = null;
		RobotEthernetCmd robotNetworkCmd = RobotEthernetCmd.getInstance();
		
		try {
			if(xmlFilePath.startsWith("com/")){
				is = Thread.currentThread().getContextClassLoader().getResourceAsStream(xmlFilePath);
			}else{
				is = new FileInputStream(new File(xmlFilePath));
			}
			Document doc = reader.read(is);
			Element root = doc.getRootElement();
			
			robotNetworkCmd.getDataMap().clear();
			Iterator<Element> iterRoot = root.elementIterator();
			while(iterRoot.hasNext()){
				Element robotNode = iterRoot.next();
				String key = robotNode.attributeValue("key");
				String targetName = robotNode.attributeValue("targetName");
				String mainKey = key + "#" + targetName;
				
				LinkedHashMap<String,Object> commonCfg = new LinkedHashMap<String,Object>();
				commonCfg.put("seperator", robotNode.attributeValue("seperator"));
				commonCfg.put("returnCodePosition", robotNode.attributeValue("returnCodePosition"));
				commonCfg.put("successCode", robotNode.attributeValue("successCode"));
				commonCfg.put("debugIP", robotNode.attributeValue("debugIP"));
				commonCfg.put("debugPort", robotNode.attributeValue("debugPort"));
				commonCfg.put("cmdTimeout", robotNode.attributeValue("cmdTimeout"));
				commonCfg.put("sleepTime", robotNode.attributeValue("sleepTime"));
				robotNetworkCmd.setData(mainKey,"Common",commonCfg);
				
				Iterator<Element> iterRobot = robotNode.elementIterator();
				while(iterRobot.hasNext()){
					Element oprNode = iterRobot.next();
					String operationName = oprNode.attributeValue("name");
					String operator = "Myself";
					if(null != oprNode.attributeValue("operator")) operator = oprNode.attributeValue("operator");
					
					LinkedHashMap<String,Object> cmds = new LinkedHashMap<String,Object>();
					ArrayList<String> oprInfo = new ArrayList<String>();
					oprInfo.add(operator);
					cmds.put(operationName, oprInfo);
					
					Iterator<Element> iterCmd = oprNode.elementIterator();
					while(iterCmd.hasNext()){
						Element cmdNode = iterCmd.next();
						String cmdName = cmdNode.attributeValue("name");
						String cmdID = cmdNode.attributeValue("id");
						
						ArrayList<String> para = new ArrayList<String>();
						para.add(cmdNode.elementText("InParas"));
						para.add(cmdNode.elementText("InParasVal"));
						para.add(cmdName);
						cmds.put(cmdName+"#"+cmdID, para);
					}
					robotNetworkCmd.setData(mainKey, operationName, cmds);
				}
			}
			is.close();
		} catch (Exception e) {
			success = false;
			LogUtils.errorLog("XmlUtils-parseRobotEthernetCmdXml ERR:"+e.getMessage());
		}
		
		if(xmlFilePath.startsWith("com/")) saveRobotEthernetCmdXml();
		return success;
	}
	
	@SuppressWarnings("unchecked")
	public static boolean saveRobotEthernetCmdXml(){
		boolean success = true;
		
		String xmlFilePath = getXmlFilePath(true, "RobotEthernetCmd.xml");
		Map<String,LinkedHashMap<String,Object>> dataMap = RobotEthernetCmd.getInstance().getDataMap();
		if(null != dataMap && dataMap.size() > 0){
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("RobotEthernetCmd");
			
			String[] cmdInfo = null, keys = null;
			for(String mainKey:dataMap.keySet()){
				LinkedHashMap<String,Object> oprMap = dataMap.get(mainKey);
				if(!(null != oprMap && oprMap.size() > 0)) continue;
				
				LinkedHashMap<String,Object> cmdMap = (LinkedHashMap<String, Object>) oprMap.get("Common");
				if(!(null != cmdMap && cmdMap.size() > 0)) continue;
				
				keys = mainKey.split("#");
				Element robot = root.addElement("Robot");
				robot.addAttribute("key", keys[0]);
				robot.addAttribute("targetName", keys[1]);
				for(String attr:cmdMap.keySet()){
					robot.addAttribute(attr, (String)cmdMap.get(attr));
				}
				
				for(String oprName:oprMap.keySet()){
					if("Common".equals(oprName)) continue;
					cmdMap = (LinkedHashMap<String, Object>) oprMap.get(oprName);
					if(!(null != cmdMap && cmdMap.size() > 0)) continue;
					
					Element oprNode = robot.addElement("Operation")
							.addAttribute("name", oprName);
					
					for(String cmdName:cmdMap.keySet()){
						ArrayList<String> cmdPara = (ArrayList<String>) cmdMap.get(cmdName);
						if(oprName.equals(cmdName)){
							oprNode.addAttribute("operator", cmdPara.get(0));
							continue;
						}
						cmdInfo = cmdName.split("#");
						Element cmdNode = oprNode.addElement("Command")
								.addAttribute("name", cmdInfo[0])
								.addAttribute("id", cmdInfo[1]);
						cmdNode.addElement("InParas").setText(cmdPara.get(0));
						cmdNode.addElement("InParasVal").setText(cmdPara.get(1));
					}
				}
			}
			
			try {
				FileWriter fileWriter = new FileWriter(xmlFilePath);
				XMLWriter writer = new XMLWriter(fileWriter, OutputFormat.createPrettyPrint());
				writer.write(doc);
				writer.close();
			} catch (Exception e) {
				success = false;
				LogUtils.errorLog("XmlUtils-saveRobotEthernetCmdXml ERR:"+e.getMessage());
			}
		}
		
		return success;
	}
	
	public static void parseSystemConfig(){
		SystemConfig sysConfig = SystemConfig.getInstance();
		
		LinkedHashMap<String,Object> commonCfg = new LinkedHashMap<String,Object>();
		commonCfg.put("socketResponseTimeOut", "2");
		commonCfg.put("socketResponseInterval", "10");
		commonCfg.put("cmdRetryCount", "5");
		commonCfg.put("DeviceMonitorInterval", "5");
		commonCfg.put("NCProgramsRootDir", "D:\\NC_Programs");
		commonCfg.put("MachiningScriptPrefix", "JDSU44100");
		commonCfg.put("CleaningScriptPrefix", "0000000000");
		commonCfg.put("CleaningTimes", "1");
		commonCfg.put("AppMode", "0");
		commonCfg.put("CheckScheduler", "1");
		commonCfg.put("CheckRackManager", "1");
		commonCfg.put("RunningLog", "1");
		commonCfg.put("DebugLog", "0");
		commonCfg.put("DummyCNC", "127.0.0.1");
		commonCfg.put("DummyRobot", "127.0.0.1");
		
		LinkedHashMap<String,Object> databaseCfg = new LinkedHashMap<String,Object>();
		databaseCfg.put("url", "jdbc:mysql://127.0.0.1:3306/cnc_client");
		databaseCfg.put("username", "root");
		databaseCfg.put("userpwd", "111111");
		databaseCfg.put("driver", "com.mysql.jdbc.Driver");
		
		LinkedHashMap<String,Object> ftpCfg = new LinkedHashMap<String,Object>();
		ftpCfg.put("ip", "127.0.0.1");
		ftpCfg.put("port", "21");
		ftpCfg.put("username", "wwwlionking");
		ftpCfg.put("userpwd", "www111111");
		
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
			}
			is.close();
		} catch (Exception e) {
			LogUtils.errorLog("XmlUtils-parseSystemConfig() ERR:"+e.getMessage());
		}
		
		sysConfig.getDataMap().put("CommonCfg", commonCfg);
		sysConfig.getDataMap().put("DatabaseCfg", databaseCfg);
		sysConfig.getDataMap().put("FtpCfg", ftpCfg);
		
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
	
	public static boolean parseCncEthernetCmdXml(){
		boolean success = true;
		String xmlFilePath = getXmlFilePath(false, "CncEthernetCmd.xml");
		SAXReader reader = new SAXReader();
		InputStream is = null;
		CncEthernetCmd cncEthernetCmd = CncEthernetCmd.getInstance();
		
		try {
			if(xmlFilePath.startsWith("com/")){
				is = Thread.currentThread().getContextClassLoader().getResourceAsStream(xmlFilePath);
			}else{
				is = new FileInputStream(new File(xmlFilePath));
			}
			Document doc = reader.read(is);
			Element root = doc.getRootElement();
			
			cncEthernetCmd.getDataMap().clear();
			Iterator<Element> iterRoot = root.elementIterator();
			while(iterRoot.hasNext()){
				Element robotNode = iterRoot.next();
				String mainKey = robotNode.attributeValue("key");
				LinkedHashMap<String,Object> commonCfg = new LinkedHashMap<String,Object>();
				commonCfg.put("seperator", robotNode.attributeValue("seperator"));
				commonCfg.put("returnCodePosition", robotNode.attributeValue("returnCodePosition"));
				commonCfg.put("successCode", robotNode.attributeValue("successCode"));
				commonCfg.put("debugIP", robotNode.attributeValue("debugIP"));
				commonCfg.put("debugPort", robotNode.attributeValue("debugPort"));
				commonCfg.put("cmdTimeout", robotNode.attributeValue("cmdTimeout"));
				cncEthernetCmd.setData(mainKey,"Common",commonCfg);
				
				Iterator<Element> iterRobot = robotNode.elementIterator();
				while(iterRobot.hasNext()){
					Element oprNode = iterRobot.next();
					String operationName = oprNode.attributeValue("name");
					String operator = "Myself";
					if(null != oprNode.attributeValue("operator")) operator = oprNode.attributeValue("operator");
					
					LinkedHashMap<String,Object> cmds = new LinkedHashMap<String,Object>();
					ArrayList<String> oprInfo = new ArrayList<String>();
					oprInfo.add(operator);
					cmds.put(operationName, oprInfo);
					
					Iterator<Element> iterCmd = oprNode.elementIterator();
					while(iterCmd.hasNext()){
						Element cmdNode = iterCmd.next();
						String cmdName = cmdNode.attributeValue("name");
						String cmdID = cmdNode.attributeValue("id");
						
						ArrayList<String> para = new ArrayList<String>();
						para.add(cmdNode.elementText("InParas"));
						para.add(cmdNode.elementText("OutParas"));
						para.add(cmdName);
						cmds.put(cmdName+"#"+cmdID, para);
					}
					cncEthernetCmd.setData(mainKey, operationName, cmds);
				}
			}
			is.close();
		} catch (Exception e) {
			success = false;
			LogUtils.errorLog("XmlUtils-parseCncEthernetCmdXml ERR:"+e.getMessage());
		}
		
		if(xmlFilePath.startsWith("com/")) saveCncEthernetCmdXml();
		return success;
	}
	
	@SuppressWarnings("unchecked")
	public static boolean saveCncEthernetCmdXml(){
		boolean success = true;
		
		String xmlFilePath = getXmlFilePath(true, "CncEthernetCmd.xml");
		Map<String,LinkedHashMap<String,Object>> dataMap = CncEthernetCmd.getInstance().getDataMap();
		if(null != dataMap && dataMap.size() > 0){
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("CncEthernetCmd");
			
			String[] cmdInfo = null;
			for(String mainKey:dataMap.keySet()){
				LinkedHashMap<String,Object> oprMap = dataMap.get(mainKey);
				if(!(null != oprMap && oprMap.size() > 0)) continue;
				
				LinkedHashMap<String,Object> cmdMap = (LinkedHashMap<String, Object>) oprMap.get("Common");
				if(!(null != cmdMap && cmdMap.size() > 0)) continue;
				
				Element robot = root.addElement("CNC");
				robot.addAttribute("key", mainKey);
				for(String attr:cmdMap.keySet()){
					robot.addAttribute(attr, (String)cmdMap.get(attr));
				}
				
				for(String oprName:oprMap.keySet()){
					if("Common".equals(oprName)) continue;
					cmdMap = (LinkedHashMap<String, Object>) oprMap.get(oprName);
					if(!(null != cmdMap && cmdMap.size() > 0)) continue;
					
					Element oprNode = robot.addElement("Operation")
							.addAttribute("name", oprName);
					
					for(String cmdName:cmdMap.keySet()){
						ArrayList<String> cmdPara = (ArrayList<String>) cmdMap.get(cmdName);
						if(oprName.equals(cmdName)){
							oprNode.addAttribute("operator", cmdPara.get(0));
							continue;
						}
						cmdInfo = cmdName.split("#");
						Element cmdNode = oprNode.addElement("Command")
								.addAttribute("name", cmdInfo[0])
								.addAttribute("id", cmdInfo[1]);
						cmdNode.addElement("InParas").setText(cmdPara.get(0));
						cmdNode.addElement("OutParas").setText(cmdPara.get(1));
					}
				}
			}
			
			try {
				FileWriter fileWriter = new FileWriter(xmlFilePath);
				XMLWriter writer = new XMLWriter(fileWriter, OutputFormat.createPrettyPrint());
				writer.write(doc);
				writer.close();
			} catch (Exception e) {
				success = false;
				LogUtils.errorLog("XmlUtils-saveCncEthernetCmdXml ERR:"+e.getMessage());
			}
		}
		
		return success;
	}
}
