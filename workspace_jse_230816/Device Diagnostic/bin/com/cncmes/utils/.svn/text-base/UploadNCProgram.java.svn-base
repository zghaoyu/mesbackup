package com.cncmes.utils;

import java.io.File;
import java.util.LinkedHashMap;

import com.cncmes.base.CNC;
import com.cncmes.base.CncWebAPIItems;
import com.cncmes.base.SpecItems;
import com.cncmes.base.TaskItems;
import com.cncmes.data.CncData;
import com.cncmes.data.CncWebAPI;
import com.cncmes.data.SystemConfig;
import com.cncmes.data.TaskData;
import com.cncmes.data.WorkpieceData;

public class UploadNCProgram {
	private static UploadNCProgram uploadNCProgram = new UploadNCProgram();
	private static String ncProgramsRootDir = "";
	private UploadNCProgram(){}
	public static UploadNCProgram getInstance(){
		return uploadNCProgram;
	}
	
	private static void getCommonSettings() {
		try {
			SystemConfig sysCfg = SystemConfig.getInstance();
			LinkedHashMap<String,Object> config = sysCfg.getCommonCfg();
			ncProgramsRootDir = (String)config.get("NCProgramsRootDir");
		} catch (Exception e) {
			LogUtils.errorLog("UploadNCProgram fails to load system config:"+e.getMessage());
		}
	}
	
	public Thread uploadSubProgramsThread(CNC cncCtrl, String taskID, String cncIP){
		Thread thr = new Thread(new UploadSubPrograms(cncCtrl, taskID, cncIP));
		thr.start();
		return thr;
	}
	
	public Thread uploadMainProgramThread(CNC cncCtrl, String taskID, String cncIP){
		Thread thr = new Thread(new UploadMainProgram(cncCtrl, taskID, cncIP));
		thr.start();
		return thr;
	}
	
	/**
	 * 
	 * @param cncCtrl controller of CNC
	 * @param taskID the task ID associated to the sub programs uploading
	 * @param cncIP the CNC to receive the sub programs
	 * @return true while sub programs for all workpieces are uploaded successfully
	 * sequence of all uploaded sub programs and workpiece IDs is matching(apple to apple),
	 * 				before calling this function, all workpieces indicated by workpieceIDs have put into the machine and
	 * 				all workpieces' working zones were recorded in the memory(CncData)
	 */
	public static boolean uploadSubPrograms(CNC cncCtrl, String taskID, String cncIP){
		boolean success = false;
		
		CncWebAPI cncWebAPI = CncWebAPI.getInstance();
		TaskData taskData = TaskData.getInstance();
		CncData cncData = CncData.getInstance();
		WorkpieceData wpData = WorkpieceData.getInstance();
		
		getCommonSettings();
		LinkedHashMap<Integer,String> workpieceIDs = cncData.getWorkpieceIDs(cncIP);
		String cncModel = cncData.getCncModel(cncIP);
		
		String sp = LogUtils.separator;
		String logContPre = sp + taskID + sp + cncIP + sp + cncModel + sp;
		String logFile = "UploadProgram_" + TimeUtils.getCurrentDate("yyyyMMdd") + ".log";
		
		if(workpieceIDs.isEmpty()){
			LogUtils.operationLog(logFile, "Upload Sub Programs Failed:No workpieces in CNC "+cncIP);
			return false;
		}
		
		//Clear sub programs before uploading
		taskData.setData(taskID, TaskItems.SUBPROGRAMS, "");
		
		//Get NC Sub and Main program names
		LinkedHashMap<CncWebAPIItems, String> cncWebAPICommonCfg = cncWebAPI.getCommonCfg(cncModel);
		String[] ncProgramName = new String[7];
		ncProgramName[0] = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGMAIN);
		ncProgramName[1] = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGSUB1);
		ncProgramName[2] = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGSUB2);
		ncProgramName[3] = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGSUB3);
		ncProgramName[4] = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGSUB4);
		ncProgramName[5] = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGSUB5);
		ncProgramName[6] = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGSUB6);
		
		//Upload sub programs
		LogUtils.operationLog(logFile, "Upload Sub Programs Started");
		int curProc = 0, ncProgramIndex = 0;
		String programFileName = "",subPrograms = "",programID = "",mesCode = "";
		String programFolder = "", programPath = "", wpID = "";
		
		File file = null;
		for(int workZone:workpieceIDs.keySet()){
			ncProgramIndex = workZone;
			wpID = workpieceIDs.get(workZone);
			LinkedHashMap<SpecItems,Object> spec = wpData.getNextProcInfo(wpID);
			curProc = wpData.getNextProcNo(wpID, spec);
			programFileName = wpData.getNextProcProgram(wpID, cncModel, spec, curProc);
			mesCode = DataUtils.getMescodeByWorkpieceID(wpID);
			programFolder = ncProgramsRootDir + File.separator + mesCode + File.separator + cncModel + File.separator;
			programID = "NULL";
			//Get programID according to the spec of each workpiece
			if(ncProgramIndex < 7){
				file = new File(programFolder + programFileName);
				if(!file.exists()){
					LogUtils.operationLog(logFile, "NG"+logContPre+wpID+sp+ncProgramName[ncProgramIndex]+sp+programFolder+programFileName+sp+"File not found");
				}else{
					programID = ncProgramName[ncProgramIndex];
				}
			}else{
				LogUtils.operationLog(logFile, "NG"+logContPre+wpID+sp+"null"+sp+programFolder+sp+"Index out of limit");
				LogUtils.errorLog("UploadNCProgram uploadSubPrograms failed:ncProgramIndex "+ncProgramIndex+" is out of limit");
			}
			if("NULL".equals(programID)) break;
			
			//Upload sub program
			if(cncCtrl.uploadSubProgram(cncIP, programID, programFolder+programFileName)){
				LogUtils.operationLog(logFile, "OK"+logContPre+"Zone_"+workZone+":"+wpID+sp+ncProgramName[ncProgramIndex]+sp+programFolder+programFileName);
				if("".equals(subPrograms)){
					subPrograms = programID;
					programPath = programFolder + programFileName;
				}else{
					subPrograms += ";" + programID;
					programPath += ";" + programFolder + programFileName;
				}
			}else{
				if("".equals(subPrograms)){
					subPrograms = "NULL";
				}else{
					subPrograms += ";NULL";
				}
				LogUtils.operationLog(logFile, "NG"+logContPre+wpID+sp+ncProgramName[ncProgramIndex]+sp+programFolder+programFileName+sp+"Command Failed");
				LogUtils.errorLog("UploadNCProgram uploadSubPrograms("+programID+","+programFolder+programFileName+") failed:check command log for the details");
				break;
			}
		}
		if("".equals(subPrograms)){
			subPrograms = "NULL";
			LogUtils.operationLog(logFile, "Upload Sub Programs Failed");
		}else{
			LogUtils.operationLog(logFile, "Upload Sub Programs Finished");
		}
		
		//Check all uploaded sub programs
		if(!"".equals(programPath) && subPrograms.indexOf("NULL") < 0){
			String[] localPath = programPath.split(";");
			String[] ncPrograms = subPrograms.split(";");
			LogUtils.operationLog(logFile, "Check Sub Programs Started");
			for(int i=0; i<ncPrograms.length; i++){
				String nc_md5 = cncCtrl.downloadSubProgram(cncIP, ncPrograms[i]);
				String local_md5 = cncWebAPI.getFileContentMD5(localPath[i]);
				if(!local_md5.equals(nc_md5)){
					subPrograms = "NULL";
					LogUtils.operationLog(logFile, "NG"+logContPre+"MD5_VALIDATION_ERROR"+sp+ncPrograms[i]+sp+localPath[i]+sp+"MD5 is not matched");
					LogUtils.errorLog("UploadNCProgram uploadSubPrograms failed:MD5 is not matched");
					break;
				}
			}
			LogUtils.operationLog(logFile, "Check Sub Programs Finished");
		}
		
		//Sequence of subPrograms is matching to which of workpieceIDs
		taskData.setData(taskID, TaskItems.SUBPROGRAMS, subPrograms);
		if(!"".equals(programPath) && subPrograms.indexOf("NULL") < 0) success = true;
		
		return success;
	}
	
	/**
	 * 
	 * @param cncCtrl controller of CNC
	 * @param taskID task associated to the main program uploading
	 * @param cncIP the CNC ready for main program uploading
	 * @param workpieceIDs the workpieces aleady put in the machining
	 * @return true while the main program is uploaded successfully
	 * before calling this function,sub programs of all workpieces should be uploaded already,
	 * 				and content of the main program is generated dynamically according to the sub programs name and working zones
	 * 				of all workpieces in the machine
	 */
	public static boolean uploadMainProgram(CNC cncCtrl, String taskID, String cncIP){
		boolean success = false;
		CncWebAPI cncWebAPI = CncWebAPI.getInstance();
		TaskData taskData = TaskData.getInstance();
		CncData cncData = CncData.getInstance();
		
		String cncModel = cncData.getCncModel(cncIP);
		LinkedHashMap<Integer,String> workpieceIDs = cncData.getWorkpieceIDs(cncIP);
		
		String sp = LogUtils.separator;
		String logContPre = sp + taskID + sp + cncIP + sp + cncModel + sp;
		String logFile = "UploadProgram_" + TimeUtils.getCurrentDate("yyyyMMdd") + ".log";
		
		if(workpieceIDs.isEmpty()){
			LogUtils.operationLog(logFile, "Upload Main Programs Failed:Length of workpieceIDs is zero");
			return false;
		}
		
		//Clear main program before uploading
		taskData.setData(taskID, TaskItems.MAINPROGRAM, "");
		
		//Get NC main program name
		LinkedHashMap<CncWebAPIItems, String> cncWebAPICommonCfg = cncWebAPI.getCommonCfg(cncModel);
		String programID = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGMAIN);
		
		//Check sub programs uploading status
		String subProgs = "";
		int subProgramUploadTimeLimit = 60; //seconds
		int retryCount = subProgramUploadTimeLimit;
		while(true){
			retryCount--;
			subProgs = (String) taskData.getData(taskID).get(TaskItems.SUBPROGRAMS);
			if(!"".equals(subProgs)) break;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			if(retryCount <= 0) break;
		}
		
		LogUtils.operationLog(logFile, "Upload Main Programs Started");
		
		//Get sub programs name and the working zones
		//Note:sequence of workpieceIDs and subPrograms is the same
		String subPrograms = (String) taskData.getData(taskID).get(TaskItems.SUBPROGRAMS);
		String ids = "", zones = "";
		for(int zone:workpieceIDs.keySet()){
			if("".equals(ids)){
				ids = workpieceIDs.get(zone);
				zones = "" + zone;
			}else{
				ids += ";" + workpieceIDs.get(zone);
				zones += ";" + zone;
			}
		}
		
		//Generate main program content
		//One important condition:sequence of workpieceIDs,subPrograms and workzones is the same
		String mainProgramContent = cncCtrl.generateMainProgramContent(cncIP, ids, subPrograms, zones);
		
		//Upload main program
		if(!"".equals(mainProgramContent) && cncCtrl.uploadMainProgram(cncIP, programID, mainProgramContent.replace("O"+programID, ""))){
			//Check main program
			String nc_md5 = cncCtrl.downloadMainProgram(cncIP, programID);
			String md5Content = mainProgramContent.replace("#r#n", "");
			String local_md5 = MathUtils.MD5Encode(md5Content);
			if(local_md5.equals(nc_md5)){
				taskData.setData(taskID, TaskItems.MAINPROGRAM, programID);
				LogUtils.operationLog(logFile, "OK"+logContPre+"MAIN_PROGRAM_UPLOAD_OK"+sp+programID+sp+mainProgramContent.replace("#r#n", " "));
				success = true;
			}else{
				LogUtils.operationLog(logFile, "NG"+logContPre+"MAIN_PROGRAM_MD5CHK_NG"+sp+programID+sp+mainProgramContent.replace("#r#n", " "));
				LogUtils.errorLog("GN_MD5_RAW:"+subPrograms+"/"+zones);
				LogUtils.errorLog("GN_MD5_STR:"+md5Content);
				LogUtils.errorLog("UploadNCProgram uploadMainProgram failed:MD5 is not matched");
			}
		}else{
			if("".equals(mainProgramContent)){
				LogUtils.operationLog(logFile, "NG"+logContPre+"MAIN_PROGRAM_CONT_NULL"+sp+programID+sp+mainProgramContent);
			}else{
				LogUtils.operationLog(logFile, "NG"+logContPre+"MAIN_PROGRAM_UPLOAD_NG"+sp+programID+sp+mainProgramContent.replace("#r#n", " "));
			}
		}
		LogUtils.operationLog(logFile, "Upload Main Programs Finished");
		
		return success;
	}
	
	class UploadSubPrograms implements Runnable{
		private CNC cncCtrl;
		private String cncIP;
		private String taskID;
		
		public UploadSubPrograms(CNC cncCtrl, String taskID, String cncIP){
			this.cncCtrl = cncCtrl;
			this.taskID = taskID;
			this.cncIP = cncIP;
		}
		
		@Override
		public void run() {
			uploadSubPrograms(cncCtrl, taskID, cncIP);
		}
	}
	
	class UploadMainProgram implements Runnable{
		private CNC cncCtrl;
		private String cncIP;
		private String taskID;
		
		public UploadMainProgram(CNC cncCtrl, String taskID, String cncIP){
			this.cncCtrl = cncCtrl;
			this.taskID = taskID;
			this.cncIP = cncIP;
		}
		
		@Override
		public void run() {
			uploadMainProgram(cncCtrl, taskID, cncIP);
		}
	}
}
