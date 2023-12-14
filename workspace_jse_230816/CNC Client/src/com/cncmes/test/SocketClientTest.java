package com.cncmes.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.Socket;

import org.junit.Test;

import com.cncmes.ctrl.SocketClient;
import com.cncmes.ctrl.SocketServer;
import com.cncmes.handler.SocketDataHandler;
import com.cncmes.handler.SocketRespHandler;

public class SocketClientTest {
	private String ip = "10.10.206.113";
	private int port = 1234;
	private String cmdEndChr = "LF";

	@Test
	public void test() {
		SocketServer server = SocketServer.getInstance();
		SocketClient client = SocketClient.getInstance();
		SocketRespHandler serverHandler = new SocketRespHandler() {

			@Override
			public void doHandle(String in, Socket s) {

			}

		};
		SocketDataHandler clientHandler = new SocketDataHandler() {

			@Override
			public void doHandle(String in, Socket s) {

			}

		};

		try {
			server.socketSvrStart(port, serverHandler);
			Socket s = client.connect(ip, port, clientHandler, cmdEndChr);
			try {
				Thread.sleep(2000);
//			client.sendData(s, "quit", cmdEndChr);
//			Thread.sleep(2000);
			client.removeSocket(s);
			Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		} catch (IOException  e) {
			e.printStackTrace();
		}
	}

}
