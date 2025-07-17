package com.sto.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import com.sto.data.ProductSpec;
import com.sto.utils.GUIUtils;
import com.sto.utils.XmlUtils;
import javax.swing.JComboBox;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class ProdSpec extends JDialog {
	private static final long serialVersionUID = 5050029939364015546L;
	private final JPanel contentPanel = new JPanel();
	private JComboBox<String> comboBoxSpecName;
	
	private static ProdSpec productSpec = new ProdSpec();
	private JTable tableCriterialConfig;
	private JTable tableImagePathConfig;
	private JLabel lblMessage;
	
	public static ProdSpec getInstance(){
		return productSpec;
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
	
	private void setTableModel(JTable myTable, String[] title, Object[][] data){
		myTable.setModel(new MyTableModel(title,data));
		myTable.setRowHeight(25);
		fitTableColumns(myTable);
		setMessage("");
	}
	
	private void setMessage(String msg){
		if(null!=lblMessage) lblMessage.setText(msg);
	}
	
	/**
	 * Create the dialog.
	 */
	private ProdSpec() {
		XmlUtils.parseProductSpec();
		setIconImage(Toolkit.getDefaultToolkit().getImage(ProdSpec.class.getResource("/com/sto/img/standard_24.png")));
		setModal(true);
		setTitle("Product Spec - Criteria of OK Product");
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 450;
		int height = 600;
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0};
		gbl_contentPanel.rowHeights = new int[] {30, 280, 30, 120};
		gbl_contentPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JPanel specPanel = new JPanel();
			specPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
			GridBagConstraints gbc_specPanel = new GridBagConstraints();
			gbc_specPanel.fill = GridBagConstraints.BOTH;
			gbc_specPanel.gridx = 0;
			gbc_specPanel.gridy = 0;
			contentPanel.add(specPanel, gbc_specPanel);
			GridBagLayout gbl_specPanel = new GridBagLayout();
			gbl_specPanel.columnWidths = new int[] {250, 78, 120, 2};
			gbl_specPanel.rowHeights = new int[] {0};
			gbl_specPanel.columnWeights = new double[]{0.0, 0.0, 1.0};
			gbl_specPanel.rowWeights = new double[]{0.0};
			specPanel.setLayout(gbl_specPanel);
			{
				JLabel lblCommonSetting = new JLabel("Common Setting");
				GridBagConstraints gbc_lblCommonSetting = new GridBagConstraints();
				gbc_lblCommonSetting.insets = new Insets(0, 0, 0, 5);
				gbc_lblCommonSetting.gridx = 0;
				gbc_lblCommonSetting.gridy = 0;
				specPanel.add(lblCommonSetting, gbc_lblCommonSetting);
			}
			{
				JLabel lblSpecName = new JLabel("Spec Name");
				lblSpecName.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent arg0) {
						if(2==arg0.getClickCount() && lblSpecName.isEnabled()){
							ProductSpec config = ProductSpec.getInstance();
							String specName = comboBoxSpecName.getSelectedItem().toString();
							String configID = config.getConfigID(specName);
							if(0==JOptionPane.showConfirmDialog(contentPanel, "Are you sure of deleting spec below?\r\n"+specName+"= \""+configID+"\"", "Change imgProcRootDir?", JOptionPane.YES_NO_OPTION)){
								config.removeData(configID);
								if(XmlUtils.saveProductSpec(config.getDataMap())){
									setTableModel(tableCriterialConfig, config.getCfgDataTitle(), config.getCfgData(null,false));
									setTableModel(tableImagePathConfig, config.getCfgDataTitle(), config.getCfgData(null,true));
									GUIUtils.setComboBoxValues(comboBoxSpecName, config.getSpecNames());
								}else{
									JOptionPane.showMessageDialog(contentPanel, "Delete Spec("+specName+" = \""+configID+"\") failed, please try again.", "Delete Spec Failed", JOptionPane.ERROR_MESSAGE);
								}
							}
						}
					}
				});
				lblSpecName.setToolTipText("Double click to delete current spec");
				GridBagConstraints gbc_lblSpecName = new GridBagConstraints();
				gbc_lblSpecName.insets = new Insets(0, 0, 0, 5);
				gbc_lblSpecName.anchor = GridBagConstraints.EAST;
				gbc_lblSpecName.gridx = 1;
				gbc_lblSpecName.gridy = 0;
				specPanel.add(lblSpecName, gbc_lblSpecName);
			}
			{
				comboBoxSpecName = new JComboBox<String>();
				comboBoxSpecName.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent arg0) {
						ProductSpec config = ProductSpec.getInstance();
						String configID = config.getConfigID(comboBoxSpecName.getSelectedItem().toString());
						setTableModel(tableCriterialConfig, config.getCfgDataTitle(), config.getCfgData(configID,false));
						setTableModel(tableImagePathConfig, config.getCfgDataTitle(), config.getCfgData(configID,true));
					}
				});
				GridBagConstraints gbc_comboBoxSpecName = new GridBagConstraints();
				gbc_comboBoxSpecName.fill = GridBagConstraints.HORIZONTAL;
				gbc_comboBoxSpecName.gridx = 2;
				gbc_comboBoxSpecName.gridy = 0;
				specPanel.add(comboBoxSpecName, gbc_comboBoxSpecName);
			}
		}
		{
			JScrollPane scrollPaneCommonSetting = new JScrollPane();
			GridBagConstraints gbc_scrollPaneCommonSetting = new GridBagConstraints();
			gbc_scrollPaneCommonSetting.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPaneCommonSetting.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneCommonSetting.gridx = 0;
			gbc_scrollPaneCommonSetting.gridy = 1;
			contentPanel.add(scrollPaneCommonSetting, gbc_scrollPaneCommonSetting);
			{
				tableCriterialConfig = new JTable();
				scrollPaneCommonSetting.setViewportView(tableCriterialConfig);
			}
		}
		{
			JPanel imgPathPanel = new JPanel();
			imgPathPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
			GridBagConstraints gbc_imgPathPanel = new GridBagConstraints();
			gbc_imgPathPanel.fill = GridBagConstraints.BOTH;
			gbc_imgPathPanel.gridx = 0;
			gbc_imgPathPanel.gridy = 2;
			contentPanel.add(imgPathPanel, gbc_imgPathPanel);
			GridBagLayout gbl_imgPathPanel = new GridBagLayout();
			gbl_imgPathPanel.columnWidths = new int[] {250, 65, 120, 15};
			gbl_imgPathPanel.rowHeights = new int[] {0};
			gbl_imgPathPanel.columnWeights = new double[]{0.0, 0.0, 0.0};
			gbl_imgPathPanel.rowWeights = new double[]{0.0};
			imgPathPanel.setLayout(gbl_imgPathPanel);
			{
				JLabel lblRawImagePath = new JLabel("Raw Image Path");
				GridBagConstraints gbc_lblRawImagePath = new GridBagConstraints();
				gbc_lblRawImagePath.insets = new Insets(0, 0, 0, 5);
				gbc_lblRawImagePath.gridx = 0;
				gbc_lblRawImagePath.gridy = 0;
				imgPathPanel.add(lblRawImagePath, gbc_lblRawImagePath);
			}
			{
				JButton btnAddImagePath = new JButton("Add Image Path");
				btnAddImagePath.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent arg0) {
						if(1==arg0.getButton() && btnAddImagePath.isEnabled()){
							ProductSpec productSpec = ProductSpec.getInstance();
							String configID = productSpec.getConfigID(comboBoxSpecName.getSelectedItem().toString());
							int row = 0, col = 0;
							Object[][] newData = null;
							String[] title = productSpec.getCfgDataTitle();
							Object[][] data = productSpec.getCfgData(configID, true);
							if(data.length>0) row = data.length;
							col = title.length;
							newData = new Object[row+1][col];
							if(row>0){
								for(int i=0; i<row; i++){
									newData[i] = data[i].clone();
								}
							}
							newData[row][0] = "imgFolder_"+row;
							setTableModel(tableImagePathConfig, title, newData);
						}
					}
				});
				GridBagConstraints gbc_btnAddImagePath = new GridBagConstraints();
				gbc_btnAddImagePath.gridx = 2;
				gbc_btnAddImagePath.gridy = 0;
				imgPathPanel.add(btnAddImagePath, gbc_btnAddImagePath);
			}
		}
		{
			JScrollPane scrollPaneRawImagePath = new JScrollPane();
			GridBagConstraints gbc_scrollPaneRawImagePath = new GridBagConstraints();
			gbc_scrollPaneRawImagePath.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPaneRawImagePath.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneRawImagePath.gridx = 0;
			gbc_scrollPaneRawImagePath.gridy = 3;
			contentPanel.add(scrollPaneRawImagePath, gbc_scrollPaneRawImagePath);
			{
				tableImagePathConfig = new JTable();
				scrollPaneRawImagePath.setViewportView(tableImagePathConfig);
			}
		}
		
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				GridBagLayout gbl_buttonPane = new GridBagLayout();
				gbl_buttonPane.columnWidths = new int[]{308, 57, 59, 0};
				gbl_buttonPane.rowHeights = new int[]{23, 0};
				gbl_buttonPane.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
				gbl_buttonPane.rowWeights = new double[]{0.0, Double.MIN_VALUE};
				buttonPane.setLayout(gbl_buttonPane);
			}
			{
				JButton btnSave = new JButton("Save");
				btnSave.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if(1 == e.getButton()){
							ProductSpec productSpec = ProductSpec.getInstance();
							int rowCnt = 0;
							boolean needToSave = false;
							String paraName = "", paraVal = "", newCfgID = "", newName = "";
							String tmpName = "", tmpCfgID = "", oriCfgID = "", oriName = "";
							
							rowCnt = tableCriterialConfig.getRowCount();
							for(int i=0; i<rowCnt; i++){
								paraName = (null!=tableCriterialConfig.getValueAt(i, 0))?String.valueOf(tableCriterialConfig.getValueAt(i, 0)).trim():"";
								paraVal = (null!=tableCriterialConfig.getValueAt(i, 1))?String.valueOf(tableCriterialConfig.getValueAt(i, 1)).trim():"";
								
								if("imgProcRootDir".equals(paraName) && !"".equals(paraVal)){
									if("NULL".equals(paraVal.toUpperCase())) paraVal = "";
									newCfgID = paraVal;
								}
								if("specName".equals(paraName) && !"".equals(paraVal)){
									if("NULL".equals(paraVal.toUpperCase())) paraVal = "";
									newName = paraVal;
								}
								if(!"".equals(newCfgID) && !"".equals(newName)) break;
							}
							
							if("".equals(newCfgID) || "".equals(newName)){
								JOptionPane.showMessageDialog(contentPanel, "Save product spec failed\r\nspecName or imgProcRootDir can't be blank", "Save Product Spec Error", JOptionPane.ERROR_MESSAGE);
								return;
							}else{
								oriName = comboBoxSpecName.getSelectedItem().toString();
								oriCfgID = productSpec.getConfigID(oriName);
								if(!oriName.equals(newName) || !oriCfgID.equals(newCfgID)){
									if(!oriCfgID.equals(newCfgID)) tmpName = productSpec.getSpecName(newCfgID);
									if(!oriName.equals(newName)) tmpCfgID = productSpec.getConfigID(newName);
									if(!"".equals(tmpCfgID)){
										JOptionPane.showMessageDialog(contentPanel, "Save product spec failed\r\nspecName \""+newName+"\" is existing!", "Save Product Spec Error", JOptionPane.ERROR_MESSAGE);
										return;
									}else if(!"".equals(tmpName)){
										JOptionPane.showMessageDialog(contentPanel, "Save product spec failed\r\n"+tmpName+" = \""+newCfgID+"\" is existing!", "Save Product Spec Error", JOptionPane.ERROR_MESSAGE);
										return;
									}else if(oriName.equals(newName) && !oriCfgID.equals(newCfgID)){
										productSpec.removeData(oriCfgID);
									}else if("".equals(tmpName) && "".equals(tmpCfgID)){
										int rtn = JOptionPane.showConfirmDialog(contentPanel, "Product spec "+newName+" = \""+newCfgID+"\" is not existing,replace current spec?\r\n"
												+ "1.\"Yes\" to replace current spec("+oriName+" = \""+oriCfgID+"\")\r\n"
												+ "2.\"No\" to add new spec("+newName+" = \""+newCfgID+"\")\r\n"
												+ "3.\"Cancel\" to skip any change", "Replace Or Add Product Spec?", JOptionPane.YES_NO_CANCEL_OPTION);
										if(2==rtn){
											return;
										}else if(0==rtn){
											productSpec.removeData(oriCfgID);
										}
									}
								}
								
								for(int i=0; i<rowCnt; i++){
									paraName = (null!=tableCriterialConfig.getValueAt(i, 0))?String.valueOf(tableCriterialConfig.getValueAt(i, 0)).trim():"";
									paraVal = (null!=tableCriterialConfig.getValueAt(i, 1))?String.valueOf(tableCriterialConfig.getValueAt(i, 1)).trim():"";
									paraName = paraName.replace("(mm)", "");
									paraName = paraName.replace("(degree)", "");
									
									if(!"".equals(paraName) && !"".equals(paraVal)){
										if("NULL".equals(paraVal.toUpperCase())) paraVal = "";
										productSpec.setData(newCfgID, paraName, paraVal);
										needToSave = true;
									}
								}
								
								ArrayList<String> imgPath = new ArrayList<String>();
								rowCnt = tableImagePathConfig.getRowCount();
								for(int i=0; i<rowCnt; i++){
									paraName = (null!=tableImagePathConfig.getValueAt(i, 0))?String.valueOf(tableImagePathConfig.getValueAt(i, 0)).trim():"";
									paraVal = (null!=tableImagePathConfig.getValueAt(i, 1))?String.valueOf(tableImagePathConfig.getValueAt(i, 1)).trim():"";
									paraName = paraName.replace("(mm)", "");
									paraName = paraName.replace("(degree)", "");
									
									if(!"".equals(paraName) && !"".equals(paraVal)){
										if("NULL".equals(paraVal.toUpperCase())) paraVal = "";
										imgPath.add(paraVal);
									}
								}
								if(imgPath.size()>0){
									productSpec.setData(newCfgID, "rawImgFolders", imgPath);
									needToSave = true;
								}
								
								if(needToSave){
									if(!XmlUtils.saveProductSpec(productSpec.getDataMap())){
										JOptionPane.showMessageDialog(contentPanel, "Save product spec failed\r\nplease check system log for the details", "Save Product Spec Error", JOptionPane.ERROR_MESSAGE);
										setMessage("Save product spec failed");
									}else{
										GUIUtils.setComboBoxValues(comboBoxSpecName, productSpec.getSpecNames());
										GUIUtils.setComboBoxSelectedIdx(comboBoxSpecName, newName);
										setMessage("Save product spec OK");
									}
								}
							}
						}
					}
				});
				{
					lblMessage = new JLabel("");
					GridBagConstraints gbc_lblMessage = new GridBagConstraints();
					gbc_lblMessage.insets = new Insets(0, 0, 0, 5);
					gbc_lblMessage.gridx = 0;
					gbc_lblMessage.gridy = 0;
					buttonPane.add(lblMessage, gbc_lblMessage);
				}
				GridBagConstraints gbc_btnSave = new GridBagConstraints();
				gbc_btnSave.anchor = GridBagConstraints.NORTHWEST;
				gbc_btnSave.insets = new Insets(0, 0, 0, 5);
				gbc_btnSave.gridx = 1;
				gbc_btnSave.gridy = 0;
				buttonPane.add(btnSave, gbc_btnSave);
				getRootPane().setDefaultButton(btnSave);
			}
			JButton btnClose = new JButton("Close");
			btnClose.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(1 == e.getButton()){
						productSpec.dispose();
					}
				}
			});
			GridBagConstraints gbc_btnClose = new GridBagConstraints();
			gbc_btnClose.anchor = GridBagConstraints.NORTHWEST;
			gbc_btnClose.gridx = 2;
			gbc_btnClose.gridy = 0;
			buttonPane.add(btnClose, gbc_btnClose);
		}
		
		ProductSpec config = ProductSpec.getInstance();
		setTableModel(tableCriterialConfig, config.getCfgDataTitle(), config.getCfgData(null,false));
		setTableModel(tableImagePathConfig, config.getCfgDataTitle(), config.getCfgData(null,true));
		GUIUtils.setComboBoxValues(comboBoxSpecName, config.getSpecNames());
	}
}
