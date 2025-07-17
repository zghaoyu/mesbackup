package com.cncmes.gui.panel;

//import javax.swing.JPanel;
import java.awt.BorderLayout;

import javax.swing.JPanel;

/**
 * 
 * @author W000586 Hui Zhi Fang
 * 2022/3/2
 */
public class CenterPanel extends JPanel {

	private static final long serialVersionUID = -8201941445691980580L;

	private static CenterPanel centerPanel = new CenterPanel();

//	private static double rate = 0.8;
//	private JPanel panel = null;

	/**
	 * Create the panel.
	 */
	private CenterPanel() {
		setLayout(new BorderLayout(0, 0));
	}

	public static CenterPanel getInstance() {
		return centerPanel;
	}

	public void changePanel(JPanel panel) {
//		this.panel = panel;
		this.removeAll();
		this.add(panel);
		this.updateUI();
	}

//	@Override
//	public void repaint() {
//		if (null != panel) {
//			Dimension containerSize = this.getSize();
//			panel.setSize((int) (containerSize.width * rate), (int) (containerSize.height * rate));
//			panel.setLocation(containerSize.width / 2 - this.panel.getSize().width / 2,
//					containerSize.height / 2 - this.panel.getSize().height / 2);
//			super.repaint();
//		}
//	}

}
