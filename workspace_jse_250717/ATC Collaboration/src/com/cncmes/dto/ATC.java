package com.cncmes.dto;

/**
 * *Zhong
 * *
 */
public class ATC {
    private int id;
    private String name;
    private String ip;
    private int port;
    private String cnc_ip;
    private int cnc_port;

    public ATC(int id, String name, String ip, int port, String cnc_ip, int cnc_port) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.cnc_ip = cnc_ip;
        this.cnc_port = cnc_port;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getCnc_ip() {
        return cnc_ip;
    }

    public void setCnc_ip(String cnc_ip) {
        this.cnc_ip = cnc_ip;
    }

    public int getCnc_port() {
        return cnc_port;
    }

    public void setCnc_port(int cnc_port) {
        this.cnc_port = cnc_port;
    }

    @Override
    public String toString() {
        return "ATC{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                ", cnc_ip='" + cnc_ip + '\'' +
                ", cnc_port=" + cnc_port +
                '}';
    }
}
