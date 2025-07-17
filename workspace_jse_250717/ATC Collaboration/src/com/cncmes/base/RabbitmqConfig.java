package com.cncmes.base;

public enum RabbitmqConfig {
    DIRECT_EXCHANGE_NAME("Direct_exchange"),
    QUEUE_CNC_TO_ATC("ATC_receive_"),
    QUEUE_ATC_TO_CNC("CNC_receive_"),
    RK_CNC_TO_ATC("Routing_Key_cnc_to_atc_"),
    RK_ATC_TO_CNC("Routing_Key_atc_to_cnc_");
    private String name;
    private RabbitmqConfig(String name) {this.name = name;}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
