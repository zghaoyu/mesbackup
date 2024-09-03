package com.cncmes.gui.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.cncmes.gui.dialog.SysConfig;
import com.cncmes.gui.dialog.UserLogin;
import com.cncmes.gui.panel.MainPanel;
import com.cncmes.utils.LoginSystem;

/**
 * 
 * @author W000586 Hui Zhi Fang 2022/3/8
 */
public class MenuBarListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		MainPanel mainPanel = MainPanel.getInstance();
		JMenuItem menu = (JMenuItem) e.getSource();

		if (menu == mainPanel.getMntmLogin()) {
			loginSystem(mainPanel);
		} else if (menu == mainPanel.getMntmExit()) {
			exitSystem(mainPanel);

		} else if (menu == mainPanel.getMntmAboutRackStation()) {

		} else if (menu == mainPanel.getMntmConfig()) {
			// if(btnSysCfg.isEnabled() && 1 == e.getButton()){
			SysConfig dialog = SysConfig.getInstance();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);

		}
	}

	private void exitSystem(MainPanel mainPanel) {
		int rtn = JOptionPane.showConfirmDialog(mainPanel, "Are you sure of quiting from CNC Rack Station", "Exit",
				JOptionPane.YES_NO_OPTION);
		if (0 == rtn) {
			System.exit(0);
		}
	}

	private void loginSystem(MainPanel mainPanel) {
		// if(1 == e.getButton() && btnLogin.isEnabled()){
		if (LoginSystem.userHasLoginned()) {
			if (0 == JOptionPane.showConfirmDialog(mainPanel, "Are you sure of logging out now?", "Log Out?",
					JOptionPane.YES_NO_OPTION)) {
				LoginSystem.userLogout();
				mainPanel.refreshButtonsEnabled();
			}
		} else {
			UserLogin dialog = UserLogin.getInstance();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		}
	}
}
