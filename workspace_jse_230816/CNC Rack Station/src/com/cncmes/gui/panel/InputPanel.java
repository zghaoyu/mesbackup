package com.cncmes.gui.panel;

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.Dimension;
import java.awt.BorderLayout;
import javax.swing.border.LineBorder;

import com.cncmes.base.PermissionItems;
import com.cncmes.dto.CNCLine;
import com.cncmes.dto.Fixture;
import com.cncmes.dto.Rack;
//import com.cncmes.dto.SubOrder;
import com.cncmes.gui.listener.InputPanelListener;
import com.cncmes.gui.listener.JComboBoxListener;
import com.cncmes.gui.model.InputTableModel;
import com.cncmes.gui.model.LineComboBoxModel;
import com.cncmes.gui.model.RackComboBoxModel;
import com.cncmes.utils.GUIUtils;
import com.cncmes.utils.LoginSystem;

import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Font;
import java.awt.event.ItemEvent;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JComboBox;

/**
 * �о��������������󶨵Ľ���
 * @author W000586 Hui Zhi 2022/3/2
 *
 */
public class InputPanel extends JPanel {


	private static final long serialVersionUID = -4900446293028375745L;

	private static InputPanel inputPanel = new InputPanel();

	private JPanel panel_input;
	private JPanel panel_input_title;
	private JPanel panel_input_content;

	private JLabel lblInput;
	private JLabel lblFixtureNo;
	private JLabel lblMaterialNo;
	private JLabel lblMessage;
	private JLabel lblSuborder;

	private JTextField input_fixture;
	private JTextField input_material;
	private JScrollPane scrollPane;
	private JTable table;
	private JButton btnAdd;
	private JButton btnClear;
	private JLabel lblLineName;
	private JLabel lblRack;
//	private JComboBox<SubOrder> comboBoxSubOrder;
	private JComboBox<CNCLine> comboBoxLineName;
	private JComboBox<Rack> comboBoxRack;
	private JButton btnSubmit;
	private JButton btnConfirm;
	private JPanel panel;
	private JLabel lblQty;
	private JLabel lblMaterialCount;
	private JButton btnDelete;
	private JButton btnDeleteAll;

	private Fixture currentFixture;
	private String[] tableTitle;
	private InputTableModel emptyTableModel;
	private CNCLine cncLine = null;
	private Rack rack = null;
	/**
	 * Create the panel.
	 */
	private InputPanel() {

		this.setBackground(new Color(245, 245, 245));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 10, 7, 10, 0 };
		gridBagLayout.rowHeights = new int[] { 10, 5, 10, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		panel_input = new JPanel();
		panel_input.setBorder(new LineBorder(Color.LIGHT_GRAY));
		panel_input.setBackground(Color.WHITE);
		GridBagConstraints gbc_panel_input = new GridBagConstraints();
		gbc_panel_input.insets = new Insets(0, 0, 5, 5);
		gbc_panel_input.fill = GridBagConstraints.BOTH;
		gbc_panel_input.gridx = 1;
		gbc_panel_input.gridy = 1;
		add(panel_input, gbc_panel_input);
		panel_input.setLayout(new BorderLayout(0, 0));

		panel_input_title = new JPanel();
		panel_input_title.setPreferredSize(new Dimension(10, 25));
		panel_input_title.setBackground(SystemColor.controlHighlight);
		panel_input.add(panel_input_title, BorderLayout.NORTH);

		lblInput = new JLabel("Input");
		lblInput.setFont(new Font("Tahoma", Font.BOLD, 12));
		panel_input_title.add(lblInput);

		panel_input_content = new JPanel();
		panel_input_content.setBackground(Color.WHITE);
		panel_input.add(panel_input_content, BorderLayout.CENTER);
		GridBagLayout gbl_panel_input_content = new GridBagLayout();
		gbl_panel_input_content.columnWidths = new int[] { 10, 0, 120, 0, 30, 0, 120, 0, 0, 5, 0 };
		gbl_panel_input_content.rowHeights = new int[] { 35, 0, 20, 25, 10, 25, 25, 25, 0, 0, 0, 5, 0 };
		gbl_panel_input_content.columnWeights = new double[] { 0.0, 1.0, 1.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		gbl_panel_input_content.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		panel_input_content.setLayout(gbl_panel_input_content);

		lblLineName = new JLabel("Line Name :");
		lblLineName.setFont(GUIUtils.contentFont);
		GridBagConstraints gbc_lblLineName = new GridBagConstraints();
		gbc_lblLineName.anchor = GridBagConstraints.EAST;
		gbc_lblLineName.insets = new Insets(0, 0, 5, 5);
		gbc_lblLineName.gridx = 1;
		gbc_lblLineName.gridy = 2;
		panel_input_content.add(lblLineName, gbc_lblLineName);

		comboBoxLineName = new JComboBox<CNCLine>();
		comboBoxLineName.setFont(GUIUtils.contentFont);

		comboBoxLineName.setModel(new LineComboBoxModel());
		comboBoxLineName.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_comboBoxLineName = new GridBagConstraints();
		gbc_comboBoxLineName.insets = new Insets(0, 0, 5, 5);
		gbc_comboBoxLineName.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxLineName.gridx = 2;
		gbc_comboBoxLineName.gridy = 2;
		panel_input_content.add(comboBoxLineName, gbc_comboBoxLineName);

		lblRack = new JLabel("Rack Name :");
		lblRack.setFont(GUIUtils.contentFont);
		GridBagConstraints gbc_lblRackId = new GridBagConstraints();
		gbc_lblRackId.insets = new Insets(0, 0, 5, 5);
		gbc_lblRackId.anchor = GridBagConstraints.EAST;
		gbc_lblRackId.gridx = 5;
		gbc_lblRackId.gridy = 2;
		panel_input_content.add(lblRack, gbc_lblRackId);

		comboBoxRack = new JComboBox<Rack>();
		comboBoxRack.setFont(GUIUtils.contentFont);
		comboBoxRack.setModel(new RackComboBoxModel(0));
		comboBoxRack.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_comboBoxRack = new GridBagConstraints();
		gbc_comboBoxRack.insets = new Insets(0, 0, 5, 5);
		gbc_comboBoxRack.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxRack.gridx = 6;
		gbc_comboBoxRack.gridy = 2;
		panel_input_content.add(comboBoxRack, gbc_comboBoxRack);
		comboBoxRack.addItemListener(e -> {
			if(e.getStateChange() == ItemEvent.SELECTED)
			{
				rack = (Rack) comboBoxRack.getSelectedItem();

			}
		});

		lblFixtureNo = new JLabel("Fixture No :");
		lblFixtureNo.setFont(GUIUtils.contentFont);
		GridBagConstraints gbc_lblFixtureNo_2 = new GridBagConstraints();
		gbc_lblFixtureNo_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblFixtureNo_2.anchor = GridBagConstraints.EAST;
		gbc_lblFixtureNo_2.gridx = 1;
		gbc_lblFixtureNo_2.gridy = 4;
		panel_input_content.add(lblFixtureNo, gbc_lblFixtureNo_2);

		input_fixture = new JTextField();
		input_fixture.setFont(GUIUtils.contentFont);
		GridBagConstraints gbc_Input_fixture = new GridBagConstraints();
		gbc_Input_fixture.insets = new Insets(0, 0, 5, 5);
		gbc_Input_fixture.fill = GridBagConstraints.HORIZONTAL;
		gbc_Input_fixture.gridx = 2;
		gbc_Input_fixture.gridy = 4;
		panel_input_content.add(input_fixture, gbc_Input_fixture);
		input_fixture.setColumns(10);


		btnConfirm = new JButton("Confirm");
		btnConfirm.setFont(GUIUtils.contentFont);

		GridBagConstraints gbc_btnConfirm = new GridBagConstraints();
		gbc_btnConfirm.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnConfirm.insets = new Insets(0, 0, 5, 5);
		gbc_btnConfirm.gridx = 3;
		gbc_btnConfirm.gridy = 4;
		panel_input_content.add(btnConfirm, gbc_btnConfirm);

		lblMaterialNo = new JLabel("Material No :");
		lblMaterialNo.setFont(GUIUtils.contentFont);
		GridBagConstraints gbc_lblMaterialNo = new GridBagConstraints();
		gbc_lblMaterialNo.anchor = GridBagConstraints.EAST;
		gbc_lblMaterialNo.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaterialNo.gridx = 5;
		gbc_lblMaterialNo.gridy = 4;
		panel_input_content.add(lblMaterialNo, gbc_lblMaterialNo);

		input_material = new JTextField();
		input_material.setFont(GUIUtils.contentFont);
		GridBagConstraints gbc_Input_material_1 = new GridBagConstraints();
		gbc_Input_material_1.insets = new Insets(0, 0, 5, 5);
		gbc_Input_material_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_Input_material_1.gridx = 6;
		gbc_Input_material_1.gridy = 4;
		panel_input_content.add(input_material, gbc_Input_material_1);
		input_material.setColumns(10);

		btnAdd = new JButton("Add");
		btnAdd.setFont(GUIUtils.contentFont);
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAdd.insets = new Insets(0, 0, 5, 5);
		gbc_btnAdd.gridx = 7;
		gbc_btnAdd.gridy = 4;
		panel_input_content.add(btnAdd, gbc_btnAdd);

		btnClear = new JButton("Clear");
		btnClear.setFont(GUIUtils.contentFont);

		GridBagConstraints gbc_btnClear = new GridBagConstraints();
		gbc_btnClear.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnClear.insets = new Insets(0, 0, 5, 5);
		gbc_btnClear.gridx = 8;
		gbc_btnClear.gridy = 4;
		panel_input_content.add(btnClear, gbc_btnClear);
//add binding sub order
//		lblSuborder = new JLabel("sub order No :");
//		lblSuborder.setFont(GUIUtils.contentFont);
//		GridBagConstraints gbc_lblSuborder_3 = new GridBagConstraints();
//		gbc_lblSuborder_3.insets = new Insets(0, 0, 5, 5);
//		gbc_lblSuborder_3.anchor = GridBagConstraints.EAST;
//		gbc_lblSuborder_3.gridx = 1;
//		gbc_lblSuborder_3.gridy = 6;
//		panel_input_content.add(lblSuborder, gbc_lblSuborder_3);
//
//		input_fixture = new JTextField();
//		input_fixture.setFont(GUIUtils.contentFont);
//		GridBagConstraints gbc_Input_fixture1 = new GridBagConstraints();
//		gbc_Input_fixture1.insets = new Insets(0, 0, 5, 5);
//		gbc_Input_fixture1.fill = GridBagConstraints.HORIZONTAL;
//		gbc_Input_fixture1.gridx = 2;
//		gbc_Input_fixture1.gridy = 6;
//		panel_input_content.add(input_fixture, gbc_Input_fixture1);
//		input_fixture.setColumns(10);


		lblMessage = new JLabel("");
		lblMessage.setFont(GUIUtils.contentFont);
		lblMessage.setForeground(Color.RED);
		GridBagConstraints gbc_labelMessage = new GridBagConstraints();
		gbc_labelMessage.gridwidth = 5;
		gbc_labelMessage.insets = new Insets(0, 0, 5, 5);
		gbc_labelMessage.gridx = 2;
		gbc_labelMessage.gridy = 6;
		panel_input_content.add(lblMessage, gbc_labelMessage);

		btnDelete = new JButton("Delete");
		btnDelete.setFont(GUIUtils.contentFont);
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.insets = new Insets(0, 0, 5, 5);
		gbc_btnDelete.gridx = 7;
		gbc_btnDelete.gridy = 7;
		panel_input_content.add(btnDelete, gbc_btnDelete);

		btnDeleteAll = new JButton("Delete All");
		btnDeleteAll.setFont(GUIUtils.contentFont);
		GridBagConstraints gbc_btnDeleteAll = new GridBagConstraints();
		gbc_btnDeleteAll.insets = new Insets(0, 0, 5, 5);
		gbc_btnDeleteAll.gridx = 8;
		gbc_btnDeleteAll.gridy = 7;
		panel_input_content.add(btnDeleteAll, gbc_btnDeleteAll);

		scrollPane = new JScrollPane();
		// scrollPane.setOpaque(false);
		// scrollPane.getViewport().setOpaque(false);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 8;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 8;
		panel_input_content.add(scrollPane, gbc_scrollPane);

		table = new JTable();
		table.setFont(new Font("Tahoma", Font.PLAIN, 11));
		table.setSelectionBackground(new Color(135, 206, 250));
		table.setRowHeight(30);
		table.getTableHeader().setReorderingAllowed(false);
		tableTitle = new String[] { "ID", "Material No", "Serial No", "Drawing No", "Scan Time" };
		table.setModel(new InputTableModel(null, tableTitle));
		scrollPane.setViewportView(table);

		panel = new JPanel();
		panel.setBackground(Color.WHITE);
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 8;
		gbc_panel.insets = new Insets(0, 0, 5, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 9;
		panel_input_content.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 10, 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 10, 0, 10, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		lblQty = new JLabel("Material QTY:");
		lblQty.setFont(GUIUtils.contentFont);
		GridBagConstraints gbc_lblQty = new GridBagConstraints();
		gbc_lblQty.insets = new Insets(0, 0, 5, 5);
		gbc_lblQty.gridx = 1;
		gbc_lblQty.gridy = 1;
		panel.add(lblQty, gbc_lblQty);

		lblMaterialCount = new JLabel("0");
		lblMaterialCount.setFont(GUIUtils.contentFont);
		GridBagConstraints gbc_lblMaterialCount = new GridBagConstraints();
		gbc_lblMaterialCount.insets = new Insets(0, 0, 5, 0);
		gbc_lblMaterialCount.gridx = 2;
		gbc_lblMaterialCount.gridy = 1;
		panel.add(lblMaterialCount, gbc_lblMaterialCount);

		btnSubmit = new JButton("Submit");
		btnSubmit.setFont(GUIUtils.contentFont);
		GridBagConstraints gbc_btnSubmit = new GridBagConstraints();
		gbc_btnSubmit.insets = new Insets(0, 0, 5, 5);
		gbc_btnSubmit.gridx = 8;
		gbc_btnSubmit.gridy = 10;
		panel_input_content.add(btnSubmit, gbc_btnSubmit);
		btnSubmit.setVisible(false);

		emptyTableModel = new InputTableModel(null, tableTitle);
		addListener();
		clearAllData();

		refreshButtonsEnabled();
	}

	private void addListener() {
		InputPanelListener pListener = new InputPanelListener();
		btnAdd.addActionListener(pListener);
		btnClear.addActionListener(pListener);
		btnConfirm.addActionListener(pListener);
		btnSubmit.addActionListener(pListener);
		btnDelete.addActionListener(pListener);
		btnDeleteAll.addActionListener(pListener);

		JComboBoxListener jListener = new JComboBoxListener();
		comboBoxLineName.addItemListener(jListener);

	}

	public void clearAllData() {
		input_fixture.setText("");
		input_material.setText("");
		setFixtureEditable(true);
		setMaterialEditable(false);
		setCurrentFixture(null);
		GUIUtils.setLabelText(lblMessage, "");
		clearTable();
	}
	public void refreshButtonsEnabled(){
		btnConfirm.setEnabled(!LoginSystem.accessDenied(PermissionItems.SYSCONFIG));
	}

	public void clearTable() {
		refreshTable(emptyTableModel);
	}

	public void refreshTable(InputTableModel model) {
		table.setModel(model);
		refreshMaterialQty();
	}

	private void refreshMaterialQty() {
		int qty = table.getModel().getRowCount();
		GUIUtils.setLabelText(lblMaterialCount, "" + qty);
	}

	public void setFixtureEditable(Boolean b) {
		input_fixture.setEditable(b);
		btnConfirm.setEnabled(b);
	}

	public void setMaterialEditable(Boolean b) {
		input_material.setEditable(b);
		btnAdd.setEnabled(b);
	}

	public String[] getTableTitle() {
		return tableTitle;
	}

	public static InputPanel getInstance() {
		return inputPanel;
	}

	public JTextField getInput_fixture() {
		return input_fixture;
	}

	public JTextField getInput_material() {
		return input_material;
	}

	public JLabel getLblMessage() {
		return lblMessage;
	}

	public JLabel getLblMaterialCount() {
		return lblMaterialCount;
	}

	public JButton getBtnAdd() {
		return btnAdd;
	}

	public JButton getBtnClear() {
		return btnClear;
	}

	public JButton getBtnSubmit() {
		return btnSubmit;
	}

	public JButton getBtnConfirm() {
		return btnConfirm;
	}

	public JButton getBtnDelete() {
		return btnDelete;
	}

	public JButton getBtnDeleteAll() {
		return btnDeleteAll;
	}

	public JComboBox<CNCLine> getComboBoxLineName() {
		return comboBoxLineName;
	}

	public JComboBox<Rack> getComboBoxRack() {
		return comboBoxRack;
	}

	public JTable getTable() {
		return table;
	}

	public Fixture getCurrentFixture() {
		return currentFixture;
	}

	public void setCurrentFixture(Fixture currentFixture) {
		this.currentFixture = currentFixture;
	}

	public CNCLine getCncLine() {
		return cncLine;
	}

	public void setCncLine(CNCLine cncLine) {
		this.cncLine = cncLine;
	}

	public Rack getRack() {
		return rack;
	}

	public void setRack(Rack rack) {
		this.rack = rack;
	}
}
