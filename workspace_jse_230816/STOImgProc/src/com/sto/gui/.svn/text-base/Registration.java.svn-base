package com.sto.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.sto.utils.DesUtils;
import com.sto.utils.LoginSystem;
import com.sto.utils.TimeUtils;

public class Registration extends JDialog {
	private static final long serialVersionUID = 443451971436692094L;
	private final JPanel contentPanel = new JPanel();
	
	private static Registration registration = new Registration();
	private JTextField textField_userName;
	private JPasswordField passwordField_pwd;
	
	public static Registration getInstance(){
		return registration;
	}
	
	/**
	 * Create the dialog.
	 */
	private Registration() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(Registration.class.getResource("/com/sto/img/loginMain_24.png")));
		setModal(true);
		setTitle("Registration");
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
			lblUserName.setIcon(new ImageIcon(Registration.class.getResource("/com/sto/img/user_24.png")));
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
			lblPassword.setIcon(new ImageIcon(Registration.class.getResource("/com/sto/img/keys_24.png")));
			GridBagConstraints gbc_lblPassword = new GridBagConstraints();
			gbc_lblPassword.fill = GridBagConstraints.BOTH;
			gbc_lblPassword.insets = new Insets(0, 0, 0, 5);
			gbc_lblPassword.gridx = 0;
			gbc_lblPassword.gridy = 1;
			contentPanel.add(lblPassword, gbc_lblPassword);
		}
		{
			passwordField_pwd = new JPasswordField();
			passwordField_pwd.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					if(2==arg0.getClickCount() && passwordField_pwd.isEditable()){
						if(!LoginSystem.userHasLoginned()){
							showConfirmationDialog();
							if(LoginSystem.userHasConfirmationCode()){
								try {
									passwordField_pwd.setText(DesUtils.encrypt("ATL"+TimeUtils.getCurrentDate("yyyyMMdd")));
								} catch (Exception e) {
								}
							}
						}
					}
				}
			});
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
				JButton btnRegister = new JButton("Register");
				btnRegister.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if(btnRegister.isEnabled() && 1==e.getButton()){
							btnRegister.setEnabled(false);
							String name = textField_userName.getText().trim();
							String pwd = String.valueOf(passwordField_pwd.getPassword());
							String msg = LoginSystem.userLogin(name, pwd);
							if("OK".equals(msg)){
								textField_userName.setText("");
								passwordField_pwd.setText("");
								registration.dispose();
							}else{
								JOptionPane.showMessageDialog(contentPanel, msg, "Registration Failed", JOptionPane.ERROR_MESSAGE);
							}
							btnRegister.setEnabled(true);
						}
					}
				});
				btnRegister.setActionCommand("Login");
				buttonPane.add(btnRegister);
				getRootPane().setDefaultButton(btnRegister);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent arg0) {
						if (1 == arg0.getButton()){
							registration.dispose();
						}
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	protected void showConfirmationDialog(){
		try {
			Confirmation dialog = Confirmation.getInstance();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
