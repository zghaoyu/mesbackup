package com.cncmes.base;

public enum RabbitmqConfig {
    DIRECT_EXCHANGE_NAME("direct_exchange"),
    QUEUE_CNC_TO_ATC("atc_action"),
    QUEUE_ATC_TO_CNC("cnc_action");
    private String name;
    private RabbitmqConfig(String name) {this.name = name;}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
