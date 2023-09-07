package com.cncmes.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.cncmes.base.CNC;
import com.cncmes.ctrl.CncFactory;
import com.cncmes.test.JNACallTest;

import java.awt.Toolkit;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class CppDllCall extends JDialog {
	private final JPanel contentPanel = new JPanel();
	private static CppDllCall cppDllCall = new CppDllCall();
	private static final long serialVersionUID = 2L;
	private JTextField textField_number1;
	private JTextField textField_number2;
	private JTextField textField_calcResult;
	
	public static CppDllCall getInstance(){
		return cppDllCall;
	}
	
	/**
	 * Create the dialog.
	 */
	private CppDllCall() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(CppDllCall.class.getResource("/com/cncmes/img/Butterfly_24.png")));
		setModal(true);
		setTitle("CPP Dll Call Test");
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 400;
		int height = 175;
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] {87, 287, 0};
		gbl_contentPanel.rowHeights = new int[]{30, 30, 30, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblNumber = new JLabel("Number 1");
			GridBagConstraints gbc_lblNumber = new GridBagConstraints();
			gbc_lblNumber.fill = GridBagConstraints.BOTH;
			gbc_lblNumber.insets = new Insets(0, 0, 5, 5);
			gbc_lblNumber.gridx = 0;
			gbc_lblNumber.gridy = 0;
			contentPanel.add(lblNumber, gbc_lblNumber);
		}
		{
			textField_number1 = new JTextField();
			textField_number1.setHorizontalAlignment(SwingConstants.LEFT);
			GridBagConstraints gbc_textField_number1 = new GridBagConstraints();
			gbc_textField_number1.fill = GridBagConstraints.BOTH;
			gbc_textField_number1.insets = new Insets(0, 0, 5, 0);
			gbc_textField_number1.gridx = 1;
			gbc_textField_number1.gridy = 0;
			contentPanel.add(textField_number1, gbc_textField_number1);
			textField_number1.setColumns(20);
		}
		{
			JLabel lblNumber_1 = new JLabel("Number 2");
			GridBagConstraints gbc_lblNumber_1 = new GridBagConstraints();
			gbc_lblNumber_1.fill = GridBagConstraints.BOTH;
			gbc_lblNumber_1.insets = new Insets(0, 0, 5, 5);
			gbc_lblNumber_1.gridx = 0;
			gbc_lblNumber_1.gridy = 1;
			contentPanel.add(lblNumber_1, gbc_lblNumber_1);
		}
		{
			textField_number2 = new JTextField();
			textField_number2.setHorizontalAlignment(SwingConstants.LEFT);
			GridBagConstraints gbc_textField_number2 = new GridBagConstraints();
			gbc_textField_number2.fill = GridBagConstraints.BOTH;
			gbc_textField_number2.insets = new Insets(0, 0, 5, 0);
			gbc_textField_number2.gridx = 1;
			gbc_textField_number2.gridy = 1;
			contentPanel.add(textField_number2, gbc_textField_number2);
			textField_number2.setColumns(20);
		}
		{
			JLabel lblCalcResult = new JLabel("Calc Result");
			GridBagConstraints gbc_lblCalcResult = new GridBagConstraints();
			gbc_lblCalcResult.fill = GridBagConstraints.BOTH;
			gbc_lblCalcResult.insets = new Insets(0, 0, 0, 5);
			gbc_lblCalcResult.gridx = 0;
			gbc_lblCalcResult.gridy = 2;
			contentPanel.add(lblCalcResult, gbc_lblCalcResult);
		}
		{
			textField_calcResult = new JTextField();
			textField_calcResult.setHorizontalAlignment(SwingConstants.LEFT);
			GridBagConstraints gbc_textField_calcResult = new GridBagConstraints();
			gbc_textField_calcResult.fill = GridBagConstraints.BOTH;
			gbc_textField_calcResult.gridx = 1;
			gbc_textField_calcResult.gridy = 2;
			contentPanel.add(textField_calcResult, gbc_textField_calcResult);
			textField_calcResult.setColumns(20);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton calcButton = new JButton("Calc 1");
				calcButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if(1 == e.getButton()){
							int n1 = Integer.parseInt(textField_number1.getText());
							int n2 = Integer.parseInt(textField_number2.getText());
							JNACallTest jna = new JNACallTest("libCppDllTest");
							int sum = jna.add(n1, n2);
							int fl = jna.factorial(sum);
							textField_calcResult.setText(String.valueOf(sum) + "/" + String.valueOf(fl));
						}
					}
				});
				calcButton.setActionCommand("Calc");
				buttonPane.add(calcButton);
				getRootPane().setDefaultButton(calcButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if(1 == e.getButton()){
							cppDllCall.dispose();
						}
					}
				});
				{
					JButton btnCalc = new JButton("Calc 2");
					btnCalc.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if(1 == e.getButton()){
								int n1 = Integer.parseInt(textField_number1.getText());
								int n2 = Integer.parseInt(textField_number2.getText());
								JNACallTest jna = new JNACallTest("libDllTest");
								int sum = jna.add(n1, n2);
								int fl = jna.factorial(sum);
								textField_calcResult.setText(String.valueOf(sum) + "/" + String.valueOf(fl));
							}
						}
					});
					buttonPane.add(btnCalc);
				}
				{
					JButton btnCalc3 = new JButton("Calc 3");
					btnCalc3.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if(1 == e.getButton()){
								String ip = textField_number1.getText();
								String libName = textField_number2.getText();
								CNC cnc = CncFactory.getInstance(libName,libName,libName);
								if(null != cnc){
									try {
										textField_calcResult.setText(String.valueOf(cnc.openDoor(ip))+"/"+String.valueOf(cnc.closeDoor(ip)));
									} catch (Exception e1) {
										JOptionPane.showMessageDialog(cppDllCall.getContentPane(), e1.getMessage(), "Invoke Error", JOptionPane.ERROR_MESSAGE);
									}
								}
							}
						}
					});
					buttonPane.add(btnCalc3);
				}
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
}