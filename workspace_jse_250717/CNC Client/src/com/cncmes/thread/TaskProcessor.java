package com.cncmes.thread;

import java.io.*;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import javax.swing.JProgressBar;

import com.alibaba.fastjson.JSON;
import com.cncmes.base.CNC;
import com.cncmes.base.CncItems;
import com.cncmes.base.CncWebAPIItems;
import com.cncmes.base.DeviceOP;
import com.cncmes.base.DeviceState;
import com.cncmes.base.DriverItems;
import com.cncmes.base.ErrorCode;
import com.cncmes.base.IRobot;
import com.cncmes.base.RobotItems;
import com.cncmes.base.SpecItems;
import com.cncmes.base.TaskItems;
import com.cncmes.base.WorkpieceItems;
import com.cncmes.ctrl.CncFactory;
import com.cncmes.ctrl.IRobotFactory;
import com.cncmes.ctrl.RackClient;
import com.cncmes.ctrl.SchedulerClient;
import com.cncmes.dao.DAO;
import com.cncmes.dao.impl.DAOImpl;
import com.cncmes.data.*;
//import com.cncmes.gui.MaterialInput;
import com.cncmes.dto.*;
import com.cncmes.gui.MyConfirmDialog;
import com.cncmes.utils.*;
//import com.cncmes.utils.DebugUtils;

import net.sf.json.JSONObject;
import org.dom4j.DocumentException;

/**
 * @author LI ZI LONG
 * Create Task Handling Threads
 */
public class TaskProcessor {
    private static String lineName;
    private static CncWebAPI cncWebAPI = CncWebAPI.getInstance();
    private final static int CONCURR_OP_TIMEOUT = 600; //second
    private final static String CNC_Status_Monitor = "com.cncmes.dto.CNCStatusMonitor";
    public static List<OrderScheduler> OrderSchedulers = new ArrayList<OrderScheduler>();


    private static TaskProcessor taskProcesser = new TaskProcessor();

    private TaskProcessor() {
    }

    public static TaskProcessor getInstance(String ln) {
        lineName = ln;
        return taskProcesser;
    }

    /**
     * start the Task Processor thread
     */
    public void run() {
        ThreadUtils.Run(new TaskThread());
        RunningMsg.set("Task Processor Started");
    }

    //function for MES cycle working
    private Boolean workpiecesProcessingCircle(String[] workpiecesID) {
        String msg = "";
        for (String s : workpiecesID) {
            if (msg.equals("")) {
                msg = s;
            } else {
                msg += ";" + s;
            }
        }
        Boolean success = false;
        try {
            Socket socket = new Socket("localhost", 8765);
            // 要发送给CNC Scheduler服务器的信息
            OutputStream os = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(os);
            pw.write(msg);
            pw.flush();
            socket.shutdownOutput();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    private Boolean TimeToScheduleNextTask(){
        Boolean success = false;
        try {
            Socket socket = new Socket("localhost", 8765);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return success;
    }



    //if occur error send the message to Engineer by email and work wechat
    private void errorInform() {
        //email inform--------------------------------------------------------------------
        String recipients = "HY_Zhong@sto-tech.hk;parischeung@sto-tech.hk;parischeung@lfm-agile.com.hk;parischeung@gmail.com;jianheng_xiao@sto-tech.hk";
        ErrorEMailUtil eMailUtil = new ErrorEMailUtil("mail1.sto-tech.hk", "465", "Eddie", "HY_Zhong@sto-tech.hk", "W000650STO", "HY_Zhong@sto-tech.hk", recipients);
        String content = "<p>Hello.</p>" +
                "<p style=\"text-indent: 3em;\">I so sorry to inform you of MES run fail.Please check the attachments for details</p>" +
                "<p>Best Regards.</p>";
        try {
            eMailUtil.sendEmail("MES Alarming Info", content);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        //work wechat inform---------------------------------------------------------------
        WorkWechatUtils.sendAlarmNotification();
        //---------------------------------------------------------------------------------
    }

    /**
     * Release workpieces for the scheduler to reschedule
     *
     * @param ids Set STANDBY state for relative workpieces stored in memory of both CNC Client and Scheduler
     */
    private void resetMaterialState(String[] ids) {
        if (null != ids && ids.length > 0) {
            WorkpieceData wpData = WorkpieceData.getInstance();
            for (int i = 0; i < ids.length; i++) {
                wpData.setWorkpieceState(ids[i], DeviceState.STANDBY, i == (ids.length - 1) ? false : true, false, false, true, false);
            }
        }
    }

    /**
     * Release not loaded workpieces for scheduler to reschedule
     *
     * @param taskID     taskID from scheduler
     * @param logFile    log file full path
     * @param logContPre prefix of log content
     */
    private void checkMaterialOfRobot(String taskID, String logFile, String logContPre) {
        TaskData taskData = TaskData.getInstance();
        RobotData robotData = RobotData.getInstance();
        LinkedHashMap<TaskItems, Object> task = taskData.getTaskByID(taskID);
        if (null == task) return;

        String robotIP = (String) task.get(TaskItems.ROBOTIP);
        String wpIDs = (String) task.get(TaskItems.MATERIALIDS);

        String[] scheduledIDs = wpIDs.split(";");
        String[] inTrayMaterialIDs = null;
        LinkedHashMap<RobotItems, String> info = robotData.getMaterialInfo(robotIP);
        String inTrayIDs = info.get(RobotItems.WPIDS);
        if (!"".equals(inTrayIDs)) inTrayMaterialIDs = inTrayIDs.split(";");

        if (null == inTrayMaterialIDs) {
            resetMaterialState(scheduledIDs);
            LogUtils.operationLog(logFile, logContPre + "resetMaterialState(all:" + wpIDs + ")");
        } else {
            String notLoadedIDs = "";
            boolean bFound = false;
            for (String id : scheduledIDs) {
                bFound = false;
                for (String id2 : inTrayMaterialIDs) {
                    if (id.equals(id2)) {
                        bFound = true;
                        break;
                    }
                }
                if (!bFound) {
                    if ("".equals(notLoadedIDs)) {
                        notLoadedIDs = id;
                    } else {
                        notLoadedIDs += ";" + id;
                    }
                }
            }
            if (!"".equals(notLoadedIDs)) {
                resetMaterialState(notLoadedIDs.split(";"));
                LogUtils.operationLog(logFile, logContPre + "resetMaterialState(part:" + notLoadedIDs + ")");
            }
        }
    }

    public void needToFlushCnc(CNC cncCtrl, String cncIP) {
        cncCtrl.flushProgram(cncIP, true);
    }

    public void checkRobotBattery(IRobot robotCtrl, String robotIP, String logFile, String logContPre)     //if battery less than 20 percent go to charge and then if battery more than 80 percent continue task
    {
        if (Integer.parseInt(robotCtrl.getBattery(robotIP)) <= 30) {
            if (!robotCtrl.gotoCharge(robotIP, true))    //include goto charge station and enable charging action
            {
                LogUtils.operationLog(logFile, logContPre + "Robot gotoCharge failed");
                return;
            }
            LogUtils.operationLog(logFile, logContPre + "Robot gotoCharge start");
//            while (true) {                //if task queue have new task will stop charging
//                if (Integer.parseInt(robotCtrl.getBattery(robotIP)) >= 80) {
//                    break;
//                }
//            }
//            LogUtils.operationLog(logFile, logContPre + "Robot finish charge");
            robotCtrl.moveBackward(robotIP);
        }
    }

    private OrderScheduler getOrderInfoByMoCode(String order_no) {
        DAOImpl dao = new DAOImpl("com.cncmes.dto.OrderScheduler");
        try {
            List<Object> os = dao.findByCnd(new String[]{"moCode", "have_finish"}, new String[]{order_no, "0"});
            for (Object o : os) {

                return (OrderScheduler) o;
            }
        } catch (Exception throwables) {
            throwables.printStackTrace();
            return null;
        }
        return null;
    }

    private OrderScheduler getOrderInfoByBarcode(String barcode) {
        DAOImpl daoImp = new DAOImpl("com.cncmes.dto.MaterialTracing", true);
        try {
            List<Object> objects = daoImp.tracingProcessedCardByFixtureBarcode(barcode);
            MaterialTracing materialTrace = (MaterialTracing) objects.get(0);
            OrderScheduler orderScheduler = getOrderInfoByMoCode(materialTrace.getProcessedCardNo());
            List resultList = DBUtils.execute("select each_material_productnum from cnc_material where material_no = '" + materialTrace.getMaterialNo() + "'");
            if(resultList.size()>=0)
            {
                Map resultMap = (Map) resultList.get(0);
                orderScheduler.setEach_material_productnum((Integer) resultMap.get("each_material_productnum"));
            }else orderScheduler.setEach_material_productnum(1);

            return orderScheduler;
        } catch (Exception throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    /**
     * Pick materials from rack to Robot's Tray
     *
     * @param robotCtrl    remote controller of robot
     * @param robotIP      ip of robot
     * @param rackID       ID of material rack
     * @param rackSlots    target slots number of material rack
     * @param workpieceIDs expecting workpiece IDs from target slots
     * @param logFile      log file full path
     * @param logContPre   prefix of log content
     * @return true if all target workpieces is successfully picked from rack and put onto robot's tray
     */
    public boolean getMaterialFromRack(IRobot robotCtrl, String robotIP, String rackID, String[] rackSlots, String[] workpieceIDs, String logFile, String logContPre, String cncModel, CNC cncCtrl, String cncIP,Map infoMap,DeviceState cncState) {

        boolean bOK = true;
        RobotData robotData = RobotData.getInstance();
        WorkpieceData wpData = WorkpieceData.getInstance();
        String[] trayEmptySlots = robotData.getEmptySlots(robotIP);
        LinkedHashMap map = new LinkedHashMap();    // <barcode,upload program name>
        TaskData taskData = TaskData.getInstance();
        int index = -1;
        if (null == trayEmptySlots) {
            LogUtils.operationLog(logFile, logContPre + "Robot getMaterialFromRack failed cause of tray is full");
            bOK = false;
        } else {
            for (String rackSlotID : rackSlots) {
                index++;
                if (index < trayEmptySlots.length) {
                    ArrayList<Object> rtn = robotCtrl.pickAndPlace(robotIP, "input_rack_1_slot_" + rackSlotID, "robot_pallet_a_slot_" + trayEmptySlots[index]);
                    if ((Boolean) rtn.get(0)) {              // rtn[0] is robot action result ; rtn[1] is barcode result
                        if (!DebugUtils.isMassProduction()) // need to upload program for batch machining
                        {
                            { //upload program
                                String fileName = "O";
                                try {
                                    DecimalFormat decimalFormat = new DecimalFormat("00");
                                    Integer filepos = Integer.valueOf(XmlUtils.getCncConfig(cncIP, "workzone" + (index + 1)));  //name of file that upload to cnc
                                    String numFormat = decimalFormat.format(filepos);
                                    fileName += numFormat + "01";    //OXX01
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (DocumentException e) {
                                    e.printStackTrace();
                                }
                                final String uploadName = fileName;
                                String resultBarcode = (String) rtn.get(1);
                                if(resultBarcode.equals("-1") || resultBarcode == null || resultBarcode.equals("None"))
                                {
//                                    if(!DebugUtils.isHMLV()) resultBarcode = "F649A";
                                }

                                if(!DebugUtils.isHMLV()) resultBarcode = "F649A";   //if mode is MassProduction then disable barcode function.
                                final String barcode = resultBarcode;

                                if (!barcode.equals("None") && barcode.contains("A"))     //only record barcode which A side up
//                                if (!barcode.equals("None"))     //only record barcode which A side up
                                {
                                    DAOImpl daoImp = new DAOImpl("com.cncmes.dto.MaterialTracing", true);
                                    List<Object> objects = null;
                                    try {
                                        objects = daoImp.tracingProcessedCardByFixtureBarcode(barcode);
                                        if (objects != null && objects.size() > 0)               //fixture normal status:1.base on barcode trace processed card 2.base on barcode can get processed program
                                        {
                                            MaterialTracing materialTrace = (MaterialTracing) objects.get(0);
                                            if(materialTrace.getProcessedCardNo() != null && !materialTrace.getProcessedCardNo().equals(""))
                                            {
                                                DAOImpl processDetailDao = new DAOImpl("com.cncmes.dto.CncProcessDetail");
//------------------------------------------
//                                                CncData cncData = CncData.getInstance();
//                                                int capacity= (int) cncData.getData(cncIP).get(CncItems.WKZONEQTY);
                                                String cnc_pos = XmlUtils.getCncConfig(cncIP, "workzone" + (index + 1));
//                                                if(Integer.valueOf(rackSlotID) > capacity)
//                                                {
//                                                    cnc_pos = (XmlUtils.getCncConfig(cncIP, "workzone" +(Integer.valueOf(rackSlotID) % capacity)));
//                                                }else {
//                                                    cnc_pos = (XmlUtils.getCncConfig(cncIP, "workzone" + rackSlotID));
//                                                }
//------------------------------------------
                                                List<Object> dr = processDetailDao.getCNCProcessDetail(cncIP);
                                                if (dr != null) {
                                                    CncProcessDetail cncProcessDetail = (CncProcessDetail) dr.get(0);
                                                    String detail = cncProcessDetail.getProcessDetail();

                                                    double simTime = 0;
                                                    OrderScheduler orderScheduler = getOrderInfoByBarcode(barcode);
                                                    simTime = orderScheduler.getProcesstime();

                                                    double totalTime = (double) infoMap.get("simulate_time") + simTime;
                                                    infoMap.put("simulate_time",totalTime);
                                                    cncProcessDetail.setSimulation_time(totalTime);
                                                    detail += ";SLOT_" + cnc_pos + " --> " + materialTrace.getDwgNo();
                                                    cncProcessDetail.setProcessDetail(detail);
                                                    processDetailDao.monitorDBupdate(cncProcessDetail);
                                                } else {
                                                    String detail = "SLOT_" + cnc_pos + " --> " + materialTrace.getDwgNo();
                                                    //get CNC Info by  cncIP
                                                    DAOImpl cncInfoDao = new DAOImpl("com.cncmes.dto.CNCInfo", true);
                                                    List<Object> ci = cncInfoDao.getCNCInfoByIP(cncIP);
                                                    CNCInfo cncInfo = (CNCInfo) ci.get(0);
                                                    double simTime = 0;
                                                    OrderScheduler orderScheduler = getOrderInfoByBarcode(barcode);
                                                    simTime = orderScheduler.getProcesstime();
                                                    infoMap.put("simulate_time",simTime);
                                                    CncProcessDetail cncProcessDetail = new CncProcessDetail("Hartford MVP-8", cncInfo.getName(), cncIP, detail, simTime, 0);
                                                    processDetailDao.monitorDBadd(cncProcessDetail);
                                                }
                                            }
                                        }


                                    } catch (SQLException throwables) {
                                        System.out.println("Error in getMaterialFromRack. May be cause by barcode part");
                                        throwables.printStackTrace();
                                    } catch (DocumentException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
//                            record slot position and dwg to database cnc_mes      For cnc monitor system


//                            ------------------------------------------------


//                            if (barcode == null || barcode.equals("None") || barcode.equals("-1")) {    //scan barcode fail
//                                bOK = false;
//                                robotData.setData(robotIP, RobotItems.GRIPMATERIAL, workpieceIDs[index]);
//                                robotData.addMaterialInfo(robotIP, workpieceIDs[index], "0", DeviceState.STANDBY);
//                                LogUtils.operationLog(logFile, logContPre + "At RACK_"+rackSlotID+" Scan barcode failed ");
//                                break;
//                            }
                                if (barcode.contains("A")) {      //A side upward : upload processed program;Other side : Upload blank program
                                    OrderScheduler orderScheduler = getOrderInfoByBarcode(barcode);
                                    if (orderScheduler != null)
                                        OrderSchedulers.add(orderScheduler);           //record processed order info
                                    else System.out.println("record OrderSchedulers failed.Barcode No : " + barcode);
                                    if(cncState == DeviceState.STANDBY) {       //PREPAREFINISH will upload program when the cnc finish machining.
                                        Callable<Boolean> callableTask = () -> {
                                            RunningMsg.set("NC program " + uploadName + " is uploading");
                                            Boolean uploadSuccess = false;
                                            for (int i = 0; i < 10; i++) {                                                  //prevent from upload conflict
                                                if (cncCtrl.getAndUploadProgramByBarcode(cncIP, barcode, uploadName)) {     //programName should be named {#uploadName }
                                                    LogUtils.operationLog(logFile, logContPre + "Upload nc program " + uploadName + " successfully");
                                                    uploadSuccess = true;
                                                    break;
                                                }
                                            }
                                            if (!uploadSuccess) {
                                                CncData cncData = CncData.getInstance();
//                                        cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);          //20240418 disable if upload fail would not stop system
                                                RunningMsg.set("Upload " + uploadName + " nc program failed!");
                                                LogUtils.operationLog(logFile, logContPre + "Robot upload program failed.");
                                            }
                                            return uploadSuccess;
                                        };
                                        ExecutorService service = Executors.newCachedThreadPool();
                                        Future<Boolean> future = service.submit(callableTask);
                                        try {
                                            if (!future.get()) {
//                                    CNC have 14 blank program. If upload failed it will use blank program.
//                                    return false;
                                            }
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        } catch (ExecutionException e) {
                                            e.printStackTrace();
                                        } catch (NullPointerException e) {
                                            e.printStackTrace();
                                        }
                                    }else {

                                        map.put(barcode,uploadName);

                                    }
                                } else {
                                    LogUtils.operationLog(logFile, logContPre + "At RACK_" + rackSlotID + " Scan barcode failed or fixture orientation error ,scan content is"+barcode);
                                }
                            }
                        }
                        wpData.setData(workpieceIDs[index], WorkpieceItems.ID, workpieceIDs[index]);
                        DataUtils.updateWorkpieceData(workpieceIDs[index], lineName, rackID, rackSlotID);
                        robotData.setData(robotIP, RobotItems.GRIPMATERIAL, "");
                        robotData.updateSlot(robotIP, Integer.valueOf(trayEmptySlots[index]), workpieceIDs[index]);             //(RobotItems.SLOT?, workpieceID)
                        robotData.addMaterialInfo(robotIP, workpieceIDs[index], trayEmptySlots[index], DeviceState.STANDBY);
                        LogUtils.operationLog(logFile, logContPre + "Robot pickMaterialFromRack(" + workpieceIDs[index] + ",Rack_" + rackID + ":" + rackSlotID + "=>RobotSlot_" + trayEmptySlots[index] + ") done");
                    } else {
                        bOK = false;
                        robotData.setData(robotIP, RobotItems.GRIPMATERIAL, workpieceIDs[index]);
                        robotData.addMaterialInfo(robotIP, workpieceIDs[index], "0", DeviceState.STANDBY);
                        LogUtils.operationLog(logFile, logContPre + "Robot pickMaterialFromRack(" + workpieceIDs[index] + "," + rackID + "," + rackSlotID + ") failed");
                        break;
                    }
                }
            }
            infoMap.put("barcodeInfo",map);
        }
        return bOK;
    }

    /**
     * Put materials onto target rack from robot's tray
     *
     * @param robotCtrl  remote controller of the robot
     * @param robotIP    ip of the robot
     * @param rackID     ID of the material rack
     * @param rackSlots  Output rack slots
     * @param logFile    log file full path
     * @param logContPre prefix of the log content
     * @return true if all materials in Robot's tray have been successfully put onto the material rack
     */
    public boolean putMaterialOntoRack(IRobot robotCtrl, String robotIP, String rackID, String[] rackSlots, String logFile, String logContPre, String cncModel) {
        boolean bOK = true;
        RobotData robotData = RobotData.getInstance();
        WorkpieceData wpData = WorkpieceData.getInstance();
        RackClient rackClient = RackClient.getInstance();
        LinkedHashMap<RobotItems, String> info = robotData.getMaterialInfo(robotIP);
        String wpIDs = info.get(RobotItems.WPIDS);
        String traySlotIDs = info.get(RobotItems.WPSLOTIDS);
        String states = info.get(RobotItems.WPSTATES);
        String lineName = (String) robotData.getData(robotIP).get(RobotItems.LINENAME);
        if (null == rackSlots) rackSlots = rackClient.getRackEmptySlots(lineName, rackID);
        System.out.println("at putMaterialOntoRack - put to output rack : "+Arrays.deepToString(rackSlots));
        if ("".equals(wpIDs) || null == rackSlots) {

            if ("".equals(wpIDs))
                LogUtils.operationLog(logFile, logContPre + "Robot putMaterialOntoRack failed cause of tray is empty");
            if (null == rackSlots) {
                if (rackClient.rackServerIsReady(lineName, rackID)) {
                    LogUtils.operationLog(logFile, logContPre + "Robot putMaterialOntoRack failed ,cause of Rack " + rackID + " is full");
                } else {
                    LogUtils.operationLog(logFile, logContPre + "Robot putMaterialOntoRack failed cause of Rack Manager is not launched or can't be reached");
                }
            }
            bOK = false;
        } else {
            String[] workpieceIDs = wpIDs.split(";");
            String[] traySlotNo = traySlotIDs.split(";");
            String[] wpStates = states.split(";");
            int index = -1;
            for (String slotNo : traySlotNo) {
                index++;
                if (index < rackSlots.length) {
                    DeviceState wpState = wpData.workpieceIsDone(workpieceIDs[index]) ? DeviceState.FINISH : DeviceState.WORKING;
                    // use pickAndPlace instead of putMaterialOntoRack   2021.11.3  Hui Zhi
                    //if(!robotCtrl.putMaterialOntoRack(robotIP, rackID, rackSlots[index], slotNo, cncModel)){
                    ArrayList<Object> rtn = robotCtrl.pickAndPlace(robotIP, "robot_pallet_a_slot_" + slotNo, "output_rack_1_slot_" + rackSlots[index]);
                    if (!(Boolean) rtn.get(0)) {
//					if(!robotCtrl.pickAndPlace(robotIP, "robot_pallet_a_slot_" + slotNo, "input_rack_" + rackID + "_slot_" + rackSlots[index] )){

                        bOK = false;
                        robotData.setData(robotIP, RobotItems.GRIPMATERIAL, workpieceIDs[index]);
                        robotData.addMaterialInfo(robotIP, workpieceIDs[index], "0", DataUtils.getDevStateByString(wpStates[index]));
                        LogUtils.operationLog(logFile, logContPre + "Robot putMaterialOntoRack(" + workpieceIDs[index] + "," + rackID + "," + rackSlots[index] + ") failed");
                        break;
                    } else {
                        LogUtils.operationLog(logFile, logContPre + "Robot putMaterialOntoRack(" + workpieceIDs[index] + ",RobotSlot_" + slotNo + "=>Rack_" + rackID + ":" + rackSlots[index] + ") done");
                        robotData.deleteMaterialInfo(robotIP, workpieceIDs[index]);
                        robotData.setData(robotIP, RobotItems.GRIPMATERIAL, "");
                        robotData.updateSlot(robotIP, Integer.parseInt(slotNo), "");

                        //Update Rack Manager with workpiece FINISH|WORKING state
                        wpData.setWorkpieceState(workpieceIDs[index], wpState, false, true, false, true, false);
                        wpData.setRackID(workpieceIDs[index], rackID);
                        wpData.setSlotNo(workpieceIDs[index], rackSlots[index]);
                        rackClient.updateRackInfo(lineName, rackID, workpieceIDs[index], true, false, true);
                    }
                }
            }
        }
        return bOK;
    }

    /**
     * Unload materials from machine and put them onto Robot's tray
     *
     * @param robotCtrl    remote controller of robot
     * @param robotIP      ip of robot
     * @param cncIP        ip of machine
     * @param workZones    working zones of machine
     * @param workpieceIDs all materials in working zones of machine
     * @param logFile      log file full path
     * @param logContPre   prefix of log content
     * @return true if all materials are successfully unloaded from machine
     */
    //Change Robot to IRobot by Hui Zhi 2021.11.3                           rotate and load fixture at the same time
    public boolean unloadMaterialFromMachine(IRobot robotCtrl, String robotIP, CNC cncCtrl, String cncIP, String[] workZones, String[] workpieceIDs, String logFile, String logContPre) {
        System.out.println("at unloadMaterialFromMachine workZones: " + Arrays.toString(workZones));
        boolean bOK = true;
        RobotData robotData = RobotData.getInstance();
        WorkpieceData wpData = WorkpieceData.getInstance();
        CncData cncData = CncData.getInstance();
        String[] emptyTraySlots = robotData.getEmptySlots(robotIP);
        System.out.println("at unloadMaterialFromMachine   emptyTraySlots = "+emptyTraySlots);
        if (null == emptyTraySlots) {

            LogUtils.operationLog(logFile, logContPre + "Robot unloadMaterialFromMachine failed cause of tray is full");
            bOK = false;
        } else {
            int index = -1;
            int cnc_pos = -1;
            int num = 0;
            //String cncModel = cncData.getCncModel(cncIP);	//disable
            String cncPositon = cncData.getCNCPosition(cncIP);
            String cncZone = "";
            if ("cnc79marker".equals(cncPositon)) {
                cncZone = "cnc_79";
            } else {
                String[] tempSpace = cncPositon.split("_");
                cncZone = "cnc_"+tempSpace[tempSpace.length-1];      //use cnc's ID.it can get from the tagname
//                cncZone = "cnc_72";
            }
            int[] spcificZone = new int[workZones.length];   //特定工件加工需摆在cnc特定位置、对specificZone初始化
            //specific用于放fixture到robot的同时转到下一个位置
            for (int i = 0; i < spcificZone.length; i++) {
                spcificZone[i] = 1;
            }
            for (String workZone : workZones) {
                try {
                    spcificZone[num] = Integer.parseInt(XmlUtils.getCncConfig(cncIP, "workzone" + Integer.valueOf(num+1)));       //从XML中获取工件在CNC中加工的位置
                    num++;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            }
            for (String workZone : workZones) {
                index++;    //index begin at 0


                if (index < emptyTraySlots.length) {
                    DeviceState wpState = wpData.workpieceIsDone(workpieceIDs[index]) ? DeviceState.FINISH : DeviceState.WORKING;
                    // use pickAndPlace instead of pickMaterialFromMachine   2021.11.3  Hui Zhi
                    //if(robotCtrl.pickMaterialFromMachine(robotIP, cncIP, Integer.parseInt(workZone), emptyTraySlots[index], cncModel)){

                    /* Check Position and Rotate same angle
                     * and then release fixture
                     * */

                    if (index == 0) {   // adjust the first cnc position is prerequisite of pick from cnc
                        cnc_pos = spcificZone[0];
                        if (!cncCtrl.hfReleaseFixture(cncIP, spcificZone[0])) {
                            LogUtils.operationLog(logFile, logContPre + "Robot pickMaterialFromMachine(" + workpieceIDs[0] + ",Zone_" + spcificZone[0] + "=>RobotSlot_" + emptyTraySlots[0] + ") CNC Rotate error");
                            bOK = false;
                            break;
                        }
                    }

                    if (!robotCtrl.pickFromName(robotIP, cncZone))  //pickfromcnc
                    {
                        bOK = false;
                        robotData.setData(robotIP, RobotItems.GRIPMATERIAL, workpieceIDs[index]);
                        robotData.addMaterialInfo(robotIP, workpieceIDs[index], "0", wpState);
                        LogUtils.operationLog(logFile, logContPre + "Robot pickMaterialFromMachine(" + workpieceIDs[index] + "," + spcificZone[index] + ") failed");
                        break;
                    }

                    cncCtrl.startMachining(cncIP, ""); // close gas valve  2024.4.17

                    if (index + 1 < spcificZone.length) {
                        cnc_pos = spcificZone[index + 1];
                    }

                    Thread threadReleaseFixture = new Thread(new Runnable() { // cnc rotate next fixture position thread
                        int cnc_pos, index;

                        public Runnable setParam(int param1, int param2) {
                            cnc_pos = param1;
                            index = param2;
                            return this;
                        }

                        @Override
                        public void run() {
                            //1 position have rotated

                            if (!cncCtrl.hfReleaseFixture(cncIP, cnc_pos)) {
                                LogUtils.operationLog(logFile, logContPre + "Robot pickMaterialFromMachine(" + workpieceIDs[index] + ",Zone_" + cnc_pos + "=>RobotSlot_" + emptyTraySlots[index] + ") CNC Rotate error");
                            }
                        }
                    }.setParam(cnc_pos, index));
                    threadReleaseFixture.start();

                    if (robotCtrl.placeToName(robotIP, "robot_pallet_a_slot_" + emptyTraySlots[index])) //place to robot
                    {
                        LogUtils.operationLog(logFile, logContPre + "Robot pickMaterialFromMachine(" + workpieceIDs[index] + ",Zone_" + spcificZone[index] + "=>RobotSlot_" + emptyTraySlots[index] + ") done");
                        robotData.setData(robotIP, RobotItems.GRIPMATERIAL, "");
                        robotData.updateSlot(robotIP, Integer.parseInt(emptyTraySlots[index]), workpieceIDs[index]);
                        robotData.addMaterialInfo(robotIP, workpieceIDs[index], emptyTraySlots[index], wpState);
                        cncData.setWorkpieceID(cncIP, spcificZone[index], "");
                        //---------------------------------------------------- update orderScheduler's info
                        if (OrderSchedulers.size() > 0) {
                            System.out.println("update OrderList : " + OrderSchedulers);
                            OrderScheduler orderScheduler = OrderSchedulers.get(0);
                            orderScheduler.setQty(orderScheduler.getQty() - orderScheduler.getEach_material_productnum());
                            if (orderScheduler.getQty() <= 0) {
                                orderScheduler.setHave_finish(1);
                            }
                            try {
                                DAOImpl dao = new DAOImpl("com.cncmes.dto.OrderScheduler");
                                dao.update(orderScheduler);     //update database
                                OrderSchedulers.remove(0);
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }

                        }

                        //----------------------------------------------------
                    } else {

                        bOK = false;
                        robotData.setData(robotIP, RobotItems.GRIPMATERIAL, workpieceIDs[index]);
                        robotData.addMaterialInfo(robotIP, workpieceIDs[index], "0", wpState);
                        LogUtils.operationLog(logFile, logContPre + "Robot pickMaterialFromMachine(" + workpieceIDs[index] + "," + spcificZone[index] + ") failed");
                        break;
                    }


//					if (!cncCtrl.hfReleaseFixture(cncIP, Integer.parseInt(workZone))){
//						LogUtils.operationLog(logFile, logContPre+"Robot pickMaterialFromMachine("+workpieceIDs[index]+",Zone_"+workZone+"=>RobotSlot_"+emptyTraySlots[index]+") CNC Rotate error");
//						bOK = false;
//						break;
//					}


//					if(robotCtrl.pickAndPlace(robotIP, cncZone, "robot_pallet_a_slot_" + emptyTraySlots[index])){
//						LogUtils.operationLog(logFile, logContPre+"Robot pickMaterialFromMachine("+workpieceIDs[index]+",Zone_"+workZone+"=>RobotSlot_"+emptyTraySlots[index]+") done");
//						robotData.setData(robotIP, RobotItems.GRIPMATERIAL, "");
//						robotData.updateSlot(robotIP, Integer.parseInt(emptyTraySlots[index]), workpieceIDs[index]);
//						robotData.addMaterialInfo(robotIP, workpieceIDs[index], emptyTraySlots[index], wpState);
//						cncData.setWorkpieceID(cncIP, Integer.parseInt(workZone), "");
//					}else{
//						bOK = false;
//						robotData.setData(robotIP, RobotItems.GRIPMATERIAL, workpieceIDs[index]);
//						robotData.addMaterialInfo(robotIP, workpieceIDs[index], "0", wpState);
//						LogUtils.operationLog(logFile, logContPre+"Robot pickMaterialFromMachine("+workpieceIDs[index]+","+workZone+") failed");
//						break;
//					}
                }
            }
            //----------------------------------------------------------------------- update cnc_process_detail for cnc monitor system
            if (!DebugUtils.isMassProduction()) // need to upload program for batch machining
            {
                DAOImpl processDetailDao = new DAOImpl("com.cncmes.dto.CncProcessDetail");
                try {
                    List<Object> dr = processDetailDao.getCNCProcessDetail(cncIP);
                    if (dr != null && dr.size() != 0) {
                        CncProcessDetail processDetail = (CncProcessDetail) dr.get(0);
                        processDetail.setHave_finish(1);
                        processDetailDao.monitorDBupdate(processDetail);
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return bOK;
    }
    /**
     * sequence : 1.unload  2.load
     * @param robotCtrl
     * @param robotIP
     * @param cncCtrl
     * @param cncIP
     * @param unLoadWorkZones    slot position of the workpiece in cnc            use in unload
     * @param unloadWorkpieceIDs  id of the workpiece in cnc                                  use in unload
     * @param logFile
     * @param logContentPre
     * @return
     */
    public boolean loadAndUnloadMaterial(IRobot robotCtrl, String robotIP, CNC cncCtrl, String cncIP,String[] unLoadWorkZones, String[] unloadWorkpieceIDs, String logFile, String logContentPre)
    {
        String taskInfoDto = "com.cncmes.dto.TaskInfo";
        boolean bOK = true;
        String record_workzone = "";
        String record_workpieceID = "";
        //------------------------------------------------use in unload
        RobotData robotData = RobotData.getInstance();
        CncData cncData = CncData.getInstance();
        WorkpieceData wpData = WorkpieceData.getInstance();
        String[] emptyTraySlots = robotData.getEmptySlots(robotIP);         //Robot's tray slot
        LinkedList<String> emptyTraySlotsList = Arrays.stream(emptyTraySlots).collect(Collectors.toCollection(LinkedList::new));//upload action finish add one;unload action finish remove one
        //------------------------------------------------
        //------------------------------------------------use in load
        String[] emptyWorkzones = cncData.getEmptyWorkzones(cncIP);
        LinkedList<String> emptyWorkzonesList = Arrays.stream(emptyWorkzones).collect(Collectors.toCollection(LinkedList::new)); //attention : emptyworkzones not actual empty slot
        LinkedHashMap<RobotItems, String> robotinfo = robotData.getMaterialInfo(robotIP);
        String wpIDsUpload = robotinfo.get(RobotItems.WPIDS);
        String wpSlots = robotinfo.get(RobotItems.WPSLOTIDS); //在机器人托盘上的物料所在的slot位置
        String states = robotinfo.get(RobotItems.WPSTATES);
        String cncModel = cncData.getCncModel(cncIP);
        if ("".equals(wpIDsUpload))
        {
            LogUtils.operationLog(logFile, logContentPre + "Robot loadMaterialOntoMachine failed cause of tray is empty");
            bOK = false;
            return bOK;
        }
        String[] workpieceIDsUpload = wpIDsUpload.split(";");
        String[] traySlotsNo = wpSlots.split(";");//在机器人托盘上的物料所在的slot位置
        String[] wpStates = states.split(";");
        //------------------------------------------------
        int wpUploadNum = workpieceIDsUpload.length;
        int wpUnloadNum = unloadWorkpieceIDs.length;

        ArrayList<String> uploadPosList = new ArrayList();  //workpieces upload position
        for(int i = 1;i<=traySlotsNo.length;i++)
        {
            try {
                uploadPosList.add(XmlUtils.getCncConfig(cncIP, "workzone" + i));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }
        LinkedList<String> unloadWorkZoneList = new LinkedList();      //record unload WorkZoneSequence:1.unload slot = upload slot 2.others
        LinkedList<String> unloadWorkpieceIDList = new LinkedList();

        LinkedList<String> uploadTraySlotsList = new LinkedList();      //record upload RobotTraySlotSequence:1.unload slot = upload slot 2.others
        LinkedList<String> uploadWorkpieceIDList = new LinkedList();
        LinkedList<String> uploadWorkZoneList = new LinkedList<>();     //upload CNC zones position sequence

        Map<String,String> uploadWorkZoneIndex = new LinkedHashMap<>();

        for(int i = 0;i<unLoadWorkZones.length;i++ )
        {
            if((uploadPosList.contains(unLoadWorkZones[i])))        //slot position which unload position = load position
            {
                unloadWorkZoneList.addFirst(unLoadWorkZones[i]);
                unloadWorkpieceIDList.addFirst(unloadWorkpieceIDs[i]);
                uploadTraySlotsList.addFirst(String.valueOf(uploadPosList.indexOf(unLoadWorkZones[i])+1));    //robot tray slot num, not index
                uploadWorkpieceIDList.addFirst(workpieceIDsUpload[uploadPosList.indexOf(unLoadWorkZones[i])]);
                uploadWorkZoneList.addFirst(unLoadWorkZones[i]);                        //record sequence of the upload to cnc's slot position
                uploadWorkZoneIndex.put(unLoadWorkZones[i],String.valueOf(uploadPosList.indexOf(unLoadWorkZones[i])));       //map = (uploadPosition,traySlotIndex)

            }
            else{
                unloadWorkZoneList.addLast(unLoadWorkZones[i]);          //slot position which unload position != load position
                unloadWorkpieceIDList.addLast(unloadWorkpieceIDs[i]);
//                uploadWorkZoneIndex.put(unLoadWorkZones[i],String.valueOf(i));   //map = (uploadPosition,workZoneIndex)
            }
        }
        System.out.println("at loadAndUnloadMaterial  unloadWorkZoneList : "+unloadWorkZoneList);
        for(int i=0;i<uploadPosList.size();i++)
        {
            if(!uploadTraySlotsList.contains(uploadPosList.get(i)))
            {
                uploadTraySlotsList.addLast(String.valueOf(i+1));
                uploadWorkpieceIDList.addLast(workpieceIDsUpload[i]);
                uploadWorkZoneList.addLast(uploadPosList.get(i));
            }
        }
        System.out.println("at loadAndUnloadMaterial  uploadTraySlotsList :"+uploadTraySlotsList );

        LinkedList<String> wpSlotList = new LinkedList<>();
        if (null == emptyTraySlotsList||emptyTraySlotsList.size() == 0) {
            LogUtils.operationLog(logFile, logContentPre + "Robot unloadMaterialFromMachine failed cause of tray is full");
            bOK = false;
        }else {

            int uploadIndex = 0;
            String cncZone;
            String cncPositon = cncData.getCNCPosition(cncIP);
            if ("cnc79marker".equals(cncPositon)) {
                cncZone = "cnc_79";
            } else {
                String[] tempSpace = cncPositon.split("_");
                cncZone = "cnc_"+tempSpace[tempSpace.length-1];      //use cnc's ID.it can get from the tagname
            }
            int maxLength = wpUnloadNum>wpUploadNum ? wpUnloadNum:wpUploadNum;
            for(int i=0;i<maxLength;i++){
                if(i<wpUnloadNum)
                {
                    DeviceState wpState = wpData.workpieceIsDone(unloadWorkpieceIDList.get(i)) ? DeviceState.FINISH : DeviceState.WORKING;
                    if (!cncCtrl.hfReleaseFixture(cncIP, Integer.parseInt(unloadWorkZoneList.get(i)))){
                        LogUtils.operationLog(logFile, logContentPre + "Robot pickMaterialFromMachine(" + unloadWorkpieceIDList.get(i) + ",Zone_" + unloadWorkZoneList.get(i) + "=>RobotSlot_" + emptyTraySlotsList.get(0) + ") CNC Rotate error");
                        return false;
                    }
                    ArrayList<Object> unloadrtn = robotCtrl.pickAndPlace(robotIP, cncZone, "robot_pallet_a_slot_"+emptyTraySlotsList.get(0));
                    if ((Boolean) unloadrtn.get(0)) {
                        LogUtils.operationLog(logFile, logContentPre + "Robot pickMaterialFromMachine(" + unloadWorkpieceIDList.get(i) + ",Zone_" + unloadWorkZoneList.get(i) + "=>RobotSlot_" + emptyTraySlotsList.get(0) + ") done");
                        robotData.setData(robotIP, RobotItems.GRIPMATERIAL, "");
                        robotData.updateSlot(robotIP, Integer.parseInt(emptyTraySlotsList.get(0)), unloadWorkpieceIDList.get(i));
                        robotData.addMaterialInfo(robotIP, unloadWorkpieceIDList.get(i), emptyTraySlotsList.get(0), wpState);
                        cncData.setWorkpieceID(cncIP, Integer.parseInt(unloadWorkZoneList.get(i)), "");
//                        unloadhaveDone.add(unLoadWorkZones[i]);
                        emptyTraySlotsList.remove(0);
                        if(!emptyWorkzonesList.contains(unloadWorkZoneList.get(i)))
                        {
                            emptyWorkzonesList.addFirst(unloadWorkZoneList.get(i));
                        }
                        cncCtrl.startMachining(cncIP, "");
                    }else {
                        robotData.setData(robotIP, RobotItems.GRIPMATERIAL, unloadWorkpieceIDList.get(i));
                        robotData.addMaterialInfo(robotIP, unloadWorkpieceIDList.get(i), "0", wpState);
                        LogUtils.operationLog(logFile, logContentPre + "Robot pickMaterialFromMachine(" + unloadWorkpieceIDList.get(i) + "," + unloadWorkZoneList.get(i) + ") failed");
                        return false;
                    }
                }
                if(i<wpUploadNum)
                {
                    //cnc rotate and pickAndPlace at the same time
                    Thread threadReleaseFixture = new Thread(new Runnable() {
                        int cnc_pos, index;

                        public Runnable setParam(int param1, int param2) {
                            cnc_pos = param1;
                            index = param2;
                            return this;
                        }

                        @Override
                        public void run() {
                            if (!cncCtrl.hfReleaseFixture(cncIP, cnc_pos)) {
                                LogUtils.operationLog(logFile, logContentPre + "Robot putMaterialOntoMachine(" + uploadWorkpieceIDList.get(index) + ",RobotSlot_" + uploadTraySlotsList.get(index) + "=>Zone_" + emptyWorkzonesList.get(0) + ") CNC Rotate error");
                            }
                        }
                    }.setParam(Integer.parseInt(uploadWorkZoneList.get(i)), i));
                    threadReleaseFixture.start();


//                    if (!cncCtrl.hfReleaseFixture(cncIP, Integer.parseInt(uploadWorkZoneList.get(i)))){
//                        LogUtils.operationLog(logFile, logContentPre + "Robot putMaterialOntoMachine(" + uploadWorkpieceIDList.get(i) + ",RobotSlot_" + uploadTraySlotsList.get(i) + "=>Zone_" + emptyWorkzonesList.get(0) + ") CNC Rotate error");
//                        return false;
//                    }
                    if(record_workzone.equals("") || record_workzone == null)
                    {
                        record_workzone = uploadWorkZoneList.get(i);
                    }else {
                        record_workzone = record_workzone + ";" + uploadWorkZoneList.get(i);
                    }
                    if(record_workpieceID.equals(""))
                    {
                        record_workpieceID = uploadWorkpieceIDList.get(i);
                    }else record_workpieceID = record_workpieceID + ";" +uploadWorkpieceIDList.get(i);

                    ArrayList<Object> uploadrtn = robotCtrl.pickAndPlace(robotIP, "robot_pallet_a_slot_"+uploadTraySlotsList.get(i), cncZone);
                    if((Boolean)uploadrtn.get(0)){
                        LogUtils.operationLog(logFile, logContentPre + "Robot putMaterialOntoMachine(" + uploadWorkpieceIDList.get(i) + ",RobotSlot_" + uploadTraySlotsList.get(i) + "=>Zone_" + emptyWorkzonesList.get(0) + ") done");
                        robotData.deleteMaterialInfo(robotIP, uploadWorkpieceIDList.get(i));
                        robotData.setData(robotIP, RobotItems.GRIPMATERIAL, "");
                        robotData.updateSlot(robotIP, Integer.parseInt(uploadTraySlotsList.get(i)), "");
                        cncData.setWorkpieceID(cncIP, Integer.parseInt(uploadWorkZoneList.get(i)), uploadWorkpieceIDList.get(i));                    //record cnc_slot_position and relevant workpiece's id
                        cncData.setNCProgram(cncIP, Integer.parseInt(uploadWorkZoneList.get(i)), wpData.getNextProcProgram(uploadWorkpieceIDList.get(i), cncModel, null));
                        cncData.setSpecNo(cncIP, Integer.parseInt(uploadWorkZoneList.get(i)), wpData.getSpecNo(uploadWorkpieceIDList.get(i)));
                        cncData.setZoneSimulationTime(cncIP, Integer.parseInt(uploadWorkZoneList.get(i)), wpData.getNextProcSimtime(uploadWorkpieceIDList.get(i), cncModel, null));
                        cncCtrl.startMachining(cncIP, ""); //close gas valve
                        emptyTraySlotsList.addFirst(uploadTraySlotsList.get(i));
                    }else {
                        robotData.setData(robotIP, RobotItems.GRIPMATERIAL, uploadWorkpieceIDList.get(i));
                        robotData.addMaterialInfo(robotIP, uploadWorkpieceIDList.get(i), "0", DataUtils.getDevStateByString(wpStates[i]));
                        LogUtils.operationLog(logFile, logContentPre + "Robot putMaterialOntoMachine(" + uploadWorkpieceIDList.get(i) + "," + uploadWorkZoneList.get(i) + ") failed");
                        return false;
                    }
                }

            }

//            DAO dao = new DAOImpl(taskInfoDto);
//            TaskInfo taskInfo = new TaskInfo();
//            try {
//                List<Object> os = dao.findByCnd(new String[]{"cnc_ip", "have_done"}, new String[]{cncIP, "0"});
//                for(Object o : os){
//                    taskInfo = (TaskInfo)o;
//                }
//                com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
//                jsonObject.put("unload_workzone",record_workzone);
//                jsonObject.put("unload_workpieceID",record_workpieceID);
//                taskInfo.setTask_info(jsonObject.toJSONString());
//                taskInfo.setHave_done(0);
//                dao.update(taskInfo);
//            } catch (SQLException throwables) {
//                throwables.printStackTrace();
//            }
            DAO dao = new DAOImpl(taskInfoDto);
            TaskInfo taskInfo = new TaskInfo();
            try {
                List<Object> os = dao.findByCnd(new String[]{"cnc_ip", "have_done"}, new String[]{cncIP, "0"});
                for(Object o : os){
                    taskInfo = (TaskInfo)o;
                }
                com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
                jsonObject.put("unload_workzone",record_workzone);
                jsonObject.put("unload_workpieceID",record_workpieceID);
                taskInfo.setTask_info(jsonObject.toJSONString());
                taskInfo.setHave_done(0);
                dao.update(taskInfo);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }
        return bOK;
    }


    /**
     * Load materials from robot's tray onto target machine
     *
     * @param robotCtrl     remote controller of robot
     * @param robotIP       ip of robot
     * @param cncIP         ip of machine
     * @param logFile       log file full path
     * @param logContentPre prefix of log content
     * @return true if all materials are successfully loaded onto machine
     */
    //Change Robot to IRobot by Hui Zhi 2021.11.3
    public boolean loadMaterialOntoMachine(IRobot robotCtrl, String robotIP, CNC cncCtrl, String cncIP, String logFile, String logContentPre) {
//	public boolean loadMaterialOntoMachine(Robot robotCtrl, String robotIP, String cncIP, String logFile, String logContentPre){
        String record_workzone = "";
        String record_workpieceID = "";
        String taskInfoDto = "com.cncmes.dto.TaskInfo";
        boolean bOK = true;

        RobotData robotData = RobotData.getInstance();
        CncData cncData = CncData.getInstance();
        WorkpieceData wpData = WorkpieceData.getInstance();
        String[] workzones = cncData.getEmptyWorkzones(cncIP);

        LinkedHashMap<RobotItems, String> info = robotData.getMaterialInfo(robotIP);
        String wpIDs = info.get(RobotItems.WPIDS);
        String wpSlots = info.get(RobotItems.WPSLOTIDS); //在机器人托盘上的物料所在的slot位置

        String states = info.get(RobotItems.WPSTATES);
        String cncModel = cncData.getCncModel(cncIP);

        if ("".equals(wpIDs) || null == workzones) {

            if ("".equals(wpIDs))
                LogUtils.operationLog(logFile, logContentPre + "Robot loadMaterialOntoMachine failed cause of tray is empty");
            if (null == workzones)
                LogUtils.operationLog(logFile, logContentPre + "Robot loadMaterialOntoMachine failed cause of machine is full");
            bOK = false;
        } else {
            String[] workpieceIDs = wpIDs.split(";");       //workpiece's id
            String[] traySlotsNo = wpSlots.split(";");      //position in robot tray
            String[] wpStates = states.split(";");          //workpiece's state


            int index = -1;

            String cncPosition = cncData.getCNCPosition(cncIP);
            String cncZone = "";
            if ("cnc79marker".equals(cncPosition)) {
                cncZone = "cnc_79";
            } else {
                String[] tempSpace = cncPosition.split("_");
                cncZone = "cnc_"+tempSpace[tempSpace.length-1];      //use cnc's ID.it can get from the tagname
//                cncZone = "cnc_72";
            }
            int cnc_pos = 1;
            for (String slotNo : traySlotsNo) {
                index++;
                if (index < workzones.length) {     //workzones is cnc empty position
                    // use pickAndPlace instead of putMaterialOntoMachine   2021.11.3  Hui Zhi
                    //if(!robotCtrl.putMaterialOntoMachine(robotIP, cncIP, Integer.parseInt(workzones[index]), slotNo, cncModel)){

                    /* Check Position and Rotate same angle
                     * and then release fixture
                     * */

                    try {

                        cnc_pos = Integer.parseInt(XmlUtils.getCncConfig(cncIP, "workzone" + (index+1)));        //修改cnc放夹具的位置
                        if(record_workzone.equals(""))
                        {
                            record_workzone = record_workzone + cnc_pos;
                        }else {
                            record_workzone = record_workzone +";"+cnc_pos;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }

                    if(record_workpieceID.equals(""))
                    {
                        record_workpieceID = record_workpieceID + workpieceIDs[index];
                    }else {
                        record_workpieceID = record_workpieceID +";"+workpieceIDs[index];
                    }


                    //cnc rotate and pickAndPlace at the same time
                    Thread threadReleaseFixture = new Thread(new Runnable() {
                        int cnc_pos, index;

                        public Runnable setParam(int param1, int param2) {
                            cnc_pos = param1;
                            index = param2;
                            return this;
                        }

                        @Override
                        public void run() {
                            if (!cncCtrl.hfReleaseFixture(cncIP, cnc_pos)) {
                                LogUtils.operationLog(logFile, logContentPre + "Robot putMaterialOntoMachine(" + workpieceIDs[index] + ",RobotSlot_" + slotNo + "=>Zone_" + cnc_pos + ") CNC Rotate error");
                            }
                        }
                    }.setParam(cnc_pos, index));
                    threadReleaseFixture.start();

//					if (!cncCtrl.hfReleaseFixture(cncIP, Integer.parseInt(workzones[index]))){
//						LogUtils.operationLog(logFile, logContentPre+"Robot putMaterialOntoMachine("+workpieceIDs[index]+",RobotSlot_"+slotNo+"=>Zone_"+workzones[index]+") CNC Rotate error");
//						bOK = false;
//						break;
//					}
                    ArrayList<Object> rtn = robotCtrl.pickAndPlace(robotIP, "robot_pallet_a_slot_" + slotNo, cncZone);
                    if (!(Boolean) rtn.get(0)) { //  + Integer.parseInt(workzones[index]))){
                        bOK = false;
                        robotData.setData(robotIP, RobotItems.GRIPMATERIAL, workpieceIDs[index]);
                        robotData.addMaterialInfo(robotIP, workpieceIDs[index], "0", DataUtils.getDevStateByString(wpStates[index]));
                        LogUtils.operationLog(logFile, logContentPre + "Robot putMaterialOntoMachine(" + workpieceIDs[index] + "," + cnc_pos + ") failed");
                        break;
                    } else {
                        LogUtils.operationLog(logFile, logContentPre + "Robot putMaterialOntoMachine(" + workpieceIDs[index] + ",RobotSlot_" + slotNo + "=>Zone_" + cnc_pos + ") done");
                        robotData.deleteMaterialInfo(robotIP, workpieceIDs[index]);
                        robotData.setData(robotIP, RobotItems.GRIPMATERIAL, "");
                        robotData.updateSlot(robotIP, Integer.parseInt(slotNo), "");
                        cncData.setWorkpieceID(cncIP, cnc_pos, workpieceIDs[index]);                    //record cnc_slot_position and relevant workpiece's id
                        cncData.setNCProgram(cncIP, cnc_pos, wpData.getNextProcProgram(workpieceIDs[index], cncModel, null));
                        cncData.setSpecNo(cncIP, cnc_pos, wpData.getSpecNo(workpieceIDs[index]));
                        cncData.setZoneSimulationTime(cncIP, cnc_pos, wpData.getNextProcSimtime(workpieceIDs[index], cncModel, null));
                        cncCtrl.startMachining(cncIP, ""); //close gas valve
                    }
                }
            }
            // Clamp all fixture
//            if (!cncCtrl.hfClampFixture(cncIP, 1)) {
//                LogUtils.operationLog(logFile, logContentPre + "Robot putMaterialOntoMachine CNC Clamp Fixture error");
//                bOK = false;
//            }

//--------------------------------------------------record each CNC workzone unload info for load/unload
            DAO dao = new DAOImpl(taskInfoDto);
            TaskInfo taskInfo = new TaskInfo();
            try {
                List<Object> os = dao.findByCnd(new String[]{"cnc_ip", "have_done"}, new String[]{cncIP, "0"});
                for(Object o : os){
                    taskInfo = (TaskInfo)o;
                }
                com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
                jsonObject.put("unload_workzone",record_workzone);
                jsonObject.put("unload_workpieceID",record_workpieceID);
                taskInfo.setTask_info(jsonObject.toJSONString());
                taskInfo.setHave_done(0);
                dao.update(taskInfo);


            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }
        return bOK;
    }

    /**
     * Add new materials into the memory for machining status tracing
     *
     * @param wpData  reference of the workpieces data
     * @param wpIDs   the new added workpiece IDs
     * @param rackIDs the workpieces are from this material rack
     * @param slotIDs the workpieces are from these slot IDs of the material rack
     * @return all new added workpieces in string array
     */
    private String[] addNewWorkpieces(WorkpieceData wpData, String wpIDs, String rackIDs, String slotIDs) {
        String[] ids;
        String[] rIDs;
        String[] sIDs;
        ids = wpIDs.split(";");
        rIDs = rackIDs.split(";");
        sIDs = slotIDs.split(";");
        for (int i = 0; i < ids.length; i++) {
            wpData.setData(ids[i], WorkpieceItems.ID, ids[i]);
            DataUtils.updateWorkpieceData(ids[i], lineName, rIDs[i], sIDs[i]);
        }
        SystemInfo.addTotalWorkpieceQty(ids.length);
        return ids;
    }

    /**
     * Check whether it is safe to close door
     *
     * @param timeout_s safe check timeout
     * @return true if it is safe to close door
     */
    public boolean safeToCloseDoor(String cncIP, String robotIP, int timeout_s) {
        boolean safe = true;
        CncData cncData = CncData.getInstance();
        RobotData robotData = RobotData.getInstance();
        String machineTag = (String) cncData.getItemVal(cncIP, CncItems.TAGNAME);
        String robotSleep = (String) robotData.getItemVal(robotIP, RobotItems.SLEEP);
        String robotPos = (String) robotData.getItemVal(robotIP, RobotItems.POSITION);
        String robotParking = (String) robotData.getItemVal(robotIP, RobotItems.POS_PARKING);
        if (null != robotParking && machineTag.equals(robotPos)) safe = false;

        if (!safe) {
            while (true) {
                if ("Sleeping".equals(robotSleep)) {
                    RunningMsg.set("Robot is sleeping.");
                    try {
                        Thread.sleep(1000);
                        RunningMsg.set("Robot is sleeping..");
                        Thread.sleep(1000);
                        RunningMsg.set("Robot is sleeping...");
                    } catch (InterruptedException e) {
                    }
                } else {
                    break;
                }
            }

            long startT = System.currentTimeMillis();
            long timePassed = 0, timeout_ms = timeout_s * 1000;
            while (true) {
                timePassed = System.currentTimeMillis() - startT;
                robotPos = (String) robotData.getItemVal(robotIP, RobotItems.POSITION);
                robotParking = (String) robotData.getItemVal(robotIP, RobotItems.POS_PARKING);
                if (null != robotParking && machineTag.equals(robotPos)) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                    }
                } else {
                    safe = true;
                    break;
                }

                if (timePassed > timeout_ms) break;
            }
        }

        return safe;
    }

    private String initWorkpieceData(int[] workZones, String[] workpieceIDs, String lineName, String cncModel, boolean failExitingID) {
        String errMsg = "", ids = "";
        WorkpieceData wpData = WorkpieceData.getInstance();

        for (int i = 0; i < workpieceIDs.length; i++) {
            if ("".equals(workpieceIDs[i])) continue;
            if (null == wpData.getData(workpieceIDs[i]) || (null != wpData.getData(workpieceIDs[i]) && !failExitingID)) {
                if (null != wpData.getData(workpieceIDs[i])) {
                    if (wpData.canMachineByCNC(workpieceIDs[i], cncModel, null)) {
                        wpData.removeData(workpieceIDs[i]);
                    } else {
                        errMsg = workpieceIDs[i] + " can't be machined by " + cncModel;
                        break;
                    }
                }
                wpData.setData(workpieceIDs[i], WorkpieceItems.ID, workpieceIDs[i]);
                DataUtils.updateWorkpieceData(workpieceIDs[i], lineName, "3", "" + workZones[i]);
                if ("".equals(ids)) {
                    ids = workpieceIDs[i];
                } else {
                    ids += ";" + workpieceIDs[i];
                }
                if (wpData.canMachineByCNC(workpieceIDs[i], cncModel, null)) {
                    LinkedHashMap<SpecItems, Object> spec = wpData.getNextProcInfo(workpieceIDs[i]);
                    int curProc = wpData.getNextProcNo(workpieceIDs[i], spec);
                    int procTime = wpData.getNextProcSimtime(workpieceIDs[i], cncModel, spec, curProc);
                    wpData.setCurrentProcNo(workpieceIDs[i], curProc);
                    wpData.appendData(workpieceIDs[i], WorkpieceItems.NCMODEL, cncModel);
                    wpData.appendData(workpieceIDs[i], WorkpieceItems.MACHINETIME, "" + procTime);
                    wpData.appendData(workpieceIDs[i], WorkpieceItems.PROCESS, wpData.getNextProcName(workpieceIDs[i], spec, curProc));
                    wpData.appendData(workpieceIDs[i], WorkpieceItems.PROGRAM, wpData.getNextProcProgram(workpieceIDs[i], cncModel, spec, curProc));
                    wpData.appendData(workpieceIDs[i], WorkpieceItems.SURFACE, "" + wpData.getNextProcSurface(workpieceIDs[i], spec, curProc));
                } else {
                    errMsg = workpieceIDs[i] + " can't be machined by " + cncModel;
                    break;
                }
            } else {
                if (failExitingID) {
                    errMsg = workpieceIDs[i] + " is already existing and can't be override";
                    break;
                }
            }
        }

        if (!"".equals(errMsg) && !"".equals(ids)) removeWorkpieceData(ids.split(";"));

        return errMsg;
    }

    private void removeWorkpieceData(String[] workpieceIDs) {
        WorkpieceData wpData = WorkpieceData.getInstance();
        for (int i = 0; i < workpieceIDs.length; i++) {
            wpData.removeData(workpieceIDs[i]);
        }
    }

    private boolean startMachining(CNC cncCtrl, String cncIP, String programName, int retryTimes, int timeout_s) {
        boolean ok = false;

        long startT, diffTime, timeout_ms;
        DeviceState cncState = DeviceState.STANDBY;

        timeout_ms = timeout_s * 1000;
        for (int i = 0; i < retryTimes; i++) {
            // just for trial run remove main program
            //if(cncCtrl.startMachining(cncIP, programName)){
            if (cncCtrl.startMachining(cncIP, "")) {
                startT = System.currentTimeMillis();
                while (true) {
                    diffTime = System.currentTimeMillis() - startT;
                    cncState = cncCtrl.getMachineState(cncIP);
                    if (DeviceState.WORKING == cncState) {
                        ok = true;
                        break;
                    } else {
                        try {
                            Thread.sleep(1000);
                            RunningMsg.set("Start machining...");
                        } catch (InterruptedException e) {
                        }
                    }
                    if (diffTime > timeout_ms) break;
                }
            }
            if (ok) break;
        }

        return ok;
    }

    public String openDoorEx(CNC cncCtrl, String cncIP) {
        String errMsg = "";
        CncData cncData = CncData.getInstance();

        while (true) {
            if (cncCtrl.openDoor(cncIP)) {
                cncData.setData(cncIP, CncItems.OP_OPENDOOR, "1");
                cncData.setData(cncIP, CncItems.OP_CLOSEDOOR, "");
                break;
            } else {
                errMsg = "开门失败，重试【开门】操作前请重启控制箱：\r\n";
                errMsg += "1.关闭控制箱电源（开关打到11点钟方向）\r\n";
                errMsg += "2.等待5秒钟后打开控制箱电源（开关打到12点钟方向）\r\n\r\n";
                errMsg += "重试开门操作请点击【是】按钮，\r\n否则请点击【否】终止任务。";
                MyConfirmDialog.showDialog("请确认是否重试【开门】操作", errMsg);
                if (MyConfirmDialog.OPTION_NO == MyConfirmDialog.getConfirmFlag()) {
                    errMsg = "Fail to open door";
                    break;
                } else {
                    errMsg = "";
                }
            }
        }

        return errMsg;
    }

    public String closeDoorEx(CNC cncCtrl, String cncIP) {
        String errMsg = "";
        CncData cncData = CncData.getInstance();

        while (true) {
            if (cncCtrl.closeDoor(cncIP)) {
                cncData.setData(cncIP, CncItems.OP_CLOSEDOOR, "1");
                cncData.setData(cncIP, CncItems.OP_OPENDOOR, "");
                break;
            } else {
                errMsg = "关门失败，重试【关门】操作前请重启控制箱：\r\n";
                errMsg += "1.关闭控制箱电源（开关打到11点钟方向）\r\n";
                errMsg += "2.等待5秒钟后打开控制箱电源（开关打到12点钟方向）\r\n\r\n";
                errMsg += "重试关门操作请点击【是】按钮，\r\n否则请点击【否】终止任务。";
                MyConfirmDialog.showDialog("请确认是否重试【关门】操作", errMsg);
                if (MyConfirmDialog.OPTION_NO == MyConfirmDialog.getConfirmFlag()) {
                    errMsg = "Fail to close door";
                    break;
                } else {
                    errMsg = "";
                }
            }
        }

        return errMsg;
    }

    public String doCleaning(CNC cncCtrl, String cncIP, int[] workZones, String[] workpieceIDs, JProgressBar progBar) {
        String rtn = "", lineName = "", cncModel = "";
        CncData cncData = CncData.getInstance();
        lineName = cncData.getLineName(cncIP);
        cncModel = cncData.getCncModel(cncIP);

        rtn = initWorkpieceData(workZones, workpieceIDs, lineName, cncModel, true);
        if (!"".equals(rtn)) return rtn;
        if (!"".equals(openDoorEx(cncCtrl, cncIP))) return "Cleaning:Open door failed";
        if (!"".equals(closeDoorEx(cncCtrl, cncIP))) return "Cleaning:Close door failed";
        if (!UploadNCProgram.uploadSubPrograms(cncCtrl, cncIP, workZones, workpieceIDs))
            return "Cleaning:Upload sub programs failed";

        //disable by Hui Zhi 2021/11/13
        //if(!UploadNCProgram.uploadMainProgram(cncCtrl, cncIP, workZones, workpieceIDs)) return "Cleaning:Upload main program failed";

        CncWebAPI cncWebAPI = CncWebAPI.getInstance();

        String mainProgramName = cncWebAPI.getMainProgramName(cncData.getCncModel(cncIP));
        if (startMachining(cncCtrl, cncIP, mainProgramName, 3, 10)) {
            DeviceMonitor devMonitor = DeviceMonitor.getInstance();
            if (!devMonitor.cncMachiningIsDone(cncCtrl, cncIP, 480, progBar)) {
                return "Cleaning failed";
            } else {
                removeWorkpieceData(workpieceIDs);
                try {
                    Thread.sleep(5000);//Waiting for door unlock
                } catch (InterruptedException e) {
                }
            }
        } else {
            return "Cleaning:Start cleaning program failed";
        }

        return rtn;
    }

    //Change Robot to IRobot by Hui Zhi 2021.11.3
    public boolean runConcurrentOP(DeviceOP deviceOP, String cncIP, String robotIP, JSONObject jsonParas, CNC cncCtrl, IRobot robotCtrl, int retryTimes, int checkTimeout_s) {
//	public boolean runConcurrentOP(DeviceOP deviceOP, String cncIP, String robotIP, JSONObject jsonParas, CNC cncCtrl, Robot robotCtrl, int retryTimes, int checkTimeout_s){
        boolean success = false;
        String opFlag = "";

        for (int i = 0; i < retryTimes; i++) {
            ThreadUtils.Run(new ConcurrentOP(deviceOP, cncIP, robotIP, jsonParas, cncCtrl, robotCtrl));

            CncData cncData = CncData.getInstance();
            RobotData robotData = RobotData.getInstance();

            long startT = System.currentTimeMillis();
            long timeout_ms = checkTimeout_s * 1000;
            long time_diff = 0;
            while (true) {
                time_diff = System.currentTimeMillis() - startT;
                switch (deviceOP) {

                    case CNC_HF_OPENDOOR:
                        opFlag = (String) cncData.getItemVal(cncIP, CncItems.OP_HF_OPENDOOR);
                        if ("-1".equals(opFlag)) success = true;
                        break;
                    case CNC_HF_CLOSEDOOR:
                        opFlag = (String) cncData.getItemVal(cncIP, CncItems.OP_HF_CLOSEDOOR);
                        if ("-1".equals(opFlag)) success = true;
                        break;
                    case CNC_HF_RELEASEFIXTURE:
                        opFlag = (String) cncData.getItemVal(cncIP, CncItems.OP_HF_RELEASEFIXTURE);
                        if ("-1".equals(opFlag)) success = true;
                        break;
                    case CNC_HF_CLAMPFIXTURE:
                        opFlag = (String) cncData.getItemVal(cncIP, CncItems.OP_HF_CLAMPFIXTURE);
                        if ("-1".equals(opFlag)) success = true;
                        break;
                    case CNC_AXIS4_MOVE:
                        opFlag = (String) cncData.getItemVal(cncIP, CncItems.OP_AXIS4MOVE);
                        if ("-1".equals(opFlag)) success = true;
                        break;
                    case CNC_OPENDOOR:
                        opFlag = (String) cncData.getItemVal(cncIP, CncItems.OP_OPENDOOR);
                        if ("-1".equals(opFlag)) success = true;
                        break;
                    case CNC_CLOSEDOOR:
                        opFlag = (String) cncData.getItemVal(cncIP, CncItems.OP_CLOSEDOOR);
                        if ("-1".equals(opFlag)) success = true;
                        break;
                    case CNC_RELEASEFIXTURE:
                        opFlag = (String) cncData.getItemVal(cncIP, CncItems.OP_RELEASEFIXTURE);
                        if ("-1".equals(opFlag)) success = true;
                        break;
                    case CNC_CLAMPFIXTURE:
                        opFlag = (String) cncData.getItemVal(cncIP, CncItems.OP_CLAMPFIXTURE);
                        if ("-1".equals(opFlag)) success = true;
                        break;
                    case CNC_CLEANING:
                        opFlag = (String) cncData.getItemVal(cncIP, CncItems.OP_CLEANING);
                        if ("-1".equals(opFlag)) success = true;
                        break;
                    case ROBOT_MOVETORACK:
                        opFlag = (String) robotData.getItemVal(robotIP, RobotItems.OP_MOVETORACK);
                        if ("-1".equals(opFlag)) success = true;
                        break;
                    case ROBOT_MOVETOMACHINE:
                        opFlag = (String) robotData.getItemVal(robotIP, RobotItems.OP_MOVETOMACHINE);
                        if ("-1".equals(opFlag)) success = true;
                        break;
                    //test robot charge 2023.10.5
                    case ROBOT_GOTOCHARGE:
                            opFlag = (String) robotData.getItemVal(robotIP, RobotItems.OP_GOTOCHARGE);
                        if ("1".equals(opFlag)) success = true;
                        break;

                    case ROBOT_PNPTESTING:
                        opFlag = (String) robotData.getItemVal(robotIP, RobotItems.OP_PNPTESTING);
                        if ("0".equals(opFlag) || "-1".equals(opFlag) || "-2".equals(opFlag)) success = true;
                        break;
                }
                if (time_diff > timeout_ms || success) break;
            }

            if (success) break;
        }

        if (!success)
            LogUtils.errorLog(deviceOP + ":" + opFlag + "(Retry=" + retryTimes + ",Timeout=" + checkTimeout_s + "s)");
        return success;
    }

    public boolean concurrentOPDone(String cncIP, String robotIP, DeviceOP devOP, int timeout_s) {
        boolean done = false, loop = true;
        String opFlag = "";

        long startT = System.currentTimeMillis();
        long timePassed = 0, timeout_ms = timeout_s * 1000;

        CncData cncData = CncData.getInstance();
        RobotData robotData = RobotData.getInstance();

        while (loop) {
            try {
                timePassed = System.currentTimeMillis() - startT;
                switch (devOP) {
                    case CNC_HF_OPENDOOR:
                        if ("1".equals((String) cncData.getItemVal(cncIP, CncItems.OP_HF_OPENDOOR))) {
                            done = true;
                        } else if ("0".equals((String) cncData.getItemVal(cncIP, CncItems.OP_HF_OPENDOOR))) {
                            loop = false;
                        }
                        break;
                    case CNC_HF_CLOSEDOOR:
                        if ("1".equals((String) cncData.getItemVal(cncIP, CncItems.OP_HF_CLOSEDOOR))) {
                            done = true;
                        } else if ("0".equals((String) cncData.getItemVal(cncIP, CncItems.OP_HF_CLOSEDOOR))) {
                            loop = false;
                        }
                        break;
                    case CNC_HF_RELEASEFIXTURE:
                        if ("1".equals((String) cncData.getItemVal(cncIP, CncItems.OP_HF_RELEASEFIXTURE))) {
                            done = true;
                        } else if ("0".equals((String) cncData.getItemVal(cncIP, CncItems.OP_HF_RELEASEFIXTURE))) {
                            loop = false;
                        }
                        break;
                    case CNC_HF_CLAMPFIXTURE:
                        if ("1".equals((String) cncData.getItemVal(cncIP, CncItems.OP_HF_CLAMPFIXTURE))) {
                            done = true;
                        } else if ("0".equals((String) cncData.getItemVal(cncIP, CncItems.OP_HF_CLAMPFIXTURE))) {
                            loop = false;
                        }
                        break;
                    case CNC_OPENDOOR:
                        if ("1".equals((String) cncData.getItemVal(cncIP, CncItems.OP_OPENDOOR))) {
                            done = true;
                        } else if ("0".equals((String) cncData.getItemVal(cncIP, CncItems.OP_OPENDOOR))) {
                            loop = false;
                        }
                        break;
                    case CNC_CLOSEDOOR:
                        if ("1".equals((String) cncData.getItemVal(cncIP, CncItems.OP_CLOSEDOOR))) {
                            done = true;
                        } else if ("0".equals((String) cncData.getItemVal(cncIP, CncItems.OP_CLOSEDOOR))) {
                            loop = false;
                        }
                        break;
                    case CNC_RELEASEFIXTURE:
                        if ("1".equals((String) cncData.getItemVal(cncIP, CncItems.OP_RELEASEFIXTURE))) {
                            done = true;
                        } else if ("0".equals((String) cncData.getItemVal(cncIP, CncItems.OP_RELEASEFIXTURE))) {
                            loop = false;
                        }
                        break;
                    case CNC_CLAMPFIXTURE:
                        if ("1".equals((String) cncData.getItemVal(cncIP, CncItems.OP_CLAMPFIXTURE))) {
                            done = true;
                        } else if ("0".equals((String) cncData.getItemVal(cncIP, CncItems.OP_CLAMPFIXTURE))) {
                            loop = false;
                        }
                        break;
                    case CNC_CLEANING:
                        if ("1".equals((String) cncData.getItemVal(cncIP, CncItems.OP_CLEANING))) {
                            done = true;
                        } else if ("0".equals((String) cncData.getItemVal(cncIP, CncItems.OP_CLEANING))) {
                            loop = false;
                        }
                        break;
                    case ROBOT_MOVETORACK:
                        startT = robotSleeping(startT, robotIP);
                        if ("1".equals((String) robotData.getItemVal(robotIP, RobotItems.OP_MOVETORACK))) {
                            done = true;
                        } else if ("0".equals((String) robotData.getItemVal(robotIP, RobotItems.OP_MOVETORACK))) {
                            loop = false;
                        }
                        break;
                    case ROBOT_MOVETOMACHINE:
                        startT = robotSleeping(startT, robotIP);
                        if ("1".equals((String) robotData.getItemVal(robotIP, RobotItems.OP_MOVETOMACHINE))) {
                            done = true;
                        } else if ("0".equals((String) robotData.getItemVal(robotIP, RobotItems.OP_MOVETOMACHINE))) {
                            loop = false;
                        }
                        break;
                    case ROBOT_PNPTESTING:
                        startT = robotSleeping(startT, robotIP);
                        opFlag = (String) robotData.getItemVal(robotIP, RobotItems.OP_PNPTESTING);
                        if ("0".equals(opFlag) || "-2".equals(opFlag)) {
                            loop = false;
                        } else if ("1".equals(opFlag)) {
                            done = true;
                        }
                        break;
                    //test robot charge 2023.10.5
                    case ROBOT_GOTOCHARGE:
                        startT = robotSleeping(startT, robotIP);
                        if ("1".equals((String) robotData.getItemVal(robotIP, RobotItems.OP_GOTOCHARGE))) {
                            done = true;
                        } else if ("0".equals((String) robotData.getItemVal(robotIP, RobotItems.OP_GOTOCHARGE))) {
                            loop = false;
                        }
                        break;

                    default:
                        loop = false;
                }
                if (timePassed > timeout_ms || done) break;
            } catch (Exception e) {
                LogUtils.errorLog(devOP + "/" + robotIP + "->" + cncIP + "/" + timeout_s + ":" + e.getMessage());
            }
        }

        return done;
    }

    private long robotSleeping(long sleepDoneT, String robotIP) {
        long doneT = sleepDoneT;
        String robotSleep = "";
        RobotData robotData = RobotData.getInstance();

        while (true) {
            robotSleep = (String) robotData.getItemVal(robotIP, RobotItems.SLEEP);
            if ("Sleeping".equals(robotSleep)) {
                doneT = System.currentTimeMillis();
                RunningMsg.set("Robot is sleeping.");
                try {
                    Thread.sleep(1000);
                    RunningMsg.set("Robot is sleeping..");
                    Thread.sleep(1000);
                    RunningMsg.set("Robot is sleeping...");
                } catch (InterruptedException e) {
                }
            } else {
                break;
            }
        }

        return doneT;
    }

    class ConcurrentOP implements Runnable {
        private JSONObject jsonParas;
        private DeviceOP deviceOP;
        private CNC cncCtrl;

        //private Robot robotCtrl;//disable
        private IRobot robotCtrl;//Change Robot to IRobot by Hui Zhi 2021.11.3

        private CncData cncData = CncData.getInstance();
        private RobotData robotData = RobotData.getInstance();

        private String cncIP;
        private String robotIP;

        //Change Robot to IRobot by Hui Zhi 2021.11.3
        public ConcurrentOP(DeviceOP deviceOP, String cncIP, String robotIP, JSONObject jsonParas, CNC cncCtrl, IRobot robotCtrl) {
            //public ConcurrentOP(DeviceOP deviceOP, String cncIP, String robotIP, JSONObject jsonParas, CNC cncCtrl, Robot robotCtrl){
            this.deviceOP = deviceOP;
            this.jsonParas = jsonParas;        //input paras
            this.cncCtrl = cncCtrl;
            this.robotCtrl = robotCtrl;

            this.cncData = CncData.getInstance();
            this.robotData = RobotData.getInstance();

            this.cncIP = cncIP;
            this.robotIP = robotIP;
        }

        @Override
        public void run() {
//			String cncModel = cncData.getCncModel(cncIP);
            boolean okToMove = true;
            LinkedHashMap<Integer, String> wpIDs = null;

            switch (deviceOP) {

                case CNC_HF_OPENDOOR:
                    cncData.setData(cncIP, CncItems.OP_HF_OPENDOOR, "-1");
                    if (cncCtrl.hfOpenDoor(cncIP)) {
                        cncData.setData(cncIP, CncItems.OP_HF_OPENDOOR, "1");
                        cncData.setData(cncIP, CncItems.OP_HF_CLOSEDOOR, "");
                    } else {
                        cncData.setData(cncIP, CncItems.OP_HF_OPENDOOR, "0");
                    }

                    break;
                case CNC_HF_CLOSEDOOR:
                    cncData.setData(cncIP, CncItems.OP_HF_CLOSEDOOR, "-1");
                    if (cncCtrl.hfCloseDoor(cncIP)) {
                        cncData.setData(cncIP, CncItems.OP_HF_CLOSEDOOR, "1");
                        cncData.setData(cncIP, CncItems.OP_HF_OPENDOOR, "");
                    } else {
                        cncData.setData(cncIP, CncItems.OP_HF_CLOSEDOOR, "0");
                    }

                    break;
                case CNC_HF_RELEASEFIXTURE:

                    wpIDs = cncData.getWorkpieceIDs(cncIP);

                    if (wpIDs.size() > 0) {
                        cncData.setData(cncIP, CncItems.OP_HF_RELEASEFIXTURE, "-1");
                        int okCnt = 0;
                        for (int workZone : wpIDs.keySet()) {
                            if (!cncCtrl.hfReleaseFixture(cncIP, workZone)) {
                                cncData.setData(cncIP, CncItems.OP_HF_RELEASEFIXTURE, "0");
                                break;
                            } else {
                                okCnt++;
                            }
                        }

                        if (okCnt >= wpIDs.size()) {
                            cncData.setData(cncIP, CncItems.OP_HF_RELEASEFIXTURE, "1");
                            cncData.setData(cncIP, CncItems.OP_HF_CLAMPFIXTURE, "");
                        }
                    } else {
                        cncData.setData(cncIP, CncItems.OP_HF_RELEASEFIXTURE, "-2");
                    }

                    break;
                case CNC_HF_CLAMPFIXTURE:
                    wpIDs = cncData.getWorkpieceIDs(cncIP);
                    if (wpIDs.size() > 0) {
                        cncData.setData(cncIP, CncItems.OP_HF_CLAMPFIXTURE, "-1");
                        int okCnt = 0;
                        for (int workZone : wpIDs.keySet()) {
                            if (!cncCtrl.hfClampFixture(cncIP, workZone)) {
                                cncData.setData(cncIP, CncItems.OP_HF_CLAMPFIXTURE, "0");
                                break;
                            } else {
                                okCnt++;
                            }
                        }

                        if (okCnt >= wpIDs.size()) {
                            cncData.setData(cncIP, CncItems.OP_HF_CLAMPFIXTURE, "1");
                            cncData.setData(cncIP, CncItems.OP_HF_RELEASEFIXTURE, "");
                        }
                    } else {
                        cncData.setData(cncIP, CncItems.OP_HF_CLAMPFIXTURE, "-2");
                    }

                    break;
                case CNC_AXIS4_MOVE:
                    // setup Axis 4 for 0,90,180,270 angle

                    break;
                case CNC_OPENDOOR:
                    cncData.setData(cncIP, CncItems.OP_OPENDOOR, "-1");
//				if(cncCtrl.openDoor(cncIP)){
                    if (cncCtrl.openDoor(cncIP)) {
                        cncData.setData(cncIP, CncItems.OP_OPENDOOR, "1");
                        cncData.setData(cncIP, CncItems.OP_CLOSEDOOR, "");
                    } else {
                        cncData.setData(cncIP, CncItems.OP_OPENDOOR, "0");
                    }
                    break;
                case CNC_CLOSEDOOR:
                    cncData.setData(cncIP, CncItems.OP_CLOSEDOOR, "-1");
                    if (cncCtrl.closeDoor(cncIP)) {
                        cncData.setData(cncIP, CncItems.OP_CLOSEDOOR, "1");
                        cncData.setData(cncIP, CncItems.OP_OPENDOOR, "");
                    } else {
                        cncData.setData(cncIP, CncItems.OP_CLOSEDOOR, "0");
                    }
                    break;
                case CNC_RELEASEFIXTURE:
                    wpIDs = cncData.getWorkpieceIDs(cncIP);
                    if (wpIDs.size() > 0) {
                        cncData.setData(cncIP, CncItems.OP_RELEASEFIXTURE, "-1");
                        int okCnt = 0;
                        for (int workZone : wpIDs.keySet()) {
                            if (!cncCtrl.releaseFixture(cncIP, workZone)) {
                                cncData.setData(cncIP, CncItems.OP_RELEASEFIXTURE, "0");
                                break;
                            } else {
                                okCnt++;
                            }
                        }

                        if (okCnt >= wpIDs.size()) {
                            cncData.setData(cncIP, CncItems.OP_RELEASEFIXTURE, "1");
                            cncData.setData(cncIP, CncItems.OP_CLAMPFIXTURE, "");
                        }
                    } else {
                        cncData.setData(cncIP, CncItems.OP_RELEASEFIXTURE, "-2");
                    }
                    break;
                case CNC_CLAMPFIXTURE:
                    wpIDs = cncData.getWorkpieceIDs(cncIP);
                    if (wpIDs.size() > 0) {
                        cncData.setData(cncIP, CncItems.OP_CLAMPFIXTURE, "-1");
                        int okCnt = 0;
                        for (int workZone : wpIDs.keySet()) {
                            if (!cncCtrl.clampFixture(cncIP, workZone)) {
                                cncData.setData(cncIP, CncItems.OP_CLAMPFIXTURE, "0");
                                break;
                            } else {
                                okCnt++;
                            }
                        }

                        if (okCnt >= wpIDs.size()) {
                            cncData.setData(cncIP, CncItems.OP_CLAMPFIXTURE, "1");
                            cncData.setData(cncIP, CncItems.OP_RELEASEFIXTURE, "");
                        }
                    } else {
                        cncData.setData(cncIP, CncItems.OP_CLAMPFIXTURE, "-2");
                    }
                    break;
                case CNC_CLEANING:
                    if (null != jsonParas) {
                        cncData.setData(cncIP, CncItems.OP_CLEANING, "-1");
                        String cncIP = jsonParas.getString("cncIP");
                        String[] wkZones = jsonParas.getString("workZones").split(";");
                        String[] codeIDs = jsonParas.getString("codeIDs").split(";");
                        int[] workZones = new int[wkZones.length];
                        for (int i = 0; i < wkZones.length; i++) {
                            workZones[i] = Integer.parseInt(wkZones[i]);
                        }
                        if ("".equals(doCleaning(cncCtrl, cncIP, workZones, codeIDs, null))) {
                            cncData.setData(cncIP, CncItems.OP_CLEANING, "1");
                        } else {
                            cncData.setData(cncIP, CncItems.OP_CLEANING, "0");
                        }
                    } else {
                        cncData.setData(cncIP, CncItems.OP_CLEANING, "-2");
                    }
                    break;
                case ROBOT_MOVETORACK:
                    okToMove = true;
                    if ("-1".equals((String) robotData.getItemVal(robotIP, RobotItems.OP_MOVETOMACHINE))) {
                        okToMove = concurrentOPDone(cncIP, robotIP, DeviceOP.ROBOT_MOVETOMACHINE, CONCURR_OP_TIMEOUT);
                    }
                    if (okToMove) {
//					String rackID = "";
                        if (null != jsonParas) {
                            robotData.setData(robotIP, RobotItems.OP_MOVETORACK, "-1");
                            String rackID = jsonParas.getString("rackID");

                            String tagName = DataUtils.getRackByRackID(rackID).getTagname();
                            //if(robotCtrl.moveToRack(robotIP, rackID, cncModel)){
//						if(robotCtrl.dockTo(robotIP, robotData.getChargerPosition(robotIP))){// use dock to charger of IRobot 2021.11.3 Hui Zhi
                            //MODIFY in 2023 11.24
                            if (robotCtrl.dockTo(robotIP, tagName)) {
//						if(robotCtrl.dockTo(robotIP, robotData.getChargerPosition(robotIP))){
                                robotData.setData(robotIP, RobotItems.OP_MOVETORACK, "1");
                                robotData.setData(robotIP, RobotItems.OP_MOVETOMACHINE, "");
                            } else {
                                robotData.setData(robotIP, RobotItems.OP_MOVETORACK, "0");
                            }
                        } else {
                            robotData.setData(robotIP, RobotItems.OP_MOVETORACK, "-3");
                        }
                    } else {
                        robotData.setData(robotIP, RobotItems.OP_MOVETORACK, "-2");
                    }
                    break;
//test robot charge 2023.10.5

                case ROBOT_GOTOCHARGE:
                    okToMove = true;
//					if("-1".equals((String)robotData.getItemVal(robotIP, RobotItems.OP_GOTOCHARGE))){
//						okToMove = concurrentOPDone(cncIP, robotIP, DeviceOP.ROBOT_MOVETOMACHINE, CONCURR_OP_TIMEOUT);
//					}
                    if (okToMove) {
                        if (robotCtrl.gotoCharge(robotIP, true)) {
                            robotData.setData(robotIP, RobotItems.OP_GOTOCHARGE, "1");
                            robotData.setData(robotIP, RobotItems.OP_MOVETORACK, "");
                            robotData.setData(robotIP, RobotItems.OP_MOVETOMACHINE, "");
                        } else {
                            robotData.setData(robotIP, RobotItems.OP_GOTOCHARGE, "0");
                        }
                    } else {
                        robotData.setData(robotIP, RobotItems.OP_GOTOCHARGE, "-2");
                    }
                    break;
                case ROBOT_MOVETOMACHINE:
                    okToMove = true;
                    if ("-1".equals((String) robotData.getItemVal(robotIP, RobotItems.OP_MOVETORACK))) {
                        okToMove = concurrentOPDone(cncIP, robotIP, DeviceOP.ROBOT_MOVETORACK, CONCURR_OP_TIMEOUT);
                    }
                    if (okToMove) {
                        robotData.setData(robotIP, RobotItems.OP_MOVETOMACHINE, "-1");
                        //if(robotCtrl.moveToMachine(robotIP, cncIP, cncModel)){
                        if ("10.10.206.72".equals(cncIP)||"10.10.206.73".equals(cncIP)) {
                            robotCtrl.moveTo(robotIP, "pos03");
                        }
                        if (robotCtrl.dockTo(robotIP, cncData.getCNCPosition(cncIP))) {//use dock to machine of IRobot instead of moveToMachine 2021.11.3 Hui Zhi
                            robotData.setData(robotIP, RobotItems.OP_MOVETOMACHINE, "1");
                            robotData.setData(robotIP, RobotItems.OP_MOVETORACK, "");
                        } else {
                            robotData.setData(robotIP, RobotItems.OP_MOVETOMACHINE, "0");
                        }
                    } else {
                        robotData.setData(robotIP, RobotItems.OP_MOVETOMACHINE, "-2");
                    }
                    break;
                //disable ROBOT_PNPTESTING 2021.11.3 Hui Zhi
                case ROBOT_PNPTESTING:
//				if(!"0".equals((String)robotData.getItemVal(robotIP, RobotItems.OP_PNPTESTING))){
//					if("-1".equals((String)robotData.getItemVal(robotIP, RobotItems.OP_MOVETORACK))){
//						if(!concurrentOPDone(cncIP, robotIP, DeviceOP.ROBOT_MOVETORACK, CONCURR_OP_TIMEOUT)){
//							robotData.setData(robotIP, RobotItems.OP_PNPTESTING, "-2");
//							break;
//						}
//					}
//					if(!"1".equals((String)robotData.getItemVal(robotIP, RobotItems.OP_MOVETORACK))){
//						robotData.setData(robotIP, RobotItems.OP_PNPTESTING, "-2");
//						break;
//					}
//
//					robotData.setData(robotIP, RobotItems.OP_PNPTESTING, "-1");
//					robotData.setData(robotIP, RobotItems.OP_STOPPNPTESTING, "-1");
//					while(true){
//						if(!robotCtrl.pickMaterialFromTray(robotIP, 5, cncModel)){
//							robotData.setData(robotIP, RobotItems.OP_PNPTESTING, "0");
//							break;
//						}
//						if("1".equals((String)robotData.getItemVal(robotIP, RobotItems.OP_STOPPNPTESTING))){
//							robotData.setData(robotIP, RobotItems.OP_PNPTESTING, "1");
//							break;
//						}
//					}
//				}
                    break;
            }
        }
    }

    /**
     * Create threads to handle all machining tasks assigned by the scheduler
     */
    class TaskThread implements Runnable {
        @Override
        public void run() {
            TaskData taskData = TaskData.getInstance();
            RobotData robotData = RobotData.getInstance();
            WorkpieceData wpData = WorkpieceData.getInstance();
            CncData cncData = CncData.getInstance();
            CncDriver cncDriver = CncDriver.getInstance();
            RobotDriver robotDriver = RobotDriver.getInstance();

            String cncIP, robotIP, taskID;
            String cncModel, robotModel;
            String wpIDs = "", rackIDs = "", slotIDs = "", wpStates = "", error = "";
            String[] ids = null;
            LinkedHashMap<TaskItems, Object> task;

            while (!ThreadController.getStopFlag()) {
                try {
                    if (taskData.taskCount() > 0) {
                        task = taskData.getTask();
                        if (null == task) {
                            Thread.sleep(1000);
                            continue;
                        }
                        cncIP = (String) task.get(TaskItems.MACHINEIP);
                        robotIP = (String) task.get(TaskItems.ROBOTIP);
                        cncModel = (String) cncData.getData(cncIP).get(CncItems.MODEL);
                        robotModel = (String) robotData.getData(robotIP).get(RobotItems.MODEL);
                        DeviceState cncState = (DeviceState) task.get(TaskItems.MACHINESTATE);
                        System.out.println("at TaskThread :" + task);
                        //Clear the machining data
                        if (DeviceState.STANDBY == cncState) {
                            cncData.clearMachiningData(cncIP);
                            cncData.setData(cncIP, CncItems.DT_DATE, TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
                            cncData.setData(cncIP, CncItems.DT_READYLOADING, System.currentTimeMillis());
                            cncData.setData(cncIP, CncItems.D_ROBOTL, robotIP);
                            cncData.setData(cncIP, CncItems.D_ROBOTPOSL, robotData.getItemVal(robotIP, RobotItems.POSITION));
                            wpData.clearFinishedWorkpiece();
                        } else {
                            cncData.setData(cncIP, CncItems.D_ROBOTUL, robotIP);
                            cncData.setData(cncIP, CncItems.D_ROBOTPOSUL, robotData.getItemVal(robotIP, RobotItems.POSITION));
                        }

                        //Get task information
                        wpIDs = (String) task.get(TaskItems.MATERIALIDS);
                        rackIDs = (String) task.get(TaskItems.RACKIDS);
                        slotIDs = (String) task.get(TaskItems.SLOTIDS);
                        taskID = (String) task.get(TaskItems.SCHEDULERTASKID);
                        cncData.setData(cncIP, CncItems.D_TASKID, taskID);
                        robotData.setData(robotIP, RobotItems.TASKID, taskID);
                        //Refresh workpiece information - to trace the workpiece's status
                        if (!"".equals(wpIDs) && !"".equals(rackIDs) && !"".equals(slotIDs)) {
                            ids = addNewWorkpieces(wpData, wpIDs, rackIDs, slotIDs);
                        }

                        //Refresh task information
                        if (DeviceState.FINISH == cncState) {
                            LinkedHashMap<Integer, String> workpieceIDs = cncData.getWorkpieceIDs(cncIP);
                            wpIDs = "";
                            wpStates = "";
                            rackIDs = "";
                            String mID = "";
                            DeviceState mState = DeviceState.WORKING;
                            for (int zoneNo : workpieceIDs.keySet()) {
                                mID = workpieceIDs.get(zoneNo);
                                mState = wpData.workpieceIsDone(mID) ? DeviceState.FINISH : DeviceState.WORKING;
                                if ("".equals(wpIDs)) {
                                    wpIDs = mID;
                                    rackIDs = cncIP;
                                    slotIDs = "" + zoneNo;
                                    wpStates = "" + mState;
                                } else {
                                    wpIDs += ";" + mID;
                                    rackIDs += ";" + cncIP;
                                    slotIDs += ";" + zoneNo;
                                    wpStates += ";" + mState;
                                }
                                if (DeviceState.FINISH == mState) SystemInfo.addFinishedWorkpieceQty(1);
                            }
                            System.out.println("At TaskProcessor.TaskThread.CNC Finish wpIDS : "+wpIDs);
                            System.out.println("At TaskProcessor.TaskThread.CNC Finish rackIDs : "+rackIDs);
                            System.out.println("At TaskProcessor.TaskThread.CNC Finish slotIDs : "+slotIDs);
                            System.out.println("At TaskProcessor.TaskThread.CNC Finish wpStates : "+wpStates);
                            taskData.setData(taskID, TaskItems.MATERIALIDS, wpIDs);
                            taskData.setData(taskID, TaskItems.RACKIDS, rackIDs);
                            taskData.setData(taskID, TaskItems.SLOTIDS, slotIDs);
                            taskData.setData(taskID, TaskItems.MATERIALSTATES, wpStates);
                        }

                        if(DeviceState.PREPAREFINISH == cncState)
                        {
                            DAO dao = new DAOImpl("com.cncmes.dto.TaskInfo");
                            TaskInfo taskInfo = new TaskInfo();
                            try {
                                List<Object> os = dao.findByCnd(new String[]{"cnc_ip", "have_done"}, new String[]{cncIP, "0"});
                                for(Object o : os){
                                    taskInfo = (TaskInfo)o;
                                }
                                com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(taskInfo.getTask_info());
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                            LinkedHashMap<Integer, String> workpieceIDs = cncData.getWorkpieceIDs(cncIP);
                            wpIDs = "";
                            wpStates = "";
                            rackIDs = "";
                            String mID = "";
                            DeviceState mState = DeviceState.WORKING;
                            for (int zoneNo : workpieceIDs.keySet()) {
                                mID = workpieceIDs.get(zoneNo);
                                mState = wpData.workpieceIsDone(mID) ? DeviceState.FINISH : DeviceState.WORKING;
                                if ("".equals(wpIDs)) {
                                    wpIDs = mID;
                                    rackIDs = cncIP;
                                    slotIDs = "" + zoneNo;
                                    wpStates = "" + mState;
                                } else {
                                    wpIDs += ";" + mID;
                                    rackIDs += ";" + cncIP;
                                    slotIDs += ";" + zoneNo;
                                    wpStates += ";" + mState;
                                }
                                System.out.println("At TaskProcessor.TaskThread.CNC PREPAREFINISH wpIDS : "+wpIDs);
                                System.out.println("At TaskProcessor.TaskThread.CNC PREPAREFINISH rackIDs : "+rackIDs);
                                System.out.println("At TaskProcessor.TaskThread.CNC PREPAREFINISH slotIDs : "+slotIDs);
                                System.out.println("At TaskProcessor.TaskThread.CNC PREPAREFINISH wpStates : "+wpStates);
                                if (DeviceState.FINISH == mState) SystemInfo.addFinishedWorkpieceQty(1);
                            }
//                            taskData.setData(taskID, TaskItems.MATERIALIDS, wpIDs);
//                            taskData.setData(taskID, TaskItems.RACKIDS, rackIDs);
//                            taskData.setData(taskID, TaskItems.SLOTIDS, slotIDs);
//                            taskData.setData(taskID, TaskItems.MATERIALSTATES, );wpStates
                            //-------------------------------------------------------------------------------------
                            cncData.clearMachiningData(cncIP);
                            cncData.setData(cncIP, CncItems.DT_DATE, TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
                            cncData.setData(cncIP, CncItems.DT_READYLOADING, System.currentTimeMillis());
                            cncData.setData(cncIP, CncItems.D_ROBOTL, robotIP);
                            cncData.setData(cncIP, CncItems.D_ROBOTPOSL, robotData.getItemVal(robotIP, RobotItems.POSITION));
                            wpData.clearFinishedWorkpiece();
                        }

                        //Check device driver
                        String robotDrvName = (String) robotDriver.getData(robotModel).get(DriverItems.DRIVER);
                        String cncDrvName = (String) cncDriver.getData(cncModel).get(DriverItems.DRIVER);
                        String cncDataHandler = (String) cncDriver.getData(cncModel).get(DriverItems.DATAHANDLER);
                        CNC cncCtrl = CncFactory.getInstance(cncDrvName, cncDataHandler, cncModel);

                        //Change Robot to IRobot by Hui Zhi 2021.11.3
                        //Robot robotCtrl = RobotFactory.getInstance(robotDrvName);
                        IRobot robotCtrl = IRobotFactory.getInstance(robotDrvName);

                        if (null == cncCtrl) {
                            if (null != robotCtrl)
                                robotData.setRobotState(robotIP, DeviceState.STANDBY, false, false, true, false);
                            cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
                            taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_DRIVER.getErrDesc(), false);
                            if (DeviceState.STANDBY == cncState) resetMaterialState(ids);
                            RunningMsg.set("Driver of " + cncModel + "(" + cncIP + ") is wrong");
                            continue;
                        }

                        if (null == robotCtrl) {
                            robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                            cncData.setCncState(cncIP, cncState, false, false, true, false);
                            taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_DRIVER.getErrDesc(), false);
                            if (DeviceState.STANDBY == cncState) resetMaterialState(ids);
                            RunningMsg.set("Driver of " + robotModel + " is wrong");
                            continue;
                        }

                        //Lock the robot
                        taskData.setTaskState(taskID, DeviceState.LOCK, true, false, true, null, false);
                        robotData.setRobotState(robotIP, DeviceState.PLAN, false, false, true, false);

                        //Clear the alarming message
                        cncData.setData(cncIP, CncItems.ALARMMSG, "");
                        cncData.setCncLastState(cncIP, DeviceState.PLAN);

                        //Create task handling thread
                        Thread thr = new Thread(new TaskHandle(robotIP, cncIP, taskID));
                        //Alarm Thread will monitor entire line
                        Thread alarmMonitorthr = new Thread(new AlarmMonitorThread(taskID));
                        alarmMonitorthr.setName("AlarmMonitorThread_" + new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis()));
                        RunningMsg.set("TaskProcessor@" + thr.getName() + "|" + System.currentTimeMillis() + "|" + robotIP + "->" + cncIP);
                        thr.start();
                        alarmMonitorthr.start();
                        String logFile = LogUtils.getOperationLogFileName(cncIP, cncModel);
                        String logContPre = lineName + LogUtils.separator + robotIP + "->" + cncIP + LogUtils.separator;
                        // FiveAspect will finish at the moment of marco = 10
                        Thread fiveAspectMonitorthr = new Thread(new FiveAspectProcessMonitorThread(cncIP, cncCtrl, taskID, logContPre, logFile));
                        fiveAspectMonitorthr.setName("FiveAspectMonitor" + taskID);
                        if (DebugUtils.isFiveAspectProcess()) {
                            fiveAspectMonitorthr.start();
                        }


//						thr.join();
                    } else {
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    error = e.getMessage();
                    if (null != error) LogUtils.errorLog("TaskProcessor ERR:" + error);
                }
            }
        }
    }

    class AlarmMonitorThread implements Runnable {
        private String taskID;

        public AlarmMonitorThread(String taskID) {
            this.taskID = taskID;
        }

        @Override
        public void run() {
            TaskData taskData = TaskData.getInstance();
            while (true) {
                if (taskData.getTaskState(taskID) == DeviceState.ALARMING) {
                    errorInform();
                    break;
                }
            }
        }
    }

    //This thread will monitor cnc status and variable 850 to check whether cnc need to rotate 90 degree
    class FiveAspectProcessMonitorThread implements Runnable {
        CNC cncCtrl = null;
        String cncIP = null;
        String taskID = null;
        String logFile = null;
        String logContPre = null;

        @Override
        public void run() {
            // get CNC status from database
            if (DebugUtils.isFiveAspectProcess()) {
                TaskData taskData = TaskData.getInstance();
                RunningMsg.set("Fixture rotate 90 degree begin");
                Boolean result = cncCtrl.hfFiveAspectProcess(cncIP);
                if (result == false) {
                    RunningMsg.set("Fixture rotate 90 degree failed,please check as soon as possibly");
                    taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.FIVE_ASPECT_PROCESS_ROTATE.getErrDesc(), false);
                    LogUtils.operationLog(logFile, logContPre + "fail to rotate fixture error，more detail please refer FiveAspectMachining.log");
                } else {
                    LogUtils.operationLog(logFile, logContPre + taskID + " : Rotate fixtures to 90 degree successfully");
                    cncCtrl.startMachining(cncIP, "");
                }
            }
        }

        public FiveAspectProcessMonitorThread(String cncip, CNC cncCTRL, String taskid, String logContPre1, String logFile1) {
            cncIP = cncip;
            cncCtrl = cncCTRL;
            taskID = taskid;
            logContPre = logContPre1;
            logFile = logFile1;
        }
    }

    /**
     * Handle a specific task in an individual thread
     */
    class TaskHandle implements Runnable {
        private String robotIP;
        private String cncIP;
        private String taskID;

        public TaskHandle(String robotIP, String cncIP, String taskID) {
            this.robotIP = robotIP;
            this.cncIP = cncIP;
            this.taskID = taskID;
        }

        @Override
        public void run() {
            String cncModel, robotModel;
            CNC cncCtrl = null;
            String[] rackSlots = new String[14];      //restore the position of be proceeded fixture
            TaskData taskData = TaskData.getInstance();
            WorkpieceData wpData = WorkpieceData.getInstance();
            CncData cncData = CncData.getInstance();
            RobotData robotData = RobotData.getInstance();
            RackProduct rackP = RackProduct.getInstance();
            CncDriver cncDriver = CncDriver.getInstance();
            RobotDriver robotDriver = RobotDriver.getInstance();
            UploadNCProgram uploadNCProgram = UploadNCProgram.getInstance();
            String lineName = cncData.getLineName(cncIP);
            Map recordInfo = new LinkedHashMap();
            try {
                DeviceState cncState = (DeviceState) taskData.getData(taskID).get(TaskItems.MACHINESTATE);

                if (DeviceState.WORKING == cncData.getCncState(cncIP)) return;
                if (DeviceState.FINISH == cncState) Thread.sleep(3000);//For testing only

                RunningMsg.set("Task Handling Started - " + robotIP + "->" + cncIP);
                String logContPre = lineName + LogUtils.separator + robotIP + "->" + cncIP + LogUtils.separator;
//                String logContent = logContPre + "Task " + taskID + "(" + (DeviceState.STANDBY == cncState ? "Load Materials" : "Unload Materials") + ") started";
                String logContent = logContPre + "Task " + taskID + "(" + (DeviceState.STANDBY == cncState ? "Load Materials" : DeviceState.FINISH == cncState?"Unload Materials":"Load/Unload in one time") + ") started";

                cncModel = (String) cncData.getData(cncIP).get(CncItems.MODEL);
                cncData.setCncState(cncIP, DeviceState.WORKING, true, false, true, false);//
                taskData.setTaskState(taskID, DeviceState.WORKING, true, false, true, null, false);
                String logFile = LogUtils.getOperationLogFileName(cncIP, cncModel);
                LogUtils.operationLog(logFile, logContent);

                robotModel = (String) robotData.getData(robotIP).get(RobotItems.MODEL);
                String cncDrvName = (String) cncDriver.getData(cncModel).get(DriverItems.DRIVER);
                String cncDataHandler = (String) cncDriver.getData(cncModel).get(DriverItems.DATAHANDLER);
                String rackIDs = (String) taskData.getTaskByID(taskID).get(TaskItems.RACKIDS);

                String slotIDs = (String) taskData.getTaskByID(taskID).get(TaskItems.SLOTIDS);          //workpiece slot in input rack

                String wkpIDs = (String) taskData.getTaskByID(taskID).get(TaskItems.MATERIALIDS);       //workpiece ID

                String robotDrvName = (String) robotDriver.getData(robotModel).get(DriverItems.DRIVER);

                //rackP is for finish-machining workpieces
                String[] pRackIDs = rackP.getRackIDsByLineName(lineName);
                LogUtils.operationLog(logFile, logContPre + "load basic setting done");

                cncCtrl = CncFactory.getInstance(cncDrvName, cncDataHandler, cncModel);
                cncData.setData(cncIP, CncItems.CTRL, cncCtrl);
                IRobot robotCtrl = IRobotFactory.getInstance(robotDrvName);

                LogUtils.operationLog(logFile, logContPre + "load device driver done");

                LinkedHashMap<String, String> slotInfo = new LinkedHashMap<String, String>();      //record slot of pick from rack and put onto rack
                LinkedHashMap<String, String> wpInfo = new LinkedHashMap<String, String>();

                if (DeviceState.FINISH != cncState) {//CNC is standby/(prepare finish) and waiting for materials
                    RunningMsg.set("Getting material from rack");
                    String[] rIDs = rackIDs.split(";");
                    String[] sIDs = slotIDs.split(";");     //workpiece slot in input rack
                    String[] wpIDs = wkpIDs.split(";");

                    //Split material info by Rack

                    for (int i = 0; i < rIDs.length; i++) {
                        String s = slotInfo.get(rIDs[i]);
                        String wp = wpInfo.get(rIDs[i]);
                        if (null == s) {
                            slotInfo.put(rIDs[i], sIDs[i]);
                            wpInfo.put(rIDs[i], wpIDs[i]);
                        } else {
                            slotInfo.put(rIDs[i], s + "," + sIDs[i]);       //slotInfo (rackId,workpiece slot in input rack)      (9,"1,2,3,4,5,6,7,8,9")
                            wpInfo.put(rIDs[i], wp + "," + wpIDs[i]);
                        }
                    }

//					checkRobotBattery(robotCtrl, robotIP,logFile, logContPre);
                    LogUtils.operationLog(logFile, logContPre + "Robot gets materials from rack started");
                    for (String rackID : slotInfo.keySet()) {
                        RunningMsg.set("Robot " + robotModel + "/" + robotIP + " moves to Rack " + rackID);
                        // use dock to charger of IRobot 2021.11.3 Hui Zhi
                        //if(!robotCtrl.moveToRack(robotIP, rackID, cncModel)){
                        //FIXME Need to get the rack position from database 2022/5/18 Hui Zhi
                        //if(!robotCtrl.dockTo(robotIP, robotData.getChargerPosition(robotIP))){

//-------------------------------------------------------------------clean the cnc program
//                        if(DeviceState.STANDBY == cncState) {                     //Attention : Uploading program in load/unload action may be caused conflict.Because uploading  program is not permission in processing time.
                        if(DeviceState.STANDBY == cncState) {
                            for (int i = 1; i <= 14; i++) {
                                String fileName = "O";
                                DecimalFormat decimalFormat = new DecimalFormat("00");
                                String numFormat = decimalFormat.format(i);
                                fileName += numFormat + "01";   //OXX01

                                Thread uploadBlankThread = new Thread(new Runnable() {
                                    String cncIP;
                                    String name;
                                    CNC cncCtrl;

                                    public Runnable setParam(CNC cnc, String param2, String param3) {
                                        cncCtrl = cnc;
                                        name = param2;
                                        cncIP = param3;
                                        return this;
                                    }

                                    @Override
                                    public void run() {
                                        RunningMsg.set("Blank nc program " + name + " is uploading");
                                        Boolean uploadSuccess = false;
                                        for (int i = 0; i < 10; i++) {
                                            if (cncCtrl.uploadSubProgram(cncIP, name, (String) SystemConfig.getInstance().getCommonCfg().get("LocalFolderURL") + "blank.nc")) {
                                                RunningMsg.set("Upload " + name + " blank nc program successfully!");
                                                LogUtils.operationLog(logFile, logContPre + "Upload blank nc program " + name + " successfully.");
                                                uploadSuccess = true;
                                                break;
                                            }
                                        }
                                        if (!uploadSuccess) {
                                            CncData cncData = CncData.getInstance();
                                            cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
                                            taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_NCPROGRAM.getErrDesc(), false);
                                            RunningMsg.set("Upload " + name + " blank nc program failed!");
                                            LogUtils.operationLog(logFile, logContPre + "Robot upload" + name + " blank program failed.");
                                        }
                                    }
                                }.setParam(cncCtrl, fileName, cncIP));
                                uploadBlankThread.setName("UploadBlankProgramThread_" + i);
                                uploadBlankThread.start();
                            }
                        }
//--------------------------------------------------------------


                        if (!robotCtrl.dockTo(robotIP, "virtual_cnc_1")) {
                            robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                            //Unlock scheduled machine and workpieces
                            cncData.setCncState(cncIP, DeviceState.STANDBY, false, false, true, false);
                            resetMaterialState(wpIDs);

                            taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_RACK.getErrDesc(), false);
                            LogUtils.operationLog(logFile, logContPre + "Robot moves to rack " + rackID + " failed");
                            return;
                        }
                        robotData.setRobotState(robotIP, DeviceState.WORKING, true, false, true, false);
                        LogUtils.operationLog(logFile, logContPre + "Robot moves to rack " + rackID + " done");
                        String[] slotOfgetFromRack = slotInfo.get(rackID).split(",");

                        cncData.setData(cncIP, CncItems.RECORD_FIXTURE_IN_RACK_SLOTID, slotOfgetFromRack);


//						workpiecesProcessingCircle(wpInfo.get(rackID).split(","));


                        if (!getMaterialFromRack(robotCtrl, robotIP, rackID, slotOfgetFromRack, wpInfo.get(rackID).split(","), logFile, logContPre, cncModel, cncCtrl, cncIP,recordInfo,cncState)) {
                            robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                            //Unlock scheduled machine and workpieces
                            cncData.setCncState(cncIP, DeviceState.STANDBY, false, false, true, false);
                            checkMaterialOfRobot(taskID, logFile, logContPre);
                            taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_PICKMTFROM_RACK.getErrDesc(), false);
                            return;
                        }
                    }
                    LogUtils.operationLog(logFile, logContPre + "Robot gets materials from rack done");
                }                    checkMaterialOfRobot(taskID, logFile, logContPre);



                if (cncState == DeviceState.FINISH){
//                    robotCtrl.gotoCharge(robotIP,true);
                    DeviceState currentState = cncCtrl.getMachineState(cncIP);
                    double val = cncCtrl.getMacro(cncIP,900);  //if val = 500 mean finish process
                    while (currentState == DeviceState.WORKING)
                    {
                        LogUtils.operationLog(logFile, logContPre + "wait for finishing machine, now state is " + currentState);
                        System.out.println("wait for finishing machine, now state is " + currentState);
                        currentState = cncCtrl.getMachineState(cncIP);
                        val = cncCtrl.getMacro(cncIP,900);
                        Thread.sleep(6000);
                    }
                }

//				checkRobotBattery(robotCtrl, robotIP,logFile, logContPre);
                RunningMsg.set("Robot moves to " + cncIP);
                LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVETOMACHINE starts");

                if(!robotCtrl.moveBackward(robotIP))
                {
                    robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                    taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_MACHINE.getErrDesc(), false);
                    LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVEBACKWARD failed");
                    return;
                }

                if (runConcurrentOP(DeviceOP.ROBOT_MOVETOMACHINE, cncIP, robotIP, null, cncCtrl, robotCtrl, 1, 360)) {//change 30 second to 60 by Hui Zhi  2022/5/18
                    LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVETOMACHINE done");
                } else {
                    robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                    taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_MACHINE.getErrDesc(), false);
                    LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVETOMACHINE failed");
                    return;
                }
                robotData.setRobotState(robotIP, DeviceState.WORKING, true, false, true, false);
// 2024.4.16
//                RunningMsg.set("CNC door opening");
//                if (!cncCtrl.+(cncIP)) {
//                    cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
//                    taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_OPEN_DOOR.getErrDesc(), false);
//                    if (cncState == DeviceState.FINISH) {
//                        //Unlock the robot
//                        robotData.setRobotState(robotIP, DeviceState.STANDBY, false, false, true, false);
//                    }
//                    RunningMsg.set(cncModel + "(" + cncIP + ") fail to open door");
//                    LogUtils.operationLog(logFile, logContPre + "Machine opens door failed");
//                    return;
//                }
//                LogUtils.operationLog(logFile, logContPre + "Machine opens door done");
                RunningMsg.set("CNC door is opening");

                cncCtrl.startMachining(cncIP, "");       // Open the door.   Opening the door action is wrote in nc program
                LogUtils.operationLog(logFile, logContPre + "CNC door is opening");

                //Refresh machining information - to trace workpiece's status
                if (DeviceState.FINISH != cncState && !"".equals(wkpIDs)) {
                    String[] ids = wkpIDs.split(";");
                    long simulationT = 0;
                    for (int i = 0; i < ids.length; i++) {
                        LinkedHashMap<SpecItems, Object> spec = wpData.getNextProcInfo(ids[i]);
                        int curProc = wpData.getNextProcNo(ids[i], spec);
                        int procTime = wpData.getNextProcSimtime(ids[i], cncModel, spec, curProc);
                        simulationT += procTime;

                        wpData.setCurrentProcNo(ids[i], curProc);
                        wpData.appendData(ids[i], WorkpieceItems.NCMODEL, cncModel);
                        wpData.appendData(ids[i], WorkpieceItems.MACHINETIME, "" + procTime);
                        wpData.appendData(ids[i], WorkpieceItems.PROCESS, wpData.getNextProcName(ids[i], spec, curProc));
                        wpData.appendData(ids[i], WorkpieceItems.PROGRAM, wpData.getNextProcProgram(ids[i], cncModel, spec, curProc));
                        wpData.appendData(ids[i], WorkpieceItems.SURFACE, "" + wpData.getNextProcSurface(ids[i], spec, curProc));

                        cncData.setExpectedMachiningTime(cncIP, simulationT);
                    }
                }

                if (cncState == DeviceState.FINISH || cncCtrl.getMacro(cncIP,900) == Double.valueOf(500)) {     // 900 var = 500 mean finish process

                    //cnc machining finish
                    Boolean doAction = false;
                    DAO dao = new DAOImpl("com.cncmes.dto.TaskInfo");
                    TaskInfo taskInfo = new TaskInfo();
                    try {
                        List<Object> os = dao.findByCnd(new String[]{"cnc_ip", "have_done"}, new String[]{cncIP, "0"});
                        if(os.size()>0)
                        {
                            for(Object o : os){
                                taskInfo = (TaskInfo)o;
                            }
                            com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(taskInfo.getTask_info());
                            doAction = taskInfo.getTask_info()==null || taskInfo.getTask_info().equals("");     //
                        }
                    } catch (Exception throwables) {
                        throwables.printStackTrace();
                    }


                    LinkedHashMap<Integer, String> wpIDs = cncData.getWorkpieceIDs(cncIP);
                    if (wpIDs.isEmpty()) {
                        RunningMsg.set("Robot fails to unload materials from " + cncIP + "(no materials)");
                        cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);

                        //Unlock the robot
                        robotData.setRobotState(robotIP, DeviceState.STANDBY, false, false, true, false);
                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_PICKMTFROM_MACHINE.getErrDesc(), false);
                        LogUtils.operationLog(logFile, logContPre + "Robot unloads materials from machine failed cause of no materials");
                        return;
                    }

                    RunningMsg.set("Robot unloads material from Machine " + cncIP);
                    String workZones = "", wpID = "", ids = "";
                    for (int workZone : wpIDs.keySet()) {
                        wpID = wpIDs.get(workZone);
                        RunningMsg.set("Machine release fixture " + workZone);

                        LogUtils.operationLog(logFile, logContPre + "Machine releases fixture " + workZone + "/" + wpID + " done");

                        if ("".equals(workZones)) {
                            workZones = "" + workZone;
                            ids = wpID;
                        } else {
                            workZones += "," + workZone;
                            ids += "," + wpID;
                        }
                    }
                    //Check whether the Robot has moved to target CNC or not
//                    if(doAction) {       //load and unload together

                        LogUtils.operationLog(logFile, logContPre + "Robot moving to machine status checking");
                        if (!concurrentOPDone(cncIP, robotIP, DeviceOP.ROBOT_MOVETOMACHINE, CONCURR_OP_TIMEOUT)) {

                            robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                            taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_MACHINE.getErrDesc(), false);
                            LogUtils.operationLog(logFile, logContPre + "Robot moves to machine failed");
                            return;
                        }
                        LogUtils.operationLog(logFile, logContPre + "Machine opens door done");
                        LogUtils.operationLog(logFile, logContPre + "Robot moves to machine done");
//                    }
                    //Robot unloads materials from CNC
                    for(int i = 0;i <= 30;i++)
                    {
                        if(cncCtrl.checkSensor(cncIP, "3304", "1") == false)
                        {
                            Thread.sleep(3000);
                        }
                    }
                    if (cncCtrl.checkSensor(cncIP, "3304", "1"))     //detect whether the door is opened
                    {
                        LogUtils.operationLog(logFile, logContPre + "cnc open door done successfully");
                        boolean bOK = unloadMaterialFromMachine(robotCtrl, robotIP, cncCtrl, cncIP, workZones.split(","), ids.split(","), logFile, logContPre);
                        if (!bOK) {
                            RunningMsg.set("Robot fails to unload materials from " + cncIP);
                            robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                            cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
                            taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_PICKMTFROM_MACHINE.getErrDesc(), false);
                            LogUtils.operationLog(logFile, logContPre + "Robot unloads materials from machine failed");
                            return;
                        }
                        cncData.setData(cncIP, CncItems.DT_FINISHUNLOADING, System.currentTimeMillis());
                        cncData.setData(cncIP, CncItems.FINISHSHOW, "");
                        cncData.clearWorkpieceID(cncIP);//Clear the workpieces in CNC
                        LogUtils.operationLog(logFile, logContPre + "Robot unloads materials from machine done");
                    } else
                        {        //open door fail
                        if(!doAction)
                        {
                            robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                            cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
                            taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_OPEN_DOOR.getErrDesc(), false);

                            cncData.setData(cncIP, CncItems.DT_FINISHUNLOADING, System.currentTimeMillis());
                            cncData.setData(cncIP, CncItems.FINISHSHOW, "");
                            cncData.clearWorkpieceID(cncIP);//Clear the workpieces in CNC
                            LogUtils.operationLog(logFile, logContPre + "Machine's status is FINISH ,but CNC door open fail");

                            if (!DebugUtils.isMassProduction()) // monitor system database
                            {
                                DAOImpl processDetailDao = new DAOImpl("com.cncmes.dto.CncProcessDetail");
                                try {
                                    List<Object> dr = processDetailDao.getCNCProcessDetail(cncIP);
                                    if (dr != null && dr.size() != 0) {
                                        CncProcessDetail processDetail = (CncProcessDetail) dr.get(0);
                                        processDetail.setHave_finish(1);
                                        processDetailDao.monitorDBupdate(processDetail);
                                    }
                                } catch (SQLException throwables) {
                                    throwables.printStackTrace();
                                }
                            }
                        }else {
                            robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                            cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
                            taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_OPEN_DOOR.getErrDesc(), false);
                            if (cncState == DeviceState.FINISH) {
                                //Unlock the robot
                                robotData.setRobotState(robotIP, DeviceState.STANDBY, false, false, true, false);
                            }
                            RunningMsg.set(cncModel + "(" + cncIP + ") fail to open door");
                            LogUtils.operationLog(logFile, logContPre + "Machine opens door failed");
                            return;
                        }
                    }


                    //Robot moves to Rack
//					checkRobotBattery(robotCtrl, robotIP,logFile, logContPre);


                    if(!robotCtrl.moveBackward(robotIP))
                    {
                        robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_RACK.getErrDesc(), false);
                        LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVEBACKWARD failed");
                        return;
                    }

                    RunningMsg.set("Robot moves to Rack " + pRackIDs[0]);
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("rackID", pRackIDs[0]);
                    LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVETO_OUTPUTRACK starts");
                    if (runConcurrentOP(DeviceOP.ROBOT_MOVETORACK, cncIP, robotIP, jsonObj, cncCtrl, robotCtrl, 1, 60)) {//change 30 second to 60 by Hui Zhi  2022/5/18
                        LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVETO_OUTPUTRACK done");
                    } else {
                        robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_RACK.getErrDesc(), false);
                        LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVETO_OUTPUTRACK failed");
                        return;
                    }


                    //Check whether Robot has moved to target Rack or not
//					checkRobotBattery(robotCtrl, robotIP,logFile, logContPre);

//add at 2023.10.10				add flush program after unload fixture from cnc----------------------------------------------------------
                    //disable 2024.4.16
//                    RunningMsg.set("CNC door closing");
//                    if (!cncCtrl.hfCloseDoor(cncIP)) {
//                        cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
//                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_CLOSE_DOOR.getErrDesc(), false);
//                        RunningMsg.set("CNC fails to close door");
//                        LogUtils.operationLog(logFile, logContPre + "Machine fails to close door");
//                        return;
//                    }
//                    LogUtils.operationLog(logFile, logContPre + "Machine closes door done");


//					needToFlushCnc(cncCtrl,cncIP);

//----------------------------------------------------------------------------------------------------------------------------------------


                    LogUtils.operationLog(logFile, logContPre + "Robot moving to OutputRack status checking");
                    if (!concurrentOPDone(cncIP, robotIP, DeviceOP.ROBOT_MOVETORACK, CONCURR_OP_TIMEOUT)) {

                        robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_RACK.getErrDesc(), false);
                        LogUtils.operationLog(logFile, logContPre + "Robot moves to Rack " + pRackIDs[0] + " failed");
                        return;
                    }
                    LogUtils.operationLog(logFile, logContPre + "Robot moves to Rack " + pRackIDs[0] + " done");

                    RunningMsg.set("Robot puts materials onto Rack " + pRackIDs[0]);
                    rackSlots = (String[]) cncData.getData(cncIP).get(CncItems.RECORD_FIXTURE_IN_RACK_SLOTID);          //put fixture onto the slot where the fixture picked from

                    if (!putMaterialOntoRack(robotCtrl, robotIP, pRackIDs[0], rackSlots, logFile, logContPre, cncModel)) {//     rack:null
                        robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_PUTMTONTO_RACK.getErrDesc(), false);
                        return;
                    }
                    LogUtils.operationLog(logFile, logContPre + "Robot puts materials onto Rack " + pRackIDs[0] + " done");

                    //TODO For testing stage only	//disabled by Hui Zhi 2021/23/28
//					LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT_PNPTESTING started");
//					if(runConcurrentOP(DeviceOP.ROBOT_PNPTESTING, cncIP, robotIP, jsonObj, cncCtrl, robotCtrl, 1, 30)){
//						LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT_PNPTESTING done");
//					}else{
//						LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT_PNPTESTING failed");
//					}

                    //disabled by Hui Zhi 2021/23/28
                    //Check whether CNC cleaning is done or not
//					RunningMsg.set("Check CNC 2nd cleaning status");
//					LogUtils.operationLog(logFile, logContPre+"CNC 2nd cleaning status checking");
//					if(!concurrentOPDone(cncIP,robotIP,DeviceOP.CNC_CLEANING,2000)){
//						cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
//						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_PICKMTFROM_MACHINE.getErrDesc(), false);
//						LogUtils.operationLog(logFile, logContPre+"CNC 2nd cleaning timeout");
//						return;
//					}
//					cncData.setCNCTimingData(cncIP, CncItems.DT_CLEANING_STOP2, System.currentTimeMillis());
//					robotData.setData(robotIP, RobotItems.OP_STOPPNPTESTING, "1");
//					LogUtils.operationLog(logFile, logContPre+"CNC 2nd cleaning done");

//					RunningMsg.set("CNC door opening");
//					if(!cncCtrl.openDoor(cncIP)){
//						cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
//						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_PICKMTFROM_MACHINE.getErrDesc(), false);
//						LogUtils.operationLog(logFile, logContPre+"CNC door opening failed after 2nd cleaning");
//						return;
//					}
//					LogUtils.operationLog(logFile, logContPre+"CNC door opening done");


                    //Unlock CNC
                    cncData.setCncState(cncIP, DeviceState.STANDBY, true, false, true, false);
                    LogUtils.operationLog(logFile, logContPre + "Unlock machine done");
//
//                    //Pop finished task
                    cncData.saveMachiningData(cncIP);
                    taskData.taskPopByID(taskID, true);
//
////					in order to make robot and cnc working 24 hour make a working circle
////					set workpiece status standby and inform scheduler
//
//
//                    //Unlock Robot

                    if(!robotCtrl.moveBackward(robotIP))
                    {
                        robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_RACK.getErrDesc(), false);
                        LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVEBACKWARD failed");
                        return;
                    }

                    robotData.setRobotState(robotIP, DeviceState.STANDBY, true, false, true, false);
                    LogUtils.operationLog(logFile, logContPre + "Unlock robot done");
                    if (DebugUtils.cycleWorking) {
                        workpiecesProcessingCircle(ids.split(","));                 //make mes system circle run if you not need this function just delete this line
                    }
                } else if(cncState == DeviceState.STANDBY) {

                    Double simulateTime = (Double) recordInfo.get("simulate_time");
                    com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
                    TaskInfo taskInfo = new TaskInfo(taskID,jsonObject.toJSONString(),cncIP,0,simulateTime);
            		DAO dao = new DAOImpl("com.cncmes.dto.TaskInfo");
                    try {
                        dao.add(taskInfo);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }

                    //Check whether Robot has moved to target CNC or not
                    LogUtils.operationLog(logFile, logContPre + "Robot moving to machine status checking");
                    if (!concurrentOPDone(cncIP, robotIP, DeviceOP.ROBOT_MOVETOMACHINE, CONCURR_OP_TIMEOUT)) {

                        robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_MACHINE.getErrDesc(), false);
                        LogUtils.operationLog(logFile, logContPre + "Robot moves to machine failed");
                        return;
                    }
                    LogUtils.operationLog(logFile, logContPre + "Robot moves to machine done");

                    RunningMsg.set("Robot loads material onto " + cncIP);
                    if (cncCtrl.checkSensor(cncIP, "3304", "1"))        //detect whether the door is opened
                    {
                        if (!loadMaterialOntoMachine(robotCtrl, robotIP, cncCtrl, cncIP, logFile, logContPre)) {
                            robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                            taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_PUTMTONTO_MACHINE.getErrDesc(), false);
                            RunningMsg.set("Robot fails to load materials onto " + cncIP);
                            return;
                        }
                        LogUtils.operationLog(logFile, logContPre + "Robot loads materials onto machine done");
                    }else {
                        cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_OPEN_DOOR.getErrDesc(), false);
                        if (cncState == DeviceState.FINISH) {
                            //Unlock the robot
                            robotData.setRobotState(robotIP, DeviceState.STANDBY, false, false, true, false);
                        }
                        RunningMsg.set(cncModel + "(" + cncIP + ") fail to open door");
                        LogUtils.operationLog(logFile, logContPre + "Machine opens door failed");
                        return;
                    }



                    // Create sub programs uploading thread
                    // disable upload program for trial run 2023/6/30
//					// uploadNCProgram.uploadSubProgramsThread(cncCtrl, cncIP);

                    // Unlock the Robot


//					robotData.setRobotState(robotIP, DeviceState.STANDBY, true, false, true, false);
//					LogUtils.operationLog(logFile, logContPre+"Unlock robot done");

//					//TODO Robot moves to rack for debug stage only
//					RunningMsg.set("Robot moves to Rack "+pRackIDs[0]);
////					RunningMsg.set("Robot moves to charge station");
//					JSONObject jsonObj = new JSONObject();
//					jsonObj.put("rackID", pRackIDs[0]);
//					LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT_MOVETORACK starts");
////					LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT START TO GO CHARGE STATION");
//					if(runConcurrentOP(DeviceOP.ROBOT_MOVETORACK, cncIP, robotIP, jsonObj, cncCtrl, robotCtrl, 1, 60))
////					if(robotCtrl.gotoCharge(robotIP,true))
//					{//change 30 second to 60 by Hui Zhi  2022/5/18
//						LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT_MOVETORACK "+pRackIDs[0]+" done");
////						LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT BEGIN TO CHARGE");
//					}else{
//						robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
//						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_RACK.getErrDesc(), false);
////						taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_GOTO_CHARGE.getErrDesc(), false);
//						LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT_MOVETORACK "+pRackIDs[0]+" failed");
////						LogUtils.operationLog(logFile, logContPre+"ConcurrentOP ROBOT_GOTO CHARGE FAILED");
//						return;
//					}

                    robotData.setRobotState(robotIP, DeviceState.STANDBY, true, false, true, false);
                    LogUtils.operationLog(logFile, logContPre + "Unlock robot done");

                    //TODO Robot moves to rack for debug stage only
                    RunningMsg.set("Robot moves to Rack " + pRackIDs[0]);       //the action After load to cnc then run to rack
                    JSONObject jsonObj = new JSONObject();
//					jsonObj.put("rackID", pRackIDs[0]);
                    jsonObj.put("rackID", "9");
                    LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVETORACK starts");

                    if(!robotCtrl.moveBackward(robotIP))
                    {
                        robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_RACK.getErrDesc(), false);
                        LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVEBACKWARD failed");
                        return;
                    }

                    if (runConcurrentOP(DeviceOP.ROBOT_MOVETORACK, cncIP, robotIP, jsonObj, cncCtrl, robotCtrl, 1, 60)) {//change 30 second to 60 by Hui Zhi  2022/5/18
                        LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVETORACK " + "9" + " done");
                    } else {
                        robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_RACK.getErrDesc(), false);
                        LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVETORACK " + "9" + " failed");
                        return;
                    }

                    //Clamp all fixtures
                    LinkedHashMap<Integer, String> wpIDs = cncData.getWorkpieceIDs(cncIP);
                    String workpieceIDs = "";
                    RunningMsg.set("Machine " + cncIP + " clamps fixtures");
                    //disable at 2024.4.17
//                    for (int wkZone : wpIDs.keySet()) {
//                        if ("".equals(workpieceIDs)) {
//                            workpieceIDs = wpIDs.get(wkZone);
//                        } else {
//                            workpieceIDs += ";" + wpIDs.get(wkZone);
//                        }
//
//                        if (!cncCtrl.hfClampFixture(cncIP, wkZone)) {
//
//                            cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
//                            taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_CLAMP_FIXTURE.getErrDesc(), false);
//                            RunningMsg.set("CNC fails to clamp fixtures");
//                            LogUtils.operationLog(logFile, logContPre + "Machine clamps fixture " + wkZone + "/" + wpIDs.get(wkZone) + " failed");
//                            return;
//                        } else {
//                            wpData.setWorkpieceState(wpIDs.get(wkZone), DeviceState.WORKING, false, true, false, true, false);
//                            //Update Rack Manager with workpiece WORKING state
//                            RackClient.getInstance().updateRackInfo(lineName, pRackIDs[0], wpIDs.get(wkZone), true, false, true);
//                            LogUtils.operationLog(logFile, logContPre + "Machine clamps fixture " + wkZone + "/" + wpIDs.get(wkZone) + " done");
//                        }
//                    }
//                    LogUtils.operationLog(logFile, logContPre + "Machine clamps fixtures done");


//

//                    2024.1.16
//                    RunningMsg.set("CNC door closing");
//                    if (!cncCtrl.hfCloseDoor(cncIP)) {
//
//                        cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
//                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_CLOSE_DOOR.getErrDesc(), false);
//                        RunningMsg.set("CNC fails to close door");
//                        LogUtils.operationLog(logFile, logContPre + "Machine fails to close door");
//                        return;
//                    }
//                    LogUtils.operationLog(logFile, logContPre + "Machine closes door done");

                    RunningMsg.set("CNC starts machining");
                    LinkedHashMap<CncWebAPIItems, String> cncWebAPICommonCfg = cncWebAPI.getCommonCfg(cncModel);
                    String mainProgram = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGMAIN);

                    if (!cncCtrl.startMachinePrepared(cncIP)) {
                        cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_START_MACHINING.getErrDesc(), false);
                        RunningMsg.set("CNC fail to do machining");
                        LogUtils.operationLog(logFile, logContPre + "Prepared start 900 '1' failed");
                        return;
                    }

//					if(!startMachining(cncCtrl,cncIP,mainProgram,3,10)){
                    if (!cncCtrl.startMachining(cncIP, "")) {     //close door and machining
                        cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_START_MACHINING.getErrDesc(), false);
                        RunningMsg.set("CNC fail to do machining");
                        LogUtils.operationLog(logFile, logContPre + "Machine starts machining failed");
                        return;
                    } else {
                        Boolean closeDoor = false;
                        for (int i = 0; i < 30; i++)        //15s detect whether the door is closed
                        {
                            if (cncCtrl.checkSensor(cncIP, "3305", "1")) {
                                LogUtils.operationLog(logFile, logContPre + "Machine close door successfully");
                                LogUtils.operationLog(logFile, logContPre + "Machine starts machining");
                                cncData.setCncLastState(cncIP, DeviceState.LOCK);
                                closeDoor = true;
                                break;
                            }
                            Thread.sleep(3000);
                        }
                        if (!closeDoor) {
                            cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
                            taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_CLOSE_DOOR.getErrDesc(), false);
                            RunningMsg.set("CNC fails to close door");
                            LogUtils.operationLog(logFile, logContPre + "Machine fails to close door");
                            return;
                        }

                    }
                }else if(cncState == DeviceState.PREPAREFINISH)
                {
                    System.out.println("Entry prepare finish stage");
                    while (cncCtrl.getMachineState(cncIP) != DeviceState.STANDBY)
                    {
                        Thread.sleep(3000);
                        System.out.println("wait for finishing machine");
                    }

                    for (int i = 1; i <= 14; i++) {
                        String fileName = "O";
                        DecimalFormat decimalFormat = new DecimalFormat("00");
                        String numFormat = decimalFormat.format(i);
                        fileName += numFormat + "01";   //OXX01

                        Thread uploadBlankThread = new Thread(new Runnable() {
                            String cncIP;
                            String name;
                            CNC cncCtrl;

                            public Runnable setParam(CNC cnc, String param2, String param3) {
                                cncCtrl = cnc;
                                name = param2;
                                cncIP = param3;
                                return this;
                            }

                            @Override
                            public void run() {
                                RunningMsg.set("Blank nc program " + name + " is uploading");
                                Boolean uploadSuccess = false;
                                for (int i = 0; i < 10; i++) {
                                    if (cncCtrl.uploadSubProgram(cncIP, name, (String) SystemConfig.getInstance().getCommonCfg().get("LocalFolderURL") + "blank.nc")) {
                                        RunningMsg.set("Upload " + name + " blank nc program successfully!");
                                        LogUtils.operationLog(logFile, logContPre + "Upload blank nc program " + name + " successfully.");
                                        uploadSuccess = true;
                                        break;
                                    }
                                }
                                if (!uploadSuccess) {
                                    CncData cncData = CncData.getInstance();
                                    cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
                                    taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_NCPROGRAM.getErrDesc(), false);
                                    RunningMsg.set("Upload " + name + " blank nc program failed!");
                                    LogUtils.operationLog(logFile, logContPre + "Robot upload" + name + " blank program failed.");
                                }
                            }
                        }.setParam(cncCtrl, fileName, cncIP));
                        uploadBlankThread.setName("UploadBlankProgramThread_" + i);
                        uploadBlankThread.start();
                    }
                    Thread.sleep(2000);
                    //upload barcode program
                    LinkedHashMap barcodeInfo = (LinkedHashMap) recordInfo.get("barcodeInfo");      //<barcode,program name>
                    Set<String> barcodes = barcodeInfo.keySet();
                    for(String barcode : barcodes)
                    {
                        String uploadName = (String) barcodeInfo.get(barcode);
                        Thread uploadBarcodeThread = new Thread(new Runnable() {
                            String cncIP;
                            String name;
                            String barcode;
                            CNC cncCtrl;

                            public Runnable setParam(CNC cnc, String param2, String param3,String param4) {
                                cncCtrl = cnc;
                                name = param2;
                                cncIP = param3;
                                barcode = param4;
                                return this;
                            }
                            @Override
                            public void run() {
                                RunningMsg.set("NC program " + uploadName + " is uploading");
                                Boolean uploadSuccess = false;
                                for (int i = 0; i < 10; i++) {                                                  //prevent from upload conflict
                                    if (cncCtrl.getAndUploadProgramByBarcode(cncIP, barcode, uploadName)) {     //programName should be named {#uploadName }
                                        LogUtils.operationLog(logFile, logContPre + "Upload nc program " + uploadName + " successfully");
                                        uploadSuccess = true;
                                        break;
                                    }
                                }
                                if (!uploadSuccess) {
//                                        cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);          //20240418 disable if upload fail would not stop system
                                    RunningMsg.set("Upload " + uploadName + " nc program failed!");
                                    LogUtils.operationLog(logFile, logContPre + "Robot upload program failed.");
                                }
                            }
                        }.setParam(cncCtrl,uploadName,cncIP,barcode));
                        uploadBarcodeThread.setName("UploadBlankProgramThread_" + barcode);
                        uploadBarcodeThread.start();
                    }


                    String[] unloadWorkZone = null;
                    String[] unloadWorkPieceID = null;
                    LogUtils.operationLog(logFile, logContPre + "Robot moving to machine status checking");
                    if (!concurrentOPDone(cncIP, robotIP, DeviceOP.ROBOT_MOVETOMACHINE, CONCURR_OP_TIMEOUT)) {
                        robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_MACHINE.getErrDesc(), false);
                        LogUtils.operationLog(logFile, logContPre + "Robot moves to machine failed");
                        return;
                    }
//                    cncCtrl.startMachining(cncIP,"");
                    boolean oppenDoor = false;
                    for(int i = 0;i<30;i++ )
                    {
                        if (cncCtrl.checkSensor(cncIP, "3304", "1"))
                        {
                            oppenDoor = true;
                            break;
                        }else {
                            Thread.sleep(3000);
                        }
                    }
                    //wait for door opening
                    LogUtils.operationLog(logFile, logContPre + "Robot moves to machine done");
                    RunningMsg.set("Robot load/unload material onto " + cncIP);
                    if (oppenDoor)        //detect whether the door is opened
                    {
                        DAO dao = new DAOImpl("com.cncmes.dto.TaskInfo");
                        TaskInfo taskInfo = new TaskInfo();
                        try {
                            List<Object> os = dao.findByCnd(new String[]{"cnc_ip", "have_done"}, new String[]{cncIP, "0"});
                            for(Object o : os){
                                taskInfo = (TaskInfo)o;
                            }
                            taskInfo.setHave_done(1);
                            dao.update(taskInfo);
                            com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(taskInfo.getTask_info());
                            unloadWorkZone = jsonObject.getString("unload_workzone").split(";");
                            unloadWorkPieceID = jsonObject.getString("unload_workpieceID").split(";");
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                        Double simulateTime = (Double) recordInfo.get("simulate_time");
                        JSONObject jsonObject = new JSONObject();
                        TaskInfo task = new TaskInfo(taskID,jsonObject.toString(),cncIP,0,simulateTime);    //task{"asdadxcz",{},"10.10.206.178",0,60.00}

                        try {
                            dao.add(task);
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }

                        if(!loadAndUnloadMaterial(robotCtrl,robotIP,cncCtrl,cncIP,unloadWorkZone,unloadWorkPieceID,logFile,logContPre))
                        {
                            robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                            taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_LOAD_UNLOAD_MACHINE.getErrDesc(), false);
                            RunningMsg.set("Robot fails to load/unload materials onto " + cncIP);
                            return;
                        }
                        LogUtils.operationLog(logFile, logContPre + "Robot loads/unloads materials onto machine done");
                    }else {
                        cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_OPEN_DOOR.getErrDesc(), false);
                        if (cncState == DeviceState.FINISH) {
                            //Unlock the robot
                            robotData.setRobotState(robotIP, DeviceState.STANDBY, false, false, true, false);
                        }
                        RunningMsg.set(cncModel + "(" + cncIP + ") fail to open door");
                        LogUtils.operationLog(logFile, logContPre + "Machine opens door failed");
                        return;
                    }

                    robotData.setRobotState(robotIP, DeviceState.STANDBY, true, false, true, false);
                    LogUtils.operationLog(logFile, logContPre + "Unlock robot done");
                    //TODO Robot moves to rack for debug stage only
//                    RunningMsg.set("Robot moves to Rack " + pRackIDs[0]);       //the action After load to cnc then run to rack
//                    JSONObject jsonObj = new JSONObject();
//                    jsonObj.put("rackID", "9");
//                    LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVETORACK starts");
//                    if (runConcurrentOP(DeviceOP.ROBOT_MOVETORACK, cncIP, robotIP, jsonObj, cncCtrl, robotCtrl, 1, 60)) {//change 30 second to 60 by Hui Zhi  2022/5/18
//                        LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVETORACK " + "9" + " done");
//                    } else {
//                        robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
//                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_RACK.getErrDesc(), false);
//                        LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVETORACK " + "9" + " failed");
//                        return;
//                    }
                    //set finished workpieces to output rack
                    RunningMsg.set("Robot moves to Rack " + pRackIDs[0]);
                    JSONObject json = new JSONObject();
                    json.put("rackID", pRackIDs[0]);

                    if(!robotCtrl.moveBackward(robotIP))
                    {
                        robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_RACK.getErrDesc(), false);
                        LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVEBACKWARD failed");
                        return;
                    }

                    LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVETORACK starts");
                    if (runConcurrentOP(DeviceOP.ROBOT_MOVETORACK, cncIP, robotIP, json, cncCtrl, robotCtrl, 1, 60)) {//change 30 second to 60 by Hui Zhi  2022/5/18
                        LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVETORACK done");
                    } else {
                        robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_RACK.getErrDesc(), false);
                        LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVETORACK failed");
                        return;
                    }

                    //Clamp all fixtures
                    RunningMsg.set("Machine " + cncIP + " clamps fixtures");

                    RunningMsg.set("CNC starts machining");
                    LinkedHashMap<CncWebAPIItems, String> cncWebAPICommonCfg = cncWebAPI.getCommonCfg(cncModel);
                    String mainProgram = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGMAIN);

                    if (!cncCtrl.startMachinePrepared(cncIP)) {
                        cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_START_MACHINING.getErrDesc(), false);
                        RunningMsg.set("CNC fail to do machining");
                        LogUtils.operationLog(logFile, logContPre + "Prepared start 900 '1' failed");
                        return;
                    }

//					if(!startMachining(cncCtrl,cncIP,mainProgram,3,10)){
                    if (!cncCtrl.startMachining(cncIP, "")) {     //close door and machining
                        cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_START_MACHINING.getErrDesc(), false);
                        RunningMsg.set("CNC fail to do machining");
                        LogUtils.operationLog(logFile, logContPre + "Machine starts machining failed");
                        return;
                    } else {
                        Boolean closeDoor = false;
                        for (int i = 0; i < 30; i++)        //15s detect whether the door is closed
                        {
                            if (cncCtrl.checkSensor(cncIP, "3305", "1")) {
                                LogUtils.operationLog(logFile, logContPre + "Machine starts machining");
                                cncData.setCncLastState(cncIP, DeviceState.LOCK);
                                closeDoor = true;
                                break;
                            }
                            Thread.sleep(3000);
                        }
                        if (!closeDoor) {
                            cncData.setCncState(cncIP, DeviceState.ALARMING, false, false, true, false);
                            taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.MC_CLOSE_DOOR.getErrDesc(), false);
                            RunningMsg.set("CNC fails to close door");
                            LogUtils.operationLog(logFile, logContPre + "Machine fails to close door");
                            return;
                        }
//                        Double simulateTime = (Double) recordInfo.get("simulate_time");
//                        JSONObject jsonObject = new JSONObject();
//                        TaskInfo taskInfo = new TaskInfo(taskID,jsonObject.toString(),cncIP,0,simulateTime);
//                        DAO dao = new DAOImpl("com.cncmes.dto.TaskInfo");
//                        try {
//                            dao.add(taskInfo);
//                        } catch (SQLException throwables) {
//                            throwables.printStackTrace();
//                        }

                    }
                    //set finished workpieces to output rack
//                    RunningMsg.set("Robot moves to Rack " + pRackIDs[0]);
//                    JSONObject jsonOutput = new JSONObject();
//                    jsonOutput.put("rackID", pRackIDs[0]);
//                    LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVETORACK starts");
//                    if (runConcurrentOP(DeviceOP.ROBOT_MOVETORACK, cncIP, robotIP, jsonOutput, cncCtrl, robotCtrl, 1, 60)) {//change 30 second to 60 by Hui Zhi  2022/5/18
//                        LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVETORACK done");
//                    } else {
//                        robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
//                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_RACK.getErrDesc(), false);
//                        LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVETORACK failed");
//                        return;
//                    }
                    LogUtils.operationLog(logFile, logContPre + "Robot moving to Rack status checking");
                    if (!concurrentOPDone(cncIP, robotIP, DeviceOP.ROBOT_MOVETORACK, CONCURR_OP_TIMEOUT)) {

                        robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_MOVETO_RACK.getErrDesc(), false);
                        LogUtils.operationLog(logFile, logContPre + "Robot moves to Rack " + pRackIDs[0] + " failed");
                        return;
                    }
                    LogUtils.operationLog(logFile, logContPre + "Robot moves to Rack " + pRackIDs[0] + " done");
                    RunningMsg.set("Robot puts materials onto Rack " + pRackIDs[0]);
                    rackSlots = (String[]) cncData.getData(cncIP).get(CncItems.RECORD_FIXTURE_IN_RACK_SLOTID);          //put fixture onto the slot where the fixture picked from

                    if (!putMaterialOntoRack(robotCtrl, robotIP, pRackIDs[0], rackSlots, logFile, logContPre, cncModel)) {//     rack:null
                        robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_PUTMTONTO_RACK.getErrDesc(), false);
                        return;
                    }
                    LogUtils.operationLog(logFile, logContPre + "Robot puts materials onto Rack " + pRackIDs[0] + " done");


                    if (runConcurrentOP(DeviceOP.ROBOT_GOTOCHARGE, cncIP, robotIP, json, cncCtrl, robotCtrl, 1, 60)) {
                        LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_GOTOCHARGE done");
                    } else {
                        robotData.setRobotState(robotIP, DeviceState.ALARMING, false, false, true, false);
                        taskData.setTaskState(taskID, DeviceState.ALARMING, false, false, true, ErrorCode.RB_GOTO_CHARGE.getErrDesc(), false);
                        LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_GOTOCHARGE failed");
                        return;
                    }
                }
                if (taskData.taskCount() <= 0) RunningMsg.set("Task done");
            } catch (Exception e) {
                LogUtils.errorLog("TaskProcessor-TaskHandle ERR:" + e.getMessage());
            }
        }
    }
}
