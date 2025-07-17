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

import com.cncmes.base.CNC;
import com.cncmes.base.CncItems;
import com.cncmes.base.CncWebAPIItems;
import com.cncmes.base.DeviceState;
import com.cncmes.base.DriverItems;
import com.cncmes.base.PermissionItems;
import com.cncmes.base.RobotItems;
import com.cncmes.base.SchedulerItems;
import com.cncmes.base.TaskItems;
import com.cncmes.base.WorkpieceItems;
import com.cncmes.ctrl.CncFactory;
import com.cncmes.ctrl.SchedulerClient;
import com.cncmes.data.CncData;
import com.cncmes.data.CncDriver;
import com.cncmes.data.CncWebAPI;
import com.cncmes.data.RobotData;
import com.cncmes.data.SystemConfig;
import com.cncmes.data.TaskData;
import com.cncmes.data.WorkpieceData;
import com.cncmes.utils.DataUtils;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.LoginSystem;
import com.cncmes.utils.MathUtils;
import com.cncmes.utils.NetUtils;
import com.cncmes.utils.TimeUtils;
import com.cncmes.utils.UploadNCProgram;
import com.cncmes.utils.XmlUtils;

import net.sf.json.JSONObject;

import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JScrollPane;
import java.awt.Insets;
import javax.swing.JList;
import javax.swing.JTextPane;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.border.BevelBorder;
import javax.swing.JTextField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JToolBar;
import javax.swing.AbstractListModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

public class DeviceDiagnostic extends JFrame {
	private static final long serialVersionUID = 1L;
	private static DeviceDiagnostic frame = new DeviceDiagnostic();
	private JPanel contentPane;
	private SystemTray systemTray = null;
	private TrayIcon trayIcon = null;
	private JTextField textFieldEngineerName;
	private JLabel lblRunningMessage;
	private JTextPane textPaneErrorDescription;
	private JTextPane textPaneFailureAnalysis;
	private JCheckBox chckbxProblemFixed;
	private JList<String> listAlarmTask;
	private JButton btnStartHandling;
	private JButton btnSubmitFaReport;
	private JButton btnLogin;
	private JButton btnSysConfig;
	private JButton btnRobot;
	private JButton btnMachine;
	private JMenuItem mntmRobotController;
	private JMenuItem mntmCncController;
	private JMenuItem mntmCncEthernetCmd;
	
	private String curLineName = "";
	private String curTaskID = "";
	private static BarcodeInput barcodeInput = BarcodeInput.getInstance();
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame = DeviceDiagnostic.getInstance();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static DeviceDiagnostic getInstance(){
		return frame;
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
			
			trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(DeviceDiagnostic.class.getResource("/com/cncmes/img/doctor_32.png")),"CNC Device Diagnostic Client",popup);
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
	
	private void refreshListObject(JList<String> list, String[] data){
		String[] values;
		if(null != data && data.length > 0){
			values = data;
		}else{
			values = new String[]{""};
		}
		
		list.setModel(new AbstractListModel<String>() {
			private static final long serialVersionUID = 2L;
			public int getSize() {
				return values.length;
			}
			public String getElementAt(int index) {
				return values[index];
			}
		});
		
		boolean taskFound = false;
		TaskData taskData = TaskData.getInstance();
		String[] task;
		String taskID = "", lineName = "";
		for(int i=0; i<values.length; i++){
			if(!"".equals(values[i])){
				task = values[i].split(":");
				taskID = task[1].trim();
				lineName = (String) taskData.getData(taskID).get(TaskItems.LINENAME);
				if(!"".equals(curLineName) && !"".equals(curTaskID)){
					if(curLineName.equals(lineName) && curTaskID.equals(taskID)){
						list.setSelectedIndex(i);
						taskFound = true;
						break;
					}
				}else{
					curLineName = lineName;
					curTaskID = taskID;
					list.setSelectedIndex(i);
					taskFound = true;
					break;
				}
			}
		}
		list.repaint();
		
		if(!taskFound){
			if(!"".equals(lineName) && !"".equals(taskID)){
				curLineName = lineName;
				curTaskID = taskID;
			}else{
				curLineName = "";
				curTaskID = "";
			}
		}
		
		refreshErrorDescription();
	}
	
	private void refreshErrorDescription(){
		String errDesc = "",machineWPIDs="",machineWPSlotIDs="",machineWPStates="";
		String robotWPIDs="",robotWPSlotIDs="",robotWPStates="",engineer="",temp="";
		String[] ids = null, states = null, slots = null;
		TaskData taskData = TaskData.getInstance();
		
		if(!"".equals(curLineName) && !"".equals(curTaskID)){
			LinkedHashMap<TaskItems, Object> taskInfo = taskData.getData(curTaskID);
			machineWPIDs = ""+taskInfo.get(TaskItems.MACHINEWPIDS);
			machineWPSlotIDs = ""+taskInfo.get(TaskItems.MACHINEWPSLOTIDS);
			machineWPStates = ""+taskInfo.get(TaskItems.MACHINEWPSTATES);
			robotWPIDs = ""+taskInfo.get(TaskItems.ROBOTWPIDS);
			robotWPSlotIDs = ""+taskInfo.get(TaskItems.ROBOTWPSLOTIDS);
			robotWPStates = ""+taskInfo.get(TaskItems.ROBOTWPSTATES);
			engineer = ""+taskInfo.get(TaskItems.ENGINEER);
			
			errDesc = "ErrInfo: " + taskInfo.get(TaskItems.ERRORCODE);
			errDesc += "\r\nAlarmTime: " + taskInfo.get(TaskItems.ALARMTIME);
			if(!"".equals(engineer)){
				errDesc += "\r\nBeing Handled By: " + engineer;
				errDesc += "/" + taskInfo.get(TaskItems.HANDLINGIP);
				errDesc += "/" + taskInfo.get(TaskItems.STARTHANDLINGTIME);
			}
			errDesc += "\r\nLineName: " + curLineName;
			errDesc += "\r\nMachineIP: " + taskInfo.get(TaskItems.MACHINEIP) + "/" + taskInfo.get(TaskItems.MACHINESTATE);
			if(!"".equals(machineWPIDs)){
				ids = machineWPIDs.split(";");
				states = machineWPStates.split(";");
				slots = machineWPSlotIDs.split(";");
				temp = "";
				for(int i=0; i<ids.length; i++){
					temp += "\r\n" + "Zone_"+slots[i]+": "+ids[i]+"/"+states[i];
				}
				errDesc += temp;
			}
			errDesc += "\r\nRobotIP: " + taskInfo.get(TaskItems.ROBOTIP) + "/" + taskInfo.get(TaskItems.ROBOTSTATE);
			if(!"".equals(robotWPIDs)){
				ids = robotWPIDs.split(";");
				states = robotWPStates.split(";");
				slots = robotWPSlotIDs.split(";");
				temp = "";
				for(int i=0; i<ids.length; i++){
					temp += "\r\n" + "Slot_"+slots[i]+": "+ids[i]+"/"+states[i];
				}
				errDesc += temp;
			}
			if(null != lblRunningMessage) setRunningMsg("Task "+curTaskID+" is going to be handled");
		}
		if(null != textPaneErrorDescription){
			textPaneErrorDescription.setText(errDesc);
			textPaneErrorDescription.repaint();
		}
	}
	
	public void refreshGUI() {
		String[] tasks = TaskData.getInstance().taskInfo();
		refreshListObject(listAlarmTask, tasks);
		if(null == tasks){
			setRunningMsg("Congratulations: there is no problematic task now.");
		}
	}
	
	private void setTitle(){
		if(null==frame) return;
		String title = frame.getTitle();
		if(null!=title){
			title = title.split("##")[0] + "##Welcome " + LoginSystem.getUserName();
			frame.setTitle(title);
		}
	}
	
	public void refreshButtonsEnabled(){
		btnSysConfig.setEnabled(!LoginSystem.accessDenied(PermissionItems.SYSCONFIG));
		btnRobot.setEnabled(!LoginSystem.accessDenied(PermissionItems.SYSCONFIG));
		btnMachine.setEnabled(!LoginSystem.accessDenied(PermissionItems.SYSCONFIG));
		btnStartHandling.setEnabled(!LoginSystem.accessDenied(PermissionItems.SYSCONFIG));
		btnSubmitFaReport.setEnabled(!LoginSystem.accessDenied(PermissionItems.SYSCONFIG));
		mntmRobotController.setEnabled(!LoginSystem.accessDenied(PermissionItems.SYSCONFIG));
		mntmCncController.setEnabled(!LoginSystem.accessDenied(PermissionItems.SYSCONFIG));
		mntmCncEthernetCmd.setEnabled(!LoginSystem.accessDenied(PermissionItems.SYSCONFIG));
		setTitle();
		if(LoginSystem.userHasLoginned()){
			btnLogin.setIcon(new ImageIcon(DeviceDiagnostic.class.getResource("/com/cncmes/img/logout_24.png")));
			btnLogin.setToolTipText("Logout System");
			textFieldEngineerName.setText(LoginSystem.getUserName());
		}else{
			btnLogin.setIcon(new ImageIcon(DeviceDiagnostic.class.getResource("/com/cncmes/img/login_24.png")));
			btnLogin.setToolTipText("Login System");
			textFieldEngineerName.setText("");
		}
	}
	
	private String getCrlnStr(String idStr, String slotStr, String prefix){
		String[] ids = null, slots = null;
		String temp = "";
		ids = idStr.split(";");
		slots = slotStr.split(";");
		
		for(int i=0; i<ids.length; i++){
			if("".equals(temp)){
				temp = prefix + " " + slots[i] + ": " + ids[i];
			}else{
				temp += "\r\n" + prefix + " " + slots[i] + ": " + ids[i];
			}
		}
		
		return temp;
	}
	
	private void setRunningMsg(String msg){
		lblRunningMessage.setText(msg);
		lblRunningMessage.repaint();
	}
	
	/**
	 * Create the frame.
	 */
	private DeviceDiagnostic() {
		DataUtils.getDeviceInfo(null);
		setIconImage(Toolkit.getDefaultToolkit().getImage(DeviceDiagnostic.class.getResource("/com/cncmes/img/doctor_32.png")));
		setTitle("CNC Device Diagnostic Client");
		setResizable(false);
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				int rtn = JOptionPane.showConfirmDialog(contentPane, "Run CNC Device Dignostic Client in the background", "Minimize", JOptionPane.YES_NO_OPTION);
				if(0 == rtn){
					frame.dispose();
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
					int rtn = JOptionPane.showConfirmDialog(contentPane, "Are you sure of quiting from CNC Device Diagnostic Client", "Exit", JOptionPane.YES_NO_OPTION);
					if(0 == rtn){
						System.exit(0);
					}
				}
			}
		});
		mntmExit.setIcon(new ImageIcon(DeviceDiagnostic.class.getResource("/com/cncmes/img/Exit_16.png")));
		mnFile.add(mntmExit);
		
		JMenu mnTroubleShooting = new JMenu("Trouble Shooting");
		menuBar.add(mnTroubleShooting);
		
		mntmRobotController = new JMenuItem("Robot Ethernet Cmd");
		mntmRobotController.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(16 == e.getModifiers()){
					try {
						RobotEthernetCmdConfig dialog = RobotEthernetCmdConfig.getInstance();
						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dialog.setVisible(true);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		mntmRobotController.setIcon(new ImageIcon(DeviceDiagnostic.class.getResource("/com/cncmes/img/robots_16.png")));
		mnTroubleShooting.add(mntmRobotController);
		
		mntmCncController = new JMenuItem("CNC Web API");
		mntmCncController.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(16 == e.getModifiers()){
					try {
						CNCWebAPIConfig dialog = CNCWebAPIConfig.getInstance();
						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dialog.setVisible(true);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		mntmCncController.setIcon(new ImageIcon(DeviceDiagnostic.class.getResource("/com/cncmes/img/3d_printer_16.png")));
		mnTroubleShooting.add(mntmCncController);
		
		mntmCncEthernetCmd = new JMenuItem("CNC Ethernet Cmd");
		mntmCncEthernetCmd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16 == arg0.getModifiers()){
					try {
						CNCEthernetCmdCfg dialog = CNCEthernetCmdCfg.getInstance();
						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dialog.setVisible(true);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		mntmCncEthernetCmd.setIcon(new ImageIcon(DeviceDiagnostic.class.getResource("/com/cncmes/img/CNC_16.png")));
		mnTroubleShooting.add(mntmCncEthernetCmd);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmAboutCncDevice = new JMenuItem("About CNC Device Diagnostic Client");
		mntmAboutCncDevice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16 == arg0.getModifiers()){
					BarcodeInput.getInstance().showDialog();
				}
			}
		});
		mnHelp.add(mntmAboutCncDevice);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panelTop = new JPanel();
		panelTop.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		contentPane.add(panelTop, BorderLayout.NORTH);
		GridBagLayout gbl_panelTop = new GridBagLayout();
		gbl_panelTop.columnWidths = new int[] {380, 100, 160, 140};
		gbl_panelTop.rowHeights = new int[] {0};
		gbl_panelTop.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0};
		gbl_panelTop.rowWeights = new double[]{0.0};
		panelTop.setLayout(gbl_panelTop);
		
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		GridBagConstraints gbc_toolBar = new GridBagConstraints();
		gbc_toolBar.anchor = GridBagConstraints.WEST;
		gbc_toolBar.insets = new Insets(0, 0, 0, 5);
		gbc_toolBar.gridx = 0;
		gbc_toolBar.gridy = 0;
		panelTop.add(toolBar, gbc_toolBar);
		
		btnLogin = new JButton("");
		btnLogin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(btnLogin.isEnabled() && 1==e.getButton()){
					if(LoginSystem.userHasLoginned()){
						if(0==JOptionPane.showConfirmDialog(contentPane, "Are you sure of logging out now?", "Log Out?", JOptionPane.YES_NO_OPTION)){
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
		btnLogin.setToolTipText("Login System");
		btnLogin.setBorderPainted(false);
		btnLogin.setIcon(new ImageIcon(DeviceDiagnostic.class.getResource("/com/cncmes/img/login_24.png")));
		toolBar.add(btnLogin);
		
		btnRobot = new JButton("");
		btnRobot.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(btnRobot.isEnabled() && 1 == e.getButton()){
					try {
						RobotEthernetCmdConfig dialog = RobotEthernetCmdConfig.getInstance();
						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dialog.setVisible(true);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		btnSysConfig = new JButton("");
		btnSysConfig.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(btnSysConfig.isEnabled() && 1 == arg0.getButton()){
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
		btnSysConfig.setBorderPainted(false);
		btnSysConfig.setIcon(new ImageIcon(DeviceDiagnostic.class.getResource("/com/cncmes/img/setting_24.png")));
		btnSysConfig.setToolTipText("System setup");
		toolBar.add(btnSysConfig);
		btnRobot.setToolTipText("Robot Controller");
		btnRobot.setBorderPainted(false);
		btnRobot.setIcon(new ImageIcon(DeviceDiagnostic.class.getResource("/com/cncmes/img/robots_24.png")));
		toolBar.add(btnRobot);
		
		btnMachine = new JButton("");
		btnMachine.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(btnMachine.isEnabled() && 1 == e.getButton()){
					try {
						CNCWebAPIConfig dialog = CNCWebAPIConfig.getInstance();
						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dialog.setVisible(true);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		btnMachine.setToolTipText("CNC Controller");
		btnMachine.setBorderPainted(false);
		btnMachine.setIcon(new ImageIcon(DeviceDiagnostic.class.getResource("/com/cncmes/img/3d_printer_24.png")));
		toolBar.add(btnMachine);
		
		JButton btnRefreshtasklist = new JButton("");
		btnRefreshtasklist.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(1 == e.getButton() && btnRefreshtasklist.isEnabled()){
					btnRefreshtasklist.setEnabled(false);
					
					XmlUtils.parseSystemConfig();
					LinkedHashMap<String,Object> sysConfig = SystemConfig.getInstance().getCommonCfg();
					boolean runningLog = Integer.valueOf((String)sysConfig.get("RunningLog"))>0?true:false;
					boolean debugLog = Integer.valueOf((String)sysConfig.get("DebugLog"))>0?true:false;
					LogUtils.clearLog();
					LogUtils.setEnabledFlag(runningLog);
					LogUtils.setDebugLogFlag(debugLog);
					
					SchedulerClient sc = SchedulerClient.getInstance();
					setRunningMsg("Getting problematic task list, please wait...");
					if(sc.schedulerServerIsReady(SchedulerItems.PORTTASK)){
						if(!sc.getAlarmTask()){
							setRunningMsg("Error: fail to get the problematic task list! Please try again.");
						}
					}else{
						setRunningMsg("Error: the Scheduler can not be reached! Please try again.");
					}
					btnRefreshtasklist.setEnabled(true);
				}
			}
		});
		btnRefreshtasklist.setToolTipText("Refresh alarm task list");
		btnRefreshtasklist.setBorderPainted(false);
		btnRefreshtasklist.setIcon(new ImageIcon(DeviceDiagnostic.class.getResource("/com/cncmes/img/refresh_24.png")));
		toolBar.add(btnRefreshtasklist);
		
		JButton btnExit = new JButton("");
		toolBar.add(btnExit);
		btnExit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(1 == e.getButton()){
					int rtn = JOptionPane.showConfirmDialog(contentPane, "Are you sure of quiting from CNC Device Diagnostic Client", "Exit", JOptionPane.YES_NO_OPTION);
					if(0 == rtn){
						System.exit(0);
					}
				}
			}
		});
		btnExit.setToolTipText("Exit from system");
		btnExit.setBorderPainted(false);
		btnExit.setIcon(new ImageIcon(DeviceDiagnostic.class.getResource("/com/cncmes/img/Exit_24.png")));
		
		JLabel lblEngineerName = new JLabel("Engineer Name");
		GridBagConstraints gbc_lblEngineerName = new GridBagConstraints();
		gbc_lblEngineerName.insets = new Insets(0, 0, 0, 5);
		gbc_lblEngineerName.anchor = GridBagConstraints.EAST;
		gbc_lblEngineerName.gridx = 1;
		gbc_lblEngineerName.gridy = 0;
		panelTop.add(lblEngineerName, gbc_lblEngineerName);
		
		textFieldEngineerName = new JTextField();
		textFieldEngineerName.setEditable(false);
		GridBagConstraints gbc_textFieldEngineerName = new GridBagConstraints();
		gbc_textFieldEngineerName.insets = new Insets(0, 0, 0, 5);
		gbc_textFieldEngineerName.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldEngineerName.gridx = 2;
		gbc_textFieldEngineerName.gridy = 0;
		panelTop.add(textFieldEngineerName, gbc_textFieldEngineerName);
		textFieldEngineerName.setColumns(10);
		
		btnStartHandling = new JButton("Start Handling");
		btnStartHandling.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String runningMsg = "", btnText = "";
				if(1 == e.getButton() && btnStartHandling.isEnabled()){
					if(!"".equals(curLineName) && !"".equals(curTaskID)){
						String engineer = textFieldEngineerName.getText().trim();
						if("".equals(engineer)){
							setRunningMsg("Error: Engineer Name can not be null");
							return;
						}
						
						btnText = btnStartHandling.getText();
						SchedulerClient sc = SchedulerClient.getInstance();
						String taskInfo = sc.getTaskInfo(curLineName, curTaskID);
						if(null != taskInfo && !"".equals(taskInfo)){
							String[] data = taskInfo.split(",");
							if(data.length > 2){
								String sMD5 = data[data.length-1];
								String dt = taskInfo.substring(0, taskInfo.length()-sMD5.length()-1);
								if(sMD5.equals(MathUtils.MD5Encode(dt))){
									taskInfo = taskInfo.replace(data[0]+",", "").replace(","+sMD5, "");
									TaskData taskData = TaskData.getInstance();
									JSONObject jsonTask = JSONObject.fromObject(taskInfo);
									String state = jsonTask.getString("taskState");
									if("Start Handling".equals(btnText)){
										setRunningMsg("Checking current task now...");
										runningMsg = "Check current task failed, please try again.";
										if("HANDLING".equals(state)){
											taskData.setData(curTaskID, TaskItems.STATE, DeviceState.HANDLING);
											refreshListObject(listAlarmTask,taskData.taskInfo());
										}else{
											setRunningMsg("Posting your request, please wait...");
											String handlingIP = NetUtils.getLocalIP();
											jsonTask.put("taskState", ""+DeviceState.HANDLING);
											jsonTask.put("engineer", engineer);
											jsonTask.put("startHandlingTime", TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
											jsonTask.put("handlingIP", handlingIP);
											
											if(sc.updateTask(jsonTask.toString())){
												taskData.setData(curTaskID, TaskItems.ENGINEER, engineer);
												taskData.setData(curTaskID, TaskItems.STATE, DeviceState.HANDLING);
												taskData.setData(curTaskID, TaskItems.HANDLINGIP, handlingIP);
												refreshListObject(listAlarmTask,taskData.taskInfo());
												runningMsg = "You are now handling task "+curTaskID;
											}else{
												runningMsg = "Your request is failed to post, please try again.";
											}
										}
										setRunningMsg(runningMsg);
									}else{
										if(0 == JOptionPane.showConfirmDialog(frame, "Do you really want to discard the handling", "Discard Handling", JOptionPane.YES_NO_OPTION)){
											taskData.setTaskState(curTaskID, DeviceState.ALARMING, false);
											refreshListObject(listAlarmTask,taskData.taskInfo());
										}
									}
								}
							}else{
								setRunningMsg("Fail to get problematic tasks, please try again");
							}
						}
					}else{
						setRunningMsg("Please select the problematic task you are going to handle at the left side.");
					}
				}
			}
		});
		GridBagConstraints gbc_btnStartHandling = new GridBagConstraints();
		gbc_btnStartHandling.insets = new Insets(0, 0, 0, 5);
		gbc_btnStartHandling.gridx = 3;
		gbc_btnStartHandling.gridy = 0;
		panelTop.add(btnStartHandling, gbc_btnStartHandling);
		
		JPanel panelLeft = new JPanel();
		panelLeft.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		contentPane.add(panelLeft, BorderLayout.WEST);
		GridBagLayout gbl_panelLeft = new GridBagLayout();
		gbl_panelLeft.columnWidths = new int[] {220};
		gbl_panelLeft.rowHeights = new int[] {30, 0, 0};
		gbl_panelLeft.columnWeights = new double[]{1.0};
		gbl_panelLeft.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		panelLeft.setLayout(gbl_panelLeft);
		
		JLabel lblProblematicTaskList = new JLabel("Problematic Task List");
		GridBagConstraints gbc_lblProblematicTaskList = new GridBagConstraints();
		gbc_lblProblematicTaskList.insets = new Insets(0, 0, 5, 0);
		gbc_lblProblematicTaskList.gridx = 0;
		gbc_lblProblematicTaskList.gridy = 0;
		panelLeft.add(lblProblematicTaskList, gbc_lblProblematicTaskList);
		
		JScrollPane scrollPaneAlarmTask = new JScrollPane();
		GridBagConstraints gbc_scrollPaneAlarmTask = new GridBagConstraints();
		gbc_scrollPaneAlarmTask.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneAlarmTask.gridx = 0;
		gbc_scrollPaneAlarmTask.gridy = 1;
		panelLeft.add(scrollPaneAlarmTask, gbc_scrollPaneAlarmTask);
		
		listAlarmTask = new JList<String>();
		listAlarmTask.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String listText = listAlarmTask.getSelectedValue();
				if(null != listText && !"".equals(listText)){
					String[] task = listText.split(":");
					if(task.length > 1){
						TaskData taskData = TaskData.getInstance();
						curTaskID = task[1].trim();
						curLineName = (String) taskData.getData(curTaskID).get(TaskItems.LINENAME);
						refreshErrorDescription();
						if(!"".equals(curTaskID)){
							DeviceState taskState = taskData.getTaskState(curTaskID);
							
							if(DeviceState.HANDLING == taskState){
								String handlingIP = (String) taskData.getData(curTaskID).get(TaskItems.HANDLINGIP);
								String handlingName = (String) taskData.getData(curTaskID).get(TaskItems.ENGINEER);
								String name = textFieldEngineerName.getText().trim();
								String localIP = NetUtils.getLocalIP();
								
								if(localIP.equals(handlingIP) && name.equals(handlingName)){
									btnStartHandling.setEnabled(true);
									btnSubmitFaReport.setEnabled(true);
								}else{
									btnStartHandling.setEnabled(false);
									btnSubmitFaReport.setEnabled(false);
									setRunningMsg("Task "+curTaskID+" is handled by "+handlingName+" on "+handlingIP);
								}
								btnStartHandling.setText("Discard Handling");
							}else{
								btnStartHandling.setText("Start Handling");
								btnStartHandling.setEnabled(true);
								btnSubmitFaReport.setEnabled(false);
							}
						}
					}
				}
			}
		});
		refreshListObject(listAlarmTask, null);
		scrollPaneAlarmTask.setViewportView(listAlarmTask);
		
		JPanel panelCenter = new JPanel();
		panelCenter.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		contentPane.add(panelCenter, BorderLayout.CENTER);
		panelCenter.setLayout(new BorderLayout(0, 0));
		
		JPanel panelMainCenter = new JPanel();
		panelCenter.add(panelMainCenter, BorderLayout.CENTER);
		GridBagLayout gbl_panelMainCenter = new GridBagLayout();
		gbl_panelMainCenter.columnWidths = new int[] {0};
		gbl_panelMainCenter.rowHeights = new int[] {30, 0, 30, 0};
		gbl_panelMainCenter.columnWeights = new double[]{1.0};
		gbl_panelMainCenter.rowWeights = new double[]{0.0, 1.0, 0.0, 1.0};
		panelMainCenter.setLayout(gbl_panelMainCenter);
		
		JLabel lblErrorDescription = new JLabel("Failure Description");
		GridBagConstraints gbc_lblErrorDescription = new GridBagConstraints();
		gbc_lblErrorDescription.insets = new Insets(0, 0, 5, 0);
		gbc_lblErrorDescription.gridx = 0;
		gbc_lblErrorDescription.gridy = 0;
		panelMainCenter.add(lblErrorDescription, gbc_lblErrorDescription);
		
		JScrollPane scrollPaneErrorDescription = new JScrollPane();
		GridBagConstraints gbc_scrollPaneErrorDescription = new GridBagConstraints();
		gbc_scrollPaneErrorDescription.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPaneErrorDescription.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneErrorDescription.gridx = 0;
		gbc_scrollPaneErrorDescription.gridy = 1;
		panelMainCenter.add(scrollPaneErrorDescription, gbc_scrollPaneErrorDescription);
		
		textPaneErrorDescription = new JTextPane();
		textPaneErrorDescription.setEditable(false);
		scrollPaneErrorDescription.setViewportView(textPaneErrorDescription);
		
		JLabel lblFailureAnalysis = new JLabel("FA Report");
		GridBagConstraints gbc_lblFailureAnalysis = new GridBagConstraints();
		gbc_lblFailureAnalysis.insets = new Insets(0, 0, 5, 0);
		gbc_lblFailureAnalysis.gridx = 0;
		gbc_lblFailureAnalysis.gridy = 2;
		panelMainCenter.add(lblFailureAnalysis, gbc_lblFailureAnalysis);
		
		JScrollPane scrollPaneFailureAnalysis = new JScrollPane();
		GridBagConstraints gbc_scrollPaneFailureAnalysis = new GridBagConstraints();
		gbc_scrollPaneFailureAnalysis.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneFailureAnalysis.gridx = 0;
		gbc_scrollPaneFailureAnalysis.gridy = 3;
		panelMainCenter.add(scrollPaneFailureAnalysis, gbc_scrollPaneFailureAnalysis);
		
		textPaneFailureAnalysis = new JTextPane();
		scrollPaneFailureAnalysis.setViewportView(textPaneFailureAnalysis);
		
		JPanel panelMainBottom = new JPanel();
		panelCenter.add(panelMainBottom, BorderLayout.SOUTH);
		
		chckbxProblemFixed = new JCheckBox("Problem Fixed");
		panelMainBottom.add(chckbxProblemFixed);
		
		btnSubmitFaReport = new JButton("Submit FA Report");
		btnSubmitFaReport.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(1 == e.getButton() && btnSubmitFaReport.isEnabled()){
					if(!chckbxProblemFixed.isSelected()){
						JOptionPane.showMessageDialog(contentPane, "Please check \"Problem Fixed\" option before submitting the FA report", "Submit FA Report Error", JOptionPane.ERROR_MESSAGE);
					}else{
						String faDesc = textPaneFailureAnalysis.getText().trim();
						if("".equals(faDesc)){
							JOptionPane.showMessageDialog(contentPane, "FA Report can not be null", "Submit FA Report Error", JOptionPane.ERROR_MESSAGE);
						}else{
							if("".equals(curTaskID)) return;
							String machineIP = "",machinePort = "",machineModel = "",machineState = "",machineWPIDs = "",machineWPSlotIDs = "",machineWPStates = "";
							String robotIP = "",robotPort = "",robotModel = "",robotState = "",robotWPIDs = "",robotWPSlotIDs = "",robotWPStates = "";
							String lineName = "";
							CNC cncCtrl = null;
							
							CncDriver cncDriver = CncDriver.getInstance();
							CncData cncData = CncData.getInstance();
							RobotData robotData = RobotData.getInstance();
							TaskData taskData = TaskData.getInstance();
							LinkedHashMap<TaskItems, Object> taskInfo = taskData.getData(curTaskID);
							machineIP = ""+taskInfo.get(TaskItems.MACHINEIP);
							machinePort = ""+cncData.getData(machineIP).get(CncItems.PORT);
							machineModel = cncData.getCncModel(machineIP);
							machineState = ""+taskInfo.get(TaskItems.MACHINESTATE);
							machineWPIDs = ""+taskInfo.get(TaskItems.MACHINEWPIDS);
							machineWPSlotIDs = ""+taskInfo.get(TaskItems.MACHINEWPSLOTIDS);
							machineWPStates = ""+taskInfo.get(TaskItems.MACHINEWPSTATES);
							robotIP = ""+taskInfo.get(TaskItems.ROBOTIP);
							robotPort = ""+robotData.getData(robotIP).get(RobotItems.PORT);
							robotModel = ""+robotData.getData(robotIP).get(RobotItems.MODEL);
							robotState = ""+taskInfo.get(TaskItems.ROBOTSTATE);
							robotWPIDs = ""+taskInfo.get(TaskItems.ROBOTWPIDS);
							robotWPSlotIDs = ""+taskInfo.get(TaskItems.ROBOTWPSLOTIDS);
							robotWPStates = ""+taskInfo.get(TaskItems.ROBOTWPSTATES);
							lineName = ""+taskInfo.get(TaskItems.LINENAME);
							
							if(!"".equals(machineWPIDs)){
								if("FINISH".equals(machineState)){
									if(1==JOptionPane.showConfirmDialog(contentPane, "DeviceDiagnostic client detects below workpieces in the machine\r\n"+getCrlnStr(machineWPIDs,machineWPSlotIDs,"Zone")
											+"\r\nHave you finished unloading them from machine manually?", "Workpieces Unloading From Machine", JOptionPane.YES_NO_OPTION)){
										JOptionPane.showMessageDialog(contentPane, "You have to unload all workpieces from machine manually before submitting the FA report!", "Submit FA Report Failed", JOptionPane.ERROR_MESSAGE);
										return;
									}
									if(!"".equals(robotWPIDs)){
										if(1==JOptionPane.showConfirmDialog(contentPane, "DeviceDiagnostic client detects below workpieces in robot's tray\r\n"+getCrlnStr(robotWPIDs,robotWPSlotIDs,"Slot")
												+"\r\nHave you finished unloading them from robot's tray manually?", "Workpieces Unloading From Robot", JOptionPane.YES_NO_OPTION)){
											JOptionPane.showMessageDialog(contentPane, "You have to unload all workpieces from robot manually before submitting the FA report!", "Submit FA Report Failed", JOptionPane.ERROR_MESSAGE);
											return;
										}
									}
								}else{
									if(!"".equals(robotWPIDs)){
										if(1==JOptionPane.showConfirmDialog(contentPane, "DeviceDiagnostic client detects below workpieces in robot's tray\r\n"+getCrlnStr(robotWPIDs,robotWPSlotIDs,"Slot")
												+"\r\nHave you finished loading them onto the machine manually?", "Load Workpiece from Robot to Machine", JOptionPane.YES_NO_OPTION)){
											JOptionPane.showMessageDialog(contentPane, "You have to load all workpieces onto the machine manually!", "Submit FA Report Failed", JOptionPane.ERROR_MESSAGE);
											return;
										}
									}
								}
							}else if(!"".equals(robotWPIDs)){
								if("FINISH".equals(machineState)){
									if(1==JOptionPane.showConfirmDialog(contentPane, "DeviceDiagnostic client detects below workpieces in robot's tray\r\n"+getCrlnStr(robotWPIDs,robotWPSlotIDs,"Slot")
											+"\r\nHave you finished unloading them from robot manually?", "Workpieces Unloading", JOptionPane.YES_NO_OPTION)){
										JOptionPane.showMessageDialog(contentPane, "You have to unload all workpieces from robot manually before submitting the FA report!", "Submit FA Report Failed", JOptionPane.ERROR_MESSAGE);
										return;
									}
								}else{
									if(1==JOptionPane.showConfirmDialog(contentPane, "DeviceDiagnostic client detects below workpieces in robot's tray\r\n"+getCrlnStr(robotWPIDs,robotWPSlotIDs,"Slot")
											+"\r\nHave you finished loading them onto the machine manually?", "Load Workpiece from Robot to Machine", JOptionPane.YES_NO_OPTION)){
										JOptionPane.showMessageDialog(contentPane, "You have to load all workpieces onto the machine manually!", "Submit FA Report Failed", JOptionPane.ERROR_MESSAGE);
										return;
									}
								}
							}
							
							//2D bar code scanning/clamping/program uploading/start machining routine
							if(!"FINISH".equals(machineState) && (!"".equals(machineWPIDs) || !"".equals(robotWPIDs))){
								WorkpieceData wpData = WorkpieceData.getInstance();
								UploadNCProgram uploadNCProgram = UploadNCProgram.getInstance();
								
								//1. Manually 2D bar code scanning
								setRunningMsg("Scan 2D barcode");
								barcodeInput.showDialog();
								
								String workpieceIDs = barcodeInput.getWorkpieceIDs();
								String workzones = barcodeInput.getWorkZones();
								String invalidIDs = "";
								String[] wpIDs = null;
								String[] zones = null;
								if("".equals(workzones) || "".equals(workpieceIDs)){
									JOptionPane.showMessageDialog(contentPane, "You have to manually scan the barcode first before submitting the FA report!", "Scan Barcode Error", JOptionPane.ERROR_MESSAGE);
									return;
								}else{
									//Check the workpieceIDs and machining spec
									setRunningMsg("Check the workpiece IDs");
									wpIDs = workpieceIDs.split(";");
									zones = workzones.split(";");
									cncData.clearWorkpieceID(machineIP);
									for(int i=0; i<wpIDs.length; i++){
										wpData.setData(wpIDs[i], WorkpieceItems.ID, wpIDs[i]);
										DataUtils.updateWorkpieceData(wpIDs[i], lineName, machineIP, zones[i]);
										if(!wpData.canMachineByCNC(wpIDs[i], machineModel, null)){
											if("".equals(invalidIDs)){
												invalidIDs = "Zone " + zones[i] + ": " + wpIDs[i];
											}else{
												invalidIDs += "\r\n" + "Zone " + zones[i] + ": " + wpIDs[i];
											}
										}
										cncData.setWorkpieceID(machineIP, Integer.valueOf(zones[i]), wpIDs[i]);
									}
									
									if(!"".equals(invalidIDs)){
										JOptionPane.showMessageDialog(contentPane, "Below workpieces can't be fabricated by "+machineModel+",please unload them and try again.\r\n"+invalidIDs, "Workpieces Input Error", JOptionPane.ERROR_MESSAGE);
										return;
									}
									
									//Check device driver
									setRunningMsg("Check device driver");
									String cncDrvName = (String)cncDriver.getData(machineModel).get(DriverItems.DRIVER);
									String cncDataHandler = (String)cncDriver.getData(machineModel).get(DriverItems.DATAHANDLER);
									cncCtrl = CncFactory.getInstance(cncDrvName,cncDataHandler,machineModel);
									if(null == cncCtrl){
										JOptionPane.showMessageDialog(contentPane, "Driver of "+machineModel+" is not found!", "Machine Driver Error", JOptionPane.ERROR_MESSAGE);
										return;
									}else{
										cncData.setData(machineIP, CncItems.CTRL, cncCtrl);
									}
									
									//Upload sub programs
									setRunningMsg("Upload NC sub programs");
									if(!UploadNCProgram.uploadSubPrograms(cncCtrl, curTaskID, machineIP)){
										JOptionPane.showMessageDialog(contentPane, "Upload NC Sub programs failed,please try again.", "Upload NC Sub Programs Error", JOptionPane.ERROR_MESSAGE);
										return;
									}
								}
								
								//2. Workpieces clamping
								setRunningMsg("Workpices clamping");
								for(int i=0; i<zones.length; i++){
									if(!cncCtrl.clampFixture(machineIP, Integer.valueOf(zones[i]))){
										if("".equals(invalidIDs)){
											invalidIDs = "Zone " + zones[i] + ": " + wpIDs[i];
										}else{
											invalidIDs += "\r\n" + "Zone " + zones[i] + ": " + wpIDs[i];
										}
									}
								}
								if(!"".equals(invalidIDs)){
									JOptionPane.showMessageDialog(contentPane, "Below workpieces fail to clamp,please check!\r\n"+invalidIDs, "Workpieces Clamping Error", JOptionPane.ERROR_MESSAGE);
									return;
								}
								
								//3. Main program uploading
								setRunningMsg("Upload NC main program");
								if(!UploadNCProgram.uploadMainProgram(cncCtrl, curTaskID, machineIP)){
									JOptionPane.showMessageDialog(contentPane, "Upload NC Main program failed,please try again.", "Upload NC Main Program Error", JOptionPane.ERROR_MESSAGE);
									return;
								}
								if(!taskData.subProgramOK(curTaskID) || !taskData.mainProgramOK(curTaskID)){
									if(!taskData.subProgramOK(curTaskID)) JOptionPane.showMessageDialog(contentPane, "Upload NC sub programs failed,please try again.", "Upload NC Sub Programs Error", JOptionPane.ERROR_MESSAGE);
									if(!taskData.mainProgramOK(curTaskID)) JOptionPane.showMessageDialog(contentPane, "Upload NC main programs failed,please try again.", "Upload NC Main Program Error", JOptionPane.ERROR_MESSAGE);
									return;
								}
								
								//4. Start machining
								setRunningMsg("Start the machining");
								CncWebAPI cncWebAPI = CncWebAPI.getInstance();
								LinkedHashMap<CncWebAPIItems, String> cncWebAPICommonCfg = cncWebAPI.getCommonCfg(machineModel);
								String mainProgram = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGMAIN);
								if(!cncCtrl.startMachining(machineIP, mainProgram)){
									JOptionPane.showMessageDialog(contentPane, "Fail to start the machining,please try again", "Start Machining Error", JOptionPane.ERROR_MESSAGE);
									return;
								}
							}
							
							//Update Alarming Task
							setRunningMsg("Submit FA Report");
							SchedulerClient sc = SchedulerClient.getInstance();
							taskData.setData(curTaskID, TaskItems.STATE, DeviceState.FIXED);
							if(sc.updateTask(taskData.getTaskInfoJsonStr(curTaskID))){
								taskData.removeData(curTaskID);
								sc.getAlarmTask();//Refresh problematic task list
								textPaneFailureAnalysis.setText("");
								chckbxProblemFixed.setSelected(false);
								setRunningMsg("Submit FA Report OK");
							}else{
								setRunningMsg("Submit FA Report Failed");
							}
						}
					}
				}
			}
		});
		panelMainBottom.add(btnSubmitFaReport);
		
		JPanel panelBottom = new JPanel();
		panelBottom.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		contentPane.add(panelBottom, BorderLayout.SOUTH);
		
		lblRunningMessage = new JLabel("Select the problematic task you are going to handle at the left side");
		panelBottom.add(lblRunningMessage);
		refreshButtonsEnabled();
	}
	
	class ScanBarcode implements Runnable{
		@Override
		public void run() {
			barcodeInput.showDialog();
		}
	}
}
