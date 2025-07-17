package com.cncmes.gui.frame;

import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.cncmes.gui.panel.MainPanel;

/**
 * 
 * @author W000586 Hui Zhi Fang 2022/3/1
 */
public class RackStation extends JFrame {

	private static RackStation rackStation = new RackStation();
	private static final long serialVersionUID = 4775422583731365727L;
	private JPanel contentPane;
	private String lookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";

	public static RackStation getInstance() {
		return rackStation;
	}
	
	
	/**
	 * Create the frame.
	 */
	private RackStation() {
		try {
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}  
		this.setTitle("CNC Rack Station");
//		this.setSize(1024, 768);
		this.setSize(1000, 700);
//		this.setSize(800, 600);
		this.setIconImage(Toolkit.getDefaultToolkit()
				.getImage(RackStation.class.getResource("/com/cncmes/img/Butterfly_orange_24.png")));
		this.setLocationRelativeTo(null);// center
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		contentPane = MainPanel.getInstance();
		this.setContentPane(contentPane);
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RackStation frame = getInstance();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
