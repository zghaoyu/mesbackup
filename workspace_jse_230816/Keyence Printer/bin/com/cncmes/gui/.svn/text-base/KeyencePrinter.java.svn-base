package com.cncmes.gui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.cncmes.ctrl.SocketClient;
import com.cncmes.handler.SocketDataHandler;
import com.cncmes.utils.MathUtils;
import com.cncmes.utils.ThreadUtils;

import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.ImageIcon;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

public class KeyencePrinter extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	
	private static boolean borderPainted = false;
	private SystemTray systemTray = null;
	private TrayIcon trayIcon = null;
	
	private static ServerSocket svrSocket = null;
	private static final int PORTSVRSOCKET = 20523;
	private JTextField textFieldPrinterIP;
	private JTextField textFieldPort;
	private JLabel lblPrinterResponse;
	private JButton btnConnect;
	private JButton btnGo;
	private JButton btnStartPrinting;
	private JComboBox<String> comboBoxQuery;
	private JList<String> listPrintContent;
	private JList<String> listState;
	
	private static boolean abortPrinting = true;
	private String currBarcode = "";
	private String lastBarcode = "";
	private LinkedHashMap<String,String> barcodeList = new LinkedHashMap<String,String>();
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		checkPreviousInstance();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					KeyencePrinter frame = new KeyencePrinter();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private static void checkPreviousInstance(){
		try {
			svrSocket = new ServerSocket(PORTSVRSOCKET);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Keyence Printer is already started", "Program Launch", JOptionPane.INFORMATION_MESSAGE);
			System.exit(0);
		}
	}
	private void showSystemTray(){
		if(SystemTray.isSupported()){
			if(null == systemTray) systemTray = SystemTray.getSystemTray();
			if(null != trayIcon) systemTray.remove(trayIcon);
			
			PopupMenu popup = new PopupMenu();
			MenuItem mainMenuItem = new MenuItem("Show Main GUI");
			mainMenuItem.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(true);
				}
			});
			popup.add(mainMenuItem);
			
			trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(KeyencePrinter.class.getResource("/com/cncmes/img/Butterfly_blue_24.png")),"Keyence Printer",popup);
			trayIcon.setImageAutoSize(true);
			trayIcon.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(true);
				}
			});
			
			try {
				systemTray.add(trayIcon);
			} catch (AWTException e1) {
			}
		}
	}
	
	/**
	 * Create the frame.
	 */
	private KeyencePrinter() {
		abortPrinting = true;
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(KeyencePrinter.class.getResource("/com/cncmes/img/Butterfly_blue_24.png")));
		setTitle("Keyence Printer");
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				showSystemTray();
			}
		});
		
		int width = 640;
		int height = 500;
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.setIcon(new ImageIcon(KeyencePrinter.class.getResource("/com/cncmes/img/Exit_16.png")));
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(16 == e.getModifiers()){
					int rtn = JOptionPane.showConfirmDialog(contentPane, "Are you sure to quit from Keyence Printer?", "Exit", JOptionPane.YES_NO_OPTION);
					if(0 == rtn){
						if(null != svrSocket){
							try {
								svrSocket.close();
							} catch (IOException e1) {
							}
						}
						System.exit(0);
					}
				}
			}
		});
		
		JMenuItem mntmLoadBarcodeList = new JMenuItem("Load Barcode List From File");
		mntmLoadBarcodeList.setIcon(new ImageIcon(KeyencePrinter.class.getResource("/com/cncmes/img/folderOpen_16.png")));
		mntmLoadBarcodeList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(16 == e.getModifiers()){
					JFileChooser jfc = new JFileChooser();
					jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
					jfc.setCurrentDirectory(new File(System.getProperty("user.dir")));
					jfc.showDialog(contentPane, "Open");
					
					File file = jfc.getSelectedFile();
					if(file != null){
						String fileName = file.getName();
						String sourcePath = file.getAbsolutePath();
						
						if(file.isFile()){
							if(fileName.toLowerCase().endsWith(".txt")){
								File f = new File(sourcePath);
								BufferedReader br = null;
								try {
									br = new BufferedReader(new FileReader(f));
									String errBarcode = "";
									String okBarcode = "";
									String readLine = "";
									while(null != (readLine = br.readLine())){
										if(readLine.length() < 6 || readLine.length() > 38){
											if(!"".equals(readLine)) errBarcode = "Error Barcode: "+readLine+"\n";
										}else{
											if("".equals(okBarcode)){
												okBarcode = readLine;
											}else{
												okBarcode += "," + readLine;
											}
										}
										
										if(!"".equals(errBarcode) && !"".equals(okBarcode)){
											if(0 != JOptionPane.showConfirmDialog(contentPane, "There are invalid barcode in the file\n"+errBarcode+"Continue anyway?", "Continue to load barcode?", JOptionPane.YES_NO_OPTION)){
												return;
											}
										}
									}
									
									if(!"".equals(okBarcode)){
										String[] barcodes = okBarcode.split(",");
										String[] printState = new String[barcodes.length];
										barcodeList.clear();
										for(int i=0; i<barcodes.length; i++){
											printState[i] = "Standby";
											barcodeList.put(barcodes[i], printState[i]);
										}
										setListContent(listPrintContent,barcodes);
										setListContent(listState,printState);
									}
									
								} catch (Exception e1) {
									JOptionPane.showMessageDialog(contentPane, "Open barcode list file("+fileName+") failed.\nError: "+e1.getMessage(), "Open File Error", JOptionPane.ERROR_MESSAGE);
								} finally {
									try {
										if(null != br) br.close();
									} catch (IOException e1) {
									}
								}
							}else{
								JOptionPane.showMessageDialog(contentPane, "Barcode list file("+fileName+") is not a txt file", "Open File Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			}
		});
		mnFile.add(mntmLoadBarcodeList);
		mnFile.add(mntmExit);
		
		JMenu mnAbout = new JMenu("About");
		menuBar.add(mnAbout);
		
		JMenuItem mntmVersionInfo = new JMenuItem("Version Info");
		mnAbout.add(mntmVersionInfo);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panelTop = new JPanel();
		panelTop.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		contentPane.add(panelTop, BorderLayout.NORTH);
		GridBagLayout gbl_panelTop = new GridBagLayout();
		gbl_panelTop.columnWidths = new int[] {70, 120, 40, 60, 120, 40, 110, 48, 2};
		gbl_panelTop.rowHeights = new int[] {30};
		gbl_panelTop.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0};
		gbl_panelTop.rowWeights = new double[]{0.0};
		panelTop.setLayout(gbl_panelTop);
		
		JLabel lblPrinterIp = new JLabel("Printer IP");
		GridBagConstraints gbc_lblPrinterIp = new GridBagConstraints();
		gbc_lblPrinterIp.anchor = GridBagConstraints.EAST;
		gbc_lblPrinterIp.insets = new Insets(0, 0, 0, 5);
		gbc_lblPrinterIp.gridx = 0;
		gbc_lblPrinterIp.gridy = 0;
		panelTop.add(lblPrinterIp, gbc_lblPrinterIp);
		
		textFieldPrinterIP = new JTextField();
		textFieldPrinterIP.setText("192.168.0.100");
		GridBagConstraints gbc_textFieldPrinterIP = new GridBagConstraints();
		gbc_textFieldPrinterIP.insets = new Insets(0, 0, 0, 5);
		gbc_textFieldPrinterIP.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldPrinterIP.gridx = 1;
		gbc_textFieldPrinterIP.gridy = 0;
		panelTop.add(textFieldPrinterIP, gbc_textFieldPrinterIP);
		textFieldPrinterIP.setColumns(10);
		
		JLabel lblPort = new JLabel("Port");
		GridBagConstraints gbc_lblPort = new GridBagConstraints();
		gbc_lblPort.anchor = GridBagConstraints.EAST;
		gbc_lblPort.insets = new Insets(0, 0, 0, 5);
		gbc_lblPort.gridx = 2;
		gbc_lblPort.gridy = 0;
		panelTop.add(lblPort, gbc_lblPort);
		
		textFieldPort = new JTextField();
		textFieldPort.setText("9004");
		GridBagConstraints gbc_textFieldPort = new GridBagConstraints();
		gbc_textFieldPort.insets = new Insets(0, 0, 0, 5);
		gbc_textFieldPort.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldPort.gridx = 3;
		gbc_textFieldPort.gridy = 0;
		panelTop.add(textFieldPort, gbc_textFieldPort);
		textFieldPort.setColumns(10);
		
		btnConnect = new JButton("Connect");
		btnConnect.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(1 == e.getButton() && btnConnect.isEnabled()){
					String ip = textFieldPrinterIP.getText().trim();
					int port = Integer.parseInt(textFieldPort.getText().trim());
					String btnText = btnConnect.getText();
					if(!"".equals(ip) && port>1024){
						if("Connect".equals(btnText)){
							try {
								if(SocketClient.getInstance().connect(ip, port, new SocketClientDataHandler())){
									btnConnect.setText("Disconnect");
									setEnabledState(true);
								}
							} catch (IOException e1) {
								JOptionPane.showMessageDialog(contentPane, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
							}
						}else{
							try {
								SocketClient.getInstance().sendData(ip, port, "quit");
							} catch (IOException e1) {
								JOptionPane.showMessageDialog(contentPane, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
							} finally {
								btnConnect.setText("Connect");
								setEnabledState(false);
							}
						}
					}else{
						JOptionPane.showMessageDialog(contentPane, "IP or port is not correct", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		GridBagConstraints gbc_btnConnect = new GridBagConstraints();
		gbc_btnConnect.insets = new Insets(0, 0, 0, 5);
		gbc_btnConnect.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnConnect.gridx = 4;
		gbc_btnConnect.gridy = 0;
		panelTop.add(btnConnect, gbc_btnConnect);
		
		JLabel lblQuery = new JLabel("Query");
		GridBagConstraints gbc_lblQuery = new GridBagConstraints();
		gbc_lblQuery.insets = new Insets(0, 0, 0, 5);
		gbc_lblQuery.anchor = GridBagConstraints.EAST;
		gbc_lblQuery.gridx = 5;
		gbc_lblQuery.gridy = 0;
		panelTop.add(lblQuery, gbc_lblQuery);
		
		comboBoxQuery = new JComboBox<String>();
		comboBoxQuery.setModel(new DefaultComboBoxModel<String>(new String[] {"Error Code", "System Status", "Linear Setting", "Group Condition", "Counter Condition", "Counter Value", "Counter Repeats", "Total Print Count", "System Date Time", "Current Print String", "Last Print String"}));
		GridBagConstraints gbc_comboBoxQuery = new GridBagConstraints();
		gbc_comboBoxQuery.insets = new Insets(0, 0, 0, 5);
		gbc_comboBoxQuery.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxQuery.gridx = 6;
		gbc_comboBoxQuery.gridy = 0;
		panelTop.add(comboBoxQuery, gbc_comboBoxQuery);
		
		btnGo = new JButton("GO");
		btnGo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if("Connect".equals(btnConnect.getText())) return;
				if(1 == e.getButton() && btnGo.isEnabled()){
					btnGo.setEnabled(false);
					
					String ip = textFieldPrinterIP.getText().trim();
					int port = Integer.parseInt(textFieldPort.getText().trim());
					String queryInfo = comboBoxQuery.getSelectedItem().toString();
					try {
						queryPrinterInfo(ip, port, queryInfo);
					} catch (IOException e1) {
						lblPrinterResponse.setText("Error: " + e1.getMessage());
					}
					
					btnGo.setEnabled(true);
				}
			}
		});
		btnGo.setEnabled(false);
		GridBagConstraints gbc_btnGo = new GridBagConstraints();
		gbc_btnGo.gridx = 7;
		gbc_btnGo.gridy = 0;
		panelTop.add(btnGo, gbc_btnGo);
		
		JPanel panelCenter = new JPanel();
		panelCenter.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		contentPane.add(panelCenter, BorderLayout.CENTER);
		GridBagLayout gbl_panelCenter = new GridBagLayout();
		gbl_panelCenter.columnWidths = new int[] {350, 140, 130, 2};
		gbl_panelCenter.rowHeights = new int[] {30, 338, 2};
		gbl_panelCenter.columnWeights = new double[]{0.0, 1.0, 0.0};
		gbl_panelCenter.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		panelCenter.setLayout(gbl_panelCenter);
		
		JLabel lblPrintContent = new JLabel("Printing Sequence - Barcode List");
		GridBagConstraints gbc_lblPrintContent = new GridBagConstraints();
		gbc_lblPrintContent.insets = new Insets(0, 0, 5, 5);
		gbc_lblPrintContent.gridx = 0;
		gbc_lblPrintContent.gridy = 0;
		panelCenter.add(lblPrintContent, gbc_lblPrintContent);
		
		JLabel lblState = new JLabel("State");
		GridBagConstraints gbc_lblState = new GridBagConstraints();
		gbc_lblState.insets = new Insets(0, 0, 5, 5);
		gbc_lblState.gridx = 1;
		gbc_lblState.gridy = 0;
		panelCenter.add(lblState, gbc_lblState);
		
		JLabel lblOperation = new JLabel("Operation");
		GridBagConstraints gbc_lblOperation = new GridBagConstraints();
		gbc_lblOperation.insets = new Insets(0, 0, 5, 0);
		gbc_lblOperation.gridx = 2;
		gbc_lblOperation.gridy = 0;
		panelCenter.add(lblOperation, gbc_lblOperation);
		
		JScrollPane scrollPanePrintContent = new JScrollPane();
		GridBagConstraints gbc_scrollPanePrintContent = new GridBagConstraints();
		gbc_scrollPanePrintContent.fill = GridBagConstraints.BOTH;
		gbc_scrollPanePrintContent.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPanePrintContent.gridx = 0;
		gbc_scrollPanePrintContent.gridy = 1;
		panelCenter.add(scrollPanePrintContent, gbc_scrollPanePrintContent);
		
		listPrintContent = new JList<String>();
		listPrintContent.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(1 == e.getButton()){
					int index = listPrintContent.getSelectedIndex();
					listState.setSelectedIndex(index);
				}
			}
		});
		setListContent(listPrintContent, null);
		listPrintContent.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		scrollPanePrintContent.setViewportView(listPrintContent);
		
		JScrollPane scrollPaneState = new JScrollPane();
		GridBagConstraints gbc_scrollPaneState = new GridBagConstraints();
		gbc_scrollPaneState.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPaneState.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneState.gridx = 1;
		gbc_scrollPaneState.gridy = 1;
		panelCenter.add(scrollPaneState, gbc_scrollPaneState);
		
		listState = new JList<String>();
		listState.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(1 == e.getButton()){
					int index = listState.getSelectedIndex();
					listPrintContent.setSelectedIndex(index);
				}
			}
		});
		setListContent(listState, null);
		scrollPaneState.setViewportView(listState);
		
		JPanel panelPrintOP = new JPanel();
		GridBagConstraints gbc_panelPrintOP = new GridBagConstraints();
		gbc_panelPrintOP.anchor = GridBagConstraints.WEST;
		gbc_panelPrintOP.fill = GridBagConstraints.VERTICAL;
		gbc_panelPrintOP.gridx = 2;
		gbc_panelPrintOP.gridy = 1;
		panelCenter.add(panelPrintOP, gbc_panelPrintOP);
		GridBagLayout gbl_panelPrintOP = new GridBagLayout();
		gbl_panelPrintOP.columnWidths = new int[]{0, 0};
		gbl_panelPrintOP.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panelPrintOP.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panelPrintOP.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panelPrintOP.setLayout(gbl_panelPrintOP);
		
		btnStartPrinting = new JButton("");
		btnStartPrinting.setEnabled(false);
		GridBagConstraints gbc_btnStartPrinting = new GridBagConstraints();
		gbc_btnStartPrinting.insets = new Insets(0, 0, 5, 0);
		gbc_btnStartPrinting.gridx = 0;
		gbc_btnStartPrinting.gridy = 0;
		panelPrintOP.add(btnStartPrinting, gbc_btnStartPrinting);
		btnStartPrinting.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				if(btnStartPrinting.isEnabled()){
					btnStartPrinting.setBorderPainted(borderPainted);
					borderPainted = !borderPainted;
				}
			}
		});
		btnStartPrinting.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if("Connect".equals(btnConnect.getText())) return;
				if(1 == e.getButton() && btnStartPrinting.isEnabled()){
					String tips = btnStartPrinting.getToolTipText();
					if("Start Printing".equals(tips)){
						if(barcodeList.isEmpty()){
							JOptionPane.showMessageDialog(contentPane, "There are no barcodes available to print", "No barcodes available", JOptionPane.INFORMATION_MESSAGE);
							return;
						}
						
						btnStartPrinting.setToolTipText("Stop Printing");
						btnStartPrinting.setIcon(new ImageIcon(KeyencePrinter.class.getResource("/com/cncmes/img/stop_128.png")));
						abortPrinting = false;
						ThreadUtils.Run(new PrintingBarcode());
					}else{
						abortPrinting = true;
					}
				}
			}
		});
		btnStartPrinting.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnStartPrinting.setIcon(new ImageIcon(KeyencePrinter.class.getResource("/com/cncmes/img/printing_128.png")));
		btnStartPrinting.setToolTipText("Start Printing");
		
		JPanel panelBottom = new JPanel();
		panelBottom.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		contentPane.add(panelBottom, BorderLayout.SOUTH);
		
		lblPrinterResponse = new JLabel("Connect the printer first before operation");
		panelBottom.add(lblPrinterResponse);
	}
	
	private void setEnabledState(boolean connOK) {
		textFieldPrinterIP.setEnabled(!connOK);
		textFieldPort.setEnabled(!connOK);
		btnGo.setEnabled(connOK);
		btnStartPrinting.setEnabled(connOK);
		
		if(connOK){
			lblPrinterResponse.setText("Printer Ready");
		}else{
			lblPrinterResponse.setText("Connect the printer first before operation");
		}
	}
	
	private void setListContent(JList<String> jList, String[] content) {
		String[] values;
		if(null != content && content.length > 0){
			values = content;
		}else{
			values = new String[]{""};
		}
		
		jList.setModel(new AbstractListModel<String>() {
			private static final long serialVersionUID = 2L;
			public int getSize() {
				return values.length;
			}
			public String getElementAt(int index) {
				return values[index];
			}
		});
		jList.setSelectedIndex(0);
		jList.repaint();
	}
	
	private void setListSelectedIndex(String printContent){
		String[] states = new String[barcodeList.size()];
		int idx = -1;
		int selectedIdx = 0;
		
		for(String key:barcodeList.keySet()){
			idx++;
			states[idx] = barcodeList.get(key);
			if(printContent.equals(listPrintContent.getModel().getElementAt(idx))){
				selectedIdx = idx;
				listPrintContent.setSelectedIndex(idx);
				listPrintContent.repaint();
			}
		}
		
		setListContent(listState,states);
		listState.setSelectedIndex(selectedIdx);
		listState.repaint();
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
	
	private boolean setPrintBarcode(String ip, int port, String printBarcode){
		boolean OK = false;
		if (!"".equals(printBarcode)){
			try {
				String cmd = "BE,1,1," + printBarcode + MathUtils.MD5Encode(printBarcode);
				SocketClient.getInstance().sendData(ip, port, cmd);
				currBarcode = printBarcode;
				OK = true;
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(contentPane, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		return OK;
	}
	
	private void queryPrinterInfo(String ip, int port, String infoDesc) throws IOException{
		SocketClient sc = SocketClient.getInstance();
		if("System Status".equals(infoDesc)) sc.sendData(ip, port, "SB");
		if("Linear Setting".equals(infoDesc)) sc.sendData(ip, port, "FL,1,0");
		if("Group Condition".equals(infoDesc)) sc.sendData(ip, port, "F6,1,1");
		if("Counter Condition".equals(infoDesc)) sc.sendData(ip, port, "CP,0,L");
		if("Counter Value".equals(infoDesc)) sc.sendData(ip, port, "CN,0,A");
		if("Counter Repeats".equals(infoDesc)) sc.sendData(ip, port, "CR,0,A");
		if("Total Print Count".equals(infoDesc)) sc.sendData(ip, port, "KH,1");
		if("System Date Time".equals(infoDesc)) sc.sendData(ip, port, "DB");
		if("Error Code".equals(infoDesc)) sc.sendData(ip, port, "EV");
		
		if("Current Print String".equals(infoDesc)){
			sc.sendData(ip, port, "BF,1,1");
		}
		if("Last Print String".equals(infoDesc)){
			sc.sendData(ip, port, "UZ,1,0");
		}
	}
	
	class SocketClientDataHandler extends SocketDataHandler{
		@Override
		public void doHandle(String in, Socket s) {
			if(abortPrinting) lblPrinterResponse.setText(in);
			
			String[] arrIn = in.split(",");
			String md5 = "",val = "";
			if(arrIn.length > 0){
				switch(arrIn[0]){
				case "SB":
					if(arrIn.length > 1){
						lblPrinterResponse.setText(arrIn[1]+":"+getPrinterStatus(arrIn[1]));
					}
					break;
				case "FL"://Page 10
					break;
				case "F6"://Page 12
					break;
				case "BF"://Page 16
					if(arrIn.length > 3 && arrIn[3].length() > 32){
						val = arrIn[3].substring(0, arrIn[3].length() - 32);
						md5 = arrIn[3].substring(arrIn[3].length() - 32);
						if(md5.equals(MathUtils.MD5Encode(val))){
							currBarcode = val;
							if(abortPrinting) lblPrinterResponse.setText("Current Barcode = "+currBarcode);
						}else{
							if(abortPrinting) lblPrinterResponse.setText("Current Barcode Validation Failed: "+in);
						}
					}else{
						if(abortPrinting) lblPrinterResponse.setText("Get Current Barcode Error: "+in);
					}
					break;
				case "UZ"://Page 14
					if(arrIn.length > 3 && arrIn[3].length() > 32){
						val = arrIn[3].substring(0, arrIn[3].length() - 32);
						md5 = arrIn[3].substring(arrIn[3].length() - 32);
						if(md5.equals(MathUtils.MD5Encode(val))){
							lastBarcode = val;
							if(abortPrinting) lblPrinterResponse.setText("Last Barcode = "+lastBarcode);
						}else{
							if(abortPrinting) lblPrinterResponse.setText("Last Barcode Validation Failed: "+in);
						}
					}else{
						if(abortPrinting) lblPrinterResponse.setText("Get Last Barcode Error: "+in);
					}
					break;
				case "CP"://Page 14
					break;
				case "CN"://Page 15
					break;
				case "CR"://Page 15
					break;
				case "KH"://Page 23
					break;
				case "DB"://Page 24
					break;
				case "EV"://Page 7
					if(arrIn.length > 1){
						if(Integer.parseInt(arrIn[1]) > 0){
							lblPrinterResponse.setText("Error:"+arrIn[1]);
						}else{
							lblPrinterResponse.setText("OK:"+arrIn[1]);
						}
					}else{
						lblPrinterResponse.setText("Error:"+in);
					}
					break;
				}
			}
		}
	}
	
	class PrintingBarcode implements Runnable{
		private String ip = textFieldPrinterIP.getText();
		private int port = Integer.parseInt(textFieldPort.getText());
		private int retryCount = 60; //one minute
		private int retryInterval = 1000; //ms
		
		@Override
		public void run() {
			System.out.println("PrintingBarcode "+Thread.currentThread().getName()+" started");
			
			btnGo.setEnabled(false);
			boolean printFinish = true;
			for(String barcode:barcodeList.keySet()){
				if(abortPrinting){
					JOptionPane.showMessageDialog(contentPane, "Print barcode "+barcode+" abort", "Printing Abort", JOptionPane.INFORMATION_MESSAGE);
					break;
				}
				
				String state = barcodeList.get(barcode);
				if("Standby".equals(state)){
					state = "Printing";
					barcodeList.put(barcode, state);
					setListSelectedIndex(barcode);
					if(!setPrintBarcode(ip, port, barcode)) break;
					lblPrinterResponse.setText("Printing "+barcode+ " ... ");
				}
				
				if("Printing".equals(state)){
					int retry = retryCount;
					boolean printOK = false;
					try {
						while(retry > 0){
							if(abortPrinting) break;
							retry--;
							lblPrinterResponse.setText("Printing "+barcode+ " ... "+retry);
							queryPrinterInfo(ip, port, "Last Print String");
							if(barcode.equals(lastBarcode)){
								barcodeList.put(barcode, "Finish");
								setListSelectedIndex(barcode);
								printOK = true;
								break;
							}else{
								long curTime = System.currentTimeMillis();
								while(true){
									if(System.currentTimeMillis() - curTime > retryInterval) break;
								}
							}
						}
						
						if(abortPrinting){
							lblPrinterResponse.setText("Print barcode "+barcode+" abort");
							break;
						}
						if(!printOK){
							lblPrinterResponse.setText("Print barcode "+barcode+" time out");
							JOptionPane.showMessageDialog(contentPane, "Print barcode "+barcode+" time out", "Printing Timeout", JOptionPane.ERROR_MESSAGE);
							break;
						}
					} catch (IOException e) {
						printFinish = false;
						lblPrinterResponse.setText("Query Last Print String Error: "+e.getMessage());
					}
				}
			}
			
			if(printFinish) lblPrinterResponse.setText("Printer is ready");
			btnGo.setEnabled(true);
			btnStartPrinting.setToolTipText("Start Printing");
			btnStartPrinting.setIcon(new ImageIcon(KeyencePrinter.class.getResource("/com/cncmes/img/printing_128.png")));
		}
	}
}
