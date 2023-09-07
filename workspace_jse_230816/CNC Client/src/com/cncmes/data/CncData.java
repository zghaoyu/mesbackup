package com.cncmes.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.commons.io.FileUtils;

import com.cncmes.base.CNC;
import com.cncmes.base.CncItems;
import com.cncmes.base.DeviceObserver;
import com.cncmes.base.DeviceState;
import com.cncmes.base.DeviceSubject;
import com.cncmes.base.RobotItems;
import com.cncmes.base.RunningData;
import com.cncmes.base.TaskItems;
import com.cncmes.handler.impl.CncClientDataHandler;
import com.cncmes.utils.DataUtils;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.MathUtils;
import com.cncmes.utils.MyFileUtils;
import com.cncmes.utils.MySystemUtils;
import com.cncmes.utils.TimeUtils;

import net.sf.json.JSONObject;

/**
 * Running Data of All CNC
 * @author LI ZI LONG
 *
 */
public final class CncData extends RunningData<CncItems> implements DeviceSubject {
	private final int WKZONEQTY = 6;
	private static TaskData taskData = TaskData.getInstance();
	private static RobotData robotData = RobotData.getInstance();
	private static CncData cncData = new CncData();
	private static ArrayList<DeviceObserver> observers = new ArrayList<DeviceObserver>();
	
	private CncData(){}
	public static CncData getInstance(){
		return cncData;
	}
	
	@Override
	public void registerObserver(DeviceObserver observer) {
		observers.add(observer);
	}

	@Override
	public void deleteObserver(DeviceObserver observer) {
		int i = observers.indexOf(observer);
		if(i >= 0) observers.remove(i);
	}
	
	@Override
	public void notifyObservers(ArrayList<DeviceObserver> observers, String data, boolean threadMode, boolean threadSequential, boolean theLastThread) {
		if(observers.size() > 0){
			for(DeviceObserver observer:observers){
				observer.update(cncData, data, threadMode, threadSequential, theLastThread);
			}
		}
	}
	
	public String getDataForObservers(String machineIP){
		String info = "";
		
		String jsonStr = getCNCInfoJsonStr(machineIP);
		if(!"".equals(jsonStr)){
			JSONObject jsonObj = JSONObject.fromObject(jsonStr);
			info = machineIP + ":" + jsonObj.getString("port");
			info += "," + machineIP;
			info += "," + jsonObj.getString("port");
			info += "," + jsonObj.getString("cncState");
			info += "," + jsonObj.getString("model");
			info += "," + jsonObj.getString("lineName");
			info += "," + jsonObj.getString("expMachineTime");
			info += "," + jsonObj.getString("wkZoneQty");
			info += "," + jsonObj.getString("progress")+"%";
			info += "," + jsonObj.getString("wpZN1");
			info += "," + jsonObj.getString("wpZN2");;
			info += "," + jsonObj.getString("wpZN3");;
			info += "," + jsonObj.getString("wpZN4");;
			info += "," + jsonObj.getString("wpZN5");;
			info += "," + jsonObj.getString("wpZN6");;
			info += "," + jsonObj.getString("wpZN7");;
			info += "," + jsonObj.getString("wpZN8");;
			info += "," + jsonObj.getString("wpZN9");;
			info += "," + jsonObj.getString("wpZN10");;
			info += "," + jsonObj.getString("wpZN11");;
			info += "," + jsonObj.getString("wpZN12");;
			info += "," + jsonObj.getString("wpZN13");;
			info += "," + jsonObj.getString("wpZN14");;
			info += "," + jsonObj.getString("wpIDs");;
			info += "," + jsonObj.getString("wpZones");;
			info += "," + jsonObj.getString("wpStates");;
			info += "," + jsonObj.getString("dataHandlerIP");;
			info += "," + jsonObj.getString("dataHandlerPort");;
			info += "," + MathUtils.MD5Encode(info);
		}
		
		return info;
	}
	
	private String getCNCInfoJsonStr(String ip){
		String jsonStr = "";
		
		LinkedHashMap<CncItems, Object> dt = getData(ip);
		if(null != dt){
			String wpIDs="",wpZones="",wpStates="";
			String wpInfo = cncData.getWorkpiecesInMachine(ip);
			if(!"".equals(wpInfo)){
				String[] inf = wpInfo.split("/");
				wpIDs = inf[0];wpZones = inf[1];wpStates = inf[2];
			}
			
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("wpIDs", wpIDs);
			jsonObj.put("wpZones", wpZones);
			jsonObj.put("wpStates", wpStates);
			jsonObj.put("cncState", ""+getCncState(ip));
			jsonObj.put("port", ""+dt.get(CncItems.PORT));
			jsonObj.put("ip", ip);
			jsonObj.put("model", ""+dt.get(CncItems.MODEL));
			jsonObj.put("lineName", ""+dt.get(CncItems.LINENAME));
			jsonObj.put("expMachineTime", ""+dt.get(CncItems.EXPMACHINETIME));
			jsonObj.put("wkZoneQty", ""+dt.get(CncItems.WKZONEQTY));
			jsonObj.put("progress", ""+(null!=dt.get(CncItems.PROGRESS)?dt.get(CncItems.PROGRESS):0));
			jsonObj.put("wpZN1", (null!=dt.get(CncItems.D_WPID_ZN1)?dt.get(CncItems.D_WPID_ZN1):""));
			jsonObj.put("wpZN2", (null!=dt.get(CncItems.D_WPID_ZN2)?dt.get(CncItems.D_WPID_ZN2):""));
			jsonObj.put("wpZN3", (null!=dt.get(CncItems.D_WPID_ZN3)?dt.get(CncItems.D_WPID_ZN3):""));
			jsonObj.put("wpZN4", (null!=dt.get(CncItems.D_WPID_ZN4)?dt.get(CncItems.D_WPID_ZN4):""));
			jsonObj.put("wpZN5", (null!=dt.get(CncItems.D_WPID_ZN5)?dt.get(CncItems.D_WPID_ZN5):""));
			jsonObj.put("wpZN6", (null!=dt.get(CncItems.D_WPID_ZN6)?dt.get(CncItems.D_WPID_ZN6):""));
			jsonObj.put("wpZN7", (null!=dt.get(CncItems.D_WPID_ZN7)?dt.get(CncItems.D_WPID_ZN7):""));
			jsonObj.put("wpZN8", (null!=dt.get(CncItems.D_WPID_ZN8)?dt.get(CncItems.D_WPID_ZN8):""));
			jsonObj.put("wpZN9", (null!=dt.get(CncItems.D_WPID_ZN9)?dt.get(CncItems.D_WPID_ZN9):""));
			jsonObj.put("wpZN10", (null!=dt.get(CncItems.D_WPID_ZN10)?dt.get(CncItems.D_WPID_ZN10):""));
			jsonObj.put("wpZN11", (null!=dt.get(CncItems.D_WPID_ZN11)?dt.get(CncItems.D_WPID_ZN11):""));
			jsonObj.put("wpZN12", (null!=dt.get(CncItems.D_WPID_ZN12)?dt.get(CncItems.D_WPID_ZN12):""));
			jsonObj.put("wpZN13", (null!=dt.get(CncItems.D_WPID_ZN13)?dt.get(CncItems.D_WPID_ZN13):""));
			jsonObj.put("wpZN14", (null!=dt.get(CncItems.D_WPID_ZN14)?dt.get(CncItems.D_WPID_ZN14):""));
			jsonObj.put("dataHandlerIP", CncClientDataHandler.getIP());
			jsonObj.put("dataHandlerPort", ""+CncClientDataHandler.getPort());
			jsonStr = jsonObj.toString();
		}
		
		return jsonStr;
	}
	
	public void dumpCNCInfo(String ip){
		String path = MySystemUtils.getMemoryDumpPath(ip);
		if(!"".equals(path)){
			String content = getCNCInfoJsonStr(ip);
			if(!"".equals(content)) MyFileUtils.saveToFile(content, path);
		}
	}
	
	public void restoreCNCInfo(String ip, DeviceState restoreState, boolean informObserver, boolean threadMode, boolean threadSequential, boolean theLastThread){
		String path = MySystemUtils.getMemoryDumpPath(ip);
		if(!"".equals(path)){
			if(MyFileUtils.getFilePassedDays(path)<=0.5){
				try {
					String jsonStr = FileUtils.readFileToString(new File(path), "UTF-8");
					JSONObject jsonObj = JSONObject.fromObject(jsonStr);
					DeviceState devState = DataUtils.getDevStateByString(jsonObj.getString("cncState"));
					if(null == restoreState || devState == restoreState){
						setData(ip, CncItems.IP, ip);
						setData(ip, CncItems.PORT, Integer.valueOf(jsonObj.getString("port")));
						setData(ip, CncItems.STATE, devState);
						setData(ip, CncItems.LINENAME, jsonObj.getString("lineName"));
						setData(ip, CncItems.EXPMACHINETIME, jsonObj.getString("expMachineTime"));
						setData(ip, CncItems.WKZONEQTY, Integer.valueOf(jsonObj.getString("wkZoneQty")));
						setData(ip, CncItems.PROGRESS, Integer.valueOf(jsonObj.getString("progress")));
						setData(ip, CncItems.D_WPID_ZN1, jsonObj.getString("wpZN1"));
						setData(ip, CncItems.D_WPID_ZN2, jsonObj.getString("wpZN2"));
						setData(ip, CncItems.D_WPID_ZN3, jsonObj.getString("wpZN3"));
						setData(ip, CncItems.D_WPID_ZN4, jsonObj.getString("wpZN4"));
						setData(ip, CncItems.D_WPID_ZN5, jsonObj.getString("wpZN5"));
						setData(ip, CncItems.D_WPID_ZN6, jsonObj.getString("wpZN6"));
						setData(ip, CncItems.D_WPID_ZN7, jsonObj.getString("wpZN7"));
						setData(ip, CncItems.D_WPID_ZN8, jsonObj.getString("wpZN8"));
						setData(ip, CncItems.D_WPID_ZN9, jsonObj.getString("wpZN9"));
						setData(ip, CncItems.D_WPID_ZN10, jsonObj.getString("wpZN10"));
						setData(ip, CncItems.D_WPID_ZN11, jsonObj.getString("wpZN11"));
						setData(ip, CncItems.D_WPID_ZN12, jsonObj.getString("wpZN12"));
						setData(ip, CncItems.D_WPID_ZN13, jsonObj.getString("wpZN13"));
						setData(ip, CncItems.D_WPID_ZN14, jsonObj.getString("wpZN14"));
						if(informObserver) notifyObservers(observers,getDataForObservers(ip),threadMode,threadSequential,theLastThread);
					}
				} catch (IOException e) {
				}
			}
		}
	}
	
	public int getWorkpieceZoneNo(String ip, String workpieceID){
		int zoneNo = 0;
		int zoneQty =getWorkzoneQTY(ip);
		
		for(int zone=1; zone<zoneQty; zone++){
			if(workpieceID.equals(getWorkpieceID(ip, zone))){
				zoneNo = zone;
				break;
			}
		}
		
		return zoneNo;
	}
	
	/**
	 * @param ip IP address of the CNC
	 * @param devState state of the CNC
	 * @param threadMode whether use thread mode
	 * @param threadSequential whether the thread is executed sequentially
	 * @param theLastThread whether it's the last thread,trigger all threads execution and only useful while threadMode and threadSequential is both true
	 * @param forceUpdate whether to force update
	 */
	public synchronized void setCncState(String ip, DeviceState devState, boolean threadMode,boolean threadSequential,boolean theLastThread, boolean forceUpdate){
		DeviceState oriState = getCncState(ip);
		setData(ip, CncItems.STATE, devState);
		if(DeviceState.ALARMING == devState){
			setData(ip, CncItems.DT_ALARM, TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
			setCncLastState(ip, devState);
		}
		if(oriState != devState || forceUpdate){
			notifyObservers(observers,getDataForObservers(ip),threadMode,threadSequential,theLastThread);
			LogUtils.debugLog("DevState_"+ip+"_", ""+devState);
		}
	}
	
	public synchronized DeviceState getCncState(String ip){
		return ((DeviceState)getData(ip).get(CncItems.STATE));
	}
	
	public synchronized void setWorkpieceID(String ip,int workZoneNo,String workpieceID){
		if(1 == workZoneNo) setData(ip, CncItems.D_WPID_ZN1, workpieceID);
		if(2 == workZoneNo) setData(ip, CncItems.D_WPID_ZN2, workpieceID);
		if(3 == workZoneNo) setData(ip, CncItems.D_WPID_ZN3, workpieceID);
		if(4 == workZoneNo) setData(ip, CncItems.D_WPID_ZN4, workpieceID);
		if(5 == workZoneNo) setData(ip, CncItems.D_WPID_ZN5, workpieceID);
		if(6 == workZoneNo) setData(ip, CncItems.D_WPID_ZN6, workpieceID);
		if(7 == workZoneNo) setData(ip, CncItems.D_WPID_ZN7, workpieceID);
		if(8 == workZoneNo) setData(ip, CncItems.D_WPID_ZN8, workpieceID);
		if(9 == workZoneNo) setData(ip, CncItems.D_WPID_ZN9, workpieceID);
		if(10 == workZoneNo) setData(ip, CncItems.D_WPID_ZN10, workpieceID);
		if(11 == workZoneNo) setData(ip, CncItems.D_WPID_ZN11, workpieceID);
		if(12 == workZoneNo) setData(ip, CncItems.D_WPID_ZN12, workpieceID);
		if(13 == workZoneNo) setData(ip, CncItems.D_WPID_ZN13, workpieceID);
		if(14 == workZoneNo) setData(ip, CncItems.D_WPID_ZN14, workpieceID);
		if(!"".equals(workpieceID)){
			String wpIDs = (String) getItemVal(ip,CncItems.WPIDS);
			String wpZones = (String) getItemVal(ip,CncItems.WPZONES);
			if(null==wpIDs || "".equals(wpIDs)){
				setData(ip, CncItems.WPIDS, workpieceID);
				setData(ip, CncItems.WPZONES, ""+workZoneNo);
			}else{
				setData(ip, CncItems.WPIDS, wpIDs+";"+workpieceID);
				setData(ip, CncItems.WPZONES, wpZones+";"+workZoneNo);
			}
		}
	}
	
	public synchronized String getWorkpieceID(String ip,int workZoneNo){
		String workpieceID = "";
		
		LinkedHashMap<CncItems,Object> dtMap = dataMap.get(ip);
		if(null != dtMap){
			Object id = null;
			if(1 == workZoneNo) id = dtMap.get(CncItems.D_WPID_ZN1);
			if(2 == workZoneNo) id = dtMap.get(CncItems.D_WPID_ZN2);
			if(3 == workZoneNo) id = dtMap.get(CncItems.D_WPID_ZN3);
			if(4 == workZoneNo) id = dtMap.get(CncItems.D_WPID_ZN4);
			if(5 == workZoneNo) id = dtMap.get(CncItems.D_WPID_ZN5);
			if(6 == workZoneNo) id = dtMap.get(CncItems.D_WPID_ZN6);
			if(7 == workZoneNo) id = dtMap.get(CncItems.D_WPID_ZN7);
			if(8 == workZoneNo) id = dtMap.get(CncItems.D_WPID_ZN8);
			if(9 == workZoneNo) id = dtMap.get(CncItems.D_WPID_ZN9);
			if(10 == workZoneNo) id = dtMap.get(CncItems.D_WPID_ZN10);
			if(11 == workZoneNo) id = dtMap.get(CncItems.D_WPID_ZN11);
			if(12 == workZoneNo) id = dtMap.get(CncItems.D_WPID_ZN12);
			if(13 == workZoneNo) id = dtMap.get(CncItems.D_WPID_ZN13);
			if(14 == workZoneNo) id = dtMap.get(CncItems.D_WPID_ZN14);
			if(null != id) workpieceID = (String)id;
		}
		
		return workpieceID;
	}
	
	public synchronized LinkedHashMap<Integer,String> getWorkpieceIDs(String ip){
		LinkedHashMap<Integer,String> ids = new LinkedHashMap<Integer,String>();
		
		String id = "";
		int workzoneQty = getWorkzoneQTY(ip);
		for(int i=1; i<=workzoneQty; i++){
			id = getWorkpieceID(ip, i);
			if(!"".equals(id)) ids.put(i, id);
		}
		
		return ids;
	}
	
	public DeviceState getCncLastState(String ip){
		DeviceState state = DeviceState.SHUTDOWN;
		
		LinkedHashMap<CncItems,Object> dtMap = dataMap.get(ip);
		if(null != dtMap){
			if(null != dtMap.get(CncItems.LASTSTATE)) state = (DeviceState) dtMap.get(CncItems.LASTSTATE);
		}
		
		return state;
	}
	
	public void setCncLastState(String ip, DeviceState state){
		setData(ip, CncItems.LASTSTATE, state);
		if(DeviceState.FINISH == state){
			setData(ip, CncItems.FINISHSHOW, getCncDataString(ip));
		}
	}
	
	public String getWorkpiecesInMachine(String ip){
		LinkedHashMap<Integer,String> ids = getWorkpieceIDs(ip);
		String wpIDs="",wpSlotIDs="",wpStates="",wps = "";
		
		if(!ids.isEmpty()){
			WorkpieceData wpData = WorkpieceData.getInstance();
			for(int workzone:ids.keySet()){
				if("".equals(wpIDs)){
					wpIDs = ids.get(workzone);
					wpSlotIDs = "" + workzone;
					wpStates = "" + wpData.getWorkpieceState(ids.get(workzone));
				}else{
					wpIDs += ";" + ids.get(workzone);
					wpSlotIDs += ";" + workzone;
					wpStates += ";" + wpData.getWorkpieceState(ids.get(workzone));
				}
			}
		}
		if(!"".equals(wpIDs)) wps = wpIDs+"/"+wpSlotIDs+"/"+wpStates;
		
		return wps;
	}
	
	public void clearWorkpieceID(String ip){
		for(int i=1; i<=WKZONEQTY; i++){
			setWorkpieceID(ip, i, "");
		}
	}
	
	public int getWorkpieceQTY(String ip){
		LinkedHashMap<Integer,String> ids = getWorkpieceIDs(ip);
		return ids.size();
	}
	
	public int getWorkzoneQTY(String ip){
		int qty = 0;
		
		LinkedHashMap<CncItems,Object> dtMap = dataMap.get(ip);
		if(null != dtMap) qty = (int) dtMap.get(CncItems.WKZONEQTY);
		if(qty > WKZONEQTY) qty = WKZONEQTY;
		
		return qty;
	}
	
	public String[] getEmptyWorkzones(String ip){
		String[] workzones = null;
		String zones = "";
		
		int workzoneQty = getWorkzoneQTY(ip);
		for(int i=1; i<=workzoneQty; i++){
			if("".equals(getWorkpieceID(ip,i))){
				if("".equals(zones)){
					zones = "" + i;
				}else{
					zones += "," + i;
				}
			}
		}
		if(!"".equals(zones)) workzones = zones.split(",");
		
		return workzones;
	}
	
	public String getNotEmptyWorkzones(String ip){
		String zones = "";
		
		int workzoneQty = getWorkzoneQTY(ip);
		for(int i=1; i<=workzoneQty; i++){
			if(!"".equals(getWorkpieceID(ip,i))){
				if("".equals(zones)){
					zones = "" + i;
				}else{
					zones += ";" + i;
				}
			}
		}
		
		return zones;
	}
	
	public int getWorkingCNCQty(String lineName){
		int qty = 0;
		
		if(null != lineName){
			LinkedHashMap<CncItems, Object> dt = null;
			for(String cncIP:dataMap.keySet()){
				dt = dataMap.get(cncIP);
				if(lineName.equals(dt.get(CncItems.LINENAME)+"")){
					if(DeviceState.WORKING == (DeviceState)dt.get(CncItems.STATE)){
						qty++;
					}
				}
			}
		}
		
		return qty;
	}
	
	public String[] getAlarmingCNCs(String lineName){
		String[] cncs = null;
		String temp = "";
		int index = 0;
		
		if(null != lineName){
			LinkedHashMap<CncItems, Object> dt = null;
			for(String cncIP:dataMap.keySet()){
				dt = dataMap.get(cncIP);
				if(lineName.equals(dt.get(CncItems.LINENAME)+"")){
					if(DeviceState.ALARMING == (DeviceState)dt.get(CncItems.STATE)){
						index++;
						if("".equals(temp)){
							temp = index + ":" + cncIP;
						}else{
							temp += "," + index + ":" + cncIP;
						}
					}
				}
			}
		}
		if(!"".equals(temp)) cncs = temp.split(",");
		
		return cncs;
	}
	
	public String getFtpIP(String ip){
		String ftpip = "";
		
		LinkedHashMap<CncItems,Object> dtMap = dataMap.get(ip);
		if(null != dtMap) ftpip = (String) dtMap.get(CncItems.FTPIP);
		
		return ftpip;
	}
	
	public String getCncModel(String ip){
		String model = "";
		
		LinkedHashMap<CncItems,Object> dtMap = dataMap.get(ip);
		if(null != dtMap) model = (String) dtMap.get(CncItems.MODEL);
		
		return model;
	}
	
	public CNC getCncController(String ip){
		return (CNC) dataMap.get(ip).get(CncItems.CTRL);
	}
	
	public String getLineName(String ip){
		String lineName = "";
		
		LinkedHashMap<CncItems,Object> dtMap = dataMap.get(ip);
		if(null != dtMap) lineName = (String) dtMap.get(CncItems.LINENAME);
		
		return lineName;
	}
	
	public void setCNCTimingData(String ip, CncItems timeItem, long t){
		setData(ip, timeItem, t);
	}
	
	public void setStartMachiningTime(String ip,long startT){
		setData(ip, CncItems.DT_MACHINING_START, startT);
		setData(ip, CncItems.DT_MACHINING_ZNS, getNotEmptyWorkzones(ip));
	}
	
	public long getStartMachiningTime(String ip){
		long t = 0;
		
		LinkedHashMap<CncItems,Object> dtMap = getData(ip);
		if(null != dtMap){
			Object mt = dtMap.get(CncItems.DT_MACHINING_START);
			if(null != mt) t = (long) mt;
		}
		
		return t;
	}
	
	public void setFinishMachiningTime(String ip,long finishT){
		setData(ip, CncItems.DT_MACHINING_FINISH, finishT);
		updateZoneStartTimeData(ip, finishT);
	}
	
	public long getFinishMachiningTime(String ip){
		long t = 0;
		
		LinkedHashMap<CncItems,Object> dtMap = getData(ip);
		if(null != dtMap){
			Object mt = dtMap.get(CncItems.DT_MACHINING_FINISH);
			if(null != mt) t = (long) mt;
		}
		
		return t;
	}
	
	public void setExpectedMachiningTime(String ip,long expectedT){
		setData(ip, CncItems.EXPMACHINETIME, expectedT);
	}
	
	public long getExpectedMachiningTime(String ip){
		long simulationT = 0;
		
		LinkedHashMap<Integer,String> workpieceIDs = getWorkpieceIDs(ip);
		if(workpieceIDs.size() > 0){
			for(String id:workpieceIDs.values()){
				simulationT += WorkpieceData.getInstance().getNextProcSimtime(id, getCncModel(ip), null);
			}
		}
		
		return simulationT;
	}
	
	public long getZoneStartTime(String ip, int zoneNo){
		long startT = 0;
		if(zoneNo > 0 && zoneNo <= WKZONEQTY){
			LinkedHashMap<CncItems,Object> dt = getData(ip);
			if(null != dt){
				if(1 == zoneNo) startT = (long) dt.get(CncItems.DT_START_ZN1);
				if(2 == zoneNo) startT = (long) dt.get(CncItems.DT_START_ZN2);
				if(3 == zoneNo) startT = (long) dt.get(CncItems.DT_START_ZN3);
				if(4 == zoneNo) startT = (long) dt.get(CncItems.DT_START_ZN4);
				if(5 == zoneNo) startT = (long) dt.get(CncItems.DT_START_ZN5);
				if(6 == zoneNo) startT = (long) dt.get(CncItems.DT_START_ZN6);
				if(7 == zoneNo) startT = (long) dt.get(CncItems.DT_START_ZN7);
				if(8 == zoneNo) startT = (long) dt.get(CncItems.DT_START_ZN8);
				if(9 == zoneNo) startT = (long) dt.get(CncItems.DT_START_ZN9);
				if(10 == zoneNo) startT = (long) dt.get(CncItems.DT_START_ZN10);
				if(11 == zoneNo) startT = (long) dt.get(CncItems.DT_START_ZN11);
				if(12 == zoneNo) startT = (long) dt.get(CncItems.DT_START_ZN12);
				if(13 == zoneNo) startT = (long) dt.get(CncItems.DT_START_ZN13);
				if(14 == zoneNo) startT = (long) dt.get(CncItems.DT_START_ZN14);
			}
		}
		return startT;
	}
	
	public void setZoneStartTime(String ip, int zoneNo, long startT){
		if(zoneNo > 0 && zoneNo <= WKZONEQTY){
			if(1 == zoneNo) setData(ip, CncItems.DT_START_ZN1, startT);
			if(2 == zoneNo) setData(ip, CncItems.DT_START_ZN2, startT);
			if(3 == zoneNo) setData(ip, CncItems.DT_START_ZN3, startT);
			if(4 == zoneNo) setData(ip, CncItems.DT_START_ZN4, startT);
			if(5 == zoneNo) setData(ip, CncItems.DT_START_ZN5, startT);
			if(6 == zoneNo) setData(ip, CncItems.DT_START_ZN6, startT);
			if(7 == zoneNo) setData(ip, CncItems.DT_START_ZN7, startT);
			if(8 == zoneNo) setData(ip, CncItems.DT_START_ZN8, startT);
			if(9 == zoneNo) setData(ip, CncItems.DT_START_ZN9, startT);
			if(10 == zoneNo) setData(ip, CncItems.DT_START_ZN10, startT);
			if(11 == zoneNo) setData(ip, CncItems.DT_START_ZN11, startT);
			if(12 == zoneNo) setData(ip, CncItems.DT_START_ZN12, startT);
			if(13 == zoneNo) setData(ip, CncItems.DT_START_ZN13, startT);
			if(14 == zoneNo) setData(ip, CncItems.DT_START_ZN14, startT);
			
			if(startT > 0){
				String[] zones = (""+getData(ip).get(CncItems.DT_MACHINING_ZNS)).split(";");
				int finishZNs = 0;
				for(int i=0; i<zones.length; i++){
					if(zoneNo > Integer.valueOf(zones[i])){
						finishZNs++;
					}else{
						break;
					}
				}
				setData(ip, CncItems.DT_FINISH_ZNS_QTY, finishZNs);
			}
		}
	}
	
	public long getZoneSimulationTime(String ip, int zoneNo){
		long startT = 0;
		if(zoneNo > 0 && zoneNo <= WKZONEQTY){
			LinkedHashMap<CncItems,Object> dt = getData(ip);
			if(null != dt){
				if(1 == zoneNo) startT = (long) dt.get(CncItems.DT_SIMT_ZN1);
				if(2 == zoneNo) startT = (long) dt.get(CncItems.DT_SIMT_ZN2);
				if(3 == zoneNo) startT = (long) dt.get(CncItems.DT_SIMT_ZN3);
				if(4 == zoneNo) startT = (long) dt.get(CncItems.DT_SIMT_ZN4);
				if(5 == zoneNo) startT = (long) dt.get(CncItems.DT_SIMT_ZN5);
				if(6 == zoneNo) startT = (long) dt.get(CncItems.DT_SIMT_ZN6);
				if(7 == zoneNo) startT = (long) dt.get(CncItems.DT_SIMT_ZN7);
				if(8 == zoneNo) startT = (long) dt.get(CncItems.DT_SIMT_ZN8);
				if(9 == zoneNo) startT = (long) dt.get(CncItems.DT_SIMT_ZN9);
				if(10 == zoneNo) startT = (long) dt.get(CncItems.DT_SIMT_ZN10);
				if(11 == zoneNo) startT = (long) dt.get(CncItems.DT_SIMT_ZN11);
				if(12 == zoneNo) startT = (long) dt.get(CncItems.DT_SIMT_ZN12);
				if(13 == zoneNo) startT = (long) dt.get(CncItems.DT_SIMT_ZN13);
				if(14 == zoneNo) startT = (long) dt.get(CncItems.DT_SIMT_ZN14);
			}
		}
		return startT;
	}
	
	public void setZoneSimulationTime(String ip, int zoneNo, long simulationT){
		if(zoneNo > 0 && zoneNo <= WKZONEQTY){
			if(1 == zoneNo) setData(ip, CncItems.DT_SIMT_ZN1, simulationT);
			if(2 == zoneNo) setData(ip, CncItems.DT_SIMT_ZN2, simulationT);
			if(3 == zoneNo) setData(ip, CncItems.DT_SIMT_ZN3, simulationT);
			if(4 == zoneNo) setData(ip, CncItems.DT_SIMT_ZN4, simulationT);
			if(5 == zoneNo) setData(ip, CncItems.DT_SIMT_ZN5, simulationT);
			if(6 == zoneNo) setData(ip, CncItems.DT_SIMT_ZN6, simulationT);
			if(7 == zoneNo) setData(ip, CncItems.DT_SIMT_ZN7, simulationT);
			if(8 == zoneNo) setData(ip, CncItems.DT_SIMT_ZN8, simulationT);
			if(9 == zoneNo) setData(ip, CncItems.DT_SIMT_ZN9, simulationT);
			if(10 == zoneNo) setData(ip, CncItems.DT_SIMT_ZN10, simulationT);
			if(11 == zoneNo) setData(ip, CncItems.DT_SIMT_ZN11, simulationT);
			if(12 == zoneNo) setData(ip, CncItems.DT_SIMT_ZN12, simulationT);
			if(13 == zoneNo) setData(ip, CncItems.DT_SIMT_ZN13, simulationT);
			if(14 == zoneNo) setData(ip, CncItems.DT_SIMT_ZN14, simulationT);
		}
	}
	
	public void setNCProgram(String ip, int zoneNo, String ncProgram){
		if(zoneNo > 0 && zoneNo <= WKZONEQTY){
			if(1 == zoneNo) setData(ip, CncItems.D_NCPROG_ZN1, ncProgram);
			if(2 == zoneNo) setData(ip, CncItems.D_NCPROG_ZN2, ncProgram);
			if(3 == zoneNo) setData(ip, CncItems.D_NCPROG_ZN3, ncProgram);
			if(4 == zoneNo) setData(ip, CncItems.D_NCPROG_ZN4, ncProgram);
			if(5 == zoneNo) setData(ip, CncItems.D_NCPROG_ZN5, ncProgram);
			if(6 == zoneNo) setData(ip, CncItems.D_NCPROG_ZN6, ncProgram);
			if(7 == zoneNo) setData(ip, CncItems.D_NCPROG_ZN7, ncProgram);
			if(8 == zoneNo) setData(ip, CncItems.D_NCPROG_ZN8, ncProgram);
			if(9 == zoneNo) setData(ip, CncItems.D_NCPROG_ZN9, ncProgram);
			if(10 == zoneNo) setData(ip, CncItems.D_NCPROG_ZN10, ncProgram);
			if(11 == zoneNo) setData(ip, CncItems.D_NCPROG_ZN11, ncProgram);
			if(12 == zoneNo) setData(ip, CncItems.D_NCPROG_ZN12, ncProgram);
			if(13 == zoneNo) setData(ip, CncItems.D_NCPROG_ZN13, ncProgram);
			if(14 == zoneNo) setData(ip, CncItems.D_NCPROG_ZN14, ncProgram);
		}
	}
	
	public void setSpecNo(String ip, int zoneNo, long specNo){
		if(zoneNo > 0 && zoneNo <= WKZONEQTY){
			if(1 == zoneNo) setData(ip, CncItems.D_SPECNO_ZN1, specNo);
			if(2 == zoneNo) setData(ip, CncItems.D_SPECNO_ZN2, specNo);
			if(3 == zoneNo) setData(ip, CncItems.D_SPECNO_ZN3, specNo);
			if(4 == zoneNo) setData(ip, CncItems.D_SPECNO_ZN4, specNo);
			if(5 == zoneNo) setData(ip, CncItems.D_SPECNO_ZN5, specNo);
			if(6 == zoneNo) setData(ip, CncItems.D_SPECNO_ZN6, specNo);
			if(7 == zoneNo) setData(ip, CncItems.D_SPECNO_ZN7, specNo);
			if(8 == zoneNo) setData(ip, CncItems.D_SPECNO_ZN8, specNo);
			if(9 == zoneNo) setData(ip, CncItems.D_SPECNO_ZN9, specNo);
			if(10 == zoneNo) setData(ip, CncItems.D_SPECNO_ZN10, specNo);
			if(11 == zoneNo) setData(ip, CncItems.D_SPECNO_ZN11, specNo);
			if(12 == zoneNo) setData(ip, CncItems.D_SPECNO_ZN12, specNo);
			if(13 == zoneNo) setData(ip, CncItems.D_SPECNO_ZN13, specNo);
			if(14 == zoneNo) setData(ip, CncItems.D_SPECNO_ZN14, specNo);
		}
	}
	
	public boolean subProgramOK(String cncIP){
		boolean ok = false;
		
		String program = (String) getData(cncIP).get(CncItems.SUBPROGRAMS);
		if(null != program && !"".equals(program) && program.indexOf("NULL") < 0) ok = true;
		
		if(!ok) LogUtils.errorLog("CNC "+cncIP+" sub programs NG:"+program);
		return ok;
	}
	
	public boolean mainProgramOK(String cncIP){
		boolean ok = false;
		
		String program = (String) getData(cncIP).get(CncItems.MAINPROGRAM);
		if(null != program && !"".equals(program)) ok = true;
		
		if(!ok) LogUtils.errorLog("CNC "+cncIP+" main program NG:"+program);
		return ok;
	}
	
	public void clearMachiningData(String ip){
		setData(ip, CncItems.DT_DATE,"");
		setData(ip, CncItems.DT_ALARM,"");
		setData(ip, CncItems.DT_READYLOADING,0L);
		setData(ip, CncItems.DT_MACHINING_START,0L);
		setData(ip, CncItems.DT_MACHINING_FINISH,0L);
		setData(ip, CncItems.DT_FINISHUNLOADING,0L);
		
		setData(ip, CncItems.DT_CLEANING_START1,0L);
		setData(ip, CncItems.DT_CLEANING_START2,0L);
		setData(ip, CncItems.DT_CLEANING_STOP1,0L);
		setData(ip, CncItems.DT_CLEANING_STOP2,0L);
		
		setData(ip, CncItems.DT_START_ZN1,0L);
		setData(ip, CncItems.DT_START_ZN2,0L);
		setData(ip, CncItems.DT_START_ZN3,0L);
		setData(ip, CncItems.DT_START_ZN4,0L);
		setData(ip, CncItems.DT_START_ZN5,0L);
		setData(ip, CncItems.DT_START_ZN6,0L);
		
		setData(ip, CncItems.DT_SIMT_ZN1,0L);
		setData(ip, CncItems.DT_SIMT_ZN2,0L);
		setData(ip, CncItems.DT_SIMT_ZN3,0L);
		setData(ip, CncItems.DT_SIMT_ZN4,0L);
		setData(ip, CncItems.DT_SIMT_ZN5,0L);
		setData(ip, CncItems.DT_SIMT_ZN6,0L);
		
		setData(ip, CncItems.D_NCPROG_ZN1,"");
		setData(ip, CncItems.D_NCPROG_ZN2,"");
		setData(ip, CncItems.D_NCPROG_ZN3,"");
		setData(ip, CncItems.D_NCPROG_ZN4,"");
		setData(ip, CncItems.D_NCPROG_ZN5,"");
		setData(ip, CncItems.D_NCPROG_ZN6,"");
		
		setData(ip, CncItems.D_ROBOTL,"");
		setData(ip, CncItems.D_ROBOTPOSL,"");
		setData(ip, CncItems.D_ROBOTUL,"");
		setData(ip, CncItems.D_ROBOTPOSUL,"");
		
		setData(ip, CncItems.D_SPECNO_ZN1,0L);
		setData(ip, CncItems.D_SPECNO_ZN2,0L);
		setData(ip, CncItems.D_SPECNO_ZN3,0L);
		setData(ip, CncItems.D_SPECNO_ZN4,0L);
		setData(ip, CncItems.D_SPECNO_ZN5,0L);
		setData(ip, CncItems.D_SPECNO_ZN6,0L);
		
		setData(ip, CncItems.D_WPID_ZN1,"");
		setData(ip, CncItems.D_WPID_ZN2,"");
		setData(ip, CncItems.D_WPID_ZN3,"");
		setData(ip, CncItems.D_WPID_ZN4,"");
		setData(ip, CncItems.D_WPID_ZN5,"");
		setData(ip, CncItems.D_WPID_ZN6,"");
		
		setData(ip, CncItems.D_TASKID,"");
		setData(ip, CncItems.D_ERR,"");
		setData(ip, CncItems.DT_MACHINING_ZNS, "");
		setData(ip, CncItems.DT_FINISH_ZNS_QTY, 0);
		
		setData(ip, CncItems.WPIDS,"");
		setData(ip, CncItems.WPZONES,"");
		setData(ip, CncItems.WPSTATES,"");
		setData(ip, CncItems.FINISHSHOW,"");
		setData(ip, CncItems.PROGRESS, 0);
	}
	
	public void saveMachiningData(String ip){
		String logFile = "MachiningData_" + TimeUtils.getCurrentDate("yyyyMMdd") + ".log";
		Object valObj = null;
		JSONObject jsonObj = new JSONObject();
		
		valObj = getItemVal(ip, CncItems.DT_DATE);
		jsonObj.put("DateTime", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.STATE);
		jsonObj.put("State", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.MODEL);
		jsonObj.put("MachineModel", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.IP);
		jsonObj.put("MachineIP", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.PORT);
		jsonObj.put("MachinePort", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.DT_ALARM);
		jsonObj.put("AlarmT(ms)", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.DT_READYLOADING);
		jsonObj.put("ReadyT(ms)", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.DT_MACHINING_START);
		jsonObj.put("StartMachiningT(ms)", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.DT_CLEANING_START1);
		jsonObj.put("StartCleaningT1(ms)", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.DT_CLEANING_STOP1);
		jsonObj.put("StopCleaningT1(ms)", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.DT_MACHINING_FINISH);
		jsonObj.put("FinishMachiningT(ms)", null!=valObj?valObj:"");
		if(null != valObj && (long)valObj > 0) jsonObj.put("State", DeviceState.FINISH);
		
		valObj = getItemVal(ip, CncItems.DT_FINISHUNLOADING);
		jsonObj.put("FinishULT(ms)", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.DT_CLEANING_START2);
		jsonObj.put("StartCleaningT2(ms)", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.DT_CLEANING_STOP2);
		jsonObj.put("StopCleaningT2(ms)", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.DT_START_ZN1);
		jsonObj.put("StartT_ZN1(ms)", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.DT_START_ZN2);
		jsonObj.put("StartT_ZN2(ms)", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.DT_START_ZN3);
		jsonObj.put("StartT_ZN3(ms)", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.DT_START_ZN4);
		jsonObj.put("StartT_ZN4(ms)", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.DT_START_ZN5);
		jsonObj.put("StartT_ZN5(ms)", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.DT_START_ZN6);
		jsonObj.put("StartT_ZN6(ms)", null!=valObj?valObj:"");
		
		jsonObj.put("WPID_ZN1", "");
		jsonObj.put("WPID_ZN2", "");
		jsonObj.put("WPID_ZN3", "");
		jsonObj.put("WPID_ZN4", "");
		jsonObj.put("WPID_ZN5", "");
		jsonObj.put("WPID_ZN6", "");
		
		String wpIDs = (String) getItemVal(ip, CncItems.WPIDS);
		String wpZones = (String) getItemVal(ip, CncItems.WPZONES);
		if(null != wpIDs && !"".equals(wpIDs)){
			String[] ids = wpIDs.split(";");
			String[] zones = wpZones.split(";");
			for(int i=0; i<ids.length; i++){
				if(1 == Integer.parseInt(zones[i])) jsonObj.put("WPID_ZN1", ids[i]);
				if(2 == Integer.parseInt(zones[i])) jsonObj.put("WPID_ZN2", ids[i]);
				if(3 == Integer.parseInt(zones[i])) jsonObj.put("WPID_ZN3", ids[i]);
				if(4 == Integer.parseInt(zones[i])) jsonObj.put("WPID_ZN4", ids[i]);
				if(5 == Integer.parseInt(zones[i])) jsonObj.put("WPID_ZN5", ids[i]);
				if(6 == Integer.parseInt(zones[i])) jsonObj.put("WPID_ZN6", ids[i]);
				if(7 == Integer.parseInt(zones[i])) jsonObj.put("WPID_ZN7", ids[i]);
				if(8 == Integer.parseInt(zones[i])) jsonObj.put("WPID_ZN8", ids[i]);
				if(9 == Integer.parseInt(zones[i])) jsonObj.put("WPID_ZN9", ids[i]);
				if(10 == Integer.parseInt(zones[i])) jsonObj.put("WPID_ZN10", ids[i]);
				if(11 == Integer.parseInt(zones[i])) jsonObj.put("WPID_ZN11", ids[i]);
				if(12 == Integer.parseInt(zones[i])) jsonObj.put("WPID_ZN12", ids[i]);
				if(13 == Integer.parseInt(zones[i])) jsonObj.put("WPID_ZN13", ids[i]);
				if(14 == Integer.parseInt(zones[i])) jsonObj.put("WPID_ZN14", ids[i]);
			}
		}
		
		valObj = getItemVal(ip, CncItems.D_NCPROG_ZN1);
		jsonObj.put("NCProg_ZN1", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.D_NCPROG_ZN2);
		jsonObj.put("NCProg_ZN2", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.D_NCPROG_ZN3);
		jsonObj.put("NCProg_ZN3", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.D_NCPROG_ZN4);
		jsonObj.put("NCProg_ZN4", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.D_NCPROG_ZN5);
		jsonObj.put("NCProg_ZN5", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.D_NCPROG_ZN6);
		jsonObj.put("NCProg_ZN6", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.D_SPECNO_ZN1);
		jsonObj.put("SpecNo_ZN1", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.D_SPECNO_ZN2);
		jsonObj.put("SpecNo_ZN2", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.D_SPECNO_ZN3);
		jsonObj.put("SpecNo_ZN3", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.D_SPECNO_ZN4);
		jsonObj.put("SpecNo_ZN4", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.D_SPECNO_ZN5);
		jsonObj.put("SpecNo_ZN5", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.D_SPECNO_ZN6);
		jsonObj.put("SpecNo_ZN6", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.D_TASKID);
		jsonObj.put("TaskID", null!=valObj?valObj:"");
		
		valObj = getItemVal(ip, CncItems.D_ERR);
		jsonObj.put("Error", null!=valObj?valObj:"");
		
		String title = "", data = "", key = "", val = "";
		@SuppressWarnings("unchecked")
		Iterator<String> iterData = jsonObj.keys();
		while(iterData.hasNext()){
			key = iterData.next();
			val = ""+jsonObj.get(key);
			if("".equals(title)){
				title = key;
				data = val;
			}else{
				title += "," + key;
				data += "," + val;
			}
		}
		if(!"".equals(title)) LogUtils.machiningDataLog(logFile, title+"\r\n"+data);
	}
	
	public String getCncDataString(String ip){
		String data = "", zones = "", taskID = "", robotIP = "", robotCmd = "";
		String[] zoneNo = null;
		long[] zoneStartT = null, zoneSimulationT = null, zoneSimFinishT = null;
		long startT = 0, finishT = 0, currT = 0, mtime = 0, finishZNs = 0;
		float progress = 0f, unitT = 10f, tmp1 = 0f, tmp2 = 0f;
		boolean bNoZoneStart = true;
		
		LinkedHashMap<CncItems,Object> dtMap = dataMap.get(ip);
		if(null != dtMap){
			zones = (String) dtMap.get(CncItems.DT_MACHINING_ZNS);
			startT = getStartMachiningTime(ip);
			finishT = getFinishMachiningTime(ip);
			currT = System.currentTimeMillis();
			taskID = (String) dtMap.get(CncItems.D_TASKID);
			robotIP = ""; robotCmd = "";
			if(null != taskID && !"".equals(taskID)){
				try {
					robotIP = (String) taskData.getData(taskID).get(TaskItems.ROBOTIP);
					robotCmd = (String) robotData.getData(robotIP).get(RobotItems.CMD);
				} catch (Exception e) {
					robotIP = "";
					robotCmd = "";
				}
			}
			
			DeviceState state = (DeviceState) dtMap.get(CncItems.STATE);
			data += "status: " + state + "\r\n";
			data += "cmd: " + (""+dtMap.get(CncItems.COMMAND)).split(",")[0] + "\r\n";
			data += "robot: " + robotCmd + "\r\n";
			if(startT > 0){
				if(finishT > 0){
					mtime = finishT - startT;
				}else{
					mtime = currT - startT;
				}
			}
			data += "machineT: " + showMachiningTime(mtime) + "\r\n";
			
			if(null != zones && !"".equals(zones)){
				zoneNo = zones.split(";");
				data += "zoneQty: " + zoneNo.length + "\r\n";
				zoneStartT = new long[zoneNo.length];
				zoneSimulationT = new long[zoneNo.length];
				zoneSimFinishT = new long[zoneNo.length];
				for(int i=0; i<zoneNo.length; i++){
					zoneStartT[i] = getZoneStartTime(ip, Integer.valueOf(zoneNo[i]));
					if(zoneStartT[i] > 0) bNoZoneStart = false;
					zoneSimulationT[i] = getZoneSimulationTime(ip, Integer.valueOf(zoneNo[i]));
					zoneSimFinishT[i] = 0;
					for(int j=i; j>=0; j--){
						zoneSimFinishT[i] += zoneSimulationT[j];
					}
				}
				
				//Calculate zone progress
				finishZNs = 0;
				for(int i=0; i<zoneNo.length; i++){
					if(finishT > 0){
						data += "zone_"+zoneNo[i]+": 100%\r\n";
					}else{
						if(bNoZoneStart){
							if(startT > 0 && mtime > (zoneSimFinishT[i] * (long)unitT * 100)){
								if(i == (zoneNo.length-1)){
									data += "zone_"+zoneNo[i]+": 99%\r\n";
								}else{
									data += "zone_"+zoneNo[i]+": 100%\r\n";
									finishZNs++;
								}
							}else{
								data += "zone_"+zoneNo[i]+": 0%\r\n";
							}
						}else{
							if(zoneStartT[i] > 0){
								if((i+1) < zoneNo.length){
									if(zoneStartT[i+1] > 0){
										data += "zone_"+zoneNo[i]+": 100%\r\n";
									}else{
										progress = (currT - zoneStartT[i])/unitT/zoneSimulationT[i];
										if(progress > 100) progress = 99f;
										data += "zone_"+zoneNo[i]+": "+MathUtils.roundFloat(progress, 2)+"%\r\n";
									}
								}else{
									if(DeviceState.FINISH == getCncLastState(ip)){
										data += "zone_"+zoneNo[i]+": 100%\r\n";
									}else{
										progress = (currT - zoneStartT[i])/unitT/zoneSimulationT[i];
										if(progress > 100) progress = 99f;
										data += "zone_"+zoneNo[i]+": "+MathUtils.roundFloat(progress, 2)+"%\r\n";
									}
								}
							}else{
								data += "zone_"+zoneNo[i]+": 0%\r\n";
							}
						}
					}
				}
				
				//Update machining progress
				if(finishT > 0){
					setMachiningProgress(ip, 100);
				}else{
					if(!bNoZoneStart) finishZNs = Integer.valueOf(""+dtMap.get(CncItems.DT_FINISH_ZNS_QTY));
					int maxIndex = zoneNo.length -1;
					if(finishZNs > 0){
						if(bNoZoneStart){
							tmp1 = mtime / unitT / zoneSimFinishT[maxIndex];
							tmp2 = (zoneNo.length>finishZNs?(finishZNs+1):finishZNs) * 100f / zoneNo.length;
							progress = (tmp1 > tmp2)?tmp2:tmp1;
						}else{
							progress = finishZNs * 100f / zoneNo.length;
							for(int i=maxIndex; i>=0; i--){
								if(zoneStartT[i] > 0){
									tmp1 = (currT - zoneStartT[i])/unitT/zoneSimFinishT[maxIndex];
									tmp2 = zoneSimulationT[i] * 100f / zoneSimFinishT[maxIndex];
									progress += (tmp1>tmp2)?tmp2:tmp1;
									break;
								}
							}
						}
					}else{
						progress = (currT - startT) / unitT / zoneSimFinishT[maxIndex];
						tmp2 = zoneSimulationT[0] * 100f / zoneSimFinishT[maxIndex];
						if(progress > tmp2) progress = tmp2;
					}
					
					if(progress > 100){
						setMachiningProgress(ip, 99);
					}else{
						setMachiningProgress(ip, (100<=(int)progress)?99:(int)progress);
					}
				}
			}
			
			if(!"".equals(""+dtMap.get(CncItems.PARAS))){
				data += dtMap.get(CncItems.PARAS)+"\r\n";
			}
			if(DeviceState.ALARMING == state){
				if("".equals(""+dtMap.get(CncItems.ALARMMSG))){
					data += "error: " + dtMap.get(CncItems.D_ERR);
				}else{
					data += dtMap.get(CncItems.ALARMMSG);
				}
			}
		}
		return data;
	}
	
	public int getMachiningProgress(String ip){
		int progress = 0;
		
		LinkedHashMap<CncItems,Object> dtMap = dataMap.get(ip);
		if(null != dtMap){
			if(null != dtMap.get(CncItems.PROGRESS)){
				progress = (int)dtMap.get(CncItems.PROGRESS);
			}
		}
		
		return progress;
	}
	
	public void setMachiningProgress(String ip, int progress){
		int oriProgress = 0;
		LinkedHashMap<CncItems,Object> dtMap = dataMap.get(ip);
		if(null != dtMap){
			if(null != dtMap.get(CncItems.PROGRESS)){
				oriProgress = (int)dtMap.get(CncItems.PROGRESS);
				if(oriProgress != progress){
					setData(ip, CncItems.PROGRESS, progress);
					notifyObservers(observers,getDataForObservers(ip),false,false,true);
				}
			}
		}
	}
	
	/**
	 * add by Hui Zhi 2022/5/16
	 * @param robotIP
	 * @return position name where robot parking
	 */
	public String getCNCPosition(String cncIP) {
		String CNCPos = "";
		LinkedHashMap<CncItems, Object> data = getData(cncIP);
		if (null != data) {
			CNCPos = (String) data.get(CncItems.TAGNAME);
		}
		return CNCPos;
	}
	
	private void updateZoneStartTimeData(String ip,long finishT){
		if(finishT <=0) return;
		String zones = "";
		String[] zoneNo = null;
		long[] zoneSimulationT = null, zoneSimFinishT = null;
		long startT = 0;
		boolean bZeroZoneStartT = true;
		
		zones = (String) getData(ip).get(CncItems.DT_MACHINING_ZNS);
		if(!"".equals(zones)){
			zoneNo = zones.split(";");
			zoneSimulationT = new long[zoneNo.length];
			zoneSimFinishT = new long[zoneNo.length];
			for(int i=0; i<zoneNo.length; i++){
				if(getZoneStartTime(ip, Integer.valueOf(zoneNo[i])) > 0){
					bZeroZoneStartT = false;
					break;
				}
				zoneSimFinishT[i] = 0;
				zoneSimulationT[i] = getZoneSimulationTime(ip, Integer.valueOf(zoneNo[i]));
				for(int j=i; j>=0; j--){
					zoneSimFinishT[i] += zoneSimulationT[j];
				}
			}
			
			if(bZeroZoneStartT){
				startT = getStartMachiningTime(ip);
				for(int i=0; i<zoneNo.length; i++){
					if(0 == i){
						setZoneStartTime(ip, Integer.valueOf(zoneNo[i]), startT);
					}else{
						setZoneStartTime(ip, Integer.valueOf(zoneNo[i]), startT+(finishT-startT)*zoneSimFinishT[i-1]/zoneSimFinishT[zoneNo.length-1]);
					}
				}
			}
		}
	}
	
	private String showMachiningTime(long t_ms){
		String s = "0";
		long temp = 0;
		
		if(t_ms < 1000){
			s = t_ms + "ms";
		}else if(t_ms < 60000){
			s = (t_ms / 1000) + "s";
		}else if(t_ms < 3600000){
			s = (t_ms / 60000) + "min" + ((t_ms % 60000) / 1000) + "s";
		}else{
			s = (t_ms / 3600000) + "hr";
			temp = t_ms % 3600000;
			if(temp > 60000){
				s = s + (temp / 60000) + "min";
				temp = temp % 60000;
			}else{
				s = s + "0min";
			}
			s = s + (temp / 1000) + "s";
		}
		
		return s;
	}
}
