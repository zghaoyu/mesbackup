package com.cncmes.ctrl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.LinkedHashMap;

import org.apache.commons.io.FileUtils;

//import com.cncmes.base.ClientType;
import com.cncmes.base.DeviceState;
import com.cncmes.base.MemoryItems;
import com.cncmes.base.SchedulerDataItems;
import com.cncmes.base.SchedulerItems;
import com.cncmes.base.TaskItems;
import com.cncmes.data.SchedulerCfg;
import com.cncmes.data.SchedulerMachine;
import com.cncmes.data.SchedulerMaterial;
import com.cncmes.data.SchedulerRobot;
import com.cncmes.data.SchedulerTask;
import com.cncmes.data.TaskData;
import com.cncmes.gui.Scheduler;
import com.cncmes.handler.SocketRespHandler;
import com.cncmes.thread.ThreadController;
import com.cncmes.utils.DataUtils;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.MathUtils;
import com.cncmes.utils.MyFileUtils;
import com.cncmes.utils.MySystemUtils;
import com.cncmes.utils.RunningMsg;
import com.cncmes.utils.ThreadUtils;
import com.cncmes.utils.TimeUtils;

import net.sf.json.JSONObject;

public class SchedulerServer {
	private static SchedulerServer schedulerSvr = new SchedulerServer();
	private LinkedHashMap<String,Socket> machineClientSockets = new LinkedHashMap<String,Socket>();
	private LinkedHashMap<String,Socket> robotClientSockets = new LinkedHashMap<String,Socket>();
	private LinkedHashMap<String,Socket> materialClientSockets = new LinkedHashMap<String,Socket>();
	private LinkedHashMap<String,Socket> taskClientSockets = new LinkedHashMap<String,Socket>();
	private LinkedHashMap<String,Socket> rackClientSockets = new LinkedHashMap<String,Socket>();
	private LinkedHashMap<String,String> schedulerThread = new LinkedHashMap<String,String>();
	private LinkedHashMap<String,String> workingLines = new LinkedHashMap<String,String>();
	private SocketServer machineSS = null;
	private SocketServer robotSS = null;
	private SocketServer materialSS = null;
	private SocketServer taskSS = null;
	private SocketServer taskUpdateSS = null;
	private SocketServer rackSS = null;
	
	private static int PORTMACHINE = 0;
	private static int PORTROBOT = 0;
	private static int PORTMATERIAL = 0;
	private static int PORTTASK = 0;
	private static int PORTRACK = 0;
	private static int PORTTASKUPDATE = 0;
	
	private static LinkedHashMap<String,Boolean> bMachineDataSyn = new LinkedHashMap<String,Boolean>();
	private static LinkedHashMap<String,Boolean> bRobotDataSyn = new LinkedHashMap<String,Boolean>();
	private static LinkedHashMap<String,Boolean> bMaterialDataSyn = new LinkedHashMap<String,Boolean>();
	
	private static LinkedHashMap<String,Boolean> bMachineCmdOK = new LinkedHashMap<String,Boolean>();
	private static LinkedHashMap<String,Boolean> bRobotCmdOK = new LinkedHashMap<String,Boolean>();
	private static LinkedHashMap<String,Boolean> bMaterialCmdOK = new LinkedHashMap<String,Boolean>();
	private static LinkedHashMap<String,Boolean> bRackCmdOK = new LinkedHashMap<String,Boolean>();
	
	private static LinkedHashMap<String,String> scheduledMachine = new LinkedHashMap<String,String>();
	private static LinkedHashMap<String,String> scheduledRobot = new LinkedHashMap<String,String>();
	private static LinkedHashMap<String,String> devControlCenter = new LinkedHashMap<String,String>();
//	private static LinkedHashMap<ClientType,LinkedHashMap<String,String>> myClients = new LinkedHashMap<ClientType,LinkedHashMap<String,String>>();
	
	private static SchedulerTask schedulerTask = SchedulerTask.getInstance();
	private static SchedulerCfg scCfg = null;
	private static Scheduler schedulerGUI = Scheduler.getInstance();
	
	private SchedulerServer(){}
	static{
		scCfg = SchedulerCfg.getInstance();
		PORTMACHINE = scCfg.getPort(SchedulerItems.PORTMACHINE);
		PORTROBOT = scCfg.getPort(SchedulerItems.PORTROBOT);
		PORTMATERIAL = scCfg.getPort(SchedulerItems.PORTMATERIAL);
		PORTTASK = scCfg.getPort(SchedulerItems.PORTTASK);
		PORTTASKUPDATE = scCfg.getPort(SchedulerItems.PORTTASKUPDATE);
		PORTRACK = scCfg.getPort(SchedulerItems.PORTRACK);
	}
	
	public static SchedulerServer getInstance(){
		return schedulerSvr;
	}
	
	public String start(){
		String msg = "OK";
		msg = MySystemUtils.sysReadyToStart();
		if(!"OK".equals(msg)) return msg;
		
		machineSS = SocketServer.getInstance();
		try {
			machineSS.socketSvrStart(PORTMACHINE, new MachineMsgHandler());
		} catch (IOException e) {
			return "COM port("+PORTMACHINE+") for machine starts failed\r\n"+e.getMessage();
		}
		
		robotSS = SocketServer.getInstance();
		try {
			robotSS.socketSvrStart(PORTROBOT, new RobotMsgHandler());
		} catch (IOException e) {
			machineSS.stopSvrPort(PORTMACHINE);
			return "COM port("+PORTROBOT+") for robot starts failed\r\n"+e.getMessage();
		}
		
		materialSS = SocketServer.getInstance();
		try {
			materialSS.socketSvrStart(PORTMATERIAL, new MaterialMsgHandler());
		} catch (IOException e) {
			machineSS.stopSvrPort(PORTMACHINE);
			robotSS.stopSvrPort(PORTROBOT);
			return "COM port("+PORTMATERIAL+") for material starts failed\r\n"+e.getMessage();
		}
		
		taskSS = SocketServer.getInstance();
		try {
			taskSS.socketSvrStart(PORTTASK, new TaskMsgHandler());
		} catch (IOException e) {
			machineSS.stopSvrPort(PORTMACHINE);
			robotSS.stopSvrPort(PORTROBOT);
			materialSS.stopSvrPort(PORTMATERIAL);
			return "COM port("+PORTTASK+") for task starts failed\r\n"+e.getMessage();
		}
		
		rackSS = SocketServer.getInstance();
		try {
			rackSS.socketSvrStart(PORTRACK, new RackMsgHandler());
		} catch (IOException e) {
			machineSS.stopSvrPort(PORTMACHINE);
			robotSS.stopSvrPort(PORTROBOT);
			materialSS.stopSvrPort(PORTMATERIAL);
			taskSS.stopSvrPort(PORTTASK);
			return "COM port("+PORTRACK+") for rack starts failed\r\n"+e.getMessage();
		}
		
		taskUpdateSS = SocketServer.getInstance();
		try {
			taskUpdateSS.socketSvrStart(PORTTASKUPDATE, new TaskMsgHandler());
		} catch (IOException e) {
			machineSS.stopSvrPort(PORTMACHINE);
			robotSS.stopSvrPort(PORTROBOT);
			materialSS.stopSvrPort(PORTMATERIAL);
			taskSS.stopSvrPort(PORTTASK);
			rackSS.stopSvrPort(PORTRACK);
			return "COM port("+PORTTASKUPDATE+") for taskUpdate starts failed\r\n"+e.getMessage();
		}
		
		restoreControlCenterInfo(true, true);
		MySystemUtils.sysMemoryRestore("All", MemoryItems.MATERIAL, DeviceState.STANDBY, false);
		
		ThreadController.initStopFlag();
		if(schedulerThread.size() > 0){
			for(String lineName:schedulerThread.keySet()){
				if("".equals(schedulerThread.get(lineName))) startScheduler(lineName);
			}
		}
		
		return msg;
	}
	
	public void dumpControlCenterInfo(){
		String path = DataUtils.getMemoryDumpPath("ControlCenter");
		if(!"".equals(path)){
			if(devControlCenter.size() > 0){
				String content = "";
				JSONObject jsonObj = new JSONObject();
				for(String lineName:devControlCenter.keySet()){
					jsonObj.put("lineName", lineName);
					jsonObj.put("controlCenter", devControlCenter.get(lineName));
					if("".equals(content)){
						content = jsonObj.toString();
					}else{
						content += "\r\n" + jsonObj.toString();
					}
				}
				if(!"".equals(content)) MyFileUtils.saveToFile(content, path);
			}
		}
	}
	
	public void restoreControlCenterInfo(boolean informControlCenter, boolean threadMode){
		String path = DataUtils.getMemoryDumpPath("ControlCenter");
		if(!"".equals(path)){
			if(MyFileUtils.getFilePassedDays(path)<=0.5){
				try {
					String[] jsonStrs = FileUtils.readFileToString(new File(path), "UTF-8").split("\r\n");
					String[] ccCfg = null;
					String lineName = "", ccInfo = "";
					CtrlCenterClient ccClient = CtrlCenterClient.getInstance();
					for(String jsonStr:jsonStrs){
						JSONObject jsonObj = JSONObject.fromObject(jsonStr);
						lineName = jsonObj.getString("lineName");
						ccInfo = jsonObj.getString("controlCenter");
						devControlCenter.put(lineName, ccInfo);
						if(informControlCenter){
							ccCfg = jsonObj.getString("controlCenter").split(":");
							ccClient.informControlCenter("schedulerStarted", ccCfg[0], ccCfg[1], lineName, ccInfo, threadMode);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public String stop(){
		String msg = "OK";
		
		if(devControlCenter.size() > 0){
			String[] ccCfg = null;
			CtrlCenterClient ccClient = CtrlCenterClient.getInstance();
			try {
				for(String lineName:devControlCenter.keySet()){
					ccCfg = devControlCenter.get(lineName).split(":");
					ccClient.informControlCenter("schedulerStopped", ccCfg[0], ccCfg[1], lineName, ccCfg[0], false);
				}
			} catch (Exception e) {
			}
		}
		SocketClient.getInstance().disconnectAllSockets();
		if(null != machineSS) machineSS.stopSvrPort(PORTMACHINE);
		if(null != robotSS) robotSS.stopSvrPort(PORTROBOT);
		if(null != materialSS) materialSS.stopSvrPort(PORTMATERIAL);
		if(null != taskSS) taskSS.stopSvrPort(PORTTASK);
		if(null != taskUpdateSS) taskUpdateSS.stopSvrPort(PORTTASKUPDATE);
		if(null != rackSS) rackSS.stopSvrPort(PORTRACK);
		if(schedulerThread.size() > 0){
			for(String lineName:schedulerThread.keySet()){
				schedulerThread.put(lineName, "");
			}
		}
		
		ThreadController.stopScheduler();
		MySystemUtils.sysMemoryDump(MemoryItems.ALL);
		
		return msg;
	}
	
	public void startScheduler(String lineName){
		String scLine = schedulerThread.get(lineName);
		
		if(null != lineName && (null == scLine || null != scLine && "".equals(scLine))){
			ThreadUtils.Run(new SchedulerExecution(lineName));
		}
	}
	
	public void addScheduledMachine(String machineIP){
		scheduledMachine.put(machineIP, machineIP);
	}
	
	public void removeScheduledMachine(String machineIP){
		scheduledMachine.remove(machineIP);
	}
	
	public boolean machineIsScheduled(String machineIP){
		return (null==scheduledMachine.get(machineIP)?false:true);
	}
	
	public void addScheduledRobot(String robotIP){
		scheduledRobot.put(robotIP, robotIP);
	}
	
	public void removeScheduledRobot(String robotIP){
		scheduledRobot.remove(robotIP);
	}
	
	public boolean robotIsScheduled(String robotIP){
		return (null==scheduledRobot.get(robotIP)?false:true);
	}
	
	private boolean sendMsgToClient(Socket s,String feedback){
		boolean success = false;
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			bw.write(feedback + "\n");
			bw.flush();
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return success;
	}
	
	/**
	 * 
	 * @param data The material data for both Scheduler and Rack_Manager
	 * @param in
	 */
	private void setMaterialData(String[] data, String in){
		SchedulerMaterial sMaterial = SchedulerMaterial.getInstance();
		
		String lineName = data[1];
		bMaterialDataSyn.put(lineName, true);
		sMaterial.setData(data[0], SchedulerDataItems.LINENAME, lineName);
		
		Object oriState = sMaterial.getData(data[0]).get(SchedulerDataItems.STATE);
		DeviceState curState = DataUtils.getDevStateByString(data[2]);
		if(null == oriState || null != oriState && (DeviceState)oriState != curState){
			sMaterial.setData(data[0], SchedulerDataItems.STATE, curState);
		}
		
		sMaterial.setData(data[0], SchedulerDataItems.PROCESSQTY, data[3]);
		sMaterial.setData(data[0], SchedulerDataItems.PROCESSNAME, data[4]);
		sMaterial.setData(data[0], SchedulerDataItems.SURFACE, data[5]);
		sMaterial.setData(data[0], SchedulerDataItems.SIMTIME, data[6]);
		sMaterial.setData(data[0], SchedulerDataItems.NCMODEL, data[7]);
		sMaterial.setData(data[0], SchedulerDataItems.PROGRAM, data[8]);
		sMaterial.setData(data[0], SchedulerDataItems.RACKID, data[9]);
		sMaterial.setData(data[0], SchedulerDataItems.SLOTNO, data[10]);
		if("0".equals(data[11])) bMaterialDataSyn.put(lineName, false);
	}
	
	/**
	 * 
	 * @author Zilong Mainly for Scheduler to receive machine data from CNC_Client
	 *
	 */
	class MachineMsgHandler implements SocketRespHandler{
		@Override
		public void doHandle(String in, Socket s) {
			if(null == s || null == in) return;
			
			String[] data = in.split(",");
			if(data.length > 2){
				String sMD5 = data[data.length-1];
				String dt = in.substring(0, in.length()-sMD5.length()-1);
				
				if(sMD5.equals(MathUtils.MD5Encode(dt))){
					machineClientSockets.put(""+PORTMACHINE, s);
					if("setState".equals(data[0])){
						bMachineCmdOK.put(data[3], true); //Command from server side no need to feedback
					}else if("checkReady".equals(data[0])){
						sendMsgToClient(s,in);
					}else{
						sendMsgToClient(s,in);
						SchedulerMachine sMachine = SchedulerMachine.getInstance();
						
						String lineName = data[5];
						bMachineDataSyn.put(lineName, true);
						workingLines.put(lineName, lineName);
						sMachine.setData(data[0], SchedulerDataItems.LINENAME, lineName);
						sMachine.setData(data[0], SchedulerDataItems.IP, data[1]);
						sMachine.setData(data[0], SchedulerDataItems.PORT, data[2]);
						
						Object oriState = sMachine.getData(data[0]).get(SchedulerDataItems.STATE);
						DeviceState curState = DataUtils.getDevStateByString(data[3]);
						if(null == oriState || null != oriState && (DeviceState)oriState != curState){
							sMachine.setData(data[0], SchedulerDataItems.STATE, curState);
							if(DeviceState.STANDBY == curState) removeScheduledMachine(data[1]);
						}
						
						sMachine.setData(data[0], SchedulerDataItems.MODEL, data[4]);
						sMachine.setData(data[0], SchedulerDataItems.MACHINETIME, data[6]);
						sMachine.setData(data[0], SchedulerDataItems.WKZONEQTY, data[7]);
						sMachine.setData(data[0], SchedulerDataItems.PROGRESS, data[8]);
						
						sMachine.setData(data[0], SchedulerDataItems.SLOT1, data[9]);
						sMachine.setData(data[0], SchedulerDataItems.SLOT2, data[10]);
						sMachine.setData(data[0], SchedulerDataItems.SLOT3, data[11]);
						sMachine.setData(data[0], SchedulerDataItems.SLOT4, data[12]);
						sMachine.setData(data[0], SchedulerDataItems.SLOT5, data[13]);
						sMachine.setData(data[0], SchedulerDataItems.SLOT6, data[14]);
						sMachine.setData(data[0], SchedulerDataItems.WPIDS, data[15]);
						sMachine.setData(data[0], SchedulerDataItems.WPSLOTIDS, data[16]);
						sMachine.setData(data[0], SchedulerDataItems.WPSTATES, data[17]);
						sMachine.setData(data[0], SchedulerDataItems.CONTROLCENTER, data[18]+":"+data[19]);
						bMachineDataSyn.put(lineName, false);
						devControlCenter.put(lineName, data[18]+":"+data[19]+":OK"); //IP:Port
						startScheduler(lineName); //Start the resources scheduled once receive machines' info
						schedulerGUI.refreshGUI(0);
					}
				}
			}
		}
		
	}
	
	/**
	 * @author Zilong Mainly for Scheduler to receive robot data from CNC_Client
	 */
	class RobotMsgHandler implements SocketRespHandler{
		@Override
		public void doHandle(String in, Socket s) {
			if(null == s || null == in) return;
			
			String[] data = in.split(",");
			if(data.length > 2){
				String sMD5 = data[data.length-1];
				String dt = in.substring(0, in.length()-sMD5.length()-1);
				if(sMD5.equals(MathUtils.MD5Encode(dt))){
					robotClientSockets.put(""+PORTROBOT, s);
					if("setState".equals(data[0])){
						bRobotCmdOK.put(data[3], true);
					}else if("checkReady".equals(data[0])){
						sendMsgToClient(s,in);
					}else{
						sendMsgToClient(s,in);
						SchedulerRobot sRobot = SchedulerRobot.getInstance();
						
						String lineName = data[5];
						bRobotDataSyn.put(lineName, true);
						sRobot.setData(data[0], SchedulerDataItems.IP, data[1]);
						sRobot.setData(data[0], SchedulerDataItems.PORT, data[2]);
						
						Object oriState = sRobot.getData(data[0]).get(SchedulerDataItems.STATE);
						DeviceState curState = DataUtils.getDevStateByString(data[3]);
						if(null == oriState || null != oriState && (DeviceState)oriState != curState){
							sRobot.setData(data[0], SchedulerDataItems.STATE, curState);
							if(DeviceState.STANDBY == curState) removeScheduledRobot(data[1]);
						}
						
						sRobot.setData(data[0], SchedulerDataItems.MODEL, data[4]);
						sRobot.setData(data[0], SchedulerDataItems.LINENAME, lineName);
						sRobot.setData(data[0], SchedulerDataItems.POSITION, data[6]);
						sRobot.setData(data[0], SchedulerDataItems.CAPACITY, data[7]);
						sRobot.setData(data[0], SchedulerDataItems.SLOT1, data[8]);
						sRobot.setData(data[0], SchedulerDataItems.SLOT2, data[9]);
						sRobot.setData(data[0], SchedulerDataItems.SLOT3, data[10]);
						sRobot.setData(data[0], SchedulerDataItems.SLOT4, data[11]);
						sRobot.setData(data[0], SchedulerDataItems.SLOT5, data[12]);
						sRobot.setData(data[0], SchedulerDataItems.SLOT6, data[13]);
						sRobot.setData(data[0], SchedulerDataItems.SLOT7, data[14]);
						sRobot.setData(data[0], SchedulerDataItems.SLOT8, data[15]);
						sRobot.setData(data[0], SchedulerDataItems.SLOT9, data[16]);
						sRobot.setData(data[0], SchedulerDataItems.GRIPMATERIAL, data[17]);
						sRobot.setData(data[0], SchedulerDataItems.WPIDS, data[18]);
						sRobot.setData(data[0], SchedulerDataItems.WPSLOTIDS, data[19]);
						sRobot.setData(data[0], SchedulerDataItems.WPSTATES, data[20]);
						sRobot.setData(data[0], SchedulerDataItems.CONTROLCENTER, data[21]+":"+data[22]);
						sRobot.setData(data[0], SchedulerDataItems.BATTERY, data[23]);
						bRobotDataSyn.put(lineName, false);
						devControlCenter.put(lineName, data[21]+":"+data[22]+":OK"); //IP:Port
						schedulerGUI.refreshGUI(1);
					}
				}
			}
		}
		
	}
	
	/**
	 * 
	 * @author Zilong Mainly for Scheduler to receive material data from CNC_Client
	 *
	 */
	class MaterialMsgHandler implements SocketRespHandler{
		@Override
		public void doHandle(String in, Socket s) {
			if(null == s || null == in) return;
			
			String[] data = in.split(",");
			if(data.length > 2){
				String sMD5 = data[data.length-1];
				String dt = in.substring(0, in.length()-sMD5.length()-1);
				if(sMD5.equals(MathUtils.MD5Encode(dt))){
					materialClientSockets.put(""+PORTMATERIAL, s);
					if("setState".equals(data[0])){
						bMaterialCmdOK.put(data[3], true);
					}else if("checkReady".equals(data[0])){
						sendMsgToClient(s,in);
					}else if("removeMaterial".equals(data[0])){
						sendMsgToClient(s,in);
						SchedulerMaterial sMaterial = SchedulerMaterial.getInstance();
						sMaterial.removeData(data[1]);
						schedulerGUI.refreshGUI(2);
						schedulerGUI.refreshGUI(3);
					}else{
						RunningMsg.set(in);
						sendMsgToClient(s,in);
						setMaterialData(data,in);
						schedulerGUI.refreshGUI(2);
						schedulerGUI.refreshGUI(3);
						schedulerGUI.refreshGUI(5);
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @author Zilong Mainly for Scheduler to receive material data from Rack_Manager
	 *
	 */
	class RackMsgHandler implements SocketRespHandler{
		@Override
		public void doHandle(String in, Socket s) {
			if(null == s || null == in) return;
			
			String[] data = in.split(",");
			if(data.length > 2){
				String sMD5 = data[data.length-1];
				String dt = in.substring(0, in.length()-sMD5.length()-1);
				if(sMD5.equals(MathUtils.MD5Encode(dt))){
					rackClientSockets.put(""+PORTRACK, s);
					if("setState".equals(data[0])){
						bRackCmdOK.put(data[3], true);
					}else if("checkReady".equals(data[0])){
						sendMsgToClient(s,in);
					}else{
						RunningMsg.set(in);
						sendMsgToClient(s,in);
						setMaterialData(data,in);
						schedulerGUI.refreshGUI(2);
						schedulerGUI.refreshGUI(5);
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @author Zilong Mainly for Scheduler to receive requests from CNC_Client and Device Diagnostic Client
	 *
	 */
	class TaskMsgHandler implements SocketRespHandler{
		@Override
		public void doHandle(String in, Socket s) {
			if(null == s || null == in) return;
			String[] data = in.split(",");
			if(data.length > 2){
				String sMD5 = data[data.length-1];
				String dt = in.substring(0, in.length()-sMD5.length()-1);
				if(sMD5.equals(MathUtils.MD5Encode(dt))){
					taskClientSockets.put(""+PORTTASK, s);
					if("checkReady".equals(data[0])){
						sendMsgToClient(s,in);
					}else if("pauseSchedulerForLine".equals(data[0])){
						sendMsgToClient(s,in);
						String ccInfo = devControlCenter.get(data[1]);
						if(null!=ccInfo && ccInfo.indexOf(":OK")>=0){
							ccInfo = ccInfo.replace(":OK", ":NG");
							devControlCenter.put(data[1], ccInfo);
						}
					}else if("resumeSchedulerForLine".equals(data[0])){
						sendMsgToClient(s,in);
						String ccInfo = devControlCenter.get(data[1]);
						if(null!=ccInfo && ccInfo.indexOf(":NG")>=0){
							ccInfo = ccInfo.replace(":NG", ":OK");
							devControlCenter.put(data[1], ccInfo);
						}
					}else if("getTask".equals(data[0])){
						String taskInfo = schedulerTask.getTaskInfoByLineName(data[1]);
						if(!"".equals(taskInfo)){
							String cmd = data[0]+","+taskInfo;
							cmd += ","+MathUtils.MD5Encode(cmd);
							sendMsgToClient(s,cmd);
						}else{
							sendMsgToClient(s,in);
						}
					}else if("removeTask".equals(data[0])){
						String taskInfo = (String) schedulerTask.getData(data[2]).get(data[1]);
						if(null != taskInfo){
							JSONObject jsonObj = JSONObject.fromObject(taskInfo);
							if(null != jsonObj){
								removeScheduledMachine(jsonObj.getString("machineIP"));
								removeScheduledRobot(jsonObj.getString("robotIP"));
								schedulerTask.removeTaskInfo(data[2], data[1]);
								if(sendMsgToClient(s,in)){
									RunningMsg.set("removeTask:"+jsonObj.getString("robotIP")+"->"+jsonObj.getString("machineIP")+" OK");
								}else{
									RunningMsg.set("removeTask:"+jsonObj.getString("robotIP")+"->"+jsonObj.getString("machineIP")+" NG");
								}
							}
						}
					}else if("updateTask".equals(data[0])){
						sendMsgToClient(s,in);//Must put here at the very beginning to response to client in time
						String taskInfo = in.replace(data[0]+",", "").replace(","+sMD5, "");
						try {
							JSONObject jsonObj = JSONObject.fromObject(taskInfo);
							String lineName = jsonObj.getString("lineName");
							String taskID = jsonObj.getString("taskID");
							String machineIP = jsonObj.getString("machineIP");
							String robotIP = jsonObj.getString("robotIP");
							DeviceState taskState = DataUtils.getDevStateByString(jsonObj.getString("taskState"));
							DeviceState machineState = DataUtils.getDevStateByString(jsonObj.getString("machineState"));
							DeviceState robotState = DataUtils.getDevStateByString(jsonObj.getString("robotState"));
							String machineKey = jsonObj.getString("machineKey");
							String robotKey = jsonObj.getString("robotKey");
							String ccInfo = devControlCenter.get(lineName);
							String ccIP = "", ccPort = "0";
							if(null != ccInfo && ccInfo.indexOf(":") >= 0){
								String[] cc = ccInfo.split(":");
								ccIP = cc[0];
								ccPort = cc[1];
							}
							if(DeviceState.ALARMING == taskState){
								if(DeviceState.STANDBY == robotState) removeScheduledRobot(robotIP);
								if(DeviceState.STANDBY == machineState || DeviceState.FINISH == machineState) removeScheduledMachine(machineIP);
							}
							RunningMsg.set(data[0]+":"+taskID);
							if(DeviceState.FIXED == taskState){//Update the alarming task
								TaskData taskData = TaskData.getInstance();
								SchedulerMachine sMachine = SchedulerMachine.getInstance();
								SchedulerRobot sRobot = SchedulerRobot.getInstance();
								CtrlCenterClient ctrlCC = CtrlCenterClient.getInstance();
								
								if(DeviceState.FINISH == machineState){
									if(!taskData.deviceIsPlannedForOtherTask(taskID, machineIP, TaskItems.MACHINEIP)){
										removeScheduledMachine(machineIP);
										sMachine.setMachineState(machineKey, DeviceState.STANDBY);
										ctrlCC.setObjectState("setMachineState", ccIP, ccPort, machineIP, DeviceState.STANDBY, true);
										schedulerGUI.refreshGUI(0);
									}
									if(!taskData.deviceIsPlannedForOtherTask(taskID, robotIP, TaskItems.ROBOTIP)){
										removeScheduledRobot(robotIP);
										sRobot.setRobotState(robotKey, DeviceState.STANDBY);
										ctrlCC.setObjectState("setRobotState", ccIP, ccPort, robotIP, DeviceState.STANDBY, true);
										schedulerGUI.refreshGUI(1);
									}
									schedulerTask.removeTaskInfo(lineName, taskID);
									ctrlCC.informControlCenter("removeTask", ccIP, ccPort, taskID, "", true);
								}else{
									if(!taskData.deviceIsPlannedForOtherTask(taskID, machineIP, TaskItems.MACHINEIP)){
										sMachine.setMachineState(machineKey, DeviceState.WORKING);
										ctrlCC.setObjectState("setMachineState", ccIP, ccPort, machineIP, DeviceState.WORKING, true);
										schedulerGUI.refreshGUI(0);
									}
									if(!taskData.deviceIsPlannedForOtherTask(taskID, robotIP, TaskItems.ROBOTIP)){
										sRobot.setRobotState(robotKey, DeviceState.STANDBY);
										ctrlCC.setObjectState("setRobotState", ccIP, ccPort, robotIP, DeviceState.STANDBY, true);
										schedulerGUI.refreshGUI(1);
									}
									jsonObj.put("taskState", ""+DeviceState.WORKING);
									schedulerTask.updateTaskInfo(lineName, taskID, jsonObj.toString());
									ctrlCC.informControlCenter("updateAlarmTask", ccIP, ccPort, taskID, machineIP, true);
								}
							}else{
								schedulerTask.updateTaskInfo(lineName, taskID, taskInfo);
							}
						} catch (Exception e) {
						}
					}else if("getAlarmTask".equals(data[0])){
						String taskInfo = schedulerTask.getAlarmTask();
						if(!"".equals(taskInfo)){
							String cmd = data[0]+","+taskInfo;
							cmd += ","+MathUtils.MD5Encode(cmd);
							sendMsgToClient(s,cmd);
						}else{
							sendMsgToClient(s,in);
						}
					}else if("getTaskInfo".equals(data[0])){
						String lineName = data[1];
						String taskID = data[2];
						String taskInfo = schedulerTask.getTaskInfo(lineName, taskID);
						String cmd = data[0]+","+taskInfo;
						cmd += ","+MathUtils.MD5Encode(cmd);
						sendMsgToClient(s,cmd);
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @author Zilong The resources scheduled thread for each production line
	 *
	 */
	class SchedulerExecution implements Runnable{
		private String lineName = "";
		public SchedulerExecution(String lineName){
			this.lineName = lineName;
		}
		
		@Override
		public void run() {
			RunningMsg.set("Scheduler for "+lineName+" start");
			schedulerThread.put(lineName, lineName);
			String taskID = "", taskInfo = "";
			String robotIP = "", robotKey = "", machineIP = "", logContPre = "", logFile = "";
			String cncModel = "", wpIDs = "",rackIDs = "",slotIDs = "",wpStates = "";
			String ncModels = "", rackID = "", slotID = "", controlCenter = "";
			DeviceState robotState = null;
			String[] trayEmptySlots = null;
			String[] emptyZones = null;
			DeviceState machineState = null;
			
			int zonesQty = 0, workzoneQTY = 0, materialQTY = 0, qty = 0;
			
			boolean machineDataSyn = false;
			boolean robotDataSyn = false;
			boolean materialDataSyn = false;
			boolean standbyTaskAsigned = false;
			
			LinkedHashMap<String,LinkedHashMap<SchedulerDataItems,Object>> machineList = new LinkedHashMap<String,LinkedHashMap<SchedulerDataItems,Object>>();
			LinkedHashMap<String,LinkedHashMap<SchedulerDataItems,Object>> robotList = new LinkedHashMap<String,LinkedHashMap<SchedulerDataItems,Object>>();
			LinkedHashMap<String,LinkedHashMap<SchedulerDataItems,Object>> materialList = new LinkedHashMap<String,LinkedHashMap<SchedulerDataItems,Object>>();
			
			SchedulerMachine machine = SchedulerMachine.getInstance();
			SchedulerRobot robot = SchedulerRobot.getInstance();
			SchedulerMaterial material = SchedulerMaterial.getInstance();
			
			while(!ThreadController.getSchedulerStopFlag()){
				try {
					schedulerGUI.refreshGUI(5);
					
					controlCenter = devControlCenter.get(lineName);
					if(null!=controlCenter && controlCenter.endsWith(":NG")){//Control Center is stopped
						Thread.sleep(5000);
						continue;
					}
					
					machineDataSyn = false;
					robotDataSyn = false;
					materialDataSyn = false;
					if(null != bMachineDataSyn.get(lineName)) machineDataSyn = bMachineDataSyn.get(lineName);
					if(null != bRobotDataSyn.get(lineName)) robotDataSyn = bRobotDataSyn.get(lineName);
					if(null != bMaterialDataSyn.get(lineName)) materialDataSyn = bMaterialDataSyn.get(lineName);
					if(machineDataSyn || robotDataSyn || materialDataSyn){
						RunningMsg.set("Line "+lineName+" is waiting for resources input");
						Thread.sleep(1000);
						continue;
					}
					machine.getMachineList(lineName, machineList);
					robot.getRobotList(lineName, robotList);
					material.getMaterialList(lineName, materialList);
					
					if(machineList.size() > 0 && robotList.size() > 0){
						//Search the Standby and not-scheduled Robot
						for(String rKey:robotList.keySet()){
							robotIP = (String) robot.getData(rKey).get(SchedulerDataItems.IP);
							robotState = robot.getRobotState(rKey);
							robotKey = rKey;
							trayEmptySlots = robot.getEmptySlots(robotKey);
							if(DeviceState.STANDBY == robotState && !robotIsScheduled(robotIP)) break;
						}
						if(DeviceState.STANDBY != robotState || robotIsScheduled(robotIP)){
							RunningMsg.set("Robot "+robotIP+" of "+lineName+" is "+robotState);
							Thread.sleep(1000);
							continue;
						}
						if(null == trayEmptySlots){//the Standby Robot is already full loading
							RunningMsg.set("Robot "+robotIP+" of "+lineName+" is waiting for tray material unloading");
							robot.setRobotState(robotKey, DeviceState.WAITUL);
							Thread.sleep(1000);
							continue;
						}
						
						//Serve the STANDBY machine first
						standbyTaskAsigned = false;
						if(materialList.size() > 0){
							for(String machineKey:machineList.keySet()){
								machineState = (DeviceState) machine.getData(machineKey).get(SchedulerDataItems.STATE);
								machineIP = (String) machine.getData(machineKey).get(SchedulerDataItems.IP);
								if(machineIsScheduled(machineIP)) continue;//Machine has been scheduled
								
								if(DeviceState.STANDBY == machineState){
									emptyZones = machine.getEmptyWorkzones(machineKey);
									zonesQty = machine.getWorkzonesQty(machineKey);
									if(null == emptyZones || emptyZones.length != zonesQty){
										//There is material in the working zone of the machine
										machine.setMachineState(machineKey, DeviceState.WAITUL);
										continue;
									}
									
									workzoneQTY = emptyZones.length;
									materialQTY = materialList.size();
									qty = (workzoneQTY > materialQTY)?materialQTY:workzoneQTY;
									qty = (qty > trayEmptySlots.length)?trayEmptySlots.length:qty;
									cncModel = (String) machine.getData(machineKey).get(SchedulerDataItems.MODEL);
									
									wpIDs = "";rackIDs = "";slotIDs = "";wpStates = "";
									int i = -1;
									for(String workpieceID:materialList.keySet()){
										ncModels = (String) materialList.get(workpieceID).get(SchedulerDataItems.NCMODEL);
										rackID = (String) materialList.get(workpieceID).get(SchedulerDataItems.RACKID);
										slotID = (String) materialList.get(workpieceID).get(SchedulerDataItems.SLOTNO);
										if(ncModels.indexOf(cncModel) >= 0){//Workpiece can be machined by the CNC
											i++;
											if(i >= qty) break;
											material.setMaterialState(workpieceID, DeviceState.PLAN);
											if("".equals(wpIDs)){
												wpIDs = workpieceID;
												rackIDs = rackID;
												slotIDs = slotID;
												wpStates = ""+DeviceState.PLAN;
											}else{
												wpIDs += ";" + workpieceID;
												rackIDs += ";" + rackID;
												slotIDs += ";" + slotID;
												wpStates += ";"+DeviceState.PLAN;
											}
											standbyTaskAsigned = true;
										}
									}
									if(standbyTaskAsigned){
										robot.setRobotState(robotKey, DeviceState.PLAN);
										machine.setMachineState(machineKey, DeviceState.PLAN);
										
										taskInfo = machineIP+":"+machineState+":"+robotIP+":"+wpIDs+":"+rackIDs+":"+slotIDs+System.currentTimeMillis();
										taskID = MathUtils.MD5Encode(taskInfo);
										
										JSONObject jsonObj = new JSONObject();
										jsonObj.put("machineIP", machineIP);
										jsonObj.put("machineKey", machineKey);
										jsonObj.put("machineState", ""+machineState);
										jsonObj.put("robotIP", robotIP);
										jsonObj.put("robotKey", robotKey);
										jsonObj.put("robotState", ""+robotState);
										jsonObj.put("workpieceIDs", wpIDs);
										jsonObj.put("rackIDs", rackIDs);
										jsonObj.put("rackSlotIDs", slotIDs);
										jsonObj.put("materialStates", wpStates);
										jsonObj.put("taskID", taskID);
										jsonObj.put("lineName", lineName);
										jsonObj.put("controlCenter", ""+machine.getData(machineKey).get(SchedulerDataItems.CONTROLCENTER));
										jsonObj.put("taskState", ""+DeviceState.STANDBY);
										taskInfo = jsonObj.toString();
										
										schedulerTask.addTaskInfo(lineName, taskID, taskInfo);
										addScheduledMachine(machineIP);
										addScheduledRobot(robotIP);
										RunningMsg.set("Task "+robotIP+"->"+machineIP+"(loading) is ready");
										
										logContPre = lineName+LogUtils.separator+robotIP+"->"+machineIP+LogUtils.separator;
										logFile = "Scheduler_"+TimeUtils.getCurrentDate("yyyyMMdd")+".log";
										LogUtils.operationLog(logFile, logContPre+taskID+LogUtils.separator+taskInfo);
										break;
									}
								}
							}
						}
						if(standbyTaskAsigned){
							Thread.sleep(1000);
							continue; //To refresh the memory for new machine&material scheduling
						}
						
						//Serve the finish-machining targets
						for(String machineKey:machineList.keySet()){
							machineState = (DeviceState) machine.getData(machineKey).get(SchedulerDataItems.STATE);
							machineIP = (String) machine.getData(machineKey).get(SchedulerDataItems.IP);
							if(machineIsScheduled(machineIP)) continue;//Machine has been scheduled
							
							if(DeviceState.FINISH == machineState){
								robot.setRobotState(robotKey, DeviceState.PLAN);
								machine.setMachineState(machineKey, DeviceState.PLAN);
								
								taskInfo = machineIP+":"+machineState+":"+robotIP+System.currentTimeMillis();
								taskID = MathUtils.MD5Encode(taskInfo);
								
								JSONObject jsonObj = new JSONObject();
								jsonObj.put("machineIP", machineIP);
								jsonObj.put("machineKey", machineKey);
								jsonObj.put("machineState", ""+machineState);
								jsonObj.put("robotIP", robotIP);
								jsonObj.put("robotKey", robotKey);
								jsonObj.put("robotState", ""+robotState);
								jsonObj.put("taskID", taskID);
								jsonObj.put("taskState", ""+DeviceState.STANDBY);
								jsonObj.put("lineName", lineName);
								jsonObj.put("controlCenter", ""+machine.getData(machineKey).get(SchedulerDataItems.CONTROLCENTER));
								taskInfo = jsonObj.toString();
								
								schedulerTask.addTaskInfo(lineName, taskID, taskInfo);
								addScheduledMachine(machineIP);
								addScheduledRobot(robotIP);
								RunningMsg.set("Task "+robotIP+"->"+machineIP+"(unloading) is ready");
								
								logContPre = lineName+LogUtils.separator+robotIP+"->"+machineIP+LogUtils.separator;
								logFile = "Scheduler_"+TimeUtils.getCurrentDate("yyyyMMdd")+".log";
								LogUtils.operationLog(logFile, logContPre+taskID+LogUtils.separator+taskInfo);
								break;
							}
						}
						Thread.sleep(1000);
					}else{
						Thread.sleep(1000);
					}
				} catch (Exception e) {
					LogUtils.errorLog("Scheduler for "+lineName+" error:"+e.getMessage());
					RunningMsg.set("Scheduler for "+lineName+" error:"+e.getMessage());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
				}
			}
			
			schedulerThread.remove(lineName);
			RunningMsg.set("Scheduler for "+lineName+" stops");
		}
		
	}
}
