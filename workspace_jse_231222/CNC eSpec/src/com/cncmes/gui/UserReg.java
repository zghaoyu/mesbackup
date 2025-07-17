package com.cncmes.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.cncmes.utils.DTOUtils;
import com.cncmes.utils.DesUtils;
import com.cncmes.utils.GUIUtils;
import com.cncmes.utils.MathUtils;

import java.awt.Toolkit;
import java.awt.GridBagLayout;
import javax.swing.JToolBar;
import javax.swing.ListModel;

import java.awt.GridBagConstraints;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import java.awt.Insets;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.AbstractListModel;
import javax.swing.border.BevelBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

public class UserReg extends JDialog {
	private static final long serialVersionUID = -7400965626212603438L;
	private static String gDtoClassName = "com.cncmes.dto.CNCUsers";
	private final JPanel contentPanel = new JPanel();
	private static UserReg userReg = new UserReg();
	private JTextField txtUserName;
	private JTextField txtWorkId;
	private JTextField txtExpiredDays;
	private JList<String> listUsers;
	private JList<String> listAllRights;
	private JList<String> listAuthorities;
	private JButton btnSave;
	private JButton btnSearch;
	private JButton btnAdd;
	private JButton btnDel;
	private JLabel lblRunningMsg;
	
	private String curID = "";
	private String[] dataTitles = null, dtAuthorities = null;
	private Object[][] dataVals = null;
	
	public static UserReg getInstance(){
		return userReg;
	}
	
	/**
	 * Create the dialog.
	 */
	private UserReg() {
		getAllUsers();
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				String title = userReg.getTitle();
				String id = "";
				if(null!=title && title.indexOf("##")>=0){
					if(title.endsWith("##")){
						id="0";
					}else{
						id=title.split("##")[1];
					}
				}
				
				if(!"".equals(id)){
					curID = id;
					if("0".equals(id)) curID = "";
					refreshGUI(curID);
				}
			}
			@Override
			public void windowDeactivated(WindowEvent e) {
				String title = userReg.getTitle();
				if(null!=title && title.indexOf("##")>=0){
					title=title.split("##")[0];
					userReg.setTitle(title);
				}
			}
		});
		setIconImage(Toolkit.getDefaultToolkit().getImage(UserReg.class.getResource("/com/cncmes/img/user_24.png")));
		setModal(true);
		setTitle("User Registration");
		
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 600;
		int height = 400;
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panelTop = new JPanel();
			contentPanel.add(panelTop, BorderLayout.NORTH);
			GridBagLayout gbl_panelTop = new GridBagLayout();
			gbl_panelTop.columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0};
			gbl_panelTop.rowHeights = new int[] {28};
			gbl_panelTop.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
			gbl_panelTop.rowWeights = new double[]{0.0};
			panelTop.setLayout(gbl_panelTop);
			{
				JToolBar toolBar = new JToolBar();
				toolBar.setFloatable(false);
				GridBagConstraints gbc_toolBar = new GridBagConstraints();
				gbc_toolBar.insets = new Insets(0, 0, 0, 5);
				gbc_toolBar.gridx = 0;
				gbc_toolBar.gridy = 0;
				panelTop.add(toolBar, gbc_toolBar);
				{
					btnSave = new JButton("");
					btnSave.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if(btnSave.isEnabled() && 1==e.getButton()){
								btnSave.setEnabled(false);
								if(dataIsOkToSave()){
									try {
										DTOUtils.saveDataIntoDB(gDtoClassName, dataTitles, packCurrentData());
										getAllUsers();
										GUIUtils.setJListContent(listUsers, getUserNames());
										GUIUtils.setLabelText(lblRunningMsg, "Save User info OK");
									} catch (SQLException e1) {
										GUIUtils.setLabelText(lblRunningMsg, e1.getMessage());
										JOptionPane.showMessageDialog(contentPanel, "Save User failed:"+e1.getMessage(), "Save User Failed", JOptionPane.ERROR_MESSAGE);
									}
								}else{
									JOptionPane.showMessageDialog(contentPanel, "Save User failed:"+lblRunningMsg.getText(), "Save User Failed", JOptionPane.ERROR_MESSAGE);
								}
								btnSave.setEnabled(true);
							}
						}
					});
					btnSave.setToolTipText("Save User Info");
					btnSave.setBorderPainted(false);
					btnSave.setIcon(new ImageIcon(UserReg.class.getResource("/com/cncmes/img/Save_24.png")));
					toolBar.add(btnSave);
				}
				{
					JButton btnExit = new JButton("");
					btnExit.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if(btnExit.isEnabled() && 1==e.getButton()){
								userReg.dispose();
							}
						}
					});
					btnExit.setToolTipText("Exit");
					btnExit.setBorderPainted(false);
					btnExit.setIcon(new ImageIcon(UserReg.class.getResource("/com/cncmes/img/Exit_24.png")));
					toolBar.add(btnExit);
				}
			}
			{
				JLabel lblUserName = new JLabel("User Name");
				GridBagConstraints gbc_lblUserName = new GridBagConstraints();
				gbc_lblUserName.anchor = GridBagConstraints.EAST;
				gbc_lblUserName.insets = new Insets(0, 0, 0, 5);
				gbc_lblUserName.gridx = 1;
				gbc_lblUserName.gridy = 0;
				panelTop.add(lblUserName, gbc_lblUserName);
			}
			{
				txtUserName = new JTextField();
				GridBagConstraints gbc_txtUserName = new GridBagConstraints();
				gbc_txtUserName.insets = new Insets(0, 0, 0, 5);
				gbc_txtUserName.fill = GridBagConstraints.HORIZONTAL;
				gbc_txtUserName.gridx = 2;
				gbc_txtUserName.gridy = 0;
				panelTop.add(txtUserName, gbc_txtUserName);
				txtUserName.setColumns(10);
			}
			{
				btnSearch = new JButton("Search / Add");
				btnSearch.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if(btnSearch.isEnabled() && 1==e.getButton()){
							String userName = txtUserName.getText().trim();
							if("".equals(userName)){
								GUIUtils.setLabelText(lblRunningMsg,"Please input the User Name before doing search");
								return;
							}
							
							curID = "";
							if(null!=dataVals && dataVals.length>0){
								for(int i=0; i<dataVals.length; i++){
									if(userName.equals(""+dataVals[i][1])){
										curID = ""+dataVals[i][0];
										break;
									}
								}
							}
							
							refreshGUI(curID);
						}
					}
				});
				GridBagConstraints gbc_btnSearch = new GridBagConstraints();
				gbc_btnSearch.gridx = 3;
				gbc_btnSearch.gridy = 0;
				panelTop.add(btnSearch, gbc_btnSearch);
			}
		}
		{
			JPanel panelLeft = new JPanel();
			panelLeft.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
			contentPanel.add(panelLeft, BorderLayout.WEST);
			GridBagLayout gbl_panelLeft = new GridBagLayout();
			gbl_panelLeft.columnWidths = new int[] {120};
			gbl_panelLeft.rowHeights = new int[]{0, 0, 0};
			gbl_panelLeft.columnWeights = new double[]{1.0};
			gbl_panelLeft.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			panelLeft.setLayout(gbl_panelLeft);
			{
				JLabel lblAllUsers = new JLabel("All Users");
				GridBagConstraints gbc_lblAllUsers = new GridBagConstraints();
				gbc_lblAllUsers.insets = new Insets(0, 0, 5, 0);
				gbc_lblAllUsers.gridx = 0;
				gbc_lblAllUsers.gridy = 0;
				panelLeft.add(lblAllUsers, gbc_lblAllUsers);
			}
			{
				JScrollPane scrollPaneUsers = new JScrollPane();
				GridBagConstraints gbc_scrollPaneUsers = new GridBagConstraints();
				gbc_scrollPaneUsers.fill = GridBagConstraints.BOTH;
				gbc_scrollPaneUsers.gridx = 0;
				gbc_scrollPaneUsers.gridy = 1;
				panelLeft.add(scrollPaneUsers, gbc_scrollPaneUsers);
				{
					listUsers = new JList<String>();
					listUsers.addListSelectionListener(new ListSelectionListener() {
						public void valueChanged(ListSelectionEvent e) {
							int selIdx = listUsers.getSelectedIndex();
							if(selIdx>=0){
								curID = ""+dataVals[selIdx][0];
								refreshGUI(curID);
								refreshTitle();
							}
						}
					});
					listUsers.setModel(new AbstractListModel<String>() {
						private static final long serialVersionUID = 5324520823960474820L;
						String[] values = new String[] {"Operator", "System Engineer", "Maintenance Engineer", "NC Programmer", "Administrator", "Zi Long Li"};
						public int getSize() {
							return values.length;
						}
						public String getElementAt(int index) {
							return values[index];
						}
					});
					scrollPaneUsers.setViewportView(listUsers);
				}
			}
		}
		{
			JPanel panelCenter = new JPanel();
			panelCenter.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
			contentPanel.add(panelCenter, BorderLayout.CENTER);
			GridBagLayout gbl_panelCenter = new GridBagLayout();
			gbl_panelCenter.columnWidths = new int[] {80, 400};
			gbl_panelCenter.rowHeights = new int[]{0, 0, 0, 0};
			gbl_panelCenter.columnWeights = new double[]{0.0, 1.0};
			gbl_panelCenter.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
			panelCenter.setLayout(gbl_panelCenter);
			{
				JLabel lblWorkId = new JLabel("Work ID");
				GridBagConstraints gbc_lblWorkId = new GridBagConstraints();
				gbc_lblWorkId.insets = new Insets(0, 0, 5, 5);
				gbc_lblWorkId.gridx = 0;
				gbc_lblWorkId.gridy = 0;
				panelCenter.add(lblWorkId, gbc_lblWorkId);
			}
			{
				txtWorkId = new JTextField();
				txtWorkId.setText("W000055");
				GridBagConstraints gbc_txtWorkId = new GridBagConstraints();
				gbc_txtWorkId.insets = new Insets(0, 0, 5, 0);
				gbc_txtWorkId.fill = GridBagConstraints.HORIZONTAL;
				gbc_txtWorkId.gridx = 1;
				gbc_txtWorkId.gridy = 0;
				panelCenter.add(txtWorkId, gbc_txtWorkId);
				txtWorkId.setColumns(10);
			}
			{
				JLabel lblExpireddays = new JLabel("Expired Date");
				GridBagConstraints gbc_lblExpireddays = new GridBagConstraints();
				gbc_lblExpireddays.insets = new Insets(0, 0, 5, 5);
				gbc_lblExpireddays.gridx = 0;
				gbc_lblExpireddays.gridy = 1;
				panelCenter.add(lblExpireddays, gbc_lblExpireddays);
			}
			{
				txtExpiredDays = new JTextField();
				txtExpiredDays.setToolTipText("20180102 means 02-Jan-2018");
				txtExpiredDays.setText("20280102");
				GridBagConstraints gbc_txtExpiredDays = new GridBagConstraints();
				gbc_txtExpiredDays.insets = new Insets(0, 0, 5, 0);
				gbc_txtExpiredDays.fill = GridBagConstraints.HORIZONTAL;
				gbc_txtExpiredDays.gridx = 1;
				gbc_txtExpiredDays.gridy = 1;
				panelCenter.add(txtExpiredDays, gbc_txtExpiredDays);
				txtExpiredDays.setColumns(10);
			}
			{
				JLabel lblAuthorities = new JLabel("Authorities");
				GridBagConstraints gbc_lblAuthorities = new GridBagConstraints();
				gbc_lblAuthorities.insets = new Insets(0, 0, 0, 5);
				gbc_lblAuthorities.gridx = 0;
				gbc_lblAuthorities.gridy = 2;
				panelCenter.add(lblAuthorities, gbc_lblAuthorities);
			}
			{
				JPanel panelAuthorities = new JPanel();
				GridBagConstraints gbc_panelAuthorities = new GridBagConstraints();
				gbc_panelAuthorities.fill = GridBagConstraints.BOTH;
				gbc_panelAuthorities.gridx = 1;
				gbc_panelAuthorities.gridy = 2;
				panelCenter.add(panelAuthorities, gbc_panelAuthorities);
				GridBagLayout gbl_panelAuthorities = new GridBagLayout();
				gbl_panelAuthorities.columnWidths = new int[] {160, 80, 160};
				gbl_panelAuthorities.rowHeights = new int[] {0, 0};
				gbl_panelAuthorities.columnWeights = new double[]{1.0, 1.0, 1.0};
				gbl_panelAuthorities.rowWeights = new double[]{0.0, 1.0};
				panelAuthorities.setLayout(gbl_panelAuthorities);
				{
					JLabel lblAll = new JLabel("All");
					GridBagConstraints gbc_lblAll = new GridBagConstraints();
					gbc_lblAll.insets = new Insets(0, 0, 5, 5);
					gbc_lblAll.gridx = 0;
					gbc_lblAll.gridy = 0;
					panelAuthorities.add(lblAll, gbc_lblAll);
				}
				{
					JLabel lblGranted = new JLabel("Granted");
					GridBagConstraints gbc_lblGranted = new GridBagConstraints();
					gbc_lblGranted.insets = new Insets(0, 0, 5, 0);
					gbc_lblGranted.gridx = 2;
					gbc_lblGranted.gridy = 0;
					panelAuthorities.add(lblGranted, gbc_lblGranted);
				}
				{
					JScrollPane scrollPane = new JScrollPane();
					GridBagConstraints gbc_scrollPane = new GridBagConstraints();
					gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
					gbc_scrollPane.fill = GridBagConstraints.BOTH;
					gbc_scrollPane.gridx = 0;
					gbc_scrollPane.gridy = 1;
					panelAuthorities.add(scrollPane, gbc_scrollPane);
					{
						listAllRights = new JList<String>();
						listAllRights.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e) {
								if(2==e.getClickCount()) addAuthority();
							}
						});
						listAllRights.setModel(new AbstractListModel<String>() {
							private static final long serialVersionUID = 5324520823960474820L;
							String[] values = new String[] {"01. DB WRITE", "02. Device Monitoring", "03. Task Handling", "04. Rack Manager", "05. Scheduler", "06. System Config", "07. Device Controller", "08. Upload NC Program", "09. User Register"};
							public int getSize() {
								return values.length;
							}
							public String getElementAt(int index) {
								return values[index];
							}
						});
						scrollPane.setViewportView(listAllRights);
					}
				}
				{
					JPanel panelOperation = new JPanel();
					GridBagConstraints gbc_panelOperation = new GridBagConstraints();
					gbc_panelOperation.insets = new Insets(0, 0, 0, 5);
					gbc_panelOperation.fill = GridBagConstraints.BOTH;
					gbc_panelOperation.gridx = 1;
					gbc_panelOperation.gridy = 1;
					panelAuthorities.add(panelOperation, gbc_panelOperation);
					GridBagLayout gbl_panelOperation = new GridBagLayout();
					gbl_panelOperation.columnWidths = new int[] {0};
					gbl_panelOperation.rowHeights = new int[] {0, 0};
					gbl_panelOperation.columnWeights = new double[]{0.0};
					gbl_panelOperation.rowWeights = new double[]{0.0, 0.0};
					panelOperation.setLayout(gbl_panelOperation);
					{
						btnAdd = new JButton(">>");
						btnAdd.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e) {
								if(btnAdd.isEnabled() && 1==e.getButton()) addAuthority();
							}
						});
						btnAdd.setToolTipText("Add");
						GridBagConstraints gbc_btnAdd = new GridBagConstraints();
						gbc_btnAdd.insets = new Insets(0, 0, 5, 0);
						gbc_btnAdd.gridx = 0;
						gbc_btnAdd.gridy = 0;
						panelOperation.add(btnAdd, gbc_btnAdd);
					}
					{
						btnDel = new JButton("<<");
						btnDel.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e) {
								if(btnDel.isEnabled() && 1==e.getButton()) delAuthority();
							}
						});
						btnDel.setToolTipText("Delete");
						GridBagConstraints gbc_btnDel = new GridBagConstraints();
						gbc_btnDel.gridx = 0;
						gbc_btnDel.gridy = 1;
						panelOperation.add(btnDel, gbc_btnDel);
					}
				}
				{
					JScrollPane scrollPane = new JScrollPane();
					GridBagConstraints gbc_scrollPane = new GridBagConstraints();
					gbc_scrollPane.fill = GridBagConstraints.BOTH;
					gbc_scrollPane.gridx = 2;
					gbc_scrollPane.gridy = 1;
					panelAuthorities.add(scrollPane, gbc_scrollPane);
					{
						listAuthorities = new JList<String>();
						listAuthorities.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e) {
								if(2==e.getClickCount()) delAuthority();
							}
						});
						listAuthorities.setModel(new AbstractListModel<String>() {
							private static final long serialVersionUID = 8233067172290127885L;
							String[] values = new String[] {"01. DB WRITE", "02. Device Monitoring", "03. Task Handling", "04. Rack Manager", "05. Scheduler", "06. System Config", "07. Device Controller", "08. Upload NC Program", "09. User Register"};
							public int getSize() {
								return values.length;
							}
							public String getElementAt(int index) {
								return values[index];
							}
						});
						scrollPane.setViewportView(listAuthorities);
					}
				}
			}
		}
		{
			JPanel panelBottom = new JPanel();
			contentPanel.add(panelBottom, BorderLayout.SOUTH);
			{
				lblRunningMsg = new JLabel("Ready");
				panelBottom.add(lblRunningMsg);
			}
		}
		
		GUIUtils.setJListContent(listUsers, getUserNames());
		GUIUtils.setJListContent(listAllRights, getAuthorities());
	}

	private void getAllUsers() {
		ArrayList<Object> allUsers = DTOUtils.getDataFromDB(gDtoClassName, null, null);
		if(null!=allUsers && allUsers.size()>0){
			dataTitles = (String[]) allUsers.get(0);
			dataVals = (Object[][]) allUsers.get(1);
		}
	}
	
	private String[] getAuthorities() {
		String[] authors = new String[]{""};
		Object[][] data = null;
		String str = "";
		
		ArrayList<Object> authorities = DTOUtils.getDataFromDB("com.cncmes.dto.CNCUserAuthorities", null, null);
		if(null!=authorities && authorities.size()>0){
			data = (Object[][]) authorities.get(1);
			
			if(null!=data && data[0].length>2){
				for(int i=0; i<data.length; i++){
					if("".equals(str)){
						str = "" + data[i][1]+":"+data[i][2];
					}else{
						str += "," + data[i][1]+":"+data[i][2];
					}
				}
			}
		}
		if(!"".equals(str)){
			authors = str.split(",");
			dtAuthorities = str.split(",");
		}
		
		return authors;
	}
	
	private String[] parseAuthorities(String authorities){
		String[] authors = new String[]{""};
		String str = "";
		
		char[] dt = authorities.toCharArray();
		if(null!=dtAuthorities && dtAuthorities.length>0){
			for(int i=0; i<dt.length; i++){
				if("1".equals(""+dt[i]) && i<dtAuthorities.length){
					if("".equals(str)){
						str = dtAuthorities[i];
					}else{
						str += "," + dtAuthorities[i];
					}
				}
			}
		}
		if(!"".equals(str)) authors = str.split(",");
		
		return authors;
	}
	
	private String[] getUserNames(){
		String[] userNames = new String[]{""};
		String names = "";
		int col = -1;
		
		if(null!=dataTitles && null!=dataVals){
			for(int i=0; i<dataTitles.length; i++){
				if("user_name".equals(dataTitles[i])){
					col = i;
					break;
				}
			}
			if(col>=0){
				for(int i=0; i<dataVals.length; i++){
					if("".equals(names)){
						names = ""+dataVals[i][col];
					}else{
						names += ","+dataVals[i][col];
					}
				}
			}
		}
		if(!"".equals(names)) userNames = names.split(",");
		
		return userNames;
	}
	
	private void addAuthority(){
		btnAdd.setEnabled(false);
		
		int selIdx = listAllRights.getSelectedIndex();
		if(selIdx>=0){
			String selVal = listAllRights.getSelectedValue();
			ListModel<String> listModel = listAuthorities.getModel();
			int targetSize = listModel.getSize();
			boolean bFound = false;
			String newVals = "";
			
			if(!"".equals(selVal) && targetSize>0){
				for(int i=0; i<targetSize; i++){
					if(selVal.equals(listModel.getElementAt(i))) bFound = true;
					if(!"".equals(listModel.getElementAt(i))){
						if("".equals(newVals)){
							newVals = listModel.getElementAt(i);
						}else{
							newVals += "," + listModel.getElementAt(i);
						}
					}
				}
				if(!bFound){
					if("".equals(newVals)){
						newVals = selVal;
					}else{
						newVals += "," + selVal;
					}
				}
			}
			
			if(!"".equals(newVals)){
				String[] vals = newVals.split(",");
				Arrays.sort(vals);
				GUIUtils.setJListContent(listAuthorities, vals);
			}
		}
		
		btnAdd.setEnabled(true);
	}
	
	private void delAuthority(){
		btnDel.setEnabled(false);
		
		int selIdx = listAuthorities.getSelectedIndex();
		if(selIdx>=0){
			String newVals = "";
			ListModel<String> listModel = listAuthorities.getModel();
			for(int i=0; i<listModel.getSize(); i++){
				if(selIdx!=i){
					if("".equals(newVals)){
						newVals = listModel.getElementAt(i);
					}else{
						newVals += "," + listModel.getElementAt(i);
					}
				}
			}
			GUIUtils.setJListContent(listAuthorities, newVals.split(","));
		}
		
		btnDel.setEnabled(true);
	}
	
	private Object[][] packCurrentData(){
		Object[][] curData = null;
		int rowID = -1;
		
		if(null!=dataTitles && dataTitles.length>0){
			curData = new Object[1][dataTitles.length];
			for(int i=0; i<dataVals.length; i++){
				if(curID.equals(""+dataVals[i][0])){
					rowID = i;
					break;
				}
			}
			for(int i=0; i<dataTitles.length; i++){
				switch(dataTitles[i]){
				case "id":
					if(rowID>=0) curData[0][i] = Integer.parseInt(curID);
					break;
				case "user_name":
					curData[0][i] = txtUserName.getText().trim();
					break;
				case "user_pwd":
					if(rowID<0){
						curData[0][i] = MathUtils.MD5Encode(txtWorkId.getText().trim()+"STO");
					}else{
						curData[0][i] = dataVals[rowID][i];
					}
					break;
				case "user_authorities":
					String authors = "";
					try {
						authors = DesUtils.encrypt(getAuthorityStr());
					} catch (Exception e) {
					}
					curData[0][i] = authors;
					break;
				case "user_workid":
					curData[0][i] = txtWorkId.getText().trim();
					break;
				case "user_expirydate":
					curData[0][i] = txtExpiredDays.getText().trim();
					break;
				}
			}
		}
		
		return curData;
	}
	
	private boolean dataIsOkToSave(){
		if("".equals(txtUserName.getText().trim())){
			GUIUtils.setLabelText(lblRunningMsg, "User Name can't be blank");
			return false;
		}
		
		if("".equals(txtWorkId.getText().trim())){
			GUIUtils.setLabelText(lblRunningMsg, "Work ID can't be blank");
			return false;
		}
		
		if("".equals(txtExpiredDays.getText().trim())){
			GUIUtils.setLabelText(lblRunningMsg, "Expire Date can't be blank");
			return false;
		}
		
		boolean authorsNull = true;
		ListModel<String> listModel = listAuthorities.getModel();
		for(int i=0; i<listModel.getSize(); i++){
			if(!"".equals(listModel.getElementAt(i).trim())){
				authorsNull = false;
				break;
			}
		}
		if(authorsNull){
			GUIUtils.setLabelText(lblRunningMsg, "Granted Authorities can't be blank");
			return false;
		}
		
		return true;
	}
	
	private String getAuthorityStr(){
		String str = "", val = "";
		boolean bFound = false;
		
		ListModel<String> listModel = listAuthorities.getModel();
		for(int j=1; j<=16; j++){
			bFound = false;
			for(int i=0; i<listModel.getSize(); i++){
				val = listModel.getElementAt(i).split(":")[0];
				if(val.equals(""+j)){
					bFound = true;
					break;
				}
			}
			if("".equals(str)){
				str = bFound?"1":"0";
			}else{
				str += bFound?"1":"0";
			}
		}
		return str;
	}
	
	private void clearValues(){
		txtWorkId.setText("");
		txtExpiredDays.setText("");
		GUIUtils.setJListContent(listAuthorities, new String[]{""});
	}
	
	private void refreshTitle(){
		String title = userReg.getTitle();
		if(null!=title && !"".equals(curID)){
			if(title.indexOf("##")>=0){
				if(title.endsWith("##")){
					userReg.setTitle(title+curID);
				}else{
					userReg.setTitle(title.split("##")[0]+"##"+curID);
				}
			}
		}
	}
	
	private void refreshGUI(String id){
		int colID = -1;
		boolean bFound = false;
		
		if("".equals(id)){
			clearValues();
		}else{
			if(null!=dataTitles && null!=dataVals){
				for(int i=0; i<dataTitles.length; i++){
					if("id".equals(dataTitles[i])){
						colID = i;
						break;
					}
				}
				if(colID>=0){
					for(int i=0; i<dataVals.length; i++){
						if((""+dataVals[i][colID]).equals(id)){
							for(int j=0; j<dataTitles.length; j++){
								switch(dataTitles[j]){
								case "user_name":
									txtUserName.setText(""+dataVals[i][j]);
									break;
								case "user_workid":
									txtWorkId.setText(""+dataVals[i][j]);
									break;
								case "user_expirydate":
									txtExpiredDays.setText(""+dataVals[i][j]);
									break;
								case "user_authorities":
									String s = "";
									try {
										s = DesUtils.decrypt(""+dataVals[i][j]);
									} catch (Exception e) {
										s = "";
									}
									
									GUIUtils.setJListContent(listAuthorities, parseAuthorities(s));
									break;
								}
							}
							bFound = true;
							break;
						}
					}
				}
			}
			if(!bFound) clearValues();
		}
		
		GUIUtils.setJListSelectedIdx(listUsers, txtUserName.getText().trim());
	}
}
