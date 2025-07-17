package com.cncmes.gui.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import com.cncmes.dto.Rack;
import com.cncmes.utils.DataUtils;

/**
 * 
 * @author W000586 Hui Zhi Fang 2022/4/20
 *
 */
public class RackComboBoxModel implements ComboBoxModel<Rack> {

	private List<Rack> racks = new ArrayList<Rack>();

	private Rack rack = null;

	// need to modify
	public RackComboBoxModel(int lineId) {
		if (lineId > 0) {
			racks = DataUtils.getRackByLineId(lineId);
		}
	}

	@Override
	public int getSize() {
		return racks.size();
	}

	@Override
	public Rack getElementAt(int index) {
		return racks.get(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {

	}

	@Override
	public void removeListDataListener(ListDataListener l) {

	}

	@Override
	public void setSelectedItem(Object anItem) {
		rack = (Rack) anItem;
	}

	@Override
	public Object getSelectedItem() {
		if (!racks.isEmpty()) {
			return rack;
		}
		return null;
	}

}
