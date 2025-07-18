package com.cncmes.drv;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cncmes.base.CNC;
import com.cncmes.base.CncItems;
import com.cncmes.base.DeviceState;
import com.cncmes.base.DummyItems;
import com.cncmes.ctrl.SocketClient;
import com.cncmes.data.CncData;
import com.cncmes.data.CncEthernetCmd;
import com.cncmes.data.DevHelper;
import com.cncmes.data.SocketData;
import com.cncmes.data.SystemConfig;
import com.cncmes.handler.CncDataHandler;
import com.cncmes.handler.SocketDataHandler;
import com.cncmes.utils.DebugUtils;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.NetUtils;
import com.cncmes.utils.XmlUtils;

/**
 * Virtual CNC Driver
 * 
 * @author LI ZI LONG
 *
 */
public class CncDummy implements CNC {
	// private Map<String,Object> socketRespData= new
	// LinkedHashMap<String,Object>();
	private Map<Socket, String> socketRespData = new ConcurrentHashMap<Socket, String>();
	private LinkedHashMap<String, Object> commonCfg = null;
	private static LinkedHashMap<String, CncDataHandler> dataHandler = new LinkedHashMap<String, CncDataHandler>();
	private static CncData cncData = CncData.getInstance();
	private static CncEthernetCmd cncEthernetCmd = CncEthernetCmd.getInstance();

	private static int cmdRetryCount = 5;
	private static int socketRespTimeout = 2;// seconds
	private static int socketRespTimeInterval = 10;// milliseconds

	private static CncDummy cncDummy = new CncDummy();
	private static CncSocketDataHandler handler = cncDummy.new CncSocketDataHandler();

	private CncDummy() {
	}

	public static CncDummy getInstance() {
		return cncDummy;
	}

	static {
		try {
			XmlUtils.parseCncEthernetCmdXml();
		} catch (Exception e) {
			LogUtils.errorLog("CncDummy fails to load system config:" + e.getMessage());
		}
	}

	private void getCommonSettings() {
		SystemConfig sysCfg = SystemConfig.getInstance();
		LinkedHashMap<String, Object> config = sysCfg.getCommonCfg();
		cmdRetryCount = Integer.parseInt((String) config.get("cmdRetryCount"));
		socketRespTimeout = Integer.parseInt((String) config.get("socketResponseTimeOut"));
		socketRespTimeInterval = Integer.parseInt((String) config.get("socketResponseInterval"));
	}

	@Override
	public boolean openDoor(String ip) {
		if (sendCommand(ip, "openDoor", null, null)) {
			cncData.setData(ip, CncItems.OP_OPENDOOR, "1");
			return true;
		} else {
			cncData.setData(ip, CncItems.OP_OPENDOOR, "0");
			return false;
		}
	}

	@Override
	public boolean closeDoor(String ip) {
		if (sendCommand(ip, "closeDoor", null, null)) {
			cncData.setData(ip, CncItems.OP_CLOSEDOOR, "1");
			return true;
		} else {
			cncData.setData(ip, CncItems.OP_CLOSEDOOR, "0");
			return false;
		}
	}

	@Override
	public boolean clampFixture(String ip, int workZone) {
		LinkedHashMap<String, String> cmdParas = new LinkedHashMap<String, String>();
		cmdParas.put("workZone", workZone + "");
		return sendCommand(ip, "clampFixture", cmdParas, null);
	}

	@Override
	public boolean releaseFixture(String ip, int workZone) {
		LinkedHashMap<String, String> cmdParas = new LinkedHashMap<String, String>();
		cmdParas.put("workZone", workZone + "");
		return sendCommand(ip, "releaseFixture", cmdParas, null);
	}

	@Override
	public DeviceState getMachineState(String ip) {
		DeviceState state = DeviceState.SHUTDOWN;
		LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
		if (sendCommand(ip, "getMachineState", null, rtnData)) {
			CncDataHandler rsltHandler = getDataHandler(ip);
			if (null != rsltHandler) {
				for (String key : rtnData.keySet()) {
					state = rsltHandler.machineStateHandler(rtnData.get(key));
					break;
				}

			} else {
				state = DeviceState.ALARMING;
			}
		}
		state = DeviceState.STANDBY;
		return state;
	}

	@Override
	public LinkedHashMap<String, String> getAlarmInfo(String ip) {
		LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> alarmInfo = new LinkedHashMap<String, String>();

		if (sendCommand(ip, "getAlarmInfo", null, rtnData)) {
			CncDataHandler rsltHandler = getDataHandler(ip);
			if (null != rsltHandler) {
				for (String key : rtnData.keySet()) {
					alarmInfo = rsltHandler.alarmInfoHandler(rtnData.get(key));
					break;
				}
			}
		}
		String alarmMsg = "";
		if (alarmInfo.size() > 0) {
			for (String key : alarmInfo.keySet()) {
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

		if (sendCommand(ip, "getToolLife", null, rtnData)) {
			CncDataHandler rsltHandler = getDataHandler(ip);
			if (null != rsltHandler) {
				for (String key : rtnData.keySet()) {
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

		if (sendCommand(ip, "getMachiningParas", null, rtnData)) {
			CncDataHandler rsltHandler = getDataHandler(ip);
			if (null != rsltHandler) {
				for (String key : rtnData.keySet()) {
					machineParas = rsltHandler.machineParasHandler(rtnData.get(key));
					break;
				}
			}
		}
		String paras = "";
		if (machineParas.size() > 0) {
			for (String para : machineParas.keySet()) {
				if ("".equals(paras)) {
					paras = para + ": " + machineParas.get(para);
				} else {
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

		if (sendCommand(ip, "getMachiningCounter", null, rtnData)) {
			CncDataHandler rsltHandler = getDataHandler(ip);
			if (null != rsltHandler) {
				for (String key : rtnData.keySet()) {
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
		if (sendCommand(ip, "downloadMainProgram", cmdParas, rtnData)) {
			CncDataHandler rsltHandler = getDataHandler(ip);
			if (null != rsltHandler) {
				for (String key : rtnData.keySet()) {
					progContent = rtnData.get(key);
					if (!progContent.startsWith("O" + programID))
						progContent = "O" + programID + progContent;
					rtn = rsltHandler.machineNCProgramHandler(progContent, programID + ".dl");
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
		if (sendCommand(ip, "downloadSubProgram", cmdParas, rtnData)) {
			CncDataHandler rsltHandler = getDataHandler(ip);
			if (null != rsltHandler) {
				for (String key : rtnData.keySet()) {
					rtn = rsltHandler.machineNCProgramHandler(rtnData.get(key), programID + ".dl");
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
		LinkedHashMap<String, String> inParas = new LinkedHashMap<String, String>();
		inParas.put("programID", programID);
		if (sendCommand(ip, "getCurrSubProgramName", inParas, rtnData)) {
			if (!rtnData.isEmpty()) {
				for (String key : rtnData.keySet()) {
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
		if (null != preHandler) {
			content = preHandler.machineGenerateMainProgramHandler(ip, wpIDs, subPrograms, workzones);
		}

		return content;
	}

	@Override
	public boolean setDataHandler(String cncModel, CncDataHandler handler) {
		if (null != handler) {
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
	public boolean sendCommand(String ip, String operationName, LinkedHashMap<String, String> inParas,
			LinkedHashMap<String, String> outParas) {
		boolean success = false;
		LinkedHashMap<CncItems, Object> cncInfo = cncData.getData(ip);
		DevHelper devHelper = DevHelper.getInstance();
		String cmdSeperator = ";", successCode = "0", returnCodePosition = "LastElement", model = "";
		String dummyCncIP = DebugUtils.getDummyDeviceIP(DummyItems.IP_CNC);
		long cmdStartT = 0, cmdStopT = 0;
		int port = 0;
		if (null != cncInfo)
			port = Integer.parseInt("" + cncInfo.get(CncItems.PORT));

		getCommonSettings();
		model = devHelper.getModelByDriver(this.getClass().getName());
		String cmdEndChr = devHelper.getCmdTerminator(model);
		String opExecutive = cncEthernetCmd.getOperationExecutive(model, operationName);
		if (!"Myself".equals(opExecutive)) {
			CNC helper = (CNC) devHelper.getCtrlByModel(opExecutive);
			if (null != helper) {
				switch (operationName) {
				case "clampFixture":
					return helper.clampFixture(ip, Integer.parseInt(inParas.get("workZone")));
				case "closeDoor":
					return helper.closeDoor(ip);
				case "openDoor":
					return helper.openDoor(ip);
				case "releaseFixture":
					return helper.releaseFixture(ip, Integer.parseInt(inParas.get("workZone")));
				}
				return helper.sendCommand(ip, operationName, inParas, outParas);
			} else {
				LogUtils.errorLog("CncDrvWeb-sendCommand get helper(" + opExecutive + ") failed");
			}
		}

		String initParas = "ip", initVals = ip, singleCmd = "", myNameSpace = "";
		if (null != inParas && inParas.size() > 0) {
			for (String para : inParas.keySet()) {
				if ("port".equals(para)) {
					port = Integer.valueOf(inParas.get(para));
				} else if ("model".equals(para)) {
					model = inParas.get(para);
				} else if ("singleCmd".equals(para)) {
					singleCmd = inParas.get(para);
				} else {
					initParas += "," + para;
					initVals += "," + inParas.get(para);
				}
			}
		}
		initParas += ",port";
		initVals += "," + port;
		String[] reInitParas = initParas.split(",");
		String[] reInitVals = initVals.split(",");

		if ("".equals(model)) {
			myNameSpace = this.getClass().getName();
			model = devHelper.getModelByDriver(myNameSpace);
		}
		int cncCmdTimeout = socketRespTimeout; // seconds
		commonCfg = cncEthernetCmd.getCommonConfig(model);
		if (null != commonCfg && commonCfg.size() > 0) {
			cmdSeperator = (String) commonCfg.get("seperator");
			successCode = (String) commonCfg.get("successCode");
			returnCodePosition = (String) commonCfg.get("returnCodePosition");
			socketRespTimeout = (Integer.parseInt("" + commonCfg.get("cmdTimeout")) > 0
					? Integer.parseInt("" + commonCfg.get("cmdTimeout")) : socketRespTimeout);
		}

		String[] cmds = cncEthernetCmd.getAllCommands(model, operationName);
		if (!"".equals(singleCmd))
			cmds = new String[] { singleCmd };

		int totalCmds = cmds.length;
		int successCmds = 0, feedbackCode = 0;
		String pingInfo = "";

		SocketClient sc = SocketClient.getInstance();
		Socket socket;
		boolean sendOK;

		for (String cmd : cmds) {
			if ("".equals(cmd)) {
				continue;
			}

			socket = null;
			sendOK = false;
			feedbackCode = 0;
			cmdStartT = System.currentTimeMillis();
			cncData.setData(ip, CncItems.COMMAND, cmd);
			String cmdStr = cncEthernetCmd.getCommandStr(model, operationName, cmd, reInitParas, reInitVals);
			cncCmdTimeout = cncEthernetCmd.getCommandTimeout(model, operationName, cmd, socketRespTimeout);

			// socket connection
			for (int i = 0; i <= cmdRetryCount; i++) {
				// ping host if retry
				if (i > 0) {
					pingInfo = NetUtils.pingHost(dummyCncIP);
					if (!"OK".equals(pingInfo)) {
						LogUtils.commandLog(ip + "_DummyCNC",
								"NG" + LogUtils.separator + operationName + LogUtils.separator + cmd
										+ LogUtils.separator + "Ping(" + dummyCncIP + ") Failed:" + pingInfo);
						break;
					} else {
						LogUtils.commandLog(ip + "_DummyCNC", "NG" + LogUtils.separator + operationName
								+ LogUtils.separator + cmd + LogUtils.separator + "Ping(" + dummyCncIP + ") OK");
					}
				}

				String feedback = "";
				// 1.connect
				try {
					socket = sc.connect(dummyCncIP, port, handler, cmdEndChr);

					if (null == socket) {
						if (i == cmdRetryCount) {
							feedbackCode = -1;
						}
						continue;// retry
					}
					// 2.send data
					sendOK = sc.sendData(socket, cmdStr, null);
					cmdStartT = System.currentTimeMillis();

					if (!sendOK) {
						continue;// retry
					}

					// 3.waiting
					int count = cncCmdTimeout * 1000 / socketRespTimeInterval;
					while (count > 0) {
						feedback = socketRespData.get(socket);
						feedbackCode = feedbackIsGood(cmdStr, feedback, cmdSeperator, successCode, returnCodePosition);
						if (1 == feedbackCode) {// OK
							outParas = getOperationOutput(operationName, feedback, cmdSeperator, outParas);
							successCmds++;
							break;
						} else if (feedbackCode < 0) {
							break;
						} else {
							try {
								Thread.sleep(socketRespTimeInterval);
							} catch (InterruptedException e) {
							}
							count--;
						}
					}

				} catch (IOException e) {
					feedbackCode = -1;
					LogUtils.errorLog("CncAssistant(" + ip + ") sendCommand(" + operationName + "-" + cmd + ") ERR:"
							+ e.getMessage());
				} finally {
					if(null != socket){
						try {
							sc.sendData(socket, "quit", null);
						} catch (IOException e) {
							e.printStackTrace();
						}
						sc.removeSocket(socket);
						socketRespData.remove(socket);
					}

				}

				// 4.check result
				cmdStopT = System.currentTimeMillis();
				LogUtils.commandLog(ip + "_DummyCNC",
						(1 == feedbackCode ? "OK" : "NG") + LogUtils.separator + operationName + LogUtils.separator
								+ cmdStr + LogUtils.separator + feedback + LogUtils.separator + (cmdStopT - cmdStartT));

				if (0 != feedbackCode) {
					break;
				}
			}
			if (1 != feedbackCode) {
				break;
			}
		}
		List<Socket> result= sc.getAllSockets();
		for(Socket s :result){
			System.out.print(s+"|");
		}
		if (successCmds >= totalCmds)
			success = true;
		return success;
	}

	private LinkedHashMap<String, String> getOperationOutput(String operationName, String feedback, String cmdSeperator,
			LinkedHashMap<String, String> outParas) {
		if (null != outParas) {
			String[] vals = feedback.split(cmdSeperator);
			outParas.put(operationName, vals[vals.length - 1]);
		}
		return outParas;
	}

	private int feedbackIsGood(String cmdStr, String feedback, String cmdSeperator, String successCode,
			String returnCodePosition) {
		int ok = 0;
		int strIndex = 0;
		String rtnCode = "", cmdSent = "", cmdRecieved = "";

		// 1.Get the real-sent command by removing the return code
		if ("LastElement".equals(returnCodePosition)) {
			strIndex = cmdStr.lastIndexOf(cmdSeperator);
			cmdSent = cmdStr.substring(0, strIndex < 0 ? 0 : strIndex);
		} else {
			strIndex = cmdStr.indexOf(cmdSeperator);
			cmdSent = cmdStr.substring(strIndex);
		}

		// 2.Check whether return code is a success code or not
		if (null != feedback && !"".equals(feedback)) {
			if ("LastElement".equals(returnCodePosition)) {
				strIndex = feedback.lastIndexOf(cmdSeperator);
				rtnCode = feedback.substring(strIndex + 1);
				cmdRecieved = feedback.substring(0, strIndex < 0 ? 0 : strIndex);
			} else {
				strIndex = feedback.indexOf(cmdSeperator);
				rtnCode = feedback.substring(0, (strIndex > 1) ? (strIndex - 1) : 0);
				cmdRecieved = feedback.substring(strIndex);
			}
			rtnCode = successCode; // Skip return code checking
			if (cmdRecieved.indexOf(cmdSent) >= 0) {
				if (successCode.equals(rtnCode)) {
					ok = 1;
				} else {
					ok = 0;
				}
			}
		}

		return ok;
	}

	class CncSocketDataHandler extends SocketDataHandler {
		private CncSocketDataHandler() {
		}

		@Override
		public void doHandle(String in, Socket s) {
			if (null == s)
				return;
			String ip = s.getInetAddress().toString().replace("/", "");
			socketRespData.put(s, in);
			SocketData.setData(in);
			if (null != in)
				LogUtils.debugLog("CncDummySocketDataHandler_", ip + ":" + s.getPort() + LogUtils.separator + in);
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
	public boolean setReleaseMacro(String ip, String address, String value, String dec) {
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

	@Override
	public boolean hfFiveAspectProcess(String cncIP) {
		return false;
	}

	@Override
	public boolean setMacro(String ip, String name, String value, String dec) {
		return false;
	}

	@Override
	public Boolean getAndUploadProgramByBarcode(String ip,String barcode,String programName) {
		return null;
	}

	@Override
	public Boolean checkSensor(String cncip, String sensorAddress, String value) {
		return null;
	}

}
