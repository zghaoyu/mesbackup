package com.cncmes.drv;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cncmes.base.CncItems;
import com.cncmes.base.RackItems;
import com.cncmes.base.Robot;
import com.cncmes.base.RobotItems;
import com.cncmes.ctrl.SocketClient;
import com.cncmes.data.CncData;
import com.cncmes.data.RackMaterial;
import com.cncmes.data.RackProduct;
import com.cncmes.data.RobotData;
import com.cncmes.data.RobotDriver;
import com.cncmes.data.RobotEthernetCmd;
import com.cncmes.data.SocketData;
import com.cncmes.data.SystemConfig;
import com.cncmes.gui.MyConfirmDialog;
import com.cncmes.handler.SocketDataHandler;
import com.cncmes.utils.ErrorDesc;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.MathUtils;
import com.cncmes.utils.NetUtils;
import com.cncmes.utils.TimeUtils;
import com.cncmes.utils.XmlUtils;

/**
 * Robot R2D2 Ethernet-Command-Base Driver
 * @author LI ZI LONG
 *
 */
public class RobotR2D2 implements Robot {
//	private Map<String,Object> socketRespData= new LinkedHashMap<String,Object>();
	private Map<Socket, String> socketRespData = new ConcurrentHashMap<Socket, String>();
	private LinkedHashMap<String, Object> commonCfg = new LinkedHashMap<String, Object>();
	private static RobotEthernetCmd robotCmd = null;
	
	private static int cmdRetryCount = 5;
	private static int socketRespTimeout = 2;//seconds
	private static int socketRespTimeInterval = 10;//milliseconds
	
	private final static int RTN_CMD_OK = 0;
	private final static int RTN_COM_CNN_NG = 1;
	private final static int RTN_COM_CNN_TIMEOUT = 2;
	private final static int RTN_CMD_FAIL = 3;
	private final static int RTN_CMD_EMPTY = -1;
	
	private static RobotDriver robotDrv = RobotDriver.getInstance();
	private static RobotData robotData = RobotData.getInstance();
	private static CncData cncData = CncData.getInstance();
	private static RackMaterial mRack = RackMaterial.getInstance();
	private static RackProduct pRack = RackProduct.getInstance();
	private static RobotR2D2 robotR2D2 = new RobotR2D2();
	private static RobotSocketDataHandler handler = robotR2D2.new RobotSocketDataHandler();
	private RobotR2D2(){}
	
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
	
	public static RobotR2D2 getInstance(){
		return robotR2D2;
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
		LinkedHashMap<String, String> rtn = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		String tagName = getRackTagName(robotData.getItemVal(ip, RobotItems.LINENAME)+"_"+rackId);
		paras.put("tagName", tagName);
		paras.put("rackSlot", rackSlot);
		paras.put("robotSlot", robotSlot);
		String msg = "";
		boolean ok = false;
		while(true){
			ok = sendCommand(ip, "pickMaterialFromRack", paras, rtn, targetName);
			if(ok){
				robotData.setData(ip, RobotItems.GRIPMATERIAL, "");
				robotData.updateSlot(ip, Integer.parseInt(robotSlot), rtn.get("pickMaterialFromRack"));
				break;
			}else{
				msg = "机器人从"+rackSlot+"号位上取料失败，请确认"+rackSlot+"号位上是否已准备好物料。\r\n\r\n";
				msg += "重试"+rackSlot+"号位取料操作请点击【是】按钮，\r\n否则请点击【否】终止任务。";
				MyConfirmDialog.showDialog("请确认是否重试【取料】操作", msg);
				if(MyConfirmDialog.OPTION_NO == MyConfirmDialog.getConfirmFlag()){
					robotData.updateSlot(ip, Integer.parseInt(robotSlot), "");
					break;
				}
			}
		}
		return ok;
	}
	
	@Override
	public boolean putMaterialOntoRack(String ip, String rackId, String rackSlot, String robotSlot, String targetName) {
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		String tagName = getRackTagName(robotData.getItemVal(ip, RobotItems.LINENAME)+"_"+rackId);
		paras.put("tagName", tagName);
		paras.put("rackSlot", rackSlot);
		paras.put("robotSlot", robotSlot);
		String msg = "";
		boolean ok = false;
		while(true){
			ok = sendCommand(ip, "putMaterialOntoRack", paras, null, targetName);
			if(ok){
				robotData.setData(ip, RobotItems.GRIPMATERIAL, "");
				robotData.updateSlot(ip, Integer.parseInt(robotSlot), "");
				break;
			}else{
				msg = "机器人卸料到"+rackSlot+"号位上失败，请确认重试卸料操作是否安全。\r\n\r\n";
				msg += "重试卸料操作请点击【是】按钮，\r\n否则请点击【否】终止任务。";
				MyConfirmDialog.showDialog("请确认是否重试【卸料】操作", msg);
				if(MyConfirmDialog.OPTION_NO == MyConfirmDialog.getConfirmFlag()){
					break;
				}
			}
		}
		
		return ok;
	}
	
	@Override
	public boolean pickMaterialFromMachine(String ip, String machineIp, int workZone, String robotSlot, String targetName) {
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		String tagName = (String) cncData.getItemVal(machineIp, CncItems.TAGNAME);
		paras.put("tagName", tagName);
		paras.put("workZone", workZone+"");
		paras.put("robotSlot", robotSlot);
		String msg = "";
		boolean ok = false;
		while(true){
			ok = sendCommand(ip, "pickMaterialFromMachine", paras, null, targetName);
			if(ok){
				break;
			}else{
				msg = "机器人从机床"+workZone+"号位卸料失败，请确认重试卸料操作是否安全。\r\n\r\n";
				msg += "重试给机床卸料操作请点击【是】按钮，\r\n否则请点击【否】终止任务。";
				MyConfirmDialog.showDialog("请确认是否重试【给机床卸料】操作", msg);
				if(MyConfirmDialog.OPTION_NO == MyConfirmDialog.getConfirmFlag()){
					break;
				}
			}
		}
		
		return ok;
	}
	
	@Override
	public boolean putMaterialOntoMachine(String ip, String machineIp, int workZone, String robotSlot, String targetName) {
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		String tagName = (String) cncData.getItemVal(machineIp, CncItems.TAGNAME);
		paras.put("tagName", tagName);
		paras.put("workZone", workZone+"");
		paras.put("robotSlot", robotSlot);
		String msg = "";
		boolean ok = false;
		while(true){
			ok = sendCommand(ip, "putMaterialOntoMachine", paras, null, targetName);
			if(ok){
				break;
			}else{
				msg = "机器人给机床"+workZone+"号位上料失败，请确认重试上料操作是否安全。\r\n\r\n";
				msg += "重试给机床上料操作请点击【是】按钮，\r\n否则请点击【否】终止任务。";
				MyConfirmDialog.showDialog("请确认是否重试【给机床上料】操作", msg);
				if(MyConfirmDialog.OPTION_NO == MyConfirmDialog.getConfirmFlag()){
					break;
				}
			}
		}
		
		return ok;
	}
	
	@Override
	public boolean moveToMachine(String ip, String machineIp, String targetName) {
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		String tagName = (String) cncData.getItemVal(machineIp, CncItems.TAGNAME);
		String chargeTag = (String) robotData.getItemVal(ip, RobotItems.TAGNAME);
		paras.put("chargeTag", chargeTag);
		paras.put("tagName", tagName);
		paras.put("checkDoorStatus", machineIp);
		return sendCommand(ip, "moveToMachine", paras, null, targetName);
	}
	
	@Override
	public boolean moveToRack(String ip, String rackId, String targetName) {
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		String chargeTag = (String) robotData.getItemVal(ip, RobotItems.TAGNAME);
		paras.put("chargeTag", chargeTag);
		paras.put("tagName", chargeTag);
		return sendCommand(ip, "moveToRack", paras, null, targetName);
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
		String chargeTag = (String) robotData.getItemVal(ip, RobotItems.TAGNAME);
		paras.put("chargeTag", chargeTag);
		paras.put("tagName", chargeTag);
		return sendCommand(ip, "goCharging", paras, null, targetName);
	}
	
	@Override
	public boolean stopCharging(String ip, String targetName) {
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		String chargeTag = (String) robotData.getItemVal(ip, RobotItems.TAGNAME);
		paras.put("chargeTag", chargeTag);
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
	
	/**
	 * Check whether UNPARKING and END_CHARGING command can be skipped or not
	 * @param ip IP address of the Robot
	 * @param cmdName current command name
	 * @param singleCmd current command is a single command or not from debugging
	 * @return true if need to execute current command
	 */
	private boolean needToExecuteCmd(String ip, String cmdName, boolean singleCmd){
		boolean needToExecute = true;
		String msg = "";
		
		switch(cmdName.split("#")[0]){
		case "MOVE_UNPARKING":
			if(null==robotData.getItemVal(ip, RobotItems.POS_PARKING)){
				msg = "确定机器人此时是在机床安全门前吗？\r\n\r\n";
				needToExecute = false;
			}
			break;
		case "MOVE_END_CHARGING":
			if(null==robotData.getItemVal(ip, RobotItems.POS_CHARGING)){
				msg = "确定机器人此时是在充电位置充电吗？\r\n\r\n";
				needToExecute = false;
			}
			break;
		}
		
		if(!needToExecute){
			msg += "确定请点击【是】按钮，\r\n否则点击【否】按钮";
			MyConfirmDialog.showDialog("请确认机器人位置", msg);
			if(MyConfirmDialog.OPTION_YES == MyConfirmDialog.getConfirmFlag()) needToExecute = true;
		}
		
		return needToExecute;
	}
	
	/**
	 * Check whether Robot moving command is safe to execute or not
	 * @param ip IP address of the Robot
	 * @param cmdName current command name
	 * @param singleCmd current command is a single command call or not from debugging
	 * @return true if current command is safe to execute
	 */
	private boolean safeToExecuteCmd(String ip, String cmdName, boolean singleCmd){
		boolean safeToExecute = true;
		int msgFlag = 0;
		String msg = "";
		
		boolean robotIsCharging = (null==robotData.getItemVal(ip, RobotItems.POS_CHARGING))?false:true;
		String robotPos = (String) robotData.getItemVal(ip, RobotItems.POSITION);
		if(robotIsCharging){
			switch(cmdName.split("#")[0]){
			case "MOVE_TAR":
				msg = "确定机器人此时是在充电位置充电吗？\r\n\r\n";
				safeToExecute = false;
				break;
			case "MOVE_PARKING":
				msg = "确定机器人此时是在充电位置充电吗？\r\n\r\n";
				safeToExecute = false;
				break;
			case "MOVE_UNPARKING":
				msg = "确定机器人此时是在充电位置充电吗？\r\n\r\n";
				safeToExecute = false;
				break;
			case "PUT_DUT_T":
				msg = "确定机器人此时是在机床门前准备上下料吗？\r\n\r\n";
				safeToExecute = false;
				msgFlag = 1;
				break;
			case "PICK_DUT_T":
				msg = "确定机器人此时是在机床门前准备上下料吗？\r\n\r\n";
				safeToExecute = false;
				msgFlag = 1;
				break;
			}
			
			if(!safeToExecute){
				msg += "确定请点击【是】按钮，\r\n否则点击【否】按钮";
				MyConfirmDialog.showDialog("请确认机器人位置", msg);
				if(0==msgFlag){
					if(MyConfirmDialog.OPTION_NO == MyConfirmDialog.getConfirmFlag()) safeToExecute = true;
				}else if(1==msgFlag){
					if(MyConfirmDialog.OPTION_YES == MyConfirmDialog.getConfirmFlag()) safeToExecute = true;
				}
			}
		}else if(null!=robotPos){
			if(robotPos.startsWith("CNC_")){
				switch(cmdName.split("#")[0]){
				case "PUT_HT_F":
					msg = "确定机器人此时是在充电位置充电吗？\r\n\r\n";
					safeToExecute = false;
					break;
				case "CLOSE_B":
					msg = "确定机器人此时是在充电位置充电吗？\r\n\r\n";
					safeToExecute = false;
					break;
				}
			}else if(robotPos.startsWith("CS") || robotPos.startsWith("HT")){
				switch(cmdName.split("#")[0]){
				case "PUT_DUT_T":
					msg = "确定机器人此时是在机床门前准备上下料吗？\r\n\r\n";
					safeToExecute = false;
					break;
				case "PICK_DUT_T":
					msg = "确定机器人此时是在机床门前准备上下料吗？\r\n\r\n";
					safeToExecute = false;
					break;
				}
			}
			if(!safeToExecute){
				msg += "确定请点击【是】按钮，\r\n否则点击【否】按钮";
				MyConfirmDialog.showDialog("请确认机器人位置", msg);
				if(MyConfirmDialog.OPTION_YES == MyConfirmDialog.getConfirmFlag()) safeToExecute = true;
			}
		}
		
		return safeToExecute;
	}
	
	/**
	 * Check whether the Robot is safe to do Parking or not
	 * @param ip IP address of the Robot
	 * @param cmdName the command to be executed
	 * @param machineIp IP address of the machine(Parking Target)
	 * @param singleCmd current command is a single command or not
	 * @param timeout_s Parking ready check timeout
	 * @return true if it is safe to do Parking(It is always safe for single command)
	 */
	private boolean safeToDoParking(String ip, String cmdName, String machineIp, boolean singleCmd, int timeout_s){
		boolean safe = true;
		
		if(!"".equals(machineIp) && !singleCmd && "MOVE_PARKING".equals(cmdName.split("#")[0])){
			long startT = System.currentTimeMillis();
			long timePassed = 0, timeout_ms = timeout_s * 1000;
			
			if(!"1".equals(cncData.getItemVal(machineIp, CncItems.OP_OPENDOOR))){
				safe = false;
				while(true){
					timePassed = System.currentTimeMillis() - startT;
					if("1".equals(cncData.getItemVal(machineIp, CncItems.OP_OPENDOOR))){
						safe = true;
						break;
					}else{
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
						}
					}
					if(timePassed > timeout_ms) break;
				}
			}
		}
		
		return safe;
	}
	
	private ArrayList<Object> setPositionTag(String ip, String cmdName, boolean singleCmd, String[] parasName, String[] parasVal){
		ArrayList<Object> rtnObj = new ArrayList<Object>();
		rtnObj.add(0, parasName);
		rtnObj.add(1, parasVal);
		
		//TODO Must be very careful of the unparkTag
		if(!singleCmd){
			switch(cmdName.split("#")[0]){
			case "MOVE_UNPARKING":
				rtnObj = setPositionTagEx(ip,"unparkTag",singleCmd,parasName,parasVal);
				break;
			case "MOVE_PARKING":
				rtnObj = setPositionTagEx(ip,"parkTag",singleCmd,parasName,parasVal);
				break;
			case "MOVE_CHARGING":
				rtnObj = addCommandPara(parasName,parasVal,"chargeTag",""+robotData.getItemVal(ip, RobotItems.TAGNAME));
				break;
			case "MOVE_END_CHARGING":
				rtnObj = addCommandPara(parasName,parasVal,"chargeTag",""+robotData.getItemVal(ip, RobotItems.TAGNAME));
				break;
			}
		}
		
		return rtnObj;
	}
	
	private ArrayList<Object> addCommandPara(String[] parasName, String[] parasVal, String addParaName, String addParaVal){
		String str1 = addParaName, str2 = addParaVal;
		if(null!=parasName && null!=parasVal && parasName.length==parasVal.length){
			for(int i=0; i<parasName.length; i++){
				if(!addParaName.equals(parasName[i])){
					str1 += "," + parasName[i];
					str2 += "," + parasVal[i];
				}
			}
		}
		parasName = str1.split(",");
		parasVal = str2.split(",");
		
		ArrayList<Object> rtnObj = new ArrayList<Object>();
		rtnObj.add(0, parasName);
		rtnObj.add(1, parasVal);
		
		return rtnObj;
	}
	
	private ArrayList<Object> setPositionTagEx(String ip, String tagName, boolean singleCmd, String[] parasName, String[] parasVal){
		boolean setOK = false;
		String currPosition = (null!=robotData.getItemVal(ip, RobotItems.POSITION)?(String) robotData.getItemVal(ip, RobotItems.POSITION):"");
		if(!"".equals(currPosition)){
			if(null!=parasName && null!=parasVal && parasName.length==parasVal.length){
				if("".equals(getParaVal(tagName,parasName,parasVal))){
					String str1 = tagName, str2 = currPosition;
					for(int i=0; i<parasName.length; i++){
						str1 += "," + parasName[i];
						str2 += "," + parasVal[i];
					}
					parasName = str1.split(",");
					parasVal = str2.split(",");
					setOK = true;
				}else{
					for(int i=0; i<parasName.length; i++){
						if(tagName.equals(parasName[i])){
							parasVal[i] = currPosition;
							setOK = true;
							break;
						}
					}
				}
			}
		}
		
		ArrayList<Object> rtnObj = null;
		if(setOK){
			rtnObj = new ArrayList<Object>();
			rtnObj.add(0, parasName);
			rtnObj.add(1, parasVal);
		}
		return rtnObj;
	}
	
	private String getCommandParaVal(String mainKey, String operationName, String cmdName, String paraName){
		String paraVal = "";
		
		String cmdParas = robotCmd.getCommandParas(mainKey, operationName, cmdName);
		String[] paras = null, paraVals = null;
		if(!"".equals(cmdParas)){
			paras = cmdParas.split("##")[0].split(";");
			paraVals = cmdParas.split("##")[1].split(";");
			paraVal = getParaVal(paraName,paras,paraVals);
		}
		
		return paraVal;
	}
	
	private LinkedHashMap<String,String> getCommandParas(String mainKey, String operationName, String cmdName, LinkedHashMap<String,String> initParas){
		LinkedHashMap<String,String> parameters = new LinkedHashMap<String,String>();
		
		String cmdParas = robotCmd.getCommandParas(mainKey, operationName, cmdName);
		String[] paras = null, paraVals = null;
		if(!"".equals(cmdParas)){
			paras = cmdParas.split("##")[0].split(";");
			paraVals = cmdParas.split("##")[1].split(";");
			if(null!=initParas && initParas.size()>0){
				for(int n=0; n<paras.length; n++){
					if(null!=initParas.get(paras[n])) paraVals[n]=initParas.get(paras[n]);
				}
			}
			
			for(int i=0; i<paras.length; i++){
				parameters.put(paras[i], paraVals[i]);
			}
		}
		
		return parameters;
	}
	
	/**
	 * Information for Robot Moving Commands
	 * @param ip IP address of the Robot
	 * @param mainKey command configure main key
	 * @param operationName current operation name
	 * @param cmdName current command name
	 * @param parasName input parameters' name of current command
	 * @param parasVal input parameters' value of current command
	 * @param cmdFeedback return code of the execution of current command
	 */
	private void setRobotPosition(String ip, String mainKey, String operationName, String cmdName, String[] parasName, String[] parasVal, int cmdFeedback){
		String tagName = "";
		
		switch(cmdName.split("#")[0]){
		case "MOVE_TAR":
			if(1==cmdFeedback){
				tagName = getParaVal("tagName",parasName,parasVal);
				if("".equals(tagName)) tagName = getCommandParaVal(mainKey,operationName,cmdName,"tagName");
				if("".equals(tagName)) tagName = getCommandParaVal(mainKey,operationName,cmdName,"finalTargetTag");
				robotData.setData(ip, RobotItems.POS_TAR, tagName);
				robotData.setData(ip, RobotItems.POSITION, tagName);
				robotData.setData(ip, RobotItems.POS_CHARGING, null);
				robotData.setData(ip, RobotItems.POS_PARKING, null);
			}
			break;
		case "PUT_DUT_T":
		case "PICK_DUT_T":
		case "MOVE_PARKING":
			robotData.setData(ip, RobotItems.POS_CHARGING, null);
			robotData.setData(ip, RobotItems.POS_PARKING, "Parking");
			if(1==cmdFeedback && null==robotData.getItemVal(ip, RobotItems.POSITION)){
				tagName = getCommandParaVal(mainKey,operationName,cmdName,"parkTag");
				if(!"".equals(tagName)){
					robotData.setData(ip, RobotItems.POS_TAR, tagName);
					robotData.setData(ip, RobotItems.POSITION, tagName);
				}
			}
			break;
		case "PUT_HT_F":
		case "PICK_HT_F":
		case "MOVE_CHARGING":
			if(null==robotData.getItemVal(ip, RobotItems.POS_CHARGING)){
				robotData.setData(ip, RobotItems.POS_CHARGING, ""+robotData.getItemVal(ip, RobotItems.TAGNAME));
				robotData.setData(ip, RobotItems.POSITION, ""+robotData.getItemVal(ip, RobotItems.TAGNAME));
			}
			robotData.setData(ip, RobotItems.POS_PARKING, null);
			break;
		case "MOVE_UNPARKING":
			if(1==cmdFeedback){
				robotData.setData(ip, RobotItems.POS_CHARGING, null);
				robotData.setData(ip, RobotItems.POS_PARKING, null);
				if(null==robotData.getItemVal(ip, RobotItems.POSITION)){
					tagName = getCommandParaVal(mainKey,operationName,cmdName,"unparkTag");
					if(!"".equals(tagName)){
						robotData.setData(ip, RobotItems.POS_TAR, tagName);
						robotData.setData(ip, RobotItems.POSITION, tagName);
					}
				}
			}
			break;
		case "MOVE_END_CHARGING":
			if(1==cmdFeedback){
				robotData.setData(ip, RobotItems.POS_CHARGING, null);
				robotData.setData(ip, RobotItems.POS_PARKING, null);
			}
			break;
		}
	}
	
	private String getParaVal(String paraName, String[] parasName, String[] parasVal){
		String val = "";
		
		for(int i=0; i<parasName.length; i++){
			if(paraName.equals(parasName[i])){
				val = parasVal[i];
				break;
			}
		}
		
		return val;
	}
	
	private LinkedHashMap<String, String> getOperationOutput(String operationName, String cmdName, String feedback, String cmdSeperator, LinkedHashMap<String, String> outParas){
		if(null==outParas && (cmdName.startsWith("GET_SOC") 
				|| cmdName.startsWith("READBCODE") 
				|| cmdName.startsWith("CLOSE_B") 
				|| cmdName.startsWith("LAST_CMD"))){
			outParas = new LinkedHashMap<String, String>();
		}
		
		if(null != outParas){
			if("getBattery".equals(operationName) ||
					"scanBarcode".equals(operationName) ||
					cmdName.startsWith("LAST_CMD") ||
					cmdName.startsWith("GET_SOC") ||
					cmdName.startsWith("CLOSE_B") ||
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
					}else if(cmdName.startsWith("CLOSE_B")){
						String barcode1 = vals[vals.length-2];
						String barcode2 = vals[vals.length-3];
						outParas.put(operationName, barcode1+";"+barcode2);
					}else if("getBattery".equals(operationName) || cmdName.startsWith("GET_SOC")){
						outParas.put(operationName, val);
					}else{
						outParas.put(operationName, feedback);
					}
				}
			}
		}
		return outParas;
	}
	
	/**
	 * 
	 * @param cmdStr the command was sent
	 * @param feedback the return of the command
	 * @param cmdSeperator the seperator of the command
	 * @param successCode the success code of the command
	 * @param returnCodePosition the position of the command return code
	 * @return 0:No feedback yet; 1:success; -1:failed
	 */
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
			
			if(cmdStr.indexOf("LAST_CMD") >= 0){
				ok = 1;
			}else{
				if(cmdRecieved.indexOf(cmdSent) >= 0){
					if(successCode.equals(rtnCode)){
						ok = 1;
					}else{
						ok = -1;
					}
				}
			}
		}
		
		return ok;
	}
	
	private int getCmdReturnCode(String feedback,String cmdSeperator,String returnCodePosition){
		int strIndex = 0, code = -1;
		String rtnCode = "";
		
		if(null != feedback && !"".equals(feedback)){
			if("LastElement".equals(returnCodePosition)){
				strIndex = feedback.lastIndexOf(cmdSeperator);
				rtnCode = feedback.substring(strIndex+1);
			}else{
				strIndex = feedback.indexOf(cmdSeperator);
				rtnCode = feedback.substring(0, (strIndex>1)?(strIndex-1):0);
			}
			
			code = Integer.parseInt(rtnCode);
		}
		
		return code;
	}
	
	private String doSleeping(String sleepTime, String splitor){
		int sleepStart = 0, sleepStop = 0, timeNow = 0;
		String sleepT = "";
		boolean bCrossMidNight = false;
		
		if(null!=sleepTime && sleepTime.indexOf("-")>=0 && sleepTime.indexOf(":")>=0){
			String[] timePeriods = sleepTime.split(splitor);
			String[] sleepTs = null;
			timeNow = Integer.parseInt(TimeUtils.getCurrentDate("HHmm"));
			for(int i=0; i<timePeriods.length; i++){
				if(timePeriods[i].indexOf("-")>=0 && timePeriods[i].indexOf(":")>=0){
					sleepTs = timePeriods[i].split("-");
					sleepStart = Integer.parseInt(sleepTs[0].replace(":", "").trim());
					sleepStop = Integer.parseInt(sleepTs[1].replace(":", "").trim());
					bCrossMidNight = false;
					if(sleepStart>sleepStop) bCrossMidNight = true;
					
					if(!bCrossMidNight && timeNow >= sleepStart && timeNow <= sleepStop
						|| bCrossMidNight && (timeNow>=sleepStart && timeNow<=2359 || timeNow>=0 && timeNow<=sleepStop)){
						while(true){
							timeNow = Integer.parseInt(TimeUtils.getCurrentDate("HHmm"));
							if(!bCrossMidNight && (timeNow > sleepStop || timeNow < sleepStart)) break;
							if(bCrossMidNight && timeNow>sleepStop && timeNow<sleepStart) break;
							try {
								Thread.sleep(10000);
							} catch (InterruptedException e) {
							}
						}
						sleepT = timePeriods[i];
					}
				}
			}
		}
		
		return sleepT;
	}
	
	private boolean needToSleep(String sleepTime, String splitor){
		int sleepStart = 0, sleepStop = 0, timeNow = 0;
		boolean needSleep = false;
		
		if(null!=sleepTime && sleepTime.indexOf("-")>=0 && sleepTime.indexOf(":")>=0){
			String[] timePeriods = sleepTime.split(splitor);
			String[] sleepTs = null;
			timeNow = Integer.parseInt(TimeUtils.getCurrentDate("HHmm"));
			for(int i=0; i<timePeriods.length; i++){
				if(timePeriods[i].indexOf("-")>=0 && timePeriods[i].indexOf(":")>=0){
					sleepTs = timePeriods[i].split("-");
					sleepStart = Integer.parseInt(sleepTs[0].replace(":", "").trim());
					sleepStop = Integer.parseInt(sleepTs[1].replace(":", "").trim());
					if(sleepStart>sleepStop){
						if(timeNow>=sleepStart && timeNow<=2359 || timeNow>=0 && timeNow<=sleepStop){
							needSleep = true;
							break;
						}
					}else{
						if(timeNow >= sleepStart && timeNow <= sleepStop){
							needSleep = true;
							break;
						}
					}
				}
			}
		}
		
		return needSleep;
	}
	
	private int getCommandIndex(int start, int stop, String[] cmds, String cmdName){
		int cmdIdx = -1;
		if(start > stop){
			for(int i=start; i>=stop; i--){
				if(cmds[i].startsWith(cmdName)){
					cmdIdx = i;
					break;
				}
			}
		}else{
			for(int i=start; i<=stop; i++){
				if(cmds[i].startsWith(cmdName)){
					cmdIdx = i;
					break;
				}
			}
		}
		return cmdIdx;
	}
	
	@Override
	public String getLastCmd(String ip, String targetName) {
		LinkedHashMap<String, String> rtn = new LinkedHashMap<String, String>();
		if(sendCommand(ip, "getLastCmd", null, rtn, targetName)){
			return rtn.get("getLastCmd");
		}else{
			return null;
		}
	}
	
	@SuppressWarnings("resource")
	@Override
	public boolean sendCommand(String ip, String operationName, LinkedHashMap<String, String> inParas, LinkedHashMap<String, String> outParas, String targetName){
		boolean success = false;
		long cmdStartT = 0, cmdStopT = 0;
		String cmdSeperator = ";",successCode = "0",returnCodePosition = "LastElement";
		String checkDoorStatus_machineIP = "", sleepTime = "";
		LinkedHashMap<RobotItems, Object> robotInfo = robotData.getData(ip);
		
		int port = (null!=robotInfo)?(int)robotInfo.get(RobotItems.PORT):0;
		String robotModel = (null!=robotInfo)?(String)robotInfo.get(RobotItems.MODEL):"";
		String cmdEndChr = robotDrv.getCmdTerminator(robotModel);
		if(null != robotInfo) robotData.setData(ip, RobotItems.CMD, operationName);
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
					if("checkDoorStatus".equals(para)) checkDoorStatus_machineIP = inParas.get(para);
					initParas += "," + para;
					initVals += "," + inParas.get(para);
				}
			}
		}
		initParas += ",port";
		initVals += "," + port;
		String[] reInitParas = initParas.split(",");
		String[] reInitVals = initVals.split(",");
		
		String mainKey = robotModel;
		if(mainKey.indexOf("#")<0) mainKey = robotModel + "#" + targetName;
		
		int robotCmdTimeout = socketRespTimeout; //seconds
		commonCfg = (LinkedHashMap<String, Object>) robotCmd.getCommonConfig(mainKey);
		if(null != commonCfg && commonCfg.size() > 0){
			cmdSeperator = (String) commonCfg.get("seperator");
			successCode = (String) commonCfg.get("successCode");
			returnCodePosition = (String) commonCfg.get("returnCodePosition");
			socketRespTimeout = (Integer.parseInt(""+commonCfg.get("cmdTimeout"))>0?Integer.parseInt(""+commonCfg.get("cmdTimeout")):socketRespTimeout);
			sleepTime = (String) commonCfg.get("sleepTime");
		}
		
//		String dtKey = ip+":"+port;
		SocketClient sc = SocketClient.getInstance();
//		SocketDataHandler sdh = sc.getSocketDataHandler(ip, port);
//		if(null == sdh){
//			sdh = new RobotSocketDataHandler();
//			sc.addSocketDataHandler(ip, port, sdh);
//		}
		
		boolean cnnOK = true, sendOK = false;
//		if(null == sc.getSocket(ip, port) || null == socketRespData.get(dtKey)){
//			try {
//				cnnOK = sc.connect(ip, port, sdh, null, cmdEndChr);
//			} catch (IOException e) {
//				return false;
//			}
//		}
		
		String[] cmds = robotCmd.getAllCommands(mainKey, operationName);
		if(!"".equals(singleCmd)) cmds = new String[]{singleCmd};
		
		sc = SocketClient.getInstance();
		int totalCmds = cmds.length;
		int successCmds = 0, feedbackCode = 0;
		int loopStart = 0, loopStop = 0, untilIdx = -1;
		int ifCndIndex = -1, ifBodyStart = 0, ifBodyStop = 0;
		int lastRetryIndex = -1, retryIndex = -1, continueRetryCnt = 0;
		int[] cmdDoneCnt = new int[totalCmds];
		ArrayList<Integer> cmdDelay = null;
		
		Socket socket = null;
		boolean doLooping = false, meetIfCnd = false;
		String cmd = "", debugInfo = "", lastCmd = "", sleepPeriod = "";
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
					if(getFlagIndex(loopStart, loopStop, "IF", cmds)<0){
						doLooping = true;
						LogUtils.commandLog(ip, "OK"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"Rountine started");
					}else{
						LogUtils.commandLog(ip, "NG"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"Rountine can not contain IF block");
						break;
					}
				}
				if(!doLooping) continue;
			}
			
			if("IF".equals(cmd.split("#")[0])){
				successCmds++;
				ifCndIndex = -1;
				ifBodyStart = getFlagIndex(cmdIdx+2, cmds.length-1, "THEN", cmds);
				if(ifBodyStart > (cmdIdx+1)){
					ifBodyStop = getFlagIndex(ifBodyStart+1, cmds.length-1, "ENDIF", cmds);
					if(ifBodyStop > ifBodyStart) ifCndIndex = cmdIdx+1;
				}
				if(ifCndIndex < 0){
					LogUtils.commandLog(ip, "NG"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"IF block error(should contain THEN and ENDIF)");
					break;
				}else{
					LogUtils.commandLog(ip, "OK"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"IF condition("+robotCmd.getCommandCndSetting(mainKey, operationName, cmds[ifCndIndex], "IF_CND").trim()+") check");
					continue;
				}
			}
			if("THEN".equals(cmd.split("#")[0]) || "ENDIF".equals(cmd.split("#")[0])){
				successCmds++;
				if("ENDIF".equals(cmd.split("#")[0])) ifCndIndex = -1;
				continue;
			}
			if("SLEEPING".equals(cmd.split("#")[0])){
				successCmds++;
				if(needToSleep(sleepTime,cmdSeperator) && "".equals(singleCmd)){
					LogUtils.commandLog(ip, "OK"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"Sleeping("+sleepTime+") started");
					sleepPeriod = doSleeping(sleepTime,cmdSeperator);
					if(!"".equals(sleepPeriod)){
						LogUtils.commandLog(ip, "OK"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"Sleeping("+sleepPeriod+") done");
					}else{
						LogUtils.commandLog(ip, "OK"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"Sleeping("+sleepTime+") done");
					}
				}
				continue;
			}
			
			do{
				for(int loop=loopStart; loop<=loopStop; loop++){
					cmd = cmds[loop];
					cmdStartT = System.currentTimeMillis();
					if("UNTIL".equals(cmd.split("#")[0])){
						LogUtils.commandLog(ip, "OK"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"Loop end condition("+robotCmd.getCommandEndLoopCnd(mainKey, operationName, cmds[loopStop]).trim()+") check");
						feedbackCode = 1;
						continue;
					}
					if(!needToExecuteCmd(ip,cmd,!"".equals(singleCmd))){
						LogUtils.commandLog(ip, "OK"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"No need to execute");
						feedbackCode = 1;
						continue;
					}
					if(!safeToExecuteCmd(ip,cmd,!"".equals(singleCmd))){
						LogUtils.commandLog(ip, "NG"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"Risky to execute");
						feedbackCode = -1;
						doLooping = false;
						break;
					}
					ArrayList<Object> parasObj = setPositionTag(ip,cmd,!"".equals(singleCmd),reInitParas,reInitVals);
					if(null==parasObj){
						LogUtils.commandLog(ip, "NG"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"Set position tag failed");
						feedbackCode = -1;
						doLooping = false;
						break;
					}else{
						reInitParas = (String[]) parasObj.get(0);
						reInitVals = (String[]) parasObj.get(1);
					}
					if(!safeToDoParking(ip,cmd,checkDoorStatus_machineIP,!"".equals(singleCmd),30)){
						LogUtils.commandLog(ip, "NG"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"Machine door is not opened");
						feedbackCode = -1;
						doLooping = false;
						break;
					}
					
					socketRespData.put(socket, "");
					sendOK = false;
					feedbackCode = 0;
					if("".equals(cmd)){
						LogUtils.commandLog(ip, "NG"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"NULL cmd detected");
						feedbackCode = -1;
						doLooping = false;
						break;
					}
					
					//Single Command Mode will skip the sleeping routine
					if(needToSleep(sleepTime,cmdSeperator) && "".equals(singleCmd)){
						if(null != robotInfo) robotData.setData(ip, RobotItems.SLEEP, "Sleeping");
						String sleepProgName = robotCmd.getSleepRoutine(mainKey, operationName, cmd);
						if(!"".equals(sleepProgName)){
							LinkedHashMap<String,String> myParas = new LinkedHashMap<String,String>();
							if(null!=robotData.getItemVal(ip, RobotItems.POSITION)){
								myParas.put("finalTargetTag", ""+robotData.getItemVal(ip, RobotItems.POSITION));
							}
							if(RTN_CMD_FAIL==executeSubProgram(ip, port, "subProgram", sleepProgName, mainKey, targetName, myParas)){
								feedbackCode = -1;
								doLooping = false;
								break;
							}
							LogUtils.commandLog(ip, "OK"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"Sleeping("+sleepTime+") started");
							sleepPeriod = doSleeping(sleepTime,cmdSeperator);
							if(!"".equals(sleepPeriod)){
								LogUtils.commandLog(ip, "OK"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"Sleeping("+sleepPeriod+") done");
							}else{
								LogUtils.commandLog(ip, "OK"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"Sleeping("+sleepTime+") done");
							}
						}
					}
					if(null != robotInfo) robotData.setData(ip, RobotItems.SLEEP, "");
					
					cmdDelay = robotCmd.getCommandDelay(mainKey, operationName, cmd);
					robotCmdTimeout = robotCmd.getCommandTimeout(mainKey, operationName, cmd, socketRespTimeout);
					if(null!=cmdDelay && cmdDelay.get(0)>0){
						try {
							Thread.sleep(cmdDelay.get(0)*1000);
						} catch (InterruptedException e1) {
						}
					}
					
					//Connect the API server first for every command
					for(int i=0; i<2; i++){
						try {
							socket= sc.connect(ip, port, handler, cmdEndChr);
							if(null!=socket) cnnOK = true;
						} catch (IOException e) {
							LogUtils.commandLog(ip, "NG"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"Connect("+ip+":"+port+") Failed:"+e.getMessage());
							if(i==0){
								if(!sc.portOfHostOK(ip, port, 60, 10)){
									LogUtils.commandLog(ip, "NG"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"Port("+port+") is not reachable");
									return false;
								}
							}else{
								LogUtils.commandLog(ip, "NG"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"Connect("+ip+":"+port+") Failed:"+e.getMessage());
								return false;
							}
						}
						if(cnnOK) break;
					}
					
					String cmdStr = robotCmd.getCommandStr(mainKey, operationName, cmd, reInitParas, reInitVals);
					String feedback = "", pingInfo = "";
					String retryFromCommand = "";
					retryIndex = -1;
					for(int i=0; i<=cmdRetryCount; i++){
						if(i > 0 && 0 == feedbackCode){//Timeout Retry Sequence
							LogUtils.commandLog(ip, "NG"+LogUtils.separator+operationName+LogUtils.separator+cmdStr+LogUtils.separator+"Timeout Retry_"+i+"/"+cmdRetryCount);
							if("getLastCmd".equals(operationName) && 1==i){
								//Retry Step 1: Check the network
								pingInfo = NetUtils.pingHost(ip);
								if(!"OK".equals(pingInfo)){
									LogUtils.commandLog(ip, "NG"+LogUtils.separator+operationName+LogUtils.separator+cmdStr+LogUtils.separator+"Ping("+ip+") Failed:"+pingInfo);
									//Network checking with 2 hours timeout
									if(NetUtils.waitUntilNetworkOK(ip, 120, 10)){
										LogUtils.commandLog(ip, "NG"+LogUtils.separator+operationName+LogUtils.separator+cmdStr+LogUtils.separator+"Network Resumed");
									}else{
										LogUtils.commandLog(ip, "NG"+LogUtils.separator+operationName+LogUtils.separator+cmdStr+LogUtils.separator+"Network Failed");
										feedbackCode = -1;
										break; //Quit from command failed retry block
									}
								}
								LogUtils.commandLog(ip, "NG"+LogUtils.separator+operationName+LogUtils.separator+cmdStr+LogUtils.separator+"Ping("+ip+") OK");
								
								//Retry Step 2: Check the API server with 60 seconds timeout
								if(!sc.portOfHostOK(ip, port, 60, 10)){
									LogUtils.commandLog(ip, "NG"+LogUtils.separator+operationName+LogUtils.separator+cmdStr+LogUtils.separator+"Port("+port+") is not reachable");
									feedbackCode = -1;
									break; //Quit from command failed retry block
								}else{
									cnnOK = false;//Connection reset already so need to trigger reconnection
								}
								LogUtils.commandLog(ip, "NG"+LogUtils.separator+operationName+LogUtils.separator+cmdStr+LogUtils.separator+"Connect("+ip+":"+port+") OK");
							}
							
							//Retry Step 3: Check with API Server whether cmdStr has been executed successfully or not 
							if(1==i && !"getLastCmd".equals(operationName) && !"getBattery".equals(operationName)){
								feedback = getLastCmd(ip, targetName);
								cnnOK = false;//Connection reset already so need to trigger reconnection
								feedbackCode = feedbackIsGood(cmdStr,feedback,cmdSeperator,successCode,returnCodePosition);
								if(0 == feedbackCode){
									if(null!=feedback && !"".equals(feedback)){
										//Current command is different from last command
										//Execute current command again
										LogUtils.commandLog(ip, "NG"+LogUtils.separator+operationName+LogUtils.separator+cmdStr+LogUtils.separator+"LAST_CMD"+LogUtils.separator+feedback);
									}else{
										//Fail to query last command so fail current command directly
										LogUtils.commandLog(ip, "NG"+LogUtils.separator+operationName+LogUtils.separator+cmdStr+LogUtils.separator+"LAST_CMD failed:API could be dead or running into a dead loop");
										feedbackCode = -1;
										break; //Quit from command failed retry block
									}
								}else if(1 == feedbackCode){
									//Current command is same as last command and has been executed successfully
									outParas = getOperationOutput(operationName, cmd, feedback, cmdSeperator, outParas);
									break; //Quit from command failed retry block
								}else{
									if("MOVE_CHARGING".equals(cmd.split("#")[0])) setRobotPosition(ip,mainKey,operationName,cmd,reInitParas,reInitVals,feedbackCode);
									
									//TODO Be careful of the retry sequence here
									//Current command is same as last command and error happens
									String retrySequenceName = cmd.split("#")[0] + "_RETRY_" + getCmdReturnCode(feedback,cmdSeperator,returnCodePosition);
									if(!"getLastCmd".equals(operationName)) LogUtils.commandLog(ip, (1==feedbackCode?"OK":"NG")+LogUtils.separator+operationName+LogUtils.separator+cmdStr+LogUtils.separator+feedback+LogUtils.separator+retrySequenceName);
									
									LinkedHashMap<String,String> myParas = getCommandParas(mainKey,operationName,cmd,inParas);
									if(null!=robotData.getItemVal(ip, RobotItems.POSITION)){
										myParas.put("finalTargetTag", ""+robotData.getItemVal(ip, RobotItems.POSITION));
									}
									int rtnC = executeSubProgram(ip, port, "retrySequence", retrySequenceName, mainKey, targetName, myParas);
									if(RTN_CMD_FAIL==rtnC || RTN_CMD_EMPTY==rtnC){
										feedbackCode = -1;
										break; //Quit from command failed retry block
									}else if(RTN_CMD_OK==rtnC){
										retryFromCommand = robotCmd.getRetrySequenceParaVal(mainKey, retrySequenceName, "retryFrom");
										if("".equals(retryFromCommand) || retryFromCommand.equals(cmd.split("#")[0])){
											//Retry routine is done successfully so execute current command again
											feedbackCode = 0;
										}else{
											if(doLooping){
												retryIndex = getCommandIndex(loop,loopStart,cmds,retryFromCommand);
											}else{
												retryIndex = getCommandIndex(cmdIdx,0,cmds,retryFromCommand);
											}
											if(retryIndex>=0){
												if(doLooping){
													loop = retryIndex-1;
												}else{
													cmdIdx = retryIndex-1;
												}
												feedbackCode = 1;
												break; //Quit from command failed retry block
											}else{
												if(!"getLastCmd".equals(operationName)) LogUtils.commandLog(ip, (1==feedbackCode?"OK":"NG")+LogUtils.separator+operationName+LogUtils.separator+cmdStr+LogUtils.separator+feedback+LogUtils.separator+retrySequenceName+feedback+LogUtils.separator+"Can't find command "+retryFromCommand);
												feedbackCode = -1;
												break; //Quit from command failed retry block
											}
										}
									}
								}
							}
						} //End of command timeout retry block
						
//						if(i == 2) cnnOK = false; //Disabled reconnecting on 06-March-2018, may need to enable it in the future
						if(i > 0 && (0==feedbackCode || 2==feedbackCode) && !cnnOK){
							try {
								socket = sc.connect(ip, port, handler, cmdEndChr);
								if(null!= socket) cnnOK = true; 
								sendOK = false;
							} catch (IOException e) {
								cnnOK = false;
							}
							LogUtils.commandLog(ip, "NG"+LogUtils.separator+operationName+LogUtils.separator+cmdStr+LogUtils.separator+"Reconnecting("+ip+":"+port+") "+(cnnOK?"OK":"NG"));
							if(!cnnOK){
								if(i==cmdRetryCount) feedbackCode = -1;
								continue;
							}
						}
						
						feedback = "";
						try {
							if(!sendOK){
								sendOK = sc.sendData(socket, cmdStr, cmdEndChr);
								cmdStartT = System.currentTimeMillis();
							}
							int count = robotCmdTimeout * 1000 / socketRespTimeInterval;
							while(count > 0){ //Command timeout check block
								feedback = (String) socketRespData.get(socket);
								feedbackCode = feedbackIsGood(cmdStr,feedback,cmdSeperator,successCode,returnCodePosition);
								if(1 == feedbackCode){
									outParas = getOperationOutput(operationName, cmd, feedback, cmdSeperator, outParas);
									break; //Quit from command timeout check block
								}else if(feedbackCode < 0){ //Current command failed
									if("MOVE_CHARGING".equals(cmd.split("#")[0])) setRobotPosition(ip,mainKey,operationName,cmd,reInitParas,reInitVals,feedbackCode);
									
									//TODO Be careful of the retry sequence here
									String retrySequenceName = cmd.split("#")[0] + "_RETRY_" + getCmdReturnCode(feedback,cmdSeperator,returnCodePosition);
									LinkedHashMap<String,String> myParas = getCommandParas(mainKey,operationName,cmd,inParas);
									if(null!=robotData.getItemVal(ip, RobotItems.POSITION)){
										myParas.put("finalTargetTag", ""+robotData.getItemVal(ip, RobotItems.POSITION));
									}
									if(RTN_CMD_OK==executeSubProgram(ip, port, "retrySequence", retrySequenceName, mainKey, targetName, myParas)){
										if(i<cmdRetryCount && continueRetryCnt<cmdRetryCount){//Execute current command again
											if(!"getLastCmd".equals(operationName)) LogUtils.commandLog(ip, (1==feedbackCode?"OK":"NG")+LogUtils.separator+operationName+LogUtils.separator+cmdStr+LogUtils.separator+feedback+LogUtils.separator+retrySequenceName+LogUtils.separator+" OK");
											retryFromCommand = robotCmd.getRetrySequenceParaVal(mainKey, retrySequenceName, "retryFrom");
											if("".equals(retryFromCommand) || retryFromCommand.equals(cmd.split("#")[0])){
												//Retry routine is done successfully so execute current command again
												feedbackCode = 2;
												cnnOK = false;
											}else{
												if(doLooping){
													retryIndex = getCommandIndex(loop,loopStart,cmds,retryFromCommand);
												}else{
													retryIndex = getCommandIndex(cmdIdx,0,cmds,retryFromCommand);
												}
												if(retryIndex>=0){
													if(doLooping){
														loop = retryIndex-1;
													}else{
														cmdIdx = retryIndex-1;
													}
													feedbackCode = 1;
												}else{
													if(!"getLastCmd".equals(operationName)) LogUtils.commandLog(ip, (1==feedbackCode?"OK":"NG")+LogUtils.separator+operationName+LogUtils.separator+cmdStr+LogUtils.separator+feedback+LogUtils.separator+retrySequenceName+feedback+LogUtils.separator+"Can't find command "+retryFromCommand);
												}
											}
										}else{
											if(!"getLastCmd".equals(operationName)) LogUtils.commandLog(ip, (1==feedbackCode?"OK":"NG")+LogUtils.separator+operationName+LogUtils.separator+cmdStr+LogUtils.separator+feedback+LogUtils.separator+retrySequenceName+LogUtils.separator+" SKIP");
										}
									}else{
										if(!"getLastCmd".equals(operationName)) LogUtils.commandLog(ip, (1==feedbackCode?"OK":"NG")+LogUtils.separator+operationName+LogUtils.separator+cmdStr+LogUtils.separator+feedback+LogUtils.separator+retrySequenceName+LogUtils.separator+" NG");
									}
									break; //Quit from command timeout check block
								}else{
									try {
										Thread.sleep(socketRespTimeInterval);
									} catch (InterruptedException e) {
									}
									count--;
								}
							}
						} catch (IOException e) { //sendData error
							feedbackCode = -1;
							LogUtils.errorLog("Robot("+ip+") sendCommand("+operationName+"-"+cmd+") ERR:"+e.getMessage());
						} finally{
							if(null != socket){
								sc.removeSocket(socket);
								socketRespData.remove(socket);
							}
						}
						
						if(0 != feedbackCode && 2 != feedbackCode) break; //Current command success or failed
					} //End of command failed retry block
					
					//Retry sequence has been triggered and successfully done
					if(retryIndex>=0){
						if(1==feedbackCode){
							if(lastRetryIndex!=retryIndex){
								continueRetryCnt = 0;
							}else{
								continueRetryCnt++;
							}
							lastRetryIndex = retryIndex;
						}
						if(doLooping){
							continue;
						}else{
							break; //Quit from command loop block
						}
					}
					
					sc.removeSocket(socket);
					setRobotPosition(ip,mainKey,operationName,cmd,reInitParas,reInitVals,feedbackCode);
					cmdStopT = System.currentTimeMillis();
					debugInfo = LogUtils.separator + (cmdStopT-cmdStartT);
					if(LogUtils.getDebugLogFlag()){
						if(!"getLastCmd".equals(operationName)){
							lastCmd = getLastCmd(ip, targetName);
							debugInfo += LogUtils.separator + (feedback.startsWith(lastCmd)?"LAST_CMD:SAME":("LAST_CMD:DIFF"+LogUtils.separator+lastCmd));
						}
					}
					if(feedbackCode<0) debugInfo += LogUtils.separator + ErrorDesc.getR2D2ErrorDesc(getCmdReturnCode(feedback,cmdSeperator,returnCodePosition));
					if(!"getLastCmd".equals(operationName)) LogUtils.commandLog(ip, (1==feedbackCode?"OK":"NG")+LogUtils.separator+operationName+LogUtils.separator+cmdStr+LogUtils.separator+feedback+debugInfo);
					if(1 != feedbackCode){
						doLooping = false;
						break; //Current command timeout or failed, quit from command loop block
					}
					
					if(null!=cmdDelay && cmdDelay.get(1)>0){
						try {
							Thread.sleep(cmdDelay.get(1)*1000);
						} catch (InterruptedException e1) {
						}
					}
					
					//Looping stop condition checking
					if(untilIdx>0 && loop==(untilIdx+1)){
						if(stopCmdLoop(mainKey,operationName,cmd,outParas)){
							doLooping = false;
							break; //Quit from command loop block
						}
					}
					
					//IF condition checking
					if(ifCndIndex>0 && loop==ifCndIndex){
						String cmpCnd = robotCmd.getCommandCndSetting(mainKey, operationName, cmd, "IF_CND");
						meetIfCnd = outputParaMeetsCmpCnd(mainKey,operationName,cmd,outParas,cmpCnd);
						if(!meetIfCnd){
							doLooping = false;
							break; //Quit from command loop block
						}
					}
				} //End of command loop block
			}while(doLooping); //End of loop block repeating
			
			if(1 == feedbackCode){
				if(ifCndIndex<0){
					successCmds += (loopStop - loopStart + 1); 
					if(retryIndex<0) cmdIdx = loopStop;
				}else{
					if(!meetIfCnd){// If condition is not satisfied so skip doing the if body block
						successCmds += (ifBodyStop - ifBodyStart + 2);
						if(retryIndex<0) cmdIdx = ifBodyStop;
					}else{
						successCmds++;
					}
					ifCndIndex = -1;
				}
				if(retryIndex<0){ //Current command is done without retry
					continueRetryCnt = 0;
					lastRetryIndex = -1;
					cmdDoneCnt[cmdIdx] = successCmds;
				}else{ //Current command failed but retry sequence is successfully done
					if(retryIndex==0){ //Retry from the very beginning
						successCmds = 0;
					}else{ //Retry from the specified command
						successCmds = cmdDoneCnt[retryIndex-1];
					}
				}
			}else{
				break; //There is command timeout or failed
			}
		}
		
		if(successCmds >= totalCmds){
			success = true;
			if(LogUtils.getDebugLogFlag()){
				if(!"getBattery".equals(operationName) && !"getLastCmd".equals(operationName)){
					String battery = getBattery(ip, targetName);
					LogUtils.machiningDataLog("RobotBattery_" + TimeUtils.getCurrentDate("yyyyMMdd") + ".log", TimeUtils.getCurrentDate("yyyyMMddHHmmss")+","+battery+","+robotData.getItemVal(ip, RobotItems.CMD));
				}
			}
		}
		return success;
	}
	
	@SuppressWarnings("resource")
	private int executeSubProgram(String ip, int port, String operationName, String progName, String mainKey, String targetName, LinkedHashMap<String, String> initParas){
		int success = RTN_CMD_FAIL;
		LinkedHashMap<String, String> outParas = new LinkedHashMap<String, String>();
		long cmdStartT = 0, cmdStopT = 0;
		String cmdSeperator = ";",successCode = "0",returnCodePosition = "LastElement";
		String sleepTime = "", sleepPeriod = "";
		LinkedHashMap<RobotItems, Object> robotInfo = robotData.getData(ip);
		
		String robotModel = (null!=robotInfo)?(String)robotInfo.get(RobotItems.MODEL):"";
		String cmdEndChr = robotDrv.getCmdTerminator(robotModel);
		if(null != robotInfo) robotData.setData(ip, RobotItems.CMD, operationName);
		getCommonSettings();
		
		int robotCmdTimeout = socketRespTimeout; //seconds
		commonCfg = (LinkedHashMap<String, Object>) robotCmd.getCommonConfig(mainKey);
		if(null != commonCfg && commonCfg.size() > 0){
			cmdSeperator = (String) commonCfg.get("seperator");
			successCode = (String) commonCfg.get("successCode");
			returnCodePosition = (String) commonCfg.get("returnCodePosition");
			socketRespTimeout = (Integer.parseInt(""+commonCfg.get("cmdTimeout"))>0?Integer.parseInt(""+commonCfg.get("cmdTimeout")):socketRespTimeout);
			sleepTime = (String) commonCfg.get("sleepTime");
		}
		
		String dtKey = ip+":"+port;
		String[] cmds = null;
		if(progName.contains("_RETRY_")){
			cmds = robotCmd.getRetrySequence(mainKey, progName);
		}else{
			cmds = robotCmd.getSubProgram(mainKey, progName);
		}
		if(1==cmds.length && "".equals(cmds[0])) return RTN_CMD_EMPTY;
		
		boolean cnnOK = true, sendOK = false;
		SocketClient sc = SocketClient.getInstance();
//		SocketDataHandler sdh = sc.getSocketDataHandler(ip, port);
		int totalCmds = cmds.length;
		int successCmds = 0, feedbackCode = 0;
		int loopStart = 0, loopStop = 0, untilIdx = -1;
		int ifIdx = -1, ifBodyStart = 0, ifBodyStop = 0;
		ArrayList<Integer> cmdDelay = null;
		Socket socket = null;
		boolean doLooping = false, meetIfCnd = false;
		String cmd = "", debugInfo = "", lastCmd = "";
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
					if(getFlagIndex(loopStart, loopStop, "IF", cmds)<0){
						doLooping = true;
						LogUtils.commandLog(ip, "OK"+LogUtils.separator+progName+LogUtils.separator+cmd+LogUtils.separator+"Rountine started");
					}else{
						LogUtils.commandLog(ip, "NG"+LogUtils.separator+progName+LogUtils.separator+cmd+LogUtils.separator+"Rountine can not contain IF block");
						break;
					}
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
					LogUtils.commandLog(ip, "NG"+LogUtils.separator+progName+LogUtils.separator+cmd+LogUtils.separator+"IF block error(should contain THEN and ENDIF)");
					break;
				}else{
					LogUtils.commandLog(ip, "OK"+LogUtils.separator+progName+LogUtils.separator+cmd+LogUtils.separator+"IF condition("+robotCmd.getCommandCndSetting(mainKey, operationName, cmds[ifIdx], "IF_CND").trim()+") check");
					continue;
				}
			}
			if("THEN".equals(cmd.split("#")[0]) || "ENDIF".equals(cmd.split("#")[0])){
				successCmds++;
				if("ENDIF".equals(cmd.split("#")[0])) ifIdx = -1;
				continue;
			}
			if("SLEEPING".equals(cmd.split("#")[0])){
				successCmds++;
				if(needToSleep(sleepTime,cmdSeperator)){
					LogUtils.commandLog(ip, "OK"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"Sleeping("+sleepTime+") started");
					sleepPeriod = doSleeping(sleepTime,cmdSeperator);
					if(!"".equals(sleepPeriod)){
						LogUtils.commandLog(ip, "OK"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"Sleeping("+sleepPeriod+") done");
					}else{
						LogUtils.commandLog(ip, "OK"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"Sleeping("+sleepTime+") done");
					}
				}
				continue;
			}
			
			do{
				for(int loop=loopStart; loop<=loopStop; loop++){
					cmd = cmds[loop];
					cmdStartT = System.currentTimeMillis();
					if("UNTIL".equals(cmd.split("#")[0])){
						LogUtils.commandLog(ip, "OK"+LogUtils.separator+progName+LogUtils.separator+cmd+LogUtils.separator+"Loop end condition("+robotCmd.getCommandEndLoopCnd(mainKey, operationName, cmds[loopStop]).trim()+") check");
						feedbackCode = 1;
						continue;
					}
					if(!needToExecuteCmd(ip,cmd,false)){
						LogUtils.commandLog(ip, "OK"+LogUtils.separator+progName+LogUtils.separator+cmd+LogUtils.separator+"No need to execute");
						feedbackCode = 1;
						continue;
					}
					if(!safeToExecuteCmd(ip,cmd,false)){
						LogUtils.commandLog(ip, "NG"+LogUtils.separator+progName+LogUtils.separator+cmd+LogUtils.separator+"Risky to execute");
						feedbackCode = -1;
						doLooping = false;
						break;
					}
					
					String cmdParas = robotCmd.getCommandParas(mainKey, operationName, cmd);
					String[] paras = null, paraVals = null;
					if(!"".equals(cmdParas)){
						paras = cmdParas.split("##")[0].split(";");
						paraVals = cmdParas.split("##")[1].split(";");
						if(null!=initParas && initParas.size()>0){
							for(int n=0; n<paras.length; n++){
								if(null!=initParas.get(paras[n])) paraVals[n]=initParas.get(paras[n]);
							}
						}
					}
					
					ArrayList<Object> parasObj = setPositionTag(ip,cmd,false,paras,paraVals);
					if(null==parasObj){
						LogUtils.commandLog(ip, "NG"+LogUtils.separator+operationName+LogUtils.separator+cmd+LogUtils.separator+"Set position tag failed");
						feedbackCode = -1;
						doLooping = false;
						break;
					}else{
						paras = (String[]) parasObj.get(0);
						paraVals = (String[]) parasObj.get(1);
					}
					
					socketRespData.put(socket, "");
					sendOK = false;
					feedbackCode = 0;
					if("".equals(cmd)){
						LogUtils.commandLog(ip, "NG"+LogUtils.separator+progName+LogUtils.separator+cmd+LogUtils.separator+"NULL cmd detected");
						feedbackCode = -1;
						doLooping = false;
						break;
					}
					
					cmdDelay = robotCmd.getCommandDelay(mainKey, operationName, cmd);
					robotCmdTimeout = robotCmd.getCommandTimeout(mainKey, operationName, cmd, socketRespTimeout);
					if(null!=cmdDelay && cmdDelay.get(0)>0){
						try {
							Thread.sleep(cmdDelay.get(0)*1000);
						} catch (InterruptedException e1) {
						}
					}
					
					//Connect the API server first for every command
					for(int i=0; i<2; i++){
						try {
							socket = sc.connect(ip, port, handler, cmdEndChr);
							if (null != socket) cnnOK = true;
						} catch (IOException e) {
							sc.removeSocket(socket);
							LogUtils.commandLog(ip, "NG"+LogUtils.separator+progName+LogUtils.separator+cmd+LogUtils.separator+"Connect("+ip+":"+port+") Failed:"+e.getMessage());
							if(i==0){
								if(!sc.portOfHostOK(ip, port, 60, 10)){
									LogUtils.commandLog(ip, "NG"+LogUtils.separator+progName+LogUtils.separator+cmd+LogUtils.separator+"Port("+port+") is not reachable");
									return RTN_COM_CNN_NG;
								}
							}else{
								LogUtils.commandLog(ip, "NG"+LogUtils.separator+progName+LogUtils.separator+cmd+LogUtils.separator+"Connect("+ip+":"+port+") Failed:"+e.getMessage());
								return RTN_COM_CNN_TIMEOUT;
							}
						}
						if(cnnOK) break;
					}
					
					String cmdStr = robotCmd.getCommandStr(mainKey, operationName, cmd, paras, paraVals);
					String feedback = "", pingInfo = "";
					for(int i=0; i<=cmdRetryCount; i++){
						if(i > 0 && 0==feedbackCode){
							LogUtils.commandLog(ip, "NG"+LogUtils.separator+progName+LogUtils.separator+cmdStr+LogUtils.separator+"Timeout Retry_"+i+"/"+cmdRetryCount);
							if("getLastCmd".equals(operationName) && 1==i){
								//Retry Step 1: Check the network
								pingInfo = NetUtils.pingHost(ip);
								if(!"OK".equals(pingInfo)){
									LogUtils.commandLog(ip, "NG"+LogUtils.separator+progName+LogUtils.separator+cmdStr+LogUtils.separator+"Ping("+ip+") Failed:"+pingInfo);
									//Network checking with 2 hours timout
									if(NetUtils.waitUntilNetworkOK(ip, 120, 10)){
										LogUtils.commandLog(ip, "NG"+LogUtils.separator+progName+LogUtils.separator+cmdStr+LogUtils.separator+"Network Resumed");
									}else{
										LogUtils.commandLog(ip, "NG"+LogUtils.separator+progName+LogUtils.separator+cmdStr+LogUtils.separator+"Network Failed");
										feedbackCode = -1;
										break;
									}
								}
								LogUtils.commandLog(ip, "NG"+LogUtils.separator+progName+LogUtils.separator+cmdStr+LogUtils.separator+"Ping("+ip+") OK");
								
								//Retry Step 2: Check the API server with 60 seconds timeout
								if(!sc.portOfHostOK(ip, port, 60, 10)){
									LogUtils.commandLog(ip, "NG"+LogUtils.separator+progName+LogUtils.separator+cmdStr+LogUtils.separator+"Port("+port+") is not reachable");
									feedbackCode = -1;
									break;
								}else{
									cnnOK = false;//Connection reset already so need to trigger the reconnection again
								}
								LogUtils.commandLog(ip, "NG"+LogUtils.separator+progName+LogUtils.separator+cmdStr+LogUtils.separator+"Connect("+ip+":"+port+") OK");
							}
							
							//Retry Step 3: Check with API Server whether cmdStr has been executed successfully or not 
							if(1==i && !"getLastCmd".equals(operationName) && !"getBattery".equals(operationName)){
								feedback = getLastCmd(ip, targetName);
								feedbackCode = feedbackIsGood(cmdStr,feedback,cmdSeperator,successCode,returnCodePosition);
								if(0 == feedbackCode){
									if(null!=feedback && !"".equals(feedback)){
										//Current command is different from last command
										//Execute current command again
										LogUtils.commandLog(ip, "NG"+LogUtils.separator+progName+LogUtils.separator+cmdStr+LogUtils.separator+"LAST_CMD"+LogUtils.separator+feedback);
										cnnOK = false;
									}else{
										//Fail directly if fail to query last command
										LogUtils.commandLog(ip, "NG"+LogUtils.separator+progName+LogUtils.separator+cmdStr+LogUtils.separator+"LAST_CMD failed:API could be dead or running into a dead loop");
										feedbackCode = -1;
										break;
									}
								}else if(1 == feedbackCode){
									//Current command is same as last command and has been executed successfully
									outParas = getOperationOutput(operationName, cmd, feedback, cmdSeperator, outParas);
									break;
								}else{
									//Fail directly
									break;
								}
							}
						}
						
//						if(i == 2) cnnOK = false; //Disabled reconnecting on 06-March-2018, may need to enable it in the future
						if(i > 0 && 0==feedbackCode && !cnnOK){
							try {
								socket = sc.connect(ip, port, handler, cmdEndChr);
								if (null != socket) cnnOK = true;
								sendOK = false;
							} catch (IOException e) {
								cnnOK = false;
							}
							LogUtils.commandLog(ip, "NG"+LogUtils.separator+progName+LogUtils.separator+cmdStr+LogUtils.separator+"Reconnecting("+ip+":"+port+") "+(cnnOK?"OK":"NG"));
							if(!cnnOK){
								if(i==cmdRetryCount) feedbackCode = -1;
								continue;
							}
						}
						
						feedback = "";
						try {
							if(!sendOK){
								sendOK = sc.sendData(socket, cmdStr, cmdEndChr);
								cmdStartT = System.currentTimeMillis();
							}
							int count = robotCmdTimeout * 1000 / socketRespTimeInterval;
							while(count > 0){
								feedback = (String) socketRespData.get(dtKey);
								feedbackCode = feedbackIsGood(cmdStr,feedback,cmdSeperator,successCode,returnCodePosition);
								if(1 == feedbackCode){
									outParas = getOperationOutput(operationName, cmd, feedback, cmdSeperator, outParas);
									break;
								}else if(feedbackCode < 0){ //Current command failed
									break;
								}else{
									try {
										Thread.sleep(socketRespTimeInterval);
									} catch (InterruptedException e) {
									}
									count--;
								}
							}
						} catch (IOException e) { //sendData error
							feedbackCode = -1;
							LogUtils.errorLog("Robot("+ip+") sendCommand("+operationName+"-"+cmd+") ERR:"+e.getMessage());
						} finally{
							if(null != socket){
								sc.removeSocket(socket);
								socketRespData.remove(socket);
							}
						}
						
						if(0 != feedbackCode) break; //Current command success or failed
					}
					
					sc.removeSocket(socket);
					setRobotPosition(ip,mainKey,operationName,cmd,paras,paraVals,feedbackCode);
					cmdStopT = System.currentTimeMillis();
					debugInfo = LogUtils.separator + (cmdStopT-cmdStartT);
					if(LogUtils.getDebugLogFlag()){
						if(!"getLastCmd".equals(operationName)){
							lastCmd = getLastCmd(ip, targetName);
							debugInfo += LogUtils.separator + (feedback.startsWith(lastCmd)?"LAST_CMD:SAME":("LAST_CMD:DIFF"+LogUtils.separator+lastCmd));
						}
					}
					if(feedbackCode<0) debugInfo += LogUtils.separator + ErrorDesc.getR2D2ErrorDesc(getCmdReturnCode(feedback,cmdSeperator,returnCodePosition));
					
					if(!"getLastCmd".equals(operationName)) LogUtils.commandLog(ip, (1==feedbackCode?"OK":"NG")+LogUtils.separator+progName+LogUtils.separator+cmdStr+LogUtils.separator+feedback+debugInfo);
					if(1 != feedbackCode){
						doLooping = false;
						break; //Current command timeout or failed
					}
					
					if(null!=cmdDelay && cmdDelay.get(1)>0){
						try {
							Thread.sleep(cmdDelay.get(1)*1000);
						} catch (InterruptedException e1) {
						}
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
					}else{
						successCmds++;
					}
					ifIdx = -1;
				}
			}else{
				break; //There is command timeout or failed
			}
		}
		
		if(successCmds >= totalCmds){
			success = RTN_CMD_OK;
			if(LogUtils.getDebugLogFlag()){
				if(!"getBattery".equals(operationName) && !"getLastCmd".equals(operationName)){
					String battery = getBattery(ip, targetName);
					LogUtils.machiningDataLog("RobotBattery_" + TimeUtils.getCurrentDate("yyyyMMdd") + ".log", TimeUtils.getCurrentDate("yyyyMMddHHmmss")+","+battery+","+robotData.getItemVal(ip, RobotItems.CMD));
				}
			}
		}
		
		return success;
	}
	
	class RobotSocketDataHandler extends SocketDataHandler{
		private RobotSocketDataHandler(){}
		@Override
		public void doHandle(String in, Socket s) {
			if(null == s) return;
			String ip = s.getInetAddress().toString().replace("/", "");
			socketRespData.put(s, in);
			SocketData.setData(in);
			LogUtils.debugLog("RobotR2D2SocketDataHandler_", ip+":"+s.getPort()+LogUtils.separator+in);
		}
	}
}
