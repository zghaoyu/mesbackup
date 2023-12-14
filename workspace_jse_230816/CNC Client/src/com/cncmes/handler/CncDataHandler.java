package com.cncmes.handler;

import java.util.LinkedHashMap;

import com.cncmes.base.DeviceState;

public interface CncDataHandler {
	/**
	 * 
	 * @param alarmInfo the alarm information String queried from the machine
	 * @return the re-processing alarm info
	 */
	public LinkedHashMap<String, String> alarmInfoHandler(String alarmInfo);
	
	/**
	 * 
	 * @param machineState the machine state information String queried from the machine
	 * @return the state(STANDBY|WORKING|SHUTDOWN) of the machine
	 */
	public DeviceState machineStateHandler(String machineState);
	
	/**
	 * 
	 * @param machineParas the machining parameters information String queried from the machine
	 * @return the re-processing machining parameters info
	 */
	public LinkedHashMap<String, String> machineParasHandler(String machineParas);
	
	/**
	 * 
	 * @param machineToolLife the machine tools-life information String queried from the machine
	 * @return the re-processing tools-life info
	 */
	public LinkedHashMap<String, LinkedHashMap<String, String>> machineToolLifeHandler(String machineToolLife);
	
	/**
	 * 
	 * @param machineCounter the machine counter information String queried from the machine
	 * @return the re-processing counter info
	 */
	public LinkedHashMap<String, LinkedHashMap<String, String>> machineCounterHandler(String machineCounter);
	
	/**
	 * 
	 * @param ncProgramContent the content of the NC program
	 * @param saveFileName the file name for saving the NC program into root folder
	 * @return the MD5 value of the NC program
	 */
	public String machineNCProgramHandler(String ncProgramContent, String saveFileName);
	
	/**
	 * 
	 * @param ip the IP address of the machine
	 * @param programID the sub program ID
	 * @return true - means the preparing work before uploading sub programs is done successfully
	 */
	public boolean machineUploadSubProgramHandler(String ip, String programID);
	
	/**
	 * 
	 * @param ip the IP address of the machine
	 * @param programID the main program ID
	 * @return true - means the main program is fixed(content is predefined) in the machine but need to check its content and check whether it is activated or not
	 * ; false - means the main program is not fixed(the content can be specified dynamically)
	 */
	public boolean machineUploadMainProgramHandler(String ip, String programID);
	
	/**
	 * 
	 * @param ip the IP address of the machine
	 * @param wpIDs the workpiece list to be machined(IDs are separated by ";")
	 * @param subPrograms the sub program list(program names are separated by ";")
	 * @param workzones the machining zone/coordinate list(work zones are separated by ";")
	 * @return the content of the main program(different lines are separated by "#r#n")
	 */
	public String machineGenerateMainProgramHandler(String ip, String wpIDs, String subPrograms, String workzones);
	
	/**
	 * @param machiningPrograms the query result from machine in json format
	 * @return the main program name
	 */
	public String machineMainProgramNameHandler(String machiningPrograms);
	
	/**
	 * @param machiningPrograms the query result from machine in json format
	 * @return the current/running sub program name
	 */
	public String machineCurrSubProgramNameHandler(String machiningPrograms);

	/**
	 * @param hfCheckPosition the query result from machine in json format
	 * @return boolean
	 */
	public boolean machineAxisPositionHandler(String machinePosition, int address, double value);
	
	/**
	 * @param hfCheckPosition the query result from machine in json format
	 * @return boolean
	 */
	public boolean machineRotateHandler(String machinePosition, double value);
}
