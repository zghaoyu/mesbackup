package com.cncmes.handler;

import com.cncmes.base.DeviceState;

import java.util.LinkedHashMap;

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
