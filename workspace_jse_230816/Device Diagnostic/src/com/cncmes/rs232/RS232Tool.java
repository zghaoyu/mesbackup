package com.cncmes.rs232;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

public class RS232Tool {
	private static RS232Tool rs232 = new RS232Tool();
	
	private RS232Tool(){}
	
	public static RS232Tool getInstance(){
		return rs232;
	}
	
	public ArrayList<String> getSystemComPort() {
        @SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();	
        ArrayList<String> portNameList = new ArrayList<>();
        
        while (portList.hasMoreElements()) {
            String portName = portList.nextElement().getName();
            portNameList.add(portName);
        }

        return portNameList;
    }
    
    public SerialPort openPort(String portName, int baudrate, int databits, int stopbits, int parity) throws Exception {
	    CommPortIdentifier portIdentifier = null;
	    CommPort commPort = null;
	    SerialPort serialPort = null;
	    
		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
			commPort = portIdentifier.open(portName, 2000);
			serialPort = (SerialPort)commPort;
			serialPort.setSerialPortParams(baudrate, databits, stopbits, parity);
		} catch (NoSuchPortException e) {
			throw new Exception("Open "+portName+" fail - NoSuchPortException");
		} catch (PortInUseException e) {
			throw new Exception("Open "+portName+" fail - PortInUseException");
		} catch (UnsupportedCommOperationException e) {
			throw new Exception("Open "+portName+" fail - UnsupportedCommOperationException");
		}
        
        return serialPort;
    }
    
    public void closePort(SerialPort serialPort) {
    	serialPort.close();
    }
    
    public void sendToPort(SerialPort serialPort, byte[] order) throws Exception {
    	OutputStream out = null;
        try {
			out = serialPort.getOutputStream();
			out.write(order);
	        out.flush();
	        out.close();
		} catch (IOException e) {
			throw new Exception("Send data to serial port fail - IOException");
		}
    }
    
    public byte[] readFromPort(SerialPort serialPort) throws Exception {
    	InputStream in = null;
        byte[] bytes = null;
        int bufflenth = 0;
        
    	try {
			in = serialPort.getInputStream();
			bufflenth = in.available();
			
			while (bufflenth != 0) {                             
	            bytes = new byte[bufflenth];
	            in.read(bytes);
	            bufflenth = in.available();
	    	}
	    	in.close();
		} catch (IOException e) {
			throw new Exception("Read data from serial port fail - IOException");
		}
    	
        return bytes;
    }
    
    public void addListener(SerialPort port, SerialPortEventListener listener) throws Exception {
        try {
			port.addEventListener(listener);
			port.notifyOnDataAvailable(true);
	        port.notifyOnBreakInterrupt(true);
		} catch (TooManyListenersException e) {
			throw new Exception("Add listener to serial port fail - TooManyListenersException");
		}
    }
}
