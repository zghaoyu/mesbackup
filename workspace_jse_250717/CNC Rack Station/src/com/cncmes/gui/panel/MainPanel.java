package com.cncmes.gui.panel;

import javax.swing.JPanel;
import javax.swing.JMenuBar;

import java.awt.BorderLayout;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.FlowLayout;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.cncmes.base.PermissionItems;
import com.cncmes.gui.frame.RackStation;
import com.cncmes.gui.listener.MenuBarListener;
import com.cncmes.gui.listener.TitleBarListener;
import com.cncmes.utils.GUIUtils;
import com.cncmes.utils.LoginSystem;

import java.awt.SystemColor;
import java.awt.Font;
import java.awt.Color;
import javax.swing.ImageIcon;

/**
 * 
 * @author W000586 Hui Zhi Fang 2022/3/1
 */
public class MainPanel extends JPanel {

	private static final long serialVersionUID = 5271383651090680563L;
	private static MainPanel mainPanel = new MainPanel();
	private CenterPanel panelCenter;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenu mnHelp;
	private JMenuItem mntmLogin;
	private JMenuItem mntmExit;
	private JMenuItem mntmAboutRackStation;
	private JMenuItem mntmConfig;
	private JPanel panelLeft;
	private JPanel btn_settings;
	private JPanel btn_storage_material;
	private JPanel btn_input;
	private JPanel btn_output;
	private JPanel btn_rack;
	private JPanel title_panel;
	private JLabel inputLabel;
	private JLabel OutputLabel;
	private JLabel lblSettings;
	private JLabel labelStorageMaterial;
	private JLabel rackLabel;
	private JLabel title;

	public static MainPanel getInstance() {
		return mainPanel;
	}

	/**
	 * Create the panel.
	 */
	private MainPanel() {
		// MenuBar
		menuBar = new JMenuBar();
		menuBar.setBorderPainted(false);
		mnFile = new JMenu("File");
		mnHelp = new JMenu("Help");
		menuBar.add(mnFile);
		menuBar.add(mnHelp);
		// Menu File
		mntmLogin = new JMenuItem("Login");
		mntmExit = new JMenuItem("Exit");
		mntmConfig = new JMenuItem("Config");
		mnFile.add(mntmLogin);
		mnFile.add(mntmConfig);
		mnFile.add(mntmExit);
		// Menu Help
		mntmAboutRackStation = new JMenuItem("About Rack Station");
		mnHelp.add(mntmAboutRackStation);

		// panel left
		panelLeft = new JPanel();
		panelLeft.setBackground(SystemColor.controlHighlight);
		panelLeft.setPreferredSize(new Dimension(200, 0));
		panelLeft.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

		title_panel = new JPanel();
		title_panel.setPreferredSize(new Dimension(200, 150));
		title_panel.setBackground(SystemColor.controlHighlight);
		panelLeft.add(title_panel);
		title_panel.setLayout(new BorderLayout(0, 0));

		title = new JLabel(" Rack Station");
		title.setIcon(new ImageIcon(MainPanel.class.getResource("/com/cncmes/img/Butterfly_orange_24.png")));
		title.setForeground(new Color(0, 0, 0));
		title.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title_panel.add(title, BorderLayout.CENTER);

		// button 1
		btn_rack = new JPanel();
		btn_rack.setPreferredSize(new Dimension(200, 50));
		btn_rack.setBackground(GUIUtils.GRAY1);
		panelLeft.add(btn_rack);
		btn_rack.setLayout(new BorderLayout(0, 0));

		rackLabel = new JLabel("Status");
		rackLabel.setFont(GUIUtils.menuFont);
		rackLabel.setForeground(Color.DARK_GRAY);
		rackLabel.setHorizontalAlignment(SwingConstants.CENTER);
		btn_rack.add(rackLabel, BorderLayout.CENTER);

		// button 2
		btn_input = new JPanel();
		btn_input.setBackground(SystemColor.control);
		btn_input.setPreferredSize(new Dimension(200, 50));
		panelLeft.add(btn_input);
		btn_input.setLayout(new BorderLayout(0, 0));

		inputLabel = new JLabel("Binding");
		inputLabel.setForeground(Color.DARK_GRAY);
		inputLabel.setFont(GUIUtils.menuFont);
		inputLabel.setHorizontalAlignment(SwingConstants.CENTER);
		btn_input.add(inputLabel);

		// button3
		btn_output = new JPanel();
		btn_output.setBackground(SystemColor.control);
		btn_output.setPreferredSize(new Dimension(200, 50));
		panelLeft.add(btn_output);
		btn_output.setLayout(new BorderLayout(0, 0));

		OutputLabel = new JLabel("Output");
		OutputLabel.setFont(GUIUtils.menuFont);
		OutputLabel.setForeground(Color.DARK_GRAY);
		OutputLabel.setHorizontalAlignment(SwingConstants.CENTER);
		btn_output.add(OutputLabel);
		btn_output.setVisible(false);
		// button4
		btn_storage_material = new JPanel();
		btn_storage_material.setBackground(SystemColor.control);
		btn_storage_material.setPreferredSize(new Dimension(200, 50));
		panelLeft.add(btn_storage_material);
		btn_storage_material.setLayout(new BorderLayout(0, 0));

		labelStorageMaterial = new JLabel("Null Page");
		labelStorageMaterial.setFont(GUIUtils.menuFont);
		labelStorageMaterial.setForeground(Color.DARK_GRAY);
		labelStorageMaterial.setHorizontalAlignment(SwingConstants.CENTER);
		btn_storage_material.add(labelStorageMaterial);
		btn_storage_material.setVisible(false);
//		// button4
//		btn_settings = new JPanel();
//		btn_settings.setBackground(SystemColor.control);
//		btn_settings.setPreferredSize(new Dimension(200, 50));
//		panelLeft.add(btn_settings);
//		btn_settings.setLayout(new BorderLayout(0, 0));
//
//		lblSettings = new JLabel("Storage Material");
//		lblSettings.setFont(GUIUtils.menuFont);
//		lblSettings.setForeground(Color.DARK_GRAY);
//		lblSettings.setHorizontalAlignment(SwingConstants.CENTER);
//		btn_settings.add(lblSettings);

		// Panel center
		panelCenter = CenterPanel.getInstance();
		panelCenter.changePanel(StatePanel.getInstance());

		// main panel layout
		this.setLayout(new BorderLayout());
		this.add(menuBar, BorderLayout.NORTH);
		this.add(panelLeft, BorderLayout.WEST);
		this.add(panelCenter, BorderLayout.CENTER);

		addListener();
		refreshButtonsEnabled();

	}

	private void addListener() {
		TitleBarListener tbListener = new TitleBarListener();
		MenuBarListener mbListener = new MenuBarListener();
		
		btn_rack.addMouseListener(tbListener);
		btn_input.addMouseListener(tbListener);
		btn_output.addMouseListener(tbListener);
		btn_storage_material.addMouseListener(tbListener);
		
		mntmLogin.addActionListener(mbListener);
		mntmExit.addActionListener(mbListener);
		mntmConfig.addActionListener(mbListener);
		mntmAboutRackStation.addActionListener(mbListener);
	}

	public void refreshButtonsEnabled() {
		mntmConfig.setEnabled(!LoginSystem.accessDenied(PermissionItems.SYSCONFIG));
		setTitle();
		if (LoginSystem.userHasLoginned()) {
			mntmLogin.setText("Logout");
			mntmLogin.setToolTipText("Logout System");
		} else {
			mntmLogin.setText("Login");
			mntmLogin.setToolTipText("Login System");
		}
	}

	private void setTitle() {
		RackStation rackStation = RackStation.getInstance();
		if (null == rackStation)
			return;
		String title = rackStation.getTitle();
		if (null != title) {
			title = title.split("##")[0] + "##Welcome " + LoginSystem.getUserName();
			rackStation.setTitle(title);
		}
	}
	
	public JPanel getBtn_settings() {
		return btn_settings;
	}
	public JPanel getBtn_storage_material() {
		return btn_storage_material;
	}

	public JPanel getBtn_input() {
		return btn_input;
	}

	public JPanel getBtn_output() {
		return btn_output;
	}

	public JPanel getBtn_rack() {
		return btn_rack;
	}

	public CenterPanel getPanelCenter() {
		return panelCenter;
	}

	public JMenuItem getMntmLogin() {
		return mntmLogin;
	}

	public JMenuItem getMntmExit() {
		return mntmExit;
	}

	public JMenuItem getMntmAboutRackStation() {
		return mntmAboutRackStation;
	}

	public JMenuItem getMntmConfig() {
		return mntmConfig;
	}

}
