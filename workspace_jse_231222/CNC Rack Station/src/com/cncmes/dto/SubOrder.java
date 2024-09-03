package com.cncmes.dto;

/**
 * *Zhong
 * *
 * SubOrder n<-->1 Order
 * SubOrder 1<-->1 CNCLine
 */
public class SubOrder {
    private Integer id;
    private String lineName;
    private Integer qty;
    private String orderId;
    private String drawing;
    private Integer priority;
    private String realTableName = "suborder";

    public SubOrder(Integer id, String lineName, Integer qty, String orderId, String drawing, Integer priority) {
        this.id = id;
        this.lineName = lineName;
        this.qty = qty;
        this.orderId = orderId;
        this.drawing = drawing;
        this.priority = priority;
    }

    public SubOrder() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getDrawing() {
        return drawing;
    }

    public void setDrawing(String drawing) {
        this.drawing = drawing;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
