package com.sto.utils;

import java.util.LinkedHashMap;

public class LoginSystem {
	private static String userName = "guest";
	private static String userLoginTime = "";
	private static String confirmationTime = "";
	
	private static void resetParas(){
		userName = "guest";
		userLoginTime = "";
	}
	
	public static String userLogin(String name, String pwd){
		String msg = "OK", key = "", curDate = "", expiryDate = "";
		
		try {
			curDate = TimeUtils.getCurrentDate("yyyyMMdd");
			key = DesUtils.encrypt("ATL"+curDate);
			if(!key.equals(pwd)){
				msg = "Registration failed:User name or password is wrong";
			}else{
				userName = name;
				expiryDate = TimeUtils.getStringDate(TimeUtils.getTime(365), "yyyyMMdd");
				LinkedHashMap<String,String> lic = new LinkedHashMap<String,String>();
				lic.put("REG", userName);
				lic.put("MCK", DesUtils.encrypt(curDate));
				lic.put("MCD", DesUtils.encrypt(expiryDate,curDate));
				lic.put("MC", MathUtils.MD5Encode(PCInfoUtils.getHardDiskSN("C")+PCInfoUtils.getCpuID()));
				XmlUtils.saveLicFile(lic);
			}
		} catch (Exception e) {
			msg = "Registration failed:"+e.getMessage();
		}
		
		if(!"OK".equals(msg)){
			resetParas();
		}else{
			userLoginTime = TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss");
		}
		
		return msg;
	}
	
	public static String userConfirm(String confirmCode){
		String msg = "OK";
		
		try {
			if(!"ef8ll28WVrY=".equals(DesUtils.encrypt(confirmCode))){
				msg = "Confirmation code is wrong";
			}
		} catch (Exception e) {
			msg = "Confirmation failed:"+e.getMessage();
		}
		
		if(!"OK".equals(msg)){
			confirmationTime = "";
		}else{
			confirmationTime = TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss");
		}
		
		return msg;
	}
	
	public static void userLogout(){
		resetParas();
	}
	
	public static String getUserName(){
		return userName;
	}
	
	public static String getLoginTime(){
		return userLoginTime;
	}
	
	public static boolean userHasLoginned(){
		return ("".equals(userLoginTime)?false:true);
	}
	
	public static boolean userHasConfirmationCode(){
		return ("".equals(confirmationTime)?false:true);
	}
}
