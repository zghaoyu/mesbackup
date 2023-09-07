package com.cncmes.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Toolkit;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JPasswordField;
import javax.swing.ImageIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PwdConfirmation extends JDialog {
	private static final long serialVersionUID = 30L;
	private static PwdConfirmation pwdConfirm = new PwdConfirmation();
	private static String myPassword = "";
	private final JPanel contentPanel = new JPanel();
	private JPasswordField pwdPassword;
	
	public static PwdConfirmation getInstance(){
		return pwdConfirm;
	}
	
	public static void setPassword(String pwd){
		myPassword = pwd;
	}
	
	public static String getPassword(){
		return myPassword;
	}
	
	/**
	 * Create the dialog.
	 */
	private PwdConfirmation() {
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(PwdConfirmation.class.getResource("/com/cncmes/img/loginMain_24.png")));
		setTitle("Operation Confirmation");
		setModal(true);
		
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 300;
		int height = 100;
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblPassword = new JLabel("Password");
			lblPassword.setIcon(new ImageIcon(PwdConfirmation.class.getResource("/com/cncmes/img/keys_16.png")));
			GridBagConstraints gbc_lblPassword = new GridBagConstraints();
			gbc_lblPassword.insets = new Insets(0, 0, 0, 5);
			gbc_lblPassword.anchor = GridBagConstraints.EAST;
			gbc_lblPassword.gridx = 0;
			gbc_lblPassword.gridy = 0;
			contentPanel.add(lblPassword, gbc_lblPassword);
		}
		{
			pwdPassword = new JPasswordField();
			GridBagConstraints gbc_pwdPassword = new GridBagConstraints();
			gbc_pwdPassword.fill = GridBagConstraints.HORIZONTAL;
			gbc_pwdPassword.gridx = 1;
			gbc_pwdPassword.gridy = 0;
			contentPanel.add(pwdPassword, gbc_pwdPassword);
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
						if(1 == arg0.getButton()){
							setPassword(new String(pwdPassword.getPassword()));
							pwdConfirm.dispose();
						}
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent arg0) {
						if(1 == arg0.getButton()){
							setPassword("");
							pwdConfirm.dispose();
						}
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

}
