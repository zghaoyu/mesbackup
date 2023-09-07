package com.cncmes.ctrl;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.cncmes.base.RackItems;
import com.cncmes.base.SpecItems;
import com.cncmes.base.WorkpieceItems;
import com.cncmes.data.RackProduct;
import com.cncmes.data.SocketData;
import com.cncmes.data.WorkpieceData;
import com.cncmes.handler.SocketDataHandler;
import com.cncmes.handler.impl.CncClientDataHandler;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.MathUtils;
import com.cncmes.utils.ThreadUtils;

/**
 * For CNC Client(Control Center) to Communicate With the Rack Manager
 * 
 * @author LI ZI LONG
 */
public class RackClient {
	// private Map<String,Object> socketRespData= new
	// LinkedHashMap<String,Object>();
	private Map<Socket, String> socketRespData = new ConcurrentHashMap<Socket, String>();
	private LinkedHashMap<String, String> portMap = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, Boolean> cmdFlag = new LinkedHashMap<String, Boolean>();
	private int retryCount = 5;
	private int socketTimeout = 50;// 50 means 5 seconds

	private static RackClient rackClient = new RackClient();
	private static RackSocketDataHandler handler = rackClient.new RackSocketDataHandler();

	private RackClient() {
	}

	public static RackClient getInstance() {
		return rackClient;
	}

	/**
	 * @param lineName
	 *            name of the production line
	 * @param rackID
	 *            the material rack ID
	 * @return true if the Rack Manager is online and ready
	 */
	public boolean rackServerIsReady(String lineName, String rackID) {
		boolean ready = true;
		String result = "";
		String[] rackCfg = getRackConfig(lineName, rackID);
		String info = "checkState," + lineName;
		info += "," + CncClientDataHandler.getIP();
		info += "," + CncClientDataHandler.getPort();
		info += "," + MathUtils.MD5Encode(info);

		result = sendInfo(rackCfg[0], Integer.parseInt(rackCfg[1]), info);
		if(null == result || result.equals("")){
			ready = false;
		}
		
		if (!ready) {
			LogUtils.errorLog("RackClient-rackServerIsReady(" + lineName + "," + rackID + ")-sendInfo(" + rackCfg[0]
					+ "," + rackCfg[1] + "," + info + ") failed");
		}

		return ready;
	}

	/**
	 * @param lineName
	 *            name of the production line
	 * @param rackID
	 *            the material rack ID
	 * @param threadMode
	 *            whether using sub thread to inform Rack Manager
	 * @return true if succeed
	 */
	public boolean randomUpdateMaterialRack(String lineName, boolean threadMode, int qty) {
		boolean success = true;
		String result = "";
		String info = "randomUpdateMaterial," + lineName + "," + qty;
		info += "," + MathUtils.MD5Encode(info);

		String[] rackCfg = getRackConfigByLineName(lineName);
		if (threadMode) {
			ThreadUtils.Run(new sendMsgToServer(rackCfg[0], Integer.parseInt(rackCfg[1]), info, ""));
		} else {
			result = sendInfo(rackCfg[0], Integer.parseInt(rackCfg[1]), info);
			if(null == result || result.equals("")){
				success = false;
			}
		}

		return success;
	}

	/**
	 * @param lineName
	 *            name of the production line
	 * @param rackID
	 *            the material rack ID
	 * @param workpieceID
	 *            the workpiece ID
	 * @param threadMode
	 *            true to update Rack Manager in sub thread
	 * @param threadSequential
	 *            true means all sub threads are sequentially executed
	 * @param theLastThread
	 *            true means the last sub thread, only useful while threadMode
	 *            and threadSequential is both true
	 * @return true if succeed
	 */
	public boolean updateRackInfo(String lineName, String rackID, String workpieceID, boolean threadMode,
			boolean threadSequential, boolean theLastThread) {
		boolean success = true;
		String result = "";
		String info = "";

		WorkpieceData wpData = WorkpieceData.getInstance();
		LinkedHashMap<WorkpieceItems, Object> dt = wpData.getData(workpieceID);
		if (null != dt) {
			info = workpieceID;
			info += "," + rackID;
			info += "," + dt.get(WorkpieceItems.LINENAME);
			info += "," + dt.get(WorkpieceItems.STATE);
			info += "," + dt.get(WorkpieceItems.PROCQTY);
			info += "," + dt.get(WorkpieceItems.CONVEYORID);
			info += "," + dt.get(WorkpieceItems.CONVEYORSLOTNO);
			LinkedHashMap<SpecItems, String> spec = wpData.getAllProcInfo(workpieceID);
			if (null != spec) {
				info += "," + (null != dt.get(WorkpieceItems.PROCESS) ? dt.get(WorkpieceItems.PROCESS)
						: spec.get(SpecItems.PROCESSNAME));
				info += "," + (null != dt.get(WorkpieceItems.SURFACE) ? dt.get(WorkpieceItems.SURFACE)
						: spec.get(SpecItems.SURFACE));
				info += "," + (null != dt.get(WorkpieceItems.MACHINETIME) ? dt.get(WorkpieceItems.MACHINETIME)
						: spec.get(SpecItems.SIMTIME));
				info += "," + (null != dt.get(WorkpieceItems.NCMODEL) ? dt.get(WorkpieceItems.NCMODEL)
						: spec.get(SpecItems.NCMODEL));
				info += "," + (null != dt.get(WorkpieceItems.PROGRAM) ? dt.get(WorkpieceItems.PROGRAM)
						: spec.get(SpecItems.PROGRAM));
			} else {
				info += "," + (null != dt.get(WorkpieceItems.PROCESS) ? dt.get(WorkpieceItems.PROCESS) : "NULL");
				info += "," + (null != dt.get(WorkpieceItems.SURFACE) ? dt.get(WorkpieceItems.SURFACE) : "NULL");
				info += ","
						+ (null != dt.get(WorkpieceItems.MACHINETIME) ? dt.get(WorkpieceItems.MACHINETIME) : "NULL");
				info += "," + (null != dt.get(WorkpieceItems.NCMODEL) ? dt.get(WorkpieceItems.NCMODEL) : "NULL");
				info += "," + (null != dt.get(WorkpieceItems.PROGRAM) ? dt.get(WorkpieceItems.PROGRAM) : "NULL");
			}
			info += "," + MathUtils.MD5Encode(info);
		}

		if (!"".equals(info)) {
			String[] rackCfg = getRackConfig(lineName, rackID);
			if (threadMode) {
				if (threadSequential) {
					ThreadUtils.sequentialRun(new sendMsgToServer(rackCfg[0], Integer.parseInt(rackCfg[1]), info, ""),
							theLastThread);
				} else {
					ThreadUtils.Run(new sendMsgToServer(rackCfg[0], Integer.parseInt(rackCfg[1]), info, ""));
				}
			} else {
				
				result = sendInfo(rackCfg[0], Integer.parseInt(rackCfg[1]), info);
				if(null == result || result.equals("")){
					success = false;
				}
			}
		}

		return success;
	}

	/**
	 * @param lineName
	 *            name of the production line
	 * @param rackID
	 *            the material rack ID
	 * @return all empty slots of the specified rack
	 */
	public String[] getRackEmptySlots(String lineName, String rackID) {
		String[] slots = null;
		String info = "", val = "";
		String result = "";
		boolean success = true;

		info = "getRackEmptySlots";
		info += "," + lineName;
		info += "," + rackID;
		info += "," + MathUtils.MD5Encode(info);

		String[] rackCfg = getRackConfig(lineName, rackID);
		result = sendInfo(rackCfg[0], Integer.parseInt(rackCfg[1]), info);
		if(null == result || result.equals("")){
			success = false;
		}
		if (success) {
//			String key = rackCfg[0] + ":" + rackCfg[1];
//			val = (String) socketRespData.get(key);
			val = result;
			if (val.startsWith("getRackEmptySlots,")) {
				val = val.replace("getRackEmptySlots,", "");
			} else {
				val = "";
			}
		}
		if (!"".equals(val))
			slots = val.split(",");

		return slots;
	}

	private String[] getRackConfig(String lineName, String rackID) {
		String[] config = new String[] { "0", "0" };

		RackProduct rackProd = RackProduct.getInstance();
		if (rackProd.size() > 0) {
			Set<String> rackSet = rackProd.getDataMap().keySet();
			for (String id : rackSet) {
				LinkedHashMap<RackItems, Object> dt = rackProd.getData(id);
				String ln = (String) dt.get(RackItems.LINENAME);
				String rid = String.valueOf(dt.get(RackItems.ID));
				if (lineName.equals(ln) && rackID.equals(rid)) {
					config[0] = (String) dt.get(RackItems.IP);
					config[1] = String.valueOf(dt.get(RackItems.PORT));
					break;
				}
			}
		}

		return config;
	}

	private String[] getRackConfigByLineName(String lineName) {
		String[] config = new String[] { "0", "0" };

		RackProduct rackProd = RackProduct.getInstance();
		if (rackProd.size() > 0) {
			Set<String> rackSet = rackProd.getDataMap().keySet();
			for (String id : rackSet) {
				LinkedHashMap<RackItems, Object> dt = rackProd.getData(id);
				String ln = (String) dt.get(RackItems.LINENAME);
				int port = Integer.parseInt("" + dt.get(RackItems.PORT));
				if (lineName.equals(ln) && port > 1024) {
					config[0] = (String) dt.get(RackItems.IP);
					config[1] = "" + port;
					break;
				}
			}
		}

		return config;
	}

	// Change by Hui Zhi 2022/6/1
	/**
	 * 
	 * @param rackIP
	 * @param port
	 * @param info
	 * @return "" if failed, else return the result
	 */
	private String sendInfo(String rackIP, int port, String info) {
		boolean success = false;
		boolean sendOK = false;
		String result = "";
		Socket socket = null;
		portMap.put("" + port, rackIP);
		SocketClient sc = SocketClient.getInstance();

		for (int i = 0; i < retryCount; i++) {
			try {
				socket = sc.connect(rackIP, port, handler, null);
				if (null == socket) {
					continue;
				}
				System.out.println("Connect Rack OK");
				sendOK = sc.sendData(socket, info, null);
				if (!sendOK) {
					continue;
				}

				String feedback = "";
				int count = socketTimeout;
				while (count > 0) {
					feedback = socketRespData.get(socket);
					if (null != feedback && !"".equals(feedback)) {
						result = feedback;
						success = true;
						break;
					} else {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						count--;
					}
				}

			} catch (IOException e) {
				return "";// log
			} finally {
				if (null != socket) {
					try {
						sc.sendData(socket, "quit", null);
					} catch (IOException e) {
						e.printStackTrace();
					}
					sc.removeSocket(socket);
					socketRespData.remove(socket);
				}
			}

			if (success)
				break;
		}

		return result;
	}

	class RackSocketDataHandler extends SocketDataHandler {
		private RackSocketDataHandler() {
		}

		@Override
		public void doHandle(String in, Socket s) {
			if (null == s || null == in)
				return;
			String ip = s.getInetAddress().toString().replace("/", "");
			if ("127.0.0.1".equals(ip))
				ip = portMap.get("" + s.getPort());

			socketRespData.put(s, in);
			SocketData.setData(in);
		}
	}

	class sendMsgToServer implements Runnable {
		private String ip;
		private int port;
		private String signature;
		private String msg;

		public sendMsgToServer(String ip, int port, String msg, String signature) {
			this.ip = ip;
			this.port = port;
			this.msg = msg;
			this.signature = signature;
		}

		@Override
		public void run() {
			boolean OK = true;
			String result= sendInfo(ip, port, msg);
			if (null == result || result.equals("")) {
				OK = false;
			}
			if (!"".equals(signature))
				cmdFlag.put(signature, OK);
		}
	}
}
