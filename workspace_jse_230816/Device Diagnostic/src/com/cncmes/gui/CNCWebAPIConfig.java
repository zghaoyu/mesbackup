package com.cncmes.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.util.Enumeration;
import java.util.LinkedHashMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import com.cncmes.base.CncWebAPIItems;
import com.cncmes.data.CncWebAPI;
import com.cncmes.drv.CncDrvWeb;
import com.cncmes.utils.DataUtils;
import com.cncmes.utils.GUIUtils;
import com.cncmes.utils.XmlUtils;

import net.sf.json.JSONObject;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JComboBox;
import java.awt.Insets;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CNCWebAPIConfig extends JDialog {
	private static final long serialVersionUID = 22L;
	private final JPanel contentPanel = new JPanel();
	private static CNCWebAPIConfig cncWebAPIConfig = new CNCWebAPIConfig();
	private JTextField textField_CommandID;
	private JTextField textField_CommandName;
	private JLabel lblCommandName;
	private JLabel lblRunningMsg;
	private JTable tableInputParameters;
	private JTable tableOutputParameters;
	private JList<String> listAllCommands;
	private JComboBox<String> comboBoxCncModel;
	private JComboBox<String> comboBoxOperation;
	
	private String curCncBrand;
	private String curCncModel;
	private String curCncCommand;
	private String curCncOperation;
	private final String PARASEPERATOR = ";";
	private JTable tableCfgCommon;
	private JTextField textFieldOperator;
	
	public static CNCWebAPIConfig getInstance(){
		return cncWebAPIConfig;
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
	
	@SuppressWarnings("unchecked")
	private void setCurCommandParas(String...cmdName) {
		String mainKey = comboBoxCncModel.getSelectedItem().toString();
		curCncBrand = "";
		curCncModel = "";
		
		CncWebAPI cncWebAPI = CncWebAPI.getInstance();
		LinkedHashMap<CncWebAPIItems, String> commonCfg = new LinkedHashMap<CncWebAPIItems, String>();
		commonCfg = (LinkedHashMap<CncWebAPIItems, String>) cncWebAPI.getData(mainKey).get("Common");
		if(null != commonCfg){
			curCncBrand = commonCfg.get(CncWebAPIItems.BRAND);
			curCncModel = commonCfg.get(CncWebAPIItems.MODEL);
		}
		
		curCncOperation = comboBoxOperation.getSelectedItem().toString();
		if(null != cmdName && cmdName.length > 0 && !"".equals(cmdName[0])){
			curCncCommand = cmdName[0];
		}else{
			String[] cmds = cncWebAPI.getAllCommands(mainKey, curCncOperation);
			curCncCommand = cmds[0];
		}
		
		String commandID = cncWebAPI.getCmdParaVal(mainKey, curCncOperation, curCncCommand, CncWebAPIItems.ID);
		
		textField_CommandName.setText(curCncCommand.indexOf("#")>0?curCncCommand.split("#")[0]:curCncCommand);
		textField_CommandID.setText(commandID);
		textFieldOperator.setText(cncWebAPI.getOperationExecutive(mainKey, curCncOperation));
		
		lblRunningMsg.setText(curCncBrand+"_"+curCncModel+": "+curCncCommand);
		GUIUtils.setJListContent(listAllCommands, cncWebAPI.getAllCommands(mainKey, curCncOperation));
		GUIUtils.setJListSelectedIdx(listAllCommands, curCncCommand);
		
		tableCfgCommon.setModel(new MyTableModel(cncWebAPI.getCommonParasTableTitle(),cncWebAPI.getCommonParasTableData(mainKey)));
		tableInputParameters.setModel(new MyTableModel(cncWebAPI.getCmdParasTableTitle(),cncWebAPI.getCmdInputParasTableData(mainKey, curCncOperation, curCncCommand)));
		tableOutputParameters.setModel(new MyTableModel(cncWebAPI.getCmdParasTableTitle(),cncWebAPI.getCmdOutputParasTableData(mainKey, curCncOperation, curCncCommand)));
		fitTableColumns(tableCfgCommon);
		fitTableColumns(tableInputParameters);
		fitTableColumns(tableOutputParameters);
	}
	
	private void executeCommand(boolean singleMode) {
		CncWebAPI cncWebAPI = CncWebAPI.getInstance();
		String cncModel = comboBoxCncModel.getSelectedItem().toString();
		LinkedHashMap<CncWebAPIItems, String> commonCfg = cncWebAPI.getCommonCfg(cncModel);
		String debugIP = commonCfg.get(CncWebAPIItems.DEBUGIP);
		String debugPort = commonCfg.get(CncWebAPIItems.DEBUGPORT);
		
		LinkedHashMap<String, String> inParas = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> rtnData = new LinkedHashMap<String, String>();
		inParas.put("port", debugPort);
		inParas.put("model", cncModel);
		if(singleMode) inParas.put("singleCmd", curCncCommand);
		
		CncDrvWeb cncDrvWeb = CncDrvWeb.getInstance();
		if(cncDrvWeb.sendCommand(debugIP, curCncOperation, inParas, rtnData)){
			if(singleMode){
				lblRunningMsg.setText("Execute "+curCncCommand+" OK");
			}else{
				lblRunningMsg.setText("Execute "+curCncOperation+" OK");
			}
		}else{
			String errDesc = "Execute "+curCncOperation+" failed";
			if(singleMode) errDesc = "Execute "+curCncCommand+" failed";
			if(rtnData.size() > 0){
				for(String key:rtnData.keySet()){
					errDesc += "," + rtnData.get(key);
				}
			}
			lblRunningMsg.setText(errDesc);
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
	private CNCWebAPIConfig() {
		try {
			XmlUtils.parseCncWebAPIXml();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		setModal(true);
		setIconImage(Toolkit.getDefaultToolkit().getImage(CNCWebAPIConfig.class.getResource("/com/cncmes/img/3d_printer_24.png")));
		setTitle("CNC Web-Service Command Config");
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
			gbl_panelTop.columnWidths = new int[] {80, 180, 70, 190, 55};
			gbl_panelTop.rowHeights = new int[] {30};
			gbl_panelTop.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0};
			gbl_panelTop.rowWeights = new double[]{0.0};
			panelTop.setLayout(gbl_panelTop);
			{
				JLabel lblCncModel = new JLabel("CNC Model");
				GridBagConstraints gbc_lblCncModel = new GridBagConstraints();
				gbc_lblCncModel.insets = new Insets(0, 0, 0, 5);
				gbc_lblCncModel.gridx = 0;
				gbc_lblCncModel.gridy = 0;
				panelTop.add(lblCncModel, gbc_lblCncModel);
			}
			{
				comboBoxCncModel = new JComboBox<String>();
				comboBoxCncModel.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent arg0) {
						setCurCommandParas();
					}
				});
				comboBoxCncModel.setModel(new DefaultComboBoxModel<String>(DataUtils.getCNCModels()));
				GridBagConstraints gbc_comboBoxCncModel = new GridBagConstraints();
				gbc_comboBoxCncModel.insets = new Insets(0, 0, 0, 5);
				gbc_comboBoxCncModel.fill = GridBagConstraints.HORIZONTAL;
				gbc_comboBoxCncModel.gridx = 1;
				gbc_comboBoxCncModel.gridy = 0;
				panelTop.add(comboBoxCncModel, gbc_comboBoxCncModel);
			}
			{
				JLabel lblOperation = new JLabel("Operation");
				GridBagConstraints gbc_lblOperation = new GridBagConstraints();
				gbc_lblOperation.insets = new Insets(0, 0, 0, 5);
				gbc_lblOperation.gridx = 2;
				gbc_lblOperation.gridy = 0;
				panelTop.add(lblOperation, gbc_lblOperation);
			}
			{
				comboBoxOperation = new JComboBox<String>();
				comboBoxOperation.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						curCncOperation = comboBoxOperation.getSelectedItem().toString();
						setCurCommandParas();
					}
				});
				comboBoxOperation.setModel(new DefaultComboBoxModel<String>(CncWebAPI.getInstance().getAllOperations(comboBoxCncModel.getSelectedItem().toString())));
				GridBagConstraints gbc_comboBoxOperation = new GridBagConstraints();
				gbc_comboBoxOperation.insets = new Insets(0, 0, 0, 5);
				gbc_comboBoxOperation.fill = GridBagConstraints.HORIZONTAL;
				gbc_comboBoxOperation.gridx = 3;
				gbc_comboBoxOperation.gridy = 0;
				panelTop.add(comboBoxOperation, gbc_comboBoxOperation);
			}
			{
				JButton btnRun = new JButton("Run");
				btnRun.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if(1 == e.getButton()){
							if("".equals(curCncCommand)) return;
							btnRun.setEnabled(false);
							executeCommand(false);
							btnRun.setEnabled(true);
						}
					}
				});
				GridBagConstraints gbc_btnRun = new GridBagConstraints();
				gbc_btnRun.gridx = 4;
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
				gbl_panelMainCenter.rowHeights = new int[] {40, 198, 105, 108, 30, 1};
				gbl_panelMainCenter.columnWeights = new double[]{1.0};
				gbl_panelMainCenter.rowWeights = new double[]{1.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
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
					gbl_panelCfgTop.columnWidths = new int[] {80, 120, 20, 80};
					gbl_panelCfgTop.rowHeights = new int[] {30};
					gbl_panelCfgTop.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0};
					gbl_panelCfgTop.rowWeights = new double[]{0.0};
					panelCfgTop.setLayout(gbl_panelCfgTop);
					{
						lblCommandName = new JLabel(" Command Name");
						GridBagConstraints gbc_lblCommandName = new GridBagConstraints();
						gbc_lblCommandName.anchor = GridBagConstraints.WEST;
						gbc_lblCommandName.insets = new Insets(0, 0, 5, 5);
						gbc_lblCommandName.gridx = 0;
						gbc_lblCommandName.gridy = 0;
						panelCfgTop.add(lblCommandName, gbc_lblCommandName);
					}
					{
						textField_CommandName = new JTextField();
						textField_CommandName.setText("Start");
						GridBagConstraints gbc_textField_CommandName = new GridBagConstraints();
						gbc_textField_CommandName.anchor = GridBagConstraints.SOUTH;
						gbc_textField_CommandName.insets = new Insets(0, 0, 5, 5);
						gbc_textField_CommandName.fill = GridBagConstraints.HORIZONTAL;
						gbc_textField_CommandName.gridx = 1;
						gbc_textField_CommandName.gridy = 0;
						panelCfgTop.add(textField_CommandName, gbc_textField_CommandName);
						textField_CommandName.setColumns(10);
					}
					{
						JLabel lblCommandId = new JLabel("ID");
						GridBagConstraints gbc_lblCommandId = new GridBagConstraints();
						gbc_lblCommandId.insets = new Insets(0, 0, 5, 5);
						gbc_lblCommandId.gridx = 2;
						gbc_lblCommandId.gridy = 0;
						panelCfgTop.add(lblCommandId, gbc_lblCommandId);
					}
					{
						textField_CommandID = new JTextField();
						textField_CommandID.setText("1");
						GridBagConstraints gbc_textField_CommandID = new GridBagConstraints();
						gbc_textField_CommandID.anchor = GridBagConstraints.SOUTH;
						gbc_textField_CommandID.insets = new Insets(0, 0, 5, 0);
						gbc_textField_CommandID.fill = GridBagConstraints.HORIZONTAL;
						gbc_textField_CommandID.gridx = 3;
						gbc_textField_CommandID.gridy = 0;
						panelCfgTop.add(textField_CommandID, gbc_textField_CommandID);
						textField_CommandID.setColumns(10);
					}
				}
				{
					JPanel panelCfgCommon = new JPanel();
					GridBagConstraints gbc_panelCfgCommon = new GridBagConstraints();
					gbc_panelCfgCommon.insets = new Insets(0, 0, 5, 0);
					gbc_panelCfgCommon.fill = GridBagConstraints.BOTH;
					gbc_panelCfgCommon.gridx = 0;
					gbc_panelCfgCommon.gridy = 1;
					panelMainCenter.add(panelCfgCommon, gbc_panelCfgCommon);
					GridBagLayout gbl_panelCfgCommon = new GridBagLayout();
					gbl_panelCfgCommon.columnWidths = new int[] {0};
					gbl_panelCfgCommon.rowHeights = new int[] {0};
					gbl_panelCfgCommon.columnWeights = new double[]{1.0};
					gbl_panelCfgCommon.rowWeights = new double[]{1.0};
					panelCfgCommon.setLayout(gbl_panelCfgCommon);
					{
						JScrollPane scrollPaneCfgCommon = new JScrollPane();
						GridBagConstraints gbc_scrollPaneCfgCommon = new GridBagConstraints();
						gbc_scrollPaneCfgCommon.fill = GridBagConstraints.BOTH;
						gbc_scrollPaneCfgCommon.gridx = 0;
						gbc_scrollPaneCfgCommon.gridy = 0;
						panelCfgCommon.add(scrollPaneCfgCommon, gbc_scrollPaneCfgCommon);
						{
							tableCfgCommon = new JTable(new MyTableModel(CncWebAPI.getInstance().getCommonParasTableTitle(),CncWebAPI.getInstance().getCommonParasTableData(curCncBrand)));
							tableCfgCommon.setRowHeight(25);
							fitTableColumns(tableCfgCommon);
							scrollPaneCfgCommon.setViewportView(tableCfgCommon);
						}
					}
				}
				{
					JPanel panelCfgCenter = new JPanel();
					GridBagConstraints gbc_panelCfgCenter = new GridBagConstraints();
					gbc_panelCfgCenter.insets = new Insets(0, 0, 5, 0);
					gbc_panelCfgCenter.fill = GridBagConstraints.BOTH;
					gbc_panelCfgCenter.gridx = 0;
					gbc_panelCfgCenter.gridy = 2;
					panelMainCenter.add(panelCfgCenter, gbc_panelCfgCenter);
					GridBagLayout gbl_panelCfgCenter = new GridBagLayout();
					gbl_panelCfgCenter.columnWidths = new int[]{0, 0};
					gbl_panelCfgCenter.rowHeights = new int[]{0, 0, 0};
					gbl_panelCfgCenter.columnWeights = new double[]{1.0, Double.MIN_VALUE};
					gbl_panelCfgCenter.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
					panelCfgCenter.setLayout(gbl_panelCfgCenter);
					{
						JLabel lblInputParameters = new JLabel(" Input Parameters");
						GridBagConstraints gbc_lblInputParameters = new GridBagConstraints();
						gbc_lblInputParameters.anchor = GridBagConstraints.WEST;
						gbc_lblInputParameters.insets = new Insets(0, 0, 5, 0);
						gbc_lblInputParameters.gridx = 0;
						gbc_lblInputParameters.gridy = 0;
						panelCfgCenter.add(lblInputParameters, gbc_lblInputParameters);
					}
					{
						JScrollPane scrollPane_InputParameters = new JScrollPane();
						tableInputParameters = new JTable(new MyTableModel(CncWebAPI.getInstance().getCmdParasTableTitle(),CncWebAPI.getInstance().getCmdInputParasTableData(curCncBrand, curCncModel, curCncCommand)));
						tableInputParameters.setRowHeight(25);
						fitTableColumns(tableInputParameters);
						scrollPane_InputParameters.setViewportView(tableInputParameters);
						
						GridBagConstraints gbc_scrollPane_InputParameters = new GridBagConstraints();
						gbc_scrollPane_InputParameters.fill = GridBagConstraints.BOTH;
						gbc_scrollPane_InputParameters.gridx = 0;
						gbc_scrollPane_InputParameters.gridy = 1;
						panelCfgCenter.add(scrollPane_InputParameters, gbc_scrollPane_InputParameters);
					}
				}
				{
					JPanel panelCfgBottom = new JPanel();
					GridBagConstraints gbc_panelCfgBottom = new GridBagConstraints();
					gbc_panelCfgBottom.insets = new Insets(0, 0, 5, 0);
					gbc_panelCfgBottom.fill = GridBagConstraints.BOTH;
					gbc_panelCfgBottom.gridx = 0;
					gbc_panelCfgBottom.gridy = 3;
					panelMainCenter.add(panelCfgBottom, gbc_panelCfgBottom);
					GridBagLayout gbl_panelCfgBottom = new GridBagLayout();
					gbl_panelCfgBottom.columnWidths = new int[]{0, 0};
					gbl_panelCfgBottom.rowHeights = new int[]{0, 0, 0};
					gbl_panelCfgBottom.columnWeights = new double[]{1.0, Double.MIN_VALUE};
					gbl_panelCfgBottom.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
					panelCfgBottom.setLayout(gbl_panelCfgBottom);
					{
						JLabel lblOutputParameters = new JLabel(" Output Parameters");
						GridBagConstraints gbc_lblOutputParameters = new GridBagConstraints();
						gbc_lblOutputParameters.anchor = GridBagConstraints.WEST;
						gbc_lblOutputParameters.insets = new Insets(0, 0, 5, 0);
						gbc_lblOutputParameters.gridx = 0;
						gbc_lblOutputParameters.gridy = 0;
						panelCfgBottom.add(lblOutputParameters, gbc_lblOutputParameters);
					}
					{
						JScrollPane scrollPane_OutputParameters = new JScrollPane();
						tableOutputParameters = new JTable(new MyTableModel(CncWebAPI.getInstance().getCmdParasTableTitle(),CncWebAPI.getInstance().getCmdOutputParasTableData(curCncBrand, curCncModel, curCncCommand)));
						tableOutputParameters.setRowHeight(25);
						fitTableColumns(tableOutputParameters);
						scrollPane_OutputParameters.setViewportView(tableOutputParameters);
						
						GridBagConstraints gbc_scrollPane_OutputParameters = new GridBagConstraints();
						gbc_scrollPane_OutputParameters.fill = GridBagConstraints.BOTH;
						gbc_scrollPane_OutputParameters.gridx = 0;
						gbc_scrollPane_OutputParameters.gridy = 1;
						panelCfgBottom.add(scrollPane_OutputParameters, gbc_scrollPane_OutputParameters);
					}
				}
				{
					JPanel panelCfgLast = new JPanel();
					GridBagConstraints gbc_panelCfgLast = new GridBagConstraints();
					gbc_panelCfgLast.fill = GridBagConstraints.BOTH;
					gbc_panelCfgLast.gridx = 0;
					gbc_panelCfgLast.gridy = 4;
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
				JButton btnExecuteCommand = new JButton("Execute Command");
				btnExecuteCommand.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent arg0) {
						if(1 == arg0.getButton()){
							if("".equals(curCncCommand)) return;
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
								if(!"".equals(curCncCommand)){
									if(0 == JOptionPane.showConfirmDialog(cncWebAPIConfig.getContentPane(), "Are you sure to remove command "+curCncCommand+"?", "Command Removing", JOptionPane.YES_NO_OPTION)){
										btnRemoveCommand.setEnabled(false);
										CncWebAPI cncWebAPI = CncWebAPI.getInstance();
										@SuppressWarnings("unchecked")
										LinkedHashMap<String, Object> cmds = (LinkedHashMap<String, Object>) cncWebAPI.getData(comboBoxCncModel.getSelectedItem().toString()).get(curCncOperation);
										if(null != cmds){
											cmds.remove(curCncCommand);
											if(XmlUtils.saveCncWebAPIXml()){
												setCurCommandParas();
											}else{
												lblRunningMsg.setText("Remove command "+curCncCommand+" failed");
											}
										}
										btnRemoveCommand.setEnabled(true);
									}
								}
							}
						}
					});
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
							JSONObject jsonParas = new JSONObject();
							String errMsg = "";
							String inParas = "",inParasType = "",inParasVal = "";
							String outParas = "",outParasType = "",outParasVal = "";
							String mainKey = comboBoxCncModel.getSelectedItem().toString();
							String newCmdName = textField_CommandName.getText().trim();
							String newCmdID = textField_CommandID.getText().trim();
							String operator = textFieldOperator.getText().trim();
							if("".equals(operator)) operator = "Myself";
							if(!"".equals(newCmdName) && !"".equals(newCmdID)) newCmdName += "#" + newCmdID;
							
							String brand = "", model = "", newCmdURL = "";
							String debugPort = "", ncProgramName_sub1 = "", ncProgramName_sub2 = "";
							String ncProgramName_sub3 = "", ncProgramName_sub4 = "", ncProgramName_sub5 = "";
							String ncProgramName_sub6 = "", ncProgramName_main = "", debugIP = "";
							String cmdCoordinate1 = "",cmdCoordinate2 = "",cmdCoordinate3 = "";
							String cmdCoordinate4 = "",cmdCoordinate5 = "",cmdCoordinate6 = "";
							String ftpPort = "", ftpUser = "", ftpPwd = "";
							int rowCnt = tableCfgCommon.getRowCount();
							int colCnt = tableCfgCommon.getColumnCount();
							if(colCnt > 0 && rowCnt > 0){
								for(int i=0; i<rowCnt; i++){
									String paraName = (null!=tableCfgCommon.getValueAt(i, 0))?String.valueOf(tableCfgCommon.getValueAt(i, 0)).trim():"";
									switch(paraName){
									case "brand":
										brand = (null!=tableCfgCommon.getValueAt(i, 1))?String.valueOf(tableCfgCommon.getValueAt(i, 1)).trim():"";
										break;
									case "model":
										model = (null!=tableCfgCommon.getValueAt(i, 1))?String.valueOf(tableCfgCommon.getValueAt(i, 1)).trim():"";
										break;
									case "commandUrl":
										newCmdURL = (null!=tableCfgCommon.getValueAt(i, 1))?String.valueOf(tableCfgCommon.getValueAt(i, 1)).trim():"";
										break;
									case "ftpPort":
										ftpPort = (null!=tableCfgCommon.getValueAt(i, 1))?String.valueOf(tableCfgCommon.getValueAt(i, 1)).trim():"";
										break;
									case "ftpUser":
										ftpUser = (null!=tableCfgCommon.getValueAt(i, 1))?String.valueOf(tableCfgCommon.getValueAt(i, 1)).trim():"";
										break;
									case "ftpPwd":
										ftpPwd = (null!=tableCfgCommon.getValueAt(i, 1))?String.valueOf(tableCfgCommon.getValueAt(i, 1)).trim():"";
										break;
									case "debugPort":
										debugPort = (null!=tableCfgCommon.getValueAt(i, 1))?String.valueOf(tableCfgCommon.getValueAt(i, 1)).trim():"";
										break;
									case "ncProgramName_sub1":
										ncProgramName_sub1 = (null!=tableCfgCommon.getValueAt(i, 1))?String.valueOf(tableCfgCommon.getValueAt(i, 1)).trim():"";
										break;
									case "ncProgramName_sub2":
										ncProgramName_sub2 = (null!=tableCfgCommon.getValueAt(i, 1))?String.valueOf(tableCfgCommon.getValueAt(i, 1)).trim():"";
										break;
									case "ncProgramName_sub3":
										ncProgramName_sub3 = (null!=tableCfgCommon.getValueAt(i, 1))?String.valueOf(tableCfgCommon.getValueAt(i, 1)).trim():"";
										break;
									case "ncProgramName_sub4":
										ncProgramName_sub4 = (null!=tableCfgCommon.getValueAt(i, 1))?String.valueOf(tableCfgCommon.getValueAt(i, 1)).trim():"";
										break;
									case "ncProgramName_sub5":
										ncProgramName_sub5 = (null!=tableCfgCommon.getValueAt(i, 1))?String.valueOf(tableCfgCommon.getValueAt(i, 1)).trim():"";
										break;
									case "ncProgramName_sub6":
										ncProgramName_sub6 = (null!=tableCfgCommon.getValueAt(i, 1))?String.valueOf(tableCfgCommon.getValueAt(i, 1)).trim():"";
										break;
									case "ncProgramName_main":
										ncProgramName_main = (null!=tableCfgCommon.getValueAt(i, 1))?String.valueOf(tableCfgCommon.getValueAt(i, 1)).trim():"";
										break;
									case "cmdCoordinate1":
										cmdCoordinate1 = (null!=tableCfgCommon.getValueAt(i, 1))?String.valueOf(tableCfgCommon.getValueAt(i, 1)).trim():"";
										break;
									case "cmdCoordinate2":
										cmdCoordinate2 = (null!=tableCfgCommon.getValueAt(i, 1))?String.valueOf(tableCfgCommon.getValueAt(i, 1)).trim():"";
										break;
									case "cmdCoordinate3":
										cmdCoordinate3 = (null!=tableCfgCommon.getValueAt(i, 1))?String.valueOf(tableCfgCommon.getValueAt(i, 1)).trim():"";
										break;
									case "cmdCoordinate4":
										cmdCoordinate4 = (null!=tableCfgCommon.getValueAt(i, 1))?String.valueOf(tableCfgCommon.getValueAt(i, 1)).trim():"";
										break;
									case "cmdCoordinate5":
										cmdCoordinate5 = (null!=tableCfgCommon.getValueAt(i, 1))?String.valueOf(tableCfgCommon.getValueAt(i, 1)).trim():"";
										break;
									case "cmdCoordinate6":
										cmdCoordinate6 = (null!=tableCfgCommon.getValueAt(i, 1))?String.valueOf(tableCfgCommon.getValueAt(i, 1)).trim():"";
										break;
									case "debugIP":
										debugIP = (null!=tableCfgCommon.getValueAt(i, 1))?String.valueOf(tableCfgCommon.getValueAt(i, 1)).trim():"";
										break;
									}
								}
							}
							
							if("".equals(brand) || "".equals(model) || "".equals(newCmdName) 
									|| "".equals(newCmdID) || "".equals(newCmdURL)  || "".equals(debugPort) 
									|| "".equals(ncProgramName_sub1) || "".equals(ncProgramName_sub2)  || "".equals(ncProgramName_sub3) 
									|| "".equals(ncProgramName_sub4) || "".equals(ncProgramName_sub5)  || "".equals(ncProgramName_sub6) 
									|| "".equals(cmdCoordinate1) || "".equals(cmdCoordinate2)  || "".equals(cmdCoordinate3) 
									|| "".equals(cmdCoordinate4) || "".equals(cmdCoordinate5)  || "".equals(cmdCoordinate6) 
									|| "".equals(ncProgramName_main)){
								JOptionPane.showMessageDialog(cncWebAPIConfig.getContentPane(), "Command Name/ID/URL/brand/model/port/ncProgram/coordinate can not be null", "Command Setting Error", JOptionPane.ERROR_MESSAGE);
							}else{
								rowCnt = tableInputParameters.getRowCount();
								colCnt = tableInputParameters.getColumnCount();
								if(colCnt > 3 && rowCnt > 0){
									for(int i=0; i<rowCnt; i++){
										String paraName = (null!=tableInputParameters.getValueAt(i, 1))?String.valueOf(tableInputParameters.getValueAt(i, 1)).trim():"";
										String paraType = (null!=tableInputParameters.getValueAt(i, 2))?String.valueOf(tableInputParameters.getValueAt(i, 2)).trim():"";
										String paraVal = (null!=tableInputParameters.getValueAt(i, 3))?String.valueOf(tableInputParameters.getValueAt(i, 3)).trim():"";
										if("".equals(paraName) || "".equals(paraType) || "".equals(paraVal)){
											if("ip".equals(paraName)){
												paraVal = "192.168.0.255";
											}else if("port".equals(paraName)){
												paraVal = "10000";
											}else{
												continue;
											}
										}
										if(!"String".equals(paraType) && !"Integer".equals(paraType) && !"Json".equals(paraType)){
											errMsg += errMsg+"ParaType of "+paraName+"["+paraType+"] can be String, Integer or Json only\n";
											continue;
										}
										
										if("Json".equals(paraType)){
											jsonParas.put(paraName, paraVal);
										}else{
											if("".equals(inParas)){
												inParas = paraName;
												inParasType = paraType;
												inParasVal = paraVal;
											}else{
												inParas += PARASEPERATOR + paraName;
												inParasType += PARASEPERATOR + paraType;
												inParasVal += PARASEPERATOR + paraVal;
											}
										}
									}
									
									if(!jsonParas.isEmpty()){
										if("".equals(inParas)){
											inParas = "data";
											inParasType = "Json";
											inParasVal = jsonParas.toString();
										}else{
											inParas += PARASEPERATOR + "data";
											inParasType += PARASEPERATOR + "Json";
											inParasVal += PARASEPERATOR + jsonParas.toString();
										}
									}
								}
								
								rowCnt = tableOutputParameters.getRowCount();
								colCnt = tableOutputParameters.getColumnCount();
								if(colCnt > 3 && rowCnt > 0){
									for(int i=0; i<rowCnt; i++){
										String paraName = (null!=tableOutputParameters.getValueAt(i, 1))?String.valueOf(tableOutputParameters.getValueAt(i, 1)).trim():"";
										String paraType = (null!=tableOutputParameters.getValueAt(i, 2))?String.valueOf(tableOutputParameters.getValueAt(i, 2)).trim():"";
										String paraVal = (null!=tableOutputParameters.getValueAt(i, 3))?String.valueOf(tableOutputParameters.getValueAt(i, 3)).trim():"";
										if("".equals(paraName) || "".equals(paraType) || "".equals(paraVal)) continue;
										if(!"String".equals(paraType) && !"Integer".equals(paraType) && !"Json".equals(paraType)){
											errMsg += errMsg+"ParaType of "+paraName+"["+paraType+"] can be String or Integer only\n";
											continue;
										}
										
										if("".equals(outParas)){
											outParas = paraName;
											outParasType = paraType;
											outParasVal = paraVal;
										}else{
											outParas += PARASEPERATOR + paraName;
											outParasType += PARASEPERATOR + paraType;
											outParasVal += PARASEPERATOR + paraVal;
										}
									}
								}
							}
							
							if(!"".equals(errMsg)){
								errMsg += errMsg + "Keep going anyway?";
								if(0 != JOptionPane.showConfirmDialog(cncWebAPIConfig.getContentPane(), errMsg, "Proceed With Command Saving?", JOptionPane.YES_NO_OPTION)){
									return;
								}
							}
							
							if(newCmdName.equals(curCncCommand)){
								if(0 != JOptionPane.showConfirmDialog(cncWebAPIConfig.getContentPane(), "Command "+curCncCommand+" is existing, continue to update it with current settings?", "Update Existing Command?", JOptionPane.YES_NO_OPTION)){
									return;
								}
							}
							
							LinkedHashMap<CncWebAPIItems,String> commonCfg = new LinkedHashMap<CncWebAPIItems,String>();
							commonCfg.put(CncWebAPIItems.BRAND, brand);
							commonCfg.put(CncWebAPIItems.MODEL, model);
							commonCfg.put(CncWebAPIItems.URL, newCmdURL);
							commonCfg.put(CncWebAPIItems.NCPROGSUB1, ncProgramName_sub1);
							commonCfg.put(CncWebAPIItems.NCPROGSUB2, ncProgramName_sub2);
							commonCfg.put(CncWebAPIItems.NCPROGSUB3, ncProgramName_sub3);
							commonCfg.put(CncWebAPIItems.NCPROGSUB4, ncProgramName_sub4);
							commonCfg.put(CncWebAPIItems.NCPROGSUB5, ncProgramName_sub5);
							commonCfg.put(CncWebAPIItems.NCPROGSUB6, ncProgramName_sub6);
							commonCfg.put(CncWebAPIItems.NCPROGMAIN, ncProgramName_main);
							commonCfg.put(CncWebAPIItems.COORDINATE1, cmdCoordinate1);
							commonCfg.put(CncWebAPIItems.COORDINATE2, cmdCoordinate2);
							commonCfg.put(CncWebAPIItems.COORDINATE3, cmdCoordinate3);
							commonCfg.put(CncWebAPIItems.COORDINATE4, cmdCoordinate4);
							commonCfg.put(CncWebAPIItems.COORDINATE5, cmdCoordinate5);
							commonCfg.put(CncWebAPIItems.COORDINATE6, cmdCoordinate6);
							if(!"".equals(ftpPort) && !"".equals(ftpUser) && !"".equals(ftpPwd)){
								commonCfg.put(CncWebAPIItems.FTPPORT, ftpPort);
								commonCfg.put(CncWebAPIItems.FTPUSER, ftpUser);
								commonCfg.put(CncWebAPIItems.FTPPWD, ftpPwd);
							}
							commonCfg.put(CncWebAPIItems.DEBUGIP, debugIP);
							commonCfg.put(CncWebAPIItems.DEBUGPORT, debugPort);
							
							LinkedHashMap<CncWebAPIItems,String> oprInfo = new LinkedHashMap<CncWebAPIItems,String>();
							oprInfo.put(CncWebAPIItems.OPERATOR, operator);
							
							LinkedHashMap<CncWebAPIItems,String> cmdInfo = new LinkedHashMap<CncWebAPIItems,String>();
							cmdInfo.put(CncWebAPIItems.ID, newCmdID);
							cmdInfo.put(CncWebAPIItems.NAME, textField_CommandName.getText().trim());
							if(!"".equals(inParas)){
								cmdInfo.put(CncWebAPIItems.INPARAS, inParas);
								cmdInfo.put(CncWebAPIItems.INPARASTYPE, inParasType);
								cmdInfo.put(CncWebAPIItems.INPARASVAL, inParasVal);
							}
							if(!"".equals(outParas)){
								cmdInfo.put(CncWebAPIItems.OUTPARAS, outParas);
								cmdInfo.put(CncWebAPIItems.OUTPARASTYPE, outParasType);
								cmdInfo.put(CncWebAPIItems.OUTPARASVAL, outParasVal);
							}
							
							btnSaveCommand.setEnabled(false);
							CncWebAPI cncWebAPI = CncWebAPI.getInstance();
							@SuppressWarnings("unchecked")
							LinkedHashMap<String,Object> command = (LinkedHashMap<String, Object>) cncWebAPI.getData(mainKey).get(curCncOperation);
							if(!"".equals(curCncCommand) && !newCmdName.equals(curCncCommand)){
								if(0 == JOptionPane.showConfirmDialog(cncWebAPIConfig.getContentPane(), "Change command "+curCncCommand+" to "+newCmdName+" and save it with current settings?\nNote: Choose 'No' to add a new command", "Change Existing Command?", JOptionPane.YES_NO_OPTION)){
									command.remove(curCncCommand);
								}
							}
							command.put(curCncOperation, oprInfo);
							command.put(newCmdName, cmdInfo);
							cncWebAPI.setData(mainKey, "Common", commonCfg);
							cncWebAPI.setData(mainKey, curCncOperation, command);
							
							if(XmlUtils.saveCncWebAPIXml()){
								setCurCommandParas(newCmdName);
								lblRunningMsg.setText("Save command "+newCmdName+" OK");
							}else{
								lblRunningMsg.setText("Save command "+newCmdName+" failed");
								JOptionPane.showMessageDialog(cncWebAPIConfig.getContentPane(), "Save command "+newCmdName+" failed!", "Failed", JOptionPane.ERROR_MESSAGE);
							}
							btnSaveCommand.setEnabled(true);
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
						if(1 == arg0.getButton()) cncWebAPIConfig.dispose();
					}
				});
				buttonPane.add(btnClose);
			}
		}
		
		setCurCommandParas();
	}

}
