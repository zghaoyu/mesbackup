package com.sto.utils;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFileChooser;

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
	
	public static String getFileName(String filePath){
		String fileName = "";
		
		File sourceFile = new File(filePath);
		if(sourceFile.exists()) fileName = sourceFile.getName();
		
		return fileName;
	}
	
	public static boolean fileExists(String filePath){
		boolean exists = false;
		File temp = null;
		temp = new File(filePath);
		if(temp.exists() && temp.isFile()){
			exists = true;
		}
		return exists;
	}
	
	public static File getFileObject(String filePath){
		File temp = null;
		temp = new File(filePath);
		if(!(null!=temp && temp.exists() && temp.isFile())) temp = null;
		return temp;
	}
	
	public static boolean deleteFile(String filePath){
		boolean success = true;
		File temp = null;
		temp = new File(filePath);
		if(null!=temp && temp.exists() && temp.isFile()){
			success = temp.delete();
		}
		return success;
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
	
	public static boolean moveFile(String sourcePath, String targetFolder){
		boolean success = false;
		String targetPath = "";
		
		File sourceFile = new File(sourcePath);
		if(sourceFile.exists() && makeFolder(targetFolder)){
			targetPath = targetFolder + File.separator + sourceFile.getName();
			success = sourceFile.renameTo(new File(targetPath));
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
	
	public static int makeFolderEx(String folderPath){
		int flag = 0;
		File file = new File(folderPath);
		if(!file.exists()){
			flag = -1;
			if(file.mkdirs()) flag = 1;
		}else{
			flag = 2;
		}
		
		return flag;
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
	
	public static ArrayList<String> getFileNames(String rootDir, String targetDir, String[] cmpDirs, String nameEndsWith, long copyMaxQty){
		ArrayList<String> files = new ArrayList<String>();
		File temp = null;
		long qty = 0;
		boolean fileFound = false;
		
		if(null!=cmpDirs && cmpDirs.length>0){
			String[] fileList = (new File(rootDir)).list();
			if(null!=fileList && fileList.length > 0){
				for(int i=0; i<fileList.length; i++){
					if(fileList[i].endsWith(nameEndsWith)){
						fileFound = fileExists(targetDir+File.separator+fileList[i]);
						if(fileFound) continue;
						for(int j=0; j<cmpDirs.length; j++){
							fileFound = fileExists(cmpDirs[j]+File.separator+fileList[i]);
							if(fileFound) break;
						}
						if(fileFound) continue;
						
						temp = new File(rootDir + File.separator + fileList[i]);
						if(temp.isFile()){
							files.add(fileList[i]);
							qty++;
							if(qty>=copyMaxQty) break;
						}
					}
				}
			}
		}
		
		return files;
	}
	
	public static ArrayList<String> getFileNames(String rootDir, String nameEndsWith, long minModifiedT){
		ArrayList<String> files = new ArrayList<String>();
		File temp = null;
		
		String[] fileList = (new File(rootDir)).list();
		if(null!=fileList && fileList.length > 0){
			for(int i=0; i<fileList.length; i++){
				if(fileList[i].endsWith(nameEndsWith)){
					temp = new File(rootDir + File.separator + fileList[i]);
					if(temp.isFile() && temp.lastModified()>minModifiedT){
						files.add(fileList[i]);
					}
				}
			}
		}
		
		return files;
	}
	
	public static ArrayList<String> getFileNames(String rootDir, String nameEndsWith, int maxQty){
		ArrayList<String> files = new ArrayList<String>();
		File temp = null;
		
		int cnt = 0;
		String[] fileList = (new File(rootDir)).list();
		if(null!=fileList && fileList.length > 0){
			for(int i=0; i<fileList.length; i++){
				if(fileList[i].endsWith(nameEndsWith)){
					temp = new File(rootDir + File.separator + fileList[i]);
					if(temp.isFile()){
						cnt++;
						files.add(fileList[i]);
						if(cnt>=maxQty) break;
					}
				}
			}
		}
		
		return files;
	}
	
	public static ArrayList<String> getFileNames(String[] sourceDirs, String nameEndsWith){
		ArrayList<String> fileNames = new ArrayList<String>();
		File fileObj = null;
		if(null!=sourceDirs && sourceDirs.length>0){
			for(int i=0; i<sourceDirs.length; i++){
				String[] fileList = (new File(sourceDirs[i])).list();
				if(null!=fileList && fileList.length > 0){
					for(int j=0; j<fileList.length; j++){
						if(fileList[j].endsWith(nameEndsWith)){
							fileObj = getFileObject(sourceDirs[i]+File.separator+fileList[j]);
							if(fileObj.isFile()) fileNames.add(fileList[j]);
						}
					}
				}
			}
		}
		
		return fileNames;
	}
	
	public static boolean thereIsFileInDir(String rootDir, String nameEndsWith){
		boolean hasFiles = false;
		
		String[] fileList = (new File(rootDir)).list();
		if(null!=fileList && fileList.length>0){
			for(int i=0; i<fileList.length; i++){
				if(fileList[i].endsWith(nameEndsWith)){
					hasFiles = true;
					break;
				}
			}
		}
		
		return hasFiles;
	}
	
	public static String chooseFile(Component parent, String titleText, String buttonText, boolean rtnDirPath){
		String fileAbsPath = "";
		int btnIdx = 0;
		
		String defaultDir = XmlUtils.readProfileString("lastOpenDir");
		if(null==defaultDir || "".equals(defaultDir)) defaultDir = System.getProperty("user.dir");
		JFileChooser jfc = new JFileChooser();
		if(rtnDirPath){
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}else{
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
		jfc.setDialogTitle(titleText);
		jfc.setCurrentDirectory(new File(defaultDir));
		btnIdx = jfc.showDialog(parent, buttonText);
		
		if(0==btnIdx){
			File file = jfc.getSelectedFile();
			if(file != null){
				fileAbsPath = file.getAbsolutePath();
				if(file.isFile()){
					defaultDir = fileAbsPath.replace(file.getName(), "");
				}else{
					defaultDir = fileAbsPath;
					if(!rtnDirPath) fileAbsPath = "";
				}
				XmlUtils.writeProfileString("lastOpenDir", defaultDir);
			}
		}else{
			return "";
		}
		
		return (rtnDirPath?defaultDir:fileAbsPath);
	}
	
	public static long getFileLastModifiedTime(String path){
		long lastMT = 0;
		File temp = new File(path);
		if(temp.isFile()) lastMT = temp.lastModified();
		return lastMT;
	}
	
	public static void setFileLastModifiedTime(String path, long newModifiedTime){
		File temp = new File(path);
		if(null!=temp && temp.isFile()) temp.setLastModified(newModifiedTime);
	}
	
	public static double getFilePassedDays(String path){
		double passedDays = 365.0d;
		File temp = new File(path);
		if(temp.isFile()){
			passedDays = (System.currentTimeMillis() - temp.lastModified())/1000/3600/24;
		}
		return passedDays;
	}
	
	public static ArrayList<String> getSubDirs(ArrayList<String> subDirs, String sourceDir, String nameEndsWith){
		if(null==subDirs) subDirs = new ArrayList<String>();
		
		File temp = null;
		String[] fileList = (new File(sourceDir)).list();
		String subDir = "";
		if(null!=fileList && fileList.length > 0){
			for(int i=0; i<fileList.length; i++){
				temp = new File(sourceDir + File.separator + fileList[i]);
				if(temp.isDirectory()){
					subDir = sourceDir + File.separator + fileList[i];
					getSubDirs(subDirs, subDir, nameEndsWith);
				}else if(temp.isFile() && fileList[i].endsWith(nameEndsWith)){
					subDirs.add(sourceDir);
					break;
				}
				
			}
		}
		
		return subDirs;
	}
	
	public static long getDirLastProcTime(String sourceDir){
		long lastProcTime = 0;
		
		String procT = XmlUtils.readProfileString(sourceDir);
		if(!"".equals(procT)) lastProcTime = Long.parseLong(procT);
		
		return lastProcTime;
	}
	
	public static long copyMissedFiles(String imgRootDir, String targetDir, String[] cmpDirs, String nameEndsWith, long copyFilesQty){
		long missedFiles = 0;
		int makeFlag = 0;
		boolean bFound = false, bCopyAll = false;
		String sourceDir = "";
		ArrayList<String> subDirs = null;
		subDirs = MyFileUtils.getSubDirs(subDirs, imgRootDir, nameEndsWith);
		makeFlag = makeFolderEx(targetDir);
		if(!(makeFlag>=1)) return 0;
		
		if(null!=subDirs && subDirs.size()>0){
			for(int n=0; n<subDirs.size(); n++){
				sourceDir = subDirs.get(n);
//				System.out.println("Processing "+sourceDir);
				ArrayList<String> sourceNames = getFileNames(sourceDir, targetDir, cmpDirs, nameEndsWith, copyFilesQty);
				
				if(null!=sourceNames && sourceNames.size()>0){
					if(null!=cmpDirs && cmpDirs.length>0){
						for(int j=0; j<sourceNames.size(); j++){
							bFound = false;
							for(int k=0; k<cmpDirs.length; k++){
								bFound = MyFileUtils.fileExists(cmpDirs[k]+File.separator+sourceNames.get(j));
								if(bFound) break;
							}
							if(!bFound){
								try {
									copyFile(sourceDir+File.separator+sourceNames.get(j),targetDir+File.separator+sourceNames.get(j));
									missedFiles++;
									if(copyFilesQty>0 && missedFiles>copyFilesQty) break;
								} catch (IOException e) {
								}
							}
						}
					}else{
						bCopyAll = true;
					}
					
					if(bCopyAll){
						for(int i=0; i<sourceNames.size(); i++){
							try {
								copyFile(sourceDir+File.separator+sourceNames.get(i),targetDir+File.separator+sourceNames.get(i));
								missedFiles++;
								if(copyFilesQty>0 && missedFiles>copyFilesQty) break;
							} catch (IOException e) {
							}
						}
					}
				}
			}
		}
		
		return missedFiles;
	}
	
	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
}
