package com.cncmes.ctrl;

import java.util.ArrayList;
import java.util.HashMap;

import com.cncmes.rs232.RS232Tool;

import gnu.io.SerialPort;
import gnu.io.SerialPortEventListener;

/**
 * RS232 Communication Tool
 * @author LI ZI LONG
 *
 */
public class RS232Client {
	private static RS232Client rs232Client = new RS232Client();
	private static RS232Tool rs232tool = RS232Tool.getInstance();
	private HashMap<String,SerialPort> sPortMap = new HashMap<String,SerialPort>();
	private RS232Client(){}
	
	public static RS232Client getInstance(){
		return rs232Client;
	}
	
	public SerialPort openPort(String portName, int baudrate, int databits, int stopbits, int parity) throws Exception{
		SerialPort sp = null;
		try {
			removePort(portName);
			sp = rs232tool.openPort(portName, baudrate, databits, stopbits, parity);
			sPortMap.put(portName, sp);
		} catch (Exception e) {
			throw(new Exception("Open "+portName+" failed: "+e.getMessage()));
		}
		
		return sp;
	}
	
	public void closePort(String portName){
		SerialPort sp = removePort(portName);
		if(null != sp) rs232tool.closePort(sp);
	}
	
	public void addPortListener(SerialPort sp, SerialPortEventListener listener) throws Exception{
		try {
			rs232tool.addListener(sp, listener);
		} catch (Exception e) {
			throw(e);
		}
	}
	
	public ArrayList<String> getSerialPorts(){
		ArrayList<String> comPorts = rs232tool.getSystemComPort();
		return comPorts;
	}
	
	public void sendData(String portName, byte[] data) throws Exception{
		SerialPort sp = getPort(portName);
		if(null != sp){
			try {
				rs232tool.sendToPort(sp, data);
			} catch (Exception e) {
				throw(new Exception("Send data to "+portName+" port failed: "+e.getMessage()));
			}
		}else{
			throw(new Exception("Send data to "+portName+" failed: port is closed"));
		}
	}
	
	public String readData(String portName) throws Exception{
		SerialPort sp = getPort(portName);
		String r = "";
		if(null != sp){
			try {
				byte[] br = rs232tool.readFromPort(sp);
				r = byteToString(br);
			} catch (Exception e) {
				throw(new Exception("Read data from "+portName+" port failed: "+e.getMessage()));
			}
		}else{
			throw(new Exception("Read data from "+portName+" failed: port is closed"));
		}
		return r;
	}
	
	private SerialPort getPort(String portName){
		return sPortMap.get(portName);
	}
	
	private SerialPort removePort(String portName){
		SerialPort port = sPortMap.get(portName);;
		sPortMap.remove(portName);
		return port;
	}
	
	/**
	 * Keyence bar code reader SR-G100 decoding
	 * @param bs
	 * @return
	 */
	private String byteToString(byte[] bs){
		String s = "";
		
		String[] ascTable = {"","<SOH>","<STX>","<ETX>","<EOT>","<ENQ>","<ACK>","<BEL>","<BS>","<HT>","<LF>","<VT>","<CL>","<CR>","<SO>","<SI>",
				"<DLE>","<DC1>","<DC2>","<DC3>","<DC4>","<NAK>","<SYN>","<ETB>","<CAN>","<EM>","<SUB>","<ESC>","<FS>","<GS>","<RS>","<US>"};
		
		for(int i=0; i<bs.length; i++){
			if(bs[i]<32){
				s = s + ascTable[i];
			}else if(i<127){
				s = s + (char)bs[i];
			}else{
				s = s + "<del>";
			}
		}
		return s;
	}
}
