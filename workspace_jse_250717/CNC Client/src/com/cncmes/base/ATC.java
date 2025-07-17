package com.cncmes.base;

public interface ATC {
    public Boolean unloadToolFromCNC(String ip,int port,int toolNum);
    public Boolean loadToolToCNC(String ip,int port,int toolNum);
//    public Boolean closeGrip(String ip,int port);
//    public Boolean openGrip(String ip,int port);

}
