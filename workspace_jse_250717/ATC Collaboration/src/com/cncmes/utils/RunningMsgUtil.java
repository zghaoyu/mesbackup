package com.cncmes.utils;

import com.cncmes.gui.AtcCollaboration;

/**
 * *Zhong
 * *
 */
public class RunningMsgUtil {
    static public String runningMsgFormat(String msg)
    {
        String runningMsg = TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss") +"@" + System.currentTimeMillis() + "@" +msg+"\n";
        return runningMsg;
    }
    static public void appendMsgArea(String msg)
    {
        String runningMsg = TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss") +"@" + System.currentTimeMillis() + "@" +msg+"\n";
        AtcCollaboration.getMsgArea().append(runningMsg);
    }
}
