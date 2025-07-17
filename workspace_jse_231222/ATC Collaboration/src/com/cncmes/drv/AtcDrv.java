package com.cncmes.drv;

import com.cncmes.base.ATC;

import com.cncmes.utils.ModBusUtil;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.sero.util.queue.ByteQueue;

import java.net.Socket;
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

    public static AtcDrv getInstance() {
        return atcDrv;
    }

    @Override
    public Boolean unloadToolFromCNC(String ip, int port,int toolNum) {
        try {
            //move atc's arm to cnc's tool

            ModbusMaster tcpMaster = ModBusUtil.getMaster(ip,port);
            ModBusUtil.modbusTCP16(slaveId,tcpMaster,102,new short[]{3,1,0});     //手臂将刀具安装至CNC，但不要松开

            //monitor atc action result
            for(int i =0 ;i<10 ;i++)
            {
                ByteQueue byteQueue = ModBusUtil.modbusTCP03(slaveId,tcpMaster,103,1);    //check the action result
                byte[] resultByte =byteQueue.peekAll();
                if(resultByte[1] == 1)
                {
                    break;
                }else if(resultByte[1] == 0 || resultByte[1] == 3)
                {
                    Thread.sleep(4000);
                    continue;
                }else if(resultByte[1] == 2){
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
                if(resultByte[1] == 1)
                {
                    break;
                }else if(resultByte[1] == 0 || resultByte[1] == 3)
                {
                    Thread.sleep(4000);
                    continue;
                }else if(resultByte[1] == 2){
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
                if(resultByte[1] == 1)
                {
                    break;
                }else if(resultByte[1] == 0 || resultByte[1] == 3)
                {
                    Thread.sleep(4000);
                    continue;
                }else if(resultByte[1] == 2){
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
                if(resultByte[1] == 1)
                {
                    break;
                }else if(resultByte[1] == 0 || resultByte[1] == 3)
                {
                    Thread.sleep(4000);
                    continue;
                }else if(resultByte[1] == 2){
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

    Boolean informCncLock(String cncIP,String cncPort)
    {
        CncHartFordDrv cncDrv = CncHartFordDrv.getInstance();
        return false;
    }
}
