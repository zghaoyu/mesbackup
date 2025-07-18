package com.cncmes.utils;

import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.msg.*;
import com.serotonin.modbus4j.sero.util.queue.ByteQueue;

import java.util.Arrays;

/**
 * *Zhong
 * *
 */
public class ModBusUtil {
     /**
     * 工厂。
     */
    static ModbusFactory modbusFactory;

    //建立链接
    static public ModbusMaster tcpMaster;

    static {
        if (modbusFactory == null) {
            modbusFactory = new ModbusFactory();
//            tcpMaster = getMaster("10.10.206.250", 502);  // 填写要连接的TCP server
        }
    }

    /**
     * 获取master
     *
     * @return
     * @throws ModbusInitException
     */
    public static ModbusMaster getMaster(String ip, Integer port) {
        IpParameters params = new IpParameters();
        params.setHost(ip);
        params.setPort(port);

        // RTU 协议-设备是modbus rtu就用他 注意哦
        // params.setEncapsulated(true);
        // ModbusMaster master = modbusFactory.createTcpMaster(params, true);
        // TCP 协议
        ModbusMaster master = modbusFactory.createTcpMaster(params, false);

        try {
            //设置超时时间---发现设置他没用，还会报错，所以屏蔽
            // master.setTimeout(5000);
            //设置重连次数---发现设置他没用，还会报错，所以屏蔽
            //master.setRetries(3);
            //初始化
            master.init();
        } catch (ModbusInitException e) {
            e.printStackTrace();
        }
        return master;
    }

    /**
     * 读取保持寄存器 功能码[03]
     *
     * @param start     开始地址
     * @param readLenth 读取数量
     * @return
     * @throws ModbusInitException
     */
    public static ByteQueue modbusTCP03(int slaveId, ModbusMaster tcpMaster, int start, int readLenth) throws ModbusInitException {
        //发送请求
        ModbusRequest modbusRequest = null;
        try {
            modbusRequest = new ReadHoldingRegistersRequest(slaveId, start, readLenth);//功能码03
            System.out.println(modbusRequest.toString());
        } catch (ModbusTransportException e) {
            e.printStackTrace();
        }
        //收到响应
        ModbusResponse modbusResponse = null;
        try {
            modbusResponse = tcpMaster.send(modbusRequest);
        }  catch (ModbusTransportException e) {
            e.printStackTrace();
        }
        ByteQueue byteQueue = new ByteQueue(12);
        modbusResponse.write(byteQueue);
        System.out.println("功能码:" + modbusRequest.getFunctionCode());
        System.out.println("从站地址:" + modbusRequest.getSlaveId());
        System.out.println("开始地址:" + start);
        System.out.println("收到的响应信息大小:" + byteQueue.size());
        System.out.println("收到的响应信息值:" + byteQueue);
        return byteQueue;
    }

    /*
     * 写 多个寄存器 功能码[16] - 相当厂家文档写单个和多个。一样可以
     * WriteCoilRequest  05
     * WriteRegisterRequest 06
     * WriteRegistersRequest 16
     *
     */
    public static ByteQueue modbusTCP16(int slaveId, ModbusMaster tcpMaster, int writeOffset, short[] data) throws Exception {
        WriteRegistersRequest writeRegistersRequest = null;
        //收到响应
        ModbusResponse modbusResponse = null;
        try {
            writeRegistersRequest = new WriteRegistersRequest(slaveId, writeOffset, data);
            modbusResponse = tcpMaster.send(writeRegistersRequest);
            if (modbusResponse.isException()) {
                System.out.println("Exception response: message=" + modbusResponse.getExceptionMessage());
            } else {
                System.out.println("Success");
            }
        } catch (ModbusTransportException e) {
            e.printStackTrace();
        }
        ByteQueue byteQueue = new ByteQueue(12);
        modbusResponse.write(byteQueue);
        System.out.println("功能码:" + writeRegistersRequest.getFunctionCode());
        System.out.println("从站地址:" + writeRegistersRequest.getSlaveId());
        System.out.println("收到的响应信息大小:" + byteQueue.size());
        System.out.println("收到的响应信息值:" + byteQueue);
        return byteQueue;
    }

    public static ByteQueue modbusTCP06(int slaveId,ModbusMaster tcpMaster,int start,int value) throws Exception{
        WriteRegisterRequest writeRegisterRequest = null;
        //收到响应
        ModbusResponse modbusResponse = null;
        try {
            writeRegisterRequest = new WriteRegisterRequest(slaveId, start, value);
            modbusResponse = tcpMaster.send(writeRegisterRequest);
            if (modbusResponse.isException()) {
                System.out.println("Exception response: message=" + modbusResponse.getExceptionMessage());
            } else {
                System.out.println("Success");
            }
        } catch (ModbusTransportException e) {
            e.printStackTrace();
        }
        ByteQueue byteQueue = new ByteQueue(12);
        modbusResponse.write(byteQueue);
        System.out.println("功能码:" + writeRegisterRequest.getFunctionCode());
        System.out.println("从站地址:" + writeRegisterRequest.getSlaveId());
        System.out.println("收到的响应信息大小:" + byteQueue.size());
        System.out.println("收到的响应信息值:" + byteQueue);
        return byteQueue;
    }
    public static int[] registerValuetToDec(ByteQueue byteQueue)
    {
        int[] data = new int[byteQueue.size()];
        int length = byteQueue.size();

//        byte[] dataByte = byteQueue.peekAll();
//        String dataStr = ByteUtil.binary(dataByte,16);
//        System.out.println(dataStr);

//        for(int i = 0;i < 3;i++)
//        {
//            data[i] = byteQueue.pop();
//        }
//        byte[] dataByte = byteQueue.peekAll();
//        System.out.println(Arrays.toString(dataByte));
//        String dataStr = ByteUtil.binary(dataByte,16);
//        System.out.println(dataStr);


//        for(int i = 0;i<length;i++)
//        {
//            int high_pos =0;
//            int low_pos =0;
//            if(i<3) {
//                data[i] = byteQueue.pop();
//            }else {
//                byte high = byteQueue.pop();
//                if(high != 0)
//                {
//                    byte[] byteArray1 = new byte[]{high};
//                    high_pos = Integer.parseInt(new String(byteArray1));
//                }else high_pos =0;
//
////                int high_pos = Integer.parseInt(String.valueOf(byteQueue.pop()));
//                int value = high_pos*16*16;
//
//                byte low = byteQueue.pop();
//                if(low!=0)
//                {
//                    byte[] byteArray2 = new byte[]{low};
//                    low_pos = Integer.parseInt(new String(byteArray2));
//                }else low_pos = 0;
////                int low_pos = Integer.parseInt(String.valueOf(byteQueue.pop()));
//                value = value + low_pos;
//                i++;
//                data[i] =  value;
//            }
//
//        }

//        System.out.println("ToDec   " + Arrays.toString(data));
        return data;
    }

}
