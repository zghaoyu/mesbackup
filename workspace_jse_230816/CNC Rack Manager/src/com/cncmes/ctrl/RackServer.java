package com.cncmes.ctrl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.LinkedHashMap;

import org.apache.commons.io.FileUtils;

import com.cncmes.base.DeviceState;
import com.cncmes.base.MemoryItems;
import com.cncmes.base.WorkpieceItems;
import com.cncmes.data.RackMaterial;
import com.cncmes.data.RackProduct;
import com.cncmes.data.RackTemp;
import com.cncmes.data.SystemConfig;
import com.cncmes.data.WorkpieceData;
import com.cncmes.gui.RackManager;
import com.cncmes.handler.SocketRespHandler;
import com.cncmes.thread.ThreadController;
import com.cncmes.utils.DataUtils;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.MathUtils;
import com.cncmes.utils.MyFileUtils;
import com.cncmes.utils.MySystemUtils;

import net.sf.json.JSONObject;

public class RackServer {
	private static RackServer rackServer = new RackServer();
	private static RackManager rackManagerGUI = RackManager.getInstance();
	private SocketServer rackSS = null;
	private static int PORTRACK = 0;
	private static LinkedHashMap<String,String> devControlCenter = new LinkedHashMap<String,String>();
	
	private RackServer(){}
	static{
		PORTRACK = RackProduct.getInstance().getPort("All");
	}
	public static RackServer getInstance(){
		return rackServer;
	}
	
	public String start(){
		String msg = "OK";
		msg = MySystemUtils.sysReadyToStart();
		if(!"OK".equals(msg)) return msg;
		
		ThreadController.initStopFlag();
		
		rackSS = SocketServer.getInstance();
		try {
			rackSS.socketSvrStart(PORTRACK, new RackMsgHandler());
			
			LinkedHashMap<String,Object> sysConfig = SystemConfig.getInstance().getCommonCfg();
			boolean runningLog = Integer.valueOf((String)sysConfig.get("RunningLog"))>0?true:false;
			boolean debugLog = Integer.valueOf((String)sysConfig.get("DebugLog"))>0?true:false;
			LogUtils.clearLog();
			LogUtils.setEnabledFlag(runningLog);
			LogUtils.setDebugLogFlag(debugLog);
		} catch (IOException e) {
			msg = "RackServer start ERR:"+e.getMessage();
		}
		
		MySystemUtils.sysMemoryRestore("All", "All", MemoryItems.ALL, null, false);
		return msg;
	}
	
	public String stop(){
		String msg = "OK";
		
		if(devControlCenter.size() > 0){
			String[] ccCfg = null;
			CtrlCenterClient ccClient = CtrlCenterClient.getInstance();
			try {
				for(String lineName:devControlCenter.keySet()){
					ccCfg = devControlCenter.get(lineName).split(":");
					ccClient.informControlCenter("rackManagerStopped", ccCfg[0], ccCfg[1], lineName, ccCfg[0], false, false, true);
				}
			} catch (Exception e) {
			}
		}
		
		SocketClient.getInstance().disconnectAllSockets();
		if(null != rackSS) rackSS.stopSvrPort(PORTRACK);
		ThreadController.stopRackManager();
		
		MySystemUtils.sysMemoryDump("All", "All", MemoryItems.ALL);
		
		return msg;
	}
	
	public String getControlCenter(String lineName){
		return devControlCenter.get(lineName);
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
							ccClient.informControlCenter("rackManagerStarted", ccCfg[0], ccCfg[1], lineName, ccInfo, threadMode, false, true);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void feedbackToClient(Socket s,String feedback){
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			bw.write(feedback + "\n");
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	
	class RackMsgHandler implements SocketRespHandler{
		@Override
		public void doHandle(String in, Socket s) {
			if(null == s || null == in) return;
			String[] data = in.split(",");
			if(data.length > 2){
				String sMD5 = data[data.length-1];
				String dt = in.substring(0, in.length()-sMD5.length()-1);
				
				if(sMD5.equals(MathUtils.MD5Encode(dt))){
					if("checkState".equals(data[0])){
						feedbackToClient(s,in);
						if(data.length > 3){
							devControlCenter.put(data[1], data[2]+":"+data[3]);//lineName,IP:Port
						}
					}else if("randomUpdateMaterial".equals(data[0])){
						feedbackToClient(s,in);
						LogUtils.debugLog("randomUpdateMaterial_", in+","+Thread.currentThread().getName());
						if(data.length > 2){
							RackManager rackManager = RackManager.getInstance();
							int randomQty = rackManager.unloadMaterialFromRack(data[1], true);
							if(randomQty<=0) randomQty = RackProduct.getInstance().getEmptySlotsCount(data[1], "All");
							if(randomQty>0){
								int qty = Integer.parseInt(data[2]);
								if(qty > 0 && qty <= randomQty) randomQty = qty;
								rackManager.loadMaterialOntoRack(data[1], randomQty);
							}
						}
					}else if("getRackEmptySlots".equals(data[0])){
						RackProduct pRack = RackProduct.getInstance();
						String[] slots = pRack.getEmptySlots(data[1], data[2]);
						String feedback = data[0];
						if(null != slots){
							for(String slot:slots){
								feedback += "," + slot;
							}
						}else{
							feedback += ",";
						}
						feedbackToClient(s,feedback);
					}else{
						feedbackToClient(s,in);
						String workpieceID = data[0];
						String rackID = data[1];
						String lineName = data[2];
						
						WorkpieceData wpData = WorkpieceData.getInstance();
						DeviceState state = getDevStateByString(data[3]);
						wpData.setWorkpieceState(workpieceID, state);
						wpData.setData(workpieceID, WorkpieceItems.PROCESS, data[7]);
						wpData.setData(workpieceID, WorkpieceItems.SURFACE, data[8]);
						wpData.setData(workpieceID, WorkpieceItems.MACHINETIME, data[9]);
						wpData.setData(workpieceID, WorkpieceItems.NCMODEL, data[10]);
						wpData.setData(workpieceID, WorkpieceItems.PROGRAM, data[11]);
						
						if(DeviceState.WORKING == state || DeviceState.FINISH == state){
							RackTemp tmpRack = RackTemp.getInstance();
							if(DeviceState.WORKING == state){
								RackMaterial mRack = RackMaterial.getInstance();
								mRack.updateSlot(lineName, data[5], Integer.parseInt(data[6]), "");// 5:rackID,6:rackSlot
								tmpRack.putWorkpieceOnRack(lineName, data[5], workpieceID, data[6]);
								LogUtils.debugLog("RackMaterialReceive_", in);
							}
							rackManagerGUI.refreshGUI(0);
							if(DeviceState.FINISH == state){
								RackProduct pRack = RackProduct.getInstance();
								pRack.putWorkpieceOnRack(lineName, rackID, workpieceID, null);
								tmpRack.removeWorkpiece(lineName, "All", workpieceID);
								rackManagerGUI.refreshGUI(1);
							}
						}
					}
				}
			}
		}
	}
}
