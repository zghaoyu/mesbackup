package com.cncmes.gui.model;

import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import com.cncmes.dto.CNCLine;
import com.cncmes.utils.DataUtils;

/**
 * 生产线的下拉框模型
 * @author W000586 Hui Zhi Fang 2022/4/20
 *
 */
public class LineComboBoxModel implements ComboBoxModel<CNCLine> {

	private List<CNCLine> lines = DataUtils.getAllLine();
	private CNCLine line = null;

	public LineComboBoxModel() {

	}

	@Override
	public int getSize() {
		return lines.size();
	}

	@Override
	public CNCLine getElementAt(int index) {
		return lines.get(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {

	}

	@Override
	public void removeListDataListener(ListDataListener l) {

	}

	@Override
	public void setSelectedItem(Object anItem) {
		line = (CNCLine) anItem;
	}

	@Override
	public Object getSelectedItem() {
		if (!lines.isEmpty()) {
			return line;
		}
		return null;
	}

}
