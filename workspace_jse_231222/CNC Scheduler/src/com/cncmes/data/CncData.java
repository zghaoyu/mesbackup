package com.cncmes.data;

import java.util.LinkedHashMap;

import com.cncmes.base.CncItems;
import com.cncmes.base.DeviceState;
import com.cncmes.base.RunningData;

public final class CncData extends RunningData<CncItems> {
	private final int WKZONEQTY = 14;
	private static CncData cncData = new CncData();
	private CncData(){}
	
	public static CncData getInstance(){
		return cncData;
	}
	
	public synchronized void setCncState(String ip, DeviceState devState){
		setData(ip, CncItems.STATE, devState);
	}
	
	public synchronized DeviceState getCncState(String ip){
		return ((DeviceState)getData(ip).get(CncItems.STATE));
	}
	
	public synchronized void setWorkpieceID(String ip,int workZoneNo,String workpieceID){
		if(1 == workZoneNo) setData(ip, CncItems.WKPIECE1, workpieceID);
		if(2 == workZoneNo) setData(ip, CncItems.WKPIECE2, workpieceID);
		if(3 == workZoneNo) setData(ip, CncItems.WKPIECE3, workpieceID);
		if(4 == workZoneNo) setData(ip, CncItems.WKPIECE4, workpieceID);
		if(5 == workZoneNo) setData(ip, CncItems.WKPIECE5, workpieceID);
		if(6 == workZoneNo) setData(ip, CncItems.WKPIECE6, workpieceID);
		if(7 == workZoneNo) setData(ip, CncItems.WKPIECE7, workpieceID);
		if(8 == workZoneNo) setData(ip, CncItems.WKPIECE8, workpieceID);
		if(9 == workZoneNo) setData(ip, CncItems.WKPIECE9, workpieceID);
		if(10 == workZoneNo) setData(ip, CncItems.WKPIECE10, workpieceID);
		if(11 == workZoneNo) setData(ip, CncItems.WKPIECE11, workpieceID);
		if(12 == workZoneNo) setData(ip, CncItems.WKPIECE12, workpieceID);
		if(13 == workZoneNo) setData(ip, CncItems.WKPIECE13, workpieceID);
		if(14 == workZoneNo) setData(ip, CncItems.WKPIECE14, workpieceID);
	}
	
	public synchronized String getWorkpieceID(String ip,int workZoneNo){
		String workpieceID = "";
		
		LinkedHashMap<CncItems,Object> dtMap = dataMap.get(ip);
		if(null != dtMap){
			Object id = null;
			if(1 == workZoneNo) id = dtMap.get(CncItems.WKPIECE1);
			if(2 == workZoneNo) id = dtMap.get(CncItems.WKPIECE2);
			if(3 == workZoneNo) id = dtMap.get(CncItems.WKPIECE3);
			if(4 == workZoneNo) id = dtMap.get(CncItems.WKPIECE4);
			if(5 == workZoneNo) id = dtMap.get(CncItems.WKPIECE5);
			if(6 == workZoneNo) id = dtMap.get(CncItems.WKPIECE6);
			if(7 == workZoneNo) id = dtMap.get(CncItems.WKPIECE7);
			if(8 == workZoneNo) id = dtMap.get(CncItems.WKPIECE8);
			if(9 == workZoneNo) id = dtMap.get(CncItems.WKPIECE9);
			if(10 == workZoneNo) id = dtMap.get(CncItems.WKPIECE10);
			if(11 == workZoneNo) id = dtMap.get(CncItems.WKPIECE11);
			if(12 == workZoneNo) id = dtMap.get(CncItems.WKPIECE12);
			if(13 == workZoneNo) id = dtMap.get(CncItems.WKPIECE13);
			if(14 == workZoneNo) id = dtMap.get(CncItems.WKPIECE14);
			if(null != id) workpieceID = (String)id;
		}
		
		return workpieceID;
	}
	
	public synchronized LinkedHashMap<Integer,String> getWorkpieceIDs(String ip){
		LinkedHashMap<Integer,String> ids = new LinkedHashMap<Integer,String>();
		
		String id = "";
		for(int i=1; i<=WKZONEQTY; i++){
			id = getWorkpieceID(ip, i);
			if(!"".equals(id)) ids.put(i, id);
		}
		
		return ids;
	}
	
	public void restoreWorkpieceIDs(String cncIP,String rackID){
		String id = "";
		String ids = "";
		String zoneNos = "";
		String lineName = getLineName(cncIP);
		
		for(int i=1; i<=WKZONEQTY; i++){
			id = getWorkpieceID(cncIP, i);
			if(!"".equals(id)){
				if("".equals(ids)){
					ids = id;
					zoneNos = ""+i;
				}else{
					ids += ","+id;
					zoneNos += ","+i;
				}
			}
		}
		
		if(!"".equals(ids)){
			String[] emptySlotNo = RackMaterial.getInstance().getEmptySlots(lineName, rackID);
			String[] wpIDs = ids.split(",");
			String[] zones = zoneNos.split(",");
			int cnt = (emptySlotNo.length >= wpIDs.length)?wpIDs.length:emptySlotNo.length;
			for(int i=0; i<cnt; i++){
				RackMaterial.getInstance().updateSlot(lineName, rackID, Integer.parseInt(emptySlotNo[i]), wpIDs[i]);
				setWorkpieceID(cncIP, Integer.parseInt(zones[i]), "");
			}
		}
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
	
	public String getCncModel(String ip){
		String model = "";
		
		LinkedHashMap<CncItems,Object> dtMap = dataMap.get(ip);
		if(null != dtMap) model = (String) dtMap.get(CncItems.MODEL);
		
		return model;
	}
	
	public String getLineName(String ip){
		String lineName = "";
		
		LinkedHashMap<CncItems,Object> dtMap = dataMap.get(ip);
		if(null != dtMap) lineName = (String) dtMap.get(CncItems.LINENAME);
		
		return lineName;
	}
	
	public void setStartMachiningTime(String ip,long startT){
		setData(ip, CncItems.STARTTIME, startT);
	}
	
	public long getStartMachiningTime(String ip){
		long t = 0;
		
		LinkedHashMap<CncItems,Object> dtMap = getData(ip);
		if(null != dtMap){
			Object mt = dtMap.get(CncItems.STARTTIME);
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
	
	public String getCncDataString(String ip){
		String data = "";
		LinkedHashMap<CncItems,Object> dtMap = dataMap.get(ip);
		if(null != dtMap){
			DeviceState state = (DeviceState) dtMap.get(CncItems.STATE);
			data = data + "state: " + state + "\r\n";
			
			Object time = dtMap.get(CncItems.STARTTIME);
			long mtime = 0;
			if(null != time){
				mtime = (long)time;
				mtime = System.currentTimeMillis() - mtime;
			}
			
			data = data + "machineTime: " + showMachiningTime(mtime) + "\r\n";
			data = data + "toolNo: " + dtMap.get(CncItems.TOOLNO) + "\r\n";
			data = data + "feedRate: " + dtMap.get(CncItems.FEEDRATE) + "\r\n";
			data = data + "RPM: " + dtMap.get(CncItems.RPM) + "\r\n";
			data = data + "command: " + dtMap.get(CncItems.COMMAND);
		}
		return data;
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
