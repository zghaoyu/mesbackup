package com.cncmes.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.cncmes.base.CncItems;
import com.cncmes.base.DeviceState;
import com.cncmes.base.DriverItems;
import com.cncmes.base.MescodeItems;
import com.cncmes.base.RackItems;
import com.cncmes.base.RobotItems;
import com.cncmes.base.ScannerItems;
import com.cncmes.base.SchedulerItems;
import com.cncmes.base.SpecItems;
import com.cncmes.base.WorkpieceItems;
import com.cncmes.ctrl.CncFactory;
import com.cncmes.ctrl.RobotFactory;
import com.cncmes.dao.DAO;
import com.cncmes.dao.impl.DAOImpl;
import com.cncmes.data.CncData;
import com.cncmes.data.CncDriver;
import com.cncmes.data.DevHelper;
import com.cncmes.data.MachiningSpec;
import com.cncmes.data.Mescode;
import com.cncmes.data.RackMaterial;
import com.cncmes.data.RackProduct;
import com.cncmes.data.RobotData;
import com.cncmes.data.RobotDriver;
import com.cncmes.data.ScannerData;
import com.cncmes.data.ScannerDriver;
import com.cncmes.data.SchedulerCfg;
import com.cncmes.data.WorkpieceData;
import com.cncmes.dto.CNCHelper;
import com.cncmes.dto.CNCLines;
import com.cncmes.dto.CNCLinesInfo;
import com.cncmes.dto.CNCMachiningSpec;
import com.cncmes.dto.CNCMescode;
import com.cncmes.dto.CNCModels;
import com.cncmes.dto.CNCRacksInfo;
import com.cncmes.dto.CNCRobots;
import com.cncmes.dto.CNCRobotsInfo;
import com.cncmes.dto.CNCScanners;
import com.cncmes.dto.CNCScannersInfo;
import com.cncmes.dto.CNCScheduler;

public class DataUtils {
	private static Map<String,LinkedHashMap<String,Object>> cncList = new LinkedHashMap<String,LinkedHashMap<String,Object>>();
	private DataUtils(){}
	
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
		if("HANDLING".equals(state)) devState = DeviceState.HANDLING;
		if("DRIVERFAIL".equals(state)) devState = DeviceState.DRIVERFAIL;
		
		return devState;
	}
	
	/**
	 * 
	 * @return new String[]{""} if error happens or no line set in the database
	 */
	public static String[] getCNCLines(){
		String[] lineNames = null;
		DAO dao = new DAOImpl("com.cncmes.dto.CNCLines");
		try {
			ArrayList<Object> vos = dao.findAll();
			lineNames = new String[vos.size()];
			for(int i=0; i<vos.size(); i++){
				CNCLines vo = (CNCLines)vos.get(i);
				lineNames[i] = vo.getLinename();
			}
		} catch (SQLException e) {
			lineNames = new String[]{""};
		}
		
		return lineNames;
	}
	
	/**
	 * 
	 * @param lineName
	 * @return empty map if error happens or lineName is not found
	 */
	public static Map<String,LinkedHashMap<String,Object>> getCNCListByLineName(String lineName){
		LinkedHashMap<String,Object> val = new LinkedHashMap<String,Object>();
		cncList.clear();
		
		DAO dao = new DAOImpl("com.cncmes.dto.CNCLinesInfo");
		try {
			ArrayList<Object> vos = dao.findByCnd(new String[]{"linename"}, new String[]{lineName});
			if(null != vos){
				for(int i=0; i<vos.size(); i++){
					CNCLinesInfo vo = (CNCLinesInfo)vos.get(i);
					
					val.put("port", Integer.parseInt(vo.getCnc_port()));
					val.put("model", vo.getCnc_model());
					val.put("linename", lineName);
					cncList.put(vo.getCnc_ip(), val);
				}
			}
		} catch (SQLException e) {
			LogUtils.errorLog("DataUtils-getCNCListByLineName("+lineName+") ERR:"+e.getMessage());
		}
		
		return cncList;
	}
	
	/**
	 * 
	 * @return CNC list in the memory(No database access)
	 */
	public static Map<String,LinkedHashMap<String,Object>> getCNCList(){
		return cncList;
	}
	
	/**
	 * 
	 * @param lineName
	 * @return null if error happens or lineName is not found
	 */
	public static String[] getCNCsByLineName(String lineName){
		String[] cnc = null;
		getCNCListByLineName(lineName);
		
		if(!cncList.isEmpty()){
			cnc = new String[cncList.size()];
			Set<String> set = cncList.keySet();
			Iterator<String> it = set.iterator();
			int i = -1;
			while(it.hasNext()){
				i++;
				cnc[i] = it.next();
			}
		}
		
		return cnc;
	}
	
	public static long getSpecIDByWorkpieceID(String workpieceID){
		long specID = 0;
		
		Mescode mescode = Mescode.getInstance();
		Map<String, LinkedHashMap<MescodeItems, Object>> mescodeMap = mescode.getDataMap();
		if(null!=workpieceID && mescodeMap.size()>0){
			for(String mcode:mescodeMap.keySet()){
				if(workpieceID.startsWith(mcode)){
					specID = (long)mescode.getData(mcode).get(MescodeItems.SPECID);
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
	
	/**
	 * update workpiece info, specially the machining spec and reset the machining status
	 * @param workpieceID
	 * @param lineName
	 * @param rackID
	 * @param slotNo
	 */
	public static void updateWorkpieceData(String workpieceID, String lineName, String rackID, String slotNo){
		if(null==workpieceID || "".equals(workpieceID)) return;
		WorkpieceData wpData = WorkpieceData.getInstance();
		MachiningSpec mSpec = MachiningSpec.getInstance();
		LinkedHashMap<SpecItems,Object> spec = new LinkedHashMap<SpecItems,Object>();
		
		long specid = 0;
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
	
	/**
	 * load the machining spec into memory
	 * @return "OK" if no error, else return the failed reason
	 */
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
			msg = "DataUtils-getMachiningSpec ERR:"+e.getMessage();
			LogUtils.errorLog(msg);
		}
		
		return msg;
	}
	
	/**
	 * @Descripiton load the MES code list into memory
	 * @return "OK" if no error, else return the failed reason
	 */
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
			msg = "DataUtils-getMescode ERR:"+e.getMessage();
			LogUtils.errorLog(msg);
		}
		
		return msg;
	}
	
	/**
	 * @Descripiton load the device helpers into memory
	 * @return "OK" if no error, else return the failed reason
	 */
	public static String getDevHelper(){
		String msg = "OK";
		String model="",driver="",handler="";
		DevHelper devHelper = DevHelper.getInstance();
		
		DAO dao = new DAOImpl("com.cncmes.dto.CNCHelper");
		try {
			ArrayList<Object> vos = dao.findAll();
			if(null != vos){
				for(int i=0; i<vos.size(); i++){
					CNCHelper vo = (CNCHelper) vos.get(i);
					model = vo.getModel();
					driver = vo.getDriver();
					handler = vo.getHandler();
					devHelper.setData(model, DriverItems.MODEL, model);
					devHelper.setData(model, DriverItems.DRIVER, driver);
					devHelper.setData(model, DriverItems.DATAHANDLER, handler);
					devHelper.setData(model, DriverItems.CMDENDCHR, vo.getCmdend());
					
					if(model.endsWith("CNC")){
						devHelper.setData(model, DriverItems.CTRL, CncFactory.getInstance(driver, handler, model));
					}else if(model.endsWith("Robot")){
						devHelper.setData(model, DriverItems.CTRL, RobotFactory.getInstance(driver));
					}
				}
			}
		} catch (SQLException e) {
			msg = "DataUtils-getDevHelper ERR:"+e.getMessage();
			LogUtils.errorLog(msg);
		}
		
		return msg;
	}
	
	/**
	 * load devices driver into memory,including CNC,Robot and Scanner
	 * @return "OK" if no error, else return the failed reason
	 */
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
					cncDriver.setData(vo.getCnc_model(), DriverItems.CMDENDCHR, vo.getCmdendchr());
					cncDriver.setData(vo.getCnc_model(), DriverItems.DATAHANDLER, vo.getDatahandler());
				}
			}
		} catch (SQLException e) {
			msg = "DataUtils-getDeviceDriver(CNC) ERR:"+e.getMessage();
			LogUtils.errorLog(msg);
		}
		if(!"OK".equals(msg)) return msg;
		
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
			msg = "DataUtils-getDeviceDriver(Robot) ERR:"+e.getMessage();
			LogUtils.errorLog(msg);
		}
		if(!"OK".equals(msg)) return msg;
		
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
			msg = "DataUtils-getDeviceDriver(Scanner) ERR:"+e.getMessage();
			LogUtils.errorLog(msg);
		}
		
		return msg;
	}
	
	/**
	 * load devices info into memory,including CNC,Robot,Scanner,Rack and Scheduler
	 * @param lineName
	 * @return "OK" if no error, else return the failed reason
	 */
	public static String getDeviceInfo(String lineName){
		String msg = "OK";
		CncData cncData = CncData.getInstance();
		RobotData robotData = RobotData.getInstance();
		ScannerData scannerData = ScannerData.getInstance();
		
		cncData.clearData();
		DAO dao = new DAOImpl("com.cncmes.dto.CNCLinesInfo");
		try {
			ArrayList<Object> vos = null;
			if(null!=lineName && !"".equals(lineName)){
				vos = dao.findByCnd(new String[]{"linename"}, new String[]{lineName});
			}else{
				vos = dao.findAll();
			}
			if(null != vos){
				for(int i=0; i<vos.size(); i++){
					CNCLinesInfo vo = (CNCLinesInfo) vos.get(i);
					cncData.setData(vo.getCnc_ip(), CncItems.IP, vo.getCnc_ip());
					cncData.setData(vo.getCnc_ip(), CncItems.PORT, Integer.valueOf(vo.getCnc_port()));
					cncData.setData(vo.getCnc_ip(), CncItems.MODEL, vo.getCnc_model());
					cncData.setData(vo.getCnc_ip(), CncItems.STATE, DeviceState.SHUTDOWN);
					cncData.setData(vo.getCnc_ip(), CncItems.EXPMACHINETIME, 0);
					cncData.setData(vo.getCnc_ip(), CncItems.PARAS, "");
					cncData.setData(vo.getCnc_ip(), CncItems.ALARMMSG, "");
					cncData.setData(vo.getCnc_ip(), CncItems.COMMAND, "");
					cncData.setData(vo.getCnc_ip(), CncItems.LINENAME, vo.getLinename());
					cncData.setData(vo.getCnc_ip(), CncItems.WKZONEQTY, 6);
					if(null != vo.getFtp_ip()){
						cncData.setData(vo.getCnc_ip(), CncItems.FTPIP, vo.getFtp_ip());
					}else{
						cncData.setData(vo.getCnc_ip(), CncItems.FTPIP, "");
					}
					cncData.setData(vo.getCnc_ip(), CncItems.HELPER_IP, vo.getHelper_ip());
					cncData.setData(vo.getCnc_ip(), CncItems.HELPER_PORT, Integer.valueOf(vo.getHelper_port()));
					cncData.setData(vo.getCnc_ip(), CncItems.TAGNAME, vo.getCnc_tagName());
				}
			}
		} catch (SQLException e) {
			msg = "DataUtils-getDeviceInfo(CNC) ERR:"+e.getMessage();
			LogUtils.errorLog(msg);
		}
		if(!"OK".equals(msg)) return msg;
		
		robotData.clearData();
		dao = new DAOImpl("com.cncmes.dto.CNCRobotsInfo");
		try {
			ArrayList<Object> vos = null;
			if(null!=lineName && !"".equals(lineName)){
				vos = dao.findByCnd(new String[]{"linename"}, new String[]{lineName});
			}else{
				vos = dao.findAll();
			}
			if(null != vos){
				for(int i=0; i<vos.size(); i++){
					CNCRobotsInfo vo = (CNCRobotsInfo) vos.get(i);
					robotData.setData(vo.getRobot_ip(), RobotItems.IP, vo.getRobot_ip());
					robotData.setData(vo.getRobot_ip(), RobotItems.PORT, Integer.parseInt(vo.getRobot_port()));
					robotData.setData(vo.getRobot_ip(), RobotItems.MODEL, vo.getRobot_model());
					robotData.setData(vo.getRobot_ip(), RobotItems.STATE, DeviceState.SHUTDOWN);
					robotData.setData(vo.getRobot_ip(), RobotItems.SCANNERIP, vo.getScanner_ip());
					robotData.setData(vo.getRobot_ip(), RobotItems.CAPACITY, 9);
					robotData.setData(vo.getRobot_ip(), RobotItems.POSITION, "@Home:"+i);
					robotData.setData(vo.getRobot_ip(), RobotItems.LINENAME, vo.getLinename());
					robotData.setData(vo.getRobot_ip(), RobotItems.TAGNAME, vo.getTagname());
				}
			}
		} catch (SQLException e) {
			msg = "DataUtils-getDeviceInfo(Robot) ERR:"+e.getMessage();
			LogUtils.errorLog(msg);
		}
		if(!"OK".equals(msg)) return msg;
		
		dao = new DAOImpl("com.cncmes.dto.CNCScannersInfo");
		try {
			ArrayList<Object> vos = dao.findAll();
			if(null != vos){
				for(int i=0; i<vos.size(); i++){
					CNCScannersInfo vo = (CNCScannersInfo) vos.get(i);
					scannerData.setData(vo.getScanner_ip(), ScannerItems.IP, vo.getScanner_ip());
					scannerData.setData(vo.getScanner_ip(), ScannerItems.PORT, Integer.parseInt(vo.getScanner_port()));
					scannerData.setData(vo.getScanner_ip(), ScannerItems.MODEL, vo.getScanner_model());
					scannerData.setData(vo.getScanner_ip(), ScannerItems.STATE, DeviceState.SHUTDOWN);
				}
			}
		} catch (SQLException e) {
			msg = "DataUtils-getDeviceInfo(Scanner) ERR:"+e.getMessage();
			LogUtils.errorLog(msg);
		}
		if(!"OK".equals(msg)) return msg;
		
		msg = getRacksInfo(lineName);
		if(!"OK".equals(msg)) return msg;
		
		msg = getDevHelper();
		if(!"OK".equals(msg)) return msg;
		
		msg = getSchedulerConfig();
		if(!"OK".equals(msg)) return msg;
		
		msg = getMachiningSpec();
		if(!"OK".equals(msg)) return msg;
		
		msg = getMescode();
		if(!"OK".equals(msg)) return msg;
		
		msg = getDeviceDriver();
		if(!"OK".equals(msg)) return msg;
		
		return msg;
	}
	
	/**
	 * load racks info into memory
	 * @param lineName
	 * @return "OK" if no error, else return the failed reason
	 */
	public static String getRacksInfo(String lineName){
		String msg = "OK";
		RackProduct rackProduct = RackProduct.getInstance();
		RackMaterial rackMaterial = RackMaterial.getInstance();
		
		rackProduct.clearData();
		DAO dao = new DAOImpl("com.cncmes.dto.CNCRacksInfo");
		try {
			ArrayList<Object> vos = null;
			if(null!=lineName && !"".equals(lineName)){
				vos = dao.findByCnd(new String[]{"linename"}, new String[]{lineName});
			}else{
				vos = dao.findAll();
			}
			if(null != vos){
				for(int i=0; i<vos.size(); i++){
					CNCRacksInfo vo = (CNCRacksInfo) vos.get(i);
					String mainKey = vo.getLinename() + "_" + vo.getId();
					if(0 == vo.getRacktype()){
						rackMaterial.setData(mainKey, RackItems.ID, vo.getId());
						rackMaterial.setData(mainKey, RackItems.CAPACITY, vo.getCapacity());
						rackMaterial.setData(mainKey, RackItems.LINENAME, vo.getLinename());
						rackMaterial.setData(mainKey, RackItems.IP, vo.getIp());
						rackMaterial.setData(mainKey, RackItems.PORT, vo.getPort());
						rackMaterial.setData(mainKey, RackItems.STATE, DeviceState.SHUTDOWN);
						rackMaterial.setData(mainKey, RackItems.TAGNAME, vo.getTagname());
					}else{
						rackProduct.setData(mainKey, RackItems.ID, vo.getId());
						rackProduct.setData(mainKey, RackItems.CAPACITY, vo.getCapacity());
						rackProduct.setData(mainKey, RackItems.LINENAME, vo.getLinename());
						rackProduct.setData(mainKey, RackItems.IP, vo.getIp());
						rackProduct.setData(mainKey, RackItems.PORT, vo.getPort());
						rackProduct.setData(mainKey, RackItems.STATE, DeviceState.SHUTDOWN);
						rackProduct.setData(mainKey, RackItems.TAGNAME, vo.getTagname());
					}
				}
			}
		} catch (SQLException e) {
			msg = "DataUtils-getRacksInfo("+lineName+") ERR:"+e.getMessage();
			LogUtils.errorLog(msg);
		}
		
		return msg;
	}
	
	/**
	 * load the Scheduler Configure into memory
	 * @return "OK" if no error, else return the failed reason
	 */
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
					sConfig.setData(vo.getIp(), SchedulerItems.PORTTASKUPDATE, vo.getPortTaskUpdate());
					sConfig.setData(vo.getIp(), SchedulerItems.STATE, DeviceState.SHUTDOWN);
				}
			}
		} catch (SQLException e) {
			msg = "DataUtils-getSchedulerConfig ERR:"+e.getMessage();
			LogUtils.errorLog(msg);
		}
		
		return msg;
	}
	
	/**
	 * load CNC model list into memory
	 * @return null if error happens or no CNC model set in the database
	 */
	public static String[] getCNCModels(){
		String[] cncModels = null;
		String models = "";
		
		DAO dao = new DAOImpl("com.cncmes.dto.CNCModels");
		try {
			ArrayList<Object> vos = dao.findAll();
			if(null != vos){
				for(int i=0; i<vos.size(); i++){
					CNCModels vo = (CNCModels) vos.get(i);
					if("".equals(models)){
						models = vo.getCnc_model();
					}else{
						models += "," + vo.getCnc_model();
					}
				}
			}
		} catch (SQLException e) {
			LogUtils.errorLog("DataUtils-getCNCModels ERR:"+e.getMessage());
		}
		
		if("".equals(models)){
			cncModels = new String[]{""};
		}else{
			cncModels = models.split(",");
		}
		return cncModels;
	}
}