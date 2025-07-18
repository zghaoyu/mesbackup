package com.cncmes.ctrl;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cncmes.base.DeviceObserver;
import com.cncmes.base.DeviceState;
import com.cncmes.base.DeviceSubject;
import com.cncmes.base.SchedulerItems;
import com.cncmes.base.TaskItems;
import com.cncmes.data.CncData;
import com.cncmes.data.RobotData;
import com.cncmes.data.SchedulerCfg;
import com.cncmes.data.SocketData;
import com.cncmes.data.TaskData;
import com.cncmes.data.WorkpieceData;
import com.cncmes.handler.SocketDataHandler;
import com.cncmes.handler.impl.CncClientDataHandler;
import com.cncmes.utils.DataUtils;
import com.cncmes.utils.MathUtils;
import com.cncmes.utils.RunningMsg;
import com.cncmes.utils.ThreadUtils;

import net.sf.json.JSONObject;

/**
 * For CNC Client(Control Center) to Communicate With the Scheduler
 * @author LI ZI LONG
 *
 */
public class SchedulerClient implements DeviceObserver {
//	private Map<String,Object> socketRespData= new LinkedHashMap<String,Object>();
	private Map<Socket,String> socketRespData= new ConcurrentHashMap<>();//add by Hui Zhi  2022/6/2
	private LinkedHashMap<String,String> portMap = new LinkedHashMap<String,String>();
	private LinkedHashMap<String,Boolean> cmdFlag = new LinkedHashMap<String,Boolean>();
	
	private static LinkedHashMap<String,String> scheduledMachine = new LinkedHashMap<String,String>();
	private static LinkedHashMap<String,String> scheduledRobot = new LinkedHashMap<String,String>();
	
	private int cmdRetryCount = 5;
	private int socketRespTimeoutIter = 20;//20 means 2 seconds
	
	private static SchedulerClient sClient = new SchedulerClient();
	private SchedulerClient(){}
	static{
		CncData.getInstance().registerObserver(sClient);
		RobotData.getInstance().registerObserver(sClient);
		WorkpieceData.getInstance().registerObserver(sClient);
		TaskData.getInstance().registerObserver(sClient);
	}
	
	public static SchedulerClient getInstance(){
		return sClient;
	}
	
	public static void addScheduledMachine(String machineIP){
		scheduledMachine.put(machineIP, machineIP);
	}
	
	public static void removeScheduledMachine(String machineIP){
		scheduledMachine.remove(machineIP);
	}
	
	public static boolean machineIsScheduled(String machineIP){
		return (null==scheduledMachine.get(machineIP)?false:true);
	}
	
	public static void addScheduledRobot(String robotIP){
		scheduledRobot.put(robotIP, robotIP);
	}
	
	public static void removeScheduledRobot(String robotIP){
		scheduledRobot.remove(robotIP);
	}
	
	public static boolean robotIsScheduled(String robotIP){
		return (null==scheduledRobot.get(robotIP)?false:true);
	}
	
	@Override
	public void update(DeviceSubject subject, String data, boolean threadMode, boolean threadSequential, boolean theLastThread) {
		if("".equals(data)) return;
		SchedulerItems portType = SchedulerItems.PORTMACHINE;
		String devIP = "";
		
		if(subject instanceof CncData){
			portType = SchedulerItems.PORTMACHINE;
			devIP = data.split(",")[1];
			CncData cncData = (CncData) subject;
			if(DeviceState.STANDBY == cncData.getCncState(devIP) || DeviceState.PREPAREFINISH == cncData.getCncState(devIP)) removeScheduledMachine(devIP);
		}else if(subject instanceof RobotData){
			portType = SchedulerItems.PORTROBOT;
			devIP = data.split(",")[1];
			RobotData robotData = (RobotData) subject;
			if(DeviceState.STANDBY == robotData.getRobotState(devIP)) removeScheduledRobot(devIP);
		}else if(subject instanceof WorkpieceData){
			portType = SchedulerItems.PORTMATERIAL;
		}else if(subject instanceof TaskData){
			portType = SchedulerItems.PORTTASKUPDATE;
		}else{
			return;
		}
		
		if(threadMode){
			if(threadSequential){
				ThreadUtils.sequentialRun(new sendMsgToServer(portType,data,""), theLastThread);
			}else{
				ThreadUtils.Run(new sendMsgToServer(portType,data,""));
			}
		}else{
			String[] scheduler = getScheduler(portType);
			sendInfo(scheduler[0],Integer.parseInt(scheduler[1]),data);
		}
	}
	
	private boolean checkReady(SchedulerItems portType){
		String info = "checkReady,"+portType;
		info += "," + CncClientDataHandler.getIP();
		info += "," + CncClientDataHandler.getPort();
		info += "," + MathUtils.MD5Encode(info);
		
		String signature = MathUtils.MD5Encode(info);
		cmdFlag.put(signature, false);
		ThreadUtils.Run(new sendMsgToServer(portType,info,signature));
		int retryCount = socketRespTimeoutIter * cmdRetryCount;
		boolean ready = false;
		while(retryCount > 0){
			retryCount--;
			ready = cmdFlag.get(signature);
			if(ready) break;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		
		return ready;
	}
	
	/**
	 * 
	 * @param pType port type
	 * @return true while the Scheduler is ready
	 */
	public boolean schedulerServerIsReady(SchedulerItems...pType){
		boolean ready = true;
		int confirmTimes = 3;
		
		if(null != pType && pType.length > 0){
			for(SchedulerItems portType:pType){
				for(int i=0; i<confirmTimes; i++){
					ready = checkReady(portType);
					if(!ready) break;
				}
				
				if(!ready){
					if(portType == SchedulerItems.PORTMACHINE) RunningMsg.set("Scheduler for Machine State monitoring is not ready");
					if(portType == SchedulerItems.PORTROBOT) RunningMsg.set("Scheduler for Robot State monitoring is not ready");
					if(portType == SchedulerItems.PORTMATERIAL) RunningMsg.set("Scheduler for Material State monitoring is not ready");
					if(portType == SchedulerItems.PORTTASK) RunningMsg.set("Scheduler for Task Set is not ready");
					if(portType == SchedulerItems.PORTTASKUPDATE) RunningMsg.set("Scheduler for Task Updated is not ready");
					if(portType == SchedulerItems.PORTRACK) RunningMsg.set("Scheduler for Rack Manager is not ready");
				}
			}
		}else{
			RunningMsg.set("Scheduler ready check error");
			ready = false;
		}
		
		return ready;
	}
	
	/**
	 * 
	 * @param machineIP the IP address of the CNC
	 * @return true while CNC_Client successfully sends machine data to the Scheduler
	 */
	public boolean updateMachineInfo(String machineIP, boolean threadMode){
		boolean success = true;
		String info = "";
		String result = "";
		
		CncData cncData = CncData.getInstance();
		info = cncData.getDataForObservers(machineIP);
		
		if(!"".equals(info)){
			if(threadMode){
				ThreadUtils.Run(new sendMsgToServer(SchedulerItems.PORTMACHINE,info,""));
			}else{
				String[] scheduler = getScheduler(SchedulerItems.PORTMACHINE);
				result = sendInfo(scheduler[0],Integer.parseInt(scheduler[1]),info);
				if (null == result || result.equals("")) {
					success = false;
				}
			}
		}
		if(DeviceState.STANDBY == cncData.getCncState(machineIP)) removeScheduledMachine(machineIP);
		
		return success;
	}
	
	/**
	 * 
	 * @param robotIP the IP address of the Robot
	 * @return true while CNC_Client successfully sends robot data to the Scheduler
	 */
	public boolean updateRobotInfo(String robotIP, boolean threadMode){
		boolean success = true;
		String info = "";
		String result;
		
		RobotData robotData = RobotData.getInstance();
		info = robotData.getDataForObservers(robotIP);
		
		if(!"".equals(info)){
			if(threadMode){
				ThreadUtils.Run(new sendMsgToServer(SchedulerItems.PORTROBOT,info,""));
			}else{
				String[] scheduler = getScheduler(SchedulerItems.PORTROBOT);
				result = sendInfo(scheduler[0],Integer.parseInt(scheduler[1]),info);
				if (null == result || result.equals("")) {
					success = false;
				}
			}
		}
		if(DeviceState.STANDBY == robotData.getRobotState(robotIP)) removeScheduledRobot(robotIP);
		
		return success;
	}
	
	/**
	 * 
	 * @param materialID the material ID
	 * @param portType the scheduler port type
	 * @param batchUpdate true to do batch update
	 * @return true while CNC_Client/Rack_Manager successfully sends material data to the Scheduler
	 */
	public boolean updateMaterialInfo(String materialID,SchedulerItems portType,boolean batchUpdate,boolean threadMode){
		boolean success = true;
		String info = "";
		String result = "";
		
		WorkpieceData wpData = WorkpieceData.getInstance();
		info = wpData.getDataForObservers(materialID, batchUpdate);
		
		if(!"".equals(info)){
			if(threadMode){
				ThreadUtils.Run(new sendMsgToServer(portType,info,""));
			}else{
				String[] scheduler = getScheduler(portType==SchedulerItems.PORTMATERIAL?SchedulerItems.PORTMATERIAL:SchedulerItems.PORTRACK);
				result = sendInfo(scheduler[0],Integer.parseInt(scheduler[1]),info);
				if (null == result || result.equals("")) {
					success = false;
				}
			}
		}
		
		return success;
	}
	
	public boolean getTask(String lineName){
		boolean success = true;
		String result = "";
		String info = "getTask,"+lineName;
		info += "," + MathUtils.MD5Encode(info);
		
		String[] scheduler = getScheduler(SchedulerItems.PORTTASK);
		result = sendInfo(scheduler[0],Integer.parseInt(scheduler[1]),info);
//		System.out.println("at SchedulerClient.getTask: "+scheduler);
		if (null == result || result.equals("")) {
			success = false;
		}
		return success;
	}

	public Boolean getNextTask(String lineName,String taskID){
		Boolean success = true;
		String result = "";
		String info = "getNextTask,"+lineName+taskID;
		info += "," + MathUtils.MD5Encode(info);

		String[] scheduler = getScheduler(SchedulerItems.PORTTASK);
		result = sendInfo(scheduler[0],Integer.parseInt(scheduler[1]),info);
		if(null == result || result.equals("")){
			success = false;
		}
		return success;
	}
	
	public String getTaskInfo(String lineName, String taskID){
		String result = "";
		String info = "getTaskInfo,"+lineName+","+taskID;
		info += "," + MathUtils.MD5Encode(info);
		
		String[] scheduler = getScheduler(SchedulerItems.PORTTASK);
		result = sendInfo(scheduler[0],Integer.parseInt(scheduler[1]),info);
		return result;
	}
	
	public boolean getAlarmTask(){
		boolean success = true;
		String result = "";
		String info = "getAlarmTask,1";
		info += "," + MathUtils.MD5Encode(info);
		
		String[] scheduler = getScheduler(SchedulerItems.PORTTASK);
		result = sendInfo(scheduler[0],Integer.parseInt(scheduler[1]),info);
		if (null == result || result.equals("")) {
			success = false;
		}
		return success;
	}
	
	public boolean pauseSchedulerForLine(String lineName){
		boolean success = true;
		String result = "";
		String info = "pauseSchedulerForLine,"+lineName;
		info += "," + MathUtils.MD5Encode(info);
		
		String[] scheduler = getScheduler(SchedulerItems.PORTTASK);
		result = sendInfo(scheduler[0],Integer.parseInt(scheduler[1]),info);
		if (null == result || result.equals("")) {
			success = false;
		}
		return success;
	}
	
	public boolean resumeSchedulerForLine(String lineName){
		boolean success = true;
		String result = "";
		String info = "resumeSchedulerForLine,"+lineName;
		info += "," + MathUtils.MD5Encode(info);
		System.out.println("resumeSchedulerForLine : "+info);
		String[] scheduler = getScheduler(SchedulerItems.PORTTASK);
//		System.out.println("at resumeSchedulerForLine:"+ Arrays.deepToString(scheduler));
		result = sendInfo(scheduler[0],Integer.parseInt(scheduler[1]),info);
		if (null == result || result.equals("")) {
			success = false;
		}
		return success;
	}
	
	public boolean removeTask(String lineName,String curTaskID,boolean threadMode){
		boolean success = true;
		String result = "";
		String info = "removeTask,"+curTaskID+","+lineName;
		info += "," + MathUtils.MD5Encode(info);
		
		if(threadMode){
			ThreadUtils.Run(new sendMsgToServer(SchedulerItems.PORTTASK,info,""));
		}else{
			String[] scheduler = getScheduler(SchedulerItems.PORTTASK);
			
			result = sendInfo(scheduler[0],Integer.parseInt(scheduler[1]),info);
			if (null == result || result.equals("")) {
				success = false;
			}
			String cncIP = (String) TaskData.getInstance().getTaskByID(curTaskID).get(TaskItems.MACHINEIP);
			String robotIP = (String) TaskData.getInstance().getTaskByID(curTaskID).get(TaskItems.ROBOTIP);
			removeScheduledMachine(cncIP);
			removeScheduledRobot(robotIP);
		}
		
		return success;
	}
	
	private String[] getScheduler(SchedulerItems portType){
		String[] scheduler = new String[]{"","0"};
		
		Map<String,LinkedHashMap<SchedulerItems,Object>> sCfg = SchedulerCfg.getInstance().getDataMap();
		if(sCfg.size() > 0){
			for(String ip:sCfg.keySet()){
				LinkedHashMap<SchedulerItems,Object> s = sCfg.get(ip);
				if(null != s){
					scheduler[0] = ip;
					if(portType == SchedulerItems.PORTMACHINE) scheduler[1] = (String) s.get(SchedulerItems.PORTMACHINE);
					if(portType == SchedulerItems.PORTROBOT) scheduler[1] = (String) s.get(SchedulerItems.PORTROBOT);
					if(portType == SchedulerItems.PORTMATERIAL) scheduler[1] = (String) s.get(SchedulerItems.PORTMATERIAL);
					if(portType == SchedulerItems.PORTTASK) scheduler[1] = (String) s.get(SchedulerItems.PORTTASK);
					if(portType == SchedulerItems.PORTTASKUPDATE) scheduler[1] = (String) s.get(SchedulerItems.PORTTASKUPDATE);
					if(portType == SchedulerItems.PORTRACK) scheduler[1] = (String) s.get(SchedulerItems.PORTRACK);
				}
			}
		}

		
		return scheduler;
	}
	
	/**
	 * Change by Hui Zhi 2022/6/2
	 * @param schedulerIP the IP address of the Scheduler
	 * @param port the service port number of the Scheduler
	 * @param info the info to be sent
	 * @return true while message is successfully sending to the Scheduler
	 */
	private String sendInfo(String schedulerIP, int port, String info) {
		boolean success = false;
		boolean sendOK = false;
		String result = "";
		Socket socket = null;
		portMap.put("" + port, schedulerIP);
		SocketClient sc = SocketClient.getInstance();
		SocketDataHandler handler = getSocketDataHandler(schedulerIP, "" + port);

		for (int i = 0; i < cmdRetryCount; i++) {
			try {
				socket = sc.connect(schedulerIP, port, handler, null);
				if (null == socket) {
					continue;
				}

				sendOK = sc.sendData(socket, info, null);
				if (!sendOK) {
					continue;
				}

				String feedback = "";
				int count = socketRespTimeoutIter;
				while (count > 0) {
					feedback = socketRespData.get(socket);
					if (null != feedback && !"".equals(feedback)) {
						result = feedback;
						success = true;
						break;
					} else {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						count--;
					}
				}

			} catch (IOException e) {
				return "";// TODO log
			} finally {
				if (null != socket) {
					try {
						sc.sendData(socket, "quit", null);//close socket
					} catch (IOException e) {
						e.printStackTrace();
					}
//					sc.removeSocket(socket);
					socketRespData.remove(socket);
				}

			}

			if (success)
				break;
		}

		return result;
	}
	
	private SocketDataHandler getSocketDataHandler(String schedulerIP,String port){
		SocketDataHandler sdh = null;
		
		LinkedHashMap<SchedulerItems,Object> sCfg = SchedulerCfg.getInstance().getData(schedulerIP);
		if(null != sCfg){
			String pt = (String) sCfg.get(SchedulerItems.PORTMACHINE);
			if(port.equals(pt)) return new MachineSocketDataHandler();
			
			pt = (String) sCfg.get(SchedulerItems.PORTROBOT);
			if(port.equals(pt)) return new RobotSocketDataHandler();
			
			pt = (String) sCfg.get(SchedulerItems.PORTMATERIAL);
			if(port.equals(pt)) return new MaterialSocketDataHandler();
			
			pt = (String) sCfg.get(SchedulerItems.PORTTASK);
			if(port.equals(pt)) return new TaskSocketDataHandler();
			
			pt = (String) sCfg.get(SchedulerItems.PORTRACK);
			if(port.equals(pt)) return new RackSocketDataHandler();
			
			pt = (String) sCfg.get(SchedulerItems.PORTTASKUPDATE);
			if(port.equals(pt)) return new TaskSocketDataHandler();
		}
		
		return sdh;
	}
	
	/**
	 * 
	 * Mainly for CNC_Client to receive responses from the Scheduler
	 *
	 */
	class MachineSocketDataHandler extends SocketDataHandler{
		@Override
		public void doHandle(String in, Socket s) {
			if(null == in || null == s) return;
			String ip = s.getInetAddress().toString().replace("/", "");
			if("127.0.0.1".equals(ip)) ip = portMap.get(""+s.getPort());
			
			socketRespData.put(s, in);
			SocketData.setData(in);
			
			String[] data = in.split(",");
			if(data.length > 2){
				String sMD5 = data[data.length-1];
				String dt = in.substring(0, in.length()-sMD5.length()-1);
				
				if(sMD5.equals(MathUtils.MD5Encode(dt))){
					switch(data[0]){
					}
				}
			}
		}
		
	}
	
	/**
	 * 
	 * Mainly for CNC_Client to receive robot info from the Scheduler
	 *
	 */
	class RobotSocketDataHandler extends SocketDataHandler{
		@Override
		public void doHandle(String in, Socket s) {
			if(null == in || null == s) return;
			String ip = s.getInetAddress().toString().replace("/", "");
			if("127.0.0.1".equals(ip)) ip = portMap.get(""+s.getPort());;
			
			socketRespData.put(s, in);
			SocketData.setData(in);
			
			String[] data = in.split(",");
			if(data.length > 2){
				String sMD5 = data[data.length-1];
				String dt = in.substring(0, in.length()-sMD5.length()-1);
				
				if(sMD5.equals(MathUtils.MD5Encode(dt))){
					switch(data[0]){
					}
				}
			}
		}
		
	}
	
	/**
	 * 
	 * Mainly for CNC_Client to receive material info from the Scheduler
	 *
	 */
	class MaterialSocketDataHandler extends SocketDataHandler{
		@Override
		public void doHandle(String in, Socket s) {
			if(null == in || null == s) return;
			String ip = s.getInetAddress().toString().replace("/", "");
			if("127.0.0.1".equals(ip)) ip = portMap.get(""+s.getPort());;
			
			socketRespData.put(s, in);
			SocketData.setData(in);
			
			String[] data = in.split(",");
			if(data.length > 2){
				String sMD5 = data[data.length-1];
				String dt = in.substring(0, in.length()-sMD5.length()-1);
				
				if(sMD5.equals(MathUtils.MD5Encode(dt))){
					switch(data[0]){
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * Mainly for Rack_Manager to receive material info from the Scheduler
	 *
	 */
	class RackSocketDataHandler extends SocketDataHandler{
		@Override
		public void doHandle(String in, Socket s) {
			if(null == in || null == s) return;
			String ip = s.getInetAddress().toString().replace("/", "");
			if("127.0.0.1".equals(ip)) ip = portMap.get(""+s.getPort());;
			
			socketRespData.put(s, in);
			SocketData.setData(in);
			
			String[] data = in.split(",");
			if(data.length > 2){
				String sMD5 = data[data.length-1];
				String dt = in.substring(0, in.length()-sMD5.length()-1);
				
				if(sMD5.equals(MathUtils.MD5Encode(dt))){
					switch(data[0]){
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * Mainly for CNC_Client to receive task from the Scheduler
	 *
	 */
	class TaskSocketDataHandler extends SocketDataHandler{
		@Override
		public void doHandle(String in, Socket s) {
			if(null == in || null == s) return;
			String ip = s.getInetAddress().toString().replace("/", "");
			if("127.0.0.1".equals(ip)) ip = portMap.get(""+s.getPort());
			
			socketRespData.put(s, in);
			SocketData.setData(in);
			
			String[] data = in.split(",");
			if(data.length > 2){
				String sMD5 = data[data.length-1];
				String dt = in.substring(0, in.length()-sMD5.length()-1);
				
				if(sMD5.equals(MathUtils.MD5Encode(dt))){
					if("getTask".equals(data[0])){
						/**
						 * Finished-machining task format: machineIP+":"+machineState+":"+robotIP+":"+taskID+":"+lineName
						 * Not-finished-machining task format: machineIP+":"+machineState+":"+robotIP+":"+workpieceIDs+":"+rackIDs+":"+rackSlotIDs+":"+taskID+":"+lineName
						 */
						String taskInfo = in.replace(data[0]+",", "").replace(","+sMD5, "");
						String[] tasks = taskInfo.split("/");
						JSONObject jsonObj = new JSONObject();
						TaskData taskData = TaskData.getInstance();
						for(int i=0; i<tasks.length; i++){
							if(!tasks[i].startsWith("{")) continue;
							jsonObj = JSONObject.fromObject(tasks[i]);
							if(null != jsonObj){
								String cncIP = jsonObj.getString("machineIP");
								String robotIP = jsonObj.getString("robotIP");
								String curTaskID = jsonObj.getString("taskID");
								if(null == taskData.getData(curTaskID)){
									if(!machineIsScheduled(cncIP) && !robotIsScheduled(robotIP)){
										if(taskData.taskPush(jsonObj.toString())){
											addScheduledMachine(cncIP);
											addScheduledRobot(robotIP);
										}
									}else{
										if(machineIsScheduled(cncIP)) System.out.println(robotIP+"->"+cncIP+": CNC "+cncIP+" was scheduled");
										if(robotIsScheduled(robotIP)) System.out.println(robotIP+"->"+cncIP+": Robot "+robotIP+" was scheduled");
									}
								}
							}
						}
					}else if("removeTask".equals(data[0])){
						try {
							TaskData taskData = TaskData.getInstance();
							String cncIP = (String) taskData.getTaskByID(data[1]).get(TaskItems.MACHINEIP);
							String robotIP = (String) taskData.getTaskByID(data[1]).get(TaskItems.ROBOTIP);
							removeScheduledMachine(cncIP);
							removeScheduledRobot(robotIP);
							taskData.removeData(data[1]);
						} catch (Exception e) {
						}
					}else if("updateTask".equals(data[0])){
						String taskInfo = in.replace(data[0]+",", "").replace(","+sMD5, "");
						try {
							JSONObject jsonObj = JSONObject.fromObject(taskInfo);
							DeviceState state = DataUtils.getDevStateByString(jsonObj.getString("taskState"));
							if(null != state){
								RunningMsg.set("updateTask "+jsonObj.getString("taskID")+" OK");
							}
						} catch (Exception e) {
						}
					}else if("getAlarmTask".equals(data[0])){
						String taskInfo = in.replace(data[0]+",", "").replace(","+sMD5, "");
						if(!"".equals(taskInfo.trim())){
							try {
								String[] tasks = taskInfo.split("/");
								for(String task:tasks){
									JSONObject jsonObj = JSONObject.fromObject(task);
									DeviceState state = DataUtils.getDevStateByString(jsonObj.getString("taskState"));
									if(null != state && DeviceState.ALARMING == state){
										TaskData.getInstance().taskPush(jsonObj.toString());
										RunningMsg.set("getAlarmTask "+jsonObj.getString("taskID")+" OK");
									}
								}
							} catch (Exception e) {
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * CNC_Client sends message to the Scheduler
	 *
	 */
	class sendMsgToServer implements Runnable{
		private String msg;
		private String signature;
		private SchedulerItems portCfgNo;
		
		public sendMsgToServer(SchedulerItems portCfgNo, String msg, String signature){
			this.msg = msg;
			this.portCfgNo = portCfgNo;
			this.signature = signature;
		}
		
		@Override
		public void run() {
			String[] scheduler = getScheduler(portCfgNo);
			String result = "";
			boolean OK = true;
			result = sendInfo(scheduler[0],Integer.parseInt(scheduler[1]),msg);
			if (null == result || result.equals("")) {
				OK = false;
			}
			if(!"".equals(signature)) cmdFlag.put(signature, OK);
		}
	}
}
