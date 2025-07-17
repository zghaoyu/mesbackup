package com.cncmes.gui;

import com.cncmes.drv.AtcDrv;
import com.cncmes.drv.CncHartFordDrv;
import com.cncmes.dto.ATC;
import com.cncmes.dto.Result;
import com.cncmes.service.AtcService;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * *Zhong
 * *
 */
public class AtcCommandInterface extends JDialog {
    private JComboBox<String> comboBoxAtcIP;
    private JComboBox<String> comboBoxCommand;

    private static final long serialVersionUID = 18L;
    private final JPanel contentPanel = new JPanel();
    private static AtcCommandInterface atcCommandInterface = new AtcCommandInterface();
    private AtcService atcService = new AtcService();
    private AtcDrv atcDrv = new AtcDrv();
    private CncHartFordDrv cncDrv = new CncHartFordDrv();
    private static String[] atcIPArray = new String[]{"10.10.206.250"};
    private String[] commandArray = new String[]{"Load tool raise arm","Load tool lower arm","Unload tool lower arm","Unload tool raise arm","Load tool","Unload tool","Check CNC Sensor","Check ATC tool status","Start CNC machining","Get CNC status","Test"};
    public static AtcCommandInterface getInstance(){
        return atcCommandInterface;
    }
    String selectIP = "";
    private AtcCommandInterface() {
        setModal(true);
        setIconImage(Toolkit.getDefaultToolkit().getImage(AtcCommandInterface.class.getResource("/com/cncmes/img/robots_24.png")));
        setTitle("ATC Command Interface");
        setResizable(false);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int width = 480;
        int height = 420;
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

        List<String> atcIPList = new ArrayList<>();
        for(ATC atc : atcService.getAllATC())
        {
            atcIPList.add(atc.getIp());
//            atcIPList.add(atc.getCnc_ip());
        }

        atcIPArray = atcIPList.toArray(new String[atcIPList.size()]);
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
                JLabel lblIp_1 = new JLabel("I     P");
                GridBagConstraints gbc_lblIp_1 = new GridBagConstraints();
                gbc_lblIp_1.insets = new Insets(0, 0, 5, 5);
                gbc_lblIp_1.gridx = 0;
                gbc_lblIp_1.gridy = 0;
                panelBasic.add(lblIp_1, gbc_lblIp_1);
            }
            {
                comboBoxAtcIP = new JComboBox<String>();
                GridBagConstraints gbc_comboBoxIP = new GridBagConstraints();
                gbc_comboBoxIP.fill = GridBagConstraints.HORIZONTAL;
                gbc_comboBoxIP.insets = new Insets(0, 0, 5, 5);
                gbc_comboBoxIP.gridx = 1;
                gbc_comboBoxIP.gridy = 0;
                panelBasic.add(comboBoxAtcIP, gbc_comboBoxIP);
                comboBoxAtcIP.setModel(new DefaultComboBoxModel<String>(atcIPArray));
                selectIP = atcIPList.get(0);
                comboBoxAtcIP.addItemListener(e -> {
                    if (e.getStateChange() == ItemEvent.SELECTED){
                        selectIP = (String) comboBoxAtcIP.getSelectedItem();
                    }
                });
            }
            {
                JLabel lblCmd = new JLabel("C M D");
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
                comboBoxCommand.setModel(new DefaultComboBoxModel<String>(commandArray));
            }
            {
                JButton btnSendCommand = new JButton("Send Command");
                GridBagConstraints gbc_btnSendCommand = new GridBagConstraints();
                gbc_btnSendCommand.fill = GridBagConstraints.HORIZONTAL;
                gbc_btnSendCommand.insets = new Insets(0, 0, 5, 0);
                gbc_btnSendCommand.gridx = 2;
                gbc_btnSendCommand.gridy = 1;
                panelBasic.add(btnSendCommand, gbc_btnSendCommand);

                btnSendCommand.addMouseListener(new MouseAdapter(){
                    @Override
                    public void mouseClicked(MouseEvent arg0) {
                        String atcIP = comboBoxAtcIP.getSelectedItem().toString();
                        String cmd = comboBoxCommand.getSelectedItem().toString();
                        if (btnSendCommand.isEnabled() && 1 == arg0.getButton()){
                            boolean ok = true;
                            boolean cmdExist = true;
                            btnSendCommand.setEnabled(false);
                            Object result = "";
                            switch (cmd){
                                case "Load tool raise arm":
                                    result = "raise arm [Load]";
                                    atcDrv.loadActionUp(selectIP,502,1);
                                    break;
                                case "Load tool lower arm":
                                    result = "lower arm [load]";
                                    atcDrv.loadActionDown(selectIP,502);
                                    break;
                                case "Unload tool lower arm":
                                    atcDrv.unloadActionDown(selectIP,502,1);
                                    result = "lower arm [Unload]";
                                    break;
                                case "Unload tool raise arm":
                                    atcDrv.unloadActionUp(selectIP,502);
                                    result = "raise arm [Unload]";
                                    break;
                                case "Check CNC Sensor":
                                    boolean rs = cncDrv.checkSensor("10.10.206.178","3304", "1");
                                    result = rs;
                                    break;
                                case "Check ATC tool status":
                                    Result result1 =  atcDrv.checkToolStatus(atcIP,502,2);
                                    result = result1.getErrorCasuse();
                                    break;
                                case "Start CNC machining":
                                    result = cncDrv.startMachining("10.10.206.178",8193,"");
                                    break;
                                case "Get CNC status":
                                    result = cncDrv.getMachineState("10.10.206.73",8193);
                                    break;
                                default:
                                    cmdExist = false;
                                    JOptionPane.showMessageDialog(atcCommandInterface.getContentPane(), "Command[" + cmd + "] is not supported", "Command ERROR", JOptionPane.ERROR_MESSAGE);
                                    break;
                            }
                            btnSendCommand.setEnabled(true);
                            if (cmdExist) {
                                if (ok) {
                                    JOptionPane.showMessageDialog(atcCommandInterface.getContentPane(), "Command[" + cmd + "] execution OK\r\n" + "result is " + result.toString(), "Command execution OK", JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(atcCommandInterface.getContentPane(), "Command[" + cmd + "] execution failed", "Command Failed", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }
}
