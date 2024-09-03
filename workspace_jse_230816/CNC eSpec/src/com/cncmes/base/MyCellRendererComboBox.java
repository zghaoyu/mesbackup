package com.cncmes.base;

import java.awt.Component;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings({ "rawtypes" })
public class MyCellRendererComboBox extends JComboBox implements TableCellRenderer{
	private static final long serialVersionUID = 7L;
	private String[] vals = null;
	
	@SuppressWarnings("unchecked")
	public MyCellRendererComboBox(String[] initVals){
		super();
		vals = new String[initVals.length];
		vals = initVals;
		this.setModel(new DefaultComboBoxModel(vals));
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		if(isSelected){
			setForeground(table.getForeground());
			super.setBackground(table.getBackground());
		}else{
			setForeground(table.getForeground());
			setBackground(table.getBackground());
		}
		
		for(int i=0; i<vals.length; i++){
			if(vals[i].equals(""+value)){
				this.setSelectedIndex(i);
			}
		}
		
		return this;
	}
}