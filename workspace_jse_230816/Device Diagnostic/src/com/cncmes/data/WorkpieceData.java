/**
 * Functions of this class: record and trace the status of every workpiece
 */
package com.cncmes.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.commons.io.FileUtils;

import com.cncmes.base.DeviceObserver;
import com.cncmes.base.DeviceState;
import com.cncmes.base.DeviceSubject;
import com.cncmes.base.RunningData;
import com.cncmes.base.SpecItems;
import com.cncmes.base.WorkpieceItems;
import com.cncmes.utils.DataUtils;
import com.cncmes.utils.MathUtils;
import com.cncmes.utils.MyFileUtils;
import com.cncmes.utils.MySystemUtils;

import net.sf.json.JSONObject;

public class WorkpieceData extends RunningData<WorkpieceItems> implements DeviceSubject {
	private static WorkpieceData wpData = new WorkpieceData();
	private static ArrayList<DeviceObserver> observers = new ArrayList<DeviceObserver>();
	private WorkpieceData(){}
	
	public static WorkpieceData getInstance(){
		return wpData;
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
				observer.update(wpData, data, threadMode, threadSequential, theLastThread);
			}
		}
	}
	
	public String getDataForObservers(String materialID, boolean batchUpdate){
		String info = "";
		String jsonStr = getWorkpieceInfoJsonStr(materialID);
		
		if(!"".equals(jsonStr)){
			JSONObject jsonObj = JSONObject.fromObject(jsonStr);
			info = materialID;
			info += "," + jsonObj.getString("lineName");
			info += "," + jsonObj.getString("state");
			info += "," + jsonObj.getString("procQty");
			info += "," + jsonObj.getString("process");
			info += "," + jsonObj.getString("surface");
			info += "," + jsonObj.getString("machineTime");
			info += "," + jsonObj.getString("ncModel");
			info += "," + jsonObj.getString("program");
			info += "," + jsonObj.getString("conveyorID");
			info += "," + jsonObj.getString("conveyorSlotNo");
			if(batchUpdate){
				info += ",1";
			}else{
				info += ",0";
			}
			info += "," + MathUtils.MD5Encode(info);
		}
		
		return info;
	}
	
	private String getWorkpieceInfoJsonStr(String materialID){
		String jsonStr = "";
		
		LinkedHashMap<WorkpieceItems,Object> dt = getData(materialID);
		if(null != dt){
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("id", materialID);
			jsonObj.put("lineName", ""+dt.get(WorkpieceItems.LINENAME));
			jsonObj.put("state", ""+dt.get(WorkpieceItems.STATE));
			jsonObj.put("procQty", ""+dt.get(WorkpieceItems.PROCQTY));
			jsonObj.put("specID", ""+(null!=dt.get(WorkpieceItems.SPECID)?dt.get(WorkpieceItems.SPECID):0L));
			jsonObj.put("curProc", ""+(null!=dt.get(WorkpieceItems.CURPROC)?dt.get(WorkpieceItems.CURPROC):0));
			
			LinkedHashMap<SpecItems, String> spec = getAllProcInfo(materialID);
			if(null != spec){
				jsonObj.put("process", "" + (null!=dt.get(WorkpieceItems.PROCESS)?dt.get(WorkpieceItems.PROCESS):spec.get(SpecItems.PROCESSNAME)));
				jsonObj.put("surface", "" + (null!=dt.get(WorkpieceItems.SURFACE)?dt.get(WorkpieceItems.SURFACE):spec.get(SpecItems.SURFACE)));
				jsonObj.put("machineTime", "" + (null!=dt.get(WorkpieceItems.MACHINETIME)?dt.get(WorkpieceItems.MACHINETIME):spec.get(SpecItems.SIMTIME)));
				jsonObj.put("ncModel", "" + (null!=dt.get(WorkpieceItems.NCMODEL)?dt.get(WorkpieceItems.NCMODEL):spec.get(SpecItems.NCMODEL)));
				jsonObj.put("program", "" + (null!=dt.get(WorkpieceItems.PROGRAM)?dt.get(WorkpieceItems.PROGRAM):spec.get(SpecItems.PROGRAM)));
			}else{
				jsonObj.put("process", "" + (null!=dt.get(WorkpieceItems.PROCESS)?dt.get(WorkpieceItems.PROCESS):"NULL"));
				jsonObj.put("surface", "" + (null!=dt.get(WorkpieceItems.SURFACE)?dt.get(WorkpieceItems.SURFACE):"NULL"));
				jsonObj.put("machineTime", "" + (null!=dt.get(WorkpieceItems.MACHINETIME)?dt.get(WorkpieceItems.MACHINETIME):"NULL"));
				jsonObj.put("ncModel", "" + (null!=dt.get(WorkpieceItems.NCMODEL)?dt.get(WorkpieceItems.NCMODEL):"NULL"));
				jsonObj.put("program", "" + (null!=dt.get(WorkpieceItems.PROGRAM)?dt.get(WorkpieceItems.PROGRAM):"NULL"));
			}
			
			jsonObj.put("conveyorID", ""+dt.get(WorkpieceItems.CONVEYORID));
			jsonObj.put("conveyorSlotNo", ""+dt.get(WorkpieceItems.CONVEYORSLOTNO));
			
			jsonObj.put("proc1Status", ""+(null!=dt.get(WorkpieceItems.PROC1STATUS)?dt.get(WorkpieceItems.PROC1STATUS):DeviceState.PLAN));
			jsonObj.put("proc2Status", ""+(null!=dt.get(WorkpieceItems.PROC2STATUS)?dt.get(WorkpieceItems.PROC2STATUS):DeviceState.PLAN));
			jsonObj.put("proc3Status", ""+(null!=dt.get(WorkpieceItems.PROC3STATUS)?dt.get(WorkpieceItems.PROC3STATUS):DeviceState.PLAN));
			jsonObj.put("proc4Status", ""+(null!=dt.get(WorkpieceItems.PROC4STATUS)?dt.get(WorkpieceItems.PROC4STATUS):DeviceState.PLAN));
			jsonObj.put("proc5Status", ""+(null!=dt.get(WorkpieceItems.PROC5STATUS)?dt.get(WorkpieceItems.PROC5STATUS):DeviceState.PLAN));
			jsonObj.put("proc6Status", ""+(null!=dt.get(WorkpieceItems.PROC6STATUS)?dt.get(WorkpieceItems.PROC6STATUS):DeviceState.PLAN));
			
			jsonStr = jsonObj.toString();
		}
		
		return jsonStr;
	}
	
	public void dumpWorkpieceInfo(){
		if(dataMap.size() <= 0) return;
		
		String key = "materialInfo";
		String path = MySystemUtils.getMemoryDumpPath(key);
		if(!"".equals(path)){
			String content = "", temp = "";
			for(String id:dataMap.keySet()){
				temp = getWorkpieceInfoJsonStr(id);
				if("".equals(content)){
					content = temp;
				}else{
					content += "\r\n" + temp;
				}
			}
			if(!"".equals(content)) MyFileUtils.saveToFile(content, path);
		}
	}
	
	public String restoreWorkpieceInfo(DeviceState restoreState, boolean informObserver, boolean threadMode, boolean threadSequential, boolean theLastThread){
		String key = "materialInfo";
		String path = MySystemUtils.getMemoryDumpPath(key);
		String lastID = "";
		if(!"".equals(path)){
			try {
				String[] jsonStrs = FileUtils.readFileToString(new File(path), "UTF-8").split("\r\n");
				JSONObject jsonObj = null;
				for(String jsonStr:jsonStrs){
					jsonObj = JSONObject.fromObject(jsonStr);
					DeviceState devState = DataUtils.getDevStateByString(jsonObj.getString("state"));
					if(null == restoreState || devState == restoreState){
						setData(jsonObj.getString("id"), WorkpieceItems.ID, jsonObj.getString("id"));
						setData(jsonObj.getString("id"), WorkpieceItems.STATE, devState);
						setData(jsonObj.getString("id"), WorkpieceItems.SPECID, Long.valueOf(jsonObj.getString("specID")));
						setData(jsonObj.getString("id"), WorkpieceItems.LINENAME, jsonObj.getString("lineName"));
						setData(jsonObj.getString("id"), WorkpieceItems.CONVEYORID, jsonObj.getString("conveyorID"));
						setData(jsonObj.getString("id"), WorkpieceItems.CONVEYORSLOTNO, jsonObj.getString("conveyorSlotNo"));
						setData(jsonObj.getString("id"), WorkpieceItems.PROCQTY, Integer.valueOf(jsonObj.getString("procQty")));
						setData(jsonObj.getString("id"), WorkpieceItems.PROCESS, jsonObj.getString("process"));
						setData(jsonObj.getString("id"), WorkpieceItems.NCMODEL, jsonObj.getString("ncModel"));
						setData(jsonObj.getString("id"), WorkpieceItems.PROGRAM, jsonObj.getString("program"));
						setData(jsonObj.getString("id"), WorkpieceItems.SURFACE, jsonObj.getString("surface"));
						setData(jsonObj.getString("id"), WorkpieceItems.MACHINETIME, jsonObj.getString("machineTime"));
						setData(jsonObj.getString("id"), WorkpieceItems.CURPROC, Integer.valueOf(jsonObj.getString("curProc")));
						setData(jsonObj.getString("id"), WorkpieceItems.PROC1STATUS, DataUtils.getDevStateByString(jsonObj.getString("proc1Status")));
						setData(jsonObj.getString("id"), WorkpieceItems.PROC2STATUS, DataUtils.getDevStateByString(jsonObj.getString("proc2Status")));
						setData(jsonObj.getString("id"), WorkpieceItems.PROC3STATUS, DataUtils.getDevStateByString(jsonObj.getString("proc3Status")));
						setData(jsonObj.getString("id"), WorkpieceItems.PROC4STATUS, DataUtils.getDevStateByString(jsonObj.getString("proc4Status")));
						setData(jsonObj.getString("id"), WorkpieceItems.PROC5STATUS, DataUtils.getDevStateByString(jsonObj.getString("proc5Status")));
						setData(jsonObj.getString("id"), WorkpieceItems.PROC6STATUS, DataUtils.getDevStateByString(jsonObj.getString("proc6Status")));
						if(informObserver){
							lastID = jsonObj.getString("id");
							notifyObservers(observers,getDataForObservers(lastID,true),threadMode, threadSequential, theLastThread);
						}
					}
				}
				if(!"".equals(lastID)) notifyObservers(observers,getDataForObservers(lastID,false),threadMode, threadSequential, theLastThread);
			} catch (IOException e) {
			}
		}
		return lastID;
	}
	
	/**
	 * 
	 * @param id
	 * @param state
	 * @param batchUpdate whether to tell this is a batch update
	 * @param threadMode whether use thread mode
	 * @param threadSequential whether the thread is executed sequentially
	 * @param theLastThread whether it's the last thread,only set to true to execute all threads
	 * @param forceUpdate whether to force update
	 */
	public synchronized void setWorkpieceState(String id,DeviceState state,boolean batchUpdate,boolean threadMode,boolean threadSequential,boolean theLastThread,boolean forceUpdate){
		DeviceState oriState = (DeviceState)getWorkpieceState(id);
		setData(id,WorkpieceItems.STATE,state);
		if(oriState != state || forceUpdate) notifyObservers(observers,getDataForObservers(id,batchUpdate),threadMode,threadSequential,theLastThread);
	}
	
	public synchronized Object getWorkpieceState(String id){
		return getData(id).get(WorkpieceItems.STATE);
	}
	
	public synchronized void setProcessState(String workpieceID,int procNo,DeviceState procState){
		LinkedHashMap<WorkpieceItems, Object> wp = getData(workpieceID);
		if(null != wp){
			int procQty = (int) wp.get(WorkpieceItems.PROCQTY);
			if(procQty >= procNo){
				if(1 == procNo) setData(workpieceID,WorkpieceItems.PROC1STATUS,procState);
				if(2 == procNo) setData(workpieceID,WorkpieceItems.PROC2STATUS,procState);
				if(3 == procNo) setData(workpieceID,WorkpieceItems.PROC3STATUS,procState);
				if(4 == procNo) setData(workpieceID,WorkpieceItems.PROC4STATUS,procState);
				if(5 == procNo) setData(workpieceID,WorkpieceItems.PROC5STATUS,procState);
				if(6 == procNo) setData(workpieceID,WorkpieceItems.PROC6STATUS,procState);
			}
		}
	}
	
	public synchronized int getUnscheduledCount(){
		int unscheduledWorkpieces = 0;
		Object state;
		
		for(String key:dataMap.keySet()){
			state = getData(key).get(WorkpieceItems.STATE);
			if(null != state && DeviceState.UNSCHEDULE == (DeviceState)state){
				unscheduledWorkpieces++;
			}
		}
		
		return unscheduledWorkpieces;
	}
	
	public synchronized String getUnscheduledID(){
		String id = "";
		Object state;
		
		for(String key:dataMap.keySet()){
			state = getData(key).get(WorkpieceItems.STATE);
			if(null != state && DeviceState.UNSCHEDULE == (DeviceState)state){
				id = (String) getData(key).get(WorkpieceItems.ID);
				break;
			}
		}
		
		return id;
	}
	
	public int getWorkpieceQtyByState(DeviceState devState){
		int qty = 0;
		Object state;
		
		try {
			for(String key:dataMap.keySet()){
				state = getData(key).get(WorkpieceItems.STATE);
				if(null != state && devState == (DeviceState)state){
					qty++;
				}
			}
		} catch (Exception e) {
		}
		
		return qty;
	}
	
	public long getSpecNo(String workpieceID){
		long specNo = 0;
		
		LinkedHashMap<WorkpieceItems, Object> dtMap = dataMap.get(workpieceID);
		if(null != dtMap) specNo = (long) dtMap.get(WorkpieceItems.SPECID);
		
		return specNo;
	}
	
	public void setCurrentProcNo(String workpieceID,int procNo){
		setData(workpieceID, WorkpieceItems.CURPROC, procNo);
	}
	
	public int getCurrentProcNo(String workpieceID){
		int procNo = 0;
		
		LinkedHashMap<WorkpieceItems, Object> dtMap = dataMap.get(workpieceID);
		if(null != dtMap) procNo = (int) dtMap.get(WorkpieceItems.CURPROC);
		
		return procNo;
	}
	
	public LinkedHashMap<SpecItems,String> getAllProcInfo(String workpieceID){
		LinkedHashMap<SpecItems,String> procs = new LinkedHashMap<SpecItems,String>();
		String procName = "";
		String program = "";
		String ncModel = "";
		String simTime = "";
		String surface = "";
		String procNo = "";
		
		int procQty = getProcQty(workpieceID);
		if(procQty > 0){
			for(int i=1; i<=procQty; i++){
				LinkedHashMap<SpecItems, Object> spec = getOneProcInfo(workpieceID, i);
				if(null != spec){
					if("".equals(procName)){
						procName = (String) spec.get(SpecItems.PROCESSNAME);
						program = (String) spec.get(SpecItems.PROGRAM);
						ncModel = (String) spec.get(SpecItems.NCMODEL);
						simTime = (String) spec.get(SpecItems.SIMTIME);
						surface = String.valueOf(spec.get(SpecItems.SURFACE));
						procNo = String.valueOf(spec.get(SpecItems.PROCNO));
					}else{
						procName += "/" + (String) spec.get(SpecItems.PROCESSNAME);
						program += "/" + (String) spec.get(SpecItems.PROGRAM);
						ncModel += "/" + (String) spec.get(SpecItems.NCMODEL);
						simTime += "/" + (String) spec.get(SpecItems.SIMTIME);
						surface += "/" + String.valueOf(spec.get(SpecItems.SURFACE));
						procNo += "/" + String.valueOf(spec.get(SpecItems.PROCNO));
					}
				}
			}
			
			procs.put(SpecItems.PROCESSNAME, procName.replace(",", "#"));
			procs.put(SpecItems.PROGRAM, program.replace(",", "#"));
			procs.put(SpecItems.NCMODEL, ncModel.replace(",", "#"));
			procs.put(SpecItems.SIMTIME, simTime.replace(",", "#"));
			procs.put(SpecItems.SURFACE, surface.replace(",", "#"));
			procs.put(SpecItems.PROCNO, procNo.replace(",", "#"));
		}
		
		return procs;
	}
	
	public boolean canMachineByCNC(String workpieceID,String cncModel,LinkedHashMap<SpecItems,Object> spec,Integer...procNo){
		boolean ok = false;
		String procName = "";
		String procNCModel = "";
		
		if(null == spec || (procNo.length > 0 && null != procNo[0] && procNo[0] > 0)) spec = getNextProcInfo(workpieceID,procNo);
		if(null != spec){
			procName = (String) spec.get(SpecItems.PROCESSNAME);
			procNCModel = (String) spec.get(SpecItems.NCMODEL);
		}
		
		String cncSupportProc = CncDriver.getInstance().getCNCSupportProcess(cncModel);
		if(procNCModel.indexOf(cncModel) >=0 && cncSupportProc.indexOf(procName) >= 0) ok = true;
		
		return ok;
	}
	
	public int getNextProcSimtime(String workpieceID,String cncModel,LinkedHashMap<SpecItems,Object> spec,Integer...procNo){
		int simulationtime = 0;
		String procNCModel = "";
		String simTime = "";
		
		if(null == spec || (procNo.length > 0 && null != procNo[0] && procNo[0] > 0)) spec = getNextProcInfo(workpieceID,procNo);
		if(null != spec){
			procNCModel = (String) spec.get(SpecItems.NCMODEL);
			simTime = (String) spec.get(SpecItems.SIMTIME);
			
			String[] models = procNCModel.split(",");
			String[] times = simTime.split(",");
			
			for(int i=0; i<models.length; i++){
				if(cncModel.equals(models[i])){
					simulationtime = Integer.parseInt(times[i]);
					break;
				}
			}
		}
		
		return simulationtime;
	}
	
	public String getNextProcProgram(String workpieceID,String cncModel,LinkedHashMap<SpecItems,Object> spec,Integer...procNo){
		String ncProgram = "";
		String procNCModel = "";
		String program = "";
		
		if(null == spec || (procNo.length > 0 && null != procNo[0] && procNo[0] > 0)) spec = getNextProcInfo(workpieceID,procNo);
		if(null != spec){
			procNCModel = (String) spec.get(SpecItems.NCMODEL);
			program = (String) spec.get(SpecItems.PROGRAM);
			
			String[] models = procNCModel.split(",");
			String[] progs = program.split(",");
			
			for(int i=0; i<models.length; i++){
				if(cncModel.equals(models[i])){
					ncProgram = progs[i];
					break;
				}
			}
		}
		
		return ncProgram;
	}
	
	public int getNextProcNo(String workpieceID,LinkedHashMap<SpecItems,Object> spec){
		int procNo = 0;
		
		if(null == spec) spec = getNextProcInfo(workpieceID);
		if(null != spec) procNo = (int) spec.get(SpecItems.PROCNO);
		
		return procNo;
	}
	
	public int getNextProcSurface(String workpieceID,LinkedHashMap<SpecItems,Object> spec,Integer...procNo){
		int surface = 0;
		
		if(null == spec || (procNo.length > 0 && null != procNo[0] && procNo[0] > 0)) spec = getNextProcInfo(workpieceID,procNo);
		if(null != spec) surface = (int) spec.get(SpecItems.SURFACE);
		
		return surface;
	}
	
	public String getNextProcName(String workpieceID,LinkedHashMap<SpecItems,Object> spec,Integer...procNo){
		String procName = "";
		
		if(null == spec || (procNo.length > 0 && null != procNo[0] && procNo[0] > 0)) spec = getNextProcInfo(workpieceID,procNo);
		if(null != spec) procName = (String) spec.get(SpecItems.PROCESSNAME);
		
		return procName;
	}
	
	@SuppressWarnings("unchecked")
	public LinkedHashMap<SpecItems, Object> getNextProcInfo(String workpieceID,Integer...procNo){
		LinkedHashMap<SpecItems,Object> spec = null;
		int checkProcNo = 0;
		if(procNo.length > 0 && null != procNo[0] && procNo[0] > 0) checkProcNo = procNo[0];
		
		if(!workpieceIsDone(workpieceID)){
			MachiningSpec mcSpec = MachiningSpec.getInstance();
			LinkedHashMap<WorkpieceItems,Object> wp = getData(workpieceID);
			int processQty = (int) wp.get(WorkpieceItems.PROCQTY);
			for(int i=1;i<=6;i++){
				if(processQty >= i){
					if(checkProcNo > 0 && checkProcNo <= processQty) i = checkProcNo;
					Object st = null;
					if(1 == i) st = wp.get(WorkpieceItems.PROC1STATUS);
					if(2 == i) st = wp.get(WorkpieceItems.PROC2STATUS);
					if(3 == i) st = wp.get(WorkpieceItems.PROC3STATUS);
					if(4 == i) st = wp.get(WorkpieceItems.PROC4STATUS);
					if(5 == i) st = wp.get(WorkpieceItems.PROC5STATUS);
					if(6 == i) st = wp.get(WorkpieceItems.PROC6STATUS);
					
					if(null != st && DeviceState.FINISH != (DeviceState)st){
						String specID = ""+wp.get(WorkpieceItems.SPECID);
						if(1 == i) spec = (LinkedHashMap<SpecItems, Object>) mcSpec.getData(specID).get(SpecItems.PROC1);
						if(2 == i) spec = (LinkedHashMap<SpecItems, Object>) mcSpec.getData(specID).get(SpecItems.PROC2);
						if(3 == i) spec = (LinkedHashMap<SpecItems, Object>) mcSpec.getData(specID).get(SpecItems.PROC3);
						if(4 == i) spec = (LinkedHashMap<SpecItems, Object>) mcSpec.getData(specID).get(SpecItems.PROC4);
						if(5 == i) spec = (LinkedHashMap<SpecItems, Object>) mcSpec.getData(specID).get(SpecItems.PROC5);
						if(6 == i) spec = (LinkedHashMap<SpecItems, Object>) mcSpec.getData(specID).get(SpecItems.PROC6);
						break;
					}
					if(checkProcNo > 0 && checkProcNo <= processQty) break;
				}
			}
		}
		
		return spec;
	}
	
	public int getProcQty(String workpieceID){
		int qty = 0;
		
		LinkedHashMap<WorkpieceItems,Object> wp = getData(workpieceID);
		if(null != wp) qty = (int) wp.get(WorkpieceItems.PROCQTY);
		
		return qty;
	}
	
	@SuppressWarnings("unchecked")
	public LinkedHashMap<SpecItems, Object> getOneProcInfo(String workpieceID,int procNo){
		LinkedHashMap<SpecItems,Object> spec = null;
		
		LinkedHashMap<WorkpieceItems,Object> wp = getData(workpieceID);
		if(null != wp){
			int processQty = (int) wp.get(WorkpieceItems.PROCQTY);
			if(procNo > 0 && procNo <= processQty){
				Object st = null;
				if(1 == procNo) st = wp.get(WorkpieceItems.PROC1STATUS);
				if(2 == procNo) st = wp.get(WorkpieceItems.PROC2STATUS);
				if(3 == procNo) st = wp.get(WorkpieceItems.PROC3STATUS);
				if(4 == procNo) st = wp.get(WorkpieceItems.PROC4STATUS);
				if(5 == procNo) st = wp.get(WorkpieceItems.PROC5STATUS);
				if(6 == procNo) st = wp.get(WorkpieceItems.PROC6STATUS);
				
				if(null != st){
					MachiningSpec mcSpec = MachiningSpec.getInstance();
					String specID = ""+wp.get(WorkpieceItems.SPECID);
					if(1 == procNo) spec = (LinkedHashMap<SpecItems, Object>) mcSpec.getData(specID).get(SpecItems.PROC1);
					if(2 == procNo) spec = (LinkedHashMap<SpecItems, Object>) mcSpec.getData(specID).get(SpecItems.PROC2);
					if(3 == procNo) spec = (LinkedHashMap<SpecItems, Object>) mcSpec.getData(specID).get(SpecItems.PROC3);
					if(4 == procNo) spec = (LinkedHashMap<SpecItems, Object>) mcSpec.getData(specID).get(SpecItems.PROC4);
					if(5 == procNo) spec = (LinkedHashMap<SpecItems, Object>) mcSpec.getData(specID).get(SpecItems.PROC5);
					if(6 == procNo) spec = (LinkedHashMap<SpecItems, Object>) mcSpec.getData(specID).get(SpecItems.PROC6);
				}
			}
		}
		
		return spec;
	}
	
	public boolean workpieceIsDone(String workpieceID){
		boolean done = false;
		
		LinkedHashMap<WorkpieceItems,Object> wp = getData(workpieceID);
		if(null != wp){
			int processQty = (int) wp.get(WorkpieceItems.PROCQTY);
			if(processQty > 0){
				done = true;
				for(int i=1;i<=6;i++){
					if(processQty >= i){
						Object st = null;
						if(1 == i) st = wp.get(WorkpieceItems.PROC1STATUS);
						if(2 == i) st = wp.get(WorkpieceItems.PROC2STATUS);
						if(3 == i) st = wp.get(WorkpieceItems.PROC3STATUS);
						if(4 == i) st = wp.get(WorkpieceItems.PROC4STATUS);
						if(5 == i) st = wp.get(WorkpieceItems.PROC5STATUS);
						if(6 == i) st = wp.get(WorkpieceItems.PROC6STATUS);
						
						if(null != st){
							if(DeviceState.FINISH != (DeviceState)st){
								done = false;
								break;
							}
						}
					}else{
						break;
					}
				}
			}else{
				done = true;
			}
		}
		
		return done;
	}
	
	public void appendData(String workpieceID,WorkpieceItems item,Object val){
		LinkedHashMap<WorkpieceItems,Object> dt = getData(workpieceID);
		if(null != dt){
			if(null == dt.get(item)){
				dt.put(item, val);
			}else{
				dt.put(item, dt.get(item)+"/"+val);
			}
		}
	}
	
	public Object getItemVal(String workpieceID,WorkpieceItems item){
		Object val = null;
		
		try {
			val = getData(workpieceID).get(item);
		} catch (Exception e) {
		}
		
		return val;
	}
	
	public String getLineName(String workpieceID){
		String val = "";
		
		if(null != getData(workpieceID)){
			val = (String) getData(workpieceID).get(WorkpieceItems.LINENAME);
		}
		
		return val;
	}
	
	public void setSlotNo(String workpieceID, String slotNo){
		setData(workpieceID, WorkpieceItems.CONVEYORSLOTNO, slotNo);
	}
	
	public void setRackID(String workpieceID, String rackID){
		setData(workpieceID, WorkpieceItems.CONVEYORID, rackID);
	}
	
	public String getRackID(String workpieceID){
		String val = "";
		
		if(null != getData(workpieceID)){
			val = (String) getData(workpieceID).get(WorkpieceItems.CONVEYORID);
		}
		
		return val;
	}
	
	public void printWorkpiece(){
		for(String key:dataMap.keySet()){
			System.out.println(key+getWorkpieceState(key));
		}
	}
}
