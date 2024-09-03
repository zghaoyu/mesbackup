package com.cncmes.gui.panel;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 
 * @author W000586 Hui Zhi 2022/3/2
 *
 */
public class SettingsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3606430636790642629L;

	private static SettingsPanel settingsPanel = new SettingsPanel();

	/**
	 * Create the panel.
	 */
	private SettingsPanel() {
		this.setBackground(Color.WHITE);

		JLabel label = new JLabel("4");
		label.setFont(new Font("Tahoma", Font.BOLD, 93));
		add(label);
	}

	public static SettingsPanel getInstance() {
		return settingsPanel;
	}

}
