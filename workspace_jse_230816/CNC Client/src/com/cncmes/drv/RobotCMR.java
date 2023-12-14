package com.cncmes.drv;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cncmes.base.IRobot;
import com.cncmes.base.RobotItems;
import com.cncmes.ctrl.SocketClient;

import com.cncmes.data.RobotData;
import com.cncmes.data.RobotDriver;
import com.cncmes.data.RobotEthernetCmd;
import com.cncmes.data.SocketData;
import com.cncmes.handler.SocketDataHandler;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.TimeUtils;
import com.cncmes.utils.XmlUtils;

public class RobotCMR implements IRobot {
	private RobotCMR(){}
	
	private static RobotCMR robotCMR = new RobotCMR();
	private static RobotSocketDataHandler handler = robotCMR.new RobotSocketDataHandler();
	private static RobotDriver robotDrv = RobotDriver.getInstance();
	private static RobotData robotData = RobotData.getInstance();
	
	private Map<Socket,String> socketRespData= new ConcurrentHashMap<Socket, String>();
	private static RobotEthernetCmd robotCmd = null;
	private static int socketRespTimeInterval = 10;//milliseconds
	
	static{
		try {
			XmlUtils.parseRobotEthernetCmdXml();
			robotCmd = RobotEthernetCmd.getInstance();
		} catch (Exception e) {
			LogUtils.errorLog("RobotCMR fails to load system config:"+e.getMessage());
		}
	}
	
	public static RobotCMR getInstance(){
		return robotCMR;
	}
	
	@Override
	public boolean moveTo(String ip, String targetPositionName) {
		ArrayList<Object> rtn = this.execute_cmd(ip, "move_to,"+targetPositionName+",", 300);
		return (boolean)rtn.get(0);
	}

	@Override
	public boolean dockTo(String ip, String dockPositionName) {
		ArrayList<Object> rtn = this.execute_cmd(ip, "dock_to,"+dockPositionName+",", 300);
		return (boolean)rtn.get(0);
	}

	@Override
	public boolean gotoCharge(String ip, boolean enableCharging) {
		ArrayList<Object> rtn = this.execute_cmd(ip, "go_to_charge,"+(enableCharging?1:0)+",", 300);
		return (boolean)rtn.get(0);
	}
	
	@Override
	public boolean enableCharging(String ip) {
		ArrayList<Object> rtn = this.execute_cmd(ip, "enable_charging,", 300);
		return (boolean) rtn.get(0);
	}
	
	@Override
	public boolean pickAndPlace(String ip, String pickFromPose, String placeToPose) {
//		System.out.println(pickFromPose+"-----------"+placeToPose);
		ArrayList<Object> rtn = this.execute_cmd(ip, "pick_and_place,"+pickFromPose+","+placeToPose+",", 120);

		return (boolean)rtn.get(0);

		
//		//FIXME for testing 2022/5/16 Hui Zhi Fang
//		System.out.printf("Robot pick material from %s to %s\n", pickFromPose, placeToPose);
//		try {
//			Thread.sleep(4000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		return true;
	}
	@Override
	public boolean pickFromName(String ip, String pickFromPose)
	{
		ArrayList<Object> rtn = this.execute_cmd(ip,"pick_from_name,"+pickFromPose+",",120);
		return (boolean)rtn.get(0);
	}
	@Override
	public boolean placeToName(String ip, String placeToPose)
	{
		ArrayList<Object> rtn = this.execute_cmd(ip,"place_to_name,"+placeToPose+",",120);
		return (boolean)rtn.get(0);
	}

	@Override
	public boolean adjustGripperPose(String ip, String targetPoseName, String pnpType) {
		ArrayList<Object> rtn = this.execute_cmd(ip, "adjust_gripper_pose,"+targetPoseName+","+pnpType+",", 60);
		return (boolean)rtn.get(0);
	}

	@Override
	public String readBarcode(String ip) {
		ArrayList<Object> rtn = this.execute_cmd(ip, "read_barcode,", 5);
		if((boolean)rtn.get(0)){
			return (String)rtn.get(1);
		}else{
			return "NG";
		}
	}
	
	@Override
	public String getBattery(String ip) {
		ArrayList<Object> rtn = this.execute_cmd(ip, "get_battery,", 5);
		if((boolean)rtn.get(0)){
			robotData.setData(ip, RobotItems.BATTERY, rtn.get(1));// add By Hui Zhi 2021/12/28
			return (String)rtn.get(1);
		}else{
			return "-1";
		}
	}

	@Override
	public String getAgvPosition(String ip) {
		ArrayList<Object> rtn = this.execute_cmd(ip, "get_agv_position,", 300);
		return (String) rtn.get(1);
	}
	
	
	@Override
	public boolean moveBackward(String ip) {
		ArrayList<Object> rtn = this.execute_cmd(ip, "agv_move_backward,", 300);
		return  (boolean) rtn.get(0);
	}
	
	private String calcValidationCode(String strData){
		StringBuilder strB = new StringBuilder("");
		
		int iChrCnt = strData.length();
		if(iChrCnt > 0){
			byte[] ascii = strData.getBytes();
			int xor = ascii[0];
			if(iChrCnt > 1){
				for(int i=1; i<ascii.length; i++){
					xor ^= ascii[i];
				}
			}
			strB.append(String.format("%02x", xor));
		}
		
		return strB.toString();
	}
	
	/**
	 * 
	 * @param ip the robot IP
	 * @param cmd the command name
	 * @param timeout_s 
	 * @return
	 */
	private ArrayList<Object> execute_cmd(String ip, String cmd, int timeout_s) {
		ArrayList<Object> rtn = new ArrayList<Object>();
		boolean bOK = false, sendOK = false;
		Socket socket = null;

		LinkedHashMap<RobotItems, Object> robotInfo = robotData.getData(ip);
		int port = (null != robotInfo) ? (int) robotInfo.get(RobotItems.PORT) : 0;
		String robotModel = (null != robotInfo) ? (String) robotInfo.get(RobotItems.MODEL) : "";
		String cmdEndChr = robotDrv.getCmdTerminator(robotModel);
		SocketClient sc = SocketClient.getInstance();

		cmd = cmd + this.calcValidationCode(cmd);
//		System.out.println(cmd);
		try {
			// add by Hui Zhi 2022/5/20
			socket = sc.connect(ip, port, handler, cmdEndChr);
		} catch (IOException e) {
			LogUtils.commandLog(ip, "NG" + LogUtils.separator + cmd.split(",")[0] + LogUtils.separator + cmd
					+ LogUtils.separator + "Connect(" + ip + ":" + port + ") Failed:" + e.getMessage());
		}

		if (null != socket) {
			if (timeout_s <= 0)	timeout_s = 60;
			try {


				sendOK = sc.sendData(socket, cmd, cmdEndChr);

				if (sendOK) {
					int count = timeout_s * 1000 / socketRespTimeInterval;
					while (count > 0) { // Command timeout check block
						String feedback = (String) socketRespData.get(socket);
						if (feedback != null) {
							String[] data = feedback.split(",");
							if (data[0].equals("OK")) {
								rtn.add(0, true);
								rtn.add(1,data[1]);
								bOK = true;
							}
							if (data[0].equals("NG")) {
								rtn.add(0, false);
								rtn.add(1,feedback);
							}
							break;
						}
						
						try {
							Thread.sleep(socketRespTimeInterval);
						} catch (InterruptedException e) {
						}
						count--;
					}
				}
			} catch (IOException e) {
				LogUtils.commandLog(ip, "NG" + LogUtils.separator + cmd.split(",")[0] + LogUtils.separator + cmd
						+ LogUtils.separator + "sendData(" + ip + ":" + port + ") Failed:" + e.getMessage());
			} finally {
				if(null != socket){
					sc.removeSocket(socket);
					socketRespData.remove(socket);
				}
			}
		}

		if (!bOK) {
			if (rtn.size() <= 0)
				rtn.add(0, bOK);
			if (rtn.size() == 1)
				rtn.add(1, "NG");
		}
		return rtn;
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<String> getCommandStr(String mainKey, String operation, String cmdName){
		String cmdStr = cmdName.split("#")[0];
		String cmdTimeout = "300";//Default is 300s
		ArrayList<String> rtns = new ArrayList<String>();
		
		LinkedHashMap<String, Object> oprMap = robotCmd.getData(mainKey);
		if(null != oprMap && oprMap.size() > 0){
			LinkedHashMap<String, Object> cmdMap = (LinkedHashMap<String, Object>) oprMap.get(operation);
			if(null != cmdMap && cmdMap.size() >= 2){
				ArrayList<String> cmdParas = (ArrayList<String>) cmdMap.get(cmdName);


				if(null != cmdParas && cmdParas.size() > 1){
					String[] paras = cmdParas.get(0).split(";");
					String[] vals = cmdParas.get(1).split(";");
					for(int i=0; i<paras.length; i++){
						if(robotCmd.isCmdInputParas(paras[i])){
							cmdStr += "," + vals[i];
						}else if("CMD_TIMEOUT".equals(paras[i])){
							cmdTimeout = vals[i];
						}
					}
				}
			}
		}
		rtns.add(cmdStr+",");
		rtns.add(cmdTimeout);
		
		return rtns;
	}
	
	public boolean sendCommand(String ip, String operationName, LinkedHashMap<String, String> inParas, LinkedHashMap<String, String> outParas, String targetName){
		boolean bOK = false;
		String singleCmd="";
		
		LinkedHashMap<RobotItems, Object> robotInfo = robotData.getData(ip);
		int port = (null!=robotInfo)?(int)robotInfo.get(RobotItems.PORT):0;
		String robotModel = (null!=robotInfo)?(String)robotInfo.get(RobotItems.MODEL):"";
		
		if(null != inParas && inParas.size() > 0){
			for(String para:inParas.keySet()){
				if("port".equals(para)){
					port = Integer.valueOf(inParas.get(para));
				}else if("model".equals(para)){
					robotModel = inParas.get(para);
				}else if("singleCmd".equals(para)){
					singleCmd = inParas.get(para);
				}
			}
		}
		robotData.setData(ip, RobotItems.PORT, port);//Refresh robot's communication port
		robotData.setData(ip, RobotItems.MODEL, robotModel);//Refresh robot's model
		
		String mainKey = robotModel + "#" + targetName;
		String[] cmds = robotCmd.getAllCommands(mainKey, operationName);

		if(!"".equals(singleCmd)) cmds = new String[]{singleCmd};
		
		int totalCmds = cmds.length;
		int successCmds = 0;
		ArrayList<Object> rtn = null;
		ArrayList<String> cmd = null;

		for(int cmdIdx=0; cmdIdx<totalCmds; cmdIdx++){
			cmd = this.getCommandStr(mainKey, operationName, cmds[cmdIdx]);
//			System.out.println(cmd);
			rtn = this.execute_cmd(ip, cmd.get(0), Integer.parseInt(cmd.get(1)));
			if((boolean)rtn.get(0)){
				successCmds++;
			}else{
				break;
			}
		}

		if(successCmds >= totalCmds) bOK = true;
		
		return bOK;
	}
	
	class RobotSocketDataHandler extends SocketDataHandler{
		private RobotSocketDataHandler(){}
		@Override
		public void doHandle(String in, Socket s) {
			if(null == s) return;
			String ip = s.getInetAddress().toString().replace("/", "");
			socketRespData.put(s, in);	
			SocketData.setData(in);
			LogUtils.debugLog("RobotCMRSocketDataHandler_", ip+":"+s.getPort()+LogUtils.separator+in);
		}
	}
}
