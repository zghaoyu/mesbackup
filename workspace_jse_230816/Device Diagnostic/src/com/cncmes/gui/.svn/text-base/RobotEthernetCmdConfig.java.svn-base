package com.cncmes.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import com.cncmes.data.CncDriver;
import com.cncmes.data.RobotDriver;
import com.cncmes.data.RobotEthernetCmd;
import com.cncmes.drv.RobotR2D2;
import com.cncmes.handler.SocketDataHandler;
import com.cncmes.utils.GUIUtils;
import com.cncmes.utils.ThreadUtils;
import com.cncmes.utils.XmlUtils;
import javax.swing.JCheckBox;

public class RobotEthernetCmdConfig extends JDialog {
	private static final long serialVersionUID = 25L;
	private final JPanel contentPanel = new JPanel();
	private static RobotEthernetCmdConfig robotNetworkCmdConfig = new RobotEthernetCmdConfig();
	private static boolean loopingOperation;
	private JTextField textField_CommandName;
	private JLabel lblCommandName;
	private JLabel lblRunningMsg;
	private JTable tableCommonParameters;
	private JTable tableInputParameters;
	private JList<String> listAllCommands;
	private JComboBox<String> comboBoxRobotModel;
	private JComboBox<String> comboBoxTarget;
	private JComboBox<String> comboBoxOperation;
	
	private Map<String,Object> socketRespData= new LinkedHashMap<String,Object>();
	private String curRobotOperation;
	private String curRobotModel;
	private String curRobotTarget;
	private String curRobotCommand;
	
	private String cmdSeperator = ";";
	private JTextField textField_CommandID;
	private JTextField textFieldOperator;
	
	private JButton btnRun;
	private JButton btnExecuteCommand;
	
	public static RobotEthernetCmdConfig getInstance(){
		return robotNetworkCmdConfig;
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
            column.setWidth((int)(width*1.5)+myTable.getIntercellSpacing().width);
        }
	}
	
	private void setCurCommandParas(String...cmdName) {
		RobotEthernetCmd robotCmd = RobotEthernetCmd.getInstance();
		curRobotModel = comboBoxRobotModel.getSelectedItem().toString();
		curRobotTarget = comboBoxTarget.getSelectedItem().toString();
		String mainKey = curRobotModel + "#" + curRobotTarget;
		curRobotOperation = comboBoxOperation.getSelectedItem().toString();
		String[] cmds = robotCmd.getAllCommands(mainKey, curRobotOperation);
		if(null != cmdName && cmdName.length > 0 && !"".equals(cmdName[0])){
			curRobotCommand = cmdName[0];
		}else{
			curRobotCommand = cmds[0];
		}
		
		textField_CommandName.setText(curRobotCommand.split("#")[0]);
		textField_CommandID.setText(curRobotCommand.split("#")[1]);
		textFieldOperator.setText(robotCmd.getOperationExecutive(mainKey, curRobotOperation));
		
		lblRunningMsg.setText(curRobotModel+": "+curRobotOperation+"/"+curRobotCommand);
		GUIUtils.setJListContent(listAllCommands, cmds);
		GUIUtils.setJListSelectedIdx(listAllCommands, curRobotCommand);
		
		tableCommonParameters.setModel(new MyTableModel(robotCmd.getCommonConfigDataTitle(),robotCmd.getCommonConfigDataTable(mainKey)));
		tableInputParameters.setModel(new MyTableModel(robotCmd.getCommandParaDataTitle(),robotCmd.getCommandParaDataTable(mainKey, curRobotOperation, curRobotCommand)));
		fitTableColumns(tableCommonParameters);
		fitTableColumns(tableInputParameters);
		
	}
	
	private boolean executeCommand(boolean singleMode){
		String mainKey = curRobotModel+"#"+curRobotTarget;
		RobotEthernetCmd robotCmd = RobotEthernetCmd.getInstance();
		LinkedHashMap<String, Object> config = robotCmd.getCommonConfig(mainKey);
		if(null == config){
			lblRunningMsg.setText("Error: Load robot common settings failed");
			return false;
		}
		
		String debugIP = (String) config.get("debugIP");
		String debugPort = (String) config.get("debugPort");
		if(null == debugIP || null == debugPort){
			lblRunningMsg.setText("Error: Debug IP or Port error");
			return false;
		}
		
		LinkedHashMap<String, String> inParas = new LinkedHashMap<String, String>();
		inParas.put("port", debugPort);
		inParas.put("model", mainKey);
		if(singleMode) inParas.put("singleCmd", curRobotCommand);
		boolean success = RobotR2D2.getInstance().sendCommand(debugIP, curRobotOperation, inParas, null, curRobotTarget);
		if(!success){
			if(singleMode){
				lblRunningMsg.setText("Execute command "+curRobotCommand+" failed,check command log for the details");
			}else{
				lblRunningMsg.setText("Execute operation "+curRobotOperation+" failed,check command log for the details");
			}
		}else{
			if(singleMode){
				lblRunningMsg.setText("Execute command "+curRobotCommand+" OK");
			}else{
				lblRunningMsg.setText("Execute operation "+curRobotOperation+" OK");
			}
		}
		
		return success;
	}
	
	/**
	 * MyDataModel class is used to:
	 * 1. Show the specific data in a JTable
	 * 2. Specify which data column is editable
	 * @author Sanly
	 *
	 */
	class MyTableModel extends AbstractTableModel{
		private static final long serialVersionUID = 27L;
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
			if(0 == column){
				return false;
			}else{
				return true;
			}
		}
	}
	
	/**
	 * Create the dialog.
	 */
	private RobotEthernetCmdConfig() {
		XmlUtils.parseRobotEthernetCmdXml();
		setIconImage(Toolkit.getDefaultToolkit().getImage(RobotEthernetCmdConfig.class.getResource("/com/cncmes/img/robots_24.png")));
		setTitle("Robot Ethernet Command Config");
		setModal(true);
		setResizable(false);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 600;
		int height = 600;
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panelTop = new JPanel();
			panelTop.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
			contentPanel.add(panelTop, BorderLayout.NORTH);
			GridBagLayout gbl_panelTop = new GridBagLayout();
			gbl_panelTop.columnWidths = new int[] {40, 80, 40, 100, 70, 190, 55};
			gbl_panelTop.rowHeights = new int[] {30};
			gbl_panelTop.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0};
			gbl_panelTop.rowWeights = new double[]{0.0};
			panelTop.setLayout(gbl_panelTop);
			{
				JLabel lblRobotModel = new JLabel("Robot");
				GridBagConstraints gbc_lblRobotModel = new GridBagConstraints();
				gbc_lblRobotModel.insets = new Insets(0, 0, 0, 5);
				gbc_lblRobotModel.gridx = 0;
				gbc_lblRobotModel.gridy = 0;
				panelTop.add(lblRobotModel, gbc_lblRobotModel);
			}
			{
				comboBoxRobotModel = new JComboBox<String>();
				comboBoxRobotModel.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent arg0) {
						curRobotModel = comboBoxRobotModel.getSelectedItem().toString();
						curRobotTarget = comboBoxTarget.getSelectedItem().toString();
						String mainKey = curRobotModel + "#" + curRobotTarget;
						GUIUtils.setComboBoxValues(comboBoxOperation, RobotEthernetCmd.getInstance().getAllOperations(mainKey));
						setCurCommandParas();
					}
				});
				GridBagConstraints gbc_comboBoxRobotModel = new GridBagConstraints();
				gbc_comboBoxRobotModel.insets = new Insets(0, 0, 0, 5);
				gbc_comboBoxRobotModel.fill = GridBagConstraints.HORIZONTAL;
				gbc_comboBoxRobotModel.gridx = 1;
				gbc_comboBoxRobotModel.gridy = 0;
				panelTop.add(comboBoxRobotModel, gbc_comboBoxRobotModel);
			}
			{
				JLabel lblTarget = new JLabel("Target");
				GridBagConstraints gbc_lblTarget = new GridBagConstraints();
				gbc_lblTarget.insets = new Insets(0, 0, 0, 5);
				gbc_lblTarget.gridx = 2;
				gbc_lblTarget.gridy = 0;
				panelTop.add(lblTarget, gbc_lblTarget);
			}
			{
				comboBoxTarget = new JComboBox<String>();
				comboBoxTarget.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent arg0) {
						curRobotModel = comboBoxRobotModel.getSelectedItem().toString();
						curRobotTarget = comboBoxTarget.getSelectedItem().toString();
						String mainKey = curRobotModel + "#" + curRobotTarget;
						GUIUtils.setComboBoxValues(comboBoxOperation, RobotEthernetCmd.getInstance().getAllOperations(mainKey));
						setCurCommandParas();
					}
				});
				GridBagConstraints gbc_comboBoxTarget = new GridBagConstraints();
				gbc_comboBoxTarget.insets = new Insets(0, 0, 0, 5);
				gbc_comboBoxTarget.fill = GridBagConstraints.HORIZONTAL;
				gbc_comboBoxTarget.gridx = 3;
				gbc_comboBoxTarget.gridy = 0;
				panelTop.add(comboBoxTarget, gbc_comboBoxTarget);
			}
			{
				JLabel lblOperation = new JLabel("Operation");
				GridBagConstraints gbc_lblOperation = new GridBagConstraints();
				gbc_lblOperation.insets = new Insets(0, 0, 0, 5);
				gbc_lblOperation.gridx = 4;
				gbc_lblOperation.gridy = 0;
				panelTop.add(lblOperation, gbc_lblOperation);
			}
			{
				comboBoxOperation = new JComboBox<String>();
				comboBoxOperation.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						curRobotOperation = comboBoxOperation.getSelectedItem().toString();
						setCurCommandParas();
					}
				});
				GridBagConstraints gbc_comboBoxOperation = new GridBagConstraints();
				gbc_comboBoxOperation.insets = new Insets(0, 0, 0, 5);
				gbc_comboBoxOperation.fill = GridBagConstraints.HORIZONTAL;
				gbc_comboBoxOperation.gridx = 5;
				gbc_comboBoxOperation.gridy = 0;
				panelTop.add(comboBoxOperation, gbc_comboBoxOperation);
			}
			{
				btnRun = new JButton("Run");
				btnRun.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if(1 == e.getButton()){
							if("".equals(curRobotCommand)) return;
							if(!loopingOperation){
								btnRun.setEnabled(false);
								executeCommand(false);
								btnRun.setEnabled(true);
							}else{
								ThreadUtils.Run(new ExecuteCommand(false));
							}
						}
					}
				});
				GridBagConstraints gbc_btnRun = new GridBagConstraints();
				gbc_btnRun.gridx = 6;
				gbc_btnRun.gridy = 0;
				panelTop.add(btnRun, gbc_btnRun);
			}
		}
		{
			JPanel panelCenter = new JPanel();
			panelCenter.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
			contentPanel.add(panelCenter, BorderLayout.CENTER);
			panelCenter.setLayout(new BorderLayout(0, 0));
			{
				JPanel panelMainLeft = new JPanel();
				panelCenter.add(panelMainLeft, BorderLayout.WEST);
				GridBagLayout gbl_panelMainLeft = new GridBagLayout();
				gbl_panelMainLeft.columnWidths = new int[] {120, 0};
				gbl_panelMainLeft.rowHeights = new int[] {30, 470};
				gbl_panelMainLeft.columnWeights = new double[]{1.0, Double.MIN_VALUE};
				gbl_panelMainLeft.rowWeights = new double[]{0.0, 1.0};
				panelMainLeft.setLayout(gbl_panelMainLeft);
				{
					JLabel lblAllCommands = new JLabel("All Commands");
					GridBagConstraints gbc_lblAllCommands = new GridBagConstraints();
					gbc_lblAllCommands.insets = new Insets(0, 0, 5, 0);
					gbc_lblAllCommands.gridx = 0;
					gbc_lblAllCommands.gridy = 0;
					panelMainLeft.add(lblAllCommands, gbc_lblAllCommands);
				}
				{
					JScrollPane scrollPane_AllCommands = new JScrollPane();
					GridBagConstraints gbc_scrollPane_AllCommands = new GridBagConstraints();
					gbc_scrollPane_AllCommands.insets = new Insets(0, 0, 5, 0);
					gbc_scrollPane_AllCommands.fill = GridBagConstraints.BOTH;
					gbc_scrollPane_AllCommands.gridx = 0;
					gbc_scrollPane_AllCommands.gridy = 1;
					panelMainLeft.add(scrollPane_AllCommands, gbc_scrollPane_AllCommands);
					{
						listAllCommands = new JList<String>();
						listAllCommands.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent arg0) {
								if(1 == arg0.getButton()){
									String cmdName = listAllCommands.getSelectedValue().trim();
									setCurCommandParas(cmdName);
								}
							}
						});
						listAllCommands.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
						scrollPane_AllCommands.setViewportView(listAllCommands);
					}
				}
			}
			{
				JPanel panelMainCenter = new JPanel();
				panelMainCenter.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
				panelCenter.add(panelMainCenter, BorderLayout.CENTER);
				GridBagLayout gbl_panelMainCenter = new GridBagLayout();
				gbl_panelMainCenter.columnWidths = new int[] {455};
				gbl_panelMainCenter.rowHeights = new int[] {40, 190, 220, 30, 3};
				gbl_panelMainCenter.columnWeights = new double[]{1.0};
				gbl_panelMainCenter.rowWeights = new double[]{1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
				panelMainCenter.setLayout(gbl_panelMainCenter);
				{
					JPanel panelCfgTop = new JPanel();
					GridBagConstraints gbc_panelCfgTop = new GridBagConstraints();
					gbc_panelCfgTop.insets = new Insets(0, 0, 5, 0);
					gbc_panelCfgTop.fill = GridBagConstraints.BOTH;
					gbc_panelCfgTop.gridx = 0;
					gbc_panelCfgTop.gridy = 0;
					panelMainCenter.add(panelCfgTop, gbc_panelCfgTop);
					GridBagLayout gbl_panelCfgTop = new GridBagLayout();
					gbl_panelCfgTop.columnWidths = new int[] {80, 180, 20, 80};
					gbl_panelCfgTop.rowHeights = new int[] {30};
					gbl_panelCfgTop.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0};
					gbl_panelCfgTop.rowWeights = new double[]{0.0};
					panelCfgTop.setLayout(gbl_panelCfgTop);
					{
						lblCommandName = new JLabel(" Command Name");
						GridBagConstraints gbc_lblCommandName = new GridBagConstraints();
						gbc_lblCommandName.insets = new Insets(0, 0, 0, 5);
						gbc_lblCommandName.gridx = 0;
						gbc_lblCommandName.gridy = 0;
						panelCfgTop.add(lblCommandName, gbc_lblCommandName);
					}
					{
						textField_CommandName = new JTextField();
						textField_CommandName.setText("Start");
						GridBagConstraints gbc_textField_CommandName = new GridBagConstraints();
						gbc_textField_CommandName.insets = new Insets(0, 0, 0, 5);
						gbc_textField_CommandName.fill = GridBagConstraints.HORIZONTAL;
						gbc_textField_CommandName.gridx = 1;
						gbc_textField_CommandName.gridy = 0;
						panelCfgTop.add(textField_CommandName, gbc_textField_CommandName);
						textField_CommandName.setColumns(10);
					}
					{
						JLabel lblCommandId = new JLabel("ID");
						GridBagConstraints gbc_lblCommandId = new GridBagConstraints();
						gbc_lblCommandId.insets = new Insets(0, 0, 0, 5);
						gbc_lblCommandId.gridx = 2;
						gbc_lblCommandId.gridy = 0;
						panelCfgTop.add(lblCommandId, gbc_lblCommandId);
					}
					{
						textField_CommandID = new JTextField();
						GridBagConstraints gbc_textField_CommandID = new GridBagConstraints();
						gbc_textField_CommandID.fill = GridBagConstraints.HORIZONTAL;
						gbc_textField_CommandID.gridx = 3;
						gbc_textField_CommandID.gridy = 0;
						panelCfgTop.add(textField_CommandID, gbc_textField_CommandID);
						textField_CommandID.setColumns(10);
					}
				}
				{
					JPanel panelCfgCenter = new JPanel();
					GridBagConstraints gbc_panelCfgCenter = new GridBagConstraints();
					gbc_panelCfgCenter.insets = new Insets(0, 0, 5, 0);
					gbc_panelCfgCenter.fill = GridBagConstraints.BOTH;
					gbc_panelCfgCenter.gridx = 0;
					gbc_panelCfgCenter.gridy = 1;
					panelMainCenter.add(panelCfgCenter, gbc_panelCfgCenter);
					GridBagLayout gbl_panelCfgCenter = new GridBagLayout();
					gbl_panelCfgCenter.columnWidths = new int[]{0, 0};
					gbl_panelCfgCenter.rowHeights = new int[]{0, 0, 0};
					gbl_panelCfgCenter.columnWeights = new double[]{1.0, Double.MIN_VALUE};
					gbl_panelCfgCenter.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
					panelCfgCenter.setLayout(gbl_panelCfgCenter);
					{
						JLabel lblCommon = new JLabel(" Common Settings");
						GridBagConstraints gbc_lblCommon = new GridBagConstraints();
						gbc_lblCommon.anchor = GridBagConstraints.WEST;
						gbc_lblCommon.insets = new Insets(0, 0, 5, 0);
						gbc_lblCommon.gridx = 0;
						gbc_lblCommon.gridy = 0;
						panelCfgCenter.add(lblCommon, gbc_lblCommon);
					}
					{
						JScrollPane scrollPaneCommonSettings = new JScrollPane();
						tableCommonParameters = new JTable(new MyTableModel(RobotEthernetCmd.getInstance().getCommonConfigDataTitle(),RobotEthernetCmd.getInstance().getCommonConfigDataTable(curRobotModel+"#"+curRobotTarget)));
						tableCommonParameters.setRowHeight(25);
						fitTableColumns(tableCommonParameters);
						scrollPaneCommonSettings.setViewportView(tableCommonParameters);
						
						GridBagConstraints gbc_scrollPaneCommonSettings = new GridBagConstraints();
						gbc_scrollPaneCommonSettings.fill = GridBagConstraints.BOTH;
						gbc_scrollPaneCommonSettings.gridx = 0;
						gbc_scrollPaneCommonSettings.gridy = 1;
						panelCfgCenter.add(scrollPaneCommonSettings, gbc_scrollPaneCommonSettings);
					}
				}
				{
					JPanel panelCfgBottom = new JPanel();
					GridBagConstraints gbc_panelCfgBottom = new GridBagConstraints();
					gbc_panelCfgBottom.insets = new Insets(0, 0, 5, 0);
					gbc_panelCfgBottom.fill = GridBagConstraints.BOTH;
					gbc_panelCfgBottom.gridx = 0;
					gbc_panelCfgBottom.gridy = 2;
					panelMainCenter.add(panelCfgBottom, gbc_panelCfgBottom);
					GridBagLayout gbl_panelCfgBottom = new GridBagLayout();
					gbl_panelCfgBottom.columnWidths = new int[]{0, 0};
					gbl_panelCfgBottom.rowHeights = new int[]{0, 0, 0};
					gbl_panelCfgBottom.columnWeights = new double[]{1.0, Double.MIN_VALUE};
					gbl_panelCfgBottom.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
					panelCfgBottom.setLayout(gbl_panelCfgBottom);
					{
						JLabel lblParameters = new JLabel(" Command Input Parameters");
						GridBagConstraints gbc_lblParameters = new GridBagConstraints();
						gbc_lblParameters.anchor = GridBagConstraints.WEST;
						gbc_lblParameters.insets = new Insets(0, 0, 5, 0);
						gbc_lblParameters.gridx = 0;
						gbc_lblParameters.gridy = 0;
						panelCfgBottom.add(lblParameters, gbc_lblParameters);
					}
					{
						JScrollPane scrollPaneInputParameters = new JScrollPane();
						tableInputParameters = new JTable(new MyTableModel(RobotEthernetCmd.getInstance().getCommandParaDataTitle(),RobotEthernetCmd.getInstance().getCommandParaDataTable(curRobotModel+"#"+curRobotTarget, curRobotOperation, curRobotCommand)));
						tableInputParameters.setRowHeight(25);
						fitTableColumns(tableInputParameters);
						scrollPaneInputParameters.setViewportView(tableInputParameters);
						
						GridBagConstraints gbc_scrollPaneInputParameters = new GridBagConstraints();
						gbc_scrollPaneInputParameters.fill = GridBagConstraints.BOTH;
						gbc_scrollPaneInputParameters.gridx = 0;
						gbc_scrollPaneInputParameters.gridy = 1;
						panelCfgBottom.add(scrollPaneInputParameters, gbc_scrollPaneInputParameters);
					}
				}
				{
					JPanel panelCfgLast = new JPanel();
					GridBagConstraints gbc_panelCfgLast = new GridBagConstraints();
					gbc_panelCfgLast.fill = GridBagConstraints.BOTH;
					gbc_panelCfgLast.gridx = 0;
					gbc_panelCfgLast.gridy = 3;
					panelMainCenter.add(panelCfgLast, gbc_panelCfgLast);
					GridBagLayout gbl_panelCfgLast = new GridBagLayout();
					gbl_panelCfgLast.columnWidths = new int[]{0, 0, 0};
					gbl_panelCfgLast.rowHeights = new int[] {0};
					gbl_panelCfgLast.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
					gbl_panelCfgLast.rowWeights = new double[]{0.0};
					panelCfgLast.setLayout(gbl_panelCfgLast);
					{
						JLabel lblOperationExecutive = new JLabel(" Operation Executive");
						GridBagConstraints gbc_lblOperationExecutive = new GridBagConstraints();
						gbc_lblOperationExecutive.insets = new Insets(0, 0, 0, 5);
						gbc_lblOperationExecutive.anchor = GridBagConstraints.EAST;
						gbc_lblOperationExecutive.gridx = 0;
						gbc_lblOperationExecutive.gridy = 0;
						panelCfgLast.add(lblOperationExecutive, gbc_lblOperationExecutive);
					}
					{
						textFieldOperator = new JTextField();
						textFieldOperator.setText("Myself");
						GridBagConstraints gbc_textFieldOperator = new GridBagConstraints();
						gbc_textFieldOperator.fill = GridBagConstraints.HORIZONTAL;
						gbc_textFieldOperator.gridx = 1;
						gbc_textFieldOperator.gridy = 0;
						panelCfgLast.add(textFieldOperator, gbc_textFieldOperator);
						textFieldOperator.setColumns(10);
					}
				}
			}
		}
		{
			JPanel panelBottom = new JPanel();
			panelBottom.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
			contentPanel.add(panelBottom, BorderLayout.SOUTH);
			GridBagLayout gbl_panelBottom = new GridBagLayout();
			gbl_panelBottom.columnWidths = new int[] {575};
			gbl_panelBottom.rowHeights = new int[] {30};
			gbl_panelBottom.columnWeights = new double[]{0.0};
			gbl_panelBottom.rowWeights = new double[]{0.0};
			panelBottom.setLayout(gbl_panelBottom);
			{
				lblRunningMsg = new JLabel("Running Message");
				GridBagConstraints gbc_lblRunningMsg = new GridBagConstraints();
				gbc_lblRunningMsg.insets = new Insets(0, 0, 0, 5);
				gbc_lblRunningMsg.anchor = GridBagConstraints.WEST;
				gbc_lblRunningMsg.gridx = 0;
				gbc_lblRunningMsg.gridy = 0;
				panelBottom.add(lblRunningMsg, gbc_lblRunningMsg);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				btnExecuteCommand = new JButton("Execute Command");
				btnExecuteCommand.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent arg0) {
						if(1 == arg0.getButton()){
							if("".equals(curRobotCommand)) return;
							btnExecuteCommand.setEnabled(false);
							executeCommand(true);
							btnExecuteCommand.setEnabled(true);
						}
					}
				});
				{
					JButton btnRemoveCommand = new JButton("Remove Command");
					btnRemoveCommand.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if(1 == e.getButton()){
								if(!"".equals(curRobotCommand)){
									if(0 == JOptionPane.showConfirmDialog(contentPanel, "Are you sure to remove command "+curRobotCommand+"?", "Command Removing", JOptionPane.YES_NO_OPTION)){
										btnRemoveCommand.setEnabled(false);
										RobotEthernetCmd robotCmd = RobotEthernetCmd.getInstance();
										String mainKey = curRobotModel + "#" + curRobotTarget;
										LinkedHashMap<String, Object> oprMap = robotCmd.getData(mainKey);
										if(null != oprMap){
											@SuppressWarnings("unchecked")
											LinkedHashMap<String, Object> cmdMap = (LinkedHashMap<String, Object>) oprMap.get(curRobotOperation);
											if(null != cmdMap){
												cmdMap.remove(curRobotCommand);
												if(XmlUtils.saveRobotEthernetCmdXml()){
													setCurCommandParas();
												}else{
													lblRunningMsg.setText("Remove command "+curRobotCommand+" failed");
												}
											}
										}
										btnRemoveCommand.setEnabled(true);
									}
								}
							}
						}
					});
					{
						JCheckBox chckbxLoopOperation = new JCheckBox("Loop Operation");
						chckbxLoopOperation.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent arg0) {
								loopingOperation = chckbxLoopOperation.isSelected();
								lblRunningMsg.setText("Looping Operation is "+(loopingOperation?"enabled":"disabled"));
							}
						});
						buttonPane.add(chckbxLoopOperation);
					}
					buttonPane.add(btnRemoveCommand);
				}
				buttonPane.add(btnExecuteCommand);
			}
			{
				JButton btnSaveCommand = new JButton("Save Command");
				btnSaveCommand.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent arg0) {
						if(1 == arg0.getButton()){
							String inputParas = "",inputParasVal = "";
							String newCmdName = textField_CommandName.getText().trim();
							String newCmdID = textField_CommandID.getText().trim();
							String operator = textFieldOperator.getText().trim();
							String mainKey = curRobotModel + "#" + curRobotTarget;
							if("".equals(operator)) operator = "Myself";
							
							RobotEthernetCmd robotCmd = RobotEthernetCmd.getInstance();
							
							if("".equals(newCmdName) || "".equals(newCmdID)){
								JOptionPane.showMessageDialog(contentPanel, "Command Name/ID can not be null", "Command Setting Error", JOptionPane.ERROR_MESSAGE);
							}else{
								int rowCnt = tableCommonParameters.getRowCount();
								int colCnt = tableCommonParameters.getColumnCount();
								if(colCnt > 1 && rowCnt > 0){
									LinkedHashMap<String, Object> commonCfg = new LinkedHashMap<String, Object>();
									for(int i=0; i<rowCnt; i++){
										String paraName = (null!=tableCommonParameters.getValueAt(i, 0))?String.valueOf(tableCommonParameters.getValueAt(i, 0)).trim():"";
										String paraVal = (null!=tableCommonParameters.getValueAt(i, 1))?String.valueOf(tableCommonParameters.getValueAt(i, 1)).trim():"";
										
										paraName = paraName.replace("(s)", "").replace("(ms)", "");
										if(!"".equals(paraVal)){
											commonCfg.put(paraName, paraVal);
											if("seperator".equals(paraName)) cmdSeperator = paraVal;
										}
									}
									robotCmd.setData(mainKey, "Common", commonCfg);
								}
								
								@SuppressWarnings("unchecked")
								LinkedHashMap<String, Object> cmds = (LinkedHashMap<String, Object>) robotCmd.getData(mainKey).get(curRobotOperation);
								if(null == cmds) cmds = new LinkedHashMap<String, Object>();
								
								rowCnt = tableInputParameters.getRowCount();
								colCnt = tableInputParameters.getColumnCount();
								if(colCnt > 2 && rowCnt > 0){
									for(int i=0; i<rowCnt; i++){
										String paraName = (null!=tableInputParameters.getValueAt(i, 1))?String.valueOf(tableInputParameters.getValueAt(i, 1)).trim():"";
										String paraVal = (null!=tableInputParameters.getValueAt(i, 2))?String.valueOf(tableInputParameters.getValueAt(i, 2)).trim():"";
										if("".equals(paraName) || "".equals(paraVal)) continue;
										
										if("".equals(inputParas)){
											inputParas = paraName;
											inputParasVal = paraVal;
										}else{
											inputParas += cmdSeperator + paraName;
											inputParasVal += cmdSeperator + paraVal;
										}
									}
									
									ArrayList<String> oprInfo = new ArrayList<String>();
									oprInfo.add(operator);
									cmds.put(curRobotOperation, oprInfo);
									
									ArrayList<String> para = new ArrayList<String>();
									para.add(inputParas);
									para.add(inputParasVal);
									para.add(newCmdName);
									cmds.put(newCmdName+"#"+newCmdID, para);
								}
							
								newCmdName = newCmdName+"#"+newCmdID;
								if(newCmdName.equals(curRobotCommand)){
									if(0 != JOptionPane.showConfirmDialog(contentPanel, "Command "+curRobotCommand+" is existing, continue to update it with current settings?", "Update Existing Command?", JOptionPane.YES_NO_OPTION)){
										return;
									}
								}
								
								if(!"".equals(curRobotCommand) && !newCmdName.equals(curRobotCommand)){
									if(0 == JOptionPane.showConfirmDialog(contentPanel, "Change command "+curRobotCommand+" to "+newCmdName+" and save it with current settings?\nNote: Choose 'No' to add a new command", "Change Existing Command?", JOptionPane.YES_NO_OPTION)){
										cmds.remove(curRobotCommand);
									}
								}
								robotCmd.setData(mainKey, curRobotOperation, cmds);
								
								btnSaveCommand.setEnabled(false);
								if(XmlUtils.saveRobotEthernetCmdXml()){
									setCurCommandParas(newCmdName);
									lblRunningMsg.setText("Save command "+newCmdName+" OK");
								}else{
									lblRunningMsg.setText("Save command "+newCmdName+" failed");
									JOptionPane.showMessageDialog(contentPanel, "Save command "+newCmdName+" failed!", "Failed", JOptionPane.ERROR_MESSAGE);
								}
								btnSaveCommand.setEnabled(true);
							}
						}
					}
				});
				btnSaveCommand.setActionCommand("SaveCommand");
				buttonPane.add(btnSaveCommand);
				getRootPane().setDefaultButton(btnSaveCommand);
			}
			{
				JButton btnClose = new JButton("Close");
				btnClose.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent arg0) {
						if(1 == arg0.getButton()) robotNetworkCmdConfig.dispose();
					}
				});
				buttonPane.add(btnClose);
			}
		}
		
		GUIUtils.setComboBoxValues(comboBoxRobotModel, RobotDriver.getInstance().getRobots());
		GUIUtils.setComboBoxValues(comboBoxTarget, CncDriver.getInstance().getCncModels());
		curRobotModel = comboBoxRobotModel.getSelectedItem().toString();
		curRobotTarget = comboBoxTarget.getSelectedItem().toString();
		String mainKey = curRobotModel + "#" + curRobotTarget;
		GUIUtils.setComboBoxValues(comboBoxOperation, RobotEthernetCmd.getInstance().getAllOperations(mainKey));
		setCurCommandParas();
	}
	
	class SocketClientDataHandler extends SocketDataHandler{
		@Override
		public void doHandle(String in, Socket s) {
			lblRunningMsg.setText(in);
			
			if(null == s) return;
			String ip = s.getInetAddress().toString().replace("/", "");
			socketRespData.put(ip+":"+s.getPort(), in);
		}
	}
	
	class ExecuteCommand implements Runnable{
		private boolean singleMode;
		
		public ExecuteCommand(boolean singleMode){
			this.singleMode = singleMode;
		}
		
		@Override
		public void run() {
			btnRun.setEnabled(false);
			btnExecuteCommand.setEnabled(false);
			
			if(singleMode){
				executeCommand(singleMode);
			}else{
				while(loopingOperation){
					if(!executeCommand(singleMode)) break;
				}
			}
			
			btnRun.setEnabled(true);
			btnExecuteCommand.setEnabled(true);
		}
	}
}
