package com.cncmes.base;

import com.cncmes.dto.Result;

public interface ATC {
    public Boolean unloadToolFromCNC(String ip,int port,int toolNum);
    public Boolean loadToolToCNC(String ip,int port,int toolNum);
    public Result loadActionUp(String atcIP, int atcPort, int toolNum);
    public Result loadActionDown(String atcIP,int atcPort);
    public Result unloadActionUp(String atcIP,int atcPort);
    public Result unloadActionDown(String atcIP,int atcPort,int toolNum);
    public Result checkToolStatus(String atcIP,int atcPort,int toolNum);
//    public Boolean closeGrip(String ip,int port);
//    public Boolean openGrip(String ip,int port);

}
