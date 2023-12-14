package com.sto.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.sto.data.ProductSpec;
import com.sto.utils.ImgProcUtils;
import com.sto.utils.MyFileUtils;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.LinkedHashMap;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ProcResult extends JDialog {
	private static final long serialVersionUID = 8460910500963269650L;
	private final JPanel contentPanel = new JPanel();
	private static ProcResult procResult = new ProcResult();
	
	private JTextArea txtrProcResult;
	
	private LinkedHashMap<String,String> procRslts = new LinkedHashMap<String,String>();
	
	public static ProcResult getInstance(){
		return procResult;
	}
	
	/**
	 * Create the dialog.
	 */
	private ProcResult() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(ProcResult.class.getResource("/com/sto/img/Statistic_24.png")));
		setResizable(false);
		setTitle("Image Process Result Summary");
		
		int width = 360;
		int height = 480;
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(d.width-width, 0, width, height);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane, BorderLayout.CENTER);
			{
				txtrProcResult = new JTextArea();
				txtrProcResult.setEditable(false);
				scrollPane.setViewportView(txtrProcResult);
			}
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
						if(1==arg0.getButton() && okButton.isEnabled()) procResult.dispose();
					}
				});
				{
					JButton btnBrowseDir = new JButton("Browse Dir...");
					btnBrowseDir.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent arg0) {
							if(1==arg0.getButton() && btnBrowseDir.isEnabled()){
								String imgProcDir = MyFileUtils.chooseFile(contentPanel, "Select X-Ray image process local path", "Select Root Folder", true);
				                if(!"".equals(imgProcDir)){
				                	refreshProcResult(imgProcDir, ImgProcUtils.summarizeImgProcResult(imgProcDir));
				                }
							}
						}
					});
					buttonPane.add(btnBrowseDir);
				}
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
		
		refreshProcRslts();
	}
	
	public void clearProcResult(){
		procRslts.clear();
	}
	
	public void refreshProcRslts(){
		String[] dirs = ProductSpec.getInstance().getConfigIDs();
		if(null!=dirs && dirs.length>0){
			Arrays.sort(dirs);
			for(int i=0; i<dirs.length; i++){
				if("Debug Spec".equals(dirs[i])) continue;
				refreshProcResult(dirs[i], ImgProcUtils.summarizeImgProcResult(dirs[i]));
			}
		}
	}
	
	public void refreshProcResult(String imgProcDir, String rslt){
		procRslts.put(imgProcDir, rslt);
		
		try {
			rslt = "";
			for(String key:procRslts.keySet()){
				if("".equals(rslt)){
					rslt = procRslts.get(key);
				}else{
					rslt += "\r\n" + procRslts.get(key);
				}
			}
		} catch (Exception e) {
		}
		
		txtrProcResult.setText(rslt);
	}
}
