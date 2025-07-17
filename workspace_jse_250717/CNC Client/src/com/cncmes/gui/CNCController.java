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
import com.cncmes.utils.DataUtils;
import com.cncmes.utils.GUIUtils;
import com.cncmes.utils.RunningMsg;
import com.cncmes.utils.ThreadUtils;
import com.cncmes.utils.TimeUtils;
import com.cncmes.utils.UploadNCProgram;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.JCheckBox;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;

public class CNCController extends JDialog {
    private JComboBox<String> comboBoxIP;
    private JComboBox<String> comboBoxCommand;
    private JTextField textField_Info;

    private static final long serialVersionUID = 18L;
    private final JPanel contentPanel = new JPanel();
    private static CNCController cncController = new CNCController();
    private static CncData cncData = null;
    private JTextField txtZone1Barcode;
    private JTextField txtZone2Barcode;
    private JTextField txtZone3Barcode;
    private JTextField txtZone4Barcode;
    private JTextField txtZone5Barcode;
    private JTextField txtZone6Barcode;
    private JCheckBox chckbxAutoMode;
    private JProgressBar progressBar;
    private JButton btnLock1;
    private JButton btnLock2;
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

    private static boolean[] lockFlags = new boolean[6];
    private static boolean[] ncUploadFlags = new boolean[6];
    private static boolean doorClosedFlag = false;

    public static CNCController getInstance() {
        cncData = CncData.getInstance();
        return cncController;
    }

    /**
     * Create the dialog.
     */
    private CNCController() {
        cncData = CncData.getInstance();
        setModal(true);
        setIconImage(Toolkit.getDefaultToolkit().getImage(CNCController.class.getResource("/com/cncmes/img/Butterfly_24.png")));
        setTitle("CNC Controller");
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
            JLabel lblWorkZone = new JLabel("Work Zones");
            GridBagConstraints gbc_lblWorkZone = new GridBagConstraints();
            gbc_lblWorkZone.fill = GridBagConstraints.VERTICAL;
            gbc_lblWorkZone.insets = new Insets(0, 0, 5, 5);
            gbc_lblWorkZone.gridx = 0;
            gbc_lblWorkZone.gridy = 1;
            contentPanel.add(lblWorkZone, gbc_lblWorkZone);
        }
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
                JLabel lblZone = new JLabel("Zn_1");
                GridBagConstraints gbc_lblZone = new GridBagConstraints();
                gbc_lblZone.insets = new Insets(0, 0, 5, 5);
                gbc_lblZone.gridx = 0;
                gbc_lblZone.gridy = 0;
                panelWorkZones.add(lblZone, gbc_lblZone);
            }
            {
                txtZone1Barcode = new JTextField();
                GridBagConstraints gbc_txtZone1Barcode = new GridBagConstraints();
                gbc_txtZone1Barcode.insets = new Insets(0, 0, 5, 5);
                gbc_txtZone1Barcode.fill = GridBagConstraints.HORIZONTAL;
                gbc_txtZone1Barcode.gridx = 1;
                gbc_txtZone1Barcode.gridy = 0;
                panelWorkZones.add(txtZone1Barcode, gbc_txtZone1Barcode);
                txtZone1Barcode.setColumns(10);
            }
            {
                btnLock1 = new JButton("Lock");
                btnLock1.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (btnLock1.isEnabled() && 1 == e.getButton()) {
                            btnLock1.setEnabled(false);
                            String errMsg = lockSingleFixture(1);
                            if (!"".equals(errMsg))
                                JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                GridBagConstraints gbc_btnLock1 = new GridBagConstraints();
                gbc_btnLock1.insets = new Insets(0, 0, 5, 5);
                gbc_btnLock1.gridx = 2;
                gbc_btnLock1.gridy = 0;
                panelWorkZones.add(btnLock1, gbc_btnLock1);
            }
            {
                btnUnlock1 = new JButton("Unlock");
                btnUnlock1.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (btnUnlock1.isEnabled() && 1 == e.getButton()) {
                            btnUnlock1.setEnabled(false);
                            String errMsg = unlockSingleFixture(1);
                            if (!"".equals(errMsg))
                                JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                GridBagConstraints gbc_btnUnlock1 = new GridBagConstraints();
                gbc_btnUnlock1.insets = new Insets(0, 0, 5, 0);
                gbc_btnUnlock1.gridx = 3;
                gbc_btnUnlock1.gridy = 0;
                panelWorkZones.add(btnUnlock1, gbc_btnUnlock1);
            }
            {
                JLabel lblZone_1 = new JLabel("Zn_2");
                GridBagConstraints gbc_lblZone_1 = new GridBagConstraints();
                gbc_lblZone_1.insets = new Insets(0, 0, 5, 5);
                gbc_lblZone_1.gridx = 0;
                gbc_lblZone_1.gridy = 1;
                panelWorkZones.add(lblZone_1, gbc_lblZone_1);
            }
            {
                txtZone2Barcode = new JTextField();
                GridBagConstraints gbc_txtZone2Barcode = new GridBagConstraints();
                gbc_txtZone2Barcode.insets = new Insets(0, 0, 5, 5);
                gbc_txtZone2Barcode.fill = GridBagConstraints.HORIZONTAL;
                gbc_txtZone2Barcode.gridx = 1;
                gbc_txtZone2Barcode.gridy = 1;
                panelWorkZones.add(txtZone2Barcode, gbc_txtZone2Barcode);
                txtZone2Barcode.setColumns(10);
            }
            {
                btnLock2 = new JButton("Lock");
                btnLock2.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (btnLock2.isEnabled() && 1 == e.getButton()) {
                            btnLock2.setEnabled(false);
                            String errMsg = lockSingleFixture(2);
                            if (!"".equals(errMsg))
                                JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                GridBagConstraints gbc_btnLock2 = new GridBagConstraints();
                gbc_btnLock2.insets = new Insets(0, 0, 5, 5);
                gbc_btnLock2.gridx = 2;
                gbc_btnLock2.gridy = 1;
                panelWorkZones.add(btnLock2, gbc_btnLock2);
            }
            {
                btnUnlock2 = new JButton("Unlock");
                btnUnlock2.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (btnUnlock2.isEnabled() && 1 == e.getButton()) {
                            btnUnlock2.setEnabled(false);
                            String errMsg = unlockSingleFixture(2);
                            if (!"".equals(errMsg))
                                JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                GridBagConstraints gbc_btnUnlock2 = new GridBagConstraints();
                gbc_btnUnlock2.insets = new Insets(0, 0, 5, 0);
                gbc_btnUnlock2.gridx = 3;
                gbc_btnUnlock2.gridy = 1;
                panelWorkZones.add(btnUnlock2, gbc_btnUnlock2);
            }
            {
                JLabel lblZn = new JLabel("Zn_3");
                GridBagConstraints gbc_lblZn = new GridBagConstraints();
                gbc_lblZn.insets = new Insets(0, 0, 5, 5);
                gbc_lblZn.gridx = 0;
                gbc_lblZn.gridy = 2;
                panelWorkZones.add(lblZn, gbc_lblZn);
            }
            {
                txtZone3Barcode = new JTextField();
                GridBagConstraints gbc_txtZone3Barcode = new GridBagConstraints();
                gbc_txtZone3Barcode.insets = new Insets(0, 0, 5, 5);
                gbc_txtZone3Barcode.fill = GridBagConstraints.HORIZONTAL;
                gbc_txtZone3Barcode.gridx = 1;
                gbc_txtZone3Barcode.gridy = 2;
                panelWorkZones.add(txtZone3Barcode, gbc_txtZone3Barcode);
                txtZone3Barcode.setColumns(10);
            }
            {
                btnLock3 = new JButton("Lock");
                btnLock3.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (btnLock3.isEnabled() && 1 == e.getButton()) {
                            btnLock3.setEnabled(false);
                            String errMsg = lockSingleFixture(3);
                            if (!"".equals(errMsg))
                                JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                GridBagConstraints gbc_btnLock3 = new GridBagConstraints();
                gbc_btnLock3.insets = new Insets(0, 0, 5, 5);
                gbc_btnLock3.gridx = 2;
                gbc_btnLock3.gridy = 2;
                panelWorkZones.add(btnLock3, gbc_btnLock3);
            }
            {
                btnUnlock3 = new JButton("Unlock");
                btnUnlock3.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (btnUnlock3.isEnabled() && 1 == e.getButton()) {
                            btnUnlock3.setEnabled(false);
                            String errMsg = unlockSingleFixture(3);
                            if (!"".equals(errMsg))
                                JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                GridBagConstraints gbc_btnUnlock3 = new GridBagConstraints();
                gbc_btnUnlock3.insets = new Insets(0, 0, 5, 0);
                gbc_btnUnlock3.gridx = 3;
                gbc_btnUnlock3.gridy = 2;
                panelWorkZones.add(btnUnlock3, gbc_btnUnlock3);
            }
            {
                JLabel lblZn_1 = new JLabel("Zn_4");
                GridBagConstraints gbc_lblZn_1 = new GridBagConstraints();
                gbc_lblZn_1.insets = new Insets(0, 0, 5, 5);
                gbc_lblZn_1.gridx = 0;
                gbc_lblZn_1.gridy = 3;
                panelWorkZones.add(lblZn_1, gbc_lblZn_1);
            }
            {
                txtZone4Barcode = new JTextField();
                GridBagConstraints gbc_txtZone4Barcode = new GridBagConstraints();
                gbc_txtZone4Barcode.insets = new Insets(0, 0, 5, 5);
                gbc_txtZone4Barcode.fill = GridBagConstraints.HORIZONTAL;
                gbc_txtZone4Barcode.gridx = 1;
                gbc_txtZone4Barcode.gridy = 3;
                panelWorkZones.add(txtZone4Barcode, gbc_txtZone4Barcode);
                txtZone4Barcode.setColumns(10);
            }
            {
                btnLock4 = new JButton("Lock");
                btnLock4.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (btnLock4.isEnabled() && 1 == e.getButton()) {
                            btnLock4.setEnabled(false);
                            String errMsg = lockSingleFixture(4);
                            if (!"".equals(errMsg))
                                JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                GridBagConstraints gbc_btnLock4 = new GridBagConstraints();
                gbc_btnLock4.insets = new Insets(0, 0, 5, 5);
                gbc_btnLock4.gridx = 2;
                gbc_btnLock4.gridy = 3;
                panelWorkZones.add(btnLock4, gbc_btnLock4);
            }
            {
                btnUnlock4 = new JButton("Unlock");
                btnUnlock4.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (btnUnlock4.isEnabled() && 1 == e.getButton()) {
                            btnUnlock4.setEnabled(false);
                            String errMsg = unlockSingleFixture(4);
                            if (!"".equals(errMsg))
                                JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                GridBagConstraints gbc_btnUnlock4 = new GridBagConstraints();
                gbc_btnUnlock4.insets = new Insets(0, 0, 5, 0);
                gbc_btnUnlock4.gridx = 3;
                gbc_btnUnlock4.gridy = 3;
                panelWorkZones.add(btnUnlock4, gbc_btnUnlock4);
            }
            {
                JLabel lblZn_2 = new JLabel("Zn_5");
                GridBagConstraints gbc_lblZn_2 = new GridBagConstraints();
                gbc_lblZn_2.insets = new Insets(0, 0, 5, 5);
                gbc_lblZn_2.gridx = 0;
                gbc_lblZn_2.gridy = 4;
                panelWorkZones.add(lblZn_2, gbc_lblZn_2);
            }
            {
                txtZone5Barcode = new JTextField();
                GridBagConstraints gbc_txtZone5Barcode = new GridBagConstraints();
                gbc_txtZone5Barcode.insets = new Insets(0, 0, 5, 5);
                gbc_txtZone5Barcode.fill = GridBagConstraints.HORIZONTAL;
                gbc_txtZone5Barcode.gridx = 1;
                gbc_txtZone5Barcode.gridy = 4;
                panelWorkZones.add(txtZone5Barcode, gbc_txtZone5Barcode);
                txtZone5Barcode.setColumns(10);
            }
            {
                btnLock5 = new JButton("Lock");
                btnLock5.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (btnLock5.isEnabled() && 1 == e.getButton()) {
                            btnLock5.setEnabled(false);
                            String errMsg = lockSingleFixture(5);
                            if (!"".equals(errMsg))
                                JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                GridBagConstraints gbc_btnLock5 = new GridBagConstraints();
                gbc_btnLock5.insets = new Insets(0, 0, 5, 5);
                gbc_btnLock5.gridx = 2;
                gbc_btnLock5.gridy = 4;
                panelWorkZones.add(btnLock5, gbc_btnLock5);
            }
            {
                btnUnlock5 = new JButton("Unlock");
                btnUnlock5.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (btnUnlock5.isEnabled() && 1 == e.getButton()) {
                            btnUnlock5.setEnabled(false);
                            String errMsg = unlockSingleFixture(5);
                            if (!"".equals(errMsg))
                                JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                GridBagConstraints gbc_btnUnlock5 = new GridBagConstraints();
                gbc_btnUnlock5.insets = new Insets(0, 0, 5, 0);
                gbc_btnUnlock5.gridx = 3;
                gbc_btnUnlock5.gridy = 4;
                panelWorkZones.add(btnUnlock5, gbc_btnUnlock5);
            }
            {
                JLabel lblZn_3 = new JLabel("Zn_6");
                GridBagConstraints gbc_lblZn_3 = new GridBagConstraints();
                gbc_lblZn_3.insets = new Insets(0, 0, 0, 5);
                gbc_lblZn_3.gridx = 0;
                gbc_lblZn_3.gridy = 5;
                panelWorkZones.add(lblZn_3, gbc_lblZn_3);
            }
            {
                txtZone6Barcode = new JTextField();
                GridBagConstraints gbc_txtZone6Barcode = new GridBagConstraints();
                gbc_txtZone6Barcode.insets = new Insets(0, 0, 0, 5);
                gbc_txtZone6Barcode.fill = GridBagConstraints.HORIZONTAL;
                gbc_txtZone6Barcode.gridx = 1;
                gbc_txtZone6Barcode.gridy = 5;
                panelWorkZones.add(txtZone6Barcode, gbc_txtZone6Barcode);
                txtZone6Barcode.setColumns(10);
            }
            {
                btnLock6 = new JButton("Lock");
                btnLock6.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (btnLock6.isEnabled() && 1 == e.getButton()) {
                            btnLock6.setEnabled(false);
                            String errMsg = lockSingleFixture(6);
                            if (!"".equals(errMsg))
                                JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                GridBagConstraints gbc_btnLock6 = new GridBagConstraints();
                gbc_btnLock6.insets = new Insets(0, 0, 0, 5);
                gbc_btnLock6.gridx = 2;
                gbc_btnLock6.gridy = 5;
                panelWorkZones.add(btnLock6, gbc_btnLock6);
            }
            {
                btnUnlock6 = new JButton("Unlock");
                btnUnlock6.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (btnUnlock6.isEnabled() && 1 == e.getButton()) {
                            btnUnlock6.setEnabled(false);
                            String errMsg = unlockSingleFixture(6);
                            if (!"".equals(errMsg))
                                JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                GridBagConstraints gbc_btnUnlock6 = new GridBagConstraints();
                gbc_btnUnlock6.gridx = 3;
                gbc_btnUnlock6.gridy = 5;
                panelWorkZones.add(btnUnlock6, gbc_btnUnlock6);
            }
        }
        {
            JLabel lblOperation = new JLabel("Operation");
            GridBagConstraints gbc_lblOperation = new GridBagConstraints();
            gbc_lblOperation.fill = GridBagConstraints.VERTICAL;
            gbc_lblOperation.insets = new Insets(0, 0, 5, 5);
            gbc_lblOperation.gridx = 0;
            gbc_lblOperation.gridy = 2;
            contentPanel.add(lblOperation, gbc_lblOperation);
        }
        {
            JPanel panelOperation = new JPanel();
            panelOperation.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
            GridBagConstraints gbc_panelOperation = new GridBagConstraints();
            gbc_panelOperation.insets = new Insets(0, 0, 5, 0);
            gbc_panelOperation.fill = GridBagConstraints.BOTH;
            gbc_panelOperation.gridx = 1;
            gbc_panelOperation.gridy = 2;
            contentPanel.add(panelOperation, gbc_panelOperation);
            GridBagLayout gbl_panelOperation = new GridBagLayout();
            gbl_panelOperation.columnWidths = new int[]{100, 100, 100};
            gbl_panelOperation.rowHeights = new int[]{28, 28};
            gbl_panelOperation.columnWeights = new double[]{0.0, 0.0, 0.0};
            gbl_panelOperation.rowWeights = new double[]{0.0, 0.0};
            panelOperation.setLayout(gbl_panelOperation);
            {
                btnOpenDoor = new JButton("Open Door");
                btnOpenDoor.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (btnOpenDoor.isEnabled() && 1 == e.getButton()) {
                            btnOpenDoor.setEnabled(false);
                            String errMsg = openDoor();
                            btnOpenDoor.setEnabled(true);
                            if (!"".equals(errMsg))
                                JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                GridBagConstraints gbc_btnOpenDoor = new GridBagConstraints();
                gbc_btnOpenDoor.fill = GridBagConstraints.HORIZONTAL;
                gbc_btnOpenDoor.insets = new Insets(0, 0, 5, 5);
                gbc_btnOpenDoor.gridx = 0;
                gbc_btnOpenDoor.gridy = 0;
                panelOperation.add(btnOpenDoor, gbc_btnOpenDoor);
            }
            {
                btnCloseDoor = new JButton("Close Door");
                btnCloseDoor.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (btnCloseDoor.isEnabled() && 1 == e.getButton()) {
                            btnCloseDoor.setEnabled(false);
                            String errMsg = closeDoor();
                            btnCloseDoor.setEnabled(true);
                            if (!"".equals(errMsg))
                                JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                GridBagConstraints gbc_btnCloseDoor = new GridBagConstraints();
                gbc_btnCloseDoor.fill = GridBagConstraints.HORIZONTAL;
                gbc_btnCloseDoor.insets = new Insets(0, 0, 5, 5);
                gbc_btnCloseDoor.gridx = 1;
                gbc_btnCloseDoor.gridy = 0;
                panelOperation.add(btnCloseDoor, gbc_btnCloseDoor);
            }
            {
                btnUploadNcProgram = new JButton("Upload NC Program");
                btnUploadNcProgram.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (btnUploadNcProgram.isEnabled() && 1 == e.getButton()) {
                            btnUploadNcProgram.setEnabled(false);
                            String errMsg = uploadNCProgram();
                            if (!"".equals(errMsg))
                                JOptionPane.showMessageDialog(contentPanel, errMsg, "Upload Program Error", JOptionPane.ERROR_MESSAGE);
                            btnUploadNcProgram.setEnabled(true);
                        }
                    }
                });
                GridBagConstraints gbc_btnUploadNcProgram = new GridBagConstraints();
                gbc_btnUploadNcProgram.insets = new Insets(0, 0, 5, 0);
                gbc_btnUploadNcProgram.gridx = 2;
                gbc_btnUploadNcProgram.gridy = 0;
                panelOperation.add(btnUploadNcProgram, gbc_btnUploadNcProgram);
            }
            {
                btnUnlock = new JButton("Unlock All");
                btnUnlock.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (btnUnlock.isEnabled() && 1 == e.getButton()) {
                            btnUnlock.setEnabled(false);
                            String errMsg = unlockFixtures();
                            if (!"".equals(errMsg))
                                JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                GridBagConstraints gbc_btnUnlock = new GridBagConstraints();
                gbc_btnUnlock.fill = GridBagConstraints.HORIZONTAL;
                gbc_btnUnlock.insets = new Insets(0, 0, 0, 5);
                gbc_btnUnlock.gridx = 0;
                gbc_btnUnlock.gridy = 1;
                panelOperation.add(btnUnlock, gbc_btnUnlock);
            }
            {
                btnLock = new JButton("Lock All");
                btnLock.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (btnLock.isEnabled() && 1 == e.getButton()) {
                            btnLock.setEnabled(false);
                            String errMsg = lockFixtures();
                            if (!"".equals(errMsg))
                                JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                GridBagConstraints gbc_btnLock = new GridBagConstraints();
                gbc_btnLock.fill = GridBagConstraints.HORIZONTAL;
                gbc_btnLock.insets = new Insets(0, 0, 0, 5);
                gbc_btnLock.gridx = 1;
                gbc_btnLock.gridy = 1;
                panelOperation.add(btnLock, gbc_btnLock);
            }
            {
                btnStartMachining = new JButton("Start Machining");
                btnStartMachining.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (btnStartMachining.isEnabled() && 1 == e.getButton()) {
                            setButtonsEnabled(false, true);
                            String errMsg = doMachining();
                            if (!"".equals(errMsg)) {
                                setButtonsEnabled(true, false);
                                JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                });
                GridBagConstraints gbc_btnStartMachining = new GridBagConstraints();
                gbc_btnStartMachining.fill = GridBagConstraints.HORIZONTAL;
                gbc_btnStartMachining.gridx = 2;
                gbc_btnStartMachining.gridy = 1;
                panelOperation.add(btnStartMachining, gbc_btnStartMachining);
            }
        }
        {
            JLabel lblProgress = new JLabel("Progress");
            GridBagConstraints gbc_lblProgress = new GridBagConstraints();
            gbc_lblProgress.insets = new Insets(0, 0, 5, 5);
            gbc_lblProgress.gridx = 0;
            gbc_lblProgress.gridy = 3;
            contentPanel.add(lblProgress, gbc_lblProgress);
        }
        {
            progressBar = new JProgressBar();
            progressBar.setMinimum(0);
            progressBar.setMaximum(100);
            progressBar.setStringPainted(true);
            progressBar.setForeground(Color.CYAN);
            GridBagConstraints gbc_progressBar = new GridBagConstraints();
            gbc_progressBar.insets = new Insets(0, 0, 5, 0);
            gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
            gbc_progressBar.gridx = 1;
            gbc_progressBar.gridy = 3;
            contentPanel.add(progressBar, gbc_progressBar);
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
                    JLabel lblCmd = new JLabel("Cmd");
                    GridBagConstraints gbc_lblCmd = new GridBagConstraints();
                    gbc_lblCmd.insets = new Insets(0, 0, 5, 5);
                    gbc_lblCmd.gridx = 0;
                    gbc_lblCmd.gridy = 1;
                    panelBasic.add(lblCmd, gbc_lblCmd);
                }
                {
                    comboBoxCommand = new JComboBox<String>();
                    GridBagConstraints gbc_comboBoxCommand = new GridBagConstraints();
                    gbc_comboBoxCommand.insets = new Insets(0, 0, 5, 5);
                    gbc_comboBoxCommand.fill = GridBagConstraints.HORIZONTAL;
                    gbc_comboBoxCommand.gridx = 1;
                    gbc_comboBoxCommand.gridy = 1;
                    panelBasic.add(comboBoxCommand, gbc_comboBoxCommand);
                }
                {
                    JButton btnSendCommand = new JButton("Send Command");
                    GridBagConstraints gbc_btnSendCommand = new GridBagConstraints();
                    gbc_btnSendCommand.fill = GridBagConstraints.HORIZONTAL;
                    gbc_btnSendCommand.insets = new Insets(0, 0, 5, 0);
                    gbc_btnSendCommand.gridx = 2;
                    gbc_btnSendCommand.gridy = 1;
                    panelBasic.add(btnSendCommand, gbc_btnSendCommand);

                    btnSendCommand.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent arg0) {
                            String ip = comboBoxIP.getSelectedItem().toString();
                            String cmd = comboBoxCommand.getSelectedItem().toString();

                            if (btnSendCommand.isEnabled() && 1 == arg0.getButton()) {
                                CNC cncCtrl = getControl();
                                if (null != cncCtrl) {
                                    boolean ok = true;
                                    boolean cmdExist = true;
                                    Object result = "";
                                    btnSendCommand.setEnabled(false);

                                    switch (cmd) {
                                        case "openDoor":
                                            ok = cncCtrl.openDoor(ip);
                                            break;
                                        case "closeDoor":
                                            ok = cncCtrl.closeDoor(ip);
                                            break;
                                        case "clampFixture":
                                            ok = cncCtrl.clampFixture(ip, 1);
                                            break;
                                        case "getMachineState":
                                            result = cncCtrl.getMachineState(ip);
                                            ok = (result != null) ? true : false;
                                            break;
                                        case "getAlarmInfo":
                                            result = cncCtrl.getAlarmInfo(ip);
                                            ok = (result != null) ? true : false;
                                            break;
                                        case "getToolLife":
                                            result = cncCtrl.getToolLife(ip);
                                            ok = (result != null) ? true : false;
                                            break;
                                        case "getMachiningParas":
                                            result = cncCtrl.getMachiningParas(ip);
                                            ok = (result != null) ? true : false;
                                            break;
                                        case "getMachiningCounter":
                                            result = cncCtrl.getMachiningCounter(ip);
                                            ok = (result != null) ? true : false;
                                            break;
                                        case "startMachining":
                                            result = cncCtrl.startMachining(ip, "");
                                            ok = (result != null) ? true : false;
                                            break;
                                        case "pauseMachining":
                                            result = cncCtrl.pauseMachining(ip);
                                            ok = (result != null) ? true : false;
                                            break;
                                        case "resumeMachining":
                                            result = cncCtrl.resumeMachining(ip);
                                            ok = (result != null) ? true : false;
                                            break;
                                        case "uploadMainProgram":
                                            ok = cncCtrl.uploadMainProgram(ip, "", "");
                                            break;
                                        case "downloadMainProgram":
                                            result = cncCtrl.downloadMainProgram(ip, "");
                                            ok = (result != null) ? true : false;
                                            break;
                                        case "deleteMainProgram":
                                            ok = cncCtrl.deleteMainProgram(ip, "");
                                            break;
                                        case "uploadSubProgram":
                                            ok = cncCtrl.uploadSubProgram(ip, "d05", "D:\\NC_Programs\\remote\\50891441.nc");
                                            break;
                                        case "downloadSubProgram":
                                            result = cncCtrl.downloadSubProgram(ip, "d03.nc");
                                            ok = (result != null) ? true : false;
                                            break;
                                        case "deleteSubProgram":
                                            ok = cncCtrl.deleteSubProgram(ip, "");
                                            break;
                                        case "hfOpenDoor":
                                            ok = cncCtrl.hfOpenDoor(ip);
                                            break;
                                        case "hfCloseDoor":
                                            ok = cncCtrl.hfCloseDoor(ip);
                                            break;
                                        case "hfClampFixture":
                                            ok = cncCtrl.hfClampFixture(ip, 1);
                                            break;
                                        case "hfReleaseFixture":
                                            ok = cncCtrl.hfReleaseFixture(ip, 5);
                                            break;
                                        case "hfResetDoor":
                                            ok = cncCtrl.hfResetDoor(ip);
                                            break;
                                        case "hfHomeDoor":
                                            ok = cncCtrl.hfHomeDoor(ip);
                                            break;
                                        case "hfRotate":
                                            ok = cncCtrl.hfRotate(ip, 90);
                                            break;
                                        case "fiveAspectProceed":
                                            ok = cncCtrl.hfFiveAspectProcess(ip);
                                            break;
                                        case "uploadSubProgramByBarcode":
											ok = cncCtrl.getAndUploadProgramByBarcode(ip,"F631A","d20240110.nc");
											break;
                                        case "checkSensor":
                                            ok = cncCtrl.checkSensor(ip,"3304","1");
                                            break;
                                        case "getMacro":
                                            double res = cncCtrl.getMacro(ip,900);
                                            System.out.println("macro is " + res);
                                            break;
                                        default:
                                            cmdExist = false;
                                            JOptionPane.showMessageDialog(cncController.getContentPane(), "Command[" + cmd + "] is not supported", "Command ERROR", JOptionPane.ERROR_MESSAGE);
                                            break;
                                    }
                                    btnSendCommand.setEnabled(true);

                                    if (cmdExist) {
                                        if (ok) {
                                            JOptionPane.showMessageDialog(cncController.getContentPane(), "Command[" + cmd + "] execution OK\r\n" + result.toString(), "Command execution OK", JOptionPane.INFORMATION_MESSAGE);
                                        } else {
                                            JOptionPane.showMessageDialog(cncController.getContentPane(), "Command[" + cmd + "] execution failed", "Command Failed", JOptionPane.ERROR_MESSAGE);
                                        }
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(cncController.getContentPane(), "Load machine driver failed", "Driver ERROR", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    });
                    btnSendCommand.setActionCommand("Send Command");
                    getRootPane().setDefaultButton(btnSendCommand);
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
                btnLock2.setEnabled(enabled);
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
                btnLock2.setEnabled(!lockFlags[1]);
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

        tmp = txtZone2Barcode.getText().trim();
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

    private String lockFixtures() {
        String errMsg = "";
        CNC cncCtrl = getControl();

        if (null != cncCtrl) {
            String ip = comboBoxIP.getSelectedItem().toString();
            String ids = txtZone1Barcode.getText().trim();
            ids += ";" + txtZone2Barcode.getText().trim();
            ids += ";" + txtZone3Barcode.getText().trim();
            ids += ";" + txtZone4Barcode.getText().trim();
            ids += ";" + txtZone5Barcode.getText().trim();
            ids += ";" + txtZone6Barcode.getText().trim();
            String[] zns = ids.split(";");

            for (int i = 0; i < zns.length; i++) {
                if ("".equals(zns[i])) {
                    lockFlags[i] = false;
                    continue;
                }
                if (cncCtrl.clampFixture(ip, i + 1)) {
                    lockFlags[i] = true;
                } else {
                    lockFlags[i] = false;
                    errMsg += "\r\nLock fixture#" + (i + 1) + " failed";
                }
            }
        } else {
            errMsg = "Get controller failed";
        }

        if (!chckbxAutoMode.isSelected()) setButtonsEnabled(true, false);
        return errMsg;
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

    private String closeDoor() {
        String errMsg = "";
        CNC cncCtrl = getControl();
        if (null != cncCtrl) {
            String ip = comboBoxIP.getSelectedItem().toString();
            if (cncCtrl.closeDoor(ip)) {
                doorClosedFlag = true;
            } else {
                doorClosedFlag = false;
                errMsg = "Close door failed";
            }
        } else {
            errMsg = "Get controller failed";
        }
        if (!chckbxAutoMode.isSelected()) setButtonsEnabled(true, false);
        return errMsg;
    }

    private String uploadNCProgram() {
        CncData cncData = CncData.getInstance();
        CncDriver cncDriver = CncDriver.getInstance();
        WorkpieceData wpData = WorkpieceData.getInstance();

        //sequence of the workpieceIDs must be from 1 to 6
        String workpieceIDs = txtZone1Barcode.getText().trim();
        workpieceIDs += ";" + txtZone2Barcode.getText().trim();
        workpieceIDs += ";" + txtZone3Barcode.getText().trim();
        workpieceIDs += ";" + txtZone4Barcode.getText().trim();
        workpieceIDs += ";" + txtZone5Barcode.getText().trim();
        workpieceIDs += ";" + txtZone6Barcode.getText().trim();

        String cncIP = comboBoxIP.getSelectedItem().toString();
        String cncModel = (String) cncData.getData(cncIP).get(CncItems.MODEL);
        String lineName = (String) cncData.getLineName(cncIP);
        cncData.setData(cncIP, CncItems.D_TASKID, "");

        //Can't be missed - memory initialization
        cncData.clearWorkpieceID(cncIP);
        wpData.clearData();
        for (int i = 0; i < ncUploadFlags.length; i++) {
            ncUploadFlags[i] = false;
        }

        String[] ids = workpieceIDs.split(";");
        String errMsg = "", zones = "";
        workpieceIDs = "";
        for (int i = 0; i < ids.length; i++) {
            if ("".equals(ids[i])) continue;
            wpData.setData(ids[i], WorkpieceItems.ID, ids[i]);
            DataUtils.updateWorkpieceData(ids[i], lineName, "3", "" + (i + 1));
            if (wpData.canMachineByCNC(ids[i], cncModel, null)) {
                if ("".equals(workpieceIDs)) {
                    workpieceIDs = ids[i];
                    zones = "" + (i + 1);
                } else {
                    workpieceIDs += ";" + ids[i];
                    zones += ";" + (i + 1);
                }
                //Remember the working zone for each workpiece
                cncData.setWorkpieceID(cncIP, i + 1, ids[i]);
                cncData.setSpecNo(cncIP, i + 1, wpData.getSpecNo(ids[i]));
                cncData.setZoneSimulationTime(cncIP, i + 1, wpData.getNextProcSimtime(ids[i], cncModel, null));
            } else {
                errMsg += "\r\n" + ids[i] + "@Zone#" + (i + 1) + " can't be machined by " + cncModel;
            }
        }

        if (!"".equals(errMsg)) {
            if (0 != JOptionPane.showConfirmDialog(contentPanel, errMsg + "\r\nWill you skip these workpieces?", "Skip Workpiece", JOptionPane.YES_NO_OPTION)) {
                return errMsg;
            } else {
                errMsg = "";
            }
        }

        if (!"".equals(workpieceIDs)) {
            String cncDrvName = (String) cncDriver.getData(cncModel).get(DriverItems.DRIVER);
            String cncDataHandler = (String) cncDriver.getData(cncModel).get(DriverItems.DATAHANDLER);
            CNC cncCtrl = CncFactory.getInstance(cncDrvName, cncDataHandler, cncModel);

            if (null != cncCtrl) {
                cncData.setData(cncIP, CncItems.CTRL, cncCtrl);
                if (UploadNCProgram.uploadSubPrograms(cncCtrl, cncIP, cncData.getWorkpieceIDs(cncIP))) {
                    if (!UploadNCProgram.uploadMainProgram(cncCtrl, cncIP, cncData.getWorkpieceIDs(cncIP))) {
                        errMsg += "\r\nUpload main program failed";
                    } else {
                        String[] zns = zones.split(";");
                        String[] subProgs = ("" + cncData.getData(cncIP).get(CncItems.SUBPROGRAMS)).split(";");
                        int idx = -1;
                        for (String zn : zns) {
                            idx++;
                            ncUploadFlags[Integer.valueOf(zn) - 1] = true;
                            cncData.setNCProgram(cncIP, Integer.valueOf(zn), subProgs[idx]);
                        }

                        //Refresh workpiece data
                        ids = workpieceIDs.split(";");
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
                } else {
                    errMsg += "\r\nUpload sub programs failed";
                }
                if ("".equals(errMsg)) {//Upload NC Program OK
                    cncData.setData(cncIP, CncItems.CTRL, cncCtrl);
                } else {
                    cncData.setData(cncIP, CncItems.CTRL, null);
                }
            } else {
                errMsg += "\r\nGet CNC driver failed";
            }
        } else {
            errMsg += "\r\nAll workpieces can't be machined by " + cncModel;
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

    private String doMachining() {
        String errMsg = "";
        CNC cncCtrl = getControl();
        if (null != cncCtrl) {
            boolean autoMode = false;
            String ip = comboBoxIP.getSelectedItem().toString();
            autoMode = chckbxAutoMode.isSelected();
            CncWebAPI cncWebAPI = CncWebAPI.getInstance();
            CncData cncData = CncData.getInstance();
            String mainProgramName = cncWebAPI.getMainProgramName(cncData.getCncModel(ip));
            if (DeviceState.STANDBY != cncData.getCncState(ip)) {
                if (0 == JOptionPane.showConfirmDialog(contentPanel, "Machine state is not STANDBY, would you like to set it STANDBY and continue?", "Set STANDBY?", JOptionPane.YES_NO_OPTION)) {
                    cncData.setData(ip, CncItems.STATE, DeviceState.STANDBY);
                } else {
                    return "Machine state is " + cncData.getCncState(ip) + ", not ready yet for machining";
                }
            }

            if (autoMode) {
                cncData.clearMachiningData(ip);
                errMsg = lockFixtures();
                if (!"".equals(errMsg)) return errMsg;
                errMsg = uploadNCProgram();
                if (!"".equals(errMsg)) return errMsg;
                errMsg = closeDoor();
                if (!"".equals(errMsg)) return errMsg;
            } else {
                String zns = cncData.getNotEmptyWorkzones(ip);
                if ("".equals(zns)) {
                    zns = getNotBlankZones();
                    if ("".equals(zns)) return "No valid workpieces in the machine";
                }

                String[] zones = zns.split(";");
                boolean bNeedUploadNcProgram = false;
                for (String zone : zones) {
                    if (!lockFlags[Integer.valueOf(zone) - 1]) {
                        if (!lockFlags[Integer.valueOf(zone) - 1]) {
                            errMsg = lockSingleFixture(Integer.valueOf(zone));
                            if (!"".equals(errMsg)) return errMsg;
                            lockFlags[Integer.valueOf(zone) - 1] = true;
                        }
                    }
                    if (!ncUploadFlags[Integer.valueOf(zone) - 1]) bNeedUploadNcProgram = true;
                }
                if (bNeedUploadNcProgram) {
                    errMsg = uploadNCProgram();
                    if (!"".equals(errMsg)) return errMsg;
                }

                if (0 != JOptionPane.showConfirmDialog(contentPanel, "Have all fixtures locked steadily and all NC programs uploaded correctly?", "Ready To Start Machining?", JOptionPane.YES_NO_OPTION)) {
                    return "Please check and start again once everything is ready";
                }

                if (!doorClosedFlag) {
                    errMsg = closeDoor();
                    if (!"".equals(errMsg)) return errMsg;
                }
            }
            if (startMachining(cncCtrl, ip, mainProgramName, 3, 10)) {
                setMachiningProgress(0);
                cncData.setCncLastState(ip, DeviceState.LOCK);
                cncData.setData(ip, CncItems.STATE, DeviceState.WORKING);
                cncData.setData(ip, CncItems.DT_DATE, TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
                ThreadUtils.Run(new CncMonitoring(ip));
                setButtonsEnabled(false, true);
            } else {
                errMsg = "Start machining failed";
            }
        } else {
            errMsg = "Get controller failed";
        }
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

    private String unlockSingleFixture(int zoneNo) {
        String errMsg = "";
        if (zoneNo < 1 || zoneNo > 6) {
            errMsg = "Invalice Zone#" + zoneNo;
        } else {
            CNC cncCtrl = getControl();
            if (null != cncCtrl) {
                String ip = comboBoxIP.getSelectedItem().toString();
                if (cncCtrl.releaseFixture(ip, zoneNo)) {
                    lockFlags[zoneNo - 1] = false;
                } else {
                    lockFlags[zoneNo - 1] = true;
                    errMsg = "Unlock fixture#" + zoneNo + " failed";
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
                    strTemp = cncController.getTitle();
                    if (null != strTemp) {
                        cncController.setTitle(strTemp.split("##")[0] + "##" + TimeUtils.getCurrentDate("HH:mm:ss"));
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
