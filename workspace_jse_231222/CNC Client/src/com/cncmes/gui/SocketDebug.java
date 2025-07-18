package com.cncmes.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.cncmes.ctrl.SocketClient;
import com.cncmes.ctrl.SocketServer;
import com.cncmes.handler.SocketDataHandler;
import com.cncmes.handler.SocketRespHandler;
import com.cncmes.thread.ThreadController;

import java.awt.Toolkit;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.Socket;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTextArea;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

public class SocketDebug extends JDialog {
	private final JPanel contentPanel = new JPanel();
	private static SocketDebug socketDebug = new SocketDebug();
	private SocketServer socketSvr = null;
	private Socket socket = null;
	private static final long serialVersionUID = 4L;
	private JTextField textField_IP;
	private JTextField textField_port;
	private JTextField txtTestcmd;
	private JTextArea txtrClientRecieve;
	private JTextArea txtrServerReceive;
	private JComboBox<String> comboBoxTerminator;
	
	public static SocketDebug getInstance(){
		return socketDebug;
	}
	
	/**
	 * Create the dialog.
	 */
	private SocketDebug() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(SocketDebug.class.getResource("/com/cncmes/img/Butterfly_24.png")));
		setTitle("Socket Test");
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 450;
		int height = 360;
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] {120, 302};
		gbl_contentPanel.rowHeights = new int[] {0, 33, 33, 33, 33, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 1.0, 1.0, 0.0, 0.0};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblIp = new JLabel(" Server IP");
			GridBagConstraints gbc_lblIp = new GridBagConstraints();
			gbc_lblIp.anchor = GridBagConstraints.WEST;
			gbc_lblIp.fill = GridBagConstraints.VERTICAL;
			gbc_lblIp.insets = new Insets(0, 0, 5, 5);
			gbc_lblIp.gridx = 0;
			gbc_lblIp.gridy = 0;
			contentPanel.add(lblIp, gbc_lblIp);
		}
		{
			textField_IP = new JTextField();
			textField_IP.setText("127.0.0.1");
			GridBagConstraints gbc_textField_IP = new GridBagConstraints();
			gbc_textField_IP.fill = GridBagConstraints.BOTH;
			gbc_textField_IP.insets = new Insets(0, 0, 5, 0);
			gbc_textField_IP.gridx = 1;
			gbc_textField_IP.gridy = 0;
			contentPanel.add(textField_IP, gbc_textField_IP);
			textField_IP.setColumns(10);
		}
		{
			JLabel lblPort = new JLabel(" Server Port");
			GridBagConstraints gbc_lblPort = new GridBagConstraints();
			gbc_lblPort.anchor = GridBagConstraints.WEST;
			gbc_lblPort.fill = GridBagConstraints.VERTICAL;
			gbc_lblPort.insets = new Insets(0, 0, 5, 5);
			gbc_lblPort.gridx = 0;
			gbc_lblPort.gridy = 1;
			contentPanel.add(lblPort, gbc_lblPort);
		}
		{
			textField_port = new JTextField();
			textField_port.setText("9004");
			GridBagConstraints gbc_textField_port = new GridBagConstraints();
			gbc_textField_port.fill = GridBagConstraints.BOTH;
			gbc_textField_port.insets = new Insets(0, 0, 5, 0);
			gbc_textField_port.gridx = 1;
			gbc_textField_port.gridy = 1;
			contentPanel.add(textField_port, gbc_textField_port);
			textField_port.setColumns(10);
		}
		{
			JLabel lblServerRecieve = new JLabel(" Server Recieve");
			GridBagConstraints gbc_lblServerRecieve = new GridBagConstraints();
			gbc_lblServerRecieve.insets = new Insets(0, 0, 5, 5);
			gbc_lblServerRecieve.anchor = GridBagConstraints.WEST;
			gbc_lblServerRecieve.gridx = 0;
			gbc_lblServerRecieve.gridy = 2;
			contentPanel.add(lblServerRecieve, gbc_lblServerRecieve);
		}
		{
			txtrServerReceive = new JTextArea();
			txtrServerReceive.setLineWrap(true);
			txtrServerReceive.setEditable(false);
			GridBagConstraints gbc_txtrServerReceive = new GridBagConstraints();
			gbc_txtrServerReceive.insets = new Insets(0, 0, 5, 0);
			gbc_txtrServerReceive.fill = GridBagConstraints.BOTH;
			gbc_txtrServerReceive.gridx = 1;
			gbc_txtrServerReceive.gridy = 2;
			contentPanel.add(txtrServerReceive, gbc_txtrServerReceive);
		}
		{
			JLabel lblFeedback = new JLabel(" Client Recieve");
			GridBagConstraints gbc_lblFeedback = new GridBagConstraints();
			gbc_lblFeedback.anchor = GridBagConstraints.WEST;
			gbc_lblFeedback.fill = GridBagConstraints.VERTICAL;
			gbc_lblFeedback.insets = new Insets(0, 0, 5, 5);
			gbc_lblFeedback.gridx = 0;
			gbc_lblFeedback.gridy = 3;
			contentPanel.add(lblFeedback, gbc_lblFeedback);
		}
		{
			txtrClientRecieve = new JTextArea();
			txtrClientRecieve.setLineWrap(true);
			txtrClientRecieve.setEditable(false);
			GridBagConstraints gbc_txtrClientRecieve = new GridBagConstraints();
			gbc_txtrClientRecieve.insets = new Insets(0, 0, 5, 0);
			gbc_txtrClientRecieve.fill = GridBagConstraints.BOTH;
			gbc_txtrClientRecieve.gridx = 1;
			gbc_txtrClientRecieve.gridy = 3;
			contentPanel.add(txtrClientRecieve, gbc_txtrClientRecieve);
		}
		{
			JLabel lblCommand = new JLabel(" Client Command");
			GridBagConstraints gbc_lblCommand = new GridBagConstraints();
			gbc_lblCommand.anchor = GridBagConstraints.WEST;
			gbc_lblCommand.fill = GridBagConstraints.VERTICAL;
			gbc_lblCommand.insets = new Insets(0, 0, 5, 5);
			gbc_lblCommand.gridx = 0;
			gbc_lblCommand.gridy = 4;
			contentPanel.add(lblCommand, gbc_lblCommand);
		}
		{
			txtTestcmd = new JTextField();
			txtTestcmd.setText("TestCmd");
			GridBagConstraints gbc_txtTestcmd = new GridBagConstraints();
			gbc_txtTestcmd.insets = new Insets(0, 0, 5, 0);
			gbc_txtTestcmd.fill = GridBagConstraints.BOTH;
			gbc_txtTestcmd.gridx = 1;
			gbc_txtTestcmd.gridy = 4;
			contentPanel.add(txtTestcmd, gbc_txtTestcmd);
			txtTestcmd.setColumns(10);
		}
		{
			JLabel lblTerminator = new JLabel(" Terminator");
			GridBagConstraints gbc_lblTerminator = new GridBagConstraints();
			gbc_lblTerminator.anchor = GridBagConstraints.WEST;
			gbc_lblTerminator.insets = new Insets(0, 0, 0, 5);
			gbc_lblTerminator.gridx = 0;
			gbc_lblTerminator.gridy = 5;
			contentPanel.add(lblTerminator, gbc_lblTerminator);
		}
		{
			comboBoxTerminator = new JComboBox<String>();
			comboBoxTerminator.setModel(new DefaultComboBoxModel<String>(new String[] {"CRLF", "CR", "LF", "HEX"}));
			GridBagConstraints gbc_comboBoxTerminator = new GridBagConstraints();
			gbc_comboBoxTerminator.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBoxTerminator.gridx = 1;
			gbc_comboBoxTerminator.gridy = 5;
			contentPanel.add(comboBoxTerminator, gbc_comboBoxTerminator);
		}
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			FlowLayout fl_buttonPane = new FlowLayout(FlowLayout.CENTER);
			fl_buttonPane.setHgap(8);
			buttonPane.setLayout(fl_buttonPane);
			{
				JButton btnStartServer = new JButton("Start Server");
				btnStartServer.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int port = Integer.valueOf(textField_port.getText());
						if(16 == e.getModifiers() && port > 1024){
							String btnText = btnStartServer.getText();
							if("Start Server".equals(btnText)){
								ThreadController.initStopFlag();
								socketSvr = SocketServer.getInstance();
								try {
									socketSvr.socketSvrStart(port, new SocketServerRespHandler());
									btnStartServer.setText("Stop Server");
								} catch (IOException e1) {
									JOptionPane.showMessageDialog(socketDebug.getContentPane(), e1.getMessage(), "Error", JOptionPane.INFORMATION_MESSAGE);
								}
							}else{
								socketSvr.stopSvrPort(port);
								btnStartServer.setText("Start Server");
							}
						}
					}
				});
				buttonPane.add(btnStartServer);
			}
			JButton btnSendCmd = new JButton("Send Command");
			buttonPane.add(btnSendCmd);
			{
				JButton connButton = new JButton("Connect");
				buttonPane.add(connButton);
				connButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if(1 == e.getButton()){
							String btnText = connButton.getText();
							String ip = textField_IP.getText().trim();
							int port = Integer.valueOf(textField_port.getText().trim());
							String cmdend = comboBoxTerminator.getSelectedItem().toString();
							txtrClientRecieve.setText("");
							if ("Connect".equals(btnText)){
								if("".equals(ip) || port <= 1024){
									JOptionPane.showMessageDialog(socketDebug.getContentPane(), "IP or port error!", "Error", JOptionPane.INFORMATION_MESSAGE);
								}else{
									try {
										socket = SocketClient.getInstance().connect(ip, port, new SocketClientDataHandler(), cmdend);
										if(null != socket){
											ThreadController.initStopFlag();
											connButton.setText("Disconnect");
											textField_IP.setEnabled(false);
											textField_port.setEnabled(false);
										}
									} catch (IOException e1) {
										System.out.println("mouseClicked(Connect) error: "+e1.getMessage());
										JOptionPane.showMessageDialog(socketDebug.getContentPane(), e1.getMessage(), "Error", JOptionPane.INFORMATION_MESSAGE);
									}
								}
							}else{
								try {
									SocketClient.getInstance().sendData(socket, "quit", cmdend);
								} catch (IOException e1) {
									System.out.println("mouseClicked(Disconnect) error: "+e1.getMessage());
									JOptionPane.showMessageDialog(socketDebug.getContentPane(), e1.getMessage(), "Error", JOptionPane.INFORMATION_MESSAGE);
								} finally {
									connButton.setText("Connect");
									textField_IP.setEnabled(true);
									textField_port.setEnabled(true);
								}
							}
						}
					}
				});
				connButton.setActionCommand("Connect");
				getRootPane().setDefaultButton(connButton);
			}
			{
				JButton btnCancel = new JButton("Cancel");
				btnCancel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(16 == e.getModifiers()){
							socketDebug.dispose();
						}
					}
				});
				buttonPane.add(btnCancel);
			}
			btnSendCmd.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(1 == e.getButton()){
						String cmd = txtTestcmd.getText().trim();
//						String ip = textField_IP.getText().trim();
//						int port = Integer.valueOf(textField_port.getText().trim());
						String cmdend = comboBoxTerminator.getSelectedItem().toString();
						
						if (!"".equals(cmd)){
							try {
								if(SocketClient.getInstance().sendData(socket, cmd, cmdend)){
									System.out.println("sendData - " + cmd);
								}
							} catch (IOException e1) {
								System.out.println("mouseClicked(Send Cmd) error: "+e1.getMessage());
								JOptionPane.showMessageDialog(socketDebug.getContentPane(), e1.getMessage(), "Error", JOptionPane.INFORMATION_MESSAGE);
							}
						}
					}
				}
			});
		}
	}
	
	class SocketClientDataHandler extends SocketDataHandler{
		@Override
		public void doHandle(String in, Socket s) {
			txtrClientRecieve.setText(in);
		}
		
	}
	
	class SocketServerRespHandler implements SocketRespHandler{
		@Override
		public void doHandle(String in, Socket s) {
			txtrServerReceive.setText(in);
			if(null!=in){
				char[] chrs = in.toCharArray();
				for(char chr:chrs){
					System.out.print(chr);
				}
			}
			if("quit".equals(in)){
				socketDebug.setTitle("Socket Test");
			}else{
				if(null != s){
					String clientInfo = s.getInetAddress().toString()+":"+String.valueOf(s.getPort());
					String svrInfo = String.valueOf(s.getLocalPort());
					socketDebug.setTitle("Socket Test ["+clientInfo+"->"+svrInfo+"]");
				}
			}
		}
	}
}