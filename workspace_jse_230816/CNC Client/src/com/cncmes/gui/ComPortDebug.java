package com.cncmes.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.cncmes.ctrl.RS232Client;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.DefaultComboBoxModel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class ComPortDebug extends JDialog {
	private static ComPortDebug comPortDebug = new ComPortDebug();
	private final JPanel contentPanel = new JPanel();
	private static final long serialVersionUID = 6L;
	private RS232Client rs232c = null;
	private SerialPort serialPort = null;
	private String portName = "";
	private JTextField textField_command;
	private JTextField textField_feedback;
	private JComboBox<?> comboBox_comPort;
	private JComboBox<?> comboBox_baudrate;
	private JComboBox<?> comboBox_databits;
	private JComboBox<?> comboBox_stopbits;
	private JComboBox<?> comboBox_parity;
	private JComboBox<?> comboBox_CRLF;
	
	public static ComPortDebug getInstance(){
		return comPortDebug;
	}
	
	/**
	 * Create the dialog.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ComPortDebug() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(ComPortDebug.class.getResource("/com/cncmes/img/Butterfly_24.png")));
		rs232c = RS232Client.getInstance();
		setModal(true);
		setTitle("COM Port Test");
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 400;
		int height = 360;
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] {87, 287};
		gbl_contentPanel.rowHeights = new int[] {35, 35, 35, 35, 35, 35, 35, 35};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblPort = new JLabel("Port");
			GridBagConstraints gbc_lblPort = new GridBagConstraints();
			gbc_lblPort.fill = GridBagConstraints.BOTH;
			gbc_lblPort.insets = new Insets(0, 0, 5, 5);
			gbc_lblPort.gridx = 0;
			gbc_lblPort.gridy = 0;
			contentPanel.add(lblPort, gbc_lblPort);
		}
		comboBox_comPort = new JComboBox();
		GridBagConstraints gbc_comboBox_comPort = new GridBagConstraints();
		gbc_comboBox_comPort.fill = GridBagConstraints.BOTH;
		gbc_comboBox_comPort.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox_comPort.gridx = 1;
		gbc_comboBox_comPort.gridy = 0;
		contentPanel.add(comboBox_comPort, gbc_comboBox_comPort);
		{
			JLabel lblBaudRate = new JLabel("Baud Rate");
			GridBagConstraints gbc_lblBaudRate = new GridBagConstraints();
			gbc_lblBaudRate.fill = GridBagConstraints.BOTH;
			gbc_lblBaudRate.insets = new Insets(0, 0, 5, 5);
			gbc_lblBaudRate.gridx = 0;
			gbc_lblBaudRate.gridy = 1;
			contentPanel.add(lblBaudRate, gbc_lblBaudRate);
		}
		{
			comboBox_baudrate = new JComboBox();
			comboBox_baudrate.setModel(new DefaultComboBoxModel(new String[] {"115200", "57600", "38400", "19200", "9600", "4800", "2400", "1200", "600"}));
			GridBagConstraints gbc_comboBox_baudrate = new GridBagConstraints();
			gbc_comboBox_baudrate.fill = GridBagConstraints.BOTH;
			gbc_comboBox_baudrate.insets = new Insets(0, 0, 5, 0);
			gbc_comboBox_baudrate.gridx = 1;
			gbc_comboBox_baudrate.gridy = 1;
			contentPanel.add(comboBox_baudrate, gbc_comboBox_baudrate);
		}
		{
			JLabel lblDataBits = new JLabel("Data Bits");
			GridBagConstraints gbc_lblDataBits = new GridBagConstraints();
			gbc_lblDataBits.fill = GridBagConstraints.BOTH;
			gbc_lblDataBits.insets = new Insets(0, 0, 5, 5);
			gbc_lblDataBits.gridx = 0;
			gbc_lblDataBits.gridy = 2;
			contentPanel.add(lblDataBits, gbc_lblDataBits);
		}
		{
			comboBox_databits = new JComboBox();
			comboBox_databits.setModel(new DefaultComboBoxModel(new String[] {"8", "7"}));
			GridBagConstraints gbc_comboBox_databits = new GridBagConstraints();
			gbc_comboBox_databits.fill = GridBagConstraints.BOTH;
			gbc_comboBox_databits.insets = new Insets(0, 0, 5, 0);
			gbc_comboBox_databits.gridx = 1;
			gbc_comboBox_databits.gridy = 2;
			contentPanel.add(comboBox_databits, gbc_comboBox_databits);
		}
		{
			JLabel lblStopBits = new JLabel("Stop Bits");
			GridBagConstraints gbc_lblStopBits = new GridBagConstraints();
			gbc_lblStopBits.fill = GridBagConstraints.BOTH;
			gbc_lblStopBits.insets = new Insets(0, 0, 5, 5);
			gbc_lblStopBits.gridx = 0;
			gbc_lblStopBits.gridy = 3;
			contentPanel.add(lblStopBits, gbc_lblStopBits);
		}
		{
			comboBox_stopbits = new JComboBox();
			comboBox_stopbits.setModel(new DefaultComboBoxModel(new String[] {"1", "2"}));
			GridBagConstraints gbc_comboBox_stopbits = new GridBagConstraints();
			gbc_comboBox_stopbits.fill = GridBagConstraints.BOTH;
			gbc_comboBox_stopbits.insets = new Insets(0, 0, 5, 0);
			gbc_comboBox_stopbits.gridx = 1;
			gbc_comboBox_stopbits.gridy = 3;
			contentPanel.add(comboBox_stopbits, gbc_comboBox_stopbits);
		}
		{
			JLabel lblParity = new JLabel("Parity");
			GridBagConstraints gbc_lblParity = new GridBagConstraints();
			gbc_lblParity.fill = GridBagConstraints.BOTH;
			gbc_lblParity.insets = new Insets(0, 0, 5, 5);
			gbc_lblParity.gridx = 0;
			gbc_lblParity.gridy = 4;
			contentPanel.add(lblParity, gbc_lblParity);
		}
		{
			comboBox_parity = new JComboBox();
			comboBox_parity.setModel(new DefaultComboBoxModel(new String[] {"2", "1", "0"}));
			GridBagConstraints gbc_comboBox_parity = new GridBagConstraints();
			gbc_comboBox_parity.fill = GridBagConstraints.BOTH;
			gbc_comboBox_parity.insets = new Insets(0, 0, 5, 0);
			gbc_comboBox_parity.gridx = 1;
			gbc_comboBox_parity.gridy = 4;
			contentPanel.add(comboBox_parity, gbc_comboBox_parity);
		}
		{
			JLabel lblFeedback = new JLabel("Feedback");
			GridBagConstraints gbc_lblFeedback = new GridBagConstraints();
			gbc_lblFeedback.fill = GridBagConstraints.BOTH;
			gbc_lblFeedback.insets = new Insets(0, 0, 5, 5);
			gbc_lblFeedback.gridx = 0;
			gbc_lblFeedback.gridy = 5;
			contentPanel.add(lblFeedback, gbc_lblFeedback);
		}
		{
			textField_feedback = new JTextField();
			GridBagConstraints gbc_textField_feedback = new GridBagConstraints();
			gbc_textField_feedback.fill = GridBagConstraints.BOTH;
			gbc_textField_feedback.insets = new Insets(0, 0, 5, 0);
			gbc_textField_feedback.gridx = 1;
			gbc_textField_feedback.gridy = 5;
			contentPanel.add(textField_feedback, gbc_textField_feedback);
			textField_feedback.setColumns(10);
		}
		{
			JLabel lblCommand = new JLabel("Command");
			GridBagConstraints gbc_lblCommand = new GridBagConstraints();
			gbc_lblCommand.fill = GridBagConstraints.BOTH;
			gbc_lblCommand.insets = new Insets(0, 0, 5, 5);
			gbc_lblCommand.gridx = 0;
			gbc_lblCommand.gridy = 6;
			contentPanel.add(lblCommand, gbc_lblCommand);
		}
		{
			textField_command = new JTextField();
			GridBagConstraints gbc_textField_command = new GridBagConstraints();
			gbc_textField_command.insets = new Insets(0, 0, 5, 0);
			gbc_textField_command.fill = GridBagConstraints.BOTH;
			gbc_textField_command.gridx = 1;
			gbc_textField_command.gridy = 6;
			contentPanel.add(textField_command, gbc_textField_command);
			textField_command.setColumns(10);
		}
		{
			JLabel lblCrlf = new JLabel("CRLF");
			GridBagConstraints gbc_lblCrlf = new GridBagConstraints();
			gbc_lblCrlf.fill = GridBagConstraints.BOTH;
			gbc_lblCrlf.insets = new Insets(0, 0, 0, 5);
			gbc_lblCrlf.gridx = 0;
			gbc_lblCrlf.gridy = 7;
			contentPanel.add(lblCrlf, gbc_lblCrlf);
		}
		{
			comboBox_CRLF = new JComboBox();
			comboBox_CRLF.setModel(new DefaultComboBoxModel(new String[] {"CRLF", "CR", "LF"}));
			GridBagConstraints gbc_comboBox_CRLF = new GridBagConstraints();
			gbc_comboBox_CRLF.fill = GridBagConstraints.BOTH;
			gbc_comboBox_CRLF.gridx = 1;
			gbc_comboBox_CRLF.gridy = 7;
			contentPanel.add(comboBox_CRLF, gbc_comboBox_CRLF);
		}
		{
			ArrayList<String> ports = rs232c.getSerialPorts();
			if(ports.size() > 0){
				String[] ps = new String[ports.size()];
				for(int i=0; i<ports.size(); i++){
					ps[i] = ports.get(i);
				}
				comboBox_comPort.setModel(new DefaultComboBoxModel(ps));
			}else{
				comboBox_comPort.setModel(new DefaultComboBoxModel(new String[] {""}));
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton btnSendCommand = new JButton("Send Command");
				btnSendCommand.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if(1 == e.getButton()){
							String cmd = textField_command.getText().trim();
							if(!"".equals(cmd)){
								String crlf = comboBox_CRLF.getSelectedItem().toString();
								switch(crlf){
								case "CRLF":
									cmd += "\r\n";
									break;
								case "CR":
									cmd += "\r";
									break;
								case "LF":
									cmd += "\n";
									break;
								}
								try {
									rs232c.sendData(comPortDebug.portName, cmd.getBytes());
								} catch (Exception e1) {
									JOptionPane.showMessageDialog(comPortDebug.getContentPane(), e1.getMessage(), "Error", JOptionPane.INFORMATION_MESSAGE);
								}
							}
						}
					}
				});
				buttonPane.add(btnSendCommand);
			}
			{
				JButton openPortButton = new JButton("Open Port");
				openPortButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if(1 == e.getButton()){
							String btnText = openPortButton.getText();
							String portName = comboBox_comPort.getSelectedItem().toString();
							if(!"".equals(portName) && "Open Port".equals(btnText)){
								int baudrate = Integer.valueOf(comboBox_baudrate.getSelectedItem().toString());
								int databits = Integer.valueOf(comboBox_databits.getSelectedItem().toString());
								int stopbits = Integer.valueOf(comboBox_stopbits.getSelectedItem().toString());
								int parity = Integer.valueOf(comboBox_parity.getSelectedItem().toString());
								try {
									serialPort = rs232c.openPort(portName, baudrate, databits, stopbits, parity);
									rs232c.addPortListener(serialPort, new MyEventHandler(rs232c, portName));
									comPortDebug.portName = portName;
									openPortButton.setText("Close Port");
									textField_feedback.setText("");
									comboBox_comPort.setEnabled(false);
								} catch (Exception e1) {
									JOptionPane.showMessageDialog(comPortDebug.getContentPane(), e1.getMessage(), "Error", JOptionPane.INFORMATION_MESSAGE);
								}
							}else{
								rs232c.closePort(comPortDebug.portName);
								openPortButton.setText("Open Port");
								textField_feedback.setText("");
								comboBox_comPort.setEnabled(true);
							}
						}
					}
				});
				openPortButton.setActionCommand("openPort");
				buttonPane.add(openPortButton);
				getRootPane().setDefaultButton(openPortButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if(1 == e.getButton()){
							comPortDebug.dispose();
						}
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	//SerialPort Listener
	class MyEventHandler implements SerialPortEventListener {
		private RS232Client rs232c = null;
		private String portName;
		
		public MyEventHandler(RS232Client rs232c, String portName){
			this.rs232c = rs232c;
			this.portName = portName;
		}
		
		@Override
		public void serialEvent(SerialPortEvent arg0) {
			System.out.println(arg0.toString());
			int eventType = arg0.getEventType();
			switch(eventType){
			case SerialPortEvent.BI:
				System.out.println("SerialPortEvent BI:"+String.valueOf(eventType));
				break;
			case SerialPortEvent.CD:
				System.out.println("SerialPortEvent CD:"+String.valueOf(eventType));
				break;
			case SerialPortEvent.CTS:
				System.out.println("SerialPortEvent CTS:"+String.valueOf(eventType));
				break;
			case SerialPortEvent.DATA_AVAILABLE:
				System.out.println("SerialPortEvent DATA_AVAILABLE:"+String.valueOf(eventType));
				try {
					String r = rs232c.readData(portName);
					textField_feedback.setText(r);
					System.out.println(r);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				break;
			case SerialPortEvent.DSR:
				System.out.println("SerialPortEvent DSR:"+String.valueOf(eventType));
				break;
			case SerialPortEvent.FE:
				System.out.println("SerialPortEvent FE:"+String.valueOf(eventType));
				break;
			case SerialPortEvent.OE:
				System.out.println("SerialPortEvent OE:"+String.valueOf(eventType));
				break;
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
				System.out.println("SerialPortEvent OUTPUT_BUFFER_EMPTY:"+String.valueOf(eventType));
				break;
			case SerialPortEvent.PE:
				System.out.println("SerialPortEvent PE:"+String.valueOf(eventType));
				break;
			case SerialPortEvent.RI:
				System.out.println("SerialPortEvent RI:"+String.valueOf(eventType));
				break;
			}
		}
	}
}
