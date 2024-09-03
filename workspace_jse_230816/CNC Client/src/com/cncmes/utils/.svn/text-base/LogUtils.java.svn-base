package com.cncmes.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class LogUtils {
	public static final String separator = "@";
	private static boolean logEnabled;
	private static boolean debugLogEnabled;
	
	private static String getLogFolder(){
		String logFolder = System.getProperty("user.dir") + File.separator + "Log";
		File file = new File(logFolder);
		if(!file.exists()) file.mkdirs();
		
		return logFolder;
	}
	
	public static String getOperationLogFileName(String machineIP, String machineModel){
		String logFile = TimeUtils.getCurrentDate("yyyyMMdd")+"_"+machineIP+"_"+machineModel+".log";
		return logFile;
	}
	
	public static boolean getEnabledFlag(){
		return logEnabled;
	}
	
	public static void setEnabledFlag(boolean enabled){
		logEnabled = enabled;
	}
	
	public static boolean getDebugLogFlag(){
		return debugLogEnabled;
	}
	
	public static void setDebugLogFlag(boolean enabled){
		debugLogEnabled = enabled;
	}
	
	public static void clearLog(boolean forced){
		File temp = null;
		double diffTime = 0;
		String logFolder = getLogFolder();
		
		String[] fileList = (new File(logFolder)).list();
		if(fileList.length > 0){
			for(int i=0; i<fileList.length; i++){
				temp = new File(logFolder + File.separator + fileList[i]);
				if(temp.isFile()){
					diffTime = (System.currentTimeMillis() - temp.lastModified())/1000/3600/24;
					if(diffTime > 90 || forced) temp.delete();
				}
			}
		}
	}
	
	public static boolean operationLog(String fileName, String logContent){
		boolean ok = true;
		if(!logEnabled) return logEnabled;
		
		String localPath = getLogFolder() + File.separator + fileName;
		File file = new File(localPath);
		try {
			logContent = TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss") + separator + System.currentTimeMillis() + separator + logContent;
			PrintStream ps = new PrintStream(new FileOutputStream(file, true));
			ps.append(logContent);
			ps.println();
			ps.close();
		} catch (FileNotFoundException e) {
			ok = false;
		}
		
		return ok;
	}
	
	public static boolean machiningDataLog(String fileName, String logContent){
		boolean ok = true;
		if(!logEnabled) return logEnabled;
		
		String localPath = getLogFolder() + File.separator + fileName;
		File file = new File(localPath);
		try {
			PrintStream ps = new PrintStream(new FileOutputStream(file, true));
			ps.append(logContent);
			ps.println();
			ps.close();
		} catch (FileNotFoundException e) {
			ok = false;
		}
		
		return ok;
	}
	
	public static boolean errorLog(String logContent){
		boolean ok = true;
		if(!logEnabled) return logEnabled;
		
		String localPath = getLogFolder() + File.separator + "Error_"+TimeUtils.getCurrentDate("yyyyMMdd")+".log";
		File file = new File(localPath);
		try {
			logContent = TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss") + separator + System.currentTimeMillis() + separator + logContent;
			PrintStream ps = new PrintStream(new FileOutputStream(file, true));
			ps.append(logContent);
			ps.println();
			ps.close();
		} catch (FileNotFoundException e) {
			ok = false;
		}
		
		return ok;
	}
	
	public static boolean socketLog(String logContent){
		boolean ok = true;
		if(!logEnabled) return logEnabled;
		
		String localPath = getLogFolder() + File.separator + "Socket_"+TimeUtils.getCurrentDate("yyyyMMdd")+".log";
		File file = new File(localPath);
		try {
			logContent = TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss") + separator + System.currentTimeMillis() + separator + logContent;
			PrintStream ps = new PrintStream(new FileOutputStream(file, true));
			ps.append(logContent);
			ps.println();
			ps.close();
		} catch (FileNotFoundException e) {
			ok = false;
		}
		
		return ok;
	}
	
	public static boolean debugLog(String fileNamePrefix, String logContent){
		boolean ok = true;
		if(!debugLogEnabled) return debugLogEnabled;
		
		String localPath = getLogFolder() + File.separator + fileNamePrefix+TimeUtils.getCurrentDate("yyyyMMdd")+".log";
		File file = new File(localPath);
		try {
			logContent = TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss") + separator + System.currentTimeMillis() + separator + logContent;
			PrintStream ps = new PrintStream(new FileOutputStream(file, true));
			ps.append(logContent);
			ps.println();
			ps.close();
		} catch (FileNotFoundException e) {
			ok = false;
		}
		
		return ok;
	}
	
	public static boolean commandLog(String deviceID, String logContent){
		boolean ok = true;
		if(!logEnabled) return logEnabled;
		
		String localPath = getLogFolder() + File.separator + "Command_"+deviceID+"_"+TimeUtils.getCurrentDate("yyyyMMdd")+".log";
		File file = new File(localPath);
		try {
			logContent = TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss") + separator + System.currentTimeMillis() + separator + logContent;
			PrintStream ps = new PrintStream(new FileOutputStream(file, true));
			ps.append(logContent);
			ps.println();
			ps.close();
		} catch (FileNotFoundException e) {
			ok = false;
		}
		
		return ok;
	}
	
	public static boolean rawLog(String fileNamePrefix, String logContent){
		boolean ok = true;
		
		String localPath = getLogFolder() + File.separator + fileNamePrefix+TimeUtils.getCurrentDate("yyyyMMdd")+".log";
		File file = new File(localPath);
		try {
			PrintStream ps = new PrintStream(new FileOutputStream(file, true));
			ps.append(logContent);
			ps.println();
			ps.close();
		} catch (FileNotFoundException e) {
			ok = false;
		}
		
		return ok;
	}
}
