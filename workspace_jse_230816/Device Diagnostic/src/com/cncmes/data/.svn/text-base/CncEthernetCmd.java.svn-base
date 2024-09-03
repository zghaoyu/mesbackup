package com.cncmes.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.cncmes.base.RunningData;
import com.cncmes.utils.XmlUtils;

import net.sf.json.JSONObject;

/**
 * CNC Ethernet Command Configuration
 * @author LI ZI LONG
 *
 */
public class CncEthernetCmd extends RunningData<String> {
	private static final int MAXPARACOUNT = 10;
	private static CncEthernetCmd cncEthernetCmd = new CncEthernetCmd();
	private final String splitor = "##";
	
	private CncEthernetCmd(){}
	static{
		XmlUtils.parseCncEthernetCmdXml();
	}
	public static CncEthernetCmd getInstance(){
		return cncEthernetCmd;
	}
	
	public String[] getAllOperations(String mainKey){
		String[] operations = null;
		String ops = "";
		
		LinkedHashMap<String,Object> oprMap = getData(mainKey);
		if(null != oprMap && oprMap.size() > 0){
			for(String key:oprMap.keySet()){
				if(!"Common".equals(key)){
					if("".equals(ops)){
						ops = key;
					}else{
						ops += splitor + key;
					}
				}
			}
		}
		
		if("".equals(ops)){
			operations = new String[]{""};
		}else{
			operations = ops.split(splitor);
		}
		
		return operations;
	}
	
	@SuppressWarnings("unchecked")
	public String[] getAllCommands(String mainKey, String operation){
		String[] cmds = null;
		String cmd = "";
		
		LinkedHashMap<String, Object> oprMap = getData(mainKey);
		if(null != oprMap && oprMap.size() > 0){
			LinkedHashMap<String, Object> cmdMap = (LinkedHashMap<String, Object>) oprMap.get(operation);
			if(null != cmdMap && cmdMap.size() > 0){
				for(String key:cmdMap.keySet()){
					if(operation.equals(key)) continue;
					if("".equals(cmd)){
						cmd = key;
					}else{
						cmd += splitor + key;
					}
				}
			}
		}
		
		if(!"".equals(cmd)){
			cmds = cmd.split(splitor);
		}else{
			cmds = new String[]{""};
		}
		
		return cmds;
	}
	
	@SuppressWarnings("unchecked")
	public String getOperationExecutive(String mainKey, String operation){
		String executive = "Myself";
		
		LinkedHashMap<String, Object> oprMap = getData(mainKey);
		if(null != oprMap){
			LinkedHashMap<String, Object> cmdMap = (LinkedHashMap<String, Object>) oprMap.get(operation);
			if(null != cmdMap){
				if(null!=cmdMap.get(operation)){
					executive = ((ArrayList<String>)cmdMap.get(operation)).get(0);
				}
			}
		}
		
		return executive;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<String[]> decodePLCCommands(String mainKey, String operation, String cmdName, String[] reInitParas, String[] reInitVals){
		ArrayList<String[]> cmds = new ArrayList<String[]>();
		
		LinkedHashMap<String, Object> oprMap = getData(mainKey);
		if(null != oprMap && oprMap.size() > 0){
			LinkedHashMap<String, Object> cmdMap = (LinkedHashMap<String, Object>) oprMap.get(operation);
			if(null != cmdMap && cmdMap.size() > 0){
				ArrayList<String> cmdParas = (ArrayList<String>) cmdMap.get(cmdName);
				if(null != cmdParas && cmdParas.size() > 1){
					JSONObject jsonObj = null;
					try {
						jsonObj = JSONObject.fromObject(cmdParas.get(0));
					} catch (Exception e) {
						jsonObj = null;
					}
					if(null!=jsonObj && jsonObj.size()>0){
						String paraStr = "", para = "";
						if(cmdName.indexOf("Fixture")>0){//clampFixture or releaseFixture
							//Get Work Zone Number
							int zone = 0;
							if(null != reInitParas && reInitParas.length > 0){
								for(int j=0; j<reInitParas.length; j++){
									if("workZone".equals(reInitParas[j])){
										zone = Integer.parseInt(reInitVals[j]);
										break;
									}
								}
							}
							if(zone > 0) para = "zone_" + zone;
						}else if(cmdName.indexOf("Door")>0){//openDoor or closeDoor
							para = "regs";
						}
						
						if(!"".equals(para)){
							paraStr = jsonObj.getString(para);
							if(null!=paraStr && paraStr.indexOf(";")>0){
								String[] cmdInfo = paraStr.split(";");
								String[] cmd = cmdInfo[0].split("/");
								String[] rtn = cmdInfo[1].split("/");
								if(cmd.length == rtn.length){
									cmds.add(0, cmd);
									cmds.add(1, rtn);
								}
							}
						}
					}
				}
			}
		}
		
		return cmds;
	}
	
	public String[] getCommandParaDataTitle(){
		return (new String[]{"Index","ParaName","ParaVal"});
	}
	
	@SuppressWarnings("unchecked")
	public Object[][] getCommandParaDataTable(String mainKey, String operation, String cmdName, boolean bGetInParas){
		Object[][] data = null;
		ArrayList<String> para = null;
		
		LinkedHashMap<String, Object> oprMap = getData(mainKey);
		if(null != oprMap && oprMap.size() > 0){
			LinkedHashMap<String, Object> cmdMap = (LinkedHashMap<String, Object>) oprMap.get(operation);
			if(null != cmdMap && cmdMap.size() > 0){
				for(String key:cmdMap.keySet()){
					if(cmdName.equals(key)){
						para = (ArrayList<String>) cmdMap.get(cmdName);
						break;
					}
				}
			}
		}
		
		if(null != para){
			JSONObject jsonObj = null;
			try {
				if(bGetInParas){
					jsonObj = JSONObject.fromObject(para.get(0));
				}else{
					jsonObj = JSONObject.fromObject(para.get(1));
				}
			} catch (Exception e) {
				jsonObj = null;
			}
			if(null != jsonObj && jsonObj.size() > 0){
				if(jsonObj.size() < MAXPARACOUNT){
					data = new Object[MAXPARACOUNT][3];
				}else{
					data = new Object[jsonObj.size()][3];
				}
				
				Iterator<String> iter = jsonObj.keys();
				int i = -1;
				while(iter.hasNext()){
					i++;
					data[i][0] = i+1;
					data[i][1] = iter.next();
					data[i][2] = jsonObj.getString(""+data[i][1]);
				}
			}else{
				data = new Object[MAXPARACOUNT][3];
				for(int i=0; i<MAXPARACOUNT; i++){
					data[i][0] = i+1;
					data[i][1] = "";
					data[i][2] = "";
				}
			}
		}
		
		return data;
	}
	
	@SuppressWarnings("unchecked")
	public LinkedHashMap<String, Object> getCommonConfig(String mainKey){
		LinkedHashMap<String, Object> config = null;
		
		LinkedHashMap<String, Object> oprMap = getData(mainKey);
		if(null != oprMap && oprMap.size() > 0){
			config = (LinkedHashMap<String, Object>) oprMap.get("Common");
		}
		
		return config;
	}
	
	public String[] getCommonConfigDataTitle(){
		String[] title = new String[]{"ParaName", "ParaVal"};
		
		return title;
	}
	
	@SuppressWarnings("unchecked")
	public Object[][] getCommonConfigDataTable(String mainKey){
		Object[][] data = null;
		String parasName = "", parasVal = "";
		if(null == mainKey) return null;
		
		LinkedHashMap<String, Object> oprMap = getData(mainKey);
		if(null != oprMap && oprMap.size() > 0){
			LinkedHashMap<String, Object> config = (LinkedHashMap<String, Object>) oprMap.get("Common");
			if(null != config && config.size() > 0){
				for(String key:config.keySet()){
					if("".equals(parasVal)){
						parasVal = (String) config.get(key);
					}else{
						parasVal += splitor + (String) config.get(key);
					}
					
					if("socketResponseTimeout".equals(key)) key += "(s)";
					if("socketResponseInterval".equals(key)) key += "(ms)";
					if("".equals(parasName)){
						parasName = key;
					}else{
						parasName += splitor + key;
					}
				}
			}
		}
		if(!"".equals(parasVal)){
			String[] name = parasName.split(splitor);
			String[] val = parasVal.split(splitor);
			data = new Object[name.length][2];
			for(int i=0; i<name.length; i++){
				data[i][0] = name[i];
				data[i][1] = val[i];
			}
		}
		
		return data;
	}
	
	@SuppressWarnings("unchecked")
	public String getCommandStr(String mainKey, String operation, String cmdName, String[] reInitParas, String[] reInitVals){
		String rtnStr = "";
		
		LinkedHashMap<String, Object> oprMap = getData(mainKey);
		if(null != oprMap && oprMap.size() > 0){
			LinkedHashMap<String, Object> cmdMap = (LinkedHashMap<String, Object>) oprMap.get(operation);
			if(null != cmdMap && cmdMap.size() > 0){
				ArrayList<String> cmdParas = (ArrayList<String>) cmdMap.get(cmdName);
				if(null != cmdParas && cmdParas.size() > 1){
					JSONObject jsonObj = null;
					try {
						jsonObj = JSONObject.fromObject(cmdParas.get(0));
					} catch (Exception e) {
						jsonObj = null;
					}
					if(null!=jsonObj && jsonObj.size()>0){
						Iterator<String> iter = jsonObj.keys();
						String paraStr = "", valStr = "", para = "";
						while(iter.hasNext()){
							para = iter.next();
							if("".equals(paraStr)){
								paraStr = para;
								valStr = jsonObj.getString(para);
							}else{
								paraStr += splitor + para;
								valStr += splitor + jsonObj.getString(para);
							}
						}
						
						String[] paras = paraStr.split(splitor);
						String[] vals = valStr.split(splitor);
						if(null != reInitParas && reInitParas.length > 0){
							for(int i=0; i<paras.length; i++){
								for(int j=0; j<reInitParas.length; j++){
									if(reInitParas[j].equals(paras[i])){
										vals[i] = reInitVals[j];
										break;
									}
								}
							}
							for(String val:vals){
								if("".equals(rtnStr)){
									rtnStr = val;
								}else{
									rtnStr += splitor + val;
								}
							}
							vals = rtnStr.split(splitor);
						}else{
							rtnStr = valStr;
						}
						
						rtnStr = "";
						for(int i=0; i<paras.length; i++){
							if(isCmdInputParas(paras[i])){
								if("".equals(rtnStr)){
									rtnStr = vals[i];
								}else{
									rtnStr += ";" + vals[i];
								}
							}
						}
					}
				}
			}
		}
		
		LinkedHashMap<String, Object> commonCfg = (LinkedHashMap<String, Object>) oprMap.get("Common");
		String seperator = (String) commonCfg.get("seperator");
		if(!"".equals(rtnStr)){
			rtnStr = rtnStr.replace(splitor, seperator);
			rtnStr = cmdName.split("#")[0] + seperator + rtnStr + seperator;
		}else{
			rtnStr = cmdName.split("#")[0] + seperator;
		}
		rtnStr += calculateCmdConfirmCode(rtnStr);
		
		return rtnStr;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Integer> getCommandDelay(String mainKey, String operation, String cmdName){
		ArrayList<Integer> delay = new ArrayList<Integer>();
		delay.add(0, 0);
		delay.add(1, 0);
		
		LinkedHashMap<String, Object> oprMap = getData(mainKey);
		if(null != oprMap && oprMap.size() > 0){
			LinkedHashMap<String, Object> cmdMap = (LinkedHashMap<String, Object>) oprMap.get(operation);
			if(null != cmdMap && cmdMap.size() > 0){
				ArrayList<String> cmdParas = (ArrayList<String>) cmdMap.get(cmdName);
				if(null != cmdParas && cmdParas.size() > 1){
					String[] paras = cmdParas.get(0).split(";");
					String[] vals = cmdParas.get(1).split(";");
					
					for(int i=0; i<paras.length; i++){
						if("DELAY_BEF".equals(paras[i])){
							delay.add(0, Integer.parseInt(vals[i]));
						}else if("DELAY_AFT".equals(paras[i])){
							delay.add(1, Integer.parseInt(vals[i]));
						}
					}
				}
			}
		}
		
		return delay;
	}
	
	@SuppressWarnings("unchecked")
	public int getCommandTimeout(String mainKey, String operation, String cmdName, int defaultTimeout){
		LinkedHashMap<String, Object> oprMap = getData(mainKey);
		if(null != oprMap && oprMap.size() > 0){
			LinkedHashMap<String, Object> cmdMap = (LinkedHashMap<String, Object>) oprMap.get(operation);
			if(null != cmdMap && cmdMap.size() > 0){
				ArrayList<String> cmdParas = (ArrayList<String>) cmdMap.get(cmdName);
				if(null != cmdParas && cmdParas.size() > 1){
					String[] paras = cmdParas.get(0).split(";");
					String[] vals = cmdParas.get(1).split(";");
					
					for(int i=0; i<paras.length; i++){
						if("CMD_TIMEOUT".equals(paras[i])){
							if(Integer.parseInt(vals[i])>0){
								defaultTimeout = Integer.parseInt(vals[i]);
							}
							break;
						}
					}
				}
			}
		}
		
		return defaultTimeout;
	}
	
	@SuppressWarnings("unchecked")
	public String getCommandEndLoopCnd(String mainKey, String operation, String cmdName){
		String endLoopCnd = "";
		
		LinkedHashMap<String, Object> oprMap = getData(mainKey);
		if(null != oprMap && oprMap.size() > 0){
			LinkedHashMap<String, Object> cmdMap = (LinkedHashMap<String, Object>) oprMap.get(operation);
			if(null != cmdMap && cmdMap.size() > 0){
				ArrayList<String> cmdParas = (ArrayList<String>) cmdMap.get(cmdName);
				if(null != cmdParas && cmdParas.size() > 1){
					String[] paras = cmdParas.get(0).split(";");
					String[] vals = cmdParas.get(1).split(";");
					
					for(int i=0; i<paras.length; i++){
						if("END_LOOP".equals(paras[i])){
							endLoopCnd = vals[i];
							break;
						}
					}
				}
			}
		}
		
		return endLoopCnd;
	}
	
	private boolean isCmdInputParas(String paraName){
		boolean isCmdParas = true;
		
		String[] notParas = new String[]{"DELAY_BEF","DELAY_AFT","CMD_TIMEOUT","END_LOOP"};
		for(int i=0; i<notParas.length; i++){
			if(notParas[i].equals(paraName)){
				isCmdParas = false;
				break;
			}
		}
		
		return isCmdParas;
	}
	
	private String calculateCmdConfirmCode(String cmdStr){
		byte[] ascii = cmdStr.getBytes();
		int xor = ascii[0];
		
		for(int i=1; i<ascii.length; i++){
			xor ^= ascii[i];
		}
		
		return toBeautyFormat(Integer.toHexString(xor),2);
	}
	
	private String toBeautyFormat(String binaryString,int totalBits){
		String s = "";
		int zeros = totalBits - binaryString.length();
		
		if(zeros > 0){
			for(int i=0; i<zeros; i++){
				s += "0";
			}
		}
		
		return (s+binaryString);
	}
}
