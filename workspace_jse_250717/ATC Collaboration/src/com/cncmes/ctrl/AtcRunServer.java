package com.cncmes.ctrl;

import com.cncmes.dto.ATC;
import com.cncmes.service.AtcService;
import com.cncmes.thread.AtcMonitor;
import com.cncmes.thread.CncMonitor;

import com.cncmes.thread.ProcessMonitor;
import com.cncmes.thread.ThreadController;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.RunningMsgUtil;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * *Zhong
 * *
 */
public class AtcRunServer {
    private static AtcRunServer atcRunServer = new AtcRunServer();
    AtcService atcService = new AtcService();
    public static AtcRunServer getInstance(){
        return atcRunServer;
    }
    public String start(){
        List<ATC> atcList = atcService.getAllATC();
        for(ATC atc : atcList)
        {
                ThreadController.startCncMonitor();

//                CncMonitor cncMonitor = new CncMonitor(atc.getCnc_ip(),atc.getCnc_port(),atc.getIp(),atc.getPort());
//                Thread cncThread = new Thread(cncMonitor);
//                cncThread.setName("Thread_CNC_"+atc.getCnc_ip());
//                cncThread.start();

//                ThreadController.startAtcMonitor();
//                AtcMonitor atcMonitor = new AtcMonitor(atc.getIp(),atc.getPort(),atc.getCnc_ip(),atc.getCnc_port());
//                Thread atcThread = new Thread(atcMonitor);
//                atcThread.setName("Thread_ATC_"+atc.getIp());
//                atcThread.start();

            ProcessMonitor processMonitor = new ProcessMonitor(atc.getCnc_ip(),atc.getCnc_port(),atc.getIp(),atc.getPort());
            Thread  processThread = new Thread(processMonitor);
            processThread.setName("Thread_CNC_"+atc.getCnc_ip());
            processThread.start();
            LogUtils.atcAndCncLog(atc.getCnc_ip(),"ATC_"+atc.getIp()+" monitor CNC_"+atc.getCnc_ip()+" start");


        }


        return "OK";
    }

    public String stop(){

        return "";
    }

}
