package com.cncmes.drv;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cncmes.base.CNC;
import com.cncmes.base.CncItems;
import com.cncmes.base.DeviceState;
import com.cncmes.ctrl.SocketClient;
import com.cncmes.data.CncData;
import com.cncmes.data.CncDriver;
import com.cncmes.data.SocketData;
import com.cncmes.data.SystemConfig;
import com.cncmes.handler.CncDataHandler;
import com.cncmes.handler.SocketDataHandler;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.XmlUtils;

/**
 * MORI CNC Ethernet-Command-Base Driver
 * @author LI ZI LONG
 *
 */
public class CncMORI implements CNC {
	private Map<Socket, String> socketRespData = new ConcurrentHashMap<Socket, String>();
//	private Map<String,Object> socketRespData= new LinkedHashMap<String,Object>();
	private static LinkedHashMap<String, CncDataHandler> dataHandler = new LinkedHashMap<String, CncDataHandler>();
	private static CncData cncData = CncData.getInstance();
	
	private static int cmdRetryCount = 5;
	private static int socketRespTimeout = 2;//seconds
	private static int socketRespTimeInterval = 10;//milliseconds
	
	private static CncMORI cncMORI = new CncMORI();
	private static CncSocketDataHandler handler = cncMORI.new CncSocketDataHandler();
	private CncMORI(){}
	static{
		try {
			XmlUtils.parseCncEthernetCmdXml();
		} catch (Exception e) {
			LogUtils.errorLog("CncMORI fails to load system config:"+e.getMessage());
		}
	}

	private void getCommonSettings() {
		SystemConfig sysCfg = SystemConfig.getInstance();
		LinkedHashMap<String,Object> config = sysCfg.getCommonCfg();
		cmdRetryCount = Integer.parseInt((String)config.get("cmdRetryCount"));
		socketRespTimeout = Integer.parseInt((String)config.get("socketResponseTimeOut"));
		socketRespTimeInterval = Integer.parseInt((String)config.get("socketResponseInterval"));
	}
	
	public static CncMORI getInstance(){
		return cncMORI;
	}
	
	@Override
	public boolean openDoor(String ip) {
		if(sendCommand(ip, "openDoor", null, null)){
			cncData.setData(ip, CncItems.OP_OPENDOOR, "1");
			return true;
		}else{
			cncData.setData(ip, CncItems.OP_OPENDOOR, "0");
			return false;
		}
	}

	@Override
	public boolean closeDoor(String ip) {
		if(sendCommand(ip, "closeDoor", null, null)){
			cncData.setData(ip, CncItems.OP_CLOSEDOOR, "1");
			return true;
		}else{
			cncData.setData(ip, CncItems.OP_CLOSEDOOR, "0");
			return false;
		}
	}

	@Override
	public boolean clampFixture(String ip, int workZone) {
		LinkedHashMap<String, String> cmdParas = new LinkedHashMap<String, String>();
		cmdParas.put("workZone", workZone+"");
		return sendCommand(ip, "clampFixture", cmdParas, null);
	}
	
	@Override
	public boolean releaseFixture(String ip, int workZone) {
		LinkedHashMap<String, String> cmdParas = new LinkedHashMap<String, String>();
		cmdParas.put("workZone", workZone+"");
		return sendCommand(ip, "releaseFixture", cmdParas, null);
	}
	
	@Override
	public DeviceState getMachineState(String ip) {
		DeviceState state = DeviceState.SHUTDOWN;
		LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
		if(sendCommand(ip, "getMachineState", null, rtnData)){
			CncDataHandler rsltHandler = getDataHandler(ip);
			if(null != rsltHandler){
				for(String key:rtnData.keySet()){
					state = rsltHandler.machineStateHandler(rtnData.get(key));
					break;
				}
			}else{
				state = DeviceState.ALARMING;
			}
		}
		
		return state;
	}

	@Override
	public LinkedHashMap<String, String> getAlarmInfo(String ip) {
		LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> alarmInfo = new LinkedHashMap<String, String>();
		
		if(sendCommand(ip, "getAlarmInfo", null, rtnData)){
			CncDataHandler rsltHandler = getDataHandler(ip);
			if(null != rsltHandler){
				for(String key:rtnData.keySet()){
					alarmInfo = rsltHandler.alarmInfoHandler(rtnData.get(key));
					break;
				}
			}
		}
		String alarmMsg = "";
		if(alarmInfo.size() > 0){
			for(String key:alarmInfo.keySet()){
				alarmMsg += "Alarm: " + key + "_" + alarmInfo.get(key);
				break;
			}
		}
		cncData.setData(ip, CncItems.ALARMMSG, alarmMsg);
		
		return alarmInfo;
	}

	@Override
	public LinkedHashMap<String, LinkedHashMap<String, String>> getToolLife(String ip) {
		LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
		LinkedHashMap<String, LinkedHashMap<String, String>> toolLife = new LinkedHashMap<String, LinkedHashMap<String, String>>();
		
		if(sendCommand(ip, "getToolLife", null, rtnData)){
			CncDataHandler rsltHandler = getDataHandler(ip);
			if(null != rsltHandler){
				for(String key:rtnData.keySet()){
					toolLife = rsltHandler.machineToolLifeHandler(rtnData.get(key));
				}
			}
		}
		return toolLife;
	}

	@Override
	public LinkedHashMap<String, String> getMachiningParas(String ip) {
		LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> machineParas = new LinkedHashMap<String, String>();
		
		if(sendCommand(ip, "getMachiningParas", null, rtnData)){
			CncDataHandler rsltHandler = getDataHandler(ip);
			if(null != rsltHandler){
				for(String key:rtnData.keySet()){
					machineParas = rsltHandler.machineParasHandler(rtnData.get(key));
					break;
				}
			}
		}
		String paras = "";
		if(machineParas.size() > 0){
			for(String para:machineParas.keySet()){
				if("".equals(paras)){
					paras = para + ": " + machineParas.get(para);
				}else{
					paras += "\r\n" + para + ": " + machineParas.get(para);
				}
			}
		}
		cncData.setData(ip, CncItems.PARAS, paras);
		
		return machineParas;
	}

	@Override
	public LinkedHashMap<String, LinkedHashMap<String, String>> getMachiningCounter(String ip) {
		LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
		LinkedHashMap<String, LinkedHashMap<String, String>> counter = new LinkedHashMap<String, LinkedHashMap<String, String>>();
		
		if(sendCommand(ip, "getMachiningCounter", null, rtnData)){
			CncDataHandler rsltHandler = getDataHandler(ip);
			if(null != rsltHandler){
				for(String key:rtnData.keySet()){
					counter = rsltHandler.machineCounterHandler(rtnData.get(key));
					break;
				}
			}
		}
		return counter;
	}

	@Override
	public boolean startMachining(String ip, String programID) {
		LinkedHashMap<String, String> cmdParas = new LinkedHashMap<String, String>();
		cmdParas.put("program", programID);
		return sendCommand(ip, "startMachining", cmdParas, null);
	}

	@Override
	public boolean pauseMachining(String ip) {
		return sendCommand(ip, "pauseMachining", null, null);
	}

	@Override
	public boolean resumeMachining(String ip) {
		return sendCommand(ip, "resumeMachining", null, null);
	}

	@Override
	public boolean uploadMainProgram(String ip, String programID, String ncProgramPath) {
		LinkedHashMap<String, String> cmdParas = new LinkedHashMap<String, String>();
		cmdParas.put("name", programID);
		cmdParas.put("code", ncProgramPath);
		
		return sendCommand(ip, "uploadMainProgram", cmdParas, null);
	}

	@Override
	public String downloadMainProgram(String ip, String programID) {
		LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> cmdParas = new LinkedHashMap<String, String>();
		cmdParas.put("name", programID);
		String rtn = null;
		String progContent = "";
		if(sendCommand(ip, "downloadMainProgram", cmdParas, rtnData)){
			CncDataHandler rsltHandler = getDataHandler(ip);
			if(null != rsltHandler){
				for(String key:rtnData.keySet()){
					progContent = rtnData.get(key);
					if(!progContent.startsWith("O"+programID)) progContent = "O"+programID + progContent;
					rtn = rsltHandler.machineNCProgramHandler(progContent, programID+".dl");
					break;
				}
			}
		}
		return rtn;
	}

	@Override
	public boolean deleteMainProgram(String ip, String programID) {
		LinkedHashMap<String, String> cmdParas = new LinkedHashMap<String, String>();
		cmdParas.put("name", programID);
		return sendCommand(ip, "deleteMainProgram", cmdParas, null);
	}
	
	@Override
	public boolean uploadSubProgram(String ip, String programID, String ncProgramPath) {
		LinkedHashMap<String, String> cmdParas = new LinkedHashMap<String, String>();
		cmdParas.put("name", programID);
		cmdParas.put("code", ncProgramPath);
		
		return sendCommand(ip, "uploadSubProgram", cmdParas, null);
	}

	@Override
	public String downloadSubProgram(String ip, String programID) {
		LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> cmdParas = new LinkedHashMap<String, String>();
		cmdParas.put("name", programID);
		String rtn = null;
		if(sendCommand(ip, "downloadSubProgram", cmdParas, rtnData)){
			CncDataHandler rsltHandler = getDataHandler(ip);
			if(null != rsltHandler){
				for(String key:rtnData.keySet()){
					rtn = rsltHandler.machineNCProgramHandler(rtnData.get(key), programID+".dl");
					break;
				}
			}
		}
		return rtn;
	}

	@Override
	public boolean deleteSubProgram(String ip, String programID) {
		LinkedHashMap<String, String> cmdParas = new LinkedHashMap<String, String>();
		cmdParas.put("name", programID);
		return sendCommand(ip, "deleteSubProgram", cmdParas, null);
	}
	
	@Override
	public boolean mainProgramIsActivate(String ip, String programID) {
		return true;
	}
	
	@Override
	public String getMainProgramName(String ip, String programID) {
		return programID;
	}
	
	@Override
	public String getCurrSubProgramName(String ip, String programID) {
		LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
		if(sendCommand(ip, "getCurrSubProgramName", null, rtnData)){
			if(!rtnData.isEmpty()){
				for(String key:rtnData.keySet()){
					programID = rtnData.get(key);
					break;
				}
			}
		}
		return programID;
	}
	
	@Override
	public String generateMainProgramContent(String ip, String wpIDs, String subPrograms, String workzones) {
		String content = "";
		
		CncDataHandler preHandler = getDataHandler(ip);
		if(null != preHandler){
			content = preHandler.machineGenerateMainProgramHandler(ip, wpIDs, subPrograms, workzones);
		}
		
		return content;
	}
	
	@Override
	public boolean setDataHandler(String cncModel, CncDataHandler handler) {
		if(null != handler){
			dataHandler.put(cncModel, handler);
			return true;
		}
		return false;
	}
	
	@Override
	public CncDataHandler getDataHandler(String ip) {
		String cncModel = (String) CncData.getInstance().getData(ip).get(CncItems.MODEL);
		return dataHandler.get(cncModel);
	}
	
	@Override
	public boolean sendCommand(String ip, String cmd, LinkedHashMap<String, String> inParas, LinkedHashMap<String, String> rtnData){
		boolean success = false;
		long cmdStartT = 0, cmdStopT = 0;
		int port = (int) cncData.getData(ip).get(CncItems.PORT);
		String model = cncData.getCncModel(ip);
		String cmdEndChr = CncDriver.getInstance().getCmdTerminator(model);
		String dtKey = ip+":"+port;
		
		getCommonSettings();
		SocketClient sc = SocketClient.getInstance();
//		SocketDataHandler sdh = sc.getSocketDataHandler(ip, port);
//		if(null==sdh){
//			sdh = new CncSocketDataHandler();
//			sc.addSocketDataHandler(ip, port, sdh);
//		}
//		if(null == sc.getSocket(ip, port) || null == socketRespData.get(dtKey)){
//			try {
//				sc.connect(ip, port, handler, cmdEndChr);
//			} catch (IOException e) {
//				return false;
//			}
//		}else{
//			sc.startSocketClientListener(sc.getSocket(ip, port), ip, port);
//		}
		
//		socketRespData.put(dtKey, "");
		cncData.setData(ip, CncItems.COMMAND, cmd);
		boolean cnnOK = false, sendOK = false;
		Socket socket = null;
		String feedback = "";
		cmdStartT = System.currentTimeMillis();
		for (int i = 0; i <= cmdRetryCount; i++) {
			try {
				// connect
				socket = sc.connect(ip, port, handler, cmdEndChr);
				if (null == socket) {
					LogUtils.commandLog(ip, "NG" + LogUtils.separator + dtKey + LogUtils.separator + cmd
							+ LogUtils.separator + "Connecting " + (cnnOK ? "OK" : "NG"));
					continue;// retry
				}
				// send data
				cmdStartT = System.currentTimeMillis();
				sendOK = sc.sendData(socket, cmd, cmdEndChr);
				if (!sendOK) {
					continue;// retry
				}
				// receive data
				int count = socketRespTimeout * 1000 / socketRespTimeInterval;
				while (count > 0) {
					feedback = (String) socketRespData.get(socket);
					if (null != feedback && feedback.indexOf(cmd) >= 0) {
						String rtn = feedback.replace(cmd + ",", "").trim();
						if (!"".equals(rtn) && null != rtnData)
							rtnData.put(cmd, rtn);
						success = true;
						break;
					} else {
						try {
							Thread.sleep(socketRespTimeInterval);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						count--;
					}
				}

			} catch (IOException e) {
				LogUtils.errorLog("Machine(" + ip + ") sendCommand(" + cmd + ") ERR:" + e.getMessage());
			} finally {
				if(null != socket){
					sc.removeSocket(socket);
					socketRespData.remove(socket);
				}
			}

			cmdStopT = System.currentTimeMillis();
			LogUtils.commandLog(ip, (success ? "OK" : "NG") + LogUtils.separator + dtKey + LogUtils.separator + cmd
					+ LogUtils.separator + feedback + LogUtils.separator + (cmdStopT - cmdStartT));
			if (success)
				break;
		}
		return success;
	}
	
	class CncSocketDataHandler extends SocketDataHandler{
		private CncSocketDataHandler(){}
		@Override
		public void doHandle(String in, Socket s) {
			if(null == s) return;
			String ip = s.getInetAddress().toString().replace("/", "");
//			socketRespData.put(ip+":"+s.getPort(), in);
			socketRespData.put(s, in);
			SocketData.setData(in);
			if(null != in) LogUtils.debugLog("CncMORISocketDataHandler_", ip+":"+s.getPort()+LogUtils.separator+in);
		}
	}

	@Override
	public boolean hfOpenDoor(String ip) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hfCloseDoor(String ip) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hfClampFixture(String ip, int workZone) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean flushProgram(String ip, Boolean needToFlush) {
		return false;
	}

	@Override
	public double getMacro(String cncIp, double address) {
		return 0;
	}

	@Override
	public boolean setMacro(String ip, String address, String value, String dec) {
		return false;
	}

	@Override
	public boolean hfReleaseFixture(String ip, int workZone) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hfResetDoor(String ip) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hfHomeDoor(String ip) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hfRotate(String ip, int angle) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hfCheckPosition(String ip) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean startMachinePrepared(String ip) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean checkPosition(String ip, int address, double value) {
		return false;
	}
}
