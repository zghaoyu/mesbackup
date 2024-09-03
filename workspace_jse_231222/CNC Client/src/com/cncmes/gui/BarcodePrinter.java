package com.cncmes.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.SwingConstants;

import com.cncmes.ctrl.SocketClient;
import com.cncmes.handler.SocketDataHandler;
import com.cncmes.utils.MathUtils;
import com.cncmes.utils.ThreadUtils;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.Socket;

public class BarcodePrinter extends JDialog {
	private static final long serialVersionUID = 9L;
	private static BarcodePrinter barcodePrinter = new BarcodePrinter();
	private JTextField textField_printerStatus;
	private JTextField textField_printerIP;
	private JTextField textField_printerPort;
	private JTextField textField_printString;
	private JTextField textField_lastPrintString;
	private JTextField textField_printTotalCount;
	private JTextField textField_printerTime;
	private JTextField textField_linearSetting;
	private JTextField textField_printCoding;
	private JTextField textField_groupCondition;
	private JTextField textField_errorCode;
	private JTextField textField_counterCondition;
	private JTextField textField_counterValue;
	private JTextField textField_counterRepeats;
	private JTextField txtField_command;
	private JTextField textField_Feedback;
	private Socket socket = null;
	
	public static BarcodePrinter getInstance(){
		return barcodePrinter;
	}
	
	/**
	 * Create the dialog.
	 */
	private BarcodePrinter() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(BarcodePrinter.class.getResource("/com/cncmes/img/Butterfly_24.png")));
		setTitle("Barcode Printer");
		setModal(true);
		int width = 800;
		int height = 380;
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		
		BorderLayout borderLayout = new BorderLayout();
		getContentPane().setLayout(borderLayout);
		{
			JPanel panelMain = new JPanel();
			getContentPane().add(panelMain, BorderLayout.CENTER);
			panelMain.setLayout(new BorderLayout(5, 10));
			{
				JPanel panelPrinterCtrl = new JPanel();
				panelMain.add(panelPrinterCtrl, BorderLayout.CENTER);
				GridBagLayout gbl_panelPrinterCtrl = new GridBagLayout();
				gbl_panelPrinterCtrl.columnWidths = new int[] {100, 550, 100, 0};
				gbl_panelPrinterCtrl.rowHeights = new int[] {30, 30, 30, 30};
				gbl_panelPrinterCtrl.columnWeights = new double[]{0.0, 0.0, 0.0};
				gbl_panelPrinterCtrl.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0};
				panelPrinterCtrl.setLayout(gbl_panelPrinterCtrl);
				{
					JLabel lblPrinterIp = new JLabel("Printer IP");
					lblPrinterIp.setHorizontalAlignment(SwingConstants.CENTER);
					GridBagConstraints gbc_lblPrinterIp = new GridBagConstraints();
					gbc_lblPrinterIp.anchor = GridBagConstraints.EAST;
					gbc_lblPrinterIp.fill = GridBagConstraints.VERTICAL;
					gbc_lblPrinterIp.insets = new Insets(0, 0, 5, 5);
					gbc_lblPrinterIp.gridx = 0;
					gbc_lblPrinterIp.gridy = 0;
					panelPrinterCtrl.add(lblPrinterIp, gbc_lblPrinterIp);
				}
				{
					textField_printerIP = new JTextField();
					textField_printerIP.setText("192.168.0.100");
					GridBagConstraints gbc_textField_printerIP = new GridBagConstraints();
					gbc_textField_printerIP.fill = GridBagConstraints.BOTH;
					gbc_textField_printerIP.insets = new Insets(0, 0, 5, 5);
					gbc_textField_printerIP.gridx = 1;
					gbc_textField_printerIP.gridy = 0;
					panelPrinterCtrl.add(textField_printerIP, gbc_textField_printerIP);
					textField_printerIP.setColumns(10);
				}
				{
					JButton btnConnect = new JButton("Connect");
					btnConnect.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if(1 == e.getButton()){
								
								String ip = textField_printerIP.getText().trim();
								int port = Integer.parseInt(textField_printerPort.getText().trim());
								String btnText = btnConnect.getText();
								if(!"".equals(ip) && port>1024){
									if("Connect".equals(btnText)){
										try {
											socket = SocketClient.getInstance().connect(ip, port, new SocketClientDataHandler(), null);
											if(null!=socket){
												btnConnect.setText("Disconnect");
												textField_printerIP.setEnabled(false);
												textField_printerPort.setEnabled(false);
											}
										} catch (IOException e1) {
											JOptionPane.showMessageDialog(barcodePrinter.getContentPane(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
										}
									}else{
										try {
											SocketClient.getInstance().sendData(socket, "quit",null);
										} catch (IOException e1) {
											JOptionPane.showMessageDialog(barcodePrinter.getContentPane(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
										} finally {
											btnConnect.setText("Connect");
											textField_printerIP.setEnabled(true);
											textField_printerPort.setEnabled(true);
										}
									}
								}else{
									JOptionPane.showMessageDialog(barcodePrinter.getContentPane(), "IP or port is not correct", "Error", JOptionPane.ERROR_MESSAGE);
								}
							}
						}
					});
					GridBagConstraints gbc_btnConnect = new GridBagConstraints();
					gbc_btnConnect.fill = GridBagConstraints.BOTH;
					gbc_btnConnect.insets = new Insets(0, 0, 5, 5);
					gbc_btnConnect.gridx = 2;
					gbc_btnConnect.gridy = 0;
					panelPrinterCtrl.add(btnConnect, gbc_btnConnect);
				}
				{
					JLabel lblPrinterPort = new JLabel("Printer Port");
					lblPrinterPort.setHorizontalAlignment(SwingConstants.CENTER);
					GridBagConstraints gbc_lblPrinterPort = new GridBagConstraints();
					gbc_lblPrinterPort.anchor = GridBagConstraints.EAST;
					gbc_lblPrinterPort.fill = GridBagConstraints.VERTICAL;
					gbc_lblPrinterPort.insets = new Insets(0, 0, 5, 5);
					gbc_lblPrinterPort.gridx = 0;
					gbc_lblPrinterPort.gridy = 1;
					panelPrinterCtrl.add(lblPrinterPort, gbc_lblPrinterPort);
				}
				{
					textField_printerPort = new JTextField();
					textField_printerPort.setText("9004");
					GridBagConstraints gbc_textField_printerPort = new GridBagConstraints();
					gbc_textField_printerPort.fill = GridBagConstraints.BOTH;
					gbc_textField_printerPort.insets = new Insets(0, 0, 5, 5);
					gbc_textField_printerPort.gridx = 1;
					gbc_textField_printerPort.gridy = 1;
					panelPrinterCtrl.add(textField_printerPort, gbc_textField_printerPort);
					textField_printerPort.setColumns(10);
				}
				{
					JButton btnGetPrintSetting = new JButton("Get Print Setting");
					btnGetPrintSetting.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent arg0) {
							if(1 == arg0.getButton()){
//								String ip = textField_printerIP.getText().trim();
//								int port = Integer.parseInt(textField_printerPort.getText().trim());
								ThreadUtils.Run(new PrinterMonitoring());
							}
						}
					});
					GridBagConstraints gbc_btnGetPrintSetting = new GridBagConstraints();
					gbc_btnGetPrintSetting.insets = new Insets(0, 0, 5, 5);
					gbc_btnGetPrintSetting.gridx = 2;
					gbc_btnGetPrintSetting.gridy = 1;
					panelPrinterCtrl.add(btnGetPrintSetting, gbc_btnGetPrintSetting);
				}
				{
					JLabel lblPrintString = new JLabel("Print String");
					lblPrintString.setHorizontalAlignment(SwingConstants.CENTER);
					GridBagConstraints gbc_lblPrintString = new GridBagConstraints();
					gbc_lblPrintString.anchor = GridBagConstraints.EAST;
					gbc_lblPrintString.fill = GridBagConstraints.VERTICAL;
					gbc_lblPrintString.insets = new Insets(0, 0, 5, 5);
					gbc_lblPrintString.gridx = 0;
					gbc_lblPrintString.gridy = 2;
					panelPrinterCtrl.add(lblPrintString, gbc_lblPrintString);
				}
				{
					textField_printString = new JTextField();
					textField_printString.setText("ABCDEFGHIJKLMNOPQRSTUVWXYZAA");
					GridBagConstraints gbc_textField_printString = new GridBagConstraints();
					gbc_textField_printString.fill = GridBagConstraints.BOTH;
					gbc_textField_printString.insets = new Insets(0, 0, 5, 5);
					gbc_textField_printString.gridx = 1;
					gbc_textField_printString.gridy = 2;
					panelPrinterCtrl.add(textField_printString, gbc_textField_printString);
					textField_printString.setColumns(10);
				}
				{
					JButton btnSetPrintString = new JButton("Set Print String");
					btnSetPrintString.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if(1 == e.getButton()){
								String cmd = textField_printString.getText().trim();
//								String ip = textField_printerIP.getText().trim();
//								int port = Integer.parseInt(textField_printerPort.getText().trim());
								
								if (!"".equals(cmd)){
									try {
										cmd = "BE,1,1," + cmd + MathUtils.MD5Encode(cmd);
										SocketClient.getInstance().sendData(socket, cmd, null);
									} catch (IOException e1) {
										JOptionPane.showMessageDialog(barcodePrinter.getContentPane(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
									}
								}
							}
						}
					});
					GridBagConstraints gbc_btnSetPrintString = new GridBagConstraints();
					gbc_btnSetPrintString.fill = GridBagConstraints.BOTH;
					gbc_btnSetPrintString.insets = new Insets(0, 0, 5, 5);
					gbc_btnSetPrintString.gridx = 2;
					gbc_btnSetPrintString.gridy = 2;
					panelPrinterCtrl.add(btnSetPrintString, gbc_btnSetPrintString);
				}
				{
					JLabel lblCommand = new JLabel("Command");
					lblCommand.setHorizontalAlignment(SwingConstants.CENTER);
					GridBagConstraints gbc_lblCommand = new GridBagConstraints();
					gbc_lblCommand.anchor = GridBagConstraints.EAST;
					gbc_lblCommand.fill = GridBagConstraints.VERTICAL;
					gbc_lblCommand.insets = new Insets(0, 0, 0, 5);
					gbc_lblCommand.gridx = 0;
					gbc_lblCommand.gridy = 3;
					panelPrinterCtrl.add(lblCommand, gbc_lblCommand);
				}
				{
					txtField_command = new JTextField();
					txtField_command.setText("FT,1,1,1,0");
					GridBagConstraints gbc_txtField_command = new GridBagConstraints();
					gbc_txtField_command.fill = GridBagConstraints.BOTH;
					gbc_txtField_command.insets = new Insets(0, 0, 0, 5);
					gbc_txtField_command.gridx = 1;
					gbc_txtField_command.gridy = 3;
					panelPrinterCtrl.add(txtField_command, gbc_txtField_command);
					txtField_command.setColumns(10);
				}
				{
					JButton btnSendCommand = new JButton("Send Command");
					btnSendCommand.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if(1 == e.getButton()){
								String cmd = txtField_command.getText().trim();
//								String ip = textField_printerIP.getText().trim();
//								int port = Integer.parseInt(textField_printerPort.getText().trim());
								
								if (!"".equals(cmd)){
									try {
										SocketClient.getInstance().sendData(socket, cmd, null);
									} catch (IOException e1) {
										JOptionPane.showMessageDialog(barcodePrinter.getContentPane(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
									}
								}
							}
						}
					});
					GridBagConstraints gbc_btnSendCommand = new GridBagConstraints();
					gbc_btnSendCommand.insets = new Insets(0, 0, 0, 5);
					gbc_btnSendCommand.fill = GridBagConstraints.BOTH;
					gbc_btnSendCommand.gridx = 2;
					gbc_btnSendCommand.gridy = 3;
					panelPrinterCtrl.add(btnSendCommand, gbc_btnSendCommand);
				}
			}
			{
				JPanel panelPrinterInfo = new JPanel();
				panelMain.add(panelPrinterInfo, BorderLayout.SOUTH);
				GridBagLayout gbl_panelPrinterInfo = new GridBagLayout();
				gbl_panelPrinterInfo.columnWidths = new int[] {102, 270, 102, 270};
				gbl_panelPrinterInfo.rowHeights = new int[] {30, 30, 30, 30, 30, 30};
				gbl_panelPrinterInfo.columnWeights = new double[]{0.0, 0.0};
				gbl_panelPrinterInfo.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
				panelPrinterInfo.setLayout(gbl_panelPrinterInfo);
				{
					JLabel lblPrinterStatus = new JLabel("Printer Status");
					GridBagConstraints gbc_lblPrinterStatus = new GridBagConstraints();
					gbc_lblPrinterStatus.anchor = GridBagConstraints.WEST;
					gbc_lblPrinterStatus.fill = GridBagConstraints.VERTICAL;
					gbc_lblPrinterStatus.insets = new Insets(0, 0, 5, 5);
					gbc_lblPrinterStatus.gridx = 0;
					gbc_lblPrinterStatus.gridy = 0;
					panelPrinterInfo.add(lblPrinterStatus, gbc_lblPrinterStatus);
				}
				{
					textField_printerStatus = new JTextField();
					textField_printerStatus.setEditable(false);
					GridBagConstraints gbc_textField_printerStatus = new GridBagConstraints();
					gbc_textField_printerStatus.fill = GridBagConstraints.BOTH;
					gbc_textField_printerStatus.insets = new Insets(0, 0, 5, 5);
					gbc_textField_printerStatus.gridx = 1;
					gbc_textField_printerStatus.gridy = 0;
					panelPrinterInfo.add(textField_printerStatus, gbc_textField_printerStatus);
					textField_printerStatus.setColumns(10);
				}
				{
					JLabel lblLinearSetting = new JLabel("Linear Setting");
					GridBagConstraints gbc_lblLinearSetting = new GridBagConstraints();
					gbc_lblLinearSetting.anchor = GridBagConstraints.WEST;
					gbc_lblLinearSetting.fill = GridBagConstraints.VERTICAL;
					gbc_lblLinearSetting.insets = new Insets(0, 0, 5, 5);
					gbc_lblLinearSetting.gridx = 2;
					gbc_lblLinearSetting.gridy = 0;
					panelPrinterInfo.add(lblLinearSetting, gbc_lblLinearSetting);
				}
				{
					textField_linearSetting = new JTextField();
					textField_linearSetting.setEditable(false);
					GridBagConstraints gbc_textField_linearSetting = new GridBagConstraints();
					gbc_textField_linearSetting.fill = GridBagConstraints.BOTH;
					gbc_textField_linearSetting.insets = new Insets(0, 0, 5, 0);
					gbc_textField_linearSetting.gridx = 3;
					gbc_textField_linearSetting.gridy = 0;
					panelPrinterInfo.add(textField_linearSetting, gbc_textField_linearSetting);
					textField_linearSetting.setColumns(10);
				}
				{
					JLabel lblErrorCode = new JLabel("Error Code");
					GridBagConstraints gbc_lblErrorCode = new GridBagConstraints();
					gbc_lblErrorCode.anchor = GridBagConstraints.WEST;
					gbc_lblErrorCode.fill = GridBagConstraints.VERTICAL;
					gbc_lblErrorCode.insets = new Insets(0, 0, 5, 5);
					gbc_lblErrorCode.gridx = 0;
					gbc_lblErrorCode.gridy = 1;
					panelPrinterInfo.add(lblErrorCode, gbc_lblErrorCode);
				}
				{
					textField_errorCode = new JTextField();
					textField_errorCode.setEditable(false);
					GridBagConstraints gbc_textField_errorCode = new GridBagConstraints();
					gbc_textField_errorCode.fill = GridBagConstraints.BOTH;
					gbc_textField_errorCode.insets = new Insets(0, 0, 5, 5);
					gbc_textField_errorCode.gridx = 1;
					gbc_textField_errorCode.gridy = 1;
					panelPrinterInfo.add(textField_errorCode, gbc_textField_errorCode);
					textField_errorCode.setColumns(10);
				}
				{
					JLabel lblPrintCoding = new JLabel("Print Coding");
					GridBagConstraints gbc_lblPrintCoding = new GridBagConstraints();
					gbc_lblPrintCoding.anchor = GridBagConstraints.WEST;
					gbc_lblPrintCoding.fill = GridBagConstraints.VERTICAL;
					gbc_lblPrintCoding.insets = new Insets(0, 0, 5, 5);
					gbc_lblPrintCoding.gridx = 2;
					gbc_lblPrintCoding.gridy = 1;
					panelPrinterInfo.add(lblPrintCoding, gbc_lblPrintCoding);
				}
				{
					textField_printCoding = new JTextField();
					textField_printCoding.setEditable(false);
					GridBagConstraints gbc_textField_printCoding = new GridBagConstraints();
					gbc_textField_printCoding.fill = GridBagConstraints.BOTH;
					gbc_textField_printCoding.insets = new Insets(0, 0, 5, 0);
					gbc_textField_printCoding.gridx = 3;
					gbc_textField_printCoding.gridy = 1;
					panelPrinterInfo.add(textField_printCoding, gbc_textField_printCoding);
					textField_printCoding.setColumns(10);
				}
				{
					JLabel lblGroupCondition = new JLabel("Group Condition");
					GridBagConstraints gbc_lblGroupCondition = new GridBagConstraints();
					gbc_lblGroupCondition.anchor = GridBagConstraints.WEST;
					gbc_lblGroupCondition.fill = GridBagConstraints.VERTICAL;
					gbc_lblGroupCondition.insets = new Insets(0, 0, 5, 5);
					gbc_lblGroupCondition.gridx = 0;
					gbc_lblGroupCondition.gridy = 2;
					panelPrinterInfo.add(lblGroupCondition, gbc_lblGroupCondition);
				}
				{
					textField_groupCondition = new JTextField();
					textField_groupCondition.setEditable(false);
					GridBagConstraints gbc_textField_groupCondition = new GridBagConstraints();
					gbc_textField_groupCondition.fill = GridBagConstraints.BOTH;
					gbc_textField_groupCondition.insets = new Insets(0, 0, 5, 5);
					gbc_textField_groupCondition.gridx = 1;
					gbc_textField_groupCondition.gridy = 2;
					panelPrinterInfo.add(textField_groupCondition, gbc_textField_groupCondition);
					textField_groupCondition.setColumns(10);
				}
				{
					JLabel lblLastPrintString = new JLabel("Last Print String");
					GridBagConstraints gbc_lblLastPrintString = new GridBagConstraints();
					gbc_lblLastPrintString.anchor = GridBagConstraints.WEST;
					gbc_lblLastPrintString.fill = GridBagConstraints.VERTICAL;
					gbc_lblLastPrintString.insets = new Insets(0, 0, 5, 5);
					gbc_lblLastPrintString.gridx = 2;
					gbc_lblLastPrintString.gridy = 2;
					panelPrinterInfo.add(lblLastPrintString, gbc_lblLastPrintString);
				}
				{
					textField_lastPrintString = new JTextField();
					textField_lastPrintString.setEditable(false);
					GridBagConstraints gbc_textField_lastPrintString = new GridBagConstraints();
					gbc_textField_lastPrintString.fill = GridBagConstraints.BOTH;
					gbc_textField_lastPrintString.insets = new Insets(0, 0, 5, 0);
					gbc_textField_lastPrintString.gridx = 3;
					gbc_textField_lastPrintString.gridy = 2;
					panelPrinterInfo.add(textField_lastPrintString, gbc_textField_lastPrintString);
					textField_lastPrintString.setColumns(10);
				}
				{
					JLabel lblCounterCondition = new JLabel("Counter Condition");
					GridBagConstraints gbc_lblCounterCondition = new GridBagConstraints();
					gbc_lblCounterCondition.anchor = GridBagConstraints.WEST;
					gbc_lblCounterCondition.fill = GridBagConstraints.VERTICAL;
					gbc_lblCounterCondition.insets = new Insets(0, 0, 5, 5);
					gbc_lblCounterCondition.gridx = 0;
					gbc_lblCounterCondition.gridy = 3;
					panelPrinterInfo.add(lblCounterCondition, gbc_lblCounterCondition);
				}
				{
					textField_counterCondition = new JTextField();
					textField_counterCondition.setEditable(false);
					GridBagConstraints gbc_textField_counterCondition = new GridBagConstraints();
					gbc_textField_counterCondition.fill = GridBagConstraints.BOTH;
					gbc_textField_counterCondition.insets = new Insets(0, 0, 5, 5);
					gbc_textField_counterCondition.gridx = 1;
					gbc_textField_counterCondition.gridy = 3;
					panelPrinterInfo.add(textField_counterCondition, gbc_textField_counterCondition);
					textField_counterCondition.setColumns(10);
				}
				{
					JLabel lblCounterValue = new JLabel("Counter Value");
					GridBagConstraints gbc_lblCounterValue = new GridBagConstraints();
					gbc_lblCounterValue.anchor = GridBagConstraints.WEST;
					gbc_lblCounterValue.fill = GridBagConstraints.VERTICAL;
					gbc_lblCounterValue.insets = new Insets(0, 0, 5, 5);
					gbc_lblCounterValue.gridx = 2;
					gbc_lblCounterValue.gridy = 3;
					panelPrinterInfo.add(lblCounterValue, gbc_lblCounterValue);
				}
				{
					textField_counterValue = new JTextField();
					textField_counterValue.setEditable(false);
					GridBagConstraints gbc_textField_counterValue = new GridBagConstraints();
					gbc_textField_counterValue.fill = GridBagConstraints.BOTH;
					gbc_textField_counterValue.insets = new Insets(0, 0, 5, 0);
					gbc_textField_counterValue.gridx = 3;
					gbc_textField_counterValue.gridy = 3;
					panelPrinterInfo.add(textField_counterValue, gbc_textField_counterValue);
					textField_counterValue.setColumns(10);
				}
				{
					JLabel lblCounterRepeats = new JLabel("Counter Repeats");
					GridBagConstraints gbc_lblCounterRepeats = new GridBagConstraints();
					gbc_lblCounterRepeats.anchor = GridBagConstraints.WEST;
					gbc_lblCounterRepeats.fill = GridBagConstraints.VERTICAL;
					gbc_lblCounterRepeats.insets = new Insets(0, 0, 5, 5);
					gbc_lblCounterRepeats.gridx = 0;
					gbc_lblCounterRepeats.gridy = 4;
					panelPrinterInfo.add(lblCounterRepeats, gbc_lblCounterRepeats);
				}
				{
					textField_counterRepeats = new JTextField();
					textField_counterRepeats.setEditable(false);
					GridBagConstraints gbc_textField_counterRepeats = new GridBagConstraints();
					gbc_textField_counterRepeats.fill = GridBagConstraints.BOTH;
					gbc_textField_counterRepeats.insets = new Insets(0, 0, 5, 5);
					gbc_textField_counterRepeats.gridx = 1;
					gbc_textField_counterRepeats.gridy = 4;
					panelPrinterInfo.add(textField_counterRepeats, gbc_textField_counterRepeats);
					textField_counterRepeats.setColumns(10);
				}
				{
					JLabel lblPrintTotalCount = new JLabel("Print Total Count");
					GridBagConstraints gbc_lblPrintTotalCount = new GridBagConstraints();
					gbc_lblPrintTotalCount.anchor = GridBagConstraints.WEST;
					gbc_lblPrintTotalCount.fill = GridBagConstraints.VERTICAL;
					gbc_lblPrintTotalCount.insets = new Insets(0, 0, 5, 5);
					gbc_lblPrintTotalCount.gridx = 2;
					gbc_lblPrintTotalCount.gridy = 4;
					panelPrinterInfo.add(lblPrintTotalCount, gbc_lblPrintTotalCount);
				}
				{
					textField_printTotalCount = new JTextField();
					textField_printTotalCount.setEditable(false);
					GridBagConstraints gbc_textField_printTotalCount = new GridBagConstraints();
					gbc_textField_printTotalCount.fill = GridBagConstraints.BOTH;
					gbc_textField_printTotalCount.insets = new Insets(0, 0, 5, 0);
					gbc_textField_printTotalCount.gridx = 3;
					gbc_textField_printTotalCount.gridy = 4;
					panelPrinterInfo.add(textField_printTotalCount, gbc_textField_printTotalCount);
					textField_printTotalCount.setColumns(10);
				}
				{
					JLabel lblPrinterTime = new JLabel("Printer Time");
					GridBagConstraints gbc_lblPrinterTime = new GridBagConstraints();
					gbc_lblPrinterTime.anchor = GridBagConstraints.WEST;
					gbc_lblPrinterTime.fill = GridBagConstraints.VERTICAL;
					gbc_lblPrinterTime.insets = new Insets(0, 0, 0, 5);
					gbc_lblPrinterTime.gridx = 0;
					gbc_lblPrinterTime.gridy = 5;
					panelPrinterInfo.add(lblPrinterTime, gbc_lblPrinterTime);
				}
				{
					textField_printerTime = new JTextField();
					textField_printerTime.setEditable(false);
					GridBagConstraints gbc_textField_printerTime = new GridBagConstraints();
					gbc_textField_printerTime.insets = new Insets(0, 0, 0, 5);
					gbc_textField_printerTime.fill = GridBagConstraints.BOTH;
					gbc_textField_printerTime.gridx = 1;
					gbc_textField_printerTime.gridy = 5;
					panelPrinterInfo.add(textField_printerTime, gbc_textField_printerTime);
					textField_printerTime.setColumns(10);
				}
				{
					JLabel lblFeedBack = new JLabel("Feed Back");
					GridBagConstraints gbc_lblFeedBack = new GridBagConstraints();
					gbc_lblFeedBack.anchor = GridBagConstraints.WEST;
					gbc_lblFeedBack.insets = new Insets(0, 0, 0, 5);
					gbc_lblFeedBack.gridx = 2;
					gbc_lblFeedBack.gridy = 5;
					panelPrinterInfo.add(lblFeedBack, gbc_lblFeedBack);
				}
				{
					textField_Feedback = new JTextField();
					textField_Feedback.setEditable(false);
					GridBagConstraints gbc_textField_Feedback = new GridBagConstraints();
					gbc_textField_Feedback.fill = GridBagConstraints.BOTH;
					gbc_textField_Feedback.insets = new Insets(0, 0, 5, 0);
					gbc_textField_Feedback.gridx = 3;
					gbc_textField_Feedback.gridy = 5;
					panelPrinterInfo.add(textField_Feedback, gbc_textField_Feedback);
					textField_Feedback.setColumns(10);
				}
			}
		}
	}
	
	private String getPrinterStatus(String statusCode){
		String desc = "Unkown";
		switch(statusCode){
		case "00":
			desc = "Stop";
			break;
		case "01":
			desc = "Ready to print";
			break;
		case "02":
			desc = "Start running over 1";
			break;
		case "03":
			desc = "Start running over 2";
			break;
		case "04":
			desc = "Print interrupt";
			break;
		case "05":
			desc = "Starting up";
			break;
		case "06":
			desc = "Shutting down";
			break;
		case "07":
			desc = "Preparing printing";
			break;
		case "08":
			desc = "Printing oil adjustment";
			break;
		case "09":
			desc = "Long time stop cleaning";
			break;
		case "10":
			desc = "Routing resuming";
			break;
		case "11":
			desc = "Pausing";
			break;
		case "12":
			desc = "Emergency stop";
			break;
		case "13":
			desc = "Oil recycling";
			break;
		case "14":
			desc = "Printing tip sucking";
			break;
		case "15":
			desc = "Groove sucking";
			break;
		case "16":
			desc = "Oil compensation";
			break;
		case "17":
			desc = "Printing tip replacing";
			break;
		case "18":
			desc = "Pressure adjustment";
			break;
		case "19":
			desc = "Main groove dejection";
			break;
		case "20":
			desc = "Adjustment groove dejection";
			break;
		case "21":
			desc = "Full dejection";
			break;
		case "22":
			desc = "Auto cleaning";
			break;
		case "23":
			desc = "Force auto cleaning";
			break;
		case "24":
			desc = "Printing tip covering";
			break;
		case "25":
			desc = "Internal drying";
			break;
		case "26":
			desc = "Sleeping mode";
			break;
		case "27":
			desc = "Cleaning";
			break;
		case "28":
			desc = "Filter A replacing";
			break;
		case "29":
			desc = "Filter B replacing";
			break;
		case "30":
			desc = "Oil pump replacing";
			break;
		case "31":
			desc = "Filter A replacing";
			break;
		case "32":
			desc = "Sleeping mode(shutdown)";
			break;
		case "33":
			desc = "Sleeping mode(waiting)";
			break;
		case "34":
			desc = "Sleeping mode(operating)";
			break;
		case "35":
			desc = "Stop(Shutdown screen showing)";
			break;
		case "36":
			desc = "Stop(Printing tip cover confirmed screen showing)";
			break;
		case "37":
			desc = "Maintenance interrupt";
			break;
		case "38":
			desc = "Restart maintenance";
			break;
		case "39":
			desc = "Pause maintenance";
			break;
		case "40":
			desc = "Filter A replacing";
			break;
		case "41":
			desc = "Filter A replacing";
			break;
		case "42":
			desc = "Oil pump replacing";
			break;
		case "43":
			desc = "Stop(replacing)";
			break;
		case "44":
			desc = "Oil recycling";
			break;
		case "45":
			desc = "Cleaing(Oil pump drying)";
			break;
		case "46":
			desc = "Auto cleaning(Drying)";
			break;
		case "47":
			desc = "Printing tip cleaning";
			break;
		case "48":
			desc = "Cleaning before sleep";
			break;
		}
		return desc;
	}
	
	class SocketClientDataHandler extends SocketDataHandler{
		@Override
		public void doHandle(String in, Socket s) {
			textField_Feedback.setText(in);
			
			String[] arrIn = in.split(",");
			if(arrIn.length > 0){
				switch(arrIn[0]){
				case "SB":
					if(arrIn.length > 1){
						textField_printerStatus.setText(arrIn[1]+":"+getPrinterStatus(arrIn[1]));
					}else{
						textField_printerStatus.setText("Status:"+in);
					}
					break;
				case "FL"://Page 10
					textField_linearSetting.setText(in);
					break;
				case "F6"://Page 12
					textField_groupCondition.setText(in);
					break;
				case "FT"://Page 13-14
					textField_printCoding.setText(in);
					break;
				case "UZ"://Page 14
					textField_lastPrintString.setText(in);
					break;
				case "CP"://Page 14
					textField_counterCondition.setText(in);
					break;
				case "CN"://Page 15
					textField_counterValue.setText(in);
					break;
				case "CR"://Page 15
					textField_counterRepeats.setText(in);
					break;
				case "KH"://Page 23
					textField_printTotalCount.setText(in);
					break;
				case "DB"://Page 24
					textField_printerTime.setText(in);
					break;
				case "EV"://Page 7
					if(arrIn.length > 1){
						if(Integer.parseInt(arrIn[1]) > 0){
							textField_errorCode.setText("Error:"+arrIn[1]);
						}else{
							textField_errorCode.setText("OK:"+arrIn[1]);
						}
					}else{
						textField_errorCode.setText("Error:"+in);
					}
					break;
				}
			}
		}
	}
	
	class PrinterMonitoring implements Runnable{
//		private String ip;
//		private int port;
		private long sleepTms = 100;
		
		public PrinterMonitoring(){
//			this.ip = ip;
//			this.port = port;
		}
		
		@Override
		public void run() {
			String cmd = "";
			try {
				while(true){
					//1.Query System Status
					cmd = "SB";
					SocketClient.getInstance().sendData(socket, cmd, null);
					if(null == socket) break;
					Thread.sleep(sleepTms);
					
					//2.Query Linear Setting
					cmd = "FL,1,0";
					SocketClient.getInstance().sendData(socket, cmd, null);
					if(null == socket) break;
					Thread.sleep(sleepTms);
					
					//3.Query Group Condition
					cmd = "F6,1,1";
					SocketClient.getInstance().sendData(socket, cmd, null);
					if(null == socket) break;
					Thread.sleep(sleepTms);
					
					//4.Query Print String
					cmd = "FT,1,1,1,0";
					SocketClient.getInstance().sendData(socket, cmd, null);
					if(null == socket) break;
					Thread.sleep(sleepTms);
					
					//5.Query Last Print String
					cmd = "UZ,1,0";
					SocketClient.getInstance().sendData(socket, cmd, null);
					if(null == socket) break;
					Thread.sleep(sleepTms);
					
					//6.Query Counter Condition
					cmd = "CP,0,L";
					SocketClient.getInstance().sendData(socket, cmd, null);
					if(null == socket) break;
					Thread.sleep(sleepTms);
					
					//7.Query Counter Value
					cmd = "CN,0,A";
					SocketClient.getInstance().sendData(socket, cmd, null);
					if(null == socket) break;
					Thread.sleep(sleepTms);
					
					//8.Query Counter Repeats
					cmd = "CR,0,A";
					SocketClient.getInstance().sendData(socket, cmd, null);
					if(null == socket) break;
					Thread.sleep(sleepTms);
					
					//9.Query Total Print Count
					cmd = "KH,1";
					SocketClient.getInstance().sendData(socket, cmd, null);
					if(null == socket) break;
					Thread.sleep(sleepTms);
					
					//10.Query System Date Time
					cmd = "DB";
					SocketClient.getInstance().sendData(socket, cmd, null);
					if(null == socket) break;
					Thread.sleep(sleepTms);
					
					//11.Query Error Code
					cmd = "EV";
					SocketClient.getInstance().sendData(socket, cmd, null);
					if(null == socket) break;
					Thread.sleep(sleepTms);
					
					break;
				}
				System.out.println("Printer Monitoring Stopped");
			} catch (IOException | InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
	
}
