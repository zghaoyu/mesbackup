package com.cncmes.drv;

import java.util.LinkedHashMap;

import com.cncmes.base.CNC;
import com.cncmes.base.CncItems;
import com.cncmes.base.CncWebAPIItems;
import com.cncmes.base.DeviceState;
import com.cncmes.data.CncData;
import com.cncmes.data.CncWebAPI;
import com.cncmes.data.DevHelper;
import com.cncmes.handler.CncDataHandler;
import com.cncmes.utils.HttpRequestUtils;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.XmlUtils;

import net.sf.json.JSONObject;

/**
 * CNC Web Service API
 * @author LI ZI LONG
 *
 */
public class CncDrvWeb implements CNC {
	private static CncDrvWeb cncDrvWeb = new CncDrvWeb();
	private static LinkedHashMap<String, CncDataHandler> dataHandler = new LinkedHashMap<String, CncDataHandler>();
	private static CncData cncData = CncData.getInstance();
	private static CncWebAPI cncWebAPI = CncWebAPI.getInstance();
	
	private CncDrvWeb(){}
	static{
		try {
			XmlUtils.parseCncWebAPIXml();
		} catch (Exception e) {
			LogUtils.errorLog("CncDrvWeb fails to load web-api config:"+e.getMessage());
		}
	}
	
	public static CncDrvWeb getInstance(){
		return cncDrvWeb;
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
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		paras.put("workZone", workZone+"");
		return sendCommand(ip, "clampFixture", paras, null);
	}
	
	@Override
	public boolean releaseFixture(String ip, int workZone) {
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		paras.put("workZone", workZone+"");
		return sendCommand(ip, "releaseFixture", paras, null);
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
		LinkedHashMap<String, String> alarmInfo = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
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
		LinkedHashMap<String, LinkedHashMap<String, String>> toolLife = new LinkedHashMap<String, LinkedHashMap<String, String>>();
		LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
		if(sendCommand(ip, "getToolLife", null, rtnData)){
			CncDataHandler rsltHandler = getDataHandler(ip);
			if(null != rsltHandler){
				for(String key:rtnData.keySet()){
					toolLife = rsltHandler.machineToolLifeHandler(rtnData.get(key));
					break;
				}
			}
		}
		return toolLife;
	}
	
	@Override
	public LinkedHashMap<String, String> getMachiningParas(String ip) {
		LinkedHashMap<String, String> machineParas = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
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
		LinkedHashMap<String, LinkedHashMap<String, String>> counter = new LinkedHashMap<String, LinkedHashMap<String, String>>();
		LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
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
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		if(null != programID && !"".equals(programID)) paras.put("program", programID);
		return sendCommand(ip, "startMachining", paras, null);
	}
	
	@Override
	public boolean pauseMachining(String ip) {
		return sendCommand(ip, "pauseMaching", null, null);
	}
	
	@Override
	public boolean resumeMachining(String ip) {
		return sendCommand(ip, "resumeMaching", null, null);
	}
	
	@Override
	public boolean uploadMainProgram(String ip, String programID, String ncProgramPath) {
		boolean noNeedToUpload = false;
		CncDataHandler preHandler = getDataHandler(ip);
		if(null != preHandler) noNeedToUpload = preHandler.machineUploadMainProgramHandler(ip, programID);
		
		if(noNeedToUpload){
			if(mainProgramIsActivate(ip, programID)) return true;
		}
		
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		if(null != programID && !"".equals(programID)) paras.put("name", programID);
		if(null != ncProgramPath && !"".equals(ncProgramPath)) paras.put("code", ncProgramPath);
		return sendCommand(ip, "uploadMainProgram", paras, null);
	}
	
	@Override
	public String downloadMainProgram(String ip, String programID) {
		LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		if(null != programID && !"".equals(programID)) paras.put("name", programID);
		String rtn = null;
		if(sendCommand(ip, "downloadMainProgram", paras, rtnData)){
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
	public boolean deleteMainProgram(String ip, String programID) {
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		if(null != programID && !"".equals(programID)) paras.put("name", programID);
		return sendCommand(ip, "deleteMainProgram", paras, null);
	}
	
	@Override
	public boolean uploadSubProgram(String ip, String programID, String ncProgramPath) {
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		if(null != programID && !"".equals(programID)){
			paras.put("path", programID);
			paras.put("name", programID);
		}
		if(null != ncProgramPath && !"".equals(ncProgramPath)){
			paras.put("body", ncProgramPath);
			paras.put("code", ncProgramPath);
		}
		
		CncDataHandler preHandler = getDataHandler(ip);
		if(null != preHandler) preHandler.machineUploadSubProgramHandler(ip, programID);
		return sendCommand(ip, "uploadSubProgram", paras, null);
	}
	
	@Override
	public String downloadSubProgram(String ip, String programID) {
		LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		if(null != programID && !"".equals(programID)){
			paras.put("path", programID);
			paras.put("name", programID);
		}
		String rtn = null;
		if(sendCommand(ip, "downloadSubProgram", paras, rtnData)){
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
		LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
		if(null != programID && !"".equals(programID)){
			paras.put("path", programID);
			paras.put("name", programID);
		}
		return sendCommand(ip, "deleteSubProgram", paras, null);
	}
	
	@Override
	public boolean mainProgramIsActivate(String ip, String programID) {
		String mainProgramName = getMainProgramName(ip, programID);
		return programID.equals(mainProgramName);
	}
	
	@Override
	public String getMainProgramName(String ip, String programID) {
		String programName = "";
		boolean mainProgramIsFixed = false;
		CncDataHandler preHandler = getDataHandler(ip);
		if(null != preHandler) mainProgramIsFixed = preHandler.machineUploadMainProgramHandler(ip, programID);
		
		if(mainProgramIsFixed){
			LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
			if(sendCommand(ip, "getMainProgramName", null, rtnData)){
				if(null != preHandler){
					for(String key:rtnData.keySet()){
						programName = preHandler.machineMainProgramNameHandler(rtnData.get(key));
						break;
					}
				}
			}
		}else{
			programName = programID;
		}
		
		return programName;
	}
	
	@Override
	public String getCurrSubProgramName(String ip, String programID) {
		String programName = "";
		LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
		
		if(sendCommand(ip, "getCurrSubProgramName", null, rtnData)){
			CncDataHandler preHandler = getDataHandler(ip);
			if(null != preHandler){
				for(String key:rtnData.keySet()){
					programName = preHandler.machineCurrSubProgramNameHandler(rtnData.get(key));
					break;
				}
			}
		}
		
		return programName;
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
	public boolean sendCommand(String ip, String operation, LinkedHashMap<String,String> inParas, LinkedHashMap<String,String> rtnData){
		boolean success = false;
		LinkedHashMap<CncItems, Object> cncInfo = cncData.getData(ip);
		int port = (null!=cncInfo)?(int)cncInfo.get(CncItems.PORT):0;
		String model = (null!=cncInfo)?(String)cncInfo.get(CncItems.MODEL):"";
		String opExecutive = cncWebAPI.getOperationExecutive(model, operation);
		long cmdStartT = 0, cmdStopT = 0;
		if(!"Myself".equals(opExecutive)){
			CNC helper = (CNC) DevHelper.getInstance().getCtrlByModel(opExecutive);
			if(null != helper){
				switch(operation){
				case "clampFixture":
					return helper.clampFixture(ip, Integer.parseInt(inParas.get("workZone")));
				case "closeDoor":
					return helper.closeDoor(ip);
				case "openDoor":
					return helper.openDoor(ip);
				case "releaseFixture":
					return helper.releaseFixture(ip, Integer.parseInt(inParas.get("workZone")));
				}
				return helper.sendCommand(ip, operation, inParas, rtnData);
			}else{
				LogUtils.errorLog("CncDrvWeb-sendCommand get helper("+opExecutive+") failed");
			}
		}
		
		String initParas = "ip",initVals = ip,singleCmd="";
		if(null != inParas && inParas.size() > 0){
			for(String para:inParas.keySet()){
				if("port".equals(para)){
					port = Integer.valueOf(inParas.get(para));
				}else if("model".equals(para)){
					model = inParas.get(para);
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
		
		String reInitJson = "";
		JSONObject jsonObj = new JSONObject();
		if(operation.endsWith("SubProgram")){
			LinkedHashMap<CncWebAPIItems, String> commonCfg = cncWebAPI.getCommonCfg(model);
			if(null != commonCfg.get(CncWebAPIItems.FTPPORT)){
				if(null != cncInfo) jsonObj.put("server", cncInfo.get(CncItems.FTPIP));
				jsonObj.put("port", commonCfg.get(CncWebAPIItems.FTPPORT));
				jsonObj.put("user", commonCfg.get(CncWebAPIItems.FTPUSER));
				jsonObj.put("password", commonCfg.get(CncWebAPIItems.FTPPWD));
			}
		}
		if(null != inParas && inParas.size() > 0){
			for(String para:inParas.keySet()){
				if(!"port".equals(para)) jsonObj.put(para, inParas.get(para));
			}
		}
		if(jsonObj.size() > 0) reInitJson = jsonObj.toString();
		
		String[] reInitParas = initParas.split(",");
		String[] reInitVals = initVals.split(",");
		String[] cmds = cncWebAPI.getAllCommands(model, operation);
		if(!"".equals(singleCmd)) cmds = new String[]{singleCmd};
		
		if(null != cncInfo) cncData.setData(ip, CncItems.COMMAND, operation);
		for(int i=0; i<cmds.length; i++){
			cmdStartT = System.currentTimeMillis();
			String url = cncWebAPI.getCmdUrl(model, cmds[i]);
			String inParasStr = cncWebAPI.getCmdInputParas(model, operation, cmds[i], reInitParas, reInitVals, true, reInitJson);
			LinkedHashMap<String,Object> outParas = cncWebAPI.getCmdOutputParas(model, operation, cmds[i]);
			String sensorCheck = cncWebAPI.getCmdParaVal(model, operation, cmds[i], CncWebAPIItems.SENSORCHECK);
			
			if(!"".equals(url) && null != outParas){
				String rtnMsg = "OK";
				int checkSensorCount = 1;
				if ( sensorCheck.length()> 0 ){
					checkSensorCount = 30;
				}
				int try_err = 5;
				// 2023/6/20 add by Paris for check CNC sensor value 
				for (; checkSensorCount>0; checkSensorCount--){
					
					JSONObject resp = JSONObject.fromObject(HttpRequestUtils.httpPost(url, inParasStr, "utf-8", outParas));
					
					for(String para:outParas.keySet()){
						if(outParas.get(para).getClass().equals(Integer.class)){
							if(resp.getInt(para) != (int)outParas.get(para)){
								rtnMsg = "ERR";
								success = false;
								break;
							}
						}
					}
					
					if("OK".equals(rtnMsg)) success = true;
					
					for(String para:outParas.keySet()){
						if(outParas.get(para).getClass().equals(Integer.class)){
							rtnMsg += "," + para+"="+resp.getInt(para);
						}else{
							rtnMsg += "," + para+"="+resp.getString(para);
							if(null != rtnData) rtnData.put(para, resp.getString(para));
						}
					}
					
					if ( sensorCheck.length() > 0){
						
						String returnValue = rtnData.get("data");
						int returnIndex = returnValue.indexOf("value") + 8  ;
						String checkValue = returnValue.substring(returnIndex, returnIndex+1).trim();
//						
//						System.out.println(checkValue + " -> " + sensorCheck);
						
						if ( checkValue.equals(sensorCheck)){
							success = true;
							break;
						}else{
							try {
								if (checkSensorCount == 1){
									success = false;
									break;
								}
								Thread.sleep(500);
							} catch (InterruptedException e) {
								
								e.printStackTrace();
							}
							
						}
						
						if("ERR".equals(rtnMsg))
						{
							if(--try_err>0)
							{
								checkSensorCount++;
							}
						}
						
					}
				}
				cmdStopT = System.currentTimeMillis();
				LogUtils.commandLog(ip, (success?"OK":"NG")+LogUtils.separator+operation+LogUtils.separator+url+LogUtils.separator+inParasStr+LogUtils.separator+rtnMsg+LogUtils.separator+(cmdStopT-cmdStartT));
				if(!success) break;
			}
		}
		
		return success;
	}

	@Override
	public boolean hfOpenDoor(String ip) {
		
		return false;
	}

	@Override
	public boolean hfCloseDoor(String ip) {
		
		return false;
	}

	@Override
	public boolean hfClampFixture(String ip, int workZone) {
		
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
