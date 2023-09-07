package com.cncmes.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import com.cncmes.base.CncItems;
import com.cncmes.base.DeviceState;
import com.cncmes.data.CncData;

public class PaintCNCLines {
	private static PaintCNCLines cncLines = new PaintCNCLines();
	private static CncData cncData = CncData.getInstance();
	private Map<String,LinkedHashMap<String,Object>> lineMap = new LinkedHashMap<String,LinkedHashMap<String,Object>>();
	private PaintCNCLines(){}
	
	public static PaintCNCLines getInstance(){
		return cncLines;
	}
	
	public void paintCNCLines(JPanel panelCNCLine, String[] IPs){
		panelCNCLine.removeAll();
		if(!lineMap.isEmpty()) lineMap.clear();
		
		if(null == IPs){
			panelCNCLine.updateUI();
			return;
		}
		
		for(int i=0; i<IPs.length; i++){
			LinkedHashMap<String,Object> cnc = new LinkedHashMap<String,Object>();
			
			JPanel panel_CNC = new JPanel();
			panel_CNC.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
			panelCNCLine.add(panel_CNC);
			panel_CNC.setLayout(new BorderLayout(0, 0));
			
			JLabel label_CNC = new JLabel(IPs[i]);
			label_CNC.setHorizontalAlignment(SwingConstants.CENTER);
			label_CNC.setIcon(new ImageIcon(UserLogin.class.getResource("/com/cncmes/img/3d_printer_24.png")));
			panel_CNC.add(label_CNC, BorderLayout.NORTH);
			
			JTextPane textPane_CNC = new JTextPane();
			DeviceState devState = cncData.getCncState(IPs[i]);
			if(null != devState){
				textPane_CNC.setBackground(devState.getColor());
			}
			textPane_CNC.setEditable(false);
			JScrollPane jsp = new JScrollPane(textPane_CNC);
			panel_CNC.add(jsp, BorderLayout.CENTER);
			
			JProgressBar progressBar_CNC = new JProgressBar();
			progressBar_CNC.setMinimum(0);
			progressBar_CNC.setMaximum(100);
			progressBar_CNC.setStringPainted(true);
			progressBar_CNC.setForeground(Color.CYAN);
			panel_CNC.add(progressBar_CNC, BorderLayout.SOUTH);
			
			cnc.put("label_CNC", label_CNC);
			cnc.put("textPane_CNC", textPane_CNC);
			cnc.put("progressBar_CNC", progressBar_CNC);
			lineMap.put(IPs[i], cnc);
			
			String text = cncData.getCncDataString(IPs[i]);
			if(null != text) repaintCNC(IPs[i],text);
		}
		
		panelCNCLine.updateUI();
	}
	
	public void repaintCNC(String ip, String text){
		LinkedHashMap<String,Object> cnc = lineMap.get(ip);
		
		if(null != cnc){
			try {
				JLabel label_CNC = (JLabel) cnc.get("label_CNC");
				JTextPane textPane_CNC = (JTextPane) cnc.get("textPane_CNC");
				JProgressBar progressBar_CNC = (JProgressBar) cnc.get("progressBar_CNC");
				
				DeviceState devState = cncData.getCncState(ip);
				String finishShow = (String) cncData.getData(ip).get(CncItems.FINISHSHOW);
				String cncModel = (String) cncData.getData(ip).get(CncItems.MODEL);
				
				if(null != devState) textPane_CNC.setBackground(devState.getColor());
				label_CNC.setText(ip+"-"+cncModel);
				if(null!=finishShow && !"".equals(finishShow)){
					String stateDesc = "";
					if(DeviceState.WORKING == devState){
						stateDesc = "status: Do Unloading";
					}else{
						stateDesc = "status: Wait Unloading";
					}
					String[] finishText = finishShow.split("\r\n");
					String[] currText = text.split("\r\n");
					String temp = "";
					for(int i=0; i<finishText.length; i++){
						if(finishText[i].indexOf("status:") >= 0) finishText[i] = stateDesc;
						if(finishText[i].indexOf("cmd:") >= 0){
							for(int j=0; j<currText.length; j++){
								if(currText[j].indexOf("cmd:") >= 0){
									finishText[i] = currText[j];
									break;
								}
							}
						}
						if(finishText[i].indexOf("robot:") >= 0){
							for(int j=0; j<currText.length; j++){
								if(currText[j].indexOf("robot:") >= 0){
									finishText[i] = currText[j];
									break;
								}
							}
						}
						if(finishText[i].indexOf("machineT:") >= 0){
							for(int j=0; j<currText.length; j++){
								if(currText[j].indexOf("machineT:") >= 0){
									finishText[i] = currText[j];
									break;
								}
							}
						}
						if(i < (finishText.length-1)){
							temp += finishText[i] + "\r\n";
						}else{
							temp += finishText[i];
						}
					}
					textPane_CNC.setText(temp);
				}else{
					textPane_CNC.setText(text);
				}
				if(DeviceState.STANDBY == devState){
					progressBar_CNC.setValue(0);
				}else{
					progressBar_CNC.setValue(cncData.getMachiningProgress(ip));
				}
			} catch (Exception e) {
			}
		}
	}
}
