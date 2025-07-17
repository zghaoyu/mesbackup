package com.cncmes.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.cncmes.base.MyTable;
import com.cncmes.base.MyTableModel;
import com.cncmes.base.PermissionItems;
import com.cncmes.dao.DAO;
import com.cncmes.dao.impl.DAOImpl;
import com.cncmes.data.SystemConfig;
import com.cncmes.utils.ClassUtils;
import com.cncmes.utils.DTOUtils;
import com.cncmes.utils.GUIUtils;
import com.cncmes.utils.LoginSystem;
import com.cncmes.utils.XmlUtils;

import java.awt.Toolkit;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import java.awt.GridBagLayout;
import javax.swing.JToolBar;
import java.awt.GridBagConstraints;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Insets;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JTree;
import javax.swing.border.BevelBorder;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;

public class CNCeSpec extends JFrame {
	private static final long serialVersionUID = 36L;
	private static CNCeSpec cncEspec = new CNCeSpec();
	private JPanel contentPane;
	private JTextField textFieldValue;
	private MyTable tableDatabase;
	private JLabel lblTitle;
	private JLabel lblRunningMsg;
	private JComboBox<String> comboBoxFieldName;
	private JButton btnAdd;
	private JButton btnDelete;
	private JButton btnSave;
	private JButton btnSysCfg;
	private JButton btnLogin;
	private JTree menuTree;
	
	private String curDtoClassName = "";
	private TreePath curMenuPath = null;
	private TreePath selPath = null;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CNCeSpec frame = getInstance();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static CNCeSpec getInstance(){
		return cncEspec;
	}
	
	/**
	 * Create the frame.
	 */
	private CNCeSpec() {
		XmlUtils.parseSystemConfig();
		setIconImage(Toolkit.getDefaultToolkit().getImage(CNCeSpec.class.getResource("/com/cncmes/img/Butterfly_purple_24.png")));
		setResizable(false);
		setTitle("CNC eSpec");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 800;
		int height = 600;
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(16 == e.getModifiers()){
					exitSystem(true);
				}
			}
		});
		mntmExit.setIcon(new ImageIcon(CNCeSpec.class.getResource("/com/cncmes/img/Exit_16.png")));
		mnFile.add(mntmExit);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mnHelp.add(mntmAbout);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panelTop = new JPanel();
		contentPane.add(panelTop, BorderLayout.NORTH);
		GridBagLayout gbl_panelTop = new GridBagLayout();
		gbl_panelTop.columnWidths = new int[] {200, 80, 100, 60, 240, 80};
		gbl_panelTop.rowHeights = new int[] {0};
		gbl_panelTop.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, 1.0, 0.0};
		gbl_panelTop.rowWeights = new double[]{0.0};
		panelTop.setLayout(gbl_panelTop);
		
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		GridBagConstraints gbc_toolBar = new GridBagConstraints();
		gbc_toolBar.insets = new Insets(0, 0, 0, 5);
		gbc_toolBar.gridx = 0;
		gbc_toolBar.gridy = 0;
		panelTop.add(toolBar, gbc_toolBar);
		
		btnLogin = new JButton("");
		btnLogin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(1 == e.getButton() && btnLogin.isEnabled()){
					if(LoginSystem.userHasLoginned()){
						if(0==JOptionPane.showConfirmDialog(cncEspec, "Are you sure of logging out now?", "Log Out?", JOptionPane.YES_NO_OPTION)){
							LoginSystem.userLogout();
							refreshButtonsEnabled();
						}
					}else{
						UserLogin dialog = UserLogin.getInstance();
						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dialog.setVisible(true);
					}
				}
			}
		});
		btnLogin.setBorderPainted(false);
		btnLogin.setIcon(new ImageIcon(CNCeSpec.class.getResource("/com/cncmes/img/login_24.png")));
		toolBar.add(btnLogin);
		
		btnSysCfg = new JButton("");
		btnSysCfg.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(btnSysCfg.isEnabled() && 1 == e.getButton()){
					try {
						SysConfig dialog = SysConfig.getInstance();
						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dialog.setVisible(true);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		btnSysCfg.setToolTipText("System Config");
		btnSysCfg.setBorderPainted(false);
		btnSysCfg.setIcon(new ImageIcon(CNCeSpec.class.getResource("/com/cncmes/img/setting_24.png")));
		toolBar.add(btnSysCfg);
		
		btnAdd = new JButton("");
		btnAdd.setToolTipText("Add new record");
		btnAdd.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(btnAdd.isEnabled() && 1 == e.getButton()){
					DTOUtils.setDataTable(tableDatabase,curDtoClassName,1,null,null);
				}
			}
		});
		btnAdd.setBorderPainted(false);
		btnAdd.setIcon(new ImageIcon(CNCeSpec.class.getResource("/com/cncmes/img/Add_24.png")));
		toolBar.add(btnAdd);
		
		btnDelete = new JButton("");
		btnDelete.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(btnDelete.isEnabled() && 1 == e.getButton()){
					int selRow = tableDatabase.getSelectedRow();
					if(selRow>=0 && null!=tableDatabase.getValueAt(selRow, 0)){
						if(0==JOptionPane.showConfirmDialog(contentPane, "Do you really want to delete this data?", "Delete Data", JOptionPane.YES_NO_OPTION)){
							DAO dao = new DAOImpl(curDtoClassName);
							int id = Integer.parseInt(""+tableDatabase.getValueAt(selRow, 0));
							try {
								dao.delete(id);
								DTOUtils.setDataTable(tableDatabase,curDtoClassName,0,null,null);
								setRunningMsg("Delete data(id="+id+") OK");
								btnDelete.setEnabled(false);
							} catch (SQLException e1) {
								setRunningMsg("Delete data failed:"+e1.getMessage());
							}
						}
					}
				}
			}
		});
		btnDelete.setToolTipText("Delete the selected record");
		btnDelete.setEnabled(false);
		btnDelete.setBorderPainted(false);
		btnDelete.setIcon(new ImageIcon(CNCeSpec.class.getResource("/com/cncmes/img/delete_24.png")));
		toolBar.add(btnDelete);
		
		btnSave = new JButton("");
		btnSave.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(btnSave.isEnabled() && 1 == e.getButton()){
					MyTableModel tm = (MyTableModel) tableDatabase.getModel();
					Object[][] data = tm.getChangedData();
					if(null!=data){
						try {
							DTOUtils.saveDataIntoDB(curDtoClassName, tm.getDataTitle(), data);
							DTOUtils.setDataTable(tableDatabase,curDtoClassName,0,null,null);
							setRunningMsg("Save changes OK");
							btnSave.setEnabled(false);
						} catch (SQLException e1) {
							setRunningMsg("Save changes failed:"+e1.getMessage());
						}
					}
				}
			}
		});
		btnSave.setToolTipText("Save changes");
		btnSave.setEnabled(false);
		btnSave.setBorderPainted(false);
		btnSave.setIcon(new ImageIcon(CNCeSpec.class.getResource("/com/cncmes/img/Save_24.png")));
		toolBar.add(btnSave);
		
		JButton btnExit = new JButton("");
		btnExit.setToolTipText("Exit from system");
		btnExit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(1 == e.getButton()){
					exitSystem(true);
				}
			}
		});
		btnExit.setBorderPainted(false);
		btnExit.setIcon(new ImageIcon(CNCeSpec.class.getResource("/com/cncmes/img/Exit_24.png")));
		toolBar.add(btnExit);
		
		JLabel lblFieldName = new JLabel("Field Name");
		GridBagConstraints gbc_lblFieldName = new GridBagConstraints();
		gbc_lblFieldName.insets = new Insets(0, 0, 0, 5);
		gbc_lblFieldName.gridx = 1;
		gbc_lblFieldName.gridy = 0;
		panelTop.add(lblFieldName, gbc_lblFieldName);
		
		comboBoxFieldName = new JComboBox<String>();
		GridBagConstraints gbc_comboBoxFiledName = new GridBagConstraints();
		gbc_comboBoxFiledName.insets = new Insets(0, 0, 0, 5);
		gbc_comboBoxFiledName.fill = GridBagConstraints.BOTH;
		gbc_comboBoxFiledName.gridx = 2;
		gbc_comboBoxFiledName.gridy = 0;
		panelTop.add(comboBoxFieldName, gbc_comboBoxFiledName);
		
		JComboBox<String> comboBoxOperator = new JComboBox<String>();
		GridBagConstraints gbc_comboBoxOperator = new GridBagConstraints();
		gbc_comboBoxOperator.insets = new Insets(0, 0, 0, 5);
		gbc_comboBoxOperator.fill = GridBagConstraints.BOTH;
		gbc_comboBoxOperator.gridx = 3;
		gbc_comboBoxOperator.gridy = 0;
		panelTop.add(comboBoxOperator, gbc_comboBoxOperator);
		
		textFieldValue = new JTextField();
		GridBagConstraints gbc_textFieldValue = new GridBagConstraints();
		gbc_textFieldValue.insets = new Insets(0, 0, 0, 5);
		gbc_textFieldValue.fill = GridBagConstraints.BOTH;
		gbc_textFieldValue.gridx = 4;
		gbc_textFieldValue.gridy = 0;
		panelTop.add(textFieldValue, gbc_textFieldValue);
		textFieldValue.setColumns(10);
		
		JButton btnSearch = new JButton("Search");
		btnSearch.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(btnSearch.isEnabled() && 1==e.getButton()){
					String paraName = comboBoxFieldName.getSelectedItem().toString();
					String paraVal = textFieldValue.getText().trim();
					if(!"".equals(paraVal)){
						DTOUtils.setDataTable(tableDatabase,curDtoClassName,0,new String[]{paraName},new String[]{paraVal});
						btnDelete.setEnabled(false);
						btnSave.setEnabled(false);
						setRunningMsg("Search OK");
					}else{
						setRunningMsg("Field value can't be blank while doing search");
					}
				}
			}
		});
		GridBagConstraints gbc_btnSearch = new GridBagConstraints();
		gbc_btnSearch.fill = GridBagConstraints.VERTICAL;
		gbc_btnSearch.gridx = 5;
		gbc_btnSearch.gridy = 0;
		panelTop.add(btnSearch, gbc_btnSearch);
		
		JPanel panelCenter = new JPanel();
		panelCenter.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		contentPane.add(panelCenter, BorderLayout.CENTER);
		panelCenter.setLayout(new BorderLayout(0, 0));
		
		JPanel panelTitle = new JPanel();
		panelCenter.add(panelTitle, BorderLayout.NORTH);
		GridBagLayout gbl_panelTitle = new GridBagLayout();
		gbl_panelTitle.columnWidths = new int[]{0, 0};
		gbl_panelTitle.rowHeights = new int[]{0, 0};
		gbl_panelTitle.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panelTitle.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panelTitle.setLayout(gbl_panelTitle);
		
		lblTitle = new JLabel("Line Name");
		GridBagConstraints gbc_lblTitle = new GridBagConstraints();
		gbc_lblTitle.gridx = 0;
		gbc_lblTitle.gridy = 0;
		panelTitle.add(lblTitle, gbc_lblTitle);
		
		JPanel panelContent = new JPanel();
		panelContent.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panelCenter.add(panelContent, BorderLayout.CENTER);
		GridBagLayout gbl_panelContent = new GridBagLayout();
		gbl_panelContent.columnWidths = new int[]{0, 0};
		gbl_panelContent.rowHeights = new int[]{0, 0};
		gbl_panelContent.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panelContent.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panelContent.setLayout(gbl_panelContent);
		
		JScrollPane scrollPaneDatabase = new JScrollPane();
		GridBagConstraints gbc_scrollPaneDatabase = new GridBagConstraints();
		gbc_scrollPaneDatabase.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneDatabase.gridx = 0;
		gbc_scrollPaneDatabase.gridy = 0;
		panelContent.add(scrollPaneDatabase, gbc_scrollPaneDatabase);
		
		tableDatabase = new MyTable();
		tableDatabase.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setButtonEnabled();
				if(2==e.getClickCount()){
					Object temp = tableDatabase.getValueAt(tableDatabase.getSelectedRow(), 0);
					String id = "";
					if(null!=temp) id=""+temp;
					if(accessIsDenied()){
						if(LoginSystem.userHasLoginned()) JOptionPane.showMessageDialog(contentPane, "You are not authorized yet to configure this item", "Access Denied", JOptionPane.INFORMATION_MESSAGE);
					}else{
						showSpecEditor(id);
					}
				}
			}
		});
		tableDatabase.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				setButtonEnabled();
			}
		});
		scrollPaneDatabase.setViewportView(tableDatabase);
		
		JPanel panelBottom = new JPanel();
		contentPane.add(panelBottom, BorderLayout.SOUTH);
		
		lblRunningMsg = new JLabel("Ready");
		panelBottom.add(lblRunningMsg);
		
		JPanel panelLeft = new JPanel();
		panelLeft.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panelCenter.add(panelLeft, BorderLayout.WEST);
		
		menuTree = new JTree();
		menuTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				TreePath menuPath = menuTree.getSelectionPath();
				if(null!=menuPath){
					curMenuPath = menuPath;
					SystemConfig sysCfg = SystemConfig.getInstance();
					LinkedHashMap<String, Object> menuMap = sysCfg.getData("MenuCfg");
					if(null!=menuMap && menuMap.size()>0){
						String subMenuName = curMenuPath.getLastPathComponent().toString();
						String menuName = curMenuPath.getParentPath().getLastPathComponent().toString();
						Object obj = menuMap.get(menuName);
						if(null!=obj){
							@SuppressWarnings("unchecked")
							LinkedHashMap<String, Object> subMenuMap = (LinkedHashMap<String, Object>) obj;
							if(null!=subMenuMap && subMenuMap.size()>0){
								if(null!=subMenuMap.get(subMenuName)){
									@SuppressWarnings("unchecked")
									LinkedHashMap<String, Object> subMap = (LinkedHashMap<String, Object>) subMenuMap.get(subMenuName);
									
									String dtoClassName = subMap.get("dtoHome")+"."+subMap.get("dtoClass");
									if(!curDtoClassName.equals(dtoClassName)){
										btnDelete.setEnabled(false);
										btnSave.setEnabled(false);
									}
									curDtoClassName = dtoClassName;
									setTitleText(lblTitle,subMenuName);
									DTOUtils.setDataTable(tableDatabase,curDtoClassName,0,null,null);
									GUIUtils.setComboBoxValues(comboBoxFieldName,getDtoFields(curDtoClassName));
								}
							}
						}
					}
				}
			}
		});
		menuTree.setVisibleRowCount(100);
		menuTree.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		panelLeft.add(menuTree);
		
		GUIUtils.setComboBoxValues(comboBoxOperator,new String[] {"=", "Like"});
		GUIUtils.setComboBoxValues(comboBoxFieldName,getDtoFields(curDtoClassName));
		refreshButtonsEnabled();
	}
	
	private void setMenuTree(JTree tree) {
		ArrayList<TreePath> paths = new ArrayList<TreePath>();
		selPath = null;
		
		tree.setModel(new DefaultTreeModel(
			new DefaultMutableTreeNode("eSpec Operation Menu") {
				private static final long serialVersionUID = 1L;
				{
					SystemConfig sysCfg = SystemConfig.getInstance();
					LinkedHashMap<String, Object> menuMap = sysCfg.getData("MenuCfg");
					if(null!=menuMap && menuMap.size()>0){
						DefaultMutableTreeNode node_1;
						for(String menuName:menuMap.keySet()){
							node_1 = new DefaultMutableTreeNode(menuName);
							
							@SuppressWarnings("unchecked")
							LinkedHashMap<String, Object> subMenuMap = (LinkedHashMap<String, Object>) menuMap.get(menuName);
							int i = -1;
							if(null!=subMenuMap && subMenuMap.size()>0){
								DefaultMutableTreeNode node_2;
								for(String subMenuName:subMenuMap.keySet()){
									@SuppressWarnings("unchecked")
									LinkedHashMap<String, Object> subMap = (LinkedHashMap<String, Object>) subMenuMap.get(subMenuName);
									if(null==subMap) continue;
									i++;
									if(0 == i){
										curDtoClassName = subMap.get("dtoHome")+"."+subMap.get("dtoClass");
										setTitleText(lblTitle,subMenuName);
									}
									node_2 = new DefaultMutableTreeNode(subMenuName);
									node_1.add(node_2);
									if(null!=curMenuPath){
										if(subMenuName.equals(curMenuPath.getLastPathComponent().toString()) && 
												menuName.equals(curMenuPath.getParentPath().getLastPathComponent().toString())){
											selPath = new TreePath(node_2.getPath());
										}
									}
								}
							}
							
							add(node_1);
							paths.add(new TreePath(node_1.getPath()));
						}
					}
				}
			}
		));
		
		if(paths.size()>0){
			for(TreePath tp:paths){
				tree.expandPath(tp);
			}
			if(null!=selPath){
				String subMenuName = selPath.getLastPathComponent().toString();
				tree.setSelectionPath(selPath);
				setTitleText(lblTitle,subMenuName);
			}else{
				DTOUtils.setDataTable(tableDatabase,curDtoClassName,0,null,null);
			}
		}
	}
	
	private void setTitleText(JLabel lblObj, String text){
		if(null!=lblObj){
			lblObj.setText("Configuration Item - "+text);
			lblObj.repaint();
		}
	}
	
	private void exitSystem(boolean force){
		if(0 == JOptionPane.showConfirmDialog(contentPane, "Are you sure of quitting from system?", "Quit From System?", JOptionPane.YES_NO_OPTION)){
			System.exit(0);
		}
	}
	
	private String[] getDtoFields(String dtoClassName){
		String[] fields = null;
		String temp = "";
		try {
			Map<String,Object> fieldsMap = DTOUtils.getDTOFields(dtoClassName);
			if(null!=fieldsMap && fieldsMap.size()>0){
				for(String key:fieldsMap.keySet()){
					if("".equals(temp)){
						temp = key;
					}else{
						temp += "," + key;
					}
				}
				fields = temp.split(",");
			}
		} catch (Exception e) {
		}
		
		return fields;
	}
	
	public boolean accessIsDenied(){
		SystemConfig sysConfig = SystemConfig.getInstance();
		PermissionItems permission = PermissionItems.DEVMONITORING;
		LinkedHashMap<String, Object> menuConfig = sysConfig.getMenuCfgByDtoClass(curDtoClassName);
		if(null!=menuConfig){
			permission = LoginSystem.getPermission(Integer.parseInt(""+menuConfig.get("rights")));
		}
		return LoginSystem.accessDenied(permission);
	}
	
	private void setTitle(){
		if(null==cncEspec) return;
		String title = cncEspec.getTitle();
		if(null!=title){
			title = title.split("##")[0] + "##Welcome " + LoginSystem.getUserName();
			cncEspec.setTitle(title);
		}
	}
	
	public void refreshButtonsEnabled(){
		setMenuTree(menuTree);
		btnSysCfg.setEnabled(!LoginSystem.accessDenied(PermissionItems.SYSCONFIG));
		btnAdd.setEnabled(!accessIsDenied());
		if(accessIsDenied()){
			if(btnDelete.isEnabled()) btnDelete.setEnabled(false);
			if(btnSave.isEnabled()) btnSave.setEnabled(false);
		}
		setTitle();
		if(LoginSystem.userHasLoginned()){
			btnLogin.setIcon(new ImageIcon(CNCeSpec.class.getResource("/com/cncmes/img/logout_24.png")));
			btnLogin.setToolTipText("Logout System");
		}else{
			btnLogin.setIcon(new ImageIcon(CNCeSpec.class.getResource("/com/cncmes/img/login_24.png")));
			btnLogin.setToolTipText("Login System");
		}
	}
	
	private void setButtonEnabled() {
		if(accessIsDenied()) return;
		MyTableModel tm = (MyTableModel) tableDatabase.getModel();
		if(tm.dataIsChanged()){
			btnSave.setEnabled(true);
		}else{
			btnSave.setEnabled(false);
		}
		
		int selRow= tableDatabase.getSelectedRow();
		if(selRow>=0 && null!=tm.getValueAt(selRow, 0)){
			btnDelete.setEnabled(true);
		}else{
			btnDelete.setEnabled(false);
		}
	}
	
	private void showSpecEditor(String id){
		SystemConfig sysCfg = SystemConfig.getInstance();
		LinkedHashMap<String, Object> menu = sysCfg.getMenuCfgByDtoClass(curDtoClassName);
		if(null!=menu){
			if(null!=menu.get("editor") && (""+menu.get("editor")).startsWith("com.")){
				Object editorObj = ClassUtils.getObjectByClassName(""+menu.get("editor"), "getInstance");
				if(null!=editorObj){
					boolean dialogModal = false;
					if(editorObj instanceof JDialog){
						JDialog dialog = (JDialog)editorObj;
						String title = dialog.getTitle();
						dialogModal = dialog.isModal();
						dialog.setTitle(title.split("##")[0]+"##"+id);
						dialog.setVisible(true);
					}else if(editorObj instanceof JFrame){
						((JFrame)editorObj).setVisible(true);
					}
					if(dialogModal){//Must refresh the data table after the configuration done
						DTOUtils.setDataTable(tableDatabase, curDtoClassName, 0, null, null);
					}
				}
			}
		}
	}
	
	private void setRunningMsg(String msg){
		lblRunningMsg.setText(msg);
	}
}