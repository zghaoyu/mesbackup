package com.cncmes.thread;

import java.util.LinkedHashMap;

import javax.swing.JProgressBar;

import com.cncmes.base.CNC;
import com.cncmes.base.CncItems;
import com.cncmes.base.CncWebAPIItems;
import com.cncmes.base.DeviceOP;
import com.cncmes.base.DeviceState;
import com.cncmes.base.DriverItems;
import com.cncmes.base.ErrorCode;
import com.cncmes.base.IRobot;
import com.cncmes.base.RobotItems;
import com.cncmes.base.SpecItems;
import com.cncmes.base.TaskItems;
import com.cncmes.base.WorkpieceItems;
import com.cncmes.ctrl.CncFactory;
import com.cncmes.ctrl.IRobotFactory;
import com.cncmes.ctrl.RackClient;
import com.cncmes.data.CncData;
import com.cncmes.data.CncDriver;
import com.cncmes.data.CncWebAPI;
import com.cncmes.data.RackProduct;
import com.cncmes.data.RobotData;
import com.cncmes.data.RobotDriver;
import com.cncmes.data.TaskData;
import com.cncmes.data.WorkpieceData;
//import com.cncmes.gui.MaterialInput;
import com.cncmes.gui.MyConfirmDialog;
import com.cncmes.utils.DataUtils;
//import com.cncmes.utils.DebugUtils;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.RunningMsg;
import com.cncmes.utils.SystemInfo;
import com.cncmes.utils.ThreadUtils;
import com.cncmes.utils.TimeUtils;
import com.cncmes.utils.UploadNCProgram;

import net.sf.json.JSONObject;

/**
 * @author LI ZI LONG
 * Create Task Handling Threads
 */
public class TaskProcessor {
	private static String lineName;
	private static CncWebAPI cncWebAPI = CncWebAPI.getInstance();
	private final static int CONCURR_OP_TIMEOUT = 600; //second
	
	private static TaskProcessor taskProcesser = new TaskProcessor();
	private TaskProcessor(){}
	
	public static TaskProcessor getInstance(String ln){
		lineName = ln;
		return taskProcesser;
	}
	
	/**
	 * start the Task Processor thread
	 */
	public void run(){
		ThreadUtils.Run(new TaskThread());
		RunningMsg.set("Task Processor Started");
	}
	
	/**
	 * Release workpieces for the scheduler to reschedule
	 * @param ids Set STANDBY state for relative workpieces stored in memory of both CNC Client and Scheduler
	 */
	private void resetMaterialState(String[] ids){
		if(null != ids && ids.length > 0){
			WorkpieceData wpData = WorkpieceData.getInstance();
			for(int i=0; i<ids.length; i++){
				wpData.setWorkpieceState(ids[i], DeviceState.STANDBY, i==(ids.length-1)?false:true, false, false, true, false);
			}
		}
	}
	
	/**
	 * Release not loaded workpieces for scheduler to reschedule
	 * @param taskID taskID from scheduler
	 * @param logFile log file full path
	 * @param logContPre prefix of log content
	 */
	private void checkMaterialOfRobot(String taskID, String logFile, String logContPre){
		TaskData taskData = TaskData.getInstance();
		RobotData robotData = RobotData.getInstance();
		LinkedHashMap<TaskItems, Object> task = taskData.getTaskByID(taskID);
		if(null == task) return;
		
		String robotIP = (String) task.get(TaskItems.ROBOTIP);
		String wpIDs = (String) task.get(TaskItems.MATERIALIDS);
		
		String[] scheduledIDs = wpIDs.split(";");
		String[] inTrayMaterialIDs = null;
		LinkedHashMap<RobotItems,String> info = robotData.getMaterialInfo(robotIP);
		String inTrayIDs = info.get(RobotItems.WPIDS);
		if(!"".equals(inTrayIDs)) inTrayMaterialIDs = inTrayIDs.split(";");
		
		if(null == inTrayMaterialIDs){
			resetMaterialState(scheduledIDs);
			LogUtils.operationLog(logFile, logContPre+"resetMaterialState(all:"+wpIDs+")");
		}else{
			String notLoadedIDs = "";
			boolean bFound = false;
			for(String id:scheduledIDs){
				bFound = false;
				for(String id2:inTrayMaterialIDs){
					if(id.equals(id2)){
						bFound = true;
						break;
					}
				}
				if(!bFound){
					if("".equals(notLoadedIDs)){
						notLoadedIDs = id;
					}else{
						notLoadedIDs += ";" + id;
					}
				}
			}
			if(!"".equals(notLoadedIDs)){
				resetMaterialState(notLoadedIDs.split(";"));
				LogUtils.operationLog(logFile, logContPre+"resetMaterialState(part:"+notLoadedIDs+")");
			}
		}
	}
	
	/**
	 * Pick materials from rack to Robot's Tray
	 * @param robotCtrl remote controller of robot
	 * @param robotIP ip of robot
	 * @param rackID ID of material rack
	 * @param rackSlots target slots number of material rack
	 * @param workpieceIDs expecting workpiece IDs from target slots
	 * @param logFile log file full path
	 * @param logContPre prefix of log content
	 * @return true if all target workpieces is successfully picked from rack and put onto robot's tray
	 */
	
	//Change Robot to IRobot by Hui Zhi 2021.11.3
	public boolean getMaterialFromRack(IRobot robotCtrl, String robotIP, String rackID, String[] rackSlots, String[] workpieceIDs, String logFile, String logContPre, String cncModel){
//	public boolean getMaterialFromRack(Robot robotCtrl, String robotIP, String rackID, String[] rackSlots, String[] workpieceIDs, String logFile, String logContPre, String cncModel){
		boolean bOK = true;
		RobotData robotData = RobotData.getInstance();
		WorkpieceData wpData = WorkpieceData.getInstance();
		
		String[] trayEmptySlots = robotData.getEmptySlots(robotIP);
		int index = -1;
		if(null == trayEmptySlots){
			LogUtils.operationLog(logFile, logContPre+"Robot getMaterialFromRack failed cause of tray is full");
			bOK = false;
		}else{
			for(String rackSlotID:rackSlots){
				index++;
				if(index < trayEmptySlots.length){
					// use pickAndPlace instead of pickMaterialFromRack   2021.11.3  Hui Zhi
					//---------------------------------------------------------
					if(rackSlotID.equals("5")||rackSlotID.equals("6"))
					{
						continue;
					}
					//----------------------------------------------------------
					if(robotCtrl.pickAndPlace(robotIP, "input_rack_1_slot_" + rackSlotID, "robot_pallet_a_slot_" + trayEmptySlots[index] )) {
					//if(robotCtrl.pickMaterialFromRack(robotIP, rackID, rackSlotID, trayEmptySlots[index], cncModel)){	
						wpData.setData(workpieceIDs[index], WorkpieceItems.ID, workpieceIDs[index]);
						DataUtils.updateWorkpieceData(workpieceIDs[index], lineName, rackID, rackSlotID);
						
						robotData.setData(robotIP, RobotItems.GRIPMATERIAL, "");
						robotData.updateSlot(robotIP, Integer.valueOf(trayEmptySlots[index]), workpieceIDs[index]);
						robotData.addMaterialInfo(robotIP, workpieceIDs[index], trayEmptySlots[index], DeviceState.STANDBY);
						LogUtils.operationLog(logFile, logContPre+"Robot pickMaterialFromRack("+workpieceIDs[index]+",Rack_"+rackID+":"+rackSlotID+"=>RobotSlot_"+trayEmptySlots[index]+") done");
					}else{
						bOK = false;
						robotData.setData(robotIP, RobotItems.GRIPMATERIAL, workpieceIDs[index]);
						robotData.addMaterialInfo(robotIP, workpieceIDs[index], "0", DeviceState.STANDBY);
						LogUtils.operationLog(logFile, logContPre+"Robot pickMaterialFromRack("+workpieceIDs[index]+","+rackID+","+rackSlotID+") failed");
						break;
					}
				}
			}
		}
		
		return bOK;
	}
	
	/**
	 * Put materials onto target rack from robot's tray
	 * @param robotCtrl remote controller of the robot
	 * @param robotIP ip of the robot
	 * @param rackID ID of the material rack
	 * @param rackSlots Output rack slots
	 * @param logFile log file full path
	 * @param logContPre prefix of the log content
	 * @return true if all materials in Robot's tray have been successfully put onto the material rack
	 */
	
	//Change Robot to IRobot by Hui Zhi 2021.11.3
	public boolean putMaterialOntoRack(IRobot robotCtrl, String robotIP, String rackID, String[] rackSlots, String logFile, String logContPre, String cncModel){
//	public boolean putMaterialOntoRack(Robot robotCtrl, String robotIP, String rackID, String[] rackSlots, String logFile, String logContPre, String cncModel){
		boolean bOK = true;
		RobotData robotData = RobotData.getInstance();
		WorkpieceData wpData = WorkpieceData.getInstance();
		RackClient rackClient = RackClient.getInstance();
		
		LinkedHashMap<RobotItems, String> info = robotData.getMaterialInfo(robotIP);
		String wpIDs = info.get(RobotItems.WPIDS);
		String traySlotIDs = info.get(RobotItems.WPSLOTIDS);
		String states = info.get(RobotItems.WPSTATES);
		String lineName = (String) robotData.getData(robotIP).get(RobotItems.LINENAME);
		if(null==rackSlots) rackSlots = rackClient.getRackEmptySlots(lineName, rackID);
		
		if("".equals(wpIDs) || null == rackSlots){
			if("".equals(wpIDs)) LogUtils.operationLog(logFile, logContPre+"Robot putMaterialOntoRack failed cause of tray is empty");
			if(null == rackSlots){
				if(rackClient.rackServerIsReady(lineName, rackID)){
					LogUtils.operationLog(logFile, logContPre+"Robot putMaterialOntoRack failed cause of Rack "+rackID+" is full");
				}else{
					LogUtils.operationLog(logFile, logContPre+"Robot putMaterialOntoRack failed cause of Rack Manager is not launched or can't be reached");
				}
			}
			bOK = false;
		}else{
			String[] workpieceIDs = wpIDs.split(";");
			String[] traySlotNo = traySlotIDs.split(";");
			String[] wpStates = states.split(";");
			int index = -1;
			for(String slotNo:traySlotNo){
				index++;
				if(index < rackSlots.length){
					DeviceState wpState = wpData.workpieceIsDone(workpieceIDs[index])?DeviceState.FINISH:DeviceState.WORKING;
					// use pickAndPlace instead of putMaterialOntoRack   2021.11.3  Hui Zhi
					//if(!robotCtrl.putMaterialOntoRack(robotIP, rackID, rackSlots[index], slotNo, cncModel)){
					if(!robotCtrl.pickAndPlace(robotIP, "robot_pallet_a_slot_" + slotNo, "input_rack_1_slot_" + rackSlots[index] )){
//					if(!robotCtrl.pickAndPlace(robotIP, "robot_pallet_a_slot_" + slotNo, "input_rack_" + rackID + "_slot_" + rackSlots[index] )){
						bOK = false;
						robotData.setData(robotIP, RobotItems.GRIPMATERIAL, workpieceIDs[index]);
						robotData.addMaterialInfo(robotIP, workpieceIDs[index], "0", DataUtils.getDevStateByString(wpStates[index]));
						LogUtils.operationLog(logFile, logContPre+"Robot putMaterialOntoRack("+workpieceIDs[index]+","+rackID+","+rackSlots[index]+") failed");
						break;
					}else{
						LogUtils.operationLog(logFile, logContPre+"Robot putMaterialOntoRack("+workpieceIDs[index]+",RobotSlot_"+slotNo+"=>Rack_"+rackID+":"+rackSlots[index]+") done");
						robotData.deleteMaterialInfo(robotIP, workpieceIDs[index]);
						robotData.setData(robotIP, RobotItems.GRIPMATERIAL, "");
						robotData.updateSlot(robotIP, Integer.parseInt(slotNo), "");
						
						//Update Rack Manager with workpiece FINISH|WORKING state
						wpData.setWorkpieceState(workpieceIDs[index], wpState, false, true, false, true, false);
						wpData.setRackID(workpieceIDs[index], rackID);
						wpData.setSlotNo(workpieceIDs[index], rackSlots[index]);
						rackClient.updateRackInfo(lineName, rackID, workpieceIDs[index], true, false, true);
					}
				}
			}
		}
		return bOK;
	}
	
	/**
	 * Unload materials from machine and put them onto Robot's tray
	 * @param robotCtrl remote controller of robot
	 * @param robotIP ip of robot
	 * @param cncIP ip of machine
	 * @param workZones working zones of machine
	 * @param workpieceIDs all materials in working zones of machine
	 * @param logFile log file full path
	 * @param logContPre prefix of log content
	 * @return true if all materials are successfully unloaded from machine
	 */
	//Change Robot to IRobot by Hui Zhi 2021.11.3
	public boolean unloadMaterialFromMachine(IRobot robotCtrl, String robotIP, CNC cncCtrl, String cncIP, String[] workZones, String[] workpieceIDs, String logFile, String logContPre){
//	public boolean unloadMaterialFromMachine(Robot robotCtrl, String robotIP, String cncIP, String[] workZones, String[] workpieceIDs, String logFile, String logContPre){
		boolean bOK = true;
		
		RobotData robotData = RobotData.getInstance();
		WorkpieceData wpData = WorkpieceData.getInstance();
		CncData cncData = CncData.getInstance();
		String[] emptyTraySlots = robotData.getEmptySlots(robotIP);
		if(null == emptyTraySlots){
			LogUtils.operationLog(logFile, logContPre+"Robot unloadMaterialFromMachine failed cause of tray is full");
			bOK = false;
		}else{
			int index = -1;
			//String cncModel = cncData.getCncModel(cncIP);	//disable 
			String cncPositon = cncData.getCNCPosition(cncIP);
			String cncZone="";
			if("cnc79marker".equals(cncPositon))
			{
				cncZone = "cnc_79";
			}else {
				cncZone = "cnc_72";
			}
			
			for(String workZone:workZones){
				index++;
				if(index < emptyTraySlots.length){
					DeviceState wpState = wpData.workpieceIsDone(workpieceIDs[index])?DeviceState.FINISH:DeviceState.WORKING;
					// use pickAndPlace instead of pickMaterialFromMachine   2021.11.3  Hui Zhi
					//if(robotCtrl.pickMaterialFromMachine(robotIP, cncIP, Integer.parseInt(workZone), emptyTraySlots[index], cncModel)){
					
					/* Check Position and Rotate same angle
					 * and then release fixture
					 * */

					//--------------------------------------------------修改放入cnc79 1、3、11、13
					int cnc79_pos = -1;
					if(workZone.equals("1"))
					{
						cnc79_pos = 1;
					}
					if(workZone.equals("2"))
					{
						cnc79_pos = 3;
					}
					if(workZone.equals("3"))
					{
						cnc79_pos = 8;
					}
					if(workZone.equals("4"))
					{
						cnc79_pos = 10;
					}
					if (!cncCtrl.hfReleaseFixture(cncIP, cnc79_pos)){
						LogUtils.operationLog(logFile, logContPre+"Robot pickMaterialFromMachine("+workpieceIDs[index]+",Zone_"+cnc79_pos+"=>RobotSlot_"+emptyTraySlots[index]+") CNC Rotate error");
						bOK = false;
						break;
					}



//					if (!cncCtrl.hfReleaseFixture(cncIP, Integer.parseInt(workZone))){
//						LogUtils.operationLog(logFile, logContPre+"Robot pickMaterialFromMachine("+workpieceIDs[index]+",Zone_"+workZone+"=>RobotSlot_"+emptyTraySlots[index]+") CNC Rotate error");
//						bOK = false;
//						break;
//					}
					
					
					if(robotCtrl.pickAndPlace(robotIP, cncZone, "robot_pallet_a_slot_" + emptyTraySlots[index])){
						LogUtils.operationLog(logFile, logContPre+"Robot pickMaterialFromMachine("+workpieceIDs[index]+",Zone_"+workZone+"=>RobotSlot_"+emptyTraySlots[index]+") done");
						robotData.setData(robotIP, RobotItems.GRIPMATERIAL, "");
						robotData.updateSlot(robotIP, Integer.parseInt(emptyTraySlots[index]), workpieceIDs[index]);
						robotData.addMaterialInfo(robotIP, workpieceIDs[index], emptyTraySlots[index], wpState);
						cncData.setWorkpieceID(cncIP, Integer.parseInt(workZone), "");
					}else{
						bOK = false;
						robotData.setData(robotIP, RobotItems.GRIPMATERIAL, workpieceIDs[index]);
						robotData.addMaterialInfo(robotIP, workpieceIDs[index], "0", wpState);
						LogUtils.operationLog(logFile, logContPre+"Robot pickMaterialFromMachine("+workpieceIDs[index]+","+workZone+") failed");
						break;
					}
				}
			}
		}
		
		return bOK;
	}
	
	/**
	 * Load materials from robot's tray onto target machine
	 * @param robotCtrl remote controller of robot
	 * @param robotIP ip of robot
	 * @param cncIP ip of machine
	 * @param logFile log file full path
	 * @param logContentPre prefix of log content
	 * @return true if all materials are successfully loaded onto machine
	 */

	//Change Robot to IRobot by Hui Zhi 2021.11.3
	public boolean loadMaterialOntoMachine(IRobot robotCtrl, String robotIP,CNC cncCtrl, String cncIP, String logFile, String logContentPre){
//	public boolean loadMaterialOntoMachine(Robot robotCtrl, String robotIP, String cncIP, String logFile, String logContentPre){
		boolean bOK = true;
		
		RobotData robotData = RobotData.getInstance();
		CncData cncData = CncData.getInstance();
		WorkpieceData wpData = WorkpieceData.getInstance();
		String[] workzones = cncData.getEmptyWorkzones(cncIP);
		
		LinkedHashMap<RobotItems, String> info = robotData.getMaterialInfo(robotIP);
		String wpIDs = info.get(RobotItems.WPIDS);
		String wpSlots = info.get(RobotItems.WPSLOTIDS);
		String states = info.get(RobotItems.WPSTATES);
		String cncModel = cncData.getCncModel(cncIP);
		
		if("".equals(wpIDs) || null == workzones){
			if("".equals(wpIDs)) LogUtils.operationLog(logFile, logContentPre+"Robot loadMaterialOntoMachine failed cause of tray is empty");
			if(null == workzones) LogUtils.operationLog(logFile, logContentPre+"Robot loadMaterialOntoMachine failed cause of machine is full");
			bOK = false;
		}else{
			String[] workpieceIDs = wpIDs.split(";");
			String[] traySlotsNo = wpSlots.split(";");
			String[] wpStates = states.split(";");
			
			
			int index = -1;
			
			String cncPositon = cncData.getCNCPosition(cncIP);
			String cncZone="";
			if("cnc79marker".equals(cncPositon))
			{
				cncZone = "cnc_79";
			}else {
				cncZone = "cnc_72";
			}
			for(String slotNo:traySlotsNo){
				index++;
				if(index < workzones.length){
					// use pickAndPlace instead of putMaterialOntoMachine   2021.11.3  Hui Zhi
					//if(!robotCtrl.putMaterialOntoMachine(robotIP, cncIP, Integer.parseInt(workzones[index]), slotNo, cncModel)){
					
					/* Check Position and Rotate same angle
					 * and then release fixture
					 * */

//					if(index>=4)      //skip 4h position
//					{
//						index++;
//					}


					//--------------------------------------------------修改放入cnc79 1、3、11、13
					int cnc79_pos = -1;
					if(workzones[index].equals("1"))
					{
						cnc79_pos = 1;
					}
					if(workzones[index].equals("2"))
					{
						cnc79_pos = 3;
					}
					if(workzones[index].equals("3"))
					{
						cnc79_pos = 8;
					}
					if(workzones[index].equals("4"))
					{
						cnc79_pos = 10;
					}
					if (!cncCtrl.hfReleaseFixture(cncIP, cnc79_pos)){
						LogUtils.operationLog(logFile, logContentPre+"Robot putMaterialOntoMachine("+workpieceIDs[index]+",RobotSlot_"+slotNo+"=>Zone_"+cnc79_pos+") CNC Rotate error");
//						bOK = false;
//						break;
					}


//					if (!cncCtrl.hfReleaseFixture(cncIP, Integer.parseInt(workzones[index]))){
//						LogUtils.operationLog(logFile, logContentPre+"Robot putMaterialOntoMachine("+workpieceIDs[index]+",RobotSlot_"+slotNo+"=>Zone_"+workzones[index]+") CNC Rotate error");
//						bOK = false;
//						break;
//					}
					
					if(!robotCtrl.pickAndPlace(robotIP, "robot_pallet_a_slot_" + slotNo, cncZone)){ //  + Integer.parseInt(workzones[index]))){
						bOK = false;
						robotData.setData(robotIP, RobotItems.GRIPMATERIAL, workpieceIDs[index]);
						robotData.addMaterialInfo(robotIP, workpieceIDs[index], "0", DataUtils.getDevStateByString(wpStates[index]));
						LogUtils.operationLog(logFile, logContentPre+"Robot putMaterialOntoMachine("+workpieceIDs[index]+","+workzones[index]+") failed");
						break;
					}else{
						LogUtils.operationLog(logFile, logContentPre+"Robot putMaterialOntoMachine("+workpieceIDs[index]+",RobotSlot_"+slotNo+"=>Zone_"+workzones[index]+") done");
						robotData.deleteMaterialInfo(robotIP, workpieceIDs[index]);
						robotData.setData(robotIP, RobotItems.GRIPMATERIAL, "");
						robotData.updateSlot(robotIP, Integer.parseInt(slotNo), "");
						cncData.setWorkpieceID(cncIP, Integer.parseInt(workzones[index]), workpieceIDs[index]);
						cncData.setNCProgram(cncIP, Integer.parseInt(workzones[index]), wpData.getNextProcProgram(workpieceIDs[index], cncModel, null));
						cncData.setSpecNo(cncIP, Integer.parseInt(workzones[index]), wpData.getSpecNo(workpieceIDs[index]));
						cncData.setZoneSimulationTime(cncIP, Integer.parseInt(workzones[index]), wpData.getNextProcSimtime(workpieceIDs[index], cncModel, null));
					}
				}
			}
			// Clamp all fixture
			if (!cncCtrl.hfClampFixture(cncIP, 1)){
				LogUtils.operationLog(logFile, logContentPre+"Robot putMaterialOntoMachine CNC Clamp Fixture error");
				bOK = false;
			}
		}
		return bOK;
	}
	
	/**
	 * Add new materials into the memory for machining status tracing
	 * @param wpData reference of the workpieces data
	 * @param wpIDs the new added workpiece IDs
	 * @param rackIDs the workpieces are from this material rack
	 * @param slotIDs the workpieces are from these slot IDs of the material rack
	 * @return all new added workpieces in string array
	 */
	private String[] addNewWorkpieces(WorkpieceData wpData, String wpIDs, String rackIDs, String slotIDs) {
		String[] ids;
		String[] rIDs;
		String[] sIDs;
		ids = wpIDs.split(";");
		rIDs = rackIDs.split(";");
		sIDs = slotIDs.split(";");
		for(int i=0; i<ids.length; i++){
			wpData.setData(ids[i], WorkpieceItems.ID, ids[i]);
			DataUtils.updateWorkpieceData(ids[i], lineName, rIDs[i], sIDs[i]);
		}
		SystemInfo.addTotalWorkpieceQty(ids.length);
		return ids;
	}
	
	/**
	 * Check whether it is safe to close door

	 * @param timeout_s safe check timeout
	 * @return true if it is safe to close door
	 */
	public boolean safeToCloseDoor(String cncIP, String robotIP, int timeout_s){
		boolean safe = true;
		CncData cncData = CncData.getInstance();
		RobotData robotData = RobotData.getInstance();
		String machineTag = (String) cncData.getItemVal(cncIP, CncItems.TAGNAME);
		String robotSleep = (String) robotData.getItemVal(robotIP, RobotItems.SLEEP);
		String robotPos = (String) robotData.getItemVal(robotIP, RobotItems.POSITION);
		String robotParking = (String) robotData.getItemVal(robotIP, RobotItems.POS_PARKING);
		if(null!=robotParking && machineTag.equals(robotPos)) safe = false;
		
		if(!safe){
			while(true){
				if("Sleeping".equals(robotSleep)){
					RunningMsg.set("Robot is sleeping.");
					try {
						Thread.sleep(1000);
						RunningMsg.set("Robot is sleeping..");
						Thread.sleep(1000);
						RunningMsg.set("Robot is sleeping...");
					} catch (InterruptedException e) {
					}
				}else{
					break;
				}
			}
			
			long startT = System.currentTimeMillis();
			long timePassed = 0, timeout_ms = timeout_s * 1000;
			while(true){
				timePassed = System.currentTimeMillis() - startT;
				robotPos = (String) robotData.getItemVal(robotIP, RobotItems.POSITION);
				robotParking = (String) robotData.getItemVal(robotIP, RobotItems.POS_PARKING);
				if(null!=robotParking && machineTag.equals(robotPos)){				
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
					}
				}else{
					safe = true;
					break;
				}
				
				if(timePassed > timeout_ms) break;
			}
		}
		
		return safe;
	}
	//Only for Testing. Add by Hui Zhi 2022/5/17 
	public boolean safeToCloseDoor(String cncIP, String robotIP,IRobot robot, int timeout_s){
		boolean safe = false;
		if(!safe){
			try {
				Thread.sleep(3000);
				safe = true;
			} catch (InterruptedException e) {
			}
			
		}
		
		return safe;
	}
	
	private String initWorkpieceData(int[] workZones, String[] workpieceIDs, String lineName, String cncModel, boolean failExitingID){
		String errMsg = "", ids = "";
		WorkpieceData wpData = WorkpieceData.getInstance();
		
		for(int i=0; i<workpieceIDs.length; i++){
			if("".equals(workpieceIDs[i])) continue;
			if(null==wpData.getData(workpieceIDs[i]) || (null!=wpData.getData(workpieceIDs[i]) && !failExitingID)){
				if(null!=wpData.getData(workpieceIDs[i])) {
					if(wpData.canMachineByCNC(workpieceIDs[i], cncModel, null)){
						wpData.removeData(workpieceIDs[i]);
					}else{
						errMsg = workpieceIDs[i] + " can't be machined by " + cncModel;
						break;
					}
				}
				wpData.setData(workpieceIDs[i], WorkpieceItems.ID, workpieceIDs[i]);
				DataUtils.updateWorkpieceData(workpieceIDs[i], lineName, "3", ""+workZones[i]);
				if("".equals(ids)){
					ids = workpieceIDs[i];
				}else{
					ids += ";" + workpieceIDs[i];
				}
				if(wpData.canMachineByCNC(workpieceIDs[i], cncModel, null)){
					LinkedHashMap<SpecItems,Object> spec = wpData.getNextProcInfo(workpieceIDs[i]);
					int curProc = wpData.getNextProcNo(workpieceIDs[i], spec);
					int procTime = wpData.getNextProcSimtime(workpieceIDs[i], cncModel, spec, curProc);
					wpData.setCurrentProcNo(workpieceIDs[i], curProc);
					wpData.appendData(workpieceIDs[i], WorkpieceItems.NCMODEL, cncModel);
					wpData.appendData(workpieceIDs[i], WorkpieceItems.MACHINETIME, ""+procTime);
					wpData.appendData(workpieceIDs[i], WorkpieceItems.PROCESS, wpData.getNextProcName(workpieceIDs[i], spec, curProc));
					wpData.appendData(workpieceIDs[i], WorkpieceItems.PROGRAM, wpData.getNextProcProgram(workpieceIDs[i], cncModel, spec, curProc));
					wpData.appendData(workpieceIDs[i], WorkpieceItems.SURFACE, ""+wpData.getNextProcSurface(workpieceIDs[i], spec, curProc));
				}else{
					errMsg = workpieceIDs[i] + " can't be machined by " + cncModel;
					break;
				}
			}else{
				if(failExitingID){
					errMsg = workpieceIDs[i] + " is already existing and can't be override";
					break;
				}
			}
		}
		
		if(!"".equals(errMsg) && !"".equals(ids)) removeWorkpieceData(ids.split(";"));
		
		return errMsg;
	}
	
	private void removeWorkpieceData(String[] workpieceIDs){
		WorkpieceData wpData = WorkpieceData.getInstance();
		for(int i=0; i<workpieceIDs.length; i++){
			wpData.removeData(workpieceIDs[i]);
		}
	}
	
	private boolean startMachining(CNC cncCtrl, String cncIP, String programName, int retryTimes, int timeout_s){
		boolean ok = false;
		
		long startT, diffTime, timeout_ms;
		DeviceState cncState = DeviceState.STANDBY;
		
		timeout_ms = timeout_s*1000;
		for(int i=0; i<retryTimes; i++){
			// just for trial run remove main program
			//if(cncCtrl.startMachining(cncIP, programName)){
			if(cncCtrl.startMachining(cncIP, "")){
				startT = System.currentTimeMillis();
				while(true){
					diffTime = System.currentTimeMillis() - startT;
					cncState = cncCtrl.getMachineState(cncIP);
					if(DeviceState.WORKING == cncState){
						ok = true;
						break;
					}else{
						try {
							Thread.sleep(1000);
							RunningMsg.set("Start machining...");
						} catch (InterruptedException e) {
						}
					}
					if(diffTime>timeout_ms) break;
				}
			}
			if(ok) break;
		}
		
		return ok;
	}
	
	public String openDoorEx(CNC cncCtrl, String cncIP){
		String errMsg = "";
		CncData cncData = CncData.getInstance();
		
		while(true){
			if(cncCtrl.openDoor(cncIP)){
				cncData.setData(cncIP, CncItems.OP_OPENDOOR, "1");
				cncData.setData(cncIP, CncItems.OP_CLOSEDOOR, "");
				break;
			}else{
				errMsg = "开门失败，重试【开门】操作前请重启控制箱：\r\n";
				errMsg += "1.关闭控制箱电源（开关打到11点钟方向）\r\n";
				errMsg += "2.等待5秒钟后打开控制箱电源（开关打到12点钟方向）\r\n\r\n";
				errMsg += "重试开门操作请点击【是】按钮，\r\n否则请点击【否】终止任务。";
				MyConfirmDialog.showDialog("请确认是否重试【开门】操作", errMsg);
				if(MyConfirmDialog.OPTION_NO == MyConfirmDialog.getConfirmFlag()){
					errMsg = "Fail to open door";
					break;
				}else{
					errMsg = "";
				}
			}
		}
		
		return errMsg;
	}
	
	public String closeDoorEx(CNC cncCtrl, String cncIP){
		String errMsg = "";
		CncData cncData = CncData.getInstance();
		
		while(true){
			if(cncCtrl.closeDoor(cncIP)){
				cncData.setData(cncIP, CncItems.OP_CLOSEDOOR, "1");
				cncData.setData(cncIP, CncItems.OP_OPENDOOR, "");
				break;
			}else{
				errMsg = "关门失败，重试【关门】操作前请重启控制箱：\r\n";
				errMsg += "1.关闭控制箱电源（开关打到11点钟方向）\r\n";
				errMsg += "2.等待5秒钟后打开控制箱电源（开关打到12点钟方向）\r\n\r\n";
				errMsg += "重试关门操作请点击【是】按钮，\r\n否则请点击【否】终止任务。";
				MyConfirmDialog.showDialog("请确认是否重试【关门】操作", errMsg);
				if(MyConfirmDialog.OPTION_NO == MyConfirmDialog.getConfirmFlag()){
					errMsg = "Fail to close door";
					break;
				}else{
					errMsg = "";
				}
			}
		}
		
		return errMsg;
	}
	
	public String doCleaning(CNC cncCtrl, String cncIP, int[] workZones, String[] workpieceIDs, JProgressBar progBar){
		String rtn = "", lineName = "", cncModel = "";
		CncData cncData = CncData.getInstance();
		lineName = cncData.getLineName(cncIP);
		cncModel = cncData.getCncModel(cncIP);
		
		rtn = initWorkpieceData(workZones,workpieceIDs,lineName,cncModel,true);
		if(!"".equals(rtn)) return rtn;
		if(!"".equals(openDoorEx(cncCtrl,cncIP))) return "Cleaning:Open door failed";
		if(!"".equals(closeDoorEx(cncCtrl,cncIP))) return "Cleaning:Close door failed";
		if(!UploadNCProgram.uploadSubPrograms(cncCtrl, cncIP, workZones, workpieceIDs)) return "Cleaning:Upload sub programs failed";
		
		//disable by Hui Zhi 2021/11/13
		//if(!UploadNCProgram.uploadMainProgram(cncCtrl, cncIP, workZones, workpieceIDs)) return "Cleaning:Upload main program failed";
		
		CncWebAPI cncWebAPI = CncWebAPI.getInstance();
		
		String mainProgramName = cncWebAPI.getMainProgramName(cncData.getCncModel(cncIP));
		if(startMachining(cncCtrl, cncIP, mainProgramName, 3, 10)){
			DeviceMonitor devMonitor = DeviceMonitor.getInstance();
			if(!devMonitor.cncMachiningIsDone(cncCtrl, cncIP, 480, progBar)){
				return "Cleaning failed";
			}else{
				removeWorkpieceData(workpieceIDs);
				try {
					Thread.sleep(5000);//Waiting for door unlock
				} catch (InterruptedException e) {
				}
			}
		}else{
			return "Cleaning:Start cleaning program failed";
		}
		
		return rtn;
	}
	//Change Robot to IRobot by Hui Zhi 2021.11.3
	public boolean runConcurrentOP(DeviceOP deviceOP, String cncIP, String robotIP, JSONObject jsonParas, CNC cncCtrl, IRobot robotCtrl, int retryTimes, int checkTimeout_s){
//	public boolean runConcurrentOP(DeviceOP deviceOP, String cncIP, String robotIP, JSONObject jsonParas, CNC cncCtrl, Robot robotCtrl, int retryTimes, int checkTimeout_s){
		boolean success = false;
		String opFlag = "";
		
		for(int i=0; i<retryTimes; i++){
			ThreadUtils.Run(new ConcurrentOP(deviceOP, cncIP, robotIP, jsonParas, cncCtrl, robotCtrl));
			
			CncData cncData = CncData.getInstance();
			RobotData robotData = RobotData.getInstance();
			
			long startT = System.currentTimeMillis();
			long timeout_ms = checkTimeout_s * 1000;
			long time_diff = 0;
			while(true){
				time_diff = System.currentTimeMillis() - startT;
				switch(deviceOP){
				
				case CNC_HF_OPENDOOR:
					opFlag = (String)cncData.getItemVal(cncIP, CncItems.OP_HF_OPENDOOR);
					if("-1".equals(opFlag)) success=true;
					break;
				case CNC_HF_CLOSEDOOR:
					opFlag = (String)cncData.getItemVal(cncIP, CncItems.OP_HF_CLOSEDOOR);
					if("-1".equals(opFlag)) success=true;
					break;
				case CNC_HF_RELEASEFIXTURE:
					opFlag = (String)cncData.getItemVal(cncIP, CncItems.OP_HF_RELEASEFIXTURE);
					if("-1".equals(opFlag)) success=true;
					break;
				case CNC_HF_CLAMPFIXTURE:
					opFlag = (String)cncData.getItemVal(cncIP, CncItems.OP_HF_CLAMPFIXTURE);
					if("-1".equals(opFlag)) success=true;
					break;
				case CNC_AXIS4_MOVE:
					opFlag = (String)cncData.getItemVal(cncIP, CncItems.OP_AXIS4MOVE);
					if("-1".equals(opFlag)) success=true;
					break;
				case CNC_OPENDOOR:
					opFlag = (String)cncData.getItemVal(cncIP, CncItems.OP_OPENDOOR);
					if("-1".equals(opFlag)) success=true;
					break;
				case CNC_CLOSEDOOR:
					opFlag = (String)cncData.getItemVal(cncIP, CncItems.OP_CLOSEDOOR);
					if("-1".equals(opFlag)) success=true;
					break;
				case CNC_RELEASEFIXTURE:
					opFlag = (String)cncData.getItemVal(cncIP, CncItems.OP_RELEASEFIXTURE);
					if("-1".equals(opFlag)) success=true;
					break;
				case CNC_CLAMPFIXTURE:
					opFlag = (String)cncData.getItemVal(cncIP, CncItems.OP_CLAMPFIXTURE);
					if("-1".equals(opFlag)) success=true;
					break;
				case CNC_CLEANING:
					opFlag = (String)cncData.getItemVal(cncIP, CncItems.OP_CLEANING);
					if("-1".equals(opFlag)) success=true;
					break;
				case ROBOT_MOVETORACK:
					opFlag = (String)robotData.getItemVal(robotIP, RobotItems.OP_MOVETORACK);
					if("-1".equals(opFlag)) success=true;
					break;
				case ROBOT_MOVETOMACHINE:
					opFlag = (String)robotData.getItemVal(robotIP, RobotItems.OP_MOVETOMACHINE);
					if("-1".equals(opFlag)) success=true;
					break;
				case ROBOT_PNPTESTING:
					opFlag = (String)robotData.getItemVal(robotIP, RobotItems.OP_PNPTESTING);
					if("0".equals(opFlag) || "-1".equals(opFlag) || "-2".equals(opFlag)) success=true;
					break;
				}
				if(time_diff>timeout_ms || success) break;
			}
			
			if(success) break;
		}
		
		if(!success) LogUtils.errorLog(deviceOP+":"+opFlag+"(Retry="+retryTimes+",Timeout="+checkTimeout_s+"s)");
		return success;
	}
	
	public boolean concurrentOPDone(String cncIP, String robotIP, DeviceOP devOP, int timeout_s){
		boolean done = false, loop = true;
		String opFlag = "";
		
		long startT = System.currentTimeMillis();
		long timePassed = 0, timeout_ms = timeout_s * 1000;
		
		CncData cncData = CncData.getInstance();
		RobotData robotData = RobotData.getInstance();
		
		while(loop){
			try {
				timePassed = System.currentTimeMillis() - startT;
				switch(devOP){
				case CNC_HF_OPENDOOR:
					if("1".equals((String)cncData.getItemVal(cncIP, CncItems.OP_HF_OPENDOOR))){
						done = true;
					}else if("0".equals((String)cncData.getItemVal(cncIP, CncItems.OP_HF_OPENDOOR))){
						loop = false;
					}
					break;
				case CNC_HF_CLOSEDOOR:
					if("1".equals((String)cncData.getItemVal(cncIP, CncItems.OP_HF_CLOSEDOOR))){
						done = true;
					}else if("0".equals((String)cncData.getItemVal(cncIP, CncItems.OP_HF_CLOSEDOOR))){
						loop = false;
					}
					break;
				case CNC_HF_RELEASEFIXTURE:
					if("1".equals((String)cncData.getItemVal(cncIP, CncItems.OP_HF_RELEASEFIXTURE))){
						done = true;
					}else if("0".equals((String)cncData.getItemVal(cncIP, CncItems.OP_HF_RELEASEFIXTURE))){
						loop = false;
					}
					break;
				case CNC_HF_CLAMPFIXTURE:
					if("1".equals((String)cncData.getItemVal(cncIP, CncItems.OP_HF_CLAMPFIXTURE))){
						done = true;
					}else if("0".equals((String)cncData.getItemVal(cncIP, CncItems.OP_HF_CLAMPFIXTURE))){
						loop = false;
					}
					break;
				case CNC_OPENDOOR:
					if("1".equals((String)cncData.getItemVal(cncIP, CncItems.OP_OPENDOOR))){
						done = true;
					}else if("0".equals((String)cncData.getItemVal(cncIP, CncItems.OP_OPENDOOR))){
						loop = false;
					}
					break;
				case CNC_CLOSEDOOR:
					if("1".equals((String)cncData.getItemVal(cncIP, CncItems.OP_CLOSEDOOR))){
						done = true;
					}else if("0".equals((String)cncData.getItemVal(cncIP, CncItems.OP_CLOSEDOOR))){
						loop = false;
					}
					break;
				case CNC_RELEASEFIXTURE:
					if("1".equals((String)cncData.getItemVal(cncIP, CncItems.OP_RELEASEFIXTURE))){
						done = true;
					}else if("0".equals((String)cncData.getItemVal(cncIP, CncItems.OP_RELEASEFIXTURE))){
						loop = false;
					}
					break;
				case CNC_CLAMPFIXTURE:
					if("1".equals((String)cncData.getItemVal(cncIP, CncItems.OP_CLAMPFIXTURE))){
						done = true;
					}else if("0".equals((String)cncData.getItemVal(cncIP, CncItems.OP_CLAMPFIXTURE))){
						loop = false;
					}
					break;
				case CNC_CLEANING:
					if("1".equals((String)cncData.getItemVal(cncIP, CncItems.OP_CLEANING))){
						done = true;
					}else if("0".equals((String)cncData.getItemVal(cncIP, CncItems.OP_CLEANING))){
						loop = false;
					}
					break;
				case ROBOT_MOVETORACK:
					startT = robotSleeping(startT,robotIP);
					if("1".equals((String)robotData.getItemVal(robotIP, RobotItems.OP_MOVETORACK))){
						done = true;
					}else if("0".equals((String)robotData.getItemVal(robotIP, RobotItems.OP_MOVETORACK))){
						loop = false;
					}
					break;
				case ROBOT_MOVETOMACHINE:
					startT = robotSleeping(startT,robotIP);
					if("1".equals((String)robotData.getItemVal(robotIP, RobotItems.OP_MOVETOMACHINE))){
						done = true;
					}else if("0".equals((String)robotData.getItemVal(robotIP, RobotItems.OP_MOVETOMACHINE))){
						loop = false;
					}
					break;
				case ROBOT_PNPTESTING:
					startT = robotSleeping(startT,robotIP);
					opFlag = (String)robotData.getItemVal(robotIP, RobotItems.OP_PNPTESTING);
					if("0".equals(opFlag) || "-2".equals(opFlag)){
						loop=false;
					}else if("1".equals(opFlag)){
						done = true;
					}
					break;
				default:
					loop = false;
				}
				if(timePassed > timeout_ms || done) break;
			} catch (Exception e) {
				LogUtils.errorLog(devOP+"/"+robotIP+"->"+cncIP+"/"+timeout_s+":"+e.getMessage());
			}
		}
		
		return done;
	}
	
	private long robotSleeping(long sleepDoneT, String robotIP){
		long doneT = sleepDoneT;
		String robotSleep = "";
		RobotData robotData = RobotData.getInstance();
		
		while(true){
			robotSleep = (String) robotData.getItemVal(robotIP, RobotItems.SLEEP);
			if("Sleeping".equals(robotSleep)){
				doneT = System.currentTimeMillis();
				RunningMsg.set("Robot is sleeping.");
				try {
					Thread.sleep(1000);
					RunningMsg.set("Robot is sleeping..");
					Thread.sleep(1000);
					RunningMsg.set("Robot is sleeping...");
				} catch (InterruptedException e) {
				}
			}else{
				break;
			}
		}
		
		return doneT;
	}
	
	class ConcurrentOP implements Runnable{
		private JSONObject jsonParas;
		private DeviceOP deviceOP;
		private CNC cncCtrl;
		
		//private Robot robotCtrl;//disable
		private IRobot robotCtrl;//Change Robot to IRobot by Hui Zhi 2021.11.3
		
		private CncData cncData = CncData.getInstance();
		private RobotData robotData = RobotData.getInstance();
		
		private String cncIP;
		private String robotIP;
		
		//Change Robot to IRobot by Hui Zhi 2021.11.3
		public ConcurrentOP(DeviceOP deviceOP, String cncIP, String robotIP, JSONObject jsonParas, CNC cncCtrl, IRobot robotCtrl){
		//public ConcurrentOP(DeviceOP deviceOP, String cncIP, String robotIP, JSONObject jsonParas, CNC cncCtrl, Robot robotCtrl){
			this.deviceOP = deviceOP;
			this.jsonParas = jsonParas;
			this.cncCtrl = cncCtrl;
			this.robotCtrl = robotCtrl;
			
			this.cncData = CncData.getInstance();
			this.robotData = RobotData.getInstance();
			
			this.cncIP = cncIP;
			this.robotIP = robotIP;
		}
		 
		@Override
		public void run() {
//			String cncModel = cncData.getCncModel(cncIP);
			boolean okToMove = true;
			LinkedHashMap<Integer,String> wpIDs = null;
			
			switch(deviceOP){
			case CNC_HF_OPENDOOR:
				cncData.setData(cncIP, CncItems.OP_HF_OPENDOOR, "-1");
				if(cncCtrl.hfOpenDoor(cncIP)){
					cncData.setData(cncIP, CncItems.OP_HF_OPENDOOR, "1");
					cncData.setData(cncIP, CncItems.OP_HF_CLOSEDOOR, "");
				}else{
					cncData.setData(cncIP, CncItems.OP_HF_OPENDOOR, "0");
				}
				
				break;
			case CNC_HF_CLOSEDOOR:
				cncData.setData(cncIP, CncItems.OP_HF_CLOSEDOOR, "-1");
				if(cncCtrl.hfCloseDoor(cncIP)){
					cncData.setData(cncIP, CncItems.OP_HF_CLOSEDOOR, "1");
					cncData.setData(cncIP, CncItems.OP_HF_OPENDOOR, "");
				}else{
					cncData.setData(cncIP, CncItems.OP_HF_CLOSEDOOR, "0");
				}
				
				break;
			case CNC_HF_RELEASEFIXTURE:

				wpIDs = cncData.getWorkpieceIDs(cncIP);
				
				if(wpIDs.size() > 0){
					cncData.setData(cncIP, CncItems.OP_HF_RELEASEFIXTURE, "-1");
					int okCnt = 0;
					for(int workZone:wpIDs.keySet()){
						if(!cncCtrl.hfReleaseFixture(cncIP, workZone)){
							cncData.setData(cncIP, CncItems.OP_HF_RELEASEFIXTURE, "0");
							break;
						}else{
							okCnt++;
						}
					}
					
					if(okCnt >= wpIDs.size()){
						cncData.setData(cncIP, CncItems.OP_HF_RELEASEFIXTURE, "1");
						cncData.setData(cncIP, CncItems.OP_HF_CLAMPFIXTURE, "");
					}
				}else{
					cncData.setData(cncIP, CncItems.OP_HF_RELEASEFIXTURE, "-2");
				}
				
				break;
			case CNC_HF_CLAMPFIXTURE:
				wpIDs = cncData.getWorkpieceIDs(cncIP);
				if(wpIDs.size() > 0){
					cncData.setData(cncIP, CncItems.OP_HF_CLAMPFIXTURE, "-1");
					int okCnt = 0;
					for(int workZone:wpIDs.keySet()){
						if(!cncCtrl.hfClampFixture(cncIP, workZone)){
							cncData.setData(cncIP, CncItems.OP_HF_CLAMPFIXTURE, "0");
							break;
						}else{
							okCnt++;
						}
					}
					
					if(okCnt >= wpIDs.size()){
						cncData.setData(cncIP, CncItems.OP_HF_CLAMPFIXTURE, "1");
						cncData.setData(cncIP, CncItems.OP_HF_RELEASEFIXTURE, "");
					}
				}else{
					cncData.setData(cncIP, CncItems.OP_HF_CLAMPFIXTURE, "-2");
				}
				
				break;
			case CNC_AXIS4_MOVE:
				// setup Axis 4 for 0,90,180,270 angle
				
				break;
			case CNC_OPENDOOR:
				cncData.setData(cncIP, CncItems.OP_OPENDOOR, "-1");
//				if(cncCtrl.openDoor(cncIP)){
				if(cncCtrl.openDoor(cncIP)){
					cncData.setData(cncIP, CncItems.OP_OPENDOOR, "1");
					cncData.setData(cncIP, CncItems.OP_CLOSEDOOR, "");
				}else{
					cncData.setData(cncIP, CncItems.OP_OPENDOOR, "0");
				}
				break;
			case CNC_CLOSEDOOR:
				cncData.setData(cncIP, CncItems.OP_CLOSEDOOR, "-1");
				if(cncCtrl.closeDoor(cncIP)){
					cncData.setData(cncIP, CncItems.OP_CLOSEDOOR, "1");
					cncData.setData(cncIP, CncItems.OP_OPENDOOR, "");
				}else{
					cncData.setData(cncIP, CncItems.OP_CLOSEDOOR, "0");
				}
				break;
			case CNC_RELEASEFIXTURE:
				wpIDs = cncData.getWorkpieceIDs(cncIP);
				if(wpIDs.size() > 0){
					cncData.setData(cncIP, CncItems.OP_RELEASEFIXTURE, "-1");
					int okCnt = 0;
					for(int workZone:wpIDs.keySet()){
						if(!cncCtrl.releaseFixture(cncIP, workZone)){
							cncData.setData(cncIP, CncItems.OP_RELEASEFIXTURE, "0");
							break;
						}else{
							okCnt++;
						}
					}
					
					if(okCnt >= wpIDs.size()){
						cncData.setData(cncIP, CncItems.OP_RELEASEFIXTURE, "1");
						cncData.setData(cncIP, CncItems.OP_CLAMPFIXTURE, "");
					}
				}else{
					cncData.setData(cncIP, CncItems.OP_RELEASEFIXTURE, "-2");
				}
				break;
			case CNC_CLAMPFIXTURE:
				wpIDs = cncData.getWorkpieceIDs(cncIP);
				if(wpIDs.size() > 0){
					cncData.setData(cncIP, CncItems.OP_CLAMPFIXTURE, "-1");
					int okCnt = 0;
					for(int workZone:wpIDs.keySet()){
						if(!cncCtrl.clampFixture(cncIP, workZone)){
							cncData.setData(cncIP, CncItems.OP_CLAMPFIXTURE, "0");
							break;
						}else{
							okCnt++;
						}
					}
					
					if(okCnt >= wpIDs.size()){
						cncData.setData(cncIP, CncItems.OP_CLAMPFIXTURE, "1");
						cncData.setData(cncIP, CncItems.OP_RELEASEFIXTURE, "");
					}
				}else{
					cncData.setData(cncIP, CncItems.OP_CLAMPFIXTURE, "-2");
				}
				break;
			case CNC_CLEANING:
				if(null!=jsonParas){
					cncData.setData(cncIP, CncItems.OP_CLEANING, "-1");
					String cncIP = jsonParas.getString("cncIP");
					String[] wkZones = jsonParas.getString("workZones").split(";");
					String[] codeIDs = jsonParas.getString("codeIDs").split(";");
					int[] workZones = new int[wkZones.length];
					for(int i=0; i<wkZones.length; i++){
						workZones[i] = Integer.parseInt(wkZones[i]);
					}
					if("".equals(doCleaning(cncCtrl,cncIP,workZones,codeIDs,null))){
						cncData.setData(cncIP, CncItems.OP_CLEANING, "1");
					}else{
						cncData.setData(cncIP, CncItems.OP_CLEANING, "0");
					}
				}else{
					cncData.setData(cncIP, CncItems.OP_CLEANING, "-2");
				}
				break;
			case ROBOT_MOVETORACK:
				okToMove = true;				
				if("-1".equals((String)robotData.getItemVal(robotIP, RobotItems.OP_MOVETOMACHINE))){
					okToMove = concurrentOPDone(cncIP, robotIP, DeviceOP.ROBOT_MOVETOMACHINE, CONCURR_OP_TIMEOUT);
				}
				if(okToMove){
//					String rackID = "";
					if(null!=jsonParas){
						robotData.setData(robotIP, RobotItems.OP_MOVETORACK, "-1");
//						rackID = jsonParas.getString("rackID");
						//if(robotCtrl.moveToRack(robotIP, rackID, cncModel)){
//						if(robotCtrl.dockTo(robotIP, robotData.getChargerPosition(robotIP))){// use dock to charger of IRobot 2021.11.3 Hui Zhi 
						//FIXME testing 2022/5/18 Hui Zhi
						if(robotCtrl.dockTo(robotIP, "virtual_cnc_1")){
//						if(robotCtrl.dockTo(robotIP, robotData.getChargerPosition(robotIP))){
							robotData.setData(robotIP, RobotItems.OP_MOVETORACK, "1");
							robotData.setData(robotIP, RobotItems.OP_MOVETOMACHINE, "");
						}else{
							robotData.setData(robotIP, RobotItems.OP_MOVETORACK, "0");
						}
					}else{
						robotData.setData(robotIP, RobotItems.OP_MOVETORACK, "-3");
					}
				}else{
					robotData.setData(robotIP, RobotItems.OP_MOVETORACK, "-2");
				}
				break;
			case ROBOT_MOVETOMACHINE:
				okToMove = true;
				if("-1".equals((String)robotData.getItemVal(robotIP, RobotItems.OP_MOVETORACK))){
					okToMove = concurrentOPDone(cncIP, robotIP, DeviceOP.ROBOT_MOVETORACK, CONCURR_OP_TIMEOUT);
				}
				if(okToMove){
					robotData.setData(robotIP, RobotItems.OP_MOVETOMACHINE, "-1");
					//if(robotCtrl.moveToMachine(robotIP, cncIP, cncModel)){
					if ("10.10.206.72".equals(cncIP)){
						robotCtrl.moveTo(robotIP, "pos03");
					}
					if(robotCtrl.dockTo(robotIP, cncData.getCNCPosition(cncIP))){//use dock to machine of IRobot instead of moveToMachine 2021.11.3 Hui Zhi 
						robotData.setData(robotIP, RobotItems.OP_MOVETOMACHINE, "1");
						robotData.setData(robotIP, RobotItems.OP_MOVETORACK, "");
					}else{
						robotData.setData(robotIP, RobotItems.OP_MOVETOMACHINE, "0");
					}
				}else{
					robotData.setData(robotIP, RobotItems.OP_MOVETOMACHINE, "-2");
				}
				break;
				//disable ROBOT_PNPTESTING 2021.11.3 Hui Zhi 
			case ROBOT_PNPTESTING:
//				if(!"0".equals((String)robotData.getItemVal(robotIP, RobotItems.OP_PNPTESTING))){
//					if("-1".equals((String)robotData.getItemVal(robotIP, RobotItems.OP_MOVETORACK))){
//						if(!concurrentOPDone(cncIP, robotIP, DeviceOP.ROBOT_MOVETORACK, CONCURR_OP_TIMEOUT)){
//							robotData.setData(robotIP, RobotItems.OP_PNPTESTING, "-2");
//							break;
//						}
//					}
//					if(!"1".equals((String)robotData.getItemVal(robotIP, RobotItems.OP_MOVETORACK))){
//						robotData.setData(robotIP, RobotItems.OP_PNPTESTING, "-2");
//						break;
//					}
//					
//					robotData.setData(robotIP, RobotItems.OP_PNPTESTING, "-1");
//					robotData.setData(robotIP, RobotItems.OP_STOPPNPTESTING, "-1");
//					while(true){
//						if(!robotCtrl.pickMaterialFromTray(robotIP, 5, cncModel)){
//							robotData.setData(robotIP, RobotItems.OP_PNPTESTING, "0");
//							break;
//						}
//						if("1".equals((String)robotData.getItemVal(robotIP, RobotItems.OP_STOPPNPTESTING))){
//							robotData.setData(robotIP, RobotItems.OP_PNPTESTING, "1");
//							break;
//						}
//					}
//				}
				break;
			}
		}
	}
	
	/**
	 * Create threads to handle all machining tasks assigned by the scheduler
	 */
	class TaskThread implements Runnable{
		@Override
		public void run() {
			TaskData taskData = TaskData.getInstance();
			RobotData robotData = RobotData.getInstance();
			WorkpieceData wpData = WorkpieceData.getInstance();
			CncData cncData = CncData.getInstance();
			CncDriver cncDriver = CncDriver.getInstance();
			RobotDriver robotDriver = RobotDriver.getInstance();
			
			String cncIP,robotIP,taskID;
			String cncModel,robotModel;
			String wpIDs="",rackIDs="",slotIDs="",wpStates="",error="";
			String[] ids=null;
			LinkedHashMap<TaskItems,Object> task;
			
			while(!ThreadController.getStopFlag()){
				try {
					if(taskData.taskCount() > 0){
						task = taskData.getTask();
						if(null == task){
							Thread.sleep(1000);
							continue;
						}
						
						cncIP = (String) task.get(TaskItems.MACHINEIP);
						robotIP = (String) task.get(TaskItems.ROBOTIP);
						cncModel = (String) cncData.getData(cncIP).get(CncItems.MODEL);
						robotModel = (String) robotData.getData(robotIP).get(RobotItems.MODEL);
						DeviceState cncState = (DeviceState) task.get(TaskItems.MACHINESTATE);
						
						//Clear the machining data
						if(DeviceState.STANDBY == cncState){
							cncData.clearMachiningData(cncIP);
							cncData.setData(cncIP, CncItems.DT_DATE, TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
							cncData.setData(cncIP, CncItems.DT_READYLOADING, System.currentTimeMillis());
							cncData.setData(cncIP, CncItems.D_ROBOTL, robotIP);
							cncData.setData(cncIP, CncItems.D_ROBOTPOSL, robotData.getItemVal(robotIP, RobotItems.POSITION));
							wpData.clearFinishedWorkpiece();
						}else{
							cncData.setData(cncIP, CncItems.D_ROBOTUL, robotIP);
							cncData.setData(cncIP, CncItems.D_ROBOTPOSUL, robotData.getItemVal(robotIP, RobotItems.POSITION));
						}
						
						//Get task information
						wpIDs = (String) task.get(TaskItems.MATERIALIDS);
						rackIDs = (String) task.get(TaskItems.RACKIDS);
						slotIDs = (String) task.get(TaskItems.SLOTIDS);
						taskID = (String) task.get(TaskItems.SCHEDULERTASKID);
						cncData.setData(cncIP, CncItems.D_TASKID, taskID);
						robotData.setData(robotIP, RobotItems.TASKID, taskID);
						
						//Refresh workpiece information - to trace the workpiece's status
						if(!"".equals(wpIDs) && !"".equals(rackIDs) && !"".equals(slotIDs)){
							ids = addNewWorkpieces(wpData, wpIDs, rackIDs, slotIDs);
						}
						
						//Refresh task information
						if(DeviceState.FINISH == cncState){
							LinkedHashMap<Integer,String> workpieceIDs = cncData.getWorkpieceIDs(cncIP);
							wpIDs="";wpStates="";rackIDs="";
							String mID = "";
							DeviceState mState = DeviceState.WORKING;
							for(int zoneNo:workpieceIDs.keySet()){
								mID = workpieceIDs.get(zoneNo);
								mState = wpData.workpieceIsDone(mID)?DeviceState.FINISH:DeviceState.WORKING;
								if("".equals(wpIDs)){
									wpIDs = mID;
									rackIDs = cncIP;
									slotIDs = ""+zoneNo;
									wpStates = ""+mState;
								}else{
									wpIDs += ";"+mID;
									rackIDs += ";"+cncIP;
									slotIDs += ";"+zoneNo;
									wpStates += ";"+mState;
								}
								if(DeviceState.FINISH == mState) SystemInfo.addFinishedWorkpieceQty(1);
							}
							taskData.setData(taskID, TaskItems.MATERIALIDS, wpIDs);
							taskData.setData(taskID, TaskItems.RACKIDS, rackIDs);
							taskData.setData(taskID, TaskItems.SLOTIDS, slotIDs);
							taskData.setData(taskID, TaskItems.MATERIALSTATES, wpStates);
						}
						
						//Check device driver
						String robotDrvName = (String)robotDriver.getData(robotModel).get(DriverItems.DRIVER);
						String cncDrvName = (String)cncDriver.getData(cncModel).get(DriverItems.DRIVER);
						String cncDataHandler = (String)cncDriver.getData(cncModel).get(DriverItems.DATAHANDLER);
						CNC cncCtrl = CncFactory.getInstance(cncDrvName,cncDataHandler,cncModel);

						//Change Robot to IRobot by Hui Zhi 2021.11.3
						//Robot robotCtrl = RobotFactory.getInstance(robotDrvName);
						IRobot robotCtrl = IRobotFactory.getInstance(robotDrvName);
						
						if(null == cncCtrl){
							if(null != robotCtrl) robotData.setRobotState(robotIP, DeviceState.STANDBY, false, false, true, false);
							cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
							taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_DRIVER.getErrDesc(), false);
							if(DeviceState.STANDBY == cncState) resetMaterialState(ids);
							RunningMsg.set("Driver of "+cncModel+"("+cncIP+") is wrong");
							continue;
						}
						
						if(null == robotCtrl){
							robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
							cncData.setCncState(cncIP, cncState, false, false, true, false);
							taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_DRIVER.getErrDesc(), false);
							if(DeviceState.STANDBY == cncState) resetMaterialState(ids);
							RunningMsg.set("Driver of "+robotModel+" is wrong");
							continue;
						}
						
						//Lock the robot
						taskData.setTaskState(taskID, DeviceState.LOCK, true, false, true, null, false);
						robotData.setRobotState(robotIP, DeviceState.PLAN, false, false, true, false);
						
						//Clear the alarming message
						cncData.setData(cncIP, CncItems.ALARMMSG, "");
						cncData.setCncLastState(cncIP, DeviceState.PLAN);
						
						//Create task handling thread
						Thread thr = new Thread(new TaskHandle(robotIP,cncIP,taskID));
						RunningMsg.set("TaskProcessor@"+thr.getName()+"|"+System.currentTimeMillis()+"|"+robotIP+"->"+cncIP);
						thr.start();
//						thr.join();
					}else{
						Thread.sleep(1000);
					}
				}catch(Exception e){
					error = e.getMessage();
					if(null != error) LogUtils.errorLog("TaskProcessor ERR:"+error);
				}
			}
		}
	}
	
	/**
	 * Handle a specific task in an individual thread
	 */
	class TaskHandle implements Runnable{
		private String robotIP;
		private String cncIP;
		private String taskID;
		
		public TaskHandle(String robotIP,String cncIP,String taskID){
			this.robotIP = robotIP;
			this.cncIP = cncIP;
			this.taskID = taskID;
		}
		
		@Override
		public void run() {
			String cncModel,robotModel;
			CNC cncCtrl = null;
			
			TaskData taskData = TaskData.getInstance();
			WorkpieceData wpData = WorkpieceData.getInstance();
			CncData cncData = CncData.getInstance();
			RobotData robotData = RobotData.getInstance();
			RackProduct rackP = RackProduct.getInstance();
			CncDriver cncDriver = CncDriver.getInstance();
			RobotDriver robotDriver = RobotDriver.getInstance();
			UploadNCProgram uploadNCProgram = UploadNCProgram.getInstance();
			String lineName = cncData.getLineName(cncIP);
			
			try {
				DeviceState cncState = (DeviceState) taskData.getData(taskID).get(TaskItems.MACHINESTATE);

				if(DeviceState.WORKING == cncData.getCncState(cncIP)) return;
				if(DeviceState.FINISH == cncState) Thread.sleep(3000);//For testing only
				
				RunningMsg.set("Task Handling Started - "+robotIP+"->"+cncIP);
				String logContPre = lineName+LogUtils.separator+robotIP+"->"+cncIP+LogUtils.separator;
				String logContent = logContPre+"Task "+taskID+"("+(DeviceState.STANDBY == cncState?"Load Materials":"Unload Materials")+") started";
				
				cncModel = (String) cncData.getData(cncIP).get(CncItems.MODEL);
				cncData.setCncState(cncIP, DeviceState.WORKING, true, false, true, false);
				taskData.setTaskState(taskID, DeviceState.WORKING, true, false, true, null, false);
				String logFile = LogUtils.getOperationLogFileName(cncIP, cncModel);
				LogUtils.operationLog(logFile, logContent);
				
				robotModel = (String) robotData.getData(robotIP).get(RobotItems.MODEL);
				String cncDrvName = (String)cncDriver.getData(cncModel).get(DriverItems.DRIVER);
				String cncDataHandler = (String)cncDriver.getData(cncModel).get(DriverItems.DATAHANDLER);
				String rackIDs = (String) taskData.getTaskByID(taskID).get(TaskItems.RACKIDS);
//				System.out.println("|||||||||||||||||||||||||||||||||||||||||||||rackID:"+rackIDs);
				String slotIDs = (String) taskData.getTaskByID(taskID).get(TaskItems.SLOTIDS);
//				System.out.println("|||||||||||||||||||||||||||||||||||||||||||||slotID:"+slotIDs);
				String wkpIDs = (String) taskData.getTaskByID(taskID).get(TaskItems.MATERIALIDS);
//				System.out.println("|||||||||||||||||||||||||||||||||||||||||||||wkpIDs:"+wkpIDs);
				String robotDrvName = (String)robotDriver.getData(robotModel).get(DriverItems.DRIVER);
				
				//rackP is for finish-machining workpieces
				String[] pRackIDs = rackP.getRackIDsByLineName(lineName);
				LogUtils.operationLog(logFile, logContPre+"load basic setting done");
				
				cncCtrl =  CncFactory.getInstance(cncDrvName,cncDataHandler,cncModel);
				cncData.setData(cncIP, CncItems.CTRL, cncCtrl);

				//Change Robot to IRobot by Hui Zhi 2021.11.3
				//Robot robotCtrl = RobotFactory.getInstance(robotDrvName);
				IRobot robotCtrl = IRobotFactory.getInstance(robotDrvName);
				
				LogUtils.operationLog(logFile, logContPre+"load device driver done");
				
				if(DeviceState.FINISH != cncState){//CNC is standby and waiting for materials
					RunningMsg.set("Getting material from rack");
					String[] rIDs = rackIDs.split(";");
					String[] sIDs = slotIDs.split(";");
					String[] wpIDs = wkpIDs.split(";");
					
					//Split material info by Rack
					LinkedHashMap<String,String> slotInfo = new LinkedHashMap<String,String>();
					LinkedHashMap<String,String> wpInfo = new LinkedHashMap<String,String>();
					for(int i=0; i<rIDs.length; i++){
						String s = slotInfo.get(rIDs[i]);
						String wp = wpInfo.get(rIDs[i]);
						if(null == s){
							slotInfo.put(rIDs[i], sIDs[i]);
							wpInfo.put(rIDs[i], wpIDs[i]);
						}else{
							slotInfo.put(rIDs[i], s+","+sIDs[i]);
							wpInfo.put(rIDs[i], wp+","+wpIDs[i]);
						}
					}
					
					LogUtils.operationLog(logFile, logContPre+"Robot gets materials from rack started");
					for(String rackID:slotInfo.keySet()){
						RunningMsg.set("Robot "+robotModel+"/"+robotIP+" moves to Rack "+rackID);
						// use dock to charger of IRobot 2021.11.3 Hui Zhi 
						//if(!robotCtrl.moveToRack(robotIP, rackID, cncModel)){
						//FIXME Need to get the rack position from database 2022/5/18 Hui Zhi
						//if(!robotCtrl.dockTo(robotIP, robotData.getChargerPosition(robotIP))){
						
						if(!robotCtrl.dockTo(robotIP, "virtual_cnc_1")){
							robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
							//Unlock scheduled machine and workpieces
							cncData.setCncState(cncIP, DeviceState.STANDBY, false, false, true, false);
							resetMaterialState(wpIDs);
							
							taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_RACK.getErrDesc(), false);
							LogUtils.operationLog(logFile, logContPre+"Robot moves to rack "+rackID+" failed");
							return;
						}
						robotData.setRobotState(robotIP, DeviceState.WORKING, true, false, true, false);
						LogUtils.operationLog(logFile, logContPre+"Robot moves to rack "+rackID+" done");
						
						if(!getMaterialFromRack(robotCtrl, robotIP, rackID, slotInfo.get(rackID).split(","), wpInfo.get(rackID).split(","), logFile, logContPre, cncModel)){
							robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
							//Unlock scheduled machine and workpieces
							cncData.setCncState(cncIP, DeviceState.STANDBY, false, false, true, false);
							checkMaterialOfRobot(taskID,logFile, logContPre);
							
							taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_PICKMTFROM_RACK.getErrDesc(), false);
							return;
						}
					}
					checkMaterialOfRobot(taskID, logFile, logContPre);
					LogUtils.operationLog(logFile, logContPre+"Robot gets materials from rack done");
				}
				//disable by Hui Zhi 
//				else{ //Machining finished
//					//TODO Cleaning
//					RunningMsg.set("CNC 1st cleaning");
//					LogUtils.operationLog(logFile, logContPre+"CNC 1st cleaning started");
//					int[] wkZones = new int[]{1,2,3,4,5};
//					String[] codeIDs = new String[]{"00000000131","00000000232","00000000333","00000000434","00000000535"};
//					cncData.setCNCTimingData(cncIP, CncItems.DT_CLEANING_START1, System.currentTimeMillis());
//					if(!"".equals(doCleaning(cncCtrl,cncIP,wkZones,codeIDs,null))){
//						RunningMsg.set("CNC 1st cleaning failed");
//						cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
//						
//						//Unlock the robot
//						robotData.setRobotState(robotIP, DeviceState.STANDBY, false, false, true, false);
//						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_PICKMTFROM_MACHINE.getErrDesc(), false);
//						LogUtils.operationLog(logFile, logContPre+"CNC 1st cleaning failed");
//						return;
//					}
//					cncData.setCNCTimingData(cncIP, CncItems.DT_CLEANING_STOP1, System.currentTimeMillis());
//					robotData.setData(robotIP, RobotItems.OP_STOPPNPTESTING, "1");
//					LogUtils.operationLog(logFile, logContPre+"CNC 1st cleaning done");
//					
//					//TODO For testing stage only	//disabled by Hui Zhi 2021/12/28
//					RunningMsg.set("Check 1st PNP testing status at HT");
//					LogUtils.operationLog(logFile, logContPre+"1st PNP testing status checking");
//					if(!concurrentOPDone(cncIP,robotIP,DeviceOP.ROBOT_PNPTESTING,CONCURR_OP_TIMEOUT)){
//						robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
//						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_PICKMTFROM_RACK.getErrDesc(), false);
//						LogUtils.operationLog(logFile, logContPre+"1st PNP testing status checking timeout");
//						return;
//					}
//					LogUtils.operationLog(logFile, logContPre+"1st PNP testing status checking done");
//				}
				
				//* 2023//7/20 add by Paris */
//				RunningMsg.set("Check XYZ Home Position "+cncIP);
//				LogUtils.operationLog(logFile, logContPre+"Check XYZ Home Position");
//				
//				if (!cncCtrl.hfCheckPosition(cncIP)){
//					LogUtils.operationLog(logFile, logContPre+"Machine XYZ not in Home Position");
//					return;
//				}
				/* end */
				
				RunningMsg.set("Robot moves to "+cncIP);
				LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT_MOVETOMACHINE starts");
				if(runConcurrentOP(DeviceOP.ROBOT_MOVETOMACHINE, cncIP, robotIP, null, cncCtrl, robotCtrl, 1, 360)){//change 30 second to 60 by Hui Zhi  2022/5/18
					LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT_MOVETOMACHINE done");
				}else{
					robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
					taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_MACHINE.getErrDesc(), false);
					LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT_MOVETOMACHINE failed");
					return;
				}
				robotData.setRobotState(robotIP, DeviceState.WORKING, true, false, true, false);
				
				RunningMsg.set("CNC door opening");
				if(!cncCtrl.hfOpenDoor(cncIP)){
					cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
					taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_OPEN_DOOR.getErrDesc(), false);
					if(cncState == DeviceState.FINISH){
						//Unlock the robot
						robotData.setRobotState(robotIP, DeviceState.STANDBY, false, false, true, false);
					}
					RunningMsg.set(cncModel+"("+cncIP+") fail to open door");
					LogUtils.operationLog(logFile, logContPre+"Machine opens door failed");
					return;
				}
				LogUtils.operationLog(logFile, logContPre+"Machine opens door done");
				
				//Refresh machining information - to trace workpiece's status
				if(DeviceState.FINISH != cncState && !"".equals(wkpIDs)){
					String[] ids = wkpIDs.split(";");
					long simulationT = 0;
					for(int i=0; i<ids.length; i++){
						LinkedHashMap<SpecItems,Object> spec = wpData.getNextProcInfo(ids[i]);
						int curProc = wpData.getNextProcNo(ids[i], spec);
						int procTime = wpData.getNextProcSimtime(ids[i], cncModel, spec, curProc);
						simulationT += procTime;
						
						wpData.setCurrentProcNo(ids[i], curProc);
						wpData.appendData(ids[i], WorkpieceItems.NCMODEL, cncModel);
						wpData.appendData(ids[i], WorkpieceItems.MACHINETIME, ""+procTime);
						wpData.appendData(ids[i], WorkpieceItems.PROCESS, wpData.getNextProcName(ids[i], spec, curProc));
						wpData.appendData(ids[i], WorkpieceItems.PROGRAM, wpData.getNextProcProgram(ids[i], cncModel, spec, curProc));
						wpData.appendData(ids[i], WorkpieceItems.SURFACE, ""+wpData.getNextProcSurface(ids[i], spec, curProc));
						
						cncData.setExpectedMachiningTime(cncIP, simulationT);
					}
				}
				
				if(cncState == DeviceState.FINISH){
					LinkedHashMap<Integer,String> wpIDs = cncData.getWorkpieceIDs(cncIP);
					if(wpIDs.isEmpty()){
						RunningMsg.set("Robot fails to unload materials from "+cncIP+"(no materials)");
						cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
						
						//Unlock the robot
						robotData.setRobotState(robotIP, DeviceState.STANDBY, false, false, true, false);
						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_PICKMTFROM_MACHINE.getErrDesc(), false);
						LogUtils.operationLog(logFile, logContPre+"Robot unloads materials from machine failed cause of no materials");
						return;
					}
					
					RunningMsg.set("Robot unloads material from Machine "+cncIP);
					String workZones = "", wpID = "", ids = "";
					for(int workZone:wpIDs.keySet()){
						wpID = wpIDs.get(workZone);
						RunningMsg.set("Machine release fixture "+workZone);
						
						/* disable 2023/7/20 release fixture at Axis C
						if(!cncCtrl.releaseFixture(cncIP, workZone)){
							cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
							
							//Unlock the robot
							robotData.setRobotState(robotIP, DeviceState.STANDBY, false, false, true, false);
							taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_RELEASE_FIXTURE.getErrDesc(), false);
							LogUtils.operationLog(logFile, logContPre+"Machine releases fixture "+workZone+"/"+wpID+" failed");
							return;
						}
						*/
						
						LogUtils.operationLog(logFile, logContPre+"Machine releases fixture "+workZone+"/"+wpID+" done");
						
						if("".equals(workZones)){
							workZones = "" + workZone;
							ids = wpID;
						}else{
							workZones += "," + workZone;
							ids += "," + wpID;
						}
					}
					
					//Check whether the Robot has moved to target CNC or not
					LogUtils.operationLog(logFile, logContPre+"Robot moving to machine status checking");
					if(!concurrentOPDone(cncIP,robotIP,DeviceOP.ROBOT_MOVETOMACHINE,CONCURR_OP_TIMEOUT)){
						robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_MACHINE.getErrDesc(), false);
						LogUtils.operationLog(logFile, logContPre+"Robot moves to machine failed");
						return;
					}
					LogUtils.operationLog(logFile, logContPre+"Robot moves to machine done");
					
					//Robot unloads materials from CNC
					boolean bOK = unloadMaterialFromMachine(robotCtrl, robotIP, cncCtrl, cncIP, workZones.split(","), ids.split(","), logFile, logContPre);
					if(!bOK){
						RunningMsg.set("Robot fails to unload materials from "+cncIP);
						robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
						cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_PICKMTFROM_MACHINE.getErrDesc(), false);
						LogUtils.operationLog(logFile, logContPre+"Robot unloads materials from machine failed");
						return;
					}
					cncData.setData(cncIP, CncItems.DT_FINISHUNLOADING, System.currentTimeMillis());
					cncData.setData(cncIP, CncItems.FINISHSHOW, "");
					cncData.clearWorkpieceID(cncIP);//Clear the workpieces in CNC
					LogUtils.operationLog(logFile, logContPre+"Robot unloads materials from machine done");
					
					//Robot moves to Rack
					RunningMsg.set("Robot moves to Rack "+pRackIDs[0]);
					JSONObject jsonObj = new JSONObject();
					jsonObj.put("rackID", pRackIDs[0]);
					LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT_MOVETORACK starts");
					if(runConcurrentOP(DeviceOP.ROBOT_MOVETORACK, cncIP, robotIP, jsonObj, cncCtrl, robotCtrl, 1, 60)){//change 30 second to 60 by Hui Zhi  2022/5/18
						LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT_MOVETORACK done");
					}else{
						robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_RACK.getErrDesc(), false);
						LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT_MOVETORACK failed");
						return;
					}
					//disabled by Hui Zhi 2021/12/28
//					RunningMsg.set("CNC Door closing");
//					if(safeToCloseDoor(cncIP, robotIP,300)){
//						//TODO Cleaning
//						LogUtils.operationLog(logFile, logContPre+"CNC 2nd cleaning started");
//						RunningMsg.set("CNC cleaning");
//						jsonObj.clear();
//						jsonObj.put("cncIP", cncIP);
//						jsonObj.put("workZones", "1;5;6");
//						jsonObj.put("codeIDs", "00000000131;00000000535;00000000636");
//						LogUtils.operationLog(logFile, logContPre+"ConcurrentOP CNC_CLEANING starts");
//						if(runConcurrentOP(DeviceOP.CNC_CLEANING, cncIP, robotIP, jsonObj, cncCtrl, robotCtrl, 1, 30)){
//							cncData.setCNCTimingData(cncIP, CncItems.DT_CLEANING_START2, System.currentTimeMillis());
//							LogUtils.operationLog(logFile, logContPre+"ConcurrentOP CNC_CLEANING done");
//						}else{
//							cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
//							taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_START_MACHINING.getErrDesc(), false);
//							LogUtils.operationLog(logFile, logContPre+"ConcurrentOP CNC_CLEANING failed");
//							return;
//						}
//					}else{
//						cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
//						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_PICKMTFROM_MACHINE.getErrDesc(), false);
//						LogUtils.operationLog(logFile, logContPre+"CNC door closing for machine cleaning failed");
//						return;
//					}
					
					//Check whether Robot has moved to target Rack or not
					LogUtils.operationLog(logFile, logContPre+"Robot moving to Rack status checking");
					if(!concurrentOPDone(cncIP, robotIP,DeviceOP.ROBOT_MOVETORACK,CONCURR_OP_TIMEOUT)){
						robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_RACK.getErrDesc(), false);
						LogUtils.operationLog(logFile, logContPre+"Robot moves to Rack "+pRackIDs[0]+" failed");
						return;
					}
					LogUtils.operationLog(logFile, logContPre+"Robot moves to Rack "+pRackIDs[0]+" done");
					
					RunningMsg.set("Robot puts materials onto Rack "+pRackIDs[0]);
					if(!putMaterialOntoRack(robotCtrl, robotIP, pRackIDs[0], null, logFile, logContPre, cncModel)){
						robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_PUTMTONTO_RACK.getErrDesc(), false);
						return;
					}
					LogUtils.operationLog(logFile, logContPre+"Robot puts materials onto Rack "+pRackIDs[0]+" done");
					
					//TODO For testing stage only	//disabled by Hui Zhi 2021/23/28
//					LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT_PNPTESTING started");
//					if(runConcurrentOP(DeviceOP.ROBOT_PNPTESTING, cncIP, robotIP, jsonObj, cncCtrl, robotCtrl, 1, 30)){
//						LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT_PNPTESTING done");
//					}else{
//						LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT_PNPTESTING failed");
//					}
					
					//disabled by Hui Zhi 2021/23/28
					//Check whether CNC cleaning is done or not
//					RunningMsg.set("Check CNC 2nd cleaning status");
//					LogUtils.operationLog(logFile, logContPre+"CNC 2nd cleaning status checking");
//					if(!concurrentOPDone(cncIP,robotIP,DeviceOP.CNC_CLEANING,2000)){
//						cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
//						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_PICKMTFROM_MACHINE.getErrDesc(), false);
//						LogUtils.operationLog(logFile, logContPre+"CNC 2nd cleaning timeout");
//						return;
//					}
//					cncData.setCNCTimingData(cncIP, CncItems.DT_CLEANING_STOP2, System.currentTimeMillis());
//					robotData.setData(robotIP, RobotItems.OP_STOPPNPTESTING, "1");
//					LogUtils.operationLog(logFile, logContPre+"CNC 2nd cleaning done");
					
//					RunningMsg.set("CNC door opening");
//					if(!cncCtrl.openDoor(cncIP)){
//						cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
//						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_PICKMTFROM_MACHINE.getErrDesc(), false);
//						LogUtils.operationLog(logFile, logContPre+"CNC door opening failed after 2nd cleaning");
//						return;
//					}
//					LogUtils.operationLog(logFile, logContPre+"CNC door opening done");
					
					//TODO For testing stage only	//disabled by Hui Zhi 2021/23/28
//					RunningMsg.set("Check PNP testing status at HT");
//					LogUtils.operationLog(logFile, logContPre+"PNP testing status checking");
//					if(!concurrentOPDone(cncIP,robotIP,DeviceOP.ROBOT_PNPTESTING,CONCURR_OP_TIMEOUT)){
//						robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
//						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_PICKMTFROM_RACK.getErrDesc(), false);
//						LogUtils.operationLog(logFile, logContPre+"PNP testing status checking timeout");
//						return;
//					}
//					LogUtils.operationLog(logFile, logContPre+"PNP testing status checking done");
					
					//Unlock CNC
					cncData.setCncState(cncIP, DeviceState.STANDBY, true, false, true, false);
					LogUtils.operationLog(logFile, logContPre+"Unlock machine done");
					
					//Pop finished task
					cncData.saveMachiningData(cncIP);
					taskData.taskPopByID(taskID, true);
					
					//disable stopCharging 2021.11.3 Hui Zhi 
					//TODO Robot stops charging: For debug stage only
//					robotCtrl.stopCharging(robotIP, cncModel);
//					if(DebugUtils.autoInputMaterialEnabled()){
//						int iQty = MaterialInput.getInputQty(lineName);
//						if(iQty>0){
//							RackClient rackClient = RackClient.getInstance();
//							rackClient.randomUpdateMaterialRack(lineName, false, iQty);
//							RunningMsg.set("Workpiece clear");
//						}
//					}
					
					//Unlock Robot
					robotData.setRobotState(robotIP, DeviceState.STANDBY, true, false, true, false);
					LogUtils.operationLog(logFile, logContPre+"Unlock robot done");
				}else{
					//Check whether Robot has moved to target CNC or not
					
					LogUtils.operationLog(logFile, logContPre+"Robot moving to machine status checking");
					if(!concurrentOPDone(cncIP,robotIP,DeviceOP.ROBOT_MOVETOMACHINE,CONCURR_OP_TIMEOUT)){
						robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_MACHINE.getErrDesc(), false);
						LogUtils.operationLog(logFile, logContPre+"Robot moves to machine failed");
						return;
					}
					LogUtils.operationLog(logFile, logContPre+"Robot moves to machine done");
					
					RunningMsg.set("Robot loads material onto "+cncIP);
					if(!loadMaterialOntoMachine(robotCtrl,robotIP,cncCtrl,cncIP,logFile,logContPre)){
						robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_PUTMTONTO_MACHINE.getErrDesc(), false);
						RunningMsg.set("Robot fails to load materials onto "+cncIP);
						return;
					}
					LogUtils.operationLog(logFile, logContPre+"Robot loads materials onto machine done");
					
					// Create sub programs uploading thread
					// disable upload program for trial run 2023/6/30
//					// uploadNCProgram.uploadSubProgramsThread(cncCtrl, cncIP);
					
					// Unlock the Robot
					robotData.setRobotState(robotIP, DeviceState.STANDBY, true, false, true, false);
					LogUtils.operationLog(logFile, logContPre+"Unlock robot done");
					
					//TODO Robot moves to rack for debug stage only
					RunningMsg.set("Robot moves to Rack "+pRackIDs[0]);
					JSONObject jsonObj = new JSONObject();
					jsonObj.put("rackID", pRackIDs[0]);
					LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT_MOVETORACK starts");
					if(runConcurrentOP(DeviceOP.ROBOT_MOVETORACK, cncIP, robotIP, jsonObj, cncCtrl, robotCtrl, 1, 60)){//change 30 second to 60 by Hui Zhi  2022/5/18
						LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT_MOVETORACK "+pRackIDs[0]+" done");
					}else{
						robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_RACK.getErrDesc(), false);
						LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT_MOVETORACK "+pRackIDs[0]+" failed");
						return;
					}
					
					//Clamp all fixtures
					LinkedHashMap<Integer,String> wpIDs = cncData.getWorkpieceIDs(cncIP);
					String workpieceIDs = "";
					RunningMsg.set("Machine "+cncIP+" clamps fixtures");
					for(int wkZone:wpIDs.keySet()){
						if("".equals(workpieceIDs)){
							workpieceIDs = wpIDs.get(wkZone);
						}else{
							workpieceIDs += ";" + wpIDs.get(wkZone);
						}
						
						if(!cncCtrl.hfClampFixture(cncIP, wkZone)){
							cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
							taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_CLAMP_FIXTURE.getErrDesc(), false);
							RunningMsg.set("CNC fails to clamp fixtures");
							LogUtils.operationLog(logFile, logContPre+"Machine clamps fixture "+wkZone+"/"+wpIDs.get(wkZone)+" failed");
							return;
						}else{
							wpData.setWorkpieceState(wpIDs.get(wkZone), DeviceState.WORKING, false, true, false, true, false);
							//Update Rack Manager with workpiece WORKING state
							RackClient.getInstance().updateRackInfo(lineName, pRackIDs[0], wpIDs.get(wkZone), true, false, true);
							LogUtils.operationLog(logFile, logContPre+"Machine clamps fixture "+wkZone+"/"+wpIDs.get(wkZone)+" done");
						}
					}
					LogUtils.operationLog(logFile, logContPre+"Machine clamps fixtures done");
					
					//Disable for trial run Paris 2023/7/24
//					//Create Main Program uploading thread
//					RunningMsg.set("Machine "+cncIP+" gets main program");
//					Thread uploadProgramThr = uploadNCProgram.uploadMainProgramThread(cncCtrl, cncIP);
//					uploadProgramThr.join();
//					
//					if(cncData.subProgramOK(cncIP) && cncData.mainProgramOK(cncIP)){
//						String mainProgramName = (String) cncData.getData(cncIP).get(CncItems.MAINPROGRAM);
//						if(!cncCtrl.mainProgramIsActivate(cncIP, mainProgramName)){
//							cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
//							taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_MAINPROGRAM.getErrDesc(), false);
//							RunningMsg.set("CNC Main Program is not activated");
//							LogUtils.operationLog(logFile, logContPre+"Machine gets main program failed:Main program is not activated");
//							return;
//						}
//						LogUtils.operationLog(logFile, logContPre+"Machine gets main program done");
//					}else{
//						if(!cncData.subProgramOK(cncIP)) LogUtils.operationLog(logFile, logContPre+"Machine gets sub programs failed");
//						if(!cncData.mainProgramOK(cncIP)) LogUtils.operationLog(logFile, logContPre+"Machine gets main program failed");
//						cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
//						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_NCPROGRAM.getErrDesc(), false);
//						RunningMsg.set("CNC fails get machining programs");
//						return;
//					}
//					
//					RunningMsg.set("Safe check for door closing");
//					
//					if(!safeToCloseDoor(cncIP, robotIP, robotCtrl, 300)){
//						cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
//						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_CLOSE_DOOR.getErrDesc(), false);
//						RunningMsg.set("Safe check for door closing timeout");
//						LogUtils.operationLog(logFile, logContPre+"Safe check for door closing timeout");
//						return;
//					}
//					
					RunningMsg.set("CNC door closing");
					if(!cncCtrl.hfCloseDoor(cncIP)){
						cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_CLOSE_DOOR.getErrDesc(), false);
						RunningMsg.set("CNC fails to close door");
						LogUtils.operationLog(logFile, logContPre+"Machine fails to close door");
						return;
					}
					LogUtils.operationLog(logFile, logContPre+"Machine closes door done");
					
					RunningMsg.set("CNC starts machining");
					LinkedHashMap<CncWebAPIItems, String> cncWebAPICommonCfg = cncWebAPI.getCommonCfg(cncModel);
					String mainProgram = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGMAIN);
					
					if(!cncCtrl.startMachinePrepared(cncIP)){
						cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_START_MACHINING.getErrDesc(), false);
						RunningMsg.set("CNC fail to do machining");
						LogUtils.operationLog(logFile, logContPre+"Prepared start 900 '1' failed");
						return;
					}
					
//					if(!startMachining(cncCtrl,cncIP,mainProgram,3,10)){
					if(!cncCtrl.startMachining(cncIP,"")){
						cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_START_MACHINING.getErrDesc(), false);
						RunningMsg.set("CNC fail to do machining");
						LogUtils.operationLog(logFile, logContPre+"Machine starts machining failed");
						return;
					}else{
						LogUtils.operationLog(logFile, logContPre+"Machine starts machining");
						cncData.setCncLastState(cncIP, DeviceState.LOCK);
					}
					
					//TODO For testing stage only	//disabled by Hui Zhi 2021/12/28
//					LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT_PNPTESTING started");
//					if(runConcurrentOP(DeviceOP.ROBOT_PNPTESTING, cncIP, robotIP, jsonObj, cncCtrl, robotCtrl, 1, 30)){
//						LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT_PNPTESTING done");
//					}else{
//						LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT_PNPTESTING failed");
//					}
				}
				
				if(taskData.taskCount() <= 0) RunningMsg.set("Task done");
			} catch (Exception e) {
				LogUtils.errorLog("TaskProcessor-TaskHandle ERR:"+e.getMessage());
			}
		}
	}
}
