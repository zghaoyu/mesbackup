package com.cncmes.gui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import com.cncmes.base.DeviceState;
import com.cncmes.base.PermissionItems;
import com.cncmes.base.SchedulerDataItems;
import com.cncmes.ctrl.SchedulerServer;
import com.cncmes.ctrl.SocketServer;
import com.cncmes.dao.DAO;
import com.cncmes.dao.impl.DAOImpl;
import com.cncmes.data.SchedulerCfg;
import com.cncmes.data.SchedulerMachine;
import com.cncmes.data.SchedulerMaterial;
import com.cncmes.data.SchedulerRobot;
import com.cncmes.data.SchedulerStatistic;
import com.cncmes.data.SchedulerTask;
import com.cncmes.dto.CNCProcessCard;
import com.cncmes.dto.OrderScheduler;
import com.cncmes.thread.ThreadController;
import com.cncmes.utils.*;

import net.sf.json.JSONObject;

import java.awt.GridLayout;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;

import javax.swing.border.BevelBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class Scheduler extends JFrame {
	private static final long serialVersionUID = 15L;
	private static Scheduler scheduler = new Scheduler();
	private JPanel contentPane;
	private JPanel panelStatisticExtra;
	private JPanel panelMachineExtra;
	private JPanel panelRobotExtra;
	private JPanel panelMaterialExtra;
	private JList<String> listTask;
	
	private JMenuItem mntmStart;
	private JMenuItem mntmStop;
	private JButton btnStart;
	private JButton btnStop;
	private JButton btnLogin;
	private JButton btnConfig;
	private JButton confirmOrder;
	private JTable tableMachine;
	private JTable tableRobot;
	private JTable tableMaterial;
	private JTable tableStatistic;
	
	private JLabel lblConnectingDevices;
	private JLabel lblMachineworking;
	private JLabel lblMachinestandby;
	private JLabel lblMachineshutdown;
	private JLabel lblMachinealarm;
	private JLabel lblMachinePlan;
	private JLabel lblRobotWorking;
	private JLabel lblRobotstandby;
	private JLabel lblRobotshutdown;
	private JLabel lblRobotalarm;
	private JLabel lblRobotPlan;
	private JLabel lblWorkpiecetotal;
	private JLabel lblWorkpiecestandby;
	private JLabel lblWorkpieceplan;
	private JLabel lblWorkpiecemachining;
	private JLabel lblWorkpiecedone;
	private JLabel lblMessage;
	private JLabel lblHeartbeat;
	private JLabel lblCurrentProcessMoCode;

    private JComboBox<String> cnc72orderComBox;
    private JComboBox<String> cnc79orderComBox;
    private JTextField cnc72orderField;
    private JTextField cnc79orderField;


	private Object[][] machineNewData;
	private Object[][] machineRawData;
	private Object[][] robotNewData;
	private Object[][] robotRawData;
	private Object[][] materialNewData;
	private Object[][] materialRawData;
	private Object[][] statisticNewData;
	private Object[][] statisticRawData;
	
	private String statisticOriLine;
	private String statisticCurLine;
	private String machineOriLine;
	private String machineCurLine;
	private String robotOriLine;
	private String robotCurLine;
	private String materialOriLine;
	private String materialCurLine;
	
	private String machineOriState;
	private String machineCurState;
	private String robotOriState;
	private String robotCurState;
	private String materialOriState;
	private String materialCurState;


	private SystemTray systemTray = null;
	private TrayIcon trayIcon = null;
	
	private static ServerSocket svrSocket = null;
	private static final int PORTSVRSOCKET = 20521;

	private String processCardDto = "com.cncmes.dto.CNCProcessCard";
	private String orderSchedulerDto = "com.cncmes.dto.OrderScheduler";
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		checkPreviousInstance();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Scheduler frame = Scheduler.getInstance();
					frame.setVisible(true);
					ThreadController.stopScheduler();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static Scheduler getInstance(){
		return scheduler;
	}
	
	private void initParas(){
		statisticOriLine = "All";
		statisticCurLine = "All";
		machineOriLine = "All";
		machineCurLine = "All";
		robotOriLine = "All";
		robotCurLine = "All";
		materialOriLine = "All";
		materialCurLine = "All";
		
		machineOriState = "All";
		machineCurState = "All";
		robotOriState = "All";
		robotCurState = "All";
		materialOriState = "All";
		materialCurState = "All";
	}
	
	private void setTitle(){
		if(null==scheduler) return;
		String title = scheduler.getTitle();
		if(null!=title){
			title = title.split("##")[0] + "##Welcome " + LoginSystem.getUserName();
			scheduler.setTitle(title);
		}
	}
	
	public void refreshButtonsEnabled(){
		btnConfig.setEnabled(!LoginSystem.accessDenied(PermissionItems.SYSCONFIG));
		btnStart.setEnabled(!LoginSystem.accessDenied(PermissionItems.SCHEDULER));
		mntmStart.setEnabled(!LoginSystem.accessDenied(PermissionItems.SCHEDULER));
		setTitle();
		if(LoginSystem.userHasLoginned()){
			btnLogin.setIcon(new ImageIcon(Scheduler.class.getResource("/com/cncmes/img/logout_24.png")));
			btnLogin.setToolTipText("Logout System");
		}else{
			btnLogin.setIcon(new ImageIcon(Scheduler.class.getResource("/com/cncmes/img/login_24.png")));
			btnLogin.setToolTipText("Login System");
		}
	}
	
	private void setStartButtonEnabled(boolean enabled,boolean...stopBtnEnabled){
		if(LoginSystem.accessDenied(PermissionItems.SCHEDULER)) return;
		btnStart.setEnabled(enabled);
		mntmStart.setEnabled(enabled);
		if(null!=stopBtnEnabled && stopBtnEnabled.length > 0){
			btnStop.setEnabled(stopBtnEnabled[0]);
			mntmStop.setEnabled(stopBtnEnabled[0]);
		}else{
			btnStop.setEnabled(!enabled);
			mntmStop.setEnabled(!enabled);
		}
	}
	
	/**
	 * Create the frame.
	 */
	private Scheduler() {
		System.out.println(getAllOrderSchedulers());
		String msg = "";
		ThreadController.initStopFlag();
		msg = MySystemUtils.sysDatabaseOK();
		if("OK".equals(msg)){
			DataUtils.getMachiningSpec();
			DataUtils.getMescode();
			DataUtils.getDeviceDriver();
			DataUtils.getDeviceInfo();
		}else{
			RunningMsg.set(msg);
		}


		initParas();
		setIconImage(Toolkit.getDefaultToolkit().getImage(Scheduler.class.getResource("/com/cncmes/img/Butterfly_pink_24.png")));
		setTitle("CNC Scheduler");
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				int rtn = JOptionPane.showConfirmDialog(scheduler.getContentPane(), "Run the CNC Scheduler in background", "Minimize", JOptionPane.YES_NO_OPTION);
				if(0 == rtn){
					scheduler.dispose();
					showSystemTray();
				}
			}
		});
		
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 1000;
		int height = 700;
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
		mntmExit.setIcon(new ImageIcon(Scheduler.class.getResource("/com/cncmes/img/Exit_16.png")));
		mnFile.add(mntmExit);
		
		JMenu mnRun = new JMenu("Run");
		menuBar.add(mnRun);
		
		mntmStart = new JMenuItem("Start");
		mntmStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16 == arg0.getModifiers() && mntmStart.isEnabled()){
					String msg = SchedulerServer.getInstance().start();
					if("OK".equals(msg)){
						for(int i=0; i<10; i++){
							refreshGUI(i);
						}
						setStartButtonEnabled(false);
					}else{
						JOptionPane.showMessageDialog(contentPane, msg, "Scheduler Launch Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		mntmStart.setIcon(new ImageIcon(Scheduler.class.getResource("/com/cncmes/img/start_16.png")));
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
		mntmStop.setIcon(new ImageIcon(Scheduler.class.getResource("/com/cncmes/img/stop_16.png")));
		mnRun.add(mntmStop);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmAbout = new JMenuItem("About CNC Scheduler");
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(16 == e.getModifiers()){
					String lineName = "D2E1D2A";
					String taskID = "", taskInfo = "";
					
					JSONObject jsonObj = new JSONObject();
					
					for(int j=0; j<2; j++){
						for(int i=0; i<1; i++){
							taskID = MathUtils.MD5Encode(System.currentTimeMillis()+""+j);
							jsonObj.put("taskID", taskID);
							jsonObj.put("taskState", ""+DeviceState.ALARMING);
							jsonObj.put("machineIP", "10.10.95."+(65+i));
							if(0 == j%2){
								jsonObj.put("machineState", ""+DeviceState.FINISH);
							}else{
								jsonObj.put("machineState", ""+DeviceState.WORKING);
							}
							jsonObj.put("machineKey", "10.10.95."+(65+i)+":10000");
							jsonObj.put("robotIP", "10.10.95."+(172+i));
							jsonObj.put("robotState", ""+DeviceState.ALARMING);
							jsonObj.put("robotKey", "10.10.95."+(172+i)+":9006");
							
							jsonObj.put("machineWPIDs", "M00004ABCD;M00005ABCD;M00006ABCD");
							jsonObj.put("machineWPSlotIDs", "4;5;6");
							jsonObj.put("machineWPStates", "FINISH;FINISH;FINISH");
							
							jsonObj.put("robotWPIDs", "M00001ABCD;M00002ABCD;M00003ABCD");
							jsonObj.put("robotWPSlotIDs", "1;2;3");
							jsonObj.put("robotWPStates", "FINISH;FINISH;FINISH");
							
							jsonObj.put("lineName", lineName);
							jsonObj.put("errorCode", "Robot fails to unload materials from machine");
							jsonObj.put("alarmTime", TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
							
							taskInfo = jsonObj.toString();
							SchedulerTask.getInstance().addTaskInfo(lineName, taskID, taskInfo);
						}
					}
				}
			}
		});
		mnHelp.add(mntmAbout);
		
		JMenuItem mntmForceQuitting = new JMenuItem("Force Quitting");
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
			public void mouseClicked(MouseEvent arg0) {
				if(btnLogin.isEnabled() && 1 == arg0.getButton()){
					if(LoginSystem.userHasLoginned()){
						if(0==JOptionPane.showConfirmDialog(scheduler, "Are you sure of logging out now?", "Log Out?", JOptionPane.YES_NO_OPTION)){
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
		btnLogin.setToolTipText("Login system");
		btnLogin.setBorderPainted(false);
		btnLogin.setIcon(new ImageIcon(Scheduler.class.getResource("/com/cncmes/img/login_24.png")));
		toolBar.add(btnLogin);
		
		btnConfig = new JButton("");
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
		btnConfig.setToolTipText("Communication config");
		btnConfig.setBorderPainted(false);
		btnConfig.setIcon(new ImageIcon(Scheduler.class.getResource("/com/cncmes/img/setting_24.png")));
		toolBar.add(btnConfig);
		
		btnStart = new JButton("");
		btnStart.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
//				cnc72orderField.setEnabled(false);
//				cnc79orderField.setEnabled(false);
//				confirmOrder.setEnabled(false);
				if(1 == arg0.getButton() && btnStart.isEnabled()){
					String msg = SchedulerServer.getInstance().start();
					if("OK".equals(msg)){
						for(int i=0; i<10; i++){
							refreshGUI(i);
						}
						setStartButtonEnabled(false);
					}else{
						JOptionPane.showMessageDialog(contentPane, msg, "Scheduler Launch Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		btnStart.setToolTipText("Start scheduler");
		btnStart.setBorderPainted(false);
		btnStart.setIcon(new ImageIcon(Scheduler.class.getResource("/com/cncmes/img/start_24.png")));
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
		btnStop.setToolTipText("Stop scheduler");
		btnStop.setBorderPainted(false);
		btnStop.setIcon(new ImageIcon(Scheduler.class.getResource("/com/cncmes/img/stop_24.png")));
		toolBar.add(btnStop);
		
		JButton btnExit = new JButton("");
		btnExit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(1 == arg0.getButton()){
					exitSystem(false);
				}
			}
		});
		btnExit.setToolTipText("Exit from system");
		btnExit.setBorderPainted(false);
		btnExit.setIcon(new ImageIcon(Scheduler.class.getResource("/com/cncmes/img/Exit_24.png")));
		toolBar.add(btnExit);
//add at 2023 11 28-----------------------------------------------------------------------------------------
		JLabel lblBlankLabel1 = new JLabel("                                                                                        ");
		toolBar.add(lblBlankLabel1);
		lblCurrentProcessMoCode = new JLabel(" There are currently not orders to process ");
		toolBar.add(lblCurrentProcessMoCode);
//		JLabel lblBlankLabel1 = new JLabel("    ");
//		toolBar.add(lblBlankLabel1);
//		JLabel lblCNC72orderLabel = new JLabel("CNC72 MoCode  :      ");
//		toolBar.add(lblCNC72orderLabel);
//		cnc72orderField = new JTextField();
//		toolBar.add(cnc72orderField);
//		cnc72orderComBox = new JComboBox<>();
//		toolBar.add(cnc72orderComBox);
//
//		JLabel lblBlankLabel2 = new JLabel("     ");
//		toolBar.add(lblBlankLabel2);
//		JLabel lblCNC79orderLabel = new JLabel("CNC79 MoCode  :      ");
//		toolBar.add(lblCNC79orderLabel);
//		cnc79orderField = new JTextField();
//		toolBar.add(cnc79orderField);
////		cnc79orderComBox = new JComboBox<>();
////		toolBar.add(cnc79orderComBox);
//		confirmOrder = new JButton("confirm");
//		confirmOrder.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				String cnc72order = cnc72orderField.getText();
//				String cnc79order = cnc79orderField.getText();
//				if(cnc72order==null||cnc72order.equals("")||cnc79order==null||cnc79order.equals(""))
//				{
//					JOptionPane.showMessageDialog(null,"CNC MoCode can not be blank!","ERROR",JOptionPane.WARNING_MESSAGE);
//				}else {
//					//验证 MoCode 是否存在
//					CNCProcessCard cncProcessCard72 = getProcessCardDTO(new String[] { cnc72order.trim() });
//					CNCProcessCard cncProcessCard79 = getProcessCardDTO(new String[] { cnc79order.trim() });
//					if(cncProcessCard72 == null||cncProcessCard79 == null)
//					{	//MoCode 不存在
//						JOptionPane.showMessageDialog(null,"CNC MoCode doesn't exist!","ERROR",JOptionPane.WARNING_MESSAGE);
//					}else {
//
//					}
//				}
//			}
//		});
//		toolBar.add(confirmOrder);

//-----------------------------------------------------------------------------------------------------------
		JPanel panelMain = new JPanel();
		contentPane.add(panelMain, BorderLayout.CENTER);
		panelMain.setLayout(new GridLayout(0, 1, 0, 0));
		statisticNewData = SchedulerStatistic.getInstance().getTableData(statisticCurLine);
		statisticRawData = statisticNewData;
		
		JPanel panelMachine = new JPanel();
		panelMachine.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panelMain.add(panelMachine);
		panelMachine.setLayout(new BorderLayout(0, 0));
		
		JPanel panelMachineOP = new JPanel();
		panelMachine.add(panelMachineOP, BorderLayout.NORTH);
		GridBagLayout gbl_panelMachineOP = new GridBagLayout();
		gbl_panelMachineOP.columnWidths = new int[] {180, 200, 80, 200, 140};
		gbl_panelMachineOP.rowHeights = new int[] {0};
		gbl_panelMachineOP.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0};
		gbl_panelMachineOP.rowWeights = new double[]{0.0};
		panelMachineOP.setLayout(gbl_panelMachineOP);
		
		JLabel lblMachineLineName = new JLabel("Machine Info : Line Name");
		lblMachineLineName.setIcon(new ImageIcon(Scheduler.class.getResource("/com/cncmes/img/3d_printer_24.png")));
		GridBagConstraints gbc_lblMachineLineName = new GridBagConstraints();
		gbc_lblMachineLineName.insets = new Insets(0, 0, 0, 5);
		gbc_lblMachineLineName.anchor = GridBagConstraints.EAST;
		gbc_lblMachineLineName.gridx = 0;
		gbc_lblMachineLineName.gridy = 0;
		panelMachineOP.add(lblMachineLineName, gbc_lblMachineLineName);
		
		JComboBox<String> comboBoxMachineLineName = new JComboBox<String>();
		comboBoxMachineLineName.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				machineCurLine = comboBoxMachineLineName.getSelectedItem().toString();
				refreshGUI(0);
			}
		});
		comboBoxMachineLineName.setModel(new DefaultComboBoxModel<String>(new String[] {"All", "D2E1D1A", "D2E1D2A", "TS61A", "TS62A"}));
		GridBagConstraints gbc_comboBoxMachineLineName = new GridBagConstraints();
		gbc_comboBoxMachineLineName.insets = new Insets(0, 0, 0, 5);
		gbc_comboBoxMachineLineName.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxMachineLineName.gridx = 1;
		gbc_comboBoxMachineLineName.gridy = 0;
		panelMachineOP.add(comboBoxMachineLineName, gbc_comboBoxMachineLineName);
		
		JLabel lblMachineState = new JLabel("CNC State");
		GridBagConstraints gbc_lblMachineState = new GridBagConstraints();
		gbc_lblMachineState.anchor = GridBagConstraints.EAST;
		gbc_lblMachineState.insets = new Insets(0, 0, 0, 5);
		gbc_lblMachineState.gridx = 2;
		gbc_lblMachineState.gridy = 0;
		panelMachineOP.add(lblMachineState, gbc_lblMachineState);
		
		JComboBox<String> comboBoxMachineState = new JComboBox<String>();
		comboBoxMachineState.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				machineCurState = comboBoxMachineState.getSelectedItem().toString();
				refreshGUI(0);
			}
		});
		comboBoxMachineState.setModel(new DefaultComboBoxModel<String>(new String[] {"All", "Working", "Standby", "Shutdown", "Alarm"}));
		GridBagConstraints gbc_comboBoxMachineState = new GridBagConstraints();
		gbc_comboBoxMachineState.insets = new Insets(0, 0, 0, 5);
		gbc_comboBoxMachineState.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxMachineState.gridx = 3;
		gbc_comboBoxMachineState.gridy = 0;
		panelMachineOP.add(comboBoxMachineState, gbc_comboBoxMachineState);
		
		JScrollPane scrollPaneMachine = new JScrollPane();
		machineNewData = SchedulerMachine.getInstance().getTableData(machineCurLine,machineCurState);
		machineRawData = machineNewData;
		tableMachine = new JTable(new MyTableModel(SchedulerMachine.getInstance().getTableTitle(),machineNewData));
		tableMachine.setRowHeight(25);
		fitTableColumns(tableMachine);
		scrollPaneMachine.setViewportView(tableMachine);
		panelMachine.add(scrollPaneMachine, BorderLayout.CENTER);
		
		panelMachineExtra = new JPanel();
		panelMachine.add(panelMachineExtra, BorderLayout.SOUTH);
		GridBagLayout gbl_panelMachineExtra = new GridBagLayout();
		gbl_panelMachineExtra.columnWidths = new int[] {140, 140, 240, 140, 140};
		gbl_panelMachineExtra.rowHeights = new int[] {0};
		gbl_panelMachineExtra.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panelMachineExtra.rowWeights = new double[]{0.0};
		panelMachineExtra.setLayout(gbl_panelMachineExtra);
		
		lblMachineworking = new JLabel("Working Machine: 0");
		GridBagConstraints gbc_lblMachineworking = new GridBagConstraints();
		gbc_lblMachineworking.insets = new Insets(0, 0, 0, 5);
		gbc_lblMachineworking.gridx = 0;
		gbc_lblMachineworking.gridy = 0;
		panelMachineExtra.add(lblMachineworking, gbc_lblMachineworking);
		
		lblMachinestandby = new JLabel("Standby Machine: 0");
		GridBagConstraints gbc_lblMachinestandby = new GridBagConstraints();
		gbc_lblMachinestandby.insets = new Insets(0, 0, 0, 5);
		gbc_lblMachinestandby.gridx = 1;
		gbc_lblMachinestandby.gridy = 0;
		panelMachineExtra.add(lblMachinestandby, gbc_lblMachinestandby);
		
		lblMachineshutdown = new JLabel("Shutdown Machine: 0");
		GridBagConstraints gbc_lblMachineshutdown = new GridBagConstraints();
		gbc_lblMachineshutdown.insets = new Insets(0, 0, 0, 5);
		gbc_lblMachineshutdown.gridx = 2;
		gbc_lblMachineshutdown.gridy = 0;
		panelMachineExtra.add(lblMachineshutdown, gbc_lblMachineshutdown);
		
		lblMachinealarm = new JLabel("Alarm Machine: 0");
		GridBagConstraints gbc_lblMachinealarm = new GridBagConstraints();
		gbc_lblMachinealarm.insets = new Insets(0, 0, 0, 5);
		gbc_lblMachinealarm.gridx = 3;
		gbc_lblMachinealarm.gridy = 0;
		panelMachineExtra.add(lblMachinealarm, gbc_lblMachinealarm);
		
		lblMachinePlan = new JLabel("Plan Machine: 0");
		GridBagConstraints gbc_lblMachinePlan = new GridBagConstraints();
		gbc_lblMachinePlan.gridx = 4;
		gbc_lblMachinePlan.gridy = 0;
		panelMachineExtra.add(lblMachinePlan, gbc_lblMachinePlan);
		
		JPanel panelRobot = new JPanel();
		panelRobot.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panelMain.add(panelRobot);
		panelRobot.setLayout(new BorderLayout(0, 0));
		
		JPanel panelRobotOP = new JPanel();
		panelRobot.add(panelRobotOP, BorderLayout.NORTH);
		GridBagLayout gbl_panelRobotOP = new GridBagLayout();
		gbl_panelRobotOP.columnWidths = new int[] {180, 200, 80, 200, 140};
		gbl_panelRobotOP.rowHeights = new int[] {0};
		gbl_panelRobotOP.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0};
		gbl_panelRobotOP.rowWeights = new double[]{0.0};
		panelRobotOP.setLayout(gbl_panelRobotOP);
		
		JLabel lblWorkerLineName = new JLabel("Worker Info : Line Name");
		lblWorkerLineName.setIcon(new ImageIcon(Scheduler.class.getResource("/com/cncmes/img/robots_24.png")));
		GridBagConstraints gbc_lblWorkerLineName = new GridBagConstraints();
		gbc_lblWorkerLineName.insets = new Insets(0, 0, 0, 5);
		gbc_lblWorkerLineName.anchor = GridBagConstraints.EAST;
		gbc_lblWorkerLineName.gridx = 0;
		gbc_lblWorkerLineName.gridy = 0;
		panelRobotOP.add(lblWorkerLineName, gbc_lblWorkerLineName);
		
		JComboBox<String> comboBoxWorkerLineName = new JComboBox<String>();
		comboBoxWorkerLineName.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				robotCurLine = comboBoxWorkerLineName.getSelectedItem().toString();
				refreshGUI(1);
			}
		});
		comboBoxWorkerLineName.setModel(new DefaultComboBoxModel<String>(new String[] {"All", "D2E1D1A", "D2E1D2A", "TS61A", "TS62A"}));
		GridBagConstraints gbc_comboBoxWorkerLineName = new GridBagConstraints();
		gbc_comboBoxWorkerLineName.insets = new Insets(0, 0, 0, 5);
		gbc_comboBoxWorkerLineName.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxWorkerLineName.gridx = 1;
		gbc_comboBoxWorkerLineName.gridy = 0;
		panelRobotOP.add(comboBoxWorkerLineName, gbc_comboBoxWorkerLineName);
		
		JLabel lblWorkerState = new JLabel("Robot State");
		GridBagConstraints gbc_lblWorkerState = new GridBagConstraints();
		gbc_lblWorkerState.anchor = GridBagConstraints.EAST;
		gbc_lblWorkerState.insets = new Insets(0, 0, 0, 5);
		gbc_lblWorkerState.gridx = 2;
		gbc_lblWorkerState.gridy = 0;
		panelRobotOP.add(lblWorkerState, gbc_lblWorkerState);
		
		JComboBox<String> comboBoxWorkerState = new JComboBox<String>();
		comboBoxWorkerState.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				robotCurState = comboBoxWorkerState.getSelectedItem().toString();
			}
		});
		comboBoxWorkerState.setModel(new DefaultComboBoxModel<String>(new String[] {"All", "Working", "Standby", "Shutdown", "Alarm"}));
		GridBagConstraints gbc_comboBoxWorkerState = new GridBagConstraints();
		gbc_comboBoxWorkerState.insets = new Insets(0, 0, 0, 5);
		gbc_comboBoxWorkerState.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxWorkerState.gridx = 3;
		gbc_comboBoxWorkerState.gridy = 0;
		panelRobotOP.add(comboBoxWorkerState, gbc_comboBoxWorkerState);
		
		JScrollPane scrollPaneRobot = new JScrollPane();
		robotNewData = SchedulerRobot.getInstance().getTableData(robotCurLine,robotCurState);
		robotRawData = robotNewData;
		tableRobot = new JTable(new MyTableModel(SchedulerRobot.getInstance().getTableTitle(),robotNewData));
		tableRobot.setRowHeight(25);
		fitTableColumns(tableRobot);
		scrollPaneRobot.setViewportView(tableRobot);
		panelRobot.add(scrollPaneRobot, BorderLayout.CENTER);
		
		panelRobotExtra = new JPanel();
		panelRobot.add(panelRobotExtra, BorderLayout.SOUTH);
		GridBagLayout gbl_panelRobotExtra = new GridBagLayout();
		gbl_panelRobotExtra.columnWidths = new int[] {140, 140, 160, 140, 140};
		gbl_panelRobotExtra.rowHeights = new int[] {0};
		gbl_panelRobotExtra.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
		gbl_panelRobotExtra.rowWeights = new double[]{0.0};
		panelRobotExtra.setLayout(gbl_panelRobotExtra);
		
		lblRobotWorking = new JLabel("Working Robot: 0");
		GridBagConstraints gbc_lblRobotWorking = new GridBagConstraints();
		gbc_lblRobotWorking.insets = new Insets(0, 0, 0, 5);
		gbc_lblRobotWorking.gridx = 0;
		gbc_lblRobotWorking.gridy = 0;
		panelRobotExtra.add(lblRobotWorking, gbc_lblRobotWorking);
		
		lblRobotstandby = new JLabel("Standby Robot: 0");
		GridBagConstraints gbc_lblRobotstandby = new GridBagConstraints();
		gbc_lblRobotstandby.insets = new Insets(0, 0, 0, 5);
		gbc_lblRobotstandby.gridx = 1;
		gbc_lblRobotstandby.gridy = 0;
		panelRobotExtra.add(lblRobotstandby, gbc_lblRobotstandby);
		
		lblRobotshutdown = new JLabel("Shutdown Robot: 0");
		GridBagConstraints gbc_lblRobotshutdown = new GridBagConstraints();
		gbc_lblRobotshutdown.insets = new Insets(0, 0, 0, 5);
		gbc_lblRobotshutdown.gridx = 2;
		gbc_lblRobotshutdown.gridy = 0;
		panelRobotExtra.add(lblRobotshutdown, gbc_lblRobotshutdown);
		
		lblRobotalarm = new JLabel("Alarm Robot: 0");
		GridBagConstraints gbc_lblRobotalarm = new GridBagConstraints();
		gbc_lblRobotalarm.insets = new Insets(0, 0, 0, 5);
		gbc_lblRobotalarm.gridx = 3;
		gbc_lblRobotalarm.gridy = 0;
		panelRobotExtra.add(lblRobotalarm, gbc_lblRobotalarm);
		
		lblRobotPlan = new JLabel("Plan Robot: 0");
		GridBagConstraints gbc_lblRobotPlan = new GridBagConstraints();
		gbc_lblRobotPlan.gridx = 4;
		gbc_lblRobotPlan.gridy = 0;
		panelRobotExtra.add(lblRobotPlan, gbc_lblRobotPlan);
		
		JPanel panelMaterial = new JPanel();
		panelMaterial.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panelMain.add(panelMaterial);
		panelMaterial.setLayout(new BorderLayout(0, 0));
		
		JPanel panelMaterialOP = new JPanel();
		panelMaterial.add(panelMaterialOP, BorderLayout.NORTH);
		GridBagLayout gbl_panelMaterialOP = new GridBagLayout();
		gbl_panelMaterialOP.columnWidths = new int[] {180, 200, 80, 200, 140};
		gbl_panelMaterialOP.rowHeights = new int[] {0};
		gbl_panelMaterialOP.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0};
		gbl_panelMaterialOP.rowWeights = new double[]{0.0};
		panelMaterialOP.setLayout(gbl_panelMaterialOP);
		
		JLabel lblMaterialLineName = new JLabel("Material Info : Line Name");
		lblMaterialLineName.setIcon(new ImageIcon(Scheduler.class.getResource("/com/cncmes/img/Material_24.png")));
		GridBagConstraints gbc_lblMaterialLineName = new GridBagConstraints();
		gbc_lblMaterialLineName.insets = new Insets(0, 0, 0, 5);
		gbc_lblMaterialLineName.anchor = GridBagConstraints.EAST;
		gbc_lblMaterialLineName.gridx = 0;
		gbc_lblMaterialLineName.gridy = 0;
		panelMaterialOP.add(lblMaterialLineName, gbc_lblMaterialLineName);
		
		JComboBox<String> comboBoxMaterialLineName = new JComboBox<String>();
		comboBoxMaterialLineName.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				materialCurLine = comboBoxMaterialLineName.getSelectedItem().toString();
				refreshGUI(2);
			}
		});
		comboBoxMaterialLineName.setModel(new DefaultComboBoxModel<String>(new String[] {"All", "D2E1D1A", "D2E1D2A", "TS61A", "TS62A"}));
		GridBagConstraints gbc_comboBoxMaterialLineName = new GridBagConstraints();
		gbc_comboBoxMaterialLineName.insets = new Insets(0, 0, 0, 5);
		gbc_comboBoxMaterialLineName.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxMaterialLineName.gridx = 1;
		gbc_comboBoxMaterialLineName.gridy = 0;
		panelMaterialOP.add(comboBoxMaterialLineName, gbc_comboBoxMaterialLineName);
		
		JLabel lblMaterialState = new JLabel("Material State");
		GridBagConstraints gbc_lblMaterialState = new GridBagConstraints();
		gbc_lblMaterialState.anchor = GridBagConstraints.EAST;
		gbc_lblMaterialState.insets = new Insets(0, 0, 0, 5);
		gbc_lblMaterialState.gridx = 2;
		gbc_lblMaterialState.gridy = 0;
		panelMaterialOP.add(lblMaterialState, gbc_lblMaterialState);
		
		JComboBox<String> comboBoxMaterialState = new JComboBox<String>();
		comboBoxMaterialState.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				materialCurState = comboBoxMaterialState.getSelectedItem().toString();
			}
		});
		comboBoxMaterialState.setModel(new DefaultComboBoxModel<String>(new String[] {"All", "Working", "Standby", "Finish", "Plan"}));
		GridBagConstraints gbc_comboBoxMaterialState = new GridBagConstraints();
		gbc_comboBoxMaterialState.insets = new Insets(0, 0, 0, 5);
		gbc_comboBoxMaterialState.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxMaterialState.gridx = 3;
		gbc_comboBoxMaterialState.gridy = 0;
		panelMaterialOP.add(comboBoxMaterialState, gbc_comboBoxMaterialState);
		
		JScrollPane scrollPaneMaterial = new JScrollPane();
		materialNewData = SchedulerMaterial.getInstance().getTableData(materialCurLine,materialCurState);
		materialRawData = materialNewData;
		tableMaterial = new JTable(new MyTableModel(SchedulerMaterial.getInstance().getTableTitle(),materialNewData));
		tableMaterial.setRowHeight(25);
		fitTableColumns(tableMaterial);
		scrollPaneMaterial.setViewportView(tableMaterial);
		panelMaterial.add(scrollPaneMaterial, BorderLayout.CENTER);
		
		panelMaterialExtra = new JPanel();
		panelMaterial.add(panelMaterialExtra, BorderLayout.SOUTH);
		GridBagLayout gbl_panelMaterialExtra = new GridBagLayout();
		gbl_panelMaterialExtra.columnWidths = new int[] {140, 140, 140, 160, 140};
		gbl_panelMaterialExtra.rowHeights = new int[] {0};
		gbl_panelMaterialExtra.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
		gbl_panelMaterialExtra.rowWeights = new double[]{0.0};
		panelMaterialExtra.setLayout(gbl_panelMaterialExtra);
		
		lblWorkpiecetotal = new JLabel("Total Workpiece: 0");
		GridBagConstraints gbc_lblWorkpiecetotal = new GridBagConstraints();
		gbc_lblWorkpiecetotal.insets = new Insets(0, 0, 0, 5);
		gbc_lblWorkpiecetotal.gridx = 0;
		gbc_lblWorkpiecetotal.gridy = 0;
		panelMaterialExtra.add(lblWorkpiecetotal, gbc_lblWorkpiecetotal);
		
		lblWorkpiecestandby = new JLabel("Standby Workpiece: 0");
		GridBagConstraints gbc_lblWorkpiecestandby = new GridBagConstraints();
		gbc_lblWorkpiecestandby.insets = new Insets(0, 0, 0, 5);
		gbc_lblWorkpiecestandby.gridx = 1;
		gbc_lblWorkpiecestandby.gridy = 0;
		panelMaterialExtra.add(lblWorkpiecestandby, gbc_lblWorkpiecestandby);
		
		lblWorkpieceplan = new JLabel("Plan Workpiece: 0");
		GridBagConstraints gbc_lblWorkpieceplan = new GridBagConstraints();
		gbc_lblWorkpieceplan.insets = new Insets(0, 0, 0, 5);
		gbc_lblWorkpieceplan.gridx = 2;
		gbc_lblWorkpieceplan.gridy = 0;
		panelMaterialExtra.add(lblWorkpieceplan, gbc_lblWorkpieceplan);
		
		lblWorkpiecemachining = new JLabel("Machining Workpiece: 0");
		GridBagConstraints gbc_lblWorkpiecemachining = new GridBagConstraints();
		gbc_lblWorkpiecemachining.insets = new Insets(0, 0, 0, 5);
		gbc_lblWorkpiecemachining.gridx = 3;
		gbc_lblWorkpiecemachining.gridy = 0;
		panelMaterialExtra.add(lblWorkpiecemachining, gbc_lblWorkpiecemachining);
		
		lblWorkpiecedone = new JLabel("Done Workpiece: 0");
		GridBagConstraints gbc_lblWorkpiecedone = new GridBagConstraints();
		gbc_lblWorkpiecedone.gridx = 4;
		gbc_lblWorkpiecedone.gridy = 0;
		panelMaterialExtra.add(lblWorkpiecedone, gbc_lblWorkpiecedone);
		
		JPanel panelStatistic = new JPanel();
		panelStatistic.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panelMain.add(panelStatistic);
		panelStatistic.setLayout(new BorderLayout(0, 0));
		
		JPanel panelStatisticOP = new JPanel();
		panelStatistic.add(panelStatisticOP, BorderLayout.NORTH);
		GridBagLayout gbl_panelStatisticOP = new GridBagLayout();
		gbl_panelStatisticOP.columnWidths = new int[] {180, 200, 80, 40, 300};
		gbl_panelStatisticOP.rowHeights = new int[] {0};
		gbl_panelStatisticOP.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panelStatisticOP.rowWeights = new double[]{0.0};
		panelStatisticOP.setLayout(gbl_panelStatisticOP);
		
		JLabel lblStatisticLineName = new JLabel("Statistic Info : Line Name");
		lblStatisticLineName.setIcon(new ImageIcon(Scheduler.class.getResource("/com/cncmes/img/Statistics_24.png")));
		GridBagConstraints gbc_lblStatisticLineName = new GridBagConstraints();
		gbc_lblStatisticLineName.insets = new Insets(0, 0, 5, 5);
		gbc_lblStatisticLineName.anchor = GridBagConstraints.EAST;
		gbc_lblStatisticLineName.gridx = 0;
		gbc_lblStatisticLineName.gridy = 0;
		panelStatisticOP.add(lblStatisticLineName, gbc_lblStatisticLineName);
		
		JComboBox<String> comboBoxStatisticLineName = new JComboBox<String>();
		comboBoxStatisticLineName.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				statisticCurLine = comboBoxStatisticLineName.getSelectedItem().toString();
				refreshGUI(3);
			}
		});
		comboBoxStatisticLineName.setModel(new DefaultComboBoxModel<String>(new String[] {"All", "D2E1D1A", "D2E1D2A", "TS61A", "TS62A"}));
		GridBagConstraints gbc_comboBoxStatisticLineName = new GridBagConstraints();
		gbc_comboBoxStatisticLineName.insets = new Insets(0, 0, 5, 5);
		gbc_comboBoxStatisticLineName.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxStatisticLineName.gridx = 1;
		gbc_comboBoxStatisticLineName.gridy = 0;
		panelStatisticOP.add(comboBoxStatisticLineName, gbc_comboBoxStatisticLineName);
		
		JScrollPane scrollPaneStatistic = new JScrollPane();
		tableStatistic = new JTable(new MyTableModel(SchedulerStatistic.getInstance().getTableTitle(),statisticNewData));
		tableStatistic.setRowHeight(25);
		fitTableColumns(tableStatistic);
		scrollPaneStatistic.setViewportView(tableStatistic);
		panelStatistic.add(scrollPaneStatistic, BorderLayout.CENTER);
		
		panelStatisticExtra = new JPanel();
		panelStatistic.add(panelStatisticExtra, BorderLayout.SOUTH);
		GridBagLayout gbl_panelStatisticExtra = new GridBagLayout();
		gbl_panelStatisticExtra.columnWidths = new int[] {150, 500, 30};
		gbl_panelStatisticExtra.rowHeights = new int[] {0};
		gbl_panelStatisticExtra.columnWeights = new double[]{0.0, Double.MIN_VALUE, 0.0};
		gbl_panelStatisticExtra.rowWeights = new double[]{0.0};
		panelStatisticExtra.setLayout(gbl_panelStatisticExtra);
		
		lblConnectingDevices = new JLabel("Connecting Devices: 100");
		GridBagConstraints gbc_lblConnectingDevices = new GridBagConstraints();
		gbc_lblConnectingDevices.insets = new Insets(0, 0, 0, 5);
		gbc_lblConnectingDevices.gridx = 0;
		gbc_lblConnectingDevices.gridy = 0;
		panelStatisticExtra.add(lblConnectingDevices, gbc_lblConnectingDevices);
		
		lblMessage = new JLabel("Message");
		GridBagConstraints gbc_lblMessage = new GridBagConstraints();
		gbc_lblMessage.anchor = GridBagConstraints.WEST;
		gbc_lblMessage.insets = new Insets(0, 0, 0, 5);
		gbc_lblMessage.gridx = 1;
		gbc_lblMessage.gridy = 0;
		panelStatisticExtra.add(lblMessage, gbc_lblMessage);
		
		lblHeartbeat = new JLabel("59");
		GridBagConstraints gbc_lblHeartbeat = new GridBagConstraints();
		gbc_lblHeartbeat.gridx = 2;
		gbc_lblHeartbeat.gridy = 0;
		panelStatisticExtra.add(lblHeartbeat, gbc_lblHeartbeat);
		
		JPanel panelTask = new JPanel();
		panelTask.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		contentPane.add(panelTask, BorderLayout.WEST);
		GridBagLayout gbl_panelTask = new GridBagLayout();
		gbl_panelTask.columnWidths = new int[] {200};
		gbl_panelTask.rowHeights = new int[] {30, 520};
		gbl_panelTask.columnWeights = new double[]{1.0};
		gbl_panelTask.rowWeights = new double[]{0.0, 1.0};
		panelTask.setLayout(gbl_panelTask);
		
		JLabel lblTaskScheduled = new JLabel("Task Scheduled");
		lblTaskScheduled.setIcon(new ImageIcon(Scheduler.class.getResource("/com/cncmes/img/Scheduler_24.png")));
		GridBagConstraints gbc_lblTaskScheduled = new GridBagConstraints();
		gbc_lblTaskScheduled.insets = new Insets(0, 0, 5, 0);
		gbc_lblTaskScheduled.gridx = 0;
		gbc_lblTaskScheduled.gridy = 0;
		panelTask.add(lblTaskScheduled, gbc_lblTaskScheduled);
		
		JScrollPane scrollPaneTask = new JScrollPane();
		GridBagConstraints gbc_scrollPaneTask = new GridBagConstraints();
		gbc_scrollPaneTask.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneTask.gridx = 0;
		gbc_scrollPaneTask.gridy = 1;
		panelTask.add(scrollPaneTask, gbc_scrollPaneTask);
		
		listTask = new JList<String>();
		setTaskListContent(null);
		listTask.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		scrollPaneTask.setViewportView(listTask);
		refreshGUI(5);
		if(!"OK".equals(msg)){
			setStartButtonEnabled(false, false);
		}else{
			setStartButtonEnabled(true);
		}
		refreshButtonsEnabled();
	}
	
	private void setTaskListContent(String[] tasks){
		String[] values;
		if(null == tasks){
			values = new String[] {" "};
		}else{
			values = tasks;
		}
		
		GUIUtils.setJListContent(listTask, values);
	}
	
	private static void checkPreviousInstance(){
		try {
			svrSocket = new ServerSocket(PORTSVRSOCKET);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "CNC Scheduler is already started", "Program Launch", JOptionPane.INFORMATION_MESSAGE);
			System.exit(0);
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
			
			trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(Scheduler.class.getResource("/com/cncmes/img/Butterfly_pink_24.png")),"CNC Scheduler",popup);
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
	
	private void refreshTaskInfo(){
		setTaskListContent(SchedulerTask.getInstance().getTaskShowList());
	}
	
	private void refreshRunningMsg(){
		try {
			int[] ports = SchedulerCfg.getInstance().getAllPorts();
			lblConnectingDevices.setText("Total Connections: "+SocketServer.getAcceptConnections(ports));
			lblMessage.setText(RunningMsg.get());
			lblHeartbeat.setText(TimeUtils.getCurrentDate("ss"));
		} catch (Exception e) {
		}
	}
	
	private void refreshStatisticInfo() {
		try {
			if(!statisticCurLine.equals(statisticOriLine)){
				statisticOriLine = statisticCurLine;
				statisticNewData = SchedulerStatistic.getInstance().getTableData(statisticCurLine);
				statisticRawData = statisticNewData;
				tableStatistic.setModel(new MyTableModel(SchedulerStatistic.getInstance().getTableTitle(),statisticNewData));
				fitTableColumns(tableStatistic);
			}else{
				statisticRawData = statisticNewData;
				statisticNewData = SchedulerStatistic.getInstance().getTableData(statisticCurLine);
				if(statisticNewData.length != statisticRawData.length){
					statisticRawData = statisticNewData;
					tableStatistic.setModel(new MyTableModel(SchedulerStatistic.getInstance().getTableTitle(),statisticNewData));
					fitTableColumns(tableStatistic);
				}else{
					String newVal = "";
					String oldVal = "";
					for(int row=0; row<statisticNewData.length; row++){
						for(int col=0; col<statisticNewData[0].length; col++){
							newVal = "";
							oldVal = "";
							if(null != statisticNewData[row][col]) newVal = String.valueOf(statisticNewData[row][col]);
							if(null != statisticRawData[row][col]) oldVal = String.valueOf(statisticRawData[row][col]);
							
							if(!newVal.equals(oldVal)){
								statisticRawData[row][col] = statisticNewData[row][col];
								tableStatistic.setValueAt(statisticNewData[row][col], row, col);
								fitTableColumns(tableStatistic);
								tableStatistic.repaint();
							}
						}
					}
				}
			}
		} catch (Exception e) {
		}
	}
	
	private void refreshMachineInfo() {
		try {
			LinkedHashMap<SchedulerDataItems,Object> statisticInfo = SchedulerMachine.getInstance().getStatisticData(machineCurLine);
			
			lblMachineworking.setText("Working Machine: "+statisticInfo.get(SchedulerDataItems.QTYWORKING));
			lblMachinestandby.setText("Standby Machine: "+statisticInfo.get(SchedulerDataItems.QTYSTANDBY));
			lblMachineshutdown.setText("Shutdown Machine: "+statisticInfo.get(SchedulerDataItems.QTYSHUTDOWN));
			lblMachinealarm.setText("Alarm Machine: "+statisticInfo.get(SchedulerDataItems.QTYALARM));
			lblMachinePlan.setText("Plan Machine: "+statisticInfo.get(SchedulerDataItems.QTYPLAN));
			panelMachineExtra.updateUI();
			
			if(!machineCurLine.equals(machineOriLine) || !machineCurState.equals(machineOriState)){
				machineOriLine = machineCurLine;
				machineOriState = machineCurState;
				machineNewData = SchedulerMachine.getInstance().getTableData(machineCurLine,machineCurState);
				machineRawData = machineNewData;
				tableMachine.setModel(new MyTableModel(SchedulerMachine.getInstance().getTableTitle(),machineNewData));
				fitTableColumns(tableMachine);
			}else{
				machineRawData = machineNewData;
				machineNewData = SchedulerMachine.getInstance().getTableData(machineCurLine,machineCurState);
				if(machineNewData.length != machineRawData.length){
					machineRawData = machineNewData;
					tableMachine.setModel(new MyTableModel(SchedulerMachine.getInstance().getTableTitle(),machineNewData));
					fitTableColumns(tableMachine);
				}else{
					String newVal = "";
					String oldVal = "";
					for(int row=0; row<machineNewData.length; row++){
						for(int col=0; col<machineNewData[0].length; col++){
							newVal = "";
							oldVal = "";
							if(null != machineNewData[row][col]) newVal = String.valueOf(machineNewData[row][col]);
							if(null != machineRawData[row][col]) oldVal = String.valueOf(machineRawData[row][col]);
							
							if(!newVal.equals(oldVal)){
								machineRawData[row][col] = machineNewData[row][col];
								tableMachine.setValueAt(machineNewData[row][col], row, col);
								fitTableColumns(tableMachine);
								tableMachine.repaint();
							}
						}
					}
				}
			}
		} catch (Exception e) {
		}
	}
	
	private void refreshRobotInfo() {
		try {
			LinkedHashMap<SchedulerDataItems,Object> statisticInfo = SchedulerRobot.getInstance().getStatisticData(robotCurLine);
			
			lblRobotWorking.setText("Working Robot: "+statisticInfo.get(SchedulerDataItems.QTYWORKING));
			lblRobotstandby.setText("Standby Robot: "+statisticInfo.get(SchedulerDataItems.QTYSTANDBY));
			lblRobotshutdown.setText("Shutdown Robot: "+statisticInfo.get(SchedulerDataItems.QTYSHUTDOWN));
			lblRobotalarm.setText("Alarm Robot: "+statisticInfo.get(SchedulerDataItems.QTYALARM));
			lblRobotPlan.setText("Plan Robot: "+statisticInfo.get(SchedulerDataItems.QTYPLAN));
			panelRobotExtra.updateUI();
			
			if(!robotCurLine.equals(robotOriLine) || !robotCurState.equals(robotOriState)){
				robotOriLine = robotCurLine;
				robotOriState = robotCurState;
				robotNewData = SchedulerRobot.getInstance().getTableData(robotCurLine,robotCurState);
				robotRawData = robotNewData;
				tableRobot.setModel(new MyTableModel(SchedulerRobot.getInstance().getTableTitle(),robotNewData));
				fitTableColumns(tableRobot);
			}else{
				robotRawData = robotNewData;
				robotNewData = SchedulerRobot.getInstance().getTableData(robotCurLine,robotCurState);
				if(robotNewData.length != robotRawData.length){
					robotRawData = robotNewData;
					tableRobot.setModel(new MyTableModel(SchedulerRobot.getInstance().getTableTitle(),robotNewData));
					fitTableColumns(tableRobot);
				}else{
					String newVal = "";
					String oldVal = "";
					for(int row=0; row<robotNewData.length; row++){
						for(int col=0; col<robotNewData[0].length; col++){
							newVal = "";
							oldVal = "";
							if(null != robotNewData[row][col]) newVal = String.valueOf(robotNewData[row][col]);
							if(null != robotRawData[row][col]) oldVal = String.valueOf(robotRawData[row][col]);
							
							if(!newVal.equals(oldVal)){
								robotRawData[row][col] = robotNewData[row][col];
								tableRobot.setValueAt(robotNewData[row][col], row, col);
								fitTableColumns(tableRobot);
								tableRobot.repaint();
							}
						}
					}
				}
			}
		} catch (Exception e) {
		}
	}
	
	private void refreshMaterialInfo() {
		try {
			LinkedHashMap<SchedulerDataItems,Object> statisticInfo = SchedulerMaterial.getInstance().getStatisticData(materialCurLine);
			
			lblWorkpiecetotal.setText("Total Workpiece: "+statisticInfo.get(SchedulerDataItems.QTYTOTAL));
			lblWorkpiecestandby.setText("Standby Workpiece: "+statisticInfo.get(SchedulerDataItems.QTYSTANDBY));
			lblWorkpieceplan.setText("Plan Workpiece: "+statisticInfo.get(SchedulerDataItems.QTYPLAN));
			lblWorkpiecemachining.setText("Machining Workpiece: "+statisticInfo.get(SchedulerDataItems.QTYWORKING));
			lblWorkpiecedone.setText("Done Workpiece: "+statisticInfo.get(SchedulerDataItems.QTYFINISH));
			panelMaterialExtra.updateUI();
			
			if(!materialCurLine.equals(materialOriLine) || !materialCurState.equals(materialOriState)){
				materialOriLine = materialCurLine;
				materialOriState = materialCurState;
				materialNewData = SchedulerMaterial.getInstance().getTableData(materialCurLine,materialCurState);
				materialRawData = materialNewData;
				tableMaterial.setModel(new MyTableModel(SchedulerMaterial.getInstance().getTableTitle(),materialNewData));
				fitTableColumns(tableMaterial);
			}else{
				materialRawData = materialNewData;
				materialNewData = SchedulerMaterial.getInstance().getTableData(materialCurLine,materialCurState);
				if(materialNewData.length != materialRawData.length){
					materialRawData = materialNewData;
					tableMaterial.setModel(new MyTableModel(SchedulerMaterial.getInstance().getTableTitle(),materialNewData));
					fitTableColumns(tableMaterial);
				}else{
					String newVal = "";
					String oldVal = "";
					for(int row=0; row<materialNewData.length; row++){
						for(int col=0; col<materialNewData[0].length; col++){
							newVal = "";
							oldVal = "";
							if(null != materialNewData[row][col]) newVal = String.valueOf(materialNewData[row][col]);
							if(null != materialRawData[row][col]) oldVal = String.valueOf(materialRawData[row][col]);
							
							if(!newVal.equals(oldVal)){
								materialRawData[row][col] = materialNewData[row][col];
								tableMaterial.setValueAt(materialNewData[row][col], row, col);
								fitTableColumns(tableMaterial);
								tableMaterial.repaint();
							}
						}
					}
				}
			}
		} catch (Exception e) {
		}
	}
	
	public void refreshGUI(int guiIndex){
		switch(guiIndex){
		case 0:
			refreshMachineInfo();
			break;
		case 1:
			refreshRobotInfo();
			break;
		case 2:
			refreshMaterialInfo();
			break;
		case 3:
			refreshStatisticInfo();
			break;
		case 4:
			refreshTaskInfo();
			break;
		case 5:
			refreshRunningMsg();
			break;
		default:
			break;
		}
	}
	
	private void exitSystem(boolean force) {
		String msg = "OK";
		
		msg = MySystemUtils.sysReadyToStop();
		if(!"OK".equals(msg)){
			if(!force){
				JOptionPane.showMessageDialog(contentPane, msg, "Can't Quit from the Scheduler Now", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
		msg = SchedulerServer.getInstance().stop();
		if("OK".equals(msg)){
			int rtn = JOptionPane.showConfirmDialog(scheduler.getContentPane(), "Are you sure of quiting from CNC Scheduler", "Exit", JOptionPane.YES_NO_OPTION);
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
			JOptionPane.showMessageDialog(contentPane, msg, "Can't Quit from the Scheduler Now", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void stopSystem() {
		String msg = "OK";
		
		msg = MySystemUtils.sysReadyToStop();
		if(!"OK".equals(msg)){
			JOptionPane.showMessageDialog(contentPane, msg, "Can't Stop the Scheduler Now", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		msg = SchedulerServer.getInstance().stop();
		if("OK".equals(msg)){
//			cnc72orderField.setEnabled(true);
//			cnc79orderField.setEnabled(true);
//			confirmOrder.setEnabled(true);
			setStartButtonEnabled(true);
		}else{
			JOptionPane.showMessageDialog(contentPane, msg, "Can't Stop the Scheduler Now", JOptionPane.ERROR_MESSAGE);
		}
	}

	public JComboBox<String> getCnc72orderComBox() {
		return cnc72orderComBox;
	}

	public void setCnc72orderComBox(JComboBox<String> cnc72orderComBox) {
		this.cnc72orderComBox = cnc72orderComBox;
	}

	public JComboBox<String> getCnc79orderComBox() {
		return cnc79orderComBox;
	}

	public void setCnc79orderComBox(JComboBox<String> cnc79orderComBox) {
		this.cnc79orderComBox = cnc79orderComBox;
	}

	public JLabel getLblCurrentProcessMoCode() {
		return lblCurrentProcessMoCode;
	}

	public void setLblCurrentProcessMoCode(JLabel lblCurrentProcessMoCode) {
		this.lblCurrentProcessMoCode = lblCurrentProcessMoCode;
	}

	// get the object by order No from database
	private CNCProcessCard getProcessCardDTO(String[] orderNo) {
		CNCProcessCard vo = null;
		DAO dao = new DAOImpl(processCardDto);
		try {
			ArrayList<Object> vos = dao.findByCnd(new String[] { "order_no" }, orderNo);
			if (null != vos) {
				vo = (CNCProcessCard) vos.get(0);// if repeat?
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return vo;
	}

	//return all not finish cncProcessCard
	private List<CNCProcessCard> getAllProcessCards()
	{
		ArrayList<Object> vos = null;
		ArrayList<CNCProcessCard> cncProcessCards = new ArrayList<>();

		DAO dao = new DAOImpl(processCardDto);
		try {
			vos = dao.findByCnd(new String[]{"is_delete"},new String[]{"0"});
			for(Object cncProcessCard:vos){
				cncProcessCards.add((CNCProcessCard)cncProcessCard);
			}
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
		return cncProcessCards;
	}

	private List<OrderScheduler> getAllOrderSchedulers()
	{
		List<CNCProcessCard> cncProcessCards = getAllProcessCards();
		if(cncProcessCards==null||cncProcessCards.size()<=0)
		{
			lblCurrentProcessMoCode.setText("There are currently not orders to process");
			return null;
		}
		ArrayList<OrderScheduler> orderSchedulers = new ArrayList<>();
		for (CNCProcessCard cncProcessCard : cncProcessCards)
		{
			OrderScheduler orderInfo = getOrderInfoByMoCode(cncProcessCard.getOrder_no());
			//get process time from  process file

			orderInfo.setProcesstime(FileUtils.getNCProgramTime(orderInfo.getMoCode()));
			orderSchedulers.add(orderInfo);
		}
		Collections.sort(orderSchedulers);          //sort the orderSchedulers base on the requestTime and process time

		return orderSchedulers;
	}

	private OrderScheduler getOrderInfoByMoCode(String order_no)
	{
		DAOImpl dao = new DAOImpl(orderSchedulerDto,true);
		try {
			List<Object> os = dao.findOrderSchedulerByMoCode(order_no);
			for (Object o :os){
				return (OrderScheduler) o;
			}
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
		return null;
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
		private int rowCount = 0;
		private int colCount = 0;
		
		public MyTableModel(String[] tableTitle,Object[][] tableData){
			super();
			title = tableTitle;
			myData = tableData;
			colCount = tableTitle.length;
			if(null != tableData) rowCount = tableData.length;
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
			return false;
		}
	}
}