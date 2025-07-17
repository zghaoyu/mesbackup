package com.sto.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.LinkedHashMap;

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
import com.sto.data.SystemConfig;
import com.sto.utils.LogUtils;
import com.sto.utils.XmlUtils;

public class SysConfig extends JDialog {
	private static final long serialVersionUID = 1264785192220319879L;
	private final JPanel contentPanel = new JPanel();
	
	private static SysConfig sysConfig = new SysConfig();
	private JTable tableCommonSetting;
	private JTable tableImgPathSetting;
	public static SysConfig getInstance(){
		return sysConfig;
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
		private boolean[] rowEnabled;
		private int rowCount = 0;
		private int colCount = 0;
		String val = "";
		public MyTableModel(String[] tableTitle,Object[][] tableData){
			super();
			title = tableTitle;
			myData = tableData;
			colCount = tableTitle.length;
			if(null != tableData){
				rowCount = tableData.length;
				rowEnabled = new boolean[rowCount];
				for(int i=0; i<rowCount; i++){
					val = ""+myData[i][0];
					if(val.endsWith("Dir") || val.endsWith("Folder")){
						rowEnabled[i] = false;
					}else{
						rowEnabled[i] = true;
					}
				}
			}
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
				return rowEnabled[row];
			}
		}
	}
	
	private void setTableModel(JTable myTable, String[] title, Object[][] data){
		myTable.setModel(new MyTableModel(title,data));
		myTable.setRowHeight(25);
		fitTableColumns(myTable);
	}
	
	/**
	 * Create the dialog.
	 */
	private SysConfig() {
		XmlUtils.parseSystemConfig();
		XmlUtils.parseProductSpec();
		setIconImage(Toolkit.getDefaultToolkit().getImage(SysConfig.class.getResource("/com/sto/img/setting_24.png")));
		setModal(true);
		setTitle("System Config");
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 450;
		int height = 450;
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0};
		gbl_contentPanel.rowHeights = new int[] {30, 175, 30, 130};
		gbl_contentPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 1.0, 0.0, 1.0};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblCommonSetting = new JLabel("Common Setting");
			GridBagConstraints gbc_lblCommonSetting = new GridBagConstraints();
			gbc_lblCommonSetting.insets = new Insets(0, 0, 5, 0);
			gbc_lblCommonSetting.gridx = 0;
			gbc_lblCommonSetting.gridy = 0;
			contentPanel.add(lblCommonSetting, gbc_lblCommonSetting);
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
				tableCommonSetting = new JTable();
				scrollPaneCommonSetting.setViewportView(tableCommonSetting);
			}
		}
		{
			JLabel lblImgPathSetting = new JLabel("Image Process Local Path ( Read Only Here - Configured In Product Spec)");
			GridBagConstraints gbc_lblImgPathSetting = new GridBagConstraints();
			gbc_lblImgPathSetting.insets = new Insets(0, 0, 5, 0);
			gbc_lblImgPathSetting.gridx = 0;
			gbc_lblImgPathSetting.gridy = 2;
			contentPanel.add(lblImgPathSetting, gbc_lblImgPathSetting);
		}
		{
			JScrollPane scrollPaneImgPathSetting = new JScrollPane();
			GridBagConstraints gbc_scrollPaneImgPathSetting = new GridBagConstraints();
			gbc_scrollPaneImgPathSetting.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPaneImgPathSetting.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneImgPathSetting.gridx = 0;
			gbc_scrollPaneImgPathSetting.gridy = 3;
			contentPanel.add(scrollPaneImgPathSetting, gbc_scrollPaneImgPathSetting);
			{
				tableImgPathSetting = new JTable();
				tableImgPathSetting.setEnabled(false);
				scrollPaneImgPathSetting.setViewportView(tableImgPathSetting);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton btnSave = new JButton("Save");
				btnSave.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if(1 == e.getButton()){
							SystemConfig systemConfig = SystemConfig.getInstance();
							int rowCnt = 0;
							boolean needToSave = false;
							
							rowCnt = tableCommonSetting.getRowCount();
							for(int i=0; i<rowCnt; i++){
								String paraName = (null!=tableCommonSetting.getValueAt(i, 0))?String.valueOf(tableCommonSetting.getValueAt(i, 0)).trim():"";
								String paraVal = (null!=tableCommonSetting.getValueAt(i, 1))?String.valueOf(tableCommonSetting.getValueAt(i, 1)).trim():"";
								paraName = paraName.replace("(s)", "").replace("(ms)", "");
								
								if(!"".equals(paraName) && !"".equals(paraVal)){
									if("NULL".equals(paraVal.toUpperCase())) paraVal = "";
									systemConfig.setData("CommonCfg", paraName, paraVal);
									needToSave = true;
								}
							}
							
							if(needToSave){
								if(!XmlUtils.saveSystemConfig(systemConfig.getDataMap())){
									JOptionPane.showMessageDialog(contentPanel, "Save system config failed\r\nplease check system log for the details", "Save System Config Error", JOptionPane.ERROR_MESSAGE);
								}else{
									JOptionPane.showMessageDialog(contentPanel, "Save system config OK", "Save System Config OK", JOptionPane.INFORMATION_MESSAGE);
								}
								
								LinkedHashMap<String,Object> commonCfg = systemConfig.getData("CommonCfg");
								boolean runningLog = Integer.valueOf((String)commonCfg.get("runningLog"))>0?true:false;
								LogUtils.setEnabledFlag(runningLog);
							}
						}
					}
				});
				buttonPane.add(btnSave);
				getRootPane().setDefaultButton(btnSave);
			}
			{
				JButton btnClose = new JButton("Close");
				btnClose.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if(1 == e.getButton()){
							sysConfig.dispose();
						}
					}
				});
				buttonPane.add(btnClose);
			}
		}
		
		SystemConfig config = SystemConfig.getInstance();
		setTableModel(tableCommonSetting, config.getCommonCfgTitle(), config.getCommonCfgData());
		setTableModel(tableImgPathSetting, config.getImgPathCfgTitle(), ProductSpec.getInstance().getImgProcDirs());
	}
}
