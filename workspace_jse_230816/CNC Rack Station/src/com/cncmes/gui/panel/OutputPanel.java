package com.cncmes.gui.panel;

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.SystemColor;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.border.LineBorder;
import javax.swing.JTextField;

/**
 * 
 * @author W000586 Hui Zhi 2022/3/2
 *
 */
public class OutputPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6188892909569777890L;

	private static OutputPanel outputPanel = new OutputPanel();
	private JPanel panel_output;
	private JPanel panel_output_title;
	private JPanel panel_output_content;
	private JLabel lblOutput;
	private JLabel lblFixtureNo;
	private JTextField textField;
	private JLabel label;
	private JTextField textField_1;

	/**
	 * Create the panel.
	 */
	private OutputPanel() {
		this.setBackground(new Color(245, 245, 245));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 10, 44, 10, 0 };
		gridBagLayout.rowHeights = new int[] { 10, 24, 10, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		panel_output = new JPanel();
		panel_output.setBackground(Color.WHITE);
		panel_output.setBorder(new LineBorder(Color.LIGHT_GRAY));
		GridBagConstraints gbc_panel_output = new GridBagConstraints();
		gbc_panel_output.insets = new Insets(0, 0, 5, 5);
		gbc_panel_output.fill = GridBagConstraints.BOTH;
		gbc_panel_output.gridx = 1;
		gbc_panel_output.gridy = 1;
		add(panel_output, gbc_panel_output);
		panel_output.setLayout(new BorderLayout(0, 0));

		panel_output_title = new JPanel();
		panel_output_title.setBackground(SystemColor.controlHighlight);
		panel_output.add(panel_output_title, BorderLayout.NORTH);

		lblOutput = new JLabel("Output");
		panel_output_title.add(lblOutput);

		panel_output_content = new JPanel();
		panel_output_content.setBackground(Color.WHITE);
		panel_output.add(panel_output_content, BorderLayout.CENTER);
		GridBagLayout gbl_panel_output_content = new GridBagLayout();
		gbl_panel_output_content.columnWidths = new int[] { 25, 0, 120, 10, 0 };
		gbl_panel_output_content.rowHeights = new int[] { 25, 0, 10, 0, 0 };
		gbl_panel_output_content.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel_output_content.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel_output_content.setLayout(gbl_panel_output_content);
		
		lblFixtureNo = new JLabel("Fixture No :");
		GridBagConstraints gbc_lblFixtureNo = new GridBagConstraints();
		gbc_lblFixtureNo.anchor = GridBagConstraints.EAST;
		gbc_lblFixtureNo.insets = new Insets(0, 0, 5, 5);
		gbc_lblFixtureNo.gridx = 1;
		gbc_lblFixtureNo.gridy = 1;
		panel_output_content.add(lblFixtureNo, gbc_lblFixtureNo);
		
		textField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 2;
		gbc_textField.gridy = 1;
		panel_output_content.add(textField, gbc_textField);
		textField.setColumns(10);
		
		label = new JLabel("Fixture No :");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.EAST;
		gbc_label.insets = new Insets(0, 0, 0, 5);
		gbc_label.gridx = 1;
		gbc_label.gridy = 3;
		panel_output_content.add(label, gbc_label);
		
		textField_1 = new JTextField();
		textField_1.setColumns(10);
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.insets = new Insets(0, 0, 0, 5);
		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_1.gridx = 2;
		gbc_textField_1.gridy = 3;
		panel_output_content.add(textField_1, gbc_textField_1);

	}

	public static OutputPanel getInstance() {
		return outputPanel;
	}

}
