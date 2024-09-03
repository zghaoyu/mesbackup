package com.cncmes.ctrl;

import com.cncmes.dto.ATC;
import com.cncmes.service.AtcService;
import com.cncmes.thread.AtcMonitor;
import com.cncmes.thread.CncMonitor;
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
            Callable<Integer> Task = () -> {
                AtcMonitor atcMonitor = new AtcMonitor(atc.getIp(),atc.getPort());
                Thread atcThread = new Thread(atcMonitor);
                atcThread.setName("Thread_ATC_"+atc.getIp());
                atcThread.start();

                CncMonitor cncMonitor = new CncMonitor(atc.getCnc_ip(),atc.getCnc_port());
                Thread cncThread = new Thread(cncMonitor);
                cncThread.setName("Thread_CNC_"+atc.getCnc_ip());
                cncThread.start();

                return 1;
            };
        }


        return "OK";
    }



    public String stop(){
        return "";
    }

}
