package com.cncmes.utils;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.cncmes.base.DeviceState;
import com.cncmes.base.DriverItems;
import com.cncmes.base.MescodeItems;
import com.cncmes.base.RackItems;
import com.cncmes.base.SchedulerItems;
import com.cncmes.base.SpecItems;
import com.cncmes.base.WorkpieceItems;
import com.cncmes.dao.DAO;
import com.cncmes.dao.impl.DAOImpl;
import com.cncmes.data.CncDriver;
import com.cncmes.data.MachiningSpec;
import com.cncmes.data.Mescode;
import com.cncmes.data.RackMaterial;
import com.cncmes.data.RackProduct;
import com.cncmes.data.RackTemp;
import com.cncmes.data.RobotDriver;
import com.cncmes.data.ScannerDriver;
import com.cncmes.data.SchedulerCfg;
import com.cncmes.data.WorkpieceData;
import com.cncmes.dto.CNCMachiningSpec;
import com.cncmes.dto.CNCMescode;
import com.cncmes.dto.CNCModels;
import com.cncmes.dto.CNCRacksInfo;
import com.cncmes.dto.CNCRobots;
import com.cncmes.dto.CNCScanners;
import com.cncmes.dto.CNCScheduler;

public class DataUtils {
	private DataUtils(){}
	
	/**
	 * 
	 * @param ip the device IP or object ID
	 * @return the local path to store the memory dump
	 */
	public static String getMemoryDumpPath(String ip){
		String path = getMemoryDumpRootDir();
		
		if(MyFileUtils.makeFolder(path)){
			path = path + File.separator + ip.replace(":", "_") + ".txt";
		}else{
			path = "";
		}
		
		return path;
	}
	
	public static String getMemoryDumpRootDir(){
		return System.getProperty("user.dir") + File.separator + "memDump";
	}
	
	public static DeviceState getDevStateByString(String state){
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
	
	public static int getSpecIDByWorkpieceID(String workpieceID){
		int specID = 0;
		
		Mescode mescode = Mescode.getInstance();
		Map<String, LinkedHashMap<MescodeItems, Object>> mescodeMap = mescode.getDataMap();
		if(null!=workpieceID && mescodeMap.size()>0){
			for(String mcode:mescodeMap.keySet()){
				if(workpieceID.startsWith(mcode)){
					specID = (int)mescode.getData(mcode).get(MescodeItems.SPECID);
					break;
				}
			}
		}
		
		return specID;
	}
	
	public static String getMescodeByWorkpieceID(String workpieceID){
		String strCode = "";
		
		Mescode mescode = Mescode.getInstance();
		Map<String, LinkedHashMap<MescodeItems, Object>> mescodeMap = mescode.getDataMap();
		if(null!=workpieceID && mescodeMap.size()>0){
			for(String mcode:mescodeMap.keySet()){
				if(workpieceID.startsWith(mcode)){
					strCode = mcode;
					break;
				}
			}
		}
		
		return strCode;
	}
	
	public static void updateWorkpieceData(String workpieceID, String lineName, String rackID, String slotNo){
		WorkpieceData wpData = WorkpieceData.getInstance();
		MachiningSpec mSpec = MachiningSpec.getInstance();
		LinkedHashMap<SpecItems,Object> spec = new LinkedHashMap<SpecItems,Object>();
		
		int specid = 0;
		int procqty = 0;
		wpData.setData(workpieceID, WorkpieceItems.ID, workpieceID);
		if(!"".equals(lineName)) wpData.setData(workpieceID, WorkpieceItems.LINENAME, lineName);
		if(!"".equals(rackID)) wpData.setData(workpieceID, WorkpieceItems.CONVEYORID, rackID);
		if(!"".equals(slotNo)) wpData.setData(workpieceID, WorkpieceItems.CONVEYORSLOTNO, slotNo);
		
		specid = getSpecIDByWorkpieceID(workpieceID);
		wpData.setData(workpieceID, WorkpieceItems.SPECID, specid);
		
		if(specid > 0){
			spec = mSpec.getData(String.valueOf(specid));
			if(null != spec && (int)spec.get(SpecItems.PROCESSQTY) > 0){
				procqty = (int)spec.get(SpecItems.PROCESSQTY);
				wpData.setData(workpieceID, WorkpieceItems.PROCQTY, procqty);
			}
		}else{
			wpData.setData(workpieceID, WorkpieceItems.PROCQTY, procqty);
		}
		
		if(procqty > 0){
			if(procqty >= 1) wpData.setData(workpieceID, WorkpieceItems.PROC1STATUS, DeviceState.STANDBY);
			if(procqty >= 2) wpData.setData(workpieceID, WorkpieceItems.PROC2STATUS, DeviceState.STANDBY);
			if(procqty >= 3) wpData.setData(workpieceID, WorkpieceItems.PROC3STATUS, DeviceState.STANDBY);
			if(procqty >= 4) wpData.setData(workpieceID, WorkpieceItems.PROC4STATUS, DeviceState.STANDBY);
			if(procqty >= 5) wpData.setData(workpieceID, WorkpieceItems.PROC5STATUS, DeviceState.STANDBY);
			if(procqty >= 6) wpData.setData(workpieceID, WorkpieceItems.PROC6STATUS, DeviceState.STANDBY);
		}
	}
	
	public static String getMachiningSpec(){
		String msg = "OK";
		MachiningSpec mSpec = MachiningSpec.getInstance();
		
		DAO dao = new DAOImpl("com.cncmes.dto.CNCMachiningSpec");
		try {
			ArrayList<Object> vos = dao.findAll();
			if(null != vos){
				for(int i=0; i<vos.size(); i++){
					CNCMachiningSpec vo = (CNCMachiningSpec) vos.get(i);
					
					if(null != vo.getProc1_name() && !"".equals(vo.getProc1_name()) && vo.getProc1_surface() > 0){
						LinkedHashMap<SpecItems,Object> proc = new LinkedHashMap<SpecItems,Object>();
						proc.put(SpecItems.PROCESSNAME, vo.getProc1_name());
						proc.put(SpecItems.PROGRAM, vo.getProc1_program());
						proc.put(SpecItems.NCMODEL, vo.getProc1_ncmodel());
						proc.put(SpecItems.SIMTIME, vo.getProc1_simtime());
						proc.put(SpecItems.SURFACE, vo.getProc1_surface());
						proc.put(SpecItems.PROCNO, 1);
						
						mSpec.setData(String.valueOf(vo.getId()), SpecItems.ID, vo.getId());
						mSpec.setData(String.valueOf(vo.getId()), SpecItems.PROCESSQTY, 1);
						mSpec.setData(String.valueOf(vo.getId()), SpecItems.PROC1, proc);
					}else{
						continue;
					}
					
					if(null != vo.getProc2_name() && !"".equals(vo.getProc2_name()) && vo.getProc2_surface() > 0){
						LinkedHashMap<SpecItems,Object> proc = new LinkedHashMap<SpecItems,Object>();
						proc.put(SpecItems.PROCESSNAME, vo.getProc2_name());
						proc.put(SpecItems.PROGRAM, vo.getProc2_program());
						proc.put(SpecItems.NCMODEL, vo.getProc2_ncmodel());
						proc.put(SpecItems.SIMTIME, vo.getProc2_simtime());
						proc.put(SpecItems.SURFACE, vo.getProc2_surface());
						proc.put(SpecItems.PROCNO, 2);
						
						mSpec.setData(String.valueOf(vo.getId()), SpecItems.PROCESSQTY, 2);
						mSpec.setData(String.valueOf(vo.getId()), SpecItems.PROC2, proc);
					}else{
						continue;
					}
					
					if(null != vo.getProc3_name() && !"".equals(vo.getProc3_name()) && vo.getProc3_surface() > 0){
						LinkedHashMap<SpecItems,Object> proc = new LinkedHashMap<SpecItems,Object>();
						proc.put(SpecItems.PROCESSNAME, vo.getProc3_name());
						proc.put(SpecItems.PROGRAM, vo.getProc3_program());
						proc.put(SpecItems.NCMODEL, vo.getProc3_ncmodel());
						proc.put(SpecItems.SIMTIME, vo.getProc3_simtime());
						proc.put(SpecItems.SURFACE, vo.getProc3_surface());
						proc.put(SpecItems.PROCNO, 3);
						
						mSpec.setData(String.valueOf(vo.getId()), SpecItems.PROCESSQTY, 3);
						mSpec.setData(String.valueOf(vo.getId()), SpecItems.PROC3, proc);
					}else{
						continue;
					}
					
					if(null != vo.getProc4_name() && !"".equals(vo.getProc4_name()) && vo.getProc4_surface() > 0){
						LinkedHashMap<SpecItems,Object> proc = new LinkedHashMap<SpecItems,Object>();
						proc.put(SpecItems.PROCESSNAME, vo.getProc4_name());
						proc.put(SpecItems.PROGRAM, vo.getProc4_program());
						proc.put(SpecItems.NCMODEL, vo.getProc4_ncmodel());
						proc.put(SpecItems.SIMTIME, vo.getProc4_simtime());
						proc.put(SpecItems.SURFACE, vo.getProc4_surface());
						proc.put(SpecItems.PROCNO, 4);
						
						mSpec.setData(String.valueOf(vo.getId()), SpecItems.PROCESSQTY, 4);
						mSpec.setData(String.valueOf(vo.getId()), SpecItems.PROC4, proc);
					}else{
						continue;
					}
					
					if(null != vo.getProc5_name() && !"".equals(vo.getProc5_name()) && vo.getProc5_surface() > 0){
						LinkedHashMap<SpecItems,Object> proc = new LinkedHashMap<SpecItems,Object>();
						proc.put(SpecItems.PROCESSNAME, vo.getProc5_name());
						proc.put(SpecItems.PROGRAM, vo.getProc5_program());
						proc.put(SpecItems.NCMODEL, vo.getProc5_ncmodel());
						proc.put(SpecItems.SIMTIME, vo.getProc5_simtime());
						proc.put(SpecItems.SURFACE, vo.getProc5_surface());
						proc.put(SpecItems.PROCNO, 5);
						
						mSpec.setData(String.valueOf(vo.getId()), SpecItems.PROCESSQTY, 5);
						mSpec.setData(String.valueOf(vo.getId()), SpecItems.PROC5, proc);
					}else{
						continue;
					}
					
					if(null != vo.getProc6_name() && !"".equals(vo.getProc6_name()) && vo.getProc6_surface() > 0){
						LinkedHashMap<SpecItems,Object> proc = new LinkedHashMap<SpecItems,Object>();
						proc.put(SpecItems.PROCESSNAME, vo.getProc6_name());
						proc.put(SpecItems.PROGRAM, vo.getProc6_program());
						proc.put(SpecItems.NCMODEL, vo.getProc6_ncmodel());
						proc.put(SpecItems.SIMTIME, vo.getProc6_simtime());
						proc.put(SpecItems.SURFACE, vo.getProc6_surface());
						proc.put(SpecItems.PROCNO, 6);
						
						mSpec.setData(String.valueOf(vo.getId()), SpecItems.PROCESSQTY, 6);
						mSpec.setData(String.valueOf(vo.getId()), SpecItems.PROC6, proc);
					}else{
						continue;
					}
				}
			}
		} catch (SQLException e) {
			msg = "DataUtils getMachiningSpec ERR:"+e.getMessage();
		}
		
		return msg;
	}
	
	public static String getMescode(){
		String msg = "OK";
		Mescode mescode = Mescode.getInstance();
		
		DAO dao = new DAOImpl("com.cncmes.dto.CNCMescode");
		try {
			ArrayList<Object> vos = dao.findAll();
			if(null != vos){
				for(int i=0; i<vos.size(); i++){
					CNCMescode vo = (CNCMescode) vos.get(i);
					mescode.setData(vo.getMescode(), MescodeItems.MESCODE, vo.getMescode());
					mescode.setData(vo.getMescode(), MescodeItems.SPECID, vo.getSpecid());
				}
			}
		} catch (SQLException e) {
			msg = "DataUtils getMescode ERR:"+e.getMessage();
		}
		
		return msg;
	}
	
	public static String getDeviceDriver(){
		String msg = "OK";
		CncDriver cncDriver = CncDriver.getInstance();
		RobotDriver robotDriver = RobotDriver.getInstance();
		ScannerDriver scannerDriver = ScannerDriver.getInstance();
		
		DAO dao = new DAOImpl("com.cncmes.dto.CNCModels");
		try {
			ArrayList<Object> vos = dao.findAll();
			if(null != vos){
				for(int i=0; i<vos.size(); i++){
					CNCModels vo = (CNCModels) vos.get(i);
					cncDriver.setData(vo.getCnc_model(), DriverItems.MODEL, vo.getCnc_model());
					cncDriver.setData(vo.getCnc_model(), DriverItems.DRIVER, vo.getDriver());
					cncDriver.setData(vo.getCnc_model(), DriverItems.PROCESS, vo.getMachiningprocess());
					cncDriver.setData(vo.getCnc_model(), DriverItems.CMDENDCHR, vo.getCmdEndChr());
				}
			}
		} catch (SQLException e) {
			msg = "DataUtils getDeviceDriver(CNC) ERR:"+e.getMessage();
			return msg;
		}
		
		dao = new DAOImpl("com.cncmes.dto.CNCRobots");
		try {
			ArrayList<Object> vos = dao.findAll();
			if(null != vos){
				for(int i=0; i<vos.size(); i++){
					CNCRobots vo = (CNCRobots) vos.get(i);
					robotDriver.setData(vo.getRobot_model(), DriverItems.MODEL, vo.getRobot_model());
					robotDriver.setData(vo.getRobot_model(), DriverItems.DRIVER, vo.getDriver());
					robotDriver.setData(vo.getRobot_model(), DriverItems.CMDENDCHR, vo.getCmdEndChr());
				}
			}
		} catch (SQLException e) {
			msg = "DataUtils getDeviceDriver(Robot) ERR:"+e.getMessage();
			return msg;
		}
		
		dao = new DAOImpl("com.cncmes.dto.CNCScanners");
		try {
			ArrayList<Object> vos = dao.findAll();
			if(null != vos){
				for(int i=0; i<vos.size(); i++){
					CNCScanners vo = (CNCScanners) vos.get(i);
					scannerDriver.setData(vo.getScanner_model(), DriverItems.MODEL, vo.getScanner_model());
					scannerDriver.setData(vo.getScanner_model(), DriverItems.DRIVER, vo.getDriver());
					scannerDriver.setData(vo.getScanner_model(), DriverItems.CMDENDCHR, vo.getCmdEndChr());
				}
			}
		} catch (SQLException e) {
			msg = "DataUtils getDeviceDriver(Scanner) ERR:"+e.getMessage();
			return msg;
		}
		
		return msg;
	}
	
	public static String getDeviceInfo(){
		String msg = "OK";
		
		msg = getRacksInfo();
		if(!"OK".equals(msg)) return msg;
		
		msg = getSchedulerConfig();
		if(!"OK".equals(msg)) return msg;
		
		return msg;
	}
	
	public static String getRacksInfo(){
		String msg = "OK";
		RackMaterial rackMaterial = RackMaterial.getInstance();
		RackProduct rackProduct = RackProduct.getInstance();
		RackTemp rackTemp = RackTemp.getInstance();
		
		DAO dao = new DAOImpl("com.cncmes.dto.CNCRacksInfo");
		try {
			ArrayList<Object> vos = dao.findAll();
			if(null != vos){
				for(int i=0; i<vos.size(); i++){
					CNCRacksInfo vo = (CNCRacksInfo) vos.get(i);
					String mainKey = vo.getLinename() + "_" + vo.getId();
					if(0 == vo.getRacktype()){
						rackMaterial.setData(mainKey, RackItems.ID, vo.getId());
						rackMaterial.setData(mainKey, RackItems.CAPACITY, vo.getCapacity());
						rackMaterial.setData(mainKey, RackItems.LINENAME, vo.getLinename());
						
						rackTemp.setData(mainKey, RackItems.ID, vo.getId());
						rackTemp.setData(mainKey, RackItems.CAPACITY, vo.getCapacity());
						rackTemp.setData(mainKey, RackItems.LINENAME, vo.getLinename());
					}else{
						rackProduct.setData(mainKey, RackItems.ID, vo.getId());
						rackProduct.setData(mainKey, RackItems.CAPACITY, vo.getCapacity());
						rackProduct.setData(mainKey, RackItems.LINENAME, vo.getLinename());
						rackProduct.setData(mainKey, RackItems.IP, vo.getIp());
						rackProduct.setData(mainKey, RackItems.PORT, vo.getPort());
						rackProduct.setData(mainKey, RackItems.STATE, DeviceState.SHUTDOWN);
					}
				}
			}
		} catch (SQLException e) {
			msg = "DataUtils getRacksInfo ERR:"+e.getMessage();
		}
		
		return msg;
	}
	
	public static String getSchedulerConfig(){
		String msg = "OK";
		SchedulerCfg sConfig = SchedulerCfg.getInstance();
		
		DAO dao = new DAOImpl("com.cncmes.dto.CNCScheduler");
		try {
			ArrayList<Object> vos = dao.findAll();
			if(null != vos){
				for(int i=0; i<vos.size(); i++){
					CNCScheduler vo = (CNCScheduler) vos.get(i);
					sConfig.setData(vo.getIp(), SchedulerItems.IP, vo.getIp());
					sConfig.setData(vo.getIp(), SchedulerItems.PORTMACHINE, vo.getPortmachine());
					sConfig.setData(vo.getIp(), SchedulerItems.PORTMATERIAL, vo.getPortmaterial());
					sConfig.setData(vo.getIp(), SchedulerItems.PORTROBOT, vo.getPortrobot());
					sConfig.setData(vo.getIp(), SchedulerItems.PORTTASK, vo.getPorttask());
					sConfig.setData(vo.getIp(), SchedulerItems.PORTRACK, vo.getPortrack());
					sConfig.setData(vo.getIp(), SchedulerItems.STATE, DeviceState.SHUTDOWN);
				}
			}
		} catch (SQLException e) {
			msg = "DataUtils getSchedulerConfig ERR:"+e.getMessage();
		}
		
		return msg;
	}
}