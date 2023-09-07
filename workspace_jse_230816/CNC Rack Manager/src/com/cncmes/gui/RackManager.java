package com.cncmes.gui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import com.cncmes.base.DeviceState;
import com.cncmes.base.PermissionItems;
import com.cncmes.base.RackItems;
import com.cncmes.base.SchedulerItems;
import com.cncmes.base.SpecItems;
import com.cncmes.base.WorkpieceItems;
import com.cncmes.ctrl.CtrlCenterClient;
import com.cncmes.ctrl.RackServer;
import com.cncmes.ctrl.SchedulerClient;
import com.cncmes.ctrl.SocketServer;
import com.cncmes.data.Mescode;
import com.cncmes.data.RackData;
import com.cncmes.data.RackMaterial;
import com.cncmes.data.RackProduct;
import com.cncmes.data.RackTemp;
import com.cncmes.data.WorkpieceData;
import com.cncmes.thread.ThreadController;
import com.cncmes.utils.DataUtils;
import com.cncmes.utils.LoginSystem;
import com.cncmes.utils.MyFileUtils;
import com.cncmes.utils.MySystemUtils;
import com.cncmes.utils.RunningMsg;
import com.cncmes.utils.TimeUtils;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.ImageIcon;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import java.awt.GridBagConstraints;
import javax.swing.JComboBox;
import javax.swing.JDialog;

import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;

import javax.swing.DefaultComboBoxModel;
import javax.swing.border.BevelBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ItemEvent;

public class RackManager extends JFrame {
	private static final long serialVersionUID = 17L;
	private JPanel contentPane;
	private JButton btnStart;
	private JButton btnStop;
	private JButton btnLogin;
	private JButton btnConfig;
	private JMenuItem mntmStart;
	private JMenuItem mntmStop;
	private JTable tableRackMaterial;
	private JTable tableRackProduct;
	private JComboBox<String> comboBoxMrackLineName;
	private JComboBox<String> comboBoxPrackLineName;
	private JScrollPane scrollPaneRackMaterial;
	private JScrollPane scrollPaneRackProduct;
	
	private MyTableModel materialTableModel;
	private MyTableModel productTableModel;
	private static Object[][] materialRawData;
	private static Object[][] materialNewData;
	private static Object[][] productRawData;
	private static Object[][] productNewData;
	private String materialOriLine = "All";
	private String materialCurLine = "All";
	private String productOriLine = "All";
	private String productCurLine = "All";
	private String materialLastPointer = "";
	
	private JLabel lblMrackTotal;
	private JLabel lblMrackStandby;
	private JLabel lblMrackPlan;
	private JLabel lblMrackEmptySlots;
	private JLabel lblWorkingWorkpieces;
	private JLabel lblPrackTotal;
	private JLabel lblPrackEmptySlots;
	private JPanel panelRackMaterialExtra;
	private JPanel panelRackProductExtra;
	
	private JButton btnNewMaterialSubmit;
	private JButton btnNewMaterialInput;
	private JButton btnUnloadAllProduct;
	
	private static boolean bMaterialEditMode = false;
	private static boolean bProductEditMode = false;
	
	private SystemTray systemTray = null;
	private TrayIcon trayIcon = null;
	
	private static RackManager rackManager = new RackManager();
	private JLabel lblTotalConnections;
	
	private static ServerSocket svrSocket = null;
	private static final int PORTSVRSOCKET = 20522;
	private static final int RACKIDCOLNO = 2;
	private static final int SLOTIDCOLNO = 3;
	private static final int WKPCIDCOLNO = 4;
	private JLabel lblRunningMsg;
	private JMenuItem mntmForceQuitting;
	private JButton btnUnloadAll;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		checkPreviousInstance();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RackManager frame = RackManager.getInstance();
					frame.setVisible(true);
					ThreadController.stopRackManager();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static RackManager getInstance(){
		return rackManager;
	}
	
	/**
	 * Create the frame.
	 */
	private RackManager() {
		String msg = "OK";
		ThreadController.initStopFlag();
		msg = MySystemUtils.sysLoadSettingsFromDB();
		if(!"OK".equals(msg)) RunningMsg.set(msg);
		
		setIconImage(Toolkit.getDefaultToolkit().getImage(RackManager.class.getResource("/com/cncmes/img/Butterfly_orange_24.png")));
		setTitle("CNC Rack Manager");
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				int rtn = JOptionPane.showConfirmDialog(rackManager.getContentPane(), "Run the CNC Rack Manager in background", "Minimize", JOptionPane.YES_NO_OPTION);
				if(0 == rtn){
					rackManager.dispose();
					showSystemTray();
				}
			}
		});
		
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 800;
		int height = 600;
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(16 == e.getModifiers()){
					exitSystem(false);
				}
			}
		});
		mntmExit.setIcon(new ImageIcon(RackManager.class.getResource("/com/cncmes/img/Exit_16.png")));
		mnFile.add(mntmExit);
		
		JMenu mnRun = new JMenu("Run");
		menuBar.add(mnRun);
		
		mntmStart = new JMenuItem("Start");
		mntmStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16 == arg0.getModifiers() && mntmStart.isEnabled()){
					String msg = RackServer.getInstance().start();
					if("OK".equals(msg)){
						for(int i=0; i<10; i++){
							refreshGUI(i);
						}
						setStartButtonEnabled(false);
					}else{
						JOptionPane.showMessageDialog(contentPane, "RackManager Launches Error\r\n"+msg, "RackManager Launches Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		mntmStart.setIcon(new ImageIcon(RackManager.class.getResource("/com/cncmes/img/start_16.png")));
		mnRun.add(mntmStart);
		
		mntmStop = new JMenuItem("Stop");
		mntmStop.setEnabled(false);
		mntmStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16 == arg0.getModifiers() && mntmStop.isEnabled()){
					stopSystem();
				}
			}
		});
		mntmStop.setIcon(new ImageIcon(RackManager.class.getResource("/com/cncmes/img/stop_16.png")));
		mnRun.add(mntmStop);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmAboutCncRack = new JMenuItem("About CNC Rack Manager");
		mnHelp.add(mntmAboutCncRack);
		
		mntmForceQuitting = new JMenuItem("Force Quitting");
		mntmForceQuitting.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16 == arg0.getModifiers()){
					PwdConfirmation dialog = PwdConfirmation.getInstance();
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
					
					String pwd = PwdConfirmation.getPassword();
					if("000000".equals(pwd)){
						exitSystem(true);
					}else{
						JOptionPane.showMessageDialog(contentPane, "Your password "+pwd+" is not correct", "Wrong Password", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		mnHelp.add(mntmForceQuitting);
		
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
			public void mouseClicked(MouseEvent e) {
				if(btnLogin.isEnabled() && 1==e.getButton()){
					if(LoginSystem.userHasLoginned()){
						if(0==JOptionPane.showConfirmDialog(rackManager, "Are you sure of logging out now?", "Log Out?", JOptionPane.YES_NO_OPTION)){
							LoginSystem.userLogout();
							refreshButtonsEnabled();
						}
					}else{
						UserLogin dialog = UserLogin.getInstance();
						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dialog.setVisible(true);
					}
				}
			}
		});
		btnLogin.setBorderPainted(false);
		btnLogin.setIcon(new ImageIcon(RackManager.class.getResource("/com/cncmes/img/login_24.png")));
		toolBar.add(btnLogin);
		
		btnConfig = new JButton("");
		btnConfig.setToolTipText("System setup");
		btnConfig.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(btnConfig.isEnabled() && 1 == arg0.getButton()){
					try {
						SysConfig dialog = SysConfig.getInstance();
						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dialog.setVisible(true);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		btnConfig.setBorderPainted(false);
		btnConfig.setIcon(new ImageIcon(RackManager.class.getResource("/com/cncmes/img/setting_24.png")));
		toolBar.add(btnConfig);
		
		btnStart = new JButton("");
		btnStart.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(1 == arg0.getButton() && btnStart.isEnabled()){
					String msg = RackServer.getInstance().start();
					if("OK".equals(msg)){
						for(int i=0; i<10; i++){
							refreshGUI(i);
						}
						setStartButtonEnabled(false);
					}else{
						JOptionPane.showMessageDialog(contentPane, "RackManager Launches Error\r\n"+msg, "RackManager Launches Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		btnStart.setBorderPainted(false);
		btnStart.setIcon(new ImageIcon(RackManager.class.getResource("/com/cncmes/img/start_24.png")));
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
		btnStop.setBorderPainted(false);
		btnStop.setIcon(new ImageIcon(RackManager.class.getResource("/com/cncmes/img/stop_24.png")));
		toolBar.add(btnStop);
		
		JButton btnExit = new JButton("");
		btnExit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(1 == e.getButton()){
					exitSystem(false);
				}
			}
		});
		btnExit.setBorderPainted(false);
		btnExit.setIcon(new ImageIcon(RackManager.class.getResource("/com/cncmes/img/Exit_24.png")));
		toolBar.add(btnExit);
		
		JPanel panelMain = new JPanel();
		contentPane.add(panelMain, BorderLayout.CENTER);
		panelMain.setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel panelRackMaterial = new JPanel();
		panelRackMaterial.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panelMain.add(panelRackMaterial);
		panelRackMaterial.setLayout(new BorderLayout(0, 0));
		
		JPanel panelRackMaterialOP = new JPanel();
		panelRackMaterial.add(panelRackMaterialOP, BorderLayout.NORTH);
		GridBagLayout gbl_panelRackMaterialOP = new GridBagLayout();
		gbl_panelRackMaterialOP.columnWidths = new int[] {220, 200, 140, 140, 90, 10};
		gbl_panelRackMaterialOP.rowHeights = new int[]{0, 0};
		gbl_panelRackMaterialOP.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panelRackMaterialOP.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panelRackMaterialOP.setLayout(gbl_panelRackMaterialOP);
		
		JLabel lblMrackLineName = new JLabel("Material Rack Info : Line Name");
		lblMrackLineName.setIcon(new ImageIcon(RackManager.class.getResource("/com/cncmes/img/Material_24.png")));
		GridBagConstraints gbc_lblMrackLineName = new GridBagConstraints();
		gbc_lblMrackLineName.insets = new Insets(0, 0, 0, 5);
		gbc_lblMrackLineName.anchor = GridBagConstraints.EAST;
		gbc_lblMrackLineName.gridx = 0;
		gbc_lblMrackLineName.gridy = 0;
		panelRackMaterialOP.add(lblMrackLineName, gbc_lblMrackLineName);
		
		comboBoxMrackLineName = new JComboBox<String>();
		comboBoxMrackLineName.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				materialCurLine = comboBoxMrackLineName.getSelectedItem().toString();
				refreshGUI(0);
			}
		});
		comboBoxMrackLineName.setModel(new DefaultComboBoxModel<String>(RackMaterial.getInstance().getLineNames(false)));
		GridBagConstraints gbc_comboBoxMrackLineName = new GridBagConstraints();
		gbc_comboBoxMrackLineName.insets = new Insets(0, 0, 0, 5);
		gbc_comboBoxMrackLineName.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxMrackLineName.gridx = 1;
		gbc_comboBoxMrackLineName.gridy = 0;
		panelRackMaterialOP.add(comboBoxMrackLineName, gbc_comboBoxMrackLineName);
		
		btnNewMaterialSubmit = new JButton("Submit New Material");
		btnNewMaterialSubmit.setEnabled(false);
		btnNewMaterialSubmit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(1 == arg0.getButton() && btnNewMaterialSubmit.isEnabled()){
					if("All".equals(materialCurLine)){
						JOptionPane.showMessageDialog(rackManager.getContentPane(), "Line Name can not be All", "Line Name Error", JOptionPane.ERROR_MESSAGE);
					}else{
						if(!"OK".equals(MySystemUtils.sysSchedulerOK(SchedulerItems.PORTMATERIAL))){
							JOptionPane.showMessageDialog(rackManager.getContentPane(), "Please start the Scheduler first!", "Scheduler Not Started", JOptionPane.ERROR_MESSAGE);
							return;
						}
						btnNewMaterialSubmit.setEnabled(false);
						
						String wpIDs = "",rackIDs = "",slotIDs = "";
						for(int row=0; row<materialRawData.length; row++){
							Object id = tableRackMaterial.getValueAt(row, WKPCIDCOLNO);
							if(null != id){
								if("".equals(wpIDs)){
									wpIDs = (String) id;
									rackIDs = (String) tableRackMaterial.getValueAt(row, RACKIDCOLNO);
									slotIDs = (String) tableRackMaterial.getValueAt(row, SLOTIDCOLNO);
								}else{
									wpIDs += "," + (String) id;
									rackIDs += "," + (String) tableRackMaterial.getValueAt(row, RACKIDCOLNO);
									slotIDs += "," + (String) tableRackMaterial.getValueAt(row, SLOTIDCOLNO);
								}
							}
						}
						if(!"".equals(wpIDs)){
							String[] ids = wpIDs.split(",");
							String[] racks = rackIDs.split(",");
							String[] slots = slotIDs.split(",");
							
							int pRackEmptySlotsCnt = RackProduct.getInstance().getEmptySlotsCount(materialCurLine, "All");
							if(pRackEmptySlotsCnt < ids.length){
								JOptionPane.showMessageDialog(rackManager.getContentPane(), "Submit material count("+ids.length+") is larger than Product Rack empty slots("+pRackEmptySlotsCnt+")!", "Product Rack has no enough empty slots", JOptionPane.ERROR_MESSAGE);
								return;
							}
							
							String lastID = "";
							RackMaterial rackM = RackMaterial.getInstance();
							WorkpieceData wpData = WorkpieceData.getInstance();
							SchedulerClient sc = SchedulerClient.getInstance();
							
							for(int i=0; i<ids.length; i++){
								if(rackM.putWorkpieceOnRack(materialCurLine, racks[i], ids[i], slots[i])){
									DataUtils.updateWorkpieceData(ids[i], materialCurLine, "", "");
									if((int)wpData.getItemVal(ids[i], WorkpieceItems.PROCQTY)>0){
										if(machiningProgramsReady(ids[i],(int)wpData.getItemVal(ids[i], WorkpieceItems.PROCQTY))){
											wpData.setWorkpieceState(ids[i], DeviceState.STANDBY);
											sc.updateMaterialInfo(ids[i], SchedulerItems.PORTRACK, true);
											lastID = ids[i];
										}else{
											lblRunningMsg.setText(RunningMsg.get());
											wpData.setWorkpieceState(ids[i], DeviceState.ALARMING);
										}
									}else{
										lblRunningMsg.setText("Machining Spec for "+ids[i]+" is wrongly set");
										wpData.setWorkpieceState(ids[i], DeviceState.ALARMING);
									}
								}
							}
							if(!"".equals(lastID)){
								sc.updateMaterialInfo(lastID, SchedulerItems.PORTRACK, false);
							}
						}
						bMaterialEditMode = false;
						materialOriLine = ""; //Trigger table data refreshing
						refreshGUI(0);
					}
				}
			}
		});
		GridBagConstraints gbc_btnNewMaterialSubmit = new GridBagConstraints();
		gbc_btnNewMaterialSubmit.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewMaterialSubmit.gridx = 2;
		gbc_btnNewMaterialSubmit.gridy = 0;
		panelRackMaterialOP.add(btnNewMaterialSubmit, gbc_btnNewMaterialSubmit);
		
		btnNewMaterialInput = new JButton("Input New Material");
		btnNewMaterialInput.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(1 == arg0.getButton()){
					if("All".equals(materialCurLine)){
						JOptionPane.showMessageDialog(rackManager.getContentPane(), "Line Name can not be All", "Line Name Error", JOptionPane.ERROR_MESSAGE);
					}else{
						btnNewMaterialInput.setEnabled(false);
						bMaterialEditMode = true;
						materialTableModel = new MyTableModel(RackMaterial.getInstance().getTableTitle(),materialNewData,WKPCIDCOLNO);
						tableRackMaterial.setModel(materialTableModel);
						fitTableColumns(tableRackMaterial);
						if(0 == JOptionPane.showConfirmDialog(rackManager.getContentPane(), "Randomly input material?", "Randomly Input", JOptionPane.YES_NO_OPTION)){
							String workpieceID = "";
							String[] mesCodes = Mescode.getInstance().getMesCodes();
							int mesCnt = mesCodes.length;
							for(int i=0; i<materialNewData.length; i++){
								workpieceID = mesCodes[(((i+1) % mesCnt)>0?((i+1) % mesCnt):mesCnt)-1] + System.currentTimeMillis() + (new Random().nextInt(100));
								tableRackMaterial.setValueAt(workpieceID, i, WKPCIDCOLNO);
							}
							tableRackMaterial.repaint();
						}
						btnNewMaterialInput.setEnabled(true);
						btnNewMaterialSubmit.setEnabled(true);
					}
				}
			}
		});
		GridBagConstraints gbc_btnNewMaterialInput = new GridBagConstraints();
		gbc_btnNewMaterialInput.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewMaterialInput.gridx = 3;
		gbc_btnNewMaterialInput.gridy = 0;
		panelRackMaterialOP.add(btnNewMaterialInput, gbc_btnNewMaterialInput);
		
		btnUnloadAll = new JButton("Unload All");
		btnUnloadAll.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(1 == arg0.getButton()){
					unloadAllMaterialFromRack(btnUnloadAll, false);
				}
			}
		});
		GridBagConstraints gbc_btnUnloadAll = new GridBagConstraints();
		gbc_btnUnloadAll.gridx = 4;
		gbc_btnUnloadAll.gridy = 0;
		panelRackMaterialOP.add(btnUnloadAll, gbc_btnUnloadAll);
		
		materialRawData = RackMaterial.getInstance().getTableData("All");
		materialNewData = materialRawData;
		materialTableModel = new MyTableModel(RackMaterial.getInstance().getTableTitle(),materialNewData);
		scrollPaneRackMaterial = new JScrollPane();
		tableRackMaterial = new JTable(materialTableModel);
		
		tableRackMaterial.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				int row = ((JTable)e.getSource()).rowAtPoint(e.getPoint());
				int col = ((JTable)e.getSource()).columnAtPoint(e.getPoint());
				if("".equals(materialLastPointer)){
					materialLastPointer = row+","+col;
				}else{
					String curPointer = row+","+col;
					if(!curPointer.equals(materialLastPointer)){
						String[] pointer = materialLastPointer.split(",");
						row = Integer.parseInt(pointer[0]);
						col = Integer.parseInt(pointer[1]);
						String sVal = "";
						String rawVal = "";
						Object val = tableRackMaterial.getValueAt(row, col);
						if(null != val) sVal = (String) val;
						if(null != materialNewData[row][col]) rawVal = (String) materialNewData[row][col];
						if(!sVal.equals(rawVal)){
							materialRawData[row][col] = sVal;
						}
						materialLastPointer = curPointer;
					}
				}
			}
		});
		
		tableRackMaterial.addKeyListener(new KeyAdapter(){
			public void keyReleased(KeyEvent e) {
				int row = tableRackMaterial.getSelectedRow();
				int col = tableRackMaterial.getSelectedColumn();
				if("".equals(materialLastPointer)){
					materialLastPointer = row+","+col;
				}else{
					String curPointer = row+","+col;
					String[] pointer;
					if(!curPointer.equals(materialLastPointer)){
						pointer = materialLastPointer.split(",");
					}else{
						pointer = curPointer.split(",");
					}
					row = Integer.parseInt(pointer[0]);
					col = Integer.parseInt(pointer[1]);
					String sVal = "";
					String rawVal = "";
					Object val = tableRackMaterial.getValueAt(row, col);
					if(null != val) sVal = (String) val;
					if(null != materialNewData[row][col]) rawVal = (String) materialNewData[row][col];
					if(!sVal.equals(rawVal)){
						materialRawData[row][col] = sVal;
					}
					materialLastPointer = curPointer;
				}
			}
		});
		
		tableRackMaterial.setRowHeight(25);
		fitTableColumns(tableRackMaterial);
		scrollPaneRackMaterial.setViewportView(tableRackMaterial);
		panelRackMaterial.add(scrollPaneRackMaterial, BorderLayout.CENTER);
		
		panelRackMaterialExtra = new JPanel();
		panelRackMaterial.add(panelRackMaterialExtra, BorderLayout.SOUTH);
		GridBagLayout gbl_panelRackMaterialExtra = new GridBagLayout();
		gbl_panelRackMaterialExtra.columnWidths = new int[] {150, 150, 150, 150, 150, 50};
		gbl_panelRackMaterialExtra.rowHeights = new int[]{0, 0};
		gbl_panelRackMaterialExtra.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panelRackMaterialExtra.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panelRackMaterialExtra.setLayout(gbl_panelRackMaterialExtra);
		
		lblMrackTotal = new JLabel("Total Workpiece: 100");
		GridBagConstraints gbc_lblMrackTotal = new GridBagConstraints();
		gbc_lblMrackTotal.insets = new Insets(0, 0, 0, 5);
		gbc_lblMrackTotal.gridx = 0;
		gbc_lblMrackTotal.gridy = 0;
		panelRackMaterialExtra.add(lblMrackTotal, gbc_lblMrackTotal);
		
		lblMrackStandby = new JLabel("Standby Workpiece: 80");
		GridBagConstraints gbc_lblMrackStandby = new GridBagConstraints();
		gbc_lblMrackStandby.insets = new Insets(0, 0, 0, 5);
		gbc_lblMrackStandby.gridx = 1;
		gbc_lblMrackStandby.gridy = 0;
		panelRackMaterialExtra.add(lblMrackStandby, gbc_lblMrackStandby);
		
		lblMrackPlan = new JLabel("Plan Workpiece: 20");
		GridBagConstraints gbc_lblMrackPlan = new GridBagConstraints();
		gbc_lblMrackPlan.insets = new Insets(0, 0, 0, 5);
		gbc_lblMrackPlan.gridx = 2;
		gbc_lblMrackPlan.gridy = 0;
		panelRackMaterialExtra.add(lblMrackPlan, gbc_lblMrackPlan);
		
		lblMrackEmptySlots = new JLabel("Empty Slots: 0");
		GridBagConstraints gbc_lblMrackEmptySlots = new GridBagConstraints();
		gbc_lblMrackEmptySlots.insets = new Insets(0, 0, 0, 5);
		gbc_lblMrackEmptySlots.gridx = 3;
		gbc_lblMrackEmptySlots.gridy = 0;
		panelRackMaterialExtra.add(lblMrackEmptySlots, gbc_lblMrackEmptySlots);
		
		lblWorkingWorkpieces = new JLabel("Working Workpiece: 0");
		GridBagConstraints gbc_lblWorkingWorkpieces = new GridBagConstraints();
		gbc_lblWorkingWorkpieces.gridx = 4;
		gbc_lblWorkingWorkpieces.gridy = 0;
		panelRackMaterialExtra.add(lblWorkingWorkpieces, gbc_lblWorkingWorkpieces);
		
		JPanel panelRackProduct = new JPanel();
		panelRackProduct.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panelMain.add(panelRackProduct);
		panelRackProduct.setLayout(new BorderLayout(0, 0));
		
		JPanel panelRackProductOP = new JPanel();
		panelRackProduct.add(panelRackProductOP, BorderLayout.NORTH);
		GridBagLayout gbl_panelRackProductOP = new GridBagLayout();
		gbl_panelRackProductOP.columnWidths = new int[] {220, 200, 140, 120, 120};
		gbl_panelRackProductOP.rowHeights = new int[]{0, 0};
		gbl_panelRackProductOP.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panelRackProductOP.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panelRackProductOP.setLayout(gbl_panelRackProductOP);
		
		JLabel lblPrackLineName = new JLabel("Product Rack Info : Line Name");
		lblPrackLineName.setIcon(new ImageIcon(RackManager.class.getResource("/com/cncmes/img/Workpiece_24.png")));
		GridBagConstraints gbc_lblPrackLineName = new GridBagConstraints();
		gbc_lblPrackLineName.insets = new Insets(0, 0, 0, 5);
		gbc_lblPrackLineName.anchor = GridBagConstraints.EAST;
		gbc_lblPrackLineName.gridx = 0;
		gbc_lblPrackLineName.gridy = 0;
		panelRackProductOP.add(lblPrackLineName, gbc_lblPrackLineName);
		
		comboBoxPrackLineName = new JComboBox<String>();
		comboBoxPrackLineName.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				productCurLine = comboBoxPrackLineName.getSelectedItem().toString();
				refreshGUI(1);
			}
		});
		comboBoxPrackLineName.setModel(new DefaultComboBoxModel<String>(RackProduct.getInstance().getLineNames(false)));
		GridBagConstraints gbc_comboBoxPrackLineName = new GridBagConstraints();
		gbc_comboBoxPrackLineName.insets = new Insets(0, 0, 0, 5);
		gbc_comboBoxPrackLineName.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxPrackLineName.gridx = 1;
		gbc_comboBoxPrackLineName.gridy = 0;
		panelRackProductOP.add(comboBoxPrackLineName, gbc_comboBoxPrackLineName);
		
		btnUnloadAllProduct = new JButton("Unload All Product");
		btnUnloadAllProduct.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(1 == arg0.getButton()){
					unloadAllMaterialFromRack(btnUnloadAllProduct, true);
				}
			}
		});
		GridBagConstraints gbc_btnUnloadAllProduct = new GridBagConstraints();
		gbc_btnUnloadAllProduct.insets = new Insets(0, 0, 0, 5);
		gbc_btnUnloadAllProduct.gridx = 2;
		gbc_btnUnloadAllProduct.gridy = 0;
		panelRackProductOP.add(btnUnloadAllProduct, gbc_btnUnloadAllProduct);
		
		productRawData = RackProduct.getInstance().getTableData("All");
		productNewData = productRawData;
		productTableModel = new MyTableModel(RackMaterial.getInstance().getTableTitle(),productNewData);
		scrollPaneRackProduct = new JScrollPane();
		tableRackProduct = new JTable(productTableModel);
		
		tableRackProduct.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				if(2 == e.getClickCount()){
					if("All".equals(productCurLine)){
						JOptionPane.showMessageDialog(rackManager.getContentPane(), "Line Name can not be All", "Line Name Error", JOptionPane.ERROR_MESSAGE);
					}else{
						int row = ((JTable)e.getSource()).rowAtPoint(e.getPoint());
						String sVal = "";
						Object val = tableRackProduct.getValueAt(row, WKPCIDCOLNO);
						if(null != val) sVal = (String) val;
						if(!"".equals(sVal)){
							if(0 == JOptionPane.showConfirmDialog(rackManager.getContentPane(), "Do you want to unload product "+sVal, "Unload Product?", JOptionPane.YES_NO_OPTION)){
								String rackID = (String) WorkpieceData.getInstance().getData(sVal).get(WorkpieceItems.CONVEYORID);
								String slotNo = (String) WorkpieceData.getInstance().getData(sVal).get(WorkpieceItems.CONVEYORSLOTNO);
								RackProduct.getInstance().updateSlot(productCurLine, rackID, Integer.parseInt(slotNo), "");
								productOriLine = ""; //Trigger table data refreshing
							}
						}
					}
				}
			}
		});
		
		tableRackProduct.setRowHeight(25);
		fitTableColumns(tableRackProduct);
		scrollPaneRackProduct.setViewportView(tableRackProduct);
		panelRackProduct.add(scrollPaneRackProduct, BorderLayout.CENTER);
		
		panelRackProductExtra = new JPanel();
		panelRackProduct.add(panelRackProductExtra, BorderLayout.SOUTH);
		GridBagLayout gbl_panelRackProductExtra = new GridBagLayout();
		gbl_panelRackProductExtra.columnWidths = new int[] {150, 150, 180, 320};
		gbl_panelRackProductExtra.rowHeights = new int[]{0, 0};
		gbl_panelRackProductExtra.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panelRackProductExtra.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panelRackProductExtra.setLayout(gbl_panelRackProductExtra);
		
		lblPrackTotal = new JLabel("Total Workpiece: 100");
		GridBagConstraints gbc_lblPrackTotal = new GridBagConstraints();
		gbc_lblPrackTotal.insets = new Insets(0, 0, 0, 5);
		gbc_lblPrackTotal.gridx = 0;
		gbc_lblPrackTotal.gridy = 0;
		panelRackProductExtra.add(lblPrackTotal, gbc_lblPrackTotal);
		
		lblPrackEmptySlots = new JLabel("Empty Slots: 50");
		GridBagConstraints gbc_lblPrackEmptySlots = new GridBagConstraints();
		gbc_lblPrackEmptySlots.insets = new Insets(0, 0, 0, 5);
		gbc_lblPrackEmptySlots.gridx = 1;
		gbc_lblPrackEmptySlots.gridy = 0;
		panelRackProductExtra.add(lblPrackEmptySlots, gbc_lblPrackEmptySlots);
		
		lblTotalConnections = new JLabel("Total Connections: 1");
		GridBagConstraints gbc_lblTotalConnections = new GridBagConstraints();
		gbc_lblTotalConnections.insets = new Insets(0, 0, 0, 5);
		gbc_lblTotalConnections.gridx = 2;
		gbc_lblTotalConnections.gridy = 0;
		panelRackProductExtra.add(lblTotalConnections, gbc_lblTotalConnections);
		
		lblRunningMsg = new JLabel("");
		GridBagConstraints gbc_lblRunningMsg = new GridBagConstraints();
		gbc_lblRunningMsg.gridx = 3;
		gbc_lblRunningMsg.gridy = 0;
		panelRackProductExtra.add(lblRunningMsg, gbc_lblRunningMsg);
		
		materialLastPointer = "";
		if(!"OK".equals(msg)) setStartButtonEnabled(false,false);
		lblRunningMsg.setText(RunningMsg.get());
		refreshButtonsEnabled();
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
			
			trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(RackManager.class.getResource("/com/cncmes/img/Butterfly_orange_24.png")),"CNC Rack Manager",popup);
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
	
	private void fitTableColumns(JTable myTable){
        myTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTableHeader header = myTable.getTableHeader();
        int rowCount = myTable.getRowCount();
        Enumeration<TableColumn> columns = myTable.getColumnModel().getColumns();
        while(columns.hasMoreElements())
        {
            TableColumn column = (TableColumn)columns.nextElement();
            int col = header.getColumnModel().getColumnIndex(column.getIdentifier());
            int width = (int)header.getDefaultRenderer().getTableCellRendererComponent
            (myTable, column.getIdentifier(), false, false, -1, col).getPreferredSize().getWidth();
            for(int row = 0; row < rowCount; row++)
            {
                int preferedWidth = (int)myTable.getCellRenderer(row, col).getTableCellRendererComponent
                (myTable, myTable.getValueAt(row, col), false, false, row, col).getPreferredSize().getWidth();
                width = Math.max(width, preferedWidth);
            }
            header.setResizingColumn(column);
            column.setWidth((int)(width*1.2)+myTable.getIntercellSpacing().width);
        }
	}
	
	private void refreshRackMaterial() {
		try {
			LinkedHashMap<RackItems,Object> rackStatisticInfo = RackMaterial.getInstance().getRackStatisticInfoByLineName(materialCurLine);
			RackTemp rackTemp = RackTemp.getInstance();
			lblMrackTotal.setText("Total Workpiece: "+rackStatisticInfo.get(RackItems.WPTOTAL));
			lblMrackStandby.setText("Standby Workpiece: "+rackStatisticInfo.get(RackItems.WPSTANDBY));
			lblMrackPlan.setText("Plan Workpiece: "+rackStatisticInfo.get(RackItems.WPPLAN));
			lblMrackEmptySlots.setText("Empty Slots: "+rackStatisticInfo.get(RackItems.EMPTYSLOTS));
			lblWorkingWorkpieces.setText("Working Workpiece: "+rackTemp.getNotEmptySlotsCount(materialCurLine, "All"));
			panelRackMaterialExtra.updateUI();
			if(bMaterialEditMode) return;
			
			if(!materialCurLine.equals(materialOriLine)){
				materialOriLine = materialCurLine;
				materialNewData = RackMaterial.getInstance().getTableData(materialCurLine);
				materialRawData = materialNewData;
				materialTableModel = new MyTableModel(RackMaterial.getInstance().getTableTitle(),materialNewData);
				tableRackMaterial.setModel(materialTableModel);
				fitTableColumns(tableRackMaterial);
			}else{
				materialRawData = materialNewData;
				materialNewData = RackMaterial.getInstance().getTableData(materialCurLine);
				if(materialNewData.length != materialRawData.length){
					materialRawData = materialNewData;
					materialTableModel = new MyTableModel(RackMaterial.getInstance().getTableTitle(),materialNewData);
					tableRackMaterial.setModel(materialTableModel);
					fitTableColumns(tableRackMaterial);
				}else{
					String newVal = "";
					String oldVal = "";
					boolean bFitCol = true;
					for(int row=0; row<materialNewData.length; row++){
						for(int col=0; col<materialNewData[0].length; col++){
							newVal = "";
							oldVal = "";
							if(null != materialNewData[row][col]) newVal = String.valueOf(materialNewData[row][col]);
							if(null != materialRawData[row][col]) oldVal = String.valueOf(materialRawData[row][col]);
							
							if(!newVal.equals(oldVal)){
								if(bFitCol){
									fitTableColumns(tableRackMaterial);
									bFitCol = false;
								}
								if("".equals(newVal) && null == materialRawData[row][materialRawData[0].length-1]) materialNewData[row][col] = materialRawData[row][col];//User manually input workpiece ID
								materialRawData[row][col] = materialNewData[row][col];
								tableRackMaterial.setValueAt(materialNewData[row][col], row, col);
								tableRackMaterial.repaint();
							}else if("".equals(newVal)){
								materialRawData[row][col] = materialNewData[row][col];
								tableRackMaterial.setValueAt(materialNewData[row][col], row, col);
								tableRackMaterial.repaint();
							}
						}
					}
				}
			}
		} catch (Exception e) {
		}
	}

	private void refreshRackProduct() {
		try {
			int port = RackProduct.getInstance().getPort(productCurLine);
			LinkedHashMap<RackItems,Object> rackStatisticInfo = RackProduct.getInstance().getRackStatisticInfoByLineName(productCurLine);
			lblPrackTotal.setText("Total Workpiece: "+rackStatisticInfo.get(RackItems.WPTOTAL));
			lblPrackEmptySlots.setText("Empty Slots: "+rackStatisticInfo.get(RackItems.EMPTYSLOTS));
			lblTotalConnections.setText("Total Connections: "+SocketServer.getAcceptConnections(port));
			panelRackProductExtra.updateUI();
			
			if(!productCurLine.equals(productOriLine)){
				bProductEditMode = false;
				productOriLine = productCurLine;
				productNewData = RackProduct.getInstance().getTableData(productCurLine);
				productRawData = productNewData;
				productTableModel = new MyTableModel(RackProduct.getInstance().getTableTitle(),productNewData);
				tableRackProduct.setModel(productTableModel);
				fitTableColumns(tableRackProduct);
			}else{
				productRawData = productNewData;
				productNewData = RackProduct.getInstance().getTableData(productCurLine);
				if(productNewData.length != productRawData.length){
					productRawData = productNewData;
					productTableModel = new MyTableModel(RackProduct.getInstance().getTableTitle(),productNewData);
					tableRackProduct.setModel(productTableModel);
					fitTableColumns(tableRackProduct);
				}else{
					String newVal = "";
					String oldVal = "";
					boolean bFitCol = true;
					for(int row=0; row<productNewData.length; row++){
						for(int col=0; col<productNewData[0].length; col++){
							newVal = "";
							oldVal = "";
							if(null != productNewData[row][col]) newVal = String.valueOf(productNewData[row][col]);
							if(null != productRawData[row][col]) oldVal = String.valueOf(productRawData[row][col]);
							
							if(!newVal.equals(oldVal) && !bProductEditMode){
								if("".equals(newVal) && null == productRawData[row][productRawData[0].length-1]) productNewData[row][col] = productRawData[row][col];
								if(bFitCol){
									fitTableColumns(tableRackProduct);
									bFitCol = false;
								}
								productRawData[row][col] = productNewData[row][col];
								tableRackProduct.setValueAt(productNewData[row][col], row, col);
								tableRackProduct.repaint();
							}
						}
					}
				}
			}
		} catch (Exception e) {
		}
	}
	
	public void refreshGUI(int guiType){
		switch(guiType){
		case 0:
			refreshRackMaterial();
			break;
		case 1:
			refreshRackProduct();
			break;
		default:
			break;
		}
	}
	
	private void setTitle(){
		if(null==rackManager) return;
		String title = rackManager.getTitle();
		if(null!=title){
			title = title.split("##")[0] + "##Welcome " + LoginSystem.getUserName();
			rackManager.setTitle(title);
		}
	}
	
	public void refreshButtonsEnabled(){
		btnConfig.setEnabled(!LoginSystem.accessDenied(PermissionItems.SYSCONFIG));
		btnStart.setEnabled(!LoginSystem.accessDenied(PermissionItems.RACKMANAGER));
		mntmStart.setEnabled(!LoginSystem.accessDenied(PermissionItems.RACKMANAGER));
		btnNewMaterialInput.setEnabled(!LoginSystem.accessDenied(PermissionItems.RACKMANAGER));
		btnUnloadAll.setEnabled(!LoginSystem.accessDenied(PermissionItems.RACKMANAGER));
		btnUnloadAllProduct.setEnabled(!LoginSystem.accessDenied(PermissionItems.RACKMANAGER));
		setTitle();
		if(LoginSystem.userHasLoginned()){
			btnLogin.setIcon(new ImageIcon(RackManager.class.getResource("/com/cncmes/img/logout_24.png")));
			btnLogin.setToolTipText("Logout System");
		}else{
			btnLogin.setIcon(new ImageIcon(RackManager.class.getResource("/com/cncmes/img/login_24.png")));
			btnLogin.setToolTipText("Login System");
		}
	}
	
	private void setStartButtonEnabled(boolean enabled,boolean...stopBtnEnabled) {
		if(LoginSystem.accessDenied(PermissionItems.RACKMANAGER)) return;
		btnStart.setEnabled(enabled);
		mntmStart.setEnabled(enabled);
		if(null!=stopBtnEnabled && stopBtnEnabled.length>0){
			btnStop.setEnabled(stopBtnEnabled[0]);
			mntmStop.setEnabled(stopBtnEnabled[0]);
		}else{
			btnStop.setEnabled(!enabled);
			mntmStop.setEnabled(!enabled);
		}
	}
	
	private boolean machiningProgramsReady(String workpieceID, int procQty){
		boolean programsReady = false;
		
		WorkpieceData wpData = WorkpieceData.getInstance();
		String ncModel = "", ncProgram = "", svrFilePath = "", errMsg = "";
		String[] models = null, programs = null;
		String ncProgramRootDir = MySystemUtils.getNCProgramRootDir();
		
		if(procQty<=0){
			RunningMsg.set("Process Qty of "+workpieceID+" should be larger than zero");
			return programsReady;
		}
		
		for(int procNo=1; procNo<=procQty; procNo++){
			LinkedHashMap<SpecItems, Object> procInfo = wpData.getOneProcInfo(workpieceID, procNo);
			if(null==procInfo){
				errMsg = "There is no valid spec for "+workpieceID;
				break;
			}
			ncModel = ""+procInfo.get(SpecItems.NCMODEL);
			ncProgram = ""+procInfo.get(SpecItems.PROGRAM);
			models = ncModel.split(",");
			programs = ncProgram.split(",");
			for(int j=0; j<models.length; j++){
//				svrFilePath = "/"+DataUtils.getMescodeByWorkpieceID(workpieceID)+"/"+models[j]+"/"+programs[j];
//				if(!FTPUtils.fileExists(svrFilePath)){
//					errMsg += "\r\nServre File " + programs[j] + " is not existing";
//					break;
//				}
				svrFilePath = ncProgramRootDir+File.separator+DataUtils.getMescodeByWorkpieceID(workpieceID)+File.separator+models[j]+File.separator+programs[j];
				if(!MyFileUtils.fileExists(svrFilePath)){
					errMsg += "\r\nServre File " + programs[j] + " is not existing";
					break;
				}
			}
			if(!"".equals(errMsg)) break;
		}
		if("".equals(errMsg)){
			programsReady = true;
		}else{
			RunningMsg.set(errMsg);
		}
		
		return programsReady;
	}
	
	private void exitSystem(boolean force) {
		String msg = "OK";
		
		msg = MySystemUtils.sysReayToStop();
		if(!"OK".equals(msg)){
			if(!force){
				JOptionPane.showMessageDialog(contentPane, "RackManager can't be stopped at this moment!\r\n"+msg, "RackManager Stops Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
		msg = RackServer.getInstance().stop();
		
		if("OK".equals(msg)){
			int rtn = JOptionPane.showConfirmDialog(rackManager.getContentPane(), "Are you sure of quiting from CNC Rack Manager", "Exit", JOptionPane.YES_NO_OPTION);
			if(0 == rtn){
				if(null != svrSocket){
					try {
						svrSocket.close();
					} catch (IOException e1) {
					}
				}
				System.exit(0);
			}
		}else{
			JOptionPane.showMessageDialog(contentPane, "RackManager can't be stopped at this moment!\r\n"+msg, "RackManager Stops Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void stopSystem() {
		String msg = "OK";
		msg = MySystemUtils.sysReayToStop();
		if(!"OK".equals(msg)){
			JOptionPane.showMessageDialog(contentPane, "RackManager can't be stopped at this moment!\r\n"+msg, "RackManager Stops Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		msg = RackServer.getInstance().stop();
		if("OK".equals(msg)){
			setStartButtonEnabled(true);
		}else{
			JOptionPane.showMessageDialog(contentPane, "RackManager can't be stopped at this moment!\r\n"+msg, "RackManager Stops Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void loadMaterialOntoRack(String lineName, int randomPutQty){
		int curPutQty = 0;
		String lastID = "", workpieceID = "";
		String[] emptySlots = null, keys = null;
		if(randomPutQty <= 0) return;
		
		RackMaterial rackM = RackMaterial.getInstance();
		WorkpieceData wpData = WorkpieceData.getInstance();
		SchedulerClient sc = SchedulerClient.getInstance();
		
		String[] mainKeys = rackM.getMainKey(lineName, "All");
		if(null==mainKeys) return;
		
		String[] mesCodes = Mescode.getInstance().getMesCodes();
		if(mesCodes.length==1 && "".equals(mesCodes[0])) return;
		
		for(String key:mainKeys){
			keys = key.split("_");
			if(keys.length!=2) continue;
			emptySlots = rackM.getEmptySlots(keys[0], keys[1]);
			if(null==emptySlots) continue;
			
			for(int i=0; i<emptySlots.length; i++){
				workpieceID = mesCodes[(i<mesCodes.length?i:0)] + emptySlots[i] + TimeUtils.getCurrentDate("yyyyMMddHHmmss");
				if(rackM.putWorkpieceOnRack(lineName, keys[1], workpieceID, emptySlots[i])){
					DataUtils.updateWorkpieceData(workpieceID, lineName, "", "");
					if((int)wpData.getItemVal(workpieceID, WorkpieceItems.PROCQTY)>0){
						if(machiningProgramsReady(workpieceID,(int)wpData.getItemVal(workpieceID, WorkpieceItems.PROCQTY))){
							wpData.setWorkpieceState(workpieceID, DeviceState.STANDBY);
							sc.updateMaterialInfo(workpieceID, SchedulerItems.PORTRACK, true);
							lastID = workpieceID;
						}else{
							wpData.setWorkpieceState(workpieceID, DeviceState.ALARMING);
						}
					}else{
						wpData.setWorkpieceState(workpieceID, DeviceState.ALARMING);
					}
					curPutQty++;
					if(curPutQty >= randomPutQty) break;
				}
			}
			if(curPutQty >= randomPutQty) break;
		}
		
		if(!"".equals(lastID)){
			sc.updateMaterialInfo(lastID, SchedulerItems.PORTRACK, false);
		}
	}
	
	public int unloadMaterialFromRack(String curLineName, boolean bRackProd){
		int unloadQty = 0;
		RackData rackObj = null;
		
		if(bRackProd){
			rackObj = RackProduct.getInstance();
		}else{
			rackObj = RackMaterial.getInstance();
		}
		
		WorkpieceData wpData = WorkpieceData.getInstance();
		RackServer rackSvr = RackServer.getInstance();
		CtrlCenterClient ccClient = CtrlCenterClient.getInstance();
		SchedulerClient scClient = SchedulerClient.getInstance();
		String[] mainKeys = rackObj.getMainKey(curLineName,"All");
		String[] ccCfg = null;
		String controlCenter = "";
		
		if(null != mainKeys){
			String theLastID = "", ccIP = "", ccPort = "";
			for(String mainKey:mainKeys){
				LinkedHashMap<RackItems, Object> rack = rackObj.getData(mainKey);
				if(null != rack){
					int capacity = (int) rack.get(RackItems.CAPACITY);
					if(capacity > 45) capacity = 45;
					
					String[] keys = mainKey.split("_");
					for(int i=1; i<=capacity; i++){
						RackItems key = rackObj.getRackItems(i);
						Object slot = rack.get(key);
						if(null != slot && !"".equals(slot)){
							rackObj.updateSlot(curLineName, keys[1], i, "");
							wpData.removeData(""+slot);
							unloadQty++;
							
							if(bRackProd){
								controlCenter = rackSvr.getControlCenter(keys[0]);
								if(null!=controlCenter && controlCenter.indexOf(":")>=0){
									ccCfg = controlCenter.split(":");
									theLastID = "" + slot;
									ccIP = ccCfg[0]; ccPort = ccCfg[1];
									ccClient.informControlCenter("removeMaterial", ccIP, ccPort, theLastID, theLastID, true, true, false);
								}
							}else{
								theLastID = "" + slot;
								scClient.informScheduler("removeMaterial", SchedulerItems.PORTMATERIAL, theLastID, true, true, false);
							}
						}
					}
				}
			}
			if(!"".equals(theLastID)){
				if(bRackProd){
					ccClient.informControlCenter("removeMaterial", ccIP, ccPort, theLastID, theLastID, true, true, true);
				}else{
					scClient.informScheduler("removeMaterial", SchedulerItems.PORTMATERIAL, theLastID, true, true, true);
				}
			}
		}
		
		return unloadQty;
	}
	
	private int unloadAllMaterialFromRack(JButton btnUnload, boolean bRackProd) {
		String curLineName = "", msgContent = "", msgTitle = "";
		int unloadQty = 0;
		
		if(bRackProd){
			curLineName = productCurLine;
			msgContent = "Do you want to unload all products now?\r\n"+
					"Note: All products must be really unloaded from the rack already before doing this operation";
			msgTitle = "Unload All Products?";
		}else{
			curLineName = materialCurLine;
			msgContent = "Do you want to unload all materials now?\r\n"+
					"Note: All materials must be really unloaded from the rack already before doing this operation";
			msgTitle = "Unload All Materials?";
		}
		
		if("All".equals(curLineName)){
			JOptionPane.showMessageDialog(rackManager.getContentPane(), "Line Name can not be All", "Line Name Error", JOptionPane.ERROR_MESSAGE);
		}else{
			if(0 == JOptionPane.showConfirmDialog(rackManager.getContentPane(), msgContent, msgTitle, JOptionPane.YES_NO_OPTION)){
				btnUnload.setEnabled(false);
				unloadQty = unloadMaterialFromRack(curLineName, bRackProd);
				btnUnload.setEnabled(true);
				
				if(bRackProd){
					productOriLine = ""; //Trigger table data refreshing
					refreshGUI(1);
				}else{
					materialOriLine = ""; //Trigger table data refreshing
					refreshGUI(0);
				}
			}
		}
		
		return unloadQty;
	}

	private static void checkPreviousInstance(){
		try {
			svrSocket = new ServerSocket(PORTSVRSOCKET);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "CNC Rack Manager is already started", "Program Launch", JOptionPane.INFORMATION_MESSAGE);
			System.exit(0);
		}
	}
	
	/**
	 * MyDataModel class is used to:
	 * 1. Show the specific data in a JTable
	 * 2. Specify which data column is editable
	 * @author Sanly
	 *
	 */
	class MyTableModel extends AbstractTableModel{
		private static final long serialVersionUID = 17L;
		private Object[][] myData;
		private String[] title;
		private int[] editableCol;
		private int rowCount = 0;
		private int colCount = 0;
		
		public MyTableModel(String[] tableTitle,Object[][] tableData,int...editableCol){
			super();
			title = tableTitle;
			myData = tableData;
			colCount = tableTitle.length;
			if(null != tableData) rowCount = tableData.length;
			this.editableCol = editableCol;
		}
		
		@Override
		public int getRowCount() {
			return rowCount;
		}

		@Override
		public int getColumnCount() {
			return colCount;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return myData[rowIndex][columnIndex];
		}
		
		public void setValueAt(Object value, int row, int column){
			myData[row][column] = value;
		}
		
		public String getColumnName(int column){
			return title[column];
		}
		
		public boolean isCellEditable(int row, int column){
			if(null != editableCol && editableCol.length > 0){
				for(int i=0; i<editableCol.length; i++){
					if(column == editableCol[i]) return true;
				}
			}
			return false;
		}
	}
}
