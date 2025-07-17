package com.cncmes.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.cncmes.base.CNC;
import com.cncmes.base.CncItems;
import com.cncmes.base.DeviceState;
import com.cncmes.base.DriverItems;
import com.cncmes.base.SpecItems;
import com.cncmes.base.WorkpieceItems;
import com.cncmes.ctrl.CncFactory;
import com.cncmes.data.CncData;
import com.cncmes.data.CncDriver;
import com.cncmes.data.CncWebAPI;
import com.cncmes.data.WorkpieceData;
import com.cncmes.thread.DeviceMonitor;
import com.cncmes.utils.*;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import java.awt.event.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;

public class CNCConfigDialog extends JDialog {
    private JComboBox<String> comboBoxIP;
    private JComboBox<String> comboBoxCommand;
    private JTextField textField_Info;

    private static final long serialVersionUID = 18L;
    private final JPanel contentPanel = new JPanel();
    private static CNCConfigDialog cncConfigDialog = new CNCConfigDialog();
    private static CncData cncData = null;
    private JTextField txtZone1Barcode;
    private JTextField txtProcessPosition;
    private JTextField txtZone3Barcode;
    private JTextField txtZone4Barcode;
    private JTextField txtZone5Barcode;
    private JTextField txtZone6Barcode;
    private JCheckBox chckbxAutoMode;
    private JProgressBar progressBar;
    private JButton btnLock1;
    private JButton btnConfim;
    private JButton btnLock3;
    private JButton btnLock4;
    private JButton btnLock5;
    private JButton btnLock6;
    private JButton btnUnlock1;
    private JButton btnUnlock2;
    private JButton btnUnlock3;
    private JButton btnUnlock4;
    private JButton btnUnlock5;
    private JButton btnUnlock6;
    private JButton btnOpenDoor;
    private JButton btnCloseDoor;
    private JButton btnUploadNcProgram;
    private JButton btnLock;
    private JButton btnUnlock;
    private JButton btnStartMachining;
    private JButton btnStopMonitoring;
    private JComboBox<String> cncCapacity;

    private String capacity = "1";
    private String[] positions;
    private String cncIP;
    private static boolean[] lockFlags = new boolean[6];
    private static boolean[] ncUploadFlags = new boolean[6];
    private static boolean doorClosedFlag = false;

    public static CNCConfigDialog getInstance() {
        cncData = CncData.getInstance();
        return cncConfigDialog;
    }

    /**
     * Create the dialog.
     */
    private CNCConfigDialog() {
        cncData = CncData.getInstance();
        setModal(true);
        setIconImage(Toolkit.getDefaultToolkit().getImage(CNCController.class.getResource("/com/cncmes/img/Butterfly_24.png")));
        setTitle("CNC Config");
        setResizable(false);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int width = 480;
        int height = 480;
        setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);

        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[]{90, 390};
        gbl_contentPanel.rowHeights = new int[]{120, 210, 80, 20};
        gbl_contentPanel.columnWeights = new double[]{1.0, 1.0};
        gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0};
        contentPanel.setLayout(gbl_contentPanel);
        {
            JPanel panelWorkZones = new JPanel();
            panelWorkZones.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
            GridBagConstraints gbc_panelWorkZones = new GridBagConstraints();
            gbc_panelWorkZones.insets = new Insets(0, 0, 5, 0);
            gbc_panelWorkZones.fill = GridBagConstraints.BOTH;
            gbc_panelWorkZones.gridx = 1;
            gbc_panelWorkZones.gridy = 1;
            contentPanel.add(panelWorkZones, gbc_panelWorkZones);
            GridBagLayout gbl_panelWorkZones = new GridBagLayout();
            gbl_panelWorkZones.columnWidths = new int[]{40, 0, 0};
            gbl_panelWorkZones.rowHeights = new int[]{28, 28, 28, 28, 28, 28};
            gbl_panelWorkZones.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0};
            gbl_panelWorkZones.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
            panelWorkZones.setLayout(gbl_panelWorkZones);
            {
                JLabel lblZone = new JLabel("CNC Capacity : ");
                GridBagConstraints gbc_lblZone = new GridBagConstraints();
                gbc_lblZone.insets = new Insets(0, 0, 5, 5);
                gbc_lblZone.gridx = 0;
                gbc_lblZone.gridy = 0;
                panelWorkZones.add(lblZone, gbc_lblZone);
            }
            {
                cncCapacity = new JComboBox<>();
                GridBagConstraints gbc_txtZone1Barcode = new GridBagConstraints();
                gbc_txtZone1Barcode.insets = new Insets(0, 0, 5, 5);
                gbc_txtZone1Barcode.fill = GridBagConstraints.HORIZONTAL;
                gbc_txtZone1Barcode.gridx = 1;
                gbc_txtZone1Barcode.gridy = 0;
                panelWorkZones.add(cncCapacity, gbc_txtZone1Barcode);
                for (int i = 1; i <= 14; i++) {
                    cncCapacity.addItem(String.valueOf(i));
                }
                cncCapacity.addItemListener(e -> {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        capacity = (String) cncCapacity.getSelectedItem();
                    }
                });
            }
            {
                JLabel lblZone_1 = new JLabel("Process Position");
                GridBagConstraints gbc_lblZone_1 = new GridBagConstraints();
                gbc_lblZone_1.insets = new Insets(0, 0, 5, 5);
                gbc_lblZone_1.gridx = 0;
                gbc_lblZone_1.gridy = 1;
                panelWorkZones.add(lblZone_1, gbc_lblZone_1);
            }
            {
                txtProcessPosition = new JTextField("please use ; as division mark");
                GridBagConstraints gbc_txtZone2Barcode = new GridBagConstraints();
                gbc_txtZone2Barcode.insets = new Insets(0, 0, 5, 5);
                gbc_txtZone2Barcode.fill = GridBagConstraints.HORIZONTAL;
                gbc_txtZone2Barcode.gridx = 1;
                gbc_txtZone2Barcode.gridy = 1;
                panelWorkZones.add(txtProcessPosition, gbc_txtZone2Barcode);
                txtProcessPosition.setColumns(10);
                txtProcessPosition.addFocusListener(new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        if (txtProcessPosition.getText().equals("please use ; as division mark")){
                            txtProcessPosition.setText("");     //将提示文字清空
                            txtProcessPosition.setForeground(Color.black);  //设置用户输入的字体颜色为黑色
                        }
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        if (txtProcessPosition.getText().equals("")){
                            cncIP = comboBoxIP.getSelectedItem().toString();

                            txtProcessPosition.setForeground(Color.gray); //将提示文字设置为灰色
                            txtProcessPosition.setText("please use ; as division mark");     //显示提示文字
                        }
                    }
                });
            }
            {
                btnConfim = new JButton("Confirm");
                btnConfim.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (btnConfim.isEnabled() && 1 == e.getButton()) {
                            positions = txtProcessPosition.getText().split(";");
                            cncIP = comboBoxIP.getSelectedItem().toString();
                            XmlUtils.setCncConfig(cncIP,"capacity",capacity);
                            for(int i = 1;i <= positions.length;i++)
                            {
                                XmlUtils.setCncConfig(cncIP,"workzone"+i,positions[i-1]);
                            }
                            JOptionPane.showMessageDialog(null, "Config successfully");
                        }
                    }
                });
                GridBagConstraints gbc_btnLock2 = new GridBagConstraints();
                gbc_btnLock2.insets = new Insets(0, 0, 5, 5);
                gbc_btnLock2.gridx = 2;
                gbc_btnLock2.gridy = 1;
                panelWorkZones.add(btnConfim, gbc_btnLock2);
            }

//            {
//                JLabel lblZone_1 = new JLabel();
//                GridBagConstraints gbc_lblZone_1 = new GridBagConstraints();
//                gbc_lblZone_1.insets = new Insets(0, 0, 5, 5);
//                gbc_lblZone_1.gridx = 1;
//                gbc_lblZone_1.gridy = 2;
//                panelWorkZones.add(lblZone_1, gbc_lblZone_1);
//            }

        }
        {
            JLabel lblBasic = new JLabel("Basic Info");
            GridBagConstraints gbc_lblBasic = new GridBagConstraints();
            gbc_lblBasic.fill = GridBagConstraints.VERTICAL;
            gbc_lblBasic.insets = new Insets(0, 0, 0, 5);
            gbc_lblBasic.gridx = 0;
            gbc_lblBasic.gridy = 0;
            contentPanel.add(lblBasic, gbc_lblBasic);
        }
        {
            JPanel panelBasic = new JPanel();
            panelBasic.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
            GridBagConstraints gbc_panelBasic = new GridBagConstraints();
            gbc_panelBasic.fill = GridBagConstraints.BOTH;
            gbc_panelBasic.gridx = 1;
            gbc_panelBasic.gridy = 0;
            contentPanel.add(panelBasic, gbc_panelBasic);
            GridBagLayout gbl_panelBasic = new GridBagLayout();
            gbl_panelBasic.columnWidths = new int[]{40, 210, 100};
            gbl_panelBasic.rowHeights = new int[]{28, 28, 28};
            gbl_panelBasic.columnWeights = new double[]{0.0, 0.0, 0.0};
            gbl_panelBasic.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0};
            panelBasic.setLayout(gbl_panelBasic);
            {
                JLabel lblIp_1 = new JLabel("IP");
                GridBagConstraints gbc_lblIp_1 = new GridBagConstraints();
                gbc_lblIp_1.insets = new Insets(0, 0, 5, 5);
                gbc_lblIp_1.gridx = 0;
                gbc_lblIp_1.gridy = 0;
                panelBasic.add(lblIp_1, gbc_lblIp_1);
            }
            {
                comboBoxIP = new JComboBox<String>();
                GridBagConstraints gbc_comboBoxIP = new GridBagConstraints();
                gbc_comboBoxIP.fill = GridBagConstraints.HORIZONTAL;
                gbc_comboBoxIP.insets = new Insets(0, 0, 5, 5);
                gbc_comboBoxIP.gridx = 1;
                gbc_comboBoxIP.gridy = 0;
                panelBasic.add(comboBoxIP, gbc_comboBoxIP);
                comboBoxIP.setModel(new DefaultComboBoxModel<String>(getIPList()));
                {
                    JButton btnRefreshCncList = new JButton("Refresh CNC List");
                    GridBagConstraints gbc_btnRefreshCncList = new GridBagConstraints();
                    gbc_btnRefreshCncList.fill = GridBagConstraints.HORIZONTAL;
                    gbc_btnRefreshCncList.insets = new Insets(0, 0, 5, 0);
                    gbc_btnRefreshCncList.gridx = 2;
                    gbc_btnRefreshCncList.gridy = 0;
                    panelBasic.add(btnRefreshCncList, gbc_btnRefreshCncList);
                    btnRefreshCncList.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (1 == e.getButton()) {
                                btnRefreshCncList.setEnabled(false);

                                GUIUtils.setComboBoxValues(comboBoxIP, getIPList());
                                setCncInfo();

                                btnRefreshCncList.setEnabled(true);
                            }
                        }
                    });
                }
//                {
//                    comboBoxIP.addItemListener(new ItemListener() {
//                        @Override
//                        public void itemStateChanged(ItemEvent e) {
//                            cncIP = comboBoxIP.getSelectedItem().toString();
//                            System.out.println(cncIP);
//                        }
//                    });
//                }
                {
                    JLabel lblInfo_1 = new JLabel("Info");
                    GridBagConstraints gbc_lblInfo_1 = new GridBagConstraints();
                    gbc_lblInfo_1.insets = new Insets(0, 0, 5, 5);
                    gbc_lblInfo_1.gridx = 0;
                    gbc_lblInfo_1.gridy = 2;
                    panelBasic.add(lblInfo_1, gbc_lblInfo_1);
                }
                {
                    textField_Info = new JTextField();
                    GridBagConstraints gbc_textField_Info = new GridBagConstraints();
                    gbc_textField_Info.insets = new Insets(0, 0, 5, 5);
                    gbc_textField_Info.fill = GridBagConstraints.HORIZONTAL;
                    gbc_textField_Info.gridx = 1;
                    gbc_textField_Info.gridy = 2;
                    panelBasic.add(textField_Info, gbc_textField_Info);
                    textField_Info.setEditable(false);
                    textField_Info.setText("9100");
                    textField_Info.setColumns(10);
                }
                {
                    btnStopMonitoring = new JButton("Stop Monitoring");
                    btnStopMonitoring.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (btnStopMonitoring.isEnabled() && 1 == e.getButton()) {
                                if (0 == JOptionPane.showConfirmDialog(contentPanel, "Are you sure of stopping the monitoring", "Stop Machining Monitoring", JOptionPane.YES_NO_OPTION)) {
                                    String ip = comboBoxIP.getSelectedItem().toString();
                                    CncData cncData = CncData.getInstance();
                                    cncData.setCncLastState(ip, DeviceState.SHUTDOWN);
                                }
                            }
                        }
                    });
                    GridBagConstraints gbc_btnStopMonitoring = new GridBagConstraints();
                    gbc_btnStopMonitoring.fill = GridBagConstraints.HORIZONTAL;
                    gbc_btnStopMonitoring.insets = new Insets(0, 0, 5, 0);
                    gbc_btnStopMonitoring.gridx = 2;
                    gbc_btnStopMonitoring.gridy = 2;
                    panelBasic.add(btnStopMonitoring, gbc_btnStopMonitoring);
                }
                {
                    JLabel lblMode_1 = new JLabel("Mode");
                    GridBagConstraints gbc_lblMode_1 = new GridBagConstraints();
                    gbc_lblMode_1.insets = new Insets(0, 0, 0, 5);
                    gbc_lblMode_1.gridx = 0;
                    gbc_lblMode_1.gridy = 3;
                    panelBasic.add(lblMode_1, gbc_lblMode_1);
                }
                {
                    chckbxAutoMode = new JCheckBox("Semi-Auto Mode");
                    chckbxAutoMode.setSelected(true);
                    GridBagConstraints gbc_chckbxAutoMode = new GridBagConstraints();
                    gbc_chckbxAutoMode.anchor = GridBagConstraints.WEST;
                    gbc_chckbxAutoMode.insets = new Insets(0, 0, 0, 5);
                    gbc_chckbxAutoMode.gridx = 1;
                    gbc_chckbxAutoMode.gridy = 3;
                    panelBasic.add(chckbxAutoMode, gbc_chckbxAutoMode);
                }
                comboBoxIP.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent arg0) {
                        setCncInfo();
                    }
                });
            }
        }

        setCncInfo();
        setButtonsEnabled(true, false);
    }

    public void setButtonsEnabled(boolean enabled, boolean force) {
        try {
            if (force) {
                btnLock1.setEnabled(enabled);
                btnConfim.setEnabled(enabled);
                btnLock3.setEnabled(enabled);
                btnLock4.setEnabled(enabled);
                btnLock5.setEnabled(enabled);
                btnLock6.setEnabled(enabled);
                btnUnlock1.setEnabled(enabled);
                btnUnlock2.setEnabled(enabled);
                btnUnlock3.setEnabled(enabled);
                btnUnlock4.setEnabled(enabled);
                btnUnlock5.setEnabled(enabled);
                btnUnlock6.setEnabled(enabled);
                btnOpenDoor.setEnabled(enabled);
                btnCloseDoor.setEnabled(enabled);
                btnUploadNcProgram.setEnabled(enabled);
                btnLock.setEnabled(enabled);
                btnUnlock.setEnabled(enabled);
                btnStartMachining.setEnabled(enabled);
            } else {
                btnLock1.setEnabled(!lockFlags[0]);
                btnConfim.setEnabled(!lockFlags[1]);
                btnLock3.setEnabled(!lockFlags[2]);
                btnLock4.setEnabled(!lockFlags[3]);
                btnLock5.setEnabled(!lockFlags[4]);
                btnLock6.setEnabled(!lockFlags[5]);
                btnUnlock1.setEnabled(lockFlags[0]);
                btnUnlock2.setEnabled(lockFlags[1]);
                btnUnlock3.setEnabled(lockFlags[2]);
                btnUnlock4.setEnabled(lockFlags[3]);
                btnUnlock5.setEnabled(lockFlags[4]);
                btnUnlock6.setEnabled(lockFlags[5]);
                btnOpenDoor.setEnabled(enabled);
                btnCloseDoor.setEnabled(enabled);
                btnUploadNcProgram.setEnabled(enabled);
                btnLock.setEnabled(enabled);
                btnUnlock.setEnabled(enabled);
                btnStartMachining.setEnabled(enabled);
            }
        } catch (Exception e) {
        }
    }

    private CNC getControl() {
        CNC ctrl = null;

        String ip = comboBoxIP.getSelectedItem().toString();
        CncDriver cncDriver = CncDriver.getInstance();
        String cncModel = (String) cncData.getData(ip).get(CncItems.MODEL);
        String cncDrvName = (String) cncDriver.getData(cncModel).get(DriverItems.DRIVER);
        String cncDataHandler = (String) cncDriver.getData(cncModel).get(DriverItems.DATAHANDLER);
        ctrl = CncFactory.getInstance(cncDrvName, cncDataHandler, cncModel);

        return ctrl;
    }

    private void setCncInfo() {
        String deviceIP = comboBoxIP.getSelectedItem().toString();
        String info = "Unkown device";
        String cncModel = "";
        if (null != cncData.getData(deviceIP)) {
            cncModel = (String) cncData.getData(deviceIP).get(CncItems.MODEL);
            info = cncModel;
            info += " / " + cncData.getData(deviceIP).get(CncItems.PORT);
            cncData.clearMachiningData(deviceIP);
        }
        textField_Info.setText(info);
        GUIUtils.setComboBoxValues(comboBoxCommand, getCommandList(cncModel));
    }

    private String[] getCommandList(String mainKey) {
        String[] cmds = CncWebAPI.getInstance().getAllOperations(mainKey);
        return cmds;
    }

    private String[] getIPList() {
        String[] ips = null;
        String tmp = "";

        Map<String, LinkedHashMap<CncItems, Object>> dt = cncData.getDataMap();
        for (String ip : dt.keySet()) {
            if ("".equals(tmp)) {
                tmp = ip;
            } else {
                tmp += "," + ip;
            }
        }

        if (!"".equals(tmp)) {
            ips = tmp.split(",");
        } else {
            ips = new String[]{" "};
        }
        return ips;
    }

    private String getNotBlankZones() {
        String zns = "", tmp = "";

        tmp = txtZone1Barcode.getText().trim();
        if (!"".equals(tmp)) {
            if ("".equals(zns)) {
                zns = "1";
            } else {
                zns += ";1";
            }
        }

        tmp = txtProcessPosition.getText().trim();
        if (!"".equals(tmp)) {
            if ("".equals(zns)) {
                zns = "2";
            } else {
                zns += ";2";
            }
        }

        tmp = txtZone3Barcode.getText().trim();
        if (!"".equals(tmp)) {
            if ("".equals(zns)) {
                zns = "3";
            } else {
                zns += ";3";
            }
        }

        tmp = txtZone4Barcode.getText().trim();
        if (!"".equals(tmp)) {
            if ("".equals(zns)) {
                zns = "4";
            } else {
                zns += ";4";
            }
        }

        tmp = txtZone5Barcode.getText().trim();
        if (!"".equals(tmp)) {
            if ("".equals(zns)) {
                zns = "5";
            } else {
                zns += ";5";
            }
        }

        tmp = txtZone6Barcode.getText().trim();
        if (!"".equals(tmp)) {
            if ("".equals(zns)) {
                zns = "6";
            } else {
                zns += ";6";
            }
        }
        return zns;
    }

    private String openDoor() {
        String errMsg = "";
        CNC cncCtrl = getControl();
        if (null != cncCtrl) {
            String ip = comboBoxIP.getSelectedItem().toString();
            if (cncCtrl.openDoor(ip)) {
                doorClosedFlag = false;
            } else {
                doorClosedFlag = true;
                errMsg = "Open door failed";
            }
        } else {
            errMsg = "Get controller failed";
        }
        if (!chckbxAutoMode.isSelected()) setButtonsEnabled(true, false);
        return errMsg;
    }

    private String unlockFixtures() {
        String errMsg = "";
        CNC cncCtrl = getControl();
        if (null != cncCtrl) {
            String ip = comboBoxIP.getSelectedItem().toString();
            for (int workZone = 1; workZone <= 6; workZone++) {
//				if(lockFlags[workZone-1]){
                if (cncCtrl.releaseFixture(ip, workZone)) {
                    lockFlags[workZone - 1] = false;
                } else {
                    errMsg += "\r\n" + "Unlock fixture#" + workZone + " failed";
                }
//				}
            }
        } else {
            errMsg = "Get controller failed";
        }
        if (!chckbxAutoMode.isSelected()) setButtonsEnabled(true, false);
        return errMsg;
    }

    private boolean startMachining(CNC cncCtrl, String cncIP, String programName, int retryTimes, int timeout_s) {
        boolean ok = false;

        long startT, diffTime, timeout_ms;
        DeviceState cncState = DeviceState.STANDBY;

        timeout_ms = timeout_s * 1000;
        for (int i = 0; i < retryTimes; i++) {
            if (cncCtrl.startMachining(cncIP, programName)) {
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

    private String doCleaning(CNC cncCtrl, String cncIP, int[] workZones, String[] workpieceIDs) {
        String rtn = "", lineName = "", cncModel = "";
        CncData cncData = CncData.getInstance();
        lineName = cncData.getLineName(cncIP);
        cncModel = cncData.getCncModel(cncIP);

        rtn = initWorkpieceData(workZones, workpieceIDs, lineName, cncModel, true);
        if (!"".equals(rtn)) return rtn;
        if (!cncCtrl.openDoor(cncIP)) return "Cleaning:Open door failed";
        if (!cncCtrl.closeDoor(cncIP)) return "Cleaning:Close door failed";
        if (!UploadNCProgram.uploadSubPrograms(cncCtrl, cncIP, workZones, workpieceIDs))
            return "Cleaning:Upload sub programs failed";
        if (!UploadNCProgram.uploadMainProgram(cncCtrl, cncIP, workZones, workpieceIDs))
            return "Cleaning:Upload main program failed";

        CncWebAPI cncWebAPI = CncWebAPI.getInstance();
        String mainProgramName = cncWebAPI.getMainProgramName(cncData.getCncModel(cncIP));
        if (cncCtrl.startMachining(cncIP, mainProgramName)) {
            DeviceMonitor devMonitor = DeviceMonitor.getInstance();
            if (!devMonitor.cncMachiningIsDone(cncCtrl, cncIP, 0, null)) {
                return "Cleaning failed";
            } else {
                removeWorkpieceData(workpieceIDs);
            }
        } else {
            return "Cleaning:Start cleaning program failed";
        }

        return rtn;
    }

    private String lockSingleFixture(int zoneNo) {
        String errMsg = "";
        if (zoneNo < 1 || zoneNo > 6) {
            errMsg = "Invalid Zone#" + zoneNo;
        } else {
            CNC cncCtrl = getControl();
            if (null != cncCtrl) {
                String ip = comboBoxIP.getSelectedItem().toString();
                if (cncCtrl.clampFixture(ip, zoneNo)) {
                    lockFlags[zoneNo - 1] = true;
                } else {
                    lockFlags[zoneNo - 1] = false;
                    errMsg = "Lock fixture#" + zoneNo + " failed";
                }
            } else {
                errMsg = "Get controller failed";
            }
        }
        if (!chckbxAutoMode.isSelected()) setButtonsEnabled(true, false);
        return errMsg;
    }


    private void setMachiningProgress(int val) {
        if (null != progressBar) {
            progressBar.setValue(val);
            progressBar.repaint();
        }
    }

    private void resetRunningFlags() {
        for (int i = 0; i < ncUploadFlags.length; i++) {
            ncUploadFlags[i] = false;
        }
    }

    class CncMonitoring implements Runnable {
        private String cncIP;

        public CncMonitoring(String ip) {
            cncIP = ip;
        }

        @Override
        public void run() {
            String strTemp = "";
            CncData cncData = CncData.getInstance();
            DeviceState cncState = DeviceState.WORKING;
            DeviceMonitor devMonitor = DeviceMonitor.getInstance();
            while (true) {
                devMonitor.cncMonitoring(cncIP);
                cncState = cncData.getCncLastState(cncIP);
                cncData.getCncDataString(cncIP);
                setMachiningProgress(cncData.getMachiningProgress(cncIP));
                if (DeviceState.FINISH == cncState || DeviceState.SHUTDOWN == cncState) break;
                try {
                    strTemp = cncConfigDialog.getTitle();
                    if (null != strTemp) {
                        cncConfigDialog.setTitle(strTemp.split("##")[0] + "##" + TimeUtils.getCurrentDate("HH:mm:ss"));
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }

            if (DeviceState.FINISH == cncState) {
                CNC cncCtrl = cncData.getCncController(cncIP);
                int[] workZones = new int[]{1, 2, 3};
                String[] codeIDs = new String[]{"00000000131", "00000000232", "00000000333"};
                strTemp = doCleaning(cncCtrl, cncIP, workZones, codeIDs);
                if (!"".equals(strTemp)) {
                    JOptionPane.showMessageDialog(contentPanel, strTemp, "Cleaning Error", JOptionPane.ERROR_MESSAGE);
                }
                cncData.saveMachiningData(cncIP);
                strTemp = openDoor();
                if (!"".equals(strTemp)) {
                    JOptionPane.showMessageDialog(contentPanel, strTemp, "Operaion Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    strTemp = unlockFixtures();
                    if (!"".equals(strTemp)) {
                        JOptionPane.showMessageDialog(contentPanel, strTemp, "Operaion Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        cncData.setData(cncIP, CncItems.STATE, DeviceState.STANDBY);
                    }
                }
            }
            cncData.clearMachiningData(cncIP);
            resetRunningFlags();
            setButtonsEnabled(true, false);
        }
    }
}
