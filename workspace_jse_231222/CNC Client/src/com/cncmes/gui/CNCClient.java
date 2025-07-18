package com.cncmes.gui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.awt.Toolkit;
import java.awt.TrayIcon;

import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.alibaba.fastjson.JSONObject;
import com.cncmes.base.DeviceState;
import com.cncmes.base.MemoryItems;
import com.cncmes.base.PermissionItems;
import com.cncmes.base.RobotItems;
import com.cncmes.ctrl.ConnectDevices;
import com.cncmes.ctrl.DeviceServer;
import com.cncmes.ctrl.ScannerReading;
import com.cncmes.ctrl.SchedulerClient;
import com.cncmes.ctrl.SocketServer;
import com.cncmes.dao.DAO;
import com.cncmes.dao.impl.DAOImpl;
import com.cncmes.data.CncData;
import com.cncmes.data.RobotData;
import com.cncmes.data.SystemConfig;
import com.cncmes.data.WorkpieceData;
import com.cncmes.dto.*;
import com.cncmes.handler.impl.CncClientDataHandler;
import com.cncmes.thread.TaskMonitor;
import com.cncmes.thread.ThreadController;
import com.cncmes.utils.*;
import org.dom4j.DocumentException;

import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;

import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.UIManager;
import javax.swing.JList;
import javax.swing.DefaultComboBoxModel;
import java.awt.GridLayout;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.JCheckBox;
import java.awt.Font;

/**
 * GUI of Devices Control Center 
 * @author LI ZI LONG
 *
 */
public class CNCClient extends JFrame {
	private JPanel contentPane;
	private static CNCClient cncClient = new CNCClient();
	private static final long serialVersionUID = 1L;
	private JPanel panelCNCLine;
	private JList<String> list_onlineDev;
	private JList<String> list_taskQueue;
	private JList<String> list_alarmRobots;
	private JList<String> list_alarmCncs;
	private JMenuItem mntmCircleRunAutoLine;
	private JMenuItem mntmStartAutoLine;
	private JMenuItem mntmStopAutoLine;
	private JMenuItem mntmSystemConfig;
	private JMenuItem mntmCncWebApi;
	private JMenuItem mntmCncEthernetCommand;
	private JMenuItem mntmRobotNetworkCmd;
	private JMenuItem mntmUploadProgram;
	private JMenuItem mntmCncController;
	private JMenuItem mntmRobotController;
	private JMenuItem mntmScannerController;
	private JMenuItem mntmStartDeviceSvr;
	private JButton btnStartAutoLine;
	private JButton btnStopAutoLine;
	private JButton btnConnect;
	private JButton btnConfig;
	private JButton btnLogin;
	private JScrollPane jspTaskQueue;
	private JScrollPane jspAlarmRobots;
	private JScrollPane jspAlarmCncs;
	private JPanel panelOnlineDev;
	private JPanel panel_status;
	private SystemTray systemTray = null;
	private TrayIcon trayIcon = null;
	private JLabel lblRobotInfo = null;
	private JLabel lblWorkpieceInfo;
	private JLabel lblMessage = null;
	private JLabel lblHeartbeat;
	private JComboBox<String> comboBox_lineName;
	
	private static final int PORTSVRSOCKET = 20520;
	private static SocketServer socketServer = SocketServer.getInstance();
	private static String[] cncLines = null;
	private static String curLineName = "";
	private JCheckBox chckbxAutoInputMaterials;
	private JCheckBox chckbxCycleWorking;
	private JCheckBox chckbxfiveAspectProcesss;
	private JMenuItem mntmOneCycleController;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		checkPreviousInstance();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CNCClient frame = CNCClient.getInstance();
					frame.setVisible(true);
					ThreadController.Stop();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void save(OrderScheduler orderScheduler){
		orderScheduler.setQty(orderScheduler.getQty()-orderScheduler.getEach_material_productnum());
		if(orderScheduler.getQty()<=0)
		{
			orderScheduler.setHave_finish(1);
		}
		DAOImpl dao = new DAOImpl("com.cncmes.dto.OrderScheduler");
		try {
			dao.update(orderScheduler);     //update database
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
	}

	/**
	 * Create the frame.
	 */
	private CNCClient() {
//		DAO dao = new DAOImpl("com.cncmes.dto.TaskInfo");
//		TaskInfo taskInfo = new TaskInfo();
////		taskInfo = new TaskInfo("DDDDDD2BCB2B449D5CC91DB494B29CB976",new JSONObject().toJSONString(),"10.10.206.74",0,140);
//		List<Object> os = null;
//		try {
//			os = dao.findByCnd(new String[]{"cnc_ip", "have_done"}, new String[]{"10.10.206.72", "0"});
//			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
////			TaskInfo info;
//			for(Object o : os){
//				taskInfo = (TaskInfo)o;
//				System.out.println(simpleDateFormat.format(taskInfo.getCreate_time()));
//				System.out.println(taskInfo);
//
//			}
//
//
//			Date beginDate = (taskInfo.getCreate_time());
//			Date currentDate = new Date();
//			BigDecimal remainderTime = BigDecimal.valueOf(taskInfo.getSimulate_time()).subtract(BigDecimal.valueOf(TimeUtils.getTwoDatePoor(beginDate,currentDate)));
//			Boolean prepareNextTaskFlag =remainderTime.compareTo(BigDecimal.valueOf(10))==-1?true:false;
//			System.out.println(prepareNextTaskFlag);
//////			dao.add(taskInfo);
//		} catch (SQLException throwables) {
//			throwables.printStackTrace();
//		}
//		TimeZone time = TimeZone.getTimeZone("Etc/GMT-8");  //转换为中国时区
//		TimeZone.setDefault(time);
//		com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
//		TaskInfo taskInfo = new TaskInfo("LKXXXXXX",jsonObject.toJSONString(),"10.10.206.72",0,20.0,new Date());
//		DAO dao = new DAOImpl("com.cncmes.dto.TaskInfo");
//		try {
//			dao.add(taskInfo);
//		} catch (SQLException throwables) {
//			throwables.printStackTrace();
//		}



		String startupMsg = "";
		ThreadController.initStopFlag();
		startupMsg = MySystemUtils.sysDatabaseOK();
		if("OK".equals(startupMsg)){
			cncLines = DataUtils.getCNCLines();
			curLineName = cncLines[0];
			MySystemUtils.sysLoadSettingsFromDB(curLineName);
		}else{
			cncLines = new String[]{""};
			startupMsg = "Connect system database failed!";
		}
		
		setIconImage(Toolkit.getDefaultToolkit().getImage(CNCClient.class.getResource("/com/cncmes/img/Butterfly_24.png")));
		setTitle("CNC Client");
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				int rtn = JOptionPane.showConfirmDialog(cncClient.getContentPane(), "Run the CNC Client in background", "Minimize", JOptionPane.YES_NO_OPTION);
				if(0 == rtn){
					cncClient.dispose();
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
		mntmExit.setIcon(new ImageIcon(CNCClient.class.getResource("/com/cncmes/img/Exit_16.png")));
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(16 == e.getModifiers()){
					exitSystem(false);
				}
			}
		});
		
		JMenuItem mntmCopyFile = new JMenuItem("Copy File");
		mntmCopyFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(16 == e.getModifiers()){
					JFileChooser jfc = new JFileChooser();
					jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
					jfc.showDialog(cncClient.getContentPane(), "Copy");
					
					File file = jfc.getSelectedFile();
					if(file != null){
						String rootPath = System.getProperty("user.dir") + File.separator + file.getName();
						String sourcePath = file.getAbsolutePath();
						
						if(file.isDirectory()){
							try {
								MyFileUtils.copyFolder(sourcePath, rootPath);
								JOptionPane.showMessageDialog(cncClient.getContentPane(), sourcePath+"->"+rootPath, "Copy File Done", JOptionPane.INFORMATION_MESSAGE);
							} catch (FileNotFoundException e1) {
								JOptionPane.showMessageDialog(cncClient.getContentPane(), e1.getMessage(), "Copy File Error", JOptionPane.ERROR_MESSAGE);
							} catch (IOException e1) {
								JOptionPane.showMessageDialog(cncClient.getContentPane(), e1.getMessage(), "Copy File Error", JOptionPane.ERROR_MESSAGE);
							}
						}else if(file.isFile()){
							try {
								MyFileUtils.copyFile(sourcePath, rootPath);
								JOptionPane.showMessageDialog(cncClient.getContentPane(), sourcePath+"->"+rootPath, "Copy File Done", JOptionPane.INFORMATION_MESSAGE);
							} catch (FileNotFoundException e1) {
								JOptionPane.showMessageDialog(cncClient.getContentPane(), e1.getMessage(), "Copy File Error", JOptionPane.ERROR_MESSAGE);
							} catch (IOException e1) {
								JOptionPane.showMessageDialog(cncClient.getContentPane(), e1.getMessage(), "Copy File Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			}
		});
		mnFile.add(mntmCopyFile);
		mnFile.add(mntmExit);
		
		JMenu mnDebug = new JMenu("Debug");
		menuBar.add(mnDebug);
		
		JMenuItem mntmCppDllCall = new JMenuItem("DLL Calling");
		mntmCppDllCall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16 == arg0.getModifiers()){
					CppDllCall dialog = CppDllCall.getInstance();
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
				}
			}
		});
		
		JMenuItem mntmSockedDebug = new JMenuItem("Socked Test");
		mntmSockedDebug.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(16 == e.getModifiers()){
					SocketDebug dialog = SocketDebug.getInstance();
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
				}
			}
		});
		mnDebug.add(mntmSockedDebug);
		
		JMenuItem mntmComPort = new JMenuItem("COM Port Test");
		mntmComPort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(16 == e.getModifiers()){
					ComPortDebug dialog = ComPortDebug.getInstance();
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
				}
			}
		});
		mnDebug.add(mntmComPort);
		mnDebug.add(mntmCppDllCall);
		
		JMenuItem mntmBarcodePrinter = new JMenuItem("Barcode Printer");
		mntmBarcodePrinter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(16 == e.getModifiers()){
					BarcodePrinter dialog = BarcodePrinter.getInstance();
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
				}
			}
		});
		
		mntmUploadProgram = new JMenuItem("Upload Program");
		mntmUploadProgram.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16 == arg0.getModifiers()){
					try {
						UploadProgram dialog = UploadProgram.getInstance();
						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dialog.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		mnDebug.add(mntmUploadProgram);
		mnDebug.add(mntmBarcodePrinter);
		
		mntmStartDeviceSvr = new JMenuItem("Start Device Svr");
		mntmStartDeviceSvr.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(16 == e.getModifiers()){
					String mnText = mntmStartDeviceSvr.getText();
					if("Start Device Svr".equals(mnText)){
						LogUtils.setEnabledFlag(true);
						ThreadController.initStopFlag();
						DeviceServer.launch();
						mntmStartDeviceSvr.setText("Stop Device Svr");
					}else{
						DeviceServer.stop();
						mntmStartDeviceSvr.setText("Start Device Svr");
					}
				}
			}
		});
		
		JMenuItem mntmBarcodeReading = new JMenuItem("Barcode Reading");
		mntmBarcodeReading.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(16 == e.getModifiers()){
					ScannerReading bcValidate = new ScannerReading();
					BarcodeInput.getInstance().showDialog(bcValidate);
					System.out.println("RawBarcode="+bcValidate.getRawBarcode());
					System.out.println("RealBarcode="+bcValidate.getRealBarcode());
				}
			}
		});
		mnDebug.add(mntmBarcodeReading);
		mnDebug.add(mntmStartDeviceSvr);
		
		chckbxAutoInputMaterials = new JCheckBox("Auto Input Materials");
		chckbxAutoInputMaterials.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(1==arg0.getButton()){
					DebugUtils.setAutoInputMaterial(chckbxAutoInputMaterials.isSelected());
				}
			}
		});
		chckbxAutoInputMaterials.setFont(new Font("Microsoft YAHEI", Font.PLAIN, 12));
		mnDebug.add(chckbxAutoInputMaterials);

		chckbxCycleWorking = new JCheckBox("Cycle Working");
		chckbxCycleWorking.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(1==arg0.getButton()){
					DebugUtils.setCycleWorking(chckbxCycleWorking.isSelected());
				}
			}
		});

		chckbxCycleWorking.setFont(new Font("Microsoft YAHEI", Font.PLAIN, 12));
		mnDebug.add(chckbxCycleWorking);

		chckbxfiveAspectProcesss = new JCheckBox("Five Aspect Process");
		chckbxfiveAspectProcesss.setSelected(false);
		chckbxfiveAspectProcesss.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				DebugUtils.setFiveAspectProcess(chckbxfiveAspectProcesss.isSelected());

			}
		});
		chckbxfiveAspectProcesss.setFont(new Font("Microsoft YAHEI", Font.PLAIN, 12));
		mnDebug.add(chckbxfiveAspectProcesss);


		JMenu mnSetup = new JMenu("Setup");
		menuBar.add(mnSetup);
		
		mntmCncWebApi = new JMenuItem("CNC Web-Service Command");
		mntmCncWebApi.setIcon(new ImageIcon(CNCClient.class.getResource("/com/cncmes/img/3d_printer_16.png")));
		mntmCncWebApi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16 == arg0.getModifiers()){
					try {
						CNCWebAPIConfig dialog = CNCWebAPIConfig.getInstance();
						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dialog.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		mntmSystemConfig = new JMenuItem("System Config");
		mntmSystemConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(16 == e.getModifiers()){
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
		mntmSystemConfig.setIcon(new ImageIcon(CNCClient.class.getResource("/com/cncmes/img/setting_16.png")));
		mnSetup.add(mntmSystemConfig);
		mnSetup.add(mntmCncWebApi);
		
		mntmRobotNetworkCmd = new JMenuItem("Robot Ethernet Command");
		mntmRobotNetworkCmd.addActionListener(new ActionListener() {
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
		
		mntmCncEthernetCommand = new JMenuItem("CNC Ethernet Command");
		mntmCncEthernetCommand.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(16 == e.getModifiers()){
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
		mntmCncEthernetCommand.setIcon(new ImageIcon(CNCClient.class.getResource("/com/cncmes/img/CNC_16.png")));
		mnSetup.add(mntmCncEthernetCommand);
		mntmRobotNetworkCmd.setIcon(new ImageIcon(CNCClient.class.getResource("/com/cncmes/img/robots_16.png")));
		mnSetup.add(mntmRobotNetworkCmd);
		
		JMenu mnController = new JMenu("Controller");
		menuBar.add(mnController);
		
		mntmCncController = new JMenuItem("CNC Controller");
		mntmCncController.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16 == arg0.getModifiers()){
					try {
						CNCController dialog = CNCController.getInstance();
						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dialog.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		mnController.add(mntmCncController);
		
		mntmRobotController = new JMenuItem("Robot Controller");
		mntmRobotController.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16 == arg0.getModifiers()){
					try {
						RobotController dialog = RobotController.getInstance();
						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dialog.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		mnController.add(mntmRobotController);
		
		mntmScannerController = new JMenuItem("Scanner Controller");
		mntmScannerController.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16 == arg0.getModifiers()){
					try {
						ScannerController dialog = ScannerController.getInstance();
						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dialog.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		mnController.add(mntmScannerController);
		
		mntmOneCycleController = new JMenuItem("\u63A7\u5236\u4E2D\u5FC3");
		mntmOneCycleController.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16 == arg0.getModifiers()){
					try {
						OneCycleController dialog = OneCycleController.getInstance();
						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dialog.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		mnController.add(mntmOneCycleController);
		
		JMenu mnRun = new JMenu("Run");
		menuBar.add(mnRun);
		
		mntmStartAutoLine = new JMenuItem("Start Auto Line");
		mntmStartAutoLine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(16 == e.getModifiers() && mntmStartAutoLine.isEnabled()){
					curLineName = comboBox_lineName.getSelectedItem().toString();
					startAutoLine();
				}
			}
		});
		mntmStartAutoLine.setEnabled(false);
		mntmStartAutoLine.setIcon(new ImageIcon(CNCClient.class.getResource("/com/cncmes/img/start_16.png")));
		mnRun.add(mntmStartAutoLine);
		
		mntmStopAutoLine = new JMenuItem("Stop Auto Line");
		mntmStopAutoLine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(16 == e.getModifiers() && mntmStopAutoLine.isEnabled()){
					stopAutoLine();
				}
			}
		});
		mntmStopAutoLine.setEnabled(false);
		mntmStopAutoLine.setIcon(new ImageIcon(CNCClient.class.getResource("/com/cncmes/img/stop_16.png")));
		mnRun.add(mntmStopAutoLine);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmRootPath = new JMenuItem("Root Path");
		mntmRootPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(16 == e.getModifiers()){
					JOptionPane.showMessageDialog(cncClient.getContentPane(), "Root Path="+System.getProperty("user.dir"), "Root Path", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		mnHelp.add(mntmRootPath);
		
		JMenuItem mntmAboutCncClient = new JMenuItem("About CNC Client");
		mntmAboutCncClient.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16 == arg0.getModifiers()){
					String msg = "System recalls that the Robot is now charging";
					msg += "\r\nDirectly execute command Ping 127.0.0.1 could cause damage to the Robot";
					MyConfirmDialog.showDialog("Risky To Execute Command", msg);
					if(MyConfirmDialog.OPTION_YES == MyConfirmDialog.getConfirmFlag()){
						System.out.println(NetUtils.pingHost("127.0.0.1"));
					}else{
						String str = "FW,001,";
						System.out.println(str+MathUtils.calculateCmdConfirmCode(str));
					}
				}
			}
		});
		mnHelp.add(mntmAboutCncClient);
		
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

		JMenuItem mntmConfigCnc = new JMenuItem("CNC Config");
		mntmConfigCnc.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(16 == e.getModifiers())
				{
					CNCConfigDialog dialog = CNCConfigDialog.getInstance();
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
				}
			}
		});
		mnHelp.add(mntmConfigCnc);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel mainPanel = new JPanel();
		contentPane.add(mainPanel, BorderLayout.CENTER);
		mainPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel panelLineName = new JPanel();
		mainPanel.add(panelLineName, BorderLayout.NORTH);
		GridBagLayout gbl_panelLineName = new GridBagLayout();
		gbl_panelLineName.columnWidths = new int[] {208, 70, 210, 96, 400};
		gbl_panelLineName.rowHeights = new int[] {23, 5};
		gbl_panelLineName.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0};
		gbl_panelLineName.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panelLineName.setLayout(gbl_panelLineName);
		
		JToolBar toolBar_commonOp = new JToolBar();
		toolBar_commonOp.setFloatable(false);
		GridBagConstraints gbc_toolBar_commonOp = new GridBagConstraints();
		gbc_toolBar_commonOp.anchor = GridBagConstraints.WEST;
		gbc_toolBar_commonOp.gridx = 0;
		gbc_toolBar_commonOp.gridy = 0;
		panelLineName.add(toolBar_commonOp, gbc_toolBar_commonOp);
		
		JLabel lblLineName = new JLabel("Line Name");
		lblLineName.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblLineName = new GridBagConstraints();
		gbc_lblLineName.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblLineName.insets = new Insets(0, 0, 0, 5);
		gbc_lblLineName.gridx = 1;
		gbc_lblLineName.gridy = 0;
		panelLineName.add(lblLineName, gbc_lblLineName);
		
		comboBox_lineName = new JComboBox<String>();
		comboBox_lineName.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				curLineName = comboBox_lineName.getSelectedItem().toString();
			}
		});
		comboBox_lineName.setModel(new DefaultComboBoxModel<String>(cncLines));
		GridBagConstraints gbc_comboBox_lineName = new GridBagConstraints();
		gbc_comboBox_lineName.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_lineName.insets = new Insets(0, 0, 0, 5);
		gbc_comboBox_lineName.gridx = 2;
		gbc_comboBox_lineName.gridy = 0;
		panelLineName.add(comboBox_lineName, gbc_comboBox_lineName);
		
		btnConnect = new JButton("Connect");
		btnConnect.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(1 == e.getButton() && btnConnect.isEnabled()){
					curLineName = comboBox_lineName.getSelectedItem().toString();
					String[] ips = DataUtils.getCNCsByLineName(curLineName);
					DataUtils.getDeviceInfo(curLineName);
					PaintCNCLines.getInstance().paintCNCLines(panelCNCLine, ips);
					if(null != ips){
						enableAutoLineButton(true);
					}else{
						disableAutoLineButton();
					}
				}
			}
		});
		GridBagConstraints gbc_btnConnect = new GridBagConstraints();
		gbc_btnConnect.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnConnect.insets = new Insets(0, 0, 0, 5);
		gbc_btnConnect.gridx = 3;
		gbc_btnConnect.gridy = 0;
		panelLineName.add(btnConnect, gbc_btnConnect);
		
		btnLogin = new JButton("");
		btnLogin.setBackground(UIManager.getColor("Button.background"));
		toolBar_commonOp.add(btnLogin);
		btnLogin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(1 == arg0.getButton() && btnLogin.isEnabled()){
					if(LoginSystem.userHasLoginned()){
						if(0==JOptionPane.showConfirmDialog(cncClient, "Are you sure of logging out now?", "Log Out?", JOptionPane.YES_NO_OPTION)){
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
		btnLogin.setIcon(new ImageIcon(CNCClient.class.getResource("/com/cncmes/img/login_24.png")));
		btnLogin.setToolTipText("Login system");
		
		btnStartAutoLine = new JButton("");
		btnStartAutoLine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnStartAutoLine.setEnabled(false);
		btnStartAutoLine.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(1 == e.getButton() && btnStartAutoLine.isEnabled()){
					curLineName = comboBox_lineName.getSelectedItem().toString();
					startAutoLine();
				}
			}
		});
		
		btnConfig = new JButton("");
		btnConfig.setToolTipText("System setup");
		btnConfig.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(btnConfig.isEnabled() && 1 == e.getButton()){
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
		btnConfig.setIcon(new ImageIcon(CNCClient.class.getResource("/com/cncmes/img/setting_24.png")));
		toolBar_commonOp.add(btnConfig);
		btnStartAutoLine.setBorderPainted(false);
		btnStartAutoLine.setToolTipText("Start Auto Line");
		btnStartAutoLine.setIcon(new ImageIcon(CNCClient.class.getResource("/com/cncmes/img/start_24.png")));
		toolBar_commonOp.add(btnStartAutoLine);
		
		btnStopAutoLine = new JButton("");
		btnStopAutoLine.setEnabled(false);
		btnStopAutoLine.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(1 == e.getButton() && btnStopAutoLine.isEnabled()){
					stopAutoLine();
				}
			}
		});
		btnStopAutoLine.setBorderPainted(false);
		btnStopAutoLine.setToolTipText("Stop Auto Line");
		btnStopAutoLine.setIcon(new ImageIcon(CNCClient.class.getResource("/com/cncmes/img/stop_24.png")));
		toolBar_commonOp.add(btnStopAutoLine);
		
		JButton btnExit = new JButton("");
		btnExit.setToolTipText("Exit from system");
		btnExit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(1 == e.getButton()){
					exitSystem(false);
				}
			}
		});
		btnExit.setBorderPainted(false);
		btnExit.setIcon(new ImageIcon(CNCClient.class.getResource("/com/cncmes/img/Exit_24.png")));
		toolBar_commonOp.add(btnExit);
		
		panelOnlineDev = new JPanel();
		panelOnlineDev.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		mainPanel.add(panelOnlineDev, BorderLayout.WEST);
		GridBagLayout gbl_panelOnlineDev = new GridBagLayout();
		gbl_panelOnlineDev.columnWidths = new int[] {162};
		gbl_panelOnlineDev.rowHeights = new int[] {25, 100, 25, 140, 25, 100, 25, 100};
		gbl_panelOnlineDev.columnWeights = new double[]{1.0};
		gbl_panelOnlineDev.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0};
		panelOnlineDev.setLayout(gbl_panelOnlineDev);
		
		JLabel lblOnlineDevices = new JLabel("Online Devices");
		lblOnlineDevices.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(2==e.getClickCount()){
					setOnlineDev();
				}
			}
		});
		lblOnlineDevices.setToolTipText("Double clicking to refresh");
		GridBagConstraints gbc_lblOnlineDevices = new GridBagConstraints();
		gbc_lblOnlineDevices.insets = new Insets(0, 0, 5, 0);
		gbc_lblOnlineDevices.gridx = 0;
		gbc_lblOnlineDevices.gridy = 0;
		panelOnlineDev.add(lblOnlineDevices, gbc_lblOnlineDevices);
		
		list_onlineDev = new JList<String>();
		list_onlineDev.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setOnlineDev();
		
		JScrollPane jspOnlineDev = new JScrollPane();
		jspOnlineDev.setViewportView(list_onlineDev);
		GridBagConstraints gbc_jsp_onlineDev = new GridBagConstraints();
		gbc_jsp_onlineDev.insets = new Insets(0, 0, 5, 0);
		gbc_jsp_onlineDev.fill = GridBagConstraints.BOTH;
		gbc_jsp_onlineDev.gridx = 0;
		gbc_jsp_onlineDev.gridy = 1;
		panelOnlineDev.add(jspOnlineDev, gbc_jsp_onlineDev);
		
		JLabel lblTaskQueue = new JLabel("Task Queue");
		lblTaskQueue.setIcon(new ImageIcon(CNCClient.class.getResource("/com/cncmes/img/Scheduler_16.png")));
		GridBagConstraints gbc_lblTaskQueue = new GridBagConstraints();
		gbc_lblTaskQueue.insets = new Insets(0, 0, 5, 0);
		gbc_lblTaskQueue.gridx = 0;
		gbc_lblTaskQueue.gridy = 2;
		panelOnlineDev.add(lblTaskQueue, gbc_lblTaskQueue);
		
		list_taskQueue = new JList<String>();
		setTaskQueue(null);
		list_taskQueue.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		jspTaskQueue = new JScrollPane();
		jspTaskQueue.setViewportView(list_taskQueue);
		GridBagConstraints gbc_jsp_taskQueue = new GridBagConstraints();
		gbc_jsp_taskQueue.insets = new Insets(0, 0, 5, 0);
		gbc_jsp_taskQueue.fill = GridBagConstraints.BOTH;
		gbc_jsp_taskQueue.gridx = 0;
		gbc_jsp_taskQueue.gridy = 3;
		panelOnlineDev.add(jspTaskQueue, gbc_jsp_taskQueue);
		
		JLabel lblAlarmCncs = new JLabel("Alarm CNCs");
		lblAlarmCncs.setIcon(new ImageIcon(CNCClient.class.getResource("/com/cncmes/img/3d_printer_16.png")));
		GridBagConstraints gbc_lblAlarmCncs = new GridBagConstraints();
		gbc_lblAlarmCncs.insets = new Insets(0, 0, 5, 0);
		gbc_lblAlarmCncs.gridx = 0;
		gbc_lblAlarmCncs.gridy = 4;
		panelOnlineDev.add(lblAlarmCncs, gbc_lblAlarmCncs);
		
		list_alarmCncs = new JList<String>();
		setAlarmCncs(null);
		list_alarmCncs.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		jspAlarmCncs = new JScrollPane();
		jspAlarmCncs.setViewportView(list_alarmCncs);
		GridBagConstraints gbc_jsp_alarmCncs = new GridBagConstraints();
		gbc_jsp_alarmCncs.insets = new Insets(0, 0, 5, 0);
		gbc_jsp_alarmCncs.fill = GridBagConstraints.BOTH;
		gbc_jsp_alarmCncs.gridx = 0;
		gbc_jsp_alarmCncs.gridy = 5;
		panelOnlineDev.add(jspAlarmCncs, gbc_jsp_alarmCncs);
		
		JLabel lblAlarmRobots = new JLabel("Alarm Robots");
		lblAlarmRobots.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(2 == arg0.getClickCount()){
					String lineName = comboBox_lineName.getSelectedItem().toString();
					MaterialInput.setLineName(lineName);
					MaterialInput.showDialog();
				}
			}
		});
		lblAlarmRobots.setIcon(new ImageIcon(CNCClient.class.getResource("/com/cncmes/img/robots_16.png")));
		GridBagConstraints gbc_lblAlarmRobots = new GridBagConstraints();
		gbc_lblAlarmRobots.insets = new Insets(0, 0, 5, 0);
		gbc_lblAlarmRobots.gridx = 0;
		gbc_lblAlarmRobots.gridy = 6;
		panelOnlineDev.add(lblAlarmRobots, gbc_lblAlarmRobots);
		
		list_alarmRobots = new JList<String>();
		setAlarmRobots(null);
		list_alarmRobots.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		jspAlarmRobots = new JScrollPane();
		jspAlarmRobots.setViewportView(list_alarmRobots);
		GridBagConstraints gbc_jsp_alarmRobots = new GridBagConstraints();
		gbc_jsp_alarmRobots.insets = new Insets(0, 0, 5, 0);
		gbc_jsp_alarmRobots.fill = GridBagConstraints.BOTH;
		gbc_jsp_alarmRobots.gridx = 0;
		gbc_jsp_alarmRobots.gridy = 7;
		panelOnlineDev.add(jspAlarmRobots, gbc_jsp_alarmRobots);
		
		panelCNCLine = new JPanel();
		panelCNCLine.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		mainPanel.add(panelCNCLine, BorderLayout.CENTER);
		panelCNCLine.setLayout(new GridLayout(0, 3, 0, 0));
		
		String[] cncList = null;
		if("OK".equals(startupMsg)) cncList = DataUtils.getCNCsByLineName(curLineName);
		PaintCNCLines.getInstance().paintCNCLines(panelCNCLine, cncList);
		if(null != cncList){
			enableAutoLineButton(true);
		}else{
			disableAutoLineButton();
		}
		
		panel_status = new JPanel();
		panel_status.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		mainPanel.add(panel_status, BorderLayout.SOUTH);
		GridBagLayout gbl_panel_status = new GridBagLayout();
		gbl_panel_status.columnWidths = new int[] {180, 220, 570, 30};
		gbl_panel_status.rowHeights = new int[] {20};
		gbl_panel_status.columnWeights = new double[]{1.0, 1.0, 0.0, 0.0};
		gbl_panel_status.rowWeights = new double[]{1.0};
		panel_status.setLayout(gbl_panel_status);
		
		lblWorkpieceInfo = new JLabel("WKPC: " + WorkpieceData.getInstance().getDataMap().size()+"("+SystemInfo.getFinishedWorkpieceQty()+"/"+SystemInfo.getTotalWorkpieceQty()+")");
		GridBagConstraints gbc_lblWorkpieceInfo = new GridBagConstraints();
		gbc_lblWorkpieceInfo.insets = new Insets(0, 0, 0, 5);
		gbc_lblWorkpieceInfo.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblWorkpieceInfo.gridx = 0;
		gbc_lblWorkpieceInfo.gridy = 0;
		panel_status.add(lblWorkpieceInfo, gbc_lblWorkpieceInfo);
		
		lblRobotInfo = new JLabel();
		lblRobotInfo.setIcon(new ImageIcon(CNCClient.class.getResource("/com/cncmes/img/robots_24.png")));
		GridBagConstraints gbc_lblRobotInfo = new GridBagConstraints();
		gbc_lblRobotInfo.insets = new Insets(0, 0, 0, 5);
		gbc_lblRobotInfo.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblRobotInfo.gridx = 1;
		gbc_lblRobotInfo.gridy = 0;
		panel_status.add(lblRobotInfo, gbc_lblRobotInfo);
		
		lblMessage = new JLabel(startupMsg);
		GridBagConstraints gbc_lblMessage = new GridBagConstraints();
		gbc_lblMessage.insets = new Insets(0, 0, 0, 5);
		gbc_lblMessage.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblMessage.gridx = 2;
		gbc_lblMessage.gridy = 0;
		panel_status.add(lblMessage, gbc_lblMessage);
		
		lblHeartbeat = new JLabel("00");
		GridBagConstraints gbc_lblHeartbeat = new GridBagConstraints();
		gbc_lblHeartbeat.gridx = 3;
		gbc_lblHeartbeat.gridy = 0;
		panel_status.add(lblHeartbeat, gbc_lblHeartbeat);
		
		refreshButtonsEnabled();
	}

	private void setAlarmRobots(String[] data) {
		String[] values;
		if(null == data){
			values = new String[]{""};
		}else{
			values = data;
		}
		
		GUIUtils.setJListContent(list_alarmRobots, values);
	}

	private void setAlarmCncs(String[] data) {
		String[] values;
		if(null == data){
			values = new String[]{""};
		}else{
			values = data;
		}
		
		GUIUtils.setJListContent(list_alarmCncs, values);
	}

	private void setTaskQueue(String[] data) {
		String[] values;
		if(null == data){
			values = new String[]{""};
		}else{
			values = data;
		}
		
		GUIUtils.setJListContent(list_taskQueue, values);
	}

	private void setOnlineDev() {
		String[] data = null;
		try {
			data = NetUtils.getOnlineIPs();
		} catch (Exception e) {
		}
		
		String[] values;
		if(null == data){
			values = new String[]{""};
		}else{
			values = data;
		}
		
		GUIUtils.setJListContent(list_onlineDev, values);
	}
	
	public static CNCClient getInstance(){
		return cncClient;
	}
	
	public void refreshLineNames(String lineName){
		cncLines = DataUtils.getCNCLines();
		GUIUtils.setComboBoxValues(comboBox_lineName, cncLines);
		GUIUtils.setComboBoxSelectedIdx(comboBox_lineName, lineName);
		MySystemUtils.sysLoadSettingsFromDB(curLineName);
	}
	
	public void setTaskQueueContent(String[] tq){
		try {
			setTaskQueue(tq);
		} catch (Exception e) {
		}
	}
	
	public void refreshHeartbeat(){
		WorkpieceData wpData = WorkpieceData.getInstance();
		lblHeartbeat.setText(TimeUtils.getCurrentDate("ss"));
		lblWorkpieceInfo.setText("WKPC: "+wpData.size()+"("+SystemInfo.getFinishedWorkpieceQty()+"/"+SystemInfo.getTotalWorkpieceQty()+"/"+DebugUtils.autoInputMaterialEnabled()+")");
		showRunningMsg();
	}
	
	public void setStatusPaneContent(){
		try {
			CncData cncData = CncData.getInstance();
			RobotData robotData = RobotData.getInstance();
			Map<String,LinkedHashMap<RobotItems,Object>> robotDataMap = robotData.getDataMap();
			Set<String> set = robotDataMap.keySet();
			Iterator<String> it = set.iterator();
			
			String robotIP = "";
			DeviceState devState = DeviceState.STANDBY;
			while(it.hasNext()){
				robotIP = it.next();
				devState = robotData.getRobotState(robotIP);
				if(DeviceState.WORKING == devState){
					break;
				}
			}
			
			String model = (String) robotDataMap.get(robotIP).get(RobotItems.MODEL) + ":" + robotIP + "/" + devState;
			lblRobotInfo.setText(model);
			showRunningMsg();
			panel_status.updateUI();
			
			setAlarmCncs(cncData.getAlarmingCNCs(curLineName));
			setAlarmRobots(robotData.getAlarmingRobots(curLineName));
		} catch (Exception e) {
		}
	}
	
	private void showRunningMsg(){
		lblMessage.setText(RunningMsg.get());
	}
	
	private void setTitle(){
		if(null==cncClient) return;
		String title = cncClient.getTitle();
		if(null!=title){
			title = title.split("##")[0] + "##Welcome " + LoginSystem.getUserName();
			cncClient.setTitle(title);
		}
	}
	
	public void refreshButtonsEnabled(){
		btnConfig.setEnabled(!LoginSystem.accessDenied(PermissionItems.SYSCONFIG));
		mntmSystemConfig.setEnabled(!LoginSystem.accessDenied(PermissionItems.SYSCONFIG));
		mntmRobotNetworkCmd.setEnabled(!LoginSystem.accessDenied(PermissionItems.ROBOTCONTROL));
		mntmCncWebApi.setEnabled(!LoginSystem.accessDenied(PermissionItems.CNCCONTROL));
		mntmCncEthernetCommand.setEnabled(!LoginSystem.accessDenied(PermissionItems.CNCCONTROL));
		mntmStartDeviceSvr.setEnabled(!LoginSystem.accessDenied(PermissionItems.SYSCONFIG));
		btnStartAutoLine.setEnabled(!LoginSystem.accessDenied(PermissionItems.TASKHANDLING));
		mntmStartAutoLine.setEnabled(!LoginSystem.accessDenied(PermissionItems.TASKHANDLING));
		mntmUploadProgram.setEnabled(!LoginSystem.accessDenied(PermissionItems.CNCCONTROL));
		mntmCncController.setEnabled(!LoginSystem.accessDenied(PermissionItems.CNCCONTROL));
		mntmRobotController.setEnabled(!LoginSystem.accessDenied(PermissionItems.ROBOTCONTROL));
		mntmScannerController.setEnabled(!LoginSystem.accessDenied(PermissionItems.ROBOTCONTROL));
		setTitle();
		if(LoginSystem.userHasLoginned()){
			btnLogin.setIcon(new ImageIcon(CNCClient.class.getResource("/com/cncmes/img/logout_24.png")));
			btnLogin.setToolTipText("Logout System");
		}else{
			btnLogin.setIcon(new ImageIcon(CNCClient.class.getResource("/com/cncmes/img/login_24.png")));
			btnLogin.setToolTipText("Login System");
		}
	}
	
	private void enableAutoLineButton(boolean enabled){
		mntmStartAutoLine.setEnabled(enabled);
		btnStartAutoLine.setEnabled(enabled);
		mntmStopAutoLine.setEnabled(!enabled);
		btnStopAutoLine.setEnabled(!enabled);
		btnConnect.setEnabled(enabled);
		comboBox_lineName.setEnabled(enabled);
	}
	
	private void disableAutoLineButton(){
		mntmStartAutoLine.setEnabled(false);
		btnStartAutoLine.setEnabled(false);
		mntmStopAutoLine.setEnabled(false);
		btnStopAutoLine.setEnabled(false);
	}
	
	private void startAutoLine(){
		String ret = "";
		ret = MySystemUtils.sysLoadSettingsFromDB(curLineName);
		if(!"OK".equals(ret)){
			JOptionPane.showMessageDialog(cncClient.getContentPane(), ret, "Start Auto Line Error", JOptionPane.ERROR_MESSAGE);
			RunningMsg.set(ret);showRunningMsg();
			return;
		}
		
		ret = MySystemUtils.sysReadyToStart(curLineName);
		if(!"OK".equals(ret)){
			JOptionPane.showMessageDialog(cncClient.getContentPane(), ret, "Start Auto Line Error", JOptionPane.ERROR_MESSAGE);
			RunningMsg.set(ret);showRunningMsg();
			return;
		}

		MySystemUtils.sysMemoryRestore(curLineName, MemoryItems.ALL, DeviceState.ALARMING, true);
		MySystemUtils.sysMemoryRestore(curLineName, MemoryItems.WORKPIECE, null, true);
		
		ret = ThreadController.Run(curLineName);
		if("OK".equals(ret)){
			enableAutoLineButton(false);
			if(TaskMonitor.taskMonitorIsEnabled()){
				SchedulerClient.getInstance().resumeSchedulerForLine(curLineName);
			}
		}else{
			RunningMsg.set(ret);showRunningMsg();
			JOptionPane.showMessageDialog(cncClient.getContentPane(), ret, "Start Auto Line Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void stopAutoLine(){
		String msg = MySystemUtils.sysReayToStop(curLineName);
		if(!"OK".equals(msg)){
			JOptionPane.showMessageDialog(cncClient, msg, "Can't be stopped", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		MySystemUtils.sysMemoryDump(curLineName, MemoryItems.ALL);
		if(TaskMonitor.taskMonitorIsEnabled()){
			SchedulerClient.getInstance().pauseSchedulerForLine(curLineName);
		}
		ThreadController.Stop();
		ConnectDevices.resetDevInitFlag();
		enableAutoLineButton(true);
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
			
			trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(CNCClient.class.getResource("/com/cncmes/img/Butterfly_24.png")),"CNC Client",popup);
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
	
	private void exitSystem(boolean force) {
		String msg = MySystemUtils.sysReayToStop(curLineName);
		if(!"OK".equals(msg)){
			if(!force){
				JOptionPane.showMessageDialog(cncClient, msg, "Can't be stopped", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
		int rtn = JOptionPane.showConfirmDialog(cncClient.getContentPane(), "Are you sure of quiting from CNC Client", "Exit", JOptionPane.YES_NO_OPTION);
		if(0 == rtn){
			MySystemUtils.sysMemoryDump(curLineName, MemoryItems.ALL);
			if(TaskMonitor.taskMonitorIsEnabled()){
				SchedulerClient.getInstance().pauseSchedulerForLine(curLineName);
			}
			ThreadController.Stop();
			socketServer.stopSvrPort(PORTSVRSOCKET);
			System.exit(0);
		}
	}

	private static void checkPreviousInstance(){
		try {
			socketServer.socketSvrStart(PORTSVRSOCKET, new CncClientDataHandler());
			CncClientDataHandler.setPort(PORTSVRSOCKET);
			CncClientDataHandler.setIP(NetUtils.getLocalIP());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "CNC Client is already started", "Program Launch", JOptionPane.INFORMATION_MESSAGE);
			System.exit(0);
//			if(null!=cncClient) cncClient.setVisible(true);
		}
	}
}