package com.cncmes.base;

public enum AtcResult {
    ATC_MOVE_SUCCESS("ATC move successfully"),
    ATC_CONNECT_ERROR("ATC connect timeout"),
    ATC_TAKE_TOOL_ERROR("The tool gap or a error occurs while ATC taking tool"),
    ATC_LOAD_UNLOAD_TOOL_ERROR("The error occurs while ATC loading/unloading tool");
    private String resultDesc;
    private AtcResult(String resultDesc){
        this.resultDesc = resultDesc;
    }

    public String getResultDesc() {
        return resultDesc;
    }

    public void setResultDesc(String resultDesc) {
        this.resultDesc = resultDesc;
    }
}
