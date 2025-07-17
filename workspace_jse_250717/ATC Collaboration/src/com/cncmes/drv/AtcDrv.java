package com.cncmes.drv;

import com.cncmes.base.ATC;

import com.cncmes.dto.Result;
import com.cncmes.handler.impl.AtcDataHandler;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.ModBusUtil;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.sero.util.queue.ByteQueue;

import java.net.Socket;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * *Zhong
 * *
 */
public class AtcDrv implements ATC {
    private static AtcDrv atcDrv = new AtcDrv();
//    private static AtcSocketDataHandler atcHandle = atcDrv.new AtcSocketDataHandler();
    private static int socketRespTimeInterval = 10;//milliseconds
    private Map<Socket,String> socketRespData = new ConcurrentHashMap<Socket, String>();
    private int slaveId = 1;
    private AtcDataHandler atcDataHandler = new AtcDataHandler();
    public static AtcDrv getInstance() {
        return atcDrv;
    }
    private int checkCycle = 80;
    private int timeInterval = 2000;
    @Override
    public Boolean unloadToolFromCNC(String ip, int port,int toolNum) {
        try {
            //move atc's arm to cnc's tool

            ModbusMaster tcpMaster = ModBusUtil.getMaster(ip,port);
            ModBusUtil.modbusTCP16(slaveId,tcpMaster,102,new short[]{3,1,0});     //手臂将刀具安装至CNC，但不要松开

            //monitor atc action result
            for(int i = 0 ;i<10 ;i++)
            {
                ByteQueue byteQueue = ModBusUtil.modbusTCP03(slaveId,tcpMaster,103,1);    //check the action result
                byte[] resultByte =byteQueue.peekAll();
                if(resultByte[4] == 1)
                {
                    break;
                }else if(resultByte[4] == 0 || resultByte[4] == 3)
                {
                    Thread.sleep(4000);
                    continue;
                }else if(resultByte[4] == 2){
                    ByteQueue errorQueue = ModBusUtil.modbusTCP03(slaveId,tcpMaster,104,1);     //check the error reason
                    return false;
                }
            }

            //补cnc松气unlock的操作

            //take away cnc's tool
            ModBusUtil.modbusTCP16(slaveId,tcpMaster,101,new short[]{(short) toolNum,4,1,0});
            //monitor atc action result
            for(int i =0 ;i<10 ;i++)
            {
                ByteQueue byteQueue = ModBusUtil.modbusTCP03(slaveId,tcpMaster,103,1);
                byte[] resultByte =byteQueue.peekAll();
                if(resultByte[4] == 1)
                {
                    break;
                }else if(resultByte[4] == 0 || resultByte[4] == 3)
                {
                    Thread.sleep(4000);
                    continue;
                }else if(resultByte[4] == 2){
                    ByteQueue errorQueue = ModBusUtil.modbusTCP03(slaveId,tcpMaster,104,1);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Boolean loadToolToCNC(String ip, int port,int toolNum) {
        ModbusMaster tcpMaster = ModBusUtil.getMaster(ip,port);
        try {
            //move atc's arm and install tool to cnc
            ModBusUtil.modbusTCP16(slaveId,tcpMaster,101,new short[]{(short) toolNum,1,0});
            //monitor atc action result
            for(int i =0 ;i<10 ;i++)
            {
                ByteQueue byteQueue = ModBusUtil.modbusTCP03(slaveId,tcpMaster,103,1);
                byte[] resultByte =byteQueue.peekAll();
                if(resultByte[4] == 1)
                {
                    break;
                }else if(resultByte[4] == 0 || resultByte[4] == 3)
                {
                    Thread.sleep(4000); 
                    continue;
                }else if(resultByte[4] == 2){
                    ByteQueue errorQueue = ModBusUtil.modbusTCP03(slaveId,tcpMaster,104,1);
                    return false;
                }
            }

            //补cnc的lock的操作

            //safety recovery atc's arm
            ModBusUtil.modbusTCP16(slaveId,tcpMaster,102,new short[]{2,0});
            //monitor atc action result
            for(int i =0 ;i<10 ;i++)
            {
                ByteQueue byteQueue = ModBusUtil.modbusTCP03(slaveId,tcpMaster,103,1);
                byte[] resultByte =byteQueue.peekAll();
                if(resultByte[4] == 1)
                {
                    break;
                }else if(resultByte[4] == 0 || resultByte[4] == 3)
                {
                    Thread.sleep(4000);
                    continue;
                }else if(resultByte[4] == 2){
                    ByteQueue errorQueue = ModBusUtil.modbusTCP03(slaveId,tcpMaster,104,1);
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public Result loadActionUp(String atcIP, int atcPort, int toolNum)
    {
        ModbusMaster tcpMaster = ModBusUtil.getMaster(atcIP,atcPort);
        Boolean result = false;
        String errorCause = "Execute load action 1 time out";
        long executeTime = 0;
        try {
            //move atc's arm and install tool to cnc
            ModBusUtil.modbusTCP16(slaveId,tcpMaster,101,new short[]{(short) toolNum,1});
            Date begin = new Date();
            //monitor atc action result
            for(int i =0 ;i<checkCycle ;i++)
            {
                ByteQueue byteQueue = ModBusUtil.modbusTCP03(slaveId,tcpMaster,103,1);
                byte[] resultByte =byteQueue.peekAll();
                if(resultByte[4] == 1)
                {

                    result = true;
                    errorCause = "Load up action success";
                    break;
                }else if(resultByte[4] == 0 || resultByte[4] == 3)
                {
                    Thread.sleep(timeInterval);
                }else if(resultByte[4] == 2){

                    ByteQueue errorQueue = ModBusUtil.modbusTCP03(slaveId,tcpMaster,104,1);
                    byte[] errorByte = errorQueue.peekAll();
                    errorCause = atcDataHandler.atcErrorResultHandler(errorByte[4]);
                    break;
                }
            }
            Date end = new Date();
            executeTime = (end.getTime() - begin.getTime())/1000;
            errorCause = errorCause + "; Execute Time : "+executeTime+"s";

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(result,errorCause);
    }
    @Override
    public Result loadActionDown(String atcIP,int atcPort)
    {
        ModbusMaster tcpMaster = ModBusUtil.getMaster(atcIP,atcPort);
        Boolean result = false;
        String errorCause = "Execute load action 2 time out";
        long executeTime = 0;
        try {
            //move atc's arm and install tool to cnc
            ModBusUtil.modbusTCP16(slaveId,tcpMaster,102,new short[]{2});
            Date begin = new Date();
            //monitor atc action result
            for(int i =0 ;i<checkCycle ;i++)
            {
                ByteQueue byteQueue = ModBusUtil.modbusTCP03(slaveId,tcpMaster,103,1);
                byte[] resultByte =byteQueue.peekAll();
                if(resultByte[4] == 1)
                {

                    result = true;
                    errorCause = "load down action success";
                    break;
                }else if(resultByte[4] == 0 || resultByte[4] == 3)
                {
                    Thread.sleep(timeInterval);
                }else if(resultByte[4] == 2){

                    ByteQueue errorQueue = ModBusUtil.modbusTCP03(slaveId,tcpMaster,104,1);
                    byte[] errorByte = errorQueue.peekAll();
                    errorCause = atcDataHandler.atcErrorResultHandler(errorByte[4]);
                    break;
                }

            }
            Date end = new Date();
            executeTime = (end.getTime() - begin.getTime())/1000;
            errorCause = errorCause + "; Execute Time : "+executeTime+"s";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(result,errorCause);
    }
    @Override
    public Result unloadActionUp(String atcIP,int atcPort)
    {
        ModbusMaster tcpMaster = ModBusUtil.getMaster(atcIP,atcPort);
        Boolean result = false;
        String errorCause = "Execute unload action 3 time out";
        long executeTime = 0;
        try {
            ModBusUtil.modbusTCP16(slaveId,tcpMaster,102,new short[]{3});     //手臂将刀具安装至CNC，但不要松开
            Date begin = new Date();
            //monitor atc action result
            for(int i =0 ;i<checkCycle ;i++)
            {
                ByteQueue byteQueue = ModBusUtil.modbusTCP03(slaveId,tcpMaster,103,1);    //check the action result
                byte[] resultByte =byteQueue.peekAll();
                if(resultByte[4] == 1)
                {

                    result = true;
                    errorCause = "unload up action success";
                    break;
                }else if(resultByte[4] == 0 || resultByte[4] == 3)    // uncompleted  status
                {
                    Thread.sleep(timeInterval);
                }else if(resultByte[4] == 2){

                    ByteQueue errorQueue = ModBusUtil.modbusTCP03(slaveId,tcpMaster,104,1);     //check the error reason
                    byte[] errorByte = errorQueue.peekAll();
                    errorCause = atcDataHandler.atcErrorResultHandler(errorByte[4]);
                    break;
                }
            }
            Date end = new Date();
            executeTime = (end.getTime() - begin.getTime())/1000;
            errorCause = errorCause + "; Execute Time : "+executeTime+"s";
        }catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(result,errorCause);
    }
    @Override
    public Result unloadActionDown(String atcIP,int atcPort,int toolNum)
    {
        ModbusMaster tcpMaster = ModBusUtil.getMaster(atcIP,atcPort);
        Boolean result = false;
        String errorCause = "Execute unload action 4 time out";
        long executeTime = 0;
        try {
            ModBusUtil.modbusTCP16(slaveId,tcpMaster,101,new short[]{(short) toolNum,4});    //手臂将刀具安装至CNC，但不要松开
            Date begin  = new Date();
            //monitor atc action result
            for(int i =0 ;i<checkCycle ;i++)
            {
                ByteQueue byteQueue = ModBusUtil.modbusTCP03(slaveId,tcpMaster,103,1);    //check the action result
                byte[] resultByte =byteQueue.peekAll();
                if(resultByte[4] == 1)
                {

                    result = true;
                    errorCause = "unload down action success";
                    break;
                }else if(resultByte[4] == 0 || resultByte[4] == 3)
                {
                    Thread.sleep(timeInterval);
                }else if(resultByte[4] == 2){

                    ByteQueue errorQueue = ModBusUtil.modbusTCP03(slaveId,tcpMaster,104,1);     //check the error reason
                    byte[] errorByte = errorQueue.peekAll();
                    errorCause = atcDataHandler.atcErrorResultHandler(errorByte[4]);
                    break;
                }
            }
            Date end = new Date();
            executeTime = (end.getTime() - begin.getTime())/1000;
            errorCause = errorCause + "; Execute Time : "+executeTime+"s";
        }catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(result,errorCause);
    }

    @Override
    public Result checkToolStatus(String atcIP,int atcPort,int toolNum) {
        ModbusMaster tcpMaster = ModBusUtil.getMaster(atcIP,atcPort);
        Boolean result = false;
        String errorCause = "Time out";
        try {
            ByteQueue byteQueue = ModBusUtil.modbusTCP03(slaveId,tcpMaster,toolNum,1);    //check the action result
            byte[] resultByte =byteQueue.peekAll();
            if(resultByte[4] == 1)
            {
                errorCause = "No error, tool library have "+toolNum+" tool";
                result = true;
            }else {
                errorCause = "Error, tool library don't have "+toolNum+" tool";
                result = false;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(result,errorCause);
    }



}
