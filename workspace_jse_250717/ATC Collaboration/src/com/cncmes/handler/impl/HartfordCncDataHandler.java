package com.cncmes.handler.impl;
import com.cncmes.base.DeviceState;
import com.cncmes.handler.CncDataHandler;
import com.cncmes.utils.LogUtils;
import net.sf.json.JSONObject;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;

/*
 * 	copy MoriCncDataHandler 2021.11.3 Hui Zhi 
 */

public class HartfordCncDataHandler implements CncDataHandler {

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
