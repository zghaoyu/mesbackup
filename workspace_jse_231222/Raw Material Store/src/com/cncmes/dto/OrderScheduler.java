package com.cncmes.dto;

import java.util.Date;

/**
 * *Zhong
 * *
 */
public class OrderScheduler implements Comparable {
    private int id;
    private String moCode;
    private Date peRequestDate;
    private int qty;
    private double processtime;
    private int have_finish;
    private int each_material_productnum = 0; //this Colum don't save in table order_info.it can be obtain in table cnc_material
    private String realTableName = "order_info";

    public OrderScheduler() {
    }

    public OrderScheduler(int id, String moCode, Date peRequestDate, int qty, double processtime, String realTableName) {
        this.id = id;
        this.moCode = moCode;
        this.peRequestDate = peRequestDate;
        this.qty = qty;
        this.processtime = processtime;
        this.realTableName = realTableName;
    }

    public OrderScheduler(int id, String moCode, Date peRequestDate, Integer qty, double processtime) {
        this.id = id;
        this.moCode = moCode;
        this.peRequestDate = peRequestDate;
        this.qty = qty;
        this.processtime = processtime;
    }

    public OrderScheduler(int id, String moCode, Date peRequestDate, int qty, double processtime, int have_finish, int each_material_productnum) {
        this.id = id;
        this.moCode = moCode;
        this.peRequestDate = peRequestDate;
        this.qty = qty;
        this.processtime = processtime;
        this.have_finish = have_finish;
        this.each_material_productnum = each_material_productnum;
    }

    public String getMoCode() {
        return moCode;
    }

    public void setMoCode(String moCode) {
        this.moCode = moCode;
    }

    public Date getPeRequestDate() {
        return peRequestDate;
    }

    public void setPeRequestDate(Date peRequestDate) {
        this.peRequestDate = peRequestDate;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public double getProcesstime() {
        return processtime;
    }

    public void setProcesstime(double processtime) {
        this.processtime = processtime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getRealTableName() {
        return realTableName;
    }

    public void setRealTableName(String realTableName) {
        this.realTableName = realTableName;
    }

    public int getHave_finish() {
        return have_finish;
    }

    public void setHave_finish(int have_finish) {
        this.have_finish = have_finish;
    }

    public int getEach_material_productnum() {
        return each_material_productnum;
    }

    public void setEach_material_productnum(int each_material_productnum) {
        this.each_material_productnum = each_material_productnum;
    }

    @Override
    public String toString() {
        return "OrderScheduler{" +
                "id=" + id +
                ", moCode='" + moCode + '\'' +
                ", peRequestDate=" + peRequestDate +
                ", qty=" + qty +
                ", processtime=" + processtime +
                ", have_finish=" + have_finish +
                ", each_material_productnum=" + each_material_productnum +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        //require time is first priority .processtime is second
        OrderScheduler orderScheduler = (OrderScheduler) o;
        if (this.peRequestDate == null && orderScheduler.peRequestDate == null) {
            if (this.processtime >= orderScheduler.processtime) {
                return 1;
            } else return -1;
        } else if (this.peRequestDate == null && orderScheduler.peRequestDate != null) {
            return 1;
        } else if (this.peRequestDate != null && orderScheduler.peRequestDate == null)
            return -1;
        else {//两个都有交货时间
            if (this.peRequestDate.before(orderScheduler.peRequestDate)) {
                return 1;
            } else {
                if (this.peRequestDate.equals(orderScheduler.peRequestDate))
                    return this.processtime <= orderScheduler.processtime ? 1 : -1;
                else return -1;
            }
        }

    }
}
