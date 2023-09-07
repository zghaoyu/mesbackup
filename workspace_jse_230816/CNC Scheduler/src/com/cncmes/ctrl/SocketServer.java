package com.cncmes.ctrl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.cncmes.handler.SocketRespHandler;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.RunningMsg;
import com.cncmes.utils.ThreadUtils;

public class SocketServer {
	private static SocketServer socketSvr = new SocketServer();
	private static ServerSocket ss = null;
	private Map<Integer,LinkedHashMap<String,Object>> ssMap = new LinkedHashMap<Integer,LinkedHashMap<String,Object>>();
	private static LinkedHashMap<Integer,Integer> cnnCounter = new LinkedHashMap<Integer,Integer>();
	private static long nullDataInterval = 50; //ms
	private static long nullDataTimeOutCnt = 100;
	
	private SocketServer(){}
	public static SocketServer getInstance(){
		return socketSvr;
	}
	
	public static int getAcceptConnections(int...ports){
		int count = 0;
		
		if(null != ports && ports.length > 0){
			for(int i=0; i<ports.length; i++){
				if(null != cnnCounter.get(ports[i])){
					count += cnnCounter.get(ports[i]);
				}
			}
		}
		
		return count;
	}
	
	/**
	 * 
	 * @param port the listened port of server socket
	 * @param socketRespHandler the client data handler
	 * @return true if the server socket is started successfully
	 * @throws IOException
	 */
	public boolean socketSvrStart(int port, SocketRespHandler socketRespHandler) throws IOException {
		boolean success = false;
		try {
			ss = new ServerSocket(port);
			if(null != socketRespHandler) socketRespHandler.doHandle("Client Server started", null);
			LinkedHashMap<String,Object> ssLHP = new LinkedHashMap<String,Object>();
			ssLHP.put("serverSocket", ss);
			ssLHP.put("socketRespHandler", socketRespHandler);
			ssMap.put(port, ssLHP);
			
			cnnCounter.put(port, 0);//Waiting for connecting from client
			ThreadUtils.Run(new SocketSvrListener(port));
			success = true;
		} catch (IOException e) {
			LogUtils.errorLog("SocketServer SocketSrvStart("+port+") failed: "+e.getMessage());
			throw e;
		}
		
		return success;
	}
	
	/**
	 * 
	 * @param port the listened port of server socket
	 */
	public void stopSvrPort(int port){
		Set<Integer> set = ssMap.keySet();
		Iterator<Integer> it = set.iterator();
		
		while(it.hasNext()){
			int p = it.next();
			if(port == p){
				try {
					cnnCounter.put(port, -1);//Motivated stop
					ServerSocket ss = (ServerSocket) ssMap.get(port).get("serverSocket");
					ss.close();
				} catch (IOException e) {
				}
				break;
			}
		}
	}
	
	/**
	 * It is used to accept connection from clients
	 * @author Sanly
	 *
	 */
	class SocketSvrListener implements Runnable{
		private int port;
		
		public SocketSvrListener(int port){
			this.port = port;
		}
		
		@Override
		public void run() {
			String errMsg = "";
			int oriCount = 0;
			ServerSocket ss = (ServerSocket) ssMap.get(port).get("serverSocket");
			SocketRespHandler socketRespHandler = (SocketRespHandler) ssMap.get(port).get("socketRespHandler");
			
			try {
				while(cnnCounter.get(port) >= 0){
					Socket s = ss.accept();//There is client connecting
					System.out.println("connect:"+s);//TODO delete
					if(null != socketRespHandler) socketRespHandler.doHandle("Client Socket - " + String.valueOf(s.getPort()) + " connected", s);
					oriCount = cnnCounter.get(port);
					if(oriCount < 0) oriCount = 0;
					cnnCounter.put(port, oriCount + 1);
					
					//Start a thread at server side to serve the client
					ThreadUtils.Run(new SocketSvrThread(s, socketRespHandler, port));
				}
			} catch (IOException e) {
				errMsg = e.getMessage();
			} finally {
				if("".equals(errMsg)){//Normal stop
					RunningMsg.set("SocketServer SocketSrvListener stopped:"+port);
				}else{
					if(cnnCounter.get(port) >= 0){//Abnormal stop
						RunningMsg.set("SocketServer SocketSrvListener("+port+") stopped: "+errMsg);
						try {
							socketSvrStart(port, socketRespHandler);
						} catch (IOException e) {
						}
					}
				}
			}
		}
	}
	
	/**
	 * It is used to receive client's message and handle it
	 * @author Sanly
	 *
	 */
	class SocketSvrThread implements Runnable{
		private int port;
		private Socket s = null;
		private SocketRespHandler socketRespHandler = null;
		private long continueNullCnt = 0;
		
		public SocketSvrThread(Socket s,SocketRespHandler socketRespHandler,int port){
			this.s = s;
			this.socketRespHandler = socketRespHandler;
			this.port = port;
		}
		
		public void run(){
			String errMsg = "";
			BufferedReader br = null;
			
			try {
				br = new BufferedReader(new InputStreamReader(s.getInputStream()));
				
				String r = null;
				while (cnnCounter.get(port) > 0) {
					if(!br.ready()){
						try {
							Thread.sleep(nullDataInterval);
						} catch (InterruptedException e) {
						}
						continue;
					}
					r = br.readLine();
					if(null != socketRespHandler) socketRespHandler.doHandle(r, s);
					if(null != r){
						continueNullCnt = 0;
					}else{
						continueNullCnt++;
						try {
							Thread.sleep(nullDataInterval);
						} catch (InterruptedException e) {
						}
					}
					if(continueNullCnt >= nullDataTimeOutCnt){
						try {
							continueNullCnt--;
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
					}
					if("quit".equals(r)) break;
				}
				
				br.close();
				s.close();
				System.out.println("Server close connection:"+s);//TODO delete
			} catch (IOException e) {
				errMsg = e.getMessage();
				e.printStackTrace();//TODO need to delete later Hui Zhi 2022/6/7
			} finally {
				if(cnnCounter.get(port) > 0) cnnCounter.put(port, cnnCounter.get(port) - 1);
				if("".equals(errMsg)){//Normal stop
					RunningMsg.set("SocketServer SocketSrvThread stopped:"+port);
				}else{
					if(cnnCounter.get(port) >= 0){//Abnormal stop
						RunningMsg.set("SocketServer SocketSrvThread("+port+") stopped: "+errMsg);
						try {
							socketSvrStart(port, socketRespHandler);
						} catch (IOException e) {
						}
					}
				}
			}
		}
	}
}