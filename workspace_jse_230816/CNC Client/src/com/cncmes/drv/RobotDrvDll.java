package com.cncmes.drv;

import java.util.LinkedHashMap;

import com.cncmes.base.Robot;
import com.cncmes.base.RobotItems;
import com.cncmes.data.RobotData;
import com.sun.jna.Native;

/**
 * Robot External Driver
 * @author LI ZI LONG
 *
 */
public class RobotDrvDll implements Robot {
	private static RobotDrvDll robotDrvDll = new RobotDrvDll();
	private RobotDrvDll(){}
	public static RobotDrvDll getInstance(String libName){
		Native.register(libName);
		return robotDrvDll;
	}
	
	private native int pickMaterialFromTray(String ip, int port, int slotNo, String targetName);
	private native int putMaterialOntoTray(String ip, int port, int slotNo, String targetName);
	private native int pickMaterialFromRack(String ip, int port, String rackId, String position, String targetName);
	private native int putMaterialOntoRack(String ip, int port, String rackId, String position, String targetName);
	private native int pickMaterialFromMachine(String ip, int port, String machineIp, int workZone, String targetName);
	private native int putMaterialOntoMachine(String ip, int port, String machineIp, int workZone, String targetName);
	private native int moveToMachine(String ip, int port, String machineIp, String targetName);
	private native int moveToRack(String ip, int port, String rackId, String targetName);
	private native String getBattery(String ip, int port, String targetName);
	private native String scanBarcode(String ip, int port, String targetName);
	private native int goCharging(String ip, int port, String targetName);
	private native int stopCharging(String ip, int port, String targetName);
	
	private int getPort(String ip){
		int port = 0;
		try {
			port = (int) RobotData.getInstance().getDataMap().get(ip).get(RobotItems.PORT);
		} catch (Exception e) {
		}
		
		return port;
	}
	
	@Override
	public boolean pickMaterialFromTray(String ip, int slotNo, String targetName) {
		return pickMaterialFromTray(ip,getPort(ip),slotNo,targetName)==0?true:false;
	}
	
	@Override
	public boolean putMaterialOntoTray(String ip, int slotNo, String targetName) {
		return putMaterialOntoTray(ip,getPort(ip),slotNo,targetName)==0?true:false;
	}
	
	@Override
	public boolean pickMaterialFromRack(String ip, String rackId, String rackSlot, String robotSlot, String rackPositionTag) {
		return pickMaterialFromRack(ip,getPort(ip),rackId,rackSlot,rackPositionTag)==0?true:false;
	}
	
	@Override
	public boolean putMaterialOntoRack(String ip, String rackId, String rackSlot, String robotSlot, String rackPositionTag) {
		return putMaterialOntoRack(ip,getPort(ip),rackId,rackSlot,rackPositionTag)==0?true:false;
	}
	
	@Override
	public boolean pickMaterialFromMachine(String ip, String machineIp, int workZone, String robotSlot, String machinePositionTag) {
		return pickMaterialFromMachine(ip,getPort(ip),machineIp,workZone,machinePositionTag)==0?true:false;
	}
	
	@Override
	public boolean putMaterialOntoMachine(String ip, String machineIp, int workZone, String robotSlot, String machinePositionTag) {
		return putMaterialOntoMachine(ip,getPort(ip),machineIp,workZone,machinePositionTag)==0?true:false;
	}
	
	@Override
	public boolean moveToMachine(String ip, String machineIp, String targetName) {
		return moveToMachine(ip,getPort(ip),machineIp,targetName)==0?true:false;
	}
	
	@Override
	public boolean moveToRack(String ip, String rackId, String targetName) {
		return moveToRack(ip,getPort(ip),rackId,targetName)==0?true:false;
	}
	
	@Override
	public String getBattery(String ip, String targetName) {
		return getBattery(ip,getPort(ip),targetName);
	}
	
	@Override
	public String scanBarcode(String ip, String targetName) {
		return scanBarcode(ip,getPort(ip),targetName);
	}
	
	@Override
	public boolean goCharging(String ip, String targetName) {
		return goCharging(ip,getPort(ip),targetName)==0?true:false;
	}
	
	@Override
	public boolean sendCommand(String ip, String operationName, LinkedHashMap<String, String> inParas, LinkedHashMap<String, String> outParas, String targetName){
		return true;
	}
	@Override
	public boolean stopCharging(String ip, String targetName) {
		return stopCharging(ip,getPort(ip),targetName)==0?true:false;
	}
	@Override
	public String getLastCmd(String ip, String targetName) {
		return null;
	}
}
