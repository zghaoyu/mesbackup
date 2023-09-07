package com.cncmes.handler.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.cncmes.base.CncWebAPIItems;
import com.cncmes.base.DeviceState;
import com.cncmes.data.CncData;
import com.cncmes.data.CncWebAPI;
import com.cncmes.handler.CncDataHandler;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.MathUtils;
import com.cncmes.utils.MyFileUtils;

import net.sf.json.JSONObject;

public class BroCncDataHandler implements CncDataHandler {
	private static CncWebAPI cncWebAPI = CncWebAPI.getInstance();
	private static BroCncDataHandler dataHandler = new BroCncDataHandler();
	private BroCncDataHandler(){}
	
	public static BroCncDataHandler getInstance(){
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
			String red = state.get("red");
			String yello = state.get("yellow");
			String green = state.get("green");
			
			if("OFF".equals(red) && "OFF".equals(yello) && "OFF".equals(green)){
				devState = DeviceState.STANDBY; //Just started up
			}else if("OFF".equals(red) && "ON".equals(yello) && "OFF".equals(green)){
				devState = DeviceState.STANDBY; //Just finished machining
			}else if("OFF".equals(red) && "OFF".equals(yello) && "ON".equals(green)){
				devState = DeviceState.WORKING; //Machining is ongoing
			}else if("ON".equals(red)){
				devState = DeviceState.ALARMING;
			}
			
			//TODO
			//From green to all off stands for abnormal case
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
				String mainKey = dt.getString("name");
				String count = dt.getString("count");
				dt = JSONObject.fromObject(count);
				
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
		String strMD5 = ncProgramContent.replace("\r\n", "");
		if(!"".equals(strMD5)) strMD5 = MathUtils.MD5Encode(strMD5);
		
		if(null != saveFileName && !"".equals(saveFileName)){
			String localFolder = System.getProperty("user.dir") + File.separator + "apiDown";
			String localPath = localFolder + File.separator + "Bro_"+saveFileName;
			MyFileUtils.makeFolder(localFolder);
			try {
				FileOutputStream fos = new FileOutputStream(localPath);
				fos.write(ncProgramContent.getBytes());
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return strMD5;
	}

	@Override
	public boolean machineUploadSubProgramHandler(String ip, String programID) {
		return true;
	}

	@Override
	public boolean machineUploadMainProgramHandler(String ip, String programID) {
		return false; //Needs to upload the main program
	}

	@Override
	public String machineGenerateMainProgramHandler(String ip, String wpIDs, String subPrograms, String workzones) {
		String content = "";
		String[] programs = subPrograms.split(";");
		String[] zones = workzones.split(";");
		
		CncData cncData = CncData.getInstance();
		String cncModel = cncData.getCncModel(ip);
		
		//Get coordinate switching commands
		LinkedHashMap<CncWebAPIItems, String> cncWebAPICommonCfg = cncWebAPI.getCommonCfg(cncModel);
		String[] coordinateCmds = new String[7];
		coordinateCmds[1] = cncWebAPICommonCfg.get(CncWebAPIItems.COORDINATE1);
		coordinateCmds[2] = cncWebAPICommonCfg.get(CncWebAPIItems.COORDINATE2);
		coordinateCmds[3] = cncWebAPICommonCfg.get(CncWebAPIItems.COORDINATE3);
		coordinateCmds[4] = cncWebAPICommonCfg.get(CncWebAPIItems.COORDINATE4);
		coordinateCmds[5] = cncWebAPICommonCfg.get(CncWebAPIItems.COORDINATE5);
		coordinateCmds[6] = cncWebAPICommonCfg.get(CncWebAPIItems.COORDINATE6);
		
		int zoneNo = 0;
		for(int i=0; i<zones.length; i++){
			zoneNo = Integer.valueOf(zones[i]);
			if(zoneNo > 0 && zoneNo < 7){
				if("".equals(content)){
					content = coordinateCmds[zoneNo] + "#r#n" + "G65 P" + programs[i];
				}else{
					content += "#r#n" + coordinateCmds[zoneNo] + "#r#n" + "G65 P" + programs[i];
				}
			}else{
				content = "";
				break;
			}
		}
		if(!"".equals(content)) content += "#r#nM30#r#n";
		
		return content;
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
			LogUtils.errorLog("BroCncDataHandler machineMainProgramNameHandler Err:"+e.getMessage()+LogUtils.separator+machiningPrograms);
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
			LogUtils.errorLog("BroCncDataHandler machineCurrSubProgramNameHandler Err:"+e.getMessage()+LogUtils.separator+machiningPrograms);
		}
		
		return progName;
	}
}
