package com.cncmes.thread;

import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JProgressBar;

import com.cncmes.base.CNC;
import com.cncmes.base.CncItems;
import com.cncmes.base.CncWebAPIItems;
import com.cncmes.base.DeviceState;
import com.cncmes.base.ErrorCode;
import com.cncmes.base.TaskItems;
import com.cncmes.data.CncData;
import com.cncmes.data.CncWebAPI;
import com.cncmes.data.TaskData;
import com.cncmes.data.WorkpieceData;
import com.cncmes.utils.*;

/**
 * @author LI ZI LONG
 * Machine State Monitoring; Machining Progress Monitoring
 */
public class DeviceMonitor {
	private static DeviceMonitor devMonitor = new DeviceMonitor();
	private static int monitorInterval = 5000; //unit is ms
	private static LinkedHashMap<String, String> workZones = new LinkedHashMap<String, String>();
	
	private DeviceMonitor(){}
	public static DeviceMonitor getInstance(){
		return devMonitor;
	}
	
	public static void setMonitorInterval(int interval){
		monitorInterval = interval;
		TaskMonitor.setMonitorInterval(monitorInterval);
	}
	
	/**
	 * start the Machine State Monitoring thread
	 */
	public void run(){
		ThreadUtils.Run(new MyThread());
		RunningMsg.set("DeviceMonitor Started");
	}
	
	/**
	 * machining status monitoring thread
	 */
	class MyThread implements Runnable{
		@Override
		public void run() {
			Map<String, LinkedHashMap<String, Object>> cncList = null;
			while(!ThreadController.getStopFlag()){
				try {
					cncList = DataUtils.getCNCList();
					if(!cncList.isEmpty()){
						for(String cncIP:cncList.keySet()){
							cncMonitoring(cncIP);
						}
					}
					Thread.sleep(monitorInterval);
				} catch (Exception e) {
					LogUtils.errorLog("DeviceMonitor ERR:"+e.getMessage());
				}
			}
			RunningMsg.set("DeviceMonitor Stop");
		}
	}
	
	public boolean cncMachiningIsDone(CNC cncCtrl, String cncIP, long totalTime, JProgressBar progBar){
		boolean done = false;
		int checkedTimes = 0, alarmingTimes = 0, progress = 0;
		long startTime = 0, passedTime = 0;
		
		if(null==cncCtrl) return false;
		DeviceState cncState = null;
		startTime = System.currentTimeMillis();
		if(totalTime<=0) totalTime = 300;
		
		while(true){
			cncState = cncCtrl.getMachineState(cncIP);
			RunningMsg.set("Checking CNC Status: "+cncState);
			passedTime = (System.currentTimeMillis() - startTime)/1000;
			progress = (int)((double)passedTime/totalTime*100);
			if(progress>100) progress = 99;
			if(null!=progBar) progBar.setValue(progress);
			
			if(DeviceState.STANDBY==cncState || DeviceState.FINISH==cncState){
				checkedTimes++;
			}else{
				checkedTimes = 0;
			}
			
			if(DeviceState.ALARMING == cncState){
				alarmingTimes++;
			}else{
				alarmingTimes = 0;
			}
			
			if(checkedTimes>=3){
				if(null!=progBar) progBar.setValue(100);
				done = true;
				break;
			}else{
				try {
					if(checkedTimes > 0 || alarmingTimes > 0){
						Thread.sleep(1000);
					}else{
						Thread.sleep(monitorInterval);
					}
				} catch (InterruptedException e) {
				}
			}
			
			if(alarmingTimes>=3) break;
		}
		
		if(done){
			try {
				Thread.sleep(5000);//Waiting for door unlock
			} catch (InterruptedException e) {
			}
		}
		
		return done;
	}
	
	public void cncMonitoring(String cncIP){
		String logFile, logContPre, lineName, robotIP, curSubProgram;
		long zoneStartT;
		LinkedHashMap<CncWebAPIItems, String> cncWebAPICommonCfg = null;
		
		String cncModel, taskID;
		String[] ncProgramName = new String[7], zones = null;
		int procNo = 0, zoneNo = 0;
		CNC cncCtrl = null;
		DeviceState devState = DeviceState.SHUTDOWN, lastState = null;
		DeviceState realtimeState = DeviceState.WORKING;
		
		CncData cncData = CncData.getInstance();
		WorkpieceData wpData = WorkpieceData.getInstance();
		TaskData taskData = TaskData.getInstance();
		CncWebAPI cncWebAPI = CncWebAPI.getInstance();
		
		devState = cncData.getCncState(cncIP);
		cncCtrl = (CNC) cncData.getData(cncIP).get(CncItems.CTRL);
		if(null==cncCtrl) return;
		RunningMsg.set("Monitoring CNC Status: "+devState);
		if(DeviceState.SHUTDOWN == devState){
			devState = cncCtrl.getMachineState(cncIP);
			if(DeviceState.SHUTDOWN == devState) return;
			cncData.setData(cncIP, CncItems.STATE, devState);
		}
		cncModel = (String) cncData.getData(cncIP).get(CncItems.MODEL);
		taskID = (String) cncData.getData(cncIP).get(CncItems.D_TASKID);
		robotIP = "defaultRobot";
		if(null==taskID) taskID = "";
		
		//Machining state monitoring
		lastState = cncData.getCncLastState(cncIP);
		if(DeviceState.LOCK == lastState){//LOCK state means start-machining has been triggered
			workZones.put(cncIP, cncData.getNotEmptyWorkzones(cncIP));
			realtimeState = cncCtrl.getMachineState(cncIP);
			if(DeviceState.WORKING == realtimeState){//Machining started
				cncData.setCncLastState(cncIP, realtimeState);
				cncData.setStartMachiningTime(cncIP, System.currentTimeMillis());
				lastState = realtimeState;
			}
			if(DeviceState.ALARMING == realtimeState){
				cncCtrl.getAlarmInfo(cncIP);
				cncData.setCncState(cncIP, DeviceState.ALARMING, true, false, true, false);
				if(!"".equals(taskID)) taskData.setTaskState(taskID, DeviceState.ALARMING, true, false, true, ErrorCode.MC_INTERRUPTION.getErrDesc(), false);

				//email inform--------------------------------------------------------------------
				String recipients = "HY_Zhong@sto-tech.hk;parischeung@sto-tech.hk;parischeung@lfm-agile.com.hk;parischeung@gmail.com";
//				String recipients = "HY_Zhong@sto-tech.hk";
				ErrorEMailUtil eMailUtil = new ErrorEMailUtil("mail1.sto-tech.hk","465","Eddie","HY_Zhong@sto-tech.hk","W000650STO","HY_Zhong@sto-tech.hk",recipients);
				String content = "<p>Hello.</p>" +
						"<p style=\"text-indent: 3em;\">I so sorry to inform you of MES run fail.Please check the attachment for details</p>" +
						"<p>Best Regards.</p>" ;
				try {
					eMailUtil.sendEmail("MES Alarming Info", content);
				} catch (GeneralSecurityException e) {
					e.printStackTrace();
				}
				//work wechat inform---------------------------------------------------------------
				WorkWechatUtils.sendAlarmNotification();
				//---------------------------------------------------------------------------------
			}
			
			//TODO There is bug in CncDrvWeb
			//Machining parameters monitoring - showing the real time machining status
			//cncCtrl.getMachiningParas(cncIP);
		}
		
		//Machining progress monitoring - only for WORKING machines
		if(DeviceState.WORKING == devState && DeviceState.WORKING == lastState){
			realtimeState = cncCtrl.getMachineState(cncIP);
			
			//Check current machining program - only for WORKING machines
			if(DeviceState.WORKING == realtimeState){
				//Get NC Sub and Main program names
				cncWebAPICommonCfg = cncWebAPI.getCommonCfg(cncModel);
				ncProgramName[0] = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGMAIN);
				ncProgramName[1] = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGSUB1);
				ncProgramName[2] = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGSUB2);
				ncProgramName[3] = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGSUB3);
				ncProgramName[4] = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGSUB4);
				ncProgramName[5] = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGSUB5);
				ncProgramName[6] = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGSUB6);
				
				//Get sub programs of working zones
				String subPrograms = "";
				if(null != workZones.get(cncIP)){
					zones = workZones.get(cncIP).split(";");
					for(int i=0; i<zones.length; i++){
						zoneNo = Integer.valueOf(zones[i]);
						if(zoneNo > 0 && zoneNo <= 6){
							if("".equals(subPrograms)){
								subPrograms = ncProgramName[zoneNo];
							}else{
								subPrograms += "#" + ncProgramName[zoneNo];
							}
						}
					}
				}
				
				//Check current machining program
				curSubProgram = cncCtrl.getCurrSubProgramName(cncIP, subPrograms);
				if(!"".equals(curSubProgram)){
					for(int i=1; i<ncProgramName.length; i++){
						if(curSubProgram.equals(ncProgramName[i])){
							zoneStartT = cncData.getZoneStartTime(cncIP, i);
							if(zoneStartT <= 0){
								cncData.setZoneStartTime(cncIP, i, System.currentTimeMillis());
							}
						}
					}
				}	
			}
			
			if(DeviceState.ALARMING == realtimeState){
				cncCtrl.getAlarmInfo(cncIP);
				cncData.setCncState(cncIP, DeviceState.ALARMING, true, false, true, false);
				if(!"".equals(taskID)) taskData.setTaskState(taskID, DeviceState.ALARMING, true, false, true, ErrorCode.MC_INTERRUPTION.getErrDesc(), false);

				//email inform--------------------------------------------------
//				String recipients = "HY_Zhong@sto-tech.hk;parischeung@sto-tech.hk;parischeung@lfm-agile.com.hk;parischeung@gmail.com";
				String recipients = "HY_Zhong@sto-tech.hk";
				ErrorEMailUtil eMailUtil = new ErrorEMailUtil("mail1.sto-tech.hk","465","Eddie","HY_Zhong@sto-tech.hk","W000650STO","HY_Zhong@sto-tech.hk",recipients);
				String content = "<p>Hello.</p>" +
						"<p style=\"text-indent: 3em;\">I so sorry to inform you of MES run fail.Please check the attachment for details</p>" +
						"<p>Best Regards.</p>" ;
				try {
					eMailUtil.sendEmail("MES Alarming Info", content);
				} catch (GeneralSecurityException e) {
					e.printStackTrace();
				}
				//work wechat inform---------------------------------------------------------------
				WorkWechatUtils.sendAlarmNotification();
				//---------------------------------------------------------------------------------

			}
			
			if(DeviceState.STANDBY == realtimeState || DeviceState.FINISH == realtimeState){
				lineName = cncData.getLineName(cncIP);
				if(!"".equals(taskID)) robotIP = (String) taskData.getData(taskID).get(TaskItems.ROBOTIP);
				logFile = LogUtils.getOperationLogFileName(cncIP, cncModel);
				LinkedHashMap<Integer,String> wpIDs = cncData.getWorkpieceIDs(cncIP);
				logContPre = lineName+LogUtils.separator+robotIP+"->"+cncIP+LogUtils.separator;
				
				LogUtils.operationLog(logFile, logContPre+"Machining done");
				for(String workpieceID:wpIDs.values()){
					procNo = wpData.getCurrentProcNo(workpieceID);
					wpData.setProcessState(workpieceID, procNo, DeviceState.FINISH);
				}
				if(!"".equals(taskID)) taskData.taskPopByID(taskID, true); //Finish machining
				cncData.setCncState(cncIP, DeviceState.FINISH, true, false, true, false);
				cncData.setCncLastState(cncIP, DeviceState.FINISH);
				cncData.setFinishMachiningTime(cncIP, System.currentTimeMillis());
				workZones.remove(cncIP);
				LogUtils.operationLog(logFile, logContPre+"Unlock machine done");
			}
		}
	}
}
