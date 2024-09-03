package com.cncmes.drv;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;

import com.cncmes.base.CncItems;
import com.cncmes.base.DummyItems;
import com.cncmes.base.RackItems;
import com.cncmes.base.Robot;
import com.cncmes.base.RobotItems;
import com.cncmes.ctrl.SocketClient;
import com.cncmes.data.CncData;
import com.cncmes.data.DevHelper;
import com.cncmes.data.RackMaterial;
import com.cncmes.data.RackProduct;
import com.cncmes.data.RobotData;
import com.cncmes.data.RobotEthernetCmd;
import com.cncmes.data.SocketData;
import com.cncmes.data.SystemConfig;
import com.cncmes.handler.SocketDataHandler;
import com.cncmes.utils.DebugUtils;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.MathUtils;
import com.cncmes.utils.TimeUtils;
import com.cncmes.utils.XmlUtils;

/**
 * Virtual Robot Driver
 * @author LI ZI LONG
 *
 */
public class RobotDummy implements Robot {
	private Map<String,Object> socketRespData= new LinkedHashMap<String,Object>();
	private LinkedHashMap<String,String> portMap = new LinkedHashMap<String,String>();
	private LinkedHashMap<String, Object> commonCfg = new LinkedHashMap<String, Object>();
	private static RobotEthernetCmd robotCmd = null;
	
	private static int cmdRetryCount = 5;
	private static int socketRespTimeout = 2;//seconds
	private static int socketRespTimeInterval = 10;//milliseconds
	
	private static RobotData robotData = RobotData.getInstance();
	private static CncData cncData = CncData.getInstance();
	private static RackMaterial mRack = RackMaterial.getInstance();
	private static RackProduct pRack = RackProduct.getInstance();
	private static RobotDummy robotDummy = new RobotDummy();
	private RobotDummy(){}
	public static RobotDummy getInstance(){
		return robotDummy;
	}
	
	static{
		try {
			XmlUtils.parseRobotEthernetCmdXml();
			robotCmd = RobotEthernetCmd.getInstance();
		} catch (Exception e) {
			LogUtils.errorLog("RobotR2D2 fails to load system config:"+e.getMessage());
		}
	}

	private void getCommonSettings() {
		SystemConfig sysCfg = SystemConfig.getInstance();
		LinkedHashMap<String,Object> config = sysCfg.getCommonCfg();
		cmdRetryCount = Integer.parseInt((String)config.get("cmdRetryCount"));
		socketRespTimeout = Integer.parseInt((String)config.get("socketResponseTimeOut"));
		socketRespTimeInterval = Integer.parseInt((String)config.get("socketResponseInterval"));
	}
	
	@Override
	public boolean pickMaterialFromTray(String ip, int slotNo, String targetName) {
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		paras.put("slotNo", slotNo+"");
		return sendCommand(ip, "pickMaterialFromTray", paras, null, targetName);
	}
	
	@Override
	public boolean putMaterialOntoTray(String ip, int slotNo, String targetName) {
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		paras.put("slotNo", slotNo+"");
		return sendCommand(ip, "putMaterialOntoTray", paras, null, targetName);
	}
	
	@Override
	public boolean pickMaterialFromRack(String ip, String rackId, String rackSlot, String robotSlot, String targetName) {
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		String tagName = getRackTagName(robotData.getItemVal(ip, RobotItems.LINENAME)+"_"+rackId);
		paras.put("tagName", tagName);
		paras.put("rackSlot", rackSlot);
		paras.put("robotSlot", robotSlot);
		return sendCommand(ip, "pickMaterialFromRack", paras, null, targetName);
	}
	
	@Override
	public boolean putMaterialOntoRack(String ip, String rackId, String rackSlot, String robotSlot, String targetName) {
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		String tagName = getRackTagName(robotData.getItemVal(ip, RobotItems.LINENAME)+"_"+rackId);
		paras.put("tagName", tagName);
		paras.put("rackSlot", rackSlot);
		paras.put("robotSlot", robotSlot);
		return sendCommand(ip, "putMaterialOntoRack", paras, null, targetName);
	}
	
	@Override
	public boolean pickMaterialFromMachine(String ip, String machineIp, int workZone, String robotSlot, String targetName) {
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		String tagName = (String) cncData.getItemVal(machineIp, CncItems.TAGNAME);
		paras.put("tagName", tagName);
		paras.put("workZone", workZone+"");
		paras.put("robotSlot", robotSlot);
		return sendCommand(ip, "pickMaterialFromMachine", paras, null, targetName);
	}
	
	@Override
	public boolean putMaterialOntoMachine(String ip, String machineIp, int workZone, String robotSlot, String targetName) {
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		String tagName = (String) cncData.getItemVal(machineIp, CncItems.TAGNAME);
		paras.put("tagName", tagName);
		paras.put("workZone", workZone+"");
		paras.put("robotSlot", robotSlot);
		return sendCommand(ip, "putMaterialOntoMachine", paras, null, targetName);
	}
	
	@Override
	public boolean moveToMachine(String ip, String machineIp, String targetName) {
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		paras.put("machineIp", machineIp);
		if(sendCommand(ip, "moveToMachine", paras, null, targetName)){
			robotData.setData(ip, RobotItems.POSITION, machineIp);
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public boolean moveToRack(String ip, String rackId, String targetName) {
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		paras.put("rackId", rackId);
		if(sendCommand(ip, "moveToRack", paras, null, targetName)){
			robotData.setData(ip, RobotItems.POSITION, "rack_"+rackId);
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public String getBattery(String ip, String targetName) {
		LinkedHashMap<String, String> rtn = new LinkedHashMap<String, String>();
		if(sendCommand(ip, "getBattery", null, rtn, targetName)){
			robotData.setData(ip, RobotItems.BATTERY, rtn.get("getBattery"));
			return rtn.get("getBattery");
		}else{
			return null;
		}
	}
	
	@Override
	public String scanBarcode(String ip, String targetName) {
		LinkedHashMap<String, String> rtn = new LinkedHashMap<String, String>();
		if(sendCommand(ip, "scanBarcode", null, rtn, targetName)){
			return rtn.get("scanBarcode");
		}else{
			return null;
		}
	}
	
	@Override
	public boolean goCharging(String ip, String targetName) {
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		paras.put("tagName", ip);
		if(sendCommand(ip, "goCharging", paras, null, targetName)){
			robotData.setData(ip, RobotItems.POSITION, "charging");
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public boolean stopCharging(String ip, String targetName) {
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		paras.put("tagName", ip);
		return sendCommand(ip, "stopCharging", paras, null, targetName);
	}
	
	private String getRackTagName(String mainKey){
		String tagName = "";
		
		if(null!=mRack.getItemVal(mainKey, RackItems.TAGNAME)){
			tagName = (String) mRack.getItemVal(mainKey, RackItems.TAGNAME);
		}else if(null!=pRack.getItemVal(mainKey, RackItems.TAGNAME)){
			tagName = (String) pRack.getItemVal(mainKey, RackItems.TAGNAME);
		}
		
		return tagName;
	}
	
	private LinkedHashMap<String, String> getOperationOutput(String operationName, String cmdName, String feedback, String cmdSeperator, LinkedHashMap<String, String> outParas){
		if(null==outParas && (cmdName.startsWith("GET_SOC") || cmdName.startsWith("READBCODE"))){
			outParas = new LinkedHashMap<String, String>();
		}
		if(null != outParas){
			if("getBattery".equals(operationName) ||
					"scanBarcode".equals(operationName) ||
					cmdName.startsWith("GET_SOC") ||
					cmdName.startsWith("READBCODE")){
				String[] vals = feedback.split(cmdSeperator);
				if(vals.length > 2){
					String val = vals[vals.length-2];
					if("scanBarcode".equals(operationName) || cmdName.startsWith("READBCODE")){
						String barcode = "", md5 = "", temp = "";
						if(val.length() > 32){
							temp = val.substring(0, val.length()-32);
							md5 = val.substring(val.length()-32);
							if(md5.equals(MathUtils.MD5Encode(temp))) barcode = temp;
						}
						outParas.put(operationName, barcode);
						if("".equals(barcode)) LogUtils.errorLog("Wrong barcode:"+val);
					}else{
						outParas.put(operationName, val);
					}
				}
			}
		}
		return outParas;
	}
	
	private int feedbackIsGood(String cmdStr,String feedback,String cmdSeperator,String successCode,String returnCodePosition){
		int ok = 0;
		int strIndex = 0;
		String rtnCode = "", cmdSent = "", cmdRecieved = "";
		
		if("LastElement".equals(returnCodePosition)){
			strIndex = cmdStr.lastIndexOf(cmdSeperator);
			cmdSent = cmdStr.substring(0, strIndex<0?0:strIndex);
		}else{
			strIndex = cmdStr.indexOf(cmdSeperator);
			cmdSent = cmdStr.substring(strIndex);
		}
		
		if(null != feedback && !"".equals(feedback)){
			if("LastElement".equals(returnCodePosition)){
				strIndex = feedback.lastIndexOf(cmdSeperator);
				rtnCode = feedback.substring(strIndex+1);
				cmdRecieved = feedback.substring(0, strIndex<0?0:strIndex);
			}else{
				strIndex = feedback.indexOf(cmdSeperator);
				rtnCode = feedback.substring(0, (strIndex>1)?(strIndex-1):0);
				cmdRecieved = feedback.substring(strIndex);
			}
			
			if(cmdRecieved.indexOf(cmdSent) >= 0){
				if(successCode.equals(rtnCode)){
					ok = 1;
				}else{
					ok = -1;
				}
			}
		}
		
		return ok;
	}
	
	private int getFlagIndex(int startIdx, int stopIdx, String flag, String[] cmds){
		int flagIdx = -1;
		if(cmds.length>startIdx && cmds.length>stopIdx && stopIdx>=startIdx){
			for(int i=startIdx; i<=stopIdx; i++){
				if(flag.equals(cmds[i].split("#")[0])){
					flagIdx = i;
					break;
				}
			}
		}
		return flagIdx;
	}
	
	private boolean stopCmdLoop(String mainKey, String operation, String cmdName, LinkedHashMap<String, String> outParas){
		boolean stopLoop = true;
		String cmdOutParaVal = "", endLoopCnd = "";
		
		if(null!=outParas){
			String cmdEndLoopCnd = robotCmd.getCommandEndLoopCnd(mainKey, operation, cmdName).trim();
			if(!outParas.isEmpty() && null!=outParas.get(operation)){
				cmdOutParaVal = outParas.get(operation);
				try {
					if(!"".equals(cmdEndLoopCnd)){
						if(cmdEndLoopCnd.startsWith(">=")){
							endLoopCnd = cmdEndLoopCnd.replace(">=", "");
							if(Float.parseFloat(endLoopCnd)>Float.parseFloat(cmdOutParaVal)) stopLoop = false;
						}else if(cmdEndLoopCnd.startsWith("<=")){
							endLoopCnd = cmdEndLoopCnd.replace("<=", "");
							if(Float.parseFloat(endLoopCnd)<Float.parseFloat(cmdOutParaVal)) stopLoop = false;
						}else if(cmdEndLoopCnd.startsWith("==")){
							endLoopCnd = cmdEndLoopCnd.replace("==", "");
							if(Integer.parseInt(endLoopCnd)!=Integer.parseInt(cmdOutParaVal)) stopLoop = false;
						}else if(cmdEndLoopCnd.startsWith(">")){
							endLoopCnd = cmdEndLoopCnd.replace(">", "");
							if(Float.parseFloat(endLoopCnd)>=Float.parseFloat(cmdOutParaVal)) stopLoop = false;
						}else if(cmdEndLoopCnd.startsWith("<")){
							endLoopCnd = cmdEndLoopCnd.replace("<", "");
							if(Float.parseFloat(endLoopCnd)<=Float.parseFloat(cmdOutParaVal)) stopLoop = false;
						}else if(cmdEndLoopCnd.startsWith("=")){
							endLoopCnd = cmdEndLoopCnd.replace("=", "");
							if(Integer.parseInt(endLoopCnd)!=Integer.parseInt(cmdOutParaVal)) stopLoop = false;
						}
					}
				} catch (NumberFormatException e) {
					stopLoop = true;
				}
			}
		}
		
		return stopLoop;
	}
	
	private boolean outputParaMeetsCmpCnd(String mainKey, String operation, String cmdName, LinkedHashMap<String, String> outParas, String cmpCnd){
		boolean meetCnd = false;
		String cmdOutParaVal = "", cmpVal = "";
		
		if(null!=outParas){
			if(!outParas.isEmpty() && null!=outParas.get(operation)){
				cmdOutParaVal = outParas.get(operation);
				try {
					if(!"".equals(cmpCnd)){
						if(cmpCnd.startsWith(">=")){
							cmpVal = cmpCnd.replace(">=", "");
							if(Float.parseFloat(cmdOutParaVal)>=Float.parseFloat(cmpVal)) meetCnd = true;
						}else if(cmpCnd.startsWith("<=")){
							cmpVal = cmpCnd.replace("<=", "");
							if(Float.parseFloat(cmdOutParaVal)<=Float.parseFloat(cmpVal)) meetCnd = true;
						}else if(cmpCnd.startsWith("==")){
							cmpVal = cmpCnd.replace("==", "");
							if(Integer.parseInt(cmdOutParaVal)==Integer.parseInt(cmpVal)) meetCnd = true;
						}else if(cmpCnd.startsWith(">")){
							cmpVal = cmpCnd.replace(">", "");
							if(Float.parseFloat(cmdOutParaVal)>Float.parseFloat(cmpVal)) meetCnd = true;
						}else if(cmpCnd.startsWith("<")){
							cmpVal = cmpCnd.replace("<", "");
							if(Float.parseFloat(cmdOutParaVal)<Float.parseFloat(cmpVal)) meetCnd = true;
						}else if(cmpCnd.startsWith("=")){
							cmpVal = cmpCnd.replace("=", "");
							if(Integer.parseInt(cmdOutParaVal)==Integer.parseInt(cmpVal)) meetCnd = true;
						}
					}
				} catch (NumberFormatException e) {
					meetCnd = false;
				}
			}
		}
		
		return meetCnd;
	}
	
	@Override
	public boolean sendCommand(String ip, String operationName, LinkedHashMap<String, String> inParas, LinkedHashMap<String, String> outParas, String targetName){
		boolean success = false;
		String cmdSeperator = ";",successCode = "0",returnCodePosition = "LastElement";
		LinkedHashMap<RobotItems, Object> robotInfo = robotData.getData(ip);
		
		int port = (null!=robotInfo)?(int)robotInfo.get(RobotItems.PORT):0;
		String robotModel = (null!=robotInfo)?(String)robotInfo.get(RobotItems.MODEL):"";
		if(null != robotInfo && !"getBattery".equals(operationName)) robotData.setData(ip, RobotItems.CMD, operationName);
		
		DevHelper devHelper = DevHelper.getInstance();
		String model = devHelper.getModelByDriver(this.getClass().getName());
		String cmdEndChr = devHelper.getCmdTerminator(model);
		getCommonSettings();
		
		String initParas = "ip",initVals = ip,singleCmd="";
		if(null != inParas && inParas.size() > 0){
			for(String para:inParas.keySet()){
				if("port".equals(para)){
					port = Integer.valueOf(inParas.get(para));
				}else if("model".equals(para)){
					robotModel = inParas.get(para);
				}else if("singleCmd".equals(para)){
					singleCmd = inParas.get(para);
				}else{
					initParas += "," + para;
					initVals += "," + inParas.get(para);
				}
			}
		}
		initParas += ",port";
		initVals += "," + port;
		String[] reInitParas = initParas.split(",");
		String[] reInitVals = initVals.split(",");
		
		commonCfg = (LinkedHashMap<String, Object>) robotCmd.getCommonConfig(robotModel+"#"+targetName);
		if(null != commonCfg && commonCfg.size() > 0){
			cmdSeperator = (String) commonCfg.get("seperator");
			successCode = (String) commonCfg.get("successCode");
			returnCodePosition = (String) commonCfg.get("returnCodePosition");
		}
		
		String dtKey = ip+":"+port;
		portMap.put(""+port, ip);

		SocketClient sc = SocketClient.getInstance();
		SocketDataHandler sdh = sc.getSocketDataHandler(ip, port);
		if(null==sdh){
			sdh = new RobotSocketDataHandler();
			sc.addSocketDataHandler(ip, port, sdh);
		}
		boolean cnnOK = true, sendOK = false;
		if(null == sc.getSocket(ip, port) || null == socketRespData.get(dtKey)){
			try {
				cnnOK = sc.connect(ip, port, sdh, DummyItems.IP_ROBOT, cmdEndChr);
			} catch (IOException e) {
				return false;
			}
		}
		
		RobotEthernetCmd robotCmd = RobotEthernetCmd.getInstance();
		String mainKey = robotModel;
		if(mainKey.indexOf("#")<0) mainKey = robotModel + "#" + targetName;
		String[] cmds = robotCmd.getAllCommands(mainKey, operationName);
		if(!"".equals(singleCmd)) cmds = new String[]{singleCmd};
		
		int totalCmds = cmds.length;
		int successCmds = 0, feedbackCode = 0;
		int loopStart = 0, loopStop = 0, untilIdx = -1;
		int ifIdx = -1, ifBodyStart = 0, ifBodyStop = 0;
		
		boolean doLooping = false, meetIfCnd = false;
		String cmd = "";
		for(int cmdIdx=0; cmdIdx<totalCmds; cmdIdx++){
			cmd = cmds[cmdIdx];
			loopStart = cmdIdx;
			loopStop = cmdIdx;
			doLooping = false;
			meetIfCnd = false;
			if("LOOP".equals(cmd.split("#")[0])){
				successCmds++;
				untilIdx = getFlagIndex(cmdIdx+1, cmds.length-1, "UNTIL", cmds);
				if(untilIdx<totalCmds){
					loopStart = cmdIdx+1;
					loopStop = untilIdx+1;
					doLooping = true;
					LogUtils.commandLog(ip, "OK"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"Rountine started");
				}
				if(!doLooping) continue;
			}
			
			if("IF".equals(cmd.split("#")[0])){
				successCmds++;
				ifIdx = -1;
				ifBodyStart = getFlagIndex(cmdIdx+2, cmds.length-1, "THEN", cmds);
				if(ifBodyStart > (cmdIdx+1)){
					ifBodyStop = getFlagIndex(ifBodyStart+1, cmds.length-1, "ENDIF", cmds);
					if(ifBodyStop > ifBodyStart) ifIdx = cmdIdx+1;
				}
				if(ifIdx < 0){
					LogUtils.commandLog(ip, "NG"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"IF block error(should contain THEN and ENDIF)");
					break;
				}else{
					continue;
				}
			}
			if("THEN".equals(cmd.split("#")[0]) || "ENDIF".equals(cmd.split("#")[0])){
				successCmds++;
				if("ENDIF".equals(cmd.split("#")[0])) ifIdx = -1;
				continue;
			}
			
			do{
				for(int loop=loopStart; loop<=loopStop; loop++){
					cmd = cmds[loop];
					if("UNTIL".equals(cmd.split("#")[0])){
						LogUtils.commandLog(ip, "OK"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"Loop end condition("+robotCmd.getCommandEndLoopCnd(mainKey, operationName, cmds[loopStop]).trim()+") check");
						feedbackCode = 1;
						continue;
					}
					socketRespData.put(dtKey, "");
					sendOK = false;
					feedbackCode = 0;
					if("".equals(cmd)) continue;
					
					String cmdStr = robotCmd.getCommandStr(mainKey, operationName, cmd, reInitParas, reInitVals);
					for(int i=0; i<=cmdRetryCount; i++){
						if(i == 2) cnnOK = false;
						if(i > 0 && 0==feedbackCode && !cnnOK){
							try {
								cnnOK = sc.connect(ip, port, sdh, DummyItems.IP_ROBOT, cmdEndChr);
								sendOK = false;
							} catch (IOException e) {
								cnnOK = false;
							}
							
							LogUtils.commandLog(ip, "NG"+LogUtils.separator+operationName+LogUtils.separator+cmdStr+LogUtils.separator+"Reconnecting "+(cnnOK?"OK":"NG"));
							if(!cnnOK){
								if(i==cmdRetryCount) feedbackCode = -1;
								continue;
							}
						}
						
						String feedback = "";
						try {
							if(!sendOK) sendOK = sc.sendData(ip, port, cmdStr);
							int count = socketRespTimeout * 1000 / socketRespTimeInterval;
							while(count > 0){
								feedback = (String) socketRespData.get(dtKey);
								feedbackCode = feedbackIsGood(cmdStr,feedback,cmdSeperator,successCode,returnCodePosition);
								if(1==feedbackCode){
									outParas = getOperationOutput(operationName, cmd, feedback, cmdSeperator, outParas);
									break;
								}else if(feedbackCode<0){
									break;
								}else{
									try {
										Thread.sleep(socketRespTimeInterval);
									} catch (InterruptedException e) {
									}
									count--;
								}
							}
						} catch (IOException e) {
							feedbackCode = -1;
							LogUtils.errorLog("Robot("+ip+") sendCommand("+operationName+"-"+cmd+") ERR:"+e.getMessage());
						}
						
						LogUtils.commandLog(ip, (1==feedbackCode?"OK":"NG")+LogUtils.separator+operationName+LogUtils.separator+cmdStr+LogUtils.separator+feedback);
						if(0!=feedbackCode) break;
					}
					
					if(1!=feedbackCode){
						doLooping = false;
						break; //Current command timeout or failed
					}
					
					if(untilIdx>0 && loop==(untilIdx+1)){
						if(stopCmdLoop(mainKey,operationName,cmd,outParas)){
							doLooping = false;
							break;
						}
					}
					
					if(ifIdx>0 && loop==ifIdx){
						String cmpCnd = robotCmd.getCommandCndSetting(mainKey, operationName, cmd, "IF_CND");
						meetIfCnd = outputParaMeetsCmpCnd(mainKey,operationName,cmd,outParas,cmpCnd);
						if(!meetIfCnd){
							doLooping = false;
							break;
						}
					}
				}
			}while(doLooping);
			
			if(1 == feedbackCode){
				if(ifIdx<0){
					successCmds += (loopStop - loopStart + 1); 
					cmdIdx = loopStop;
				}else{
					if(!meetIfCnd){
						successCmds += (ifBodyStop - ifBodyStart + 2);
						cmdIdx = ifBodyStop;
					}
					ifIdx = -1;
				}
			}else{
				break; //There is command timeout or failed
			}
		}
		
		if(successCmds>=totalCmds){
			success=true;
			if(!"getBattery".equals(operationName)){
				String battery = getBattery(ip, targetName);
				LogUtils.machiningDataLog("RobotBattery_" + TimeUtils.getCurrentDate("yyyyMMdd") + ".log", TimeUtils.getCurrentDate("yyyyMMddHHmmss")+","+battery+","+robotData.getItemVal(ip, RobotItems.CMD));
			}
		}
		return success;
	}
	
	class RobotSocketDataHandler extends SocketDataHandler{
		@Override
		public void doHandle(String in, Socket s) {
			if(null == s) return;
			String ip = s.getInetAddress().toString().replace("/", "");
			if("127.0.0.1".equals(ip) || DebugUtils.getDummyDeviceIP(DummyItems.IP_ROBOT).equals(ip)) ip = portMap.get(""+s.getPort());
			socketRespData.put(ip+":"+s.getPort(), in);
			SocketData.setData(in);
			LogUtils.debugLog("RobotSocketDataHandler_", ip+":"+s.getPort()+LogUtils.separator+in);
		}
	}
}
