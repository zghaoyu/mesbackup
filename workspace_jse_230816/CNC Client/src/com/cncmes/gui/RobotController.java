package com.cncmes.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.cncmes.base.CncItems;
import com.cncmes.base.DriverItems;
import com.cncmes.base.Robot;
import com.cncmes.base.RobotItems;
import com.cncmes.ctrl.RobotFactory;
import com.cncmes.data.CncData;
import com.cncmes.data.RobotData;
import com.cncmes.data.RobotDriver;
import com.cncmes.data.RobotEthernetCmd;
import com.cncmes.utils.GUIUtils;
import com.cncmes.utils.XmlUtils;

public class RobotController extends JDialog {
	private JComboBox<String> comboBoxIP;
	private JComboBox<String> comboBoxCommand;
	private JComboBox<String> comboBoxMachineIP;
	private JComboBox<String> comboBoxRackID;
	private JTextField textField_Info;
	private JLabel lblMessage;
	
	private static final long serialVersionUID = 19L;
	private final JPanel contentPanel = new JPanel();
	private static RobotData robotData = RobotData.getInstance();
	private static CncData cncData = CncData.getInstance();
	private static RobotController robotController = new RobotController();
	private JTextField txtTargetSlot;
	private JTextField txtRobotSlot;
	public static RobotController getInstance(){
		return robotController;
	}
	
	private String[] getCommandList(String mainKey){
		String[] cmds = RobotEthernetCmd.getInstance().getAllOperations(mainKey);
		return cmds;
	}
	
	private String[] getIPList(){
		String[] ips = null;
		String tmp = "";
		
		Map<String, LinkedHashMap<RobotItems, Object>> dtMap = robotData.getDataMap();
		for(String ip:dtMap.keySet()){
			if("".equals(tmp)){
				tmp = ip;
			}else{
				tmp += "," + ip;
			}
		}
		
		if(!"".equals(tmp)){
			ips = tmp.split(",");
		}else{
			ips = new String[]{" "};
		}
		
		return ips;
	}
	
	private String[] getCncIPList(){
		String[] ips = null;
		String tmp = "";
		
		Map<String, LinkedHashMap<CncItems, Object>> dt = cncData.getDataMap();
		for(String ip:dt.keySet()){
			if("".equals(tmp)){
				tmp = ip;
			}else{
				tmp += "," + ip;
			}
		}
		
		if(!"".equals(tmp)){
			ips = tmp.split(",");
		}else{
			ips = new String[]{" "};
		}
		
		System.out.println(tmp);
		return ips;
	}
	
	private void setRobotInfo() {
		String deviceIP = comboBoxIP.getSelectedItem().toString();
		String cncIP = comboBoxMachineIP.getSelectedItem().toString();
		String info = "Unkown device";
		String robotModel = "", cncModel = "";
		if(null != robotData.getData(deviceIP)){
			robotModel = (String) robotData.getData(deviceIP).get(RobotItems.MODEL);
			info = "" + robotModel;
			info += " / " + robotData.getData(deviceIP).get(RobotItems.PORT);
			cncModel = cncData.getCncModel(cncIP);
		}
		textField_Info.setText(info);
		GUIUtils.setComboBoxValues(comboBoxCommand, getCommandList(robotModel + "#" + cncModel));
	}

	/**
	 * Create the dialog.
	 */
	private RobotController() {
		setModal(true);
		XmlUtils.parseRobotEthernetCmdXml();
		setIconImage(Toolkit.getDefaultToolkit().getImage(RobotController.class.getResource("/com/cncmes/img/Butterfly_24.png")));
		setTitle("Robot Controller");
		setResizable(false);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 400;
		int height = 280;
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0, 0};
		gbl_contentPanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblIp = new JLabel("IP");
			GridBagConstraints gbc_lblIp = new GridBagConstraints();
			gbc_lblIp.insets = new Insets(0, 0, 5, 5);
			gbc_lblIp.anchor = GridBagConstraints.EAST;
			gbc_lblIp.gridx = 0;
			gbc_lblIp.gridy = 0;
			contentPanel.add(lblIp, gbc_lblIp);
		}
		{
			comboBoxIP = new JComboBox<String>();
			comboBoxIP.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent arg0) {
					setRobotInfo();
				}
			});
			comboBoxIP.setModel(new DefaultComboBoxModel<String>(getIPList()));
			GridBagConstraints gbc_comboBoxIP = new GridBagConstraints();
			gbc_comboBoxIP.insets = new Insets(0, 0, 5, 0);
			gbc_comboBoxIP.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBoxIP.gridx = 1;
			gbc_comboBoxIP.gridy = 0;
			contentPanel.add(comboBoxIP, gbc_comboBoxIP);
		}
		{
			JLabel lblInfo = new JLabel("Info");
			GridBagConstraints gbc_lblInfo = new GridBagConstraints();
			gbc_lblInfo.anchor = GridBagConstraints.EAST;
			gbc_lblInfo.insets = new Insets(0, 0, 5, 5);
			gbc_lblInfo.gridx = 0;
			gbc_lblInfo.gridy = 1;
			contentPanel.add(lblInfo, gbc_lblInfo);
		}
		{
			textField_Info = new JTextField();
			textField_Info.setEditable(false);
			textField_Info.setText("9100");
			GridBagConstraints gbc_textField_Info = new GridBagConstraints();
			gbc_textField_Info.insets = new Insets(0, 0, 5, 0);
			gbc_textField_Info.fill = GridBagConstraints.HORIZONTAL;
			gbc_textField_Info.gridx = 1;
			gbc_textField_Info.gridy = 1;
			contentPanel.add(textField_Info, gbc_textField_Info);
			textField_Info.setColumns(10);
		}
		{
			JLabel lblCommand = new JLabel("Command");
			GridBagConstraints gbc_lblCommand = new GridBagConstraints();
			gbc_lblCommand.anchor = GridBagConstraints.EAST;
			gbc_lblCommand.insets = new Insets(0, 0, 5, 5);
			gbc_lblCommand.gridx = 0;
			gbc_lblCommand.gridy = 2;
			contentPanel.add(lblCommand, gbc_lblCommand);
		}
		{
			comboBoxCommand = new JComboBox<String>();
			comboBoxCommand.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent arg0) {
					String cmd = comboBoxCommand.getSelectedItem().toString();
					if(cmd.endsWith("Rack")){
						comboBoxRackID.setEnabled(true);
						comboBoxMachineIP.setEnabled(false);
					}else if(cmd.endsWith("Machine")){
						comboBoxRackID.setEnabled(false);
						comboBoxMachineIP.setEnabled(true);
					}else{
						comboBoxRackID.setEnabled(false);
						comboBoxMachineIP.setEnabled(false);
					}
				}
			});
			GridBagConstraints gbc_comboBoxCommand = new GridBagConstraints();
			gbc_comboBoxCommand.insets = new Insets(0, 0, 5, 0);
			gbc_comboBoxCommand.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBoxCommand.gridx = 1;
			gbc_comboBoxCommand.gridy = 2;
			contentPanel.add(comboBoxCommand, gbc_comboBoxCommand);
		}
		{
			lblMessage = new JLabel("");
			GridBagConstraints gbc_lblMessage = new GridBagConstraints();
			gbc_lblMessage.insets = new Insets(0, 0, 5, 0);
			gbc_lblMessage.anchor = GridBagConstraints.WEST;
			gbc_lblMessage.gridx = 1;
			gbc_lblMessage.gridy = 3;
			contentPanel.add(lblMessage, gbc_lblMessage);
		}
		{
			JLabel lblMachineIP = new JLabel("Machine IP");
			GridBagConstraints gbc_lblMachineIP = new GridBagConstraints();
			gbc_lblMachineIP.anchor = GridBagConstraints.EAST;
			gbc_lblMachineIP.insets = new Insets(0, 0, 5, 5);
			gbc_lblMachineIP.gridx = 0;
			gbc_lblMachineIP.gridy = 4;
			contentPanel.add(lblMachineIP, gbc_lblMachineIP);
		}
		{
			comboBoxMachineIP = new JComboBox<String>();
			comboBoxMachineIP.setEnabled(false);
			comboBoxMachineIP.setModel(new DefaultComboBoxModel<String>(getCncIPList()));
			GridBagConstraints gbc_comboBoxMachineIP = new GridBagConstraints();
			gbc_comboBoxMachineIP.insets = new Insets(0, 0, 5, 0);
			gbc_comboBoxMachineIP.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBoxMachineIP.gridx = 1;
			gbc_comboBoxMachineIP.gridy = 4;
			contentPanel.add(comboBoxMachineIP, gbc_comboBoxMachineIP);
		}
		{
			JLabel lblRackId = new JLabel("Rack ID");
			GridBagConstraints gbc_lblRackId = new GridBagConstraints();
			gbc_lblRackId.anchor = GridBagConstraints.EAST;
			gbc_lblRackId.insets = new Insets(0, 0, 5, 5);
			gbc_lblRackId.gridx = 0;
			gbc_lblRackId.gridy = 5;
			contentPanel.add(lblRackId, gbc_lblRackId);
		}
		{
			comboBoxRackID = new JComboBox<String>();
			comboBoxRackID.setEnabled(false);
			comboBoxRackID.setModel(new DefaultComboBoxModel<String>(new String[] {"1", "2", "3", "4"}));
			GridBagConstraints gbc_comboBoxRackID = new GridBagConstraints();
			gbc_comboBoxRackID.insets = new Insets(0, 0, 5, 0);
			gbc_comboBoxRackID.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBoxRackID.gridx = 1;
			gbc_comboBoxRackID.gridy = 5;
			contentPanel.add(comboBoxRackID, gbc_comboBoxRackID);
		}
		{
			JLabel lblTargetSlot = new JLabel("Target Slot");
			GridBagConstraints gbc_lblTargetSlot = new GridBagConstraints();
			gbc_lblTargetSlot.anchor = GridBagConstraints.EAST;
			gbc_lblTargetSlot.insets = new Insets(0, 0, 5, 5);
			gbc_lblTargetSlot.gridx = 0;
			gbc_lblTargetSlot.gridy = 6;
			contentPanel.add(lblTargetSlot, gbc_lblTargetSlot);
		}
		{
			txtTargetSlot = new JTextField();
			txtTargetSlot.setText("1");
			GridBagConstraints gbc_txtTargetSlot = new GridBagConstraints();
			gbc_txtTargetSlot.insets = new Insets(0, 0, 5, 0);
			gbc_txtTargetSlot.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtTargetSlot.gridx = 1;
			gbc_txtTargetSlot.gridy = 6;
			contentPanel.add(txtTargetSlot, gbc_txtTargetSlot);
			txtTargetSlot.setColumns(10);
		}
		{
			JLabel lblRobotSlot = new JLabel("Robot Slot");
			GridBagConstraints gbc_lblRobotSlot = new GridBagConstraints();
			gbc_lblRobotSlot.anchor = GridBagConstraints.EAST;
			gbc_lblRobotSlot.insets = new Insets(0, 0, 0, 5);
			gbc_lblRobotSlot.gridx = 0;
			gbc_lblRobotSlot.gridy = 7;
			contentPanel.add(lblRobotSlot, gbc_lblRobotSlot);
		}
		{
			txtRobotSlot = new JTextField();
			txtRobotSlot.setText("1");
			GridBagConstraints gbc_txtRobotSlot = new GridBagConstraints();
			gbc_txtRobotSlot.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtRobotSlot.gridx = 1;
			gbc_txtRobotSlot.gridy = 7;
			contentPanel.add(txtRobotSlot, gbc_txtRobotSlot);
			txtRobotSlot.setColumns(10);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton btnSendCommand = new JButton("Send Command");
				btnSendCommand.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent arg0) {
						String ip = comboBoxIP.getSelectedItem().toString();
						String cmd = comboBoxCommand.getSelectedItem().toString();
						RobotData robotData = RobotData.getInstance();
						
						if(1 == arg0.getButton()){
							String robotModel = (String) robotData.getData(ip).get(RobotItems.MODEL);
							String cncIP = comboBoxMachineIP.getSelectedItem().toString();
							String targetName = cncData.getCncModel(cncIP);
							String rackID = comboBoxRackID.getSelectedItem().toString();
							String tgtSlot = txtTargetSlot.getText().trim();
							String robotSlot = txtRobotSlot.getText().trim();
							Robot robotCtrl = RobotFactory.getInstance((String)RobotDriver.getInstance().getData(robotModel).get(DriverItems.DRIVER));
							if(null != robotCtrl){
								boolean ok = true;
								boolean cmdExist = true;
								String result = null;
								switch(cmd){
								case "pickMaterialFromTray":
									ok = robotCtrl.pickMaterialFromTray(ip, -1, targetName);
									break;
								case "putMaterialOntoTray":
									ok = robotCtrl.putMaterialOntoTray(ip, -1, targetName);
									break;
								case "pickMaterialFromRack":
									ok = robotCtrl.pickMaterialFromRack(ip, rackID, tgtSlot, robotSlot, targetName);
									break;
								case "putMaterialOntoRack":
									ok = robotCtrl.putMaterialOntoRack(ip, rackID, tgtSlot, robotSlot, targetName);
									break;
								case "pickMaterialFromMachine":
									ok = robotCtrl.pickMaterialFromMachine(ip, cncIP, Integer.parseInt(tgtSlot), robotSlot, targetName);
									break;
								case "putMaterialOntoMachine":
									ok = robotCtrl.putMaterialOntoMachine(ip, cncIP, Integer.parseInt(tgtSlot), robotSlot, targetName);
									break;
								case "moveToMachine":
									ok = robotCtrl.moveToMachine(ip, cncIP, targetName);
									break;
								case "moveToRack":
									ok = robotCtrl.moveToRack(ip, rackID, targetName);
									break;
								case "getBattery":
									result = robotCtrl.getBattery(ip, targetName);
									ok = result!=null?true:false;
									break;
								case "goCharging":
									ok = robotCtrl.goCharging(ip, targetName);
									break;
								case "stopCharging":
									ok = robotCtrl.stopCharging(ip, targetName);
									break;
								case "scanBarcode":
									result = robotCtrl.scanBarcode(ip, targetName);
									ok = (result!=null)?true:false;
									break;
								default:
									cmdExist = false;
									JOptionPane.showMessageDialog(robotController.getContentPane(), "Command["+cmd+"] is not supported", "Command ERROR", JOptionPane.ERROR_MESSAGE);
									break;
								}
								
								if(cmdExist){
									if(ok){
										lblMessage.setText((null!=result?"rtn = "+result:"Cmd["+cmd+"] execution OK"));
									}else{
										JOptionPane.showMessageDialog(robotController.getContentPane(), "Command["+cmd+"] execution failed", "Command Failed", JOptionPane.ERROR_MESSAGE);
									}
								}
							}else{
								JOptionPane.showMessageDialog(robotController.getContentPane(), "Load robot driver failed", "Driver ERROR", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				});
				{
					JButton btnRefreshRobotList = new JButton("Refresh Robot List");
					btnRefreshRobotList.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if(1 == e.getButton()){
								btnRefreshRobotList.setEnabled(false);
								
								comboBoxIP.setModel(new DefaultComboBoxModel<String>(getIPList()));
								comboBoxIP.repaint();
								setRobotInfo();
								
								comboBoxMachineIP.setModel(new DefaultComboBoxModel<String>(getCncIPList()));
								comboBoxMachineIP.repaint();
								
								btnRefreshRobotList.setEnabled(true);
							}
						}
					});
					buttonPane.add(btnRefreshRobotList);
				}
				btnSendCommand.setActionCommand("Send Command");
				buttonPane.add(btnSendCommand);
				getRootPane().setDefaultButton(btnSendCommand);
			}
			{
				JButton btnCancel = new JButton("Cancel");
				btnCancel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent arg0) {
						if(1 == arg0.getButton()){
							robotController.dispose();
						}
					}
				});
				btnCancel.setActionCommand("Cancel");
				buttonPane.add(btnCancel);
			}
		}
		
		setRobotInfo();
	}
}
