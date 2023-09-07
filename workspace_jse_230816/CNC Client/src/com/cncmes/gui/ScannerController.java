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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.cncmes.base.DriverItems;
import com.cncmes.base.Scanner;
import com.cncmes.base.ScannerItems;
import com.cncmes.ctrl.ScannerFactory;
import com.cncmes.data.ScannerData;
import com.cncmes.data.ScannerDriver;

public class ScannerController extends JDialog {
	private JComboBox<String> comboBoxIP;
	private JComboBox<String> comboBoxCommand;
	private JTextField textField_Info;
	
	private static final long serialVersionUID = 20L;
	private final JPanel contentPanel = new JPanel();
	private static ScannerController scannerController = new ScannerController();
	public static ScannerController getInstance(){
		return scannerController;
	}
	
	private String[] getCommandList(){
		String[] cmds = new String[]{"doScanning"};
		
		return cmds;
	}
	
	private String[] getIPList(){
		String[] ips = null;
		String tmp = "";
		
		for(String ip:ScannerData.getInstance().getDataMap().keySet()){
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
	
	private void setScannerInfo() {
		String deviceIP = comboBoxIP.getSelectedItem().toString();
		String info = "Unkown device";
		if(null != ScannerData.getInstance().getData(deviceIP)){
			info = "" + ScannerData.getInstance().getData(deviceIP).get(ScannerItems.MODEL);
			info += " / " + ScannerData.getInstance().getData(deviceIP).get(ScannerItems.PORT);
		}
		textField_Info.setText(info);
	}
	
	/**
	 * Create the dialog.
	 */
	private ScannerController() {
		setModal(true);
		setIconImage(Toolkit.getDefaultToolkit().getImage(ScannerController.class.getResource("/com/cncmes/img/Butterfly_24.png")));
		setTitle("Scanner Controller");
		setResizable(false);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 400;
		int height = 180;
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0, 0};
		gbl_contentPanel.rowHeights = new int[] {0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0};
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
					setScannerInfo();
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
			gbc_lblCommand.insets = new Insets(0, 0, 0, 5);
			gbc_lblCommand.gridx = 0;
			gbc_lblCommand.gridy = 2;
			contentPanel.add(lblCommand, gbc_lblCommand);
		}
		{
			comboBoxCommand = new JComboBox<String>();
			comboBoxCommand.setModel(new DefaultComboBoxModel<String>(getCommandList()));
			GridBagConstraints gbc_comboBoxCommand = new GridBagConstraints();
			gbc_comboBoxCommand.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBoxCommand.gridx = 1;
			gbc_comboBoxCommand.gridy = 2;
			contentPanel.add(comboBoxCommand, gbc_comboBoxCommand);
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
						ScannerData scannerData = ScannerData.getInstance();
						
						if(1 == arg0.getButton()){
							String scannerModel = (String) scannerData.getData(ip).get(ScannerItems.MODEL);
							Scanner scannerCtrl = ScannerFactory.getInstance((String)ScannerDriver.getInstance().getData(scannerModel).get(DriverItems.DRIVER));
							if(null != scannerCtrl){
								String ok = "";
								boolean cmdExist = true;
								switch(cmd){
								case "doScanning":
									ok = scannerCtrl.doScanning(ip);
									break;
								default:
									cmdExist = false;
									JOptionPane.showMessageDialog(scannerController.getContentPane(), "Command["+cmd+"] is not supported", "Command ERROR", JOptionPane.ERROR_MESSAGE);
									break;
								}
								
								if(cmdExist){
									if(!"".equals(ok)){
										JOptionPane.showMessageDialog(scannerController.getContentPane(), "Command["+cmd+"] execution OK", "Command execution OK", JOptionPane.INFORMATION_MESSAGE);
									}else{
										JOptionPane.showMessageDialog(scannerController.getContentPane(), "Command["+cmd+"] execution failed", "Command Failed", JOptionPane.ERROR_MESSAGE);
									}
								}
							}else{
								JOptionPane.showMessageDialog(scannerController.getContentPane(), "Load machine driver failed", "Driver ERROR", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				});
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
							scannerController.dispose();
						}
					}
				});
				btnCancel.setActionCommand("Cancel");
				buttonPane.add(btnCancel);
			}
		}
		
		setScannerInfo();
	}
}
