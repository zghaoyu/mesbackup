package com.cncmes.base;

import javax.swing.table.AbstractTableModel;

public class RowHeaderTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 51L;
	private static String[] title = null;
	
	public RowHeaderTableModel(String[] titles){
		if(null!=titles && titles.length>0){
			title = titles;
		}else{
			title = new String[]{"title"};
		}
	}
	
	@Override
	public int getRowCount() {
		return title.length;
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return title[rowIndex];
	}

}
