package com.cncmes.utils;

import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListModel;

public class GUIUtils {
	public static void setJListContent(JList<String> list, String[] vals){
		if(null!=list){
			list.setModel(new AbstractListModel<String>() {
				private static final long serialVersionUID = 1L;
				String[] values = vals;
				public int getSize() {
					return values.length;
				}
				public String getElementAt(int index) {
					return values[index];
				}
			});
		}
	}
	
	public static void setJListSelectedIdx(JList<String> list, String val){
		if(null!=list && null!=val && !"".equals(val.trim())){
			ListModel<String> lm = list.getModel();
			int len = lm.getSize();
			boolean bFound = false;
			String items = "";
			if(len > 0){
				for(int i=0; i<len; i++){
					if("".equals(items)){
						items = lm.getElementAt(i);
					}else{
						items += "," + lm.getElementAt(i);
					}
					
					if(val.equals(lm.getElementAt(i))){
						list.setSelectedIndex(i);
						bFound = true;
						break;
					}
				}
			}
			if(!bFound){//Don't remember why doing this
				if("".equals(items)){
					items = val;
				}else{
					items += "," + val;
				}
				String[] vals = items.split(",");
				setJListContent(list,vals);
				list.setSelectedIndex(vals.length-1);
			}
		}
	}
	
	public static void setComboBoxValues(JComboBox<String> comboBox, String[] vals){
		if(null!=comboBox && null!=vals && vals.length>0){
			comboBox.setModel(new DefaultComboBoxModel<String>(vals));
		}else{
			if(null!=comboBox) comboBox.removeAllItems();
		}
		if(null!=comboBox) comboBox.repaint();
	}
	
	public static void setComboBoxSelectedIdx(JComboBox<String> comboBox, String val){
		if(null!=comboBox && null!=val && comboBox.getItemCount()>0){
			for(int i=0; i<comboBox.getItemCount(); i++){
				if(val.equals(comboBox.getItemAt(i))){
					comboBox.setSelectedIndex(i);
					break;
				}
			}
		}
	}
	
	public static void setLabelText(JLabel lblObj,String txt){
		if(null!=lblObj){
			lblObj.setText(txt);
			lblObj.repaint();
		}
	}
}
