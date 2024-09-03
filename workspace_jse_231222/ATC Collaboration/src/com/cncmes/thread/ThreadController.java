package com.cncmes.thread;

/**
 * *Zhong
 * *
 */
public class ThreadController {
    private static boolean stopAtcMonitorThread = false;
    private static boolean stopCncMonitorThread = false;
    public static String Run(){
        String readyChk = "OK";
        return readyChk;
    }
    public static void stopAtcMonitor(){
        stopAtcMonitorThread = true;
    }
    public static void stopCncMonitor(){
        stopCncMonitorThread = true;
    }

    public static boolean isStopAtcMonitorThread() {
        return stopAtcMonitorThread;
    }

    public static void setStopAtcMonitorThread(boolean stopAtcMonitorThread) {
        ThreadController.stopAtcMonitorThread = stopAtcMonitorThread;
    }

    public static boolean isStopCncMonitorThread() {
        return stopCncMonitorThread;
    }

    public static void setStopCncMonitorThread(boolean stopCncMonitorThread) {
        ThreadController.stopCncMonitorThread = stopCncMonitorThread;
    }

    public static void initStopFlag(){
        stopAtcMonitorThread = false;
    }
}
