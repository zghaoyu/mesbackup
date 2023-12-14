package com.cncmes.base;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;

@SuppressWarnings("rawtypes")
public class MyCellEditorComboBox extends JComboBox implements TableCellEditor{
	private static final long serialVersionUID = 8L;
	private EventListenerList listenerList = new EventListenerList();
	private ChangeEvent changeEvent = new ChangeEvent(this);
	private String[] vals = null;
	
	@SuppressWarnings({ "unchecked" })
	public MyCellEditorComboBox(String[] initVals){
		super();
		vals = new String[initVals.length];
		vals = initVals;
		this.setModel(new DefaultComboBoxModel(vals));
	}
	
	@Override
	public Object getCellEditorValue() {
		return this.getSelectedItem().toString();
	}
	@Override
	public boolean isCellEditable(EventObject anEvent) {
		return true;
	}
	@Override
	public boolean shouldSelectCell(EventObject anEvent) {
		return true;
	}
	@Override
	public boolean stopCellEditing() {
		CellEditorListener listener;
		Object[] listeners = listenerList.getListenerList();
		for(int i=0; i<listeners.length; i++){
			if(listeners[i] == CellEditorListener.class){
				listener = (CellEditorListener)listeners[i+1];
				listener.editingStopped(changeEvent); //Inform JTable object to update the cell value with setValueAt() method
			}
		}
		
		return true;
	}
	@Override
	public void cancelCellEditing() {
	}
	@Override
	public void addCellEditorListener(CellEditorListener l) {
		listenerList.add(CellEditorListener.class, l);
	}
	@Override
	public void removeCellEditorListener(CellEditorListener l) {
		listenerList.remove(CellEditorListener.class, l);
	}
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		for(int i=0; i<vals.length; i++){
			if(vals[i].equals((String)value)){
				this.setSelectedIndex(i);
			}
		}
		
		return this;
	}
}
