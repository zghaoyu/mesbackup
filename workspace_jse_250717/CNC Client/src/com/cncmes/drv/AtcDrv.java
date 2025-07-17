package com.cncmes.drv;

import com.cncmes.base.ATC;
import com.cncmes.base.RobotItems;
import com.cncmes.ctrl.SocketClient;
import com.cncmes.handler.SocketDataHandler;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.ModBusUtil;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.sero.util.queue.ByteQueue;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * *Zhong
 * *
 */
public class AtcDrv implements ATC {
    private static AtcDrv atcDrv = new AtcDrv();
    private static AtcSocketDataHandler atcHandle = atcDrv.new AtcSocketDataHandler();
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
            ModBusUtil.modbusTCP16(slaveId,tcpMaster,0,new short[]{(short) toolNum,3,1,0});
            //monitor atc action result
            for(int i =0 ;i<10 ;i++)
            {
                ByteQueue byteQueue = ModBusUtil.modbusTCP03(slaveId,tcpMaster,3,1);
                byte[] resultByte =byteQueue.peekAll();
                if(resultByte[1] == 1)
                {
                    break;
                }else if(resultByte[1] == 0)
                {
                    Thread.sleep(4000);
                    continue;
                }else return false;
            }

            //补cnc松气unlock的操作

            //take away cnc's tool
            ModBusUtil.modbusTCP16(slaveId,tcpMaster,0,new short[]{(short) toolNum,4,1,0});
            //monitor atc action result
            for(int i =0 ;i<10 ;i++)
            {
                ByteQueue byteQueue = ModBusUtil.modbusTCP03(slaveId,tcpMaster,3,1);
                byte[] resultByte =byteQueue.peekAll();
                if(resultByte[1] == 1)
                {
                    break;
                }else if(resultByte[1] == 0)
                {
                    Thread.sleep(4000);
                    continue;
                }else return false;
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
            ModBusUtil.modbusTCP16(slaveId,tcpMaster,0,new short[]{(short) toolNum,1,1,0});
            //monitor atc action result
            for(int i =0 ;i<10 ;i++)
            {
                ByteQueue byteQueue = ModBusUtil.modbusTCP03(slaveId,tcpMaster,3,1);
                byte[] resultByte =byteQueue.peekAll();
                if(resultByte[1] == 1)
                {
                    break;
                }else if(resultByte[1] == 0)
                {
                    Thread.sleep(4000);
                    continue;
                }else return false;
            }

            //补cnc的lock的操作

            //safety recovery atc's arm
            ModBusUtil.modbusTCP16(slaveId,tcpMaster,0,new short[]{(short) toolNum,2,1,0});
            //monitor atc action result
            for(int i =0 ;i<10 ;i++)
            {
                ByteQueue byteQueue = ModBusUtil.modbusTCP03(slaveId,tcpMaster,3,1);
                byte[] resultByte =byteQueue.peekAll();
                if(resultByte[1] == 1)
                {
                    break;
                }else if(resultByte[1] == 0)
                {
                    Thread.sleep(4000);
                    continue;
                }else return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     *
     * @param ip the robot IP
     * @param cmd the command name
     * @param timeout_s
     * @return
     */
    private ArrayList<Object> execute_cmd(String ip, int port,String cmd, int timeout_s) {
        ArrayList<Object> rtn = new ArrayList<Object>();
        boolean bOK = false, sendOK = false;
        Socket socket = null;
        SocketClient sc = SocketClient.getInstance();
        String cmdEndChr = "CR";

        try {
            socket = sc.connect(ip, port, atcHandle, cmdEndChr);
        } catch (IOException e) {
            LogUtils.commandLog(ip, "NG" + LogUtils.separator + cmd.split(",")[0] + LogUtils.separator + cmd
                    + LogUtils.separator + "Connect(" + ip + ":" + port + ") Failed:" + e.getMessage());
        }

        if (null != socket) {
            if (timeout_s <= 0)	timeout_s = 60;
            try {
//                sendOK = sc.sendData(socket, cmd, cmdEndChr);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                bw.write(cmd);
                bw.flush();
                sendOK = true;

                if (sendOK) {
                    int count = timeout_s * 1000 / socketRespTimeInterval;
                    while (count > 0) { // Command timeout check block
                        String feedback = (String) socketRespData.get(socket);
                        if (feedback != null) {
                            String[] data = feedback.split(",");
                            if (data[0].equals("OK")) {
                                rtn.add(0, true);
                                rtn.add(1,data[1]);
                                bOK = true;
                            }
                            if (data[0].equals("NG")) {
                                rtn.add(0, false);
                                rtn.add(1,feedback);
                            }
                            break;
                        }

                        try {
                            Thread.sleep(socketRespTimeInterval);
                        } catch (InterruptedException e) {
                        }
                        count--;
                    }
                }
            } catch (IOException e) {
                LogUtils.commandLog(ip, "NG" + LogUtils.separator + cmd.split(",")[0] + LogUtils.separator + cmd
                        + LogUtils.separator + "sendData(" + ip + ":" + port + ") Failed:" + e.getMessage());
            } finally {
                if(null != socket){
                    sc.removeSocket(socket);
                    socketRespData.remove(socket);
                }
            }
        }

        if (!bOK) {
            if (rtn.size() <= 0)
                rtn.add(0, bOK);
            if (rtn.size() == 1)
                rtn.add(1, "NG");
        }
        return rtn;
    }
    class AtcSocketDataHandler extends SocketDataHandler {
        private AtcSocketDataHandler(){}
        @Override
        public void doHandle(String in, Socket s) {
            socketRespData.put(s, in);
        }
    }
}
