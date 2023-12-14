package com.cncmes.ctrl;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;

import com.cncmes.base.RackItems;
import com.cncmes.base.SpecItems;
import com.cncmes.base.WorkpieceItems;
import com.cncmes.data.RackProduct;
import com.cncmes.data.SocketData;
import com.cncmes.data.WorkpieceData;
import com.cncmes.handler.SocketDataHandler;
import com.cncmes.utils.MathUtils;
import com.cncmes.utils.RunningMsg;

public class RackClient {
	private Map<String,Object> socketRespData= new LinkedHashMap<String,Object>();
	private LinkedHashMap<String,String> portMap = new LinkedHashMap<String,String>();
	private int retryCount = 5;
	private int socketTimeout = 50;//50 means 5 seconds
	
	private static RackClient rackClient = new RackClient();
	private RackClient(){}
	public static RackClient getInstance(){
		return rackClient;
	}
	
	public boolean rackServerIsReady(String lineName, String rackID){
		boolean ready = true;
		
		String[] rackCfg = getRackConfig(lineName, rackID);
		String info = "checkState,1";
		info += "," + MathUtils.MD5Encode(info);
		
		ready = sendInfo(rackCfg[0],Integer.parseInt(rackCfg[1]),info);
		
		if(!ready) RunningMsg.set("Rack Manager is not ready");
		
		return ready;
	}
	//没有被调用
	public boolean updateRackInfo(String lineName, String rackID, String workpieceID){
		boolean success = false;
		String info = "";
		String[] rackCfg = getRackConfig(lineName, rackID);
		
		WorkpieceData wpData = WorkpieceData.getInstance();
		LinkedHashMap<WorkpieceItems,Object> dt = wpData.getData(workpieceID);
		if(null != dt){
			info = workpieceID;
			info += "," + rackID;
			info += "," + dt.get(WorkpieceItems.LINENAME);
			info += "," + dt.get(WorkpieceItems.STATE);
			info += "," + dt.get(WorkpieceItems.PROCQTY);
			info += "," + dt.get(WorkpieceItems.CONVEYORID);
			info += "," + dt.get(WorkpieceItems.CONVEYORSLOTNO);
			LinkedHashMap<SpecItems, String> spec = wpData.getAllProcInfo(workpieceID);
			if(null != spec){
				info += "," + spec.get(SpecItems.PROCESSNAME);
				info += "," + spec.get(SpecItems.SURFACE);
				info += "," + spec.get(SpecItems.SIMTIME);
				info += "," + spec.get(SpecItems.NCMODEL);
				info += "," + spec.get(SpecItems.PROGRAM);
				
				info += "," + MathUtils.MD5Encode(info);
			}
		}
		
		if(!"".equals(info)) success = sendInfo(rackCfg[0],Integer.parseInt(rackCfg[1]),info);
		
		return success;
	}
	
	private String[] getRackConfig(String lineName, String rackID){
		String[] config = new String[]{"0","0"};
		
		RackProduct rackProd = RackProduct.getInstance();
		if(rackProd.size() > 0){
			for(String id:rackProd.getDataMap().keySet()){
				LinkedHashMap<RackItems, Object> dt = rackProd.getData(id);
				String ln = (String) dt.get(RackItems.LINENAME);
				String rid = String.valueOf(dt.get(RackItems.ID));
				if(lineName.equals(ln) && rackID.equals(rid)){
					config[0] = (String)dt.get(RackItems.IP);
					config[1] = String.valueOf(dt.get(RackItems.PORT));
					break;
				}
			}
		}
		
		return config;
	}
	
	private boolean sendInfo(String rackIP, int port, String info){
		boolean success = false;
		String key = rackIP+":"+port;
		portMap.put(""+port, rackIP);
		
		SocketClient sc = SocketClient.getInstance();
		if(null == sc.getSocket(rackIP, port) || null == socketRespData.get(key)){
			try {
				sc.connect(rackIP, port, new RackSocketDataHandler(), null);
				socketRespData.put(key, "");
			} catch (IOException e) {
				return false;
			}
		}else{
			socketRespData.put(key, "");
			sc.startSocketClientListener(sc.getSocket(rackIP, port), rackIP, port);
			if(null == sc.getSocketDataHandler(rackIP, port)){
				sc.addSocketDataHandler(rackIP, port, new RackSocketDataHandler());
			}
		}
		
		boolean bOK = false;
		for(int i=0; i<retryCount; i++){
			if(i>0 && !success && !bOK){
				try {
					bOK = sc.connect(rackIP, port, new RackSocketDataHandler(), null);
				} catch (IOException e) {
					continue;
				}
			}
			
			try {
				sc.sendData(rackIP, port, info);
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
	
	class RackSocketDataHandler extends SocketDataHandler{
		@Override
		public void doHandle(String in, Socket s) {
			if(null == s || null == in) return;
			String ip = s.getInetAddress().toString().replace("/", "");
			if("127.0.0.1".equals(ip)) ip = portMap.get(""+s.getPort());
			
			socketRespData.put(ip+":"+s.getPort(), in);
			SocketData.setData(in);
		}
	}
}
