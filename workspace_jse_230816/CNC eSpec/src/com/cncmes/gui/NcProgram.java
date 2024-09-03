package com.cncmes.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.cncmes.base.MyTable;
import com.cncmes.base.MyTableModel;
import com.cncmes.base.PermissionItems;
import com.cncmes.dao.DAO;
import com.cncmes.dao.impl.DAOImpl;
import com.cncmes.data.SystemConfig;
import com.cncmes.utils.DTOUtils;
import com.cncmes.utils.GUIUtils;
import com.cncmes.utils.LoginSystem;
import com.cncmes.utils.MyFileUtils;
import com.cncmes.utils.NetUtils;
import com.cncmes.utils.TimeUtils;

import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.GridBagLayout;
import javax.swing.JToolBar;

import java.awt.GridBagConstraints;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.awt.Insets;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.border.BevelBorder;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTable;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.io.FileUtils;

//import org.apache.commons.io.FileUtils;

import javax.swing.event.ListSelectionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class NcProgram extends JDialog {
	private static final long serialVersionUID = 32L;
	private final JPanel contentPanel = new JPanel();
	private static NcProgram ncProgram = new NcProgram();
	private static CNCeSpec cncEspec = CNCeSpec.getInstance();
	private static String gDtoClassName = "com.cncmes.dto.CNCMachiningSpec";
	private static String nDtoClassName = "com.cncmes.dto.CNCNcProgram";// add by Hui Zhi 2021/1/12
	private JTextField txtDwgNo;
	private JTextField txtDescription;
	private JList<String> listDWG;
	private JComboBox<String> comboBoxProcQty;
	private JComboBox<String> comboBoxSpecType;
	private JButton btnSave;
	private JButton btnAddProc;
	private JButton btnDeleteProc;
	private JButton btnSysCfg;
	
	private Object[][] dtObject = null;
	private String[] dtFields = null;
	private String[] dwgNo = null;
	private String curID = "", lastDwgNo = "";
	private MyTable tableProcessCfg;
	private JLabel lblRunningmsg;
	private JTable tableRowHeader;
	
	public static NcProgram getInstance(){
		return ncProgram;
	}
	
	/**
	 * Create the dialog.
	 */
	private NcProgram() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				String title = ncProgram.getTitle();
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
					dwgNo = getDwgNOs();
					GUIUtils.setJListContent(listDWG,dwgNo);
					refreshSpec();
				}
			}
			@Override
			public void windowDeactivated(WindowEvent e) {
				String title = ncProgram.getTitle();
				if(null!=title && title.indexOf("##")>=0){
					title=title.split("##")[0];
					ncProgram.setTitle(title);
				}
			}
		});
		setResizable(false);
		setModal(true);
		setIconImage(Toolkit.getDefaultToolkit().getImage(NcProgram.class.getResource("/com/cncmes/img/ncProgram_user_24.png")));
		setTitle("NC Program Configuration");
		
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 800;
		int height = 600;
		setBounds((d.width - width) / 2, (d.height - height) / 2, width, height);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panelTop = new JPanel();
			contentPanel.add(panelTop, BorderLayout.NORTH);
			GridBagLayout gbl_panelTop = new GridBagLayout();
			gbl_panelTop.columnWidths = new int[] {200, 60, 440, 80};
			gbl_panelTop.rowHeights = new int[] {0};
			gbl_panelTop.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0};
			gbl_panelTop.rowWeights = new double[]{0.0};
			panelTop.setLayout(gbl_panelTop);
			{
				JToolBar toolBar = new JToolBar();
				toolBar.setFloatable(false);
				GridBagConstraints gbc_toolBar = new GridBagConstraints();
				gbc_toolBar.anchor = GridBagConstraints.WEST;
				gbc_toolBar.insets = new Insets(0, 0, 0, 5);
				gbc_toolBar.gridx = 0;
				gbc_toolBar.gridy = 0;
				panelTop.add(toolBar, gbc_toolBar);
				{
					btnSysCfg = new JButton("");
					btnSysCfg.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if(btnSysCfg.isEnabled() && 1==e.getButton()){
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
					btnSysCfg.setToolTipText("System Configuration");
					btnSysCfg.setBorderPainted(false);
					btnSysCfg.setIcon(new ImageIcon(NcProgram.class.getResource("/com/cncmes/img/setting_24.png")));
					toolBar.add(btnSysCfg);
				}
				{
					btnAddProc = new JButton("");
					btnAddProc.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if(btnAddProc.isEnabled() && 1==e.getButton()){
								String[] myTitle = null;
								int maxRow = 6;
								if(null!=tableProcessCfg){
									MyTableModel tm = (MyTableModel) tableProcessCfg.getModel();
									myTitle = tm.getDataTitle();
									if(tm.getColumnCount() < maxRow){
										DTOUtils.setDataTableEx(tableProcessCfg, gDtoClassName, 1, myTitle, null, tableRowHeader);
										setRunningMsg(lblRunningmsg,"Ready");
									}else{
										setRunningMsg(lblRunningmsg,"procNo can't exceed 6");
									}
								}
							}
						}
					});
					btnAddProc.setToolTipText("Add Process");
					btnAddProc.setBorderPainted(false);
					btnAddProc.setIcon(new ImageIcon(NcProgram.class.getResource("/com/cncmes/img/newFile_24.png")));
					toolBar.add(btnAddProc);
				}
				{
					btnDeleteProc = new JButton("");
					btnDeleteProc.setEnabled(false);
					btnDeleteProc.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if(btnDeleteProc.isEnabled() && 1==e.getButton()){
								if(!recordIsDeletable(0)){//Just in case, actually it's no need to check here
									setRunningMsg(lblRunningmsg,"There is no valid record to delete!");
									return;
								}
								
								int selRow = tableProcessCfg.getSelectedRow();
								int selCol = tableProcessCfg.getSelectedColumn();
								int rowCnt = tableProcessCfg.getRowCount();
								int colCnt = tableProcessCfg.getColumnCount();
								int delProcNo = 0;
								MyTableModel myTm = (MyTableModel) tableProcessCfg.getModel();
								if(myTm.isRowHeaderMode()){
									if(selCol!=(colCnt-1)){
										setRunningMsg(lblRunningmsg,"Please delete process#"+tableProcessCfg.getValueAt(0, colCnt-1)+" first!");
										return;
									}
									delProcNo = Integer.valueOf(""+tableProcessCfg.getValueAt(0, selCol));
								}else{
									if(selRow!=(rowCnt-1)){
										setRunningMsg(lblRunningmsg,"Please delete process#"+tableProcessCfg.getValueAt(rowCnt-1, 0)+" first!");
										return;
									}
									delProcNo = Integer.valueOf(""+tableProcessCfg.getValueAt(selRow, 0));
								}
								if(0==JOptionPane.showConfirmDialog(contentPanel, "Do you really want to delete setting of process#"+delProcNo+"?", "Delete Setting", JOptionPane.YES_NO_OPTION)){
									Object[][] myData = myTm.getCurrentData();
									Object[][] newData = new Object[myData.length-1][myData[0].length];
									int row = -1;
									for(int i=0; i<myData.length; i++){
										if(myTm.isRowHeaderMode() && selCol!=i || !myTm.isRowHeaderMode() && selRow!=i){
											row++;
											for(int j=0; j<myData[0].length; j++){
												newData[row][j] = myData[i][j];
											}
										}
									}
									
									String errMsg = "";
									ArrayList<Object> data = null;
									try {
										data = packChangedData(newData);
									} catch (RuntimeException e2) {
										data = null;
										setRunningMsg(lblRunningmsg, e2.getMessage());
										errMsg = "Save configuration failed.\r\n"+e2.getMessage();
									}
									if(null!=data){
										try {
											String[] tt = (String[]) data.get(0);
											Object[][] val = (Object[][]) data.get(1);
											if(DTOUtils.saveDataIntoDB(gDtoClassName, tt, val)){
												refreshSpec();
												setRunningMsg(lblRunningmsg,"Save configuration OK");
											}else{
												errMsg = "Update system database failed";
											}
										} catch (SQLException e1) {
											errMsg = "Save configuration failed.\r\n"+e1.getMessage();
										}
									}
									if(!"".equals(errMsg)){
										JOptionPane.showMessageDialog(contentPanel, errMsg, "Save Config Error", JOptionPane.ERROR_MESSAGE);
										setRunningMsg(lblRunningmsg, "Save configuration failed");
									}
								}
							}
						}
					});
					btnDeleteProc.setToolTipText("Delete Process");
					btnDeleteProc.setBorderPainted(false);
					btnDeleteProc.setIcon(new ImageIcon(NcProgram.class.getResource("/com/cncmes/img/deleteFile_24.png")));
					toolBar.add(btnDeleteProc);
				}
				{
					btnSave = new JButton("");
					btnSave.setEnabled(false);
					btnSave.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if(btnSave.isEnabled() && 1==e.getButton()){
								String errMsg = "";
								ArrayList<Object> data = null;
								try {
									data = packChangedData(null);
								} catch (RuntimeException e2) {
									data = null;
									errMsg = "Save configuration failed.\r\n"+e2.getMessage();
								}
								if(null!=data){
									try {
										String[] tt = (String[]) data.get(0);// Note£ºtt means table's title (Hui Zhi 2022/1/11)
										Object[][] val = (Object[][]) data.get(1);
										
										// add by Hui Zhi 2022/1/22 
										String[] tt2 = (String[]) data.get(2);
										Object val2 = data.get(3);
										
										if(DTOUtils.saveDataIntoDB(gDtoClassName, tt, val)){
											//save the code to database (add by Hui Zhi 2022/2/16)
											if (!saveNcProgramData(tt2, val2)) {
												errMsg = "Save Nc Program failed";
											}
											refreshSpec();
											setRunningMsg(lblRunningmsg,"Save configuration OK");
										}else{
											errMsg = "Update system database failed";
										}
										btnSave.setEnabled(false);
									} catch (SQLException e1) {
										errMsg = "Save configuration failed.\r\n"+e1.getMessage();
									}
								}
								if(!"".equals(errMsg)){
									JOptionPane.showMessageDialog(contentPanel, errMsg, "Save Config Error", JOptionPane.ERROR_MESSAGE);
									setRunningMsg(lblRunningmsg, "Save configuration failed");
								}
							}
						}
					});
					btnSave.setToolTipText("Save current spec");
					btnSave.setBorderPainted(false);
					btnSave.setIcon(new ImageIcon(NcProgram.class.getResource("/com/cncmes/img/Save_24.png")));
					toolBar.add(btnSave);
				}
				{
					JButton btnExit = new JButton("");
					btnExit.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if(1==e.getButton()){
								ncProgram.dispose();
							}
						}
					});
					btnExit.setToolTipText("Close the window");
					btnExit.setBorderPainted(false);
					btnExit.setIcon(new ImageIcon(NcProgram.class.getResource("/com/cncmes/img/Exit_24.png")));
					toolBar.add(btnExit);
				}
			}
			{
				JLabel lblDwgNo = new JLabel("DWG NO");
				GridBagConstraints gbc_lblDwgNo = new GridBagConstraints();
				gbc_lblDwgNo.insets = new Insets(0, 0, 0, 5);
				gbc_lblDwgNo.gridx = 1;
				gbc_lblDwgNo.gridy = 0;
				panelTop.add(lblDwgNo, gbc_lblDwgNo);
			}
			{
				txtDwgNo = new JTextField();
				GridBagConstraints gbc_txtDwgNo = new GridBagConstraints();
				gbc_txtDwgNo.insets = new Insets(0, 0, 0, 5);
				gbc_txtDwgNo.fill = GridBagConstraints.BOTH;
				gbc_txtDwgNo.gridx = 2;
				gbc_txtDwgNo.gridy = 0;
				panelTop.add(txtDwgNo, gbc_txtDwgNo);
				txtDwgNo.setColumns(10);
			}
			{
				JButton btnSearch = new JButton("Search/Add");
				btnSearch.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if(1==e.getButton()){
							String dwgno = txtDwgNo.getText().trim();
							if("".equals(dwgno)){
								setRunningMsg(lblRunningmsg,"Please input the DWG NO before doing search");
								return;
							}
							
							curID = "";
							int idx = getFieldIndex(dtFields,"dwgno");
							int idCol = getFieldIndex(dtFields,"id");
							if(idCol>=0 && idx>=0 && null!=dtObject && dtObject.length>0){
								for(int i=0; i<dtObject.length; i++){
									if(dwgno.equals(""+dtObject[i][idx])){
										curID = ""+dtObject[i][idCol];
										break;
									}
								}
							}
							
							refreshSpec();
						}
					}
				});
				GridBagConstraints gbc_btnSearch = new GridBagConstraints();
				gbc_btnSearch.fill = GridBagConstraints.VERTICAL;
				gbc_btnSearch.gridx = 3;
				gbc_btnSearch.gridy = 0;
				panelTop.add(btnSearch, gbc_btnSearch);
			}
		}
		{
			JPanel panelCenter = new JPanel();
			panelCenter.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
			contentPanel.add(panelCenter, BorderLayout.CENTER);
			GridBagLayout gbl_panelCenter = new GridBagLayout();
			gbl_panelCenter.columnWidths = new int[] {570};
			gbl_panelCenter.rowHeights = new int[] {30, 464};
			gbl_panelCenter.columnWeights = new double[]{0.0};
			gbl_panelCenter.rowWeights = new double[]{0.0, 0.0};
			panelCenter.setLayout(gbl_panelCenter);
			{
				JPanel panelT = new JPanel();
				GridBagConstraints gbc_panelT = new GridBagConstraints();
				gbc_panelT.fill = GridBagConstraints.HORIZONTAL;
				gbc_panelT.insets = new Insets(0, 0, 5, 0);
				gbc_panelT.gridx = 0;
				gbc_panelT.gridy = 0;
				panelCenter.add(panelT, gbc_panelT);
				GridBagLayout gbl_panelT = new GridBagLayout();
				gbl_panelT.columnWidths = new int[] {60, 500};
				gbl_panelT.rowHeights = new int[] {30};
				gbl_panelT.columnWeights = new double[]{0.0, 1.0};
				gbl_panelT.rowWeights = new double[]{0.0};
				panelT.setLayout(gbl_panelT);
				{
					JLabel lblDescription = new JLabel("Description");
					GridBagConstraints gbc_lblDescription = new GridBagConstraints();
					gbc_lblDescription.anchor = GridBagConstraints.EAST;
					gbc_lblDescription.insets = new Insets(0, 0, 0, 5);
					gbc_lblDescription.gridx = 0;
					gbc_lblDescription.gridy = 0;
					panelT.add(lblDescription, gbc_lblDescription);
				}
				{
					txtDescription = new JTextField();
					txtDescription.setToolTipText("Spec description");
					GridBagConstraints gbc_txtDescription = new GridBagConstraints();
					gbc_txtDescription.fill = GridBagConstraints.HORIZONTAL;
					gbc_txtDescription.gridx = 1;
					gbc_txtDescription.gridy = 0;
					panelT.add(txtDescription, gbc_txtDescription);
					txtDescription.setColumns(10);
				}
			}
			{
				JPanel panelC = new JPanel();
				GridBagConstraints gbc_panelC = new GridBagConstraints();
				gbc_panelC.fill = GridBagConstraints.BOTH;
				gbc_panelC.insets = new Insets(0, 0, 5, 0);
				gbc_panelC.gridx = 0;
				gbc_panelC.gridy = 1;
				panelCenter.add(panelC, gbc_panelC);
				panelC.setLayout(new BorderLayout(0, 0));
				{
					JPanel panelT1 = new JPanel();
					panelC.add(panelT1, BorderLayout.NORTH);
					GridBagLayout gbl_panelT1 = new GridBagLayout();
					gbl_panelT1.columnWidths = new int[] {140, 70, 70, 70, 210};
					gbl_panelT1.rowHeights = new int[] {0};
					gbl_panelT1.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
					gbl_panelT1.rowWeights = new double[]{0.0};
					panelT1.setLayout(gbl_panelT1);
					{
						JLabel lblProcQty = new JLabel("Machining Process Qty");
						GridBagConstraints gbc_lblProcQty = new GridBagConstraints();
						gbc_lblProcQty.insets = new Insets(0, 0, 0, 5);
						gbc_lblProcQty.gridx = 0;
						gbc_lblProcQty.gridy = 0;
						panelT1.add(lblProcQty, gbc_lblProcQty);
					}
					{
						comboBoxProcQty = new JComboBox<String>();
						comboBoxProcQty.setEnabled(false);
						comboBoxProcQty.setModel(new DefaultComboBoxModel<String>(new String[] {"1", "2", "3", "4", "5", "6"}));
						GridBagConstraints gbc_comboBoxProcQty = new GridBagConstraints();
						gbc_comboBoxProcQty.insets = new Insets(0, 0, 0, 5);
						gbc_comboBoxProcQty.fill = GridBagConstraints.HORIZONTAL;
						gbc_comboBoxProcQty.gridx = 1;
						gbc_comboBoxProcQty.gridy = 0;
						panelT1.add(comboBoxProcQty, gbc_comboBoxProcQty);
					}
					{
						JLabel lblSpecType = new JLabel("Spec Type");
						GridBagConstraints gbc_lblSpecType = new GridBagConstraints();
						gbc_lblSpecType.insets = new Insets(0, 0, 0, 5);
						gbc_lblSpecType.gridx = 2;
						gbc_lblSpecType.gridy = 0;
						panelT1.add(lblSpecType, gbc_lblSpecType);
					}
					{
						comboBoxSpecType = new JComboBox<String>();
						comboBoxSpecType.setEnabled(false);
						comboBoxSpecType.setModel(new DefaultComboBoxModel<String>(new String[] {"Eval", "MP"}));
						GridBagConstraints gbc_comboBoxSpecType = new GridBagConstraints();
						gbc_comboBoxSpecType.fill = GridBagConstraints.HORIZONTAL;
						gbc_comboBoxSpecType.gridx = 3;
						gbc_comboBoxSpecType.gridy = 0;
						panelT1.add(comboBoxSpecType, gbc_comboBoxSpecType);
					}
				}
				{
					JPanel panelC1 = new JPanel();
					panelC.add(panelC1, BorderLayout.CENTER);
					GridBagLayout gbl_panelC1 = new GridBagLayout();
					gbl_panelC1.columnWidths = new int[]{0, 0};
					gbl_panelC1.rowHeights = new int[]{0, 0};
					gbl_panelC1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
					gbl_panelC1.rowWeights = new double[]{1.0, Double.MIN_VALUE};
					panelC1.setLayout(gbl_panelC1);
					{
						JScrollPane scrollPaneProcessCfg = new JScrollPane();
						GridBagConstraints gbc_scrollPaneProcessCfg = new GridBagConstraints();
						gbc_scrollPaneProcessCfg.fill = GridBagConstraints.BOTH;
						gbc_scrollPaneProcessCfg.gridx = 0;
						gbc_scrollPaneProcessCfg.gridy = 0;
						panelC1.add(scrollPaneProcessCfg, gbc_scrollPaneProcessCfg);
						{
							tableProcessCfg = new MyTable();
							tableProcessCfg.addKeyListener(new KeyAdapter() {
								@Override
								public void keyReleased(KeyEvent e) {
									setButtonEnabled();
								}
							});
							tableProcessCfg.addMouseListener(new MouseAdapter() {
								@Override
								public void mouseClicked(MouseEvent e) {
									setButtonEnabled();
									if(2==e.getClickCount()){
										int selRow = tableProcessCfg.getSelectedRow();
										int selCol = tableProcessCfg.getSelectedColumn();
										String colName = (String) tableRowHeader.getModel().getValueAt(selRow,0);
										if(colName.startsWith("ncProgram_")){
											String fileAbsPath = MyFileUtils.chooseFile(contentPanel, colName.replace("ncProgram_", "")+" NC Program File Selection", "Select NC Program File");
											if(!"".equals(fileAbsPath)) tableProcessCfg.setValueAt(fileAbsPath, selRow, selCol);
										}
									}
								}
							});
							scrollPaneProcessCfg.setViewportView(tableProcessCfg);
						}
						{
							tableRowHeader = new JTable();
							scrollPaneProcessCfg.setRowHeaderView(tableRowHeader);
						}
					}
				}
			}
		}
		{
			JPanel panelWest = new JPanel();
			panelWest.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
			contentPanel.add(panelWest, BorderLayout.WEST);
			GridBagLayout gbl_panelWest = new GridBagLayout();
			gbl_panelWest.columnWidths = new int[] {200};
			gbl_panelWest.rowHeights = new int[]{0, 0, 0};
			gbl_panelWest.columnWeights = new double[]{1.0};
			gbl_panelWest.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			panelWest.setLayout(gbl_panelWest);
			{
				JLabel lblAllDwg = new JLabel("All DWG");
				GridBagConstraints gbc_lblAllDwg = new GridBagConstraints();
				gbc_lblAllDwg.insets = new Insets(0, 0, 5, 0);
				gbc_lblAllDwg.gridx = 0;
				gbc_lblAllDwg.gridy = 0;
				panelWest.add(lblAllDwg, gbc_lblAllDwg);
			}
			{
				JScrollPane scrollPaneDWG = new JScrollPane();
				GridBagConstraints gbc_scrollPaneDWG = new GridBagConstraints();
				gbc_scrollPaneDWG.fill = GridBagConstraints.BOTH;
				gbc_scrollPaneDWG.gridx = 0;
				gbc_scrollPaneDWG.gridy = 1;
				panelWest.add(scrollPaneDWG, gbc_scrollPaneDWG);
				{
					listDWG = new JList<String>();
					listDWG.addListSelectionListener(new ListSelectionListener() {
						public void valueChanged(ListSelectionEvent e) {
							String val = listDWG.getSelectedValue();
							if(null==val) return;
							
							curID = "";
							if(!"".equals(val)){
								int idx = getFieldIndex(dtFields,"dwgno");
								int idCol = getFieldIndex(dtFields,"id");
								if(idCol>=0 && idx>=0 && null!=dtObject && dtObject.length>0){
									for(int i=0; i<dtObject.length; i++){
										if(val.equals(""+dtObject[i][idx])){
											curID = ""+dtObject[i][idCol];
											break;
										}
									}
								}
							}
							if(!lastDwgNo.equals(val)){
								refreshSpec();
								lastDwgNo = val;
							}
						}
					});
					listDWG.setModel(new AbstractListModel<String>() {
						private static final long serialVersionUID = 1L;
						String[] values = new String[] {"AC4C07600518000Z"};
						public int getSize() {
							return values.length;
						}
						public String getElementAt(int index) {
							return values[index];
						}
					});
					scrollPaneDWG.setViewportView(listDWG);
				}
			}
		}
		{
			JPanel panelBottom = new JPanel();
			contentPanel.add(panelBottom, BorderLayout.SOUTH);
			{
				lblRunningmsg = new JLabel("Ready");
				panelBottom.add(lblRunningmsg);
			}
		}
	}
	
	private void setButtonEnabled() {
		if(cncEspec.accessIsDenied()) return;
		if(null!=comboBoxSpecType && "MP".equals(comboBoxSpecType.getSelectedItem().toString())){
			setRunningMsg(lblRunningmsg,"MP Spec can't be modified");
			btnSave.setEnabled(false);
		}else{
			MyTableModel tm = (MyTableModel) tableProcessCfg.getModel();
			if(tm.dataIsChanged()){
				btnSave.setEnabled(true);
			}else{
				btnSave.setEnabled(false);
			}
		}
		
		btnDeleteProc.setEnabled(recordIsDeletable(0));
	}
	
	private void setRunningMsg(JLabel lblObj,String msg){
		if(null!=lblObj){
			lblObj.setText(msg);
			lblObj.repaint();
		}
	}
	
	private String[] getDwgNOs() {
		String dwgs = "";
		int idx = -1;
		ArrayList<Object> data = DTOUtils.getDataFromDB(gDtoClassName, null, null);
		dtFields = (String[]) data.get(0);
		dtObject = (Object[][]) data.get(1);
		if(null!=dtObject && dtObject.length>0){
			idx = getFieldIndex(dtFields,"dwgno");
			if(idx>=0){
				for(int i=0; i<dtObject.length; i++){
					if("".equals(dwgs)){
						dwgs = "" + dtObject[i][idx];
					}else{
						dwgs += "," + dtObject[i][idx];
					}
				}
			}
		}
		dwgNo = dwgs.split(",");
		return dwgNo;
	}
	
	private int getFieldIndex(String[] fields,String fieldName){
		int idx = -1;
		
		if(null!=fields && fields.length>0){
			for(int i=0; i<fields.length; i++){
				if(fields[i].equals(fieldName)){
					idx = i;
					break;
				}
			}
		}
		
		return idx;
	}
	
	private boolean recordIsDeletable(int chkColIndex){
		boolean isDeletable = false;
		int notNullQty = 0;
		if(null!=tableProcessCfg && tableProcessCfg.getSelectedRow()>=0){
			int selRow = tableProcessCfg.getSelectedRow();
			int selCol = tableProcessCfg.getSelectedColumn();
			MyTableModel myTm = (MyTableModel) tableProcessCfg.getModel();
			if(myTm.isRowHeaderMode() && selCol>=0 || !myTm.isRowHeaderMode() && selRow>=0){
				Object[][] myData = myTm.getCurrentData();
				if(null!=myData && myData.length>1 && myData[0].length>chkColIndex){
					Object dt1 = null, dt2 = null;
					if(myTm.isRowHeaderMode()){
						dt1 = myData[chkColIndex][selCol];
					}else{
						dt1 = myData[selRow][chkColIndex];
					}
					if(null!=dt1){
						for(int i=0; i<myData.length; i++){
							if(myTm.isRowHeaderMode()){
								dt2 = myData[chkColIndex][i];
							}else{
								dt2 = myData[i][chkColIndex];
							}
							if(null!=dt2){
								notNullQty++;
								if(notNullQty>1){
									isDeletable = true;
									break;
								}
							}
						}
					}
				}
			}
		}
		return isDeletable;
	}
	/**
	 * 
	 * 2022/2/16 created by Hui Zhi
	 * @param fields the table's fields
	 * @param data the table's value
	 * @return true if save to database successfully
	 * @throws SQLException
	 */
	private boolean saveNcProgramData(String[] fields, Object data) throws SQLException {
		if (null == fields || null == data) {
			return false;
		}
		// change Object to List
		List<Object[]> list = new ArrayList<>();
		if (data instanceof ArrayList<?>) {
			for (Object o : (List<?>) data) {
				list.add(Object[].class.cast(o));
			}
		}
		// change list<Object[]> to Object[][]
		Object[][] val = new Object[list.size()][fields.length];
		Object[] ncProgramData = null;
		for (int i = 0; i < list.size(); i++) {
			ncProgramData = list.get(i);
			for (int j = 0; j < ncProgramData.length; j++) {
				val[i][j] = ncProgramData[j];
			}
		}

		if (DTOUtils.saveDataIntoDB(nDtoClassName, fields, val)) {
			return true;
		}
		
		return false;
	}

	private ArrayList<Object> packChangedData(Object[][] beSavedData) throws RuntimeException{
		ArrayList<Object> dtObj = null;
		
		MyTableModel tm = (MyTableModel) tableProcessCfg.getModel();
		String[] title = tm.getDataTitle();
		Object[][] data = null;
		if(null!=beSavedData){
			data = beSavedData;
		}else{
			data = tm.getCurrentData();
		}
		if(null!=title && null!=data){
			DAO dao = new DAOImpl(gDtoClassName);
			String[] fields = dao.getDataFields();//id is the first element
			Object[][] packData = null;
			
			
			// add by Hui Zhi 2022/2/15 
			// save to the table "cnc_ncprogram"
			DAO dao2 = new DAOImpl(nDtoClassName);
			String[] ncProgramFields =dao2.getDataFields();
			Object[] ncProgramData = null;
			List<Object[]> list = null;
			// end
			
			if(title.length>0 && data.length>0 
					&& title.length==data[0].length
					&& null!=fields && fields.length>0){
				//Re-pack the changed data
				packData = new Object[1][fields.length];
				
				if(!"".equals(curID)) packData[0][getFieldIndex(fields,"id")] = Integer.parseInt(curID);
				
				String dwg = txtDwgNo.getText().trim();
				String des = txtDescription.getText().trim();
				String specType = comboBoxSpecType.getSelectedItem().toString();
				if("".equals(dwg) || "".equals(des)){
					setRunningMsg(lblRunningmsg,"DWG NO and Description can't be blank");
					return null;
				}
				packData[0][getFieldIndex(fields,"dwgno")] = dwg;
				packData[0][getFieldIndex(fields,"description")] = des;
				packData[0][getFieldIndex(fields,"spec_type")] = specType;
				
				if(data.length<6){
					int startIdx = data.length + 1;
					for(int i=startIdx; i<=6; i++){
						packData[0][getFieldIndex(fields,"proc"+i+"_name")] = "";
						packData[0][getFieldIndex(fields,"proc"+i+"_ncmodel")] = "";
						packData[0][getFieldIndex(fields,"proc"+i+"_program")] = "";
						packData[0][getFieldIndex(fields,"proc"+i+"_simtime")] = "";
						packData[0][getFieldIndex(fields,"proc"+i+"_surface")] = 0;
					}
				}
				
				//Deal with setting of each process
				String procName = "", ncModel = "", ncProgram = "", ncSimTime = "";
				int surface = 0;
				for(int i=1; i<=data.length; i++){
					procName = (String) data[i-1][getFieldIndex(title,"procName")];
					surface = Integer.parseInt(""+data[i-1][getFieldIndex(title,"surface")]);
					
					ncModel = ""; ncProgram = ""; ncSimTime = "";
					for(int j=0; j<title.length; j++){
						if(title[j].startsWith("ncProgram_")){
							if("".equals(ncModel)){
								ncModel = title[j].replace("ncProgram_", "");
								ncProgram = (null==data[i-1][j]?" ":""+data[i-1][j]);
							}else{
								ncModel += "," + title[j].replace("ncProgram_", "");
								ncProgram += "," + (null==data[i-1][j]?" ":""+data[i-1][j]);
							}
						}
						if(title[j].startsWith("ncSimTime_")){
							if("".equals(ncSimTime)){
								ncSimTime = (null==data[i-1][j]?" ":""+data[i-1][j]);
							}else{
								ncSimTime += "," + (null==data[i-1][j]?" ":""+data[i-1][j]);
							}
						}
					}
					
					if(!"".equals(ncProgram) && !"".equals(ncSimTime)){
						String[] models = ncModel.split(",");
						String[] programs = ncProgram.split(",");
						String[] simTimes = ncSimTime.split(",");
						ncModel = ""; ncProgram = ""; ncSimTime = "";
						for(int j=0; j<programs.length; j++){
							if(!"".equals(programs[j].trim()) && !"".equals(simTimes[j].trim())){
								if("".equals(ncModel)){
									ncModel = models[j].trim();
									ncProgram = programs[j].trim();
									ncSimTime = simTimes[j].trim();
								}else{
									ncModel += "," + models[j].trim();
									ncProgram += "," + programs[j].trim();
									ncSimTime += "," + simTimes[j].trim();
								}
							}
						}
					}
					if("".equals(ncModel) || "".equals(ncProgram) || "".equals(ncSimTime)) surface = 0;
					
					packData[0][getFieldIndex(fields,"proc"+i+"_name")] = procName;
					packData[0][getFieldIndex(fields,"proc"+i+"_ncmodel")] = ncModel;
					packData[0][getFieldIndex(fields,"proc"+i+"_program")] = ncProgram;
					packData[0][getFieldIndex(fields,"proc"+i+"_simtime")] = ncSimTime;
					packData[0][getFieldIndex(fields,"proc"+i+"_surface")] = surface;
					
					if(surface>0){
						SystemConfig sysConfig = SystemConfig.getInstance();
						LinkedHashMap<String, Object> commonCfg = sysConfig.getCommonCfg();
						String ncScriptDir = (String) commonCfg.get("NCProgramsRootDir");
						
						String[] models = ncModel.split(",");
						String[] programs = ncProgram.split(",");
						String svrFolderPath = "", svrFileName = "";
						for(int j=0; j<programs.length; j++){
							if(MyFileUtils.fileExists(programs[j])){
								svrFolderPath = ncScriptDir + File.separator + dwg + File.separator + models[j];
								MyFileUtils.makeFolder(svrFolderPath);
								svrFileName = i+"_"+new File(programs[j]).getName();
								try {
									if(MyFileUtils.copyFile(programs[j], svrFolderPath + File.separator + svrFileName)){
										programs[j] = svrFileName;
										
										// add by Hui Zhi 2022/2/16 
										File file = new File(svrFolderPath + File.separator + svrFileName);
										String code = FileUtils.readFileToString(file, "UTF-8");
										ncProgramData = new Object[ncProgramFields.length];
										ncProgramData[getFieldIndex(ncProgramFields, "proc_program")] = code;
										ncProgramData[getFieldIndex(ncProgramFields, "file_name")] = svrFileName;
										ncProgramData[getFieldIndex(ncProgramFields, "cnc_model")] = models[j]; 
										ncProgramData[getFieldIndex(ncProgramFields, "proc_no")] = i;
										ncProgramData[getFieldIndex(ncProgramFields, "dwgno")] = dwg;
										ncProgramData[getFieldIndex(ncProgramFields, "rev")] = "A";// need to modify
										ncProgramData[getFieldIndex(ncProgramFields, "ip")] = NetUtils.getLocalIP();
										ncProgramData[getFieldIndex(ncProgramFields, "pc_name")] = NetUtils.getLocalHostName();
										ncProgramData[getFieldIndex(ncProgramFields, "user_id")] = LoginSystem.getUserId();
										ncProgramData[getFieldIndex(ncProgramFields, "upload_date")] = TimeUtils.getCurrentDate("yyyy-MM-dd HH:mm:ss");
										
										if (null == list){
											list = new ArrayList<>();
										}
										list.add(ncProgramData);
										// end by Hui Zhi
									}
								} catch (IOException e) {
									throw new RuntimeException("Upload "+programs[j]+" Failed:"+e.getMessage());
								} 
							}
						}
						
						ncProgram = "";
						for(int j=0; j<programs.length; j++){
							if("".equals(ncProgram)){
								ncProgram = programs[j];
							}else{
								ncProgram += "," + programs[j];
							}
						}
						packData[0][getFieldIndex(fields, "proc" + i + "_program")] = ncProgram;
					}
				}
				
				dtObj = new ArrayList<Object>();
				dtObj.add(0, fields);
				dtObj.add(1, packData);
				dtObj.add(2, ncProgramFields);//add by Hui Zhi
				dtObj.add(3, list);//add by Hui Zhi 2022/2/16
			}
		}
		
		return dtObj;
	}
	
	private void refreshTitle(){
		String title = ncProgram.getTitle();
		if(null!=title && !"".equals(curID)){
			if(title.indexOf("##")>=0){
				if(title.endsWith("##")){
					ncProgram.setTitle(title+curID);
				}else{
					ncProgram.setTitle(title.split("##")[0]+"##"+curID);
				}
			}
		}
	}
	
	private void refreshButtonsEnabled(){
		btnSysCfg.setEnabled(!LoginSystem.accessDenied(PermissionItems.SYSCONFIG));
		btnAddProc.setEnabled(!cncEspec.accessIsDenied());
		if(cncEspec.accessIsDenied()){
			if(btnDeleteProc.isEnabled()) btnDeleteProc.setEnabled(false);
			if(btnSave.isEnabled()) btnSave.setEnabled(false);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void refreshSpec(){
		//Get data from database
		setRunningMsg(lblRunningmsg,"Ready");
		ArrayList<Object> dt = DTOUtils.getDataFromDB(gDtoClassName, new String[]{"id"}, new String[]{curID});
		dtFields = (String[]) dt.get(0);
		Object[] data = null;
		if(null!=dt.get(1)){
			Object[][] tmp = (Object[][]) dt.get(1);
			data = tmp[0];
		}
		
		//Check whether current spec is newly added or not
		if("".equals(curID)){
			String tmpDwgNo = txtDwgNo.getText().trim();
			if(!"".equals(tmpDwgNo)){
				dt = DTOUtils.getDataFromDB(gDtoClassName, new String[]{"dwgno"}, new String[]{tmpDwgNo});
				dtFields = (String[]) dt.get(0);
				if(null!=dt.get(1)){
					Object[][] tmp = (Object[][]) dt.get(1);
					data = tmp[0];
				}
			}
		}
		
		//Get all registered CNC models
		String[] cncModels = null;
		SystemConfig sysCfg = SystemConfig.getInstance();
		LinkedHashMap<String, Object> renderCfg = sysCfg.getData("RenderCfg");
		if(null!=renderCfg){
			LinkedHashMap<String, Object> renderMap = (LinkedHashMap<String, Object>) renderCfg.get("cncModels");
			if(null!=renderMap) cncModels = (String[]) renderMap.get("listVals");
		}
		
		//Generate titles of new data
		String titles = "procNo,procName,surface";
		if(null!=cncModels && cncModels.length>0){
			for(String cncModel:cncModels){
				titles += "," + "ncProgram_" + cncModel;
			}
			for(String cncModel:cncModels){
				titles += "," + "ncSimTime_" + cncModel;
			}
		}
		String[] newTitle = titles.split(",");
		
		if(null==data){
			DTOUtils.setDataTableEx(tableProcessCfg, gDtoClassName, 0, newTitle, new Object[1][newTitle.length], tableRowHeader);
			comboBoxProcQty.setSelectedIndex(0);
			comboBoxProcQty.repaint();
			btnSave.setEnabled(false);
			btnDeleteProc.setEnabled(false);
			return;
		}
		
		//Refresh DWG NO and spec description
		String dwgno = ""+data[getFieldIndex(dtFields,"dwgno")];
		txtDwgNo.setText(dwgno);
		txtDescription.setText(""+data[getFieldIndex(dtFields,"description")]);
		GUIUtils.setJListSelectedIdx(listDWG,txtDwgNo.getText().trim());
		GUIUtils.setComboBoxSelectedIdx(comboBoxSpecType,""+data[getFieldIndex(dtFields,"spec_type")]);
		Object id = data[getFieldIndex(dtFields,"id")];
		if(null!=id) curID = ""+id;
		
		//Generate the new data
		Object[][] newData = null;
		String strData = "", ncModels = "", ncSimTime = "", ncProgram = "";
		String tmp1 = "", tmp2 = "";
		String[] models = null, simTimes = null, programs = null;
		boolean bFound = false;
		int procQty = 0, iFound = 0;
		for(int i=1; i<=6; i++){
			if(Integer.parseInt(""+data[getFieldIndex(dtFields,"proc"+i+"_surface")])>0){
				procQty++;
				tmp1 = ""; tmp2 = "";
				if(null!=cncModels && cncModels.length>0){
					ncModels = (String) data[getFieldIndex(dtFields,"proc"+i+"_ncmodel")];
					ncSimTime = (String) data[getFieldIndex(dtFields,"proc"+i+"_simtime")];
					ncProgram = (String) data[getFieldIndex(dtFields,"proc"+i+"_program")];
					if(null!=ncModels && null!=ncSimTime && null!=ncProgram){
						models = ncModels.split(",");
						simTimes = ncSimTime.split(",");
						programs = ncProgram.split(",");
						for(String model:cncModels){
							bFound = false;
							for(int j=0; j<models.length; j++){
								if(model.equals(models[j])){
									if(programs[j].startsWith(i+"_")){
										bFound = true;
										iFound = j;
									}
									break;
								}
							}
							if(bFound){
								tmp1 += "##" + programs[iFound];
								tmp2 += "##" + simTimes[iFound];
							}else{
								tmp1 += "## ";
								tmp2 += "## ";
							}
						}
					}
				}
				
				if("".equals(strData)){
					strData = "" + i 
							+ "##" + data[getFieldIndex(dtFields,"proc"+i+"_name")]
							+ "##" + data[getFieldIndex(dtFields,"proc"+i+"_surface")]
							+ tmp1 + tmp2;
				}else{
					strData += "\r\n" + i 
							+ "##" + data[getFieldIndex(dtFields,"proc"+i+"_name")]
							+ "##" + data[getFieldIndex(dtFields,"proc"+i+"_surface")]
							+ tmp1 + tmp2;
				}
			}else{
				break;
			}
		}
		if(!"".equals(strData)){
			String[] firstD = strData.split("\r\n");
			newData = new Object[firstD.length][newTitle.length];
			for(int i=0; i<firstD.length; i++){
				newData[i] = firstD[i].split("##");
			}
		}else{
			newData = new Object[1][newTitle.length];
		}
		
		//Refresh the data table
		DTOUtils.setDataTableEx(tableProcessCfg, gDtoClassName, 0, newTitle, newData, tableRowHeader);
		GUIUtils.setComboBoxSelectedIdx(comboBoxProcQty,procQty>0?""+procQty:"1");
		refreshTitle();
		refreshButtonsEnabled();
		btnSave.setEnabled(false);
		btnDeleteProc.setEnabled(false);
	}
}