package com.cncmes.ctrl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.cncmes.handler.SocketDataHandler;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.NetUtils;
import com.cncmes.utils.RunningMsg;
import com.cncmes.utils.ThreadUtils;

/**
 * Socket Client
 * 
 * @author LI ZI LONG
 *
 */
public class SocketClient {
	private static SocketClient socketClient = new SocketClient();
	private List<Socket> socketList = new CopyOnWriteArrayList<Socket>();

	private SocketClient() {
	}

	public static SocketClient getInstance() {
		return socketClient;
	}

	private String getMapKey(String ip, int port) {
		return (ip + ":" + port);
	}

	public boolean portOfHostOK(String ip, int port, int timeout_s, int interval_s) {
		boolean ok = false;

		int loopCnt = timeout_s / interval_s;
		for (int i = 0; i <= loopCnt; i++) {
			try {
				Socket s = new Socket(ip, port);
				s.close();
				ok = true;
				break;
			} catch (Exception e) {
				try {
					Thread.sleep(interval_s * 1000);
				} catch (InterruptedException e1) {
				}
			}
		}

		return ok;
	}

	/**
	 * ����������boolean��ΪSocket HuiZhi 2022/5/20
	 * 
	 * @param ip
	 *            the host IP
	 * @param port
	 *            the port number
	 * @param handler
	 *            the SocketDataHandler
	 * @param cmdEndChr
	 *            the command end character (CRLF/CR/LF/HEX)
	 * @return socket, or null if connect failed
	 * @throws IOException
	 */
	public Socket connect(String ip, int port, SocketDataHandler handler, String cmdEndChr) throws IOException {
		Socket socket = null;

		if (null == handler) {
			LogUtils.errorLog(
					"SocketClient connect(" + getMapKey(ip, port) + ") failed:SocketDataHandler can't be NULL");
			throw (new IOException("Fail connecting to server: Client Socket Data Handler can't be NULL"));
		}

		boolean okToConnect = !("".equals(ip) || port <= 1024);

		if (okToConnect) {
			try {
				socket = new Socket(ip, port);
//				System.out.println("Connect:" + socket);// FIXME need to delete
				socketList.add(socket);
				ThreadUtils.Run(new SocketDataListener(socket, handler, cmdEndChr));

			} catch (IOException e) {
				LogUtils.errorLog("SocketClient connecSt(" + getMapKey(ip, port) + ") failed:" + e.getMessage()
						+ "\r\nPing(" + ip + "):" + NetUtils.pingHost(ip) + "\r\nNetstat(" + ip + "):\r\n"
						+ NetUtils.getNetworkStatus(ip));
				throw (new IOException("Fail connecting to server: " + e.getMessage()));
			}
		}

		return socket;
	}

	/**
	 * comment by Hui Zhi 2022/5/30
	 * 
	 * @param s
	 *            a socket
	 * @param data
	 * @param cmdEndChr
	 *            command end character
	 * @return true if send data successfully
	 * @throws IOException
	 */
	public boolean sendData(Socket s, String data, String cmdEndChr) throws IOException {

		boolean success = false;
		String err = "";
		if (null != s) {
			String ip = s.getInetAddress().getHostAddress();
			int port = s.getPort();

			if (null == cmdEndChr) {
				cmdEndChr = "\r";
			} else {
				switch (cmdEndChr) {
				case "CRLF":
					cmdEndChr = "\r\n";
					break;
				case "CR":
					cmdEndChr = "\r";
					break;
				case "LF":
					cmdEndChr = "\n";
					break;
				case "HEX":
					break;
				}
			}

			try {
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
				if ("HEX".equals(cmdEndChr)) {
					bw.write(Integer.parseInt(data, 16));
				} else {
					bw.write(data + cmdEndChr);
				}
				bw.flush();
//				System.out.println(data);
//				System.out.println("Client send data :" + data);// TODO delete
				success = true;
			} catch (IOException e) {
				err = e.getMessage();
				LogUtils.errorLog("SocketClient sendData(" + getMapKey(ip, port) + ") failed:" + err);
				throw (new IOException("Fail sending data to server: " + err));

			} finally {
				if ("quit".equals(data)) {
					removeSocket(s);
					success = true;
				}
			}
		}

		if (!success)
			LogUtils.errorLog("SocketClient fails sending data to server: " + data);
		return success;
	}

	public List<Socket> getSocket(String ip, int port) {
		List<Socket> result = new ArrayList<>();

		if (socketList.size() <= 0) {
			return result;
		}

		Iterator<Socket> it = socketList.iterator();
		while (it.hasNext()) {
			Socket s = it.next();
			if (s.getInetAddress().equals(ip) && s.getPort() == port) {
				result.add(s);
			}
		}

		return result;
	}

	public List<Socket> getAllSockets() {
		return socketList;
	}

	public Socket removeSocket(Socket s) {
		try {
			if (null != s) {
				socketList.remove(s);
				s.close();
//				System.out.println("Client close the connection: " + s);// TODO delete
			}
		} catch (IOException e) {
		}

		return s;
	}

	// add by Hui Zhi 2022/5/23
	public void disconnectAllSockets() {
		if (!socketList.isEmpty()) {
			for (Socket s : socketList) {
				removeSocket(s);
			}
		}
	}

	class SocketDataListener implements Runnable {
		private Socket socket = null;
		private SocketDataHandler handler = null;
		private String cmdEndChr = "";
		private String ip = "";
		private int port;

		public SocketDataListener(Socket s, SocketDataHandler handler, String cmdEndChr) {
			this.socket = s;
			this.cmdEndChr = cmdEndChr;
			this.handler = handler;
			this.ip = s.getInetAddress().getHostAddress();
			this.port = s.getPort();
		}

		@Override
		public void run() {
			String errMsg = "";
			BufferedReader br = null;
			try {
				String r = null;
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				while (!socket.isClosed()) {

					// ����
					if ("HEX".equals(cmdEndChr)) {
						r = "" + br.read();
						if (r.equals("-1")) { // disconnect
							break;
						}
					} else {
						r = br.readLine();
						if (null == r) { // disconnect
							break;
						}
					}

					if (null != handler) {
//						System.out.println("Client receive data is :" + r);// TODO delete
						handler.doHandle(r, socket);
					}

					if ("quit".equals(r))
						break;
				}
				br.close();
			} catch (IOException e) {
				removeSocket(socket);
				errMsg = e.getMessage();
			} finally {
				if ("".equals(errMsg)) {
					RunningMsg.set(
							"SocketClient SocketDataListener(" + getMapKey(ip, port) + ") Stop - " + socket.isClosed());
				} else {
					LogUtils.errorLog("SocketClient SocketDataListener(" + getMapKey(ip, port) + ") Stop:" + errMsg);
				}
			}
		}
	}

}
