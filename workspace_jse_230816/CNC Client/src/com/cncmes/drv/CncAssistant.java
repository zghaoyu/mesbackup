package com.cncmes.drv;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cncmes.base.CNC;
import com.cncmes.base.CncItems;
import com.cncmes.base.DeviceState;
import com.cncmes.ctrl.SocketClient;
import com.cncmes.data.CncData;
import com.cncmes.data.CncEthernetCmd;
import com.cncmes.data.DevHelper;
import com.cncmes.data.SocketData;
import com.cncmes.data.SystemConfig;
import com.cncmes.handler.CncDataHandler;
import com.cncmes.handler.SocketDataHandler;
import com.cncmes.utils.DebugUtils;
import com.cncmes.utils.ErrorDesc;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.NetUtils;
import com.cncmes.utils.XmlUtils;

/**
 * CNC Assistant
 * 
 * @author LI ZI LONG
 *
 */
public class CncAssistant implements CNC {
	// private Map<String,Object> socketRespData= new
	// LinkedHashMap<String,Object>();
	private Map<Socket, String> socketRespData = new ConcurrentHashMap<Socket, String>();
	private LinkedHashMap<String, String> portMap = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, Object> commonCfg = null;
	private static LinkedHashMap<String, CncDataHandler> dataHandler = new LinkedHashMap<String, CncDataHandler>();
	private static CncData cncData = CncData.getInstance();
	private static CncEthernetCmd cncEthernetCmd = CncEthernetCmd.getInstance();

	private static int cmdRetryCount = 5;
	private static int socketRespTimeout = 2;// seconds
	private static int socketRespTimeInterval = 10;// milliseconds

	private static CncAssistant cncAssistant = new CncAssistant();
	private static CncSocketDataHandler handler = cncAssistant.new CncSocketDataHandler();// Add
																							// by
																							// Hui
																							// Zhi
																							// 2022/6/1

	private CncAssistant() {
	}

	public static CncAssistant getInstance() {
		return cncAssistant;
	}

	static {
		try {
			XmlUtils.parseCncEthernetCmdXml();
		} catch (Exception e) {
			LogUtils.errorLog("CncAssistant fails to load system config:" + e.getMessage());
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
		// Disable errDoor by Hui Zhi 2021/11/30, and resetDoor every time
		// boolean bNeedReset = sendCommand(ip, "errDoor", null, null);
		// if(bNeedReset) sendCommand(ip, "resetDoor", null, null);
		sendCommand(ip, "resetDoor", null, null);
		return sendCommand(ip, "openDoor", null, null);
	}

	@Override
	public boolean closeDoor(String ip) {
		// Disable errDoor by Hui Zhi 2021/11/30, and resetDoor every time
		// boolean bNeedReset = sendCommand(ip, "errDoor", null, null);
		// if(bNeedReset) sendCommand(ip, "resetDoor", null, null);
		sendCommand(ip, "resetDoor", null, null);
		return sendCommand(ip, "closeDoor", null, null);
	}

	@Override
	public boolean clampFixture(String ip, int workZone) {
		// TODO Now zone 1 clamps all fixtures
		if (workZone > 1)
			return true;

		LinkedHashMap<String, String> cmdParas = new LinkedHashMap<String, String>();
		cmdParas.put("workZone", "1");
		sendCommand(ip, "befClampFixture", cmdParas, null);

		cmdParas.put("workZone", workZone + "");
		return sendCommand(ip, "clampFixture", cmdParas, null);
	}

	@Override
	public boolean releaseFixture(String ip, int workZone) {
		// TODO Now zone 1 releases all fixtures
		if (workZone > 1)
			return true;

		LinkedHashMap<String, String> cmdParas = new LinkedHashMap<String, String>();
		cmdParas.put("workZone", workZone + "");
		return sendCommand(ip, "releaseFixture", cmdParas, null);
	}

	@Override
	public DeviceState getMachineState(String ip) {
		return DeviceState.ALARMING;
	}

	@Override
	public LinkedHashMap<String, String> getAlarmInfo(String ip) {
		return null;
	}

	@Override
	public LinkedHashMap<String, LinkedHashMap<String, String>> getToolLife(String ip) {
		return null;
	}

	@Override
	public LinkedHashMap<String, String> getMachiningParas(String ip) {
		return null;
	}

	@Override
	public LinkedHashMap<String, LinkedHashMap<String, String>> getMachiningCounter(String ip) {
		return null;
	}

	@Override
	public boolean startMachining(String ip, String programID) {
		return false;
	}

	@Override
	public boolean pauseMachining(String ip) {
		return false;
	}

	@Override
	public boolean resumeMachining(String ip) {
		return false;
	}

	@Override
	public boolean uploadMainProgram(String ip, String programID, String ncProgramPath) {
		return false;
	}

	@Override
	public String downloadMainProgram(String ip, String programID) {
		return null;
	}

	@Override
	public boolean deleteMainProgram(String ip, String programID) {
		return false;
	}

	@Override
	public boolean uploadSubProgram(String ip, String programID, String ncProgramPath) {
		return false;
	}

	@Override
	public String downloadSubProgram(String ip, String programID) {
		return null;
	}

	@Override
	public boolean deleteSubProgram(String ip, String programID) {
		return false;
	}

	@Override
	public boolean mainProgramIsActivate(String ip, String programID) {
		return true;
	}

	@Override
	public String getMainProgramName(String ip, String programID) {
		return null;
	}

	@Override
	public String getCurrSubProgramName(String ip, String programID) {
		return null;
	}

	@Override
	public String generateMainProgramContent(String ip, String wpIDs, String subPrograms, String workzones) {
		return null;
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
		// The IP passed to this function is CNC's IP or the Assistant's IP
		LinkedHashMap<CncItems, Object> cncInfo = cncData.getData(ip);
		DevHelper devHelper = DevHelper.getInstance();
		String cmdSeperator = ";", successCode = "0", debugInfo = "";
		String cmdEndChr = "CR", returnCodePosition = "LastElement";
		long cmdStartT = 0, cmdStopT = 0;

		int port = 0;
		String model = devHelper.getModelByDriver(this.getClass().getName());
		getCommonSettings();
		cncData.setData(ip, CncItems.COMMAND, operationName);
		cmdEndChr = devHelper.getCmdTerminator(model);
		// Get the CNC Assistant via CNC's data
		if (null != cncInfo && null != cncInfo.get(CncItems.HELPER_PORT) && null != cncInfo.get(CncItems.HELPER_IP)) {
			port = Integer.parseInt("" + cncInfo.get(CncItems.HELPER_PORT));
			ip = "" + cncInfo.get(CncItems.HELPER_IP);
		}

		String initParas = "ip", initVals = ip, singleCmd = "";
		if (null != inParas && inParas.size() > 0) {
			for (String para : inParas.keySet()) {
				if ("singleCmd".equals(para)) {
					singleCmd = inParas.get(para);
				} else {
					initParas += "," + para;
					initVals += "," + inParas.get(para);
					if ("port".equals(para) && 0 == port) {
						port = Integer.parseInt(inParas.get(para));
					}
				}
			}
		}
		initParas += ",port";
		initVals += "," + port;
		String[] reInitParas = initParas.split(",");
		String[] reInitVals = initVals.split(",");

		// Get common settings of the helper
		int cncCmdTimeout = socketRespTimeout; // seconds
		commonCfg = cncEthernetCmd.getCommonConfig(model);
		if (null != commonCfg && commonCfg.size() > 0) {
			cmdSeperator = (String) commonCfg.get("seperator");
			successCode = (String) commonCfg.get("successCode");
			returnCodePosition = (String) commonCfg.get("returnCodePosition");
			cncCmdTimeout = (Integer.parseInt("" + commonCfg.get("cmdTimeout")) > 0
					? Integer.parseInt("" + commonCfg.get("cmdTimeout")) : socketRespTimeout);
		}

		// String dtKey = ip+":"+port;
		portMap.put("" + port, ip);

		SocketClient sc = SocketClient.getInstance();
		Socket socket = null;
		boolean cnnOK = false, sendOK = false;
		try {
			socket = sc.connect(ip, port, handler, cmdEndChr);

			if (null != socket) {
				cnnOK = true;
			}
			// Get all commands by operation name
			String[] cmds = cncEthernetCmd.getAllCommands(model, operationName);
			if (!"".equals(singleCmd))
				cmds = new String[] { singleCmd };

			int totalCmds = cmds.length;
			int successCmds = 0, feedbackCode = 0, totalRetryTimes = 0;
			for (String cmd : cmds) {
				sendOK = false;
				feedbackCode = 0;
				if ("".equals(cmd))
					continue;
				cmdStartT = System.currentTimeMillis();
				if ("HEX".equals(cmdEndChr)) {// PLC commands
					ArrayList<String[]> plcCmds = cncEthernetCmd.decodePLCCommands(model, operationName, cmd,
							reInitParas, reInitVals);
					if (plcCmds.size() < 2) {
						LogUtils.commandLog(ip, "NG" + LogUtils.separator + operationName + LogUtils.separator
								+ "Decode " + cmd + " failed");
						break;
					}
					// 发送的数据
					String[] plccmds = plcCmds.get(0);
					// 返回的数据
					String[] plcrtns = plcCmds.get(1);
					
					String cmdNG = "", cmdDelay = "", cmdOK = "", pingInfo = "", plccmd = "";
					String[] checkVals;
					totalCmds = plccmds.length;
					
					// 顺序执行指令，feedback code = 1 则成功| -1 则失败| 0 则可重试
					for (int plcCmdIdx = 0; plcCmdIdx < plccmds.length; plcCmdIdx++) {
						plccmd = plccmds[plcCmdIdx];
						sendOK = false;
						feedbackCode = 0;
						// 是否有配置 NG返回的数据和延迟等待的时间，如无则初始化为空
						checkVals = plcrtns[plcCmdIdx].split("#");
						cmdOK = checkVals[0];
						cmdNG = (checkVals.length > 1) ? checkVals[1] : "";
						cmdDelay = (checkVals.length > 2) ? checkVals[2] : "";
						
						for (int i = 0; i <= cmdRetryCount; i++) {
							// if retry, testing the network first
							if (i > 0) {
								pingInfo = NetUtils.pingHost(ip);
								if (!"OK".equals(pingInfo)) {
									LogUtils.commandLog(ip,
											"NG" + LogUtils.separator + operationName + LogUtils.separator + plccmd
													+ LogUtils.separator + "Ping(" + ip + ") Failed:" + pingInfo);
									//等待 120分钟？ disable by Hui Zhi 2022/6/17
//									if (NetUtils.waitUntilNetworkOK(ip, 120, 10)) {
//										LogUtils.commandLog(ip, "NG" + LogUtils.separator + operationName
//												+ LogUtils.separator + plccmd + LogUtils.separator + "Network resumed");
//									} else {
//										LogUtils.commandLog(ip, "NG" + LogUtils.separator + operationName
//												+ LogUtils.separator + plccmd + LogUtils.separator + "Network failed");
//										feedbackCode = -1;
//										break;
//									}
									continue;//add by Hui Zhi  2022/6/17
								}
								LogUtils.commandLog(ip, "NG" + LogUtils.separator + operationName + LogUtils.separator
										+ plccmd + LogUtils.separator + "Ping(" + ip + ") OK");

								// reset door
								if (operationName.startsWith("closeDoor") || operationName.startsWith("openDoor")) {
									LogUtils.commandLog(ip, "NG" + LogUtils.separator + operationName
											+ LogUtils.separator + plccmd + LogUtils.separator + "Door Reset Started");
									LinkedHashMap<String, String> oriInParas = new LinkedHashMap<String, String>();
									for (int k = 0; k < reInitParas.length; k++) {
										oriInParas.put(reInitParas[k], reInitVals[k]);
									}
									if (!sendCommand(ip, "resetDoor", oriInParas, null)) {
										feedbackCode = -1;
										LogUtils.commandLog(ip,
												"NG" + LogUtils.separator + operationName + LogUtils.separator + plccmd
														+ LogUtils.separator + "Door Reset Failed");
									} else {
										if (totalRetryTimes < cmdRetryCount) {
											cnnOK = false;
											plcCmdIdx = -1;
											successCmds = 0;
											feedbackCode = 1;
											totalRetryTimes++;
											LogUtils.commandLog(ip,
													"NG" + LogUtils.separator + operationName + LogUtils.separator
															+ plccmd + LogUtils.separator + "Door Reset OK");
										} else {
											feedbackCode = -1;
											LogUtils.commandLog(ip,
													"NG" + LogUtils.separator + operationName + LogUtils.separator
															+ plccmd + LogUtils.separator
															+ "Door Reset Failed:retry skipped");
										}
									}
									break;
								}
							}//end if (i > 0)

							// reconnect
							if (i == 2)
								cnnOK = false;
							if (i > 0 && 0 == feedbackCode && !cnnOK) {
								try {
									if (null != socket) {
										sc.removeSocket(socket);
										socketRespData.remove(socket);
										socket = null;
									}
									socket = sc.connect(ip, port, handler, cmdEndChr);
									if (null != socket)
										cnnOK = true;
									sendOK = false;
								} catch (IOException e) {
									cnnOK = false;
								}

								LogUtils.commandLog(ip, "NG" + LogUtils.separator + operationName + LogUtils.separator
										+ plccmd + LogUtils.separator + "Reconnecting " + (cnnOK ? "OK" : "NG"));
								if (!cnnOK) {
									if (i == cmdRetryCount)
										feedbackCode = -1;
									continue;
								}
							}

							String feedback = "";
							try {
								// send data
								sendOK = sc.sendData(socket, plccmd, cmdEndChr);

								if (!sendOK) {
									continue;
								}

								// if need to delay
								if (!"".equals(cmdDelay) && Integer.parseInt(cmdDelay) > 0) {
									try {
										Thread.sleep(Integer.parseInt(cmdDelay));
									} catch (Exception e) {
									}
								}

								// receive data
								int count = cncCmdTimeout * 1000 / socketRespTimeInterval;
								while (count > 0) {
									try {
										feedback = Integer
												.toHexString(Integer.parseInt("" + socketRespData.get(socket)));
									} catch (NumberFormatException e1) {
										feedback = "";
									}
									if (cmdOK.toLowerCase().equals(feedback)) {
										feedbackCode = 1;
										successCmds++;
										break;
									} else if (!"".equals(cmdNG) && cmdNG.toLowerCase().equals(feedback)) {
										feedbackCode = -1;
										break;
									} else {
										try {
											// sc.sendData(socket, "0",
											// cmdEndChr);
											Thread.sleep(socketRespTimeInterval);
											// sc.sendData(socket, plccmd,
											// cmdEndChr);
											// Thread.sleep(200);
										} catch (InterruptedException e) {
										}
										count--;
									}
								}

							} catch (IOException e) {
								feedbackCode = -1;
								LogUtils.errorLog("CncAssistant(" + ip + ") sendCommand(" + operationName + "-" + plccmd
										+ ") ERR:" + e.getMessage());
							}

							cmdStopT = System.currentTimeMillis();
							debugInfo = "" + (cmdStopT - cmdStartT);
							if (1 != feedbackCode)
								debugInfo += LogUtils.separator
										+ ErrorDesc.getPLCErrorDesc(Integer.parseInt(plccmd, 16));
							LogUtils.commandLog(ip,
									(1 == feedbackCode ? "OK" : "NG") + LogUtils.separator + operationName
											+ LogUtils.separator + plccmd + LogUtils.separator + feedback
											+ LogUtils.separator + debugInfo);
							if (0 != feedbackCode)
								break;
						} // end of retry
						if (1 != feedbackCode)
							break;
					} // end of one command
				} else {
					// Add command parameters into the command string
					String cmdStr = cncEthernetCmd.getCommandStr(model, operationName, cmd, reInitParas, reInitVals);
					cncCmdTimeout = cncEthernetCmd.getCommandTimeout(model, operationName, cmd, socketRespTimeout);
					socketRespData.put(socket, "");
					for (int i = 0; i <= cmdRetryCount; i++) {
						if (i == 2)
							cnnOK = false;
						if (i > 0 && 0 == feedbackCode && !cnnOK) {
							sc = SocketClient.getInstance();
							try {
								socket = sc.connect(ip, port, handler, cmdEndChr);
								if (null != socket)
									cnnOK = true;
								sendOK = false;
							} catch (IOException e) {
								cnnOK = false;
							}

							LogUtils.commandLog(ip, "NG" + LogUtils.separator + operationName + LogUtils.separator
									+ cmdStr + LogUtils.separator + "Reconnecting " + (cnnOK ? "OK" : "NG"));
							if (!cnnOK) {
								if (i == cmdRetryCount)
									feedbackCode = -1;
								continue;
							}
						}

						String feedback = "";
						try {
							if (!sendOK) {
								sendOK = sc.sendData(socket, cmdStr, cmdEndChr);
							}
							int count = cncCmdTimeout * 1000 / socketRespTimeInterval;
							while (count > 0) {
								feedback = (String) socketRespData.get(socket);
								feedbackCode = feedbackIsGood(cmdStr, feedback, cmdSeperator, successCode,
										returnCodePosition);
								if (1 == feedbackCode) {
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
							LogUtils.errorLog("CncAssistant(" + ip + ") sendCommand(" + operationName + "-" + cmd
									+ ") ERR:" + e.getMessage());
						} finally {
							if (null != socket) {
								sc.removeSocket(socket);
								socketRespData.remove(socket);
							}

						}

						cmdStopT = System.currentTimeMillis();
						LogUtils.commandLog(ip,
								(1 == feedbackCode ? "OK" : "NG") + LogUtils.separator + operationName
										+ LogUtils.separator + cmdStr + LogUtils.separator + feedback
										+ LogUtils.separator + (cmdStopT - cmdStartT));
						if (0 != feedbackCode)
							break;
					}
				} // end else

				if (1 != feedbackCode)
					break;
			}

			if (successCmds >= totalCmds) {
				success = true;
			}
		} catch (IOException e) {
			return false; // TODO log
		}finally{
			if (null != socket) {
				sc.removeSocket(socket);
				socketRespData.remove(socket);
			}
		}
		
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

		// 1.Get the real-sent command via eliminating the return code
		if ("LastElement".equals(returnCodePosition)) {
			strIndex = cmdStr.lastIndexOf(cmdSeperator);
			cmdSent = cmdStr.substring(0, strIndex < 0 ? 0 : strIndex);
		} else {
			strIndex = cmdStr.indexOf(cmdSeperator);
			cmdSent = cmdStr.substring(strIndex);
		}

		// 2.Check whether the return code stands for success or not
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
					ok = -1;
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
			if ("127.0.0.1".equals(ip) || DebugUtils.getDummyDeviceIP(null).equals(ip))
				ip = portMap.get("" + s.getPort());
			if (null != in)
				LogUtils.debugLog("CncSocketDataHandler_", ip + ":" + s.getPort() + LogUtils.separator + in);
			socketRespData.put(s, in);
			SocketData.setData(in);
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
}
