package com.cncmes.base;

import java.util.LinkedHashMap;

import com.cncmes.handler.CncDataHandler;

/**
 * Interface for CNC Control
 * @author LI ZI LONG
 *
 */
public interface CNC {
	/**
	 * @param ip the IP address of the CNC
	 * @param operationName the operation to be executed
	 * @param inParas input parameters, key is the parameter name and value is the parameter value
	 * @param outParas output parameters, key is the parameter name and value is the parameter value
	 * @return true if the specified operation is executed successfully
	 */
	public boolean sendCommand(String ip, String operationName, LinkedHashMap<String, String> inParas, LinkedHashMap<String, String> outParas);
	
	/**
	/**
	 * @param ip the IP address of the CNC
	 * @return true after the door is opened successfully
	 */
	// public boolean cncIOopenDoor(String ip);
	
	/**
	 * @param ip the IP address of the CNC
	 * @return true after the door is closed successfully
	 */
	// public boolean cncIOcloseDoor(String ip);
	
	/**
	 * @param ip the IP address of the CNC
	 * @param workZone the machining zone/coordinate number
	 * @return true after the fixture is clamped successfully
	 */
	// public boolean cncIOclampFixture(String ip, int workZone);
	
	/**
	 * @param ip the IP address of the CNC
	 * @param workZone the machining zone/coordinate number
	 * @return true after the fixture is released successfully
	 */
	// public boolean cncIOreleaseFixture(String ip, int workZone);
	
	/**
	 * @param ip the IP address of the CNC
	 * @return true after the door is opened successfully
	 */
	public boolean openDoor(String ip);
	
	/**
	 * @param ip the IP address of the CNC
	 * @return true after the door is closed successfully
	 */
	public boolean closeDoor(String ip);
	
	/**
	 * @param ip the IP address of the CNC
	 * @param workZone the machining zone/coordinate number
	 * @return true after the fixture is clamped successfully
	 */
	public boolean clampFixture(String ip, int workZone);
	
	/**
	 * @param ip the IP address of the CNC
	 * @param workZone the machining zone/coordinate number
	 * @return true after the fixture is released successfully
	 */
	public boolean releaseFixture(String ip, int workZone);
	
	/**
	 * @param ip the IP address of the CNC
	 * @return the machine state(STANDBY|WORKING|ALARMING|DRIVERFAIL|FINISH|SHUTDOWN|WAITUL|PLAN|LOCK) of the CNC
	 */
	public DeviceState getMachineState(String ip);
	
	/**
	 * @param ip the IP address of the CNC
	 * @return LinkedHashMap<key, value> key is the alarming code and value is the alarming message
	 */
	public LinkedHashMap<String, String> getAlarmInfo(String ip);
	
	/**
	 * @param ip the IP address of the CNC
	 * @return public LinkedHashMap<k1, LinkedHashMap<k2, value>> k1 is the key of CNC tools library, k2 is the tool number, value is the tool life
	 */
	public LinkedHashMap<String, LinkedHashMap<String, String>> getToolLife(String ip);
	
	/**
	 * @param ip the IP address of the CNC
	 * @return LinkedHashMap<paraName, paraVal> paraName is the output parameter name and paraVal is the output value
	 */
	public LinkedHashMap<String, String> getMachiningParas(String ip);
	
	/**
	 * @param ip the IP address of the CNC
	 * @return LinkedHashMap<k1, LinkedHashMap<k2, value>> k1 is the key of the machining counter, k1 is the counter ID and value is the reading of the counter
	 */
	public LinkedHashMap<String, LinkedHashMap<String, String>> getMachiningCounter(String ip);
	
	/**
	 * @param ip the IP address of the CNC
	 * @param programID the program to be executed by CNC
	 * @return true if the program is started successfully
	 */
	public boolean startMachining(String ip, String programID);
	
	/**
	 * @param ip the IP address of the CNC
	 * @return true if current machining is paused successfully
	 */
	public boolean pauseMachining(String ip);
	
	/**
	 * @param ip the IP address of the CNC
	 * @return true if the paused machining is resumed successfully
	 */
	public boolean resumeMachining(String ip);
	
	/**
	 * @param ip the IP address of the CNC
	 * @param programID the program name to be used for main program uploading
	 * @param ncProgramPath the local file path of the main program
	 * @return true if the main program is uploaded successfully
	 */
	public boolean uploadMainProgram(String ip, String programID, String ncProgramPath);
	
	/**
	 * @param ip the IP address of the CNC
	 * @param programID the main program to download
	 * @return true if the main program downloading is successful
	 */
	public String downloadMainProgram(String ip, String programID);
	
	/**
	 * @param ip the IP address of the CNC
	 * @param programID the main program to be deleted
	 * @return true if the specified main program is deleted successfully
	 */
	public boolean deleteMainProgram(String ip, String programID);
	
	/**
	 * @param ip the IP address of the CNC
	 * @param programID the sub program name to be used for sub program uploading
	 * @param ncProgramPath the local file path of the sub program
	 * @return true if the sub program uploading is successful
	 */
	public boolean uploadSubProgram(String ip, String programID, String ncProgramPath);
	
	/**
	 * @param ip the IP address of the CNC
	 * @param programID the sub program to download
	 * @return true if the sub program downloading is successful
	 */
	public String downloadSubProgram(String ip, String programID);
	
	/**
	 * @param ip the IP address of the CNC
	 * @param programID the sub program to delete
	 * @return true if the specified sub program is deleted successfully
	 */
	public boolean deleteSubProgram(String ip, String programID);
	
	/**
	 * @param cncModel model of the CNC
	 * @param handler the data handler of the specified CNC model 
	 * @return true if the data handler is set successfully
	 */
	public boolean setDataHandler(String cncModel, CncDataHandler handler);
	
	/**
	 * @param ip the IP address of the CNC
	 * @return the data handler of the specified CNC
	 */
	public CncDataHandler getDataHandler(String ip);
	
	/**
	 * @param ip the IP address of the CNC
	 * @param programID the main program to check
	 * @return true if the specified main program is currently activated
	 */
	public boolean mainProgramIsActivate(String ip, String programID);
	
	/**
	 * @param ip the IP address of the CNC
	 * @param programID the main program to get
	 * @return content of the specified main program
	 */
	public String getMainProgramName(String ip, String programID);
	
	/**
	 * @param ip the IP address of the CNC
	 * @param programID the sub program to get
	 * @return content of the specified sub program
	 */
	public String getCurrSubProgramName(String ip, String programID);
	
	/**
	 * @param ip the IP address of the CNC
	 * @param wpIDs the workpiece IDs to be machined
	 * @param subPrograms the sub programs for the workpiece machining
	 * @param workzones the work zones for the workpieces
	 * @return content of the generated main program
	 */
	public String generateMainProgramContent(String ip, String wpIDs, String subPrograms, String workzones);
	
	/**
	 * @param ip the IP address of the CNC
	 * @return the data handler of the specified CNC
	 */
	public boolean hfOpenDoor(String ip);
	
	/**
	 * @param ip the IP address of the CNC
	 * @return the data handler of the specified CNC
	 */
	public boolean hfCloseDoor(String ip);
	/**
	 * @param ip the IP address of the CNC
	 * @param workZone the machining zone/coordinate number
	 * @return true after the fixture is clamped successfully
	 */
	public boolean hfClampFixture(String ip, int workZone);


	public boolean flushProgram(String ip,Boolean needToFlush);

	double getMacro(String cncIp, double address);

	boolean setReleaseMacro(String ip, String address, String value, String dec);

	/**
	 * @param ip the IP address of the CNC
	 * @param workZone the machining zone/coordinate number
	 * @return true after the fixture is released successfully
	 */
	public boolean hfReleaseFixture(String ip, int workZone);
	
	/**
	 * @param ip the IP address of the CNC
	 * @return the data handler of the specified CNC
	 */
	public boolean hfResetDoor(String ip);
	
	/**
	 * @param ip the IP address of the CNC
	 * @return the data handler of the specified CNC
	 */
	public boolean hfHomeDoor(String ip);
	
	/**
	 * @param ip the IP address of the CNC
	 * @param angle the machining axis 4(0,90,180,270)
	 * @return the data handler of the specified CNC
	 */
	public boolean hfRotate(String ip,int angle);
	
	/**
	 * @param ip the IP address of the CNC
	 * @return the data handler of the specified CNC
	 */
	public boolean hfCheckPosition(String ip);
	
	/**
	 * @param ip the IP address of the CNC
	 * set CNC register 900 value is 1 to start normal program
	 * @return the data handler of the specified CNC
	 */
	
	public boolean startMachinePrepared(String ip);
	public boolean checkPosition(String ip, int address, double value);
	public boolean hfFiveAspectProcess(String cncIP);
	public boolean setMacro(String ip,String name, String value,String dec);

	/**
	 *
	 * @param ip
	 * @param barcode    fixture barcode
	 * @param programName	the name is upload program to cnc and rename the program
	 * @return
	 */
	public Boolean getAndUploadProgramByBarcode(String ip,String barcode,String programName);

	/**
	 *
	 * @param cncip
	 * @param sensorAddress
	 * @param value
	 * @return if value == sensorValue return true else return false
	 */
    Boolean checkSensor(String cncip, String sensorAddress, String value);
}
