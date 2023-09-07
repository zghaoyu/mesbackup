package com.cncmes.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BarcodeInput extends JDialog {
	private static final long serialVersionUID = 14L;
	private final JPanel contentPanel = new JPanel();
	private static BarcodeInput thisDialog = new BarcodeInput();
	private JTextField txtBarcode1;
	private JLabel lblWorkZone2;
	private JTextField txtBarcode2;
	private JLabel lblWorkZone3;
	private JTextField txtBarcode3;
	private JLabel lblWorkZone4;
	private JTextField txtBarcode4;
	private JLabel lblWorkZone5;
	private JTextField txtBarcode5;
	private JLabel lblWorkZone6;
	private JTextField txtBarcode6;
	private JButton btnSubmit;
	
	private String workzones;
	private String workpieceIDs;
	
	public static BarcodeInput getInstance(){
		return thisDialog;
	}
	
	public void showDialog(){
		thisDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		thisDialog.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				thisDialog.dispose();
			}
		});
		
		txtBarcode1.setText("");
		txtBarcode2.setText("");
		txtBarcode3.setText("");
		txtBarcode4.setText("");
		txtBarcode5.setText("");
		txtBarcode6.setText("");
		workzones = "";
		workpieceIDs = "";
		thisDialog.setVisible(true);
	}
	
	public String getWorkZones(){
		return workzones;
	}
	
	public String getWorkpieceIDs(){
		return workpieceIDs;
	}
	
	/**
	 * Create the dialog.
	 */
	private BarcodeInput() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(BarcodeInput.class.getResource("/com/cncmes/img/doctor_24.png")));
		setTitle("Barcode Scanning");
		setModal(true);
		
		int width = 400;
		int height = 240;
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblWorkZone1 = new JLabel("Work Zone 1");
			GridBagConstraints gbc_lblWorkZone1 = new GridBagConstraints();
			gbc_lblWorkZone1.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblWorkZone1.insets = new Insets(0, 0, 5, 5);
			gbc_lblWorkZone1.gridx = 0;
			gbc_lblWorkZone1.gridy = 0;
			contentPanel.add(lblWorkZone1, gbc_lblWorkZone1);
		}
		{
			txtBarcode1 = new JTextField();
			GridBagConstraints gbc_txtBarcode1 = new GridBagConstraints();
			gbc_txtBarcode1.insets = new Insets(0, 0, 5, 0);
			gbc_txtBarcode1.fill = GridBagConstraints.BOTH;
			gbc_txtBarcode1.gridx = 1;
			gbc_txtBarcode1.gridy = 0;
			contentPanel.add(txtBarcode1, gbc_txtBarcode1);
			txtBarcode1.setColumns(10);
		}
		{
			lblWorkZone2 = new JLabel("Work Zone 2");
			GridBagConstraints gbc_lblWorkZone2 = new GridBagConstraints();
			gbc_lblWorkZone2.anchor = GridBagConstraints.EAST;
			gbc_lblWorkZone2.insets = new Insets(0, 0, 5, 5);
			gbc_lblWorkZone2.gridx = 0;
			gbc_lblWorkZone2.gridy = 1;
			contentPanel.add(lblWorkZone2, gbc_lblWorkZone2);
		}
		{
			txtBarcode2 = new JTextField();
			GridBagConstraints gbc_txtBarcode2 = new GridBagConstraints();
			gbc_txtBarcode2.insets = new Insets(0, 0, 5, 0);
			gbc_txtBarcode2.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtBarcode2.gridx = 1;
			gbc_txtBarcode2.gridy = 1;
			contentPanel.add(txtBarcode2, gbc_txtBarcode2);
			txtBarcode2.setColumns(10);
		}
		{
			lblWorkZone3 = new JLabel("Work Zone 3");
			GridBagConstraints gbc_lblWorkZone3 = new GridBagConstraints();
			gbc_lblWorkZone3.anchor = GridBagConstraints.EAST;
			gbc_lblWorkZone3.insets = new Insets(0, 0, 5, 5);
			gbc_lblWorkZone3.gridx = 0;
			gbc_lblWorkZone3.gridy = 2;
			contentPanel.add(lblWorkZone3, gbc_lblWorkZone3);
		}
		{
			txtBarcode3 = new JTextField();
			GridBagConstraints gbc_txtBarcode3 = new GridBagConstraints();
			gbc_txtBarcode3.insets = new Insets(0, 0, 5, 0);
			gbc_txtBarcode3.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtBarcode3.gridx = 1;
			gbc_txtBarcode3.gridy = 2;
			contentPanel.add(txtBarcode3, gbc_txtBarcode3);
			txtBarcode3.setColumns(10);
		}
		{
			lblWorkZone4 = new JLabel("Work Zone 4");
			GridBagConstraints gbc_lblWorkZone4 = new GridBagConstraints();
			gbc_lblWorkZone4.anchor = GridBagConstraints.EAST;
			gbc_lblWorkZone4.insets = new Insets(0, 0, 5, 5);
			gbc_lblWorkZone4.gridx = 0;
			gbc_lblWorkZone4.gridy = 3;
			contentPanel.add(lblWorkZone4, gbc_lblWorkZone4);
		}
		{
			txtBarcode4 = new JTextField();
			GridBagConstraints gbc_txtBarcode4 = new GridBagConstraints();
			gbc_txtBarcode4.insets = new Insets(0, 0, 5, 0);
			gbc_txtBarcode4.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtBarcode4.gridx = 1;
			gbc_txtBarcode4.gridy = 3;
			contentPanel.add(txtBarcode4, gbc_txtBarcode4);
			txtBarcode4.setColumns(10);
		}
		{
			lblWorkZone5 = new JLabel("Work Zone 5");
			GridBagConstraints gbc_lblWorkZone5 = new GridBagConstraints();
			gbc_lblWorkZone5.anchor = GridBagConstraints.EAST;
			gbc_lblWorkZone5.insets = new Insets(0, 0, 5, 5);
			gbc_lblWorkZone5.gridx = 0;
			gbc_lblWorkZone5.gridy = 4;
			contentPanel.add(lblWorkZone5, gbc_lblWorkZone5);
		}
		{
			txtBarcode5 = new JTextField();
			GridBagConstraints gbc_txtBarcode5 = new GridBagConstraints();
			gbc_txtBarcode5.insets = new Insets(0, 0, 5, 0);
			gbc_txtBarcode5.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtBarcode5.gridx = 1;
			gbc_txtBarcode5.gridy = 4;
			contentPanel.add(txtBarcode5, gbc_txtBarcode5);
			txtBarcode5.setColumns(10);
		}
		{
			lblWorkZone6 = new JLabel("Work Zone 6");
			GridBagConstraints gbc_lblWorkZone6 = new GridBagConstraints();
			gbc_lblWorkZone6.anchor = GridBagConstraints.EAST;
			gbc_lblWorkZone6.insets = new Insets(0, 0, 5, 5);
			gbc_lblWorkZone6.gridx = 0;
			gbc_lblWorkZone6.gridy = 5;
			contentPanel.add(lblWorkZone6, gbc_lblWorkZone6);
		}
		{
			txtBarcode6 = new JTextField();
			GridBagConstraints gbc_txtBarcode6 = new GridBagConstraints();
			gbc_txtBarcode6.insets = new Insets(0, 0, 5, 0);
			gbc_txtBarcode6.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtBarcode6.gridx = 1;
			gbc_txtBarcode6.gridy = 5;
			contentPanel.add(txtBarcode6, gbc_txtBarcode6);
			txtBarcode6.setColumns(10);
		}
		{
			btnSubmit = new JButton("Submit");
			btnSubmit.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					if(1 == arg0.getButton()){
						String id = "", temp = "";
						workzones = "";
						workpieceIDs = "";
						
						id = txtBarcode1.getText().trim();
						if(!"".equals(id)){
							if("".equals(workzones)){
								workzones = "1";
								workpieceIDs = id;
								temp = "Zone 1: " + id;
							}else{
								workzones += ";" + "1";
								workpieceIDs += ";" + id;
								temp += "\r\n" + "Zone 1: " + id;
							}
						}
						
						id = txtBarcode2.getText().trim();
						if(!"".equals(id)){
							if("".equals(workzones)){
								workzones = "2";
								workpieceIDs = id;
								temp = "Zone 2: " + id;
							}else{
								workzones += ";" + "2";
								workpieceIDs += ";" + id;
								temp += "\r\n" + "Zone 2: " + id;
							}
						}
						
						id = txtBarcode3.getText().trim();
						if(!"".equals(id)){
							if("".equals(workzones)){
								workzones = "3";
								workpieceIDs = id;
								temp = "Zone 3: " + id;
							}else{
								workzones += ";" + "3";
								workpieceIDs += ";" + id;
								temp += "\r\n" + "Zone 3: " + id;
							}
						}
						
						id = txtBarcode4.getText().trim();
						if(!"".equals(id)){
							if("".equals(workzones)){
								workzones = "4";
								workpieceIDs = id;
								temp = "Zone 4: " + id;
							}else{
								workzones += ";" + "4";
								workpieceIDs += ";" + id;
								temp += "\r\n" + "Zone 4: " + id;
							}
						}
						
						id = txtBarcode5.getText().trim();
						if(!"".equals(id)){
							if("".equals(workzones)){
								workzones = "5";
								workpieceIDs = id;
								temp = "Zone 5: " + id;
							}else{
								workzones += ";" + "5";
								workpieceIDs += ";" + id;
								temp += "\r\n" + "Zone 5: " + id;
							}
						}
						
						id = txtBarcode6.getText().trim();
						if(!"".equals(id)){
							if("".equals(workzones)){
								workzones = "6";
								workpieceIDs = id;
								temp = "Zone 6: " + id;
							}else{
								workzones += ";" + "6";
								workpieceIDs += ";" + id;
								temp += "\r\n" + "Zone 6: " + id;
							}
						}
						
						if(!"".equals(workzones)){
							if(0==JOptionPane.showConfirmDialog(contentPanel, "Would you like to submit below materials now?\r\n"+temp, "Manually Submit Scanning Barcode", JOptionPane.YES_NO_OPTION)){
								thisDialog.dispose();
							}
						}
					}
				}
			});
			GridBagConstraints gbc_btnSubmit = new GridBagConstraints();
			gbc_btnSubmit.anchor = GridBagConstraints.EAST;
			gbc_btnSubmit.gridx = 1;
			gbc_btnSubmit.gridy = 6;
			contentPanel.add(btnSubmit, gbc_btnSubmit);
		}
	}
}
