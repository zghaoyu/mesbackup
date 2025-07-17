package com.cncmes.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.cncmes.base.CNC;
import com.cncmes.base.CncItems;
import com.cncmes.base.CncWebAPIItems;
import com.cncmes.base.DeviceOP;
import com.cncmes.base.DeviceState;
import com.cncmes.base.DriverItems;
import com.cncmes.base.IRobot;
import com.cncmes.base.MaterialsIOType;
import com.cncmes.base.RobotItems;
import com.cncmes.base.SpecItems;
import com.cncmes.base.WorkpieceItems;
import com.cncmes.ctrl.CncFactory;
import com.cncmes.ctrl.IRobotFactory;
import com.cncmes.data.CncData;
import com.cncmes.data.CncDriver;
import com.cncmes.data.CncWebAPI;
import com.cncmes.data.RobotData;
import com.cncmes.data.RobotDriver;
import com.cncmes.data.SystemConfig;
import com.cncmes.data.WorkpieceData;
import com.cncmes.thread.DeviceMonitor;
import com.cncmes.thread.TaskProcessor;
import com.cncmes.utils.DataUtils;
import com.cncmes.utils.LogUtils;
import com.cncmes.utils.RunningMsg;
import com.cncmes.utils.ThreadUtils;
import com.cncmes.utils.TimeUtils;
import com.cncmes.utils.UploadNCProgram;

import net.sf.json.JSONObject;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.JCheckBox;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;
import java.awt.GridLayout;
import java.awt.Font;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class OneCycleController extends JDialog {
	private JComboBox<String> comboBoxIP;
	private JTextField textField_Info;

	private JComboBox<String> comboBoxRobotIP;
	private JTextField textField_RobotInfo;

	private static final long serialVersionUID = 18L;
	private final JPanel contentPanel = new JPanel();
	private static OneCycleController oneCycleController = new OneCycleController();
	private static CncData cncData = null;
	private static RobotData robotData = null;
	private JProgressBar progressBar;
	private JButton btnOpenDoor;
	private JButton btnCloseDoor;
	private JButton btnUploadNcProgram;
	private JButton btnLock;
	private JButton btnUnlock;
	private JButton btnStartMachining;

	private JButton btnGoHT;
	private JButton btnGoCharging;
	private JButton btnEndCharging;
	private JButton btnGetBattery;
	private JButton btnGetMaterials;
	private JButton btnPutMaterials;
	private JButton btnMoveToMachine;
	private JButton btnLoading;
	private JButton btnUnloading;
	private JButton btnExecute;
	private JButton btnGetCncState;
	private JButton btnGetRobotState;

	private JLabel lblCncStt;
	private JLabel lblRobotStt;
	private JLabel lblRobotBattery;

	private JCheckBox chckbxCheckbox_0;
	private JCheckBox chckbxCheckbox_1;
	private JCheckBox chckbxCheckbox_2;

	private static boolean[] lockFlags = new boolean[6];
	private static boolean[] ncUploadFlags = new boolean[6];
	private static boolean doorClosedFlag = false;
	private static String rackID = "3";
	private final static int CONCURR_OP_TIMEOUT = 600; // second
	private JLabel lblMachiningStatus;
	private JCheckBox chckbxAutoLul;

	public static OneCycleController getInstance() {
		cncData = CncData.getInstance();
		robotData = RobotData.getInstance();
		return oneCycleController;
	}

	/**
	 * Create the dialog.
	 */
	private OneCycleController() {
		cncData = CncData.getInstance();
		robotData = RobotData.getInstance();
		setModal(true);
		setIconImage(Toolkit.getDefaultToolkit()
				.getImage(OneCycleController.class.getResource("/com/cncmes/img/Butterfly_24.png")));
		setTitle("\u52A0\u5DE5\u63A7\u5236\u4E2D\u5FC3");
		setResizable(false);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 520;
		int height = 520;
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);

		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 90, 390 };
		gbl_contentPanel.rowHeights = new int[] { 90, 40, 90, 100, 100 };
		gbl_contentPanel.columnWeights = new double[] { 1.0, 1.0 };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 1.0 };
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblWorkZone = new JLabel("\u4E0A\u4E0B\u6599\u4F4D\u7F6E");
			lblWorkZone.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
			GridBagConstraints gbc_lblWorkZone = new GridBagConstraints();
			gbc_lblWorkZone.fill = GridBagConstraints.VERTICAL;
			gbc_lblWorkZone.insets = new Insets(0, 0, 5, 5);
			gbc_lblWorkZone.gridx = 0;
			gbc_lblWorkZone.gridy = 1;
			contentPanel.add(lblWorkZone, gbc_lblWorkZone);
		}
		{
			JPanel panelWorkZones = new JPanel();
			panelWorkZones.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
			GridBagConstraints gbc_panelWorkZones = new GridBagConstraints();
			gbc_panelWorkZones.insets = new Insets(0, 0, 5, 0);
			gbc_panelWorkZones.fill = GridBagConstraints.BOTH;
			gbc_panelWorkZones.gridx = 1;
			gbc_panelWorkZones.gridy = 1;
			contentPanel.add(panelWorkZones, gbc_panelWorkZones);
			GridBagLayout gbl_panelWorkZones = new GridBagLayout();
			gbl_panelWorkZones.columnWidths = new int[] { 126, 126, 126 };
			gbl_panelWorkZones.rowHeights = new int[] { 0 };
			gbl_panelWorkZones.columnWeights = new double[] { 0.0, 0.0, 0.0 };
			gbl_panelWorkZones.rowWeights = new double[] { 0.0 };
			panelWorkZones.setLayout(gbl_panelWorkZones);
			{
				chckbxCheckbox_0 = new JCheckBox("1-3\u4E0A\u6599/4-6\u4E0B\u6599");
				chckbxCheckbox_0.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
				GridBagConstraints gbc_chckbxCheckbox_0 = new GridBagConstraints();
				gbc_chckbxCheckbox_0.fill = GridBagConstraints.BOTH;
				gbc_chckbxCheckbox_0.insets = new Insets(0, 0, 5, 5);
				gbc_chckbxCheckbox_0.gridx = 0;
				gbc_chckbxCheckbox_0.gridy = 0;
				panelWorkZones.add(chckbxCheckbox_0, gbc_chckbxCheckbox_0);

				chckbxCheckbox_0.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						if (chckbxCheckbox_0.isSelected()) {
							if (chckbxCheckbox_1.isSelected())
								chckbxCheckbox_1.setSelected(false);
							if (chckbxCheckbox_2.isSelected())
								chckbxCheckbox_2.setSelected(false);
						} else {
							if (!chckbxCheckbox_1.isSelected() && !chckbxCheckbox_2.isSelected())
								chckbxCheckbox_0.setSelected(true);
						}
					}
				});
			}
			{
				chckbxCheckbox_1 = new JCheckBox("4-6\u4E0A\u6599/1-3\u4E0B\u6599");
				chckbxCheckbox_1.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
				GridBagConstraints gbc_chckbxCheckbox_1 = new GridBagConstraints();
				gbc_chckbxCheckbox_1.fill = GridBagConstraints.BOTH;
				gbc_chckbxCheckbox_1.insets = new Insets(0, 0, 5, 5);
				gbc_chckbxCheckbox_1.gridx = 1;
				gbc_chckbxCheckbox_1.gridy = 0;
				panelWorkZones.add(chckbxCheckbox_1, gbc_chckbxCheckbox_1);

				chckbxCheckbox_1.addChangeListener(new ChangeListener() {

					public void stateChanged(ChangeEvent e) {
						if (chckbxCheckbox_1.isSelected()) {
							if (chckbxCheckbox_0.isSelected())
								chckbxCheckbox_0.setSelected(false);
							if (chckbxCheckbox_2.isSelected())
								chckbxCheckbox_2.setSelected(false);
						} else {
							if (!chckbxCheckbox_0.isSelected() && !chckbxCheckbox_2.isSelected())
								chckbxCheckbox_1.setSelected(true);
						}
					}
				});
			}
			{
				chckbxCheckbox_2 = new JCheckBox("1-6\u4E0A\u6599/1-6\u4E0B\u6599");
				chckbxCheckbox_2.setSelected(true);
				chckbxCheckbox_2.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
				GridBagConstraints gbc_chckbxCheckbox_2 = new GridBagConstraints();
				gbc_chckbxCheckbox_2.fill = GridBagConstraints.BOTH;
				gbc_chckbxCheckbox_2.insets = new Insets(0, 0, 5, 5);
				gbc_chckbxCheckbox_2.gridx = 2;
				gbc_chckbxCheckbox_2.gridy = 0;
				panelWorkZones.add(chckbxCheckbox_2, gbc_chckbxCheckbox_2);

				chckbxCheckbox_2.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						if (chckbxCheckbox_2.isSelected()) {
							if (chckbxCheckbox_0.isSelected())
								chckbxCheckbox_0.setSelected(false);
							if (chckbxCheckbox_1.isSelected())
								chckbxCheckbox_1.setSelected(false);
						} else {
							if (!chckbxCheckbox_0.isSelected() && !chckbxCheckbox_1.isSelected())
								chckbxCheckbox_2.setSelected(true);
						}
					}
				});
			}
		}
		{
			JLabel lblOperation = new JLabel("\u673A\u5E8A\u64CD\u4F5C");
			lblOperation.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
			GridBagConstraints gbc_lblOperation = new GridBagConstraints();
			gbc_lblOperation.fill = GridBagConstraints.VERTICAL;
			gbc_lblOperation.insets = new Insets(0, 0, 5, 5);
			gbc_lblOperation.gridx = 0;
			gbc_lblOperation.gridy = 2;
			contentPanel.add(lblOperation, gbc_lblOperation);
		}
		{
			JPanel panelOperation = new JPanel();
			panelOperation.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
			GridBagConstraints gbc_panelOperation = new GridBagConstraints();
			gbc_panelOperation.insets = new Insets(0, 0, 5, 0);
			gbc_panelOperation.fill = GridBagConstraints.BOTH;
			gbc_panelOperation.gridx = 1;
			gbc_panelOperation.gridy = 2;
			contentPanel.add(panelOperation, gbc_panelOperation);
			GridBagLayout gbl_panelOperation = new GridBagLayout();
			gbl_panelOperation.columnWidths = new int[] { 126, 126, 126 };
			gbl_panelOperation.rowHeights = new int[] { 28, 28 };
			gbl_panelOperation.columnWeights = new double[] { 0.0, 0.0, 0.0 };
			gbl_panelOperation.rowWeights = new double[] { 0.0, 0.0 };
			panelOperation.setLayout(gbl_panelOperation);
			{
				btnOpenDoor = new JButton("\u5F00\u95E8");
				btnOpenDoor.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
					}
				});
				btnOpenDoor.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
				btnOpenDoor.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseClicked(MouseEvent e) {
						if (btnOpenDoor.isEnabled() && 1 == e.getButton()) {
							setButtonsEnabled(false, true);
							ThreadUtils.Run(new ButtonOnClick(btnOpenDoor.getText()));
						}
					}
				});
				GridBagConstraints gbc_btnOpenDoor = new GridBagConstraints();
				gbc_btnOpenDoor.fill = GridBagConstraints.HORIZONTAL;
				gbc_btnOpenDoor.insets = new Insets(0, 0, 5, 5);
				gbc_btnOpenDoor.gridx = 0;
				gbc_btnOpenDoor.gridy = 0;
				panelOperation.add(btnOpenDoor, gbc_btnOpenDoor);
			}
			{
				btnCloseDoor = new JButton("\u5173\u95E8");
				btnCloseDoor.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
					}
				});
				btnCloseDoor.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
				btnCloseDoor.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if (btnCloseDoor.isEnabled() && 1 == e.getButton()) {
							setButtonsEnabled(false, true);
							ThreadUtils.Run(new ButtonOnClick(btnCloseDoor.getText()));
						}
					}
				});
				GridBagConstraints gbc_btnCloseDoor = new GridBagConstraints();
				gbc_btnCloseDoor.fill = GridBagConstraints.HORIZONTAL;
				gbc_btnCloseDoor.insets = new Insets(0, 0, 5, 5);
				gbc_btnCloseDoor.gridx = 1;
				gbc_btnCloseDoor.gridy = 0;
				panelOperation.add(btnCloseDoor, gbc_btnCloseDoor);
			}
			{
				btnUploadNcProgram = new JButton("\u4E0A\u4F20\u52A0\u5DE5\u7A0B\u5E8F");
				btnUploadNcProgram.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
				btnUploadNcProgram.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseClicked(MouseEvent e) {
						if (btnUploadNcProgram.isEnabled() && 1 == e.getButton()) {
							setButtonsEnabled(false, true);
							ThreadUtils.Run(new ButtonOnClick(btnUploadNcProgram.getText()));
						}
					}
				});
				GridBagConstraints gbc_btnUploadNcProgram = new GridBagConstraints();
				gbc_btnUploadNcProgram.fill = GridBagConstraints.HORIZONTAL;
				gbc_btnUploadNcProgram.insets = new Insets(0, 0, 5, 0);
				gbc_btnUploadNcProgram.gridx = 2;
				gbc_btnUploadNcProgram.gridy = 0;
				panelOperation.add(btnUploadNcProgram, gbc_btnUploadNcProgram);
			}
			{
				btnUnlock = new JButton("\u89E3\u9501\u5DE5\u4EF6");
				btnUnlock.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
				btnUnlock.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if (btnUnlock.isEnabled() && 1 == e.getButton()) {
							setButtonsEnabled(false, true);
							ThreadUtils.Run(new ButtonOnClick(btnUnlock.getText()));
						}
					}
				});
				GridBagConstraints gbc_btnUnlock = new GridBagConstraints();
				gbc_btnUnlock.fill = GridBagConstraints.HORIZONTAL;
				gbc_btnUnlock.insets = new Insets(0, 0, 0, 5);
				gbc_btnUnlock.gridx = 0;
				gbc_btnUnlock.gridy = 1;
				panelOperation.add(btnUnlock, gbc_btnUnlock);
			}
			{
				btnLock = new JButton("\u9501\u7D27\u5DE5\u4EF6");
				btnLock.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
				btnLock.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseClicked(MouseEvent e) {
						if (btnLock.isEnabled() && 1 == e.getButton()) {
							setButtonsEnabled(false, true);
							ThreadUtils.Run(new ButtonOnClick(btnLock.getText()));
						}
					}
				});
				GridBagConstraints gbc_btnLock = new GridBagConstraints();
				gbc_btnLock.fill = GridBagConstraints.HORIZONTAL;
				gbc_btnLock.insets = new Insets(0, 0, 0, 5);
				gbc_btnLock.gridx = 1;
				gbc_btnLock.gridy = 1;
				panelOperation.add(btnLock, gbc_btnLock);
			}
			{
				btnStartMachining = new JButton("\u6267\u884C\u52A0\u5DE5\u7A0B\u5E8F");
				btnStartMachining.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
				btnStartMachining.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if (btnStartMachining.isEnabled() && 1 == e.getButton()) {
							setButtonsEnabled(false, true);
							ThreadUtils.Run(new ButtonOnClick(btnStartMachining.getText()));
						}
					}
				});
				GridBagConstraints gbc_btnStartMachining = new GridBagConstraints();
				gbc_btnStartMachining.fill = GridBagConstraints.HORIZONTAL;
				gbc_btnStartMachining.gridx = 2;
				gbc_btnStartMachining.gridy = 1;
				panelOperation.add(btnStartMachining, gbc_btnStartMachining);
			}
		}
		{
			JLabel lblBasic = new JLabel("\u57FA\u672C\u4FE1\u606F");
			lblBasic.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					if (2 == e.getClickCount()) {
						setButtonsEnabled(true, true);
					}
				}
			});
			lblBasic.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
			GridBagConstraints gbc_lblBasic = new GridBagConstraints();
			gbc_lblBasic.fill = GridBagConstraints.VERTICAL;
			gbc_lblBasic.insets = new Insets(0, 0, 5, 5);
			gbc_lblBasic.gridx = 0;
			gbc_lblBasic.gridy = 0;
			contentPanel.add(lblBasic, gbc_lblBasic);
		}
		{
			JPanel panelBasic = new JPanel();
			panelBasic.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
			GridBagConstraints gbc_panelBasic = new GridBagConstraints();
			gbc_panelBasic.insets = new Insets(0, 0, 5, 0);
			gbc_panelBasic.fill = GridBagConstraints.BOTH;
			gbc_panelBasic.gridx = 1;
			gbc_panelBasic.gridy = 0;
			contentPanel.add(panelBasic, gbc_panelBasic);
			GridBagLayout gbl_panelBasic = new GridBagLayout();
			gbl_panelBasic.columnWidths = new int[] { 40, 140, 100, 70 };
			gbl_panelBasic.rowHeights = new int[] { 28, 28 };
			gbl_panelBasic.columnWeights = new double[] { 0.0, 1.0, 1.0, 0.0 };
			gbl_panelBasic.rowWeights = new double[] { 0.0, 0.0 };
			panelBasic.setLayout(gbl_panelBasic);
			{
				JLabel lblCncIP = new JLabel("\u673A \u5E8A IP");
				lblCncIP.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
				GridBagConstraints gbc_lblCncIP = new GridBagConstraints();
				gbc_lblCncIP.insets = new Insets(0, 0, 5, 5);
				gbc_lblCncIP.gridx = 0;
				gbc_lblCncIP.gridy = 0;
				panelBasic.add(lblCncIP, gbc_lblCncIP);
			}
			{
				comboBoxIP = new JComboBox<String>();
				comboBoxIP.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
				GridBagConstraints gbc_comboBoxIP = new GridBagConstraints();
				gbc_comboBoxIP.fill = GridBagConstraints.HORIZONTAL;
				gbc_comboBoxIP.insets = new Insets(0, 0, 5, 5);
				gbc_comboBoxIP.gridx = 1;
				gbc_comboBoxIP.gridy = 0;
				panelBasic.add(comboBoxIP, gbc_comboBoxIP);
				comboBoxIP.setModel(new DefaultComboBoxModel<String>(getIPList()));
				{
					textField_Info = new JTextField();
					textField_Info.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
					GridBagConstraints gbc_textField_Info = new GridBagConstraints();
					gbc_textField_Info.insets = new Insets(0, 0, 5, 5);
					gbc_textField_Info.fill = GridBagConstraints.HORIZONTAL;
					gbc_textField_Info.gridx = 2;
					gbc_textField_Info.gridy = 0;
					panelBasic.add(textField_Info, gbc_textField_Info);
					textField_Info.setEditable(false);
					textField_Info.setText("9100");
					textField_Info.setColumns(10);
				}
				{
					btnGetCncState = new JButton("\u673A \u5E8A \u72B6 \u6001");
					btnGetCncState.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
					btnGetCncState.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if (btnGetCncState.isEnabled() && 1 == e.getButton()) {
								btnGetCncState.setEnabled(false);
								showCncState(getCncState());
								btnGetCncState.setEnabled(true);
							}
						}
					});
					GridBagConstraints gbc_btnGetCncState = new GridBagConstraints();
					gbc_btnGetCncState.fill = GridBagConstraints.HORIZONTAL;
					gbc_btnGetCncState.insets = new Insets(0, 0, 5, 0);
					gbc_btnGetCncState.gridx = 3;
					gbc_btnGetCncState.gridy = 0;
					panelBasic.add(btnGetCncState, gbc_btnGetCncState);
				}
				{
					JLabel lblRobotIP = new JLabel("\u673A\u5668\u4EBAIP");
					lblRobotIP.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
					GridBagConstraints gbc_lblRobotIP = new GridBagConstraints();
					gbc_lblRobotIP.anchor = GridBagConstraints.EAST;
					gbc_lblRobotIP.insets = new Insets(0, 0, 0, 5);
					gbc_lblRobotIP.gridx = 0;
					gbc_lblRobotIP.gridy = 1;
					panelBasic.add(lblRobotIP, gbc_lblRobotIP);
				}
				{
					comboBoxRobotIP = new JComboBox<String>();
					comboBoxRobotIP.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
					GridBagConstraints gbc_comboBoxRobotIP = new GridBagConstraints();
					gbc_comboBoxRobotIP.insets = new Insets(0, 0, 0, 5);
					gbc_comboBoxRobotIP.fill = GridBagConstraints.HORIZONTAL;
					gbc_comboBoxRobotIP.gridx = 1;
					gbc_comboBoxRobotIP.gridy = 1;
					panelBasic.add(comboBoxRobotIP, gbc_comboBoxRobotIP);
					comboBoxRobotIP.setModel(new DefaultComboBoxModel<String>(getRobotList()));
				}
				{
					textField_RobotInfo = new JTextField();
					textField_RobotInfo.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
					textField_RobotInfo.setEditable(false);
					textField_RobotInfo.setText("R2D2 / 7000");
					GridBagConstraints gbc_textField_RobotInfo = new GridBagConstraints();
					gbc_textField_RobotInfo.insets = new Insets(0, 0, 0, 5);
					gbc_textField_RobotInfo.fill = GridBagConstraints.HORIZONTAL;
					gbc_textField_RobotInfo.gridx = 2;
					gbc_textField_RobotInfo.gridy = 1;
					panelBasic.add(textField_RobotInfo, gbc_textField_RobotInfo);
					textField_RobotInfo.setColumns(10);
				}
				{
					btnGetRobotState = new JButton("\u673A\u5668\u4EBA\u72B6\u6001");
					btnGetRobotState.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
					btnGetRobotState.addMouseListener(new MouseAdapter() {

						@Override
						public void mouseClicked(MouseEvent e) {
							if (btnGetRobotState.isEnabled() && 1 == e.getButton()) {
								btnGetRobotState.setEnabled(false);
								showRobotBattery(robotGetBattery());
								showRobotPosition(robotGetPosition());
								btnGetRobotState.setEnabled(true);
							}
						}
					});
					GridBagConstraints gbc_btnGetRobotState = new GridBagConstraints();
					gbc_btnGetRobotState.fill = GridBagConstraints.HORIZONTAL;
					gbc_btnGetRobotState.gridx = 3;
					gbc_btnGetRobotState.gridy = 1;
					panelBasic.add(btnGetRobotState, gbc_btnGetRobotState);
				}
				{
					JLabel lblRobotOP = new JLabel("\u673A\u5668\u4EBA\u64CD\u4F5C");
					lblRobotOP.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
					GridBagConstraints gbc_lblRobotOP = new GridBagConstraints();
					gbc_lblRobotOP.insets = new Insets(0, 0, 5, 5);
					gbc_lblRobotOP.gridx = 0;
					gbc_lblRobotOP.gridy = 3;
					contentPanel.add(lblRobotOP, gbc_lblRobotOP);
				}
				{
					JPanel panelRobotOP = new JPanel();
					panelRobotOP.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
					GridBagConstraints gbc_panelRobotOP = new GridBagConstraints();
					gbc_panelRobotOP.insets = new Insets(0, 0, 5, 0);
					gbc_panelRobotOP.fill = GridBagConstraints.BOTH;
					gbc_panelRobotOP.gridx = 1;
					gbc_panelRobotOP.gridy = 3;
					contentPanel.add(panelRobotOP, gbc_panelRobotOP);
					GridBagLayout gbl_panelRobotOP = new GridBagLayout();
					gbl_panelRobotOP.columnWidths = new int[] { 138, 120, 120 };
					gbl_panelRobotOP.rowHeights = new int[] { 0, 0, 0 };
					gbl_panelRobotOP.columnWeights = new double[] { 0.0, 0.0, 0.0 };
					gbl_panelRobotOP.rowWeights = new double[] { 0.0, 0.0, 0.0 };
					panelRobotOP.setLayout(gbl_panelRobotOP);
					{
						btnGoHT = new JButton("\u79FB\u81F3\u5145\u7535\u4F4D\u7F6E");
						btnGoHT.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
						btnGoHT.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e) {
								if (btnGoHT.isEnabled() && 1 == e.getButton()) {
									setButtonsEnabled(false, true);
									ThreadUtils.Run(new ButtonOnClick(btnGoHT.getText()));
								}
							}
						});
						GridBagConstraints gbc_btnGoHT = new GridBagConstraints();
						gbc_btnGoHT.fill = GridBagConstraints.HORIZONTAL;
						gbc_btnGoHT.insets = new Insets(0, 0, 5, 5);
						gbc_btnGoHT.gridx = 0;
						gbc_btnGoHT.gridy = 0;
						panelRobotOP.add(btnGoHT, gbc_btnGoHT);
					}
					{
						btnGoCharging = new JButton("\u5F00\u59CB\u5145\u7535");
						btnGoCharging.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
						btnGoCharging.addMouseListener(new MouseAdapter() {

							@Override
							public void mouseClicked(MouseEvent e) {
								if (btnGoCharging.isEnabled() && 1 == e.getButton()) {
									setButtonsEnabled(false, true);
									ThreadUtils.Run(new ButtonOnClick(btnGoCharging.getText()));
								}
							}
						});
						GridBagConstraints gbc_btnGoCharging = new GridBagConstraints();
						gbc_btnGoCharging.fill = GridBagConstraints.HORIZONTAL;
						gbc_btnGoCharging.insets = new Insets(0, 0, 5, 5);
						gbc_btnGoCharging.gridx = 1;
						gbc_btnGoCharging.gridy = 0;
						panelRobotOP.add(btnGoCharging, gbc_btnGoCharging);
					}
					{
						btnEndCharging = new JButton("\u7ED3\u675F\u5145\u7535");
						btnEndCharging.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
						btnEndCharging.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e) {
								if (btnEndCharging.isEnabled() && 1 == e.getButton()) {
									setButtonsEnabled(false, true);
									ThreadUtils.Run(new ButtonOnClick(btnEndCharging.getText()));
								}
							}
						});
						GridBagConstraints gbc_btnEndCharging = new GridBagConstraints();
						gbc_btnEndCharging.fill = GridBagConstraints.HORIZONTAL;
						gbc_btnEndCharging.insets = new Insets(0, 0, 5, 0);
						gbc_btnEndCharging.gridx = 2;
						gbc_btnEndCharging.gridy = 0;
						panelRobotOP.add(btnEndCharging, gbc_btnEndCharging);
					}
					{
						btnGetBattery = new JButton("\u67E5\u8BE2\u5269\u4F59\u7535\u91CF");
						btnGetBattery.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
						btnGetBattery.addMouseListener(new MouseAdapter() {

							@Override
							public void mouseClicked(MouseEvent e) {
								if (btnGetBattery.isEnabled() && 1 == e.getButton()) {
									setButtonsEnabled(false, true);
									ThreadUtils.Run(new ButtonOnClick(btnGetBattery.getText()));
								}
							}
						});
						GridBagConstraints gbc_btnGetBattery = new GridBagConstraints();
						gbc_btnGetBattery.fill = GridBagConstraints.HORIZONTAL;
						gbc_btnGetBattery.insets = new Insets(0, 0, 5, 5);
						gbc_btnGetBattery.gridx = 0;
						gbc_btnGetBattery.gridy = 1;
						panelRobotOP.add(btnGetBattery, gbc_btnGetBattery);
					}
					{
						btnGetMaterials = new JButton("\u673A\u5668\u4EBA\u81EA\u4E0A\u6599");
						btnGetMaterials.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
						btnGetMaterials.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e) {
								if (btnGetMaterials.isEnabled() && 1 == e.getButton()) {
									setButtonsEnabled(false, true);
									ThreadUtils.Run(new ButtonOnClick(btnGetMaterials.getText()));
								}
							}
						});
						GridBagConstraints gbc_btnGetMaterials = new GridBagConstraints();
						gbc_btnGetMaterials.fill = GridBagConstraints.HORIZONTAL;
						gbc_btnGetMaterials.insets = new Insets(0, 0, 5, 5);
						gbc_btnGetMaterials.gridx = 1;
						gbc_btnGetMaterials.gridy = 1;
						panelRobotOP.add(btnGetMaterials, gbc_btnGetMaterials);
					}
					{
						btnPutMaterials = new JButton("\u673A\u5668\u4EBA\u81EA\u4E0B\u6599");
						btnPutMaterials.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
						btnPutMaterials.addMouseListener(new MouseAdapter() {

							@Override
							public void mouseClicked(MouseEvent e) {
								if (btnPutMaterials.isEnabled() && 1 == e.getButton()) {
									setButtonsEnabled(false, true);
									ThreadUtils.Run(new ButtonOnClick(btnPutMaterials.getText()));
								}
							}
						});
						GridBagConstraints gbc_btnPutMaterials = new GridBagConstraints();
						gbc_btnPutMaterials.fill = GridBagConstraints.HORIZONTAL;
						gbc_btnPutMaterials.insets = new Insets(0, 0, 5, 0);
						gbc_btnPutMaterials.gridx = 2;
						gbc_btnPutMaterials.gridy = 1;
						panelRobotOP.add(btnPutMaterials, gbc_btnPutMaterials);
					}
					{
						btnMoveToMachine = new JButton("\u79FB\u81F3\u673A\u5E8A\u4E0A\u4E0B\u6599\u4F4D\u7F6E");
						btnMoveToMachine.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
						btnMoveToMachine.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e) {
								if (btnMoveToMachine.isEnabled() && 1 == e.getButton()) {
									setButtonsEnabled(false, true);
									ThreadUtils.Run(new ButtonOnClick(btnMoveToMachine.getText()));
								}
							}
						});
						GridBagConstraints gbc_btnParking = new GridBagConstraints();
						gbc_btnParking.fill = GridBagConstraints.HORIZONTAL;
						gbc_btnParking.insets = new Insets(0, 0, 0, 5);
						gbc_btnParking.gridx = 0;
						gbc_btnParking.gridy = 2;
						panelRobotOP.add(btnMoveToMachine, gbc_btnParking);
					}
					{
						btnLoading = new JButton("\u7ED9\u673A\u5E8A\u4E0A\u6599");
						btnLoading.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
						btnLoading.addMouseListener(new MouseAdapter() {

							@Override
							public void mouseClicked(MouseEvent e) {
								if (btnLoading.isEnabled() && 1 == e.getButton()) {
									setButtonsEnabled(false, true);
									ThreadUtils.Run(new ButtonOnClick(btnLoading.getText()));
								}
							}
						});
						GridBagConstraints gbc_btnLoading = new GridBagConstraints();
						gbc_btnLoading.fill = GridBagConstraints.HORIZONTAL;
						gbc_btnLoading.insets = new Insets(0, 0, 0, 5);
						gbc_btnLoading.gridx = 1;
						gbc_btnLoading.gridy = 2;
						panelRobotOP.add(btnLoading, gbc_btnLoading);
					}
					{
						btnUnloading = new JButton("\u7ED9\u673A\u5E8A\u4E0B\u6599");
						btnUnloading.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
						btnUnloading.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e) {
								if (btnUnloading.isEnabled() && 1 == e.getButton()) {
									setButtonsEnabled(false, true);
									ThreadUtils.Run(new ButtonOnClick(btnUnloading.getText()));
								}
							}
						});
						GridBagConstraints gbc_btnUnloading = new GridBagConstraints();
						gbc_btnUnloading.fill = GridBagConstraints.HORIZONTAL;
						gbc_btnUnloading.gridx = 2;
						gbc_btnUnloading.gridy = 2;
						panelRobotOP.add(btnUnloading, gbc_btnUnloading);
					}
				}
				{
					JLabel lblExe = new JLabel("\u4E0A\u6599\u5E76\u52A0\u5DE5");
					lblExe.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
					GridBagConstraints gbc_lblExe = new GridBagConstraints();
					gbc_lblExe.insets = new Insets(0, 0, 5, 5);
					gbc_lblExe.gridx = 0;
					gbc_lblExe.gridy = 4;
					contentPanel.add(lblExe, gbc_lblExe);
				}
				{
					JPanel panelExe = new JPanel();
					panelExe.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
					GridBagConstraints gbc_panelExe = new GridBagConstraints();
					gbc_panelExe.insets = new Insets(0, 0, 5, 0);
					gbc_panelExe.fill = GridBagConstraints.BOTH;
					gbc_panelExe.gridx = 1;
					gbc_panelExe.gridy = 4;
					contentPanel.add(panelExe, gbc_panelExe);
					panelExe.setLayout(new GridLayout(1, 0, 0, 0));
					{
						JPanel panelStatus = new JPanel();
						panelExe.add(panelStatus);
						GridBagLayout gbl_panelStatus = new GridBagLayout();
						gbl_panelStatus.columnWidths = new int[] { 70, 109 };
						gbl_panelStatus.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
						gbl_panelStatus.columnWeights = new double[] { 0.0, 0.0 };
						gbl_panelStatus.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
						panelStatus.setLayout(gbl_panelStatus);
						{
							JLabel lblAutoMode = new JLabel("\u81EA \u52A8 \u6A21 \u5F0F");
							lblAutoMode.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
							GridBagConstraints gbc_lblAutoMode = new GridBagConstraints();
							gbc_lblAutoMode.insets = new Insets(0, 0, 5, 5);
							gbc_lblAutoMode.gridx = 0;
							gbc_lblAutoMode.gridy = 0;
							panelStatus.add(lblAutoMode, gbc_lblAutoMode);
						}
						{
							chckbxAutoLul = new JCheckBox("\u81EA\u52A8\u5FAA\u73AF\u4E0A\u4E0B\u6599");
							chckbxAutoLul.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
							GridBagConstraints gbc_chckbxAutoLul = new GridBagConstraints();
							gbc_chckbxAutoLul.insets = new Insets(0, 0, 5, 0);
							gbc_chckbxAutoLul.gridx = 1;
							gbc_chckbxAutoLul.gridy = 0;
							panelStatus.add(chckbxAutoLul, gbc_chckbxAutoLul);
						}
						{
							JLabel lblCncstatus = new JLabel("\u673A \u5E8A \u72B6 \u6001");
							lblCncstatus.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
							GridBagConstraints gbc_lblCncstatus = new GridBagConstraints();
							gbc_lblCncstatus.insets = new Insets(0, 0, 5, 5);
							gbc_lblCncstatus.gridx = 0;
							gbc_lblCncstatus.gridy = 1;
							panelStatus.add(lblCncstatus, gbc_lblCncstatus);
						}
						{
							lblCncStt = new JLabel("Unknown");
							lblCncStt.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
							GridBagConstraints gbc_lblCncStt = new GridBagConstraints();
							gbc_lblCncStt.insets = new Insets(0, 0, 5, 0);
							gbc_lblCncStt.gridx = 1;
							gbc_lblCncStt.gridy = 1;
							panelStatus.add(lblCncStt, gbc_lblCncStt);
						}
						{
							JLabel lblRobotpos = new JLabel("\u673A\u5668\u4EBA\u4F4D\u7F6E");
							lblRobotpos.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
							GridBagConstraints gbc_lblRobotpos = new GridBagConstraints();
							gbc_lblRobotpos.insets = new Insets(0, 0, 5, 5);
							gbc_lblRobotpos.gridx = 0;
							gbc_lblRobotpos.gridy = 2;
							panelStatus.add(lblRobotpos, gbc_lblRobotpos);
						}
						{
							lblRobotStt = new JLabel("Unknown");
							lblRobotStt.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
							GridBagConstraints gbc_lblRobotStt = new GridBagConstraints();
							gbc_lblRobotStt.insets = new Insets(0, 0, 5, 0);
							gbc_lblRobotStt.gridx = 1;
							gbc_lblRobotStt.gridy = 2;
							panelStatus.add(lblRobotStt, gbc_lblRobotStt);
						}
						{
							JLabel lblBattery = new JLabel("\u673A\u5668\u4EBA\u7535\u91CF");
							lblBattery.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
							GridBagConstraints gbc_lblBattery = new GridBagConstraints();
							gbc_lblBattery.insets = new Insets(0, 0, 5, 5);
							gbc_lblBattery.gridx = 0;
							gbc_lblBattery.gridy = 3;
							panelStatus.add(lblBattery, gbc_lblBattery);
						}
						{
							lblRobotBattery = new JLabel("??%");
							lblRobotBattery.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
							GridBagConstraints gbc_lblRobotBattery = new GridBagConstraints();
							gbc_lblRobotBattery.insets = new Insets(0, 0, 5, 0);
							gbc_lblRobotBattery.gridx = 1;
							gbc_lblRobotBattery.gridy = 3;
							panelStatus.add(lblRobotBattery, gbc_lblRobotBattery);
						}
						{
							JLabel lblProgress_1 = new JLabel("\u52A0 \u5DE5 \u8FDB \u5EA6");
							lblProgress_1.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
							GridBagConstraints gbc_lblProgress_1 = new GridBagConstraints();
							gbc_lblProgress_1.insets = new Insets(0, 0, 5, 5);
							gbc_lblProgress_1.gridx = 0;
							gbc_lblProgress_1.gridy = 4;
							panelStatus.add(lblProgress_1, gbc_lblProgress_1);
						}
						{
							progressBar = new JProgressBar();
							GridBagConstraints gbc_progressBar = new GridBagConstraints();
							gbc_progressBar.insets = new Insets(0, 0, 5, 0);
							gbc_progressBar.fill = GridBagConstraints.BOTH;
							gbc_progressBar.gridx = 1;
							gbc_progressBar.gridy = 4;
							panelStatus.add(progressBar, gbc_progressBar);
							progressBar.setMinimum(0);
							progressBar.setMaximum(100);
							progressBar.setStringPainted(true);
							progressBar.setForeground(Color.CYAN);
						}
						{
							JLabel lblCurrStatus = new JLabel("\u5F53 \u524D \u72B6 \u6001");
							lblCurrStatus.addMouseListener(new MouseAdapter() {

								@Override
								public void mouseClicked(MouseEvent e) {
									if (2 == e.getClickCount()) {
										String prefix = setMachiningCodePrefix();
										JOptionPane.showMessageDialog(contentPanel, "NC Script Prefix is " + prefix,
												"Program Prefix", JOptionPane.INFORMATION_MESSAGE);
									}
								}
							});
							lblCurrStatus.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
							GridBagConstraints gbc_lblCurrStatus = new GridBagConstraints();
							gbc_lblCurrStatus.insets = new Insets(0, 0, 0, 5);
							gbc_lblCurrStatus.gridx = 0;
							gbc_lblCurrStatus.gridy = 5;
							panelStatus.add(lblCurrStatus, gbc_lblCurrStatus);
						}
						{
							lblMachiningStatus = new JLabel("\u7CFB\u7EDF\u5C31\u7EEA");
							lblMachiningStatus.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
							GridBagConstraints gbc_lblMachiningStatus = new GridBagConstraints();
							gbc_lblMachiningStatus.gridx = 1;
							gbc_lblMachiningStatus.gridy = 5;
							panelStatus.add(lblMachiningStatus, gbc_lblMachiningStatus);
						}
					}
					{
						btnExecute = new JButton("\u81EA\u52A8\u4E0A\u6599\u5E76\u542F\u52A8\u52A0\u5DE5\u7A0B\u5E8F");
						btnExecute.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e) {
								if (btnExecute.isEnabled() && 1 == e.getButton()) {
									setButtonsEnabled(false, true);
									ThreadUtils.Run(new ExecuteTask());
								}
							}
						});
						btnExecute.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
						panelExe.add(btnExecute);
					}
				}
				comboBoxIP.addItemListener(new ItemListener() {

					public void itemStateChanged(ItemEvent arg0) {
						setCncInfo();
					}
				});
				comboBoxRobotIP.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent arg0) {
						setRobotInfo();
					}
				});
			}

		}

		setCncInfo();
		setRobotInfo();
		setButtonsEnabled(true, false);
		setLULSlots();
	}

	public void setButtonsEnabled(boolean enabled, boolean force) {
		try {
			if (force) {
				btnOpenDoor.setEnabled(enabled);
				btnCloseDoor.setEnabled(enabled);
				btnUploadNcProgram.setEnabled(enabled);
				btnLock.setEnabled(enabled);
				btnUnlock.setEnabled(enabled);
				btnStartMachining.setEnabled(enabled);

				btnGoHT.setEnabled(enabled);
				btnGoCharging.setEnabled(enabled);
				btnEndCharging.setEnabled(enabled);
				btnGetBattery.setEnabled(enabled);
				btnGetMaterials.setEnabled(enabled);
				btnPutMaterials.setEnabled(enabled);
				btnMoveToMachine.setEnabled(enabled);
				btnLoading.setEnabled(enabled);
				btnUnloading.setEnabled(enabled);
				btnExecute.setEnabled(enabled);
				chckbxCheckbox_0.setEnabled(enabled);
				chckbxCheckbox_1.setEnabled(enabled);
				chckbxCheckbox_2.setEnabled(enabled);
			} else {
				String robotPos = robotGetPosition();
				String cncTag = getCncTagName();
				boolean cncWorking = cncIsWorking();
//				boolean robotParking = robotIsParking();
				boolean robotCharging = robotIsCharging();

				btnOpenDoor.setEnabled(!cncWorking);
				btnCloseDoor.setEnabled(!robotPos.contains(cncTag));
				btnUploadNcProgram.setEnabled(!cncWorking);
				btnLock.setEnabled(!cncWorking);
				btnUnlock.setEnabled(!cncWorking);
				btnStartMachining.setEnabled(!cncWorking);

//				btnGoHT.setEnabled(!(robotPos.contains("CS") || robotPos.contains("HT")));
//				btnGoCharging.setEnabled((robotPos.contains("CS") || robotPos.contains("HT")) && !(robotParking || robotCharging));
//				btnEndCharging.setEnabled(robotCharging);		
				
				//add by Hui Zhi Fang 2021.11.2
				btnGoHT.setEnabled(!robotPos.contains("Charger"));
				//btnGoCharging.setEnabled((robotPos.contains("Charger")));
				//btnEndCharging.setEnabled(robotCharging);
				btnGoCharging.setEnabled(enabled);
				btnEndCharging.setEnabled(enabled);
				//end
				
				btnGetBattery.setEnabled(enabled);

				btnGetMaterials.setEnabled(robotCharging);
				btnPutMaterials.setEnabled(robotCharging);

				btnMoveToMachine.setEnabled(!robotPos.contains(cncTag));
				//btnLoading.setEnabled(robotPos.contains(cncTag) && robotParking);
				//btnUnloading.setEnabled(robotPos.contains(cncTag) && robotParking);
				btnLoading.setEnabled(enabled);
				btnUnloading.setEnabled(enabled);
				
				btnExecute.setEnabled(!cncWorking);
				
				
				setLULSlots();	//enable chckbxCheckbox_0/1/2 button, add by Hui Zhi  2021.11.3
				
			}
		} catch (Exception e) {
		}
	}

	private IRobot getRobotControl() {
		IRobot ctrl = null;

		String ip = comboBoxRobotIP.getSelectedItem().toString();
		RobotDriver robotDriver = RobotDriver.getInstance();
		String robotModel = (String) robotData.getData(ip).get(RobotItems.MODEL);
		String robotDrvName = (String) robotDriver.getData(robotModel).get(DriverItems.DRIVER);
		ctrl = IRobotFactory.getInstance(robotDrvName);

		return ctrl;
	}

	private void setRobotInfo() {
		String deviceIP = comboBoxRobotIP.getSelectedItem().toString();
		String info = "Unkown device";
		String model = "";
		if (null != robotData.getData(deviceIP)) {
			model = (String) robotData.getData(deviceIP).get(RobotItems.MODEL);
			info = model;
			info += " / " + robotData.getData(deviceIP).get(RobotItems.PORT);
		}
		textField_RobotInfo.setText(info);
	}

	private String[] getRobotList() {
		String[] ips = null;
		String tmp = "";

		Map<String, LinkedHashMap<RobotItems, Object>> dt = robotData.getDataMap();
		for (String ip : dt.keySet()) {
			if ("".equals(tmp)) {
				tmp = ip;
			} else {
				tmp += "," + ip;
			}
		}

		if (!"".equals(tmp)) {
			ips = tmp.split(",");
		} else {
			ips = new String[] { " " };
		}

		System.out.println(tmp);
		return ips;
	}

	private CNC getCncControl() {
		CNC ctrl = null;

		String ip = comboBoxIP.getSelectedItem().toString();
		CncDriver cncDriver = CncDriver.getInstance();
		String cncModel = (String) cncData.getData(ip).get(CncItems.MODEL);
		String cncDrvName = (String) cncDriver.getData(cncModel).get(DriverItems.DRIVER);
		String cncDataHandler = (String) cncDriver.getData(cncModel).get(DriverItems.DATAHANDLER);
		ctrl = CncFactory.getInstance(cncDrvName, cncDataHandler, cncModel);

		return ctrl;
	}

	private void setCncInfo() {
		String deviceIP = comboBoxIP.getSelectedItem().toString();
		String info = "Unkown device";
		String cncModel = "";
		if (null != cncData.getData(deviceIP)) {
			cncModel = (String) cncData.getData(deviceIP).get(CncItems.MODEL);
			info = cncModel;
			info += " / " + cncData.getData(deviceIP).get(CncItems.PORT);
			cncData.clearMachiningData(deviceIP);
		}
		textField_Info.setText(info);
	}

	private String[] getIPList() {
		String[] ips = null;
		String tmp = "";

		Map<String, LinkedHashMap<CncItems, Object>> dt = cncData.getDataMap();
		for (String ip : dt.keySet()) {
			if ("".equals(tmp)) {
				tmp = ip;
			} else {
				tmp += "," + ip;
			}
		}

		if (!"".equals(tmp)) {
			ips = tmp.split(",");
		} else {
			ips = new String[] { " " };
		}

		System.out.println(tmp);
		return ips;
	}

	@SuppressWarnings("unchecked")
	private String getNotBlankZones() {
		String zns = "";

		ArrayList<Object> selectedSlots = getSelectedSlots(MaterialsIOType.ROBOTTOMACHINE);
		ArrayList<Integer> toSlots = (ArrayList<Integer>) selectedSlots.get(1);
		if (toSlots.size() > 0) {
			for (int i = 0; i < toSlots.size(); i++) {
				if ("".equals(zns)) {
					zns = "" + toSlots.get(i);
				} else {
					zns += "," + toSlots.get(i);
				}
			}
		}

		return zns;
	}

	private boolean robotGoHT() {
		boolean ok = false;
		IRobot ctrl = getRobotControl();

		if (null != ctrl) {
			String ip = comboBoxRobotIP.getSelectedItem().toString();
			// ok = ctrl.moveToRack(ip, rackID, "MORI_DR5A");
			ok = ctrl.dockTo(ip, "ts6charger");
		}

		return ok;
	}

	private boolean robotGoCharging() {
		boolean ok = false;
		IRobot ctrl = getRobotControl();

		if (null != ctrl) {
			String ip = comboBoxRobotIP.getSelectedItem().toString();
			// ok = ctrl.goCharging(ip, "MORI_DR5A");
			ok = ctrl.enableCharging(ip);
		}

		return ok;
	}

	private boolean robotEndCharging() {
		boolean ok = false;
		IRobot ctrl = getRobotControl();

		if (null != ctrl) {
			String ip = comboBoxRobotIP.getSelectedItem().toString();
			// ok = ctrl.stopCharging(ip, "MORI_DR5A");
			 ok = ctrl.moveBackward(ip);
		}

		return ok;
	}

	private String robotGetBattery() {
		String rtn = "?";
		IRobot ctrl = getRobotControl();

		if (null != ctrl) {
			String ip = comboBoxRobotIP.getSelectedItem().toString();
			// rtn = ctrl.getBattery(ip, "MORI_DR5A");
			rtn = ctrl.getBattery(ip);
		}

		return rtn;
	}

	private String robotGetPosition() {
		/*String pos = "Unknown";
		String ip = comboBoxRobotIP.getSelectedItem().toString();
		String ps = (String) robotData.getItemVal(ip, RobotItems.POSITION);
		String parking = (String) robotData.getItemVal(ip, RobotItems.POS_PARKING);
		String charging = (String) robotData.getItemVal(ip, RobotItems.POS_CHARGING);

		if (null != ps) {
			if (null != parking || null != charging) {
				if (null != parking)
					pos = ps + " / " + parking;
				if (null != charging)
					pos = ps + " / " + charging;
			} else {
				pos = ps;
			}
		}
		return pos;*/
		
		// add by Hui Zhi Fang 2021.11.2 
		String position = "Unknown";
		IRobot ctrl = getRobotControl();

		if (null != ctrl) {
			String ip = comboBoxRobotIP.getSelectedItem().toString();
			position = ctrl.getAgvPosition(ip);
		}
		return position;
	}

//	private boolean robotIsParking() {
////		boolean parking = false;
////		String ip = comboBoxRobotIP.getSelectedItem().toString();
////		String pk = (String) robotData.getItemVal(ip, RobotItems.POS_PARKING);
////		if (null != pk)
////			parking = true;
////		return parking;
//		
//		// add by Hui Zhi Fang 2021.11.2 
//		boolean parking = false;
//		String position = robotGetPosition();
//		if(null != position){
//			if(position.equals("Charger") || position.equals("CNC_01"))
//				parking = true;
//		}
//		return parking;
//	}

	private boolean robotIsCharging() {
//		boolean charging = false;
//		String ip = comboBoxRobotIP.getSelectedItem().toString();
//		String cg = (String) robotData.getItemVal(ip, RobotItems.POS_CHARGING);
//		if (null != cg)
//			charging = true;		
//		return charging;
		
		// add by Hui Zhi Fang 2021.11.2 
		boolean charging = false;
		String position = robotGetPosition();
		if(null != position){
			if(position.equals("Charger"))
				charging = true;
		}
		return charging;
	}

	@SuppressWarnings("unchecked")
	private boolean robotGetMaterials() {
		boolean ok = false;
		IRobot ctrl = getRobotControl();

		if (null != ctrl) {
			String ip = comboBoxRobotIP.getSelectedItem().toString();
			clearRobotTray(ip);
			ArrayList<Object> selectedSlots = getSelectedSlots(MaterialsIOType.HTTOROBOT);
			ArrayList<Integer> fromSlots = (ArrayList<Integer>) selectedSlots.get(0);
			ArrayList<Integer> toSlots = (ArrayList<Integer>) selectedSlots.get(1);

			if (fromSlots.size() > 0) {
				for (int i = 0; i < fromSlots.size(); i++) {
					// ok = ctrl.pickMaterialFromRack(ip, rackID, "" + fromSlots.get(i), "" + toSlots.get(i), "MORI_DR5A");
					ArrayList<Object> rtn = ctrl.pickAndPlace(ip, "input_rack_b_slot_" + fromSlots.get(i), "robot_pallet_a_slot_" + toSlots.get(i));
					ok = (boolean) rtn.get(0);
					if (!ok)
						break;
				}
			}
		}

		return ok;
	}

	@SuppressWarnings("unchecked")
	private boolean robotPutMaterials() {
		boolean ok = false;
		IRobot ctrl = getRobotControl();

		if (null != ctrl) {
			String ip = comboBoxRobotIP.getSelectedItem().toString();
			ArrayList<Object> selectedSlots = getSelectedSlots(MaterialsIOType.ROBOTTOHT);
			ArrayList<Integer> fromSlots = (ArrayList<Integer>) selectedSlots.get(0);
			ArrayList<Integer> toSlots = (ArrayList<Integer>) selectedSlots.get(1);
			if (fromSlots.size() > 0) {
				for (int i = 0; i < fromSlots.size(); i++) {
					// ok = ctrl.putMaterialOntoRack(ip, rackID, "" + toSlots.get(i), "" + fromSlots.get(i), "MORI_DR5A");
					ArrayList<Object> rtn = ctrl.pickAndPlace(ip, "robot_pallet_a_slot_" + fromSlots.get(i), "input_rack_b_slot_" + toSlots.get(i));
					ok = (boolean) rtn.get(0);
					if (!ok)
						break;
				}
			}
		}

		return ok;
	}

	private boolean robotGoCNC() {
		boolean ok = false;
		IRobot ctrl = getRobotControl();

		if (null != ctrl) {
			String ip = comboBoxRobotIP.getSelectedItem().toString();
			// String machineIp = comboBoxIP.getSelectedItem().toString();
			// ok = ctrl.moveToRack(ip, rackID, "MORI_DR5A");
			ok = ctrl.dockTo(ip, "CNC79Marker ");
		}

		return ok;
	}

	@SuppressWarnings("unchecked")
	private boolean robotLoading() {
		boolean ok = false;
		IRobot ctrl = getRobotControl();

		if (null != ctrl) {
			String ip = comboBoxRobotIP.getSelectedItem().toString();
			String machineIp = comboBoxIP.getSelectedItem().toString();
			clearCncWorkzone(machineIp);
			ArrayList<Object> selectedSlots = getSelectedSlots(MaterialsIOType.ROBOTTOMACHINE);
			ArrayList<Integer> fromSlots = (ArrayList<Integer>) selectedSlots.get(0);
			ArrayList<Integer> toSlots = (ArrayList<Integer>) selectedSlots.get(1);
			if (fromSlots.size() > 0) {
				for (int i = 0; i < fromSlots.size(); i++) {
					// ok = ctrl.putMaterialOntoMachine(ip, machineIp, toSlots.get(i), "" + fromSlots.get(i), "MORI_DR5A");
					ArrayList<Object> rtn = ctrl.pickAndPlace(ip, "robot_pallet_a_slot_" + fromSlots.get(i), "cnc_79_slot_" + toSlots.get(i));//change by Hui Zhi
					ok = (boolean) rtn.get(0);
					if (!ok)
						break;
				}
			}
		}

		return ok;
	}

	@SuppressWarnings("unchecked")
	private boolean robotUnloading() {
		boolean ok = false;
		IRobot ctrl = getRobotControl();

		if (null != ctrl) {
			String ip = comboBoxRobotIP.getSelectedItem().toString();
//			String machineIp = comboBoxIP.getSelectedItem().toString();
			ArrayList<Object> selectedSlots = getSelectedSlots(MaterialsIOType.MACHINETOROBOT);
			ArrayList<Integer> fromSlots = (ArrayList<Integer>) selectedSlots.get(0);
			ArrayList<Integer> toSlots = (ArrayList<Integer>) selectedSlots.get(1);
			if (fromSlots.size() > 0) {
				for (int i = fromSlots.size() - 1 ; i >= 0; i--) {
					// ok = ctrl.pickMaterialFromMachine(ip, machineIp, fromSlots.get(i), "" + toSlots.get(i),"MORI_DR5A");
					ArrayList<Object> rtn = ctrl.pickAndPlace(ip, "cnc_79_slot_" + fromSlots.get(i), "robot_pallet_a_slot_" + toSlots.get(i));//change by Hui Zhi
					ok = (boolean) rtn.get(0);
					if (!ok)
						break;
				}
			}
		}
		return ok;
	}

	private String setMachiningCodePrefix() {
		SystemConfig sysCfg = SystemConfig.getInstance();
		LinkedHashMap<String, Object> config = sysCfg.getCommonCfg();
		String ncProgramPrefix = (String) config.get("MachiningScriptPrefix");
		ncProgramPrefix = ncProgramPrefix.substring(0, ncProgramPrefix.length() - 1);

		if (chckbxCheckbox_2.isSelected()) {
			ncProgramPrefix = ncProgramPrefix + "6";
		} else {
			ncProgramPrefix = ncProgramPrefix + "3";
		}
		UploadNCProgram.setMachiningCodePrefix(ncProgramPrefix);

		return (ncProgramPrefix + "/" + config.get("CleaningScriptPrefix") + "/" + config.get("CleaningTimes"));
	}

	private void setLULSlots() {
		SystemConfig sysCfg = SystemConfig.getInstance();
		LinkedHashMap<String, Object> config = sysCfg.getCommonCfg();
		String machiningCodePrefix = (String) config.get("MachiningScriptPrefix");
		String slots = "3";
		if (machiningCodePrefix.endsWith("6"))
			slots = "6";
		if (slots.equals("6")) {
			if (!chckbxCheckbox_2.isSelected())
				chckbxCheckbox_2.setSelected(true);
			if (chckbxCheckbox_0.isSelected())
				chckbxCheckbox_0.setSelected(false);
			if (chckbxCheckbox_1.isSelected())
				chckbxCheckbox_1.setSelected(false);
			chckbxCheckbox_2.setEnabled(true);
			chckbxCheckbox_0.setEnabled(false);
			chckbxCheckbox_1.setEnabled(false);
		} else {
			if (!chckbxCheckbox_0.isSelected() && !chckbxCheckbox_1.isSelected())
				chckbxCheckbox_1.setSelected(true);
			if (chckbxCheckbox_2.isSelected())
				chckbxCheckbox_2.setSelected(false);
			chckbxCheckbox_0.setEnabled(true);
			chckbxCheckbox_1.setEnabled(true);
			chckbxCheckbox_2.setEnabled(false);
		}
	}

	private ArrayList<Object> getSelectedSlots(MaterialsIOType ioType) {
		ArrayList<Object> slots = new ArrayList<Object>();
		ArrayList<Integer> fromSlots = new ArrayList<Integer>();
		ArrayList<Integer> toSlots = new ArrayList<Integer>();

		if (chckbxCheckbox_0.isSelected()) {
			fromSlots.add(1);
			fromSlots.add(2);
			fromSlots.add(3);
			toSlots.add(1);
			toSlots.add(2);
			toSlots.add(3);
			switch (ioType) {
			case HTTOROBOT:
				break;
			case ROBOTTOHT:
				toSlots.clear();
				toSlots.add(1);
				toSlots.add(2);
				toSlots.add(3);
				break;
			case MACHINETOROBOT:
				break;
			case ROBOTTOMACHINE:
				break;
			}
		} else if (chckbxCheckbox_1.isSelected()) {
			fromSlots.add(1);
			fromSlots.add(2);
			fromSlots.add(3);
			toSlots.add(1);
			toSlots.add(2);
			toSlots.add(3);
			switch (ioType) {
			case HTTOROBOT:
				fromSlots.clear();
				fromSlots.add(1);
				fromSlots.add(2);
				fromSlots.add(3);
				break;
			case ROBOTTOHT:
				break;
			case MACHINETOROBOT:
				break;
			case ROBOTTOMACHINE:
				break;
			}
		} else if (chckbxCheckbox_2.isSelected()) {
			fromSlots.add(1);
			fromSlots.add(2);
			fromSlots.add(3);
			fromSlots.add(4);
			fromSlots.add(5);
			fromSlots.add(6);
			toSlots.add(1);
			toSlots.add(2);
			toSlots.add(3);
			toSlots.add(4);
			toSlots.add(5);
			toSlots.add(6);
		}

		slots.add(0, fromSlots);
		slots.add(1, toSlots);
		return slots;
	}

	@SuppressWarnings("unchecked")
	private String getWorkpieceIDs() {
		String ids = "";

		ArrayList<Object> selectedSlots = getSelectedSlots(MaterialsIOType.ROBOTTOMACHINE);
		ArrayList<Integer> toSlots = (ArrayList<Integer>) selectedSlots.get(1);
		if (toSlots.size() > 0) {
			for (int i = 0; i < toSlots.size(); i++) {
				if ("".equals(ids)) {
					ids = UploadNCProgram.getMachiningCodePrefix() + toSlots.get(i);
				} else {
					ids += ";" + UploadNCProgram.getMachiningCodePrefix() + toSlots.get(i);
				}
			}
		}

		return ids;
	}

	/**
	 * 
	 * @param splitor
	 *            The splitor of slots and workpiece IDs
	 * @return String[0]: Loading-from slots of Home Table | String[1]
	 *         Unloading-to slots of Home Table | String[2] Workpiece IDs
	 */
	@SuppressWarnings("unchecked")
	private String[] getSlotAndWorkpieceIDs(String splitor) {
		String[] ids = new String[] { "", "", "" };

		ArrayList<Object> selectedSlots = getSelectedSlots(MaterialsIOType.HTTOROBOT);
		ArrayList<Integer> fromSlots = (ArrayList<Integer>) selectedSlots.get(0);
		ArrayList<Integer> toSlots = (ArrayList<Integer>) selectedSlots.get(1);
		if (toSlots.size() > 0) {
			for (int i = 0; i < toSlots.size(); i++) {
				if ("".equals(ids[0])) {
					ids[0] = "" + fromSlots.get(i);
					ids[1] = "" + toSlots.get(i);
					ids[2] = UploadNCProgram.getMachiningCodePrefix() + (i + 1);
				} else {
					ids[0] += splitor + fromSlots.get(i);
					ids[1] += splitor + toSlots.get(i);
					ids[2] += splitor + UploadNCProgram.getMachiningCodePrefix() + (i + 1);
				}
			}
		}

		return ids;
	}

	private boolean cncIsWorking() {
		boolean isWorking = false;

		String ip = comboBoxIP.getSelectedItem().toString();
		String state = "" + cncData.getCncLastState(ip);
		if (state.startsWith("LOCK"))
			isWorking = true;

		return isWorking;
	}

	private boolean cncDoorOpened() {
		boolean opened = true;

		String deviceIP = comboBoxIP.getSelectedItem().toString();
		if (!"1".equals(cncData.getItemVal(deviceIP, CncItems.OP_OPENDOOR)))
			opened = false;

		return opened;
	}

	private String getCncTagName() {
		String deviceIP = comboBoxIP.getSelectedItem().toString();
		String tagName = "" + cncData.getData(deviceIP).get(CncItems.TAGNAME);
		return tagName;
	}

	private String getCncState() {
		String state = "SHUTDOWN";

		CNC cncCtrl = getCncControl();
		if (null != cncCtrl) {
			String ip = comboBoxIP.getSelectedItem().toString();
			state = "" + cncCtrl.getMachineState(ip);
		}

		return state;
	}

	private void showCncState(String state) {
		lblCncStt.setText(state + " / " + getCncTagName());
	}

	private void showRobotBattery(String battery) {
		lblRobotBattery.setText(battery);
	}

	private void showRobotPosition(String pos) {
		lblRobotStt.setText(pos);
	}

	@SuppressWarnings("unchecked")
	private String lockFixtures() {
		String errMsg = "";
		CNC cncCtrl = getCncControl();

		if (null != cncCtrl) {
			String ip = comboBoxIP.getSelectedItem().toString();
			ArrayList<Object> selectedSlots = getSelectedSlots(MaterialsIOType.ROBOTTOMACHINE);
			ArrayList<Integer> toSlots = (ArrayList<Integer>) selectedSlots.get(1);
			if (toSlots.size() > 0) {	
				for (int i = 0; i < toSlots.size(); i++) {
					if (cncCtrl.clampFixture(ip, toSlots.get(i))) {
						lockFlags[toSlots.get(i) - 1] = true;
					} else {
						lockFlags[toSlots.get(i) - 1] = false;
						errMsg += "\r\nLock fixture#" + (toSlots.get(i)) + " failed";
					}
				}
			} else {
				errMsg = "No slots are selected";
			}
		} else {
			errMsg = "Get controller failed";
		}

		return errMsg;
	}

	private String openDoor() {
		String errMsg = "";
		CNC cncCtrl = getCncControl();
		if (null != cncCtrl) {
			String ip = comboBoxIP.getSelectedItem().toString();
			CncData cncData = CncData.getInstance();
			String lineName = cncData.getLineName(ip);
			TaskProcessor taskProc = TaskProcessor.getInstance(lineName);

			if (cncCtrl.openDoor(ip)) {
				doorClosedFlag = false;
			} else {
				if ("".equals(taskProc.openDoorEx(cncCtrl, ip))) {
					doorClosedFlag = false;
				} else {
					doorClosedFlag = true;
					errMsg = "Open door failed";
				}
			}
		} else {
			errMsg = "Get controller failed";
		}
		return errMsg;
	}

	private String closeDoor() {
		String errMsg = "";
		CNC cncCtrl = getCncControl();
		if (null != cncCtrl) {
			String ip = comboBoxIP.getSelectedItem().toString();
			CncData cncData = CncData.getInstance();
			String lineName = cncData.getLineName(ip);
			TaskProcessor taskProc = TaskProcessor.getInstance(lineName);

			if (cncCtrl.closeDoor(ip)) {
				doorClosedFlag = true;
			} else {
				if ("".equals(taskProc.closeDoorEx(cncCtrl, ip))) {
					doorClosedFlag = true;
				} else {
					doorClosedFlag = false;
					errMsg = "Close door failed";
				}
			}
		} else {
			errMsg = "Get controller failed";
		}
		return errMsg;
	}

	@SuppressWarnings("unchecked")
	private String uploadNCProgram() {
		ArrayList<Object> selectedSlots = getSelectedSlots(MaterialsIOType.ROBOTTOMACHINE);
		ArrayList<Integer> toSlots = (ArrayList<Integer>) selectedSlots.get(1);
		if (toSlots.isEmpty()) {
			return "No slots are selected";
		}

		CncData cncData = CncData.getInstance();
		CncDriver cncDriver = CncDriver.getInstance();
		WorkpieceData wpData = WorkpieceData.getInstance();

		// Set NC Scripts Code
		setMachiningCodePrefix();

		// sequence of the workpieceIDs must be from 1 to 6
		String workpieceIDs = getWorkpieceIDs();
		String cncIP = comboBoxIP.getSelectedItem().toString();
		String cncModel = (String) cncData.getData(cncIP).get(CncItems.MODEL);
		String lineName = (String) cncData.getLineName(cncIP);

		// Can't be missed - memory initialization
		cncData.clearWorkpieceID(cncIP);
		wpData.clearData();
		for (int i = 0; i < ncUploadFlags.length; i++) {
			ncUploadFlags[i] = false;
		}

		String[] ids = workpieceIDs.split(";");
		String errMsg = "", zones = "";
		for (int i = 0; i < ids.length; i++) {
			if ("".equals(ids[i]))
				continue;
			wpData.setData(ids[i], WorkpieceItems.ID, ids[i]);
			DataUtils.updateWorkpieceData(ids[i], lineName, rackID, "" + toSlots.get(i));
			if (wpData.canMachineByCNC(ids[i], cncModel, null)) {
				if ("".equals(zones)) {
					workpieceIDs = ids[i];
					zones = "" + toSlots.get(i);
				} else {
					workpieceIDs += ";" + ids[i];
					zones += ";" + toSlots.get(i);
				}
				// Remember the working zone for each workpiece
				cncData.setWorkpieceID(cncIP, toSlots.get(i), ids[i]);
				cncData.setSpecNo(cncIP, toSlots.get(i), wpData.getSpecNo(ids[i]));
				cncData.setZoneSimulationTime(cncIP, toSlots.get(i), wpData.getNextProcSimtime(ids[i], cncModel, null));
			} else {
				errMsg += "\r\n" + ids[i] + "@Zone#" + toSlots.get(i) + " can't be machined by " + cncModel;
			}
		}

		if (!"".equals(errMsg)) {
			if (0 != JOptionPane.showConfirmDialog(contentPanel, errMsg + "\r\nWill you skip these workpieces?",
					"Skip Workpiece", JOptionPane.YES_NO_OPTION)) {
				return errMsg;
			} else {
				errMsg = "";
			}
		}

		if (!"".equals(workpieceIDs)) {
			String cncDrvName = (String) cncDriver.getData(cncModel).get(DriverItems.DRIVER);
			String cncDataHandler = (String) cncDriver.getData(cncModel).get(DriverItems.DATAHANDLER);
			CNC cncCtrl = CncFactory.getInstance(cncDrvName, cncDataHandler, cncModel);

			if (null != cncCtrl) {
				cncData.setData(cncIP, CncItems.CTRL, cncCtrl);
				if (UploadNCProgram.uploadSubPrograms(cncCtrl, cncIP, cncData.getWorkpieceIDs(cncIP))) {
					// disable upload main Program by Hui Zhi Fang 2021.11.3 
					//if (!UploadNCProgram.uploadMainProgram(cncCtrl, cncIP, cncData.getWorkpieceIDs(cncIP))) {
						//errMsg += "\r\nUpload main program failed";
					//} else {
						String[] zns = zones.split(";");
						String[] subProgs = ("" + cncData.getData(cncIP).get(CncItems.SUBPROGRAMS)).split(";");
						int idx = -1;
						for (String zn : zns) {
							idx++;
							ncUploadFlags[Integer.valueOf(zn) - 1] = true;
							cncData.setNCProgram(cncIP, Integer.valueOf(zn), subProgs[idx]);
						}

						// Refresh workpiece data
						ids = workpieceIDs.split(";");
						long simulationT = 0;
						for (int i = 0; i < ids.length; i++) {
							LinkedHashMap<SpecItems, Object> spec = wpData.getNextProcInfo(ids[i]);
							int curProc = wpData.getNextProcNo(ids[i], spec);
							int procTime = wpData.getNextProcSimtime(ids[i], cncModel, spec, curProc);
							simulationT += procTime;

							wpData.setCurrentProcNo(ids[i], curProc);
							wpData.appendData(ids[i], WorkpieceItems.NCMODEL, cncModel);
							wpData.appendData(ids[i], WorkpieceItems.MACHINETIME, "" + procTime);
							wpData.appendData(ids[i], WorkpieceItems.PROCESS,
									wpData.getNextProcName(ids[i], spec, curProc));
							wpData.appendData(ids[i], WorkpieceItems.PROGRAM,
									wpData.getNextProcProgram(ids[i], cncModel, spec, curProc));
							wpData.appendData(ids[i], WorkpieceItems.SURFACE,
									"" + wpData.getNextProcSurface(ids[i], spec, curProc));

							cncData.setExpectedMachiningTime(cncIP, simulationT);
						}
					//}
				} else {
					errMsg += "\r\nUpload sub programs failed";
				}
				if ("".equals(errMsg)) {// Upload NC Program OK
					cncData.setData(cncIP, CncItems.CTRL, cncCtrl);
				} else {
					cncData.setData(cncIP, CncItems.CTRL, null);
				}
			} else {
				errMsg += "\r\nGet CNC driver failed";
			}
		} else {
			errMsg += "\r\nAll workpieces can't be machined by " + cncModel;
		}

		return errMsg;
	}

	@SuppressWarnings("unchecked")
	private String unlockFixtures() {
		String errMsg = "";
		CNC cncCtrl = getCncControl();
		if (null != cncCtrl) {
			String ip = comboBoxIP.getSelectedItem().toString();
			ArrayList<Object> selectedSlots = getSelectedSlots(MaterialsIOType.ROBOTTOMACHINE);
			ArrayList<Integer> toSlots = (ArrayList<Integer>) selectedSlots.get(1);
			if (toSlots.size() > 0) {
				for (int i = 0; i < toSlots.size(); i++) {
					if (cncCtrl.releaseFixture(ip, toSlots.get(i))) {
						lockFlags[toSlots.get(i) - 1] = false;
					} else {
						errMsg += "\r\n" + "Unlock fixture#" + toSlots.get(i) + " failed";
					}
				}
			}
		} else {
			errMsg = "Get controller failed";
		}
		return errMsg;
	}

	private String doCleaning(String cncIP, CNC cncCtrl) {
		CncData cncData = CncData.getInstance();
		String lineName = cncData.getLineName(cncIP);

		TaskProcessor taskProc = TaskProcessor.getInstance(lineName);
		SystemConfig sysCfg = SystemConfig.getInstance();
		LinkedHashMap<String, Object> config = sysCfg.getCommonCfg();
		String cleaningTimes = (String) config.get("CleaningTimes");
		int cleaningQty = Integer.parseInt(cleaningTimes);
		String errMsg = "";

		int[] wkZones = new int[cleaningQty];
		String[] codeIDs = new String[cleaningQty];
		for (int i = 0; i < cleaningQty; i++) {
			wkZones[i] = i + 1;
			codeIDs[i] = UploadNCProgram.getCleaningCodePrefix() + wkZones[i];
		}
		if (!"".equals(taskProc.doCleaning(cncCtrl, cncIP, wkZones, codeIDs, progressBar))) {
			errMsg = "运行机床清洁程序失败";
		}

		return errMsg;
	}

	private String doMachining() {
		String errMsg = "";
		CNC cncCtrl = getCncControl();
		if (null != cncCtrl) {
			String ip = comboBoxIP.getSelectedItem().toString();
			CncWebAPI cncWebAPI = CncWebAPI.getInstance();
			CncData cncData = CncData.getInstance();
			String cncModel = cncData.getCncModel(ip);
			String mainProgramName = cncWebAPI.getMainProgramName(cncModel);
			String cncState = getCncState();
			if (!cncState.contains("" + DeviceState.STANDBY)) {
				if (cncState.contains("" + DeviceState.FINISH)) {
					if (0 != JOptionPane.showConfirmDialog(contentPanel, "机床当前状态为FINISH并非正常就绪状态，确定继续执行加工程序吗?",
							"执行加工程序?", JOptionPane.YES_NO_OPTION)) {
						return "Machine state is " + cncState + ", not ready yet for machining";
					}
				} else {
					return "Machine state is " + cncState + ", not ready yet for machining";
				}
			}
			cncData.setData(ip, CncItems.STATE, DeviceState.STANDBY);

			String zns = getNotBlankZones();
			if ("".equals(zns))
				return "No valid workpieces in the machine";

			String[] zones = zns.split(",");
			boolean bNeedUploadNcProgram = false;
			for (String zone : zones) {
				if (!lockFlags[Integer.valueOf(zone) - 1]) {
					if (!lockFlags[Integer.valueOf(zone) - 1]) {
						errMsg = lockSingleFixture(Integer.valueOf(zone));
						if (!"".equals(errMsg))
							return errMsg;
						lockFlags[Integer.valueOf(zone) - 1] = true;
					}
				}
				if (!ncUploadFlags[Integer.valueOf(zone) - 1])
					bNeedUploadNcProgram = true;
			}
			if (bNeedUploadNcProgram) {
				errMsg = uploadNCProgram();
				if (!"".equals(errMsg))
					return errMsg;
			}

			if (0 != JOptionPane.showConfirmDialog(contentPanel, "确定“所有工件已经锁紧并且已经上传正确的加工程序”吗?", "执行加工程序?",
					JOptionPane.YES_NO_OPTION)) {
				return "Please check and start again once everything is ready";
			}

			if (!doorClosedFlag) {
				errMsg = closeDoor();
				if (!"".equals(errMsg))
					return errMsg;
			}

			if (startMachining(cncCtrl, ip, mainProgramName, 3, 10)) {
				setMachiningStatus("执行加工程序");
				setMachiningProgress(0);
				cncData.setCncLastState(ip, DeviceState.LOCK);
				cncData.setData(ip, CncItems.STATE, DeviceState.WORKING);
				cncData.setData(ip, CncItems.DT_DATE, TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
				showCncState(getCncState());

				DeviceMonitor devMonitor = DeviceMonitor.getInstance();
				if (!devMonitor.cncMachiningIsDone(cncCtrl, ip, cncData.getExpectedMachiningTime(ip), progressBar)) {
					errMsg = "机床加工失败：可能原因是机床报警了";
				} else {
					setMachiningStatus("执行清洁程序");
					errMsg = doCleaning(ip, cncCtrl);
					if ("".equals(errMsg)) {
						cncData.setCncLastState(ip, DeviceState.FINISH);
						setMachiningStatus("加工任务完成");
					}
				}
				cncData.clearMachiningData(ip);
				resetRunningFlags();
				showCncState(getCncState());
			} else {
				errMsg = "Start machining failed";
			}
		} else {
			errMsg = "Get controller failed";
		}
		return errMsg;
	}

	private boolean startMachining(CNC cncCtrl, String cncIP, String programName, int retryTimes, int timeout_s) {
		boolean ok = false;

		long startT, diffTime, timeout_ms;
		DeviceState cncState = DeviceState.STANDBY;

		timeout_ms = timeout_s * 1000;
		for (int i = 0; i < retryTimes; i++) {
			if (cncCtrl.startMachining(cncIP, programName)) {
				startT = System.currentTimeMillis();
				while (true) {
					diffTime = System.currentTimeMillis() - startT;
					cncState = cncCtrl.getMachineState(cncIP);
					if (DeviceState.WORKING == cncState) {
						ok = true;
						break;
					} else {
						try {
							Thread.sleep(1000);
							RunningMsg.set("Start machining...");
						} catch (InterruptedException e) {
						}
					}
					if (diffTime > timeout_ms)
						break;
				}
			}
			if (ok)
				break;
		}

		return ok;
	}

	private String lockSingleFixture(int zoneNo) {
		String errMsg = "";
		if (zoneNo < 1 || zoneNo > 6) {
			errMsg = "Invalid Zone#" + zoneNo;
		} else {
			CNC cncCtrl = getCncControl();
			if (null != cncCtrl) {
				String ip = comboBoxIP.getSelectedItem().toString();
				if (cncCtrl.clampFixture(ip, zoneNo)) {
					lockFlags[zoneNo - 1] = true;
				} else {
					lockFlags[zoneNo - 1] = false;
					errMsg = "Lock fixture#" + zoneNo + " failed";
				}
			} else {
				errMsg = "Get controller failed";
			}
		}
		return errMsg;
	}

	private void setMachiningProgress(int val) {
		if (null != progressBar) {
			progressBar.setValue(val);
			progressBar.repaint();
		}
	}

	private void resetRunningFlags() {
		for (int i = 0; i < ncUploadFlags.length; i++) {
			ncUploadFlags[i] = false;
		}
	}

	private void setMachiningStatus(String status) {
		lblMachiningStatus.setText(status);
	}

	private void clearCncWorkzone(String cncIP) {
		CncData cncData = CncData.getInstance();
		cncData.clearWorkpieceID(cncIP);
	}

	private void clearRobotTray(String robotIP) {
		RobotData robotData = RobotData.getInstance();
		robotData.clearAllSlots(robotIP);
	}

	/**
	 * @param String
	 *            robotIP IP address of the robot
	 * @param String
	 *            cncIP IP address of the machine
	 * @param int
	 *            rackID ID of the materials rack
	 * @param String
	 *            inputRackSlots Slot's ID must be separated by comma
	 * @param String
	 *            outputRackSlots Slot's ID must be separated by comma
	 * @param String
	 *            wkpIDs Workpiece's ID must be separated by comma
	 */
	private String taskOneCycle(String robotIP, String cncIP, String rackID, String inputRackSlots,
			String outputRackSlots, String wkpIDs) {
		String errMsg = "", cncModel = "", robotModel = "";
		int stepCounter = 0;
		CNC cncCtrl = null;

		setMachiningStatus("加工任务开始");
		WorkpieceData wpData = WorkpieceData.getInstance();
		CncData cncData = CncData.getInstance();
		RobotData robotData = RobotData.getInstance();
		CncDriver cncDriver = CncDriver.getInstance();
		RobotDriver robotDriver = RobotDriver.getInstance();
		UploadNCProgram uploadNCProgram = UploadNCProgram.getInstance();
		String lineName = cncData.getLineName(cncIP);
		
		TaskProcessor taskProc = TaskProcessor.getInstance(lineName);

		//disabled cleaning 2022/1/8 Hui Zhi
//		SystemConfig sysCfg = SystemConfig.getInstance();
//		LinkedHashMap<String, Object> config = sysCfg.getCommonCfg();
//		String cleaningTimes = (String) config.get("CleaningTimes");
//		int cleaningQty = Integer.parseInt(cleaningTimes); 

		try {
			cncModel = (String) cncData.getData(cncIP).get(CncItems.MODEL);
			robotModel = (String) robotData.getData(robotIP).get(RobotItems.MODEL);
			String cncDrvName = (String) cncDriver.getData(cncModel).get(DriverItems.DRIVER);
			String cncDataHandler = (String) cncDriver.getData(cncModel).get(DriverItems.DATAHANDLER);
			String robotDrvName = (String) robotDriver.getData(robotModel).get(DriverItems.DRIVER);

			cncCtrl = CncFactory.getInstance(cncDrvName, cncDataHandler, cncModel);
			cncData.setData(cncIP, CncItems.CTRL, cncCtrl);
			
			// Disable Robot instance and use IRobot instance.  2021.11.3 Hui Zhi
			//Robot robotCtrl = RobotFactory.getInstance(robotDrvName);
			IRobot robotCtrl = IRobotFactory.getInstance(robotDrvName);
			
			clearCncWorkzone(cncIP);
			clearRobotTray(robotIP);

			setMachiningStatus("检查机床状态");
			DeviceState cncState = cncCtrl.getMachineState(cncIP);
			setMachiningStatus("机床状态" + cncState);
			if (DeviceState.STANDBY != cncState) {
				errMsg = "机床尚未就绪(" + cncState + ")\r\n请检查机床驱动程序是否已启动";
				return errMsg;
			}

			RunningMsg.set("Task Handling Started - " + robotIP + "->" + cncIP);
			String logFile = LogUtils.getOperationLogFileName(cncIP, cncModel);
			String logContPre = lineName + LogUtils.separator + robotIP + "->" + cncIP + LogUtils.separator;

			// Step 1: CNC opens door
			setMachiningStatus("机床开门");
			RunningMsg.set("CNC door opening");
			if (taskProc.runConcurrentOP(DeviceOP.CNC_OPENDOOR, cncIP, robotIP, null, cncCtrl, robotCtrl, 1, 30)) {
				LogUtils.operationLog(logFile, logContPre + "ConcurrentOP CNC_OPENDOOR started");
			} else {
				errMsg = "机床【开门】失败(ConcurrentOP)";
				LogUtils.operationLog(logFile, logContPre + errMsg);
				return errMsg;
			}
			stepCounter++;

			// Step 2: Robot gets materials from rack
			setMachiningStatus("机器人自上料");
			RunningMsg.set("Robot " + robotModel + "/" + robotIP + " gets materials from Rack " + rackID);
			LogUtils.operationLog(logFile, logContPre + "Robot gets materials from rack " + rackID + " started");
			LinkedHashMap map = new LinkedHashMap();
			if (!taskProc.getMaterialFromRack(robotCtrl, robotIP, rackID, inputRackSlots.split(","), wkpIDs.split(","),
					logFile, logContPre, cncModel,cncCtrl,cncIP,map,cncState)) {
				errMsg = "【机器人自上料】失败";
				LogUtils.operationLog(logFile, logContPre + errMsg);
				return errMsg;
			}
			LogUtils.operationLog(logFile, logContPre + "Robot gets materials from rack done");
			stepCounter++;

			// Step 3: Check whether Machine has opened the door or not
			setMachiningStatus("机床开门");
			LogUtils.operationLog(logFile, logContPre + "Machine opens door(status checking)");
			if (!taskProc.concurrentOPDone(cncIP, robotIP, DeviceOP.CNC_OPENDOOR, CONCURR_OP_TIMEOUT)) {
				if (!"".equals(taskProc.openDoorEx(cncCtrl, cncIP))) {
					errMsg = "机床【开门】失败(concurrentOP timeout)";
					LogUtils.operationLog(logFile, logContPre + errMsg);
					return errMsg;
				}
			}
			setMachiningStatus("机床开门OK");
			LogUtils.operationLog(logFile, logContPre + "Machine opens door done");
			stepCounter++;

			// Refresh machining information - to trace workpiece's status
			if (DeviceState.STANDBY == cncState) {
				String[] ids = wkpIDs.split(",");
				long simulationT = 0;
				for (int i = 0; i < ids.length; i++) {
					LinkedHashMap<SpecItems, Object> spec = wpData.getNextProcInfo(ids[i]);
					int curProc = wpData.getNextProcNo(ids[i], spec);
					int procTime = wpData.getNextProcSimtime(ids[i], cncModel, spec, curProc);
					simulationT += procTime;

					wpData.setCurrentProcNo(ids[i], curProc);
					wpData.appendData(ids[i], WorkpieceItems.NCMODEL, cncModel);
					wpData.appendData(ids[i], WorkpieceItems.MACHINETIME, "" + procTime);
					wpData.appendData(ids[i], WorkpieceItems.PROCESS, wpData.getNextProcName(ids[i], spec, curProc));
					wpData.appendData(ids[i], WorkpieceItems.PROGRAM,
							wpData.getNextProcProgram(ids[i], cncModel, spec, curProc));
					wpData.appendData(ids[i], WorkpieceItems.SURFACE,
							"" + wpData.getNextProcSurface(ids[i], spec, curProc));

					cncData.setExpectedMachiningTime(cncIP, simulationT);
				}
			}

			// Step 4: Robot moves to machine
			setMachiningStatus("机器人移向机床");
			RunningMsg.set("Robot moves to " + cncIP);
			LogUtils.operationLog(logFile, logContPre + "Robot moves to machine");
			//if (!robotCtrl.moveToMachine(robotIP, cncIP, cncModel)) {
			if (!robotCtrl.dockTo(robotIP, "cnc01")) {	// use the  "dock to cnc01" command 2021.11.3 Hui Zhi Fang
				errMsg = "机器人【移至机床上下料位置】失败";
				LogUtils.operationLog(logFile, logContPre + errMsg);
				return errMsg;
			} else {
				showRobotBattery(robotGetBattery());
				showRobotPosition(robotGetPosition());
			}
			robotData.setData(robotIP, RobotItems.STATE, DeviceState.WORKING);
			stepCounter++;

			// Step 5: Robot loads materials onto machine
			setMachiningStatus("给机床上料");
			RunningMsg.set("Robot loads material onto " + cncIP + " started");
			if (!taskProc.loadMaterialOntoMachine(robotCtrl, robotIP,cncCtrl, cncIP, logFile, logContPre)) {
				errMsg = "机器人【给机床上料】失败";
				LogUtils.operationLog(logFile, logContPre + errMsg);
				return errMsg;
			}
			LogUtils.operationLog(logFile, logContPre + "Robot loads materials onto machine done");
			stepCounter++;

			// Step 6: Create sub programs uploading thread
			setMachiningStatus("上传加工子程序");
			uploadNCProgram.uploadSubProgramsThread(cncCtrl, cncIP);
			stepCounter++;

			// Step 7: Robot moves to home table
			setMachiningStatus("机器人移向充电位");
			RunningMsg.set("Robot moves to Rack " + rackID);
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("rackID", rackID);
			LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVETORACK starts");
			if (taskProc.runConcurrentOP(DeviceOP.ROBOT_MOVETORACK, cncIP, robotIP, jsonObj, cncCtrl, robotCtrl, 1,
					30)) {
				LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVETORACK " + rackID + " done");
			} else {
				errMsg = "机器人【移至充电位置】失败(ConcurrentOP)";
				LogUtils.operationLog(logFile, logContPre + errMsg);
				return errMsg;
			}
			stepCounter++;

			// Step 8: Clamp all fixtures
			setMachiningStatus("锁紧工件");
			LinkedHashMap<Integer, String> wpIDs = cncData.getWorkpieceIDs(cncIP);
			String workpieceIDs = "";
			RunningMsg.set("Machine " + cncIP + " clamps fixtures");
			for (int wkZone : wpIDs.keySet()) {
				if ("".equals(workpieceIDs)) {
					workpieceIDs = wpIDs.get(wkZone);
				} else {
					workpieceIDs += ";" + wpIDs.get(wkZone);
				}

				if (!cncCtrl.clampFixture(cncIP, wkZone)) {
					errMsg = "机床【锁紧工件】 " + wkZone + "/" + wpIDs.get(wkZone) + "失败";
					LogUtils.operationLog(logFile, logContPre + errMsg);
					return errMsg;
				} else {
					wpData.setData(wpIDs.get(wkZone), WorkpieceItems.STATE, DeviceState.WORKING);
					LogUtils.operationLog(logFile,
							logContPre + "Machine clamps fixture " + wkZone + "/" + wpIDs.get(wkZone) + " done");
				}
			}
			LogUtils.operationLog(logFile, logContPre + "Machine clamps fixtures done");
			stepCounter++;
			
			//disable upload main program by Hui Zhi 2021/11/12
			// Step 9: Create Main Program uploading thread
//			setMachiningStatus("上传主程序");
//			RunningMsg.set("Machine " + cncIP + " gets main program");
//			Thread uploadProgramThr = uploadNCProgram.uploadMainProgramThread(cncCtrl, cncIP);
//			uploadProgramThr.join();
//			setMachiningStatus("检查加工程序");
//			if (cncData.subProgramOK(cncIP) && cncData.mainProgramOK(cncIP)) {
//				String mainProgramName = (String) cncData.getData(cncIP).get(CncItems.MAINPROGRAM);
//				if (!cncCtrl.mainProgramIsActivate(cncIP, mainProgramName)) {
//					errMsg = "主程序（" + mainProgramName + "）尚未激活";
//					LogUtils.operationLog(logFile, logContPre + errMsg);
//					return errMsg;
//				}
//				LogUtils.operationLog(logFile, logContPre + "Machine gets main program done");
//			} else {
//				if (!cncData.subProgramOK(cncIP))
//					LogUtils.operationLog(logFile, logContPre + "Machine gets sub programs failed");
//				if (!cncData.mainProgramOK(cncIP))
//					LogUtils.operationLog(logFile, logContPre + "Machine gets main program failed");
//				errMsg = "给机床【上传加工程序】失败";
//				return errMsg;
//			}
//			stepCounter++;

			// Step 10: CNC closes door
			setMachiningStatus("等待机器人离开");
			RunningMsg.set("Safe check for door closing");
			if (!taskProc.safeToCloseDoor(cncIP, robotIP, 300)) {
				errMsg = "系统检测到机床安全门此时无法安全关闭";
				LogUtils.operationLog(logFile, logContPre + errMsg);
				return errMsg;
			}

			RunningMsg.set("CNC door closing");
			setMachiningStatus("机床关门");
			if (!cncCtrl.closeDoor(cncIP)) {
				if (!"".equals(taskProc.closeDoorEx(cncCtrl, cncIP))) {
					errMsg = "机床【关门】失败";
					LogUtils.operationLog(logFile, logContPre + errMsg);
					return errMsg;
				}
			}
			LogUtils.operationLog(logFile, logContPre + "Machine closes door done");
			stepCounter++;

			// Step 11: CNC starts machining
			setMachiningStatus("运行加工程序");
			RunningMsg.set("CNC starts machining");
			CncWebAPI cncWebAPI = CncWebAPI.getInstance();
			LinkedHashMap<CncWebAPIItems, String> cncWebAPICommonCfg = cncWebAPI.getCommonCfg(cncModel);
			String mainProgram = cncWebAPICommonCfg.get(CncWebAPIItems.NCPROGMAIN);
			if (!startMachining(cncCtrl, cncIP, mainProgram, 3, 10)) {
				errMsg = "机床【执行加工程序】失败";
				LogUtils.operationLog(logFile, logContPre + errMsg);
				return errMsg;
			} else {
				LogUtils.operationLog(logFile, logContPre + "Machine starts machining");
				cncData.setCncLastState(cncIP, DeviceState.LOCK);
				cncData.setData(cncIP, CncItems.STATE, DeviceState.WORKING);
				showCncState(getCncState());
			}
			stepCounter++;

			// Step 12: Waits for machining
			setMachiningStatus("正在执行加工程序");
			DeviceMonitor devMonitor = DeviceMonitor.getInstance();
			if (!devMonitor.cncMachiningIsDone(cncCtrl, cncIP, cncData.getExpectedMachiningTime(cncIP), progressBar)) {
				errMsg = "机床加工失败：可能原因是机床报警了";
				LogUtils.operationLog(logFile, logContPre + errMsg);
				return errMsg;
			}
			stepCounter++;

			// TODO Step 13: Do Cleaning
//			setMachiningStatus("执行清洁程序");
//			RunningMsg.set("CNC 1st cleaning");
//			LogUtils.operationLog(logFile, logContPre + "CNC 1st cleaning started");
//			int[] wkZones = new int[cleaningQty];
//			String[] codeIDs = new String[cleaningQty];
//			for (int i = 0; i < cleaningQty; i++) {
//				wkZones[i] = i + 1;
//				codeIDs[i] = UploadNCProgram.getCleaningCodePrefix() + wkZones[i];
//			}
//			cncData.setCNCTimingData(cncIP, CncItems.DT_CLEANING_START1, System.currentTimeMillis());
//			if (!"".equals(taskProc.doCleaning(cncCtrl, cncIP, wkZones, codeIDs, progressBar))) {
//				errMsg = "运行机床清洁程序失败";
//				LogUtils.operationLog(logFile, logContPre + errMsg);
//				return errMsg;
//			}
//			cncData.setCNCTimingData(cncIP, CncItems.DT_CLEANING_STOP1, System.currentTimeMillis());
//			robotData.setData(robotIP, RobotItems.OP_STOPPNPTESTING, "1");
//			LogUtils.operationLog(logFile, logContPre + "CNC 1st cleaning done");
//			stepCounter++;

			// Step 14: Release the fixtures
			setMachiningStatus("解锁工件");
			RunningMsg.set("Machine releases fixtures");
			wpIDs = cncData.getWorkpieceIDs(cncIP);
			if (wpIDs.isEmpty()) {
				errMsg = "【解锁工件】失败(no materials)";
				LogUtils.operationLog(logFile, logContPre + errMsg);
				return errMsg;
			}

			String workZones = "", wpID = "", ids = "";
			for (int workZone : wpIDs.keySet()) {
				wpID = wpIDs.get(workZone);
				RunningMsg.set("Machine release fixture " + workZone);
				if (!cncCtrl.releaseFixture(cncIP, workZone)) {
					errMsg = "【解锁工件】 " + workZone + "/" + wpID + "失败";
					LogUtils.operationLog(logFile, logContPre + errMsg);
					return errMsg;
				}
				LogUtils.operationLog(logFile,
						logContPre + "Machine releases fixture " + workZone + "/" + wpID + " done");

				if ("".equals(workZones)) {
					workZones = "" + workZone;
					ids = wpID;
				} else {
					workZones += "," + workZone;
					ids += "," + wpID;
				}
			}
			stepCounter++;

			// Step 15: Machine opens door
			RunningMsg.set("CNC door opening");
			setMachiningStatus("机床开门");
			if (!cncCtrl.openDoor(cncIP)) {
				if (!"".equals(taskProc.openDoorEx(cncCtrl, cncIP))) {
					errMsg = "机床【开门】失败";
					LogUtils.operationLog(logFile, logContPre + errMsg);
					return errMsg;
				}
			}
			LogUtils.operationLog(logFile, logContPre + "Machine opens door done");
			stepCounter++;

			// Step 16: Robot moves to machine
			setMachiningStatus("机器人移向机床");
			RunningMsg.set("Robot moves to machine " + cncIP);
			LogUtils.operationLog(logFile, logContPre + "Robot moves to machine");
			if (robotCtrl.dockTo(robotIP, "cnc01")) {	// use the  "dock to cnc01" command 2021.11.3 Hui Zhi Fang
//			if (robotCtrl.moveToMachine(robotIP, cncIP, cncModel)) {
				showRobotBattery(robotGetBattery());
				showRobotPosition(robotGetPosition());
				LogUtils.operationLog(logFile, logContPre + "Robot moves to machine done");
			} else {
				errMsg = "机器人【移至机床上下料位置】失败";
				LogUtils.operationLog(logFile, logContPre + errMsg);
				return errMsg;
			}
			robotData.setData(robotIP, RobotItems.STATE, DeviceState.WORKING);
			stepCounter++;

			// Step 17: Robot unloads materials from CNC
			setMachiningStatus("给机床下料");
			RunningMsg.set("Robot unloads materials from machine " + cncIP);
			boolean bOK = taskProc.unloadMaterialFromMachine(robotCtrl, robotIP, cncCtrl, cncIP, workZones.split(","),
					ids.split(","), logFile, logContPre);
			if (!bOK) {
				errMsg = "机器人【给机床下料】失败";
				LogUtils.operationLog(logFile, logContPre + errMsg);
				return errMsg;
			}
			cncData.setData(cncIP, CncItems.DT_FINISHUNLOADING, System.currentTimeMillis());
			cncData.setData(cncIP, CncItems.FINISHSHOW, "");
			cncData.clearWorkpieceID(cncIP);// Clear the workpieces in CNC
			LogUtils.operationLog(logFile, logContPre + "Robot unloads materials from machine done");
			stepCounter++;

			// Step 18: Robot moves to Rack
			setMachiningStatus("机器人移向充电位");
			RunningMsg.set("Robot moves to Rack " + rackID);
			jsonObj = new JSONObject();
			jsonObj.put("rackID", rackID);
			LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVETORACK starts");
			if (taskProc.runConcurrentOP(DeviceOP.ROBOT_MOVETORACK, cncIP, robotIP, jsonObj, cncCtrl, robotCtrl, 1,
					30)) {
				LogUtils.operationLog(logFile, logContPre + "ConcurrentOP ROBOT_MOVETORACK done");
			} else {
				errMsg = "机器人【移至充电位置】失败(ConcurrentOP)";
				LogUtils.operationLog(logFile, logContPre + errMsg);
				return errMsg;
			}
			stepCounter++;

			// TODO Step 19: Machine closes door and do cleaning
//			RunningMsg.set("CNC Door closing");
//			if (taskProc.safeToCloseDoor(cncIP, robotIP, robotCtrl, 300)) {
//				setMachiningStatus("机床关门");
//				LogUtils.operationLog(logFile, logContPre + "CNC 2nd cleaning started");
//				RunningMsg.set("CNC cleaning");
//				String zs = "", cids = "";
//				for (int i = 0; i < cleaningQty; i++) {
//					if (0 == i) {
//						zs = "" + (i + 1);
//						cids = "" + UploadNCProgram.getCleaningCodePrefix() + (i + 1);
//					} else {
//						zs = ";" + (i + 1);
//						cids = ";" + UploadNCProgram.getCleaningCodePrefix() + (i + 1);
//					}
//				}
//				jsonObj.clear();
//				jsonObj.put("cncIP", cncIP);
//				jsonObj.put("workZones", zs);
//				jsonObj.put("codeIDs", cids);
//				LogUtils.operationLog(logFile, logContPre + "ConcurrentOP CNC_CLEANING starts");
//				if (taskProc.runConcurrentOP(DeviceOP.CNC_CLEANING, cncIP, robotIP, jsonObj, cncCtrl, robotCtrl, 1,
//						30)) {
//					cncData.setCNCTimingData(cncIP, CncItems.DT_CLEANING_START2, System.currentTimeMillis());
//					LogUtils.operationLog(logFile, logContPre + "ConcurrentOP CNC_CLEANING done");
//				} else {
//					errMsg = "机床执行清洁程序失败(ConcurrentOP)";
//					LogUtils.operationLog(logFile, logContPre + errMsg);
//					return errMsg;
//				}
//			} else {
//				errMsg = "系统检测到当前机床安全门无法关闭";
//				LogUtils.operationLog(logFile, logContPre + errMsg);
//				return errMsg;
//			}
//			stepCounter++;

			// Step 20: Check whether Robot has moved to target Rack or not
			setMachiningStatus("机器人移向充电位");
			LogUtils.operationLog(logFile, logContPre + "Robot moves to Rack(status checking)");
			if (!taskProc.concurrentOPDone(cncIP, robotIP, DeviceOP.ROBOT_MOVETORACK, CONCURR_OP_TIMEOUT)) {
				errMsg = "机器人【移至充电位置】失败(concurrentOP timeout)";
				LogUtils.operationLog(logFile, logContPre + errMsg);
				return errMsg;
			}
			showRobotBattery(robotGetBattery());
			showRobotPosition(robotGetPosition());
			setMachiningStatus("机器人到达充电位");
			LogUtils.operationLog(logFile, logContPre + "Robot moves to Rack " + rackID + " done");
			stepCounter++;

			// Step 21: Robot puts materials onto rack
			setMachiningStatus("机器人自下料");
			RunningMsg.set("Robot " + robotModel + "/" + robotIP + " puts materials onto Rack " + rackID);
			LogUtils.operationLog(logFile, logContPre + "Robot puts materials onto rack " + rackID + " started");
			if (!taskProc.putMaterialOntoRack(robotCtrl, robotIP, rackID, outputRackSlots.split(","), logFile,
					logContPre, cncModel)) {
				errMsg = "【机器人自下料】失败";
				LogUtils.operationLog(logFile, logContPre + errMsg);
				return errMsg;
			}
			LogUtils.operationLog(logFile, logContPre + "Robot puts materials onto rack done");
			stepCounter++;

			// Step 22: Check whether CNC cleaning is done or not
//			setMachiningStatus("等待机床清洁");
//			RunningMsg.set("Check CNC 2nd cleaning status");
//			LogUtils.operationLog(logFile, logContPre + "CNC 2nd cleaning status checking");
//			if (!taskProc.concurrentOPDone(cncIP, robotIP, DeviceOP.CNC_CLEANING, 2000)) {
//				errMsg = "机床清洁程序超时";
//				LogUtils.operationLog(logFile, logContPre + errMsg);
//				return errMsg;
//			}
//			cncData.setCncLastState(cncIP, DeviceState.FINISH);
//			cncData.setCNCTimingData(cncIP, CncItems.DT_CLEANING_STOP2, System.currentTimeMillis());
//			robotData.setData(robotIP, RobotItems.OP_STOPPNPTESTING, "1");
//			LogUtils.operationLog(logFile, logContPre + "CNC 2nd cleaning done");
//			stepCounter++;

			// Step 23: Machine opens door
//			setMachiningStatus("机床开门");
//			RunningMsg.set("CNC door opening");
//			if (!cncCtrl.openDoor(cncIP)) {
//				if (!"".equals(taskProc.openDoorEx(cncCtrl, cncIP))) {
//					errMsg = "机床【开门】失败";
//					LogUtils.operationLog(logFile, logContPre + errMsg);
//					return errMsg;
//				}
//			}
//			LogUtils.operationLog(logFile, logContPre + "CNC door opening done");
//			stepCounter++;

			// Step 24: Unlock machine and save machining data
			setMachiningStatus("加工任务完成");
			cncData.setData(cncIP, CncItems.STATE, DeviceState.STANDBY);
			LogUtils.operationLog(logFile, logContPre + "Unlock machine done");
			cncData.saveMachiningData(cncIP);
			stepCounter++;
		} catch (Exception e) {
			LogUtils.errorLog("TaskProcessor-TaskHandle ERR:" + e.getMessage());
			if (stepCounter < 24)
				errMsg = "任务异常终止(" + stepCounter + "/24)\r\n\r\n异常信息：" + e.getMessage();
		}

		return errMsg;
	}

	class ButtonOnClick implements Runnable {
		private String btnText;

		public ButtonOnClick(String btnText) {
			this.btnText = btnText;
		}

		@Override
		public void run() {
			String errMsg = "";

			switch (btnText) {
			case "开门":
				errMsg = openDoor();
				if (!"".equals(errMsg)) {
					JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
				}
				break;
			case "关门":
				errMsg = closeDoor();
				if (!"".equals(errMsg)) {
					JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
				}
				break;
			case "上传加工程序":
				errMsg = uploadNCProgram();
				if (!"".equals(errMsg)) {
					JOptionPane.showMessageDialog(contentPanel, errMsg, "Upload Program Error",
							JOptionPane.ERROR_MESSAGE);
				}
				break;
			case "解锁工件":
				errMsg = unlockFixtures();
				if (!"".equals(errMsg)) {
					JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
				}
				break;
			case "锁紧工件":
				errMsg = lockFixtures();
				if (!"".equals(errMsg)) {
					JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
				}
				break;
			case "执行加工程序":
				errMsg = doMachining();
				if ("".equals(errMsg))
					errMsg = openDoor();
				if ("".equals(errMsg))
					errMsg = unlockFixtures();
				if (!"".equals(errMsg)) {
					JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
				}
				break;
			case "移至充电位置":
				if (!robotGoHT()) {
					JOptionPane.showMessageDialog(contentPanel, "移至充电位置失败", "Operation Error",
							JOptionPane.ERROR_MESSAGE);
				} else {
					showRobotBattery(robotGetBattery());
					showRobotPosition(robotGetPosition());
				}
				break;
			case "开始充电":
				if (!robotGoCharging()) {
					JOptionPane.showMessageDialog(contentPanel, "充电失败", "OperationError", JOptionPane.ERROR_MESSAGE);
				}
				break;
			case "结束充电":
				if (!robotEndCharging()) {
					JOptionPane.showMessageDialog(contentPanel, "结束充电失败", "OperationError", JOptionPane.ERROR_MESSAGE);
				} else {
					showRobotBattery(robotGetBattery());
					showRobotPosition(robotGetPosition());
				}
				break;
			case "查询剩余电量":
				showRobotBattery(robotGetBattery());
				showRobotPosition(robotGetPosition());
				break;
			case "机器人自上料":
				if (!robotGetMaterials()) {
					JOptionPane.showMessageDialog(contentPanel, "机器人自上料失败", "Operation Error",
							JOptionPane.ERROR_MESSAGE);
				}
				break;
			case "机器人自下料":
				if (!robotPutMaterials()) {
					JOptionPane.showMessageDialog(contentPanel, "机器人自下料失败", "Operation Error",
							JOptionPane.ERROR_MESSAGE);
				}
				break;
			case "移至机床上下料位置":
				errMsg = "";
				if (!cncDoorOpened())
					errMsg = openDoor();
				if (!"".equals(errMsg)) {
					errMsg = "机床开门失败导致机器人无法移至机床上下料位置";
					JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
				} else {
					if (!robotGoCNC()) {
						JOptionPane.showMessageDialog(contentPanel, "移至机床上下料位置失败", "Operation Error",
								JOptionPane.ERROR_MESSAGE);
					} else {
						showRobotBattery(robotGetBattery());
						showRobotPosition(robotGetPosition());
					}
				}
				break;
			case "给机床上料":
				if (!robotLoading()) {
					JOptionPane.showMessageDialog(contentPanel, "给机床上料失败", "OperationError", JOptionPane.ERROR_MESSAGE);
				}
				break;
			case "给机床下料":
				if (!robotUnloading()) {
					JOptionPane.showMessageDialog(contentPanel, "给机床下料失败", "OperationError", JOptionPane.ERROR_MESSAGE);
				}
				break;
			default:
				errMsg = "指令【" + btnText + "】未定义";
				JOptionPane.showMessageDialog(contentPanel, errMsg, "Operation Error", JOptionPane.ERROR_MESSAGE);
				break;
			}

			setButtonsEnabled(true, false);
		}
	}

	class ExecuteTask implements Runnable {
		@Override
		public void run() {
			String cncIP = comboBoxIP.getSelectedItem().toString();
			String robotIP = comboBoxRobotIP.getSelectedItem().toString();
			String msg = "";
			boolean charging = robotIsCharging();
			if (!charging) {
				msg = "您确定机器人此时正在充电位置充电吗?\r\n\r\n";
				msg += "确定请点击【是】按钮，\r\n否则点击【否】按钮";
				MyConfirmDialog.showDialog("请确认机器人位置", msg);
				if (MyConfirmDialog.OPTION_YES == MyConfirmDialog.getConfirmFlag())
					charging = true;
			}

			setMachiningCodePrefix();
			String[] ids = getSlotAndWorkpieceIDs(",");
			if (!"".equals(ids[0]) && charging) {
				msg = "";
				msg = unlockFixtures();
				if (!"".equals(msg)) {
					msg = "机床夹具解锁失败导致机器人无法执行任务";
					JOptionPane.showMessageDialog(contentPanel, msg, "Operation Error", JOptionPane.ERROR_MESSAGE);
				} else {
					while (true) {
						msg = taskOneCycle(robotIP, cncIP, "3", ids[0], ids[1], ids[2]);
						if (!"".equals(msg)) {
							JOptionPane.showMessageDialog(contentPanel, msg, "执行任务失败", JOptionPane.ERROR_MESSAGE);
							break;
						}
						showCncState(getCncState());
						if (!chckbxAutoLul.isSelected())
							break;
					}
				}
			} else {
				if ("".equals(ids[0]))
					msg = "请选择上下料位置";
				if ("".equals(msg)) {
					msg = "请通过 <机器人操作> 按钮将机器人 【移至充电位置】 并 【开始充电】";
				} else {
					msg += "，并通过 <机器人操作> 按钮将机器人 【移至充电位置】 并 【开始充电】";
				}
				JOptionPane.showMessageDialog(contentPanel, msg, "执行任务失败", JOptionPane.ERROR_MESSAGE);
			}
			setButtonsEnabled(true, false);
		}
	}
}
