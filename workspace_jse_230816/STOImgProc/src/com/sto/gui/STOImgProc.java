package com.sto.gui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;

import com.sto.data.ProductSpec;
import com.sto.data.SystemConfig;
import com.sto.utils.ImgProcUtils;
import com.sto.utils.LoginSystem;
import com.sto.utils.MyFileUtils;
import com.sto.utils.PCInfoUtils;
import com.sto.utils.VerUtils;
import com.sto.utils.XmlUtils;

import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.ImageIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.BevelBorder;
import java.awt.GridLayout;
import javax.swing.JLabel;

public class STOImgProc extends JFrame {
	private static final long serialVersionUID = 6456096044357861468L;
	private static final int PORTSVRSOCKET = 20000;
	private static ServerSocket svrSocket = null;
	private JPanel contentPane;
	private JButton btnRawImage;
	private JButton btnProcImage;
	private JButton btnStartProc;
	private JButton btnStopProc;
	private JProgressBar progressBar;
	private JLabel lblHeartbeat;
	private JLabel lblProcT;
	private JLabel lblNgQty;
	private JLabel lblOKQty;
	private JMenuItem mntmStopImageProc;
	private JMenuItem mntmSingleRunIn;
	private JMenuItem mntmStartImageProcessing;
	private JMenu mnStartImageProc;
	private JMenu mnRegistration;
	
	private SystemTray systemTray = null;
	private TrayIcon trayIcon = null;
	
	private SystemConfig sysConfig = SystemConfig.getInstance();
	private ProductSpec productSpec = ProductSpec.getInstance();
	private String specificDir = "";
	
	private static STOImgProc frame = new STOImgProc();
	
	private static void checkPreviousInstance(){
		try {
			svrSocket = new ServerSocket(PORTSVRSOCKET);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "The Image Processor is already started", "Program Launch", JOptionPane.INFORMATION_MESSAGE);
			System.exit(0);
		}
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		checkPreviousInstance();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static STOImgProc getInstance(){
		return frame;
	}
	
	/**
	 * Create the frame.
	 */
	private STOImgProc() {
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(STOImgProc.class.getResource("/com/sto/img/Company_36.png")));
		setTitle("STO X-Ray Image Processor ["+VerUtils.getVersion()+"]");
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				showSystemTray();
			}
		});
		
		int width = 900;
		int height = 600;
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16 == arg0.getModifiers() && mntmExit.isEnabled()){
					quitSystem();
				}
			}
		});
		mntmExit.setIcon(new ImageIcon(STOImgProc.class.getResource("/com/sto/img/Exit_16.png")));
		mnFile.add(mntmExit);
		
		JMenu mnConfigure = new JMenu("Configure");
		menuBar.add(mnConfigure);
		
		JMenuItem mntmSystemCfg = new JMenuItem("System Config");
		mntmSystemCfg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16 == arg0.getModifiers() && mntmSystemCfg.isEnabled()){
					showSystemCfgDialog();
				}
			}
		});
		mntmSystemCfg.setIcon(new ImageIcon(STOImgProc.class.getResource("/com/sto/img/setting_16.png")));
		mnConfigure.add(mntmSystemCfg);
		
		JMenuItem mntmCriteria = new JMenuItem("Product Spec");
		mntmCriteria.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16==arg0.getModifiers() && mntmCriteria.isEnabled()) showProductSpecDialog();
			}
		});
		mntmCriteria.setIcon(new ImageIcon(STOImgProc.class.getResource("/com/sto/img/standard_16.png")));
		mnConfigure.add(mntmCriteria);
		
		JMenu mnRun = new JMenu("Run");
		menuBar.add(mnRun);
		
		mntmStopImageProc = new JMenuItem("Stop Image Proc");
		mntmStopImageProc.setEnabled(false);
		mntmStopImageProc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16==arg0.getModifiers() && mntmStopImageProc.isEnabled()){
					ArrayList<String> runningDirs = ImgProcUtils.getRunningThreads();
					if(!runningDirs.isEmpty()){
						for(int i=0; i<runningDirs.size(); i++){
							ImgProcUtils.stopImgProcThread(runningDirs.get(i));
						}
					}
				}
			}
		});
		
		mnStartImageProc = new JMenu("Start Image Proc");
		mnStartImageProc.setIcon(new ImageIcon(STOImgProc.class.getResource("/com/sto/img/P1_16.png")));
		mnRun.add(mnStartImageProc);
		
		mntmStartImageProcessing = new JMenuItem("Start Image Processing");
		mntmStartImageProcessing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16==arg0.getModifiers() && mntmStartImageProcessing.isEnabled()){
					XmlUtils.parseSystemConfig();
					XmlUtils.parseProductSpec();
					
					String imgDirs = "";
					String[] imgProcDirs = productSpec.getConfigIDs();
                	for(int i=0; i<imgProcDirs.length; i++){
                		if("Debug Spec".equals(imgProcDirs[i])) continue;
                		if("".equals(imgDirs)){
                			imgDirs = imgProcDirs[i];
                		}else{
                			imgDirs += "\r\n" + imgProcDirs[i];
                		}
                	}
                	if("".equals(imgDirs)){
                		JOptionPane.showMessageDialog(contentPane, "There are no valid product spec yet", "No Valid Spec", JOptionPane.ERROR_MESSAGE);
                	}else{
	                	if(0==JOptionPane.showConfirmDialog(contentPane, "Are you sure of starting the realtime image processing in below path?\r\n"+imgDirs, "Start Image Processing", JOptionPane.YES_NO_OPTION)){
	                		showProcResultDialog(false);
	                		ImgProcUtils.startImgProcDemon(imgDirs.split("\r\n"),sysConfig.getImgProcQtyPerCycle(600));
	                	}
                	}
				}
			}
		});
		mntmStartImageProcessing.setIcon(new ImageIcon(STOImgProc.class.getResource("/com/sto/img/start_16.png")));
		mnStartImageProc.add(mntmStartImageProcessing);
		
		mntmSingleRunIn = new JMenuItem("Single Run In Specific Folder");
		mntmSingleRunIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16==arg0.getModifiers() && mntmSingleRunIn.isEnabled()){
					String imgProcDir = MyFileUtils.chooseFile(contentPane, "Select X-Ray Image Folder", "Select Folder", true);
	                if(!"".equals(imgProcDir)){
	                	ImgProcUtils.startImgProcThreads(new String[]{imgProcDir}, new String[]{"Debug Spec"}, false, 5000);
	                	specificDir = imgProcDir;
	                }
				}
			}
		});
		mntmSingleRunIn.setIcon(new ImageIcon(STOImgProc.class.getResource("/com/sto/img/P0_16.png")));
		mnStartImageProc.add(mntmSingleRunIn);
		mntmStopImageProc.setIcon(new ImageIcon(STOImgProc.class.getResource("/com/sto/img/stop_16.png")));
		mnRun.add(mntmStopImageProc);
		
		JMenuItem mntmShowImageProc = new JMenuItem("Show Image Proc Result");
		mntmShowImageProc.setIcon(new ImageIcon(STOImgProc.class.getResource("/com/sto/img/Statistic_16.png")));
		mntmShowImageProc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16==arg0.getModifiers() && mntmShowImageProc.isEnabled()) showProcResultDialog(true);
			}
		});
		mnRun.add(mntmShowImageProc);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16==arg0.getModifiers() && mntmAbout.isEnabled()) showAboutDialog();
			}
		});
		mntmAbout.setIcon(new ImageIcon(STOImgProc.class.getResource("/com/sto/img/about_16.png")));
		mnHelp.add(mntmAbout);
		
		mnRegistration = new JMenu("Registration");
		mnRegistration.setIcon(new ImageIcon(STOImgProc.class.getResource("/com/sto/img/Register_16.png")));
		mnHelp.add(mnRegistration);
		
		JMenuItem mntmImportLicenseFile = new JMenuItem("Import License File...");
		mntmImportLicenseFile.setIcon(new ImageIcon(STOImgProc.class.getResource("/com/sto/img/import_16.png")));
		mntmImportLicenseFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16==arg0.getModifiers() && mntmImportLicenseFile.isEnabled()){
					boolean ok = false;
					String path = MyFileUtils.chooseFile(contentPane, "Select License File", "Select File", false);
					if(!"".equals(path)){
						String targetPath = XmlUtils.getXmlFolder()+File.separator+"License.xml";
						if(MyFileUtils.deleteFile(targetPath)){
							try {
								ok = MyFileUtils.copyFile(path, targetPath);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						if(!ok){
							JOptionPane.showMessageDialog(contentPane, "Import license file failed, please try again", "Error", JOptionPane.INFORMATION_MESSAGE);
						}else{
							licenceCheck();
						}
					}
				}
			}
		});
		mnRegistration.add(mntmImportLicenseFile);
		
		JMenuItem mntmInputLicenseCode = new JMenuItem("Input License Code");
		mntmInputLicenseCode.setIcon(new ImageIcon(STOImgProc.class.getResource("/com/sto/img/input_validation_16.png")));
		mntmInputLicenseCode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(16==arg0.getModifiers() && mntmInputLicenseCode.isEnabled()){
					showRegistrationDialog();
				}
			}
		});
		mnRegistration.add(mntmInputLicenseCode);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		contentPane.add(toolBar, BorderLayout.NORTH);
		
		JButton btnExit = new JButton("");
		btnExit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(1==arg0.getButton() && btnExit.isEnabled()) quitSystem();
			}
		});
		btnExit.setBorderPainted(false);
		btnExit.setToolTipText("Exit");
		btnExit.setIcon(new ImageIcon(STOImgProc.class.getResource("/com/sto/img/Exit_24.png")));
		toolBar.add(btnExit);
		
		JButton btnSystemCfg = new JButton("");
		btnSystemCfg.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(1==arg0.getButton() && btnSystemCfg.isEnabled()) showSystemCfgDialog();
			}
		});
		btnSystemCfg.setBorderPainted(false);
		btnSystemCfg.setToolTipText("System Config");
		btnSystemCfg.setIcon(new ImageIcon(STOImgProc.class.getResource("/com/sto/img/setting_24.png")));
		toolBar.add(btnSystemCfg);
		
		JButton btnCriteria = new JButton("");
		btnCriteria.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(1==arg0.getButton() && btnCriteria.isEnabled()) showProductSpecDialog();
			}
		});
		btnCriteria.setBorderPainted(false);
		btnCriteria.setToolTipText("Product Spec");
		btnCriteria.setIcon(new ImageIcon(STOImgProc.class.getResource("/com/sto/img/standard_24.png")));
		toolBar.add(btnCriteria);
		
		btnStartProc = new JButton("");
		btnStartProc.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(1==arg0.getButton() && btnStartProc.isEnabled()){
					XmlUtils.parseSystemConfig();
					XmlUtils.parseProductSpec();
					showPopupMenu(arg0.getComponent(), arg0.getX(), arg0.getY());
				}
			}
		});
		
		JButton btnAbout = new JButton("");
		btnAbout.setToolTipText("About");
		btnAbout.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(1==arg0.getButton() && btnAbout.isEnabled()) showAboutDialog();
			}
		});
		btnAbout.setBorderPainted(false);
		btnAbout.setIcon(new ImageIcon(STOImgProc.class.getResource("/com/sto/img/about_24.png")));
		toolBar.add(btnAbout);
		
		JButton btnProcResult = new JButton("");
		btnProcResult.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(1==arg0.getButton() && btnProcResult.isEnabled()) showProcResultDialog(false);
			}
		});
		btnProcResult.setToolTipText("Show Image Process Result");
		btnProcResult.setBorderPainted(false);
		btnProcResult.setIcon(new ImageIcon(STOImgProc.class.getResource("/com/sto/img/Statistic_24.png")));
		toolBar.add(btnProcResult);
		
		btnStartProc.setBorderPainted(false);
		btnStartProc.setToolTipText("Start Image Processing");
		btnStartProc.setIcon(new ImageIcon(STOImgProc.class.getResource("/com/sto/img/start_24.png")));
		toolBar.add(btnStartProc);
		
		btnStopProc = new JButton("");
		btnStopProc.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(1==arg0.getButton() && btnStopProc.isEnabled()){
					ArrayList<String> runningDirs = ImgProcUtils.getRunningThreads();
					if(!runningDirs.isEmpty()){
						for(int i=0; i<runningDirs.size(); i++){
							ImgProcUtils.stopImgProcThread(runningDirs.get(i));
						}
					}
				}
			}
		});
		btnStopProc.setToolTipText("Stop Image Processing");
		btnStopProc.setEnabled(false);
		btnStopProc.setBorderPainted(false);
		btnStopProc.setIcon(new ImageIcon(STOImgProc.class.getResource("/com/sto/img/stop_24.png")));
		toolBar.add(btnStopProc);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		contentPane.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new GridLayout(1, 0, 0, 0));
		
		lblHeartbeat = new JLabel("Heartbeat");
		panel_1.add(lblHeartbeat);
		
		lblProcT = new JLabel("ProcT: 0 ms");
		panel_1.add(lblProcT);
		
		lblNgQty = new JLabel("NG: 0(0.0%)");
		panel_1.add(lblNgQty);
		
		lblOKQty = new JLabel("OK: 0(0.0%)");
		panel_1.add(lblOKQty);
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		panel_1.add(progressBar);
		
		JPanel panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(new GridLayout(1, 0, 0, 0));
		
		btnRawImage = new JButton("");
		btnRawImage.setBorderPainted(false);
		btnRawImage.setIcon(new ImageIcon(STOImgProc.class.getResource("/com/sto/img/Company_36.png")));
		panel.add(btnRawImage);
		
		btnProcImage = new JButton("");
		btnProcImage.setBorderPainted(false);
		btnProcImage.setIcon(new ImageIcon(STOImgProc.class.getResource("/com/sto/img/Company_36.png")));
		panel.add(btnProcImage);
		
		licenceCheck();
	}
	
	protected void setProgress(JProgressBar progBar, int val){
		if(null!=progBar) progBar.setValue(val);
	}
	
	protected void setLabelText(JLabel lblObj, String txt){
		if(null!=lblObj) lblObj.setText(txt);
	}
	
	protected void setLabelIcon(JLabel lblObj, String imgPath){
		if(null!=imgPath && !"".equals(imgPath.trim())){
			if(imgPath.startsWith("/com")){
				lblObj.setIcon(new ImageIcon(STOImgProc.class.getResource(imgPath)));
			}else{
				ImageIcon imageIcon = new ImageIcon(imgPath);
				Image image = imageIcon.getImage();
				Image smallImage = image.getScaledInstance(16,16,Image.SCALE_FAST);
				lblObj.setIcon(new ImageIcon(smallImage));
			}
		}else{
			lblObj.setIcon(null);
		}
	}
	
	protected void setButtonIcon(JButton btn, String imgPath){
		ImageIcon imageIcon = new ImageIcon(imgPath);
		Image image = imageIcon.getImage();
		Image smallImage = image.getScaledInstance(btn.getWidth()-2,btn.getHeight(),Image.SCALE_FAST);
		btn.setIcon(new ImageIcon(smallImage));
		btn.setToolTipText(imgPath);
	}
	
	protected void showSystemCfgDialog() {
		try {
			SysConfig dialog = SysConfig.getInstance();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void showProductSpecDialog() {
		try {
			ProdSpec dialog = ProdSpec.getInstance();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void showAboutDialog(){
		try {
			About dialog = About.getInstance();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void showProcResultDialog(boolean reloadRslts){
		try {
			ProcResult dialog = ProcResult.getInstance();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
			if(reloadRslts) dialog.refreshProcRslts();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void showRegistrationDialog(){
		try {
			Registration dialog = Registration.getInstance();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
			if(LoginSystem.userHasLoginned()) licenceCheck();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void showPopupMenu(Component invoker, int x, int y) {
		String[] imgProcDirs = productSpec.getConfigIDs();
		
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.removeAll();
        if(null!=imgProcDirs && imgProcDirs.length>0){
        	JMenuItem menuItem = new JMenuItem("Start Image Processing");
        	popupMenu.add(menuItem);
        	if(ImgProcUtils.imgProcDemonIsRunning()) menuItem.setEnabled(false);
        	menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                	if(16==e.getModifiers() && menuItem.isEnabled()){
	                	String imgDirs = "";
	                	for(int i=0; i<imgProcDirs.length; i++){
	                		if("Debug Spec".equals(imgProcDirs[i])) continue;
	                		if("".equals(imgDirs)){
	                			imgDirs = imgProcDirs[i];
	                		}else{
	                			imgDirs += "\r\n" + imgProcDirs[i];
	                		}
	                	}
	                	if("".equals(imgDirs)){
	                		JOptionPane.showMessageDialog(contentPane, "There are no valid product spec yet", "No Valid Spec", JOptionPane.ERROR_MESSAGE);
	                	}else{
		                	if(0==JOptionPane.showConfirmDialog(contentPane, "Are you sure of starting the realtime image processing in below path?\r\n"+imgDirs, "Start Image Processing", JOptionPane.YES_NO_OPTION)){
		                		showProcResultDialog(false);
		                		ImgProcUtils.startImgProcDemon(imgDirs.split("\r\n"),sysConfig.getImgProcQtyPerCycle(600));
		                	}
	                	}
                	}
                }
            });
        }
        
        if(null!=imgProcDirs && imgProcDirs.length>0){
        	for(int i=0; i<imgProcDirs.length; i++){
        		if("Debug Spec".equals(imgProcDirs[i])) continue;
	        	JMenuItem menuItem = new JMenuItem("Single run in: "+imgProcDirs[i]);
	        	popupMenu.add(menuItem);
	        	menuItem.setEnabled(!ImgProcUtils.imgProcThreadIsRunning(imgProcDirs[i]));
	        	menuItem.addActionListener(new MyActionListener(imgProcDirs[i]) {
	                @Override
	                public void actionPerformed(ActionEvent e) {
	                	ImgProcUtils.startImgProcThreads(new String[]{this.getImgProcDir()}, null, false, sysConfig.getImgProcQtyPerCycle(600));
	                }
	            });
        	}
        }
        
        JMenuItem menuItem = new JMenuItem("Single run in: Other Directory ...");
    	popupMenu.add(menuItem);
    	menuItem.setEnabled(!ImgProcUtils.imgProcThreadIsRunning(specificDir));
    	menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	String imgProcDir = MyFileUtils.chooseFile(invoker, "Select X-Ray Image Folder", "Select Folder", true);
                if(!"".equals(imgProcDir)){
                	ImgProcUtils.startImgProcThreads(new String[]{imgProcDir}, new String[]{"Debug Spec"}, false, 5000);
                	specificDir = imgProcDir;
                }
            }
        });
    	
        popupMenu.show(invoker, x, y);
    }
	
	protected void licenceCheck(){
		if(PCInfoUtils.licenceOK()){
			mnRegistration.setEnabled(false);
			btnStartProc.setEnabled(true);
			mnStartImageProc.setEnabled(true);
		}else{
			mnRegistration.setEnabled(true);
			btnStartProc.setEnabled(false);
			mnStartImageProc.setEnabled(false);
		}
	}
	
	public void refreshMonHeartbeat(int iconFlag, String imgProcDir){
		setLabelText(lblHeartbeat,imgProcDir);
		switch(iconFlag){
		case 0:
			setLabelIcon(lblHeartbeat,"/com/sto/img/trafficlight_green_16.png");
			break;
		case 1:
			setLabelIcon(lblHeartbeat,"/com/sto/img/trafficlight_red_16.png");
			break;
		case 2:
			setLabelIcon(lblHeartbeat,"/com/sto/img/trafficlight_red_16.png");
			break;
		}
	}
	
	public void setRunBtnEnabled(){
		boolean specificRunning = ImgProcUtils.imgProcThreadIsRunning(specificDir);
		ArrayList<String> runningThreads = ImgProcUtils.getRunningThreads();
		
		//Stop button and menus
		if(runningThreads.isEmpty()){
			btnStopProc.setEnabled(false);
			mntmStopImageProc.setEnabled(false);
		}else{
			btnStopProc.setEnabled(true);
			mntmStopImageProc.setEnabled(true);
		}
		mntmSingleRunIn.setEnabled(!specificRunning);
		
		//Start button and menus
		mntmStartImageProcessing.setEnabled(!ImgProcUtils.imgProcDemonIsRunning());
		if(runningThreads.isEmpty()
			|| !ImgProcUtils.imgProcDemonIsRunning()
			|| !specificRunning){
			mnStartImageProc.setEnabled(true);
			btnStartProc.setEnabled(true);
		}else{
			mnStartImageProc.setEnabled(false);
			btnStartProc.setEnabled(false);
			String[] imgProcDirs = productSpec.getConfigIDs();
			if(null!=imgProcDirs){
				for(int i=0; i<imgProcDirs.length; i++){
					if(!ImgProcUtils.imgProcThreadIsRunning(imgProcDirs[i])){
						btnStartProc.setEnabled(true);
						break;
					}
				}
			}
		}
	}
	
	public void setImgProcInfo(LinkedHashMap<String,Object> procResult, boolean bStart){
		setLabelText(lblHeartbeat,""+procResult.get("imgProcDir"));
		if(bStart){
			setButtonIcon(btnRawImage,procResult.get("imgProcDir")+File.separator+procResult.get("imgProcSourceName"));
		}else{
			setProgress(progressBar,(int)procResult.get("imgProcProgress"));
			setLabelText(lblProcT,""+procResult.get("imgProcTime"));
			setLabelText(lblNgQty,""+procResult.get("imgProcNGInfo"));
			setLabelText(lblOKQty,""+procResult.get("imgProcOKInfo"));
			setButtonIcon(btnProcImage,procResult.get("imgProcDir")+File.separator+procResult.get("imgProcRsltName"));
		}
	}
	
	private void showSystemTray(){
		if(SystemTray.isSupported()){
			if(null == systemTray) systemTray = SystemTray.getSystemTray();
			if(null != trayIcon) systemTray.remove(trayIcon);
			
			PopupMenu popup = new PopupMenu();
			MenuItem mainMenuItem = new MenuItem("Show Main GUI");
			mainMenuItem.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(true);
				}
			});
			popup.add(mainMenuItem);
			
			trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(STOImgProc.class.getResource("/com/sto/img/Company_36.png")),"STO X-Ray Image Processor",popup);
			trayIcon.setImageAutoSize(true);
			trayIcon.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(true);
				}
			});
			
			try {
				systemTray.add(trayIcon);
			} catch (AWTException e1) {
			}
		}
	}
	
	private void quitSystem() {
		int rtn = JOptionPane.showConfirmDialog(contentPane, "Are you sure of quiting from the Image Processor?", "Exit", JOptionPane.YES_NO_OPTION);
		if(0 == rtn){
			if(null != svrSocket){
				try {
					svrSocket.close();
				} catch (IOException e1) {
				}
			}
			System.exit(0);
		}
	}
	
	abstract class MyActionListener implements ActionListener{
		private String imgProcDir = "";
		public MyActionListener(String imgPath){
			imgProcDir = imgPath;
		}
		public String getImgProcDir(){
			return imgProcDir;
		}
	}
}
