package com.cncmes.dto;

/**
 * *Zhong
 * *
 */
public class CNCInfo {
    Integer id;
    String name;
    String ip;
    String cnc_port;
    String ftp_ip;
    String ftp_port;
    String machining_process;

    public CNCInfo(String name, String ip, String cnc_port, String ftp_ip, String ftp_port, String machining_process) {
        this.name = name;
        this.ip = ip;
        this.cnc_port = cnc_port;
        this.ftp_ip = ftp_ip;
        this.ftp_port = ftp_port;
        this.machining_process = machining_process;

    }

    public CNCInfo() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getCnc_port() {
        return cnc_port;
    }

    public void setCnc_port(String cnc_port) {
        this.cnc_port = cnc_port;
    }

    public String getFtp_ip() {
        return ftp_ip;
    }

    public void setFtp_ip(String ftp_ip) {
        this.ftp_ip = ftp_ip;
    }

    public String getFtp_port() {
        return ftp_port;
    }

    public void setFtp_port(String ftp_port) {
        this.ftp_port = ftp_port;
    }

    public String getMachining_process() {
        return machining_process;
    }

    public void setMachining_process(String machining_process) {
        this.machining_process = machining_process;
    }
}
