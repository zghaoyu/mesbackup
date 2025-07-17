package com.cncmes.handler.impl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Set;

import com.cncmes.base.CncItems;
import com.cncmes.base.DeviceState;
import com.cncmes.base.RobotItems;
import com.cncmes.base.TaskItems;
import com.cncmes.ctrl.RackClient;
import com.cncmes.data.CncData;
import com.cncmes.data.RobotData;
import com.cncmes.data.TaskData;
import com.cncmes.data.WorkpieceData;
import com.cncmes.handler.SocketRespHandler;
import com.cncmes.utils.DataUtils;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.MathUtils;

public class CncClientDataHandler implements SocketRespHandler {
	private static String ip;
	private static int port;
	
	@Override
	public void doHandle(String in, Socket s) {
		if(null == s || null == in) return;
		String[] data = in.split(",");
		if(data.length > 2){
			String sMD5 = data[data.length-1];
			String dt = in.substring(0, in.length()-sMD5.length()-1);
			if(sMD5.equals(MathUtils.MD5Encode(dt))){
				try {
					if(!"schedulerStarted".equals(data[0])) sendMsgToClient(s, in);
					LogUtils.debugLog("ccServer_", in);
					CncData cncData = CncData.getInstance();
					RobotData robotData = RobotData.getInstance();
					TaskData taskData = TaskData.getInstance();
					WorkpieceData wpData = WorkpieceData.getInstance();
					switch(data[0]){
					case "setMachineState":
						cncData.setData(data[1], CncItems.STATE, DataUtils.getDevStateByString(data[2]));
						break;
					case "setRobotState":
						robotData.setData(data[1], RobotItems.STATE, DataUtils.getDevStateByString(data[2]));
						break;
					case "removeTask":
						taskData.removeData(data[1]);
						break;
					case "updateAlarmTask":
						taskData.setData(data[1], TaskItems.SLOTIDS, DeviceState.WORKING);
						cncData.setData(data[2], CncItems.STATE, DeviceState.WORKING);
						cncData.setCncLastState(data[2], DeviceState.LOCK);//Means start-machining has been triggered
						break;
					case "schedulerStarted":
						//1. Update machine info
						String theLastID1 = "", theLastID2 = "", theLastID3 = "", theLastID4 = "";
						if(cncData.getDataMap().size() > 0){
							Set<String> set = cncData.getDataMap().keySet();
							for(String ip:set){
								theLastID1 = ip;
								cncData.setCncState(ip, cncData.getCncState(ip), true, true, false, true);
							}
						}
						
						//2. Update robot info
						if(robotData.getDataMap().size() > 0){
							Set<String> set = robotData.getDataMap().keySet();
							for(String ip:set){
								theLastID2 = ip;
								robotData.setRobotState(ip, robotData.getRobotState(ip), true, true, false, true);
							}
						}
						
						//3. Update task info
						if(taskData.getDataMap().size() > 0){
							Set<String> set = taskData.getDataMap().keySet();
							for(String taskID:set){
								theLastID3 = taskID;
								taskData.setTaskState(taskID, taskData.getTaskState(taskID), true, true, false, taskData.getErrorDesc(taskID), true);
							}
						}
						
						//4. Update workpiece info
						if(wpData.getDataMap().size() > 0){
							Set<String> set = wpData.getDataMap().keySet();
							for(String id:set){
								theLastID4 = id;
								wpData.setWorkpieceState(id, (DeviceState)wpData.getWorkpieceState(id), true, true, true, false, true);
							}
						}
						
						if(!"".equals(theLastID4)){
							wpData.setWorkpieceState(theLastID4, (DeviceState)wpData.getWorkpieceState(theLastID4), false, true, true, true, true);
						}else if(!"".equals(theLastID3)){
							taskData.setTaskState(theLastID3, taskData.getTaskState(theLastID3), true, true, true, taskData.getErrorDesc(theLastID3), true);
						}else if(!"".equals(theLastID2)){
							robotData.setRobotState(theLastID2, robotData.getRobotState(theLastID2), true, true, true, true);
						}else if(!"".equals(theLastID1)){
							cncData.setCncState(theLastID1, cncData.getCncState(theLastID1), true, true, true, true);
						}
						
						sendMsgToClient(s, in);
						break;
					case "removeMaterial":
						wpData.removeData(data[1]);
						break;
					case "rackManagerStopped":
						break;
					case "rackManagerStarted":
						theLastID4 = "";
						if(wpData.getDataMap().size() > 0){
							RackClient  rackClient = RackClient.getInstance();
							Set<String> set = wpData.getDataMap().keySet();
							String lineName = "", rackID = "";
							for(String id:set){
								theLastID4 = id;
								lineName = wpData.getLineName(id);
								rackID = wpData.getRackID(id);
								rackClient.updateRackInfo(lineName, rackID, id, true, true, false);
							}
							rackClient.updateRackInfo(lineName, rackID, theLastID4, true, true, true);
						}
						break;
					}
				} catch (Exception e) {
					LogUtils.errorLog("CncClientDataHandler doHandle ERR:"+e.getMessage()+LogUtils.separator+in);
				}
			}
		}
	}
	
	public static void setPort(int iPort){
		port = iPort;
	}
	
	public static int getPort(){
		return port;
	}
	
	public static void setIP(String sIP){
		ip = sIP;
	}
	
	public static String getIP(){
		return ip;
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
}
