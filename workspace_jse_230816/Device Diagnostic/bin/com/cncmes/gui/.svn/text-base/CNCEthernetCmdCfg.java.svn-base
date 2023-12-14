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

import com.cncmes.base.CNC;
import com.cncmes.data.CncEthernetCmd;
import com.cncmes.data.DevHelper;
import com.cncmes.handler.SocketDataHandler;
import com.cncmes.utils.GUIUtils;
import com.cncmes.utils.XmlUtils;

import net.sf.json.JSONObject;

public class CNCEthernetCmdCfg extends JDialog {
	private static final long serialVersionUID = 26L;
	private final JPanel contentPanel = new JPanel();
	private static CNCEthernetCmdCfg cncEthernetCmdCfg = new CNCEthernetCmdCfg();
	private CncEthernetCmd cncEthernetCmd = null;
	public static CNCEthernetCmdCfg getInstance(){
		return cncEthernetCmdCfg;
	}
	
	private JTextField textField_CommandName;
	private JLabel lblCommandName;
	private JLabel lblRunningMsg;
	private JTable tableCommonParameters;
	private JTable tableInputParameters;
	private JList<String> listAllCommands;
	private JComboBox<String> comboBoxCncModel;
	private JComboBox<String> comboBoxOperation;
	
	private Map<String,Object> socketRespData= new LinkedHashMap<String,Object>();
	private String curCncOperation;
	private String curCncModel;
	private String curCncCommand;
	
	private JTextField textField_CommandID;
	private JTable tableOutputParameters;
	private JTextField textFieldOperator;
	
	/**
	 * Create the dialog.
	 */
	private CNCEthernetCmdCfg() {
		cncEthernetCmd = CncEthernetCmd.getInstance();
		XmlUtils.parseCncEthernetCmdXml();
		setIconImage(Toolkit.getDefaultToolkit().getImage(CNCEthernetCmdCfg.class.getResource("/com/cncmes/img/CNC_24.png")));
		setTitle("CNC Ethernet Command Config");
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
						curCncModel = comboBoxCncModel.getSelectedItem().toString();
						GUIUtils.setComboBoxValues(comboBoxOperation, cncEthernetCmd.getAllOperations(curCncModel));
						setCurCommandParas();
					}
				});
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
				gbl_panelMainCenter.rowHeights = new int[] {40, 140, 130, 130, 30, 3};
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
						tableCommonParameters = new JTable(new MyTableModel(cncEthernetCmd.getCommonConfigDataTitle(),cncEthernetCmd.getCommonConfigDataTable(curCncModel)));
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
						tableInputParameters = new JTable(new MyTableModel(cncEthernetCmd.getCommandParaDataTitle(),cncEthernetCmd.getCommandParaDataTable(curCncModel, curCncOperation, curCncCommand,true)));
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
					gbc_panelCfgLast.insets = new Insets(0, 0, 5, 0);
					gbc_panelCfgLast.fill = GridBagConstraints.BOTH;
					gbc_panelCfgLast.gridx = 0;
					gbc_panelCfgLast.gridy = 3;
					panelMainCenter.add(panelCfgLast, gbc_panelCfgLast);
					GridBagLayout gbl_panelCfgLast = new GridBagLayout();
					gbl_panelCfgLast.columnWidths = new int[]{0, 0};
					gbl_panelCfgLast.rowHeights = new int[]{0, 0, 0};
					gbl_panelCfgLast.columnWeights = new double[]{1.0, Double.MIN_VALUE};
					gbl_panelCfgLast.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
					panelCfgLast.setLayout(gbl_panelCfgLast);
					{
						JLabel lblCommandOutputParameters = new JLabel(" Command Output Parameters");
						GridBagConstraints gbc_lblCommandOutputParameters = new GridBagConstraints();
						gbc_lblCommandOutputParameters.anchor = GridBagConstraints.WEST;
						gbc_lblCommandOutputParameters.insets = new Insets(0, 0, 5, 0);
						gbc_lblCommandOutputParameters.gridx = 0;
						gbc_lblCommandOutputParameters.gridy = 0;
						panelCfgLast.add(lblCommandOutputParameters, gbc_lblCommandOutputParameters);
					}
					{
						JScrollPane scrollPaneOutputParameters = new JScrollPane();
						GridBagConstraints gbc_scrollPaneOutputParameters = new GridBagConstraints();
						gbc_scrollPaneOutputParameters.fill = GridBagConstraints.BOTH;
						gbc_scrollPaneOutputParameters.gridx = 0;
						gbc_scrollPaneOutputParameters.gridy = 1;
						panelCfgLast.add(scrollPaneOutputParameters, gbc_scrollPaneOutputParameters);
						{
							tableOutputParameters = new JTable(new MyTableModel(cncEthernetCmd.getCommandParaDataTitle(),cncEthernetCmd.getCommandParaDataTable(curCncModel, curCncOperation, curCncCommand,false)));
							tableOutputParameters.setRowHeight(25);
							fitTableColumns(tableOutputParameters);
							scrollPaneOutputParameters.setViewportView(tableOutputParameters);
						}
					}
				}
				{
					JPanel panelCfgFirst = new JPanel();
					GridBagConstraints gbc_panelCfgFirst = new GridBagConstraints();
					gbc_panelCfgFirst.fill = GridBagConstraints.BOTH;
					gbc_panelCfgFirst.gridx = 0;
					gbc_panelCfgFirst.gridy = 4;
					panelMainCenter.add(panelCfgFirst, gbc_panelCfgFirst);
					GridBagLayout gbl_panelCfgFirst = new GridBagLayout();
					gbl_panelCfgFirst.columnWidths = new int[]{0, 0, 0};
					gbl_panelCfgFirst.rowHeights = new int[] {0};
					gbl_panelCfgFirst.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
					gbl_panelCfgFirst.rowWeights = new double[]{0.0};
					panelCfgFirst.setLayout(gbl_panelCfgFirst);
					{
						JLabel lblOperationExecutive = new JLabel(" Operation Executive");
						GridBagConstraints gbc_lblOperationExecutive = new GridBagConstraints();
						gbc_lblOperationExecutive.insets = new Insets(0, 0, 0, 5);
						gbc_lblOperationExecutive.anchor = GridBagConstraints.EAST;
						gbc_lblOperationExecutive.gridx = 0;
						gbc_lblOperationExecutive.gridy = 0;
						panelCfgFirst.add(lblOperationExecutive, gbc_lblOperationExecutive);
					}
					{
						textFieldOperator = new JTextField();
						textFieldOperator.setText("Myself");
						GridBagConstraints gbc_textFieldOperator = new GridBagConstraints();
						gbc_textFieldOperator.fill = GridBagConstraints.HORIZONTAL;
						gbc_textFieldOperator.gridx = 1;
						gbc_textFieldOperator.gridy = 0;
						panelCfgFirst.add(textFieldOperator, gbc_textFieldOperator);
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
									if(0 == JOptionPane.showConfirmDialog(contentPanel, "Are you sure to remove command "+curCncCommand+"?", "Command Removing", JOptionPane.YES_NO_OPTION)){
										btnRemoveCommand.setEnabled(false);
										LinkedHashMap<String, Object> oprMap = cncEthernetCmd.getData(curCncModel);
										if(null != oprMap){
											@SuppressWarnings("unchecked")
											LinkedHashMap<String, Object> cmdMap = (LinkedHashMap<String, Object>) oprMap.get(curCncOperation);
											if(null != cmdMap){
												cmdMap.remove(curCncCommand);
												if(XmlUtils.saveCncEthernetCmdXml()){
													setCurCommandParas();
												}else{
													lblRunningMsg.setText("Remove command "+curCncCommand+" failed");
												}
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
							String inputParas = "", outputParas = "";
							String newCmdName = textField_CommandName.getText().trim();
							String newCmdID = textField_CommandID.getText().trim();
							String operator = textFieldOperator.getText().trim();
							String mainKey = curCncModel;
							if("".equals(operator)) operator = "Myself";
							
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
										if(!"".equals(paraVal)) commonCfg.put(paraName, paraVal);
									}
									cncEthernetCmd.setData(mainKey, "Common", commonCfg);
								}
								
								@SuppressWarnings("unchecked")
								LinkedHashMap<String, Object> cmds = (LinkedHashMap<String, Object>) cncEthernetCmd.getData(mainKey).get(curCncOperation);
								if(null == cmds) cmds = new LinkedHashMap<String, Object>();
								
								rowCnt = tableInputParameters.getRowCount();
								colCnt = tableInputParameters.getColumnCount();
								if(colCnt > 2 && rowCnt > 0){
									JSONObject jsonObj = new JSONObject();
									for(int i=0; i<rowCnt; i++){
										String paraName = (null!=tableInputParameters.getValueAt(i, 1))?String.valueOf(tableInputParameters.getValueAt(i, 1)).trim():"";
										String paraVal = (null!=tableInputParameters.getValueAt(i, 2))?String.valueOf(tableInputParameters.getValueAt(i, 2)).trim():"";
										if("".equals(paraName) || "".equals(paraVal)) continue;
										jsonObj.put(paraName, paraVal);
									}
									if(jsonObj.size() > 0) inputParas = jsonObj.toString();
								}
								
								rowCnt = tableOutputParameters.getRowCount();
								colCnt = tableOutputParameters.getColumnCount();
								if(colCnt > 2 && rowCnt > 0){
									JSONObject jsonObj = new JSONObject();
									for(int i=0; i<rowCnt; i++){
										String paraName = (null!=tableOutputParameters.getValueAt(i, 1))?String.valueOf(tableOutputParameters.getValueAt(i, 1)).trim():"";
										String paraVal = (null!=tableOutputParameters.getValueAt(i, 2))?String.valueOf(tableOutputParameters.getValueAt(i, 2)).trim():"";
										if("".equals(paraName) || "".equals(paraVal)) continue;
										jsonObj.put(paraName, paraVal);
									}
									if(jsonObj.size() > 0) outputParas = jsonObj.toString();
								}
								
								ArrayList<String> oprInfo = new ArrayList<String>();
								oprInfo.add(operator);
								cmds.put(curCncOperation, oprInfo);
								
								ArrayList<String> para = new ArrayList<String>();
								para.add(inputParas);
								para.add(outputParas);
								para.add(newCmdName);
								cmds.put(newCmdName+"#"+newCmdID, para);
								
								newCmdName = newCmdName+"#"+newCmdID;
								if(newCmdName.equals(curCncCommand)){
									if(0 != JOptionPane.showConfirmDialog(contentPanel, "Command "+curCncCommand+" is existing, continue to update it with current settings?", "Update Existing Command?", JOptionPane.YES_NO_OPTION)){
										return;
									}
								}
								
								if(!"".equals(curCncCommand) && !newCmdName.equals(curCncCommand)){
									if(0 == JOptionPane.showConfirmDialog(contentPanel, "Change command "+curCncCommand+" to "+newCmdName+" and save it with current settings?\nNote: Choose 'No' to add a new command", "Change Existing Command?", JOptionPane.YES_NO_OPTION)){
										cmds.remove(curCncCommand);
									}
								}
								cncEthernetCmd.setData(mainKey, curCncOperation, cmds);
								
								btnSaveCommand.setEnabled(false);
								if(XmlUtils.saveCncEthernetCmdXml()){
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
						if(1 == arg0.getButton()) cncEthernetCmdCfg.dispose();
					}
				});
				buttonPane.add(btnClose);
			}
		}
		
		GUIUtils.setComboBoxValues(comboBoxCncModel, new String[] {"AssistantCNC","DummyCNC"});
		curCncModel = comboBoxCncModel.getSelectedItem().toString();
		GUIUtils.setComboBoxValues(comboBoxOperation, cncEthernetCmd.getAllOperations(curCncModel));
		setCurCommandParas();
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
		curCncModel = comboBoxCncModel.getSelectedItem().toString();
		curCncOperation = comboBoxOperation.getSelectedItem().toString();
		String[] cmds = cncEthernetCmd.getAllCommands(curCncModel, curCncOperation);
		if(null != cmdName && cmdName.length > 0 && !"".equals(cmdName[0])){
			curCncCommand = cmdName[0];
		}else{
			curCncCommand = cmds[0];
		}
		
		textField_CommandName.setText(curCncCommand.split("#")[0]);
		textField_CommandID.setText(curCncCommand.split("#")[1]);
		textFieldOperator.setText(cncEthernetCmd.getOperationExecutive(curCncModel, curCncOperation));
		
		lblRunningMsg.setText(curCncModel+": "+curCncOperation+"/"+curCncCommand);
		GUIUtils.setJListContent(listAllCommands, cmds);
		GUIUtils.setJListSelectedIdx(listAllCommands, curCncCommand);
		
		tableCommonParameters.setModel(new MyTableModel(cncEthernetCmd.getCommonConfigDataTitle(),cncEthernetCmd.getCommonConfigDataTable(curCncModel)));
		tableInputParameters.setModel(new MyTableModel(cncEthernetCmd.getCommandParaDataTitle(),cncEthernetCmd.getCommandParaDataTable(curCncModel, curCncOperation, curCncCommand, true)));
		tableOutputParameters.setModel(new MyTableModel(cncEthernetCmd.getCommandParaDataTitle(),cncEthernetCmd.getCommandParaDataTable(curCncModel, curCncOperation, curCncCommand, false)));
		fitTableColumns(tableCommonParameters);
		fitTableColumns(tableInputParameters);
		fitTableColumns(tableOutputParameters);
	}
	
	private void executeCommand(boolean singleMode){
		String mainKey = curCncModel;
		DevHelper devHelper = DevHelper.getInstance();
		
		LinkedHashMap<String, Object> config = cncEthernetCmd.getCommonConfig(mainKey);
		if(null == config){
			lblRunningMsg.setText("Error: Load common settings failed");
			return;
		}
		
		String debugIP = (String) config.get("debugIP");
		String debugPort = (String) config.get("debugPort");
		if(null == debugIP || null == debugPort){
			lblRunningMsg.setText("Error: Debug IP or Port error");
			return;
		}
		
		LinkedHashMap<String, String> inParas = new LinkedHashMap<String, String>();
		inParas.put("port", debugPort);
		inParas.put("model", mainKey);
		if(singleMode) inParas.put("singleCmd", curCncCommand);
		if(curCncOperation.indexOf("Fixture")>0){
			inParas.put("workZone", "1");
		}
		boolean success = false;
		CNC cncCtrl = (CNC) devHelper.getCtrlByModel(mainKey);
		if(null != cncCtrl) success = cncCtrl.sendCommand(debugIP, curCncOperation, inParas, null);
		if(!success){
			if(null == cncCtrl){
				lblRunningMsg.setText("Get Controller failed");
			}else{
				if(singleMode){
					lblRunningMsg.setText("Execute command "+curCncCommand+" failed,check command log for the details");
				}else{
					lblRunningMsg.setText("Execute operation "+curCncOperation+" failed,check command log for the details");
				}
			}
		}else{
			if(singleMode){
				lblRunningMsg.setText("Execute command "+curCncCommand+" OK");
			}else{
				lblRunningMsg.setText("Execute operation "+curCncOperation+" OK");
			}
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
	
	class SocketClientDataHandler extends SocketDataHandler{
		@Override
		public void doHandle(String in, Socket s) {
			lblRunningMsg.setText(in);
			
			if(null == s) return;
			String ip = s.getInetAddress().toString().replace("/", "");
			socketRespData.put(ip+":"+s.getPort(), in);
		}
	}
}
