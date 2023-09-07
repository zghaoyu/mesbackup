package com.cncmes.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.LinkedHashMap;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.cncmes.ctrl.RackClient;

import java.awt.Toolkit;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MaterialInput extends JDialog {
	private static final long serialVersionUID = -5420010591953514213L;
	private final JPanel contentPanel = new JPanel();
	private static String currLineName = "";
	private static LinkedHashMap<String, Integer> mInput = new LinkedHashMap<String, Integer>();
	
	private static MaterialInput materialInput = new MaterialInput();
	private JTextField txtWorkpieceqty;
	
	public static void showDialog() {
		try {
			materialInput.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			materialInput.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setLineName(String lineName){
		currLineName = lineName;
	}
	
	public static int getInputQty(String lineName){
		return (null!=mInput.get(lineName)?mInput.get(lineName):0);
	}
	
	private MaterialInput() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(MaterialInput.class.getResource("/com/cncmes/img/Material_16.png")));
		setTitle("Randomly Input Material");
		setModal(true);
		
		int width = 400;
		int height = 70;
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] {160, 140, 80};
		gbl_contentPanel.rowHeights = new int[] {0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, 0.0};
		gbl_contentPanel.rowWeights = new double[]{0.0};
		contentPanel.setLayout(gbl_contentPanel);
		
		JLabel lblInputWorkpieceQty = new JLabel("Input Workpiece QTY");
		GridBagConstraints gbc_lblInputWorkpieceQty = new GridBagConstraints();
		gbc_lblInputWorkpieceQty.insets = new Insets(0, 0, 0, 5);
		gbc_lblInputWorkpieceQty.gridx = 0;
		gbc_lblInputWorkpieceQty.gridy = 0;
		contentPanel.add(lblInputWorkpieceQty, gbc_lblInputWorkpieceQty);
		
		txtWorkpieceqty = new JTextField();
		txtWorkpieceqty.setText("1");
		GridBagConstraints gbc_txtWorkpieceqty = new GridBagConstraints();
		gbc_txtWorkpieceqty.insets = new Insets(0, 0, 0, 5);
		gbc_txtWorkpieceqty.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtWorkpieceqty.gridx = 1;
		gbc_txtWorkpieceqty.gridy = 0;
		contentPanel.add(txtWorkpieceqty, gbc_txtWorkpieceqty);
		txtWorkpieceqty.setColumns(10);
		
		JButton btnSubmit = new JButton("Submit");
		btnSubmit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(btnSubmit.isEnabled() && 1==arg0.getButton()){
					int qty = 0;
					try {
						qty = Integer.parseInt(txtWorkpieceqty.getText().trim());
					} catch (NumberFormatException e) {
						qty = 0;
					}
					
					System.out.println("LineName:"+currLineName+"/"+qty);
					mInput.put(currLineName, qty);
					if(qty>0){
						RackClient rackClient = RackClient.getInstance();
						if(rackClient.randomUpdateMaterialRack(currLineName, false, qty)){
							JOptionPane.showMessageDialog(contentPanel, "Randomly input "+qty+" workpieces OK", "Input OK", JOptionPane.INFORMATION_MESSAGE);
						}else{
							JOptionPane.showMessageDialog(contentPanel, "Randomly input "+qty+" workpieces NG", "Input Failed", JOptionPane.ERROR_MESSAGE);
						}
					}else{
						JOptionPane.showMessageDialog(contentPanel, "Input QTY is wrong, please check", "Input Invalid", JOptionPane.ERROR_MESSAGE);
					}
					
					materialInput.dispose();
				}
			}
		});
		GridBagConstraints gbc_btnSubmit = new GridBagConstraints();
		gbc_btnSubmit.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSubmit.gridx = 2;
		gbc_btnSubmit.gridy = 0;
		contentPanel.add(btnSubmit, gbc_btnSubmit);
	}

}
