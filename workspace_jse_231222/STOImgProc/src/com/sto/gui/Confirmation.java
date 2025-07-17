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
import javax.swing.border.EmptyBorder;

import com.sto.utils.LoginSystem;
import javax.swing.JPasswordField;

public class Confirmation extends JDialog {
	private static final long serialVersionUID = 7162155710115207460L;
	private final JPanel contentPanel = new JPanel();
	
	private static Confirmation confirmation = new Confirmation();
	private JPasswordField pwdConfirmCode;
	
	public static Confirmation getInstance(){
		return confirmation;
	}
	
	/**
	 * Create the dialog.
	 */
	private Confirmation() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(Confirmation.class.getResource("/com/sto/img/loginMain_24.png")));
		setModal(true);
		setTitle("Confirmation Code");
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 400;
		int height = 100;
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] {104, 270};
		gbl_contentPanel.rowHeights = new int[] {30};
		gbl_contentPanel.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblConfirmCode = new JLabel("Confirm Code");
			lblConfirmCode.setIcon(new ImageIcon(Confirmation.class.getResource("/com/sto/img/keys_24.png")));
			GridBagConstraints gbc_lblConfirmCode = new GridBagConstraints();
			gbc_lblConfirmCode.fill = GridBagConstraints.BOTH;
			gbc_lblConfirmCode.insets = new Insets(0, 0, 5, 5);
			gbc_lblConfirmCode.gridx = 0;
			gbc_lblConfirmCode.gridy = 0;
			contentPanel.add(lblConfirmCode, gbc_lblConfirmCode);
		}
		{
			pwdConfirmCode = new JPasswordField();
			GridBagConstraints gbc_pwdConfirmCode = new GridBagConstraints();
			gbc_pwdConfirmCode.insets = new Insets(0, 0, 5, 0);
			gbc_pwdConfirmCode.fill = GridBagConstraints.BOTH;
			gbc_pwdConfirmCode.gridx = 1;
			gbc_pwdConfirmCode.gridy = 0;
			contentPanel.add(pwdConfirmCode, gbc_pwdConfirmCode);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton btnConfirm = new JButton("Confirm");
				btnConfirm.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if(btnConfirm.isEnabled() && 1==e.getButton()){
							btnConfirm.setEnabled(false);
							String code = String.valueOf(pwdConfirmCode.getPassword());
							String msg = LoginSystem.userConfirm(code);
							if("OK".equals(msg)){
								pwdConfirmCode.setText("");
								confirmation.dispose();
							}else{
								JOptionPane.showMessageDialog(contentPanel, msg, "Confirmation Code Error", JOptionPane.ERROR_MESSAGE);
							}
							btnConfirm.setEnabled(true);
						}
					}
				});
				buttonPane.add(btnConfirm);
				getRootPane().setDefaultButton(btnConfirm);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent arg0) {
						if (1 == arg0.getButton()){
							pwdConfirmCode.setText("");
							confirmation.dispose();
						}
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
}