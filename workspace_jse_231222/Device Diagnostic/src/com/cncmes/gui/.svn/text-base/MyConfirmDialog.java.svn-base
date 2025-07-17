package com.cncmes.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.GridBagConstraints;
import javax.swing.ImageIcon;

public class MyConfirmDialog extends JDialog {
	private static final long serialVersionUID = 8833416709369616259L;
	public static final int OPTION_YES = 1;
	public static final int OPTION_NO = 0;
	
	private final JPanel contentPanel = new JPanel();
	private static MyConfirmDialog dialog = new MyConfirmDialog();
	private static int confirmFlag;
	
	private static JTextArea txtrMsg;
	
	public static void showDialog(String title, String msg) {
		try {
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setTitle(title);
			if(null!=txtrMsg){
				txtrMsg.setText(msg);
			}
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static int getConfirmFlag(){
		return confirmFlag;
	}
	
	private void setConfirmFlag(int flag){
		confirmFlag = flag;
	}
	
	private MyConfirmDialog() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(MyConfirmDialog.class.getResource("/com/cncmes/img/delete_16.png")));
		setTitle("Risky Operation");
		setModal(true);
		
		int width = 450;
		int height = 300;
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0};
		gbl_contentPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JScrollPane scrollPaneMsg = new JScrollPane();
			GridBagConstraints gbc_scrollPaneMsg = new GridBagConstraints();
			gbc_scrollPaneMsg.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneMsg.gridx = 0;
			gbc_scrollPaneMsg.gridy = 0;
			contentPanel.add(scrollPaneMsg, gbc_scrollPaneMsg);
			{
				txtrMsg = new JTextArea();
				txtrMsg.setEditable(false);
				scrollPaneMsg.setViewportView(txtrMsg);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("YES");
				okButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent arg0) {
						if(1==arg0.getButton()){
							setConfirmFlag(OPTION_YES);
							dialog.dispose();
						}
					}
				});
				{
					JLabel lblConfirmation = new JLabel("Are you sure of keeping going anyway ???");
					lblConfirmation.setIcon(new ImageIcon(MyConfirmDialog.class.getResource("/com/cncmes/img/help.png")));
					buttonPane.add(lblConfirmation);
				}
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("NO");
				cancelButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent arg0) {
						if(1==arg0.getButton()){
							setConfirmFlag(OPTION_NO);
							dialog.dispose();
						}
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

}
