package com.cncmes.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import com.cncmes.base.PermissionItems;
import com.cncmes.dao.DAO;
import com.cncmes.dao.impl.DAOImpl;
import com.cncmes.data.SystemConfig;
import com.cncmes.dto.CNCMaterial;
import com.cncmes.dto.CNCProcessCard;
import com.cncmes.utils.DTOUtils;
import com.cncmes.utils.GUIUtils;
import com.cncmes.utils.LoginSystem;
import com.cncmes.utils.MailUtils;
import com.cncmes.utils.NetUtils;
import com.cncmes.utils.TimeUtils;

import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Component;
import java.awt.Color;
import javax.swing.border.EtchedBorder;

/***
 * 
 * @author W000586 2022/1/17
 *
 */
public class RawMaterialStore extends JFrame {

	private static final long serialVersionUID = 1L;
	private static RawMaterialStore rawMaterialStore = new RawMaterialStore();

	private String processCardDto = "com.cncmes.dto.CNCProcessCard";
	private String materialDto = "com.cncmes.dto.CNCMaterial";
	private String machingSpecDto = "com.cncmes.dto.CNCMachiningSpec";
	private String[] orderColumnNames = new String[] { "id", "material No", "scan time" };
	private String[] materialColumnNames = new String[] { "material No" };
	

	private String curOrderNo = "";

	private JPanel contentPane;
	private JPanel panelToolBar;
	private JPanel panelCenter;
	private JPanel panelAddProcessCard;
	private JPanel panelAddMaterial;
	private JPanel panelShowDBInfo;
	private JPanel panelOrderTitle;
	private JPanel panelInputMaterial;
	private JPanel panelMaterialTitle;
	private JPanel panelMaterialSave;
	private JPanel panelQTY;

	private JLabel lblProcessCard;
	private JLabel lblOrderNo;
	private JLabel lblDrawingNo;
	private JLabel lblPartNo;
	private JLabel lblProductNum;
	private JLabel lblOrderInfo;
	private JLabel lblMaterial;
	private JLabel lblQty;
	private JLabel lblShowqty;
	private JLabel lblMessage;

	private JTable tableMaterial;
	private JTable tableOrder;

	private MyTableModel orderTableModel;
	private MyTableModel materialTableModel;

	private JScrollPane scrollPaneOrder;
	private JScrollPane scrollPaneMaterial;

	private JButton btnSysCfg;
	private JButton btnLogin;
	private JButton btnExit;
	private JButton btnConfirm;
	private JButton btnSaveMaterial;

	private JTextField textFieldOrder;
	private JTextField textFieldDrawing;
	private JTextField textFieldPart;
	private Integer eachMaterialProductNum;
	private JComboBox<Integer> comboBoxProductNum;

	public JComboBox<Integer> getComboBoxProductNum() {
		return comboBoxProductNum;
	}

	public void setComboBoxProductNum(JComboBox<Integer> comboBoxProductNum) {
		this.comboBoxProductNum = comboBoxProductNum;
	}

	/**
	 * Launch the application.
	 */

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RawMaterialStore frame = getInstance();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static RawMaterialStore getInstance() {
		return rawMaterialStore;
	}

	/**
	 * Create the frame.
	 */
	public RawMaterialStore() {
		setIconImage(Toolkit.getDefaultToolkit()
				.getImage(RawMaterialStore.class.getResource("/com/cncmes/img/Material_24.png")));
		setTitle("Raw Material Store");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Center display
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 700;
		int height = 700;
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);

		// Menu
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.setIcon(new ImageIcon(RawMaterialStore.class.getResource("/com/cncmes/img/Exit_16.png")));
		mnFile.add(mntmExit);

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmAbout = new JMenuItem("About");
		mnHelp.add(mntmAbout);

		JMenuItem selectJO = new JMenuItem("MoCode Info");
		mnHelp.add(selectJO);
		selectJO.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(16 == e.getModifiers()){
					JOInfoDialog.getInstance().showDialog();
				}
			}
		});



		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		// ToolBar Panel
		panelToolBar = new JPanel();
		contentPane.add(panelToolBar, BorderLayout.NORTH);
		panelToolBar.setLayout(new BorderLayout(0, 0));

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		panelToolBar.add(toolBar);

		btnLogin = new JButton("");
		btnLogin.setBorderPainted(false);
		btnLogin.setIcon(new ImageIcon(RawMaterialStore.class.getResource("/com/cncmes/img/login_24.png")));
		toolBar.add(btnLogin);

		btnSysCfg = new JButton("");
		btnSysCfg.setToolTipText("System Config");
		btnSysCfg.setBorderPainted(false);
		btnSysCfg.setIcon(new ImageIcon(RawMaterialStore.class.getResource("/com/cncmes/img/setting_24.png")));
		toolBar.add(btnSysCfg);

		btnExit = new JButton("");
		btnExit.setToolTipText("Exit from system");
		btnExit.setBorderPainted(false);
		btnExit.setIcon(new ImageIcon(RawMaterialStore.class.getResource("/com/cncmes/img/Exit_24.png")));
		toolBar.add(btnExit);

		panelCenter = new JPanel();
		panelCenter.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		contentPane.add(panelCenter, BorderLayout.CENTER);
		panelCenter.setLayout(new BorderLayout(0, 0));

		// Process Card Panel
		panelAddProcessCard = new JPanel();
		panelAddProcessCard.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panelCenter.add(panelAddProcessCard, BorderLayout.NORTH);

		GridBagLayout gbl_panelAddProcessCard = new GridBagLayout();
		gbl_panelAddProcessCard.columnWidths = new int[] { 100, 150, 100, 150, 130, 30 };
		gbl_panelAddProcessCard.rowHeights = new int[] { 40, 30, 30, 30 };
		gbl_panelAddProcessCard.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		gbl_panelAddProcessCard.rowWeights = new double[] { 0.0, 0.0, 0.0 };
		panelAddProcessCard.setLayout(gbl_panelAddProcessCard);

		lblProcessCard = new JLabel("Technological Process Card");
		lblProcessCard.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblTechnologicalProcessCard = new GridBagConstraints();
		gbc_lblTechnologicalProcessCard.gridwidth = 5;
		gbc_lblTechnologicalProcessCard.insets = new Insets(0, 0, 5, 5);
		gbc_lblTechnologicalProcessCard.gridx = 0;
		gbc_lblTechnologicalProcessCard.gridy = 0;
		panelAddProcessCard.add(lblProcessCard, gbc_lblTechnologicalProcessCard);

		lblOrderNo = new JLabel("MoCode No");
		GridBagConstraints gbc_lblOrderNo = new GridBagConstraints();
		gbc_lblOrderNo.anchor = GridBagConstraints.EAST;
		gbc_lblOrderNo.insets = new Insets(0, 0, 5, 5);
		gbc_lblOrderNo.gridx = 0;
		gbc_lblOrderNo.gridy = 1;
		panelAddProcessCard.add(lblOrderNo, gbc_lblOrderNo);

		textFieldOrder = new JTextField();
		GridBagConstraints gbc_textField_order_no = new GridBagConstraints();
		gbc_textField_order_no.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_order_no.insets = new Insets(0, 0, 5, 5);
		gbc_textField_order_no.gridx = 1;
		gbc_textField_order_no.gridy = 1;
		panelAddProcessCard.add(textFieldOrder, gbc_textField_order_no);
		textFieldOrder.setColumns(10);

		lblDrawingNo = new JLabel("Drawing No");
		GridBagConstraints gbc_lblDrawingNo = new GridBagConstraints();
		gbc_lblDrawingNo.anchor = GridBagConstraints.EAST;
		gbc_lblDrawingNo.insets = new Insets(0, 0, 5, 5);
		gbc_lblDrawingNo.gridx = 2;
		gbc_lblDrawingNo.gridy = 1;
		panelAddProcessCard.add(lblDrawingNo, gbc_lblDrawingNo);

		textFieldDrawing = new JTextField();
		GridBagConstraints gbc_textField_drawing_no = new GridBagConstraints();
		gbc_textField_drawing_no.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_drawing_no.insets = new Insets(0, 0, 5, 5);
		gbc_textField_drawing_no.gridx = 3;
		gbc_textField_drawing_no.gridy = 1;
		panelAddProcessCard.add(textFieldDrawing, gbc_textField_drawing_no);
		textFieldDrawing.setColumns(10);

		btnConfirm = new JButton("Confirm");
		btnConfirm.setAlignmentX(Component.CENTER_ALIGNMENT);
		GridBagConstraints gbc_btnConfirm = new GridBagConstraints();
		gbc_btnConfirm.gridheight = 2;
		gbc_btnConfirm.insets = new Insets(0, 0, 5, 5);
		gbc_btnConfirm.gridx = 4;
		gbc_btnConfirm.gridy = 1;
		panelAddProcessCard.add(btnConfirm, gbc_btnConfirm);

//		lblPartNo = new JLabel("Part No");
//		GridBagConstraints gbc_labelPartNo = new GridBagConstraints();
//		gbc_labelPartNo.anchor = GridBagConstraints.EAST;
//		gbc_labelPartNo.insets = new Insets(0, 0, 5, 5);
//		gbc_labelPartNo.gridx = 0;
//		gbc_labelPartNo.gridy = 2;
//		panelAddProcessCard.add(lblPartNo, gbc_labelPartNo);

		textFieldPart = new JTextField("123123");      //part no default 123123
		textFieldPart.setColumns(10);
		textFieldPart.setVisible(false);
		GridBagConstraints gbc_textField_part_no = new GridBagConstraints();
		gbc_textField_part_no.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_part_no.insets = new Insets(0, 0, 5, 5);
		gbc_textField_part_no.gridx = 1;
		gbc_textField_part_no.gridy = 2;
		panelAddProcessCard.add(textFieldPart, gbc_textField_part_no);

		lblMessage = new JLabel("");
		lblMessage.setForeground(Color.RED);
		GridBagConstraints gbc_lblMessage = new GridBagConstraints();
		gbc_lblMessage.gridwidth = 6;
		gbc_lblMessage.insets = new Insets(0, 0, 0, 5);
		gbc_lblMessage.gridx = 0;
		gbc_lblMessage.gridy = 3;
		panelAddProcessCard.add(lblMessage, gbc_lblMessage);

		lblProductNum = new JLabel("1 material = ");
		GridBagConstraints gbc_lblProductNum = new GridBagConstraints();
		gbc_lblProductNum.anchor = GridBagConstraints.EAST;
		gbc_lblProductNum.insets = new Insets(0, 0, 5, 5);
		gbc_lblProductNum.gridx = 2;
		gbc_lblProductNum.gridy = 2;
		panelAddProcessCard.add(lblProductNum, gbc_lblProductNum);

		comboBoxProductNum = new JComboBox<>();
		comboBoxProductNum.setPreferredSize(new Dimension(100, 20));
		for(int i=1;i<=10;i++)
		{
			comboBoxProductNum.addItem(i);
		}
		comboBoxProductNum.addItemListener(e -> {
			if(e.getStateChange() == ItemEvent.SELECTED)
			{
				eachMaterialProductNum = (Integer) comboBoxProductNum.getSelectedItem();
			}
		});
		GridBagConstraints gbc_comboBoxProductNum = new GridBagConstraints();
		gbc_comboBoxProductNum.anchor = GridBagConstraints.WEST;
		gbc_comboBoxProductNum.insets = new Insets(0, 0, 5, 0);
		gbc_comboBoxProductNum.gridx = 3;
		gbc_comboBoxProductNum.gridy = 2;
		panelAddProcessCard.add(comboBoxProductNum, gbc_comboBoxProductNum);

		JLabel lbltext = new JLabel("product");
		GridBagConstraints gbc_lbltext = new GridBagConstraints();
		gbc_lbltext.anchor = GridBagConstraints.EAST;
		gbc_lbltext.insets = new Insets(0, 0, 5, 0);
		gbc_lbltext.gridx = 3;
		gbc_lbltext.gridy = 2;
		panelAddProcessCard.add(lbltext, gbc_lbltext);

		// Material Panel
		panelAddMaterial = new JPanel();
		panelAddMaterial.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panelCenter.add(panelAddMaterial, BorderLayout.CENTER);
		panelAddMaterial.setLayout(new BorderLayout(0, 0));

		// Database Info Panel
		panelShowDBInfo = new JPanel();
		panelShowDBInfo.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelAddMaterial.add(panelShowDBInfo, BorderLayout.WEST);
		panelShowDBInfo.setLayout(new BorderLayout(0, 0));

		panelOrderTitle = new JPanel();
		panelOrderTitle.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		panelShowDBInfo.add(panelOrderTitle, BorderLayout.NORTH);

		lblOrderInfo = new JLabel("Journal Material Information");
		lblOrderInfo.setFont(new Font("Tahoma", Font.BOLD, 11));
		panelOrderTitle.add(lblOrderInfo);

		scrollPaneOrder = new JScrollPane();
		panelShowDBInfo.add(scrollPaneOrder, BorderLayout.CENTER);

		orderTableModel = new MyTableModel(orderColumnNames, new Object[0][3]);
		tableOrder = new JTable(orderTableModel);
		tableOrder.setRowHeight(25);
		tableOrder.getTableHeader().setReorderingAllowed(false);
		scrollPaneOrder.setColumnHeaderView(tableOrder);
		scrollPaneOrder.setViewportView(tableOrder);

		panelQTY = new JPanel();
		panelShowDBInfo.add(panelQTY, BorderLayout.SOUTH);
		panelQTY.setLayout(new GridLayout(0, 2, 0, 0));

		lblQty = new JLabel("QTY:");
		lblQty.setAlignmentX(Component.CENTER_ALIGNMENT);
		panelQTY.add(lblQty);

		lblShowqty = new JLabel("0");
		panelQTY.add(lblShowqty);

		// Input Material
		panelInputMaterial = new JPanel();
		panelInputMaterial.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelAddMaterial.add(panelInputMaterial);
		panelInputMaterial.setLayout(new BorderLayout(0, 0));

		panelMaterialTitle = new JPanel();
		panelMaterialTitle.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		panelInputMaterial.add(panelMaterialTitle, BorderLayout.NORTH);

		lblMaterial = new JLabel("Material");
		lblMaterial.setFont(new Font("Tahoma", Font.BOLD, 11));
		panelMaterialTitle.add(lblMaterial);

		scrollPaneMaterial = new JScrollPane();
		panelInputMaterial.add(scrollPaneMaterial, BorderLayout.CENTER);

		Object[][] materialData = initMaterialTableData(30, 1);// need to modify
		materialTableModel = new MyTableModel(materialColumnNames, materialData, 0);
		tableMaterial = new JTable(materialTableModel);
		tableMaterial.setRowHeight(25);
		tableMaterial.getTableHeader().setReorderingAllowed(false);
		tableMaterial.getColumnModel().getColumn(0).setPreferredWidth(10);

		scrollPaneMaterial.setColumnHeaderView(tableMaterial);
		scrollPaneMaterial.setViewportView(tableMaterial);

		panelMaterialSave = new JPanel();
		panelInputMaterial.add(panelMaterialSave, BorderLayout.SOUTH);

		btnSaveMaterial = new JButton("Save");
		panelMaterialSave.add(btnSaveMaterial);

		addListener();
		refreshButtonsEnabled();
	}

	private void setTitle() {
		if (null == rawMaterialStore)
			return;
		String title = rawMaterialStore.getTitle();
		if (null != title) {
			title = title.split("##")[0] + "##Welcome " + LoginSystem.getUserName();
			rawMaterialStore.setTitle(title);
		}
	}

	public Object[][] initMaterialTableData(int rowNum, int colNum) {
		Object[][] data = new Object[rowNum][colNum];
		for (int i = 0; i < rowNum; i++) {
			data[i][0] = "";
		}
		return data;
	}

	public void refreshButtonsEnabled() {
		btnSysCfg.setEnabled(!LoginSystem.accessDenied(PermissionItems.SYSCONFIG));
		btnConfirm.setEnabled(!LoginSystem.accessDenied(PermissionItems.RAWMATERIALSTORE));
		btnSaveMaterial.setEnabled(!LoginSystem.accessDenied(PermissionItems.RAWMATERIALSTORE));
		setTitle();
		if (LoginSystem.userHasLoginned()) {
			btnLogin.setIcon(new ImageIcon(RawMaterialStore.class.getResource("/com/cncmes/img/logout_24.png")));
			btnLogin.setToolTipText("Logout System");
		} else {
			btnLogin.setIcon(new ImageIcon(RawMaterialStore.class.getResource("/com/cncmes/img/login_24.png")));
			btnLogin.setToolTipText("Login System");
		}
	}

	private void refreshProcessCardTable() {
		textFieldOrder.setText("");
		textFieldDrawing.setText("");
		textFieldPart.setText("123123");//default
	}

	private void refreshMaterialTable() {
		Object[][] data = initMaterialTableData(30, 1);// need to modify
		materialTableModel = new MyTableModel(materialColumnNames, data, 0);
		tableMaterial.setModel(materialTableModel);
	}

	private void refreshOrderTable() {
		int qty = 0;
		Object[][] data = packOrderInfoData(orderColumnNames);
		orderTableModel = new MyTableModel(orderColumnNames, data);
		tableOrder.setModel(orderTableModel);
		qty = orderTableModel.getRowCount();
		GUIUtils.setLabelText(lblShowqty, "" + qty);
	}

	private void exitSystem(boolean force) {
		if (0 == JOptionPane.showConfirmDialog(contentPane, "Are you sure of quitting from system?",
				"Quit From System?", JOptionPane.YES_NO_OPTION)) {
			System.exit(0);
		}
	}

	private boolean dataIsOkToSave() {
		if (GUIUtils.checkEmpty(textFieldOrder, "Order No", lblMessage)) {
			return false;
		}
		if (GUIUtils.checkEmpty(textFieldDrawing, "Drawing No", lblMessage)) {
			return false;
		}

		if (GUIUtils.checkEmpty(textFieldPart, "Part No", lblMessage)) {
			return false;
		}
		return true;
	}

	private String[] getDtoFields(String dtoClassName) {       //return a DTO all fields string
		String[] fields = null;
		String temp = "";
		try {
			Map<String, Object> fieldsMap = DTOUtils.getDTOFields(dtoClassName);
			if (null != fieldsMap && fieldsMap.size() > 0) {
				for (String key : fieldsMap.keySet()) {
					if ("".equals(temp)) {
						temp = key;
					} else {
						temp += "," + key;
					}
				}
				fields = temp.split(",");
			}
		} catch (Exception e) {
		}

		return fields;
	}

	private int getFieldIndex(String[] fields, String fieldName) {
		int idx = -1;

		if (null != fields && fields.length > 0) {
			for (int i = 0; i < fields.length; i++) {
				if (fields[i].equals(fieldName)) {
					idx = i;
					break;
				}
			}
		}

		return idx;
	}

	private Object[][] packProcessCardData(String[] fields) {
		Object[][] curData = null;
		String orderNO = textFieldOrder.getText();
		String drawingNO = textFieldDrawing.getText();
		String partNO = textFieldPart.getText();
		String createTime = TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss");
		String ip_address = NetUtils.getLocalIP();
		String pcName = NetUtils.getLocalHostName();
		int userId = LoginSystem.getUserId();
		if (fields.length > 0) {
			curData = new Object[1][fields.length];
			curData[0][getFieldIndex(fields, "order_no")] = orderNO;
			curData[0][getFieldIndex(fields, "drawing_no")] = drawingNO;
			curData[0][getFieldIndex(fields, "part_no")] = partNO;
			curData[0][getFieldIndex(fields, "user_id")] = userId;
			curData[0][getFieldIndex(fields, "ip_address")] = ip_address;
			curData[0][getFieldIndex(fields, "pc_name")] = pcName;
			curData[0][getFieldIndex(fields, "create_time")] = createTime;
			curData[0][getFieldIndex(fields, "is_delete")] = 0;
		}
		return curData;
	}

	private Object[][] packMaterialData(String[] fields) {
		Object[][] curData = null;
		String[] materialNo = getMaterialNo();
		int processcardId = getProcessCardDTO(new String[] { curOrderNo }).getId();
		int userId = LoginSystem.getUserId();
		String ipAddress = NetUtils.getLocalIP();
		String pcName = NetUtils.getLocalHostName();
		String createTime = TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss");
		if (null != materialNo && fields.length > 0) {
			curData = new Object[materialNo.length][fields.length];
			for (int i = 0; i < materialNo.length; i++) {
				curData[i][getFieldIndex(fields, "processcard_id")] = processcardId;
				curData[i][getFieldIndex(fields, "material_no")] = materialNo[i];
				curData[i][getFieldIndex(fields, "each_material_productnum")] = eachMaterialProductNum;
				curData[i][getFieldIndex(fields, "user_id")] = userId;
				curData[i][getFieldIndex(fields, "ip_address")] = ipAddress;
				curData[i][getFieldIndex(fields, "pc_name")] = pcName;
				curData[i][getFieldIndex(fields, "create_time")] = createTime;
				curData[i][getFieldIndex(fields, "is_delete")] = 0;
			}
		}
		return curData;
	}

	// need to modify
	private Object[][] packOrderInfoData(String[] fields) {
		Object[][] curData = null;
		int processcard_id = getProcessCardDTO(new String[] { curOrderNo }).getId();

		DAO dao = new DAOImpl(materialDto);
		try {
			ArrayList<Object> vos = dao.findByCnd(new String[] { "processcard_id" },
					new String[] { "" + processcard_id });
			if (null != vos) {
				curData = new Object[vos.size()][3];
				for (int i = 0; i < vos.size(); i++) {
					CNCMaterial dto = (CNCMaterial) vos.get(i);
					curData[i][0] = i + 1;
					curData[i][1] = dto.getMaterial_no();
					curData[i][2] = dto.getCreate_time();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return curData;
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

	/**
	 * Get the data on the material table
	 * 
	 * @return
	 */
	private String[] getMaterialNo() {
		String[] materialTableData = null;
		ArrayList<String> materialNoList = new ArrayList<>();
		String materialNo = "";
		for (int i = 0; i < materialTableModel.getRowCount(); i++) {
			materialNo = materialTableModel.getValueAt(i, 0).toString().trim();
			if (materialNo.length() > 0) {
				materialNoList.add(materialNo);
			}
		}
		if (materialNoList.size() > 0) {
			materialTableData = (String[]) materialNoList.toArray(new String[materialNoList.size()]);
		}
		return materialTableData;
	}

	/**
	 * check whether the NC Program of this drawing is ready
	 */
	private void checkNCPrograme(String drawingNo) {
		DAO dao = new DAOImpl(machingSpecDto);
		try {
			ArrayList<Object> vos = dao.findByCnd(new String[] { "dwgno" }, new String[] { drawingNo });
			if (null == vos) {
				sendMail(drawingNo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private boolean sendMail(String drawingNo) {
		SystemConfig sysCfg = SystemConfig.getInstance();
		LinkedHashMap<String, Object> config = sysCfg.getCommonCfg();
		String to = (String) config.get("to");
		String cc = (String) config.get("cc");
		String subject = (String) config.get("subject");
		String content = (String) config.get("content");
		boolean sendMail = Integer.valueOf((String) config.get("sendMail")) > 0 ? true : false;
		if (sendMail) {
			content = "<br><br>" + content + "<br>" + "  1.order No: " + curOrderNo + "<br>" + "  2.drawing No: "
					+ drawingNo + "<br><br>";
			if (MailUtils.sendMail(to, cc, subject, content)) {
				return true;
			}
		}
		return false;
	}

	public void addListener() {
		ButtonListener bListener = new ButtonListener();
		ToolBarListener tbListener = new ToolBarListener();

		btnLogin.addActionListener(tbListener);
		btnSysCfg.addActionListener(tbListener);
		btnExit.addActionListener(tbListener);

		btnConfirm.addActionListener(bListener);
		btnSaveMaterial.addActionListener(bListener);

	}

	private class ToolBarListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == btnLogin) {
				if (btnLogin.isEnabled()) {
					if (LoginSystem.userHasLoginned()) {
						if (0 == JOptionPane.showConfirmDialog(rawMaterialStore, "Are you sure of logging out now?",
								"Log Out?", JOptionPane.YES_NO_OPTION)) {
							LoginSystem.userLogout();
							refreshButtonsEnabled();
						}
					} else {
						UserLogin dialog = UserLogin.getInstance();
						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dialog.setVisible(true);
					}
				}
			}

			if (e.getSource() == btnSysCfg) {
				if (btnSysCfg.isEnabled()) {
					try {
						SysConfig dialog = SysConfig.getInstance();
						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dialog.setVisible(true);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}

			if (e.getSource() == btnExit) {
				if (16 == e.getModifiers()) {
					exitSystem(true);
				}

			}

		}

	}

	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == btnConfirm && btnConfirm.isEnabled() && dataIsOkToSave()) {
				String title[];
				Object data[][];
				CNCProcessCard curProcessCard = null;

				GUIUtils.setLabelText(lblMessage, "");

				try {

					// check whether this order already exits
					curProcessCard = getProcessCardDTO(new String[] { textFieldOrder.getText().trim() });

					if (null != curProcessCard) {
						curOrderNo = curProcessCard.getOrder_no();
						GUIUtils.setLabelText(lblOrderInfo, "MoCode NO:" + curOrderNo);
						JOptionPane.showMessageDialog(null, "OK");
						refreshOrderTable();
						refreshProcessCardTable();
					} else {
						title = getDtoFields(processCardDto);
						data = packProcessCardData(title);
						if (DTOUtils.saveDataIntoDB(processCardDto, title, data)) {
							curOrderNo = "" + data[0][1];
							GUIUtils.setLabelText(lblOrderInfo, "MoCode No:" + curOrderNo);
							JOptionPane.showMessageDialog(null, "OK");
							refreshOrderTable();
							refreshProcessCardTable();
							checkNCPrograme("" + data[0][2]);// drawing No
						} else {
							JOptionPane.showMessageDialog(null, "Failed");
						}
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
				}

			}

			if (e.getSource() == btnSaveMaterial && btnConfirm.isEnabled()) {
				String title[];
				Object data[][];
				title = getDtoFields(materialDto);
				data = packMaterialData(title);

				if ("".equals(curOrderNo)) {
					JOptionPane.showMessageDialog(null,
							"Please input the barcode of the Technological Process Card first");
				} else if (null == data) {
					JOptionPane.showMessageDialog(null, "material No can't be blank");
				} else {
					try {
						if (DTOUtils.saveDataIntoDB(materialDto, title, data)) {
							JOptionPane.showMessageDialog(null, "OK");
							refreshMaterialTable();
							refreshOrderTable();
						} else {
							JOptionPane.showMessageDialog(null, "fail");
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
			}

		}

	}

	class MyTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 17L;
		private Object[][] myData;
		private String[] title;
		private int[] editableCol;
		private int rowCount = 0;
		private int colCount = 0;

		public MyTableModel(String[] tableTitle, Object[][] tableData, int... editableCol) {
			super();
			title = tableTitle;
			myData = tableData;
			colCount = tableTitle.length;
			if (null != tableData)
				rowCount = tableData.length;
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

		public void setValueAt(Object value, int row, int column) {
			myData[row][column] = value;
		}

		public String getColumnName(int column) {
			return title[column];
		}

		public boolean isCellEditable(int row, int column) {
			if (null != editableCol && editableCol.length > 0) {
				for (int i = 0; i < editableCol.length; i++) {
					if (column == editableCol[i])
						return true;
				}
			}
			return false;
		}

	}

}
