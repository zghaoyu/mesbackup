package com.cncmes.dto;

/**
 * *Zhong
 * *
 */
public class CNCStatusMonitor {
    private int id;
    private String cncIP;
    private String cncStatus;

    public CNCStatusMonitor(int id, String cncIP, String cncStatus) {
        this.id = id;
        this.cncIP = cncIP;
        this.cncStatus = cncStatus;
    }

    public CNCStatusMonitor() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCncIP() {
        return cncIP;
    }

    public void setCncIP(String cncIP) {
        this.cncIP = cncIP;
    }

    public String getCncStatus() {
        return cncStatus;
    }

    public void setCncStatus(String cncStatus) {
        this.cncStatus = cncStatus;
    }

    @Override
    public String toString() {
        return "CNCStatusMonitor{" +
                "id=" + id +
                ", cncIP='" + cncIP + '\'' +
                ", cncStatus='" + cncStatus + '\'' +
                '}';
    }
}
