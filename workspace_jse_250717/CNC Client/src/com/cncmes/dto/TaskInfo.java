package com.cncmes.dto;

import javax.xml.crypto.Data;
import java.util.Date;


/**
 * *Zhong
 * *
 */
public class TaskInfo {
    int id;
    String task_id;
    String task_info;
    String cnc_ip;
    int have_done;
    double simulate_time;
    Date create_time = new Date();
    String realTableName = "task_info";
    public TaskInfo() {
    }

    public TaskInfo(String task_id, String task_info, String cnc_ip, int have_done, double simulate_time) {
        this.task_id = task_id;
        this.task_info = task_info;
        this.cnc_ip = cnc_ip;
        this.have_done = have_done;
        this.simulate_time = simulate_time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTask_id() {
        return task_id;
    }

    public void setTask_id(String task_id) {
        this.task_id = task_id;
    }

    public String getTask_info() {
        return task_info;
    }

    public void setTask_info(String task_info) {
        this.task_info = task_info;
    }

    public String getCnc_ip() {
        return cnc_ip;
    }

    public void setCnc_ip(String cnc_ip) {
        this.cnc_ip = cnc_ip;
    }

    public Integer getHave_done() {
        return have_done;
    }

    public void setHave_done(Integer have_done) {
        this.have_done = have_done;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_Time) {
        this.create_time = create_Time;
    }

    public String getRealTableName() {
        return realTableName;
    }

    public void setRealTableName(String realTableName) {
        this.realTableName = realTableName;
    }

    public Double getSimulate_time() {
        return simulate_time;
    }

    public void setSimulate_time(Double simulate_time) {
        this.simulate_time = simulate_time;
    }

    @Override
    public String toString() {
        return "TaskInfo{" +
                "id=" + id +
                ", task_id='" + task_id + '\'' +
                ", task_info='" + task_info + '\'' +
                ", cnc_ip='" + cnc_ip + '\'' +
                ", have_done=" + have_done +
                ", simulate_time=" + simulate_time +
                ", create_time='" + create_time + '\'' +
                ", realTableName='" + realTableName + '\'' +
                '}';
    }
}
