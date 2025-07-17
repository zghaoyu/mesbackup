package com.cncmes.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.cncmes.base.RunningData;

/**
 * Robot Ethernet Command Configuration
 * @author LI ZI LONG
 *
 */
public class RobotEthernetCmd extends RunningData<String> {
	private static final int MAXPARACOUNT = 10;
	private static RobotEthernetCmd robotNetworkCmd = new RobotEthernetCmd();
	private RobotEthernetCmd(){}
	public static RobotEthernetCmd getInstance(){
		return robotNetworkCmd;
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
						ops += "," + key;
					}
				}
			}
		}
		
		if("".equals(ops)){
			operations = new String[]{""};
		}else{
			operations = ops.split(",");
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
						cmd += "," + key;
					}
				}
			}
		}
		
		if(!"".equals(cmd)){
			cmds = cmd.split(",");
		}else{
			cmds = new String[]{""};
		}
		
		return cmds;
	}
	
	@SuppressWarnings("unchecked")
	public String[] getSubProgram(String mainKey, String subProgName){
		String[] cmds = null;
		String cmd = "", operation = "subProgram";
		boolean bSubCmdStart = false;
		
		LinkedHashMap<String, Object> oprMap = getData(mainKey);
		if(null != oprMap && oprMap.size() > 0){
			LinkedHashMap<String, Object> cmdMap = (LinkedHashMap<String, Object>) oprMap.get(operation);
			if(null != cmdMap && cmdMap.size() > 0){
				for(String key:cmdMap.keySet()){
					if(operation.equals(key)) continue;
					if(key.startsWith(subProgName)){
						bSubCmdStart = true;
						continue;
					}
					if(key.startsWith("SUB_") && bSubCmdStart) break;
					if(bSubCmdStart){
						if("".equals(cmd)){
							cmd = key;
						}else{
							cmd += "," + key;
						}
					}
				}
			}
		}
		
		if(!"".equals(cmd)){
			cmds = cmd.split(",");
		}else{
			cmds = new String[]{""};
		}
		
		return cmds;
	}
	
	@SuppressWarnings("unchecked")
	public String[] getRetrySequence(String mainKey, String retrySequenceName){
		String[] cmds = null;
		String cmd = "", operation = "retrySequence";
		boolean bSubCmdStart = false;
		
		LinkedHashMap<String, Object> oprMap = getData(mainKey);
		if(null != oprMap && oprMap.size() > 0){
			LinkedHashMap<String, Object> cmdMap = (LinkedHashMap<String, Object>) oprMap.get(operation);
			if(null != cmdMap && cmdMap.size() > 0){
				for(String key:cmdMap.keySet()){
					if(operation.equals(key)) continue;
					if(key.startsWith(retrySequenceName)){
						String paraVal = getCommandCndSetting(mainKey,operation,key,"disabled");
						if("1".equals(paraVal)) break;
						bSubCmdStart = true;
						continue;
					}
					if(key.contains("_RETRY_") && bSubCmdStart) break;
					if(bSubCmdStart){
						if("".equals(cmd)){
							cmd = key;
						}else{
							cmd += "," + key;
						}
					}
				}
			}
		}
		
		if(!"".equals(cmd)){
			cmds = cmd.split(",");
		}else{
			cmds = new String[]{""};
		}
		
		return cmds;
	}
	
	@SuppressWarnings("unchecked")
	public String getRetrySequenceParaVal(String mainKey, String retrySequenceName, String paraName){
		String paraVal = "", operation = "retrySequence";
		
		LinkedHashMap<String, Object> oprMap = getData(mainKey);
		if(null != oprMap && oprMap.size() > 0){
			LinkedHashMap<String, Object> cmdMap = (LinkedHashMap<String, Object>) oprMap.get(operation);
			if(null != cmdMap && cmdMap.size() > 0){
				for(String key:cmdMap.keySet()){
					if(operation.equals(key)) continue;
					if(key.startsWith(retrySequenceName)){
						paraVal = getCommandCndSetting(mainKey,operation,key,paraName);
						break;
					}
				}
			}
		}
		
		return paraVal;
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
	
	public String[] getCommandParaDataTitle(){
		return (new String[]{"Index","ParaName","ParaVal"});
	}
	
	@SuppressWarnings("unchecked")
	public Object[][] getCommandParaDataTable(String mainKey, String operation,String cmdName){
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
			String[] paras = para.get(0).split(";");
			String[] parasVal = para.get(1).split(";");
			if(paras.length < MAXPARACOUNT){
				data = new Object[MAXPARACOUNT][3];
			}else{
				data = new Object[paras.length][3];
			}
			for(int i=0; i<paras.length; i++){
				data[i][0] = i+1;
				data[i][1] = paras[i];
				data[i][2] = parasVal[i];
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
		
		LinkedHashMap<String, Object> oprMap = getData(mainKey);
		if(null != oprMap && oprMap.size() > 0){
			LinkedHashMap<String, Object> config = (LinkedHashMap<String, Object>) oprMap.get("Common");
			if(null != config && config.size() > 0){
				for(String key:config.keySet()){
					if("".equals(parasVal)){
						parasVal = (String) config.get(key);
					}else{
						parasVal += "," + (String) config.get(key);
					}
					
					if("socketResponseTimeout".equals(key)) key += "(s)";
					if("socketResponseInterval".equals(key)) key += "(ms)";
					if("".equals(parasName)){
						parasName = key;
					}else{
						parasName += "," + key;
					}
				}
			}
		}
		if(!"".equals(parasVal)){
			String[] name = parasName.split(",");
			String[] val = parasVal.split(",");
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
					String[] paras = cmdParas.get(0).split(";");
					String[] vals = cmdParas.get(1).split(";");
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
								rtnStr += ";" + val;
							}
						}
						vals = rtnStr.split(";");
					}else{
						rtnStr = cmdParas.get(1);
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
		
		LinkedHashMap<String, Object> commonCfg = (LinkedHashMap<String, Object>) oprMap.get("Common");
		String seperator = (String) commonCfg.get("seperator");
		if(!"".equals(rtnStr)){
			rtnStr = rtnStr.replace(";", seperator);
			rtnStr = cmdName.split("#")[0] + seperator + rtnStr + seperator;
		}else{
			rtnStr = cmdName.split("#")[0] + seperator;
		}
		rtnStr += calculateCmdConfirmCode(rtnStr);
		
		return rtnStr;
	}
	
	@SuppressWarnings("unchecked")
	public String getCommandParas(String mainKey, String operation, String cmdName){
		String rtnStr = "", tmp1 = "", tmp2 = "";
		
		LinkedHashMap<String, Object> oprMap = getData(mainKey);
		if(null != oprMap && oprMap.size() > 0){
			LinkedHashMap<String, Object> cmdMap = (LinkedHashMap<String, Object>) oprMap.get(operation);
			if(null != cmdMap && cmdMap.size() > 0){
				ArrayList<String> cmdParas = (ArrayList<String>) cmdMap.get(cmdName);
				if(null != cmdParas && cmdParas.size() > 1){
					String[] paras = cmdParas.get(0).split(";");
					String[] vals = cmdParas.get(1).split(";");
					
					for(int i=0; i<paras.length; i++){
						if(isCmdInputParas(paras[i])){
							if("".equals(tmp1)){
								tmp1 = paras[i];
								tmp2 = vals[i];
							}else{
								tmp1 += ";" + paras[i];
								tmp2 += ";" + vals[i];
							}
						}
					}
				}
			}
		}
		if(!"".equals(tmp1)) rtnStr = tmp1 + "##" + tmp2;
		
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
	
	public String getCommandEndLoopCnd(String mainKey, String operation, String cmdName){
		return getCommandCndSetting(mainKey,operation,cmdName,"END_LOOP");
	}
	
	public String getSleepRoutine(String mainKey, String operation, String cmdName){
		return getCommandCndSetting(mainKey,operation,cmdName,"SLEEP_ROUTINE");
	}
	
	@SuppressWarnings("unchecked")
	public String getCommandCndSetting(String mainKey, String operation, String cmdName, String cndName){
		String cndSetting = "";
		
		LinkedHashMap<String, Object> oprMap = getData(mainKey);
		if(null != oprMap && oprMap.size() > 0){
			LinkedHashMap<String, Object> cmdMap = (LinkedHashMap<String, Object>) oprMap.get(operation);
			if(null != cmdMap && cmdMap.size() > 0){
				ArrayList<String> cmdParas = (ArrayList<String>) cmdMap.get(cmdName);
				if(null != cmdParas && cmdParas.size() > 1){
					String[] paras = cmdParas.get(0).split(";");
					String[] vals = cmdParas.get(1).split(";");
					
					for(int i=0; i<paras.length; i++){
						if(cndName.equals(paras[i])){
							cndSetting = vals[i];
							break;
						}
					}
				}
			}
		}
		
		return cndSetting;
	}
	
	public boolean isCmdInputParas(String paraName){
		boolean isCmdParas = true;
		
		String[] notParas = new String[]{"DELAY_BEF","DELAY_AFT","CMD_TIMEOUT","END_LOOP","IF_CND","SLEEP_ROUTINE"};
		for(int i=0; i<notParas.length; i++){
			if(notParas[i].equals(paraName)){
				isCmdParas = false;
				break;
			}
		}
		
		return isCmdParas;
	}
	
	public String calculateCmdConfirmCode(String cmdStr){
		byte[] ascii = cmdStr.getBytes();
		int xor = ascii[0];
		
		for(int i=1; i<ascii.length; i++){
			xor ^= ascii[i];
		}
		
		return Integer.toHexString(xor);
	}
	
}
