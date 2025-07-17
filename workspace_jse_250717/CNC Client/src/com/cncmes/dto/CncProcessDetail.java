package com.cncmes.dto;

/**
 * *Zhong
 * *For cnc monitor web
 */
public class CncProcessDetail {
    int id;
    String brand;
    String name;
    String ip;
    String process_detail;
    double simulation_time;
    int have_finish;
    String realTableName = "cnc_process_detail";

    public CncProcessDetail(int id, String brand, String name, String ip, String process_detail, double simulation_time, int have_finish, String realTableName) {
        this.id = id;
        this.brand = brand;
        this.name = name;
        this.ip = ip;
        this.process_detail = process_detail;
        this.simulation_time = simulation_time;
        this.have_finish = have_finish;
        this.realTableName = realTableName;
    }

    public CncProcessDetail(String brand, String name, String ip, String process_detail, double simulation_time, int have_finish) {
        this.brand = brand;
        this.name = name;
        this.ip = ip;
        this.process_detail = process_detail;
        this.simulation_time = simulation_time;
        this.have_finish = have_finish;
    }

    public CncProcessDetail() {
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
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

    public String getProcessDetail() {
        return process_detail;
    }

    public void setProcessDetail(String processDetail) {
        this.process_detail = processDetail;
    }

    public int getHave_finish() {
        return have_finish;
    }

    public void setHave_finish(int have_finish) {
        this.have_finish = have_finish;
    }

    public String getProcess_detail() {
        return process_detail;
    }

    public void setProcess_detail(String process_detail) {
        this.process_detail = process_detail;
    }

    public double getSimulation_time() {
        return simulation_time;
    }

    public void setSimulation_time(double simulation_time) {
        this.simulation_time = simulation_time;
    }

    public String getRealTableName() {
        return realTableName;
    }

    public void setRealTableName(String realTableName) {
        this.realTableName = realTableName;
    }

    @Override
    public String toString() {
        return "CncProcessDetail{" +
                "id=" + id +
                ", brand='" + brand + '\'' +
                ", name='" + name + '\'' +
                ", ip='" + ip + '\'' +
                ", process_detail='" + process_detail + '\'' +
                ", simulation_time=" + simulation_time +
                ", have_finish=" + have_finish +
                ", realTableName='" + realTableName + '\'' +
                '}';
    }
}
