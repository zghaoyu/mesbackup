package com.cncmes.handler;

import java.net.Socket;

public abstract class SocketDataHandler {
	public String ip;
	public int port;
	public abstract void doHandle(String in,Socket s);
}
