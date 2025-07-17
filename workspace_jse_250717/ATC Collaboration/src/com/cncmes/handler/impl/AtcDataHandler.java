package com.cncmes.handler.impl;

import com.cncmes.base.AtcErrorCode;

/**
 * *Zhong
 * *
 */
public class AtcDataHandler implements com.cncmes.handler.AtcDataHandler {
    @Override
    public String atcErrorResultHandler(byte result) {
        switch (result){
            case 1:
                return AtcErrorCode.ERROR_CODE_1.getName();
            case 2:
                return AtcErrorCode.ERROR_CODE_2.getName();
            case 3:
                return AtcErrorCode.ERROR_CODE_3.getName();
            case 4:
                return AtcErrorCode.ERROR_CODE_4.getName();
            case 5:
                return AtcErrorCode.ERROR_CODE_5.getName();
            case 6:
                return AtcErrorCode.ERROR_CODE_6.getName();
            case 7:
                return AtcErrorCode.ERROR_CODE_7.getName();
            case 8:
                return AtcErrorCode.ERROR_CODE_8.getName();
            case 9:
                return AtcErrorCode.ERROR_CODE_9.getName();
            case 10:
                return AtcErrorCode.ERROR_CODE_10.getName();
            case 11:
                return AtcErrorCode.ERROR_CODE_11.getName();
            case 12:
                return AtcErrorCode.ERROR_CODE_12.getName();
            case 13:
                return AtcErrorCode.ERROR_CODE_13.getName();
            case 14:
                return AtcErrorCode.ERROR_CODE_14.getName();
            case 15:
                return AtcErrorCode.ERROR_CODE_15.getName();
            case 16:
                return AtcErrorCode.ERROR_CODE_16.getName();
            case 17:
                return AtcErrorCode.ERROR_CODE_17.getName();
            case 18:
                return AtcErrorCode.ERROR_CODE_18.getName();
            case 19:
                return AtcErrorCode.ERROR_CODE_19.getName();
            case 20:
                return AtcErrorCode.ERROR_CODE_20.getName();
            case 21:
                return AtcErrorCode.ERROR_CODE_21.getName();
            case 22:
                return AtcErrorCode.ERROR_CODE_22.getName();
            default:
                return "Unknown Error";
        }
    }
}
