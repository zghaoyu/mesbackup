package com.cncmes.dto;

/**
 * *Zhong
 * *
 */
public class Result {
    private Boolean result;
    private String errorCasuse;

    public Result(Boolean result, String errorCasuse) {
        this.result = result;
        this.errorCasuse = errorCasuse;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public String getErrorCasuse() {
        return errorCasuse;
    }

    public void setErrorCasuse(String errorCasuse) {
        this.errorCasuse = errorCasuse;
    }
}
