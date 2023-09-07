package com.cncmes.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.cncmes.utils.LoginSystem;

import java.awt.Toolkit;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.ImageIcon;

public class UserLogin extends JDialog {
	private final JPanel contentPanel = new JPanel();
	private static UserLogin userLogin = new UserLogin();
	private static final long serialVersionUID = 3L;
	private JTextField textField_userName;
	private JPasswordField passwordField_pwd;
	
	public static UserLogin getInstance(){
		return userLogin;
	}
	
	/**
	 * Create the dialog.
	 */
	private UserLogin() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(UserLogin.class.getResource("/com/cncmes/img/loginMain_24.png")));
		setModal(true);
		setTitle("User Login");
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 400;
		int height = 140;
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] {87, 287, 0};
		gbl_contentPanel.rowHeights = new int[]{28, 28, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblUserName = new JLabel("User Name");
			lblUserName.setIcon(new ImageIcon(UserLogin.class.getResource("/com/cncmes/img/user_24.png")));
			GridBagConstraints gbc_lblUserName = new GridBagConstraints();
			gbc_lblUserName.fill = GridBagConstraints.BOTH;
			gbc_lblUserName.insets = new Insets(0, 0, 5, 5);
			gbc_lblUserName.gridx = 0;
			gbc_lblUserName.gridy = 0;
			contentPanel.add(lblUserName, gbc_lblUserName);
		}
		{
			textField_userName = new JTextField();
			GridBagConstraints gbc_textField_userName = new GridBagConstraints();
			gbc_textField_userName.fill = GridBagConstraints.BOTH;
			gbc_textField_userName.insets = new Insets(0, 0, 5, 0);
			gbc_textField_userName.gridx = 1;
			gbc_textField_userName.gridy = 0;
			contentPanel.add(textField_userName, gbc_textField_userName);
			textField_userName.setColumns(20);
		}
		{
			JLabel lblPassword = new JLabel("Password");
			lblPassword.setIcon(new ImageIcon(UserLogin.class.getResource("/com/cncmes/img/keys_24.png")));
			GridBagConstraints gbc_lblPassword = new GridBagConstraints();
			gbc_lblPassword.fill = GridBagConstraints.BOTH;
			gbc_lblPassword.insets = new Insets(0, 0, 0, 5);
			gbc_lblPassword.gridx = 0;
			gbc_lblPassword.gridy = 1;
			contentPanel.add(lblPassword, gbc_lblPassword);
		}
		{
			passwordField_pwd = new JPasswordField();
			GridBagConstraints gbc_passwordField_pwd = new GridBagConstraints();
			gbc_passwordField_pwd.fill = GridBagConstraints.BOTH;
			gbc_passwordField_pwd.gridx = 1;
			gbc_passwordField_pwd.gridy = 1;
			contentPanel.add(passwordField_pwd, gbc_passwordField_pwd);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton loginButton = new JButton("Login");
				loginButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if(loginButton.isEnabled() && 1==e.getButton()){
							loginButton.setEnabled(false);
							String name = textField_userName.getText().trim();
							String pwd = String.valueOf(passwordField_pwd.getPassword());
							String msg = LoginSystem.userLogin(name, pwd);
							if("OK".equals(msg)){
								textField_userName.setText("");
								passwordField_pwd.setText("");
								userLogin.dispose();
							}else{
								JOptionPane.showMessageDialog(contentPanel, msg, "Login Failed", JOptionPane.ERROR_MESSAGE);
							}
							loginButton.setEnabled(true);
							Scheduler.getInstance().refreshButtonsEnabled();
						}
					}
				});
				loginButton.setActionCommand("Login");
				buttonPane.add(loginButton);
				getRootPane().setDefaultButton(loginButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent arg0) {
						if (1 == arg0.getButton()){
							userLogin.dispose();
						}
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
}
