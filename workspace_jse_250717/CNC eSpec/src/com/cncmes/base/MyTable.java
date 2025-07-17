package com.cncmes.base;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.cncmes.dao.DAO;
import com.cncmes.dao.impl.DAOImpl;
import com.cncmes.utils.DTOUtils;

public class MyTable extends JTable {
	private static final long serialVersionUID = -4078439961470533331L;
	private String[] renderFields = null;
	
	public TableCellRenderer getCellRenderer(int row, int column){
		TableCellRenderer renderer = super.getCellRenderer(row, column);
		
		String[] vals = getRendererVals(row, column);
		if(null!=vals) renderer = new MyCellRendererComboBox(vals);
		
		return renderer;
	}
	
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column){
		return super.prepareRenderer(renderer, row, column);
	}
	
	public TableCellEditor getCellEditor(int row, int column){
		TableCellEditor editor = super.getCellEditor(row, column);
		
		String[] vals = getRendererVals(row, column);
		if(null!=vals) editor = new MyCellEditorComboBox(vals);
		
		return editor;
	}
	
	public Component prepareEditor(TableCellEditor editor, int row, int column){
		return super.prepareEditor(editor, row, column);
	}
	
	public void setRenderFields(String[] chkFields){
		String[] fields = chkFields;
		if(null==fields){
			DAO dao = new DAOImpl(getDtoClassName());
			fields = dao.getDataFields();//id is the first element
		}
		renderFields = fields;
	}
	
	private String getDtoClassName(){
		MyTableModel myTm = (MyTableModel) getModel();
		return myTm.getDtoClassName();
	}
	
	private String[] getRendererVals(int row, int column){
		String[][] renderCfg = null;
		String[] vals = null;
		MyTableModel myTm = (MyTableModel) getModel();
		renderCfg = DTOUtils.getRenderConfig(getDtoClassName());
		if(null!=renderCfg && null!=renderFields && renderFields.length>0){
			if(myTm.isRowHeaderMode()){
				if(renderFields.length>row){
					vals = DTOUtils.getFieldRendererInitVals(renderCfg,renderFields[row]);
				}
			}else{
				if(renderFields.length>column){
					vals = DTOUtils.getFieldRendererInitVals(renderCfg,renderFields[column]);
				}
			}
		}
		
		return vals;
	}
}
