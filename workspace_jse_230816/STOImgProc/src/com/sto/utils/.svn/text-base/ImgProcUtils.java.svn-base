package com.sto.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sto.data.ProductSpec;
import com.sto.data.SystemConfig;
import com.sto.gui.ProcResult;
import com.sto.gui.STOImgProc;

public class ImgProcUtils {
	private static final int THREAD_START = -1;
	private static final int THREAD_STOP = 1;
	private static final int THREAD_FORCE_STOP = 2;
	
	private static ExecutorService es = Executors.newCachedThreadPool();
	private static LinkedHashMap<String,Integer> runningThreads = new LinkedHashMap<String,Integer>();
	private static LinkedHashMap<String,LinkedHashMap<String,Object>> imgProcResult = new LinkedHashMap<String,LinkedHashMap<String,Object>>();
	private static LinkedHashMap<String,LinkedHashMap<String,Long>> imgProcRslts = new LinkedHashMap<String,LinkedHashMap<String,Long>>();
	private static LinkedHashMap<String,String> lastImgProcTime = new LinkedHashMap<String,String>();
	private static LinkedHashMap<String,String> firstImgProcTime = new LinkedHashMap<String,String>();
	
	private static LinkedHashMap<String,String> lastImgName = new LinkedHashMap<String,String>();
	
	private static SystemConfig sysConfig = SystemConfig.getInstance();
	private static ProductSpec prodSpec = ProductSpec.getInstance();
	private static STOImgProc imgProcGUI = STOImgProc.getInstance();
	private static int imgMonThreadFlag = 0;
	private static int copyFileThreadFlag = 0;
	
	static class ImageProcessing implements Runnable{
		private String imgRootDir = "";
		private String specifiedSpec = "";
		private boolean monitoringMode = false;
		private int maxProcQty = 600;
		
		public ImageProcessing(String imgFolder, String specID, boolean keepChecking, int procQty){
			imgRootDir = imgFolder;
			monitoringMode = keepChecking;
			specifiedSpec = specID;
			maxProcQty = (procQty<=0)?600:procQty;
		}
		
		@Override
		public void run() {
			if(!imgProcThreadIsRunning(imgRootDir)){
				MyFileUtils.makeFolder(imgRootDir);
				runningThreads.put(imgRootDir, THREAD_START);
				imgProcGUI.setRunBtnEnabled();
				
				try {
					startImgProcessing(imgRootDir,specifiedSpec,monitoringMode,maxProcQty);
				} catch (Exception e) {
				}
				
				runningThreads.put(imgRootDir, THREAD_STOP);
				imgProcGUI.setRunBtnEnabled();
			}
		}
	}
	
	static class ImageMonitoring implements Runnable{
		private String[] imgRootDirs = null;
		private int maxProcQty = 600;
		
		public ImageMonitoring(String[] imgFolders, int procQty){
			imgRootDirs = imgFolders;
			maxProcQty = (procQty<=0)?600:procQty;
		}
		
		@Override
		public void run() {
			String[] sourceDirs = null, imgSuffixes = null;
			int procInterval = sysConfig.getImgProcInterval();
			
			if(null!=imgRootDirs && imgRootDirs.length>0){
				System.out.println("Image Processing Demon Starts");
				imgMonThreadFlag = THREAD_START;
				imgProcGUI.setRunBtnEnabled();
				
				while(THREAD_START==imgMonThreadFlag){
					for(int i=0; i<imgRootDirs.length; i++){
						try {
							sourceDirs = prodSpec.getRawImgFolders(imgRootDirs[i]);
							if(null!=sourceDirs && sourceDirs.length>0){
								imgSuffixes = prodSpec.getRawImgSuffix(imgRootDirs[i]);
//								if(imgRootDirs[i].contains("Layer9")){
//									System.out.print("");
//								}
								procNewImg(sourceDirs,imgRootDirs[i],imgSuffixes[0],maxProcQty);
							}
							
							Thread.sleep(procInterval*1000);
						} catch (InterruptedException e) {
						}
					}
				}
				
				imgMonThreadFlag = THREAD_STOP;
				System.out.println("Image Processing Demon Stops");
			}else{
				System.out.println("Product Spec is not configured yet");
			}
		}
	}
	
	static class CopyFiles implements Runnable{
		//sourceDirs[i], targetDir, cmpDirs, sourceNameEndsWith
		private String[] sourceDirs = null;
		private String targetDir = "";
		private String sourceNameEndsWith = "";
		private int maxCopyQty = 600;
		
		public CopyFiles(String[] scDirs, String tgDir, String scNameEndsWith, int copyQty){
			sourceDirs = scDirs;
			targetDir = tgDir;
			sourceNameEndsWith = scNameEndsWith;
			maxCopyQty = (copyQty<=0)?600:copyQty;
		}
		
		@Override
		public void run() {
			copyFileThreadFlag = THREAD_START;
			if(null!=sourceDirs && sourceDirs.length>0){
				for(int k=0; k<sourceDirs.length; k++){
					ArrayList<String> sourceNames = MyFileUtils.getFileNames(sourceDirs[k], sourceNameEndsWith, maxCopyQty);
					if(null!=sourceNames && sourceNames.size()>0){
						MyFileUtils.makeFolder(targetDir);
						File fileObj = null;
						String[] cmpDirs = new String[]{targetDir+File.separator+"NG_NG",
								targetDir+File.separator+"NG_OK",
								targetDir+File.separator+"OK_NG",
								targetDir+File.separator+"OK_OK"};
						for(int i=0; i<sourceNames.size(); i++){
							fileObj = null;
							//Check whether the source file is in target directory or not
							for(int j=0; j<cmpDirs.length; j++){
								fileObj = MyFileUtils.getFileObject(cmpDirs[j]+File.separator+sourceNames.get(i));
								if(null!=fileObj) break;
							}
							
							if(null==fileObj){
								//Copy the source file into target directory if it's not existing
								try {
									MyFileUtils.copyFile(sourceDirs[k]+File.separator+sourceNames.get(i), targetDir+File.separator+sourceNames.get(i));
								} catch (IOException e) {
								}
							}
						}
					}
				}
			}
			copyFileThreadFlag = THREAD_STOP;
		}
	}
	
	public static void startCopyFileThread(String[] sourceDirs, String targetDir, String nameEndsWith, int copyQty){
		imgProcGUI.refreshMonHeartbeat(2, targetDir);
		if(!copyFileThreadIsRunning()){
			es.execute(new CopyFiles(sourceDirs,targetDir,nameEndsWith,copyQty));
		}
	}
	
	public static void startImgProcDemon(String[] imgRootDirs, int maxProcQty){
		if(null!=imgRootDirs && imgRootDirs.length>0){
			stopImgProcDemon();//Make sure there is only one thread for the monitoring
			es.execute(new ImageMonitoring(imgRootDirs, maxProcQty));
		}
	}
	
	public static void stopImgProcDemon(){
		if(THREAD_START==imgMonThreadFlag || THREAD_FORCE_STOP==imgMonThreadFlag){
			imgMonThreadFlag = THREAD_FORCE_STOP;
			while(true){
				if(THREAD_STOP==imgMonThreadFlag) break;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	public static void startImgProcThreads(String[] imgRootDirs, String[] specificSpec, boolean keepChecking, int maxProcQty){
		if(null!=imgRootDirs && imgRootDirs.length>0){
			String specID = null;
			for(int i=0; i<imgRootDirs.length; i++){
				specID = null;
				if(null!=specificSpec && i<specificSpec.length) specID = specificSpec[i];
				if(!imgProcThreadIsRunning(imgRootDirs[i])){
					es.execute(new ImageProcessing(imgRootDirs[i], specID, keepChecking, maxProcQty));
				}
			}
		}
	}
	
	public static void stopImgProcThread(String imgRootDir){
		if(null!=runningThreads.get(imgRootDir)){
			if(THREAD_STOP!=runningThreads.get(imgRootDir)){
				runningThreads.put(imgRootDir, THREAD_FORCE_STOP);
			}
		}
	}
	
	public static ArrayList<String> getRunningThreads(){
		ArrayList<String> runningThrs = new ArrayList<String>();
		try {
			for(String key:runningThreads.keySet()){
				if(THREAD_START==runningThreads.get(key)) runningThrs.add(key);
			}
		} catch (Exception e) {
		}
		return runningThrs;
	}
	
	public static boolean imgProcThreadIsRunning(String imgRootDir){
		boolean isRunning = false;
		
		if(null!=runningThreads.get(imgRootDir)){
			if(THREAD_START==runningThreads.get(imgRootDir)) isRunning = true;
		}
		
		return isRunning;
	}
	
	public static boolean imgProcDemonIsRunning(){
		boolean isRunning = true;
		if(THREAD_STOP==imgMonThreadFlag || 0==imgMonThreadFlag) isRunning = false;
		return isRunning;
	}
	
	public static boolean copyFileThreadIsRunning(){
		boolean isRunning = true;
		if(THREAD_STOP==copyFileThreadFlag || 0==copyFileThreadFlag) isRunning = false;
		return isRunning;
	}
	
	private static void procNewImg(String[] sourceDirs,String targetDir,String sourceNameEndsWith,int maxProcQty){
		long newFiles = 0;
		if(null!=sourceDirs && sourceDirs.length>0){
			imgProcGUI.refreshMonHeartbeat(0, targetDir);
			for(int i=0; i<sourceDirs.length; i++){
				String[] cmpDirs = new String[]{targetDir+File.separator+"NG_NG",
						targetDir+File.separator+"NG_OK",
						targetDir+File.separator+"OK_NG",
						targetDir+File.separator+"OK_OK"};
				long rtn = MyFileUtils.copyMissedFiles(sourceDirs[i], targetDir, cmpDirs, sourceNameEndsWith, 1000);
				if(rtn>0) newFiles++;
			}
			if(newFiles>0 || MyFileUtils.thereIsFileInDir(targetDir, sourceNameEndsWith)){
				if(!imgProcThreadIsRunning(targetDir)){
					startImgProcThreads(new String[]{targetDir},null,true,maxProcQty);
				}else{
					String lastT = XmlUtils.readProfileString(targetDir);
					long lastProcT = Long.parseLong("".equals(lastT)?"-1":lastT);
					long timeDiff = (System.currentTimeMillis() - lastProcT)/60000;
					if(lastProcT>0 && timeDiff>2) stopImgProcThread(targetDir);
					if(lastProcT<0) XmlUtils.writeProfileString(targetDir, ""+System.currentTimeMillis());
				}
				imgProcGUI.refreshMonHeartbeat(1, targetDir);
			}else{
				//Copy missed handling files
				startCopyFileThread(sourceDirs,targetDir,sourceNameEndsWith,maxProcQty);
			}
		}
	}
	
	private static void setLastImgProcTime(String imgRootDir, String t){
		lastImgProcTime.put(imgRootDir, t);
	}
	
	private static String getLastImgProcTime(String imgRootDir){
		String lastTime = lastImgProcTime.get(imgRootDir);
		
		if(null==lastTime){
			lastTime = TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss");
			setLastImgProcTime(imgRootDir, lastTime);
		}
		
		return lastTime;
	}
	
	private static void setFirstImgProcTime(String imgRootDir, String t){
		firstImgProcTime.put(imgRootDir, t);
	}
	
	private static String getFirstImgProcTime(String imgRootDir){
		String firstTime = firstImgProcTime.get(imgRootDir);
		
		if(null==firstTime){
			firstTime = TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss");
			setFirstImgProcTime(imgRootDir, firstTime);
		}
		
		return firstTime;
	}
	
	private static void startImgProcessing(String imgRootDir, String specificSpec, boolean monitoringMode, int maxProcQty){
		int passCnt = 0, ngCnt = 0;
		double passRate = 0.0, imgProcTime = 0.0, imgStoreT = 0.0;
		String imgKey = "", rsltName = "";
		ProcResult imgProcRslt = ProcResult.getInstance();
		
		LinkedHashMap<String,Object> procRslt = null;
		procRslt = imgProcResult.get(imgRootDir);
		if(null==procRslt){
			procRslt = new LinkedHashMap<String,Object>();
			imgProcResult.put(imgRootDir, procRslt);
		}else{
			procRslt.clear();
			imgProcResult.replace(imgRootDir, procRslt);
		}
		procRslt.put("imgProcDir", imgRootDir);
		
		ImageUtils imageUtils = new ImageUtils();
		imageUtils.setLogEnabled(false);
		clearRsltImgs(imgRootDir);
		
		String[] imgSuffixes = prodSpec.getRawImgSuffix(imgRootDir);
		ArrayList<String> imgFiles = MyFileUtils.getFileNames(imgRootDir, imgSuffixes[0], maxProcQty);
		LinkedHashMap<String,Boolean> results = new LinkedHashMap<String,Boolean>();
		
		if(null!=imgFiles && imgFiles.size()>0){
			for(int i=0; i<imgFiles.size(); i++){
				if(THREAD_FORCE_STOP==runningThreads.get(imgRootDir)) break;
				imgKey = getImageKey(imgRootDir, imgFiles.get(i));
				
				procRslt.put("imgProcSourceName", imgFiles.get(i));
				imgProcGUI.setImgProcInfo(procRslt,true);
				
				if(imageUtils.procImage(imgRootDir, imgFiles.get(i), true, specificSpec)){
					if(null==results.get(imgKey)) results.put(imgKey, true);
					rsltName = imgFiles.get(i)+".OK.png";
					storeImgProcResult(imgRootDir, imgFiles.get(i), true);
				}else{
					results.put(imgKey, false);
					rsltName = imgFiles.get(i)+".NG.png";
					storeImgProcResult(imgRootDir, imgFiles.get(i), false);
				}
				
				imgProcTime = (imgProcTime*i+imageUtils.getImgProcTime())/(i+1);
				imgStoreT = (imgStoreT*i+(System.currentTimeMillis()-imageUtils.getImgStartSavingTime()))/(i+1);
				passCnt = getOKQty(results);
				ngCnt = results.size()-passCnt;
				passRate = (double)passCnt / results.size();
				
				procRslt.put("imgProcRsltName", rsltName);
				procRslt.put("imgProcTime", "ProcT:"+imageUtils.getImgProcTime()+" ms/("+(int)imgProcTime+","+(int)imgStoreT+")");
				procRslt.put("imgProcNGInfo", "NG:"+ngCnt+"/"+results.size()+"("+(MathUtils.dblFormat((1.0-passRate)*100, 2))+"%)");
				procRslt.put("imgProcOKInfo", "OK:"+passCnt+"/"+results.size()+"("+(MathUtils.dblFormat(passRate*100, 2))+"%)");
				procRslt.put("imgProcProgress", (i+1)*100/imgFiles.size());
				imgProcGUI.setImgProcInfo(procRslt,false);
				XmlUtils.writeProfileString(imgRootDir, ""+System.currentTimeMillis());
				imgProcRslt.refreshProcResult(imgRootDir, imgProcResultToStr(imgRootDir,getImgProcResult(imgRootDir,0),getImgProcResult(imgRootDir,1)));
			}
			
			moveAllFiles(imageUtils,imgRootDir,imgFiles,monitoringMode,results);
			imgProcRslt.refreshProcResult(imgRootDir, summarizeImgProcResult(imgRootDir));
		}
	}
	
	private static void clearRsltImgs(String imgRootDir){
		String[] imgSuffixes = prodSpec.getRawImgSuffix(imgRootDir);
		ArrayList<String> imgFiles = MyFileUtils.getFileNames(imgRootDir, imgSuffixes[0], 0L);
		String fName = "";
		if(null!=imgFiles && imgFiles.size()>0){
			for(int i=0; i<imgFiles.size(); i++){
				fName = imgFiles.get(i);
				MyFileUtils.deleteFile(imgRootDir+File.separator+fName+".OK.png");
				MyFileUtils.deleteFile(imgRootDir+File.separator+fName+".NG.png");
				MyFileUtils.deleteFile(imgRootDir+File.separator+"OK_RSLT"+File.separator+fName);
				MyFileUtils.deleteFile(imgRootDir+File.separator+"NG_RSLT"+File.separator+fName);
				MyFileUtils.deleteFile(imgRootDir+File.separator+"OK_RSLT"+File.separator+fName+".OK.png");
				MyFileUtils.deleteFile(imgRootDir+File.separator+"NG_RSLT"+File.separator+fName+".NG.png");
				
				MyFileUtils.deleteFile(imgRootDir+File.separator+"OK_OK"+File.separator+fName);
				MyFileUtils.deleteFile(imgRootDir+File.separator+"OK_NG"+File.separator+fName);
				MyFileUtils.deleteFile(imgRootDir+File.separator+"OK_OK"+File.separator+fName+".OK.png");
				MyFileUtils.deleteFile(imgRootDir+File.separator+"OK_NG"+File.separator+fName+".NG.png");
				MyFileUtils.deleteFile(imgRootDir+File.separator+"NG_OK"+File.separator+fName);
				MyFileUtils.deleteFile(imgRootDir+File.separator+"NG_NG"+File.separator+fName);
				MyFileUtils.deleteFile(imgRootDir+File.separator+"NG_OK"+File.separator+fName+".OK.png");
				MyFileUtils.deleteFile(imgRootDir+File.separator+"NG_NG"+File.separator+fName+".NG.png");
			}
		}
	}
	
	private static boolean resultPassed(LinkedHashMap<String,Boolean> results, String imgKey){
		boolean passed = false;
		
		try {
			if(null!=results && results.get(imgKey)) passed = true;
		} catch (Exception e) {
		}
		
		return passed;
	}
	
	private static void moveAllFiles(ImageUtils imageUtils, String imgRootDir, ArrayList<String> fileNames, boolean moveFile, LinkedHashMap<String,Boolean> results){
		if(null!=fileNames){
			String targetFolder = "", rsltImgName = "", imgKey = "";
			String[] imgSuffixes = prodSpec.getRawImgSuffix(imgRootDir);
			String[] procRsltStr = null;
			boolean rsltPass = false;
			int rsltFlag = 0;
			for(int i=0; i<fileNames.size(); i++){
				rsltFlag = 0;
				if(fileNames.get(i).endsWith(imgSuffixes[1])){
					rsltFlag = 1;
				}else if(fileNames.get(i).endsWith(imgSuffixes[2])){
					rsltFlag = -1;
				}
				imgKey = getImageKey(imgRootDir, fileNames.get(i));
				
				if(moveFile || 0!=rsltFlag){
					rsltPass = resultPassed(results,imgKey);
					//Move images to specific folder
					if(0==rsltFlag){
						if(rsltPass){
							targetFolder = imgRootDir + File.separator + "OK_RSLT";
						}else{
							targetFolder = imgRootDir + File.separator + "NG_RSLT";
						}
					}else{
						if(-1==rsltFlag){
							if(rsltPass){
								targetFolder = imgRootDir + File.separator + "NG_OK";
							}else{
								targetFolder = imgRootDir + File.separator + "NG_NG";
							}
						}else if(1==rsltFlag){
							if(rsltPass){
								targetFolder = imgRootDir + File.separator + "OK_OK";
							}else{
								targetFolder = imgRootDir + File.separator + "OK_NG";
							}
						}
					}
					
					rsltImgName = "";
					if(MyFileUtils.fileExists(imgRootDir+File.separator+fileNames.get(i)+".OK.png")){
						rsltImgName = fileNames.get(i)+".OK.png";
					}else if(MyFileUtils.fileExists(imgRootDir+File.separator+fileNames.get(i)+".NG.png")){
						rsltImgName = fileNames.get(i)+".NG.png";
					}
					if(!"".equals(rsltImgName)){
						MyFileUtils.moveFile(imgRootDir+File.separator+fileNames.get(i), targetFolder);
						MyFileUtils.moveFile(imgRootDir+File.separator+rsltImgName, targetFolder);
					}
					
					//Save image process data
					procRsltStr = imageUtils.getImgProcRsltData(fileNames.get(i));
					procRsltStr[0] = "SN,oriResult,newResult,"+procRsltStr[0];
					procRsltStr[1] = fileNames.get(i)+","+(1==rsltFlag?"PASS":(-1==rsltFlag?"FAIL":"UNKOWN"))+","+(rsltPass?"PASS":"FAIL")+","+procRsltStr[1];
					LogUtils.imgProcLog(imgRootDir, prodSpec.getSpecName(imgRootDir)+".txt", procRsltStr[0]+"\r\n"+procRsltStr[1]);
				}
				imageUtils.clearImgProcRsltData(fileNames.get(i));
			}
		}
	}
	
	private static int getImageProcOriRslt(String imgRootDir, String imgFileName){
		int rsltFlag = 0;
		
		String[] imgSuffixes = prodSpec.getRawImgSuffix(imgRootDir);
		rsltFlag = 0;
		if(imgFileName.endsWith(imgSuffixes[1])){//OK
			rsltFlag = 1;
		}else if(imgFileName.endsWith(imgSuffixes[2])){//NG
			rsltFlag = -1;
		}
		
		return rsltFlag;
	}
	
	private static String getImageKey(String imgRootDir, String imgFileName){
		String imgKey = "";
		int rsltFlag = getImageProcOriRslt(imgRootDir, imgFileName);
		String[] imgSuffixes = prodSpec.getRawImgSuffix(imgRootDir);
		
		if(0==rsltFlag){
			imgKey = imgFileName.replace("_1"+imgSuffixes[0], "").replace("_2"+imgSuffixes[0], "");
			imgKey = imgKey.replace("_3"+imgSuffixes[0], "").replace("_4"+imgSuffixes[0], "");
		}else if(1==rsltFlag){//OK
			imgKey = imgFileName.replace("_1"+imgSuffixes[1], "").replace("_2"+imgSuffixes[1], "");
			imgKey = imgKey.replace("_3"+imgSuffixes[1], "").replace("_4"+imgSuffixes[1], "");
		}else if(-1==rsltFlag){//NG
			imgKey = imgFileName.replace("_1"+imgSuffixes[2], "").replace("_2"+imgSuffixes[2], "");
			imgKey = imgKey.replace("_3"+imgSuffixes[2], "").replace("_4"+imgSuffixes[2], "");
		}
		
		return imgKey;
	}
	
	private static int getOKQty(LinkedHashMap<String,Boolean> results){
		int okQty = 0;
		
		for(String key:results.keySet()){
			if(results.get(key)) okQty++;
		}
		
		return okQty;
	}
	
	private static String imgProcResultToStr(String imgProcDir, LinkedHashMap<String,Long> imgsQty,LinkedHashMap<String,Long> prodsQty){
		ProductSpec prodSpec = ProductSpec.getInstance();
		String procResult = "";
		long prodQty = 0, imgQty = 0;
		double passRate = 0.0, failRate = 0.0, overkill = 0.0, underkill = 0.0;
		double oriPassRate = 0.0, oriFailRate = 0.0;
		
		imgQty = imgsQty.get("NG_NG")+imgsQty.get("NG_OK")+imgsQty.get("OK_NG")+imgsQty.get("OK_OK");
		prodQty = prodsQty.get("NG_NG")+prodsQty.get("NG_OK")+prodsQty.get("OK_NG")+prodsQty.get("OK_OK");
		if(prodQty>0){
			passRate = (double)(prodsQty.get("NG_OK")+prodsQty.get("OK_OK"))/prodQty;
			failRate = 1.0 - passRate;
			overkill = (double)prodsQty.get("NG_OK")/prodQty;
			underkill = (double)prodsQty.get("OK_NG")/prodQty;
			oriPassRate = (double)(prodsQty.get("OK_NG")+prodsQty.get("OK_OK"))/prodQty;
			oriFailRate = 1.0 - oriPassRate;
		}
		
		procResult = prodSpec.getSpecName(imgProcDir)+"("+imgProcDir+") - "+imgQty+" images";
		procResult += "\r\nOri OK:"+MathUtils.roundFloat((float)oriPassRate*100, 2)+"%("+(prodsQty.get("OK_NG")+prodsQty.get("OK_OK"))+"/"+prodQty+") / NG:"+MathUtils.roundFloat((float)oriFailRate*100, 2)+"%("+(prodsQty.get("NG_NG")+prodsQty.get("NG_OK"))+"/"+prodQty+")";
		procResult += "\r\nNew OK:"+MathUtils.roundFloat((float)passRate*100, 2)+"%("+(prodsQty.get("NG_OK")+prodsQty.get("OK_OK"))+"/"+prodQty+") / NG:"+MathUtils.roundFloat((float)failRate*100, 2)+"%("+(prodsQty.get("NG_NG")+prodsQty.get("OK_NG"))+"/"+prodQty+")";
		procResult += "\r\nNG_OK:"+MathUtils.roundFloat((float)overkill*100, 2)+"%("+prodsQty.get("NG_OK")+"/"+prodQty+") / OK_NG:"+MathUtils.roundFloat((float)underkill*100, 2)+"%("+prodsQty.get("OK_NG")+"/"+prodQty+")";
		procResult += "\r\n====== "+getFirstImgProcTime(imgProcDir)+" ~ "+getLastImgProcTime(imgProcDir)+" ======";
		
		return procResult;
	}
	
	private static LinkedHashMap<String,Long> getImgProcResult(String imgRootDir, int dataFlag){
		LinkedHashMap<String,Long> imgsQty = imgProcRslts.get(imgRootDir+"_imgsQty");
		LinkedHashMap<String,Long> prodsQty = imgProcRslts.get(imgRootDir+"_prodsQty");
		
		if(null==imgsQty){
			imgsQty = new LinkedHashMap<String,Long>();
			imgsQty.put("NG_NG", 0L); imgsQty.put("OK_NG", 0L);
			imgsQty.put("NG_OK", 0L); imgsQty.put("OK_OK", 0L);
		}
		if(null==prodsQty){
			prodsQty = new LinkedHashMap<String,Long>();
			prodsQty.put("NG_NG", 0L); prodsQty.put("OK_NG", 0L);
			prodsQty.put("NG_OK", 0L); prodsQty.put("OK_OK", 0L);
			lastImgName.put(imgRootDir, "");
		}
		
		if(0==dataFlag){
			return imgsQty;
		}else{
			return prodsQty;
		}
	}
	
	private static void setImgProcResult(String imgRootDir,LinkedHashMap<String,Long> imgsQty,LinkedHashMap<String,Long> prodsQty){
		imgProcRslts.put(imgRootDir+"_imgsQty", imgsQty);
		imgProcRslts.put(imgRootDir+"_prodsQty", prodsQty);
	}
	
	private static void storeImgProcResult(String imgRootDir, String imgFileName, boolean imgOK){
		String imgKey = "";
		LinkedHashMap<String,Long> imgsQty = getImgProcResult(imgRootDir,0);
		LinkedHashMap<String,Long> prodsQty = getImgProcResult(imgRootDir,1);
		
		imgKey = getImageKey(imgRootDir, imgFileName);
		if(-1==getImageProcOriRslt(imgRootDir, imgFileName)){
			if(imgOK){//NG_OK
				imgsQty.put("NG_OK", imgsQty.get("NG_OK")+1);
				if(!imgKey.equals(lastImgName.get(imgRootDir))) prodsQty.put("NG_OK", prodsQty.get("NG_OK")+1);
			}else{//NG_NG
				imgsQty.put("NG_NG", imgsQty.get("NG_NG")+1);
				if(!imgKey.equals(lastImgName.get(imgRootDir))) prodsQty.put("NG_NG", prodsQty.get("NG_NG")+1);
			}
		}else{
			if(imgOK){//OK_OK
				imgsQty.put("OK_OK", imgsQty.get("OK_OK")+1);
				if(!imgKey.equals(lastImgName.get(imgRootDir))) prodsQty.put("OK_OK", prodsQty.get("OK_OK")+1);
			}else{//OK_NG
				imgsQty.put("OK_NG", imgsQty.get("OK_NG")+1);
				if(!imgKey.equals(lastImgName.get(imgRootDir))) prodsQty.put("OK_NG", prodsQty.get("OK_NG")+1);
			}
		}
		lastImgName.put(imgRootDir, imgKey);
		
		setLastImgProcTime(imgRootDir,TimeUtils.getStringDate(TimeUtils.getTime(TimeUtils.getSecondsOffsetTillNow(MyFileUtils.getFileLastModifiedTime(imgRootDir+File.separator+imgFileName))), "yyyy-MM-dd HH:mm:ss"));
		setImgProcResult(imgRootDir,imgsQty,prodsQty);
	}
	
	public static String summarizeImgProcResult(String imgProcDir){
		long minModifiedT = System.currentTimeMillis(), maxModifiedT = 0, curModifiedT = 0;
		LinkedHashMap<String,Long> imgsQty = new LinkedHashMap<String,Long>();
		LinkedHashMap<String,Long> prodsQty = new LinkedHashMap<String,Long>();
		
		imgsQty.put("NG_NG", 0L); prodsQty.put("NG_NG", 0L);
		imgsQty.put("NG_OK", 0L); prodsQty.put("NG_OK", 0L);
		imgsQty.put("OK_NG", 0L); prodsQty.put("OK_NG", 0L);
		imgsQty.put("OK_OK", 0L); prodsQty.put("OK_OK", 0L);
		
		if(null!=imgProcDir && !"".equals(imgProcDir.trim())){
			String[] rsltDirs = new String[]{imgProcDir+File.separator+"NG_NG",
					imgProcDir+File.separator+"NG_OK",
					imgProcDir+File.separator+"OK_NG",
					imgProcDir+File.separator+"OK_OK"};
			String lastKey = "", currKey = "";
			long prodQty = 0;
			for(int j=0; j<rsltDirs.length; j++){
				ArrayList<String> fileNames = MyFileUtils.getFileNames(rsltDirs[j], ".png", 0L);
				if(null!=fileNames && fileNames.size()>0){
					lastKey = ""; prodQty = 0;
					for(int k=0; k<fileNames.size(); k++){
						currKey = getImageKey(imgProcDir, fileNames.get(k).replace(".OK.png", "").replace(".NG.png", ""));
						if(!lastKey.equals(currKey)){
							prodQty++;
							lastKey = currKey;
						}
						curModifiedT = MyFileUtils.getFileLastModifiedTime(rsltDirs[j]+File.separator+fileNames.get(k).replace(".OK.png", "").replace(".NG.png", ""));
						if(minModifiedT>curModifiedT) minModifiedT = curModifiedT;
						if(maxModifiedT<curModifiedT) maxModifiedT = curModifiedT;
					}
					switch(j){
					case 0://NG_NG
						imgsQty.put("NG_NG", (long)fileNames.size());
						prodsQty.put("NG_NG", prodQty);
						break;
					case 1://NG_OK
						imgsQty.put("NG_OK", (long)fileNames.size());
						prodsQty.put("NG_OK", prodQty);
						break;
					case 2://OK_NG
						imgsQty.put("OK_NG", (long)fileNames.size());
						prodsQty.put("OK_NG", prodQty);
						break;
					case 3://OK_OK
						imgsQty.put("OK_OK", (long)fileNames.size());
						prodsQty.put("OK_OK", prodQty);
						break;
					}
				}
			}
		}
		setImgProcResult(imgProcDir,imgsQty,prodsQty);
		if(minModifiedT>0) setFirstImgProcTime(imgProcDir,TimeUtils.getStringDate(TimeUtils.getTime(TimeUtils.getSecondsOffsetTillNow(minModifiedT)), "yyyy-MM-dd HH:mm:ss"));
		if(maxModifiedT>0) setLastImgProcTime(imgProcDir,TimeUtils.getStringDate(TimeUtils.getTime(TimeUtils.getSecondsOffsetTillNow(maxModifiedT)), "yyyy-MM-dd HH:mm:ss"));
		
		return imgProcResultToStr(imgProcDir,imgsQty,prodsQty);
	}
}
