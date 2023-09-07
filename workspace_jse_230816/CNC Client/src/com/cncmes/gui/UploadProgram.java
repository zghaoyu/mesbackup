 package com.cncmes.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.cncmes.base.CNC;
import com.cncmes.base.CncItems;
import com.cncmes.base.DriverItems;
import com.cncmes.base.WorkpieceItems;
import com.cncmes.ctrl.CncFactory;
import com.cncmes.data.CncData;
import com.cncmes.data.CncDriver;
import com.cncmes.data.TaskData;
import com.cncmes.data.WorkpieceData;
import com.cncmes.utils.DataUtils;
import com.cncmes.utils.UploadNCProgram;

import java.awt.Toolkit;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UploadProgram extends JDialog {
	private static final long serialVersionUID = 30L;
	private final JPanel contentPanel = new JPanel();
	private static UploadProgram uploadProgram = new UploadProgram();
	private JTextField txtCncIp;
	private JTextField txtWorkpiece1;
	private JTextField txtWorkpiece2;
	private JTextField txtWorkpiece3;
	private JTextField txtWorkpiece4;
	private JTextField txtWorkpiece5;
	private JTextField txtWorkpiece6;
	private JTextField txtLineName;
	private JTextField txtTaskId;
	public static UploadProgram getInstance(){
		return uploadProgram;
	}
	
	/**
	 * Create the dialog.
	 */
	private UploadProgram() {
		setModal(true);
		setIconImage(Toolkit.getDefaultToolkit().getImage(UploadProgram.class.getResource("/com/cncmes/img/Butterfly_24.png")));
		setTitle("Upload Program");
		setBounds(100, 100, 450, 350);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] {0, 0};
		gbl_contentPanel.rowHeights = new int[] {30, 30, 30, 30, 30, 30, 30, 30, 30};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblCncIp = new JLabel("CNC IP");
			GridBagConstraints gbc_lblCncIp = new GridBagConstraints();
			gbc_lblCncIp.insets = new Insets(0, 0, 5, 5);
			gbc_lblCncIp.anchor = GridBagConstraints.EAST;
			gbc_lblCncIp.gridx = 0;
			gbc_lblCncIp.gridy = 0;
			contentPanel.add(lblCncIp, gbc_lblCncIp);
		}
		{
			txtCncIp = new JTextField();
			txtCncIp.setText("10.10.95.65");
			GridBagConstraints gbc_txtCncIp = new GridBagConstraints();
			gbc_txtCncIp.insets = new Insets(0, 0, 5, 0);
			gbc_txtCncIp.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtCncIp.gridx = 1;
			gbc_txtCncIp.gridy = 0;
			contentPanel.add(txtCncIp, gbc_txtCncIp);
			txtCncIp.setColumns(10);
		}
		{
			JLabel lblWorkpiece = new JLabel("Workpiece 1");
			GridBagConstraints gbc_lblWorkpiece = new GridBagConstraints();
			gbc_lblWorkpiece.anchor = GridBagConstraints.EAST;
			gbc_lblWorkpiece.insets = new Insets(0, 0, 5, 5);
			gbc_lblWorkpiece.gridx = 0;
			gbc_lblWorkpiece.gridy = 1;
			contentPanel.add(lblWorkpiece, gbc_lblWorkpiece);
		}
		{
			txtWorkpiece1 = new JTextField();
			txtWorkpiece1.setText("M00001150656205764850");
			GridBagConstraints gbc_txtWorkpiece1 = new GridBagConstraints();
			gbc_txtWorkpiece1.insets = new Insets(0, 0, 5, 0);
			gbc_txtWorkpiece1.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtWorkpiece1.gridx = 1;
			gbc_txtWorkpiece1.gridy = 1;
			contentPanel.add(txtWorkpiece1, gbc_txtWorkpiece1);
			txtWorkpiece1.setColumns(10);
		}
		{
			JLabel lblWorkpiece_1 = new JLabel("Workpiece 2");
			GridBagConstraints gbc_lblWorkpiece_1 = new GridBagConstraints();
			gbc_lblWorkpiece_1.anchor = GridBagConstraints.EAST;
			gbc_lblWorkpiece_1.insets = new Insets(0, 0, 5, 5);
			gbc_lblWorkpiece_1.gridx = 0;
			gbc_lblWorkpiece_1.gridy = 2;
			contentPanel.add(lblWorkpiece_1, gbc_lblWorkpiece_1);
		}
		{
			txtWorkpiece2 = new JTextField();
			txtWorkpiece2.setText("M00002150656205764869");
			GridBagConstraints gbc_txtWorkpiece2 = new GridBagConstraints();
			gbc_txtWorkpiece2.insets = new Insets(0, 0, 5, 0);
			gbc_txtWorkpiece2.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtWorkpiece2.gridx = 1;
			gbc_txtWorkpiece2.gridy = 2;
			contentPanel.add(txtWorkpiece2, gbc_txtWorkpiece2);
			txtWorkpiece2.setColumns(10);
		}
		{
			JLabel lblWorkpiece_2 = new JLabel("Workpiece 3");
			GridBagConstraints gbc_lblWorkpiece_2 = new GridBagConstraints();
			gbc_lblWorkpiece_2.anchor = GridBagConstraints.EAST;
			gbc_lblWorkpiece_2.insets = new Insets(0, 0, 5, 5);
			gbc_lblWorkpiece_2.gridx = 0;
			gbc_lblWorkpiece_2.gridy = 3;
			contentPanel.add(lblWorkpiece_2, gbc_lblWorkpiece_2);
		}
		{
			txtWorkpiece3 = new JTextField();
			txtWorkpiece3.setText("M00003150656205764881");
			GridBagConstraints gbc_txtWorkpiece3 = new GridBagConstraints();
			gbc_txtWorkpiece3.insets = new Insets(0, 0, 5, 0);
			gbc_txtWorkpiece3.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtWorkpiece3.gridx = 1;
			gbc_txtWorkpiece3.gridy = 3;
			contentPanel.add(txtWorkpiece3, gbc_txtWorkpiece3);
			txtWorkpiece3.setColumns(10);
		}
		{
			JLabel lblWorkpiece_3 = new JLabel("Workpiece 4");
			GridBagConstraints gbc_lblWorkpiece_3 = new GridBagConstraints();
			gbc_lblWorkpiece_3.anchor = GridBagConstraints.EAST;
			gbc_lblWorkpiece_3.insets = new Insets(0, 0, 5, 5);
			gbc_lblWorkpiece_3.gridx = 0;
			gbc_lblWorkpiece_3.gridy = 4;
			contentPanel.add(lblWorkpiece_3, gbc_lblWorkpiece_3);
		}
		{
			txtWorkpiece4 = new JTextField();
			txtWorkpiece4.setText("M00004150656205764871");
			GridBagConstraints gbc_txtWorkpiece4 = new GridBagConstraints();
			gbc_txtWorkpiece4.insets = new Insets(0, 0, 5, 0);
			gbc_txtWorkpiece4.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtWorkpiece4.gridx = 1;
			gbc_txtWorkpiece4.gridy = 4;
			contentPanel.add(txtWorkpiece4, gbc_txtWorkpiece4);
			txtWorkpiece4.setColumns(10);
		}
		{
			JLabel lblWorkpiece_4 = new JLabel("Workpiece 5");
			GridBagConstraints gbc_lblWorkpiece_4 = new GridBagConstraints();
			gbc_lblWorkpiece_4.anchor = GridBagConstraints.EAST;
			gbc_lblWorkpiece_4.insets = new Insets(0, 0, 5, 5);
			gbc_lblWorkpiece_4.gridx = 0;
			gbc_lblWorkpiece_4.gridy = 5;
			contentPanel.add(lblWorkpiece_4, gbc_lblWorkpiece_4);
		}
		{
			txtWorkpiece5 = new JTextField();
			txtWorkpiece5.setText("M00005150656205764883");
			GridBagConstraints gbc_txtWorkpiece5 = new GridBagConstraints();
			gbc_txtWorkpiece5.insets = new Insets(0, 0, 5, 0);
			gbc_txtWorkpiece5.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtWorkpiece5.gridx = 1;
			gbc_txtWorkpiece5.gridy = 5;
			contentPanel.add(txtWorkpiece5, gbc_txtWorkpiece5);
			txtWorkpiece5.setColumns(10);
		}
		{
			JLabel lblWorkpiece_5 = new JLabel("Workpiece 6");
			GridBagConstraints gbc_lblWorkpiece_5 = new GridBagConstraints();
			gbc_lblWorkpiece_5.anchor = GridBagConstraints.EAST;
			gbc_lblWorkpiece_5.insets = new Insets(0, 0, 5, 5);
			gbc_lblWorkpiece_5.gridx = 0;
			gbc_lblWorkpiece_5.gridy = 6;
			contentPanel.add(lblWorkpiece_5, gbc_lblWorkpiece_5);
		}
		{
			txtWorkpiece6 = new JTextField();
			txtWorkpiece6.setText("M00006150656205764868");
			GridBagConstraints gbc_txtWorkpiece6 = new GridBagConstraints();
			gbc_txtWorkpiece6.insets = new Insets(0, 0, 5, 0);
			gbc_txtWorkpiece6.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtWorkpiece6.gridx = 1;
			gbc_txtWorkpiece6.gridy = 6;
			contentPanel.add(txtWorkpiece6, gbc_txtWorkpiece6);
			txtWorkpiece6.setColumns(10);
		}
		{
			JLabel lblLineName = new JLabel("Line Name");
			GridBagConstraints gbc_lblLineName = new GridBagConstraints();
			gbc_lblLineName.anchor = GridBagConstraints.EAST;
			gbc_lblLineName.insets = new Insets(0, 0, 5, 5);
			gbc_lblLineName.gridx = 0;
			gbc_lblLineName.gridy = 7;
			contentPanel.add(lblLineName, gbc_lblLineName);
		}
		{
			txtLineName = new JTextField();
			txtLineName.setText("D2E1D2A");
			GridBagConstraints gbc_txtLineName = new GridBagConstraints();
			gbc_txtLineName.insets = new Insets(0, 0, 5, 0);
			gbc_txtLineName.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtLineName.gridx = 1;
			gbc_txtLineName.gridy = 7;
			contentPanel.add(txtLineName, gbc_txtLineName);
			txtLineName.setColumns(10);
		}
		{
			JLabel lblTaskId = new JLabel("Task ID");
			GridBagConstraints gbc_lblTaskId = new GridBagConstraints();
			gbc_lblTaskId.anchor = GridBagConstraints.EAST;
			gbc_lblTaskId.insets = new Insets(0, 0, 0, 5);
			gbc_lblTaskId.gridx = 0;
			gbc_lblTaskId.gridy = 8;
			contentPanel.add(lblTaskId, gbc_lblTaskId);
		}
		{
			txtTaskId = new JTextField();
			txtTaskId.setText("DDFCFC0BD580747D6CA64F8E25AA63AE");
			GridBagConstraints gbc_txtTaskId = new GridBagConstraints();
			gbc_txtTaskId.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtTaskId.gridx = 1;
			gbc_txtTaskId.gridy = 8;
			contentPanel.add(txtTaskId, gbc_txtTaskId);
			txtTaskId.setColumns(10);
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
						if(1 == arg0.getButton() && okButton.isEnabled()){
							okButton.setEnabled(false);
							
							TaskData taskData = TaskData.getInstance();
							CncData cncData = CncData.getInstance();
							CncDriver cncDriver = CncDriver.getInstance();
							WorkpieceData wpData = WorkpieceData.getInstance();
							String  lineName = txtLineName.getText().trim();
							
							String workpieceIDs = txtWorkpiece1.getText().trim();
							workpieceIDs += ";" + txtWorkpiece2.getText().trim();
							workpieceIDs += ";" + txtWorkpiece3.getText().trim();
							workpieceIDs += ";" + txtWorkpiece4.getText().trim();
							workpieceIDs += ";" + txtWorkpiece5.getText().trim();
							workpieceIDs += ";" + txtWorkpiece6.getText().trim();
							
							String taskID = txtTaskId.getText().trim();
							String cncIP = txtCncIp.getText().trim();
							String cncModel = (String) cncData.getData(cncIP).get(CncItems.MODEL);
							
							String[] ids = workpieceIDs.split(";");
							workpieceIDs = "";
							for(int i=0; i<ids.length; i++){
								wpData.setData(ids[i], WorkpieceItems.ID, ids[i]);
								DataUtils.updateWorkpieceData(ids[i], lineName, "3", ""+(i+1));
								if(wpData.canMachineByCNC(ids[i], cncModel, null)){
									if("".equals(workpieceIDs)){
										workpieceIDs = ids[i];
									}else{
										workpieceIDs += ";" + ids[i];
									}
									cncData.setWorkpieceID(cncIP, i+1, ids[i]);
								}
							}
							
							String errMsg = "";
							if(!"".equals(workpieceIDs)){
								String cncDrvName = (String)cncDriver.getData(cncModel).get(DriverItems.DRIVER);
								String cncDataHandler = (String)cncDriver.getData(cncModel).get(DriverItems.DATAHANDLER);
								CNC cncCtrl = CncFactory.getInstance(cncDrvName,cncDataHandler,cncModel);
								
								if(null != cncCtrl){
									cncData.setData(cncIP, CncItems.CTRL, cncCtrl);
									if(UploadNCProgram.uploadSubPrograms(cncCtrl, cncIP, cncData.getWorkpieceIDs(cncIP))){
										if(!UploadNCProgram.uploadMainProgram(cncCtrl, cncIP, cncData.getWorkpieceIDs(cncIP))){
											errMsg = "Upload main program failed";
										}
									}else{
										errMsg = "Upload sub programs failed";
									}
									taskData.removeData(taskID);
								}
							}else{
								errMsg = "All workpieces can't be machined by "+cncModel;
							}
							cncData.clearWorkpieceID(cncIP);
							wpData.clearData();
							if(!"".equals(errMsg)){
								JOptionPane.showMessageDialog(contentPanel, errMsg, "Upload Program Error", JOptionPane.ERROR_MESSAGE);
							}
							
							okButton.setEnabled(true);
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
							uploadProgram.dispose();
						}
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

}
