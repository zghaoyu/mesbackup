package com.cncmes.drv;

import java.util.LinkedHashMap;

import com.cncmes.base.CNC;
import com.cncmes.base.CncItems;
import com.cncmes.base.DeviceState;
import com.cncmes.data.CncData;
import com.cncmes.handler.CncDataHandler;
import com.sun.jna.Native;

/**
 * CNC External Driver
 * @author LI ZI LONG
 *
 */
public class CncDrvDll implements CNC {
	private static CncDrvDll cncDrvDll = new CncDrvDll();
	private static LinkedHashMap<String, CncDataHandler> dataHandler = new LinkedHashMap<String, CncDataHandler>();
	private CncDrvDll(){}
	
	public static CncDrvDll getInstance(String libName){
		Native.register(libName);
		return cncDrvDll;
	}
	
	private native int openDoor(String ip,int port);
	private native int closeDoor(String ip,int port);
	private native int clampFixture(String ip,int port,int workZone);
	private native int releaseFixture(String ip,int port,int workZone);
	private native String getMachineState(String ip,int port);
	private native String getAlarmInfo(String ip,int port);
	private native String getToolLife(String ip,int port);
	private native String getMachineParas(String ip, int port);
	private native String getMachineCounter(String ip,int port);
	private native int startMachining(String ip,int port,String programID);
	private native int pauseMachining(String ip,int port);
	private native int resumeMachining(String ip,int port);
	private native int uploadMainProgram(String ip,int port,String programID,String programPath);
	private native String downloadMainProgram(String ip,int port,String programID);
	private native int deleteMainProgram(String ip,int port,String programID);
	private native int uploadSubProgram(String ip,int port,String programID,String programPath);
	private native String downloadSubProgram(String ip,int port,String programID);
	private native int deleteSubProgram(String ip,int port,String programID);
	
	private int getPort(String ip){
		int port = 0;
		try {
			port = (int) CncData.getInstance().getData(ip).get(CncItems.PORT);
		} catch (Exception e) {
		}
		return port;
	}
	
	@Override
	public boolean openDoor(String ip) {
		return openDoor(ip,getPort(ip))==0?true:false;
	}

	@Override
	public boolean closeDoor(String ip) {
		return closeDoor(ip,getPort(ip))==0?true:false;
	}

	@Override
	public boolean clampFixture(String ip, int workZone) {
		return clampFixture(ip,getPort(ip),workZone)==0?true:false;
	}
	
	@Override
	public boolean releaseFixture(String ip, int workZone) {
		return releaseFixture(ip,getPort(ip),workZone)==0?true:false;
	}
	
	@Override
	public DeviceState getMachineState(String ip) {
		getMachineState(ip,getPort(ip));
		return null;
	}

	@Override
	public LinkedHashMap<String, String> getAlarmInfo(String ip) {
		getAlarmInfo(ip,getPort(ip));
		return null;
	}

	@Override
	public LinkedHashMap<String, LinkedHashMap<String, String>> getToolLife(String ip) {
		getToolLife(ip,getPort(ip));
		return null;
	}

	@Override
	public LinkedHashMap<String, String> getMachiningParas(String ip) {
		getMachineParas(ip,getPort(ip));
		return null;
	}

	@Override
	public LinkedHashMap<String, LinkedHashMap<String, String>> getMachiningCounter(String ip) {
		getMachineCounter(ip,getPort(ip));
		return null;
	}

	@Override
	public boolean startMachining(String ip, String programID) {
		return startMachining(ip,getPort(ip),programID)==0?true:false;
	}

	@Override
	public boolean pauseMachining(String ip) {
		return pauseMachining(ip,getPort(ip))==0?true:false;
	}

	@Override
	public boolean resumeMachining(String ip) {
		return resumeMachining(ip,getPort(ip))==0?true:false;
	}

	@Override
	public boolean uploadMainProgram(String ip, String programID, String ncProgramPath) {
		return uploadMainProgram(ip,getPort(ip),programID,ncProgramPath)==0?true:false;
	}

	@Override
	public String downloadMainProgram(String ip, String programID) {
		downloadMainProgram(ip,getPort(ip),programID);
		return null;
	}

	@Override
	public boolean deleteMainProgram(String ip, String programID) {
		return deleteMainProgram(ip,getPort(ip),programID)==0?true:false;
	}
	
	@Override
	public boolean uploadSubProgram(String ip, String programID, String ncProgramPath) {
		return uploadSubProgram(ip,getPort(ip),programID,ncProgramPath)==0?true:false;
	}

	@Override
	public String downloadSubProgram(String ip, String programID) {
		downloadSubProgram(ip,getPort(ip),programID);
		return null;
	}

	@Override
	public boolean deleteSubProgram(String ip, String programID) {
		return deleteSubProgram(ip,getPort(ip),programID)==0?true:false;
	}
	
	@Override
	public boolean mainProgramIsActivate(String ip, String programID) {
		return true;
	}
	
	@Override
	public String getMainProgramName(String ip, String programID) {
		return programID;
	}
	
	@Override
	public String getCurrSubProgramName(String ip, String programID) {
		return programID;
	}
	
	@Override
	public String generateMainProgramContent(String ip, String wpIDs, String subPrograms, String workzones) {
		String content = "";
		
		return content;
	}
	
	@Override
	public boolean setDataHandler(String cncModel, CncDataHandler handler) {
		if(null != handler){
			dataHandler.put(cncModel, handler);
			return true;
		}
		return false;
	}

	@Override
	public CncDataHandler getDataHandler(String ip) {
		String cncModel = (String) CncData.getInstance().getData(ip).get(CncItems.MODEL);
		return dataHandler.get(cncModel);
	}
	
	@Override
	public boolean sendCommand(String ip, String cmd, LinkedHashMap<String, String> inParas, LinkedHashMap<String, String> rtnData){
		return true;
	}
}
