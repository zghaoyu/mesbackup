package com.cncmes.thread;

import com.cncmes.base.CNC;
import com.cncmes.drv.AtcDrv;
import com.cncmes.drv.CncHartFordDrv;
import com.cncmes.dto.Result;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.RunningMsgUtil;

/**
 * *Zhong
 * *
 */
public class ProcessMonitor implements Runnable{
    private String cncIP;
    private int cncPort;
    private String atcIP;   //inform atc that cnc have open/close gas
    private int atcPort;
    private int toolRegister;    //record CNC's tool num

    CNC cncCtrl = CncHartFordDrv.getInstance();
    AtcDrv atcDrv = AtcDrv.getInstance();

    public ProcessMonitor(String cncIP, int cncPort, String atcIP, int atcPort) {
        this.cncIP = cncIP;
        this.cncPort = cncPort;
        this.atcIP = atcIP;
        this.atcPort = atcPort;
    }

    public ProcessMonitor() {
    }

    @Override
    public void run() {
        int loadToolID = 0;
        int unloadToolID = 0;
        toolRegister = 1;
        while (!ThreadController.isStopCncMonitorThread())
        {
            loadToolID = (int) cncCtrl.getMacro(cncIP,cncPort,950);//get cnc signal for load tool
            loadToolID = loadToolID - 30;

            unloadToolID = (int) cncCtrl.getMacro(cncIP,cncPort,951);//get cnc signal for unload tool
            unloadToolID = unloadToolID - 30;

            if(unloadToolID >= 1 && unloadToolID <= 20)
            {
                System.out.println("unload tool_"+unloadToolID+" from CNC");
                RunningMsgUtil.appendMsgArea("cnc_"+cncIP+" request to unload tool num "+unloadToolID);
                LogUtils.atcAndCncLog(cncIP,"cnc_"+cncIP+" request to unload tool num "+unloadToolID);
//
//                if(!cncCtrl.openSideDoor(cncIP,cncPort))    //open side door
//                {
//                    RunningMsgUtil.appendMsgArea("cnc_"+cncIP+" open side door error");
//                    LogUtils.atcAndCncLog(cncIP,"cnc_"+cncIP+" open side door error");
//                    return;
//                }else {
//                    RunningMsgUtil.appendMsgArea("cnc_"+cncIP+" open side door successfully");
//                    LogUtils.atcAndCncLog(cncIP,"cnc_"+cncIP+" open side door successfully");
//                }

                RunningMsgUtil.appendMsgArea("cnc_"+cncIP+" begin to unload tool.");
                LogUtils.atcAndCncLog(cncIP,"cnc_"+cncIP+" begin to unload tool.");

                Result checkUnloadTool = atcDrv.checkToolStatus(atcIP,atcPort,unloadToolID);
                if(checkUnloadTool.getResult())
                {
                    RunningMsgUtil.appendMsgArea("unload tool fail,ATC relative position already have tool.");
                    LogUtils.atcAndCncLog(cncIP,"unload tool fail,ATC relative position already have tool.");
                    return;
                }

                Result unloadUpResult = atcDrv.unloadActionUp(atcIP, atcPort);
                if (!unloadUpResult.getResult()){
                    RunningMsgUtil.appendMsgArea("unload tool fail.");
                    LogUtils.atcAndCncLog(cncIP,"unload tool fail,error cause : "+unloadUpResult.getErrorCasuse());
                    return;
                }else {
                    LogUtils.atcAndCncLog(cncIP,"Execute Unload action 3 successfully");
                }
//                if(cncCtrl.unlockTool(cncIP,cncPort))
//                {
//                    RunningMsgUtil.appendMsgArea("CNC unlock tool successfully.");
//                    LogUtils.atcAndCncLog(cncIP,"CNC unlock tool successfully.");
//                }else {
//                    RunningMsgUtil.appendMsgArea("CNC unlock tool fail.");
//                    LogUtils.atcAndCncLog(cncIP,"CNC unlock tool fail.");
//                    return;
//                }
//                try {
//                    Thread.sleep(3000); //wait for cnc unlock tool.
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                Result unloadDownResult = atcDrv.unloadActionDown(atcIP,atcPort,unloadToolID);
                if(!unloadDownResult.getResult()){
                    RunningMsgUtil.appendMsgArea("Unload tool fail.");
                    LogUtils.atcAndCncLog(cncIP,"Unload tool fail,error cause : "+unloadDownResult.getErrorCasuse());
                    return;
                }else {
                    LogUtils.atcAndCncLog(cncIP,"Execute unload action 4 successfully");
                }
                RunningMsgUtil.appendMsgArea("cnc_"+cncIP+" unload tool successfully.");
                LogUtils.atcAndCncLog(cncIP,"cnc_"+cncIP+" unload tool successfully.");
//
//                    if(!cncCtrl.closeSideDoor(cncIP,cncPort))    //close side door
//                    {
//                        RunningMsgUtil.appendMsgArea("cnc_"+cncIP+" close side door error");
//                        LogUtils.atcAndCncLog(cncIP,"cnc_"+cncIP+" close side door error");
//                        return;
//                    }else {
//                        RunningMsgUtil.appendMsgArea("cnc_"+cncIP+" close side door successfully");
//                        LogUtils.atcAndCncLog(cncIP,"cnc_"+cncIP+" close side door successfully");
            }else {
                RunningMsgUtil.appendMsgArea("cnc_"+cncIP+" unload macro = "+unloadToolID);
            }
            if(loadToolID >= 1 && loadToolID <= 20)
            {
                Result checkToolResult = atcDrv.checkToolStatus(atcIP,atcPort,loadToolID);
                if (!checkToolResult.getResult())
                {
                    RunningMsgUtil.appendMsgArea(checkToolResult.getErrorCasuse());
                    LogUtils.atcAndCncLog(cncIP,checkToolResult.getErrorCasuse());
                    return;
                }else {
                    RunningMsgUtil.appendMsgArea("ATC check tool successfully");
                }

//                    if(!cncCtrl.openSideDoor(cncIP,cncPort))    //open side door
//                    {
//                        RunningMsgUtil.appendMsgArea("cnc_"+cncIP+" open side door error");
//                        LogUtils.atcAndCncLog(cncIP,"cnc_"+cncIP+" open side door error");
//                        return;
//                    }else {
//                        RunningMsgUtil.appendMsgArea("cnc_"+cncIP+" open side door successfully");
//                        LogUtils.atcAndCncLog(cncIP,"cnc_"+cncIP+" open side door successfully");
//                    }

                RunningMsgUtil.appendMsgArea("cnc_"+cncIP+" begin to load tool.");
                LogUtils.atcAndCncLog(cncIP,"cnc_"+cncIP+" begin to load tool.");
                Result loadUpResult = atcDrv.loadActionUp(atcIP,atcPort, loadToolID);
                if(!loadUpResult.getResult()){
                    RunningMsgUtil.appendMsgArea("ATC load tool fail.");
                    LogUtils.atcAndCncLog(cncIP,"load tool fail,error cause : "+loadUpResult.getErrorCasuse());
                    return;
                }else {
                    LogUtils.atcAndCncLog(cncIP,"Execute load action 1 successfully");
                }
//                if(cncCtrl.lockTool(cncIP,cncPort))
//                {
//                    RunningMsgUtil.appendMsgArea("CNC lock tool successfully.");
//                    LogUtils.atcAndCncLog(cncIP,"CNC lock tool successfully.");
//                }else {
//                    RunningMsgUtil.appendMsgArea("CNC lock tool fail.");
//                    LogUtils.atcAndCncLog(cncIP,"CNC lock tool fail.");
//                    return;
//                }
//                try {
//                    Thread.sleep(3000); //wait for cnc unlock tool.
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                Result loadDownResult = atcDrv.loadActionDown(atcIP,atcPort);
                if(!loadDownResult.getResult()){
                    LogUtils.atcAndCncLog(cncIP,"load tool fail,error cause : "+loadDownResult.getErrorCasuse());
                    return;
                }else {
                    LogUtils.atcAndCncLog(cncIP,"Execute load action 2 successfully");
                }
                LogUtils.atcAndCncLog(cncIP,"ATC load tool to cnc_"+ cncIP +" successfully.");
                RunningMsgUtil.appendMsgArea("ATC load tool to cnc_"+ cncIP +" successfully.");

//                    if(!cncCtrl.closeSideDoor(cncIP,cncPort))    //close side door
//                    {
//                        RunningMsgUtil.appendMsgArea("cnc_"+cncIP+" close side door error");
//                        LogUtils.atcAndCncLog(cncIP,"cnc_"+cncIP+" close side door error");
//                        return;
//                    }else {
//                        RunningMsgUtil.appendMsgArea("cnc_"+cncIP+" close side door successfully");
//                        LogUtils.atcAndCncLog(cncIP,"cnc_"+cncIP+" close side door successfully");
//                    }

            }else {
                RunningMsgUtil.appendMsgArea("cnc_"+cncIP+" load macro = "+loadToolID);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }
        if(ThreadController.isStopCncMonitorThread()) LogUtils.atcAndCncLog(cncIP,"CNC monitor program stop");
    }

    public String getCncIP() {
        return cncIP;
    }

    public void setCncIP(String cncIP) {
        this.cncIP = cncIP;
    }

    public int getCncPort() {
        return cncPort;
    }

    public void setCncPort(int cncPort) {
        this.cncPort = cncPort;
    }

    public String getAtcIP() {
        return atcIP;
    }

    public void setAtcIP(String atcIP) {
        this.atcIP = atcIP;
    }

    public int getAtcPort() {
        return atcPort;
    }

    public void setAtcPort(int atcPort) {
        this.atcPort = atcPort;
    }
}
