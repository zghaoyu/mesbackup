package com.cncmes.base;

import java.util.LinkedHashMap;

public interface CNC {
    /**
     * @param ip the IP address of the CNC
     * @param operationName the operation to be executed
     * @param inParas input parameters, key is the parameter name and value is the parameter value
     * @param outParas output parameters, key is the parameter name and value is the parameter value
     * @return true if the specified operation is executed successfully
     */
    public boolean sendCommand(String ip, int port, String operationName, LinkedHashMap<String, String> inParas, LinkedHashMap<String, String> outParas);
    /**
     * @param ip the IP address of the CNC
     * @param programID the program to be executed by CNC
     * @return true if the program is started successfully
     */
    public boolean startMachining(String ip, int port,String programID);
    public double getMacro(String cncIp, int cncPort, double address);
    public boolean setMacro(String cncIP,int cncPort, String address, String value, String dec);
    public boolean openSideDoor(String cncIp, int cncPort);
    public boolean closeSideDoor(String cncIP,int cncPort);
    public Boolean checkSensor(String cncip, String sensorAddress, String value);
    public Boolean lockTool(String cncip,int port);
    public Boolean unlockTool(String cncip,int port);
    public DeviceState getMachineState(String ip,int port);

}
