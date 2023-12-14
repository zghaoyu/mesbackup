package com.cncmes.dto;

import java.util.Date;
import java.util.List;

/**
 * *Zhong
 * *
 */
public class OrderScheduler implements Comparable{
    private int id;
    private String moCode;
    private Date peRequestDate;
    private int qty;
    private double processtime;

    public OrderScheduler() {
    }

    public OrderScheduler(int id, String moCode, Date peRequestDate, Integer qty, double processtime) {
        this.id = id;
        this.moCode = moCode;
        this.peRequestDate = peRequestDate;
        this.qty = qty;
        this.processtime = processtime;
    }

    public OrderScheduler(int id, String moCode, Date peRequestDate, Integer qty) {
        this.id = id;
        this.moCode = moCode;
        this.peRequestDate = peRequestDate;
        this.qty = qty;
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

    @Override
    public String toString() {
        return "OrderScheduler{" +
                "id=" + id +
                ", moCode='" + moCode + '\'' +
                ", peRequestDate=" + peRequestDate +
                ", qty=" + qty +
                ", processtime=" + processtime +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        OrderScheduler orderScheduler = (OrderScheduler) o;
        if(this.peRequestDate == null && orderScheduler.peRequestDate == null) {
            if(this.processtime >= orderScheduler.processtime)
            {
                return 1;
            }else return -1;
        }else if (this.peRequestDate == null && orderScheduler.peRequestDate != null)
        {
            return 1;
        }else if(this.peRequestDate != null && orderScheduler.peRequestDate == null)
            return -1;
        //两个都有交货时间
        if(this.peRequestDate.after(orderScheduler.peRequestDate))
        {
            return 1;
        }else
        {
            if(this.peRequestDate.equals(orderScheduler.peRequestDate))
                return this.processtime>=orderScheduler.processtime?1:-1;
            else return -1;
        }
    }
}
