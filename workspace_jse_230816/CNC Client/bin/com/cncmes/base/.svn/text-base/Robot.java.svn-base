package com.cncmes.base;

import java.util.LinkedHashMap;

/**
 * Interfaces for Robot Control
 * @author LI ZI LONG
 *
 */
public interface Robot {
	/**
	 * @param ip the IP address of the Robot
	 * @param operationName the operation to be executed
	 * @param inParas the input parameters for the specified operation
	 * @param outParas the output parameters of the specified operation
	 * @param targetName the target object to be served by the Robot
	 * @return true if the specified operation is executed successfully
	 */
	public boolean sendCommand(String ip, String operationName, LinkedHashMap<String, String> inParas, LinkedHashMap<String, String> outParas, String targetName);
	
	/**
	 * @param ip the IP address of the Robot
	 * @param slotNo the robot tray slot number to operate
	 * @param targetName the target object to pick from
	 * @return true if operation is successful
	 */
	public boolean pickMaterialFromTray(String ip, int slotNo, String targetName);
	
	/**
	 * @param ip the IP address of the Robot
	 * @param slotNo the robot tray slot number to operate
	 * @param targetName the target object to put onto
	 * @return true if operation is successful
	 */
	public boolean putMaterialOntoTray(String ip, int slotNo, String targetName);
	
	/**
	 * @param ip the IP address of the Robot
	 * @param rackId the ID of the material rack
	 * @param rackSlot the rack slot number to operate
	 * @param robotSlot the robot slot number to operate
	 * @param targetName the target object to pick from
	 * @return true if operation is successful
	 */
	public boolean pickMaterialFromRack(String ip, String rackId, String rackSlot, String robotSlot, String targetName);
	
	/**
	 * @param ip the IP address of the Robot
	 * @param rackId the ID of the material rack
	 * @param rackSlot the rack slot number to operate
	 * @param robotSlot the robot slot number to operate
	 * @param targetName the target object to put onto
	 * @return true if operation is successful
	 */
	public boolean putMaterialOntoRack(String ip, String rackId, String rackSlot, String robotSlot, String targetName);
	
	/**
	 * @param ip the IP address of the Robot
	 * @param machineIp the IP address of the CNC
	 * @param workZone the work zone number to operate
	 * @param robotSlot the robot slot number to operate
	 * @param targetName the target object to pick from
	 * @return true if operation is successful
	 */
	public boolean pickMaterialFromMachine(String ip, String machineIp, int workZone, String robotSlot, String targetName);
	
	/**
	 * @param ip the IP address of the Robot
	 * @param machineIp the IP address of the CNC
	 * @param workZone the work zone number to operate
	 * @param robotSlot the robot slot number to operate
	 * @param targetName the target object to put onto
	 * @return true if operation is successful
	 */
	public boolean putMaterialOntoMachine(String ip, String machineIp, int workZone, String robotSlot, String targetName);
	
	/**
	 * @param ip the IP address of the Robot
	 * @param machineIp the IP address of the CNC
	 * @param targetName the target object to move to
	 * @return true if operation is successful
	 */
	public boolean moveToMachine(String ip, String machineIp, String targetName);
	
	/**
	 * @param ip the IP address of the Robot
	 * @param rackId the ID of the rack
	 * @param targetName the target object to move to
	 * @return true if operation is successful
	 */
	public boolean moveToRack(String ip, String rackId, String targetName);
	
	/**
	 * @param ip the IP address of the Robot
	 * @param targetName the target object
	 * @return the power left in percentage
	 */
	public String getBattery(String ip, String targetName);
	
	/**
	 * @param ip the IP address of the Robot
	 * @param targetName the target object
	 * @return content of the bar code
	 */
	public String scanBarcode(String ip, String targetName);
	
	/**
	 * @param ip the IP address of the Robot
	 * @param targetName the target object
	 * @return true if charging successfully
	 */
	public boolean goCharging(String ip, String targetName);
	
	/**
	 * @param ip the IP address of the Robot
	 * @param targetName the target object
	 * @return true if stop charging successfully
	 */
	public boolean stopCharging(String ip, String targetName);
	
	/**
	 * @param ip the IP address of the Robot
	 * @param targetName the target object
	 * @return execution status of the last command
	 */
	public String getLastCmd(String ip, String targetName);
}
