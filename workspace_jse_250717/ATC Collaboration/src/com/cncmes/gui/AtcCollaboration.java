package com.cncmes.gui;

//import java.awt.*;
//import javax.swing.*;
//import java.awt.event.*;

import com.cncmes.base.PermissionItems;
import com.cncmes.ctrl.AtcRunServer;
import com.cncmes.drv.CncHartFordDrv;
import com.cncmes.dto.ATC;
import com.cncmes.service.AtcService;
import com.cncmes.thread.ThreadController;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.LoginSystem;
import com.cncmes.utils.RunningMsgUtil;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * *Zhong
 * *
 */
public class AtcCollaboration extends JFrame {
    private JPanel contentPane;
    private JButton btnLogin;
    private JButton btnConfig;
    private JButton btnStart;
    private JButton btnStop;
    private JButton btnAtcCommand;

    private static JTextArea msgArea = new JTextArea(24,60);

    public static JTextArea getMsgArea() {
        return msgArea;
    }

    public static void setMsgArea(JTextArea msgArea) {
        AtcCollaboration.msgArea = msgArea;
    }

    private JComboBox<String> comboBox_ATC;

    private static String[] atcIPArray = new String[]{"10.10.10.1"};
    private static String atcIP = "";
    private static AtcCollaboration atcCollaboration = new AtcCollaboration();
    public static AtcCollaboration getInstance(){
        return atcCollaboration;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    AtcCollaboration frame = AtcCollaboration.getInstance();
                    frame.setVisible(true);
                    ThreadController.stopAtcMonitor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private AtcCollaboration(){
        AtcService atcService = new AtcService();
        List<String> atcIPList = new ArrayList<>();
        for(ATC atc : atcService.getAllATC())
        {
            atcIPList.add(atc.getIp());
        }
        atcIPArray = atcIPList.toArray(new String[atcIPList.size()]);
        setIconImage(Toolkit.getDefaultToolkit().getImage(AtcCollaboration.class.getResource("/com/cncmes/img/Butterfly_pink_24.png")));
        setTitle("ATC Collaboration Module");
//        setResizable(false);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int width = 800;
        int height = 500;
        setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                int rtn = JOptionPane.showConfirmDialog(atcCollaboration.getContentPane(), "Are you sure of quiting from ATC module", "Exit", JOptionPane.YES_NO_OPTION);
                if(0 == rtn){
                    atcCollaboration.dispose();
                }
            }
        });
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        contentPane.add(toolBar, BorderLayout.NORTH);

        btnLogin = new JButton("");
        btnLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                if(btnLogin.isEnabled() && 1 == arg0.getButton()){
                    if(LoginSystem.userHasLoginned()){
                        if(0==JOptionPane.showConfirmDialog(atcCollaboration, "Are you sure of logging out now?", "Log Out?", JOptionPane.YES_NO_OPTION)){
                            LoginSystem.userLogout();
                            refreshButtonsEnabled();
                        }
                    }else{
                        UserLogin dialog = UserLogin.getInstance();
                        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                        dialog.setVisible(true);
                        refreshButtonsEnabled();
                    }
                }
            }
        });
        btnLogin.setToolTipText("Login system");
        btnLogin.setBorderPainted(false);
        btnLogin.setIcon(new ImageIcon(AtcCollaboration.class.getResource("/com/cncmes/img/login_24.png")));



        toolBar.add(btnLogin);


//        btnConfig = new JButton("");
//        btnConfig.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent arg0) {
//                if(btnConfig.isEnabled() && 1 == arg0.getButton()){
//                    try {
//                        SysConfig dialog = SysConfig.getInstance();
//                        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//                        dialog.setVisible(true);
//                    } catch (Exception e1) {
//                        e1.printStackTrace();
//                    }
//                }
//            }
//        });
//        btnConfig.setToolTipText("Communication config");
//        btnConfig.setBorderPainted(false);
//        btnConfig.setIcon(new ImageIcon(Scheduler.class.getResource("/com/cncmes/img/setting_24.png")));
//        toolBar.add(btnConfig);

        btnStart = new JButton("");
        btnStart.setEnabled(false);
        btnStart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {

                if(1 == arg0.getButton() && btnStart.isEnabled()){
                    String msg = AtcRunServer.getInstance().start();
                    if("OK".equals(msg)){
//                        for(int i=0; i<10; i++){
//                            refreshGUI(i);
//                        }
                        msgArea.append(RunningMsgUtil.runningMsgFormat("ATC Collaboration have started"));

                        btnLogin.setEnabled(false);
                        btnStart.setEnabled(false);
                        btnStop.setEnabled(true);
                    }else{
                        JOptionPane.showMessageDialog(contentPane, msg, "Scheduler Launch Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        btnStart.setToolTipText("Run ATC");
        btnStart.setBorderPainted(false);
        btnStart.setIcon(new ImageIcon(AtcCollaboration.class.getResource("/com/cncmes/img/start_24.png")));
        toolBar.add(btnStart);


        btnStop = new JButton("");
        btnStop.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                if(1 == arg0.getButton() && btnStop.isEnabled()){
                    stopSystem();
                }
            }
        });
        btnStop.setEnabled(false);
        btnStop.setToolTipText("Stop ATC");
        btnStop.setBorderPainted(false);
        btnStop.setIcon(new ImageIcon(AtcCollaboration.class.getResource("/com/cncmes/img/stop_24.png")));
        toolBar.add(btnStop);


        btnAtcCommand = new JButton("");
        btnAtcCommand.setEnabled(false);
        btnAtcCommand.setToolTipText("ATC Command Interface");
        btnAtcCommand.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(1 == e.getButton() && btnAtcCommand.isEnabled()){
                    AtcCommandInterface atcCommandInterface = AtcCommandInterface.getInstance();
                    atcCommandInterface.setVisible(true);
                }
            }
        });
        btnAtcCommand.setBorderPainted(false);
        btnAtcCommand.setIcon(new ImageIcon(AtcCollaboration.class.getResource("/com/cncmes/img/robots_24.png")));
        toolBar.add(btnAtcCommand);


        JButton btnExit = new JButton("");
        btnExit.setToolTipText("Exit from system");
        btnExit.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(1 == e.getButton()){
                    exitSystem(false);
                }
            }
        });
        btnExit.setBorderPainted(false);
        btnExit.setIcon(new ImageIcon(AtcCollaboration.class.getResource("/com/cncmes/img/Exit_24.png")));
        toolBar.add(btnExit);


//        JPanel selectPanel = new JPanel();
//        contentPane.add(selectPanel,BorderLayout.NORTH);

        comboBox_ATC = new JComboBox<>();
        comboBox_ATC.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                atcIP = comboBox_ATC.getSelectedItem().toString();
            }
        });
        comboBox_ATC.setModel(new DefaultComboBoxModel<String>(atcIPArray));
//        GridBagConstraints gbc_comboBox_ATC = new GridBagConstraints();
//        gbc_comboBox_ATC.fill = GridBagConstraints.HORIZONTAL;
//        gbc_comboBox_ATC.insets = new Insets(0, 0, 0, 5);
//        gbc_comboBox_ATC.gridx = 2;
//        gbc_comboBox_ATC.gridy = 0;
        contentPane.add(comboBox_ATC ,BorderLayout.EAST);




        JPanel panelMsg = new JPanel();
        panelMsg.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
        panelMsg.setSize(200,200);
        contentPane.add(panelMsg, BorderLayout.CENTER);

        JScrollPane msgScroll = new JScrollPane();
//        msgScroll.setBounds(100,100,100,100);

//        GridBagLayout gbl_panelMsg = new GridBagLayout();
//        gbl_panelMsg.columnWidths = new int[] {200};
//        gbl_panelMsg.rowHeights = new int[] {30, 520};
//        gbl_panelMsg.columnWeights = new double[]{1.0};
//        gbl_panelMsg.rowWeights = new double[]{0.0, 1.0};
//        panelMsg.setLayout(gbl_panelMsg);


//        msgArea = new JTextArea(24,60);
        msgArea.setLineWrap(true);
        msgScroll.setViewportView(msgArea);
        panelMsg.add(msgScroll);
    }

    private void stopSystem() {
        String msg = "OK";
        ThreadController.stopCncMonitor();
        ThreadController.stopAtcMonitor();
        if("OK".equals(msg))
        {
            msgArea.append(RunningMsgUtil.runningMsgFormat("ATC Collaboration end!"));
            btnStop.setEnabled(false);
            btnStart.setEnabled(true);
        }else {
            msgArea.append(RunningMsgUtil.runningMsgFormat("Can't Stop the ATC Collaboration!"));
        }

//        msg = MySystemUtils.sysReadyToStop();
//        if(!"OK".equals(msg)){
//            JOptionPane.showMessageDialog(contentPane, msg, "Can't Stop the Scheduler Now", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        msg = AtcRunServer.getInstance().stop();
//        if("OK".equals(msg)){
//            btnStart.setEnabled(true);
//        }else{
//            JOptionPane.showMessageDialog(contentPane, msg, "Can't Stop the Scheduler Now", JOptionPane.ERROR_MESSAGE);
//        }
    }

    private void exitSystem(boolean force){
        int rtn = JOptionPane.showConfirmDialog(atcCollaboration.getContentPane(), "Are you sure of quiting from ATC module", "Exit", JOptionPane.YES_NO_OPTION);
        if(0 == rtn){
            System.exit(0);
        }
    }

    public void refreshButtonsEnabled(){
//        btnConfig.setEnabled(!LoginSystem.accessDenied(PermissionItems.SYSCONFIG));
        btnStart.setEnabled(!LoginSystem.accessDenied(PermissionItems.SCHEDULER));
        btnAtcCommand.setEnabled(!LoginSystem.accessDenied(PermissionItems.SCHEDULER));
        setTitle("");
        if(LoginSystem.userHasLoginned()){
            btnLogin.setIcon(new ImageIcon(AtcCollaboration.class.getResource("/com/cncmes/img/logout_24.png")));
            btnLogin.setToolTipText("Logout System");
        }else{
            btnLogin.setIcon(new ImageIcon(AtcCollaboration.class.getResource("/com/cncmes/img/login_24.png")));
            btnLogin.setToolTipText("Login System");
        }
    }

}
