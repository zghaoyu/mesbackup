package com.cncmes.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.LinkedHashMap;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import com.cncmes.data.SystemConfig;

public class FTPUtils {
	private static String IP = null;
	private static String USERNAME = null;
	private static String USERPWD = null;
	private static int PORT = 0;
	
	private FTPUtils(){}
	static{
		getCommonSettings();
	}

	private static void getCommonSettings() {
		try {
			SystemConfig sysCfg = SystemConfig.getInstance();
			LinkedHashMap<String,Object> config = sysCfg.getFtpCfg();
			IP = (String) config.get("ip");
			USERNAME = (String) config.get("username");
			USERPWD = (String) config.get("userpwd");
			PORT = Integer.parseInt((String)config.get("port"));
			USERPWD = DesUtils.decrypt(USERPWD).replace(",", "");
			USERNAME = DesUtils.decrypt(USERNAME).replace(",", "");
		} catch (Exception e) {
			LogUtils.errorLog("FTPUtils fails to load system config:"+e.getMessage());
		}
	}
	
	/**
	 * 
	 * @param localAbsolutePath the absolute path of the local file to be uploaded
	 * @param serverFolderName the relative folder path on the server to store the uploaded file
	 * @return true if the local file is uploaded successfully
	 */
	public static boolean uploadFile(String localAbsolutePath, String serverFolderName, String svrFileName){
		boolean success = false;
		FTPClient ftpClient = new FTPClient();
		FileInputStream fis = null;
		getCommonSettings();
		
		try {
			localAbsolutePath = localAbsolutePath.replaceAll("\\\\", "/");
			serverFolderName = serverFolderName.replaceAll("\\\\", "/");
			if(!serverFolderName.startsWith("/")) serverFolderName = "/" + serverFolderName;
			if(!serverFolderName.endsWith("/")) serverFolderName = serverFolderName + "/";
			
			File srcFile = new File(localAbsolutePath);
			if(!srcFile.exists()) return false;
			
			ftpClient.connect(IP, PORT);
			if(FTPReply.isPositiveCompletion(ftpClient.getReplyCode())){
				if(ftpClient.login(USERNAME, USERPWD)){
					fis = new FileInputStream(srcFile);
					
					ftpClient.makeDirectory(serverFolderName);
					if(!ftpClient.changeWorkingDirectory(serverFolderName)) return false;
					ftpClient.setBufferSize(1024);
					ftpClient.setControlEncoding("GBK");
					if(!ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE)) return false;
					success = ftpClient.storeFile(svrFileName,fis);
				}
			}
		} catch (SocketException e) {
			LogUtils.errorLog("FTPUtils uploadFile("+localAbsolutePath+","+serverFolderName+") failed:"+e.getMessage());
		} catch (IOException e) {
			LogUtils.errorLog("FTPUtils uploadFile("+localAbsolutePath+","+serverFolderName+") failed:"+e.getMessage());
		} finally {
			try {
				if(null != fis) fis.close();
				if(null != ftpClient) ftpClient.disconnect();
			} catch (IOException e) {
			}
		}
		
		return success;
	}
	
	/**
	 * 
	 * @param serverFilePath the server file location to be downloaded
	 * @param localFolderPath the local folder path to store the downloaded file
	 * @return true if the file is downloaded successfully
	 */
	public static boolean downloadFile(String serverFilePath, String localFolderPath){
		boolean success = false;
		FTPClient ftpClient = new FTPClient();
		FileOutputStream fos = null;
		getCommonSettings();
		
		try {
			serverFilePath = serverFilePath.replaceAll("\\\\", "/");
			localFolderPath = localFolderPath.replaceAll("\\\\", "/");
			if(!localFolderPath.endsWith("/")) localFolderPath = localFolderPath + "/";
			String fileName = serverFilePath.substring(serverFilePath.lastIndexOf("/") + 1);
			
			ftpClient.connect(IP, PORT);
			if(FTPReply.isPositiveCompletion(ftpClient.getReplyCode())){
				if(ftpClient.login(USERNAME, USERPWD)){
					File file = new File(localFolderPath);
					if(!file.exists()) file.mkdirs();
					
					fos = new FileOutputStream(localFolderPath + fileName);
					ftpClient.setBufferSize(1024);
					if(!ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE)) return false;
					success = ftpClient.retrieveFile(serverFilePath, fos);
				}
			}
		} catch (SocketException e) {
			LogUtils.errorLog("FTPUtils downloadFile("+serverFilePath+","+localFolderPath+") failed:"+e.getMessage());
		} catch (IOException e) {
			LogUtils.errorLog("FTPUtils downloadFile("+serverFilePath+","+localFolderPath+") failed:"+e.getMessage());
		} finally {
			try {
				if(null != fos) fos.close();
				if(null != ftpClient) ftpClient.disconnect();
			} catch (IOException e) {
			}
		}
		
		return success;
	}
	
	public static boolean fileExists(String serverFilePath){
		boolean success = false;
		FTPClient ftpClient = new FTPClient();
		getCommonSettings();
		
		try {
			serverFilePath = serverFilePath.replaceAll("\\\\", "/");
			String fileFolder = serverFilePath.substring(0, serverFilePath.lastIndexOf("/"));
			
			ftpClient.connect(IP, PORT);
			if(FTPReply.isPositiveCompletion(ftpClient.getReplyCode())){
				if(ftpClient.login(USERNAME, USERPWD)){
					if(!ftpClient.changeWorkingDirectory(fileFolder)) return false;
					String[] listNames = ftpClient.listNames(serverFilePath);
					if(null!=listNames && listNames.length>0) success = true;
				}
			}
		} catch (SocketException e) {
			LogUtils.errorLog("FTPUtils fileExists("+serverFilePath+") failed:"+e.getMessage());
		} catch (IOException e) {
			LogUtils.errorLog("FTPUtils fileExists("+serverFilePath+") failed:"+e.getMessage());
		} finally {
			try {
				if(null != ftpClient) ftpClient.disconnect();
			} catch (IOException e) {
			}
		}
		
		return success;
	}
}
