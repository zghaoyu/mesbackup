/**
 * Functions of this class: record and trace the status of every workpiece
 */
package com.cncmes.data;

import java.util.LinkedHashMap;

import com.cncmes.base.DeviceState;
import com.cncmes.base.RunningData;
import com.cncmes.base.SpecItems;
import com.cncmes.base.WorkpieceItems;

public class WorkpieceData extends RunningData<WorkpieceItems> {
	private static WorkpieceData wpData = new WorkpieceData();
	private WorkpieceData(){}
	
	public static WorkpieceData getInstance(){
		return wpData;
	}
	
	public synchronized void setWorkpieceState(String id,DeviceState state){
		setData(id,WorkpieceItems.STATE,state);
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
						if(1 == i) spec = (LinkedHashMap<SpecItems, Object>) MachiningSpec.getInstance().getData(specID).get(SpecItems.PROC1);
						if(2 == i) spec = (LinkedHashMap<SpecItems, Object>) MachiningSpec.getInstance().getData(specID).get(SpecItems.PROC2);
						if(3 == i) spec = (LinkedHashMap<SpecItems, Object>) MachiningSpec.getInstance().getData(specID).get(SpecItems.PROC3);
						if(4 == i) spec = (LinkedHashMap<SpecItems, Object>) MachiningSpec.getInstance().getData(specID).get(SpecItems.PROC4);
						if(5 == i) spec = (LinkedHashMap<SpecItems, Object>) MachiningSpec.getInstance().getData(specID).get(SpecItems.PROC5);
						if(6 == i) spec = (LinkedHashMap<SpecItems, Object>) MachiningSpec.getInstance().getData(specID).get(SpecItems.PROC6);
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
					String specID = ""+wp.get(WorkpieceItems.SPECID);
					if(1 == procNo) spec = (LinkedHashMap<SpecItems, Object>) MachiningSpec.getInstance().getData(specID).get(SpecItems.PROC1);
					if(2 == procNo) spec = (LinkedHashMap<SpecItems, Object>) MachiningSpec.getInstance().getData(specID).get(SpecItems.PROC2);
					if(3 == procNo) spec = (LinkedHashMap<SpecItems, Object>) MachiningSpec.getInstance().getData(specID).get(SpecItems.PROC3);
					if(4 == procNo) spec = (LinkedHashMap<SpecItems, Object>) MachiningSpec.getInstance().getData(specID).get(SpecItems.PROC4);
					if(5 == procNo) spec = (LinkedHashMap<SpecItems, Object>) MachiningSpec.getInstance().getData(specID).get(SpecItems.PROC5);
					if(6 == procNo) spec = (LinkedHashMap<SpecItems, Object>) MachiningSpec.getInstance().getData(specID).get(SpecItems.PROC6);
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
	
	public void printWorkpiece(){
		for(String key:dataMap.keySet()){
			System.out.println(key+getWorkpieceState(key));
		}
	}
}
