package com.cncmes.gui.listener;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

import com.cncmes.gui.panel.CenterPanel;
import com.cncmes.gui.panel.InputPanel;
import com.cncmes.gui.panel.MainPanel;
import com.cncmes.gui.panel.OutputPanel;
import com.cncmes.gui.panel.StatePanel;
import com.cncmes.gui.panel.SettingsPanel;
import com.cncmes.utils.GUIUtils;

/**
 * 
 * @author W000586 Hui Zhi Fang 2022/3/2
 */
public class TitleBarListener implements MouseListener {

	@Override
	public void mouseClicked(MouseEvent e) {
		MainPanel mainPanel = MainPanel.getInstance();
		CenterPanel centerPanel = CenterPanel.getInstance();

		JPanel panel = (JPanel) e.getSource();
		if (panel == mainPanel.getBtn_rack()) {
			centerPanel.changePanel(StatePanel.getInstance());
			GUIUtils.setColor(mainPanel.getBtn_input(), GUIUtils.GRAY2);
			GUIUtils.setColor(mainPanel.getBtn_rack(), GUIUtils.GRAY1);
			GUIUtils.setColor(mainPanel.getBtn_output(), GUIUtils.GRAY2);
			GUIUtils.setColor(mainPanel.getBtn_settings(), GUIUtils.GRAY2);

		}

		else if (panel == mainPanel.getBtn_input()) {
			centerPanel.changePanel(InputPanel.getInstance());
			GUIUtils.setColor(mainPanel.getBtn_input(), GUIUtils.GRAY1);
			GUIUtils.setColor(mainPanel.getBtn_rack(), GUIUtils.GRAY2);
			GUIUtils.setColor(mainPanel.getBtn_output(), GUIUtils.GRAY2);
			GUIUtils.setColor(mainPanel.getBtn_settings(), GUIUtils.GRAY2);
		}

		else if (panel == mainPanel.getBtn_output()) {
			centerPanel.changePanel(OutputPanel.getInstance());
			GUIUtils.setColor(mainPanel.getBtn_input(), GUIUtils.GRAY2);
			GUIUtils.setColor(mainPanel.getBtn_rack(), GUIUtils.GRAY2);
			GUIUtils.setColor(mainPanel.getBtn_output(), GUIUtils.GRAY1);
			GUIUtils.setColor(mainPanel.getBtn_settings(), GUIUtils.GRAY2);

		}

		else if (panel == mainPanel.getBtn_settings()) {
			centerPanel.changePanel(SettingsPanel.getInstance());
			GUIUtils.setColor(mainPanel.getBtn_input(), GUIUtils.GRAY2);
			GUIUtils.setColor(mainPanel.getBtn_rack(), GUIUtils.GRAY2);
			GUIUtils.setColor(mainPanel.getBtn_output(), GUIUtils.GRAY2);
			GUIUtils.setColor(mainPanel.getBtn_settings(), GUIUtils.GRAY1);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

}
