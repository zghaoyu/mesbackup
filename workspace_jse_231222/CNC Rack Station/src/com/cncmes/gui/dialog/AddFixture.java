package com.cncmes.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.cncmes.dto.Fixture;
import com.cncmes.dto.FixtureType;
import com.cncmes.gui.model.FixtureTypeComboBoxModel;
import com.cncmes.utils.DataUtils;
import com.cncmes.utils.GUIUtils;
import com.cncmes.utils.LoginSystem;
import com.cncmes.utils.NetUtils;
import com.cncmes.utils.TimeUtils;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JComboBox;
/**
 * 
 * @author W000586 Hui Zhi Fang
 *
 */
public class AddFixture extends JDialog {

	private static final long serialVersionUID = 6652053733657315011L;
	private final JPanel contentPanel = new JPanel();
	private static AddFixture addFixture = new AddFixture();
	private JTextField tfFixtureNo;
	private JLabel lblFixtureNo;
	private JLabel lblFixtureType;
	private JComboBox<FixtureType> comboBox;
	private JPanel buttonPane;
	private JButton saveButton;
	private JButton cancelButton;
	private JLabel lblMessage;
	private JLabel label;
	private JLabel label_1;
	public static String fixtureNo;
	public static AddFixture getInstance() {
		return addFixture;
	}

	/**
	 * Create the dialog.
	 */
	private AddFixture() {
		setResizable(false);
		setTitle("Add Fixture");
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 400;
		int height = 250;
		setBounds((d.width - width) / 2, (d.height - height) / 2, 400, 200);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setForeground(Color.RED);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 10, 150, 180, 0, 10, 0 };
		gbl_contentPanel.rowHeights = new int[] { 50, 50, 20, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);
		{
			lblFixtureNo = new JLabel("Fixture No :");
			GridBagConstraints gbc_lblFixtureNo = new GridBagConstraints();
			gbc_lblFixtureNo.insets = new Insets(0, 0, 5, 5);
			gbc_lblFixtureNo.gridx = 1;
			gbc_lblFixtureNo.gridy = 0;
			contentPanel.add(lblFixtureNo, gbc_lblFixtureNo);
		}
		{
			tfFixtureNo = new JTextField(fixtureNo);
			GridBagConstraints gbc_textField = new GridBagConstraints();
			gbc_textField.fill = GridBagConstraints.HORIZONTAL;
			gbc_textField.insets = new Insets(0, 0, 5, 5);
			gbc_textField.gridx = 2;
			gbc_textField.gridy = 0;
			contentPanel.add(tfFixtureNo, gbc_textField);
			tfFixtureNo.setColumns(10);

		}
		{
			label = new JLabel("*");
			label.setForeground(Color.RED);
			GridBagConstraints gbc_label = new GridBagConstraints();
			gbc_label.insets = new Insets(0, 0, 5, 5);
			gbc_label.gridx = 3;
			gbc_label.gridy = 0;
			contentPanel.add(label, gbc_label);
		}
		{
			lblFixtureType = new JLabel("Fixture Type :");
			GridBagConstraints gbc_lblFixtureType = new GridBagConstraints();
			gbc_lblFixtureType.insets = new Insets(0, 0, 5, 5);
			gbc_lblFixtureType.gridx = 1;
			gbc_lblFixtureType.gridy = 1;
			contentPanel.add(lblFixtureType, gbc_lblFixtureType);
		}
		{
			comboBox = new JComboBox<>();
			// query fixture type
			comboBox.setModel(new FixtureTypeComboBoxModel());
			GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.insets = new Insets(0, 0, 5, 5);
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 2;
			gbc_comboBox.gridy = 1;
			contentPanel.add(comboBox, gbc_comboBox);
		}
		{
			label_1 = new JLabel("*");
			label_1.setForeground(Color.RED);
			GridBagConstraints gbc_label_1 = new GridBagConstraints();
			gbc_label_1.insets = new Insets(0, 0, 5, 5);
			gbc_label_1.gridx = 3;
			gbc_label_1.gridy = 1;
			contentPanel.add(label_1, gbc_label_1);
		}
		{
			lblMessage = new JLabel("");
			lblMessage.setForeground(Color.RED);
			GridBagConstraints gbc_lblMessage = new GridBagConstraints();
			gbc_lblMessage.fill = GridBagConstraints.VERTICAL;
			gbc_lblMessage.gridwidth = 2;
			gbc_lblMessage.insets = new Insets(0, 0, 0, 5);
			gbc_lblMessage.gridx = 1;
			gbc_lblMessage.gridy = 2;
			contentPanel.add(lblMessage, gbc_lblMessage);
		}
		{
			buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				saveButton = new JButton("Save");
				saveButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String fixtureNo = tfFixtureNo.getText().trim();
						FixtureType type = (FixtureType) comboBox.getModel().getSelectedItem();
						if (!GUIUtils.checkEmpty(tfFixtureNo, "FixtureNo", lblMessage)) {
							if (null != type) {
								Fixture fixture = new Fixture();
								fixture.setFixture_no(fixtureNo);
								fixture.setUser_id(LoginSystem.getUserId());
								fixture.setIp_address(NetUtils.getLocalIP());
								fixture.setPc_name(NetUtils.getLocalHostName());
								fixture.setCreate_time(TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
								fixture.setIs_deleted(0);
								fixture.setType_id(type.getId());
								
								if (DataUtils.saveFixture(fixture)) {
									JOptionPane.showMessageDialog(addFixture, "保存成功");
									clearData();
									addFixture.dispose();
								} else {
									JOptionPane.showMessageDialog(addFixture, "保存失败", "error",
											JOptionPane.ERROR_MESSAGE);
								}
							} else {
								GUIUtils.setLabelText(lblMessage, "Fixture Type can't be blank!");
							}
						}
					}

				});
				saveButton.setActionCommand("OK");
				buttonPane.add(saveButton);
				getRootPane().setDefaultButton(saveButton);
			}
			{
				cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	private void clearData() {
		tfFixtureNo.setText("");
		lblMessage.setText("");
		comboBox.getModel().setSelectedItem(null);
	}

}
