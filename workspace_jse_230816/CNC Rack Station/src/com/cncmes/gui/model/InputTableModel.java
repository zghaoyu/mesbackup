package com.cncmes.gui.model;

import javax.swing.table.AbstractTableModel;
/**
 * 夹具与材料条码绑定界面的表格模型
 * @author W000586 Hui Zhi Fang
 *
 */
public class InputTableModel extends AbstractTableModel {
	private Object[][] tableData;
	private String[] tableTitle;
	private int rowCount = 0;
	private int colCount = 0;
	private static final long serialVersionUID = 1L;

	public InputTableModel(Object[][] tableData, String[] tableTitle) {
		super();
		this.tableData = tableData;
		this.tableTitle = tableTitle;
		colCount = tableTitle.length;
		if (null != tableData)
			rowCount = tableData.length;
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
		return tableData[rowIndex][columnIndex];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public String getColumnName(int column) {
		return tableTitle[column];
	}

}
