package com.cncmes.drv;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;

import com.cncmes.base.*;
import com.cncmes.dao.impl.DAOImpl;
import com.cncmes.data.CncData;
import com.cncmes.data.CncWebAPI;
import com.cncmes.data.DevHelper;
import com.cncmes.data.SystemConfig;
import com.cncmes.dto.CNCStatusMonitor;
import com.cncmes.dto.MaterialTracing;
import com.cncmes.handler.CncDataHandler;
import com.cncmes.handler.impl.HartfordCncDataHandler;
import com.cncmes.utils.HttpRequestUtils;

import com.cncmes.utils.LogUtils;
import com.cncmes.utils.XmlUtils;

import net.sf.json.JSONObject;

/**
 * CNC Web Service API
 *
 * @author LI ZI LONG
 */
public class CncHARTFORD implements CNC {
    private static CncHARTFORD cncHARTFORD = new CncHARTFORD();
    private static LinkedHashMap<String, CncDataHandler> dataHandler = new LinkedHashMap<String, CncDataHandler>();
    private static CncData cncData = CncData.getInstance();
    private static CncWebAPI cncWebAPI = CncWebAPI.getInstance();

    private CncHARTFORD() {
    }

    static {
        try {
            XmlUtils.parseCncWebAPIXml();
        } catch (Exception e) {
            LogUtils.errorLog("CncHARTFORD fails to load web-api config:" + e.getMessage());
        }
    }

    public static CncHARTFORD getInstance() {
        return cncHARTFORD;
    }

    @Override
    public boolean openDoor(String ip) {
        if (sendCommand(ip, "openDoor", null, null)) {
            cncData.setData(ip, CncItems.OP_OPENDOOR, "1");
            return true;
        } else {
            cncData.setData(ip, CncItems.OP_OPENDOOR, "0");
            return false;
        }
    }

    @Override
    public boolean closeDoor(String ip) {
        if (sendCommand(ip, "closeDoor", null, null)) {
            cncData.setData(ip, CncItems.OP_CLOSEDOOR, "1");
            return true;
        } else {
            cncData.setData(ip, CncItems.OP_CLOSEDOOR, "0");
            return false;
        }
    }

    @Override
    public boolean clampFixture(String ip, int workZone) {
        LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
        paras.put("workZone", workZone + "");
        return sendCommand(ip, "clampFixture", paras, null);
    }

    @Override
    public boolean releaseFixture(String ip, int workZone) {
        LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
        paras.put("workZone", workZone + "");
        return sendCommand(ip, "releaseFixture", paras, null);
    }

    @Override
    public DeviceState getMachineState(String ip) {
        DeviceState state = DeviceState.SHUTDOWN;
        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
        if (sendCommand(ip, "getMachineState", null, rtnData)) {
            CncDataHandler rsltHandler = getDataHandler(ip);
            if (null != rsltHandler) {
                for (String key : rtnData.keySet()) {
                    state = rsltHandler.machineStateHandler(rtnData.get(key));
                    break;
                }
            } else {
                state = DeviceState.ALARMING;
            }
        }
        return state;
    }

    @Override
    public LinkedHashMap<String, String> getAlarmInfo(String ip) {
        LinkedHashMap<String, String> alarmInfo = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
        if (sendCommand(ip, "getAlarmInfo", null, rtnData)) {
            CncDataHandler rsltHandler = getDataHandler(ip);
            if (null != rsltHandler) {
                for (String key : rtnData.keySet()) {
                    alarmInfo = rsltHandler.alarmInfoHandler(rtnData.get(key));
                    break;
                }
            }
        }
        String alarmMsg = "";
        if (alarmInfo.size() > 0) {
            for (String key : alarmInfo.keySet()) {
                alarmMsg += "Alarm: " + key + "_" + alarmInfo.get(key);
                break;
            }
        }
        cncData.setData(ip, CncItems.ALARMMSG, alarmMsg);

        return alarmInfo;
    }

    @Override
    public LinkedHashMap<String, LinkedHashMap<String, String>> getToolLife(String ip) {
        LinkedHashMap<String, LinkedHashMap<String, String>> toolLife = new LinkedHashMap<String, LinkedHashMap<String, String>>();
        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
        if (sendCommand(ip, "getToolLife", null, rtnData)) {
            CncDataHandler rsltHandler = getDataHandler(ip);
            if (null != rsltHandler) {
                for (String key : rtnData.keySet()) {
                    toolLife = rsltHandler.machineToolLifeHandler(rtnData.get(key));
                    break;
                }
            }
        }
        return toolLife;
    }

    @Override
    public LinkedHashMap<String, String> getMachiningParas(String ip) {
        LinkedHashMap<String, String> machineParas = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
        if (sendCommand(ip, "getMachiningParas", null, rtnData)) {
            CncDataHandler rsltHandler = getDataHandler(ip);
            if (null != rsltHandler) {
                for (String key : rtnData.keySet()) {
                    machineParas = rsltHandler.machineParasHandler(rtnData.get(key));
                    break;
                }
            }
        }

        String paras = "";
        if (machineParas.size() > 0) {
            for (String para : machineParas.keySet()) {
                if ("".equals(paras)) {
                    paras = para + ": " + machineParas.get(para);
                } else {
                    paras += "\r\n" + para + ": " + machineParas.get(para);
                }
            }
        }
        cncData.setData(ip, CncItems.PARAS, paras);

        return machineParas;
    }

    @Override
    public LinkedHashMap<String, LinkedHashMap<String, String>> getMachiningCounter(String ip) {
        LinkedHashMap<String, LinkedHashMap<String, String>> counter = new LinkedHashMap<String, LinkedHashMap<String, String>>();
        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
        if (sendCommand(ip, "getMachiningCounter", null, rtnData)) {
            CncDataHandler rsltHandler = getDataHandler(ip);
            if (null != rsltHandler) {
                for (String key : rtnData.keySet()) {
                    counter = rsltHandler.machineCounterHandler(rtnData.get(key));
                    break;
                }
            }
        }
        return counter;
    }

    @Override
    public boolean startMachining(String ip, String programID) {
        LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
        if (null != programID && !"".equals(programID)) paras.put("program", programID);
        return sendCommand(ip, "startMachining", paras, null);
    }

    @Override
    public boolean startMachinePrepared(String ip) {
        // Automation CNC start register 900 set value "1"
        LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
        paras.put("name", "900");
        paras.put("value", "100000");
        paras.put("dec", "3");
        return sendCommand(ip, "setMacro", paras, null);
    }

    @Override
    public boolean pauseMachining(String ip) {
        return sendCommand(ip, "pauseMaching", null, null);
    }

    @Override
    public boolean resumeMachining(String ip) {
        return sendCommand(ip, "resumeMaching", null, null);
    }

    @Override
    public boolean uploadMainProgram(String ip, String programID, String ncProgramPath) {
        boolean noNeedToUpload = false;
        CncDataHandler preHandler = getDataHandler(ip);
        if (null != preHandler) noNeedToUpload = preHandler.machineUploadMainProgramHandler(ip, programID);

        if (noNeedToUpload) {
            if (mainProgramIsActivate(ip, programID)) return true;
        }

        LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
        if (null != programID && !"".equals(programID)) paras.put("name", programID);
        if (null != ncProgramPath && !"".equals(ncProgramPath)) paras.put("code", ncProgramPath);
        return sendCommand(ip, "uploadMainProgram", paras, null);
    }

    @Override
    public String downloadMainProgram(String ip, String programID) {
        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
        if (null != programID && !"".equals(programID)) paras.put("name", programID);
        String rtn = null;
        if (sendCommand(ip, "downloadMainProgram", paras, rtnData)) {
            CncDataHandler rsltHandler = getDataHandler(ip);
            if (null != rsltHandler) {
                for (String key : rtnData.keySet()) {
                    rtn = rsltHandler.machineNCProgramHandler(rtnData.get(key), programID + ".dl");
                    break;
                }
            }
        }

        return rtn;
    }

    @Override
    public boolean deleteMainProgram(String ip, String programID) {
        LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
        if (null != programID && !"".equals(programID)) paras.put("name", programID);
        return sendCommand(ip, "deleteMainProgram", paras, null);
    }

    @Override
    public boolean uploadSubProgram(String ip, String programID, String ncProgramPath) {
        LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
        if (null != programID && !"".equals(programID)) {
//			paras.put("path", programID);
            paras.put("path", "//DATA_SV/" + programID);//add "//DATA_SV/"+ by Hui Zhi Fang 2021.11.1
            paras.put("name", programID);
        }
        if (null != ncProgramPath && !"".equals(ncProgramPath)) {
            paras.put("body", ncProgramPath);
            paras.put("code", ncProgramPath);
        }

        CncDataHandler preHandler = getDataHandler(ip);
        if (null != preHandler) preHandler.machineUploadSubProgramHandler(ip, programID);
        return sendCommand(ip, "uploadSubProgram", paras, null);
    }

    @Override
    public String downloadSubProgram(String ip, String programID) {
        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
        if (null != programID && !"".equals(programID)) {
//			paras.put("path", programID);
            paras.put("path", "//DATA_SV/" + programID);//add "//DATA_SV/"+ by Hui Zhi Fang 2021.11.2
            paras.put("name", programID);
        }
        String rtn = null;
        if (sendCommand(ip, "downloadSubProgram", paras, rtnData)) {
            CncDataHandler rsltHandler = getDataHandler(ip);
            if (null != rsltHandler) {
                for (String key : rtnData.keySet()) {
                    rtn = rsltHandler.machineNCProgramHandler(rtnData.get(key), programID + ".dl");
                    break;
                }
            }
        }

        return rtn;
    }

    @Override
    public boolean deleteSubProgram(String ip, String programID) {
        LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
        if (null != programID && !"".equals(programID)) {
            paras.put("path", programID);
            paras.put("name", programID);
        }
        return sendCommand(ip, "deleteSubProgram", paras, null);
    }

    @Override
    public boolean mainProgramIsActivate(String ip, String programID) {
        String mainProgramName = getMainProgramName(ip, programID);
        return programID.equals(mainProgramName);
    }

    @Override
    public String getMainProgramName(String ip, String programID) {
        String programName = "";
        boolean mainProgramIsFixed = false;
        CncDataHandler preHandler = getDataHandler(ip);
        if (null != preHandler) mainProgramIsFixed = preHandler.machineUploadMainProgramHandler(ip, programID);

        if (mainProgramIsFixed) {
            LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
            if (sendCommand(ip, "getMainProgramName", null, rtnData)) {
                if (null != preHandler) {
                    for (String key : rtnData.keySet()) {
                        programName = preHandler.machineMainProgramNameHandler(rtnData.get(key));
                        break;
                    }
                }
            }
        } else {
            programName = programID;
        }

        return programName;
    }

    @Override
    public String getCurrSubProgramName(String ip, String programID) {
        String programName = "";
        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();

        if (sendCommand(ip, "getCurrSubProgramName", null, rtnData)) {
            CncDataHandler preHandler = getDataHandler(ip);
            if (null != preHandler) {
                for (String key : rtnData.keySet()) {
                    programName = preHandler.machineCurrSubProgramNameHandler(rtnData.get(key));
                    break;
                }
            }
        }

        return programName;
    }

    @Override
    public String generateMainProgramContent(String ip, String wpIDs, String subPrograms, String workzones) {
        String content = "";

        CncDataHandler preHandler = getDataHandler(ip);
        if (null != preHandler) {
            content = preHandler.machineGenerateMainProgramHandler(ip, wpIDs, subPrograms, workzones);
        }

        return content;
    }

    @Override
    public boolean setDataHandler(String cncModel, CncDataHandler handler) {
        if (null != handler) {
            dataHandler.put(cncModel, handler);
            return true;
        }
        return false;
    }

    @Override
    public CncDataHandler getDataHandler(String ip) {
        String cncModel = (String) CncData.getInstance().getData(ip).get(CncItems.MODEL);
        return dataHandler.get(cncModel);
    }

    @Override
    public boolean sendCommand(String ip, String operation, LinkedHashMap<String, String> inParas, LinkedHashMap<String, String> rtnData) {
        boolean success = false;
        LinkedHashMap<CncItems, Object> cncInfo = cncData.getData(ip);
        int port = (null != cncInfo) ? (int) cncInfo.get(CncItems.PORT) : 0;
        String model = (null != cncInfo) ? (String) cncInfo.get(CncItems.MODEL) : "";
        String opExecutive = cncWebAPI.getOperationExecutive(model, operation);
        long cmdStartT = 0, cmdStopT = 0;
        if (!"Myself".equals(opExecutive)) {
            CNC helper = (CNC) DevHelper.getInstance().getCtrlByModel(opExecutive);
            if (null != helper) {
                switch (operation) {
                    case "clampFixture":
                        return helper.clampFixture(ip, Integer.parseInt(inParas.get("workZone")));
                    case "closeDoor":
                        return helper.closeDoor(ip);
                    case "openDoor":
                        return helper.openDoor(ip);
                    case "releaseFixture":
                        return helper.releaseFixture(ip, Integer.parseInt(inParas.get("workZone")));
                }
                return helper.sendCommand(ip, operation, inParas, rtnData);
            } else {
                LogUtils.errorLog("CncHARTFORD-sendCommand get helper(" + opExecutive + ") failed");
            }
        }

        String initParas = "ip", initVals = ip, singleCmd = "";
        if (null != inParas && inParas.size() > 0) {
            for (String para : inParas.keySet()) {
                if ("port".equals(para)) {
                    port = Integer.valueOf(inParas.get(para));
                } else if ("model".equals(para)) {
                    model = inParas.get(para);
                } else if ("singleCmd".equals(para)) {
                    singleCmd = inParas.get(para);
                } else {
                    initParas += "," + para;
                    initVals += "," + inParas.get(para);
                }
            }
        }
        initParas += ",port";
        initVals += "," + port;

        String reInitJson = "";
        JSONObject jsonObj = new JSONObject();
        if (operation.endsWith("SubProgram")) {
            LinkedHashMap<CncWebAPIItems, String> commonCfg = cncWebAPI.getCommonCfg(model);
            if (null != commonCfg.get(CncWebAPIItems.FTPPORT)) {
                if (null != cncInfo) jsonObj.put("server", cncInfo.get(CncItems.FTPIP));
                jsonObj.put("port", commonCfg.get(CncWebAPIItems.FTPPORT));
                jsonObj.put("user", commonCfg.get(CncWebAPIItems.FTPUSER));
                jsonObj.put("password", commonCfg.get(CncWebAPIItems.FTPPWD));
            }
        }
        if (null != inParas && inParas.size() > 0) {
            for (String para : inParas.keySet()) {
                if (!"port".equals(para)) jsonObj.put(para, inParas.get(para));
            }
        }
        if (jsonObj.size() > 0) reInitJson = jsonObj.toString();

        String[] reInitParas = initParas.split(",");
        String[] reInitVals = initVals.split(",");
        String[] cmds = cncWebAPI.getAllCommands(model, operation);
        if (!"".equals(singleCmd)) cmds = new String[]{singleCmd};

        if (null != cncInfo) cncData.setData(ip, CncItems.COMMAND, operation);
        for (int i = 0; i < cmds.length; i++) {
            cmdStartT = System.currentTimeMillis();
            String url = cncWebAPI.getCmdUrl(model, cmds[i]);
            String inParasStr = cncWebAPI.getCmdInputParas(model, operation, cmds[i], reInitParas, reInitVals, true, reInitJson);
            LinkedHashMap<String, Object> outParas = cncWebAPI.getCmdOutputParas(model, operation, cmds[i]);

            String sensorCheck = cncWebAPI.getCmdParaVal(model, operation, cmds[i], CncWebAPIItems.SENSORCHECK);

            if (!"".equals(url) && null != outParas) {

                String rtnMsg = "OK";
                int checkSensorCount = 1;
                if (sensorCheck.length() > 0) checkSensorCount = 30;
                int try_err = 5;
                // 2023/6/20 add by Paris for check CNC sensor value
                for (; checkSensorCount > 0; checkSensorCount--) {
                    JSONObject resp = JSONObject.fromObject(HttpRequestUtils.httpPost(url, inParasStr, "utf-8", outParas));
                    rtnMsg = "OK";
                    for (String para : outParas.keySet()) {
                        if (outParas.get(para).getClass().equals(Integer.class)) {
                            if (resp.getInt(para) != (int) outParas.get(para)) {
                                rtnMsg = "ERR";
                                success = false;
                                break;
                            }
                        }
                    }
                    if ("OK".equals(rtnMsg)) success = true;

                    for (String para : outParas.keySet()) {
                        if (outParas.get(para).getClass().equals(Integer.class)) {
                            rtnMsg += "," + para + "=" + resp.getInt(para);
                        } else {
                            rtnMsg += "," + para + "=" + resp.getString(para);
                            if (null != rtnData) rtnData.put(para, resp.getString(para));
                        }
                    }

                    if (sensorCheck.length() > 0) {

                        String returnValue = rtnData.get("data");
                        int returnIndex = returnValue.indexOf("value") + 8;

                        String checkValue = returnValue.substring(returnIndex, returnValue.length() - 2).trim();

//						System.out.println(checkValue + " -> " + sensorCheck);
                        if ("257".equals(checkValue)) checkValue = "1";
                        if ("-1".equals(checkValue)) checkValue = "1";

                        if (checkValue.equals(sensorCheck)) {
                            success = true;
                            break;
                        } else {
                            try {
                                if (checkSensorCount == 1) {
                                    success = false;
                                    break;
                                }
                                Thread.sleep(500);
                            } catch (InterruptedException e) {

                                e.printStackTrace();
                            }

                        }
                    }
                    try {
                        Thread.sleep(100);
//						System.out.println(rtnMsg);
                        if (rtnMsg.contains("ERR")) {

                            if (--try_err > 0) {
                                checkSensorCount++;

                            }
                        }
                    } catch (InterruptedException e) {

                        e.printStackTrace();
                    }
                }
                cmdStopT = System.currentTimeMillis();
                LogUtils.commandLog(ip, (success ? "OK" : "NG") + LogUtils.separator + operation + LogUtils.separator + url + LogUtils.separator + inParasStr + LogUtils.separator + rtnMsg + LogUtils.separator + (cmdStopT - cmdStartT));
                if (!success) break;
            }
        }

        return success;
    }

    @Override
    public boolean hfOpenDoor(String ip) {    //cnc program control door
        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
        if (sendCommand(ip, "hfOpenDoor", null, rtnData)) {
            cncData.setData(ip, CncItems.OP_HF_OPENDOOR, "1");
            return true;
        } else {
            cncData.setData(ip, CncItems.OP_HF_OPENDOOR, "0");
            return false;
        }
    }

    @Override
    public boolean hfCloseDoor(String ip) {
        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
        if (sendCommand(ip, "hfCloseDoor", null, rtnData)) {
            cncData.setData(ip, CncItems.OP_HF_CLOSEDOOR, "1");
            return true;
        } else {
            cncData.setData(ip, CncItems.OP_HF_CLOSEDOOR, "0");
            return false;
        }
    }

    @Override
    public boolean hfClampFixture(String ip, int workZone) {
        LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();

        paras.put("address", "3412");
        paras.put("value", "0");

        if (sendCommand(ip, "hfClampFixture", paras, rtnData)) {
            paras.put("address", "3413");
            paras.put("value", "0");
            if (sendCommand(ip, "hfClampFixture", paras, rtnData)) {
                paras.put("address", "3414");
                paras.put("value", "0");
                if (sendCommand(ip, "hfClampFixture", paras, rtnData)) {
                    paras.put("address", "3415");
                    paras.put("value", "0");
                    if (sendCommand(ip, "hfClampFixture", paras, rtnData)) {
                        cncData.setData(ip, CncItems.OP_HF_CLAMPFIXTURE, "1");
                        return true;
                    }
                }
            }
        }
        ;
        cncData.setData(ip, CncItems.OP_HF_CLAMPFIXTURE, "0");
        return false;

    }

    @Override
    public boolean flushProgram(String ip, Boolean needToFlush) {
        if (needToFlush) {
            LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
            LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
            paras.put("name", "900");
            paras.put("value", String.valueOf(101 * 1000));
            paras.put("dec", "3");
            if (sendCommand(ip, "setMacro", paras, rtnData)) {
                if (startMachining(ip, "")) ;
                else return false;
                if (startMachining(ip, "")) ;
                else return false;
            } else return false;
        }
        return true;
    }

    @Override
    public double getMacro(String cncIp, double address) {
        double macro = -1;
        LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
        paras.put("name", String.valueOf(address));
        if (sendCommand(cncIp, "getMacro", paras, rtnData)) {
            HartfordCncDataHandler rsltHandler = (HartfordCncDataHandler) getDataHandler(cncIp);
            if (null != rsltHandler) {
                for (String key : rtnData.keySet()) {
                    macro = rsltHandler.machineGetMacroHandler(rtnData.get(key));
//                    System.out.println("getMacro : " + address + " ---------> " + macro);
                    break;
                }
            }
        }
        return macro;
    }

    @Override
    public boolean setReleaseMacro(String ip, String address, String value, String dec) {
        LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
        paras.put("address", address);
        paras.put("value", "1");
        if (sendCommand(ip, "hfReleaseFixture", paras, rtnData)) return true;
        return false;
    }


//    @Override
//    public boolean hfReleaseFixture(String ip, int workZone) {
//        LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
//        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
//        String address = "";
//        int angle = -1;
//
//        // Clamp all fixture if cannot clamp return false
//        if (!hfClampFixture(ip, 1)) return false;
//
//        paras.put("name", "900");
//        paras.put("value", String.valueOf(workZone * 1000));
//        paras.put("dec", "3");
//        if (sendCommand(ip, "setMacro", paras, rtnData)) {
//            startMachining(ip, "");
//            if (startMachining(ip, "")) {
//
//            } else
//                return false;
//        } else
//            return false;
//        DeviceState deviceState = getMachineState(ip);
//        while (deviceState != DeviceState.ALARMING) {
//            try {
//                Thread.sleep(1000);
//                deviceState = getMachineState(ip);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        for (int try_count = 10; try_count > 0; try_count--) {
//
//            try {
//
//                if (checkPosition(ip, 5024, 0)) {
//                    address = "3412";
//                    break;
//                } else if (checkPosition(ip, 5024, 90)) {
//                    address = "3413";
//                    break;
//                } else if (checkPosition(ip, 5024, 180)) {
//                    address = "3414";
//                    break;
//                } else if (checkPosition(ip, 5024, 270)) {
//                    address = "3415";
//                    break;
//                }
//
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//        if ("".equals(address)) {
//            angle = -1;
//        }
//
//        if (!"".equals(address)) {
//
//
//            // release fixture on the top layer
//            paras.put("address", address);
//            paras.put("value", "1");
//            if (sendCommand(ip, "hfReleaseFixture", paras, rtnData)) {
//                cncData.setData(ip, CncItems.OP_HF_RELEASEFIXTURE, "1");
//                return true;
//            }
//        }
//        cncData.setData(ip, CncItems.OP_HF_RELEASEFIXTURE, "0");
//        return false;
//    }

    @Override     //送气关气控制权给回cnc，只负责发指令要放到哪个位置
    public boolean hfReleaseFixture(String ip, int workZone) {
        LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
        String address = "";


        // Clamp all fixture if cannot clamp return false
        if (!hfClampFixture(ip, 1)) return false;

        paras.put("name", "900");
        paras.put("value", String.valueOf(workZone * 1000));
        paras.put("dec", "3");
        if (sendCommand(ip, "setMacro", paras, rtnData)) {
//            startMachining(ip, "");
            if (startMachining(ip, "")) {

            } else
                return false;
        } else
            return false;
        DeviceState deviceState = getMachineState(ip);
        while (deviceState != DeviceState.ALARMING) {
            try {
                Thread.sleep(1000);
                deviceState = getMachineState(ip);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        cncData.setData(ip, CncItems.OP_HF_RELEASEFIXTURE, "0");
        return true;
    }

    @Override
    public boolean hfResetDoor(String ip) {
        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
        if (sendCommand(ip, "hfResetDoor", null, rtnData)) {
            cncData.setData(ip, CncItems.OP_HF_RESETDOOR, "1");
            return true;
        } else {
            cncData.setData(ip, CncItems.OP_HF_RESETDOOR, "0");
            return false;
        }
    }

    @Override
    public boolean hfHomeDoor(String ip) {
        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
        if (sendCommand(ip, "hfHomeDoor", null, rtnData)) {
            cncData.setData(ip, CncItems.OP_HF_HOMEDOOR, "1");
            return true;
        } else {
            cncData.setData(ip, CncItems.OP_HF_HOMEDOOR, "0");
            return false;
        }
    }

    @Override
    public boolean hfRotate(String ip, int angle) {

        LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
        boolean success = false;

        // check Axis angle is same.
        if (checkPosition(ip, 5024, angle)) {
            return true;
        }

        // Clamp all fixture if cannot clamp return false
        if (!hfClampFixture(ip, 1)) return false;

        // Set rotation value
        paras.put("name", "900");
        paras.put("value", String.valueOf(angle * 1000));
        paras.put("dec", "3");

        if (sendCommand(ip, "hfRotate", paras, rtnData)) {
            if (startMachining(ip, "")) {
                try {
                    Thread.sleep(10000);

                } catch (InterruptedException e) {

                    success = false;
                    e.printStackTrace();
                }
                success = checkPosition(ip, 5024, angle);
            }
        }
        return success;
    }

    @Override
    public boolean checkPosition(String ip, int address, double value) {
        LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
        boolean success = false;
        // read address
        paras.put("name", String.valueOf(address));

        if (sendCommand(ip, "getMacro", paras, rtnData)) {
            CncDataHandler rsltHandler = getDataHandler(ip);
            if (null != rsltHandler) {
                for (String key : rtnData.keySet()) {
                    success = rsltHandler.machineAxisPositionHandler(rtnData.get(key), address, value);
                    break;
                }
            }
        }
        return success;
    }


    @Override
    public boolean hfCheckPosition(String ip) {

        boolean success = false;
        // Pick and place position X:20 Y:250 Z:0
        if (checkPosition(ip, 5021, -655.772)) {
            if (checkPosition(ip, 5022, -6.365)) {
                if (checkPosition(ip, 5023, 0.0)) {
                    success = true;
                }
            }
        }
        return success;
    }

    @Override
    public boolean hfFiveAspectProcess(String cncIP) {        //mes control gas valve      now disable : gas valve control power return to CNC
        DAOImpl dao = new DAOImpl("com.cncmes.dto.CNCStatusMonitor", true);
        try {
            while (true) {
                List<Object> cncStatusMonitor = dao.getTheLatestCNCStatus(cncIP);     //get cnc status from database to reduce invoking CNC API as soon as possible.
                CNCStatusMonitor cncstatus = (CNCStatusMonitor) cncStatusMonitor.get(0);
                LogUtils.fiveAspectLog(cncIP, "**************************************begin");
                LogUtils.fiveAspectLog(cncIP, cncIP + " status : " + cncstatus.getCncStatus());
                if (cncStatusMonitor == null) {
                    LogUtils.fiveAspectLog(cncIP, "cnc query status fail");
                    return false;
                } else {
                    int openValve = 0;
                    if (cncstatus.getCncStatus().equals("PAUSE")) {          //IDLE状态是仅限在测试时使用
                        String address = "-1000";
                        Double macro = getMacro(cncIP, 850);
                        if (macro.equals(10.0)) {       //850 == 10   选择90degree程序完成
                            //all axis have finished rotate action
                            LogUtils.fiveAspectLog(cncIP, "rotate program successfully finish");
                            return true;
                        }
                        if (macro.equals(0.0)) {
                            for (int i = 0; i < 10; i++) {
//                                if (checkPosition(cncIP, 5024, 0)) {    //check angle will have error
                                address = "3412";
                                openValve = 1;
                                break;
//                                }
                            }
                            if (address.equals("-1000")) {
                                LogUtils.fiveAspectLog(cncIP, "at 3412 failed");
                                return false;
                            }
                        }
                        if (macro.equals(90.0)) {
                            for (int i = 0; i < 10; i++) {
//                                if (checkPosition(cncIP, 5024, 90)) {
                                address = "3413";
                                openValve = 1;
                                break;
//                                }
                            }
                            if (address.equals("-1000")) {
                                LogUtils.fiveAspectLog(cncIP, "at 3413 failed");
                                return false;
                            }
                        }
                        if (macro.equals(180.0)) {
                            for (int i = 0; i < 10; i++) {
//                                if (checkPosition(cncIP, 5024, 180)) {
                                address = "3414";
                                openValve = 1;
                                break;
//                                }
                            }
                            if (address.equals("-1000")) {
                                LogUtils.fiveAspectLog(cncIP, "at 3414 failed");
                                return false;
                            }
                        }
                        if (macro.equals(270.0)) {
                            for (int i = 0; i < 10; i++) {
//                                if (checkPosition(cncIP, 5024, 270)) {
                                address = "3415";
                                openValve = 1;
                                break;
//                                }
                            }
                            if (address.equals("-1000")) {
                                LogUtils.fiveAspectLog(cncIP, "at 3415 failed");
                                return false;
                            }
                        }
                        if (macro.equals(1.0))     //clamp fixture
                        {
                            if (!hfClampFixture(cncIP, 1)) {//								close gas valve action
                                LogUtils.fiveAspectLog(cncIP, "clamp fixture fail");
                                return false;
                            }
                            LogUtils.fiveAspectLog(cncIP, "clampFixture over");
                            if (!setMacro(cncIP, "850", "100000", "3"))             //启动之前设置 850=100 什么都不做相当于空位
                            {
                                LogUtils.fiveAspectLog(cncIP, "set 850 = 100 failed ");
                                return false;
                            }
                            LogUtils.fiveAspectLog(cncIP, "set 850 = 100");
                            startMachining(cncIP, "");
                            LogUtils.fiveAspectLog(cncIP, "rotate to next axis aspect");
                            openValve = 0;
                        }
                        if (openValve == 1)    //release fixture
                        {
                            if (setReleaseMacro(cncIP, address, "1000", "3")) {
                                LogUtils.fiveAspectLog(cncIP, "releaseFixture over");
                                if (!setMacro(cncIP, "850", "100000", "3"))        //启动之前设置 850=100 什么都不做相当于空位
                                {
                                    LogUtils.fiveAspectLog(cncIP, "set 850 = 100 failed ");
                                    return false;
                                }
                                LogUtils.fiveAspectLog(cncIP, "set 850 = 100");
                                startMachining(cncIP, "");
                                startMachining(cncIP, "");
                                LogUtils.fiveAspectLog(cncIP, cncIP + " : begin to rotate fixture,release variable is " + address);
                            } else {
                                LogUtils.fiveAspectLog(cncIP, "release fixture failed at " + address);
                                return false;
                            }
                        }
                    } else {
                        Thread.sleep(3000);
                    }
                }
                LogUtils.fiveAspectLog(cncIP, "**************************************end");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean setMacro(String ip, String name, String value, String dec) {
        LinkedHashMap<String, String> paras = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
        paras.put("name", name);
        paras.put("value", value);
        paras.put("dec", dec);
        if (sendCommand(ip, "setMacro", paras, rtnData)) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean getAndUploadProgramByBarcode(String ip,String barcode,String programName) {     //parameter is fixture barcode
        SystemConfig sysCfg = SystemConfig.getInstance();
        LinkedHashMap<String, Object> config = sysCfg.getCommonCfg();
        DAOImpl daoImp = new DAOImpl("com.cncmes.dto.MaterialTracing",true);
        try {
            List<Object> objects = daoImp.tracingProcessedCardByFixtureBarcode(barcode);
            MaterialTracing materialTrace = (MaterialTracing) objects.get(0);
            if(uploadSubProgram(ip,programName,(String) config.get("LocalFolderURL")+materialTrace.getProcessedCardNo()+".nc"))
                return true;
            else
                return false;
        } catch (Exception throwables) {
            throwables.printStackTrace();
            return false;
        }
    }


    @Override
    public Boolean checkSensor(String cncip, String sensorAddress, String value)
    {
        Boolean result = false;
        String inParasStr = "{\"brand\":\"hartford\",\"model\":\"MVP-8\",\"ip\":\""+cncip+"\",\"port\":8193,\"data\":\"{\\\"address\\\":\\\""+sensorAddress+"\\\"}\"}";
        LinkedHashMap<String, Object> outParas = new LinkedHashMap<>();
        outParas.put("code",0);
        outParas.put("data","?");
        JSONObject resp = JSONObject.fromObject(HttpRequestUtils.httpPost("http://10.10.206.5:6300/api/v3/hartford/io_get", inParasStr, "utf-8", outParas));
        String rtnMsg = "OK";
        for (String para : outParas.keySet()) {
            if (outParas.get(para).getClass().equals(Integer.class)) {
                if (resp.getInt(para) != (int) outParas.get(para)) {
                    rtnMsg = "ERR";
                    result = false;
                    break;
                }
            }
        }
        if ("OK".equals(rtnMsg)) {
            String data = resp.getString("data");
            JSONObject sensorValue = JSONObject.fromObject(data);
            if(sensorValue.getString("value").equals(value))
            {
                result = true;
            }
        }
        return result;
    }

}
