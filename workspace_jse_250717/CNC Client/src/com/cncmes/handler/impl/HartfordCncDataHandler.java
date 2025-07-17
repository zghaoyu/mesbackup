package com.cncmes.handler.impl;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.cncmes.base.CNC;
import com.cncmes.base.CncWebAPIItems;
import com.cncmes.base.DeviceState;
import com.cncmes.data.CncData;
import com.cncmes.data.CncWebAPI;
import com.cncmes.drv.CncDrvWeb;
import com.cncmes.handler.CncDataHandler;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.MathUtils;
import com.cncmes.utils.MyFileUtils;

import net.sf.json.JSONObject;

/*
 * 	copy MoriCncDataHandler
 */

public class HartfordCncDataHandler implements CncDataHandler {
	private static CncWebAPI cncWebAPI = CncWebAPI.getInstance();
	private static HartfordCncDataHandler dataHandler = new HartfordCncDataHandler();
	private HartfordCncDataHandler(){}
	
	public static HartfordCncDataHandler getInstance(){
		return dataHandler;
	}
	
	private String replaceEx(String oriStr,String[] target,String[] replacement){
		String rslt = oriStr;
		
		if(target.length > 0 && target.length == replacement.length){
			for(int i=0; i<target.length; i++){
				rslt = rslt.replace(target[i], replacement[i]);
			}
		}
		
		return rslt;
	}
	
	@Override
	public LinkedHashMap<String, String> alarmInfoHandler(String alarmInfo) {
		String[] target = new String[]{"\\\"","},{","[","]"};
		String[] replacement = new String[]{"\"","};{","",""};
		alarmInfo = replaceEx(alarmInfo,target,replacement);
		
		String[] data = alarmInfo.split(";");
		LinkedHashMap<String, String> info = new LinkedHashMap<String, String>();
		if(data.length > 0){
			for(int i=0; i<data.length; i++){
				JSONObject dt = JSONObject.fromObject(data[i]);
				info.put(dt.getString("num"), dt.getString("message"));
			}
		}
		return info;
	}
	
	@Override
	public DeviceState machineStateHandler(String machineState) {
		String[] target = new String[]{"\\\""};
		String[] replacement = new String[]{"\""};
		machineState = replaceEx(machineState,target,replacement);
		LinkedHashMap<String, String> state = new LinkedHashMap<String, String>();
		JSONObject jstate = JSONObject.fromObject(machineState);
		@SuppressWarnings("unchecked")
		Iterator<String> iterState = jstate.keys();
		while(iterState.hasNext()){
			String key = iterState.next();
			state.put(key, jstate.getString(key));
		}
		
		DeviceState devState = DeviceState.SHUTDOWN;
		if(state.size() > 0){
			//run: 0 RESET, 1 STOP, 2 HOLD, 3 START
			String run = state.get("run");
			//emergency: 0 Not-emergency, 1 Emergency, 2 Reset
			String emergency = state.get("emergency");
			
			if("0".equals(emergency)){
				if("0".equals(run)) devState = DeviceState.STANDBY;
				if("1".equals(run)) devState = DeviceState.FINISH;
				if("2".equals(run)) devState = DeviceState.ALARMING;
				if("3".equals(run)) devState = DeviceState.WORKING;
			}else if("1".equals(emergency)){
				devState = DeviceState.ALARMING;
			}
		}
		return devState;
	}
	
	@Override
	public LinkedHashMap<String, String> machineParasHandler(String machineParas) {
		String[] target = new String[]{"\\\""};
		String[] replacement = new String[]{"\""};
		machineParas = replaceEx(machineParas,target,replacement);
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		JSONObject jparas = JSONObject.fromObject(machineParas);
		@SuppressWarnings("unchecked")
		Iterator<String> iterParas = jparas.keys();
		while(iterParas.hasNext()){
			String key = iterParas.next();
			paras.put(key, jparas.getString(key));
		}
		return paras;
	}

	@Override
	public LinkedHashMap<String, LinkedHashMap<String, String>> machineToolLifeHandler(String machineToolLife) {
		String[] target = new String[]{"\\\"","},{","[","]"};
		String[] replacement = new String[]{"\"","};{","",""};
		machineToolLife = replaceEx(machineToolLife,target,replacement);
		
		String[] tLife = machineToolLife.split(";");
		LinkedHashMap<String, LinkedHashMap<String, String>> toolLife = new LinkedHashMap<String, LinkedHashMap<String, String>>();
		if(tLife.length > 0){
			for(int i=0; i<tLife.length; i++){
				JSONObject dt = JSONObject.fromObject(tLife[i]);
				String mainKey = dt.getString("num");
				@SuppressWarnings("unchecked")
				Iterator<String> iterTool = dt.keys();
				LinkedHashMap<String, String> tool = new LinkedHashMap<String, String>();
				while(iterTool.hasNext()){
					String key = iterTool.next();
					tool.put(key, dt.getString(key));
				}
				toolLife.put(mainKey, tool);
			}
		}
		return toolLife;
	}

	@Override
	public LinkedHashMap<String, LinkedHashMap<String, String>> machineCounterHandler(String machineCounter) {
		String[] target = new String[]{"\\\"","},{","[","]"};
		String[] replacement = new String[]{"\"","};{","",""};
		machineCounter = replaceEx(machineCounter,target,replacement);
		
		String[] arrCounter = machineCounter.split(";");
		LinkedHashMap<String, LinkedHashMap<String, String>> counter = new LinkedHashMap<String, LinkedHashMap<String, String>>();
		if(arrCounter.length > 0){
			for(int i=0; i<arrCounter.length; i++){
				JSONObject dt = JSONObject.fromObject(arrCounter[i]);
				if(null == dt) continue;
				String mainKey = "C"+i;
				
				@SuppressWarnings("unchecked")
				Iterator<String> iterCounter = dt.keys();
				LinkedHashMap<String, String> cnter = new LinkedHashMap<String, String>();
				while(iterCounter.hasNext()){
					String key = iterCounter.next();
					cnter.put(key, dt.getString(key));
				}
				counter.put(mainKey, cnter);
			}
		}
		return counter;
	}

	@Override
	public String machineNCProgramHandler(String ncProgramContent, String saveFileName) {
		ncProgramContent = ncProgramContent.replace("#r#n", "\r\n");
		if(ncProgramContent.startsWith("%")) ncProgramContent = ncProgramContent.substring(1);
		if(ncProgramContent.endsWith("%")) ncProgramContent = ncProgramContent.substring(0, ncProgramContent.length()-1);
		ncProgramContent = ncProgramContent.trim();
		String strMD5 = "", strMD5Content = "";
		String[] lines = null;
		if(ncProgramContent.indexOf("\r\n") >= 0){
			lines = ncProgramContent.split("\r\n");
			strMD5 = ncProgramContent.replace("\r\n", "");
		}else if(ncProgramContent.indexOf("\n") >= 0){
			lines = ncProgramContent.split("\n");
			strMD5 = ncProgramContent.replace("\n", "");
		}else{
			lines = ncProgramContent.split("\r");
			strMD5 = ncProgramContent.replace("\r", "");
		}
		if(lines.length<100){
			strMD5 = "";
			for(int i=0; i<lines.length; i++){
				strMD5 += lines[i].trim();
			}
		}
		if(!"".equals(strMD5)){
			//remove filename from NCProgram content.   Hui Zhi Fang	2021.11.2
			String fileName = saveFileName.substring(0,saveFileName.indexOf("."));
			strMD5 = strMD5.replace(fileName, ""); 
			//end
			strMD5Content = strMD5;
			strMD5 = MathUtils.MD5Encode(strMD5);
		}
		
		if(null != saveFileName && !"".equals(saveFileName)){
			String localFolder = System.getProperty("user.dir") + File.separator + "apiDown";
			String localPath = localFolder + File.separator + "Hartford_"+saveFileName;
			MyFileUtils.makeFolder(localFolder);
			MyFileUtils.saveToFile(ncProgramContent, localPath);
			MyFileUtils.saveToFile(strMD5+":"+strMD5Content, localPath+"MD5.txt");
		}
		
		return strMD5;
	}

	@Override
	public boolean machineUploadSubProgramHandler(String ip, String programID) {
		CncDrvWeb cncDrvWeb = CncDrvWeb.getInstance();
		boolean ok = cncDrvWeb.deleteSubProgram(ip, programID);
		return ok;
	}

	@Override
	public boolean machineUploadMainProgramHandler(String ip, String programID) {
//		return false; //Needs to upload the main program
		return true; //add by Hui Zhi Fang	2021.11.2
	}

	@Override
	public String machineGenerateMainProgramHandler(String ip, String wpIDs, String subPrograms, String workzones) {
		String content = "";
		String[] zones = workzones.split(";");
		
		CncData cncData = CncData.getInstance();
		String cncModel = cncData.getCncModel(ip);
		CNC cncCtrl = cncData.getCncController(ip);
		
		//Get coordinate switching commands
		LinkedHashMap<CncWebAPIItems, String> cncWebAPICommonCfg = cncWebAPI.getCommonCfg(cncModel);
		String[] coordinateCmds = new String[7];
		String[] programIDs = new String[7];
		coordinateCmds[1] = cncWebAPICommonCfg.get(CncWebAPIItems.COORDINATE1);
		coordinateCmds[2] = cncWebAPICommonCfg.get(CncWebAPIItems.COORDINATE2);
		coordinateCmds[3] = cncWebAPICommonCfg.get(CncWebAPIItems.COORDINATE3);
		coordinateCmds[4] = cncWebAPICommonCfg.get(CncWebAPIItems.COORDINATE4);
		coordinateCmds[5] = cncWebAPICommonCfg.get(CncWebAPIItems.COORDINATE5);
		coordinateCmds[6] = cncWebAPICommonCfg.get(CncWebAPIItems.COORDINATE6);
		programIDs[0] = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGMAIN);
		programIDs[1] = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGSUB1);
		programIDs[2] = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGSUB2);
		programIDs[3] = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGSUB3);
		programIDs[4] = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGSUB4);
		programIDs[5] = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGSUB5);
		programIDs[6] = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGSUB6);
		
		int zoneQty = cncData.getWorkzoneQTY(ip);
		for(int i=1; i<=zoneQty; i++){
			if(i < 7){
				if("".equals(content)){
					content = coordinateCmds[i] + "#r#n" + "M198P" + programIDs[i].replace("O", "");
				}else{
					content += "#r#n" + coordinateCmds[i] + "#r#n" + "M198P" + programIDs[i].replace("O", "");
				}
			}else{
				content = "";
				break;
			}
			
			if(zoneIsEmpty(i, zones)){
				if(!cncCtrl.uploadSubProgram(ip, programIDs[i], "#r#nM99#r#n")){
					content = "";
					break;
				}
			}
		}
		if(!"".equals(content)){
			content = "O"+programIDs[0] + content + "#r#nG91G28Y0#r#nG90G53G01F10000.X-547.739#r#nM30#r#n";
		}
		
		return content;
	}
	
	private boolean zoneIsEmpty(int zone, String[] zones){
		boolean empty = true;
		
		int zoneNo = 0;
		for(int i=0; i<zones.length; i++){
			zoneNo = Integer.valueOf(zones[i]);
			if(zone == zoneNo){
				empty = false;
				break;
			}
		}
		
		return empty;
	}

	@Override
	public String machineMainProgramNameHandler(String machiningPrograms) {
		String progName = "";
		String[] target = new String[]{"\\\""};
		String[] replacement = new String[]{"\""};
		machiningPrograms = replaceEx(machiningPrograms,target,replacement);
		try {
			JSONObject jsonObj = JSONObject.fromObject(machiningPrograms);
			progName = jsonObj.getString("main");
		} catch (Exception e) {
			LogUtils.errorLog("HartfordCncDataHandler machineMainProgramNameHandler Err:"+e.getMessage()+LogUtils.separator+machiningPrograms);
		}
		
		return progName;
	}

	@Override
	public String machineCurrSubProgramNameHandler(String machiningPrograms) {
		String progName = "";
		String[] target = new String[]{"\\\""};
		String[] replacement = new String[]{"\""};
		machiningPrograms = replaceEx(machiningPrograms,target,replacement);
		try {
			JSONObject jsonObj = JSONObject.fromObject(machiningPrograms);
			progName = jsonObj.getString("current");
		} catch (Exception e) {
			LogUtils.errorLog("HartfordCncDataHandler machineCurrSubProgramNameHandler Err:"+e.getMessage()+LogUtils.separator+machiningPrograms);
		}
		
		return progName;
	}

	@Override
	public boolean machineAxisPositionHandler(String machinePosition, int address, double value) {
		String[] target = new String[]{"\\\"","},{","[","]"};
		String[] replacement = new String[]{"\"","};{","",""};
		boolean check = false;
		
		machinePosition = replaceEx(machinePosition,target,replacement);
		String[] data = machinePosition.split(";");
		
		if(data.length > 0){
			for(int i=0; i<data.length; i++){
				JSONObject dt = JSONObject.fromObject(data[i]);
				int val = Integer.parseInt( dt.getString("value"));
				int addr = Integer.parseInt(dt.getString("name"));
				int dec = Integer.parseInt(dt.getString("dec"));
				if ( addr == address && value == (double)(val/Math.pow(10, dec)) )  {
					check = true;
					break;
				} 
			}
		}
		
		return check;
	}



	public double machineGetMacroHandler(String json) {
		String[] target = new String[]{"\\\"","},{","[","]"};
		String[] replacement = new String[]{"\"","};{","",""};
		boolean check = false;

		json = replaceEx(json,target,replacement);
		String[] data = json.split(";");
		double res = -1;
		if(data.length > 0){

				JSONObject dt = JSONObject.fromObject(data[0]);
				int val = Integer.parseInt( dt.getString("value"));
				int addr = Integer.parseInt(dt.getString("name"));
				int dec = Integer.parseInt(dt.getString("dec"));
				res = (double)val/Math.pow(10, dec);

		}
		return res;
	}

	@Override
	public boolean machineRotateHandler(String machinePosition, double value) {
		
		return false;
	}


}
