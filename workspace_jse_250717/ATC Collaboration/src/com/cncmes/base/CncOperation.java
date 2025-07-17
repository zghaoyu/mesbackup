package com.cncmes.base;

public enum CncOperation {
    LOCK_TOOL("LockTool"),
    UNLOCK_TOOL("UnlockTool");
    private String name;

    CncOperation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
