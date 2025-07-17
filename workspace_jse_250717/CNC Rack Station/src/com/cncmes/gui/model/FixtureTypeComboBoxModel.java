package com.cncmes.gui.model;

import java.util.ArrayList;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import com.cncmes.dto.FixtureType;
import com.cncmes.utils.DataUtils;
/**
 * 夹具类型的下拉框模型
 * @author W000586 Hui Zhi Fang
 *
 */
public class FixtureTypeComboBoxModel implements ComboBoxModel<FixtureType> {
	ArrayList<FixtureType> fixtureTypes = DataUtils.getFixtureType();
	FixtureType fixtureType = null;

	public FixtureTypeComboBoxModel() {

	}

	@Override
	public int getSize() {
		return fixtureTypes.size();
	}

	@Override
	public FixtureType getElementAt(int index) {
		return fixtureTypes.get(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {

	}

	@Override
	public void removeListDataListener(ListDataListener l) {

	}

	@Override
	public void setSelectedItem(Object anItem) {
		fixtureType = (FixtureType) anItem;
	}

	@Override
	public Object getSelectedItem() {
		if (!fixtureTypes.isEmpty()) {
			return fixtureType;
		} else {
			return null;
		}
	}

}
