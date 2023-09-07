package com.cncmes.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class MyFileUtils {
	private static String getFileFullPath(String folderPath, String fileName){
		String fileFullPath = "";
		if(folderPath.endsWith(File.separator)){
			fileFullPath = folderPath + fileName;
		}else{
			fileFullPath = folderPath + File.separator + fileName;
		}
		return fileFullPath;
	}
	
	public static boolean copyFile(String sourcePath, String targetPath) throws IOException{
		boolean success = false;
		
		File sourceFile = new File(sourcePath);
		File targetFile = new File(targetPath);
		
		if (sourceFile.exists()){
			try {
				Files.copy(sourceFile.toPath(), targetFile.toPath());
				success = true;
			} catch (IOException e) {
				throw e;
			}
		}
		
		return success;
	}
	
	public static boolean copyFolder(String oldPath, String newPath) throws IOException{
		boolean success = true;
		
		(new File(newPath)).mkdirs();
		String[] fileList = (new File(oldPath)).list();
		File temp = null;
		String sourcePath = "";
		String targetPath = "";
		
		for(int i=0; i<fileList.length; i++){
			sourcePath = getFileFullPath(oldPath, fileList[i]);
			targetPath = getFileFullPath(newPath, fileList[i]);
			
			temp = new File(sourcePath);
			if(temp.isFile()){
				try {
					success = copyFile(sourcePath, targetPath);
				}catch (IOException e) {
					success = false;
					throw e;
				}
				if(!success) break;
			}else if(temp.isDirectory()){
				copyFolder(sourcePath, targetPath);
			}
		}
		
		return success;
	}
	
	public static boolean saveToFile(String fileContent, String localPath){
		boolean success = true;
		
		try {
			FileOutputStream fos = new FileOutputStream(localPath);
			fos.write(fileContent.getBytes());
			fos.close();
		} catch (Exception e) {
			success = false;
		}
		
		return success;
	}
	
	public static boolean makeFolder(String folderPath){
		boolean success = true;
		File file = new File(folderPath);
		if(!file.exists()) success = file.mkdirs();
		return success;
	}
	
	public static String[] getFileList(String rootDir, String fileNameStartsWith){
		String[] files = null;
		File temp = null;
		String tempStr = "";
		
		String[] fileList = (new File(rootDir)).list();
		if(fileList.length > 0){
			for(int i=0; i<fileList.length; i++){
				if(fileList[i].startsWith(fileNameStartsWith)){
					temp = new File(rootDir + File.separator + fileList[i]);
					if(temp.isFile()){
						if("".equals(tempStr)){
							tempStr = rootDir + File.separator + fileList[i];
						}else{
							tempStr += ";" + rootDir + File.separator + fileList[i];
						}
					}
				}
			}
		}
		if(!"".equals(tempStr)) files = tempStr.split(";");
		
		return files;
	}
	
	public static long getFileLastModifiedTime(String path){
		long lastMT = 0;
		File temp = new File(path);
		if(temp.isFile()) lastMT = temp.lastModified();
		return lastMT;
	}
	
	public static double getFilePassedDays(String path){
		double passedDays = 365.0d;
		File temp = new File(path);
		if(temp.isFile()){
			passedDays = (System.currentTimeMillis() - temp.lastModified())/1000/3600/24;
		}
		return passedDays;
	}
	
	public static boolean fileExists(String path){
		File temp = new File(path);
		return temp.exists();
	}
}
