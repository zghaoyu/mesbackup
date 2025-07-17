package com.cncmes.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import com.cncmes.data.SystemConfig;
import com.cncmes.utils.XmlUtils;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.GridBagConstraints;
import javax.swing.JScrollPane;
import java.awt.Insets;
import javax.swing.JTable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.BevelBorder;

public class SysConfig extends JDialog {
	private static final long serialVersionUID = 19L;
	private final JPanel contentPanel = new JPanel();
	private static SysConfig sysConfig = new SysConfig();
	private JTable tableCommonSetting;
	private JTable tableDatabaseSetting;
	private JTable tableFtpSetting;
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
	}
	
	/**
	 * Create the dialog.
	 */
	private SysConfig() {
		XmlUtils.parseSystemConfig();
		setIconImage(Toolkit.getDefaultToolkit().getImage(SysConfig.class.getResource("/com/cncmes/img/setting_24.png")));
		setModal(true);
		setTitle("System Config");
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 450;
		int height = 490;
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0};
		gbl_contentPanel.rowHeights = new int[] {30, 65, 30, 130, 30, 125};
		gbl_contentPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0, 1.0};
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
			JLabel lblDatabaseSetting = new JLabel("Database Setting");
			GridBagConstraints gbc_lblDatabaseSetting = new GridBagConstraints();
			gbc_lblDatabaseSetting.insets = new Insets(0, 0, 5, 0);
			gbc_lblDatabaseSetting.gridx = 0;
			gbc_lblDatabaseSetting.gridy = 2;
			contentPanel.add(lblDatabaseSetting, gbc_lblDatabaseSetting);
		}
		{
			JScrollPane scrollPaneDatabaseSetting = new JScrollPane();
			GridBagConstraints gbc_scrollPaneDatabaseSetting = new GridBagConstraints();
			gbc_scrollPaneDatabaseSetting.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPaneDatabaseSetting.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneDatabaseSetting.gridx = 0;
			gbc_scrollPaneDatabaseSetting.gridy = 3;
			contentPanel.add(scrollPaneDatabaseSetting, gbc_scrollPaneDatabaseSetting);
			{
				tableDatabaseSetting = new JTable();
				scrollPaneDatabaseSetting.setViewportView(tableDatabaseSetting);
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
							
							rowCnt = tableDatabaseSetting.getRowCount();
							for(int i=0; i<rowCnt; i++){
								String paraName = (null!=tableDatabaseSetting.getValueAt(i, 0))?String.valueOf(tableDatabaseSetting.getValueAt(i, 0)).trim():"";
								String paraVal = (null!=tableDatabaseSetting.getValueAt(i, 1))?String.valueOf(tableDatabaseSetting.getValueAt(i, 1)).trim():"";
								
								if(!"".equals(paraName) && !"".equals(paraVal)){
									if("NULL".equals(paraVal.toUpperCase())) paraVal = "";
									systemConfig.setData("DatabaseCfg", paraName, paraVal);
									needToSave = true;
								}
							}
							
							rowCnt = tableFtpSetting.getRowCount();
							for(int i=0; i<rowCnt; i++){
								String paraName = (null!=tableFtpSetting.getValueAt(i, 0))?String.valueOf(tableFtpSetting.getValueAt(i, 0)).trim():"";
								String paraVal = (null!=tableFtpSetting.getValueAt(i, 1))?String.valueOf(tableFtpSetting.getValueAt(i, 1)).trim():"";
								
								if(!"".equals(paraName) && !"".equals(paraVal)){
									if("NULL".equals(paraVal.toUpperCase())) paraVal = "";
									systemConfig.setData("FtpCfg", paraName, paraVal);
									needToSave = true;
								}
							}
							
							if(needToSave){
								if(!XmlUtils.saveSystemConfig(systemConfig.getDataMap())){
									JOptionPane.showMessageDialog(contentPanel, "Save system config failed\r\nplease check system log for the details", "Save System Config Error", JOptionPane.ERROR_MESSAGE);
								}else{
									JOptionPane.showMessageDialog(contentPanel, "Save system config OK", "Save System Config OK", JOptionPane.INFORMATION_MESSAGE);
								}
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
		
		{
			JLabel lblFtpSetting = new JLabel("FTP Setting");
			GridBagConstraints gbc_lblFtpSetting = new GridBagConstraints();
			gbc_lblFtpSetting.insets = new Insets(0, 0, 5, 0);
			gbc_lblFtpSetting.gridx = 0;
			gbc_lblFtpSetting.gridy = 4;
			contentPanel.add(lblFtpSetting, gbc_lblFtpSetting);
		}
		{
			JScrollPane scrollPaneFtpSetting = new JScrollPane();
			GridBagConstraints gbc_scrollPaneFtpSetting = new GridBagConstraints();
			gbc_scrollPaneFtpSetting.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneFtpSetting.gridx = 0;
			gbc_scrollPaneFtpSetting.gridy = 5;
			contentPanel.add(scrollPaneFtpSetting, gbc_scrollPaneFtpSetting);
			{
				tableFtpSetting = new JTable();
				scrollPaneFtpSetting.setViewportView(tableFtpSetting);
			}
		}
		
		SystemConfig config = SystemConfig.getInstance();
		setTableModel(tableCommonSetting, config.getCommonCfgTitle(), config.getCommonCfgData());
		setTableModel(tableDatabaseSetting, config.getDatabaseCfgTitle(), config.getDatabaseCfgData());
		setTableModel(tableFtpSetting, config.getFtpCfgTitle(), config.getFtpCfgData());
	}
}
