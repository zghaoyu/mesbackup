package com.cncmes.handler;

import java.net.Socket;

public interface SocketRespHandler {
	public void doHandle(String in,Socket s);
}
