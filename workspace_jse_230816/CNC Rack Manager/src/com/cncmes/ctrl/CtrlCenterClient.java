package com.cncmes.ctrl;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;

import com.cncmes.base.DeviceState;
import com.cncmes.data.SocketData;
import com.cncmes.handler.SocketDataHandler;
import com.cncmes.utils.MathUtils;
import com.cncmes.utils.ThreadUtils;

public class  CtrlCenterClient {
	private Map<String,Object> socketRespData= new LinkedHashMap<String,Object>();
	private LinkedHashMap<String,String> portMap = new LinkedHashMap<String,String>();
	private LinkedHashMap<String,Boolean> cmdFlag = new LinkedHashMap<String,Boolean>();
	
	private int retryCount = 5;
	private int socketTimeout = 50;//50 means 5 seconds
	
	private static CtrlCenterClient ctrlCC = new CtrlCenterClient();
	private CtrlCenterClient(){}
	public static CtrlCenterClient getInstance(){
		return ctrlCC;
	}
	//û��ʹ��
	public boolean setObjectState(String cmdName, String ccIP, String ccPort, String objID, DeviceState state, boolean threadMode){
		boolean ok = true;
		String cmdStr = cmdName + "," + objID + "," + state;
		cmdStr += "," + MathUtils.MD5Encode(cmdStr);
		
		if(threadMode){
			ThreadUtils.Run(new SendMsgToServer(ccIP, ccPort, cmdStr, cmdName));
		}else{
			ok = sendInfo(ccIP, Integer.parseInt(ccPort), cmdStr);
		}
		return ok;
	}
	
	public boolean informControlCenter(String cmdName, String ccIP, String ccPort, String dtKey, String dtVal, boolean threadMode, boolean threadSequential, boolean theLastThread){
		boolean ok = true;
		String cmdStr = cmdName + "," + dtKey + "," + dtVal;
		cmdStr += "," + MathUtils.MD5Encode(cmdStr);
		
		if(threadMode){
			if(threadSequential){
				ThreadUtils.sequentialRun(new SendMsgToServer(ccIP, ccPort, cmdStr, cmdName), theLastThread);
			}else{
				ThreadUtils.Run(new SendMsgToServer(ccIP, ccPort, cmdStr, cmdName));
			}
		}else{
			ok = sendInfo(ccIP, Integer.parseInt(ccPort), cmdStr);
		}
		return ok;
	}
	
	public synchronized boolean sendInfoSync(String ccIP, int ccPort, String info){
		return sendInfo(ccIP, ccPort, info);
	}
	
	public boolean sendInfo(String ccIP, int ccPort, String info){
		if("".equals(ccIP) || 0 == ccPort) return false;
		boolean success = false;
		String key = ccIP+":"+ccPort;
		portMap.put(""+ccPort, ccIP);
		
		SocketClient sc = SocketClient.getInstance();
		if(null == sc.getSocket(ccIP, ccPort) || null == socketRespData.get(key)){
			try {
				sc.connect(ccIP, ccPort, new CtrlCCDataHandler(), null);
				socketRespData.put(key, "");
			} catch (IOException e) {
				return false;
			}
		}else{
			socketRespData.put(key, "");
			sc.startSocketClientListener(sc.getSocket(ccIP, ccPort), ccIP, ccPort);
			if(null == sc.getSocketDataHandler(ccIP, ccPort)){
				sc.addSocketDataHandler(ccIP, ccPort, new CtrlCCDataHandler());
			}
		}
		
		boolean bOK = false;
		for(int i=0; i<retryCount; i++){
			if(i>0 && !success && !bOK){
				try {
					bOK = sc.connect(ccIP, ccPort, new CtrlCCDataHandler(), null);
				} catch (IOException e) {
					continue;
				}
			}
			
			try {
				sc.sendData(ccIP, ccPort, info);
				int count = socketTimeout;
				String feedback = "";
				while(count > 0){
					feedback = (String) socketRespData.get(key);
					if(null != feedback && feedback.equals(info)){
						success = true;
						break;
					}else{
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						count--;
					}
				}
			} catch (IOException e) {
			}
			
			if(success) break;
		}
		
		return success;
	}
	
	class CtrlCCDataHandler extends SocketDataHandler{
		@Override
		public void doHandle(String in, Socket s) {
			if(null == s || null == in) return;
			String ip = s.getInetAddress().toString().replace("/", "");
			if("127.0.0.1".equals(ip)) ip = portMap.get(""+s.getPort());
			
			socketRespData.put(ip+":"+s.getPort(), in);
			SocketData.setData(in);
		}
	}
	
	class SendMsgToServer implements Runnable{
		private String msg, signature, svrIP, svrPort;
		
		public SendMsgToServer(String svrIP, String ccPort, String msg, String signature){
			this.svrIP = svrIP;
			this.svrPort = ccPort;
			this.msg = msg;
			this.signature = signature;
		}
		
		@Override
		public void run() {
			boolean OK = sendInfoSync(svrIP,Integer.parseInt(svrPort),msg);
			if(!"".equals(signature)) cmdFlag.put(signature, OK);
		}
	}
}
