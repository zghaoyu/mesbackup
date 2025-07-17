package com.cncmes.dto;

import java.math.BigDecimal;

/**
 * *Zhong
 * *
 */
public class JOInfo {
    private int id;
    private String moCode;
    private String partNumber;
    private String dwg;
    private BigDecimal qty;
    private String opSeq;
    private String opCode;


    public JOInfo(int id, String moCode, String partNumber, String dwg, BigDecimal qty, String opSeq, String opCode) {
        this.id = id;
        this.moCode = moCode;
        this.partNumber = partNumber;
        this.dwg = dwg;
        this.qty = qty;
        this.opSeq = opSeq;
        this.opCode = opCode;
    }

    public JOInfo() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMoCode() {
        return moCode;
    }

    public void setMoCode(String moCode) {
        this.moCode = moCode;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getDwg() {
        return dwg;
    }

    public void setDwg(String dwg) {
        this.dwg = dwg;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public String getOpSeq() {
        return opSeq;
    }

    public void setOpSeq(String opSeq) {
        this.opSeq = opSeq;
    }

    public String getOpCode() {
        return opCode;
    }

    public void setOpCode(String opCode) {
        this.opCode = opCode;
    }

    @Override
    public String toString() {
        return "JOInfo{" +
                "id=" + id +
                ", moCode='" + moCode + '\'' +
                ", partNumber='" + partNumber + '\'' +
                ", dwg='" + dwg + '\'' +
                ", qty=" + qty +
                ", opSeq='" + opSeq + '\'' +
                ", opCode='" + opCode + '\'' +
                '}';
    }
}
