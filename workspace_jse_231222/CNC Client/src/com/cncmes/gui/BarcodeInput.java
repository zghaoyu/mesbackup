package com.cncmes.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.cncmes.base.BarcodeValidate;
import com.cncmes.utils.GUIUtils;
import com.cncmes.utils.ThreadUtils;

import java.awt.GridBagLayout;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class BarcodeInput extends JDialog {
	private static final long serialVersionUID = 14L;
	private final JPanel contentPanel = new JPanel();
	private static BarcodeInput thisDialog = new BarcodeInput();
	private JTextField txtBarcode;
	private String barcode;
	private boolean waitingFlag;
	private BarcodeValidate bcValidate;
	private JButton confirmButton;
	
	public static BarcodeInput getInstance(){
		return thisDialog;
	}

	public JButton getConfirmButton() {
		return confirmButton;
	}

	public void setConfirmButton(JButton confirmButton) {
		this.confirmButton = confirmButton;
	}

	public void showDialog(BarcodeValidate barcodeValidate){
		waitingFlag = true;
		ThreadUtils.Run(new BarcodeWaiting());
		bcValidate = barcodeValidate;
		thisDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		thisDialog.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				waitingFlag = false;
				thisDialog.dispose();
			}
		});
		
		txtBarcode.setText("");
		thisDialog.setVisible(true);
	}
	
	/**
	 * Create the dialog.
	 */
	private BarcodeInput() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(BarcodeInput.class.getResource("/com/cncmes/img/Butterfly_24.png")));
		setTitle("Barcode Scanning");
		setModal(true);
		
		int width = 600;
		int height = 100;
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
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
			JLabel lblBarcode = new JLabel("Barcode");
			GridBagConstraints gbc_lblBarcode = new GridBagConstraints();
			gbc_lblBarcode.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblBarcode.insets = new Insets(0, 0, 0, 5);
			gbc_lblBarcode.gridx = 0;
			gbc_lblBarcode.gridy = 0;
			contentPanel.add(lblBarcode, gbc_lblBarcode);
		}
		{
			txtBarcode = new JTextField();
			GridBagConstraints gbc_txtBarcode = new GridBagConstraints();
			gbc_txtBarcode.fill = GridBagConstraints.BOTH;
			gbc_txtBarcode.gridx = 1;
			gbc_txtBarcode.gridy = 0;
			contentPanel.add(txtBarcode, gbc_txtBarcode);
			txtBarcode.setColumns(10);
		}
		{
			confirmButton = new JButton("search");
//			confirmButton.setBounds(0,0,200,80);
			confirmButton.setFont(GUIUtils.contentFont);
			GridBagConstraints gbc_btnConfirm = new GridBagConstraints();
			gbc_btnConfirm.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnConfirm.insets = new Insets(0, 0, 0, 0);
			gbc_btnConfirm.gridx = 1;
			gbc_btnConfirm.gridy = 1;
			contentPanel.add(confirmButton, gbc_btnConfirm);
		}
		confirmButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				barcode = txtBarcode.getText().trim();
//				bcValidate.doValidate(barcode);
				waitingFlag = false;
				thisDialog.dispose();
			}
		});
	}
	
	class BarcodeWaiting implements Runnable{
		@Override
		public void run() {
			long start = System.currentTimeMillis();
			long wait = 100000; // 100s
			boolean waitReading = false;
			
			while(waitingFlag){
				try {
					barcode = txtBarcode.getText().trim();
					if(barcode.length() > 32){
						if(!waitReading){
							start = System.currentTimeMillis();
							wait = 1000; // 1s
							waitReading = true;
						}else{
							if(System.currentTimeMillis() - start > wait) waitingFlag = false;
						}
					}else{
						if(System.currentTimeMillis() - start > wait) waitingFlag = false;
					}
				} catch (Exception e) {
				}
			}
			
			bcValidate.doValidate(barcode);
			thisDialog.dispose();
		}
	}
}
