package com.cncmes.base;

import java.util.LinkedHashMap;

public interface Robot {
	public boolean sendCommand(String ip, String operationName, LinkedHashMap<String, String> inParas, LinkedHashMap<String, String> outParas, String targetName);
	public boolean pickMaterialFromTray(String ip, int slotNo, String targetName);
	public boolean putMaterialOntoTray(String ip, int slotNo, String targetName);
	public boolean pickMaterialFromRack(String ip, String rackId, String rackSlot, String robotSlot, String targetName);
	public boolean putMaterialOntoRack(String ip, String rackId, String rackSlot, String robotSlot, String targetName);
	public boolean pickMaterialFromMachine(String ip, String machineIp, int workZone, String robotSlot, String targetName);
	public boolean putMaterialOntoMachine(String ip, String machineIp, int workZone, String robotSlot, String targetName);
	public boolean moveToMachine(String ip, String machineIp, String targetName);
	public boolean moveToRack(String ip, String rackId, String targetName);
	public String getBattery(String ip, String targetName);
	public String scanBarcode(String ip, String targetName);
	public boolean goCharging(String ip, String targetName);
	public boolean stopCharging(String ip, String targetName);
}
