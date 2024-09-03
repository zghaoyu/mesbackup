package com.cncmes.dto;

/**
 * *Zhong
 * *
 */
public class MaterialTracing {
    int id;
    String fixtureNo;
    String materialNo;
    String processedCardNo;
    String dwgNo;

    public MaterialTracing() {
    }

    public MaterialTracing(int id, String fixtureNo, String materialId, String processedCardNo, String dwgNo) {
        this.id = id;
        this.fixtureNo = fixtureNo;
        this.materialNo = materialId;
        this.processedCardNo = processedCardNo;
        this.dwgNo = dwgNo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFixtureNo() {
        return fixtureNo;
    }

    public void setFixtureNo(String fixtureNo) {
        this.fixtureNo = fixtureNo;
    }

    public String getMaterialNo() {
        return materialNo;
    }

    public void setMaterialNo(String materialNo) {
        this.materialNo = materialNo;
    }

    public String getProcessedCardNo() {
        return processedCardNo;
    }

    public void setProcessedCardNo(String processedCardNo) {
        this.processedCardNo = processedCardNo;
    }

    public String getDwgNo() {
        return dwgNo;
    }

    public void setDwgNo(String dwgNo) {
        this.dwgNo = dwgNo;
    }

    @Override
    public String toString() {
        return "MaterialTracing{" +
                "id=" + id +
                ", fixtureNo='" + fixtureNo + '\'' +
                ", materialNo='" + materialNo + '\'' +
                ", processedCardNo='" + processedCardNo + '\'' +
                ", dwgNo='" + dwgNo + '\'' +
                '}';
    }
}
