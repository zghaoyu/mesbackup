package com.cncmes.drv;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;

import com.cncmes.base.Scanner;
import com.cncmes.base.ScannerItems;
import com.cncmes.ctrl.SocketClient;
import com.cncmes.data.ScannerData;
import com.cncmes.data.ScannerDriver;
import com.cncmes.data.SocketData;
import com.cncmes.handler.SocketDataHandler;
import com.cncmes.utils.DebugUtils;
import com.cncmes.utils.LogUtils;

/**
 * Keyence Scanner Ethernet-Command-Base Driver
 * @author LI ZI LONG
 *
 */
public class ScannerKeyence implements Scanner {
	private Map<String,Object> socketRespData= new LinkedHashMap<String,Object>();
	private LinkedHashMap<String,String> portMap = new LinkedHashMap<String,String>();
	private int cmdRetryCount = 5;
	private int socketRespTimeout = 2;//seconds
	private int socketRespTimeInterval = 10;//milliseconds
	
	private static ScannerData scannerData = ScannerData.getInstance();
	private static ScannerDriver scannerDrv = ScannerDriver.getInstance();
	private static ScannerKeyence scannerKeyence = new ScannerKeyence();
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
		String dtKey = ip+":"+port;
		portMap.put(""+port, ip);
		
		SocketClient sc = SocketClient.getInstance();
		SocketDataHandler sdh = sc.getSocketDataHandler(ip, port);
		if(null == sdh){
			sdh = new ScannerSocketDataHandler();
			sc.addSocketDataHandler(ip, port, sdh);
		}
		if(null == sc.getSocket(ip, port) || null == socketRespData.get(dtKey)){
			try {
				sc.connect(ip, port, sdh, null, cmdEndChr);
			} catch (IOException e) {
				return false;
			}
		}
		
		boolean cnnOK = false;
		socketRespData.put(dtKey, "");
		for(int i=0; i<cmdRetryCount; i++){
			if(i == 2) cnnOK = false;
			if(i > 0 && !success && !cnnOK){
				sc = SocketClient.getInstance();
				try {
					cnnOK = sc.connect(ip, port, sdh, null, cmdEndChr);
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
			}
			
			String feedback = "";
			try {
				cnnOK = sc.sendData(ip, port, cmd);
				int count = socketRespTimeout * 1000 / socketRespTimeInterval;
				while(count > 0){
					feedback = (String) socketRespData.get(dtKey);
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
			}
			
			if(success) break;
			if(!success) LogUtils.errorLog(System.currentTimeMillis()+":"+cmd+"("+ip+":"+port+")"+" NG"+i+":"+cmd+"/"+feedback);
		}
		
		return success;
	}
	
	class ScannerSocketDataHandler extends SocketDataHandler{
		@Override
		public void doHandle(String in, Socket s) {
			if(null == s) return;
			String ip = s.getInetAddress().toString().replace("/", "");
			if("127.0.0.1".equals(ip) || DebugUtils.getDummyDeviceIP(null).equals(ip)) ip = portMap.get(""+s.getPort());
			socketRespData.put(ip+":"+s.getPort(), in);
			SocketData.setData(in);
		}
	}
}
