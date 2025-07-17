package com.cncmes.gui.listener;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import com.cncmes.dto.CNCLine;
import com.cncmes.gui.model.RackComboBoxModel;
import com.cncmes.gui.panel.InputPanel;
import com.cncmes.gui.panel.StatePanel;

/***
 * 
 * @author W000586 Hui Zhi Fang 2022/4/20
 *
 */
public class JComboBoxListener implements ItemListener {

	@Override
	public void itemStateChanged(ItemEvent e) {
		InputPanel inputPanel = InputPanel.getInstance();
		StatePanel statePanel = StatePanel.getInstance();
		if(e.getStateChange() == ItemEvent.SELECTED){
			// change rack comboBox items when line item is selected

			if (e.getSource() == inputPanel.getComboBoxLineName()) {
	
				CNCLine line = (CNCLine) e.getItem();
				inputPanel.setRack(null);
				inputPanel.setCncLine(line);
				inputPanel.getComboBoxRack().setModel(new RackComboBoxModel(line.getId()));
	
			} else if (e.getSource() == statePanel.getComboBoxLineName()) {
	
				CNCLine line = (CNCLine) e.getItem();
				statePanel.setRack(null);
				statePanel.setCncLine(line);
				statePanel.getComboBoxRack().setModel(new RackComboBoxModel(line.getId()));
			}

		}
	}


}
