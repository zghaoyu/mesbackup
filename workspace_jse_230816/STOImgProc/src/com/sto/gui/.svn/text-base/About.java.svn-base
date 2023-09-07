package com.sto.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.sto.utils.DesUtils;
import com.sto.utils.PCInfoUtils;
import com.sto.utils.XmlUtils;

import java.awt.Toolkit;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;

public class About extends JDialog {
	private static final long serialVersionUID = -8496935028635437684L;
	private static About about = new About();
	private final JPanel contentPanel = new JPanel();
	private JLabel lblRegistration;
	
	public static About getInstance(){
		return about;
	}
	
	private void showRegistration(){
		if(PCInfoUtils.licenceOK()){
			try {
				LinkedHashMap<String,String> lic = new LinkedHashMap<String,String>();
				lic = XmlUtils.parseLicFile();
				lblRegistration.setText(" Registration to "+lic.get("REG")+" (Expiry: "+DesUtils.decrypt(lic.get("MCD"), DesUtils.decrypt(lic.get("MCK")))+")");
			} catch (Exception e) {
				lblRegistration.setText(e.getMessage());
			}
		}else{
			lblRegistration.setText(" No Registration");
		}
	}
	
	/**
	 * Create the dialog.
	 */
	private About() {
		setModal(true);
		setIconImage(Toolkit.getDefaultToolkit().getImage(About.class.getResource("/com/sto/img/Company_36.png")));
		setTitle("About STO X-Ray Image Processor");
		
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 600;
		int height = 300;
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] {180, 320};
		gbl_contentPanel.rowHeights = new int[] {0};
		gbl_contentPanel.columnWeights = new double[]{1.0, 1.0};
		gbl_contentPanel.rowWeights = new double[]{1.0};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JPanel panel = new JPanel();
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.insets = new Insets(0, 0, 0, 5);
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.gridx = 0;
			gbc_panel.gridy = 0;
			contentPanel.add(panel, gbc_panel);
			panel.setLayout(new GridLayout(1, 0, 0, 0));
			{
				JLabel lblLogo = new JLabel("");
				lblLogo.setIcon(new ImageIcon(About.class.getResource("/com/sto/img/Company_64.png")));
				panel.add(lblLogo);
			}
		}
		{
			JPanel panel = new JPanel();
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.gridx = 1;
			gbc_panel.gridy = 0;
			contentPanel.add(panel, gbc_panel);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JLabel lblTitle = new JLabel(" X-Ray Image Processor");
				panel.add(lblTitle, BorderLayout.NORTH);
			}
			{
				JScrollPane scrollPane = new JScrollPane();
				panel.add(scrollPane, BorderLayout.CENTER);
				{
					JTextArea txtrAbout = new JTextArea();
					txtrAbout.setLineWrap(true);
					txtrAbout.setFont(new Font("Arial", Font.PLAIN, 12));
					txtrAbout.setEditable(false);
					txtrAbout.setText(" Version: Alfa (For Verification in ATL)\r\n Build id: 20181026-0001\r\n Powered by: STO Technology LTD\r\n Copyright \u00A9 2017 STO. All Rights Reserved.\r\n STO and the STO logo are trademarks of STO \r\n Technology LTD. The STO logo cannot be altered \r\n without STO's permission.");
					scrollPane.setViewportView(txtrAbout);
				}
			}
			{
				lblRegistration = new JLabel(" Registration");
				panel.add(lblRegistration, BorderLayout.SOUTH);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent arg0) {
						if(1==arg0.getButton() && okButton.isEnabled()) about.dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
		
		showRegistration();
	}
}
