package com.cncmes.utils;

import java.util.LinkedHashMap;

public class ErrorDesc {
	private ErrorDesc(){};
	
	public static LinkedHashMap<String,String> getPLCErrorDesc(){
		LinkedHashMap<String,String> errDesc = new LinkedHashMap<String,String>();
		
		int zone = 1, temp = 0;
		String key = "";
		for(int i=1; i<=36; i++){
			temp = i % 6;
			key = Integer.toHexString(i);
			
			if(1 == temp) errDesc.put(key, "Unlock Fixture "+zone+" failed");
			if(2 == temp) errDesc.put(key, "Lock Fixture "+zone+" failed");
			if(3 == temp) errDesc.put(key, "Lock(ext) Fixture "+zone+" failed");
			if(4 == temp) errDesc.put(key, "Unlock(ext) Fixture "+zone+" failed");
			if(5 == temp) errDesc.put(key, "Check Air Flow of Fixture "+zone+" failed");
			if(0 == temp) errDesc.put(key, "Turnoff Air Flow of Fixture "+zone+" failed");
			
			if(0 == i % 6) zone++;
		}
		
		for(String k:errDesc.keySet()){
			System.out.println(k+":"+errDesc.get(k));
		}
		
		return errDesc;
	}
	
	public static String getPLCErrorDesc(int errCode){
		String key = Integer.toHexString(errCode);
		String errDesc = "Unknow Error("+key+")";
		
		LinkedHashMap<String,String> errs = getPLCErrorDesc();
		if(errs.size()>0 && null != errs.get(key)){
			errDesc = errs.get(key);
		}
		
		return errDesc;
	}
	
	public static LinkedHashMap<String,String> getR2D2ErrorDesc(){
		LinkedHashMap<String,String> errDesc = new LinkedHashMap<String,String>();
		
		errDesc.put("100", "Move arm to pick/put top position of r2d2's hometable fail");
		errDesc.put("101", "Open gripper fail");
		errDesc.put("102", "Move arm downward to pick dut fail");
		errDesc.put("103", "Close gripper fail");
		errDesc.put("104", "Move arm upward to pick dut fail");
//		errDesc.put("105", "");
		errDesc.put("106", "Move arm downward to put dut fail");
		errDesc.put("107", "Open gripper fail");
		errDesc.put("108", "Move arm upward fail");
		errDesc.put("109", "Move arm to grab image fail");
		errDesc.put("110", "Open gripper fail");
		errDesc.put("111", "Move arm downward to pick dut in CNC/HT fail");
		errDesc.put("112", "Close gripper fail");
		errDesc.put("113", "Move arm upward fail");
		errDesc.put("114", "Move arm to back position fail");
		errDesc.put("115", "Move arm to back position fail");
		errDesc.put("116", "Move arm to grab image fail");
		errDesc.put("117", "Move arm to back position fail");
		errDesc.put("118", "Move arm to back position fail");
		errDesc.put("119", "Move arm to put dut in right postion in CNC/HT fail");
		errDesc.put("120", "Move arm downward to put dut in CNC/HT fail");
		errDesc.put("121", "Open gripper fail");
		errDesc.put("122", "Move arm upward fail");
		errDesc.put("123", "Move arm to back position fail");
		errDesc.put("124", "Stop the motion of robot base fail");
		errDesc.put("125", "Reset the motion of robot base fail");
		errDesc.put("126", "Move to navigation goal fail");
		errDesc.put("127", "Parking action fail");
		errDesc.put("128", "Unparking action fail");
		errDesc.put("129", "Move to Charging station navigation goal fail");
		errDesc.put("130", "Parking Charging station fail");
		errDesc.put("131", "Charge action fail");
		errDesc.put("132", "Take picture fail");
		errDesc.put("133", "Finish charging fail");
		
		return errDesc;
	}
	
	public static String getR2D2ErrorDesc(int errCode){
		String key = ""+errCode;
		String errDesc = "Unknow Error("+key+")";
		
		LinkedHashMap<String,String> errs = getR2D2ErrorDesc();
		if(errs.size()>0 && null != errs.get(key)){
			errDesc = errs.get(key);
		}
		
		return errDesc;
	}
}
