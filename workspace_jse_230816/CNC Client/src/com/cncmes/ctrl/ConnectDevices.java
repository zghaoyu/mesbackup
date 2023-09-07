package com.cncmes.ctrl;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.cncmes.base.CNC;
import com.cncmes.base.CncItems;
import com.cncmes.base.DeviceState;
import com.cncmes.base.DriverItems;
import com.cncmes.base.IRobot;
import com.cncmes.base.RackItems;
import com.cncmes.base.Robot;
//import com.cncmes.base.Robot;
import com.cncmes.base.RobotItems;
import com.cncmes.base.ScannerItems;
import com.cncmes.base.SchedulerItems;
import com.cncmes.data.CncData;
import com.cncmes.data.CncDriver;
import com.cncmes.data.RackProduct;
import com.cncmes.data.RobotData;
import com.cncmes.data.RobotDriver;
import com.cncmes.data.ScannerData;
import com.cncmes.data.ScannerDriver;
import com.cncmes.data.SchedulerCfg;
import com.cncmes.data.SystemConfig;
import com.cncmes.gui.CNCClient;
import com.cncmes.gui.PaintCNCLines;
import com.cncmes.utils.RunningMsg;
import com.cncmes.utils.XmlUtils;

/**
 * Connect All Offline Devices
 * @author LI ZI LONG
 *
 */
public class ConnectDevices {
	private static SocketClient socketClient = SocketClient.getInstance();
	private static LinkedHashMap<String,Boolean> bCNCStateInit = new LinkedHashMap<String,Boolean>();
	private static LinkedHashMap<String,Boolean> bRobotStateInit = new LinkedHashMap<String,Boolean>();
	private static PaintCNCLines paintCNCLines = PaintCNCLines.getInstance();
	private static String appMode = "";
	
	static{
		XmlUtils.parseSystemConfig();
	}
	
	/**
	 * try to connect all newly online devices
	 */
	public static void ConnectAllDevices(){
		SystemConfig sysCfg = SystemConfig.getInstance();
		LinkedHashMap<String,Object> config = sysCfg.getCommonCfg();
		appMode = ""+config.get("AppMode");
		
		connectRobot();
		connectCNC();
	}
	
	/**
	 * reset the initialization flags of CNC and Robot
	 */
	public static void resetDevInitFlag(){
		bCNCStateInit.clear();
		bRobotStateInit.clear();
	}
	
	/**
	 * try to connect previously offline CNC and refresh CNC GUI panel
	 */
	public static void connectCNC(){
		CncData cncData = CncData.getInstance();
		CNCClient cncClient = CNCClient.getInstance();
		Map<String,LinkedHashMap<CncItems,Object>> dataMap = cncData.getDataMap();
		if(!dataMap.isEmpty()){
			Set<String> set = dataMap.keySet();
			Iterator<String> it = set.iterator();
			
			String ip, model, cmdEndChr, cncDrvName, cncDataHandler;
			int port;
			boolean bInit;
			DeviceState devState, curState;
			CncDriver cncDriver = CncDriver.getInstance();
			CNC cncCtrl = null;
			
			while(it.hasNext()){
				try {
					bInit = true;
					ip = it.next();
					devState = cncData.getCncState(ip);
					model = (String) dataMap.get(ip).get(CncItems.MODEL);
					port = (int) dataMap.get(ip).get(CncItems.PORT);
					cmdEndChr = (String) cncDriver.getData(model).get(DriverItems.CMDENDCHR);
					cncDrvName = (String)cncDriver.getData(model).get(DriverItems.DRIVER);
					cncDataHandler = (String)cncDriver.getData(model).get(DriverItems.DATAHANDLER);
					cncCtrl = CncFactory.getInstance(cncDrvName,cncDataHandler,model);
					
					//Check the state while system startup
					if(null != bCNCStateInit.get(ip)) bInit = bCNCStateInit.get(ip);
					if(DeviceState.SHUTDOWN == devState){
						try {
							if(null == cncCtrl){
								cncData.setCncState(ip, DeviceState.DRIVERFAIL, false, false, true, false);
							}else{
								if(bInit){
									curState = cncCtrl.getMachineState(ip);
									if(DeviceState.SHUTDOWN != curState){
										RunningMsg.set("connectCNC "+ip+":"+port+" "+cmdEndChr+"/OK");
										cncData.setCncState(ip, DeviceState.STANDBY, false, false, true, false);
										cncData.setData(ip, CncItems.CTRL, cncCtrl);
										bCNCStateInit.put(ip, false);
									}
								}
							}
						} catch (Exception e) {
						}
					}
					
					if(cncData.getSpecificDataChangeFlag(ip)){
						paintCNCLines.repaintCNC(ip, cncData.getCncDataString(ip));
						cncClient.setStatusPaneContent();
						cncData.resetSpecificDataChangeFlag(ip);
						cncData.resetDataChangeFlag();
					}else if("DevStateMon".equals(appMode)){
						curState = cncCtrl.getMachineState(ip);
						if(devState != curState) cncData.setCncState(ip, curState, false, false, true, false);
					}
					cncClient.refreshHeartbeat();
				} catch (Exception e) {
				}
			}
		}
	}
	
	/**
	 * try to connect previously offline robot
	 */
	public static void connectRobot(){
		RobotData robotData = RobotData.getInstance();
		Map<String,LinkedHashMap<RobotItems,Object>> dataMap = robotData.getDataMap();
		
		if(!dataMap.isEmpty()){
			Set<String> set = dataMap.keySet();
			Iterator<String> it = set.iterator();
			
			String ip, model;
			boolean bInit;
			DeviceState devState;
			while(it.hasNext()){
				bInit = true;
				ip = it.next();
				devState = robotData.getRobotState(ip);
				if(null != bRobotStateInit.get(ip)) bInit = bRobotStateInit.get(ip);
				if(DeviceState.SHUTDOWN == devState){
					try {
						model = (String) dataMap.get(ip).get(RobotItems.MODEL);
						//Change Robot to IRobot by Hui Zhi 2021/12/28
//						Robot robotCtrl = RobotFactory.getInstance((String)RobotDriver.getInstance().getData(model).get(DriverItems.DRIVER));
						IRobot robotCtrl = IRobotFactory.getInstance((String)RobotDriver.getInstance().getData(model).get(DriverItems.DRIVER));
						if(null == robotCtrl){
							robotData.setRobotState(ip, DeviceState.DRIVERFAIL, false, false, true, false);
						}else{
							if(bInit){
//								if(null != robotCtrl.getBattery(ip,CncDriver.getInstance().getAnyCNCModel())){
								//TODO
								if("-1" != robotCtrl.getBattery(ip)){// use the command of IRobot by Hui Zhi 2021/12/28
									robotData.setRobotState(ip, DeviceState.STANDBY, false, false, true, false);
									bRobotStateInit.put(ip, false);
								}
							}
						}
					} catch (Exception e) {
					}
				}
			}
		}
	}
	
	/**
	 * try to connect previously offline Scanner
	 */
//	public static void connectScanner(){
//		ScannerData scannerData = ScannerData.getInstance();
//		Map<String,LinkedHashMap<ScannerItems,Object>> dataMap = scannerData.getDataMap();
//		
//		if(!dataMap.isEmpty()){
//			Set<String> set = dataMap.keySet();
//			Iterator<String> it = set.iterator();
//			
//			String ip;
//			int port;
//			DeviceState devState;
//			String model;
//			String cmdEndChr;
//			while(it.hasNext()){
//				ip = it.next();
//				devState = (DeviceState) dataMap.get(ip).get(ScannerItems.STATE);
//				port = (int) dataMap.get(ip).get(ScannerItems.PORT);
//				model = (String) dataMap.get(ip).get(ScannerItems.MODEL);
//				cmdEndChr = (String) ScannerDriver.getInstance().getData(model).get(DriverItems.CMDENDCHR);
//				if(DeviceState.SHUTDOWN == devState){
//					try {
//						if(null == socketClient.getSocket(ip, port) && null != socketClient.getSocketDataHandler(ip, port)){
//							if(socketClient.connect(ip, port, socketClient.getSocketDataHandler(ip, port), cmdEndChr)){
//								RunningMsg.set("connectScanner "+ip+":"+port+" "+cmdEndChr+"/OK");
//								scannerData.setData(ip, ScannerItems.STATE, DeviceState.STANDBY);
//							}
//						}
//					} catch (IOException e) {
//					}
//				}
//			}
//		}
//	}
	
	/**
	 * try to connect previously offline Scheduler
	 */
//	public static void connectScheduler(){
//		SchedulerCfg sCfg = SchedulerCfg.getInstance();
//		Map<String,LinkedHashMap<SchedulerItems,Object>> dataMap = sCfg.getDataMap();
//		if(!dataMap.isEmpty()){
//			Set<String> set = dataMap.keySet();
//			Iterator<String> it = set.iterator();
//			
//			String ip;
//			int[] port = new int[]{9900,9901,9902,9903,9904};
//			DeviceState devState;
//			String cmdEndChr;
//			while(it.hasNext()){
//				try {
//					ip = it.next();
//					devState = (DeviceState) dataMap.get(ip).get(SchedulerItems.STATE);
//					port[0] = Integer.parseInt((String)dataMap.get(ip).get(SchedulerItems.PORTMACHINE));
//					port[1] = Integer.parseInt((String)dataMap.get(ip).get(SchedulerItems.PORTROBOT));
//					port[2] = Integer.parseInt((String)dataMap.get(ip).get(SchedulerItems.PORTMATERIAL));
//					port[3] = Integer.parseInt((String)dataMap.get(ip).get(SchedulerItems.PORTTASK));
//					port[4] = Integer.parseInt((String)dataMap.get(ip).get(SchedulerItems.PORTRACK));
//					cmdEndChr = "CR";
//					if(DeviceState.SHUTDOWN == devState){
//						boolean cnnOK = false;
//						for(int i=0; i<port.length; i++){
//							try {
//								if(null == socketClient.getSocket(ip, port[i]) && null != socketClient.getSocketDataHandler(ip, port[i])){
//									if(socketClient.connect(ip, port[i], socketClient.getSocketDataHandler(ip, port[i]), cmdEndChr)){
//										RunningMsg.set("connectScheduler "+ip+":"+port[i]+" "+cmdEndChr+"/OK");
//										cnnOK = true;
//									}
//								}
//							} catch (IOException e) {
//								break;
//							}
//						}
//						if(cnnOK) sCfg.setData(ip, SchedulerItems.STATE, DeviceState.STANDBY);
//					}
//				} catch (Exception e) {
//				}
//			}
//		}
//	}
	
	/**
	 * try to connect previously offline Rack Manager
	 */
//	public static void connectRack(){
//		RackProduct rackProd = RackProduct.getInstance();
//		Map<String,LinkedHashMap<RackItems,Object>> dataMap = rackProd.getDataMap();
//		if(!dataMap.isEmpty()){
//			Set<String> set = dataMap.keySet();
//			Iterator<String> it = set.iterator();
//			
//			String mainKey;
//			String ip;
//			int port = 9950;
//			DeviceState devState;
//			String cmdEndChr;
//			while(it.hasNext()){
//				try {
//					mainKey = it.next();
//					devState = (DeviceState) dataMap.get(mainKey).get(RackItems.STATE);
//					ip = (String)dataMap.get(mainKey).get(RackItems.IP);
//					port = (int) dataMap.get(mainKey).get(RackItems.PORT);
//					cmdEndChr = "CR";
//					if(9950 == port){
//						if(DeviceState.SHUTDOWN == devState){
//							boolean cnnOK = false;
//							try {
//								if(null == socketClient.getSocket(ip, port) && null != socketClient.getSocketDataHandler(ip, port)){
//									if(socketClient.connect(ip, port, socketClient.getSocketDataHandler(ip, port), cmdEndChr)){
//										RunningMsg.set("connectRack "+ip+":"+port+" "+cmdEndChr+"/OK");
//										cnnOK = true;
//									}
//								}
//							} catch (IOException e) {
//								break;
//							}
//							if(cnnOK) rackProd.setData(mainKey, RackItems.STATE, DeviceState.STANDBY);
//						}
//					}
//				} catch (Exception e) {
//				}
//			}
//		}
//	}
}
