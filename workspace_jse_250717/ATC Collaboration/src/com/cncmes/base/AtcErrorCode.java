package com.cncmes.base;

public enum AtcErrorCode {
    ERROR_CODE_1("Scram Status"),
    ERROR_CODE_2("Resetting 0 time out"),
    ERROR_CODE_3("Failed to reset the paused state"),
    ERROR_CODE_4("Failed to reset the automatic running status"),
    ERROR_CODE_5("Action 1 Condition Failure is not allowed"),
    ERROR_CODE_6("Action 2 Condition Failure is not allowed"),
    ERROR_CODE_7("Action 3 Condition Failure is not allowed"),
    ERROR_CODE_8("Action 4 Condition Failure is not allowed"),
    ERROR_CODE_9("The robotic arm has picked up tool"),
    ERROR_CODE_10("Load tool error,there is no tool on the tool changer"),
    ERROR_CODE_11("Load tool error,incorrect tool changer selection"),
    ERROR_CODE_12("Unload tool error,incorrect tool changer selection"),
    ERROR_CODE_13("Unload tool error,the tool changer already load tool"),
    ERROR_CODE_14("scram status"),
    ERROR_CODE_15("scram status"),
    ERROR_CODE_16("Action 1 time out"),
    ERROR_CODE_17("Action 2 time out"),
    ERROR_CODE_18("Action 3 time out"),
    ERROR_CODE_19("Action 4 time out"),
    ERROR_CODE_20("Action 5 time out"),
    ERROR_CODE_21("Action 1 error,invalid tool number"),
    ERROR_CODE_22("Action 4 error,invalid tool number")

    ;
    private String name;
    private AtcErrorCode(String name) {this.name = name;}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
