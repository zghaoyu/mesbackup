package com.cncmes.drv;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cncmes.base.Scanner;
import com.cncmes.base.ScannerItems;
import com.cncmes.ctrl.SocketClient;
import com.cncmes.data.ScannerData;
import com.cncmes.data.ScannerDriver;
import com.cncmes.data.SocketData;
import com.cncmes.handler.SocketDataHandler;
import com.cncmes.utils.LogUtils;

/**
 * Keyence Scanner Ethernet-Command-Base Driver
 * @author LI ZI LONG
 *
 */
public class ScannerKeyence implements Scanner {
//	private Map<String,Object> socketRespData= new LinkedHashMap<String,Object>();
	private Map<Socket, String> socketRespData = new ConcurrentHashMap<Socket, String>();
	private int cmdRetryCount = 5;
	private int socketRespTimeout = 2;//seconds
	private int socketRespTimeInterval = 10;//milliseconds
	
	private static ScannerData scannerData = ScannerData.getInstance();
	private static ScannerDriver scannerDrv = ScannerDriver.getInstance();
	private static ScannerKeyence scannerKeyence = new ScannerKeyence();
	private static ScannerSocketDataHandler handler = scannerKeyence.new ScannerSocketDataHandler();
	private ScannerKeyence(){}
	public static ScannerKeyence getInstance(){
		return scannerKeyence;
	}
	
	@Override
	public String doScanning(String ip) {
		boolean ok = sendCommand(ip, "doScanning");
		
		String barcode = null;
		if(ok){
			int port = (int) ScannerData.getInstance().getData(ip).get(ScannerItems.PORT);
			barcode = (String)socketRespData.get(ip+":"+port);
		}
		
		return barcode;
	}
	
	private boolean sendCommand(String ip, String cmd){
		boolean success = false;
		int port = (int) scannerData.getData(ip).get(ScannerItems.PORT);
		String model = (String) scannerData.getData(ip).get(ScannerItems.MODEL);
		String cmdEndChr = scannerDrv.getCmdTerminator(model);
//		String dtKey = ip+":"+port;
		
		SocketClient sc = SocketClient.getInstance();
		Socket socket = null;
		boolean sendOK = false;
		for (int i = 0; i < cmdRetryCount; i++) {
			sc = SocketClient.getInstance();
			String feedback = "";
			try {
				socket = sc.connect(ip, port, handler, cmdEndChr);
				if(null == socket) {
					continue;
				}
				sendOK = sc.sendData(socket, cmd, cmdEndChr);
				if(!sendOK){
					continue;
				}
				
				int count = socketRespTimeout * 1000 / socketRespTimeInterval;
				while(count > 0){
					feedback = socketRespData.get(socket);
					if(null != feedback && feedback.equals(cmd)){
						success = true;
						break;
					}else{
						try {
							Thread.sleep(socketRespTimeInterval);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						count--;
					}
				}
			} catch (IOException e) {
				LogUtils.errorLog("Scanner("+ip+") sendCommand("+cmd+") ERR:"+e.getMessage());
			} finally{
				if(null != socket){
					sc.removeSocket(socket);
					socketRespData.remove(socket);
				}
			}
			
			if(success) break;
			if(!success) LogUtils.errorLog(System.currentTimeMillis()+":"+cmd+"("+ip+":"+port+")"+" NG"+i+":"+cmd+"/"+feedback);
		}
		
		return success;
	}
	
	class ScannerSocketDataHandler extends SocketDataHandler{
		private ScannerSocketDataHandler(){}
		@Override
		public void doHandle(String in, Socket s) {
			if(null == s) return;
//			String ip = s.getInetAddress().toString().replace("/", "");
			socketRespData.put(s, in);
			SocketData.setData(in);
		}
	}
}
