package com.cncmes.ctrl;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;

import com.cncmes.base.DeviceState;
import com.cncmes.base.SchedulerItems;
import com.cncmes.base.SpecItems;
import com.cncmes.base.WorkpieceItems;
import com.cncmes.data.SchedulerCfg;
import com.cncmes.data.SocketData;
import com.cncmes.data.WorkpieceData;
import com.cncmes.handler.SocketDataHandler;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.MathUtils;
import com.cncmes.utils.RunningMsg;
import com.cncmes.utils.ThreadUtils;

public class SchedulerClient {
	private Map<String,Object> socketRespData= new LinkedHashMap<String,Object>();
	private LinkedHashMap<String,String> portMap = new LinkedHashMap<String,String>();
	private LinkedHashMap<String,Boolean> cmdFlag = new LinkedHashMap<String,Boolean>();
	
	private int cmdRetryCount = 5;
	private int socketRespTimeoutIter = 20;//20 means 2 seconds
	
	private static SchedulerClient sClient = new SchedulerClient();
	private SchedulerClient(){}
	public static SchedulerClient getInstance(){
		return sClient;
	}
	
	private boolean checkReady(SchedulerItems portType){
		String info = "checkReady,"+portType;
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
	 * @param pType
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
					if(portType == SchedulerItems.PORTRACK) RunningMsg.set("Scheduler for Rack Manager is not ready");
				}
			}
		}else{
			RunningMsg.set("Scheduler ready check error");
			ready = false;
		}
		
		return ready;
	}
	
	public boolean informScheduler(String cmdName, SchedulerItems portType, String data, boolean threadMode, boolean threadSequential, boolean theLastThread){
		boolean ok = true;
		String cmdStr = cmdName + "," + data;
		cmdStr += "," + MathUtils.MD5Encode(cmdStr);
		
		if(threadMode){
			if(threadSequential){
				ThreadUtils.sequentialRun(new sendMsgToServer(portType, cmdStr, cmdName), theLastThread);
			}else{
				ThreadUtils.Run(new sendMsgToServer(portType, cmdStr, cmdName));
			}
		}else{
			String[] scheduler = getScheduler(portType);
			ok = sendInfo(scheduler[0], Integer.parseInt(scheduler[1]), cmdStr);
		}
		return ok;
	}
	
	/**
	 * 
	 * @param materialID
	 * @param portType
	 * @param batchUpdate
	 * @return true while CNC_Client/Rack_Manager successfully sends material data to the Scheduler
	 */
	public boolean updateMaterialInfo(String materialID,SchedulerItems portType,Boolean...batchUpdate){
		boolean success = true;
		String[] scheduler = getScheduler(portType==SchedulerItems.PORTMATERIAL?SchedulerItems.PORTMATERIAL:SchedulerItems.PORTRACK);
		String info = "";
		
		WorkpieceData wpData = WorkpieceData.getInstance();
		LinkedHashMap<WorkpieceItems,Object> dt = wpData.getData(materialID);
		if(null != dt){
			LinkedHashMap<SpecItems, String> spec = wpData.getAllProcInfo(materialID);
			if(null != spec){
				info = materialID;
				info += "," + dt.get(WorkpieceItems.LINENAME);
				info += "," + dt.get(WorkpieceItems.STATE);
				info += "," + dt.get(WorkpieceItems.PROCQTY);
				info += "," + spec.get(SpecItems.PROCESSNAME);
				info += "," + spec.get(SpecItems.SURFACE);
				info += "," + spec.get(SpecItems.SIMTIME);
				info += "," + spec.get(SpecItems.NCMODEL);
				info += "," + spec.get(SpecItems.PROGRAM);
				info += "," + dt.get(WorkpieceItems.CONVEYORID);
				info += "," + dt.get(WorkpieceItems.CONVEYORSLOTNO);
				if(batchUpdate.length > 0 && batchUpdate[0]){
					info += ",1";
				}else{
					info += ",0";
				}
				info += "," + MathUtils.MD5Encode(info);
				LogUtils.debugLog("RackMsend_", info);
				
			}
		}
		
		if(!"".equals(info)){
			success = sendInfo(scheduler[0],Integer.parseInt(scheduler[1]),info);
		}
		
//		System.out.println((success?"OK|":"NG|")+info);
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
					if(portType == SchedulerItems.PORTRACK) scheduler[1] = (String) s.get(SchedulerItems.PORTRACK);
				}
			}
		}
		
		return scheduler;
	}
	
	/**
	 * 
	 * @param schedulerIP
	 * @param port
	 * @param info
	 * @return true while message is successfully sending to the Scheduler
	 */
	private boolean sendInfo(String schedulerIP, int port, String info){
		boolean success = false;
		String key = schedulerIP+":"+port;
		portMap.put(""+port, schedulerIP);
		
		SocketClient sc = SocketClient.getInstance();
		if(null == sc.getSocket(schedulerIP, port) || null == socketRespData.get(key)){
			try {
				sc.connect(schedulerIP, port, getSocketDataHandler(schedulerIP, ""+port), null);
				socketRespData.put(key, "");
			} catch (IOException e) {
				return false;
			}
		}else{
			socketRespData.put(key, "");
			sc.startSocketClientListener(sc.getSocket(schedulerIP, port), schedulerIP, port);
			if(null == sc.getSocketDataHandler(schedulerIP, port)){
				sc.addSocketDataHandler(schedulerIP, port, getSocketDataHandler(schedulerIP, ""+port));
			}
		}
		
		boolean bOK = false;
		for(int i=0; i<cmdRetryCount; i++){
			if(i == 2) bOK = false;
			if(i > 0 && !success && !bOK){
				try {
					bOK = sc.connect(schedulerIP, port, getSocketDataHandler(schedulerIP, ""+port), null);
				} catch (IOException e) {
					continue;
				}
			}
			
			try {
				bOK = sc.sendData(schedulerIP, port, info);
				int count = socketRespTimeoutIter;
				String feedback = "";
				while(count > 0){
					feedback = (String) socketRespData.get(key);
					if(null != feedback && !"".equals(feedback)){
						success = true;
						break;
					}else{
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						count--;
					}
				}
			} catch (IOException e) {
			}
			
			if(success) break;
		}
		
		return success;
	}
	
	private SocketDataHandler getSocketDataHandler(String schedulerIP,String port){
		SocketDataHandler sdh = null;
		
		LinkedHashMap<SchedulerItems,Object> sCfg = SchedulerCfg.getInstance().getData(schedulerIP);
		if(null != sCfg){
			String pt = (String) sCfg.get(SchedulerItems.PORTRACK);
			if(port.equals(pt)) return new RackSocketDataHandler();
			
			pt = (String) sCfg.get(SchedulerItems.PORTMATERIAL);
			if(port.equals(pt)) return new MaterialSocketDataHandler();
		}
		
		return sdh;
	}
	
	private DeviceState getDevStateByString(String state){
		DeviceState devState = null;
		
		if("ALARMING".equals(state)) devState = DeviceState.ALARMING;
		if("FINISH".equals(state)) devState = DeviceState.FINISH;
		if("LOCK".equals(state)) devState = DeviceState.LOCK;
		if("PLAN".equals(state)) devState = DeviceState.PLAN;
		if("SHUTDOWN".equals(state)) devState = DeviceState.SHUTDOWN;
		if("STANDBY".equals(state)) devState = DeviceState.STANDBY;
		if("UNSCHEDULE".equals(state)) devState = DeviceState.UNSCHEDULE;
		if("WAITUL".equals(state)) devState = DeviceState.WAITUL;
		if("WORKING".equals(state)) devState = DeviceState.WORKING;
		
		return devState;
	}
	
	class MaterialSocketDataHandler extends SocketDataHandler{
		@Override
		public void doHandle(String in, Socket s) {
			if(null == in || null == s) return;
			String ip = s.getInetAddress().toString().replace("/", "");
			if("127.0.0.1".equals(ip)) ip = portMap.get(""+s.getPort());;
			
			socketRespData.put(ip+":"+s.getPort(), in);
			SocketData.setData(in);
			
			String[] data = in.split(",");
			if(data.length > 2){
				String sMD5 = data[data.length-1];
				String dt = in.substring(0, in.length()-sMD5.length()-1);
				
				if(sMD5.equals(MathUtils.MD5Encode(dt))){
					if("setState".equals(data[0])){
						WorkpieceData.getInstance().setWorkpieceState(data[1], getDevStateByString(data[2]));
						ThreadUtils.Run(new sendMsgToServer(SchedulerItems.PORTMATERIAL,in,data[0]));
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @author Zilong Mainly for Rack_Manager to receive material state from the Scheduler
	 *
	 */
	class RackSocketDataHandler extends SocketDataHandler{
		@Override
		public void doHandle(String in, Socket s) {
			if(null == in || null == s) return;
			String ip = s.getInetAddress().toString().replace("/", "");
			if("127.0.0.1".equals(ip)) ip = portMap.get(""+s.getPort());;
			
			socketRespData.put(ip+":"+s.getPort(), in);
			SocketData.setData(in);
			
			String[] data = in.split(",");
			if(data.length > 2){
				String sMD5 = data[data.length-1];
				String dt = in.substring(0, in.length()-sMD5.length()-1);
				
				if(sMD5.equals(MathUtils.MD5Encode(dt))){
					if("setState".equals(data[0])){
						WorkpieceData.getInstance().setWorkpieceState(data[1], getDevStateByString(data[2]));
						ThreadUtils.Run(new sendMsgToServer(SchedulerItems.PORTRACK,in,data[0]));
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @author Zilong Mainly for CNC_Client/Rack_Manager sending message to the Scheduler
	 *
	 */
	class sendMsgToServer implements Runnable{
		private String content;
		private String signature;
		private SchedulerItems portCfgNo;
		
		public sendMsgToServer(SchedulerItems portCfgNo, String content, String signature){
			this.content = content;
			this.portCfgNo = portCfgNo;
			this.signature = signature;
		}
		
		@Override
		public void run() {
			String[] scheduler = getScheduler(portCfgNo);
			boolean OK = sendInfo(scheduler[0],Integer.parseInt(scheduler[1]),content);
			cmdFlag.put(signature, OK);
		}
	}
}
