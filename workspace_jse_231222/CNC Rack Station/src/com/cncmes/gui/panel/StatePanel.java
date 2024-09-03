package com.cncmes.gui.panel;

import java.awt.Color;

import javax.swing.*;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.BorderLayout;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ItemEvent;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.cncmes.dto.CNCLine;
import com.cncmes.dto.Rack;
import com.cncmes.gui.listener.JComboBoxListener;
import com.cncmes.gui.listener.StatePanelListener;
import com.cncmes.gui.model.LineComboBoxModel;
import com.cncmes.gui.model.RackComboBoxModel;

/**
 * 
 * @author W000586 Hui Zhi Fang 2022/3/2
 */
public class StatePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4855534237842180231L;

	private static StatePanel statePanel = new StatePanel();
	private JTable table;
	private JTextField textField;
	private JComboBox<CNCLine> comboBoxLineName;
	private JComboBox<Rack> comboBoxRack;
	private JLabel lblRack;
	private JLabel rackCapacity;
	private JButton btnSearch;
	private JRadioButton rdbtnNewRadioButton;
	private JRadioButton rdbtnPlaced;
	private JRadioButton rdbtnEmpty;
	private String[] tableTitle = new String[]{"Sequence","Fixture No","Materials","State","Input Time"};
	private CNCLine cncLine = null;
	private Rack rack = null;
	/**
	 * Create the panel.
	 */
	public StatePanel() {
		this.setBackground(new Color(245, 245, 245));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 8, 0, 0, 0, 0, 4 };
		gridBagLayout.rowHeights = new int[] { 7, 250, 500, 5 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 1.0, 1.0, 1.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 1.0, 0.0 };
		setLayout(gridBagLayout);

		JPanel statePanel = new JPanel();
		statePanel.setBorder(new LineBorder(Color.LIGHT_GRAY));
		statePanel.setBackground(new Color(255, 255, 255));
		GridBagConstraints gbc_statePanel = new GridBagConstraints();
		gbc_statePanel.insets = new Insets(0, 0, 5, 5);
		gbc_statePanel.fill = GridBagConstraints.BOTH;
		gbc_statePanel.gridx = 1;
		gbc_statePanel.gridy = 1;
		add(statePanel, gbc_statePanel);
		statePanel.setLayout(new BorderLayout(0, 0));

		JPanel stateTitle = new JPanel();
		stateTitle.setBackground(SystemColor.controlHighlight);
		stateTitle.setPreferredSize(new Dimension(10, 25));
		statePanel.add(stateTitle, BorderLayout.NORTH);

		JLabel lblState = new JLabel("Rack Info");
		lblState.setFont(new Font("Tahoma", Font.PLAIN, 14));
		stateTitle.add(lblState);

		JPanel stateContent = new JPanel();
		stateContent.setBackground(new Color(255, 255, 255));
		statePanel.add(stateContent, BorderLayout.CENTER);
		GridBagLayout gbl_stateContent = new GridBagLayout();
		gbl_stateContent.columnWidths = new int[] { 0, 0, 0 };
		gbl_stateContent.rowHeights = new int[] { 30, 30 };
		gbl_stateContent.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_stateContent.rowWeights = new double[] { 0.0, 0.0 };
		stateContent.setLayout(gbl_stateContent);

		JLabel lblNewLabel = new JLabel("Line Name :");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		stateContent.add(lblNewLabel, gbc_lblNewLabel);

		comboBoxLineName = new JComboBox<>();
		comboBoxLineName.setPreferredSize(new Dimension(80, 20));
		comboBoxLineName.setModel(new LineComboBoxModel());
		GridBagConstraints gbc_comboBoxLineName = new GridBagConstraints();
		gbc_comboBoxLineName.anchor = GridBagConstraints.WEST;
		gbc_comboBoxLineName.insets = new Insets(0, 0, 5, 0);
		gbc_comboBoxLineName.gridx = 1;
		gbc_comboBoxLineName.gridy = 0;
		stateContent.add(comboBoxLineName, gbc_comboBoxLineName);

		lblRack = new JLabel("Rack Name :");
		lblRack.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_lblRack = new GridBagConstraints();
		gbc_lblRack.anchor = GridBagConstraints.EAST;
		gbc_lblRack.insets = new Insets(0, 0, 0, 5);
		gbc_lblRack.gridx = 0;
		gbc_lblRack.gridy = 1;
		stateContent.add(lblRack, gbc_lblRack);

		comboBoxRack = new JComboBox<>();
		comboBoxRack.setPreferredSize(new Dimension(80, 20));
		comboBoxRack.setModel(new RackComboBoxModel(0));
		GridBagConstraints gbc_comboBox_1 = new GridBagConstraints();
		gbc_comboBox_1.anchor = GridBagConstraints.WEST;
		gbc_comboBox_1.gridx = 1;
		gbc_comboBox_1.gridy = 1;
		stateContent.add(comboBoxRack, gbc_comboBox_1);
		comboBoxRack.addItemListener(e -> {
			if(e.getStateChange() == ItemEvent.SELECTED)
			{
				rack = (Rack) comboBoxRack.getSelectedItem();
				rackCapacity.setText(String.valueOf(rack.getCapacity()));

			}
		});


		JPanel storagePanel = new JPanel();
		storagePanel.setBorder(new LineBorder(Color.LIGHT_GRAY));
		storagePanel.setBackground(new Color(255, 255, 255));
		GridBagConstraints gbc_storagePanel = new GridBagConstraints();
		gbc_storagePanel.insets = new Insets(0, 0, 5, 5);
		gbc_storagePanel.fill = GridBagConstraints.BOTH;
		gbc_storagePanel.gridx = 2;
		gbc_storagePanel.gridy = 1;
		add(storagePanel, gbc_storagePanel);
		storagePanel.setLayout(new BorderLayout(0, 0));

		JPanel storageTitle = new JPanel();
		storageTitle.setPreferredSize(new Dimension(10, 25));
		storageTitle.setBackground(SystemColor.controlHighlight);
		storagePanel.add(storageTitle, BorderLayout.NORTH);

		JLabel lblSlots = new JLabel("Storage");
		lblSlots.setFont(new Font("Tahoma", Font.PLAIN, 14));
		storageTitle.add(lblSlots);

		JPanel panel_2 = new JPanel();
		panel_2.setBackground(Color.WHITE);
		storagePanel.add(panel_2, BorderLayout.CENTER);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] { 0, 0, 0 };
		gbl_panel_2.rowHeights = new int[] { 30 };
		gbl_panel_2.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_panel_2.rowWeights = new double[] { 0.0 };
		panel_2.setLayout(gbl_panel_2);

		JLabel lblCapacity = new JLabel("Capacity:");
		lblCapacity.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_lblCapacity = new GridBagConstraints();
		gbc_lblCapacity.anchor = GridBagConstraints.EAST;
		gbc_lblCapacity.insets = new Insets(0, 0, 0, 5);
		gbc_lblCapacity.gridx = 0;
		gbc_lblCapacity.gridy = 0;
		panel_2.add(lblCapacity, gbc_lblCapacity);

		rackCapacity = new JLabel("0");
		rackCapacity.setFont(new Font("Tahoma", Font.PLAIN, 14));
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.WEST;
		gbc_label.gridx = 1;
		gbc_label.gridy = 0;
		panel_2.add(rackCapacity, gbc_label);

		JPanel conveyorPanel = new JPanel();
		conveyorPanel.setBorder(new LineBorder(Color.LIGHT_GRAY));
		conveyorPanel.setBackground(new Color(255, 255, 255));
		GridBagConstraints gbc_conveyorPanel = new GridBagConstraints();
		gbc_conveyorPanel.insets = new Insets(0, 0, 5, 5);
		gbc_conveyorPanel.fill = GridBagConstraints.BOTH;
		gbc_conveyorPanel.gridx = 3;
		gbc_conveyorPanel.gridy = 1;
		add(conveyorPanel, gbc_conveyorPanel);
		conveyorPanel.setLayout(new BorderLayout(0, 0));

		JPanel conveyorTitle = new JPanel();
		conveyorPanel.add(conveyorTitle, BorderLayout.NORTH);
		conveyorTitle.setPreferredSize(new Dimension(10, 25));
		conveyorTitle.setBackground(SystemColor.controlHighlight);

		JLabel lblDeviceState = new JLabel("Device State");
		conveyorTitle.add(lblDeviceState);
		lblDeviceState.setFont(new Font("Tahoma", Font.PLAIN, 14));

		JPanel conveyorContent = new JPanel();
		conveyorContent.setBackground(new Color(255, 255, 255));
		conveyorPanel.add(conveyorContent, BorderLayout.CENTER);
		conveyorContent.setLayout(new BorderLayout(0, 0));

		JPanel panel_6 = new JPanel();
		panel_6.setBackground(Color.WHITE);
		conveyorContent.add(panel_6, BorderLayout.CENTER);
		panel_6.setLayout(new BorderLayout(0, 0));

		JLabel label_2 = new JLabel("Running");
		label_2.setHorizontalAlignment(SwingConstants.CENTER);
		label_2.setFont(new Font("Tahoma", Font.PLAIN, 25));
		panel_6.add(label_2);

		JPanel GripperPanel = new JPanel();
		GripperPanel.setBorder(new LineBorder(new Color(192, 192, 192)));
		GripperPanel.setBackground(new Color(255, 255, 255));
		GridBagConstraints gbc_GripperPanel = new GridBagConstraints();
		gbc_GripperPanel.insets = new Insets(0, 0, 5, 5);
		gbc_GripperPanel.fill = GridBagConstraints.BOTH;
		gbc_GripperPanel.gridx = 4;
		gbc_GripperPanel.gridy = 1;
		add(GripperPanel, gbc_GripperPanel);
		GripperPanel.setLayout(new BorderLayout(0, 0));

		JPanel gripperTitle = new JPanel();
		gripperTitle.setPreferredSize(new Dimension(10, 25));
		gripperTitle.setBackground(SystemColor.controlHighlight);
		GripperPanel.add(gripperTitle, BorderLayout.NORTH);

		JLabel lblGripper = new JLabel("Automation State");
		gripperTitle.add(lblGripper);
		lblGripper.setFont(new Font("Tahoma", Font.PLAIN, 14));

		JPanel gripperContent = new JPanel();
		gripperContent.setBackground(new Color(255, 255, 255));
		GripperPanel.add(gripperContent, BorderLayout.CENTER);
		gripperContent.setLayout(new BorderLayout(0, 0));

		JLabel lblGripperContent = new JLabel("Stop");
		lblGripperContent.setHorizontalAlignment(SwingConstants.CENTER);
		lblGripperContent.setFont(new Font("Tahoma", Font.PLAIN, 25));
		gripperContent.add(lblGripperContent);

		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new LineBorder(Color.LIGHT_GRAY));
		panel_3.setBackground(new Color(255, 255, 255));
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.insets = new Insets(0, 0, 5, 5);
		gbc_panel_3.gridwidth = 4;
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 1;
		gbc_panel_3.gridy = 2;
		add(panel_3, gbc_panel_3);
		panel_3.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setBackground(SystemColor.controlHighlight);
		panel.setPreferredSize(new Dimension(10, 25));
		panel_3.add(panel, BorderLayout.NORTH);

		JPanel panel_1 = new JPanel();
		panel_1.setBackground(Color.WHITE);
		panel_3.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		JPanel panel_4 = new JPanel();
		panel_4.setPreferredSize(new Dimension(10, 50));
		panel_1.add(panel_4, BorderLayout.NORTH);
		GridBagLayout gbl_panel_4 = new GridBagLayout();
		gbl_panel_4.columnWidths = new int[] { 20, 0, 30, 0, 70, 0, 70, 0, 20, 0 };
		gbl_panel_4.rowHeights = new int[] { 10, 0, 0, 0 };
		gbl_panel_4.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_4.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel_4.setLayout(gbl_panel_4);



		rdbtnNewRadioButton = new JRadioButton("ALL");
		rdbtnNewRadioButton.setSelected(true);
		GridBagConstraints gbc_rdbtnNewRadioButton = new GridBagConstraints();
		gbc_rdbtnNewRadioButton.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnNewRadioButton.gridx = 1;
		gbc_rdbtnNewRadioButton.gridy = 1;
		panel_4.add(rdbtnNewRadioButton, gbc_rdbtnNewRadioButton);

		rdbtnPlaced = new JRadioButton("Standby");
		GridBagConstraints gbc_rdbtnPlaced = new GridBagConstraints();
		gbc_rdbtnPlaced.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnPlaced.gridx = 2;
		gbc_rdbtnPlaced.gridy = 1;
		panel_4.add(rdbtnPlaced, gbc_rdbtnPlaced);

		rdbtnEmpty = new JRadioButton("Delete");
		GridBagConstraints gbc_rdbtnEmpty = new GridBagConstraints();
		gbc_rdbtnEmpty.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnEmpty.gridx = 3;
		gbc_rdbtnEmpty.gridy = 1;
		panel_4.add(rdbtnEmpty, gbc_rdbtnEmpty);

		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnNewRadioButton);
		group.add(rdbtnPlaced);
		group.add(rdbtnEmpty);


		JLabel lblSearch = new JLabel("Fixture\uFF1A");
		GridBagConstraints gbc_lblSearch = new GridBagConstraints();
		gbc_lblSearch.anchor = GridBagConstraints.EAST;
		gbc_lblSearch.insets = new Insets(0, 0, 5, 5);
		gbc_lblSearch.gridx = 5;
		gbc_lblSearch.gridy = 1;
		panel_4.add(lblSearch, gbc_lblSearch);

		textField = new JTextField();
		textField.setPreferredSize(new Dimension(70, 20));
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.gridx = 6;
		gbc_textField.gridy = 1;
		panel_4.add(textField, gbc_textField);
		textField.setColumns(10);

		btnSearch = new JButton("Search");
		GridBagConstraints gbc_btnSearch = new GridBagConstraints();
		gbc_btnSearch.insets = new Insets(0, 0, 5, 5);
		gbc_btnSearch.gridx = 7;
		gbc_btnSearch.gridy = 1;
		panel_4.add(btnSearch, gbc_btnSearch);

		JPanel panel_5 = new JPanel();
		panel_1.add(panel_5, BorderLayout.CENTER);
		panel_5.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		panel_5.add(scrollPane);

		table = new JTable();
		table.setSelectionBackground(new Color(135, 206, 250));
		table.setRowHeight(30);
		table.setModel(new DefaultTableModel(
			new Object[][] {
				{new Integer(1), null, null, null, null},
				{new Integer(2), null, null, null, null},
				{new Integer(3), null, null, null, null},
				{new Integer(4), null, null, null, null},
				{new Integer(5), null, null, null, null},
				{new Integer(6), null, null, null, null},


			},
			new String[] {
				"Sequence", "Fixture No", "Materials", "State", "Input Time"
			}
		));
		DefaultTableCellRenderer r = new DefaultTableCellRenderer();
		r.setHorizontalAlignment(JLabel.CENTER);
		table.setDefaultRenderer(Object.class, r);
		scrollPane.setViewportView(table);
		addListener();
	}

	private void addListener() {
		StatePanelListener statePanelListener = new StatePanelListener();
		JComboBoxListener jListener = new JComboBoxListener();
		comboBoxLineName.addItemListener(jListener);
		btnSearch.addActionListener(statePanelListener);
	}

	public JRadioButton getRdbtnNewRadioButton() {
		return rdbtnNewRadioButton;
	}

	public void setRdbtnNewRadioButton(JRadioButton rdbtnNewRadioButton) {
		this.rdbtnNewRadioButton = rdbtnNewRadioButton;
	}

	public JRadioButton getRdbtnPlaced() {
		return rdbtnPlaced;
	}

	public void setRdbtnPlaced(JRadioButton rdbtnPlaced) {
		this.rdbtnPlaced = rdbtnPlaced;
	}

	public JRadioButton getRdbtnEmpty() {
		return rdbtnEmpty;
	}

	public void setRdbtnEmpty(JRadioButton rdbtnEmpty) {
		this.rdbtnEmpty = rdbtnEmpty;
	}

	public JTable getTable() {
		return table;
	}

	public void setTable(JTable table) {
		this.table = table;
	}

	public JButton getBtnSearch() {
		return btnSearch;
	}

	public void setBtnSearch(JButton btnSearch) {
		this.btnSearch = btnSearch;
	}

	public JTextField getTextField() {
		return textField;
	}

	public void setTextField(JTextField textField) {
		this.textField = textField;
	}

	public static StatePanel getInstance() {
		return statePanel;
	}

	public JComboBox<CNCLine> getComboBoxLineName() {
		return comboBoxLineName;
	}

	public JComboBox<Rack> getComboBoxRack() {
		return comboBoxRack;
	}
	public JLabel getRackCapacity(){
		return rackCapacity;
	}

	public String[] getTableTitle() {
		return tableTitle;
	}

	public void setTableTitle(String[] tableTitle) {
		this.tableTitle = tableTitle;
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
