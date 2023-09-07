package com.cncmes.base;

import java.util.LinkedHashMap;

import com.cncmes.handler.CncDataHandler;

public interface CNC {
	public boolean sendCommand(String ip, String operationName, LinkedHashMap<String, String> inParas, LinkedHashMap<String, String> outParas);
	
	/**
	 * 
	 * @param ip the IP address of the CNC
	 * @return true after the door is opened successfully
	 */
	public boolean openDoor(String ip);
	
	/**
	 * 
	 * @param ip the IP address of the CNC
	 * @return true after the door is closed successfully
	 */
	public boolean closeDoor(String ip);
	
	/**
	 * 
	 * @param ip the IP address of the CNC
	 * @param workZone the machining zone/coordinate number
	 * @return true after the fixture is clamped successfully
	 */
	public boolean clampFixture(String ip, int workZone);
	
	/**
	 * 
	 * @param ip the IP address of the CNC
	 * @param workZone the machining zone/coordinate number
	 * @return true after the fixture is released successfully
	 */
	public boolean releaseFixture(String ip, int workZone);
	
	public DeviceState getMachineState(String ip);
	public LinkedHashMap<String, String> getAlarmInfo(String ip);
	public LinkedHashMap<String, LinkedHashMap<String, String>> getToolLife(String ip);
	public LinkedHashMap<String, String> getMachiningParas(String ip);
	public LinkedHashMap<String, LinkedHashMap<String, String>> getMachiningCounter(String ip);
	public boolean startMachining(String ip, String programID);
	public boolean pauseMachining(String ip);
	public boolean resumeMachining(String ip);
	public boolean uploadMainProgram(String ip, String programID, String ncProgramPath);
	public String downloadMainProgram(String ip, String programID);
	public boolean deleteMainProgram(String ip, String programID);
	public boolean uploadSubProgram(String ip, String programID, String ncProgramPath);
	public String downloadSubProgram(String ip, String programID);
	public boolean deleteSubProgram(String ip, String programID);
	public boolean setDataHandler(String cncModel, CncDataHandler handler);
	public CncDataHandler getDataHandler(String ip);
	public boolean mainProgramIsActivate(String ip, String programID);
	public String getMainProgramName(String ip, String programID);
	public String getCurrSubProgramName(String ip, String programID);
	public String generateMainProgramContent(String ip, String wpIDs, String subPrograms, String workzones);
}
